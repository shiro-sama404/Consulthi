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
import com.ThimoteoConsultorias.Consulthi.repository.ContentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public Content getContentById(Long id) throws ResourceNotFoundException
    {
        return contentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Conteúdo de id '" + id +"' não encontrado."));
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
    public Content getContentForStudent(Long contentId, Long studentUserId)
    throws SecurityException
    {
        Content content = getContentById(contentId);
        
        if (content.getCreator().getUser().getId().equals(studentUserId))
            return content;
            
        Professional creator = content.getCreator();
        
        boolean hasActiveLink = linkService.isActiveLink(studentUserId, creator.getUser().getId());
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
        
        // Deixar o adm visualizar?
        // if (userService.getUserById(userId).getRoles().contains(Role.ADMINISTRATOR))
        //    return true;
            
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
        
        // Garante que o tipo no DTO é o mesmo da entidade persistida
        if (contentDto.contentType() != getContentType(content))
        {
            throw new IllegalArgumentException("O tipo de conteúdo não pode ser alterado durante a edição.");
        }

        content.setName(contentDto.name());
        content.setDescription(contentDto.description());
        
        // RF07 Regra: Um usuário profissional não pode alterar qualquer conteúdo que não seja de autoria própria.
        // Já garantido pelo getContentByCreatorId
        
        content.setAccessStudentIds(contentDto.accessStudentIds());
        content.setLastModificationDate(LocalDateTime.now());
        
        
        if (content instanceof Diet diet)
            diet.setMealsEspecifications(contentDto.mealsEspecifications());
        
        else if (content instanceof Material material)
        {
            material.setTags(contentDto.tags());
            material.setContentBlocks(contentDto.contentBlocks());
        }
        
        else if (content instanceof Routine routine)
        {
            routine.setRoutineLevel(contentDto.routineLevel());
            routine.setGoals(contentDto.goals());
            
            // Substituição total dos Treinos
            List<Training> newTrainings = contentDto.trainingDtos().stream()
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
        // ... (Implementação existente, OK)
        switch (dto.contentType())
        {
            case DIET:
                return Diet.builder()
                    .name(dto.name())
                    .description(dto.description())
                    .creator(creator)
                    .accessStudentIds(dto.accessStudentIds())
                    .mealsEspecifications(dto.mealsEspecifications())
                    .build();
                    
            case MATERIAL:
                return Material.builder()
                    .name(dto.name())
                    .description(dto.description())
                    .creator(creator)
                    .accessStudentIds(dto.accessStudentIds())
                    .tags(dto.tags())
                    .contentBlocks(dto.contentBlocks())
                    .build();
                    
            case ROUTINE:
                List<Training> savedTrainings = dto.trainingDtos() != null ? 
                    dto.trainingDtos().stream()
                    .map(trainingService::createTrainingFromDTO)
                    .collect(Collectors.toList()) : List.of();

                return Routine.builder()
                    .name(dto.name())
                    .description(dto.description())
                    .creator(creator)
                    .accessStudentIds(dto.accessStudentIds())
                    .routineLevel(dto.routineLevel())
                    .goals(dto.goals())
                    .trainings(savedTrainings)
                    .build();
                    
            default:
                throw new IllegalArgumentException("Tipo de conteúdo inválido: " + dto.contentType());
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
            .accessStudentIds(content.getAccessStudentIds());
       
        switch(contentType)
        {
            case DIET:
                Diet diet = (Diet) content;
                builder.mealsEspecifications(diet.getMealsEspecifications());
                break;

            case MATERIAL:
                Material material = (Material) content;
                builder.tags(material.getTags());
                builder.contentBlocks(material.getContentBlocks());
                break;

            case ROUTINE:
                Routine routine = (Routine) content;
                builder.routineLevel(routine.getRoutineLevel());
                builder.goals(routine.getGoals());
                
                builder.trainingDtos(routine.getTrainings().stream()
                    .map(trainingService::toDTO) 
                    .collect(Collectors.toList()));
                break;

            default:
                break;
        }
        
        return builder.build();
    }
}