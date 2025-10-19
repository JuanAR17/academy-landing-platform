package com.academia.backend.web;

import com.academia.backend.dto.CityDto;
import com.academia.backend.dto.CountryDto;
import com.academia.backend.dto.StateDto;
import com.academia.backend.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/countries")
    @Operation(summary = "Obtener todos los países (solo información básica)")
    public ResponseEntity<List<CountryDto>> getAllCountriesSimple() {
        List<CountryDto> countries = locationService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/states/countries/{countryId}")
    @Operation(summary = "Obtener los estados/departamentos de un país específico")
    public ResponseEntity<List<StateDto>> getStatesByCountryId(
            @Parameter(description = "ID del país") @PathVariable Long countryId) {
        List<StateDto> states = locationService.getStatesByCountryId(countryId);
        return ResponseEntity.ok(states);
    }

    @GetMapping("/cities/states/{stateId}")
    @Operation(summary = "Obtener las ciudades de un estado/departamento específico")
    public ResponseEntity<List<CityDto>> getCitiesByStateId(
            @Parameter(description = "ID del estado/departamento") @PathVariable Long stateId) {
        List<CityDto> cities = locationService.getCitiesByStateId(stateId);
        return ResponseEntity.ok(cities);
    }
}