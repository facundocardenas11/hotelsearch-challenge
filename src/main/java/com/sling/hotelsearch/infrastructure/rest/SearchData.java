package com.sling.hotelsearch.infrastructure.rest;

import com.sling.hotelsearch.domain.model.HotelSearch;

import java.util.List;

/** Criterios de busqueda tal como se exponen en la API (fechas dd/MM/yyyy). */
public record SearchData(String hotelId, String checkIn, String checkOut, List<Integer> ages) {

    static SearchData from(HotelSearch search) {
        return new SearchData(
                search.hotelId(),
                DateFormats.format(search.checkIn()),
                DateFormats.format(search.checkOut()),
                search.ages());
    }
}
