package com.github.seregamorph.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(classes = {
    SampleIntegrationTest.Configuration.class
})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "parameter = value"
})
public class SampleIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test404() throws Exception {
        mockMvc.perform(get("/article"))
            .andExpect(status().isNotFound());
    }

    public static class Configuration {

        @Bean
        public MockMvc mockMvc(WebApplicationContext webApplicationContext) {
            DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(webApplicationContext);
            return builder.build();
        }
    }
}
