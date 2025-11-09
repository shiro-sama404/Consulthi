package com.ThimoteoConsultorias.Consulthi.repository;

import com.ThimoteoConsultorias.Consulthi.model.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>
{}