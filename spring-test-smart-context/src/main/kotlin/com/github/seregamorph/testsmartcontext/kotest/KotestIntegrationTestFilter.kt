package com.github.seregamorph.testsmartcontext.kotest

import com.github.seregamorph.testsmartcontext.IntegrationTestFilter
import io.kotest.core.spec.Spec
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.test.context.BootstrapWith
import org.springframework.test.context.ContextConfiguration

/**
 * Kotest integration Test class filter.
 * The logic of this class can be customized via
 * <pre>
 * META-INF/services/com.github.seregamorph.testsmartcontext.kotest.KotestIntegrationTestFilter
 * </pre>
 * defining subtype of this class overriding methods.
 *
 * @author Sergey Chernov
 */
open class KotestIntegrationTestFilter: IntegrationTestFilter() {

    override fun isIntegrationTest(testClass: Class<*>): Boolean {
        if (Spec::class.java.isAssignableFrom(testClass)) {
            return AnnotatedElementUtils.findMergedAnnotation(testClass, ContextConfiguration::class.java) != null
                    || AnnotatedElementUtils.findMergedAnnotation(testClass, BootstrapWith::class.java) != null
        }
        return false
    }

    companion object {
        private val instance = initInstance(KotestIntegrationTestFilter::class.java) { KotestIntegrationTestFilter() }

        @JvmStatic
        fun getInstance(): IntegrationTestFilter? {
            return instance
        }
    }
}
