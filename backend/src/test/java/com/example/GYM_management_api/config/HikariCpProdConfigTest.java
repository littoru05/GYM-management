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
    @DisplayName("Cấu hình application-prod.yml: Các thông số HikariCP đồng bộ hoàn hảo với MySQL container")
    void testHikariCpProdConfigurationValues() throws IOException {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        ClassPathResource resource = new ClassPathResource("application-prod.yml");
        assertThat(resource.exists()).isTrue();

        List<PropertySource<?>> propertySources = loader.load("application-prod.yml", resource);
        assertThat(propertySources).isNotEmpty();

        PropertySource<?> propertySource = propertySources.get(0);

        // maximum-pool-size: 30 (nhỏ hơn 50 của MySQL container, để dành 20 kết nối cho các tác vụ nội bộ)
        assertThat(propertySource.getProperty("spring.datasource.hikari.maximum-pool-size"))
                .isEqualTo("${DB_POOL_SIZE:30}");

        // minimum-idle: 30 (Fixed pool size tránh độ trễ handshake khi traffic spike)
        assertThat(propertySource.getProperty("spring.datasource.hikari.minimum-idle"))
                .isEqualTo("${DB_MIN_IDLE:30}");

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
    }
}
