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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataImportService implements CommandLineRunner {

    @Autowired
    private CountryRepo countryRepo;

    @Autowired
    private StateRepo stateRepo;

    @Autowired
    private CityRepo cityRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Tamaño de lote para procesamiento
    private static final int BATCH_SIZE = 3000;

    @Override
    public void run(String... args) throws Exception {
        // Importar siempre en desarrollo
        System.out.println("=== INICIANDO IMPORTACIÓN DE DATOS DE UBICACIONES ===");
        long startTime = System.currentTimeMillis();
        importData();
        long endTime = System.currentTimeMillis();
        System.out.println("=== IMPORTACIÓN COMPLETADA EN " + (endTime - startTime) + "ms ===");
    }

    private void importData() throws IOException {
        System.out.println("Cargando datos desde archivos JSON...");

        // Verificar si ya hay datos
        long countryCount = countryRepo.count();
        if (countryCount > 0) {
            System.out.println("Ya existen " + countryCount + " países en la base de datos. Saltando importación.");
            return;
        }

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
        System.out.println("Procesando países en lotes de " + BATCH_SIZE + "...");
        Map<Integer, Country> countriesMap = new HashMap<>();

        // Primero, obtener países existentes
        List<Country> existingCountries = countryRepo.findAll();
        boolean hasExistingData = !existingCountries.isEmpty();
        
        for (Country country : existingCountries) {
            countriesMap.put(country.getId().intValue(), country);
        }

        List<Country> countriesToSave = new ArrayList<>();
        int processed = 0;

        for (CountryJsonDto dto : countriesJson) {
            // Si no hay datos existentes, saltamos la verificación
            boolean shouldAdd = !hasExistingData || !countriesMap.containsKey(dto.getId());
            
            if (shouldAdd) {
                Country country = new Country();
                country.setId(Long.valueOf(dto.getId()));
                country.setName(dto.getName());
                country.setPhoneCode("+" + dto.getPhonecode());
                countriesToSave.add(country);

                // Guardar en lotes
                if (countriesToSave.size() >= BATCH_SIZE) {
                    List<Country> savedBatch = countryRepo.saveAll(countriesToSave);
                    for (Country saved : savedBatch) {
                        countriesMap.put(saved.getId().intValue(), saved);
                    }
                    processed += countriesToSave.size();
                    System.out.println("Países procesados: " + processed + "/" + countriesJson.size());
                    countriesToSave.clear();
                }
            }
        }

        // Guardar el último lote
        if (!countriesToSave.isEmpty()) {
            List<Country> savedBatch = countryRepo.saveAll(countriesToSave);
            for (Country saved : savedBatch) {
                countriesMap.put(saved.getId().intValue(), saved);
            }
            processed += countriesToSave.size();
            System.out.println("Países procesados: " + processed + "/" + countriesJson.size());
        }

        return countriesMap;
    }

    private Map<Integer, State> importStates(List<StateJsonDto> statesJson, Map<Integer, Country> countriesMap) {
        System.out.println("Procesando estados en lotes de " + BATCH_SIZE + "...");
        Map<Integer, State> statesMap = new HashMap<>();

        // Primero, obtener estados existentes
        List<State> existingStates = stateRepo.findAll();
        boolean hasExistingData = !existingStates.isEmpty();
        
        for (State state : existingStates) {
            statesMap.put(state.getId().intValue(), state);
        }

        List<State> statesToSave = new ArrayList<>();
        int processed = 0;
        int skipped = 0;

        for (StateJsonDto dto : statesJson) {
            // Si no hay datos existentes, saltamos la verificación
            boolean shouldAdd = !hasExistingData || !statesMap.containsKey(dto.getId());
            
            if (shouldAdd) {
                Country country = countriesMap.get(dto.getCountry_id());

                if (country == null) {
                    skipped++;
                    continue;
                }

                State state = new State();
                state.setId(Long.valueOf(dto.getId()));
                state.setName(dto.getName());
                state.setCountry(country);
                statesToSave.add(state);

                // Guardar en lotes
                if (statesToSave.size() >= BATCH_SIZE) {
                    List<State> savedBatch = stateRepo.saveAll(statesToSave);
                    for (State saved : savedBatch) {
                        statesMap.put(saved.getId().intValue(), saved);
                    }
                    processed += statesToSave.size();
                    System.out.println("Estados procesados: " + processed + "/" + statesJson.size() +
                            " (Omitidos: " + skipped + ")");
                    statesToSave.clear();
                }
            }
        }

        // Guardar el último lote
        if (!statesToSave.isEmpty()) {
            List<State> savedBatch = stateRepo.saveAll(statesToSave);
            for (State saved : savedBatch) {
                statesMap.put(saved.getId().intValue(), saved);
            }
            processed += statesToSave.size();
            System.out.println("Estados procesados: " + processed + "/" + statesJson.size() +
                    " (Omitidos: " + skipped + ")");
        }

        if (skipped > 0) {
            System.out.println("ADVERTENCIA: Se omitieron " + skipped + " estados debido a países faltantes");
        }

        return statesMap;
    }

    private void importCities(List<CityJsonDto> citiesJson, Map<Integer, State> statesMap) {
        System.out.println("Procesando ciudades en lotes de " + BATCH_SIZE + "...");

        // Verificar si hay datos existentes
        long existingCount = cityRepo.count();
        boolean hasExistingData = existingCount > 0;
        
        if (hasExistingData) {
            System.out.println("Ya existen " + existingCount + " ciudades. Saltando importación.");
            return;
        }

        List<City> citiesToSave = new ArrayList<>();
        int processed = 0;
        int skipped = 0;

        for (CityJsonDto dto : citiesJson) {
            State state = statesMap.get(dto.getState_id());

            if (state == null) {
                skipped++;
                continue;
            }

            City city = new City();
            city.setId(Long.valueOf(dto.getId()));
            city.setName(dto.getName());
            city.setState(state);
            citiesToSave.add(city);

            // Guardar en lotes
            if (citiesToSave.size() >= BATCH_SIZE) {
                cityRepo.saveAll(citiesToSave);
                cityRepo.flush(); // Forzar escritura a BD
                processed += citiesToSave.size();
                System.out.println("Ciudades procesadas: " + processed + "/" + citiesJson.size() +
                        " (Omitidas: " + skipped + ")");
                citiesToSave.clear();
            }
        }

        // Guardar el último lote
        if (!citiesToSave.isEmpty()) {
            cityRepo.saveAll(citiesToSave);
            cityRepo.flush(); // Forzar escritura a BD
            processed += citiesToSave.size();
            System.out.println("Ciudades procesadas: " + processed + "/" + citiesJson.size() +
                    " (Omitidas: " + skipped + ")");
        }

        if (skipped > 0) {
            System.out.println("ADVERTENCIA: Se omitieron " + skipped + " ciudades debido a estados faltantes");
        }
        
        System.out.println("Ciudades importadas exitosamente");
    }
}
