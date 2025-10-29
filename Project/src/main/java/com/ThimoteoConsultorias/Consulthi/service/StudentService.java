package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.model.Student;
import com.ThimoteoConsultorias.Consulthi.model.embeddables.Goal;
import com.ThimoteoConsultorias.Consulthi.repository.StudentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Student createStudentProfile(User user) {
        Goal defaultGoal = Goal.builder()
                .targetWeight(0.0f) 
                .targetBodyFat(0.0f)
                .build();
        
        Student student = Student.builder()
                .id(user.getId())
                .user(user)
                .goal(defaultGoal)
                .build();

        return studentRepository.save(student);
    }
    
    // TODO:
    // - Atualizar metas (Goal) do aluno.
    // - Buscar o perfil de Student pelo ID do User.
}