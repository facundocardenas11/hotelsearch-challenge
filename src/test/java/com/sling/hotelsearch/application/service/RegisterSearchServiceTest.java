package com.sling.hotelsearch.application.service;

import com.sling.hotelsearch.application.port.out.SearchEventPublisher;
import com.sling.hotelsearch.domain.exception.InvalidSearchException;
import com.sling.hotelsearch.domain.model.HotelSearch;
import com.sling.hotelsearch.domain.model.Search;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterSearchServiceTest {

    private final HotelSearch hotelSearch =
            new HotelSearch("h1", LocalDate.of(2023, 12, 29), LocalDate.of(2023, 12, 31), List.of(30, 29, 1, 3));

    private final Clock clock = Clock.fixed(Instant.parse("2023-12-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private SearchEventPublisher publisher;

    @Test
    void publicaLaBusquedaYDevuelveElMismoId() {
        RegisterSearchService service = new RegisterSearchService(publisher, clock);

        String id = service.register(hotelSearch);

        ArgumentCaptor<Search> captor = ArgumentCaptor.forClass(Search.class);
        verify(publisher).publish(captor.capture());
        assertAll(
                () -> assertEquals(id, captor.getValue().searchId()),
                () -> assertEquals(hotelSearch, captor.getValue().hotelSearch()));
    }

    @Test
    void busquedasIgualesRecibenIdsDistintos() {
        RegisterSearchService service = new RegisterSearchService(publisher, clock);

        String first = service.register(hotelSearch);
        String second = service.register(hotelSearch);

        assertAll(
                () -> assertNotEquals(first, second),
                () -> verify(publisher, times(2)).publish(org.mockito.ArgumentMatchers.any(Search.class)));
    }
    @Test
    void rechazaCheckInEnFechaPasada() {
        HotelSearch pasada = new HotelSearch("h1", LocalDate.of(2023, 11, 30), LocalDate.of(2023, 12, 2), List.of(30));
        RegisterSearchService service = new RegisterSearchService(publisher, clock);

        InvalidSearchException ex = assertThrows(InvalidSearchException.class, () -> service.register(pasada));

        assertAll(
                () -> assertEquals("checkIn no puede ser una fecha pasada", ex.getMessage()),
                () -> verifyNoInteractions(publisher));
    }

    @Test
    void aceptaCheckInDeHoy() {
        HotelSearch hoy = new HotelSearch("h1", LocalDate.of(2023, 12, 1), LocalDate.of(2023, 12, 3), List.of(30));

        assertNotNull(new RegisterSearchService(publisher, clock).register(hoy));
    }
}
