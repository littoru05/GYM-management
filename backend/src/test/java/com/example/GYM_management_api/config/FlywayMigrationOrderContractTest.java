package com.example.GYM_management_api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationOrderContractTest {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^V(\\d+)__.*\\.sql$");
    private static final Pattern ALL_INSERT_TABLE_PATTERN = Pattern.compile(
            "(?is)\\bINSERT\\s+(?:IGNORE\\s+)?INTO\\s+([a-zA-Z0-9_]+)"
    );
    private static final Pattern BARE_INSERT_STATEMENT_PATTERN = Pattern.compile(
            "(?is)\\bINSERT\\s+INTO\\s+([a-zA-Z0-9_]+)\\b([^;]*);"
    );

    private record MigrationScript(int version, String filename, String location, String sql) {}

    private List<MigrationScript> loadMigrationsInExecutionOrder() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<MigrationScript> scripts = new ArrayList<>();

        for (String location : List.of("classpath*:db/migration/V*.sql", "classpath*:db/migration-dev/V*.sql")) {
            for (Resource resource : resolver.getResources(location)) {
                String filename = resource.getFilename();
                assertThat(filename).isNotNull();
                Matcher matcher = VERSION_PATTERN.matcher(filename);
                assertThat(matcher.matches())
                        .as("Migration filename %s must match V<version>__<description>.sql", filename)
                        .isTrue();
                int version = Integer.parseInt(matcher.group(1));
                String sql = resource.getContentAsString(StandardCharsets.UTF_8);
                scripts.add(new MigrationScript(version, filename, location, sql));
            }
        }

        scripts.sort(Comparator.comparingInt(MigrationScript::version));
        return scripts;
    }

    @Test
    @DisplayName("Contract: Later migrations must not run bare INSERT INTO on tables already seeded by earlier migrations")
    void laterMigrationsMustBeIdempotentOnPreviouslySeededTables() throws IOException {
        List<MigrationScript> scripts = loadMigrationsInExecutionOrder();
        assertThat(scripts).isNotEmpty();

        Set<String> seededTables = new HashSet<>();

        for (MigrationScript script : scripts) {
            Matcher bareInsertMatcher = BARE_INSERT_STATEMENT_PATTERN.matcher(script.sql());
            while (bareInsertMatcher.find()) {
                String table = bareInsertMatcher.group(1).toLowerCase();
                String statementBody = bareInsertMatcher.group(2);
                boolean hasIdempotentGuard = statementBody.toUpperCase().contains("ON DUPLICATE KEY UPDATE")
                        || statementBody.toUpperCase().contains("WHERE NOT EXISTS");

                if (seededTables.contains(table)) {
                    assertThat(hasIdempotentGuard)
                            .as(
                                    "Migration %s (V%d) executes a bare 'INSERT INTO %s' without "
                                            + "INSERT IGNORE / ON DUPLICATE KEY UPDATE / WHERE NOT EXISTS, "
                                            + "which collides with rows already seeded by an earlier migration on a fresh DB",
                                    script.filename(), script.version(), table
                            )
                            .isTrue();
                }
            }

            Matcher allInsertMatcher = ALL_INSERT_TABLE_PATTERN.matcher(script.sql());
            while (allInsertMatcher.find()) {
                seededTables.add(allInsertMatcher.group(1).toLowerCase());
            }
        }
    }

    @Test
    @DisplayName("Contract: V900 products seed populates both image and image_url with non-NULL URLs because V9 runs before V900")
    void v900ProductsSeedIncludesImageAndImageUrlColumnsWithValues() throws IOException {
        List<MigrationScript> scripts = loadMigrationsInExecutionOrder();
        MigrationScript v900 = scripts.stream()
                .filter(s -> s.version() == 900)
                .findFirst()
                .orElseThrow();

        Matcher insertMatcher = Pattern.compile(
                "(?is)\\bINSERT\\s+(?:IGNORE\\s+)?INTO\\s+products\\s*\\(([^)]+)\\)\\s*VALUES\\s*([^;]+);"
        ).matcher(v900.sql());

        assertThat(insertMatcher.find())
                .as("V900 must contain an INSERT INTO products(...) VALUES ...; statement")
                .isTrue();

        List<String> columns = List.of(insertMatcher.group(1).toLowerCase().replaceAll("\\s+", "").split(","));
        assertThat(columns)
                .as("V900 products INSERT must populate both image and image_url since V9 executes at version 9 before V900")
                .contains("image", "image_url");

        String valuesBlock = insertMatcher.group(2);
        assertThat(valuesBlock)
                .as("V900 products INSERT values must include non-NULL https:// image URLs for all 3 demo products")
                .contains("https://images.unsplash.com/");
    }
}
