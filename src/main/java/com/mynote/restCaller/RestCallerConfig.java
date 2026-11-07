package com.mynote.restCaller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestCallerConfig {

    @Value("${mynote.service.url}")
    private String serviceUrl;

    @Bean
    public RestClient createRestClient() {
        return RestClient.builder()
                .baseUrl(serviceUrl)
                .build();
    }
}
