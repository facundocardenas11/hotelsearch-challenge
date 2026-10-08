package com.sling.hotelsearch.infrastructure.kafka.producer;

import com.sling.hotelsearch.application.exception.EventPublishException;
import com.sling.hotelsearch.domain.model.HotelSearch;
import com.sling.hotelsearch.domain.model.Search;
import com.sling.hotelsearch.infrastructure.kafka.KafkaTopics;
import com.sling.hotelsearch.infrastructure.kafka.SearchMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaSearchPublisherTest {

    private final Search search = new Search("id-1",
            new HotelSearch("h1", LocalDate.of(2023, 12, 29), LocalDate.of(2023, 12, 31), List.of(30, 29, 1, 3)));

    @Mock
    private KafkaTemplate<String, SearchMessage> kafkaTemplate;

    @Test
    void publicaElMensajeEnElTopicUsandoElSearchIdComoClave() {
        when(kafkaTemplate.send(anyString(), anyString(), any(SearchMessage.class)))
                .thenReturn(CompletableFuture.<SendResult<String, SearchMessage>>completedFuture(null));

        new KafkaSearchPublisher(kafkaTemplate).publish(search);

        ArgumentCaptor<SearchMessage> captor = ArgumentCaptor.forClass(SearchMessage.class);
        verify(kafkaTemplate).send(eq(KafkaTopics.HOTEL_AVAILABILITY_SEARCHES), eq("id-1"), captor.capture());
        SearchMessage sent = captor.getValue();
        assertAll(
                () -> assertEquals("id-1", sent.searchId()),
                () -> assertEquals("h1", sent.hotelId()),
                () -> assertEquals(LocalDate.of(2023, 12, 29), sent.checkIn()),
                () -> assertEquals(LocalDate.of(2023, 12, 31), sent.checkOut()),
                () -> assertEquals(List.of(30, 29, 1, 3), sent.ages()));
    }

    @Test
    void lanzaEventPublishExceptionSiKafkaFalla() {
        when(kafkaTemplate.send(anyString(), anyString(), any(SearchMessage.class)))
                .thenReturn(CompletableFuture.<SendResult<String, SearchMessage>>failedFuture(new IllegalStateException("boom")));
        KafkaSearchPublisher publisher = new KafkaSearchPublisher(kafkaTemplate);

        assertThrows(EventPublishException.class, () -> publisher.publish(search));
    }
}
