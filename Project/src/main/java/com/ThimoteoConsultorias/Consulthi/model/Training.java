package com.ThimoteoConsultorias.Consulthi.model;

import com.ThimoteoConsultorias.Consulthi.enums.MuscleGroup;
import com.ThimoteoConsultorias.Consulthi.model.embeddables.TrainingSet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.List;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "training")
public class Training
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    @CollectionTable(name = "training_muscle_groups", joinColumns = @JoinColumn(name = "training_id"))
    @Enumerated(EnumType.STRING)
    private Set<MuscleGroup> targetMuscleGroups;

    @ElementCollection
    @CollectionTable(name = "training_sets", joinColumns = @JoinColumn(name = "training_id"))
    @OrderColumn(name = "set_index")
    private List<TrainingSet> trainingSets;
}