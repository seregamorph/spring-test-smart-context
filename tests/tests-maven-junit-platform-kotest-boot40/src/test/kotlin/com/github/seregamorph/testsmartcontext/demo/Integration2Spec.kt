package com.github.seregamorph.testsmartcontext.demo

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [Integration2Spec.Configuration::class])
class Integration2Spec : SpringSpec() {

    init {
        beforeTest {
            println("Running ${Integration2Spec::class.java}")
        }

        test("test") {
            TestEventTracker.trackEvent("Running ${Integration2Spec::class.java.simpleName}.test")
        }
    }

    class Configuration
}
