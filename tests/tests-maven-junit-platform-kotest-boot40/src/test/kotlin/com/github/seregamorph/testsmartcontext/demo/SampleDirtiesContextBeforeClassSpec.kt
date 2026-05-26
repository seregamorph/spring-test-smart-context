package com.github.seregamorph.testsmartcontext.demo

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(classes = [SampleIntegrationSpec.Configuration::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class SampleDirtiesContextBeforeClassSpec : SpringSpec() {

    init {
        afterSpec {
            TestEventTracker.trackEvent("AfterAll ${SampleDirtiesContextBeforeClassSpec::class.java.simpleName}")
        }

        test("test") {
            TestEventTracker.trackEvent("Running ${SampleDirtiesContextBeforeClassSpec::class.java.simpleName}.test")
        }
    }
}
