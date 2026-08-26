package org.kodein.mock

import kotlin.reflect.KClass
import kotlin.reflect.typeOf


public open class ArgConstraintsBuilder internal constructor(private val references: References) {
    private val constraints: MutableList<ArgConstraint<*>> = ArrayList()

    internal fun getConstraints(args: Array<*>): List<ArgConstraint<*>> {
        val list = when {
            // A constraint built before this call, for a call that never happened, is indistinguishable
            // from one built for this call's arguments: constraints carry no argument type, and the
            // argument a constraint records is a placeholder a literal could equally have produced. Only
            // a *count* mismatch is detectable here; [checkNoPendingConstraints] catches the rest.
            constraints.size == args.size -> constraints.toList()
            constraints.isEmpty() -> args.map { if (it == null) ArgConstraint.isNull() else ArgConstraint.isEqual(it) }
            else -> throw Mocker.MockingException(
                "Expected ${args.size} constraint(s) for this call, but ${constraints.size} were pending: ${constraints.joinToString { it.description() }}.\n" +
                        "Either literal values are mixed with constraints (replace each literal with its constraint counterpart, isEqual(value) or isNull()), " +
                        "or a constraint was created and never passed to a mocked call."
            )
        }
        constraints.clear()
        return list
    }

    /**
     * Fails if constraints were created but never handed to a mocked call — they would otherwise be
     * silently taken by whichever call came next, or dropped with this builder.
     */
    internal fun checkNoPendingConstraints() {
        if (constraints.isEmpty()) return
        val count = constraints.size
        val pending = constraints.joinToString { it.description() }
        constraints.clear()
        throw Mocker.MockingException(
            "$count constraint(s) were created but never passed to a mocked call: $pending.\n" +
                    "A constraint only applies to the call it is given to, as an argument."
        )
    }

    @PublishedApi
    internal fun <T> toReturn(constraint: ArgConstraint<T>, cls: KClass<*>): T =
        resolvePlaceholder(constraint, cls, fromIsInstanceOf = false)

    /**
     * [toReturn]'s counterpart for [isInstanceOf] specifically.
     *
     * [isInstanceOf]'s `cls` argument (the type to check against) and its reified `T` (the type
     * this call resolves a placeholder for) are different things — see [isInstanceOf]'s doc — but
     * writing `T` explicitly, e.g. `isInstanceOf<Foo>(Foo::class)`, is a legal call that silently
     * pins them to the same, possibly too-narrow type anyway. That is the single most likely cause
     * of a placeholder lookup failing from inside this specific constraint, so the failure message
     * calls it out; every other constraint keeps [toReturn]'s plain message.
     */
    @PublishedApi
    internal fun <T> toReturnInstanceOf(constraint: ArgConstraint<T>, cls: KClass<*>): T =
        resolvePlaceholder(constraint, cls, fromIsInstanceOf = true)

    @Suppress("UNCHECKED_CAST")
    private fun <T> resolvePlaceholder(constraint: ArgConstraint<T>, cls: KClass<*>, fromIsInstanceOf: Boolean): T {
        constraints.add(constraint)

        try {
            return references.getReference(cls) as T
        } catch (e: MocKMPNoPlaceholderException) {
            // Already the processor's own explanation of why this exact type has no placeholder,
            // and what to do about it — rethrown as-is rather than folded into the generic message
            // below, which would only bury that explanation under "please open an issue".
            throw e
        } catch (e: Throwable) {
            val instanceOfHint = if (!fromIsInstanceOf) "" else
                "If this isInstanceOf() call has an explicit type argument, remove it: write isInstanceOf(${cls.bestName()}::class), " +
                        "not isInstanceOf<${cls.bestName()}>(${cls.bestName()}::class). The type argument must stay inferred from the " +
                        "mocked function's parameter type — writing it explicitly is what makes MocKMP look for a placeholder of this exact type.\n"
            throw RuntimeException("Could not find a way to get a reference of ${cls.bestName()}.\n" +
                    instanceOfHint +
                    "Make sure ${cls.bestName()} is covered by @Mock, @Fake, @UsesMocks or @UsesFakes, " +
                    "and that this Mocker has already created a mock — with mocker.mock<T>() or mocker.injectMocks(this) — " +
                    "as that is what registers the generated values on it.\n" +
                    "Otherwise, give the mocker a reference to use with mocker.useReference(...).\n" +
                    "If none of this applies, please open an issue: https://github.com/kosi-libs/MocKMP/issues/new", e)
        }
    }

    /**
     * Adds [constraint] and returns `null` in place of a placeholder.
     *
     * For call sites that discard the value: `every` and `verify` only ever read the constraints —
     * the recorded argument of a definition call is never looked at — so only the *presence* of one
     * argument matters. Going through [toReturn] would make such a call site fail whenever the
     * project happens to have no placeholder for a type it does not actually use.
     */
    internal fun <T> addConstraint(constraint: ArgConstraint<T>): T {
        constraints.add(constraint)
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

    public inline fun <reified T> isAny(capture: MutableList<T>? = null): T = toReturn(ArgConstraint.isAny(capture), T::class)
    public inline fun <reified T> isEqual(expected: T, capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isEqual(expected, capture), T::class)
    public inline fun <reified T> isNotEqual(expected: T, capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isNotEqual(expected, capture), T::class)
    public inline fun <reified T> isSame(expected: T, capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isSame(expected, capture), T::class)
    public inline fun <reified T> isNotSame(expected: T, capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isNotSame(expected, capture), T::class)
    public inline fun <reified T> isNull(capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isNull(capture), T::class)
    public inline fun <reified T> isNotNull(capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isNotNull(capture), T::class)
    public inline fun <reified T> isInstanceOf(type: KClass<*>, capture: MutableList<T>? = null): T = toReturnInstanceOf(ArgConstraint.isInstanceOf(type, capture), T::class)

    public inline fun <reified T> isValid(constraint: ArgConstraint<T>): T = toReturn<T>(constraint, T::class)

    /**
     * A constraint satisfied when [test] returns [ArgConstraint.Result.Success] for the argument.
     *
     * A parameter typed as a supertype accepts a narrower constraint — `isValid<String>` against a
     * `doAny(any: Any)` — so the argument the call actually carries need not be a [T]. It is checked
     * before [test] sees it: a mismatch is a constraint that does not match, not a
     * `ClassCastException` thrown out of the surrounding `every`/`verify`.
     */
    public inline fun <reified T> isValid(capture: MutableList<T>? = null, noinline description: () -> String = { "isValid" }, noinline test: (T) -> ArgConstraint.Result): T {
        val typeName = T::class.bestName()
        // Genuinely accepts Any?, so it needs no cast to be stored as the constraint's (T) -> Result:
        // function types are contravariant in their parameters.
        val checked: (Any?) -> ArgConstraint.Result = { arg ->
            if (arg is T) test(arg)
            else ArgConstraint.Result.Failure { "Expected an instance of $typeName, but was <$arg>" }
        }
        return isValid<T>(ArgConstraint(capture, description, checked))
    }
}
