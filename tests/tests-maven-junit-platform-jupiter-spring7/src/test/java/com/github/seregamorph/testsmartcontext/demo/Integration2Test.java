package com.github.seregamorph.testsmartcontext.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.seregamorph.testsmartcontext.jdbc.LateInitDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.postgresql.PostgreSQLContainer;

@ContextConfiguration(classes = {
    Integration2Test.Configuration.class
})
public class Integration2Test extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void test() {
        System.out.println("Integration2Test.test");
        assertEquals(1, jdbcTemplate.queryForObject("SELECT 1", Integer.class));
    }

    @Nested
    public class NestedTest {

        @Test
        public void nested() {
            System.out.println("Integration2Test.NestedTest.test");
        }
    }

    public static class Configuration {

        /**
         * The container is not started here, it's done on demand by {@link LateInitDataSource}.
         * It's stopped on context destroy via inferred close method.
         */
        @Bean
        public PostgreSQLContainer postgreSQLContainer() {
            return new PostgreSQLContainer("postgres:18.4");
        }

        @Bean
        public DataSource dataSource(PostgreSQLContainer container) {
            // the JDBC url is not known yet, because container is not running
            return new LateInitDataSource("postgres", () -> {
                container.start();
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(container.getJdbcUrl());
                config.setUsername(container.getUsername());
                config.setPassword(container.getPassword());
                return new HikariDataSource(config);
            });
        }

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }
}
