package com.smarthiring.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseConnectionTest {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void testConnection() {
        try {
            String dbName = jdbcTemplate.queryForObject(
                    "SELECT DATABASE()", String.class
            );
            log.info("========================================");
            log.info("✅ Database Connection Successful!");
            log.info("📦 Connected to: {}", dbName);
            log.info("========================================");
        } catch (Exception e) {
            log.error("========================================");
            log.error("❌ Database Connection Failed!");
            log.error("Error: {}", e.getMessage());
            log.error("========================================");
        }
    }
}