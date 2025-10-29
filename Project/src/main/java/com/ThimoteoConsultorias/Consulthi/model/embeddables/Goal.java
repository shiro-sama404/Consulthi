package com.ThimoteoConsultorias.Consulthi.model.embeddables;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Embeddable;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Goal
{
    private Float targetWeight; 
    private Float targetBodyFat;
}