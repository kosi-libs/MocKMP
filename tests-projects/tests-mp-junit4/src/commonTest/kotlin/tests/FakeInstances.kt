package tests

import org.kodein.mock.FakeProvider
import kotlin.time.Instant


@FakeProvider
internal fun provideFakeInstant() = Instant.fromEpochSeconds(0)

// Not a workaround: MocKMP does generate this one on its own — kotlin.Exception resolves through
// its JVM typealias to java.lang.Exception, and the fake lands in fake.java.lang (see
// KSName.isProtectedPackage). It is here to keep a @FakeProvider for a JDK type under test, and so
// the faked Exception is one known instance rather than a fresh one per call.
@FakeProvider
internal fun provideFakeException() = Exception()
