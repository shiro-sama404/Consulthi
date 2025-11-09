package com.ThimoteoConsultorias.Consulthi.repository;

import com.ThimoteoConsultorias.Consulthi.model.NotificationRecipient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long>
{
    /**
     * Busca todas as notificações para um usuário específico (Caixa de Entrada).
     */
    List<NotificationRecipient> findByRecipientUserIdOrderByNotificationDateSentDesc(Long recipientUserId);
    
    /**
     * Conta o número de notificações NÃO lidas para o ícone de sino.
     */
    long countByRecipientUserIdAndIsReadFalse(Long recipientUserId);
}