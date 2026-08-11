package com.n2n.booking.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.n2n.booking.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Getter
public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String email;
    private String fullName;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().name()));

        if (user.getRole() == com.n2n.booking.enums.Role.ROLE_SUPERADMIN) {
            // Superadmin gets everything, we could just rely on hasAuthority('ROLE_SUPERADMIN') 
            // in the controllers instead of populating all modules.
        } else if (user.getAdminRole() != null && user.getAdminRole().getPermissions() != null) {
            for (com.n2n.booking.entity.AdminPermission perm : user.getAdminRole().getPermissions()) {
                String mod = perm.getModuleName();
                if (perm.isCanCreate()) authorities.add(new SimpleGrantedAuthority(mod + "_CREATE"));
                if (perm.isCanRead()) authorities.add(new SimpleGrantedAuthority(mod + "_READ"));
                if (perm.isCanUpdate()) authorities.add(new SimpleGrantedAuthority(mod + "_UPDATE"));
                if (perm.isCanDelete()) authorities.add(new SimpleGrantedAuthority(mod + "_DELETE"));
            }
        }

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPassword(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
