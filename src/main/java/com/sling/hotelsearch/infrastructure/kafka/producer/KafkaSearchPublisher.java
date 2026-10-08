package com.sling.hotelsearch.infrastructure.kafka.producer;

import com.sling.hotelsearch.application.exception.EventPublishException;
import com.sling.hotelsearch.application.port.out.SearchEventPublisher;
import com.sling.hotelsearch.domain.model.Search;
import com.sling.hotelsearch.infrastructure.kafka.KafkaTopics;
import com.sling.hotelsearch.infrastructure.kafka.SearchMessage;
import com.sling.hotelsearch.infrastructure.kafka.SearchMessageMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaSearchPublisher implements SearchEventPublisher {

    private static final long SEND_TIMEOUT_SECONDS = 5;

    private final KafkaTemplate<String, SearchMessage> kafkaTemplate;

    public KafkaSearchPublisher(KafkaTemplate<String, SearchMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(Search search) {
        SearchMessage message = SearchMessageMapper.toMessage(search);
        try {
            // Se espera la confirmacion para no devolver un searchId de una busqueda que nunca se publico.
            kafkaTemplate.send(KafkaTopics.HOTEL_AVAILABILITY_SEARCHES, search.searchId(), message)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventPublishException("Publicacion interrumpida de disponibilidad", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new EventPublishException("No se pudo publicar la busqueda de disponibilidad" + search.searchId(), e);
        }
    }
}
