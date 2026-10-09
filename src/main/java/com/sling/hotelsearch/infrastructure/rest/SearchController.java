package com.sling.hotelsearch.infrastructure.rest;

import com.sling.hotelsearch.application.port.in.CountSearchUseCase;
import com.sling.hotelsearch.application.port.in.RegisterSearchUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adaptador REST de entrada. Solo traduce HTTP a casos de uso. */
@RestController
@Tag(name = "Busquedas", description = "Registro y conteo de busquedas de disponibilidad hotelera")
public class SearchController {

    private final RegisterSearchUseCase registerSearch;
    private final CountSearchUseCase countSearch;
    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    public SearchController(RegisterSearchUseCase registerSearch, CountSearchUseCase countSearch) {
        this.registerSearch = registerSearch;
        this.countSearch = countSearch;
    }

    @Operation(summary = "Registra una busqueda",
            description = "Valida el payload, lo publica en Kafka y devuelve el identificador de la busqueda.")
    @PostMapping("/search")
    @ResponseStatus(HttpStatus.CREATED)
    public SearchResponse search(@RequestBody SearchRequest request) {
        log.info("STARTED CONTROLLER POST /search hotelId={}", request.hotelId());
        SearchResponse response = new SearchResponse(registerSearch.register(request.toDomain()));
        log.info("FINISH CONTROLLER POST /search  searchId={}", response.searchId());
        return response;

    }

    @Operation(summary = "Cuenta busquedas iguales",
            description = "Devuelve la busqueda y cuantas busquedas identicas (mismo orden de edades) se registraron.")
    @GetMapping("/count")
    public CountResponse count(@RequestParam String searchId) {
        log.info("STARTED CONTROLLER GET /count searchId={}", searchId);
        CountResponse response = CountResponse.from(countSearch.count(searchId));
        log.info("FINISH CONTROLLER GET /count searchId={} count={}", searchId, response.count());
        return response;

    }
}
