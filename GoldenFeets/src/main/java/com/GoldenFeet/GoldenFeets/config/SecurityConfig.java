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
    private final CustomAuthSuccessHandler customAuthSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // --- Rutas Públicas ---
                        .requestMatchers(
                                "/", "/index", "/login", "/register", "/catalogo",
                                "/css/**", "/js/**", "/img/**", "/api/**"
                        ).permitAll()

                        // --- Rutas Protegidas por Rol ---
                        .requestMatchers("/administrador/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/gerente-entregas/**").hasAuthority("ROLE_GERENTEENTREGAS")
                        .requestMatchers("/distribuidor/**").hasAuthority("ROLE_DISTRIBUIDOR")

                        // Cualquier otra ruta requiere estar autenticado
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customAuthSuccessHandler)
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