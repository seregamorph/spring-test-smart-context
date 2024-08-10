package com.github.seregamorph.testsmartcontext.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.seregamorph.testsmartcontext.testng.AbstractTestNGSpringIntegrationTest;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(classes = {
    SampleIT.Configuration.class
})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "parameter = value"
})
public class SampleIT extends AbstractTestNGSpringIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeMethod
    public void beforeMethod() {
        System.out.println("Running " + getClass());
    }

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
