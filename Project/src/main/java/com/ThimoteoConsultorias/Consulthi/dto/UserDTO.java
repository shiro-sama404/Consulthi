package com.ThimoteoConsultorias.Consulthi.dto;

import com.ThimoteoConsultorias.Consulthi.enums.ExpertiseArea;
import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.model.embeddables.Goal;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
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
    Set<ExpertiseArea> expertiseAreas,
    List<Long>         selectedProfessionalIds,
    Goal goal
)
{}