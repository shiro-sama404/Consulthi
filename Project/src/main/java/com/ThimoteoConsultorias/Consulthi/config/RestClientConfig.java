package com.ThimoteoConsultorias.Consulthi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig
{
    /**
     * Define o RestTemplate como um bean para injeção de dependência.
     * Necessário para o AdminApiRestClient se comunicar com a API REST do backend.
     * @return Uma instância de RestTemplate.
     */
    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }
}