package org.kodein.mock

import kotlin.reflect.KClass


internal class References {

    /**
     * Set by [Mocker.registerPlaceholderProvider], called automatically by every generated
     * `MockXxx` class's constructor. Supplies a real, KSP-generated instance for a type that has
     * no builtin and no user-registered reference, replacing the unsafe-cast placeholder this
     * runtime used to construct itself.
     */
    internal var placeholderProvider: ((KClass<*>) -> Any)? = null

    /** Everything given to [Mocker.useReference]. Per-test state, dropped by [reset]. */
    private val references = ArrayList<Any>()

    /**
     * Every reference resolved so far, so that a repeated `isAny<T>()` within one test keeps handing
     * back the same instance. Also where [addReference] indexes a user reference by its exact class.
     * Per-test state, dropped by [reset] — the placeholders it holds are `MockXxx` instances built by
     * the generated provider against a throwaway [Mocker], which have no business outliving the test
     * that caused them to be created.
     */
    private val cache = HashMap<KClass<*>, Any>()

    /** Immutable seed: numeric placeholders. Kept out of [cache] so that [reset] cannot drop them. */
    @Suppress("RemoveRedundantCallsOfConversionMethods")
    private val primitives = hashMapOf<KClass<*>, Any>(
        Boolean::class to false,
        UByte::class to 0.toUByte(),
        Byte::class to 0.toByte(),
        UShort::class to 0.toUShort(),
        Short::class to 0.toShort(),
        Char::class to 0.toChar(),
        UInt::class to 0.toUInt(),
        Int::class to 0,
        Float::class to 0.toFloat(),
        ULong::class to 0.toULong(),
        Long::class to 0.toLong(),
        Double::class to 0.toDouble()
    )

    /**
     * Stdlib types whose placeholder is a plain value or an empty collection, needing nothing from
     * the user's project — the runtime counterpart of the KSP processor's own `builtins` map, which
     * emits these same types as [placeholderProvider] branches. Keep the two in sync.
     *
     * They live here rather than only in generated code because [placeholderProvider] is installed
     * by a generated `MockXxx` constructor: a [Mocker] that only ever mocks functional types never
     * gets one, and could otherwise not even produce a `""` for `isAny<String>()`.
     *
     * `kotlin.Array` is deliberately absent: its placeholder is component-type-specific on the JVM,
     * which only the processor can know.
     */
    private val defaults = hashMapOf<KClass<*>, Any>(
        Unit::class to Unit,
        String::class to "",
        List::class to emptyList<Any?>(),
        ArrayList::class to ArrayList<Any?>(),
        ArrayDeque::class to ArrayDeque<Any?>(),
        Set::class to emptySet<Any?>(),
        HashSet::class to HashSet<Any?>(),
        LinkedHashSet::class to LinkedHashSet<Any?>(),
        Map::class to emptyMap<Any?, Any?>(),
        HashMap::class to HashMap<Any?, Any?>(),
        LinkedHashMap::class to LinkedHashMap<Any?, Any?>(),
    )

    /**
     * Drops everything a single test put here, so that [Mocker.reset] hands back a genuinely clean
     * mocker.
     *
     * [placeholderProvider] deliberately survives: it points at a generated, stateless top-level
     * function, so there is nothing about it to go stale — and clearing it would break building the
     * mocks once and calling [Mocker.reset] per test, since nothing would re-register it and every
     * `isAny<T>()` would then fail.
     */
    fun reset() {
        references.clear()
        cache.clear()
    }

    fun addReference(r: Any) {
        references.add(r)
        cache[r::class] = r
    }

    fun tryGetReference(cls: KClass<*>): Any? {
        // [cache] before [primitives] so that a useReference for a primitive type still wins, as it
        // did when the two shared one map.
        cache[cls]?.let { return it }
        primitives[cls]?.let { return it }
        var ref: Any? = null
        references.forEach {
            if (cls.isInstance(it)) ref = it
        }
        // [defaults] sits exactly where the generated provider would have answered for these types,
        // so a project that has a provider resolves everything in the order it always did.
        if (ref == null) ref = defaults[cls]
        if (ref == null) ref = placeholderProvider?.invoke(cls)
        if (ref != null) {
            cache[cls] = ref
        }
        return ref
    }

    fun getReference(cls: KClass<*>): Any {
        val r = runCatching { tryGetReference(cls) }
        // A MocKMPNoPlaceholderException already explains, in the processor's own words, why no
        // placeholder exists and what to do about it — rethrown as-is rather than folded into the
        // generic message below, which would only bury that explanation.
        (r.exceptionOrNull() as? MocKMPNoPlaceholderException)?.let { throw it }
        if (r.isFailure || r.getOrThrow() == null) {
            throw IllegalStateException("Could not create an instance of ${cls.bestName()}. Please use mocker.useReference(${cls.simpleName}) to set a reference.", r.exceptionOrNull())
        }
        return r.getOrNull()!!
    }
}
