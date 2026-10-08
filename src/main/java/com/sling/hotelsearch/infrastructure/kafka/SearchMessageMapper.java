package com.sling.hotelsearch.infrastructure.kafka;

import com.sling.hotelsearch.domain.model.HotelSearch;
import com.sling.hotelsearch.domain.model.Search;

public final class SearchMessageMapper {

    private SearchMessageMapper() {
    }

    public static SearchMessage toMessage(Search search) {
        HotelSearch hs = search.hotelSearch();
        return new SearchMessage(search.searchId(), hs.hotelId(), hs.checkIn(), hs.checkOut(), hs.ages());
    }

    public static Search toDomain(SearchMessage message) {
        return new Search(
                message.searchId(),
                new HotelSearch(message.hotelId(), message.checkIn(), message.checkOut(), message.ages()));
    }
}
