package org.kodein.mock


@PublishedApi
internal class Anonymous

@PublishedApi
internal const val defaultFunctionName: String = "invoke"

// Not `inline`, unlike arities 1 and up: those are inline only because they need `reified` type
// parameters — `every { it(isAny(), …) }` resolves `isAny<A1>()` against them. Arity 0 has nothing
// to reify, and `block` would have to be `noinline` anyway, so marking it `inline` earns a compiler
// warning that the impact is insignificant rather than any benefit.
/**
 * Creates a mock of a `() -> R` function, recording its calls on [mocker] exactly as a mocked
 * interface's method would be.
 *
 * The property's declared type supplies the return type, so nothing else needs to be passed.
 * Give the mock a behaviour with `mocker.every { it() }`, or pass [block] here to do both at once.
 *
 * @param mocker The mocker that records the calls — mandatory.
 * @param functionName The name the call is recorded under; only matters if you mock several
 * functions on the same [mocker] and want them told apart in failure messages.
 * @param block Optional behaviour, replacing a separate `mocker.every { }`.
 */
public fun <R>
mockFunction0(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    block: (() -> R)? = null
): () -> R =
    Anonymous().let { rec ->
        {
            mocker.register<R>(rec, "$functionName()")
        }
    }.also {
        if (block != null) mocker.every { it() } runs { block() }
    }

// The type-string overloads exist so that the KSP processor can pass the qualified name it resolved:
// `bestName()` returns `qualifiedName` on JVM/Native but `simpleName` on JS/Wasm, so letting the
// reified overload derive it would make the registration key vary by platform.
/**
 * Creates a mock of an `(A1) -> R` function, recording its calls on [mocker]. Usually called
 * through the [reified overload][mockFunction1] below, which derives [a1Type] itself.
 *
 * **Pass the type as `a1Type = "…"`, never positionally.** `mockFunction1(mocker, "kotlin.String")`
 * also fits the reified overload — both leave exactly one parameter defaulted — and Kotlin picks
 * that one, so the type string silently becomes the [functionName] and the mock registers as
 * `kotlin.String(kotlin.String)`. Naming the argument resolves it. Arities of 2 and up pass more
 * strings than the reified overload takes, so they cannot hit this.
 *
 * @param mocker The mocker that records the calls — mandatory.
 * @param a1Type The qualified name of `A1`, used in the recorded call identifier.
 * @param functionName The name the call is recorded under.
 * @param block Optional behaviour, replacing a separate `mocker.every { }`.
 */
public inline fun <R, reified A1>
mockFunction1(
    mocker: Mocker,
    a1Type: String,
    functionName: String = defaultFunctionName,
    noinline block: ((A1) -> R)? = null
): (A1) -> R =
    Anonymous().let { rec ->
        { a1: A1 ->
            mocker.register<R>(rec, "$functionName($a1Type)", a1)
        }
    }.also {
        if (block != null) mocker.every { it(isAny()) } runs { block(it[0] as A1) }
    }

/** Mocks a `(A1, A2) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2>
mockFunction2(
    mocker: Mocker,
    a1Type: String, a2Type: String,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2) -> R)? = null
): (A1, A2) -> R =
    Anonymous().let { rec ->
        { a1: A1, a2: A2 ->
            mocker.register<R>(rec, "$functionName($a1Type, $a2Type)", a1, a2)
        }
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2) }
    }

/** Mocks a `(A1, A2, A3) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3>
mockFunction3(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3) -> R)? = null
): (A1, A2, A3) -> R =
    Anonymous().let { rec ->
        { a1: A1, a2: A2, a3: A3 ->
            mocker.register<R>(rec, "$functionName($a1Type, $a2Type, $a3Type)", a1, a2, a3)
        }
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3) }
    }

/** Mocks a `(A1, A2, A3, A4) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4>
mockFunction4(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4) -> R)? = null
): (A1, A2, A3, A4) -> R =
    Anonymous().let { rec ->
        { a1: A1, a2: A2, a3: A3, a4: A4 ->
            mocker.register<R>(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type)", a1, a2, a3, a4)
        }
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4) }
    }

/** Mocks a `(A1, A2, A3, A4, A5) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5>
mockFunction5(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5) -> R)? = null
): (A1, A2, A3, A4, A5) -> R =
    Anonymous().let { rec ->
        { a1: A1, a2: A2, a3: A3, a4: A4, a5: A5 ->
            mocker.register<R>(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type)", a1, a2, a3, a4, a5)
        }
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5) }
    }

/** Mocks a `(A1, A2, A3, A4, A5, A6) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6>
mockFunction6(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6) -> R)? = null
): (A1, A2, A3, A4, A5, A6) -> R =
    Anonymous().let { rec ->
        { a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6 ->
            mocker.register<R>(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type)", a1, a2, a3, a4, a5, a6)
        }
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6) }
    }

/** Mocks a `(A1, A2, A3, A4, A5, A6, A7) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7>
mockFunction7(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6, A7) -> R)? = null
): (A1, A2, A3, A4, A5, A6, A7) -> R =
    Anonymous().let { rec ->
        { a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7 ->
            mocker.register<R>(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type)", a1, a2, a3, a4, a5, a6, a7)
        }
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6, it[6] as A7) }
    }

/** Mocks a `(A1, A2, A3, A4, A5, A6, A7, A8) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8>
mockFunction8(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, a8Type: String,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6, A7, A8) -> R)? = null
): (A1, A2, A3, A4, A5, A6, A7, A8) -> R =
    Anonymous().let { rec ->
        { a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7, a8: A8 ->
            mocker.register<R>(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type, $a8Type)", a1, a2, a3, a4, a5, a6, a7, a8)
        }
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6, it[6] as A7, it[7] as A8) }
    }

/** Mocks a `(A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9>
mockFunction9(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, a8Type: String, a9Type: String,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R)? = null
): (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R =
    Anonymous().let { rec ->
        { a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7, a8: A8, a9: A9 ->
            mocker.register<R>(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type, $a8Type, $a9Type)", a1, a2, a3, a4, a5, a6, a7, a8, a9)
        }
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6, it[6] as A7, it[7] as A8, it[8] as A9) }
    }

/** Mocks a `(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10>
mockFunction10(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, a8Type: String, a9Type: String, a10Type: String,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R)? = null
): (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R =
    Anonymous().let { rec ->
        { a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7, a8: A8, a9: A9, a10: A10 ->
            mocker.register<R>(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type, $a8Type, $a9Type, $a10Type)", a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)
        }
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6, it[6] as A7, it[7] as A8, it[8] as A9, it[9] as A10) }
    }


/**
 * Creates a mock of an `(A1) -> R` function, recording its calls on [mocker].
 *
 * The property's declared type supplies the argument and return types, so [mocker] is all that must
 * be passed. Give the mock a behaviour with `mocker.every { it(isAny()) }`, or pass [block] here to
 * do both at once.
 *
 * @param mocker The mocker that records the calls — mandatory.
 * @param functionName The name the call is recorded under; only matters when several mocked
 * functions on the same [mocker] must be told apart.
 * @param block Optional behaviour, replacing a separate `mocker.every { }`.
 */
public inline fun <R, reified A1>
mockFunction1(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1) -> R)? = null
): (A1) -> R =
    mockFunction1(mocker, A1::class.bestName(), functionName, block)

/** Mocks a `(A1, A2) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2>
mockFunction2(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2) -> R)? = null
): (A1, A2) -> R =
    mockFunction2(mocker, A1::class.bestName(), A2::class.bestName(), functionName, block)

/** Mocks a `(A1, A2, A3) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3>
mockFunction3(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3) -> R)? = null
): (A1, A2, A3) -> R =
    mockFunction3(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), functionName, block)

/** Mocks a `(A1, A2, A3, A4) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4>
mockFunction4(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4) -> R)? = null
): (A1, A2, A3, A4) -> R =
    mockFunction4(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), functionName, block)

/** Mocks a `(A1, A2, A3, A4, A5) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5>
mockFunction5(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5) -> R)? = null
): (A1, A2, A3, A4, A5) -> R =
    mockFunction5(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), functionName, block)

/** Mocks a `(A1, A2, A3, A4, A5, A6) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6>
mockFunction6(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6) -> R)? = null
): (A1, A2, A3, A4, A5, A6) -> R =
    mockFunction6(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), functionName, block)

/** Mocks a `(A1, A2, A3, A4, A5, A6, A7) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7>
mockFunction7(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6, A7) -> R)? = null
): (A1, A2, A3, A4, A5, A6, A7) -> R =
    mockFunction7(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), functionName, block)

/** Mocks a `(A1, A2, A3, A4, A5, A6, A7, A8) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8>
mockFunction8(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6, A7, A8) -> R)? = null
): (A1, A2, A3, A4, A5, A6, A7, A8) -> R =
    mockFunction8(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), functionName, block)

/** Mocks a `(A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9>
mockFunction9(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R)? = null
): (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R =
    mockFunction9(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), A9::class.bestName(), functionName, block)

/** Mocks a `(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R` function on [mocker], recording its calls. See [mockFunction1]. */
public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10>
mockFunction10(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R)? = null
): (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R =
    mockFunction10(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), A9::class.bestName(), A10::class.bestName(), functionName, block)
