package com.ThimoteoConsultorias.Consulthi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidade de ligação que rastreia qual usuário recebeu qual notificação e se ele leu.
 */
@Entity
@Table(name = "notification_recipient")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
// Nota: Em sistemas complexos, você pode querer uma chave composta, mas o ID simples é suficiente aqui.
public class NotificationRecipient
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chave estrangeira para a notificação (Muitos Recipients para Uma Notification)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    // ID do usuário que recebe a notificação
    @Column(nullable = false)
    private Long recipientUserId; 
    
    @Builder.Default
    private boolean isRead = false;
    
    private LocalDateTime dateRead;
}