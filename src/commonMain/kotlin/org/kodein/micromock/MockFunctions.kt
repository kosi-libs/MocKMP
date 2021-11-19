package org.kodein.micromock


public inline fun <R> mockFunction0(mocker: Mocker, noinline block: ((Array<*>) -> R)? = null):
            () -> R =
    {
        mocker.register<R>(null, "invoke()")
    }.also {
        if (block != null) mocker.every { it() } runs block
    }

public inline fun <R, reified A1>
        mockFunction1(mocker: Mocker, a1Type: String, noinline block: ((Array<*>) -> R)? = null):
            (A1) -> R =
    { a1: A1 ->
        mocker.register<R>(null, "invoke($a1Type)", a1)
    }.also {
        if (block != null) mocker.every { it(isAny()) } runs block
    }

public inline fun <R, reified A1, reified A2>
        mockFunction2(mocker: Mocker, a1Type: String, a2Type: String, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2) -> R =
    { a1: A1, a2: A2 ->
        mocker.register<R>(null, "invoke($a1Type, $a2Type)", a1, a2)
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny()) } runs block
    }

public inline fun <R, reified A1, reified A2, reified A3>
        mockFunction3(mocker: Mocker, a1Type: String, a2Type: String, a3Type: String, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3) -> R =
    { a1: A1, a2: A2, a3: A3 ->
        mocker.register<R>(null, "invoke($a1Type, $a2Type, $a3Type)", a1, a2, a3)
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny()) } runs block
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4>
        mockFunction4(mocker: Mocker, a1Type: String, a2Type: String, a3Type: String, a4Type: String, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4) -> R =
    { a1: A1, a2: A2, a3: A3, a4: A4 ->
        mocker.register<R>(null, "invoke($a1Type, $a2Type, $a3Type, $a4Type)", a1, a2, a3, a4)
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny()) } runs block
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5>
        mockFunction5(mocker: Mocker, a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4, A5) -> R =
    { a1: A1, a2: A2, a3: A3, a4: A4, a5: A5 ->
        mocker.register<R>(null, "invoke($a1Type, $a2Type, $a3Type, $a4Type, $a5Type)", a1, a2, a3, a4, a5)
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny(), isAny()) } runs block
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6>
        mockFunction6(mocker: Mocker, a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4, A5, A6) -> R =
    { a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6 ->
        mocker.register<R>(null, "invoke($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type)", a1, a2, a3, a4, a5, a6)
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs block
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7>
        mockFunction7(mocker: Mocker, a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4, A5, A6, A7) -> R =
    { a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7 ->
        mocker.register<R>(null, "invoke($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type)", a1, a2, a3, a4, a5, a6, a7)
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs block
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8>
        mockFunction8(mocker: Mocker, a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, a8Type: String, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4, A5, A6, A7, A8) -> R =
    { a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7, a8: A8 ->
        mocker.register<R>(null, "invoke($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type, $a8Type)", a1, a2, a3, a4, a5, a6, a7, a8)
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs block
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9>
        mockFunction9(mocker: Mocker, a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, a8Type: String, a9Type: String, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R =
    { a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7, a8: A8, a9: A9 ->
        mocker.register<R>(null, "invoke($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type, $a8Type, $a9Type)", a1, a2, a3, a4, a5, a6, a7, a8, a9)
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs block
    }

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10>
        mockFunction10(mocker: Mocker, a1Type: String, a2Type: String, a3Type: String, a4Type: String, a5Type: String, a6Type: String, a7Type: String, a8Type: String, a9Type: String, a10Type: String, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R =
    { a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7, a8: A8, a9: A9, a10: A10 ->
        mocker.register<R>(null, "invoke($a1Type, $a2Type, $a3Type, $a4Type, $a5Type, $a6Type, $a7Type, $a8Type, $a9Type, $a10Type)", a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)
    }.also {
        if (block != null) mocker.every { it(isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny(), isAny()) } runs block
    }


public inline fun <R, reified A1>
        mockFunction1(mocker: Mocker, noinline block: ((Array<*>) -> R)? = null):
            (A1) -> R =
    mockFunction1(mocker, A1::class.bestName(), block)

public inline fun <R, reified A1, reified A2>
        mockFunction2(mocker: Mocker, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2) -> R =
    mockFunction2(mocker, A1::class.bestName(), A2::class.bestName(), block)

public inline fun <R, reified A1, reified A2, reified A3>
        mockFunction3(mocker: Mocker, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3) -> R =
    mockFunction3(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4>
        mockFunction4(mocker: Mocker, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4) -> R =
    mockFunction4(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5>
        mockFunction5(mocker: Mocker, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4, A5) -> R =
    mockFunction5(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6>
        mockFunction6(mocker: Mocker, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4, A5, A6) -> R =
    mockFunction6(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7>
        mockFunction7(mocker: Mocker, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4, A5, A6, A7) -> R =
    mockFunction7(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8>
        mockFunction8(mocker: Mocker, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4, A5, A6, A7, A8) -> R =
    mockFunction8(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9>
        mockFunction9(mocker: Mocker, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> R =
    mockFunction9(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), A9::class.bestName(), block)

public inline fun <R, reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10>
        mockFunction10(mocker: Mocker, noinline block: ((Array<*>) -> R)? = null):
            (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) -> R =
    mockFunction10(mocker, A1::class.bestName(), A2::class.bestName(), A3::class.bestName(), A4::class.bestName(), A5::class.bestName(), A6::class.bestName(), A7::class.bestName(), A8::class.bestName(), A9::class.bestName(), A10::class.bestName(), block)
