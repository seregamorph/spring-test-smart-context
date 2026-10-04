package com.github.seregamorph.testsmartcontext.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@ContextConfiguration(classes = {
    WebIntegrationTest.TestConfiguration.class
})
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "parameter = value"
})
@Testcontainers
public class WebIntegrationTest extends AbstractIntegrationTest {

    /**
     * The container is started before and stopped after the test class by the Testcontainers JUnit extension.
     */
    @Container
    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:18.4");

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

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

    public static class TestConfiguration {

    }
}
