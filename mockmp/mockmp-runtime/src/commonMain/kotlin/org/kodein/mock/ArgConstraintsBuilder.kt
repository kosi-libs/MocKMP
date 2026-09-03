package org.kodein.mock

import kotlin.reflect.KClass


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

    /**
     * The placeholder a value-less constraint ([isAny], [isNull], [isNotNull], [isInstanceOf],
     * [isValid]) hands back so the surrounding `every`/`verify` call typechecks — resolved from
     * [cls] through [References.getReference]. The value-carrying constraints ([isEqual] & co.)
     * never call this: they return what the caller already gave them.
     *
     * [fromIsInstanceOf] tailors the failure message for [isInstanceOf] only: its `type` argument
     * and its reified `T` are different things (see [isInstanceOf]'s doc), but writing `T`
     * explicitly — `isInstanceOf<Foo>(Foo::class)` — silently pins them together at a possibly
     * too-narrow type, which is the likeliest reason a lookup fails from that specific constraint.
     */
    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <T> toReturn(cls: KClass<*>, fromIsInstanceOf: Boolean = false): T {
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
     * Records [constraint] for the next mocked call. `every` and `verify` only ever read the
     * recorded constraints — never the argument a definition call carried — so a constraint that
     * has a real value to stand in for its argument ([isEqual] & co., and [isValid] with an
     * explicit placeholder) can skip [toReturn] entirely, and so never needs a placeholder for a
     * type the project does not actually use.
     */
    @PublishedApi
    internal fun <T> addConstraint(constraint: ArgConstraint<T>) {
        constraints.add(constraint)
    }

    public inline fun <reified T> isAny(capture: MutableList<T>? = null): T {
        addConstraint(ArgConstraint.isAny(capture))
        return toReturn(T::class)
    }
    @Suppress("NOTHING_TO_INLINE")
    public inline fun <T> isEqual(expected: T, capture: MutableList<T>? = null): T {
        addConstraint(ArgConstraint.isEqual(expected, capture))
        return expected
    }
    @Suppress("NOTHING_TO_INLINE")
    public inline fun <T> isNotEqual(expected: T, capture: MutableList<T>? = null): T {
        addConstraint(ArgConstraint.isNotEqual(expected, capture))
        return expected
    }
    @Suppress("NOTHING_TO_INLINE")
    public inline fun <T> isSame(expected: T, capture: MutableList<T>? = null): T {
        addConstraint(ArgConstraint.isSame(expected, capture))
        return expected
    }
    @Suppress("NOTHING_TO_INLINE")
    public inline fun <T> isNotSame(expected: T, capture: MutableList<T>? = null): T {
        addConstraint(ArgConstraint.isNotSame(expected, capture))
        return expected
    }
    public inline fun <reified T> isNull(capture: MutableList<T>? = null): T {
        addConstraint(ArgConstraint.isNull(capture))
        return toReturn(T::class)
    }
    public inline fun <reified T> isNotNull(capture: MutableList<T>? = null): T {
        addConstraint(ArgConstraint.isNotNull(capture))
        return toReturn(T::class)
    }
    public inline fun <reified T> isInstanceOf(type: KClass<*>, capture: MutableList<T>? = null): T {
        addConstraint(ArgConstraint.isInstanceOf(type, capture))
        return toReturn(T::class, fromIsInstanceOf = true)
    }
    public inline fun <reified T> isValid(constraint: ArgConstraint<T>): T {
        addConstraint(constraint)
        return toReturn(T::class)
    }
    /**
     * [isValid] for a `T` with no generated placeholder: [placeholder] is any real `T` to stand in
     * as the argument (never checked — only [constraint] is), the same escape hatch [isEqual] uses
     * with the value it is given.
     */
    @Suppress("NOTHING_TO_INLINE")
    public inline fun <T> isValid(constraint: ArgConstraint<T>, placeholder: T): T {
        addConstraint(constraint)
        return placeholder
    }

    /**
     * A constraint satisfied when [test] returns [ArgConstraint.Result.Success] for the argument.
     *
     * A parameter typed as a supertype accepts a narrower constraint — `isValid<String>` against a
     * `doAny(any: Any)` — so the argument the call actually carries need not be a [T]. It is checked
     * before [test] sees it: a mismatch is a constraint that does not match, not a
     * `ClassCastException` thrown out of the surrounding `every`/`verify`.
     */
    public inline fun <reified T> isValid(
        capture: MutableList<T>? = null,
        noinline description: () -> String = { "isValid" },
        noinline test: (T) -> ArgConstraint.Result
    ): T {
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
