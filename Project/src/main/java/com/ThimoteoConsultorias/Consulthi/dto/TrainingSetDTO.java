package com.ThimoteoConsultorias.Consulthi.dto;

import com.ThimoteoConsultorias.Consulthi.enums.TrainingTechnique;

import lombok.Builder;

@Builder
public record TrainingSetDTO
(
    Integer exerciseOrder, 
    Long exerciseId, 
    Integer repetitions,
    Integer restTimeSeconds,
    TrainingTechnique technique,
    
    // Campos opcionais de Carga/Duração
    Float loadInKg, 
    Integer durationSeconds
)
{}