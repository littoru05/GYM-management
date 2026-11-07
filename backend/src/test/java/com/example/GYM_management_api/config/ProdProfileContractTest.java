package com.example.GYM_management_api.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ProdProfileContractTest {

    private static Properties prodProps;
    private static Properties mergedProps;

    private static org.springframework.core.io.Resource getBaseApplicationYamlResource() {
        org.springframework.core.io.Resource moduleRelative = new org.springframework.core.io.FileSystemResource("src/main/resources/application.yaml");
        if (moduleRelative.exists()) {
            return moduleRelative;
        }
        org.springframework.core.io.Resource repoRootRelative = new org.springframework.core.io.FileSystemResource("backend/src/main/resources/application.yaml");
        if (repoRootRelative.exists()) {
            return repoRootRelative;
        }
        return new ClassPathResource("application.yaml");
    }

    @BeforeAll
    static void setUp() {
        ClassPathResource prodResource = new ClassPathResource("application-prod.yaml");
        assertThat(prodResource.exists())
                .as("application-prod.yaml must exist on the classpath")
                .isTrue();

        YamlPropertiesFactoryBean prodFactoryBean = new YamlPropertiesFactoryBean();
        prodFactoryBean.setResources(prodResource);
        prodFactoryBean.afterPropertiesSet();
        prodProps = prodFactoryBean.getObject();

        YamlPropertiesFactoryBean mergedFactoryBean = new YamlPropertiesFactoryBean();
        mergedFactoryBean.setResources(
                getBaseApplicationYamlResource(),
                prodResource
        );
        mergedFactoryBean.afterPropertiesSet();
        mergedProps = mergedFactoryBean.getObject();
    }

    @Test
    @DisplayName("Contract: Prod profile activation matches 'prod'")
    void testProdProfileActivation() {
        assertThat(prodProps.getProperty("spring.config.activate.on-profile"))
                .isEqualTo("prod");
    }

    @Test
    @DisplayName("Contract: Datasource URL targets external MySQL port 3306 with sslMode=PREFERRED and local timezone")
    void testDatasourceUrlInvariants() {
        String url = prodProps.getProperty("spring.datasource.url");

        assertThat(url).isNotNull();
        assertThat(url).contains("3306");
        assertThat(url).contains("sslMode=PREFERRED");
        assertThat(url).doesNotContain("useSSL=false");
        assertThat(url).contains("serverTimezone=Asia/Ho_Chi_Minh");
        assertThat(url).contains("characterEncoding=UTF-8");
    }

    @Test
    @DisplayName("Contract: HikariCP connection pool is bounded to maximum 10 and minimum 2 idle")
    void testHikariConnectionPoolBounds() {
        String maxPoolSize = prodProps.getProperty("spring.datasource.hikari.maximum-pool-size");
        String minIdle = prodProps.getProperty("spring.datasource.hikari.minimum-idle");

        assertThat(maxPoolSize).matches("(\\$\\{DB_POOL_SIZE:\\s*10\\}|10)");
        assertThat(minIdle).matches("(\\$\\{DB_MIN_IDLE:\\s*2\\}|2)");
    }

    @Test
    @DisplayName("Contract: Hibernate SQL logging and formatting are disabled in prod")
    void testHibernateSqlLoggingDisabled() {
        assertThat(prodProps.getProperty("spring.jpa.show-sql")).isEqualTo("false");
        assertThat(prodProps.getProperty("spring.jpa.properties.hibernate.format_sql")).isEqualTo("false");
        assertThat(prodProps.getProperty("spring.jpa.properties.hibernate.highlight_sql")).isEqualTo("false");
    }

    @Test
    @DisplayName("Contract: SQL and transaction loggers are suppressed to WARN/INFO")
    void testLoggingLevelsSuppressed() {
        assertThat(prodProps.getProperty("logging.level.org.hibernate.SQL")).isEqualTo("WARN");
        assertThat(prodProps.getProperty("logging.level.org.hibernate.orm.jdbc.bind")).isEqualTo("WARN");
        assertThat(prodProps.getProperty("logging.level.org.springframework.transaction"))
                .matches("WARN|INFO");
    }

    @Test
    @DisplayName("Contract: Flyway executes core schema migrations and initial demo seed migrations")
    void testFlywayLocationsInProd() {
        assertThat(prodProps.getProperty("spring.flyway.enabled")).isEqualTo("true");

        String loc0 = prodProps.getProperty("spring.flyway.locations[0]");
        String loc1 = prodProps.getProperty("spring.flyway.locations[1]");

        assertThat(List.of(loc0, loc1))
                .containsExactlyInAnyOrder("classpath:db/migration", "classpath:db/migration-dev");
    }

    @Test
    @DisplayName("Contract: Flyway is configured for expand-contract rollback safety in prod")
    void testFlywayExpandContractInProd() {
        assertThat(prodProps.getProperty("spring.flyway.validate-on-migrate")).isEqualTo("true");
        assertThat(prodProps.getProperty("spring.flyway.out-of-order")).isEqualTo("true");

        String pattern0 = prodProps.getProperty("spring.flyway.ignore-migration-patterns[0]");
        String pattern1 = prodProps.getProperty("spring.flyway.ignore-migration-patterns[1]");
        assertThat(List.of(pattern0, pattern1))
                .containsExactlyInAnyOrder("*:future", "*:missing");
    }

    @Test
    @DisplayName("Contract: Base application.yaml configures Flyway expand-contract safety")
    void testFlywayExpandContractInBase() {
        YamlPropertiesFactoryBean baseFactoryBean = new YamlPropertiesFactoryBean();
        baseFactoryBean.setResources(getBaseApplicationYamlResource());
        baseFactoryBean.afterPropertiesSet();
        Properties baseProps = baseFactoryBean.getObject();

        assertThat(baseProps).isNotNull();
        assertThat(baseProps.getProperty("spring.flyway.validate-on-migrate")).isEqualTo("true");
        assertThat(baseProps.getProperty("spring.flyway.out-of-order")).isEqualTo("true");

        String pattern0 = baseProps.getProperty("spring.flyway.ignore-migration-patterns[0]");
        String pattern1 = baseProps.getProperty("spring.flyway.ignore-migration-patterns[1]");
        assertThat(List.of(pattern0, pattern1))
                .containsExactlyInAnyOrder("*:future", "*:missing");
    }

    @Test
    @DisplayName("Contract: Prod profile successfully overrides base application.yaml properties")
    void testMergedPropertiesOverride() {
        // Base was show-sql: true, prod overrides to false
        assertThat(mergedProps.getProperty("spring.jpa.show-sql")).isEqualTo("false");

        // Base maximum-pool-size was 20, prod overrides to 10
        assertThat(mergedProps.getProperty("spring.datasource.hikari.maximum-pool-size"))
                .isEqualTo("${DB_POOL_SIZE:10}");

        // Base org.hibernate.SQL was DEBUG, prod overrides to WARN
        assertThat(mergedProps.getProperty("logging.level.org.hibernate.SQL")).isEqualTo("WARN");
    }

    @Test
    @DisplayName("Contract: Prod profile configures Actuator health/info endpoints and probes")
    void testActuatorExposureInProd() {
        assertThat(prodProps.getProperty("management.endpoints.web.exposure.include")).isEqualTo("health,info");
        assertThat(prodProps.getProperty("management.endpoint.health.show-details")).isEqualTo("always");
        assertThat(prodProps.getProperty("management.endpoint.health.probes.enabled")).isEqualTo("true");
        assertThat(prodProps.getProperty("management.info.env.enabled")).isEqualTo("true");
        assertThat(prodProps.getProperty("info.app.version")).isEqualTo("${APP_VERSION:1.0.0-dev}");
    }
}
