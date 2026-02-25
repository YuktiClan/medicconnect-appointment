package com.medicconnect.medicconnect_appointment.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public final class AuthenticatedUser implements UserDetails {

    @Getter
    private final Long userId;

    @Getter
    private final Long organizationId;

    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    private final boolean enabled;
    private final boolean accountNonLocked;

    public AuthenticatedUser(
            Long userId,
            Long organizationId,
            String username,
            Collection<? extends GrantedAuthority> authorities,
            boolean enabled,
            boolean accountNonLocked
    ) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.username = username;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // JWT-based auth
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // enforced at issuance
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
