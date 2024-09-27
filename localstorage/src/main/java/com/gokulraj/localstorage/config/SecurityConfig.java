package com.gokulraj.localstorage.config;

import  com.gokulraj.localstorage.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // AuthenticationManager bean configuration
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build(); // Build and return the AuthenticationManager
    }

    // Configure security rules using SecurityFilterChain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/register").permitAll()
                        .requestMatchers("/files/upload").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/files/delete/**").hasRole("ADMIN")
                        .requestMatchers("/files/**").hasAnyRole("ADMIN", "MANAGER", "DEVELOPER")
                        .requestMatchers("/S3/files/upload").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/S3/files/delete/**").hasRole("ADMIN")
                        .requestMatchers("/S3/files/**").hasAnyRole("ADMIN", "MANAGER", "DEVELOPER")
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(response.SC_UNAUTHORIZED, authException.getMessage());
                }))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    // Define a password encoder bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
