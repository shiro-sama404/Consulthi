// src/main/java/com/ThimoteoConsultorias/Consulthi/config/DataInitializer.java

package com.ThimoteoConsultorias.Consulthi.config;

import com.ThimoteoConsultorias.Consulthi.dto.UserDTO;
import com.ThimoteoConsultorias.Consulthi.enums.ExpertiseArea;
import com.ThimoteoConsultorias.Consulthi.enums.ExerciseType;
import com.ThimoteoConsultorias.Consulthi.enums.MuscleGroup;
import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.Exercise;
import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.repository.ExerciseRepository;
import com.ThimoteoConsultorias.Consulthi.service.UserService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner
{
    private final ExerciseRepository exerciseRepository;
    private final UserService userService;

    public DataInitializer
    (
        UserService userService,
        ExerciseRepository exerciseRepository
    )
    {
        this.exerciseRepository = exerciseRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception
    {
        System.out.println("--- Inicializando dados de teste (DataInitializer) ---");

        createExercises();

        createAdministrators();
        
        List<Long> professionalIds = createAndApproveProfessionals();
        
        createStudent(professionalIds);
        
        System.out.println("--- Inicialização de dados concluída ---");
    }

    private void createExercises()
    {
        List<Exercise> desiredExercises = List.of(
            Exercise.builder()
                .name("Supino Reto (Barra)")
                .description("Deite-se em um banco reto, segure a barra com uma pegada um pouco mais larga que os ombros. Desça a barra controladamente até o peito e empurre de volta à posição inicial.")
                .videoLink("https://www.youtube.com/watch?v=rxD321l2svE")
                .exerciseType(ExerciseType.FREE_WEIGHTS)
                .muscleGroups(Set.of(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS))
                .build(),
            Exercise.builder()
                .name("Agachamento Livre (Barra)")
                .description("Posicione a barra sobre os trapézios. Mantenha o peito erguido e o core ativado. Desça como se fosse sentar em uma cadeira, mantendo a curvatura lombar, até que os quadris fiquem abaixo dos joelhos.")
                .videoLink("https://www.youtube.com/watch?v=vmN-9oCd_F8")
                .exerciseType(ExerciseType.FREE_WEIGHTS)
                .muscleGroups(Set.of(MuscleGroup.QUADRICEPS, MuscleGroup.GLUTES, MuscleGroup.CORE, MuscleGroup.LEGS))
                .build(),
            Exercise.builder()
                .name("Puxada Alta (Pulley Frontal)")
                .description("Sente-se na máquina de polia alta e segure a barra com uma pegada larga. Puxe a barra em direção ao peito, contraindo as costas. Retorne lentamente à posição inicial.")
                .videoLink(null)
                .exerciseType(ExerciseType.MACHINE_ASSISTED)
                .muscleGroups(Set.of(MuscleGroup.LATS, MuscleGroup.BACK, MuscleGroup.BICEPS))
                .build(),
            Exercise.builder()
                .name("Rosca Direta (Halteres)")
                .description("Em pé, segure um halter em cada mão com as palmas voltadas para cima. Flexione o cotovelo, trazendo o halter em direção ao ombro, sem mover o cotovelo da lateral do corpo.")
                .videoLink(null)
                .exerciseType(ExerciseType.FREE_WEIGHTS)
                .muscleGroups(Set.of(MuscleGroup.BICEPS, MuscleGroup.FOREARM))
                .build(),
            Exercise.builder()
                .name("Prancha (Peso Corporal)")
                .description("Apoie os antebraços e as pontas dos pés no chão. Mantenha o corpo reto como uma tábua, contraindo o abdômen e os glúteos. Segure a posição pelo tempo determinado.")
                .videoLink(null)
                .exerciseType(ExerciseType.BODY_WEIGHT)
                .muscleGroups(Set.of(MuscleGroup.CORE))
                .build()
        );

        Set<String> existingExerciseNames = exerciseRepository.findAll().stream()
                .map(Exercise::getName)
                .collect(Collectors.toSet());

        List<Exercise> newExercisesToSave = desiredExercises.stream()
                .filter(exercise -> !existingExerciseNames.contains(exercise.getName()))
                .collect(Collectors.toList());

        if (!newExercisesToSave.isEmpty())
            exerciseRepository.saveAll(newExercisesToSave);
    }

    private void createAdministrators()
    {
        for (int i = 1; i <= 2; i++)
        {
            
            String username = "admin" + i;
            try
            {
                userService.getUserByUsername(username);
                continue;
            }
            catch (ResourceNotFoundException doNothing){}

            String fullName = "Admin Teste " + i;

            UserDTO adminDto = UserDTO.builder()
                .username(username)
                .rawPassword("123456")
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

            try
            {
                User existingUser = userService.getUserByUsername(username);
                ids.add(existingUser.getId());
                continue;
            }
            catch (ResourceNotFoundException doNothing){}
            
            Set<ExpertiseArea> areas = new HashSet<>();
            if (role == Role.COACH) areas.add(ExpertiseArea.SPORTS);
            if (role == Role.NUTRITIONIST) areas.add(ExpertiseArea.CLINICAL_NUTRITION);
            if (role == Role.PSYCHOLOGIST) areas.add(ExpertiseArea.EDUCACIONAL_PSYCHOLOGY);

            UserDTO professionalDto = UserDTO.builder()
                .username(username)
                .rawPassword("123456")
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

        try
        {
            userService.getUserByUsername(username);
            System.out.println("Usuário " + username + " já existe.");
            return;
        }
        catch (ResourceNotFoundException e) {}
        
        UserDTO studentDto = UserDTO.builder()
            .username(username)
            .rawPassword("123456")
            .fullName("Aluno Teste")
            .email(username + "@consulthi.com")
            .dateBirth(LocalDate.of(2000, 5, 20))
            .roles(Set.of(Role.STUDENT))
            .selectedProfessionalIds(professionalIds) 
            .build();
            
        userService.createUser(studentDto);
    }
}