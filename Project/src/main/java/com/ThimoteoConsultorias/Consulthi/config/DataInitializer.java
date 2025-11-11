// src/main/java/com/ThimoteoConsultorias/Consulthi/config/DataInitializer.java

package com.ThimoteoConsultorias.Consulthi.config;

import com.ThimoteoConsultorias.Consulthi.dto.UserDTO;
import com.ThimoteoConsultorias.Consulthi.enums.ExpertiseArea;
import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.service.UserService;
import com.ThimoteoConsultorias.Consulthi.model.User;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner
{
    private final UserService userService;

    public DataInitializer(UserService userService)
    {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception
    {
        // Configurar
        // true: Popula o banco de dados
        // false: Pula a etapa de população
        boolean initialize = false; 

        if (!initialize)
        {
            System.out.println("Dados já inicializados");
            return;
        }

        System.out.println("--- Inicializando dados de teste (DataInitializer) ---");

        createAdministrators();
        
        List<Long> professionalIds = createAndApproveProfessionals();
        
        createStudent(professionalIds);
        
        System.out.println("--- Inicialização de dados concluída ---");
    }

    private void createAdministrators()
    {
        for (int i = 1; i <= 2; i++)
        {
            String username = "admin" + i;
            String fullName = "Admin Teste " + i;

            UserDTO adminDto = UserDTO.builder()
                .username(username)
                .rawPassword("pass")
                .fullName(fullName)
                .email(username + "@consulthi.com")
                .dateBirth(LocalDate.of(1995, 1, i))
                .roles(Set.of(Role.ADMINISTRATOR))
                .build();
                
            User admin = userService.createUser(adminDto);
            userService.activateUser(admin.getId());
        }
    }

    private List<Long> createAndApproveProfessionals()
    {
        List<Role> professionalRoles = Arrays.asList(Role.COACH, Role.NUTRITIONIST, Role.PSYCHOLOGIST);
        List<Long> ids = new java.util.ArrayList<>();

        for (int i = 0; i < professionalRoles.size(); i++)
        {
            Role role = professionalRoles.get(i);
            int index = i + 1;
            String username = role.name().toLowerCase() + index;
            
            Set<ExpertiseArea> areas = new HashSet<>();
            if (role == Role.COACH) areas.add(ExpertiseArea.SPORTS);
            if (role == Role.NUTRITIONIST) areas.add(ExpertiseArea.CLINICAL_NUTRITION);
            if (role == Role.PSYCHOLOGIST) areas.add(ExpertiseArea.EDUCACIONAL_PSYCHOLOGY);

            UserDTO professionalDto = UserDTO.builder()
                .username(username)
                .rawPassword("pass" + index)
                .fullName("Profissional Teste " + index)
                .email(username + "@consulthi.com")
                .dateBirth(LocalDate.of(1988, index, 10))
                .roles(Set.of(role))
                .register("REG" + (1000 + index))
                .expertiseAreas(areas)
                .build();
                
            User professional = userService.createUser(professionalDto);
            
            userService.activateUser(professional.getId()); 
            
            ids.add(professional.getId());
        }
        return ids;
    }
    
    private void createStudent(List<Long> professionalIds)
    {
        String username = "student";
        
        UserDTO studentDto = UserDTO.builder()
            .username(username)
            .rawPassword("pass")
            .fullName("Aluno Teste")
            .email(username + "@consulthi.com")
            .dateBirth(LocalDate.of(2000, 5, 20))
            .roles(Set.of(Role.STUDENT))
            .selectedProfessionalIds(professionalIds) 
            .build();
            
        userService.createUser(studentDto);
    }
}