package com.zhisu.hotel.database;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseScriptContractTest {

    private final Path database = Path.of("..", "database").normalize();

    @Test
    void seedUsesOnlyPasswordHash() throws IOException {
        String seed = Files.readString(database.resolve("04_initial_data.sql"));

        assertThat(seed).contains("password_hash");
        assertThat(seed).doesNotContain("password = VALUES(password)");
        assertThat(seed).doesNotContain("    password,");
    }

    @Test
    void executableScriptsDoNotContainLegacyForeignKeysOrMachinePaths() throws IOException {
        try (Stream<Path> files = Files.walk(database)) {
            List<Path> scripts = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql") || path.toString().endsWith(".ps1"))
                    .toList();
            for (Path script : scripts) {
                String content = Files.readString(script);
                assertThat(content).as(script.toString()).doesNotContain("E:/Hotel-online-booking-platform");
                assertThat(content).as(script.toString()).doesNotContain("E:\\Hotel-online-booking-platform");
                assertThat(content).as(script.toString()).doesNotMatch("(?is).*REFERENCES\\s+hotel_order\\b.*");
            }
        }
    }

    @Test
    void freshInitializationRunsValidationAfterSeedData() throws IOException {
        String initializer = Files.readString(database.resolve("init_test_database.ps1"));

        assertThat(initializer.indexOf("04_initial_data.sql")).isLessThan(initializer.indexOf("validate_schema.sql"));
        assertThat(initializer).contains("migrations/full_completion.sql");
    }

    @Test
    void freshSeedPasswordsMatchDocumentedDemoCredentials() throws IOException {
        String seed = Files.readString(database.resolve("04_initial_data.sql"));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        assertThat(encoder.matches("admin123", passwordHashFor(seed, "admin"))).isTrue();
        assertThat(encoder.matches("frontdesk123", passwordHashFor(seed, "frontdesk"))).isTrue();
        assertThat(encoder.matches("guest123", passwordHashFor(seed, "guest"))).isTrue();
    }

    private String passwordHashFor(String seed, String username) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\(\\d+,\\s*'" + username + "',\\s*'([^']+)'")
                .matcher(seed);
        assertThat(matcher.find()).as("seed user " + username).isTrue();
        return matcher.group(1);
    }}