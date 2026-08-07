package org.kodein.mock


/** Not `inline`, unlike arities 1 and up — see [mockFunction0] for why. */
public fun <R>
mockSuspendFunction0(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    block: (suspend () -> R)? = null
): suspend () -> R =
    Anonymous().let { rec ->
        val method = "$functionName()"
        val f: suspend () -> R = {
            mocker.registerSuspend(rec, method)
        }
        if (block != null) mocker.everySuspendingCall<R>(rec, method, 0) runs { block() }
        f
    }

/**
 * **Pass the type as `a1Type = "…"`, never positionally.**
 *
 * `mockSuspendFunction1(mocker, "kotlin.String")` also fits the reified overload below — both leave
 * exactly one parameter defaulted — and Kotlin picks that one, so the type string silently becomes
 * the [functionName] and the mock registers as `kotlin.String(kotlin.String)`. Naming the argument
 * resolves it. Arities of 2 and up pass more strings than the reified overload takes, so they cannot
 * hit this.
 *
 * The type-string overloads exist so that the KSP processor can pass the qualified name it resolved:
 * `bestName()` returns `qualifiedName` on JVM/Native but `simpleName` on JS/Wasm, so letting the
 * reified overload derive it would make the registration key vary by platform.
 */
public inline fun <R, reified A1>
mockSuspendFunction1(
    mocker: Mocker,
    a1Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1) -> R)? = null
): suspend (A1) -> R =
    Anonymous().let { rec ->
        val method = "$functionName($a1Type)"
        val f: suspend (A1) -> R = { a1 ->
            mocker.registerSuspend(rec, method, a1)
        }
        if (block != null) mocker.everySuspendingCall<R>(rec, method, 1) runs { block(it[0] as A1) }
        f
    }

public inline fun <R, reified A1, reified A2>
mockSuspendFunction2(
    mocker: Mocker,
    a1Type: String, a2Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2) -> R)? = null
): suspend (A1, A2) -> R =
    Anonymous().let { rec ->
        val method = "$functionName($a1Type, $a2Type)"
        val f: suspend (A1, A2) -> R = { a1, a2 ->
            mocker.registerSuspend(rec, method, a1, a2)
        }
        if (block != null) mocker.everySuspendingCall<R>(rec, method, 2) runs { block(it[0] as A1, it[1] as A2) }
        f
    }

public inline fun <R, reified A1, reified A2, reified A3>
mockSuspendFunction3(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3) -> R)? = null
): suspend (A1, A2, A3) -> R =
    Anonymous().let { rec ->
        val method = "$functionName($a1Type, $a2Type, $a3Type)"
        val f: suspend (A1, A2, A3) -> R = { a1, a2, a3 ->
            mocker.registerSuspend(rec, method, a1, a2, a3)
        }
        if (block != null) mocker.everySuspendingCall<R>(rec, method, 3) runs { block(it[0] as A1, it[1] as A2, it[2] as A3) }
        f
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4>
mockSuspendFunction4(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4) -> R)? = null
): suspend (A1, A2, A3, A4) -> R =
    Anonymous().let { rec ->
        val method = "$functionName($a1Type, $a2Type, $a3Type, $a4Type)"
        val f: suspend (A1, A2, A3, A4) -> R = { a1, a2, a3, a4 ->
            mocker.registerSuspend(rec, method, a1, a2, a3, a4)
        }
        if (block != null) mocker.everySuspendingCall<R>(rec, method, 4) runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4) }
        f
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5>
mockSuspendFunction5(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5) -> R)? = null
): suspend (A1, A2, A3, A4, A5) -> R =
    Anonymous().let { rec ->
        val method = "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type)"
        val f: suspend (A1, A2, A3, A4, A5) -> R = { a1, a2, a3, a4, a5 ->
            mocker.registerSuspend(rec, method, a1, a2, a3, a4, a5)
        }
        if (block != null) mocker.everySuspendingCall<R>(rec, method, 5) runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5) }
        f
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6>
mockSuspendFunction6(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6) -> R =
    Anonymous().let { rec ->
        val method = "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type)"
        val f: suspend (A1, A2, A3, A4, A5, A6) -> R = { a1, a2, a3, a4, a5, a6 ->
            mocker.registerSuspend(rec, method, a1, a2, a3, a4, a5, a6)
        }
        if (block != null) mocker.everySuspendingCall<R>(rec, method, 6) runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6) }
        f
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7>
mockSuspendFunction7(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7) -> R =
    Anonymous().let { rec ->
        val method = "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type)"
        val f: suspend (A1, A2, A3, A4, A5, A6, A7) -> R = { a1, a2, a3, a4, a5, a6, a7 ->
            mocker.registerSuspend(rec, method, a1, a2, a3, a4, a5, a6, a7)
        }
        if (block != null) mocker.everySuspendingCall<R>(rec, method, 7) runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6, it[6] as A7) }
        f
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8>
mockSuspendFunction8(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, a8Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7, A8) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7, A8) -> R =
    Anonymous().let { rec ->
        val method = "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type, $a8Type)"
        val f: suspend (A1, A2, A3, A4, A5, A6, A7, A8) -> R = { a1, a2, a3, a4, a5, a6, a7, a8 ->
            mocker.registerSuspend(rec, method, a1, a2, a3, a4, a5, a6, a7, a8)
        }
        if (block != null) mocker.everySuspendingCall<R>(rec, method, 8) runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6, it[6] as A7, it[7] as A8) }
        f
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9>
mockSuspendFunction9(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, a8Type: String, a9Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R =
    Anonymous().let { rec ->
        val method = "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type, $a8Type, $a9Type)"
        val f: suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R = { a1, a2, a3, a4, a5, a6, a7, a8, a9 ->
            mocker.registerSuspend(rec, method, a1, a2, a3, a4, a5, a6, a7, a8, a9)
        }
        if (block != null) mocker.everySuspendingCall<R>(rec, method, 9) runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6, it[6] as A7, it[7] as A8, it[8] as A9) }
        f
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10>
mockSuspendFunction10(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, a8Type: String, a9Type: String, a10Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R =
    Anonymous().let { rec ->
        val method = "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type, $a8Type, $a9Type, $a10Type)"
        val f: suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R = { a1, a2, a3, a4, a5, a6, a7, a8, a9, a10 ->
            mocker.registerSuspend(rec, method, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)
        }
        if (block != null) mocker.everySuspendingCall<R>(rec, method, 10) runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6, it[6] as A7, it[7] as A8, it[8] as A9, it[9] as A10) }
        f
    }


public inline fun <R, reified A1>
mockSuspendFunction1(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1) -> R)? = null
): suspend (A1) -> R =
    mockSuspendFunction1(mocker, A1::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2>
mockSuspendFunction2(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2) -> R)? = null
): suspend (A1, A2) -> R =
    mockSuspendFunction2(mocker, A1::class.bestName(), A2::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3>
mockSuspendFunction3(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3) -> R)? = null
): suspend (A1, A2, A3) -> R =
    mockSuspendFunction3(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4>
mockSuspendFunction4(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4) -> R)? = null
): suspend (A1, A2, A3, A4) -> R =
    mockSuspendFunction4(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5>
mockSuspendFunction5(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5) -> R)? = null
): suspend (A1, A2, A3, A4, A5) -> R =
    mockSuspendFunction5(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6>
mockSuspendFunction6(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6) -> R =
    mockSuspendFunction6(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7>
mockSuspendFunction7(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7) -> R =
    mockSuspendFunction7(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8>
mockSuspendFunction8(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7, A8) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7, A8) -> R =
    mockSuspendFunction8(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9>
mockSuspendFunction9(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R =
    mockSuspendFunction9(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), A9::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10>
mockSuspendFunction10(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R =
    mockSuspendFunction10(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), A9::class.bestName(), A10::class.bestName(), functionName, block)

