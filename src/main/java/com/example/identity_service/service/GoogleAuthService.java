package com.example.identity_service.service;

import com.example.identity_service.dto.response.AuthenticationResponse;
import com.example.identity_service.entity.User;
import com.example.identity_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public AuthenticationResponse loginWithGoogle(String code) {
        String tokenUri = "https://oauth2.googleapis.com/token";
        Map<String, String> tokenRequest = Map.of(
                "code", code,
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "grant_type", "authorization_code"
        );

        Map<String, Object> tokenResponse = restTemplate.postForObject(tokenUri, tokenRequest, Map.class);
        String accessToken = (String) tokenResponse.get("access_token");

        // 2️⃣ Lấy user info
        String userInfoUri = "https://www.googleapis.com/oauth2/v3/userinfo";
        Map<String, Object> userInfo = restTemplate.getForObject(
                userInfoUri + "?access_token=" + accessToken, Map.class);

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String sub = (String) userInfo.get("sub");

        String firstName;
        String lastName;

        if (name != null && !name.isBlank()) {
            String[] parts = name.trim().split("\\s+"); // tách theo space
            firstName = parts[parts.length - 1]; // lấy từ cuối cùng
            if (parts.length > 1) {
                lastName = String.join(" ", Arrays.copyOf(parts, parts.length - 1)); // ghép phần trước
            } else {
                lastName = "";
            }
        } else {
            lastName = "";
            firstName = "";
        }

        log.info(userInfo.toString());

        // 3️⃣ Check DB
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .password("") // OAuth user ko có password
                            .firstName(firstName)
                            .lastName(lastName)
                            .provider("GOOGLE")
                            .providerId(sub)
                            .build();
                    return userRepository.save(newUser);
                });

        // 4️⃣ Generate JWT
        String jwt = authenticationService.generateToken(user);
        String refresh = authenticationService.generateRefreshToken(user).getToken();

        return AuthenticationResponse.builder()
                .token(jwt)
                .refreshToken(refresh)
                .authenticated(true)
                .build();
    }
}

