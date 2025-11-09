package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.enums.ExpertiseArea;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.repository.ProfessionalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class ProfessionalService
{
    private final ProfessionalRepository professionalRepository;

    public ProfessionalService(ProfessionalRepository professionalRepository)
    {
        this.professionalRepository = professionalRepository;
    }

    @Transactional
    public Professional createProfessionalProfile(User user, String register, Set<ExpertiseArea> areasOfExpertise)
    {
        Professional professional = Professional.builder()
                .user(user)
                .register(register)
                .expertiseAreas(areasOfExpertise)
                .build();
        
        return professionalRepository.save(professional);
    }

    public Professional getProfessionalById(Long id) throws ResourceNotFoundException
    {
        return professionalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Profissional de id '" + id +"' não encontrado."));
    }
}