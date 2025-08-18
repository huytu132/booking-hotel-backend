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
                        .requestMatchers("/api/users", "/api/users/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hotels/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/hotels/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/hotels/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/hotels/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))
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

//    @Bean
//    public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
//        return (request, response, authentication) -> {
//            try {
//                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//                String email = oAuth2User.getAttribute("email");
//                String name = oAuth2User.getAttribute("name");
//                String sub = oAuth2User.getAttribute("sub");
//
//                log.info("OAuth2 Success - Email: {}, Name: {}, Sub: {}", email, name, sub);
//
//                // TODO: Uncomment khi đã có UserService
//                // User user = userService.processOAuthPostLogin(email, name);
//                // String accessToken = jwtUtil.generateToken(user);
//                // String refreshToken = jwtUtil.generateRefreshToken(user);
//
//                // Tạm thời redirect về trang thành công để test
//                response.sendRedirect("/auth1/oauth2/success?email=" + email);
//
//            } catch (Exception e) {
//                log.error("Error in OAuth2 success handler", e);
//                response.sendRedirect("/auth1/login?error=oauth_processing_failed");
//            }
//        };
//    }
//
//    @Bean
//    public AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
//        return (request, response, exception) -> {
//            log.error("OAuth2 authentication failed", exception);
//            try {
//                response.sendRedirect("/auth1/login?error=oauth_failed");
//            } catch (IOException e) {
//                log.error("Error redirecting after OAuth2 failure", e);
//            }
//        };
//    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}