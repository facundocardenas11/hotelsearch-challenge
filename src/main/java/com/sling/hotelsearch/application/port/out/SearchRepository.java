package com.sling.hotelsearch.application.port.out;

import com.sling.hotelsearch.domain.model.Search;
import com.sling.hotelsearch.domain.model.SearchCount;

import java.util.Optional;

public interface SearchRepository {

    void save(Search search);

    Optional<SearchCount> findCountBySearchId(String searchId);
}
