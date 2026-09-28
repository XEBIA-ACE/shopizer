package com.ecommerce.core.controller;

import com.ecommerce.core.config.AppProperties;
import com.ecommerce.core.domain.dto.request.RegisterRequest;
import com.ecommerce.core.domain.dto.request.ResendConfirmationRequest;
import com.ecommerce.core.domain.dto.response.MessageResponse;
import com.ecommerce.core.domain.dto.response.SessionResponse;
import com.ecommerce.core.service.EmailVerificationService;
import com.ecommerce.core.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class AuthController {

    static final String REGISTRATION_ACCEPTED =
            "Registration received. A confirmation link has been sent to your email address.";
    static final String RESEND_ACCEPTED = "A new confirmation link has been sent to your email address.";

    private final RegistrationService registrationService;
    private final EmailVerificationService emailVerificationService;
    private final AppProperties properties;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        registrationService.register(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MessageResponse(REGISTRATION_ACCEPTED));
    }

    @PostMapping("/register/resend")
    public ResponseEntity<MessageResponse> resend(@Valid @RequestBody ResendConfirmationRequest request) {
        registrationService.resendConfirmation(request.email());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MessageResponse(RESEND_ACCEPTED));
    }

    @PostMapping("/verify/email")
    public ResponseEntity<SessionResponse> verifyEmail(@RequestParam("token") String token) {
        SessionResponse session = emailVerificationService.verify(token);
        return ResponseEntity.ok()
                .header(HttpHeaders.LOCATION, properties.session().redirectLocation())
                .body(session);
    }
}
