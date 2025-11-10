package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.enums.LinkStatus;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.model.Student;
import com.ThimoteoConsultorias.Consulthi.model.StudentProfessionalLink;
import com.ThimoteoConsultorias.Consulthi.model.User;
import com.ThimoteoConsultorias.Consulthi.repository.StudentProfessionalLinkRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@Lazy
@Service
public class StudentProfessionalLinkService
{
    // ----------------------------------------------------
    // 1. DEPENDÊNCIAS
    // ----------------------------------------------------
    private final StudentProfessionalLinkRepository linkRepository;
    private final LoggerService loggerService;
    private final NotificationService notificationService;
    private final ProfessionalService professionalService;

    private UserService userService;

    // ----------------------------------------------------
    // 2. CONSTRUTOR
    // ----------------------------------------------------
    public StudentProfessionalLinkService
    (
        StudentProfessionalLinkRepository linkRepository,
        LoggerService loggerService,
        NotificationService notificationService,
        ProfessionalService professionalService
    )
    {
        this.linkRepository = linkRepository;
        this.loggerService = loggerService;
        this.notificationService = notificationService;
        this.professionalService = professionalService;
    }
    
    @Autowired
    @Lazy
    public void setUserService(UserService userService)
    {
        this.userService = userService;
    }

    // ----------------------------------------------------
    // 3. MÉTODOS DE DOMÍNIO
    // ----------------------------------------------------

    /* * CREATE
     */

    /**
     * Cria links PENDING para cada profissional selecionado pelo aluno durante o cadastro (RF01).
     */
    @Transactional
    public void createPendingLinks(Student student, List<Long> professionalIds)
    {
        for (Long professionalId : professionalIds)
        {
            Professional professional = professionalService.getProfessionalById(professionalId);
            
            StudentProfessionalLink link = StudentProfessionalLink.builder()
                .student(student)
                .professional(professional)
                .status(LinkStatus.PENDING)
                .dateRequest(LocalDateTime.now())
                .build();
            
            linkRepository.save(link);
            
            notificationService.notifyProfessionalOfNewStudent(professionalId, student.getUser().getId());
        }
    }

    /*
     * READ
     */

    /**
     * Retorna um link pelo ID.
     * @throws ResourceNotFoundException se não for encontrado.
     */
    public StudentProfessionalLink getLinksById(long linkId) throws ResourceNotFoundException
    {
        return linkRepository.findById(linkId)
            .orElseThrow(() -> new ResourceNotFoundException("Link de id '" + linkId +"' não encontrado."));
    }
    
    // Métodos de listagem
    public List<StudentProfessionalLink> getLinksByProfessional(Professional professional)
    { return linkRepository.findByProfessional(professional); }
    public List<StudentProfessionalLink> getLinksByStudent(Student student)
    { return linkRepository.findByStudent(student); }
    public List<StudentProfessionalLink> getLinksByProfessionalAndStatusIn(Professional professional, Collection<LinkStatus> status)
    { return linkRepository.findByProfessionalAndStatusIn(professional, status); }
    public List<StudentProfessionalLink> getLinksByStudentAndStatusIn(Student student, Collection<LinkStatus> status)
    { return linkRepository.findByStudentAndStatusIn(student, status); }
    public List<StudentProfessionalLink> getLinksByStatusAndDateRequestBefore(Collection<LinkStatus> status, LocalDateTime dateTime)
    { return linkRepository.findByStatusAndDateRequestBefore(status, dateTime); }
    public long countByStudentAndStatusIn(Student student, Collection<LinkStatus> status)
    { return linkRepository.countByStudentAndStatusIn(student, status); }
    public long countByStudentUserIdAndProfessionalUserIdAndStatusIn(Long studentUserId, Long professionalUserId, Collection<LinkStatus> status)
    { return linkRepository.countByStudentUserIdAndProfessionalUserIdAndStatusIn(studentUserId, professionalUserId, status); }

    /**
     * Implementa a verificação de vínculo ativo (ACCEPTED) para acesso ao conteúdo (RF06).
     */
    public boolean isActiveLink(Long studentUserId, Long professionalUserId)
    {
        long count = countByStudentUserIdAndProfessionalUserIdAndStatusIn(
            studentUserId,
            professionalUserId,
            EnumSet.of(LinkStatus.ACCEPTED)
        );
        return count > 0;
    }

    /*
     * UPDATE
     */

    /**
     * Implementa a aceitação da solicitação de um aluno por um profissional.
     * RF01: Ativa o usuário se for o primeiro link aceito.
     * @throws IllegalStateException se o status não for PENDING.
     */
    @Transactional
    public StudentProfessionalLink acceptLink(Long linkId, Long professionalId)
    {
        StudentProfessionalLink link = getLinksById(linkId);
        
        if (!link.getProfessional().getUser().getId().equals(professionalId))
            throw new SecurityException("Ação não autorizada.");
        
        if (link.getStatus() == LinkStatus.PENDING)
        {
            link.setStatus(LinkStatus.ACCEPTED);
            StudentProfessionalLink savedLink = linkRepository.save(link);

            long acceptedLinksCount = linkRepository.countByStudentAndStatusIn(link.getStudent(), EnumSet.of(LinkStatus.ACCEPTED));
            
            // Ativa o usuário se este foi o primeiro link aceito
            if (acceptedLinksCount == 1) 
                userService.activateUser(link.getStudent().getUser().getId());
            
            notificationService.notifyProfessionalOfLinkAcceptance(professionalId, link.getStudent().getUser().getId());

            return savedLink;
        } else
            throw new IllegalStateException("O vínculo não está em status pendente para ser aceito.");
    }
    
    /*
     * DELETE
     */

    /**
     * Deleta um único link.
     */
    @Transactional
    public void delete(StudentProfessionalLink link)
    {
        linkRepository.delete(link);
    }

    /**
     * Deleta uma lista de links (usado pelo Scheduler).
     */
    @Transactional
    public void deleteAll(List<StudentProfessionalLink> links)
    {
        linkRepository.deleteAll(links);
    }
    
    /**
     * Implementa a remoção do vínculo (RF05), permitindo que Aluno ou Profissional solicitem.
     */
    @Transactional
    public void removeLink(long linkId, long requesterUserId)
    {
        StudentProfessionalLink link = getLinksById(linkId);
        User requesterUser = userService.getUserById(requesterUserId); // Reutiliza a busca por ID

        Long professionalId = link.getProfessional().getUser().getId();
        Long studentId = link.getStudent().getUser().getId();

        boolean isAuthorized = requesterUser.getId().equals(professionalId) || requesterUser.getId().equals(studentId);
        
        if (!isAuthorized)
            throw new SecurityException("Ação não autorizada. O usuário logado não está associado a este vínculo.");

        List<User> receavers;
        if (requesterUser.getId().equals(professionalId))
            receavers = List.of(link.getStudent().getUser());
        else
            receavers = List.of(link.getProfessional().getUser());

        notificationService.notifyLinkTermination(requesterUser, receavers);

        linkRepository.delete(link);
    }
    
    // Métodos de agendamento

    /**
     * Implementa o Método para lidar com a regra de 1 mês para solicitações expiradas.
     * Chamado pelo SchedulerService.
     * RF01: Solicitações pendentes por 1 mês são automaticamente deletadas.
     * @return O número de links deletados.
     */
    @Transactional
    public void deleteExpiredPendingLinks()
    {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        
        List<StudentProfessionalLink> expiredLinks = getLinksByStatusAndDateRequestBefore(EnumSet.of(LinkStatus.PENDING), oneMonthAgo);

        if (expiredLinks.isEmpty())
        {
            loggerService.logExpiredLinksDeletion(expiredLinks); 
            deleteAll(expiredLinks);
        }
    }

    /**
     * Implementa o Método para o Admin lidar com solicitações pendentes a 1+ semanas.
     * Chamado pelo SchedulerService.
     * RF01: Repassa solicitações ao Administrador e notifica.
     * @return O número de links escalados.
     */
    @Transactional
    public void escalateOldPendingLinksToAdmin()
    {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        
        List<StudentProfessionalLink> escalationLinks = linkRepository
            .findByStatusAndDateRequestBefore(EnumSet.of(LinkStatus.PENDING), oneWeekAgo);

        if (!escalationLinks.isEmpty())
        {
            loggerService.logEscalatedLinks(escalationLinks);
            notificationService.notifyAdminOfEscalation(escalationLinks.size());
        }
    }
}