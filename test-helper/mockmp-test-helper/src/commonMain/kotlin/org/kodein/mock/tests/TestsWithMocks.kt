package org.kodein.mock.tests

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


public abstract class TestsWithMocks : ITestsWithMocks {

    public class Deferred<T : Any> internal constructor(private val create: () -> T) : ReadWriteProperty<Any, T> {
        private var value: T? = null
        internal fun init() {
            if (value == null) value = create()
        }
        internal fun clear() {
            value = null
        }
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            init()
            return value ?: error("Null value")
        }
        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            this.value = value
        }
    }

    final override val mocksState: ITestsWithMocks.State = ITestsWithMocks.State()
}
