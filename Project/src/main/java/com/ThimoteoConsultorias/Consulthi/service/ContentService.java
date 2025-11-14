package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.dto.ContentDTO;
import com.ThimoteoConsultorias.Consulthi.enums.ContentType;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.Content;
import com.ThimoteoConsultorias.Consulthi.model.Diet;
import com.ThimoteoConsultorias.Consulthi.model.Material;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.model.Routine;
import com.ThimoteoConsultorias.Consulthi.model.Training;
import com.ThimoteoConsultorias.Consulthi.model.embeddables.TrainingSet;
import com.ThimoteoConsultorias.Consulthi.repository.ContentRepository;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentService
{
    // ----------------------------------------------------
    // 1. DEPENDÊNCIAS
    // ----------------------------------------------------
    private final ContentRepository contentRepository;
    private final ProfessionalService professionalService;
    private final StudentProfessionalLinkService linkService;
    private final TrainingService trainingService; // Necessário para mapeamento de Training DTO

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public ContentService
    (
        ContentRepository contentRepository,
        ProfessionalService professionalService,
        StudentProfessionalLinkService linkService,
        TrainingService trainingService
    )
    {
        this.contentRepository = contentRepository;
        this.professionalService = professionalService;
        this.linkService = linkService;
        this.trainingService = trainingService;
    }

    // ----------------------------------------------------
    // 3. MÉTODOS DE DOMÍNIO
    // ----------------------------------------------------

    /*
     * CREATE
     */

    /**
     * Implementa o RF07: Criação de Conteúdo Autoral.
     */
    @Transactional
    public Content createContent(ContentDTO contentDto, Long creatorUserId)
    {
        Professional creator = professionalService.getProfessionalById(creatorUserId);
        
        Content content = ToContent(contentDto, creator);

        LocalDateTime now = LocalDateTime.now();

        content.setCreationDate(now);
        content.setLastModificationDate(now);
        
        return contentRepository.save(content);
    }

    /*
     * READ
     */

    /**
     * Retorna o conteúdo pelo ID.
     * @throws ResourceNotFoundException se o Content não for encontrado.
     */
    @Transactional(readOnly = true)
    public Content getContentById(Long id) throws ResourceNotFoundException
    {
        Content content = contentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Conteúdo de id '" + id +"' não encontrado."));

        if (content.getCreator() != null && content.getCreator().getUser() != null)
            content.getCreator().getUser().getFullName();

        if (content instanceof Routine)
        {
            Routine routine = (Routine) content;

            Hibernate.initialize(routine.getGoals());

            for (Training training : routine.getTrainings())
            {
                Hibernate.initialize(training.getTargetMuscleGroups());
                Hibernate.initialize(training.getTrainingSets());

                for(TrainingSet set : training.getTrainingSets())
                    Hibernate.initialize(set);
            }
        }
        else if (content instanceof Diet)
        {
            Diet diet = (Diet) content;
            Hibernate.initialize(diet.getMealsEspecifications());
        }
        else if (content instanceof Material)
        {
            Material material = (Material) content;
            Hibernate.initialize(material.getTags());
            Hibernate.initialize(material.getContentBlocks());
        }

        return content;
    }

    /**
     * Retorna um conteúdo específico, garantindo a autoria e o tipo (RF07).
     * @throws SecurityException se o Professional não for o autor.
     */
    public Content getContentByCreatorId(Long contentId, Long creatorId, String contentType)
    throws IllegalArgumentException, SecurityException
    {
        Content content = contentRepository.findByIdAndCreatorId(contentId, creatorId)
            .orElseThrow(() -> new SecurityException("Conteúdo não encontrado ou você não tem permissão de autoria."));
        
        if (contentType != null && !contentType.isEmpty())
            if (!content.getClass().getSimpleName().equalsIgnoreCase(contentType))
                throw new IllegalArgumentException("O conteúdo encontrado é do tipo '" + content.getClass().getSimpleName() + "', mas o tipo esperado era '" + contentType + "'.");
        
        return content;
    }
   
    /**
     * Retorna lista de conteúdos criados por um Professional, com filtro opcional por tipo.
     */
    public List<Content> listAllContentByCreator(Professional creator, ContentType contentType)
    {
        if (contentType != null && contentType != ContentType.ALL)
        {
            switch(contentType)
            {
                case DIET:
                    return contentRepository.findDietsByCreatorId(creator.getId()).stream()
                        .map(diet -> (Content) diet)
                        .collect(Collectors.toList());
                case MATERIAL:
                    return contentRepository.findMaterialsByCreatorId(creator.getId()).stream()
                        .map(material -> (Content) material)
                        .collect(Collectors.toList());
                case ROUTINE:
                    return contentRepository.findRoutinesByCreatorId(creator.getId()).stream()
                        .map(routine -> (Content) routine)
                        .collect(Collectors.toList());
                default:
                    return contentRepository.findByCreatorId(creator.getUser().getId());
            }
        }
        return contentRepository.findByCreatorId(creator.getUser().getId());
    }

    /**
     * Verifica e retorna o conteúdo que um Aluno PODE acessar (RF06).
     * @throws SecurityException se o vínculo ou acesso granular for negado.
     */
    @Transactional(readOnly = true)
    public Content getContentForStudent(Long contentId, Long studentUserId)
    throws SecurityException
    {
        Content content = getContentById(contentId);
        
        if (content.getCreator().getUser().getId().equals(studentUserId))
            return content;
            
        Professional creator = content.getCreator();
        
        boolean hasActiveLink = linkService.isActiveLink(studentUserId, creator.getId());
        if (!hasActiveLink)
            throw new SecurityException("Acesso negado: Não há vínculo ativo com o profissional criador.");
        
        boolean hasGranularAccess = content.getAccessStudentIds().isEmpty() || content.getAccessStudentIds().contains(studentUserId);
        if (!hasGranularAccess)
            throw new SecurityException("Acesso negado: Conteúdo não liberado para você.");
        
        return content;
    }
    
    /**
     * Verifica se o usuário tem permissão para visualizar o conteúdo (Criador ou Aluno com Link Ativo).
     */
    public boolean isUserAuthorizedToView(Content content, Long userId)
    {
        Long creatorId = content.getCreator().getUser().getId();
        
        if (creatorId.equals(userId))
            return true;
            
        try {
            getContentForStudent(content.getId(), userId);
            return true;
        } catch (SecurityException | ResourceNotFoundException e) {
            return false;
        }
    }


    /*
     * UPDATE
     */

    /**
     * Busca o Conteúdo e o converte para DTO, validando a autoria.
     */
    @Transactional(readOnly = true)
    public ContentDTO getContentAsDTO(Long contentId, Long professionalId)
    {
        Content content = getContentByCreatorId(contentId, professionalId, null);
        return toDTO(content);
    }

    /**
     * Implementa o RF07: Edição de Conteúdo (Garante que apenas o autor edita).
     */
    @Transactional
    public Content updateContent(Long contentId, ContentDTO contentDto, Long professionalId)
    throws IllegalArgumentException, SecurityException
    {
        Content content = getContentByCreatorId(contentId, professionalId, null);
        
        if (contentDto.getContentType() != getContentType(content))
            throw new IllegalArgumentException("O tipo de conteúdo não pode ser alterado durante a edição.");

        content.setName(contentDto.getName());
        content.setDescription(contentDto.getDescription());
        
        content.setAccessStudentIds(contentDto.getAccessStudentIds());
        content.setLastModificationDate(LocalDateTime.now());
        
        
        if (content instanceof Diet diet)
            diet.setMealsEspecifications(contentDto.getMealsEspecifications());
        
        else if (content instanceof Material material)
        {
            material.setTags(contentDto.getTags());
            material.setContentBlocks(contentDto.getContentBlocks());
        }
        
        else if (content instanceof Routine routine)
        {
            routine.setRoutineLevel(contentDto.getRoutineLevel());
            routine.setGoals(contentDto.getGoals());
            
            List<Training> newTrainings = contentDto.getTrainingDtos().stream()
                .map(trainingService::createTrainingFromDTO)
                .collect(Collectors.toList());
             
            routine.setTrainings(newTrainings);
        }

        return contentRepository.save(content);
    }
    
    /*
     * DELETE
     */

    /**
     * Implementa o RF07: Deleção de Conteúdo (Garante que apenas o autor deleta).
     */
    @Transactional
    public void deleteContent(Long contentId, Long professionalId)
    {
         Content content = contentRepository.findByIdAndCreatorId(contentId, professionalId)
            .orElseThrow(() -> new SecurityException("Conteúdo não encontrado ou você não tem permissão para deletar."));
            
        contentRepository.delete(content);
    }
    
    // ----------------------------------------------------
    // 4. MÉTODOS AUXILIARES
    // ----------------------------------------------------

    /**
     * Mapeia o ContentDTO para a subclasse de Content correta.
     */
    private Content ToContent(ContentDTO dto, Professional creator)
    {
        switch (dto.getContentType())
        {
            case DIET:
                return Diet.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .creator(creator)
                    .accessStudentIds(dto.getAccessStudentIds())
                    .mealsEspecifications(dto.getMealsEspecifications())
                    .build();
                    
            case MATERIAL:
                return Material.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .creator(creator)
                    .accessStudentIds(dto.getAccessStudentIds())
                    .tags(dto.getTags())
                    .contentBlocks(dto.getContentBlocks())
                    .build();
                    
            case ROUTINE:
                List<Training> savedTrainings = dto.getTrainingDtos() != null ? 
                    dto.getTrainingDtos().stream()
                    .map(trainingService::createTrainingFromDTO)
                    .collect(Collectors.toList()) : List.of();

                return Routine.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .creator(creator)
                    .accessStudentIds(dto.getAccessStudentIds())
                    .routineLevel(dto.getRoutineLevel())
                    .goals(dto.getGoals())
                    .trainings(savedTrainings)
                    .build();
                    
            default:
                throw new IllegalArgumentException("Tipo de conteúdo inválido: " + dto.getContentType());
        }
    }

    /**
     * Identifica e retorna o tipo de conteúdo (Enum) a partir da entidade (Polimorfismo).
     */
    public ContentType getContentType(Content content)
    {
        if (content == null) return null;
        
        if (content instanceof Diet) return ContentType.DIET;
        if (content instanceof Material) return ContentType.MATERIAL;
        if (content instanceof Routine) return ContentType.ROUTINE;

        throw new IllegalArgumentException("Tipo de Content desconhecido: " + content.getClass().getName());
    }

    /**
     * Mapeia uma classe Content para ContentDTO.
     */
    private ContentDTO toDTO(Content content)
    {
        ContentType contentType = getContentType(content);
        
        ContentDTO.ContentDTOBuilder builder = ContentDTO.builder()
            .id(content.getId())
            .name(content.getName())
            .description(content.getDescription())
            .creationDate(content.getCreationDate())
            .lastModificationDate(content.getLastModificationDate())
            .contentType(contentType)
            .accessStudentIds(new HashSet<>(content.getAccessStudentIds()));
       
        switch(contentType)
        {
            case DIET:
                Diet diet = (Diet) content;
                builder.mealsEspecifications(diet.getMealsEspecifications());
                break;

            case MATERIAL:
                Material material = (Material) content;
                builder.tags(new HashSet<>(material.getTags()));
                builder.contentBlocks(new ArrayList<>(material.getContentBlocks()));
                break;

            case ROUTINE:
                Routine routine = (Routine) content;
                builder.routineLevel(routine.getRoutineLevel());
                builder.goals(new HashSet<>(routine.getGoals()));
                
                builder.trainingDtos(new ArrayList<>(routine.getTrainings()).stream()
                    .map(trainingService::toDTO) 
                    .collect(Collectors.toList()));
                break;

            default:
                break;
        }
        
        return builder.build();
    }
}