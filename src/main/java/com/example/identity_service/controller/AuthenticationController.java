package com.example.identity_service.controller;

import com.example.identity_service.dto.request.ApiResponse;
import com.example.identity_service.dto.request.AuthenticationRequest;
import com.example.identity_service.dto.request.IntrospectRequest;
import com.example.identity_service.dto.request.RefreshTokenRequest;
import com.example.identity_service.dto.response.AuthenticationResponse;
import com.example.identity_service.dto.response.IntrospectResponse;
import com.example.identity_service.dto.response.SessionResponse;
import com.example.identity_service.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        var result = authenticationService.authenticate(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);

        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh-token")
    public AuthenticationResponse refreshToken(@RequestBody RefreshTokenRequest request) {
        return authenticationService.refreshToken(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestBody RefreshTokenRequest request) {
        authenticationService.logout(request);
    }

    @GetMapping("/sessions")
    public List<SessionResponse> getSessions(@RequestParam String email) {
        return authenticationService.getSessions(email);
    }

    @PostMapping("/sessions/revoke")
    public void revokeSession(@RequestParam Long sessionId) {
        authenticationService.revokeSession(sessionId);
    }

    @PostMapping("/sessions/revoke-all")
    public void revokeAllSessions(@RequestParam String email) {
        authenticationService.revokeAllSessions(email);
    }
}
