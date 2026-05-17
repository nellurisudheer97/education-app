package com.eduapp.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Normalises PostgreSQL connection URLs supplied by Railway (and similar PaaS
 * platforms) so that the JDBC driver can accept them.
 *
 * <p>Railway exposes the database connection string via {@code DATABASE_URL} in
 * the form {@code postgresql://user:pass@host:port/db}.  The PostgreSQL JDBC
 * driver requires the URL to start with {@code jdbc:postgresql://}.  This
 * post-processor rewrites the relevant environment variables before Spring
 * resolves {@code application.yml} property placeholders, so no manual
 * {@code SPRING_DATASOURCE_URL} override is needed on Railway.</p>
 *
 * <p>Variables inspected (in priority order as declared in application.yml):
 * {@code SPRING_DATASOURCE_URL}, {@code DB_URL}, {@code DATABASE_URL}.</p>
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String JDBC_PREFIX = "jdbc:";
    private static final String PG_SCHEME   = "postgresql://";

    /** Property source name – placed at highest priority so it wins. */
    private static final String SOURCE_NAME = "railwayDatabaseUrlNormalizer";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {

        Map<String, Object> normalized = new HashMap<>();

        for (String var : new String[]{"SPRING_DATASOURCE_URL", "DB_URL", "DATABASE_URL"}) {
            String value = environment.getProperty(var);
            if (value != null && value.startsWith(PG_SCHEME)) {
                normalized.put(var, JDBC_PREFIX + value);
            }
        }

        if (!normalized.isEmpty()) {
            // Insert at position 0 so these values take precedence over the
            // OS environment property source that supplied the originals.
            environment.getPropertySources()
                       .addFirst(new MapPropertySource(SOURCE_NAME, normalized));
        }
    }
}
