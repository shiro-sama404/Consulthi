package com.ThimoteoConsultorias.Consulthi.controller;

import com.ThimoteoConsultorias.Consulthi.enums.ExpertiseArea;
import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.dto.UserDTO;
import com.ThimoteoConsultorias.Consulthi.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class AuthenticationController
{
    private final UserService userService;

    public AuthenticationController(UserService userService)
    {
        this.userService = userService;
    }

    /**
     * Mapeia a página de login. Requisito [RF02] Login Usuário.
     * O Spring Security lida com a lógica de POST para /login.
     */
    @GetMapping("/login")
    public String login() {
        // Retorna o nome do template Thymeleaf (ex: login.html)
        return "login"; 
    }

    /**
     * Exibe o formulário de cadastro. Requisito [RF01] Cadastrar Usuário.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Adiciona um objeto vazio para o formulário no Thymeleaf
        model.addAttribute("userDto", UserDTO.builder().build()); 
        model.addAttribute("roles", Role.values()); // Passa todos os tipos de role
        return "register"; // Retorna o template register.html
    }

    /**
     * Processa o envio do formulário de cadastro. Requisito [RF01] Cadastrar Usuário.
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userDto") UserDTO userDto, RedirectAttributes redirectAttributes) {
        
        try {
            // NOTE: A validação e as regras complexas (username único, etc.) devem ser adicionadas aqui.
            userService.createUser(userDto);
            
            // Sucesso!
            redirectAttributes.addFlashAttribute("message", "Solicitação de cadastro enviada com sucesso! Aguarde a aprovação.");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            // Falha (ex: username já existe)
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register"; // Redireciona de volta para o formulário
        }
        // NOTE: A complexa lógica de Aceitação/Notificação do RF01 está no UserService.
    }

    @GetMapping("/register/role-fields")
    public String getRoleSpecificFields(@RequestParam(required = false) List<String> roles, Model model) {
        
        // Converte as strings de Role para o Enum e verifica se alguma é profissional.
        boolean isProfessional = roles != null && roles.stream()
            .map(r -> {
                try {
                    return Role.valueOf(r);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .anyMatch(role -> role != null && role.isProfessionalRole()); // Usa o método isProfessionalRole

        if (isProfessional) {
            // Adiciona as áreas de especialidade para serem selecionadas no fragmento.
            model.addAttribute("expertiseAreas", ExpertiseArea.values());
            // Retorna o fragmento do Thymeleaf (ver Passo 2)
            return "register :: professionalFields"; 
        }
        
        // Se nenhuma Role profissional for selecionada, retorna um fragmento vazio
        return "register :: emptyFragment"; 
    }

    /**
     * Página inicial após o login (defaultSuccessUrl no SecurityConfig).
     */
    @GetMapping("/home")
    public String home() {
        return "home"; // Template home.html
    }
}