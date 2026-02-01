package com.smarthiring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health Check", description = "API Health Check Endpoints")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check if the API is running")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "Smart Hiring System");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    @Operation(summary = "Welcome", description = "Welcome message")
    public ResponseEntity<Map<String, String>> root() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to Smart Hiring System API!");
        response.put("documentation", "/swagger-ui/index.html");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(summary = "API Info", description = "Get API information")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Smart Hiring System");
        response.put("version", "1.0.0");
        response.put("description", "AI-Powered Smart Hiring System");
        response.put("author", "Yashpal Parmar");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("health", "/api/health");
        endpoints.put("swagger", "/swagger-ui/index.html");
        endpoints.put("api-docs", "/v3/api-docs");
        response.put("endpoints", endpoints);

        return ResponseEntity.ok(response);
    }
}