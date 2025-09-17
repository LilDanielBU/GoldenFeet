package com.GoldenFeet.GoldenFeets.config;

import jakarta.servlet.ServletException;
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
                                        Authentication authentication) throws IOException, ServletException {

        String redirectUrl = "/"; // URL por defecto para clientes

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_ADMIN")) {
                // CAMBIO: Redirige directamente a la página funcional
                redirectUrl = "/admin/usuarios";
                break;
            }
            else if (role.equals("ROLE_EMPLEADO")){
                redirectUrl = "/empleado";
                break;
            }
            else if(role.equals("ROLE_CLIENTE")){
                redirectUrl = "/cliente";
            }
            else if (role.equals("ROLE_DISTRIBUIDOR")) {
            redirectUrl = "/distribuidor";

            }
            else if (role.equals("ROLE_GERENTEENTREGAS")){
                redirectUrl ="/gerenteentregas";
;            }
            else if (role.equals("ROLE_GERENTEINVENTARIO")){
                redirectUrl ="/gerenteinventario";
            }

            // ... (puedes añadir otros roles aquí) ...
        }

        response.sendRedirect(redirectUrl);
    }
}