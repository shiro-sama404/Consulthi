package com.ThimoteoConsultorias.Consulthi.model;

import com.ThimoteoConsultorias.Consulthi.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;

import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "app_user")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true, optional = true)
    private Student studentProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true, optional = true)
    private Professional professionalProfile;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    private String        username;
    private String        passwordHash;
    private String        email;
    private String        fullName;
    private String        phoneNumber;
    private int           failedLoginAttempts;
    private LocalDate     dateBirth;
    private LocalDateTime dateLogon;
    private LocalDateTime dateLastLogin;
    private boolean       active;

    public Set<Role> getAuthorities()
    {
        return roles
            .stream()
            .collect(Collectors.toSet());
    }
}