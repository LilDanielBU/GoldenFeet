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

        String redirectUrl = "/";

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();

            switch (role) {
                case "ROLE_ADMIN":
                    redirectUrl = "/admin/usuarios";
                    break;
                case "ROLE_CLIENTE":
                    redirectUrl = "/";
                    break;

                case "ROLE_GERENTEENTREGAS":
                    redirectUrl = "/gerente-entregas/dashboard";
                    break;
                case "ROLE_DISTRIBUIDOR":
                    redirectUrl = "/distribuidor/dashboard";
                    break;
                // ... otros roles
            }
        }

        response.sendRedirect(redirectUrl);
    }
}