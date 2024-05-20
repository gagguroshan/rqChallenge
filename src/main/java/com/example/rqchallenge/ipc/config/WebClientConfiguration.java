package com.example.rqchallenge.ipc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration  {

	@Value("${dummy.endpoint.base.url}")
    private String dummyEndpointBaseUrl;
    @Value("${dummy.endpoint.cookie.key}")
    private String cookieKey;    
    @Value("${dummy.endpoint.cookie.value}")
    private String cookieValue;

    @Bean
    public WebClient.Builder webclient() {
        final int size = 16 * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();

        return WebClient
                .builder()
                .baseUrl(dummyEndpointBaseUrl)
                .defaultCookie(cookieKey,cookieValue)
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }
}