package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final RatingServiceProperties ratingServiceProperties;

    @Bean
    public RestClient bookRatingRestClient(RestClient.Builder restClientBuilder) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(ratingServiceProperties.connectTimeout());
        requestFactory.setReadTimeout(ratingServiceProperties.readTimeout());

        return restClientBuilder
                .baseUrl(ratingServiceProperties.baseUrl().toString())
                .requestFactory(requestFactory)
                .build();
    }
}
