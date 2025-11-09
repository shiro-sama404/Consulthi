package com.ThimoteoConsultorias.Consulthi.model;

import com.ThimoteoConsultorias.Consulthi.enums.GoalType;
import com.ThimoteoConsultorias.Consulthi.enums.RoutineLevel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.DiscriminatorValue;

import java.util.List;
import java.util.Set;

@SuperBuilder
@Entity
@DiscriminatorValue("ROUTINE")
@Getter
@Setter
@NoArgsConstructor
public class Routine extends Content
{
    @Enumerated(EnumType.STRING)
    private RoutineLevel routineLevel;

    @ElementCollection
    @CollectionTable(name = "routine_goals", joinColumns = @JoinColumn(name = "routine_id"))
    @Enumerated(EnumType.STRING)
    private Set<GoalType> goals;

    @OneToMany
    @JoinColumn(name = "routine_id")
    @OrderColumn(name = "training_order")
    private List<Training> trainings;
}