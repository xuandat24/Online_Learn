package com.onlinelearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OnlineLearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineLearnApplication.class, args);
    }
}
