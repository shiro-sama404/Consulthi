package com.ThimoteoConsultorias.Consulthi.controller;

import com.ThimoteoConsultorias.Consulthi.enums.LinkStatus;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.model.StudentProfessionalLink;
import com.ThimoteoConsultorias.Consulthi.service.ProfessionalService;
import com.ThimoteoConsultorias.Consulthi.service.StudentProfessionalLinkService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.EnumSet;

@Controller
@RequestMapping("/professional")
@PreAuthorize("hasAnyAuthority('COACH', 'NUTRITIONIST', 'PSYCHOLOGIST')") 
public class ProfessionalController
{
    private final ProfessionalService professionalService;
    private final StudentProfessionalLinkService linkService;

    public ProfessionalController
    (
        ProfessionalService professionalService,
        StudentProfessionalLinkService linkService
    )
    {
        this.professionalService = professionalService;
        this.linkService = linkService;
    }

    /**
     * Dashboard principal do Profissional.
     */
    @GetMapping("/dashboard")
    public String professionalHome(@AuthenticationPrincipal(expression = "id") Long currentUserId, Model model)
    {
        Professional professional = professionalService.getProfessionalById(currentUserId);
            
        EnumSet<LinkStatus> activeStatuses = EnumSet.of(LinkStatus.ACCEPTED);
        EnumSet<LinkStatus> pendingStatuses = EnumSet.of(LinkStatus.PENDING);
            
        List<StudentProfessionalLink> pendingRequests = linkService.getLinksByProfessionalAndStatusIn(professional, pendingStatuses);
        List<StudentProfessionalLink> activeStudents = linkService.getLinksByProfessionalAndStatusIn(professional, activeStatuses);

        model.addAttribute("professional", professional);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("activeStudents", activeStudents);
        
        return "professional/dashboard";
    }

    /**
     * Processa a aceitação de uma solicitação de vínculo (RF01).
     */
    @PostMapping("/links/accept/{linkId}")
    public String acceptStudentLink(@PathVariable Long linkId, @AuthenticationPrincipal(expression = "id") Long currentUserId, RedirectAttributes redirectAttributes)
    {
        linkService.acceptLink(linkId, currentUserId);
        redirectAttributes.addFlashAttribute("message", "Solicitação de aluno aceita com sucesso!");
        return "redirect:/professional/dashboard";
    }

    /**
     * Processa a remoção de um vínculo de aluno (RF05).
     */
    @PostMapping("/links/remove/{linkId}")
    public String removeStudentLink(@PathVariable Long linkId, @AuthenticationPrincipal(expression = "id") Long currentUserId, RedirectAttributes redirectAttributes)
    {
        linkService.removeLink(linkId, currentUserId);
        redirectAttributes.addFlashAttribute("message", "Acesso do aluno removido com sucesso (RF05).");
        return "redirect:/professional/dashboard";
    }
}