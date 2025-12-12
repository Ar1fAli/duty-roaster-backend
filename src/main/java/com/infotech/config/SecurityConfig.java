package com.infotech.config;

import java.util.List;

import com.infotech.service.CustomUserDetailsService;
import com.infotech.service.JwtFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomUserDetailsService customUserDetailsService;

  // @Bean
  // public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter
  // jwtFilter) throws Exception {
  //
  // http.csrf(csrf -> csrf.disable())
  // .authorizeHttpRequests(auth -> auth
  // .requestMatchers("/auth/**", "/api/categories/register/**",
  // "/api/officer/register/**",
  // "api/assignments/**", "/usr/reg")
  // .permitAll()
  // // .requestMatchers("/api/**", "/auth/**").permitAll()
  // .anyRequest().authenticated())
  // .authenticationProvider(authenticationProvider())
  // .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
  //
  // return http.build();
  // }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {

    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/auth/**",
                "/api/categories/register/**",
                "/api/officer/register/**",
                "/api/assignments/**",
                "/api/officer/unique-ranks",
                "/usr/reg")
            .permitAll()
            .anyRequest().authenticated())
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    // allow all IPs / all domains
    config.setAllowedOriginPatterns(List.of("*")); // e.g. http://192.168.29.46:3000, http://localhost:5173, etc.
    // or: config.addAllowedOriginPattern("*");

    // allow all headers (Authorization, Content-Type, etc.)
    config.setAllowedHeaders(List.of("*"));

    // allow all HTTP methods
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

    // allow credentials if you need cookies / Authorization headers across origins
    config.setAllowCredentials(true);

    // expose headers if you want frontend to read them
    config.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(customUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
