package com.github.seregamorph.testsmartcontext.demo

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(classes = [SampleIntegrationSpec.Configuration::class])
@DirtiesContext
class SampleDirtiesContextAfterClassSpec : SpringSpec() {

    init {
        afterSpec {
            TestEventTracker.trackEvent("AfterAll ${SampleDirtiesContextAfterClassSpec::class.java.simpleName}")
        }

        test("test") {
            TestEventTracker.trackEvent("Running ${SampleDirtiesContextAfterClassSpec::class.java.simpleName}.test")
        }
    }
}
