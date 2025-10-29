package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.enums.LinkStatus;
import com.ThimoteoConsultorias.Consulthi.exception.ResourceNotFoundException;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.model.Student;
import com.ThimoteoConsultorias.Consulthi.model.StudentProfessionalLink;
import com.ThimoteoConsultorias.Consulthi.repository.StudentProfessionalLinkRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StudentProfessionalLinkService
{

    private final StudentProfessionalLinkRepository linkRepository;
    private final StudentService studentService;
    private final ProfessionalService professionalService;

    public StudentProfessionalLinkService
    (
        StudentProfessionalLinkRepository linkRepository,
        StudentService studentService,
        ProfessionalService professionalService
    )
    {
        this.linkRepository = linkRepository;
        this.studentService = studentService;
        this.professionalService = professionalService;
    }

    @Transactional
    public void createPendingLinks(Student student, List<Long> professionalIds)
    {
        for (Long professionalId : professionalIds)
        {
            Optional<Professional> professionalOpt = professionalService.getProfessionalById(professionalId);
            
            if (professionalOpt.isPresent())
            {
                Professional professional = professionalOpt.get();
                
                // Cria o link inicial com status PENDING
                StudentProfessionalLink link = StudentProfessionalLink.builder()
                    .student(student)
                    .professional(professional)
                    .status(LinkStatus.PENDING)
                    .dateRequest(LocalDateTime.now())
                    .build();
                
                linkRepository.save(link);
                
                // TODO
                // Notificar o profissional sobre a solicitação de cadastro
            } else {
                // Falta tratar caso onde o profissional não é encontrado (notificar o aluno?)
            }
        }
    }

    @Transactional
    public StudentProfessionalLink acceptLink(Long linkId, Long professionalId)
    {
        StudentProfessionalLink link = linkRepository.findById(linkId)
            .orElseThrow(() -> new ResourceNotFoundException("Vínculo não encontrado. id: " + linkId));
        
        // Validação de Segurança para apenas o profissional envolvido pode aceitar
        if (!link.getProfessional().getId().equals(professionalId))
            throw new SecurityException("Ação não autorizada. Este profissional não está envolvido neste vínculo.");
        
        if (link.getStatus() == LinkStatus.PENDING)
        {
            link.setStatus(LinkStatus.ACCEPTED);
            // TODO
            // O aluno deve ser ativado/notificado após a aceitação do primeiro profissional
            // (implementar lógica de ativação do User no UserService?)
            
            return linkRepository.save(link);
        } else
            throw new IllegalStateException("O vínculo não está em status pendente para ser aceito.");
    }
    
    @Transactional
    public void removeLink(Long linkId, Long professionalId)
    {
        
        StudentProfessionalLink link = linkRepository.findById(linkId)
            .orElseThrow(() -> new ResourceNotFoundException("Vínculo não encontrado. id: " + linkId));
            
        // Validação de Segurança para apenas o profissional envolvido pode remover
        if (!link.getProfessional().getId().equals(professionalId))
            throw new SecurityException("Ação não autorizada. Este profissional não pode remover este vínculo.");
        
        // Remove o link e o acesso ao conteúdo do profissional
        linkRepository.delete(link);
        
        // TODO
        // O usuário profissional deve ser capaz de especificar quais 
        // conteúdos devem ser removidos.
        // Especificar lógica em um ContentService?.
    }

    // TODO
    //Método para lidar com a regra de 1 mês para solicitações expiradas.
    //Método para o Admin lidar com solicitações pendentes a 1+ semanas.
}