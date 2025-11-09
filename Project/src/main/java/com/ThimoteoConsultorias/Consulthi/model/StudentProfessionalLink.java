package com.ThimoteoConsultorias.Consulthi.model;

import com.ThimoteoConsultorias.Consulthi.enums.LinkStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class StudentProfessionalLink
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "professional_id")
    private Professional professional;
    
    @Enumerated(EnumType.STRING)
    private LinkStatus status;
    
    private LocalDateTime dateRequest;
}