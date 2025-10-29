package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.config.AppUser;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements UserDetailsService 
{
    private final PasswordEncoder passwordEncoder;
    private final UserRepository  userRepository;

    public AuthenticationService(PasswordEncoder passwordEncoder, UserRepository userRepository)
    {
        this.passwordEncoder   = passwordEncoder;
        this.userRepository = userRepository;
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

    @Override
    public AppUser loadUserByUsername(String username) throws UsernameNotFoundException
    {
        User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuário '" + username + "' não encontrado."));

        return new AppUser(user);
    }

    public boolean checkPasswordByUsername(String rawPassword, String username)
    {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário '"+username+"'' não encontrado."));

        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    public boolean checkPasswordByEmail(String email, String username)
    {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário '"+username+"'' não encontrado."));

        return passwordEncoder.matches(email, user.getPasswordHash());
    }
}