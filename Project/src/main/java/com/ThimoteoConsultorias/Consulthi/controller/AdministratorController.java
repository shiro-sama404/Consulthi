package com.ThimoteoConsultorias.Consulthi.controller;

import com.ThimoteoConsultorias.Consulthi.service.AdministratorService;
import com.ThimoteoConsultorias.Consulthi.model.User;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMINISTRATOR')")
public class AdministratorController
{
    private final AdministratorService administratorService;

    public AdministratorController(AdministratorService administratorService)
    {
        this.administratorService = administratorService;
    }

    // ----------------------------------------------------------------------------------
    // ENDPOINTS WEB (HTML)
    // ----------------------------------------------------------------------------------
    
    /**
     * Rota de gerenciamento principal para o Administrador (HTML - Web Dashboard).
     */
    @GetMapping
    public String adminHome(Model model)
    {
        List<User> allUsers = administratorService.listAllUsers();
        List<User> pendingUsers = administratorService.getPendingProfessionalRegistrations();
        
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("pendingUsers", pendingUsers);
        
        return "admin/dashboard";
    }

    /**
     * Endpoint para aprovar um registro de Profissional (RF01 - HTML).
     */
    @PostMapping("/approve/{userId}")
    public String approveProfessional(@PathVariable Long userId, RedirectAttributes redirectAttributes)
    {
        try {
            administratorService.approveProfessionalRegistration(userId);
            redirectAttributes.addFlashAttribute("message", "Profissional #" + userId + " aprovado com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao aprovar: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    /**
     * Endpoint para remover o cadastro de um usuário (RF08 - HTML).
     */
    @PostMapping("/remove/{userId}")
    public String removeUser(@PathVariable Long userId, RedirectAttributes redirectAttributes)
    {
        try {
            administratorService.removeUser(userId);
            redirectAttributes.addFlashAttribute("message", "Usuário #" + userId + " removido permanentemente (RF08).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // ----------------------------------------------------------------------------------
    // ENDPOINTS API (REST para Desktop/Outros Clientes)
    // ----------------------------------------------------------------------------------

    /**
     * Endpoint REST para listar todos os usuários (Usado pelo Desktop Admin).
     */
    @GetMapping("/api/users")
    @ResponseBody 
    public List<User> listAllUsersApi()
    {
        return administratorService.listAllUsers();
    }
    
    /**
     * Endpoint REST para listar registros pendentes de profissional (RF01 - Usado pelo Desktop Admin).
     */
    @GetMapping("/api/pending-professionals")
    @ResponseBody
    public List<User> getPendingProfessionalsApi()
    {
        return administratorService.getPendingProfessionalRegistrations();
    }
    
    /**
     * Endpoint REST para aprovar um registro de Profissional (RF01 - Usado pelo Desktop Admin).
     * O cliente desktop usará este endpoint.
     */
    @PostMapping("/api/approve/{userId}")
    @ResponseBody // Retorna 200 OK sem corpo
    public ResponseEntity<Void> approveProfessionalApi(@PathVariable Long userId)
    {
        administratorService.approveProfessionalRegistration(userId);
        return ResponseEntity.ok().build();
    }
}