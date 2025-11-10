package com.ThimoteoConsultorias.Consulthi.config;

import com.ThimoteoConsultorias.Consulthi.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    public LoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        // Obtém o username do usuário autenticado
        String username = authentication.getName();
        
        // Reseta a contagem de falhas
        userService.resetLoginAttempts(username); 
        
        // Continua para a página inicial (ou a defaultSuccessUrl configurada)
        response.sendRedirect(request.getContextPath() + "/home");
    }
}