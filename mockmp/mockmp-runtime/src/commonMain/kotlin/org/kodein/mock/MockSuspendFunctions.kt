package org.kodein.mock


public suspend fun <R>
mockSuspendFunction0(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    block: (suspend () -> R)? = null
): suspend () -> R =
    Anonymous().let { rec ->
        val f: suspend () -> R = {
            mocker.registerSuspend(rec, "$functionName()")
        }
        f
    }.also {
        if (block != null) mocker.everySuspending { it() } runs { block() }
    }

public suspend inline fun <R, reified A1>
mockSuspendFunction1(
    mocker: Mocker,
    a1Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1) -> R)? = null
): suspend (A1) -> R =
    Anonymous().let { rec ->
        val f: suspend (A1) -> R = { a1 ->
            mocker.registerSuspend(rec, "$functionName($a1Type)", a1)
        }
        f
    }.also {
        if (block != null) mocker.everySuspending { it(isAny()) } runs { block(it[0] as A1) }
    }

public suspend inline fun <R, reified A1, reified A2>
mockSuspendFunction2(
    mocker: Mocker,
    a1Type: String, a2Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2) -> R)? = null
): suspend (A1, A2) -> R =
    Anonymous().let { rec ->
        val f: suspend (A1, A2) -> R = { a1, a2 ->
            mocker.registerSuspend(rec, "$functionName($a1Type, $a2Type)", a1, a2)
        }
        f
    }.also {
        if (block != null) mocker.everySuspending { it(isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2) }
    }

public suspend inline fun <R, reified A1, reified A2, reified A3>
mockSuspendFunction3(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3) -> R)? = null
): suspend (A1, A2, A3) -> R =
    Anonymous().let { rec ->
        val f: suspend (A1, A2, A3) -> R = { a1, a2, a3 ->
            mocker.registerSuspend(rec, "$functionName($a1Type, $a2Type, $a3Type)", a1, a2, a3)
        }
        f
    }.also {
        if (block != null) mocker.everySuspending { it(isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3) }
    }

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4>
mockSuspendFunction4(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4) -> R)? = null
): suspend (A1, A2, A3, A4) -> R =
    Anonymous().let { rec ->
        val f: suspend (A1, A2, A3, A4) -> R = { a1, a2, a3, a4 ->
            mocker.registerSuspend(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type)", a1, a2, a3, a4)
        }
        f
    }.also {
        if (block != null) mocker.everySuspending { it(isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4) }
    }

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5>
mockSuspendFunction5(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5) -> R)? = null
): suspend (A1, A2, A3, A4, A5) -> R =
    Anonymous().let { rec ->
        val f: suspend (A1, A2, A3, A4, A5) -> R = { a1, a2, a3, a4, a5 ->
            mocker.registerSuspend(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type)", a1, a2, a3, a4, a5)
        }
        f
    }.also {
        if (block != null) mocker.everySuspending { it(isAny(), isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5) }
    }

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6>
mockSuspendFunction6(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6) -> R =
    Anonymous().let { rec ->
        val f: suspend (A1, A2, A3, A4, A5, A6) -> R = { a1, a2, a3, a4, a5, a6 ->
            mocker.registerSuspend(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type)", a1, a2, a3, a4, a5, a6)
        }
        f
    }.also {
        if (block != null) mocker.everySuspending { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6) }
    }

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7>
mockSuspendFunction7(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7) -> R =
    Anonymous().let { rec ->
        val f: suspend (A1, A2, A3, A4, A5, A6, A7) -> R = { a1, a2, a3, a4, a5, a6, a7 ->
            mocker.registerSuspend(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type)", a1, a2, a3, a4, a5, a6, a7)
        }
        f
    }.also {
        if (block != null) mocker.everySuspending { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6, it[6] as A7) }
    }

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8>
mockSuspendFunction8(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, a8Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7, A8) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7, A8) -> R =
    Anonymous().let { rec ->
        val f: suspend (A1, A2, A3, A4, A5, A6, A7, A8) -> R = { a1, a2, a3, a4, a5, a6, a7, a8 ->
            mocker.registerSuspend(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type, $a8Type)", a1, a2, a3, a4, a5, a6, a7, a8)
        }
        f
    }.also {
        if (block != null) mocker.everySuspending { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6, it[6] as A7, it[7] as A8) }
    }

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9>
mockSuspendFunction9(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, a8Type: String, a9Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R =
    Anonymous().let { rec ->
        val f: suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R = { a1, a2, a3, a4, a5, a6, a7, a8, a9 ->
            mocker.registerSuspend(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type, $a8Type, $a9Type)", a1, a2, a3, a4, a5, a6, a7, a8, a9)
        }
        f
    }.also {
        if (block != null) mocker.everySuspending { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6, it[6] as A7, it[7] as A8, it[8] as A9) }
    }

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10>
mockSuspendFunction10(
    mocker: Mocker,
    a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, a8Type: String, a9Type: String, a10Type: String,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R =
    Anonymous().let { rec ->
        val f: suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R = { a1, a2, a3, a4, a5, a6, a7, a8, a9, a10 ->
            mocker.registerSuspend(rec, "$functionName($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type, $a8Type, $a9Type, $a10Type)", a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)
        }
        f
    }.also {
        if (block != null) mocker.everySuspending { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs { block(it[0] as A1, it[1] as A2, it[2] as A3, it[3] as A4, it[4] as A5, it[5] as A6, it[6] as A7, it[7] as A8, it[8] as A9, it[9] as A10) }
    }


public suspend inline fun <R, reified A1>
mockSuspendFunction1(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1) -> R)? = null
): suspend (A1) -> R =
    mockSuspendFunction1(mocker, A1::class.bestName(), functionName, block)

public suspend inline fun <R, reified A1, reified A2>
mockSuspendFunction2(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2) -> R)? = null
): suspend (A1, A2) -> R =
    mockSuspendFunction2(mocker, A1::class.bestName(), A2::class.bestName(), functionName, block)

public suspend inline fun <R, reified A1, reified A2, reified A3>
mockSuspendFunction3(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3) -> R)? = null
): suspend (A1, A2, A3) -> R =
    mockSuspendFunction3(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), functionName, block)

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4>
mockSuspendFunction4(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4) -> R)? = null
): suspend (A1, A2, A3, A4) -> R =
    mockSuspendFunction4(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), functionName, block)

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5>
mockSuspendFunction5(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5) -> R)? = null
): suspend (A1, A2, A3, A4, A5) -> R =
    mockSuspendFunction5(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), functionName, block)

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6>
mockSuspendFunction6(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6) -> R =
    mockSuspendFunction6(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), functionName, block)

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7>
mockSuspendFunction7(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7) -> R =
    mockSuspendFunction7(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), functionName, block)

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8>
mockSuspendFunction8(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7, A8) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7, A8) -> R =
    mockSuspendFunction8(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), functionName, block)

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9>
mockSuspendFunction9(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R =
    mockSuspendFunction9(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), A9::class.bestName(), functionName, block)

public suspend inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10>
mockSuspendFunction10(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: (suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R)? = null
): suspend (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R =
    mockSuspendFunction10(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), A9::class.bestName(), A10::class.bestName(), functionName, block)

