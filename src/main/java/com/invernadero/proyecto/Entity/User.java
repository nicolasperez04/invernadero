package com.invernadero.proyecto.Entity;

import com.invernadero.proyecto.Entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entidad que representa un usuario del sistema.
 * Implementa UserDetails para integración con Spring Security.
 */
@Schema(description = "Usuario del sistema de gestión de invernaderos")
@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del usuario", example = "1")
    private Long id;

    @NotNull
    @Column(nullable = false)
    @Schema(description = "Nombre del usuario", example = "Carlos")
    private String name;

    @NotNull
    @Column(nullable = false)
    @Schema(description = "Apellido del usuario", example = "García")
    private String lastName;

    @NotNull
    @Column(nullable = false, unique = true)
    @Schema(description = "Correo electrónico (único)", example = "carlos.garcia@ejemplo.com")
    private String email;

    @NotNull
    @Column(nullable = false)
    @Schema(description = "Contraseña cifrada del usuario")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Rol del usuario en el sistema", example = "ADMIN")
    private Role role;

    @Schema(description = "Indica si el usuario está activo", example = "true")
    private boolean active = true;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return password;
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
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
