package com.academia.backend.web;

import com.academia.backend.dto.CountryDto;
import com.academia.backend.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    @Operation(summary = "Obtener todos los países con sus estados/departamentos, ciudades y códigos de teléfono")
    public ResponseEntity<List<CountryDto>> getAllCountries() {
        List<CountryDto> countries = locationService.getAllCountriesWithDetails();
        return ResponseEntity.ok(countries);
    }
}