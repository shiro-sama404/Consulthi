package com.ThimoteoConsultorias.Consulthi.dto;

import com.ThimoteoConsultorias.Consulthi.enums.MuscleGroup;

import lombok.Builder;

import java.util.List;
import java.util.Set;

@Builder
public record TrainingDTO
(
    Long id,
    String name,
    Set<MuscleGroup> targetMuscleGroups,
    List<TrainingSetDTO> trainingSets
)
{}