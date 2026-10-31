package com.homefit.server.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.homefit.server.config.JwtProperties;
import com.homefit.server.domain.Role;
import com.homefit.server.domain.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/** Kiểm tra tạo và đọc lại JWT mà không cần cơ sở dữ liệu. */
class TokenServiceTest {

    private static final String SECRET = "unit-test-secret-0123456789-abcdefghij";

    private static SecretKey key(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private static TokenService service() {
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key(SECRET)));
        return new TokenService(encoder, new JwtProperties(SECRET, 60, 30));
    }

    private static NimbusJwtDecoder decoder(String secret) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key(secret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenService.ISSUER));
        return decoder;
    }

    @Test
    void accessTokenRoundTrip() {
        User user = new User();
        user.setId(7L);
        user.setRole(Role.USER);

        String token = service().createAccessToken(user);
        Jwt jwt = decoder(SECRET).decode(token);

        assertEquals("7", jwt.getSubject());
        assertEquals("USER", jwt.getClaimAsString("role"));
        assertEquals(TokenService.ISSUER, jwt.getIssuer().toString());
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        User user = new User();
        user.setId(1L);
        String token = service().createAccessToken(user);

        assertThrows(BadJwtException.class,
                () -> decoder("another-secret-0123456789-abcdefghijk").decode(token));
    }

    @Test
    void hashIsSha256Hex() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                TokenService.hash("abc"));
    }

    @Test
    void refreshTokensAreRandomAndLongEnough() {
        TokenService service = service();
        String first = service.newRefreshTokenValue();
        String second = service.newRefreshTokenValue();
        assertNotEquals(first, second);
        assertEquals(43, first.length()); // 32 byte Base64 URL không padding
    }
}
