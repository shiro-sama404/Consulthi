package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.Content;
import com.ThimoteoConsultorias.Consulthi.model.Routine;
import com.ThimoteoConsultorias.Consulthi.model.RoutineInstance;
import com.ThimoteoConsultorias.Consulthi.model.Student;
import com.ThimoteoConsultorias.Consulthi.model.Training;
import com.ThimoteoConsultorias.Consulthi.model.TrainingHistory;
import com.ThimoteoConsultorias.Consulthi.repository.RoutineInstanceRepository;
import com.ThimoteoConsultorias.Consulthi.repository.TrainingHistoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RoutineInstanceService
{
    // ----------------------------------------------------
    // 1. DEPENDÊNCIAS
    // ----------------------------------------------------
    private final RoutineInstanceRepository routineInstanceRepository;
    private final TrainingHistoryRepository trainingHistoryRepository;
    private final StudentService studentService;
    private final ContentService contentService;
    private final TrainingService trainingService;

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public RoutineInstanceService
    (
        RoutineInstanceRepository routineInstanceRepository,
        TrainingHistoryRepository trainingHistoryRepository,
        ContentService contentService,
        StudentService studentService,
        TrainingService trainingService
    )
    {
        this.routineInstanceRepository = routineInstanceRepository;
        this.trainingHistoryRepository = trainingHistoryRepository;
        this.studentService = studentService;
        this.contentService = contentService;
        this.trainingService = trainingService;
    }

    // ----------------------------------------------------
    // 3. MÉTODOS DE DOMÍNIO
    // ----------------------------------------------------

    /*
     * CREATE
     */

    /**
     * Atribui uma rotina (Template) a um aluno, criando uma nova instância de uso.
     * @param studentUserId O ID do Aluno.
     * @param routineContentId O ID do Template de Rotina.
     * @param startDate A data de início da rotina.
     * @param endDate A data de fim da rotina.
     * @return A nova RoutineInstance.
     * @throws ResourceNotFoundException se Student ou Routine não forem encontrados.
     * @throws IllegalArgumentException se o contentId não for uma Rotina.
     * @throws IllegalStateException se a rotina já estiver ativa para o aluno.
     */
    @Transactional
    public RoutineInstance assignRoutineToStudent
    (
        Long studentUserId,
        Long routineContentId,
        LocalDate startDate, 
        LocalDate endDate
    )
    {
        Student student = studentService.getStudentById(studentUserId);

        Content content = contentService.getContentById(routineContentId);
        if (!(content instanceof Routine routine)) {
             throw new IllegalArgumentException("O ID fornecido não pertence a um template de Rotina.");
        }

        // Verifica se a instância já existe (para evitar duplicação)
        Optional<RoutineInstance> existingInstance = routineInstanceRepository.findByStudentAndRoutine(student, routine);
        if (existingInstance.isPresent())
            throw new IllegalStateException("Esta rotina já está ativa para este aluno.");
        
        RoutineInstance instance = RoutineInstance.builder()
            .student(student)
            .routine(routine)
            .startDate(startDate)
            .endDate(endDate)
            .build();
            
        return routineInstanceRepository.save(instance);
    }
    
    /*
     * HISTÓRICO / LOG
     */

    /**
     * Registra a execução de um treino por parte do aluno (Histórico).
     * @param routineInstanceId A instância de rotina que está sendo executada.
     * @param trainingId O ID do Treino (Template) que foi realizado.
     * @param studentNotes Notas do aluno sobre o treino.
     * @return O registro de TrainingHistory criado.
     * @throws ResourceNotFoundException se Instância ou Treino não forem encontrados.
     */
    @Transactional
    public TrainingHistory logTrainingExecution(Long routineInstanceId, Long trainingId, String studentNotes)
    {
        RoutineInstance instance = routineInstanceRepository.findById(routineInstanceId)
            .orElseThrow(() -> new ResourceNotFoundException("Instância de rotina não encontrada."));
        
        Training trainingTemplate = trainingService.getTrainingById(trainingId);
        
        TrainingHistory history = TrainingHistory.builder()
            .routineInstance(instance)
            .training(trainingTemplate)
            .executionDateTime(LocalDateTime.now())
            .studentNotes(studentNotes)
            .build();
            
        return trainingHistoryRepository.save(history);
    }

    /*
     * READ
     */

    /**
     * Busca todas as instâncias de rotina ativas para um aluno logado (RF06).
     */
    public List<RoutineInstance> getActiveRoutinesByStudent(Long studentUserId)
    {
        Student student = studentService.getStudentById(studentUserId);
        return routineInstanceRepository.findByStudent(student);
    }

    /**
     * Busca o histórico de execução de treinos para uma instância específica.
     */
    public List<TrainingHistory> getHistoryByRoutineInstance(Long routineInstanceId)
    {
        return trainingHistoryRepository.findByRoutineInstanceIdOrderByExecutionDateTimeDesc(routineInstanceId);
    }
}