package com.sling.hotelsearch.application.service;

import com.sling.hotelsearch.application.port.out.SearchEventPublisher;
import com.sling.hotelsearch.domain.model.HotelSearch;
import com.sling.hotelsearch.domain.model.Search;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterSearchServiceTest {

    private final HotelSearch hotelSearch =
            new HotelSearch("h1", LocalDate.of(2023, 12, 29), LocalDate.of(2023, 12, 31), List.of(30, 29, 1, 3));

    @Mock
    private SearchEventPublisher publisher;

    @Test
    void publicaLaBusquedaYDevuelveElMismoId() {
        RegisterSearchService service = new RegisterSearchService(publisher);

        String id = service.register(hotelSearch);

        ArgumentCaptor<Search> captor = ArgumentCaptor.forClass(Search.class);
        verify(publisher).publish(captor.capture());
        assertAll(
                () -> assertEquals(id, captor.getValue().searchId()),
                () -> assertEquals(hotelSearch, captor.getValue().hotelSearch()));
    }

    @Test
    void busquedasIgualesRecibenIdsDistintos() {
        RegisterSearchService service = new RegisterSearchService(publisher);

        String first = service.register(hotelSearch);
        String second = service.register(hotelSearch);

        assertAll(
                () -> assertNotEquals(first, second),
                () -> verify(publisher, times(2)).publish(org.mockito.ArgumentMatchers.any(Search.class)));
    }
}
