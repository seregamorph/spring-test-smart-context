package com.github.seregamorph.testsmartcontext.demo

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration

@WebAppConfiguration
@ContextConfiguration(classes = [Integration1Spec.Configuration::class])
class Integration1Spec : SpringSpec() {

    @Autowired
    lateinit var sampleBean: SampleBean

    init {
        beforeTest {
            println("Running ${Integration1Spec::class.java}")
        }

        test("test") {
            println("Integration1Spec.test $sampleBean")
            sampleBean.value shouldBe "default"
            TestEventTracker.trackEvent("Running ${Integration1Spec::class.java.simpleName}.test")
        }
    }

    @Import(SampleBean::class, SampleService::class)
    class Configuration
}
