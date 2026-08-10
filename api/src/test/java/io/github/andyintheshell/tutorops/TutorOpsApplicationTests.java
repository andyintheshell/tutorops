package io.github.andyintheshell.tutorops;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Import(TestDatabaseConfiguration.class)
class TutorOpsApplicationTests {

    @Test
    void contextLoads() {
    }

}
