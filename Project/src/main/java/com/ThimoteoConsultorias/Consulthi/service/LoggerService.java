package com.ThimoteoConsultorias.Consulthi.service;

import com.ThimoteoConsultorias.Consulthi.model.StudentProfessionalLink;
import com.ThimoteoConsultorias.Consulthi.model.User; // Para acesso a User.getFullName()

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoggerService
{
    /**
     * Registra em detalhes os links que foram permanentemente deletados (por tempo de expiração).
     * @param deletedLinks A lista de links que foram excluídos do sistema.
     */
    public void logExpiredLinksDeletion(List<StudentProfessionalLink> deletedLinks)
    {
        if (!deletedLinks.isEmpty())
        {
            // Constrói uma string com os IDs dos links removidos para o log
            String ids = deletedLinks.stream()
                .map(link -> String.valueOf(link.getId()))
                .collect(Collectors.joining(", "));
                
            System.out.println("[AUDIT LOG] DELETION: " + deletedLinks.size() + " links PENDING removidos por expiração (1 mês). IDs: [" + ids + "]");
        }
    }
    
    /**
     * Registra em detalhes os links que foram escalados para o Administrador (com mais de 1 semana).
     * @param escalatedLinks A lista de links que dispararam o alerta de escalada.
     */
    public void logEscalatedLinks(List<StudentProfessionalLink> escalatedLinks)
    {
        if (!escalatedLinks.isEmpty())
        {
            // Constrói uma string detalhada para auditoria (ID do Link e Nome do Aluno)
            String details = escalatedLinks.stream()
                .map(link -> "Link ID: " + link.getId() + 
                             ", Aluno: " + link.getStudent().getUser().getFullName())
                .collect(Collectors.joining("; "));
                
            System.out.println("[AUDIT LOG] ESCALATION: " + escalatedLinks.size() + 
                               " links de alunos escalados (> 1 semana). Detalhes: " + details);
        }
    }
    
    /**
     * Registra a exclusão permanente de um usuário pelo Administrador ou Scheduler (RF08 / RF04).
     * @param user O usuário que foi permanentemente deletado.
     */
    public void logPermanentUserDeletion(User user)
    {
        System.out.println("[AUDIT LOG] DELETION: Usuário ID " + user.getId() + 
                           " (" + user.getUsername() + ") foi permanentemente removido do sistema.");
    }
}