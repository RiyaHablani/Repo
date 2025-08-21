package com.hashedin.huspark.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration for monitoring, metrics, and health checks
 */
@Configuration
@EnableAspectJAutoProxy
public class MonitoringConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(MonitoringConfig.class);
    
    /**
     * Enable timing metrics for all methods annotated with @Timed
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        logger.info("Configuring timed aspect for metrics collection");
        return new TimedAspect(registry);
    }
    
    /**
     * Custom health indicator for the application
     */
    @Bean
    public HealthIndicator customHealthIndicator() {
        return new HealthIndicator() {
            @Override
            public Health health() {
                try {
                    // Add custom health checks here
                    // For now, just return UP
                    return Health.up()
                        .withDetail("application", "huSpark")
                        .withDetail("version", "8.0.1-SNAPSHOT")
                        .withDetail("timestamp", java.time.LocalDateTime.now())
                        .build();
                } catch (Exception e) {
                    logger.error("Health check failed", e);
                    return Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
                }
            }
        };
    }
}
