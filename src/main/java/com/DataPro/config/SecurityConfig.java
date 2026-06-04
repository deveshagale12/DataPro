package com.DataPro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // 1. Open public routes
                .requestMatchers("/api/users/**").permitAll()
                
                // 2. Explicitly restrict the admin dashboard to ADMIN roles only
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 3. Catch-all for any other secure resources
                .anyRequest().authenticated()
            );
        return http.build();
    }
}