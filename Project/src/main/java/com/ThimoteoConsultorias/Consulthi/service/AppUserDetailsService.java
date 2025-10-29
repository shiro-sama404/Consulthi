package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.config.AppUser;
import com.ThimoteoConsultorias.Consulthi.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService
{

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        return userRepository.findByUsername(username)
            .map(AppUser::new)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário '" + username +"' não encontrado."));
    }
}