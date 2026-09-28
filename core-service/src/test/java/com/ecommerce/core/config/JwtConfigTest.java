package com.ecommerce.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JwtConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsKeyPairFromPemFiles() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair original = generator.generateKeyPair();
        Path privatePath = writePem("private.pem", "PRIVATE KEY", original.getPrivate().getEncoded());
        Path publicPath = writePem("public.pem", "PUBLIC KEY", original.getPublic().getEncoded());

        KeyPair loaded = new JwtConfig().jwtSigningKeyPair(properties(privatePath.toString(), publicPath.toString()));

        assertThat(loaded.getPrivate().getEncoded()).isEqualTo(original.getPrivate().getEncoded());
        assertThat(loaded.getPublic().getEncoded()).isEqualTo(original.getPublic().getEncoded());
    }

    @Test
    void generatesEphemeralKeyPairWhenPathsMissing() throws Exception {
        KeyPair generated = new JwtConfig().jwtSigningKeyPair(properties("", ""));

        assertThat(generated.getPrivate().getAlgorithm()).isEqualTo("RSA");
    }

    private Path writePem(String name, String type, byte[] der) throws Exception {
        String body = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der);
        return Files.writeString(tempDir.resolve(name),
                "-----BEGIN " + type + "-----\n" + body + "\n-----END " + type + "-----\n");
    }

    private static AppProperties properties(String privatePath, String publicPath) {
        return new AppProperties(null, null, null,
                new AppProperties.Jwt("issuer", privatePath, publicPath), null, null);
    }
}
