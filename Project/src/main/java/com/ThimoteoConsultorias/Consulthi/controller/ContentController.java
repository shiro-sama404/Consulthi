package com.ThimoteoConsultorias.Consulthi.controller;

import com.ThimoteoConsultorias.Consulthi.dto.ContentDTO;
import com.ThimoteoConsultorias.Consulthi.enums.*;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.Content;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.service.ContentService;
import com.ThimoteoConsultorias.Consulthi.service.ProfessionalService;
import com.ThimoteoConsultorias.Consulthi.service.TrainingService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
    private final TrainingService trainingService;

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public ContentController(
        ContentService contentService, 
        ProfessionalService professionalService, 
        TrainingService trainingService)
    {
        this.contentService = contentService;
        this.professionalService = professionalService;
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
    public String showCreateContentForm(Model model)
    {
        model.addAttribute("contentDto", ContentDTO.builder().build()); 
        model.addAttribute("contentTypes", ContentType.values());
        
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
        try 
        {
            ContentDTO contentDto = contentService.getContentAsDTO(contentId, currentUserId); 
            
            model.addAttribute("contentDto", contentDto);
            model.addAttribute("routineLevels", RoutineLevel.values());
            model.addAttribute("goalTypes", GoalType.values());
            model.addAttribute("contentTags", ContentTag.values());
            model.addAttribute("trainingTechniques", TrainingTechnique.values());
            model.addAttribute("exercises", trainingService.listAllExercises());
            
            return "professional/content/update";
        } 
        catch (ResourceNotFoundException e) 
        {
            model.addAttribute("error", "Conteúdo não encontrado para edição.");
            return "error/404";
        }
        catch (SecurityException e) 
        {
            model.addAttribute("error", "Ação não autorizada: " + e.getMessage());
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
    public String deleteContent(@PathVariable Long contentId, 
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
        model.addAttribute("contentDto", ContentDTO.builder().contentType(contentType).build());
        
        model.addAttribute("routineLevels", RoutineLevel.values());
        model.addAttribute("goalTypes", GoalType.values());
        model.addAttribute("contentTags", ContentTag.values());
        model.addAttribute("trainingTechniques", TrainingTechnique.values());
        
        if (contentType == ContentType.ROUTINE)
            model.addAttribute("exercises", trainingService.listAllExercises()); 
        
        switch (contentType) {
            case DIET:
                return "professional/content/create :: dietFields";
            case MATERIAL:
                return "professional/content/create :: materialFields";
            case ROUTINE:
                return "professional/content/create :: routineFields";
            default:
                return "professional/content/create :: emptyFields";
        }
    }
}