package com.ThimoteoConsultorias.Consulthi.repository;

import com.ThimoteoConsultorias.Consulthi.enums.LinkStatus;
import com.ThimoteoConsultorias.Consulthi.model.Professional;
import com.ThimoteoConsultorias.Consulthi.model.Student;
import com.ThimoteoConsultorias.Consulthi.model.StudentProfessionalLink;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface StudentProfessionalLinkRepository extends JpaRepository<StudentProfessionalLink, Long>
{
    List<StudentProfessionalLink> findByProfessional(Professional professional);
    List<StudentProfessionalLink> findByStudent(Student student);
    List<StudentProfessionalLink> findByProfessionalAndStatus(Professional professional, Collection<LinkStatus> status);
    List<StudentProfessionalLink> findByStudentAndStatus(Student student, Collection<LinkStatus> status);
    List<StudentProfessionalLink> findByStatusAndDateRequestBefore(Collection<LinkStatus> status, LocalDateTime dateRequest);
    
    long countByStudentAndStatus(Student student, Collection<LinkStatus> status);
    long countByStudentUserIdAndProfessionalUserIdAndStatus(Long studentUserId, Long professionalUserId, Collection<LinkStatus> status);
}