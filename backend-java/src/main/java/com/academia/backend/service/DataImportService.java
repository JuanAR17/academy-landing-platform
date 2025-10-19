package com.academia.backend.service;

import com.academia.backend.domain.City;
import com.academia.backend.domain.Country;
import com.academia.backend.domain.State;
import com.academia.backend.dto.CityJsonDto;
import com.academia.backend.dto.CountryJsonDto;
import com.academia.backend.dto.StateJsonDto;
import com.academia.backend.repo.CityRepo;
import com.academia.backend.repo.CountryRepo;
import com.academia.backend.repo.StateRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataImportService implements CommandLineRunner {

    @Autowired
    private CountryRepo countryRepo;

    @Autowired
    private StateRepo stateRepo;

    @Autowired
    private CityRepo cityRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Solo importar si no hay datos
        if (countryRepo.count() == 0) {
            System.out.println("=== INICIANDO IMPORTACIÓN DE DATOS DE UBICACIONES ===");
            long startTime = System.currentTimeMillis();
            importData();
            long endTime = System.currentTimeMillis();
            System.out.println("=== IMPORTACIÓN COMPLETADA EN " + (endTime - startTime) + "ms ===");
        } else {
            System.out.println("Los datos de ubicaciones ya existen en la base de datos. Saltando importación.");
        }
    }

    private void importData() throws IOException {
        System.out.println("Cargando datos desde archivos JSON...");

        // Importar países
        System.out.println("Importando países...");
        List<CountryJsonDto> countriesJson = loadJsonData("data/countries.json",
                new TypeReference<List<CountryJsonDto>>() {
                });
        System.out.println("Encontrados " + countriesJson.size() + " países en el archivo JSON");
        Map<Integer, Country> countriesMap = importCountries(countriesJson);
        System.out.println("Países importados exitosamente: " + countriesMap.size());

        // Importar estados
        System.out.println("Importando estados/departamentos...");
        List<StateJsonDto> statesJson = loadJsonData("data/states.json",
                new TypeReference<List<StateJsonDto>>() {
                });
        System.out.println("Encontrados " + statesJson.size() + " estados en el archivo JSON");
        Map<Integer, State> statesMap = importStates(statesJson, countriesMap);
        System.out.println("Estados importados exitosamente: " + statesMap.size());

        // Importar ciudades
        System.out.println("Importando ciudades...");
        List<CityJsonDto> citiesJson = loadJsonData("data/cities.json",
                new TypeReference<List<CityJsonDto>>() {
                });
        System.out.println("Encontradas " + citiesJson.size() + " ciudades en el archivo JSON");
        importCities(citiesJson, statesMap);
        System.out.println("Ciudades importadas exitosamente");
    }

    private <T> List<T> loadJsonData(String filePath, TypeReference<List<T>> typeReference) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, typeReference);
        }
    }

    private Map<Integer, Country> importCountries(List<CountryJsonDto> countriesJson) {
        List<Country> countries = countriesJson.stream()
            .map(dto -> {
                Country country = new Country();
                country.setId(Long.valueOf(dto.getId()));
                country.setName(dto.getName());
                country.setPhoneCode("+" + dto.getPhonecode());
                return country;
            })
            .collect(Collectors.toList());

        List<Country> savedCountries = countryRepo.saveAll(countries);
        return savedCountries.stream()
            .collect(Collectors.toMap(country -> country.getId().intValue(), country -> country));
    }    private Map<Integer, State> importStates(List<StateJsonDto> statesJson, Map<Integer, Country> countriesMap) {
        List<State> states = statesJson.stream()
            .map(dto -> {
                State state = new State();
                state.setId(Long.valueOf(dto.getId()));
                state.setName(dto.getName());
                state.setCountry(countriesMap.get(dto.getCountry_id()));
                return state;
            })
            .collect(Collectors.toList());

        // Guardar en lotes para mejor rendimiento
        List<State> savedStates = stateRepo.saveAll(states);
        return savedStates.stream()
            .collect(Collectors.toMap(state -> state.getId().intValue(), state -> state));
    }

    private void importCities(List<CityJsonDto> citiesJson, Map<Integer, State> statesMap) {
        List<City> cities = citiesJson.stream()
            .map(dto -> {
                City city = new City();
                city.setId(Long.valueOf(dto.getId()));
                city.setName(dto.getName());
                city.setState(statesMap.get(dto.getState_id()));
                return city;
            })
            .collect(Collectors.toList());

        // Guardar en lotes para mejor rendimiento
        cityRepo.saveAll(cities);
    }
}
