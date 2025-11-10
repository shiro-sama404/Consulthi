package com.ThimoteoConsultorias.Consulthi.dto;

import com.ThimoteoConsultorias.Consulthi.enums.ContentTag;
import com.ThimoteoConsultorias.Consulthi.enums.ContentType;
import com.ThimoteoConsultorias.Consulthi.enums.GoalType;
import com.ThimoteoConsultorias.Consulthi.enums.RoutineLevel;
import com.ThimoteoConsultorias.Consulthi.model.embeddables.ContentBlock; // Import correto

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Builder
public record ContentDTO
(
    Long id,
    String name,
    String description,
    ContentType contentType, 
    Set<Long> accessStudentIds,
    LocalDateTime creationDate,
    LocalDateTime lastModificationDate,
    
    // ==========================================================
    // DIET
    // ==========================================================
    String mealsEspecifications, 
    
    // ==========================================================
    // MATERIAL
    // ==========================================================
    Set<ContentTag> tags,
    List<ContentBlock> contentBlocks, 

    // ==========================================================
    // ROUTINE
    // ==========================================================
    RoutineLevel routineLevel,
    LocalDate startDate,
    LocalDate endDate,
    Set<GoalType> goals,
    
    List<TrainingDTO> trainingDtos
) 
{}