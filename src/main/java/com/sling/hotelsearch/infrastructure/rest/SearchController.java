package com.sling.hotelsearch.infrastructure.rest;

import com.sling.hotelsearch.application.port.in.CountSearchUseCase;
import com.sling.hotelsearch.application.port.in.RegisterSearchUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador REST de entrada. Solo traduce HTTP a casos de uso. */
@RestController
@Tag(name = "Busquedas", description = "Registro y conteo de busquedas de disponibilidad hotelera")
public class SearchController {

    private final RegisterSearchUseCase registerSearch;
    private final CountSearchUseCase countSearch;

    public SearchController(RegisterSearchUseCase registerSearch, CountSearchUseCase countSearch) {
        this.registerSearch = registerSearch;
        this.countSearch = countSearch;
    }

    @Operation(summary = "Registra una busqueda",
            description = "Valida el payload, lo publica en Kafka y devuelve el identificador de la busqueda.")
    @PostMapping("/search")
    public SearchResponse search(@RequestBody SearchRequest request) {
        return new SearchResponse(registerSearch.register(request.toDomain()));
    }

    @Operation(summary = "Cuenta busquedas iguales",
            description = "Devuelve la busqueda y cuantas busquedas identicas (mismo orden de edades) se registraron.")
    @GetMapping("/count")
    public CountResponse count(@RequestParam String searchId) {
        return CountResponse.from(countSearch.count(searchId));
    }
}
