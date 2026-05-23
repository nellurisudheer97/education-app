package com.eduapp.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Railway PostgreSQL environment variables into Spring datasource
 * properties before Hikari and Flyway are initialized.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE_NAME = "railwayDatabaseUrlNormalizer";
    private static final String POSTGRESQL_SCHEME = "postgresql://";
    private static final String POSTGRES_SCHEME = "postgres://";
    private static final String JDBC_POSTGRESQL_SCHEME = "jdbc:postgresql://";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {

        Map<String, Object> normalized = new HashMap<>();
        String databaseUrl = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("DB_URL"),
                environment.getProperty("DATABASE_URL")
        );

        if (isJdbcPostgresUrl(databaseUrl)) {
            normalized.put("spring.datasource.url", databaseUrl);
        } else if (isPostgresUrl(databaseUrl)) {
            normalizeDatabaseUrl(databaseUrl, normalized);
        } else if (hasRailwayPostgresParts(environment)) {
            normalizeRailwayParts(environment, normalized);
        }

        if (!normalized.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, normalized));
        }
    }

    private boolean isJdbcPostgresUrl(String value) {
        return value != null && value.startsWith(JDBC_POSTGRESQL_SCHEME);
    }

    private boolean isPostgresUrl(String value) {
        return value != null && (value.startsWith(POSTGRESQL_SCHEME) || value.startsWith(POSTGRES_SCHEME));
    }

    private boolean hasRailwayPostgresParts(ConfigurableEnvironment environment) {
        return firstNonBlank(environment.getProperty("PGHOST"), environment.getProperty("POSTGRES_HOST")) != null
                && firstNonBlank(environment.getProperty("PGDATABASE"), environment.getProperty("POSTGRES_DB")) != null;
    }

    private void normalizeDatabaseUrl(String databaseUrl, Map<String, Object> normalized) {
        URI uri = URI.create(databaseUrl.replaceFirst("^postgres://", POSTGRESQL_SCHEME));
        String jdbcUrl = JDBC_POSTGRESQL_SCHEME + uri.getHost();

        if (uri.getPort() != -1) {
            jdbcUrl += ":" + uri.getPort();
        }
        jdbcUrl += uri.getPath();
        if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
            jdbcUrl += "?" + uri.getQuery();
        }

        normalized.put("spring.datasource.url", jdbcUrl);

        String userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            String[] credentials = userInfo.split(":", 2);
            normalized.put("spring.datasource.username", decode(credentials[0]));
            if (credentials.length > 1) {
                normalized.put("spring.datasource.password", decode(credentials[1]));
            }
        }
    }

    private void normalizeRailwayParts(ConfigurableEnvironment environment, Map<String, Object> normalized) {
        String host = firstNonBlank(environment.getProperty("PGHOST"), environment.getProperty("POSTGRES_HOST"));
        String port = firstNonBlank(environment.getProperty("PGPORT"), environment.getProperty("POSTGRES_PORT"), "5432");
        String database = firstNonBlank(environment.getProperty("PGDATABASE"), environment.getProperty("POSTGRES_DB"));
        String username = firstNonBlank(environment.getProperty("PGUSER"), environment.getProperty("POSTGRES_USER"));
        String password = firstNonBlank(environment.getProperty("PGPASSWORD"), environment.getProperty("POSTGRES_PASSWORD"));

        normalized.put("spring.datasource.url", JDBC_POSTGRESQL_SCHEME + host + ":" + port + "/" + database);
        putIfPresent(normalized, "spring.datasource.username", username);
        putIfPresent(normalized, "spring.datasource.password", password);
    }

    private void putIfPresent(Map<String, Object> normalized, String key, String value) {
        if (value != null && !value.isBlank()) {
            normalized.put(key, value);
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank() && !value.startsWith("${{")) {
                return value;
            }
        }
        return null;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
