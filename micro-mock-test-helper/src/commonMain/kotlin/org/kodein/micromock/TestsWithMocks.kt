package org.kodein.micromock

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

    public fun initDeferred() {
        defs.forEach { it.init() }
    }

    public fun clearDeferred() {
        defs.forEach { it.clear() }
    }

    public fun <T> withMocks(create: () -> T): Deferred<T> = Deferred(create)

    @BeforeTest
    public fun setUpMocksBeforeTest() {
        mocker.reset()
        @Suppress("UNCHECKED_CAST")
        setUpMocks()
        initDeferred()
    }

    protected abstract fun setUpMocks()

    protected fun <T> every(block: ArgConstraintsBuilder.() -> T) : Mocker.Every<T> = mocker.every(block)
    public fun verify(exhaustive: Boolean = true, inOrder: Boolean = true, block: ArgConstraintsBuilder.() -> Unit): Unit = mocker.verify(exhaustive = exhaustive, inOrder = inOrder, block)
}
