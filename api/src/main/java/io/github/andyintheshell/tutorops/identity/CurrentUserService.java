package io.github.andyintheshell.tutorops.identity;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findExisting(Jwt jwt) {
        String issuer = Objects.requireNonNull(jwt.getIssuer(), "JWT issuer is required").toString();
        String subject = requireClaim(jwt.getSubject());
        return appUserRepository.findByIssuerAndSubject(issuer, subject);
    }

    @Transactional
    public AppUser findOrProvision(Jwt jwt) {
        String issuer = Objects.requireNonNull(jwt.getIssuer(), "JWT issuer is required").toString();
        String subject = requireClaim(jwt.getSubject());
        String email = requiredClaim(jwt, "email");
        String displayName = displayName(jwt);

        appUserRepository.upsert(issuer, subject, email, displayName);
        return appUserRepository.findByIssuerAndSubject(issuer, subject)
                .orElseThrow(() -> new IllegalStateException("Provisioned user could not be found"));
    }

    private String displayName(Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        if (hasText(name)) {
            return name;
        }

        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (hasText(preferredUsername)) {
            return preferredUsername;
        }

        String givenName = jwt.getClaimAsString("given_name");
        String familyName = jwt.getClaimAsString("family_name");
        String combinedName = Stream.of(givenName, familyName)
                .filter(this::hasText)
                .collect(Collectors.joining(" "));
        return requireClaim(combinedName);
    }

    private String requiredClaim(Jwt jwt, String claimName) {
        return requireClaim(jwt.getClaimAsString(claimName));
    }

    private String requireClaim(String value) {
        if (!hasText(value)) {
            throw new IllegalArgumentException("Required JWT claim is missing");
        }
        return value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
