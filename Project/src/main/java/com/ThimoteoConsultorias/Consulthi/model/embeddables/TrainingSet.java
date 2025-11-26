package com.ThimoteoConsultorias.Consulthi.model.embeddables;

import com.ThimoteoConsultorias.Consulthi.enums.TrainingTechnique;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TrainingSet
{
    private Integer exerciseOrder;
    private Long exerciseId; 
    private Integer repetitions;
    private Integer restTimeSeconds;
    private Integer sets;

    @Enumerated(EnumType.STRING)
    private TrainingTechnique technique;
    
    // Campos opcionais para Carga ou Duração
    private Float loadInKg;
    private Integer durationSeconds;
}