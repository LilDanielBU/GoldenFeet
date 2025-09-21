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
    private final CustomAuthSuccessHandler customAuthSuccessHandler; // El nombre de tu clase es CustomAuthSuccessHandler

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/index", "/login", "/register", "/catalogo",
                                "/css/**", "/js/**", "/images/**", "/api/**" // Corregido /img/ a /images/ si es necesario
                        ).permitAll()

                        // --- RUTAS CORREGIDAS ---
                        // 1. La URL ahora es "/admin/**" para que coincida con tu AdminController.
                        // 2. Usamos hasRole("ADMIN") que es la convención (automáticamente busca "ROLE_ADMIN").
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/gerente-entregas/**").hasRole("GERENTEENTREGAS")
                        .requestMatchers("/distribuidor/**").hasRole("DISTRIBUIDOR")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customAuthSuccessHandler) // Tu manejador se llama CustomAuthSuccessHandler
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}