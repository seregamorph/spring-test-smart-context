package com.github.seregamorph.testsmartcontext.demo

import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter
import com.github.seregamorph.testsmartcontext.TestClassExtractor
import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SmartDirtiesKotestTestsSorterSpec : FunSpec({
    test("shouldSortMostlyAlphabeticallyAndGroupSameConfigurations") {
        TestEventTracker.trackEvent(
            "Started SmartDirtiesKotestTestsSorterSpec.shouldSortMostlyAlphabeticallyAndGroupSameConfigurations"
        )
        val sorter = SmartDirtiesTestsSorter.getInstance()
        val testItems = mutableListOf(
            Integration2Spec::class.java,
            SampleIntegrationSpec::class.java,
            Integration1Spec::class.java,
            Integration1MockBeanSpec::class.java,
            Unit1Spec::class.java,
            SampleDirtiesContextAfterClassSpec::class.java,
            SampleDirtiesContextBeforeClassSpec::class.java,
            NoBaseClass2IntegrationSpec::class.java,
            NoBaseClass1IntegrationSpec::class.java,
            SmartDirtiesKotestTestsSorterSpec::class.java
        )
        val itClassesLists = sorter.sort(testItems, TestClassExtractor.ofClass { testClass -> testClass })

        testItems shouldBe listOf(
            // UTs
            SmartDirtiesKotestTestsSorterSpec::class.java,
            Unit1Spec::class.java,
            // IT 1 - Integration1MockBeanSpec (unique config due to MockitoBean)
            Integration1MockBeanSpec::class.java,
            // IT 2 - Integration1Spec config
            Integration1Spec::class.java,
            // IT 3 - Integration2Spec config
            Integration2Spec::class.java,
            // IT 4 - SampleIntegrationSpec.Configuration via @SpringBootTest
            SampleDirtiesContextBeforeClassSpec::class.java,
            NoBaseClass1IntegrationSpec::class.java,
            NoBaseClass2IntegrationSpec::class.java,
            SampleDirtiesContextAfterClassSpec::class.java,
            // IT 5 - SampleIntegrationSpec config
            SampleIntegrationSpec::class.java
        )

        itClassesLists.sortedConfigToTests shouldBe listOf(
            listOf(Integration1MockBeanSpec::class.java),
            listOf(Integration1Spec::class.java),
            listOf(Integration2Spec::class.java),
            listOf(
                SampleDirtiesContextBeforeClassSpec::class.java,
                NoBaseClass1IntegrationSpec::class.java,
                NoBaseClass2IntegrationSpec::class.java,
                SampleDirtiesContextAfterClassSpec::class.java
            ),
            listOf(SampleIntegrationSpec::class.java)
        )

        TestEventTracker.trackEvent(
            "Finished SmartDirtiesKotestTestsSorterSpec.shouldSortMostlyAlphabeticallyAndGroupSameConfigurations"
        )
    }
})
