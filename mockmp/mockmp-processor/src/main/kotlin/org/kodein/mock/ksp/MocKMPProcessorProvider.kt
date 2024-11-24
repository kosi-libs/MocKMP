package org.kodein.mock.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider


public class MocKMPProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        MocKMPProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            throwErrors = environment.options["org.kodein.mock.errors"] == "throw",
            public = environment.options["org.kodein.mock.visibility"] == "public",
            accessorsPackage = environment.options["org.kodein.mock.package"] ?: "org.kodein.mock.generated",
            multiplatform = environment.options["org.kodein.mock.multiplatform"]?.let { it == "true" } ?: true,
        )
}
