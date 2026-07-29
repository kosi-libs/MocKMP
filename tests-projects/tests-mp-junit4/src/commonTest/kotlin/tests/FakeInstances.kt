package tests

import org.kodein.mock.FakeProvider
import kotlin.time.Instant


@FakeProvider
internal fun provideFakeInstant() = Instant.fromEpochSeconds(0)
