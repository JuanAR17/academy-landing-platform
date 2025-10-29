package com.academia.backend.service;

import com.academia.backend.domain.UserEntity;
import com.academia.backend.domain.EpaycoTransactionEntity;
import com.academia.backend.domain.RoleEntity;
import com.academia.backend.dto.PsePaymentOut;
import com.academia.backend.dto.in.CardPaymentIn;
import com.academia.backend.dto.in.ConfirmPseIn;
import com.academia.backend.dto.in.PsePaymentIn;
import com.academia.backend.repo.UserRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.academia.backend.repo.EpaycoTransactionRepo;
import com.academia.backend.repo.RoleRepo;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;
import java.util.Optional;




@Service
public class CheckoutUserService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    private final EpaycoClient epaycoClient;
    private final IpResolver ipResolver;        // ya lo tienes en /service
    private final ObjectMapper om;
    private final EpaycoTransactionRepo trxRepo; // si lo tienes


    public CheckoutUserService(
        UserRepo userRepo,
        RoleRepo roleRepo,
        PasswordEncoder passwordEncoder,
        IpResolver ipResolver,
        EpaycoClient epaycoClient,           // <- nuevo: ya lo estabas usando
        EpaycoTransactionRepo trxRepo,       // <- nuevo: ya lo estabas usando
        ObjectMapper om                      // <- opcional: si no lo tienes como @Bean puedes new ObjectMapper()
    ) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.ipResolver = ipResolver;
        this.epaycoClient = epaycoClient;     // <--- ya NO queda en null
        this.trxRepo = trxRepo;               // <--- ya NO queda en null
        this.om = (om != null) ? om : new ObjectMapper();
    }

    /** Devuelve un userId válido: reutiliza por email o crea uno nuevo. */
    @Transactional
    public UUID ensureUserForCheckout(CardPaymentIn in) {
        String email = normalize(in.getEmail());
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email es obligatorio para el checkout");
        }
        return userRepo.findByEmailIgnoreCase(email)
                .map(UserEntity::getId)
                .orElseGet(() -> createUser(in, email));
    }

    @Transactional
    protected UUID createUser(CardPaymentIn in, String email) {
        var u = new UserEntity();
        u.setEmail(email);

        // username por defecto = parte local del email
        String defaultUsername = email.split("@", 2)[0];
        if (u.getUsername() == null || u.getUsername().isBlank()) {
            u.setUsername(defaultUsername);
        }

        // Campos “best effort” por reflexión (no rompe si no existen)
        safeSet(u, "setFirstName", in.getName() != null ? in.getName() : defaultUsername);
        safeSet(u, "setLastName",  in.getLastName());
        safeSet(u, "setPhone",     in.getCellPhone());   // si tu setter se llama setPhone
        safeSet(u, "setCellPhone", in.getCellPhone());   // alternativa
        safeSetBool(u, "setActive", true);
        safeSetBool(u, "setEnabled", true);              // por si tu entidad usa enabled

        // Password aleatoria (el usuario podrá cambiar luego)
        u.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));

        // >>> asigna el rol USER sin acoplarse al modelo (ANTES de guardar)
        assignDefaultRole(u);

        try {
            return userRepo.save(u).getId();
        } catch (DataIntegrityViolationException dup) {
            // carrera por UNIQUE(email) -> reintenta leyendo
            return userRepo.findByEmailIgnoreCase(email).orElseThrow(() -> dup).getId();
        }
    }

    // ---------- helpers ----------
    private static String normalize(String email) {
        if (email == null) return null;
        email = email.trim();
        return email.isEmpty() ? null : email.toLowerCase(Locale.ROOT);
    }

    private static void safeSet(Object target, String method, String value) {
        if (value == null) return;
        try {
            Method m = target.getClass().getMethod(method, String.class);
            m.invoke(target, value);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception ignored) {
        }
    }

    private static void safeSetBool(Object target, String method, boolean value) {
        try {
            Method m1 = target.getClass().getMethod(method, boolean.class);
            m1.invoke(target, value);
            return;
        } catch (NoSuchMethodException ignored) {
        } catch (Exception ignored) {
        }
        try {
            Method m2 = target.getClass().getMethod(method, Boolean.class);
            m2.invoke(target, value);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception ignored) {
        }
    }

    // === Rol por defecto: USER sin acoplarse al modelo (ampliado para roleEntity) ===
    private void assignDefaultRole(UserEntity u) {
        var role = roleRepo.findByNameIgnoreCase("USER")
            .orElseThrow(() -> new IllegalStateException("Falta el rol USER (ejecuta V10_seed_role_user.sql)"));

        Long roleId = role.getId();

        // 1) Setters (todas las variantes típicas)
        if (tryInvoke(u, "setRoleEntity", role.getClass(), role)) return; // <-- TU CASO
        if (tryInvoke(u, "setRoleId", Long.class, roleId)) return;
        if (tryInvoke(u, "setRole", role.getClass(), role)) return;
        if (tryInvoke(u, "setRole", String.class, role.getName())) return;

        // 2) Campos directos (incluye roleEntity)
        if (trySetField(u, "roleEntity", role)) return;  // <-- TU CASO
        if (trySetField(u, "roleId", roleId)) return;
        if (trySetField(u, "role_id", roleId)) return;
        if (trySetField(u, "role", role)) return;
        if (trySetField(u, "role", role.getName())) return;

        // 3) Colecciones frecuentes
        if (tryAddToCollectionField(u, "roles", role)) return;
        if (tryAddToCollectionField(u, "authorities", role)) return;

        throw new IllegalStateException(
            "UserEntity no expone setRoleEntity/ setRoleId/ setRole/ addRole ni campos roleEntity/roleId/role/role_id/roles/authorities para asignar."
        );
    }


    /** Invoca un setter si existe. */
    private static boolean tryInvoke(Object target, String method, Class<?> argType, Object value) {
        try {
            Method m = target.getClass().getMethod(method, argType);
            m.invoke(target, value);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Asigna un campo privado si existe (conversión básica de tipos). */
    private static boolean trySetField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);

            Class<?> ft = f.getType();
            Object toSet = value;

            if (value != null && !ft.isAssignableFrom(value.getClass())) {
                if ((ft == Long.class || ft == long.class) && value instanceof Number n) {
                    if (ft == long.class) { f.setLong(target, n.longValue()); return true; }
                    toSet = n.longValue();
                } else if (ft == String.class) {
                    toSet = String.valueOf(value);
                } else {
                    return false; // tipo incompatible
                }
            }

            f.set(target, toSet);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Añade a un campo colección si existe (ej. Set<RoleEntity> roles). */
    @SuppressWarnings("unchecked")
    private static boolean tryAddToCollectionField(Object target, String fieldName, Object element) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object current = f.get(target);
            Collection<Object> coll;
            if (current == null) {
                coll = new HashSet<>();
                f.set(target, coll);
            } else if (current instanceof Collection<?> c) {
                coll = (Collection<Object>) c;
            } else {
                return false;
            }
            coll.add(element);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    public Mono<PsePaymentOut> payWithPse(PsePaymentIn in, HttpServletRequest req) {
        String clientIp = ipResolver.resolve(req);

        return epaycoClient.createPsePayment(in, clientIp)
            .map(resp -> buildPseOutAndPersist(resp, in, clientIp));
    }

    private PsePaymentOut buildPseOutAndPersist(JsonNode resp, PsePaymentIn in, String clientIp) {
        // ePayco suele devolver bajo data.transaction.data; urlbanco a veces vive en data.urlbanco
        JsonNode data = resp.path("data");
        JsonNode tx   = data.path("transaction").path("data");

        PsePaymentOut out = new PsePaymentOut();
        // url de banco (usa fallback si viene en otra rama)
        String urlBanco = data.path("urlbanco").asText(null);
        if (urlBanco == null || urlBanco.isBlank()) {
        urlBanco = tx.path("urlbanco").asText(null);
        }
        out.redirectUrl = urlBanco;

        out.refPayco = tx.path("ref_payco").isMissingNode() ? null : tx.path("ref_payco").asLong();
        out.invoice  = tx.path("factura").asText(in.getInvoice());
        out.status   = tx.path("estado").asText("Pendiente");
        out.response = tx.path("respuesta").asText(null);
        out.receipt  = tx.path("recibo").asText(null);
        out.authorizationOrTxnId = tx.path("transactionID").asText(null);
        out.ticketId  = tx.path("ticketId").asText(null);
        out.txnDate   = parseEpaycoDate(tx.path("fecha").asText(null));
        // si tienes lookup token, asígnalo aquí: out.lookupToken = ...

        // —— Persistencia opcional (no romper si falla) ——
        try {
        var e = new EpaycoTransactionEntity();
        e.setRefPayco(out.refPayco);
        e.setInvoice(out.invoice);
        e.setDescription(in.getDescription());
        e.setAmount(in.getValue());
        e.setCurrency(in.getCurrency());
        e.setStatus(out.status);
        e.setResponse(out.response);
        e.setReceipt(out.receipt);
        e.setBank(in.getBank());
        e.setIp(clientIp);
        e.setTxnDate(out.txnDate);
        e.setRawPayload(resp.toString()); // o JsonNode->String
        // completa más campos si quieres (docType, docNumber, email, etc.)
        if (trxRepo != null) trxRepo.save(e);
        } catch (Exception ignore) {}

        return out;
    }


    /** Confirmar estado de una transacción PSE en ePayco y reflejarlo en BD. */
    public Mono<PsePaymentOut> confirmPse(ConfirmPseIn in, HttpServletRequest req) {
        Long txId = in.getTransactionID();
        if (txId == null) {
        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "transactionID es obligatorio"));
        }

        return epaycoClient.confirmPseTransaction(txId)
            .publishOn(Schedulers.boundedElastic())
            .map(resp -> {
            boolean ok = resp.path("success").asBoolean(false);
            if (!ok) {
                String err = resp.path("textResponse").asText("Fallo al confirmar en ePayco");
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, err);
            }
            JsonNode data = resp.path("data");

            // ---- Mapear a nuestro modelo de salida ----
            PsePaymentOut out = new PsePaymentOut();
            out.refPayco  = data.path("ref_payco").isMissingNode() ? null : data.path("ref_payco").asLong();
            out.invoice   = data.path("factura").asText(null);
            out.status    = data.path("estado").asText(null);
            out.response  = data.path("respuesta").asText(null);
            out.receipt   = data.path("recibo").asText(null);
            out.authorizationOrTxnId = data.path("transactionID").asText(null);
            out.ticketId  = data.path("ticketId").asText(null);
            out.redirectUrl = null; // en confirmación ya no hay urlbanco
            out.txnDate   = parseEpaycoDate(data.path("fecha").asText(null));

            // ---- Persistencia/Actualización en BD (no romper si falla) ----
            try {
                EpaycoTransactionEntity e = findExistingTransaction(data)
                    .orElseGet(EpaycoTransactionEntity::new);

                // Identificadores básicos
                if (out.refPayco != null) e.setRefPayco(out.refPayco);
                if (out.invoice != null) e.setInvoice(out.invoice);

                // Valores monetarios (si vienen)
                e.setAmount(new BigDecimal(data.path("valor").asText("0")));
                e.setTax(new BigDecimal(data.path("iva").asText("0")));
                e.setBaseTax(new BigDecimal(data.path("baseiva").asText("0")));
                e.setCurrency(data.path("moneda").asText(null));
                e.setIco(BigDecimal.ZERO); // confirm no trae ico

                // Estado y metadatos
                e.setBank(data.path("banco").asText(null));
                e.setStatus(out.status);
                e.setResponse(out.response);
                e.setReceipt(out.receipt);
                e.setFranchise(data.path("franquicia").asText(null));
                e.setCodeResponse(data.path("cod_respuesta").asInt(0));
                e.setCodeError(null);
                e.setIp(data.path("ip").asText(null));
                e.setTestMode(data.path("enpruebas").asInt(0) == 1);
                e.setTxnDate(out.txnDate);

                // Datos comprador
                e.setDocType(data.path("tipo_doc").asText(null));
                e.setDocNumber(data.path("documento").asText(null));
                e.setFirstNames(data.path("nombres").asText(null));
                e.setLastNames(data.path("apellidos").asText(null));
                e.setEmail(data.path("email").asText(null));
                e.setCity(data.path("ciudad").asText(null));
                e.setAddress(data.path("direccion").asText(null));
                e.setCountryIso2(data.path("ind_pais").asText(null));

                // Raw JSON como string
                e.setRawPayload(resp.toString());

                trxRepo.save(e);
            } catch (Exception ignore) {
                // No romper la respuesta al cliente por fallo de auditoría
            }

            return out;
            });
    }

    /** Intentar encontrar la transacción por ref_payco o factura. */
    private Optional<EpaycoTransactionEntity> findExistingTransaction(JsonNode data) {
        Long ref = data.path("ref_payco").isMissingNode() ? null : data.path("ref_payco").asLong();
        String inv = data.path("factura").asText(null);

        if (ref != null) {
        try { return trxRepo.findByRefPayco(ref); } catch (Exception ignored) {}
        }
        if (inv != null && !inv.isBlank()) {
        try { return trxRepo.findFirstByInvoiceOrderByCreatedAtDesc(inv); } catch (Exception ignored) {}
        }
        return Optional.empty();
    }

    private static Instant parseEpaycoDate(String v) {
        if (v == null || v.isBlank()) return null;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(v, fmt).atZone(ZoneId.systemDefault()).toInstant();
    }



    
}
