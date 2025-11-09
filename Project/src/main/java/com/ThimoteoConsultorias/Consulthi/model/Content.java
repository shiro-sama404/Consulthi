package com.ThimoteoConsultorias.Consulthi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Estratégia de herança em uma única tabela
@DiscriminatorColumn(name = "content_type", discriminatorType = DiscriminatorType.STRING) // Coluna para identificar o tipo
@Table(name = "content")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Content
{ 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime creationDate;
    private LocalDateTime lastModificationDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional creator;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "content_access_students", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "student_id")
    private Set<Long> accessStudentIds; 
}