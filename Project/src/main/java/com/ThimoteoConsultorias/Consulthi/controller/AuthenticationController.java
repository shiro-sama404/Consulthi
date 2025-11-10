package com.ThimoteoConsultorias.Consulthi.controller;

import com.ThimoteoConsultorias.Consulthi.dto.UserDTO;
import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.service.ProfessionalService;
import com.ThimoteoConsultorias.Consulthi.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/")
public class AuthenticationController
{
    private final UserService userService;
    private final ProfessionalService professionalService;

    public AuthenticationController(UserService userService, ProfessionalService professionalService)
    {
        this.userService = userService;
        this.professionalService = professionalService;
    }

    /**
     * Mapeia a página de login.
     */
    @GetMapping("/login")
    public String login()
    {
        return "login";
    }

    /**
     * Página inicial após login bem-sucedido.
     */
    @GetMapping("/home")
    public String home()
    {
        return "home";
    }


    /*
     * FLUXO DE CADASTRO
     */

    /**
     * Exibe o formulário de cadastro.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model)
    {
        model.addAttribute("userDto", UserDTO.builder().build());
        model.addAttribute("roles", Role.values()); 
        
        try
        {
            List<Professional> allProfessionals = professionalService.getAllProfessionals(); // Assumindo novo método
            
            model.addAttribute("availableProfessionals", allProfessionals);
            
        }
        catch (Exception e)
        {
            model.addAttribute("error", "Não foi possível carregar a lista de profissionais.");
        }
        
        return "register";
    }

    /**
     * Processa o envio do formulário de cadastro.
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserDTO userDto, RedirectAttributes redirectAttributes)
    {
        try
        {
            userService.createUser(userDto);
            
            redirectAttributes.addFlashAttribute("message", "Solicitação de cadastro enviada! Aguarde a aprovação do Administrador/Profissional.");
            return "redirect:/login";

        }
        catch (IllegalArgumentException e)
        {
            redirectAttributes.addFlashAttribute("error", "Erro de Cadastro: " + e.getMessage());
            return "redirect:/register"; 
        }
    }
}