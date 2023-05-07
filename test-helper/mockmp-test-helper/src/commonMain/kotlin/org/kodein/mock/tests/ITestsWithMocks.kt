package org.kodein.mock.tests

import org.kodein.mock.ArgConstraintsBuilder
import org.kodein.mock.Mocker
import org.kodein.mock.VerificationBuilder
import kotlin.test.BeforeTest


public interface ITestsWithMocks {

    public class State {
        private val defs = ArrayList<TestsWithMocks.Deferred<*>>()
        public val mocker: Mocker = Mocker()

        public fun initDeferred() {
            defs.forEach { it.init() }
        }

        public fun clearDeferred() {
            defs.forEach { it.clear() }
        }

        public fun <T : Any> withMocks(create: () -> T): TestsWithMocks.Deferred<T> = TestsWithMocks.Deferred(create).also { defs.add(it) }
    }

    public val mocksState: State

    public val mocker: Mocker get() = mocksState.mocker

    public fun initDeferred(): Unit = mocksState.initDeferred()

    public fun clearDeferred(): Unit = mocksState.clearDeferred()

    public fun <T : Any> withMocks(create: () -> T): TestsWithMocks.Deferred<T> = mocksState.withMocks(create)

    public fun <T> every(block: ArgConstraintsBuilder.() -> T) : Mocker.Every<T> =
        mocker.every(block)
    public suspend fun <T> everySuspending(block: suspend ArgConstraintsBuilder.() -> T) : Mocker.EverySuspend<T> =
        mocker.everySuspending(block)

    public fun verify(exhaustive: Boolean = true, inOrder: Boolean = true, block: VerificationBuilder.() -> Unit): Unit =
        mocker.verify(exhaustive = exhaustive, inOrder = inOrder, block)
    public suspend fun verifyWithSuspend(exhaustive: Boolean = true, inOrder: Boolean = true, block: suspend VerificationBuilder.() -> Unit): Unit =
        mocker.verifyWithSuspend(exhaustive = exhaustive, inOrder = inOrder, block)

    @BeforeTest
    public fun injectMocksBeforeTest() {
        mocker.reset()
        setUpMocks()
        initDeferred()
        initMocksBeforeTest()
    }

    public fun setUpMocks()

    public fun initMocksBeforeTest() {}

}
