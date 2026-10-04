package com.github.seregamorph.testsmartcontext.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@ContextConfiguration(classes = {
    WebIntegrationTest.Configuration.class
})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "parameter = value"
})
public class WebIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void test404() {
        var entity = testRestTemplate.getForEntity("/article", String.class);
        assertEquals(404, entity.getStatusCode().value());
    }

    @Test
    public void testDatabase() {
        assertEquals(1, jdbcTemplate.queryForObject("SELECT 1", Integer.class));
    }

    public static class Configuration {

        /**
         * Spring Boot manages the container lifecycle and auto-configures the DataSource via JdbcConnectionDetails.
         */
        @Bean
        @ServiceConnection
        public PostgreSQLContainer<?> postgreSQLContainer() {
            return new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.4"));
        }
    }
}
