package com.academia.backend.dto;

import java.util.List;

public class CountryDto {
    private Long id;
    private String name;
    private String phoneCode;
    private List<StateDto> states;

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

    public List<StateDto> getStates() {
        return states;
    }

    public void setStates(List<StateDto> states) {
        this.states = states;
    }
}