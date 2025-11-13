package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.dto.UserDTO;
import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.Student;
import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.repository.UserRepository;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService
{
    // ----------------------------------------------------
    // 1. DEPENDÊNCIAS
    // ----------------------------------------------------
    private final PasswordEncoder passwordEncoder;
    private final UserRepository  userRepository;

    private final StudentProfessionalLinkService linkService;
    private final NotificationService notificationService;
    private final ProfessionalService professionalService; 
    private final SchedulerService schedulerService; 
    private final StudentService studentService; 

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public UserService
    (
        @Lazy StudentProfessionalLinkService linkService,
        NotificationService notificationService,
        PasswordEncoder passwordEncoder,
        ProfessionalService professionalService,
        @Lazy SchedulerService schedulerService,
        StudentService studentService,
        UserRepository userRepository
    )
    {
        this.passwordEncoder = passwordEncoder;
        this.userRepository  = userRepository;

        this.linkService = linkService;
        this.notificationService = notificationService;
        this.professionalService = professionalService;
        this.schedulerService = schedulerService;
        this.studentService = studentService;
    }

    // ----------------------------------------------------
    // 3. MÉTODOS DE DOMÍNIO
    // ----------------------------------------------------

    /*
     * CREATE
     */

    /**
     * Implementa o RF01: Cria o usuário base e perfis específicos (Aluno/Profissional).
     * @param userDetails DTO contendo os dados de registro.
     * @return O User recém-criado.
     * @throws IllegalArgumentException se o username já estiver em uso.
     */
    @Transactional(rollbackFor = Exception.class)
    public User createUser(UserDTO userDetails)
    {
        if (userRepository.findByUsername(userDetails.username()).isPresent())
             throw new IllegalArgumentException("O nome de usuário já está em uso.");
        
        String passwordHash = passwordEncoder.encode(userDetails.rawPassword());

        User user = User.builder()
            .fullName             (userDetails.fullName()      )
            .dateBirth            (userDetails.dateBirth()     )
            .username             (userDetails.username()      )
            .roles                (userDetails.roles()         )
            .passwordHash         (passwordHash                )
            .email                (userDetails.email()         )
            .phoneNumber          (userDetails.phoneNumber()   )
            .failedLoginAttempts  (0                           )
            .dateLogon            (LocalDateTime.now()         )
            .active               (false                       )
            .build();

        User savedUser = userRepository.save(user);

        if (userDetails.roles().contains(Role.STUDENT))
        {
            Student student = studentService.createStudentProfile(savedUser);
            
            if (userDetails.selectedProfessionalIds() != null && !userDetails.selectedProfessionalIds().isEmpty())
                linkService.createPendingLinks(student, userDetails.selectedProfessionalIds());
        } 
        
        if (userDetails.roles().stream().anyMatch(Role::isProfessionalRole))
        {
            professionalService.createProfessionalProfile(
                savedUser, 
                userDetails.register(),
                userDetails.expertiseAreas()
            );
            
           notificationService.notifyAdminOfProfessionalRegistration(savedUser.getId());
        }

        return savedUser;
    }

    /**
     * Ativa o usuário após a aprovação do vínculo/registro (RF01).
     * @param userId O ID do usuário a ser ativado.
     * @throws ResourceNotFoundException se o usuário não for encontrado.
     */
    @Transactional
    public void activateUser(Long userId)
    {
        User user = getUserById(userId);
        
        if (!user.isActive())
        {
            user.setActive(true);
            userRepository.save(user);

            notificationService.notifyStudentOfActivation(userId);
        }
    }

    /*
     * READ
     */

    /**
     * Retorna todos os usuários com paginação.
     */
    public Page<User> getAllUsers(Pageable pageble)
    {
        return userRepository.findAll(pageble);
    }

    /**
     * Retorna o usuário pelo ID.
     * @throws ResourceNotFoundException se o usuário não for encontrado.
     */
    public User getUserById(Long id) throws ResourceNotFoundException
    {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário de id '" + id +"' não encontrado."));
    }

    /**
     * Retorna o usuário pelo username.
     * @throws ResourceNotFoundException se o usuário não for encontrado.
     */
    public User getUserByUsername(String username) throws ResourceNotFoundException
    {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário de username '" + username +"' não encontrado."));
    }

    /**
     * Retorna o usuário pelo email.
     * @throws ResourceNotFoundException se o usuário não for encontrado.
     */
    public User getUserByEmail(String email) throws ResourceNotFoundException
    {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Email '" + email +"' não encontrado."));
    }

    /*
     * UPDATE
     */

    /**
     * Implementa o RF03: Permite que o usuário edite seus dados.
     * @throws ResourceNotFoundException se o usuário não for encontrado.
     * @throws IllegalArgumentException se o novo username já estiver em uso.
     */
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(Long id, UserDTO userDetails)
    {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado. id: " + id));

        // Garante que o novo username não esteja em uso
        Optional<User> existingUser = userRepository.findByUsername(userDetails.username());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(id))
             throw new IllegalArgumentException("O nome de usuário já está em uso por outro registro.");
        
        user.setFullName    (userDetails.fullName()   );
        user.setDateBirth   (userDetails.dateBirth()  );
        user.setUsername    (userDetails.username()   );
        user.setEmail       (userDetails.email()      );
        user.setPhoneNumber (userDetails.phoneNumber());

        // Atualiza a senha apenas se fornecida
        if (userDetails.rawPassword() != null && !userDetails.rawPassword().isEmpty())
            user.setPasswordHash(passwordEncoder.encode(userDetails.rawPassword()));

        return userRepository.save(user);
    }
    
    /**
     * Registra uma falha de login, incrementando o contador e notificando se o limite for atingido.
     */
    //@Transactional
    public void registerFailedLogin(String username)
    {
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user != null)
        {
            int newAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(newAttempts);
            
            if (newAttempts == 5)
                notificationService.notifyUserOfMultipleFailedLogins(user.getId(), newAttempts);

            userRepository.save(user);
        }
    }

    /**
     * Reseta a contagem de falhas após login bem-sucedido.
     */
    //@Transactional
    public void resetLoginAttempts(String username)
    {
        User user = getUserByUsername(username); 
        
        if (user != null && user.getFailedLoginAttempts() > 0)
        {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }

    /*
     * DELETE / INACTIVATION
     */

    /**
     * Implementa o RF04: Solicita o cancelamento do cadastro, desativando a conta
     * e agendando a exclusão dos dados em 1 mês.
     * @param userId O ID do usuário que solicita a desativação.
     * @throws ResourceNotFoundException se o usuário não for encontrado.
     */
    @Transactional
    public User requestDeactivation(Long userId) throws ResourceNotFoundException
    {
        User user = getUserById(userId);
        
        user.setActive(false);
        userRepository.save(user);

        schedulerService.scheduleDataDeletion(user);
        
        return user;
    }
}