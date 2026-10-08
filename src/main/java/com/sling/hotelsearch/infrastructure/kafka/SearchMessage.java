package com.sling.hotelsearch.infrastructure.kafka;

import java.time.LocalDate;
import java.util.List;


public record SearchMessage(String searchId, String hotelId, LocalDate checkIn, LocalDate checkOut, List<Integer> ages) {
}
