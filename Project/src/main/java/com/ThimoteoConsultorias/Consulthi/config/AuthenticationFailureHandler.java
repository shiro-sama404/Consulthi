package com.ThimoteoConsultorias.Consulthi.config;

import com.ThimoteoConsultorias.Consulthi.service.UserService;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler
{
    private final UserService userService;

    public AuthenticationFailureHandler(UserService userService)
    {
        super("/login?error"); 
        this.userService = userService;
    }

    @Override
    public void onAuthenticationFailure
    (
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    )
    throws IOException, ServletException
    {
        System.out.println("Entrou 123");
        String username = request.getParameter("username");
        System.out.println("Saiu 123");

        if (username != null) 
            userService.registerFailedLogin(username);

        super.onAuthenticationFailure(request, response, exception);
    }
}