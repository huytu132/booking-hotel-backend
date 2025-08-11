package com.example.identity_service.configuration;

import com.example.identity_service.entity.User;
import com.example.identity_service.enums.EnumRole;
import com.example.identity_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;

//    @Bean
//    ApplicationRunner applicationRunner(UserRepository userRepository){
//        return args -> {
//            if (userRepository.findByUsername("admin").isEmpty()){
//                Set<String> roles = new HashSet<>();
//                roles.add(EnumRole.ADMIN.name());
//                User user = User.builder()
//                        .email("admin")
//                        .password(passwordEncoder.encode("12345678"))
//                        .roles(roles)
//                        .build();
//                userRepository.save(user);
//                log.warn("admin user has been created with default password 12345678");
//            }
//        };
//    }
}
