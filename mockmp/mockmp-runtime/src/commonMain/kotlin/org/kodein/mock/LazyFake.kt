package org.kodein.mock

import kotlin.concurrent.Volatile
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A `by` property delegate that lazily holds a fake.
 *
 * It runs [initializer] on the property's first read and holds the result, and is the delegate the
 * MocKMP processor uses for every property of a faked interface or abstract class that holds
 * another fake.
 *
 * This is what makes faking a self-referential type possible: an eagerly-built
 * `override val parent: Node = fakeNode()` would recurse forever, since building the fake `Node`
 * requires building its `parent`, which requires building *its* `parent`, and so on. Deferring the
 * call until something actually reads `.parent` breaks that chain — constructing the outer fake costs
 * nothing, and `node.parent.parent.parent` is only ever built as many levels deep as it is read.
 *
 * An assignment always wins over [initializer], whether it happens before or after the first read:
 * writing [value] (or the property this delegates) discards the initializer, so it never runs
 * afterwards, exactly as an eagerly-initialized `var` would behave.
 *
 * **Not thread-safe against a first read racing a write.** [initializer] and the held value are
 * [Volatile] so a fully-formed value is what any thread sees, never a half-written one — but nothing
 * makes "check, then run, then store" atomic, so a first read racing another first read (or a
 * concurrent assignment) can run [initializer] more than once, with the last write winning. A fake is
 * inert data with no side effect worth deduplicating, so this trades that guarantee for not paying
 * for a lock on every read — the same trade-off [Mocker] itself documents for its own state.
 *
 * @param initializer Builds the held value on first read, unless a value is assigned first.
 */
public class LazyFake<T>(initializer: () -> T) : ReadWriteProperty<Any?, T> {

    @Suppress("ClassName")
    private object UNINITIALIZED_VALUE

    @Volatile
    private var initializer: (() -> T)? = initializer

    @Volatile
    private var _value: Any? = UNINITIALIZED_VALUE

    /** The held value: the result of `initializer` on first read, or whatever was last assigned. */
    public var value: T
        @Suppress("UNCHECKED_CAST")
        get() {
            val initializer = this.initializer
            if (initializer != null) {
                _value = initializer()
                this.initializer = null
            }
            return _value as T
        }
        set(value) {
            _value = value
            initializer = null
        }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = this.value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { this.value = value }
}
