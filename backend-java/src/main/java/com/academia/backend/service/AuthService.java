package com.academia.backend.service;

import com.academia.backend.domain.SessionEntity;
import com.academia.backend.domain.UserEntity;
import com.academia.backend.dto.ChangePasswordIn;
import com.academia.backend.dto.LoginIn;
import com.academia.backend.dto.RegisterIn;
import com.academia.backend.dto.TokenOut;
import com.academia.backend.dto.UserDto;
import com.academia.backend.repo.SessionRepo;
import com.academia.backend.repo.UserRepo;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {
  private final UserRepo users;
  private final SessionRepo sessions;
  private final JwtService jwt;
  private final SecureRandom rnd = new SecureRandom();
  private final Argon2PasswordEncoder argon = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

  @Value("${jwt.sessionTtlDays:60}")
  long sessionTtlDays;
  @Value("${refresh.hmacSecret}")
  String refreshSecret;
  @Value("${cookies.secure:false}")
  boolean cookieSecure;
  @Value("${cookies.sameSite:Strict}")
  String sameSite;
  @Value("${cookies.domain:}")
  String cookieDomain;
  @Value("${cookies.path:/}")
  String cookiePath;

  public AuthService(UserRepo users, SessionRepo sessions, JwtService jwt) {
    this.users = users;
    this.sessions = sessions;
    this.jwt = jwt;
  }

  public String hashPassword(String raw) {
    return argon.encode(raw);
  }

  public boolean verifyPassword(String hash, String raw) {
    return argon.matches(raw, hash);
  }

  public String genRandomUrlToken() {
    byte[] buf = new byte[32];
    rnd.nextBytes(buf);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
  }

  public byte[] hmacRefresh(String plain) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(refreshSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    return mac.doFinal(plain.getBytes(StandardCharsets.UTF_8));
  }

  public Cookie buildHttpOnlyCookie(String name, String value, long maxAgeSeconds) {
    Cookie c = new Cookie(name, value);
    c.setHttpOnly(true);
    c.setSecure(cookieSecure);
    c.setPath(cookiePath);
    if (!cookieDomain.isBlank())
      c.setDomain(cookieDomain);
    c.setMaxAge((int) maxAgeSeconds);
    return c;
  }

  public TokenOut mint(UserEntity user, SessionEntity row, String csrf) {
    String access = jwt.mintAccess(user.getId().toString(), row.getId().toString());
    return new TokenOut(access, csrf, user.getId());
  }

  public SessionEntity newSession(UUID userId, byte[] refreshHash, String ua) {
    SessionEntity s = new SessionEntity();
    s.setUserId(userId);
    s.setRefreshTokenHash(refreshHash);
    s.setCreatedAt(Instant.now());
    s.setExpiresAt(Instant.now().plus(sessionTtlDays, ChronoUnit.DAYS));
    s.setUserAgent(ua);
    return sessions.save(s);
  }

  public void changePassword(UUID userId, String currentPassword, String newPassword) {
    UserEntity user = users.findById(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // Verifica que la contraseña actual sea correcta
    if (!verifyPassword(user.getPasswordHash(), currentPassword)) {
      throw new RuntimeException("La contraseña actual es incorrecta");
    }

    // Verifica que la nueva contraseña sea diferente a la actual
    if (verifyPassword(user.getPasswordHash(), newPassword)) {
      throw new RuntimeException("La nueva contraseña debe ser diferente a la actual");
    }

    // Actualiza la contraseña
    user.setPasswordHash(hashPassword(newPassword));
    users.save(user);
  }

  public void updateUser(UUID userId, UserDto updateData) {
    UserEntity user = users.findById(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // Valida que el email no esté en uso por otro usuario
    if (updateData.correo != null && !updateData.correo.equals(user.getEmail())) {
      if (users.findByEmail(updateData.correo).isPresent()) {
        throw new RuntimeException("El email ya está registrado");
      }
      user.setEmail(updateData.correo);
    }

    // Valida que el username no esté en uso por otro usuario
    if (updateData.username != null && !updateData.username.equals(user.getUsername())) {
      if (users.findByUsername(updateData.username).isPresent()) {
        throw new RuntimeException("El nombre de usuario ya está en uso");
      }
      user.setUsername(updateData.username);
    }

    // Actualiza otros campos si no son null
    if (updateData.nombre != null)
      user.setNombre(updateData.nombre);
    if (updateData.apellido != null)
      user.setApellido(updateData.apellido);
    if (updateData.telefono != null)
      user.setTelefono(updateData.telefono);
    if (updateData.nacionalidad != null)
      user.setNacionalidad(updateData.nacionalidad);
    if (updateData.direccion != null)
      user.setDireccion(updateData.direccion);
    if (updateData.dondeNosViste != null)
      user.setDondeNosViste(updateData.dondeNosViste);
    if (updateData.isAdmin != null)
      user.setAdmin(updateData.isAdmin);

    users.save(user);
  }

  public UserDto getUserInfo(UUID userId) {
    UserEntity user = users.findById(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    UserDto dto = new UserDto();
    dto.id = user.getId();
    dto.correo = user.getEmail();
    dto.username = user.getUsername();
    dto.nombre = user.getNombre();
    dto.apellido = user.getApellido();
    dto.telefono = user.getTelefono();
    dto.nacionalidad = user.getNacionalidad();
    dto.direccion = user.getDireccion();
    dto.dondeNosViste = user.getDondeNosViste();
    dto.isAdmin = user.isAdmin();
    dto.createdAt = user.getCreatedAt();
    return dto;
  }

  public UserDto getUserByIdentifier(String identifier) {
    // Primero intenta parsear como UUID (ID)
    try {
      UUID id = UUID.fromString(identifier);
      return getUserInfo(id);
    } catch (IllegalArgumentException e) {
      // No es UUID, busca por email o username
      UserEntity user = users.findByEmailOrUsername(identifier)
          .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
      return getUserInfo(user.getId());
    }
  }

  public java.util.List<UserDto> getAllUsers() {
    return users.findAll().stream()
        .map(user -> {
          UserDto dto = new UserDto();
          dto.id = user.getId();
          dto.correo = user.getEmail();
          dto.username = user.getUsername();
          dto.nombre = user.getNombre();
          dto.apellido = user.getApellido();
          dto.telefono = user.getTelefono();
          dto.nacionalidad = user.getNacionalidad();
          dto.direccion = user.getDireccion();
          dto.dondeNosViste = user.getDondeNosViste();
          dto.isAdmin = user.isAdmin();
          dto.createdAt = user.getCreatedAt();
          return dto;
        })
        .toList();
  }
}
