package com.sling.hotelsearch.infrastructure.kafka.consumer;

import com.sling.hotelsearch.application.port.out.SearchRepository;
import com.sling.hotelsearch.domain.model.HotelSearch;
import com.sling.hotelsearch.domain.model.Search;
import com.sling.hotelsearch.infrastructure.kafka.SearchMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchKafkaConsumerTest {

    @Mock
    private SearchRepository repository;

    @Test
    void persisteLaBusquedaRecibida() {
        SearchMessage message = new SearchMessage(
                "id-1", "h1", LocalDate.of(2023, 12, 29), LocalDate.of(2023, 12, 31), List.of(30, 29, 1, 3));

        new SearchKafkaConsumer(repository).consume(message);

        verify(repository).save(new Search("id-1",
                new HotelSearch("h1", LocalDate.of(2023, 12, 29), LocalDate.of(2023, 12, 31), List.of(30, 29, 1, 3))));
    }
}
