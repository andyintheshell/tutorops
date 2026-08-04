package io.github.andyintheshell.tutorops.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("cloud-smoke")
class CloudSmokeSecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicAndActuatorEndpointsAreAvailableWithoutKeycloak() throws Exception {
        mockMvc.perform(get("/api/public/status")).andExpect(status().isOk());
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mockMvc.perform(get("/actuator/info")).andExpect(status().isOk());
    }

    @Test
    void applicationEndpointsAreDenied() throws Exception {
        mockMvc.perform(get("/api/me")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/student/ping")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/tutor/ping")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/ping")).andExpect(status().isForbidden());
        mockMvc.perform(get("/unrecognized")).andExpect(status().isForbidden());
    }
}
