package tests

import data.NeverTouched
import data.Processor
import org.kodein.mock.Mock
import org.kodein.mock.generated.injectMocks
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class ReferenceTests : TestsWithMocks() {

    @Mock
    lateinit var processor: Processor<NeverTouched>

    override fun setUpMocks() = mocker.injectMocks(this)

    @Test
    fun testNeverTouchedIsPlaceholdered() {
        mocker.every { processor.process(isAny()) } returns Unit
        processor.process(NeverTouched.Instance)
        mocker.verify { processor.process(isAny()) }
    }
}