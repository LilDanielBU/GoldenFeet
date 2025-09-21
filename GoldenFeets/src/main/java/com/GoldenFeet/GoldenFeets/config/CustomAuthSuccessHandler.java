package com.GoldenFeet.GoldenFeets.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {


        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isGerente = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTEENTREGAS"));

        boolean isDistribuidor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DISTRIBUIDOR"));

        boolean isCliente = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

        // Lógica de redirección con prioridad
        if (isAdmin) {
            response.sendRedirect("/admin/usuarios");
        } else if (isGerente) {
            response.sendRedirect("/gerente-entregas/dashboard");
        } else if (isDistribuidor) {
            response.sendRedirect("/distribuidor/dashboard");
        } else if (isCliente) {
            response.sendRedirect("/");
        } else {
            // Un fallback por si el usuario no tiene ninguno de los roles esperados
            response.sendRedirect("/login?error");
        }
    }
}