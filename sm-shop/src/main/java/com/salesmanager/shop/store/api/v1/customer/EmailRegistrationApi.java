package com.salesmanager.shop.store.api.v1.customer;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.store.security.JWTTokenUtil;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/v1/auth")
public class EmailRegistrationApi {

    @Inject private EmailRegistrationService registration;
    @Inject private JWTTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> input,
            @ApiIgnore MerchantStore store, @ApiIgnore Language language) throws Exception {
        try {
            registration.register(input.get("email"), input.get("password"), store, language);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use", ex);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Collections.singletonMap("message", "Confirmation link sent to your email"));
    }

    @PostMapping("/register/resend")
    public ResponseEntity<Map<String, String>> resend(@RequestBody Map<String, String> input,
            @ApiIgnore MerchantStore store) throws Exception {
        registration.resend(input.get("email"), store);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Collections.singletonMap("message", "Confirmation link sent to your email"));
    }

    @PostMapping("/verify/email")
    public ResponseEntity<Map<String, String>> verify(@RequestParam("token") String token,
            HttpServletRequest request) throws Exception {
        String jwt = registration.verify(token);
        long seconds = Math.max(0, (jwtTokenUtil.getExpirationDateFromToken(jwt).getTime()
                - new Date().getTime()) / 1000);
        ResponseCookie cookie = ResponseCookie.from("customer_session", jwt)
                .httpOnly(true).secure(request.isSecure()).sameSite("Lax")
                .path("/").maxAge(Duration.ofSeconds(seconds)).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Collections.singletonMap("redirect", "/dashboard.html"));
    }

    @GetMapping("/session")
    public Map<String, String> session(@CookieValue(value = "customer_session", defaultValue = "") String jwt) {
        return Collections.singletonMap("email", registration.sessionEmail(jwt));
    }
}
