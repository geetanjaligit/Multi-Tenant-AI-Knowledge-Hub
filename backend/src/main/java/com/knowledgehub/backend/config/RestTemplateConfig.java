package com.knowledgehub.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // RestTemplate is the "Browser" of Spring Boot. 
    // It allows our code to visit other websites or APIs (like our Python service).
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
