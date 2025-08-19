    package com.example.identity_service.controller;

    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
    import org.springframework.security.oauth2.client.registration.ClientRegistration;
    import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.RestController;

    import java.io.IOException;
    import java.util.Map;

    @RestController
    @RequestMapping("/auth/google")
    @RequiredArgsConstructor
    @Slf4j
    public class GoogleAuthController {

        private final OAuth2AuthorizedClientService clientService;
        private final ClientRegistrationRepository clientRegistrationRepository;

        // B1: FE gọi API này để lấy URL
        @GetMapping("/url")
        public Map<String, String> getGoogleLoginUrl(HttpServletRequest request) {
            ClientRegistration googleRegistration = clientRegistrationRepository.findByRegistrationId("google");

            String redirectUri = googleRegistration.getRedirectUri();
            String authUri = googleRegistration.getProviderDetails().getAuthorizationUri()
                    + "?client_id=" + googleRegistration.getClientId()
                    + "&redirect_uri=" + redirectUri
                    + "&response_type=code"
                    + "&scope=" + String.join(" ", googleRegistration.getScopes());

            return Map.of("url", authUri);
        }

        // B2: Google redirect về đây (redirect-uri trong application.properties)
        @GetMapping("/callback")
        public void handleGoogleCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
            // Redirect sang FE Vite route, kèm code
            log.info(code);
            response.sendRedirect("http://localhost:5173/auth/google/callback?code=" + code);
        }

    }

