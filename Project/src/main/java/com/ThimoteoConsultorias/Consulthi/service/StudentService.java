package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.Student;
import com.ThimoteoConsultorias.Consulthi.model.embeddables.Goal;
import com.ThimoteoConsultorias.Consulthi.repository.StudentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService
{
    // ----------------------------------------------------
    // 1. DEPENDÊNCIAS
    // ----------------------------------------------------
    private final StudentRepository studentRepository;

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public StudentService(StudentRepository studentRepository)
    {
        this.studentRepository = studentRepository;
    }

    // ----------------------------------------------------
    // 3. MÉTODOS DE DOMÍNIO
    // ----------------------------------------------------

    /*
     * CREATE
     */

    /**
     * Cria o perfil de estudante para um novo usuário.
     * @param user O User recém-criado.
     * @return A entidade Student persistida.
     */
    @Transactional
    public Student createStudentProfile(User user)
    {
        Goal defaultGoal = Goal.builder()
                .targetWeight(0.0f) 
                .targetBodyFat(0.0f)
                .build();
        
        Student student = Student.builder()
                .user(user)
                .goal(defaultGoal)
                .build();

        return studentRepository.save(student);
    }

    /*
     * READ
     */

    /**
     * Retorna o perfil Student pelo ID (que é o mesmo ID do User).
     * @param id O ID do User/Student.
     * @return A entidade Student.
     * @throws ResourceNotFoundException se o Student não for encontrado.
     */
    public Student getStudentById(Long id) throws ResourceNotFoundException
    {
        return studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Estudante de id '" + id +"' não encontrado."));
    }

    /*
     * UPDATE
     */

    /**
     * Atualiza as metas (Goal) de um estudante.
     * @param studentId O ID do estudante.
     * @param newGoal O novo objeto Goal.
     * @return A entidade Student atualizada.
     * @throws ResourceNotFoundException se o Student não for encontrado.
     */
    @Transactional
    public Student updateStudentGoals(Long studentId, Goal newGoal) throws ResourceNotFoundException
    {
        // Usa getStudentById para garantir que o usuário existe (Jeito 2 de reutilização)
        Student student = getStudentById(studentId);
            
        student.setGoal(newGoal);
        return studentRepository.save(student);
    }
}