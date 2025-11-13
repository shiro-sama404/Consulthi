package com.ThimoteoConsultorias.Consulthi.controller;

import com.ThimoteoConsultorias.Consulthi.enums.ContentType;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.Content;
import com.ThimoteoConsultorias.Consulthi.model.Diet;
import com.ThimoteoConsultorias.Consulthi.model.Material;
import com.ThimoteoConsultorias.Consulthi.model.Routine;
import com.ThimoteoConsultorias.Consulthi.model.RoutineInstance;
import com.ThimoteoConsultorias.Consulthi.model.Student;
import com.ThimoteoConsultorias.Consulthi.model.TrainingHistory;
import com.ThimoteoConsultorias.Consulthi.service.ContentService;
import com.ThimoteoConsultorias.Consulthi.service.RoutineInstanceService;
import com.ThimoteoConsultorias.Consulthi.service.StudentService;
import com.ThimoteoConsultorias.Consulthi.service.StudentProfessionalLinkService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasAuthority('STUDENT')")
public class StudentController
{
    private final ContentService contentService;
    private final RoutineInstanceService routineInstanceService;
    private final StudentService studentService;
    private final StudentProfessionalLinkService linkService;

    public StudentController
    (
        ContentService contentService,
        RoutineInstanceService routineInstanceService,
        StudentService studentService,
        StudentProfessionalLinkService linkService
    )
    {
        this.contentService = contentService;
        this.routineInstanceService = routineInstanceService;
        this.studentService = studentService;
        this.linkService = linkService;
    }

    /**
     * Dashboard principal do Aluno (RF06).
     * Exibe as Rotinas Ativas, Dietas e Materiais liberados.
     */
    @GetMapping("/dashboard")
    public String studentHome(@AuthenticationPrincipal(expression = "id") Long currentUserId, Model model)
    {
        List<RoutineInstance> activeRoutines = routineInstanceService.getActiveRoutinesByStudent(currentUserId);
        
        // TODO
        //Student student = studentService.getStudentById(currentUserId);
        //List<Content> allContent = contentService.listAllAccessibleContent(student);
        
        // List<Diet> activeDiets = allContent.stream()
        //     .filter(content -> content instanceof Diet)
        //     .map(content -> (Diet) content)
        //     .collect(Collectors.toList());
            
        // List<Material> educationalMaterials = allContent.stream()
        //     .filter(content -> content instanceof Material)
        //     .map(content -> (Material) content)
        //     .collect(Collectors.toList());

        model.addAttribute("activeRoutines", activeRoutines);
        // model.addAttribute("activeDiets", activeDiets);
        // model.addAttribute("educationalMaterials", educationalMaterials);
        
        return "student/dashboard";
    }

    /*
     * CONTEÚDO
     */
    
    /**
     * Visualiza os detalhes de um conteúdo específico (RF06).
     * Garante que o aluno TEM ACESSO e carrega o histórico se for Rotina.
     */
    @GetMapping("/content/{contentId}")
    public String viewContent(@AuthenticationPrincipal(expression = "id") Long currentUserId, @PathVariable Long contentId, Model model) {
        
        try
        {
            Content content = contentService.getContentForStudent(contentId, currentUserId);
            model.addAttribute("content", content);
            
            if (content instanceof Routine routine) 
            {
                Student student = studentService.getStudentById(currentUserId);
                
                try
                {
                    RoutineInstance instance = routineInstanceService.getActiveRoutineInstance(student, routine);
                    
                    model.addAttribute("currentInstance", instance);
                    
                    List<TrainingHistory> history = routineInstanceService.getHistoryByRoutineInstance(instance.getId());
                    model.addAttribute("history", history);
                }
                catch(ResourceNotFoundException e)
                {
                    model.addAttribute("info", "Esta rotina está liberada, mas ainda não foi ativada como uma instância de uso.");
                }
            }
            
            return "student/content/view"; 
        } 
        catch (ResourceNotFoundException e) 
        {
            model.addAttribute("error", "Conteúdo não encontrado.");
            return "error/404";
        }
        catch (SecurityException e)
        {
            model.addAttribute("error", "Acesso Negado (RF06): " + e.getMessage());
            return "error/accessDenied";
        }
    }

    /*
     * HISTÓRICO
     */
    
    /**
     * Registra que um Treino de uma Rotina Ativa foi concluído pelo aluno.
     */
    @PostMapping("/routine/{instanceId}/log-training/{trainingId}")
    public String logTrainingCompletion(
            @AuthenticationPrincipal(expression = "id") Long currentUserId,
            @PathVariable Long instanceId,
            @PathVariable Long trainingId,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try 
        {
            routineInstanceService.logTrainingExecution(instanceId, trainingId, notes);
            
            RoutineInstance instance = routineInstanceService.getRoutineInstanceById(instanceId);
            
            redirectAttributes.addFlashAttribute("message", "Treino registrado com sucesso! Ótimo trabalho!");
            
            return "redirect:/student/content/" + instance.getRoutine().getId();
        } 
        catch (ResourceNotFoundException e) 
        {
             redirectAttributes.addFlashAttribute("error", "Erro ao registrar treino: " + e.getMessage());
             return "redirect:/student/dashboard";
        }
    }
    
    /*
     * VÍNCULOS (REMOÇÃO PELO ALUNO - RF04)
     */

    /**
     * Implementa a remoção do vínculo pelo Aluno (Solicitante).
     */
    @PostMapping("/links/remove/{linkId}")
    public String removeLinkByStudent(@PathVariable Long linkId, @AuthenticationPrincipal(expression = "id") Long currentUserId, RedirectAttributes redirectAttributes) {
        try 
        {
            linkService.removeLink(linkId, currentUserId);
            redirectAttributes.addFlashAttribute("message", "Vínculo removido com sucesso. Você perdeu o acesso ao conteúdo deste profissional.");
        }
        catch (ResourceNotFoundException e)
        {
            redirectAttributes.addFlashAttribute("error", "Erro: Vínculo não encontrado.");
        }
        catch (SecurityException e)
        {
            redirectAttributes.addFlashAttribute("error", "Ação não autorizada: " + e.getMessage());
        }
        return "redirect:/student/dashboard";
    }
}