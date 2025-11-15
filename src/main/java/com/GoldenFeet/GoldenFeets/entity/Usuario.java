package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Setter
@ToString(exclude = "roles")
@EqualsAndHashCode(exclude = "roles")
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "departamento")
    private String departamento;

    @Column(name = "ciudad")
    private String ciudad;

    @Column(length = 100)
    private String localidad;

    @Column(length = 100)
    private String barrio;

    @Column(name = "informacion_adicional")
    private String informacionAdicional;

    @Column(name = "apellido")
    private String apellido;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String telefono;
    private String direccion;
    private LocalDate fechaNacimiento;
    private boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    private Set<Rol> roles;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.email;
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
        return this.activo;
    }
}