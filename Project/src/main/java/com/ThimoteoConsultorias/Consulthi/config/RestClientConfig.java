package com.ThimoteoConsultorias.Consulthi.config;

import org.springframework.boot.web.client.RestTemplateBuilder; // Importe este
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig
{
    /**
     * Define o RestTemplate como um bean para injeção de dependência.
     * Configura o RestTemplate para usar HTTP Basic Authentication
     * com as credenciais do admin criadas no DataInitializer.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder)
    {
        return builder
            .basicAuthentication("admin1", "123456")
            .build();
    }
}