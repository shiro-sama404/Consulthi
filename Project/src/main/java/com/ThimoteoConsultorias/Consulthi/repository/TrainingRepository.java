package com.ThimoteoConsultorias.Consulthi.repository;

import com.ThimoteoConsultorias.Consulthi.model.Training;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>
{}