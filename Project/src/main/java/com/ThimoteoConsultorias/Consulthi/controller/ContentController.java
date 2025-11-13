package com.ThimoteoConsultorias.Consulthi.controller;

import com.ThimoteoConsultorias.Consulthi.dto.ContentDTO;
import com.ThimoteoConsultorias.Consulthi.enums.*;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.Content;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.model.StudentProfessionalLink;
import com.ThimoteoConsultorias.Consulthi.service.ContentService;
import com.ThimoteoConsultorias.Consulthi.service.ProfessionalService;
import com.ThimoteoConsultorias.Consulthi.service.StudentProfessionalLinkService;
import com.ThimoteoConsultorias.Consulthi.service.TrainingService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller responsável pela gestão de Conteúdo (Rotina, Dieta, Material) 
 * pelos Perfis Profissionais (COACH, NUTRITIONIST, PSYCHOLOGIST).
 */
@Controller
@RequestMapping("/professional/content")
@PreAuthorize("hasAnyAuthority('COACH', 'NUTRITIONIST', 'PSYCHOLOGIST')") 
public class ContentController
{
    // -----------------------------------------------------------------------
    // 1. DEPENDÊNCIAS
    // -----------------------------------------------------------------------
    private final ContentService contentService;
    private final ProfessionalService professionalService;
    private final StudentProfessionalLinkService linkService;
    private final TrainingService trainingService;

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public ContentController(
        ContentService contentService, 
        ProfessionalService professionalService,
        StudentProfessionalLinkService linkService,
        TrainingService trainingService)
    {
        this.contentService = contentService;
        this.professionalService = professionalService;
        this.linkService = linkService;
        this.trainingService = trainingService;
    }

    // -----------------------------------------------------------------------
    // 3. GET MAPPING
    // -----------------------------------------------------------------------

    /**
     * Exibe a lista de conteúdos criados pelo profissional.
     */
    @GetMapping
    public String listContent(@AuthenticationPrincipal(expression = "id") Long currentUserId, Model model) 
    {
        Professional creator = professionalService.getProfessionalById(currentUserId);
        List<Content> contents = contentService.listAllContentByCreator(creator, ContentType.ALL);
        model.addAttribute("contents", contents);

        return "professional/content/list";
    }

    /**
     * Exibe o formulário de criação de conteúdo.
     */
    @GetMapping("/create")
    public String showCreateContentForm(@AuthenticationPrincipal(expression = "id") Long currentUserId, Model model)
    {
        model.addAttribute("contentDto", ContentDTO.builder().build());
        List<ContentType> creatableTypes = Arrays.stream(ContentType.values())
            .filter(type -> type != ContentType.ALL)
            .collect(Collectors.toList());
        model.addAttribute("contentTypes", creatableTypes);

        model.addAttribute("allContentBlockTypes", ContentBlockType.values());
        model.addAttribute("allContentTags", ContentTag.values());
        model.addAttribute("allRoutineLevels", RoutineLevel.values());
        model.addAttribute("allGoalTypes", GoalType.values());
        model.addAttribute("allTrainingTechniques", TrainingTechnique.values());
        model.addAttribute("allMuscleGroups", MuscleGroup.values());
        
        try 
        {
            model.addAttribute("exercises", trainingService.listAllExercises());
        } 
        catch (Exception e) 
        {
            model.addAttribute("exercises", List.of()); 
            model.addAttribute("error", "Alerta: Não foi possível carregar a lista de exercícios pré-definidos.");
        }

        try 
        {
            Professional professional = professionalService.getProfessionalById(currentUserId);
            List<StudentProfessionalLink> activeLinks = linkService.getLinksByProfessionalAndStatusIn(professional, EnumSet.of(LinkStatus.ACCEPTED));

            model.addAttribute("activeStudents", activeLinks);
        } 
        catch (Exception e) 
        {
            model.addAttribute("activeStudents", List.of());
            model.addAttribute("error", "Não foi possível carregar a lista de alunos.");
        }
        
        return "professional/content/create";
    }

    /**
     * Exibe o conteúdo.
     * Rota de acesso para o criador (view) e para o aluno (consume - RF06).
     */
    @GetMapping("/view/{contentId}")
    public String viewContent(@PathVariable Long contentId, @AuthenticationPrincipal(expression = "id") Long currentUserId, Model model)
    {
        try 
        {
            Content content = contentService.getContentById(contentId);
            
            if (!contentService.isUserAuthorizedToView(content, currentUserId)) 
            {
                model.addAttribute("error", "Acesso negado. Este conteúdo não foi compartilhado com você.");
                return "error/accessDenied";
            }

            model.addAttribute("content", content);
            
            if (content.getCreator().getUser().getId().equals(currentUserId)) 
                return "professional/content/view"; 
            else 
                return "student/content/consume"; 
        } 
        catch (ResourceNotFoundException e) 
        {
            model.addAttribute("error", "Conteúdo não encontrado.");
            return "error/404";
        }
    }
    
    /**
     * Exibe o formulário de atualização (GET).
     */
    @GetMapping("/edit/{contentId}")
    public String showUpdateContentForm(@PathVariable Long contentId, @AuthenticationPrincipal(expression = "id") Long currentUserId, Model model)
    {
        model.addAttribute("allContentBlockTypes", ContentBlockType.values());
        model.addAttribute("allContentTags", ContentTag.values());
        model.addAttribute("allRoutineLevels", RoutineLevel.values());
        model.addAttribute("allGoalTypes", GoalType.values());
        model.addAttribute("allTrainingTechniques", TrainingTechnique.values());
        model.addAttribute("allMuscleGroups", MuscleGroup.values());
        model.addAttribute("exercises", trainingService.listAllExercises());

        try 
        {
            ContentDTO contentDto = contentService.getContentAsDTO(contentId, currentUserId);
            model.addAttribute("contentDto", contentDto);

            try 
            {
                Professional professional = professionalService.getProfessionalById(currentUserId);
                List<StudentProfessionalLink> activeLinks = linkService.getLinksByProfessionalAndStatusIn(professional, EnumSet.of(LinkStatus.ACCEPTED));

                model.addAttribute("activeStudents", activeLinks);
            } 
            catch (Exception e) 
            {
                model.addAttribute("activeStudents", List.of());
                model.addAttribute("error", "Não foi possível carregar a lista de alunos.");
            }
            
            return "professional/content/update";
        } 
        catch (ResourceNotFoundException e) 
        {
            model.addAttribute("error", "Conteúdo não encontrado para edição.");
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
        catch (SecurityException e) 
        {
            model.addAttribute("error", "Ação não autorizada: " + e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "error/accessDenied";
        }
    }

    // -----------------------------------------------------------------------
    // 4. POST MAPPING
    // -----------------------------------------------------------------------

    /**
     * Processa a criação de um novo conteúdo (Rotina, Dieta ou Material).
     */
    @PostMapping("/create")
    public String createContent
    (
        @ModelAttribute ContentDTO contentDto, 
        @AuthenticationPrincipal(expression = "id") Long currentUserId, 
        RedirectAttributes redirectAttributes
    )
    {
        try 
        {
            contentService.createContent(contentDto, currentUserId);
            redirectAttributes.addFlashAttribute("message", "Conteúdo (" + contentDto.contentType().name() + ") criado com sucesso!");
            return "redirect:/professional/content";
        } 
        catch (IllegalArgumentException e) 
        {
            redirectAttributes.addFlashAttribute("error", "Erro de Validação: " + e.getMessage());
            return "redirect:/professional/content/create";
        } 
        catch (Exception e) 
        {
            redirectAttributes.addFlashAttribute("error", "Erro ao criar conteúdo: " + e.getMessage());
            return "redirect:/professional/content/create";
        }
    }

    /**
     * Processa a atualização de um conteúdo existente (POST/PUT - RF07).
     */
    @PostMapping("/edit/{contentId}")
    public String updateContent
    (
        @PathVariable Long contentId, 
        @ModelAttribute ContentDTO contentDto, 
        @AuthenticationPrincipal(expression = "id") Long currentUserId, 
        RedirectAttributes redirectAttributes
    ) 
    {
        try 
        {
            contentService.updateContent(contentId, contentDto, currentUserId);
            redirectAttributes.addFlashAttribute("message", "Conteúdo #" + contentId + " atualizado com sucesso!");
            return "redirect:/professional/content/view/" + contentId;
        } 
        catch (ResourceNotFoundException e) 
        {
            redirectAttributes.addFlashAttribute("error", "Erro: Conteúdo não encontrado para atualização.");
            return "redirect:/professional/content";
        } 
        catch (SecurityException e) 
        {
            redirectAttributes.addFlashAttribute("error", "Ação não autorizada: " + e.getMessage());
            return "redirect:/professional/content";
        }
    }

    /**
     * Remove o conteúdo (RF07).
     */
    @PostMapping("/delete/{contentId}")
    public String deleteContent
    (
        @PathVariable Long contentId, 
        @AuthenticationPrincipal(expression = "id") Long currentUserId, 
        RedirectAttributes redirectAttributes) 
    {
        try 
        {
            contentService.deleteContent(contentId, currentUserId);
            redirectAttributes.addFlashAttribute("message", "Conteúdo #" + contentId + " removido com sucesso.");
            return "redirect:/professional/content";
        } 
        catch (ResourceNotFoundException e) 
        {
            redirectAttributes.addFlashAttribute("error", "Erro: Conteúdo não encontrado para remoção.");
            return "redirect:/professional/content";
        } 
        catch (SecurityException e) 
        {
            redirectAttributes.addFlashAttribute("error", "Ação não autorizada: " + e.getMessage());
            return "redirect:/professional/content";
        }
    }

    // =======================================================================
    // 5. HTMX: CARREGAMENTO DE CAMPOS ESPECÍFICOS POR TIPO
    // =======================================================================

    /**
     * Endpoint HTMX chamado via AJAX para retornar o fragmento HTML 
     * com os campos específicos do tipo de conteúdo selecionado (Diet, Routine, Material).
     * * @param contentType O tipo de conteúdo (passado pelo HTMX no frontend).
     * @param model Modelo para adicionar atributos.
     * @return Nome do fragmento Thymeleaf.
     */
    @GetMapping("/fields-for-type")
    public String getFieldsForType(@RequestParam ContentType contentType, Model model)
    {
        ContentDTO.ContentDTOBuilder dtoBuilder = ContentDTO.builder()
            .contentType(contentType)
            .tags(Set.of())
            .contentBlocks(List.of())
            .goals(Set.of())
            .trainingDtos(List.of());
        
        model.addAttribute("contentDto", dtoBuilder.build());
        
        switch (contentType)
        {
            case DIET:
                return "professional/content/create :: dietFields";
                
            case MATERIAL:
                model.addAttribute("allContentBlockTypes", ContentBlockType.values());
                model.addAttribute("allContentTags", ContentTag.values());
                return "professional/content/create :: materialFields";
                
            case ROUTINE:
                model.addAttribute("allRoutineLevels", RoutineLevel.values());
                model.addAttribute("allGoalTypes", GoalType.values());
                model.addAttribute("allTrainingTechniques", TrainingTechnique.values());
                model.addAttribute("allMuscleGroups", MuscleGroup.values());
                model.addAttribute("exercises", trainingService.listAllExercises()); 
                return "professional/content/create :: routineFields";
                
            default:
                return "professional/content/create :: emptyFields";
        }
    }
}