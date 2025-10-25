package com.academia.backend.service;

import com.academia.backend.domain.UserEntity;
import com.academia.backend.domain.RoleEntity;
import com.academia.backend.dto.in.CardPaymentIn;
import com.academia.backend.repo.UserRepo;
import com.academia.backend.repo.RoleRepo;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

@Service
public class CheckoutUserService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    public CheckoutUserService(UserRepo userRepo, RoleRepo roleRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
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
}
