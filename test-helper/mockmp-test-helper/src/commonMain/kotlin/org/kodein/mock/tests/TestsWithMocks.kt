package org.kodein.mock.tests

import org.kodein.mock.ArgConstraintsBuilder
import org.kodein.mock.Mocker
import org.kodein.mock.VerificationBuilder
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.test.BeforeTest


public abstract class TestsWithMocks {

    public class Deferred<T>(private val create: () -> T) : ReadWriteProperty<Any, T> {
        private var value: T? = null
        internal fun init() {
            if (value == null) value = create()
        }
        internal fun clear() {
            value = null
        }
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            init()
            return value!!
        }
        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            this.value = value
        }
    }

    private val defs = ArrayList<Deferred<*>>()

    public val mocker: Mocker = Mocker()

    protected fun initDeferred() {
        defs.forEach { it.init() }
    }

    protected fun clearDeferred() {
        defs.forEach { it.clear() }
    }

    protected fun <T> withMocks(create: () -> T): Deferred<T> = Deferred(create)

    @BeforeTest
    public fun setUpMocksBeforeTest() {
        mocker.reset()
        setUpMocks()
        initDeferred()
    }

    protected abstract fun setUpMocks()

    public fun <T> every(block: ArgConstraintsBuilder.() -> T) : Mocker.Every<T> =
        mocker.every(block)
    public suspend fun <T> everySuspending(block: suspend ArgConstraintsBuilder.() -> T) : Mocker.EverySuspend<T> =
        mocker.everySuspending(block)

    public fun verify(exhaustive: Boolean = true, inOrder: Boolean = true, block: VerificationBuilder.() -> Unit): Unit =
        mocker.verify(exhaustive = exhaustive, inOrder = inOrder, block)
    public suspend fun verifyWithSuspend(exhaustive: Boolean = true, inOrder: Boolean = true, block: suspend VerificationBuilder.() -> Unit): Unit =
        mocker.verifyWithSuspend(exhaustive = exhaustive, inOrder = inOrder, block)
}
