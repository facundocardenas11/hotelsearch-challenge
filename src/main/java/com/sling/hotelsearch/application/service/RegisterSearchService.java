package com.sling.hotelsearch.application.service;

import com.sling.hotelsearch.application.port.in.RegisterSearchUseCase;
import com.sling.hotelsearch.application.port.out.SearchEventPublisher;
import com.sling.hotelsearch.domain.model.HotelSearch;
import com.sling.hotelsearch.domain.model.Search;

import java.util.UUID;

public class RegisterSearchService implements RegisterSearchUseCase {

    private final SearchEventPublisher publisher;

    public RegisterSearchService(SearchEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public String register(HotelSearch hotelSearch) {
        Search search = new Search(UUID.randomUUID().toString(), hotelSearch);
        publisher.publish(search);
        return search.searchId();
    }
}
