package com.mynote.config;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/login",
                        "/images/**",
                        "/line-awesome/**"
                ).permitAll()
        );

        http.with(VaadinSecurityConfigurer.vaadin(), vaadin -> vaadin
                .loginView("/login")
                .anyRequest(authorized -> authorized.authenticated())
        );

        return http.build();
    }
}
