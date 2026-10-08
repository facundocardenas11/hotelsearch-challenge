package com.sling.hotelsearch.infrastructure.rest;

import com.sling.hotelsearch.domain.model.HotelSearch;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Payload de {@code POST /search}. Las fechas llegan como texto dd/MM/yyyy. */
public record SearchRequest(
        @Schema(example = "1234aBc") String hotelId,
        @Schema(example = "29/12/2023") String checkIn,
        @Schema(example = "31/12/2023") String checkOut,
        @Schema(example = "[30, 29, 1, 3]") List<Integer> ages) {

    /** Convierte al modelo de dominio; las validaciones ocurren al construir {@link HotelSearch}. */
    HotelSearch toDomain() {
        return new HotelSearch(
                hotelId,
                DateFormats.parse("checkIn", checkIn),
                DateFormats.parse("checkOut", checkOut),
                ages);
    }
}
