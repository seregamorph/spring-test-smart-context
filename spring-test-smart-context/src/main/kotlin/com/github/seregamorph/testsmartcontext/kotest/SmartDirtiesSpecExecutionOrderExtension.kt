package com.github.seregamorph.testsmartcontext.kotest

import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter
import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSupport
import com.github.seregamorph.testsmartcontext.TestClassExtractor
import com.github.seregamorph.testsmartcontext.TestSortResult
import io.kotest.core.annotation.AutoScan
import io.kotest.core.extensions.Extension
import io.kotest.core.extensions.SpecExecutionOrderExtension
import io.kotest.core.spec.SpecRef

/**
 * Auto-discovered Kotest {@link SpecExecutionOrderExtension} which reorders and groups the integration test
 * classes per their configuration. Also stores information about last integration class per configuration,
 * which is used by {@link com.github.seregamorph.testsmartcontext.SmartDirtiesContextTestExecutionListener}.
 * <p>
 * For JUnit Jupiter classes - see {@link com.github.seregamorph.testsmartcontext.jupiter.SmartDirtiesClassOrderer},
 * for TestNG test classes - see {@link com.github.seregamorph.testsmartcontext.testng.SmartDirtiesSuiteListener}, for
 * JUnit 4 test classes - see {@link com.github.seregamorph.testsmartcontext.SmartDirtiesPostDiscoveryFilter}.
 *
 * @author Sergey Chernov
 */
@AutoScan
class SmartDirtiesSpecExecutionOrderExtension : SmartDirtiesTestsSupport(), SpecExecutionOrderExtension, Extension {

    override fun sort(specs: List<SpecRef>): List<SpecRef> {
        if (specs.isEmpty()) {
            return specs
        }

        val uniqueClasses = mutableSetOf<Class<*>?>()
        for (specRef in specs) {
            val testClass = specRef.kclass.java
            uniqueClasses.add(testClass)
        }

        if (uniqueClasses.size == 1) {
            if (classOrderStateMapSize(ENGINE_KOTEST) <= 1) {
                val testClass = specs[0].kclass.java
                setTestClassesLists(
                    ENGINE_KOTEST,
                    TestSortResult.singletonList(testClass)
                )
            }
            return specs
        }

        val sorter = SmartDirtiesTestsSorter.getInstance()
        val testClassesLists: TestSortResult
        val sortedSpecs = ArrayList<SpecRef>(specs)
        try {
            testClassesLists = sorter.sort(sortedSpecs, TestClassExtractor.ofClass { it.kclass.java })
        } catch (e: Throwable) {
            setFailureCause(e)
            throw e
        }
        setTestClassesLists(ENGINE_KOTEST, testClassesLists)
        return sortedSpecs
    }
}
