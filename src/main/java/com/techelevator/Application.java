package com.techelevator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /** Injected wherever "now" matters so tests can pin the time. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
