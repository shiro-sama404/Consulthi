package com.ThimoteoConsultorias.Consulthi.desktop;

import com.ThimoteoConsultorias.Consulthi.model.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class AdminApiRestClient
{
    private final RestTemplate restTemplate;
    private final String BASE_URL = "http://localhost:8080/admin/api"; 

    public AdminApiRestClient(RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
    }

    /**
     * Busca todos os usuários do sistema.
     * Mapeia para GET /admin/api/users.
     */
    public List<User> findAllUsers()
    {
        User[] users = restTemplate.getForObject(BASE_URL + "/users", User[].class);
        return users != null ? Arrays.asList(users) : List.of();
    }
    
    /**
     * Busca profissionais pendentes de aprovação (RF01).
     * Mapeia para GET /admin/api/pending-professionals.
     */
    public List<User> getPendingProfessionals()
    {
        User[] users = restTemplate.getForObject(BASE_URL + "/pending-professionals", User[].class);
        return users != null ? Arrays.asList(users) : List.of();
    }

    /**
     * Aprova o registro de um Profissional (RF01).
     * Mapeia para POST /admin/api/approve/{userId}.
     */
    public void approveProfessionalRegistration(Long userId)
    {
        // O endpoint é um POST que executa a lógica e retorna 200/204 (void)
        restTemplate.postForLocation(BASE_URL + "/approve/" + userId, null);
    }
    
    /**
     * Remove um usuário (RF08).
     * Mapeia para POST /admin/remove/{userId}.
     */
    public void removeUser(Long userId)
    {
        restTemplate.postForLocation("http://localhost:8080/admin/remove/" + userId, null);
    }
}