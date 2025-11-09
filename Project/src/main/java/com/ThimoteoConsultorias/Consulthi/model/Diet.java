package com.ThimoteoConsultorias.Consulthi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@SuperBuilder
@DiscriminatorValue("DIET")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Diet extends Content
{  
    @Column(columnDefinition = "TEXT")
    private String mealsEspecifications;
}