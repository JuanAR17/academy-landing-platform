package com.academia.backend.service;

import com.academia.backend.domain.Country;
import com.academia.backend.dto.CityDto;
import com.academia.backend.dto.CountryDto;
import com.academia.backend.dto.StateDto;
import com.academia.backend.repo.CountryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    @Autowired
    private CountryRepo countryRepo;

    public List<CountryDto> getAllCountriesWithDetails() {
        List<Country> countries = countryRepo.findAllWithStatesAndCities();

        return countries.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private CountryDto convertToDto(Country country) {
        CountryDto dto = new CountryDto();
        dto.setId(country.getId());
        dto.setName(country.getName());
        dto.setPhoneCode(country.getPhoneCode());

        if (country.getStates() != null) {
            List<StateDto> stateDtos = country.getStates().stream()
                .map(state -> {
                    StateDto stateDto = new StateDto();
                    stateDto.setId(state.getId());
                    stateDto.setName(state.getName());

                    if (state.getCities() != null) {
                        List<CityDto> cityDtos = state.getCities().stream()
                            .map(city -> {
                                CityDto cityDto = new CityDto();
                                cityDto.setId(city.getId());
                                cityDto.setName(city.getName());
                                return cityDto;
                            })
                            .collect(Collectors.toList());
                        stateDto.setCities(cityDtos);
                    }

                    return stateDto;
                })
                .collect(Collectors.toList());
            dto.setStates(stateDtos);
        }

        return dto;
    }
}