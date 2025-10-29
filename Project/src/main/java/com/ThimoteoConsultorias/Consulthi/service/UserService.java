package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.dto.UserDTO;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.repository.UserRepository;

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
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    private final StudentService studentService; 
    private final ProfessionalService professionalService; 
    private final StudentProfessionalLinkService linkService;

    public UserService
    (
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        StudentService studentService,
        ProfessionalService professionalService,
        StudentProfessionalLinkService linkService
    )
    {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.studentService = studentService;
        this.professionalService = professionalService;
        this.linkService = linkService;
    }

    public Page<User> getAllUsers(Pageable pageble)
    {
        return userRepository.findAll(pageble);
    }

    public User getUserById(Long id) throws ResourceNotFoundException
    {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário de id '" + id +"' não encontrado."));
    }

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
            .failedLoginAttempts  (0       )
            .dateLogon            (LocalDateTime.now()         )
            .active               (false                ) 
            .build();

        User savedUser = userRepository.save(user);

        if (userDetails.roles().contains(Role.STUDENT))
        {
            studentService.createStudentProfile(savedUser);
            
            // TODO
            // Vincular Aluno com os profissionais escolhidos
            // linkService.createPendingLinks(studentService.getStudentByUser(savedUser), userDetails.professionalIds());
        } 
        
        if (userDetails.roles().stream().anyMatch(Role::isProfessionalRole))
        {
            professionalService.createProfessionalProfile(
                savedUser, 
                userDetails.register(),
                userDetails.expertiseAreas()
            );
            
            // TODO
            // Notificar administrador sobre nova solicitação de profissional
        }
        
        return savedUser;
    }

    @Transactional(rollbackFor = Exception.class)
    public User updateUser(Long id, UserDTO userDetails)
    {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado. id: " + id));

        // Regra de Negócio:
        // Garante que o novo username não esteja em uso
        Optional<User> existingUser = userRepository.findByUsername(userDetails.username());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(id))
             throw new IllegalArgumentException("O nome de usuário já está em uso por outro registro.");
        
        user.setFullName    (userDetails.fullName()   );
        user.setDateBirth   (userDetails.dateBirth()  );
        user.setUsername    (userDetails.username()   );
        user.setEmail       (userDetails.email()      );
        user.setPhoneNumber (userDetails.phoneNumber());

        // TODO
        // A senha só deve ser alterada se rawPassword() for fornecida

        return userRepository.save(user);
    }

    public void deleteUser(Long id)
    {
        // TODO
        // Desativar a conta e agendar a exclusão invés de excluir diretamente.

        // exclusão direta
        userRepository.deleteById(id);
    }
    
    // TODO
    // Método para o Administrador para remover cadastros, 
    // e notificar profissionais vinculados.
}