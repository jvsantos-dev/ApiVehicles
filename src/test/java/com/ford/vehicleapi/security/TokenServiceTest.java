package com.ford.vehicleapi.security;

import com.ford.vehicleapi.domain.Role;
import com.ford.vehicleapi.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testes unitários de geração e validação de JWT (sem subir o Spring). */
@DisplayName("Geração e validação de JWT (unitário)")
class TokenServiceTest {

    private static final JwtProperties PROPS =
            new JwtProperties("unit-test-secret-with-at-least-32-bytes!!", Duration.ofMinutes(30), "ford-test");

    private final JwtConfig config = new JwtConfig();
    private JwtEncoder encoder;
    private JwtDecoder decoder;
    private User user;

    @BeforeEach
    void setUp() {
        SecretKey key = config.jwtSecretKey(PROPS);
        encoder = config.jwtEncoder(key);
        decoder = config.jwtDecoder(key, PROPS);
        user = new User("Maria", "maria@email.com", "hash", Role.DEALER);
        ReflectionTestUtils.setField(user, "id", 42L);
    }

    @Test
    @DisplayName("Gera token com claims de identidade, perfil, emissor e expiração")
    void issuesTokenWithExpectedClaims() {
        TokenService.IssuedToken issued = new TokenService(encoder, PROPS).issue(user);

        Jwt jwt = decoder.decode(issued.value());

        assertThat(jwt.getSubject()).isEqualTo("maria@email.com");
        assertThat(jwt.getClaimAsString("uid")).isEqualTo("42");
        assertThat(jwt.getClaimAsString("name")).isEqualTo("Maria");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("DEALER");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("ford-test");
        assertThat(jwt.getId()).isNotBlank();
        assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(30));
        assertThat(issued.expiresInSeconds()).isEqualTo(1800);
    }

    @Test
    @DisplayName("Token não carrega dados sensíveis como a senha")
    void tokenDoesNotContainPassword() {
        Jwt jwt = decoder.decode(new TokenService(encoder, PROPS).issue(user).value());

        assertThat(jwt.getClaims()).doesNotContainKeys("password");
        assertThat(jwt.getClaims().values()).doesNotContain("hash");
    }

    @Test
    @DisplayName("Token expirado é rejeitado na validação")
    void rejectsExpiredToken() {
        Clock twoHoursAgo = Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC);
        String token = new TokenService(encoder, PROPS, twoHoursAgo).issue(user).value();

        assertThatThrownBy(() -> decoder.decode(token))
                .isInstanceOf(JwtValidationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Token de outro emissor é rejeitado")
    void rejectsTokenFromAnotherIssuer() {
        JwtProperties otherIssuer = new JwtProperties(PROPS.secret(), PROPS.expiration(), "outro-emissor");
        String token = new TokenService(encoder, otherIssuer).issue(user).value();

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtValidationException.class);
    }

    @Test
    @DisplayName("Token assinado com outra chave é rejeitado")
    void rejectsTokenSignedWithAnotherKey() {
        JwtProperties otherSecret = new JwtProperties("another-secret-with-at-least-32-bytes!!!", PROPS.expiration(), PROPS.issuer());
        JwtEncoder otherEncoder = config.jwtEncoder(config.jwtSecretKey(otherSecret));
        String token = new TokenService(otherEncoder, PROPS).issue(user).value();

        assertThatThrownBy(() -> decoder.decode(token)).hasMessageContaining("signature");
    }

    @Test
    @DisplayName("Segredo com menos de 32 bytes impede a aplicação de subir")
    void rejectsWeakSecret() {
        JwtProperties weak = new JwtProperties("curto", Duration.ofHours(1), "x");

        assertThatThrownBy(() -> config.jwtSecretKey(weak))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }
}
