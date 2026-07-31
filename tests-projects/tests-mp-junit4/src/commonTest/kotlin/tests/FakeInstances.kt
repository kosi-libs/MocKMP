package tests

import org.kodein.mock.FakeProvider
import kotlin.time.Instant


@FakeProvider
internal fun provideFakeInstant() = Instant.fromEpochSeconds(0)

// kotlin.Exception is a typealias for java.lang.Exception on the JVM target, which MocKMP does
// not resolve when generating fakes (see MocKMPProcessor.addFake) — provide it explicitly.
@FakeProvider
internal fun provideFakeException() = Exception()
