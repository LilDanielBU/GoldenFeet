package com.GoldenFeet.GoldenFeets.dto;

import java.util.Set;

/**
 * DTO para actualización de usuarios desde el panel de administración.
 * Convertido a una clase normal (POJO) para ser mutable.
 */
public class AdminUsuarioUpdateDTO {

    private Integer idUsuario;
    private String nombre;
    private String email;
    private String password; // Para la nueva contraseña opcional
    private boolean activo;
    private Set<Integer> rolesId; // Usamos Set para que coincida con la lógica del controller

    // Constructor vacío requerido
    public AdminUsuarioUpdateDTO() {
    }

    // Getters y Setters

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Set<Integer> getRolesId() {
        return rolesId;
    }

    public void setRolesId(Set<Integer> rolesId) {
        this.rolesId = rolesId;
    }
}