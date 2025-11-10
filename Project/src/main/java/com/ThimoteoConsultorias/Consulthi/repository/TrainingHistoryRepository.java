package com.ThimoteoConsultorias.Consulthi.repository;

import com.ThimoteoConsultorias.Consulthi.model.TrainingHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingHistoryRepository extends JpaRepository<TrainingHistory, Long>
{
    List<TrainingHistory> findByRoutineInstanceIdOrderByExecutionDateTimeDesc(Long routineInstanceId);
}