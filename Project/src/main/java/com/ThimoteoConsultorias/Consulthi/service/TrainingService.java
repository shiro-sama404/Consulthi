package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.dto.TrainingDTO;
import com.ThimoteoConsultorias.Consulthi.dto.TrainingSetDTO;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.embeddables.TrainingSet;
import com.ThimoteoConsultorias.Consulthi.model.Training;
import com.ThimoteoConsultorias.Consulthi.repository.ExerciseRepository;
import com.ThimoteoConsultorias.Consulthi.repository.TrainingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingService
{
    // ----------------------------------------------------
    // 1. DEPENDÊNCIAS
    // ----------------------------------------------------
    private final ExerciseRepository exerciseRepository;
    private final TrainingRepository trainingRepository;

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public TrainingService
    (
        ExerciseRepository exerciseRepository,
        TrainingRepository trainingRepository
    )
    {
        this.exerciseRepository = exerciseRepository;
        this.trainingRepository = trainingRepository;
    }

    // ----------------------------------------------------
    // 3. MÉTODOS DE DOMÍNIO
    // ----------------------------------------------------

    /*
     * CREATE
     */

    /**
     * Mapeia TrainingDTO para a entidade Treino, convertendo e validando as Séries.
     * @param dto O DTO contendo os dados do treino.
     * @return A entidade Treino persistida.
     */
    @Transactional
    public Training createTrainingFromDTO(TrainingDTO dto)
    {
        List<TrainingSet> sets = dto.trainingSets().stream()
            .map(this::mapTrainingSetDtoToEntity)
            .collect(Collectors.toList());
        
        Training training = Training.builder()
            .name(dto.name())
            .targetMuscleGroups(dto.targetMuscleGroups())
            .trainingSets(sets)
            .build();
            
        return trainingRepository.save(training);
    }

    /*
     * READ
     */

    /**
     * Retorna o template de Treino pelo ID.
     * @throws ResourceNotFoundException se o Treino não for encontrado.
     */
    public Training getTrainingById(Long id) throws ResourceNotFoundException
    {
        return trainingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Treino de id '" + id +"' não encontrado."));
    }

    /**
     * Retorna oa templatea de todos os Treinos.
     */
    public List<Training> listAllExercises() throws ResourceNotFoundException
    {
        return trainingRepository.findAll();
    }

    // ----------------------------------------------------
    // 4. MÉTODOS AUXILIARES
    // ----------------------------------------------------

    /**
     * Mapeia um Training para TrainingDTO.
     */
    public TrainingDTO toDTO(Training training)
    {
        List<TrainingSetDTO> trainingSetDTOs = training
            .getTrainingSets()
            .stream()
            .map(set -> new TrainingSetDTO(
                set.getExerciseOrder(),
                set.getExerciseId(),
                set.getRepetitions(),
                set.getRestTimeSeconds(),
                set.getTechnique(),
                set.getLoadInKg(),
                set.getDurationSeconds()
            ))
            .collect(Collectors.toList());

        TrainingDTO dto = TrainingDTO.builder()
            .id(training.getId())
            .name(training.getName())
            .targetMuscleGroups(training.getTargetMuscleGroups())
            .trainingSets(trainingSetDTOs)
            .build();

        return dto;
    }

    /**
     * Método auxiliar para mapear o DTO para a entidade Embeddable TrainingSet.
     * Inclui validação para garantir que o Exercise ID existe no catálogo.
     */
    private TrainingSet mapTrainingSetDtoToEntity(TrainingSetDTO dto)
    {
        if (dto.exerciseId() != null)
        {
            exerciseRepository.findById(dto.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercício de ID " + dto.exerciseId() + " não encontrado no catálogo."));
        }

        return TrainingSet.builder()
            .exerciseOrder(dto.exerciseOrder())
            .exerciseId(dto.exerciseId())
            .repetitions(dto.repetitions())
            .restTimeSeconds(dto.restTimeSeconds())
            .technique(dto.technique())
            .loadInKg(dto.loadInKg())
            .durationSeconds(dto.durationSeconds())
            .build();
    }
}