package com.github.seregamorph.testsmartcontext.demo

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [SampleIntegrationSpec.Configuration::class])
class NoBaseClass1IntegrationSpec : SpringSpec() {

    init {
        afterSpec {
            TestEventTracker.trackEvent("AfterAll ${NoBaseClass1IntegrationSpec::class.java.simpleName}")
        }

        test("test") {
            TestEventTracker.trackEvent("Running ${NoBaseClass1IntegrationSpec::class.java.simpleName}.test")
        }
    }
}
