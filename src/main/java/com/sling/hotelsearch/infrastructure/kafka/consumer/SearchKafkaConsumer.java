package com.sling.hotelsearch.infrastructure.kafka.consumer;

import com.sling.hotelsearch.application.port.out.SearchRepository;
import com.sling.hotelsearch.infrastructure.kafka.KafkaTopics;
import com.sling.hotelsearch.infrastructure.kafka.SearchMessage;
import com.sling.hotelsearch.infrastructure.kafka.SearchMessageMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class SearchKafkaConsumer {

    private final SearchRepository repository;

    public SearchKafkaConsumer(SearchRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = KafkaTopics.HOTEL_AVAILABILITY_SEARCHES)
    public void consume(SearchMessage message) {
        repository.save(SearchMessageMapper.toDomain(message));
    }
}
