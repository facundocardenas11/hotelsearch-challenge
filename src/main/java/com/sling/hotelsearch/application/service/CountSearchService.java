package com.sling.hotelsearch.application.service;

import com.sling.hotelsearch.application.port.in.CountSearchUseCase;
import com.sling.hotelsearch.application.port.out.SearchRepository;
import com.sling.hotelsearch.domain.exception.InvalidSearchException;
import com.sling.hotelsearch.domain.exception.SearchNotFoundException;
import com.sling.hotelsearch.domain.model.SearchCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CountSearchService implements CountSearchUseCase {

    private final SearchRepository repository;
    private static final Logger log = LoggerFactory.getLogger(CountSearchService.class);

    public CountSearchService(SearchRepository repository) {
        this.repository = repository;
    }

    @Override
    public SearchCount count(String searchId) {
        if (searchId == null || searchId.isBlank()) {
            throw new InvalidSearchException("searchId es obligatorio");
        }
        log.debug("COUNT SERVICE - Consultando conteo searchId={}", searchId);
        return repository.findCountBySearchId(searchId)
                .orElseThrow(() -> new SearchNotFoundException(searchId));
    }
}
