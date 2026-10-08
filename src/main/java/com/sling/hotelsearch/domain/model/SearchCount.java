package com.sling.hotelsearch.domain.model;

public record SearchCount(String searchId, HotelSearch hotelSearch, long count) {
}
