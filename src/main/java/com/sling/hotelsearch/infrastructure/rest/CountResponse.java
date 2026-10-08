package com.sling.hotelsearch.infrastructure.rest;

import com.sling.hotelsearch.domain.model.SearchCount;

/** Respuesta de {@code GET /count}. */
public record CountResponse(String searchId, SearchData search, long count) {

    static CountResponse from(SearchCount searchCount) {
        return new CountResponse(
                searchCount.searchId(),
                SearchData.from(searchCount.hotelSearch()),
                searchCount.count());
    }
}
