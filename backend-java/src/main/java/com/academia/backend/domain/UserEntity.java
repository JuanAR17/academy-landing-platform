package com.academia.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(name = "password_hash", nullable = false, columnDefinition = "text")
  private String passwordHash;

  @Column(name = "nombre", nullable = false)
  private String nombre;

  @Column(name = "apellido")
  private String apellido;

  @Column(name = "telefono")
  private String telefono;

  @Column(name = "nacionalidad")
  private String nacionalidad;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "address_id")
  private Address address;

  @Column(name = "donde_nos_viste")
  private String dondeNosViste;

  @Column(name = "is_admin", nullable = false)
  private boolean isAdmin = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role = Role.STUDENT;

  @Column(name = "bio", columnDefinition = "text")
  private String bio;

  @Column(name = "profile_image_url")
  private String profileImageUrl;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void pre() {
    if (id == null)
      id = UUID.randomUUID();
    if (createdAt == null)
      createdAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getNacionalidad() {
    return nacionalidad;
  }

  public void setNacionalidad(String nacionalidad) {
    this.nacionalidad = nacionalidad;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public String getDondeNosViste() {
    return dondeNosViste;
  }

  public void setDondeNosViste(String dondeNosViste) {
    this.dondeNosViste = dondeNosViste;
  }

  public boolean isAdmin() {
    return isAdmin;
  }

  public void setAdmin(boolean isAdmin) {
    this.isAdmin = isAdmin;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public String getProfileImageUrl() {
    return profileImageUrl;
  }

  public void setProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }
}
