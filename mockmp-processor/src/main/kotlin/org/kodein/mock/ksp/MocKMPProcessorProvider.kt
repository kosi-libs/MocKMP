package org.kodein.mock.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider


public class MocKMPProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        MocKMPProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options["org.kodein.mock.errors"] == "throw"
        )
}
