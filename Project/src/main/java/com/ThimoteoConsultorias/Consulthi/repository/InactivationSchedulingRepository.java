package com.ThimoteoConsultorias.Consulthi.repository;

import com.ThimoteoConsultorias.Consulthi.model.InactivationScheduling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InactivationSchedulingRepository extends JpaRepository<InactivationScheduling, Long>
{
    List<InactivationScheduling> findByDateScheduledDeletionBeforeAndDateActualDeletionIsNull(LocalDateTime cutoffDate);
}