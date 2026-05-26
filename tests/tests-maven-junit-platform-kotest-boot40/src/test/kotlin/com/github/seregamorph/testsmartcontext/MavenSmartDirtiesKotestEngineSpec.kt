package com.github.seregamorph.testsmartcontext

import com.github.seregamorph.testsmartcontext.demo.*
import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request
import org.junit.platform.testkit.engine.EngineTestKit

class MavenSmartDirtiesKotestEngineSpec : FunSpec() {

    companion object {
        private const val ENGINE = "kotest"
        private lateinit var prevEngineClassOrderStateMap: Map<String,
                Map<Class<*>, SmartDirtiesTestsSupport.ClassGroupState>>
    }

    init {
        beforeSpec {
            prevEngineClassOrderStateMap = SmartDirtiesTestsSupport.setEngineClassOrderStateMap(null)
            TestEventTracker.startTracking()
        }

        afterSpec {
            TestEventTracker.stopTracking()
            SmartDirtiesTestsSupport.setEngineClassOrderStateMap(prevEngineClassOrderStateMap)
        }

        test("testSuite") {
            // to avoid confusion of duplicated test execution output
            println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
            println(">>>EngineTestKit duplicating the suite>>>")

            val events = EngineTestKit.execute(
                ENGINE, request()
                    .selectors(selectPackage("com.github.seregamorph.testsmartcontext.demo"))
                    .build()
            ).containerEvents()

            // 1 engine + 11 specs (SmartDirtiesKotestTestsSorterSpec, Unit1Spec,
            // Integration1MockBeanSpec, Integration1Spec, Integration2Spec,
            // SampleDirtiesContextBeforeClassSpec, NoBaseClass1IntegrationSpec,
            // NoBaseClass2IntegrationSpec, SampleDirtiesContextAfterClassSpec,
            // SampleIntegrationSpec, WebIntegrationSpec)
            events.assertStatistics { stats ->
                stats
                    .started(12)
                    .succeeded(12)
                    .finished(12)
                    .aborted(0)
                    .failed(0)
            }

            ArrayList(TestSmartDirtiesTestsHolder.getIntegrationTestClasses(ENGINE)) shouldBe listOf(
                Integration1MockBeanSpec::class.java,
                Integration1Spec::class.java,
                Integration2Spec::class.java,
                SampleDirtiesContextBeforeClassSpec::class.java,
                NoBaseClass1IntegrationSpec::class.java,
                NoBaseClass2IntegrationSpec::class.java,
                SampleDirtiesContextAfterClassSpec::class.java,
                SampleIntegrationSpec::class.java,
                WebIntegrationSpec::class.java
            )

            SmartDirtiesTestKitSupport.isFirstClassPerConfig(Integration1MockBeanSpec::class.java) shouldBe true
            SmartDirtiesTestKitSupport.isFirstClassPerConfig(Integration1Spec::class.java) shouldBe true
            SmartDirtiesTestKitSupport.isFirstClassPerConfig(Integration2Spec::class.java) shouldBe true
            SmartDirtiesTestKitSupport.isFirstClassPerConfig(SampleDirtiesContextBeforeClassSpec::class.java) shouldBe true
            SmartDirtiesTestKitSupport.isFirstClassPerConfig(NoBaseClass1IntegrationSpec::class.java) shouldBe false
            SmartDirtiesTestKitSupport.isFirstClassPerConfig(NoBaseClass2IntegrationSpec::class.java) shouldBe false
            SmartDirtiesTestKitSupport.isFirstClassPerConfig(SampleDirtiesContextAfterClassSpec::class.java) shouldBe false
            SmartDirtiesTestKitSupport.isFirstClassPerConfig(SampleIntegrationSpec::class.java) shouldBe true
            SmartDirtiesTestKitSupport.isFirstClassPerConfig(WebIntegrationSpec::class.java) shouldBe true

            SmartDirtiesTestKitSupport.isLastClassPerConfig(Integration1MockBeanSpec::class.java) shouldBe true
            SmartDirtiesTestKitSupport.isLastClassPerConfig(Integration1Spec::class.java) shouldBe true
            SmartDirtiesTestKitSupport.isLastClassPerConfig(Integration2Spec::class.java) shouldBe true
            SmartDirtiesTestKitSupport.isLastClassPerConfig(SampleDirtiesContextBeforeClassSpec::class.java) shouldBe false
            SmartDirtiesTestKitSupport.isLastClassPerConfig(NoBaseClass1IntegrationSpec::class.java) shouldBe false
            SmartDirtiesTestKitSupport.isLastClassPerConfig(NoBaseClass2IntegrationSpec::class.java) shouldBe false
            SmartDirtiesTestKitSupport.isLastClassPerConfig(SampleDirtiesContextAfterClassSpec::class.java) shouldBe true
            SmartDirtiesTestKitSupport.isLastClassPerConfig(SampleIntegrationSpec::class.java) shouldBe true
            SmartDirtiesTestKitSupport.isLastClassPerConfig(WebIntegrationSpec::class.java) shouldBe true

            TestEventTracker.assertConsumedEvent("Started SmartDirtiesKotestTestsSorterSpec.shouldSortMostlyAlphabeticallyAndGroupSameConfigurations")
            TestEventTracker.assertConsumedEvent("Finished SmartDirtiesKotestTestsSorterSpec.shouldSortMostlyAlphabeticallyAndGroupSameConfigurations")
            TestEventTracker.assertConsumedEvent("Running ${com.github.seregamorph.testsmartcontext.demo.Unit1Spec::class.java.name}.test")
            TestEventTracker.assertConsumedEvent("Creating context for ${Integration1MockBeanSpec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Created context for ${Integration1MockBeanSpec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Running ${Integration1MockBeanSpec::class.java.simpleName}.test")
            TestEventTracker.assertConsumedEvent("Auto-destroying context for ${Integration1MockBeanSpec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Creating context for ${Integration1Spec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Created context for ${Integration1Spec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Running ${Integration1Spec::class.java.simpleName}.test")
            TestEventTracker.assertConsumedEvent("Auto-destroying context for ${Integration1Spec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Creating context for ${Integration2Spec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Created context for ${Integration2Spec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Running ${Integration2Spec::class.java.simpleName}.test")
            TestEventTracker.assertConsumedEvent("Auto-destroying context for ${Integration2Spec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Creating context for ${SampleDirtiesContextBeforeClassSpec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Created context for ${SampleDirtiesContextBeforeClassSpec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Running ${SampleDirtiesContextBeforeClassSpec::class.java.simpleName}.test")
            TestEventTracker.assertConsumedEvent("AfterAll ${SampleDirtiesContextBeforeClassSpec::class.java.simpleName}")
            TestEventTracker.assertConsumedEvent("Running ${NoBaseClass1IntegrationSpec::class.java.simpleName}.test")
            TestEventTracker.assertConsumedEvent("AfterAll ${NoBaseClass1IntegrationSpec::class.java.simpleName}")
            TestEventTracker.assertConsumedEvent("Running ${NoBaseClass2IntegrationSpec::class.java.simpleName}.test")
            TestEventTracker.assertConsumedEvent("AfterAll ${NoBaseClass2IntegrationSpec::class.java.simpleName}")
            TestEventTracker.assertConsumedEvent("Running ${SampleDirtiesContextAfterClassSpec::class.java.simpleName}.test")
            TestEventTracker.assertConsumedEvent("AfterAll ${SampleDirtiesContextAfterClassSpec::class.java.simpleName}")
            TestEventTracker.assertConsumedEvent("Destroying context for ${SampleDirtiesContextAfterClassSpec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Creating context for ${SampleIntegrationSpec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Created context for ${SampleIntegrationSpec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Auto-destroying context for ${SampleIntegrationSpec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Creating context for ${WebIntegrationSpec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Created context for ${WebIntegrationSpec::class.java.name}")
            TestEventTracker.assertConsumedEvent("Auto-destroying context for ${WebIntegrationSpec::class.java.name}")
            TestEventTracker.assertEmpty()

            println("<<<EngineTestKit duplicating the suite<<<")
            println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        }
    }
}
