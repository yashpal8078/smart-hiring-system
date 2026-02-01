package com.smarthiring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // JPA Auditing enabled
    // This automatically populates @CreatedDate and @LastModifiedDate fields
}