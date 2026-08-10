package io.github.andyintheshell.tutorops.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import io.github.andyintheshell.tutorops.TestDatabaseConfiguration;

@SpringBootTest
@Import(TestDatabaseConfiguration.class)
@Transactional
class AppUserRepositoryIntegrationTest {

    private final AppUserRepository appUserRepository;

    @Autowired
    AppUserRepositoryIntegrationTest(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Test
    void findsUserByIssuerAndSubject() {
        AppUser savedUser = appUserRepository.saveAndFlush(
                new AppUser(
                        "https://issuer.example.test/realms/tutorops",
                        "repository-user",
                        "user@example.test",
                        "Repository User"));

        assertThat(appUserRepository.findByIssuerAndSubject(
                        savedUser.getIssuer(), savedUser.getSubject()))
                .get()
                .extracting(AppUser::getId, AppUser::getEmail, AppUser::getDisplayName)
                .containsExactly(savedUser.getId(), "user@example.test", "Repository User");
    }

    @Test
    void permitsSameSubjectForDifferentIssuers() {
        appUserRepository.saveAndFlush(new AppUser(
                "https://issuer-one.example.test",
                "same-subject",
                "one@example.test",
                "Issuer One User"));

        AppUser secondUser = appUserRepository.saveAndFlush(new AppUser(
                "https://issuer-two.example.test",
                "same-subject",
                "two@example.test",
                "Issuer Two User"));

        assertThat(appUserRepository.findByIssuerAndSubject(
                        secondUser.getIssuer(), secondUser.getSubject()))
                .containsSame(secondUser);
    }

    @Test
    void rejectsDuplicateIssuerAndSubject() {
        AppUser firstUser = appUserRepository.saveAndFlush(new AppUser(
                "https://issuer.example.test/realms/tutorops",
                "duplicate-subject",
                "first@example.test",
                "First User"));

        assertThatThrownBy(() -> appUserRepository.saveAndFlush(new AppUser(
                firstUser.getIssuer(),
                firstUser.getSubject(),
                "second@example.test",
                "Second User")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
