package com.example.identity_service.configuration;

import com.example.identity_service.entity.Role;
import com.example.identity_service.entity.User;
import com.example.identity_service.repository.RoleRepository;
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
    private final RoleRepository roleRepository;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository){
        return args -> {
            // 👉 Tạo role mặc định nếu chưa có
            createRoleIfNotExists("ADMIN", "Administrator role");
            createRoleIfNotExists("USER", "User role");
            createRoleIfNotExists("STAFF", "Staff role");

            // 👉 Tạo admin user mặc định nếu chưa có
            if (userRepository.findByEmail("admin@gmail.com").isEmpty()){
                var adminRole = roleRepository.findById("ADMIN")
                        .orElseThrow(() -> new RuntimeException("Admin role not found"));

                User user = User.builder()
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("12345678"))
                        .firstName("Admin")
                        .lastName("System")
                        .isVerified(true)
                        .build();

                user.getRoles().add(adminRole);
                //adminRole.getUsers().add(user);

                user.setCreateAt(java.time.LocalDateTime.now());
                user.setCreateBy("SYSTEM");

                userRepository.save(user);
                log.warn("✅ Admin user created: admin@gmail.com / 12345678");
            }
        };
    }

    private void createRoleIfNotExists(String roleName, String description) {
        if (!roleRepository.existsById(roleName)) {
            Role role = new Role(roleName, description);
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        }
    }
}
