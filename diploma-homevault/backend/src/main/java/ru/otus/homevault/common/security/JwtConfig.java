package ru.otus.homevault.common.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Configuration
public class JwtConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

    private static final int MIN_HS256_SECRET_BYTES = 32;

    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] secret = resolveSecret(properties.secret());
        if (secret.length < MIN_HS256_SECRET_BYTES) {
            throw new IllegalStateException("HOMEVAULT_JWT_SECRET must be at least 32 bytes for HS256");
        }
        return new SecretKeySpec(secret, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        JWKSource<SecurityContext> jwkSource = new ImmutableSecret<>(jwtSecretKey);
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private byte[] resolveSecret(String configuredSecret) {
        if (StringUtils.hasText(configuredSecret)) {
            if (configuredSecret.startsWith("base64:")) {
                return Base64.getDecoder().decode(configuredSecret.substring("base64:".length()));
            }
            return configuredSecret.getBytes(StandardCharsets.UTF_8);
        }

        byte[] generatedSecret = new byte[64];
        new SecureRandom().nextBytes(generatedSecret);
        log.warn("HOMEVAULT_JWT_SECRET is not set. Generated an ephemeral JWT secret for this application run.");
        return generatedSecret;
    }
}

