package io.github.andyintheshell.tutorops.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StatusControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void statusEndpointReturnsStatusAndService() throws Exception {
        mockMvc.perform(get("/api/public/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.service").value("tutorops-api"));
    }

    @Test
    void statusEndpointWithoutPublicPrefixRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorHealthEndpointReturnsUpStatus() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/*+json"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void actuatorInfoEndpointIsAllowed() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/*+json"))
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    void unexposedActuatorEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meEndpointAcceptsAnyAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/me")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new JwtAuthenticationToken(currentUserJwt(),
                                        List.of(new SimpleGrantedAuthority("ROLE_tutor"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-123"))
                .andExpect(jsonPath("$.username").value("alex"))
                .andExpect(jsonPath("$.email").value("alex@example.test"))
                .andExpect(jsonPath("$.firstName").value("Alex"))
                .andExpect(jsonPath("$.lastName").value("Example"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("tutor"));
    }

    private Jwt currentUserJwt() {
        Instant now = Instant.now();
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("user-123")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim("preferred_username", "alex")
                .claim("email", "alex@example.test")
                .claim("given_name", "Alex")
                .claim("family_name", "Example")
                .build();
    }

    @Test
    void tutorEndpointAllowsTutorRole() throws Exception {
        mockMvc.perform(get("/api/tutor/example")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_tutor"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void tutorEndpointRejectsStudentRole() throws Exception {
        mockMvc.perform(get("/api/tutor/example")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_student"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentEndpointAllowsStudentRole() throws Exception {
        mockMvc.perform(get("/api/student/example")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_student"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentEndpointRejectsTutorRole() throws Exception {
        mockMvc.perform(get("/api/student/example")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_tutor"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointAllowsAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/example")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminEndpointRejectsTutorRole() throws Exception {
        mockMvc.perform(get("/api/admin/example")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_tutor"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentPingReturnsOkForStudentRole() throws Exception {
        mockMvc.perform(get("/api/student/ping")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_student"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.role").value("student"));
    }

    @Test
    void tutorPingReturnsOkForTutorRole() throws Exception {
        mockMvc.perform(get("/api/tutor/ping")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_tutor"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.role").value("tutor"));
    }

    @Test
    void adminPingReturnsOkForAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/ping")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.role").value("admin"));
    }

    @Test
    void studentPingRejectsTutorRole() throws Exception {
        mockMvc.perform(get("/api/student/ping")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_tutor"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsReactOriginPreflightRequest() throws Exception {
        mockMvc.perform(options("/api/me")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("GET")));
    }

    @Test
    void rejectsUnconfiguredCorsOrigin() throws Exception {
        mockMvc.perform(options("/api/me")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
