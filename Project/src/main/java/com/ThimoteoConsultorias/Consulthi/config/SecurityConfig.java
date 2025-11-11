package com.ThimoteoConsultorias.Consulthi.config;

import com.ThimoteoConsultorias.Consulthi.service.AppUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.NoHandlerFoundException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig
{
    private final AppUserDetailsService        appUserDetailsService;
    private final PasswordEncoder              passwordEncoder;

    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final LoginSuccessHandler          loginSuccessHandler;

    public SecurityConfig
    (
        AppUserDetailsService appUserDetailsService,
        PasswordEncoder passwordEncoder,
        AuthenticationFailureHandler authenticationFailureHandler,
        LoginSuccessHandler loginSuccessHandler
    )
    {
        this.appUserDetailsService = appUserDetailsService;
        this.passwordEncoder       = passwordEncoder;

        this.authenticationFailureHandler = authenticationFailureHandler;
        this.loginSuccessHandler          = loginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
            .authorizeHttpRequests(auth -> auth
                // Permite acesso público às páginas de informação e arquivos estáticos
                .requestMatchers("/home/", "/", "/login", "/register", "/tutorial", "/css/**", "/js/**", "/img/**").permitAll()
                // Exige autenticação para qualquer outra página
                .anyRequest().authenticated()      
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(loginSuccessHandler) 
                .failureHandler(authenticationFailureHandler)
                .defaultSuccessUrl("/home", true) 
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            // Habilita exceções de mapeamento não encontrado
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/login");
                })
                .accessDeniedPage("/error")
            )
            .setSharedObject(NoHandlerFoundException.class, null);
        
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception
    {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.userDetailsService(appUserDetailsService).passwordEncoder(passwordEncoder);
        return auth.build();
    }
}