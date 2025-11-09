package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.enums.NotificationType;
import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.model.Notification;
import com.ThimoteoConsultorias.Consulthi.model.NotificationRecipient;
import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.repository.NotificationRepository;
import com.ThimoteoConsultorias.Consulthi.repository.NotificationRecipientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Lazy
@Service
public class NotificationService
{
    // ----------------------------------------------------
    // 1. DEPENDÊNCIAS
    // ----------------------------------------------------
    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository recipientRepository;
    
    private AdministratorService administratorService; 

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public NotificationService
    (
        NotificationRepository notificationRepository,
        NotificationRecipientRepository recipientRepository
    )
    {
        this.notificationRepository = notificationRepository;
        this.recipientRepository = recipientRepository;
    }

    @Autowired
    @Lazy
    public void setAdministratorService(AdministratorService administratorService)
    {
        this.administratorService = administratorService;
    }

    // ----------------------------------------------------
    // 3. MÉTODOS DE INFRAESTRUTURA (BASE)
    // ----------------------------------------------------

    /**
     * BASE: Cria e persiste uma notificação e seu destinatário.
     */
    @Transactional
    private Notification sendNotification
    (
        String title, 
        String messageBody, 
        NotificationType type, 
        Long senderUserId, 
        Long receiverUserId
    )
    {
        if (receiverUserId == null)
            throw new IllegalArgumentException("Destinatário não especificado.");
        
        Notification notification = Notification.builder()
            .title(title)
            .messageBody(messageBody)
            .type(type)
            .senderUserId(senderUserId)
            .dateSent(LocalDateTime.now())
            .build();
            
        Notification savedNotification = notificationRepository.save(notification);

        NotificationRecipient recipient = NotificationRecipient.builder()
            .notification(savedNotification)
            .recipientUserId(receiverUserId)
            .build();
            
        recipientRepository.save(recipient);
        
        System.out.println("[DB NOTIFICATION] Enviada notificação de ID " + savedNotification.getId() + " para o usuário: " + receiverUserId);
        
        return savedNotification;
    }
    
    // ----------------------------------------------------
    // 4. MÉTODOS DE DOMÍNIO (FÁBRICA)
    // ----------------------------------------------------

    /*
     * FLUXO DE VÍNCULO E SEGURANÇA
     */
    public void notifyProfessionalOfLinkAcceptance(Long professionalId, Long studentId)
    {
        sendNotification(
            "Solicitação Aceita",
            "O aluno de ID " + studentId + " aceitou seu vínculo! Você já pode fornecer acesso ao conteúdo.",
            NotificationType.INFO,
            0L, // Sistema
            professionalId
        );
    }

    public void notifyProfessionalOfApproval(Long professionalUserId)
    {
        sendNotification(
            "Registro Aprovado",
            "Seu registro como profissional foi aprovado. Você já pode fazer login e acessar seu painel!",
            NotificationType.ACCESS_GRANTED,
            0L, // Sistema
            professionalUserId
        );
    }

    /**
     * Implementa o fluxo de notificação de encerramento de vínculo (RF05/RF08).
     * Notifica todos os receptores com uma mensagem baseada na Role do remetente.
     */
    @Transactional
    public void notifyLinkTermination(User sender, List<User> receivers)
    {
        String senderUsername = sender.getUsername();
        String receiverBody;

        if (sender.getRoles().stream().anyMatch(Role::isProfessionalRole))
            receiverBody = "Seu vínculo com o profissional " + senderUsername + " foi encerrado.";
        else
            receiverBody = "O vínculo com o aluno " + senderUsername + " foi encerrado.";

        for (User receiver : receivers)
            sendNotification(
                "Vínculo Encerrado",
                receiverBody,
                NotificationType.INFO,
                sender.getId(), 
                receiver.getId()
            );
    }
    
    /**
     * Notificação RF01: Aluno ativado.
     */
    public void notifyStudentOfActivation(Long studentUserId)
    {
        sendNotification(
            "Conta Ativada",
            "Seu cadastro foi aprovado por um de seus profissionais. Você já pode fazer login e acessar o conteúdo!",
            NotificationType.ACCESS_GRANTED,
            0L, // Sistema
            studentUserId
        );
    }

    /**
     * Notificação RF01: Profissional notificado sobre nova solicitação de Aluno.
     */
    public void notifyProfessionalOfNewStudent(Long professionalId, Long studentId)
    {
        sendNotification(
            "Nova Solicitação de Vínculo",
            "O aluno de ID " + studentId + " solicitou vínculo com você! Acesse seu painel para aprovar.",
            NotificationType.PENDING_APPROVAL,
            0L, // Sistema
            professionalId
        );
    }

    /**
     * Notifica o usuário sobre múltiplas tentativas de login falhadas.
     */
    public void notifyUserOfMultipleFailedLogins(Long userId, int failedAttempts)
    {
        sendNotification(
            "Alerta de Segurança: Múltiplas Tentativas de Login Falhadas",
            "Detectamos " + failedAttempts + " tentativas de login falhadas em sua conta. Se não foi você, recomendamos alterar sua senha imediatamente.",
            NotificationType.SYSTEM_ALERT,
            0L, // Sistema
            userId
        );
    }

    /*
     * ADMIN/REGISTRO
     */

    /**
     * Notifica o Administrador sobre a necessidade de revisão de um novo Profissional pendente (RF01).
     */
    public void notifyAdminOfProfessionalRegistration(Long userId)
    {
        for (Long adminId : administratorService.getAdministratorIds())
            sendNotification(
                "Nova Aprovação Pendente",
                "O novo usuário profissional (ID: " + userId + ") aguarda sua aprovação de registro.",
                NotificationType.PENDING_APPROVAL,
                0L, // Sistema
                adminId
            );
    }

    /**
     * Notificação RF01: Alerta de Escalada (1 semana) para o Administrador.
     */
    public void notifyAdminOfEscalation(int count)
    {
        for (Long adminId : administratorService.getAdministratorIds())
            sendNotification(
                "ALERTA: Solicitações Expirando",
                "Existem " + count + " vínculos de alunos pendentes há mais de uma semana. Revisão urgente necessária.",
                NotificationType.SYSTEM_ALERT,
                0L, // Sistema
                adminId
            );
    }
}