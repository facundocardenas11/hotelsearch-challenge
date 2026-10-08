package com.sling.hotelsearch.application.port.out;

import com.sling.hotelsearch.domain.model.Search;

public interface SearchEventPublisher {

    void publish(Search search);
}
