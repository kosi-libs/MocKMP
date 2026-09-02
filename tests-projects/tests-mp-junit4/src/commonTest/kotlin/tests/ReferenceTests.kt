package tests

import data.NeverTouched
import data.Processor
import foo.Amount
import foo.MoneyTransfer
import foo.MoneyTransferService
import org.kodein.mock.Mock
import org.kodein.mock.generated.injectMocks
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class ReferenceTests : TestsWithMocks() {

    @Mock
    lateinit var processor: Processor<NeverTouched>

    @Mock
    lateinit var moneyTransferService: MoneyTransferService

    override fun setUpMocks() {
        mocker.injectMocks(this)
        mocker.useReference(Amount.of(0, 0))
    }

    @Test
    fun testNeverTouchedIsPlaceholdered() {
        mocker.every { processor.process(isAny()) } returns Unit
        processor.process(NeverTouched.Instance)
        mocker.verify { processor.process(isAny()) }
    }

    @Test
    fun testMoneyTransferServiceIsPlaceholdered() {
        mocker.every { moneyTransferService.execute(isAny()) } returns Unit
        moneyTransferService.execute(MoneyTransfer(from = "me", to = "you", amount = Amount.of(7, 99)))
        mocker.verify { moneyTransferService.execute(isAny()) }
    }
}