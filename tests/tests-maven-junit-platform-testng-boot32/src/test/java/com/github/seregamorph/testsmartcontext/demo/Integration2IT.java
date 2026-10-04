package com.github.seregamorph.testsmartcontext.demo;

import static org.testng.Assert.assertEquals;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {
    Integration2IT.Configuration.class
})
public class Integration2IT extends AbstractIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void test() {
        assertEquals(jdbcTemplate.queryForObject("SELECT 1", Integer.class), Integer.valueOf(1));
    }

    public static class Configuration {

        /**
         * The container is eagerly started on bean initialization and stopped on context destroy.
         */
        @Bean(initMethod = "start", destroyMethod = "stop")
        public PostgreSQLContainer<?> postgreSQLContainer() {
            return new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.4"));
        }

        @Bean
        public HikariDataSource dataSource(PostgreSQLContainer<?> container) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(container.getJdbcUrl());
            config.setUsername(container.getUsername());
            config.setPassword(container.getPassword());
            return new HikariDataSource(config);
        }

        @Bean
        public JdbcTemplate jdbcTemplate(HikariDataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }
}
