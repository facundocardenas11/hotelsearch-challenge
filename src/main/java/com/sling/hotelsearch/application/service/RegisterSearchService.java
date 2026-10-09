package com.sling.hotelsearch.application.service;

import com.sling.hotelsearch.application.port.in.RegisterSearchUseCase;
import com.sling.hotelsearch.application.port.out.SearchEventPublisher;
import com.sling.hotelsearch.domain.exception.InvalidSearchException;
import com.sling.hotelsearch.domain.model.HotelSearch;
import com.sling.hotelsearch.domain.model.Search;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterSearchService implements RegisterSearchUseCase {

    private final SearchEventPublisher publisher;
    private final Clock clock;

    private static final Logger log = LoggerFactory.getLogger(RegisterSearchService.class);

    public RegisterSearchService(SearchEventPublisher publisher, Clock clock) {
        this.publisher = publisher;
        this.clock = clock;
    }

    @Override
    public String register(HotelSearch hotelSearch) {
        if (hotelSearch.checkIn().isBefore(LocalDate.now(clock))) {
            throw new InvalidSearchException("checkIn no puede ser una fecha pasada");
        }
        Search search = new Search(UUID.randomUUID().toString(), hotelSearch);
        log.debug("REGISTER SEARCH SERVICE - Registrando busqueda searchId={} hotelId={}", search.searchId(), hotelSearch.hotelId());
        publisher.publish(search);
        return search.searchId();
    }
}
