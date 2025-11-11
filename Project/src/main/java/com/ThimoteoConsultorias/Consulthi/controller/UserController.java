package com.ThimoteoConsultorias.Consulthi.controller;

import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.service.SchedulerService;
import com.ThimoteoConsultorias.Consulthi.service.UserService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController
{
    private final UserService userService;
    private final SchedulerService schedulerService;

    public UserController(UserService userService, SchedulerService schedulerService) // 6. Atualizar construtor
    {
        this.userService = userService;
        this.schedulerService = schedulerService;
    }

    /**
     * Processa a solicitação de desativação de conta do usuário logado (RF04).
     * @param currentUserId O ID do usuário autenticado.
     * @param redirectAttributes Para enviar a mensagem de feedback.
     * @return Redireciona para a página de logout.
     */
    @PostMapping("/deactivate")
    public String handleDeactivation(@AuthenticationPrincipal Long currentUserId, RedirectAttributes redirectAttributes)
    {
        try 
        {
            User deactivatedUser = userService.requestDeactivation(currentUserId);
            schedulerService.scheduleDataDeletion(deactivatedUser);
            redirectAttributes.addFlashAttribute("message", "Sua conta foi desativada e será permanentemente excluída em 30 dias.");
            
            return "redirect:/logout"; 
        } 
        catch (Exception e) 
        {
            redirectAttributes.addFlashAttribute("error", "Erro ao tentar desativar a conta: " + e.getMessage());
            return "redirect:/home";
        }
    }
}