package io.github.andyintheshell.tutorops.identity;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByIssuerAndSubject(String issuer, String subject);

    @Modifying
    @Query(value = """
            INSERT INTO app_user (issuer, subject, email, display_name)
            VALUES (:issuer, :subject, :email, :displayName)
            ON CONFLICT (issuer, subject) DO UPDATE
            SET email = EXCLUDED.email,
                display_name = EXCLUDED.display_name,
                updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    void upsert(@Param("issuer") String issuer,
                @Param("subject") String subject,
                @Param("email") String email,
                @Param("displayName") String displayName);
}
