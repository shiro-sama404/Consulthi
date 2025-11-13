package com.ThimoteoConsultorias.Consulthi.controller;

import com.ThimoteoConsultorias.Consulthi.dto.UserDTO;
import com.ThimoteoConsultorias.Consulthi.enums.ExpertiseArea;
import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.model.Student;
import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.service.ProfessionalService;
import com.ThimoteoConsultorias.Consulthi.service.StudentProfessionalLinkService;
import com.ThimoteoConsultorias.Consulthi.service.StudentService;
import com.ThimoteoConsultorias.Consulthi.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
     * Mapeia a página raiz e a rota /home para a Landing Page (Visitantes).
     */
    @GetMapping({"/", "/home"})
    public String home(Authentication authentication)
    {
        if (authentication != null && authentication.isAuthenticated())
        {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMINISTRATOR")))
                return "redirect:/administrator/dashboard";
            
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("COACH") || 
                                                   a.getAuthority().equals("NUTRITIONIST") || 
                                                   a.getAuthority().equals("PSYCHOLOGIST")))
                return "redirect:/professional/dashboard";
            
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("STUDENT")))
                return "redirect:/student/dashboard";
        }
        
        // 3. Se não estiver logado ou não tiver role, mostra a home.html
        return "home"; 
    }

    /*
     * FLUXO DE CADASTRO
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model)
    {
        model.addAttribute("userDto", UserDTO.builder().build());
        
        List<Role> availableRoles = Arrays.stream(Role.values())
            .filter(role -> role != Role.ADMINISTRATOR)
            .collect(Collectors.toList());
        model.addAttribute("roles", availableRoles);

        model.addAttribute("allExpertiseAreas", ExpertiseArea.values());
        
        try
        {
            List<Professional> allProfessionals = professionalService.getAllProfessionals();
            model.addAttribute("availableProfessionals", allProfessionals);
        }
        catch (Exception e)
        {
            model.addAttribute("error", "Não foi possível carregar a lista de profissionais.");
        }
        
        return "register";
    }

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

            redirectAttributes.addFlashAttribute("message", "Solicitação de cadastro enviada! Aguarde por aprovação.");
            return "redirect:/login";

        }
        catch (IllegalArgumentException e)
        {
            redirectAttributes.addFlashAttribute("error", "Erro de Cadastro: " + e.getMessage());
            return "redirect:/register"; 
        }
    }
}