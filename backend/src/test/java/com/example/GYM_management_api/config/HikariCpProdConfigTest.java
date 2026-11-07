package com.example.GYM_management_api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HikariCpProdConfigTest {

    @Test
    @DisplayName("Cấu hình application-prod.yaml: Các thông số HikariCP đồng bộ hoàn hảo với môi trường Prod")
    void testHikariCpProdConfigurationValues() throws IOException {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        ClassPathResource resource = new ClassPathResource("application-prod.yaml");
        assertThat(resource.exists()).isTrue();

        List<PropertySource<?>> propertySources = loader.load("application-prod.yaml", resource);
        assertThat(propertySources).isNotEmpty();

        PropertySource<?> propertySource = propertySources.get(0);

        // maximum-pool-size: 10 (phù hợp AWS RDS Free Tier)
        assertThat(propertySource.getProperty("spring.datasource.hikari.maximum-pool-size"))
                .isEqualTo("${DB_POOL_SIZE:10}");

        // minimum-idle: 2 (phù hợp AWS RDS Free Tier)
        assertThat(propertySource.getProperty("spring.datasource.hikari.minimum-idle"))
                .isEqualTo("${DB_MIN_IDLE:2}");

        // connection-timeout: 30000ms (30 giây chờ lấy kết nối)
        assertThat(propertySource.getProperty("spring.datasource.hikari.connection-timeout"))
                .isEqualTo("${DB_CONNECTION_TIMEOUT_MS:30000}");

        // idle-timeout: 600000ms (10 phút)
        assertThat(propertySource.getProperty("spring.datasource.hikari.idle-timeout"))
                .isEqualTo("${DB_IDLE_TIMEOUT_MS:600000}");

        // max-lifetime: 1800000ms (30 phút, ngắn hơn MySQL wait_timeout)
        assertThat(propertySource.getProperty("spring.datasource.hikari.max-lifetime"))
                .isEqualTo("${DB_MAX_LIFETIME_MS:1800000}");

        // keepalive-time: 30000ms
        assertThat(propertySource.getProperty("spring.datasource.hikari.keepalive-time"))
                .isEqualTo("${DB_KEEPALIVE_TIME_MS:30000}");

        // leak-detection-threshold: 2000ms
        assertThat(propertySource.getProperty("spring.datasource.hikari.leak-detection-threshold"))
                .isEqualTo(2000);

        // Tắt SQL logging trong prod để giảm tải CPU MySQL và I/O disk
        assertThat(propertySource.getProperty("spring.jpa.show-sql"))
                .isEqualTo(false);

        // Forward headers qua AWS ALB / Reverse proxy
        assertThat(propertySource.getProperty("server.forward-headers-strategy"))
                .isEqualTo("framework");

        // CORS allowed origins cho Vercel & ALB
        assertThat(propertySource.getProperty("app.cors.allowed-origins"))
                .isEqualTo("${CORS_ALLOWED_ORIGINS:https://*.vercel.app,https://*.elb.amazonaws.com,http://*.elb.amazonaws.com,https://*.duckdns.org,http://*.duckdns.org,http://localhost:5173,http://localhost:3000,http://localhost:80,http://127.0.0.1:*}");

        // Flyway expand-contract rollback safety in application-prod.yml
        assertThat(propertySource.getProperty("spring.flyway.enabled")).isEqualTo(true);
        assertThat(propertySource.getProperty("spring.flyway.validate-on-migrate")).isEqualTo(true);
        assertThat(propertySource.getProperty("spring.flyway.out-of-order")).isEqualTo(true);
        assertThat(List.of(
                String.valueOf(propertySource.getProperty("spring.flyway.ignore-migration-patterns[0]")),
                String.valueOf(propertySource.getProperty("spring.flyway.ignore-migration-patterns[1]"))
        )).containsExactlyInAnyOrder("*:future", "*:missing");

        // Actuator health & info endpoints exposure in application-prod.yml
        assertThat(propertySource.getProperty("management.endpoints.web.exposure.include"))
                .isEqualTo("health,info");
        assertThat(propertySource.getProperty("management.endpoint.health.show-details"))
                .isEqualTo("always");
        assertThat(propertySource.getProperty("management.endpoint.health.probes.enabled"))
                .isEqualTo(true);
        assertThat(propertySource.getProperty("info.app.version"))
                .isEqualTo("${APP_VERSION:1.0.0-dev}");
    }
}
