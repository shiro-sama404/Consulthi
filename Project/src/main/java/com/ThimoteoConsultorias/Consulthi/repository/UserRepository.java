package com.ThimoteoConsultorias.Consulthi.repository;

import com.ThimoteoConsultorias.Consulthi.enums.Role;
import com.ThimoteoConsultorias.Consulthi.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
    public Optional<User> findByUsername(String username);
    public Optional<User> findByEmail(String email);
    public List<User> findByActiveFalseAndRolesIn(Collection<Role> roles);
    public List<User> findByRolesIn(Collection<Role> roles);

    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.studentProfile " +
           "LEFT JOIN FETCH u.professionalProfile prof " +
           "LEFT JOIN FETCH prof.expertiseAreas " +
           "LEFT JOIN FETCH u.roles")
    public List<User> findAllWithProfilesAndRoles();
}