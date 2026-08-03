package io.github.andyintheshell.tutorops.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class KeycloakClientRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLE_PREFIX = "ROLE_";

    private final String clientId;
    private final JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    public KeycloakClientRoleConverter(
            @Value("${tutorops.security.oidc.api-client-id:tutorops-api}") String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>(
                defaultAuthoritiesConverter.convert(jwt));

        Object resourceAccessClaim = jwt.getClaim("resource_access");
        if (!(resourceAccessClaim instanceof Map<?, ?> resourceAccess)) {
            return authorities;
        }

        Object clientAccessClaim = resourceAccess.get(clientId);
        if (!(clientAccessClaim instanceof Map<?, ?> clientAccess)) {
            return authorities;
        }

        Object rolesClaim = clientAccess.get("roles");
        if (!(rolesClaim instanceof Collection<?> roles)) {
            return authorities;
        }

        roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .forEach(authorities::add);

        return authorities;
    }
}
