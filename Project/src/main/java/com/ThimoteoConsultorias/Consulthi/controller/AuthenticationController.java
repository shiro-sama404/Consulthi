package com.ThimoteoConsultorias.Consulthi.controller;

import com.ThimoteoConsultorias.Consulthi.dto.UserDTO;
import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.model.Student;
import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.service.ProfessionalService;
import com.ThimoteoConsultorias.Consulthi.service.StudentProfessionalLinkService;
import com.ThimoteoConsultorias.Consulthi.service.StudentService;
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
    private final StudentProfessionalLinkService linkService;
    private final ProfessionalService professionalService;
    private final StudentService studentService;
    private final UserService userService;

    public AuthenticationController
    (
        StudentProfessionalLinkService linkService,
        ProfessionalService professionalService,
        StudentService studentService,
        UserService userService
    )
    {
        this.linkService = linkService;
        this.professionalService = professionalService;
        this.studentService = studentService;
        this.userService = userService;
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
            User newUser = userService.createUser(userDto);
            
            if (userDto.roles().contains(Role.STUDENT) && 
                userDto.selectedProfessionalIds() != null && 
                !userDto.selectedProfessionalIds().isEmpty())
            {
                Student studentProfile = studentService.getStudentById(newUser.getId());
                linkService.createPendingLinks(studentProfile, userDto.selectedProfessionalIds());
            }

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