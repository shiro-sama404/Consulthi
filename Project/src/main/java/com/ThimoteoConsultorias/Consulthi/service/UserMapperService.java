package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.dto.UserDTO;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.model.Student;
import com.ThimoteoConsultorias.Consulthi.model.Professional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserMapperService
{
    // ----------------------------------------------------
    // 1. DEPENDÊNCIAS
    // ----------------------------------------------------
    private final StudentService studentService;
    private final ProfessionalService professionalService;
    private final PasswordEncoder passwordEncoder;

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public UserMapperService
    (
        StudentService studentService, 
        ProfessionalService professionalService,
        PasswordEncoder passwordEncoder
    )
    {
        this.studentService = studentService;
        this.professionalService = professionalService;
        this.passwordEncoder = passwordEncoder;
    }

    // ----------------------------------------------------
    // 3. MÉTODOS DE MAPEO (sem transação, pois é apenas conversão)
    // ----------------------------------------------------

    /**
     * Converte um UserDTO em uma entidade User, aplicando o hash na senha.
     * Usado na criação e atualização do User.
     * @param dto O DTO de entrada.
     * @param rawPassword A senha em texto puro a ser criptografada.
     * @return A entidade User pronta para persistência.
     */
    public User toUser(UserDTO dto, String rawPassword) {
        
        String passwordHash = (rawPassword != null && !rawPassword.isEmpty()) 
                                ? passwordEncoder.encode(rawPassword)
                                : null;

        return User.builder()
            .id(dto.id())
            .fullName(dto.fullName())
            .dateBirth(dto.dateBirth())
            .username(dto.username())
            .roles(dto.roles())
            .email(dto.email())
            .phoneNumber(dto.phoneNumber())
            .passwordHash(passwordHash)
            .dateLogon(LocalDateTime.now())
            .failedLoginAttempts(0)
            .active(false)
            .build();
    }

    /**
     * Converte a entidade User (e perfis associados) para o UserDTO,
     * agregando dados de Student e Professional.
     * @param user A entidade User carregada.
     * @return O DTO com dados completos para apresentação.
     */
    public UserDTO toDTO(User user)
    {
        UserDTO.UserDTOBuilder builder = UserDTO.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .dateBirth(user.getDateBirth())
            .username(user.getUsername())
            .roles(user.getRoles())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .rawPassword(null);

        try
        {
            Professional professional = professionalService.getProfessionalById(user.getId());
            builder.register(professional.getRegister())
                .expertiseAreas(professional.getExpertiseAreas());
        }
        catch (ResourceNotFoundException ignored) {} 
       
        try
        {
            Student student = studentService.getStudentById(user.getId());
            builder.goal(student.getGoal());
        } 
        catch (ResourceNotFoundException ignored) {} 
        
        return builder.build();
    }
}