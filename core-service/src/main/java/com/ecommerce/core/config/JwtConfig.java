package com.ecommerce.core.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
public class JwtConfig {

    private static final String RSA = "RSA";
    private static final int EPHEMERAL_KEY_SIZE = 2048;

    @Bean
    public KeyPair jwtSigningKeyPair(AppProperties properties) throws IOException, GeneralSecurityException {
        AppProperties.Jwt jwt = properties.jwt();
        if (StringUtils.hasText(jwt.privateKeyPath()) && StringUtils.hasText(jwt.publicKeyPath())) {
            return new KeyPair(readPublicKey(Path.of(jwt.publicKeyPath())), readPrivateKey(Path.of(jwt.privateKeyPath())));
        }
        log.warn("JWT key paths not configured; generating an ephemeral RS256 key pair (not for production)");
        KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
        generator.initialize(EPHEMERAL_KEY_SIZE);
        return generator.generateKeyPair();
    }

    static PrivateKey readPrivateKey(Path path) throws IOException, GeneralSecurityException {
        byte[] der = decodePem(Files.readString(path));
        return KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    static PublicKey readPublicKey(Path path) throws IOException, GeneralSecurityException {
        byte[] der = decodePem(Files.readString(path));
        return KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(der));
    }

    private static byte[] decodePem(String pem) {
        String base64 = pem.replaceAll("-----(BEGIN|END) [A-Z ]+-----", "").replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
