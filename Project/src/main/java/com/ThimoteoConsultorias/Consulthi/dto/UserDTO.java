package com.ThimoteoConsultorias.Consulthi.dto;

import com.ThimoteoConsultorias.Consulthi.enums.ExpertiseArea;
import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.model.User;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record UserDTO
(
    Long               id,
    String             fullName,
    String             username,
    String             email,
    String             phoneNumber,
    String             rawPassword,
    Set<Role>          roles,
    LocalDate          dateBirth,
    String             register,
    Set<ExpertiseArea> expertiseAreas
)
{
    public static UserDTO fromUser(User user)
    {
        // TODO
        // Buscar os dados de Professional/Student
        // e incluir aqui
        return UserDTO.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .dateBirth(user.getDateBirth())
            .username(user.getUsername())
            .roles(user.getRoles())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .build();
    }
}