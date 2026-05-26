package com.github.seregamorph.testsmartcontext.demo

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Unit1Spec : FunSpec({
    test("test") {
        TestEventTracker.trackEvent("Running ${Unit1Spec::class.java.name}.test")
        true shouldBe true
    }
})
