package com.ThimoteoConsultorias.Consulthi.repository;

import com.ThimoteoConsultorias.Consulthi.model.Routine;
import com.ThimoteoConsultorias.Consulthi.model.RoutineInstance;
import com.ThimoteoConsultorias.Consulthi.model.Student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoutineInstanceRepository extends JpaRepository<RoutineInstance, Long>
{
    /**
     * Busca todas as instâncias de rotina ativas para um aluno (RF06).
     */
    List<RoutineInstance> findByStudent(Student student);

    /**
     * Busca uma instância de rotina ativa para um aluno e um template de rotina específicos.
     */
    Optional<RoutineInstance> findByStudentAndRoutine(Student student, Routine routine);
}