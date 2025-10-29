package com.ThimoteoConsultorias.Consulthi.config;

import com.ThimoteoConsultorias.Consulthi.model.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class AppUser implements UserDetails
{
    private final User user;

    public AppUser(User user)
    {
        this.user = user;
    }

    public Long getId()
    {
        return user.getId();
    }

    @Override
    public String getUsername()
    {
        return user.getUsername();
    }

    @Override
    public String getPassword()
    {
        return user.getPasswordHash();
    }

    @Override
    public boolean isEnabled()
    {
        return user.isActive();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return user.getAuthorities();
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }
}