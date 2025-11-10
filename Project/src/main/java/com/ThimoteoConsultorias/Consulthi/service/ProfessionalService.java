package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.enums.ExpertiseArea;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.repository.ProfessionalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ProfessionalService
{
    // ----------------------------------------------------
    // 1. DEPENDÊNCIAS
    // ----------------------------------------------------
    private final ProfessionalRepository professionalRepository;

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public ProfessionalService(ProfessionalRepository professionalRepository)
    {
        this.professionalRepository = professionalRepository;
    }

    // ----------------------------------------------------
    // 3. MÉTODOS DE DOMÍNIO
    // ----------------------------------------------------

    /*
     * CREATE
     */

    /**
     * Cria o perfil profissional para um novo usuário (RF01).
     * @param user O User recém-criado.
     * @param register O número de registro no conselho.
     * @param areasOfExpertise As áreas de especialidade.
     * @return A entidade Professional persistida.
     */
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

    /*
     * READ
     */

    /**
     * Retorna o perfil Professional pelo ID (que é o mesmo ID do User).
     * @param id O ID do User/Professional.
     * @return O Optional contendo o perfil Professional.
     */
    public Professional getProfessionalById(Long id)
    throws ResourceNotFoundException
    {
        return professionalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Profissional de id '" + id +"' não encontrado."));
    }
    
    /**
     * Retorna todos os perfis Professional no sistema.
     * @return Lista de todos os profissionais.
     */
    public List<Professional> getAllProfessionals()
    {
        return professionalRepository.findAll();
    }
}