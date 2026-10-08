package com.sling.hotelsearch.infrastructure.rest;

import com.sling.hotelsearch.domain.exception.InvalidSearchException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Convierte fechas del formato dd/MM/yyyy del contrato REST a {@link LocalDate} y viceversa. */
final class DateFormats {

    // STRICT para rechazar fechas inexistentes como 31/02/2023. DateTimeFormatter es thread-safe.
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private DateFormats() {
    }

    static LocalDate parse(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidSearchException(field + " es obligatorio");
        }
        try {
            return LocalDate.parse(value, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidSearchException(field + " debe tener formato dd/MM/yyyy");
        }
    }

    static String format(LocalDate date) {
        return FORMATTER.format(date);
    }
}
