package io.github.andyintheshell.tutorops.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthorizationProbeController {

    @GetMapping("/student/ping")
    public AuthorizationProbeResponse studentPing() {
        return new AuthorizationProbeResponse("ok", "student");
    }

    @GetMapping("/tutor/ping")
    public AuthorizationProbeResponse tutorPing() {
        return new AuthorizationProbeResponse("ok", "tutor");
    }

    @GetMapping("/admin/ping")
    public AuthorizationProbeResponse adminPing() {
        return new AuthorizationProbeResponse("ok", "admin");
    }

    public record AuthorizationProbeResponse(String status, String role) {
    }
}
