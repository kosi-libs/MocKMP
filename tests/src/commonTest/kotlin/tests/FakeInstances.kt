package tests

import kotlinx.datetime.Instant
import org.kodein.mock.FakeProvider


@FakeProvider
internal fun provideFakeInstant() = Instant.fromEpochSeconds(0)
