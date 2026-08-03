package io.github.andyintheshell.tutorops.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    void statusEndpointWithoutPublicPrefixIsForbidden() throws Exception {
        mockMvc.perform(get("/status"))
                .andExpect(status().isForbidden());
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
    void unexposedActuatorEndpointIsForbidden() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isForbidden());
    }
}
