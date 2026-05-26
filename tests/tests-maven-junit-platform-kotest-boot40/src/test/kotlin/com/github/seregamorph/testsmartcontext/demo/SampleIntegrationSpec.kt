package com.github.seregamorph.testsmartcontext.demo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(classes = [SampleIntegrationSpec.Configuration::class])
@ActiveProfiles("test")
@TestPropertySource(properties = ["parameter = value"])
class SampleIntegrationSpec : SpringSpec() {

    @Autowired
    lateinit var mockMvc: MockMvc

    init {
        beforeTest {
            println("Running ${SampleIntegrationSpec::class.java}")
        }

        test("test404") {
            mockMvc.perform(get("/article"))
                .andExpect(status().isNotFound())
        }
    }

    class Configuration {
        @Bean
        fun mockMvc(webApplicationContext: WebApplicationContext): MockMvc {
            return MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        }
    }
}
