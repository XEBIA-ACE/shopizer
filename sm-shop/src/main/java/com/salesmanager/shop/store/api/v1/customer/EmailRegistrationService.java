package com.salesmanager.shop.store.api.v1.customer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.salesmanager.core.business.modules.email.Email;
import com.salesmanager.core.business.repositories.customer.CustomerSessionRepository;
import com.salesmanager.core.business.repositories.customer.EmailVerificationTokenRepository;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.system.EmailService;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.customer.CustomerSession;
import com.salesmanager.core.model.customer.EmailVerificationToken;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.store.controller.customer.facade.CustomerFacade;
import com.salesmanager.shop.store.security.JWTTokenUtil;

@Service
public class EmailRegistrationService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TOKEN_LIFETIME = 24L * 60 * 60 * 1000;
    private static final long RESEND_COOLDOWN = 60_000;

    @Inject private CustomerFacade customerFacade;
    @Inject private CustomerService customerService;
    @Inject private EmailVerificationTokenRepository tokens;
    @Inject private CustomerSessionRepository sessions;
    @Inject private EmailService emailService;
    @Inject private JWTTokenUtil jwtTokenUtil;
    @Inject private UserDetailsService jwtCustomerDetailsService;

    @Value("${registration.base-url:http://localhost:8080}")
    private String baseUrl;

    private Clock clock = Clock.systemUTC();

    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String password, MerchantStore store, Language language) throws Exception {
        String normalized = normalize(email);
        if (!EMAIL.matcher(normalized).matches() || normalized.length() > 96) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address");
        }
        if (password == null || password.length() < 8 || !password.matches(".*[A-Z].*")
                || !password.matches(".*[0-9].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must have at least 8 characters, one uppercase letter and one digit");
        }
        if (customerService.getByNickForRegistration(normalized, store.getId()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        PersistableCustomer customer = new PersistableCustomer();
        customer.setEmailAddress(normalized);
        customer.setUserName(normalized);
        customer.setPassword(password);
        customerFacade.registerPendingCustomer(customer, store, language);

        Customer saved = customerService.getById(customer.getId());
        EmailVerificationToken verification = new EmailVerificationToken();
        verification.setCustomer(saved);
        verification.setResendCount(0);
        issueLink(verification, store);
    }

    @Transactional(rollbackFor = Exception.class)
    public void resend(String email, MerchantStore store) throws Exception {
        Customer customer = customerService.getByNickForRegistration(normalize(email), store.getId());
        if (customer == null || !store.getId().equals(customer.getMerchantStore().getId())
                || !Boolean.FALSE.equals(customer.getEmailVerified())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pending registration not found");
        }
        EmailVerificationToken verification = tokens.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pending registration not found"));
        if (verification.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Verification link is no longer valid");
        }
        long now = clock.millis();
        if (verification.getResendCount() >= 5) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Resend limit reached");
        }
        if (now - verification.getLastSentAt().getTime() < RESEND_COOLDOWN) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Wait 60 seconds before resending");
        }
        verification.setResendCount(verification.getResendCount() + 1);
        issueLink(verification, store);
    }

    @Transactional(rollbackFor = Exception.class)
    public String verify(String rawToken) throws Exception {
        if (rawToken == null || !rawToken.matches("[0-9a-f]{64}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification link is invalid");
        }
        EmailVerificationToken verification = tokens.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification link is invalid"));
        if (verification.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Verification link is no longer valid");
        }
        if (!verification.getExpiresAt().after(Date.from(clock.instant()))) {
            throw new ResponseStatusException(HttpStatus.GONE, "Verification link expired; request a new link");
        }
        Customer customer = verification.getCustomer();
        if (!Boolean.FALSE.equals(customer.getEmailVerified())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Verification link is no longer valid");
        }
        verification.setConsumedAt(Date.from(clock.instant()));
        customer.setEmailVerified(true);
        customerService.saveOrUpdate(customer);
        tokens.save(verification);

        String jwt = jwtTokenUtil.generateToken(jwtCustomerDetailsService.loadUserByUsername(customer.getNick()));
        CustomerSession session = new CustomerSession();
        session.setTokenHash(hash(jwt));
        session.setCustomer(customer);
        session.setExpiresAt(jwtTokenUtil.getExpirationDateFromToken(jwt));
        sessions.save(session);
        return jwt;
    }

    @Transactional(readOnly = true)
    public String sessionEmail(String jwt) {
        if (jwt == null || !jwt.matches("[A-Za-z0-9_.-]{20,4096}")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in required");
        }
        Optional<CustomerSession> session = sessions.findById(hash(jwt));
        if (!session.isPresent() || !session.get().getExpiresAt().after(Date.from(clock.instant()))
                || Boolean.FALSE.equals(session.get().getCustomer().getEmailVerified())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");
        }
        try {
            if (!jwtTokenUtil.validateToken(jwt,
                    jwtCustomerDetailsService.loadUserByUsername(session.get().getCustomer().getNick()))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");
            }
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");
        }
        return session.get().getCustomer().getEmailAddress();
    }

    private void issueLink(EmailVerificationToken verification, MerchantStore store) throws Exception {
        byte[] random = new byte[32];
        RANDOM.nextBytes(random);
        StringBuilder raw = new StringBuilder(64);
        for (byte value : random) {
            raw.append(String.format("%02x", value & 0xff));
        }
        Date sentAt = Date.from(clock.instant());
        verification.setTokenHash(hash(raw.toString()));
        verification.setLastSentAt(sentAt);
        verification.setExpiresAt(new Date(sentAt.getTime() + TOKEN_LIFETIME));
        tokens.save(verification);

        Email message = new Email();
        message.setFrom(store.getStorename());
        message.setFromEmail(store.getStoreEmailAddress());
        message.setTo(verification.getCustomer().getEmailAddress());
        message.setSubject("Confirm your email address");
        message.setTemplateName("email_template_registration_verification.ftl");
        message.getTemplateTokens().put("CONFIRMATION_URL", baseUrl.replaceAll("/+$", "")
                + "/verify-email.html?token=" + URLEncoder.encode(raw.toString(), StandardCharsets.UTF_8.name()));
        emailService.sendHtmlEmail(store, message);
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte part : digest) {
                result.append(String.format("%02x", part & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
