package io.github.andyintheshell.tutorops.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakClientRoleConverterTest {

    private final KeycloakClientRoleConverter converter =
            new KeycloakClientRoleConverter("tutorops-api");

    @Test
    void convertsClientRolesAndScopes() {
        Jwt jwt = jwt(Map.of(
                "scope", "openid profile",
                "resource_access", Map.of(
                        "tutorops-api", Map.of("roles", List.of("TUTOR", "ADMIN")))));

        assertThat(converter.convert(jwt))
                .containsExactlyInAnyOrder(
                        new SimpleGrantedAuthority("SCOPE_openid"),
                        new SimpleGrantedAuthority("SCOPE_profile"),
                        new SimpleGrantedAuthority("ROLE_TUTOR"),
                        new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void ignoresRolesForOtherClients() {
        Jwt jwt = jwt(Map.of(
                "resource_access", Map.of(
                        "another-client", Map.of("roles", List.of("ADMIN")))));

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    void handlesMissingResourceAccessClaim() {
        assertThat(converter.convert(jwt(Map.of()))).isEmpty();
    }

    private Jwt jwt(Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("user-1")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claims(existingClaims -> existingClaims.putAll(claims))
                .build();
    }
}
