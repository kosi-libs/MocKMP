package foo


@ConsistentCopyVisibility
data class Amount private constructor(
    private val inCents: Int,
) {
    val euros: Int get() = inCents / 100
    val cents: Int get() = inCents % 100

    companion object {
        fun of(euros: Int, cents: Int): Amount {
            require(euros >= 0 && cents >= 0 && cents < 100) { "Invalid amount" }
            return Amount(euros * 100 + cents)
        }
    }
}

data class MoneyTransfer(
    val from: String,
    val to: String,
    val amount: Amount,
)

interface MoneyTransferService {
    fun execute(moneyTransfer: MoneyTransfer)
}
