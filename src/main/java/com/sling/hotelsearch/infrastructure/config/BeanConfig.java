package com.sling.hotelsearch.infrastructure.config;

import com.sling.hotelsearch.application.port.in.CountSearchUseCase;
import com.sling.hotelsearch.application.port.in.RegisterSearchUseCase;
import com.sling.hotelsearch.application.port.out.SearchEventPublisher;
import com.sling.hotelsearch.application.port.out.SearchRepository;
import com.sling.hotelsearch.application.service.CountSearchService;
import com.sling.hotelsearch.application.service.RegisterSearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;


@Configuration
public class BeanConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public RegisterSearchUseCase registerSearchUseCase(SearchEventPublisher publisher, Clock clock) {
        return new RegisterSearchService(publisher, clock);
    }

    @Bean
    public CountSearchUseCase countSearchUseCase(SearchRepository repository) {
        return new CountSearchService(repository);
    }
}
