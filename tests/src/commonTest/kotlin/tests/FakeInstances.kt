package tests

import data.Date
import kotlinx.datetime.Instant
import org.kodein.mock.FakeProvider


@FakeProvider
internal fun provideFakeInstant() = Instant.fromEpochSeconds(0)

@FakeProvider
internal fun provideFakeDate() = Date()