package com.github.seregamorph.testsmartcontext.demo

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension

abstract class SpringSpec : FunSpec() {
    override fun extensions() = listOf(SpringExtension)
}
