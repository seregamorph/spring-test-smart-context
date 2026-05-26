package com.github.seregamorph.testsmartcontext.demo

import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@ContextConfiguration(classes = [WebIntegrationSpec.TestConfiguration::class])
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@TestPropertySource(properties = ["parameter = value"])
class WebIntegrationSpec : SpringSpec() {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    init {
        beforeTest {
            println("Running ${WebIntegrationSpec::class.java}")
        }

        test("test404") {
            val entity = testRestTemplate.getForEntity("/article", String::class.java)
            entity.statusCode.value() shouldBe 404
        }
    }

    class TestConfiguration
}
