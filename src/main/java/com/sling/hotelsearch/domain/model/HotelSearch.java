package com.sling.hotelsearch.domain.model;

import com.sling.hotelsearch.domain.exception.InvalidSearchException;

import java.time.LocalDate;
import java.util.List;


public record HotelSearch(String hotelId, LocalDate checkIn, LocalDate checkOut, List<Integer> ages) {

    public HotelSearch {
        if (hotelId == null || hotelId.isBlank()) {
            throw new InvalidSearchException("hotelId es obligatorio");
        }
        if (checkIn == null) {
            throw new InvalidSearchException("checkIn es obligatorio");
        }
        if (checkOut == null) {
            throw new InvalidSearchException("checkOut es obligatorio");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new InvalidSearchException("checkIn debe ser anterior a checkOut");
        }
        if (ages == null || ages.isEmpty()) {
            throw new InvalidSearchException("ages es obligatorio");
        }
        if (ages.stream().anyMatch(age -> age == null || age < 0)) {
            throw new InvalidSearchException("Las edades deben ser >= 0");
        }
        ages = List.copyOf(ages);
    }
}
