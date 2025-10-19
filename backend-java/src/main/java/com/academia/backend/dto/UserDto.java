package com.academia.backend.dto;

import com.academia.backend.domain.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class UserDto {
    @JsonIgnore // No se envía en requests
    public UUID id;

    @Email(message = "Email inválido")
    protected String correo;

    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    protected String username;

    protected String firstName;

    protected String lastName;

    protected String phone;

    protected String nationality;

    protected AddressDto address;

    protected String howDidYouFindUs;

    protected Role role;

    @JsonIgnore
    public Instant createdAt;

    // Getters and setters
    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public AddressDto getAddress() {
        return address;
    }

    public void setAddress(AddressDto address) {
        this.address = address;
    }

    public String getHowDidYouFindUs() {
        return howDidYouFindUs;
    }

    public void setHowDidYouFindUs(String howDidYouFindUs) {
        this.howDidYouFindUs = howDidYouFindUs;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Legacy methods for backward compatibility
    public Boolean getIsAdmin() {
        return role == Role.ADMIN || role == Role.SUPER_ADMIN;
    }

    public void setIsAdmin(Boolean isAdmin) {
        // This method is kept for backward compatibility but should not be used
        // Role should be set directly
    }

    public Boolean getIsSuperAdmin() {
        return role == Role.SUPER_ADMIN;
    }

    public void setIsSuperAdmin(Boolean isSuperAdmin) {
        // This method is kept for backward compatibility but should not be used
        // Role should be set directly
    }
}