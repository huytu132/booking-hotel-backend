package com.example.identity_service.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.client.RestTemplate;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    // Filter chain cho OAuth2 Login (browser-based)
    @Bean
    @Order(1)
    public SecurityFilterChain oauth2FilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/auth/**", "/login/**", "/oauth2/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/login/**", "/oauth2/**").permitAll()
                )
//                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/auth/login")
//                        .successHandler(oAuth2AuthenticationSuccessHandler())
//                        .failureHandler(oAuth2AuthenticationFailureHandler())
//                )
//                // Enable session cho OAuth2
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
//                )
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    // Filter chain cho API endpoints (JWT-based)
    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/users", "/api/users/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/hotels/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/addons/**").permitAll()
//
//                        // chỉ ADMIN mới được thêm/sửa/xóa hotel
//                        .requestMatchers(HttpMethod.POST, "/api/hotels/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/hotels/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/hotels/**").hasRole("ADMIN")
//
//                        // chỉ ADMIN mới được thêm/sửa/xóa addon
//                        .requestMatchers(HttpMethod.POST, "/api/addons/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/addons/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/addons/**").hasRole("ADMIN")
//
//                        .requestMatchers(HttpMethod.POST, "/api/features/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/features/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/features/**").hasRole("ADMIN")

                                .requestMatchers("/api/**").permitAll()

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

//    // Filter chain mặc định cho các endpoint khác
//    @Bean
//    @Order(3)
//    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/favicon.ico").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .oauth2Login(Customizer.withDefaults())
//                .csrf(AbstractHttpConfigurer::disable);
//
//        return http.build();
//    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}