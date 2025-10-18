package com.academia.backend.service;

import com.academia.backend.domain.SessionEntity;
import com.academia.backend.domain.UserEntity;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

  private static final String USER_NOT_FOUND = "Usuario no encontrado";
  private static final String INCORRECT_PASSWORD = "La contraseña actual es incorrecta";
  private static final String SAME_PASSWORD = "La nueva contraseña debe ser diferente a la actual";
  private static final String EMAIL_IN_USE = "El email ya está registrado";
  private static final String USERNAME_IN_USE = "El nombre de usuario ya está en uso";

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

  public byte[] hmacRefresh(String plain) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(refreshSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return mac.doFinal(plain.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException("Error generating refresh token", e);
    }
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
        .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

    // Verifica que la contraseña actual sea correcta
    if (!verifyPassword(user.getPasswordHash(), currentPassword)) {
      throw new IllegalArgumentException(INCORRECT_PASSWORD);
    }

    // Verifica que la nueva contraseña sea diferente a la actual
    if (verifyPassword(user.getPasswordHash(), newPassword)) {
      throw new IllegalArgumentException(SAME_PASSWORD);
    }

    // Actualiza la contraseña
    user.setPasswordHash(hashPassword(newPassword));
    users.save(user);
  }

  public void updateUser(UUID userId, UserDto updateData) {
    UserEntity user = users.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

    // Valida que el email no esté en uso por otro usuario
    if (updateData.getCorreo() != null && !updateData.getCorreo().equals(user.getEmail())) {
      if (users.findByEmail(updateData.getCorreo()).isPresent()) {
        throw new IllegalArgumentException(EMAIL_IN_USE);
      }
      user.setEmail(updateData.getCorreo());
    }

    // Valida que el username no esté en uso por otro usuario
    if (updateData.getUsername() != null && !updateData.getUsername().equals(user.getUsername())) {
      if (users.findByUsername(updateData.getUsername()).isPresent()) {
        throw new IllegalArgumentException(USERNAME_IN_USE);
      }
      user.setUsername(updateData.getUsername());
    }

    // Actualiza otros campos si no son null
    if (updateData.getNombre() != null)
      user.setNombre(updateData.getNombre());
    if (updateData.getApellido() != null)
      user.setApellido(updateData.getApellido());
    if (updateData.getTelefono() != null)
      user.setTelefono(updateData.getTelefono());
    if (updateData.getNacionalidad() != null)
      user.setNacionalidad(updateData.getNacionalidad());
    if (updateData.getDireccion() != null)
      user.setDireccion(updateData.getDireccion());
    if (updateData.getDondeNosViste() != null)
      user.setDondeNosViste(updateData.getDondeNosViste());
    if (updateData.getIsAdmin() != null)
      user.setAdmin(updateData.getIsAdmin());

    users.save(user);
  }

  public UserDto getUserInfo(UUID userId) {
    UserEntity user = users.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
    UserDto dto = new UserDto();
    dto.id = user.getId();
    dto.setCorreo(user.getEmail());
    dto.setUsername(user.getUsername());
    dto.setNombre(user.getNombre());
    dto.setApellido(user.getApellido());
    dto.setTelefono(user.getTelefono());
    dto.setNacionalidad(user.getNacionalidad());
    dto.setDireccion(user.getDireccion());
    dto.setDondeNosViste(user.getDondeNosViste());
    dto.setIsAdmin(user.isAdmin());
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
          .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
      return getUserInfo(user.getId());
    }
  }

  public java.util.List<UserDto> getAllUsers() {
    return users.findAll().stream()
        .map(user -> {
          UserDto dto = new UserDto();
          dto.id = user.getId();
          dto.setCorreo(user.getEmail());
          dto.setUsername(user.getUsername());
          dto.setNombre(user.getNombre());
          dto.setApellido(user.getApellido());
          dto.setTelefono(user.getTelefono());
          dto.setNacionalidad(user.getNacionalidad());
          dto.setDireccion(user.getDireccion());
          dto.setDondeNosViste(user.getDondeNosViste());
          dto.setIsAdmin(user.isAdmin());
          dto.createdAt = user.getCreatedAt();
          return dto;
        })
        .toList();
  }
}
