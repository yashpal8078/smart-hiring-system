package com.smarthiring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.email")
@Getter
@Setter
public class EmailConfig {

    private String from = "noreply@smarthiring.com";
    private String fromName = "Smart Hiring System";
    private boolean enabled = true;
    private String baseUrl = "http://localhost:3000";
}