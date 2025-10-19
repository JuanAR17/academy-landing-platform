package com.academia.backend.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "countries")
public class Country {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "phone_code")
    private String phoneCode;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<State> states;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }
}