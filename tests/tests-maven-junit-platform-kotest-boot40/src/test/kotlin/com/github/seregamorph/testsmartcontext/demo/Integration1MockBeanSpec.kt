package com.github.seregamorph.testsmartcontext.demo

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.web.WebAppConfiguration

@WebAppConfiguration
@ContextConfiguration(classes = [Integration1Spec.Configuration::class])
class Integration1MockBeanSpec : SpringSpec() {

    @Autowired
    lateinit var sampleService: SampleService

    @MockitoBean
    lateinit var sampleBean: SampleBean

    init {
        beforeTest {
            reset(sampleBean)
            whenever(sampleBean.value).thenReturn("mock")
        }

        test("test") {
            sampleBean.value shouldBe "mock"
            TestEventTracker.trackEvent("Running ${Integration1MockBeanSpec::class.java.simpleName}.test")
        }
    }
}
