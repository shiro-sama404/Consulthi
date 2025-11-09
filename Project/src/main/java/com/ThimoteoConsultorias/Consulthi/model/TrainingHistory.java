package com.ThimoteoConsultorias.Consulthi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_history")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TrainingHistory
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liga à qual INSTÂNCIA de Rotina este Treino executado pertence
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_instance_id", nullable = false)
    private RoutineInstance routineInstance; 
    
    // Liga ao Treino original (Template)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_template_id", nullable = false)
    private Training training;
    
    private LocalDateTime executionDateTime;
    
    private String studentNotes;
}