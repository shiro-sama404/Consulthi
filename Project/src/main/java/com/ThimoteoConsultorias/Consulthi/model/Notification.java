package com.ThimoteoConsultorias.Consulthi.model;

import com.ThimoteoConsultorias.Consulthi.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidade que armazena o conteúdo da notificação (o alerta em si).
 */
@Entity
@Table(name = "notification")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notification
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String messageBody;

    // Tipo/Categoria da notificação (EX: APPROVAL, SYSTEM, ACCESS_GRANTED)
    @Enumerated(EnumType.STRING)
    private NotificationType type; 
    
    // ID do usuário que enviou a notificação (0 para o sistema)
    @Column(nullable = false)
    private Long senderUserId; 
    
    private LocalDateTime dateSent;
    
    // Lista de entidades que mapeiam os receptores e o status de leitura
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<NotificationRecipient> recipients;
}