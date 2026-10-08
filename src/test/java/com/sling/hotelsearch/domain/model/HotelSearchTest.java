package com.sling.hotelsearch.domain.model;

import com.sling.hotelsearch.domain.exception.InvalidSearchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HotelSearchTest {

    private static final LocalDate IN = LocalDate.of(2023, 12, 29);
    private static final LocalDate OUT = LocalDate.of(2023, 12, 31);

    private static String messageOf(String hotelId, LocalDate in, LocalDate out, List<Integer> ages) {
        return assertThrows(InvalidSearchException.class, () -> new HotelSearch(hotelId, in, out, ages)).getMessage();
    }

    @Test
    void creaBusquedaValida() {
        HotelSearch search = new HotelSearch("1234aBc", IN, OUT, List.of(30, 29, 1, 3));

        assertAll(
                () -> assertEquals("1234aBc", search.hotelId()),
                () -> assertEquals(IN, search.checkIn()),
                () -> assertEquals(OUT, search.checkOut()),
                () -> assertEquals(List.of(30, 29, 1, 3), search.ages()));
    }

    @Test
    void aceptaEdadCero() {
        HotelSearch search = new HotelSearch("h", IN, OUT, List.of(0));

        assertEquals(List.of(0), search.ages());
    }

    @Test
    void copiaDefensivaDeEdades() {
        List<Integer> original = new ArrayList<>(List.of(30, 29));
        HotelSearch search = new HotelSearch("h", IN, OUT, original);

        original.add(5);

        assertEquals(List.of(30, 29), search.ages());
    }

    @Test
    void laListaDeEdadesEsInmutable() {
        HotelSearch search = new HotelSearch("h", IN, OUT, List.of(1));
        List<Integer> ages = search.ages();

        assertThrows(UnsupportedOperationException.class, () -> ages.add(2));
    }

    @Test
    void elOrdenDeEdadesDiferenciaBusquedas() {
        HotelSearch a = new HotelSearch("h", IN, OUT, List.of(30, 29));
        HotelSearch b = new HotelSearch("h", IN, OUT, List.of(29, 30));

        assertNotEquals(a, b);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rechazaHotelIdNuloOVacio(String hotelId) {
        assertEquals("hotelId es obligatorio", messageOf(hotelId, IN, OUT, List.of(1)));
    }

    @Test
    void rechazaCheckInNulo() {
        assertEquals("checkIn es obligatorio", messageOf("h", null, OUT, List.of(1)));
    }

    @Test
    void rechazaCheckOutNulo() {
        assertEquals("checkOut es obligatorio", messageOf("h", IN, null, List.of(1)));
    }

    @Test
    void rechazaCheckInIgualACheckOut() {
        assertEquals("checkIn debe ser anterior a checkOut", messageOf("h", IN, IN, List.of(1)));
    }

    @Test
    void rechazaCheckInPosteriorACheckOut() {
        assertEquals("checkIn debe ser anterior a checkOut", messageOf("h", OUT, IN, List.of(1)));
    }

    @Test
    void rechazaEdadesNulasOVacias() {
        assertAll(
                () -> assertEquals("ages es obligatorio", messageOf("h", IN, OUT, null)),
                () -> assertEquals("ages es obligatorio", messageOf("h", IN, OUT, List.of())));
    }

    @Test
    void rechazaEdadNegativa() {
        assertEquals("Las edades deben ser >= 0", messageOf("h", IN, OUT, List.of(5, -1)));
    }

    @Test
    void rechazaEdadNulaDentroDeLaLista() {
        assertEquals("Las edades deben ser >= 0", messageOf("h", IN, OUT, Arrays.asList(5, null)));
    }
}
