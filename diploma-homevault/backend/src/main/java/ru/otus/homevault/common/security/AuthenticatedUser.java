package ru.otus.homevault.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.otus.homevault.users.model.Role;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.model.UserStatus;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record AuthenticatedUser(
        UUID id,
        String email,
        UserStatus status,
        Set<Role> roles,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    public static AuthenticatedUser from(User user) {
        Set<Role> roles = Set.copyOf(user.getRoles());
        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getStatus(),
                roles,
                roles.stream()
                        .map(role -> new SimpleGrantedAuthority(role.authority()))
                        .collect(Collectors.toUnmodifiableSet())
        );
    }

    public boolean isBlocked() {
        return status == UserStatus.BLOCKED;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isBlocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !isBlocked();
    }
}

