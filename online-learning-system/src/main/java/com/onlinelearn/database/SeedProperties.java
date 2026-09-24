package com.onlinelearn.database;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class SeedProperties {

    /**
     * Flag to enable or disable initial seed data generation.
     */
    private boolean seedData = true;

    /**
     * Base URL of the application.
     */
    private String baseUrl = "http://localhost:8080";
}
