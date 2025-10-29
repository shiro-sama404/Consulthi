package com.ThimoteoConsultorias.Consulthi.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority
{
    STUDENT,
    COACH,
    PSYCHOLOGIST,
    NUTRITIONIST,
    ADMINISTRATOR;

    @Override
    public String getAuthority()
    {
        return name();
    }

    public boolean isProfessionalRole()
    {
        return this == COACH || this == PSYCHOLOGIST || this == NUTRITIONIST;
    }
}