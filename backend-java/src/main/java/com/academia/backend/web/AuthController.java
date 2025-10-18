package com.academia.backend.web;

import com.academia.backend.domain.Address;
import com.academia.backend.domain.SessionEntity;
import com.academia.backend.domain.UserEntity;
import com.academia.backend.dto.ChangePasswordIn;
import com.academia.backend.dto.LoginIn;
import com.academia.backend.dto.RegisterIn;
import com.academia.backend.dto.TokenOut;
import com.academia.backend.dto.UserDto;
import com.academia.backend.repo.SessionRepo;
import com.academia.backend.repo.UserRepo;
import com.academia.backend.service.AuthService;
import com.academia.backend.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final UserRepo users;
  private final SessionRepo sessions;
  private final AuthService auth;
  private final JwtService jwt;

  @Value("${cookies.secure:false}")
  boolean cookieSecure;
  @Value("${cookies.sameSite:Strict}")
  String sameSite;
  @Value("${cookies.domain:}")
  String cookieDomain;
  @Value("${cookies.path:/}")
  String cookiePath;

  private static final String RT_COOKIE = "rt";
  private static final String CSRF_COOKIE = "csrf";

  public AuthController(UserRepo users, SessionRepo sessions, AuthService auth, JwtService jwt) {
    this.users = users;
    this.sessions = sessions;
    this.auth = auth;
    this.jwt = jwt;
  }

  @PostMapping("/login")
  @Operation(summary = "Login: crea sesión y entrega access JWT + cookies (rt, csrf)")
  public ResponseEntity<TokenOut> login(@Valid @RequestBody LoginIn in,
      @RequestHeader(value = "User-Agent", required = false) String ua) {
    UserEntity user = users.findByEmailOrUsername(in.identifier)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    if (!auth.verifyPassword(user.getPasswordHash(), in.password))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

    String refreshPlain = auth.genRandomUrlToken();
    byte[] rth = auth.hmacRefresh(refreshPlain);

    SessionEntity row = auth.newSession(user.getId(), rth, ua);

    String csrf = auth.genRandomUrlToken();
    String access = jwt.mintAccess(user.getId().toString(), row.getId().toString());
    TokenOut out = new TokenOut(access, csrf, user.getId());

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.SET_COOKIE, buildCookie(RT_COOKIE, refreshPlain, true, 60L * 60 * 24 * 60));
    headers.add(HttpHeaders.SET_COOKIE, buildCookie(CSRF_COOKIE, csrf, false, 60L * 60 * 24 * 60));
    return ResponseEntity.ok().headers(headers).body(out);
  }

  @PostMapping("/register")
  @Operation(summary = "Registro de nuevo usuario")
  public ResponseEntity<TokenOut> register(@Valid @RequestBody RegisterIn in,
      @RequestHeader(value = "User-Agent", required = false) String ua) {
    // Valida que el email no exista
    if (users.findByEmail(in.getCorreo()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está registrado");
    }

    // Valida que el username no exista
    if (users.findByUsername(in.getUsername()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de usuario ya está en uso");
    }

    // Crea un nuevo usuario
    UserEntity user = new UserEntity();
    user.setEmail(in.getCorreo());
    user.setUsername(in.getUsername());
    user.setPasswordHash(auth.hashPassword(in.contrasena));
    user.setNombre(in.getNombre());
    user.setApellido(in.getApellido());
    user.setTelefono(in.getTelefono());
    user.setNacionalidad(in.getNacionalidad());
    if (in.getAddress() != null) {
      Address address = new Address();
      address.setDireccion(in.getAddress().getDireccion());
      address.setCiudad(in.getAddress().getCiudad());
      address.setDepartamento(in.getAddress().getDepartamento());
      address.setPais(in.getAddress().getPais());
      user.setAddress(address);
    }
    user.setDondeNosViste(in.getDondeNosViste());
    user = users.save(user);

    // Crea una sesión automáticamente
    String refreshPlain = auth.genRandomUrlToken();
    byte[] rth = auth.hmacRefresh(refreshPlain);
    SessionEntity row = auth.newSession(user.getId(), rth, ua);

    String csrf = auth.genRandomUrlToken();
    String access = jwt.mintAccess(user.getId().toString(), row.getId().toString());
    TokenOut out = new TokenOut(access, csrf, user.getId());

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.SET_COOKIE, buildCookie(RT_COOKIE, refreshPlain, true, 60L * 60 * 24 * 60));
    headers.add(HttpHeaders.SET_COOKIE, buildCookie(CSRF_COOKIE, csrf, false, 60L * 60 * 24 * 60));
    return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(out);
  }

  @PostMapping("/refresh")
  @Operation(summary = "Rota refresh y entrega nuevo access")
  public ResponseEntity<TokenOut> refresh(HttpServletRequest req) {
    String csrfCookie = getCookie(req, CSRF_COOKIE);
    String csrfHeader = req.getHeader("X-CSRF-Token");
    if (csrfCookie == null || csrfHeader == null || !csrfCookie.equals(csrfHeader))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "CSRF validation failed");

    String rt = getCookie(req, RT_COOKIE);
    if (rt == null)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token");

    byte[] rth = auth.hmacRefresh(rt);
    SessionEntity row = sessions.findByRefreshTokenHash(rth)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session"));

    if (row.isRevoked() || row.getExpiresAt().isBefore(Instant.now()))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");

    String newPlain = auth.genRandomUrlToken();
    row.setRefreshTokenHash(auth.hmacRefresh(newPlain));
    row.setLastUsedAt(Instant.now());
    sessions.save(row);

    String access = jwt.mintAccess(row.getUserId().toString(), row.getId().toString());
    TokenOut out = new TokenOut(access, null, row.getUserId());

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.SET_COOKIE, buildCookie(RT_COOKIE, newPlain, true, 60L * 60 * 24 * 60));
    return ResponseEntity.ok().headers(headers).body(out);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Revoca sesión y borra cookies")
  public void logout(HttpServletRequest req, HttpServletResponse res) {
    String rt = getCookie(req, RT_COOKIE);
    if (rt != null) {
      byte[] rth = auth.hmacRefresh(rt);
      sessions.findByRefreshTokenHash(rth).ifPresent(s -> {
        s.setRevoked(true);
        sessions.save(s);
      });
    }
    res.addHeader(HttpHeaders.SET_COOKIE, buildCookie(RT_COOKIE, "", true, 0));
    res.addHeader(HttpHeaders.SET_COOKIE, buildCookie(CSRF_COOKIE, "", false, 0));
  }

  @GetMapping("/check")
  public java.util.Map<String, String> check() {
    return java.util.Map.of("status", "success");
  }

  @PostMapping("/change-password")
  @Operation(summary = "Cambiar contraseña del usuario autenticado")
  public ResponseEntity<java.util.Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordIn in) {
    // Obtiene el userId del contexto de seguridad (JWT)
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
    }

    String userIdStr = authentication.getName();
    UUID userId;
    try {
      userId = UUID.fromString(userIdStr);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
    }

    try {
      auth.changePassword(userId, in.currentPassword, in.newPassword);
      return ResponseEntity.ok(java.util.Map.of("message", "Contraseña actualizada exitosamente"));
    } catch (RuntimeException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @PutMapping("/update")
  @Operation(summary = "Actualizar datos del usuario autenticado")
  public ResponseEntity<java.util.Map<String, String>> updateUser(@Valid @RequestBody UserDto in) {
    // Obtiene el userId del contexto de seguridad (JWT)
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
    }

    String userIdStr = authentication.getName();
    UUID userId;
    try {
      userId = UUID.fromString(userIdStr);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
    }

    try {
      auth.updateUser(userId, in);
      return ResponseEntity.ok(java.util.Map.of("message", "Datos actualizados exitosamente"));
    } catch (RuntimeException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @GetMapping("/user")
  @Operation(summary = "Obtener información de usuario: propio si no es admin, o por ID/email/username si es admin")
  public ResponseEntity<Object> getUserInfo(@RequestParam(required = false) String identifier) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
    }

    String userIdStr = authentication.getName();
    UUID userId;
    try {
      userId = UUID.fromString(userIdStr);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
    }

    // Obtener info del usuario autenticado para verificar si es admin
    UserDto currentUser = auth.getUserInfo(userId);

    if (identifier == null || identifier.trim().isEmpty()) {
      // Sin identifier: devolver info del autenticado
      if (!Boolean.TRUE.equals(currentUser.getIsAdmin())) {
        Map<String, Object> response = new HashMap<>();
        response.put("correo", currentUser.getCorreo());
        response.put("username", currentUser.getUsername());
        response.put("nombre", currentUser.getNombre());
        response.put("apellido", currentUser.getApellido());
        response.put("telefono", currentUser.getTelefono());
        response.put("nacionalidad", currentUser.getNacionalidad());
        response.put("direccion", currentUser.getAddress() != null ? currentUser.getAddress().getDireccion() : null);
        response.put("ciudad", currentUser.getAddress() != null ? currentUser.getAddress().getCiudad() : null);
        response.put("departamento", currentUser.getAddress() != null ? currentUser.getAddress().getDepartamento() : null);
        response.put("pais", currentUser.getAddress() != null ? currentUser.getAddress().getPais() : null);
        response.put("dondeNosViste", currentUser.getDondeNosViste());
        return ResponseEntity.ok(response);
      } else {
        return ResponseEntity.ok(currentUser);
      }
    } else {
      // Con identifier: solo admins pueden buscar otros usuarios
      if (!Boolean.TRUE.equals(currentUser.getIsAdmin())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores pueden buscar otros usuarios");
      }
      try {
        UserDto info = auth.getUserByIdentifier(identifier);
        return ResponseEntity.ok(info);
      } catch (RuntimeException e) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
      }
    }
  }

  @GetMapping("/users")
  @Operation(summary = "Obtener lista de todos los usuarios (solo admins)")
  public ResponseEntity<java.util.List<UserDto>> getAllUsers() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
    }

    String userIdStr = authentication.getName();
    UUID userId;
    try {
      userId = UUID.fromString(userIdStr);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
    }

    // Verificar si es admin
    UserDto currentUser = auth.getUserInfo(userId);
    if (!Boolean.TRUE.equals(currentUser.getIsAdmin())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores pueden ver la lista de usuarios");
    }

    java.util.List<UserDto> userList = auth.getAllUsers();
    return ResponseEntity.ok(userList);
  }

  // ---- helpers cookies ----
  private String buildCookie(String name, String value, boolean httpOnly, long maxAgeSeconds) {
    StringBuilder sb = new StringBuilder();
    sb.append(name).append("=").append(value).append("; Path=").append(cookiePath)
        .append("; Max-Age=").append(maxAgeSeconds);
    if (httpOnly)
      sb.append("; HttpOnly");
    if (cookieSecure)
      sb.append("; Secure");
    if (!cookieDomain.isBlank())
      sb.append("; Domain=").append(cookieDomain);
    sb.append("; SameSite=").append(sameSite);
    return sb.toString();
  }

  private static String getCookie(HttpServletRequest req, String name) {
    jakarta.servlet.http.Cookie[] arr = req.getCookies();
    if (arr == null)
      return null;
    for (jakarta.servlet.http.Cookie c : arr) {
      if (name.equals(c.getName()))
        return c.getValue();
    }
    return null;
  }
}
