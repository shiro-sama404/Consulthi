package com.ThimoteoConsultorias.Consulthi.dto;

import com.ThimoteoConsultorias.Consulthi.enums.TrainingTechnique;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSetDTO
{
    private Integer exerciseOrder; 
    private Long exerciseId; 
    private Integer sets;
    private Integer repetitions;
    private Integer restTimeSeconds;
    private TrainingTechnique technique;
    
    // Campos opcionais de Carga/Duração
    private Float loadInKg; 
    private Integer durationSeconds;
}