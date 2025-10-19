package com.academia.backend.service;

import com.academia.backend.domain.City;
import com.academia.backend.domain.Country;
import com.academia.backend.domain.State;
import com.academia.backend.dto.CityDto;
import com.academia.backend.dto.CountryDto;
import com.academia.backend.dto.StateDto;
import com.academia.backend.repo.CityRepo;
import com.academia.backend.repo.CountryRepo;
import com.academia.backend.repo.StateRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final CountryRepo countryRepo;
    private final StateRepo stateRepo;
    private final CityRepo cityRepo;

    public LocationService(CountryRepo countryRepo, StateRepo stateRepo, CityRepo cityRepo) {
        this.countryRepo = countryRepo;
        this.stateRepo = stateRepo;
        this.cityRepo = cityRepo;
    }

    // Nuevo método: Obtener todos los países (solo información básica)
    public List<CountryDto> getAllCountries() {
        List<Country> countries = countryRepo.findAll();
        return countries.stream()
                .map(country -> {
                    CountryDto dto = new CountryDto();
                    dto.setId(country.getId());
                    dto.setName(country.getName());
                    dto.setPhoneCode(country.getPhoneCode());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Nuevo método: Obtener estados por ID de país
    public List<StateDto> getStatesByCountryId(Long countryId) {
        List<State> states = stateRepo.findByCountryId(countryId);
        return states.stream()
                .map(state -> {
                    StateDto dto = new StateDto();
                    dto.setId(state.getId());
                    dto.setName(state.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Nuevo método: Obtener ciudades por ID de estado
    public List<CityDto> getCitiesByStateId(Long stateId) {
        List<City> cities = cityRepo.findByStateId(stateId);
        return cities.stream()
                .map(city -> {
                    CityDto dto = new CityDto();
                    dto.setId(city.getId());
                    dto.setName(city.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}