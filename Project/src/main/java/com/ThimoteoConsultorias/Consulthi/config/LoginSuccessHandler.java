package com.ThimoteoConsultorias.Consulthi.config;

import com.ThimoteoConsultorias.Consulthi.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler
{
    private final UserService userService;

    public LoginSuccessHandler(UserService userService)
    {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess
    (
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    )
    throws IOException, ServletException
    {
        String username = authentication.getName();
        
        userService.resetLoginAttempts(username);
        
        // ----------------------------------------------------
        // Lógica de Redirecionamento Baseada em ROLES
        // ----------------------------------------------------
        Set<String> roles = authentication.getAuthorities().stream()
                             .map(GrantedAuthority::getAuthority)
                             .collect(Collectors.toSet());

        String targetUrl = determineTargetUrl(roles);
        
        response.sendRedirect(request.getContextPath() + targetUrl);
    }
    
   /**
     * Determina a URL de destino com base na hierarquia de roles:
     * Administrador > Profissional > Aluno
     */
    private String determineTargetUrl(Set<String> roles)
    {
        if (roles.contains("ADMINISTRATOR"))
            return "/administrator/dashboard"; // 1ª Prioridade
        else if (roles.contains("COACH") || roles.contains("NUTRITIONIST") || roles.contains("PSYCHOLOGIST"))
            return "/professional/dashboard"; // 2ª Prioridade
        else if (roles.contains("STUDENT"))
            return "/student/dashboard"; // 3ª Prioridade
        
        return "/";
    }
}