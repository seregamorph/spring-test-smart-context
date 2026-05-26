package com.github.seregamorph.testsmartcontext.demo

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [SampleIntegrationSpec.Configuration::class])
class NoBaseClass2IntegrationSpec : SpringSpec() {

    init {
        afterSpec {
            TestEventTracker.trackEvent("AfterAll ${NoBaseClass2IntegrationSpec::class.java.simpleName}")
        }

        test("test") {
            TestEventTracker.trackEvent("Running ${NoBaseClass2IntegrationSpec::class.java.simpleName}.test")
        }
    }
}
