package org.kodein.mock


@PublishedApi
internal class Anonymous

@PublishedApi
internal const val defaultFunctionName: String = "invoke"

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


public inline fun <R, reified A1>
mockFunction1(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1) -> R)? = null
): (A1) -> R =
    mockFunction1(mocker, A1::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2>
mockFunction2(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2) -> R)? = null
): (A1, A2) -> R =
    mockFunction2(mocker, A1::class.bestName(), A2::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3>
mockFunction3(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3) -> R)? = null
): (A1, A2, A3) -> R =
    mockFunction3(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4>
mockFunction4(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4) -> R)? = null
): (A1, A2, A3, A4) -> R =
    mockFunction4(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5>
mockFunction5(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5) -> R)? = null
): (A1, A2, A3, A4, A5) -> R =
    mockFunction5(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6>
mockFunction6(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6) -> R)? = null
): (A1, A2, A3, A4, A5, A6) -> R =
    mockFunction6(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7>
mockFunction7(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6, A7) -> R)? = null
): (A1, A2, A3, A4, A5, A6, A7) -> R =
    mockFunction7(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8>
mockFunction8(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6, A7, A8) -> R)? = null
): (A1, A2, A3, A4, A5, A6, A7, A8) -> R =
    mockFunction8(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9>
mockFunction9(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R)? = null
): (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R =
    mockFunction9(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), A9::class.bestName(), functionName, block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10>
mockFunction10(
    mocker: Mocker,
    functionName: String = defaultFunctionName,
    noinline block: ((A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R)? = null
): (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R =
    mockFunction10(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), A9::class.bestName(), A10::class.bestName(), functionName, block)
