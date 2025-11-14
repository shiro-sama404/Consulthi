package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.enums.LinkStatus;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.model.Student;
import com.ThimoteoConsultorias.Consulthi.model.StudentProfessionalLink;
import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.repository.UserRepository;
import com.ThimoteoConsultorias.Consulthi.repository.InactivationSchedulingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdministratorService
{
    // ----------------------------------------------------
    // 1. DEPENDÊNCIAS
    // ----------------------------------------------------
    private final InactivationSchedulingRepository inactivationSchedulingRepository;
    private final UserRepository userRepository;
    private final StudentProfessionalLinkService linkService;
    private final NotificationService notificationService;
    private final ProfessionalService professionalService;
    private final StudentService studentService;
    private final UserService userService;

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public AdministratorService
    (
        InactivationSchedulingRepository inactivationSchedulingRepository,
        UserRepository userRepository,
        StudentProfessionalLinkService linkService,
        NotificationService notificationService,
        ProfessionalService professionalService,
        StudentService studentService,
        UserService userService
    )
    {
        this.inactivationSchedulingRepository = inactivationSchedulingRepository;
        this.userRepository = userRepository;
        this.linkService = linkService;
        this.notificationService = notificationService;
        this.professionalService = professionalService;
        this.studentService = studentService;
        this.userService = userService;
    }

    // ----------------------------------------------------
    // 3. MÉTODOS DE DOMÍNIO
    // ----------------------------------------------------

    /*
     * APPROVAL / UPDATE
     */

    /**
     * Implementa o RF01: Aprova o registro de um Profissional pendente.
     * Ativa o usuário e notifica o profissional.
     * @param userId ID do User (Profissional) a ser ativado.
     * @throws IllegalArgumentException se o perfil não for profissional.
     */
    @Transactional
    public void approveProfessionalRegistration(Long userId)
    throws IllegalArgumentException
    {
        User user = userService.getUserById(userId);
        
        // Validação de segurança/regra de negócio
        if (user.getRoles().stream().noneMatch(Role::isProfessionalRole))
            throw new IllegalArgumentException("A aprovação de registro se aplica apenas a perfis profissionais.");

        userService.activateUser(userId);
        
        notificationService.notifyProfessionalOfApproval(userId);
    }

    /*
     * READ
     */

    /**
     * Retorna todos os usuários do sistema sem filtro.
     */
    public List<User> listAllUsers()
    {
        return userService.getAllUsers();
    }

    /**
     * Retorna usuários que possuem a Role especificada.
     */
    public List<User> getUsersByRole(Role role)
    {
        return userRepository.findByRolesIn(Collections.singleton(role));
    }

    /**
     * Retorna todos os usuários Administradores.
     */
    public List<User> getAdministratorUsers()
    {
        return getUsersByRole(Role.ADMINISTRATOR);
    }

    /**
     * Retorna apenas os IDs de todos os Administradores.
     */
    public List<Long> getAdministratorIds()
    {
        return getAdministratorUsers()
            .stream()
            .map(User::getId)
            .collect(Collectors.toList());
    }

    /**
     * Retorna profissionais que estão inativos (pendentes de aprovação - RF01).
     */
    public List<User> getPendingProfessionalRegistrations()
    {
        EnumSet<Role> professionalRoles = EnumSet.of(Role.COACH, Role.PSYCHOLOGIST, Role.NUTRITIONIST);

        List<User> pendingUsers = userRepository.findByActiveFalseAndRolesIn(professionalRoles);

        return pendingUsers.stream()
            .filter(user -> user.getRoles().stream().anyMatch(Role::isProfessionalRole))
            .collect(Collectors.toList());
    }

    /*
     * DELETE
     */

    /**
     * Implementa o RF08: Remove permanentemente o usuário e seus dados associados.
     * Notifica os alunos ou profissionais afetados antes da exclusão.
     * @param userId ID do User a ser removido.
     */
    @Transactional
    public void removeUser(Long userId)
    {
        User userToDelete = userService.getUserById(userId);
        List<StudentProfessionalLink> links;
        
        // Vínculos que devem ser considerados para notificação (Aceitos e Pendentes)
        EnumSet<LinkStatus> activeStatuses = EnumSet.of(LinkStatus.ACCEPTED, LinkStatus.PENDING);

        // Notifica profissionais da deleção do estudante
        if (userToDelete.getRoles().contains(Role.STUDENT))
        {
            try
            {
                Student studentProfile = studentService.getStudentById(userId);
                links = linkService.getLinksByStudentAndStatusIn(studentProfile, activeStatuses);

                if (!links.isEmpty())
                {
                    List<User> receivers = links.stream()
                        .map(link -> link.getProfessional().getUser())
                        .collect(Collectors.toList());
                    
                    notificationService.notifyLinkTermination(userToDelete, receivers);
                }
            }
            catch(ResourceNotFoundException doNothing){}
        }
        
        // Notifica estudantes da deleção do profissional
        if (userToDelete.getRoles().stream().anyMatch(Role::isProfessionalRole))
        {
            try
            {
                Professional professionalProfile = professionalService.getProfessionalById(userId);
                links = linkService.getLinksByProfessionalAndStatusIn(professionalProfile, activeStatuses);

                if (!links.isEmpty())
                {
                    List<User> receivers = links.stream()
                        .map(link -> link.getStudent().getUser())
                        .collect(Collectors.toList());
                    
                    notificationService.notifyLinkTermination(userToDelete, receivers);
                }
            }
            catch(ResourceNotFoundException doNothing){}
        }

        inactivationSchedulingRepository.findById(userId).ifPresent(schedule -> {
            inactivationSchedulingRepository.delete(schedule);
        });

        userRepository.delete(userToDelete);
        System.out.println("ADMIN: Usuário " + userId + " removido permanentemente.");
    }
}