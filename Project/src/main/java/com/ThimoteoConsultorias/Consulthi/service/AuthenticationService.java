package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.config.AppUser;
import com.ThimoteoConsultorias.Consulthi.model.User;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService /*implements UserDetailsService*/ 
{
    private final PasswordEncoder passwordEncoder;
    private final UserService  userService;

    public AuthenticationService(PasswordEncoder passwordEncoder, UserService userService)
    {
        this.passwordEncoder   = passwordEncoder;
        this.userService = userService;
    }

    public Long getAuthenticatedUserId()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return ((AppUser) userDetails).getId();
        }

        throw new IllegalStateException("Usuário não autenticado");
    }

    public boolean checkPasswordByUsername(String rawPassword, String username)
    {
        User user = userService.getUserByUsername(username);
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    public boolean checkPasswordByEmail(String email, String username)
    {
        User user = userService.getUserByEmail(email);
        return passwordEncoder.matches(email, user.getPasswordHash());
    }
}