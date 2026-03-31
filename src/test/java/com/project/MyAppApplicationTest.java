package com.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MyAppApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void main_runsWithoutException() {
        MyAppApplication.main(new String[]{});
    }
}
