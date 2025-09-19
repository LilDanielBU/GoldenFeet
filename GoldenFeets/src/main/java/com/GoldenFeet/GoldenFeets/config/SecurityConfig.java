package com.GoldenFeet.GoldenFeets.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomAuthSuccessHandler customAuthSuccessHandler; // Asumo que tienes esta clase

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitamos CSRF porque usaremos JWT, que es inmune a este ataque.
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // --- Páginas Públicas y de Autenticación ---
                                "/",
                                "/index",
                                "/login",
                                "/register",
                                "/catalogo",

                                // --- Recursos Estáticos (CSS, JS, Imágenes) ---
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/img/**", // Por si usas esta carpeta también

                                // --- APIs Públicas (necesarias para el frontend) ---
                                "/api/auth/**",      // Para manejar el login/registro desde una API
                                "/api/productos/**", // Para que cualquiera pueda ver los productos
                                "/api/carrito/**"    // Para que cualquiera pueda añadir al carrito (se gestiona por sesión)
                        ).permitAll() // Todas las rutas anteriores son públicas.

                        // --- Rutas Protegidas ---
                        .requestMatchers("/administrador/**").hasAuthority("ROLE_ADMIN") // Solo para admins

                        // --- Cualquier otra ruta requiere estar autenticado ---
                        .anyRequest().authenticated()
                )

                // --- Gestión de Sesión ---
                // Usamos IF_REQUIRED para que se cree una sesión para el carrito y el formLogin,
                // pero no para las llamadas a la API con JWT.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // --- Configuración del Formulario de Login ---
                .formLogin(form -> form
                        .loginPage("/login")              // Nuestra página de login personalizada
                        .successHandler(customAuthSuccessHandler) // Qué hacer después de un login exitoso
                        .permitAll()                      // Todos pueden ver el formulario de login
                )

                // --- Configuración del Logout ---
                .logout(logout -> logout
                        .logoutUrl("/logout")                   // La URL para cerrar sesión
                        .logoutSuccessUrl("/login?logout")  // A dónde ir después de cerrar sesión
                        .permitAll()
                );

        return http.build();
    }
}