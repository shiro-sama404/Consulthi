package com.ThimoteoConsultorias.Consulthi.dto;

import com.ThimoteoConsultorias.Consulthi.enums.ContentTag;
import com.ThimoteoConsultorias.Consulthi.enums.ContentType;
import com.ThimoteoConsultorias.Consulthi.enums.GoalType;
import com.ThimoteoConsultorias.Consulthi.enums.RoutineLevel;
import com.ThimoteoConsultorias.Consulthi.model.embeddables.ContentBlock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO
{
    private Long id;
    private String name;
    private String description;
    private ContentType contentType; 
    private Set<Long> accessStudentIds;
    private LocalDateTime creationDate;
    private LocalDateTime lastModificationDate;
    
    // ==========================================================
    // DIET
    // ==========================================================
    private String mealsEspecifications; 
    
    // ==========================================================
    // MATERIAL
    // ==========================================================
    private Set<ContentTag> tags;
    private List<ContentBlock> contentBlocks; 

    // ==========================================================
    // ROUTINE
    // ==========================================================
    private RoutineLevel routineLevel;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<GoalType> goals;
    
    private List<TrainingDTO> trainingDtos;
}