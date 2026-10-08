package com.sling.hotelsearch.application.service;

import com.sling.hotelsearch.application.port.out.SearchRepository;
import com.sling.hotelsearch.domain.exception.InvalidSearchException;
import com.sling.hotelsearch.domain.exception.SearchNotFoundException;
import com.sling.hotelsearch.domain.model.HotelSearch;
import com.sling.hotelsearch.domain.model.SearchCount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountSearchServiceTest {

    @Mock
    private SearchRepository repository;

    @Test
    void devuelveElConteoCuandoLaBusquedaExiste() {
        HotelSearch hs = new HotelSearch("h1", LocalDate.of(2023, 12, 29), LocalDate.of(2023, 12, 31), List.of(1));
        SearchCount expected = new SearchCount("abc", hs, 100);
        when(repository.findCountBySearchId("abc")).thenReturn(Optional.of(expected));

        assertEquals(expected, new CountSearchService(repository).count("abc"));
    }

    @Test
    void lanzaNotFoundCuandoNoExiste() {
        when(repository.findCountBySearchId("nope")).thenReturn(Optional.empty());
        CountSearchService service = new CountSearchService(repository);

        assertThrows(SearchNotFoundException.class, () -> service.count("nope"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rechazaSearchIdNuloOVacio(String searchId) {
        CountSearchService service = new CountSearchService(repository);

        assertThrows(InvalidSearchException.class, () -> service.count(searchId));
        verifyNoInteractions(repository);
    }
}
