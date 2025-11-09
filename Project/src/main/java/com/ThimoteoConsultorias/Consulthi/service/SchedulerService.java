package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.enums.LinkStatus;
import com.ThimoteoConsultorias.Consulthi.model.InactivationScheduling;
import com.ThimoteoConsultorias.Consulthi.model.StudentProfessionalLink;
import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.repository.InactivationSchedulingRepository;
import com.ThimoteoConsultorias.Consulthi.repository.UserRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
public class SchedulerService 
{ 
    // ----------------------------------------------------
    // 1. DEPENDÊNCIAS
    // ----------------------------------------------------
    private final InactivationSchedulingRepository inactivationSchedulingRepository;
    private final NotificationService notificationService; 
    private final StudentProfessionalLinkService linkService; 
    private final UserRepository userRepository;

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public SchedulerService
    (
        InactivationSchedulingRepository inactivationSchedulingRepository,
        NotificationService notificationService,
        StudentProfessionalLinkService linkService,
        UserRepository userRepository
    )
    {
        this.userRepository = userRepository;
        this.linkService = linkService;
        this.notificationService = notificationService;
        this.inactivationSchedulingRepository = inactivationSchedulingRepository;
    }

    // ----------------------------------------------------
    // 3. MÉTODOS DE DOMÍNIO
    // ----------------------------------------------------

    /*
     * INFRASTRUCTURE / SCHEDULING
     */
    
    /**
     * Agendamento principal: Roda diariamente à 1:00 AM para processar regras.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void processScheduledTasks()
    {
        System.out.println("--- SCHEDULER: Processando regras baseadas em tempo ---");

        // RF01: Deletar solicitações pendentes após 1 mês.
        processExpiredPendingLinks();

        // RF01: Escalar solicitações antigas ao Administrador (1 semana).
        processAdminEscalation();
        
        // RF04/RF08: Excluir usuários desativados há mais de 1 mês.
        processDeactivationCleanup(); 

        System.out.println("--- SCHEDULER: Regras processadas. ---");
    }

    /**
     * Agenda a exclusão permanente de um usuário após 1 mês (RF04).
     */
    @Transactional
    public void scheduleDataDeletion(User user) {
        // Implementação conforme a lógica que já definimos
        if (inactivationSchedulingRepository.findById(user.getId()).isPresent())
        {
            System.out.println("Scheduler: Usuário " + user.getId() + " já tem exclusão agendada. Ignorando novo agendamento.");
            return;
        }

        LocalDateTime dateRequested = LocalDateTime.now();
        LocalDateTime dateScheduledDeletion = dateRequested.plusMonths(1); 
        
        InactivationScheduling scheduling = InactivationScheduling.builder()
            .user(user)
            .dateRequested(dateRequested)
            .dateScheduledDeletion(dateScheduledDeletion)
            .build();
            
        inactivationSchedulingRepository.save(scheduling);
        System.out.println("Scheduler: Exclusão de usuário " + user.getId() + " agendada para: " + dateScheduledDeletion);
    }
    
    // ----------------------------------------------------
    // 4. MÉTODOS AUXILIARES (Executados pelo processo agendado)
    // ----------------------------------------------------

    /**
     * Auxiliar: Implementa a exclusão de solicitações PENDING expiradas (RF01).
     */
    private void processExpiredPendingLinks()
    {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        
        List<StudentProfessionalLink> expiredLinks = 
            linkService.getLinksByStatusAndDateRequestBefore(EnumSet.of(LinkStatus.PENDING), oneMonthAgo);

        if (!expiredLinks.isEmpty())
        {
            linkService.deleteAll(expiredLinks);
            System.out.println("Scheduler: Deletados " + expiredLinks.size() + " links PENDING expirados (1 mês - RF01).");
        }
    }

    /**
     * Auxiliar: Implementa a escalada de solicitações antigas ao Administrador (RF01).
     */
    private void processAdminEscalation()
    {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        
        List<StudentProfessionalLink> escalationLinks = linkService.getLinksByStatusAndDateRequestBefore(EnumSet.of(LinkStatus.PENDING), oneWeekAgo);

        if (!escalationLinks.isEmpty())
        {
            System.out.println("Scheduler: " + escalationLinks.size() + " solicitações de alunos com mais de 1 semana. Notificando Admin.");
            notificationService.notifyAdminOfEscalation(escalationLinks.size());
        }
    }

    /**
     * Auxiliar: Implementa a exclusão de usuários desativados e agendados (RF04/RF08).
     */
    private void processDeactivationCleanup() {
        LocalDateTime now = LocalDateTime.now();
        
        List<InactivationScheduling> pendingDeletions = inactivationSchedulingRepository.findByDateScheduledDeletionBeforeAndDateActualDeletionIsNull(now);

        if (!pendingDeletions.isEmpty())
        {
            System.out.println("Scheduler: Encontrados " + pendingDeletions.size() + " usuários para exclusão permanente (RF04/RF08).");
            
            pendingDeletions.forEach(schedule -> {
                userRepository.delete(schedule.getUser()); 
                
                schedule.setDateActualDeletion(now);
                inactivationSchedulingRepository.save(schedule);
                
                System.out.println("Scheduler: Usuário " + schedule.getUser().getId() + " e dados removidos permanentemente.");
            });
        }
    }
}