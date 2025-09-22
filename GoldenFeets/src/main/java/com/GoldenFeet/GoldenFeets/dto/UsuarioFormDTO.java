package com.GoldenFeet.GoldenFeets.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

// Esta es una clase normal (POJO), no un record, por lo que es mutable.
public class UsuarioFormDTO {

    private String nombre;
    private String email;
    private String password;
    private String direccion;
    private LocalDate fecha_nacimiento;
    private String tipo_documento;
    private String numero_documento;
    private String telefono;
    private Set<Integer> rolesId;

    // Thymeleaf necesita un constructor sin argumentos
    public UsuarioFormDTO() {
    }

    // Getters y Setters (esto es lo que permite que th:field funcione)

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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDate getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    public void setFecha_nacimiento(LocalDate fecha_nacimiento) {
        this.fecha_nacimiento = fecha_nacimiento;
    }

    public String getTipo_documento() {
        return tipo_documento;
    }

    public void setTipo_documento(String tipo_documento) {
        this.tipo_documento = tipo_documento;
    }

    public String getNumero_documento() {
        return numero_documento;
    }

    public void setNumero_documento(String numero_documento) {
        this.numero_documento = numero_documento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Set<Integer> getRolesId() {
        return rolesId;
    }

    public void setRolesId(Set<Integer> rolesId) {
        this.rolesId = rolesId;
    }
}