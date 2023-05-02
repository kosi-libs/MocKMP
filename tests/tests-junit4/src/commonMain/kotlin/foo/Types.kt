package foo

import data.Data
import data.Direction


@RequiresOptIn
annotation class ExperimentalTest

typealias FooMap<T> = Map<T, List<Pair<Int, Set<String>>>>

interface Foo<out T : Any> {
    val roString: String
    var rwString: String
    fun doInt(int: Int)
    fun doPrimitive(string: String, int: Int)
    fun doInterface(bar: Bar)
    fun doEnum(direction: Direction)
    fun doArray(array: Array<String>)
    fun doAbstract(abs: Abs)
    fun doSealedClass(s: SCls)
    fun doSealedInterface(s: SItf)
    fun doMap(m: FooMap<String>)
    fun newInt(): Int
    fun newString(): String
    fun newStringNullable(): String?
    fun newT(): T
    val defaultT: T
    val map: FooMap<String>
    val list: List<Set<Int>>

    @Deprecated("This is a test")
    var deprecatedProperty: String

    @Deprecated("This is a test")
    fun deprecatedMethod()

    @ExperimentalTest
    var experimentalProperty: String

    @ExperimentalTest
    fun experimentalMethod()
}

typealias BarCB = (String) -> Int

interface Bar : Foo<Bar> {
    fun doNothing() {}
    fun doSomething() { doNothing() }
    fun newData(string: String, int: Int): Data
    fun doData(data: Data)
    fun doAll(string: String, int: Int, data: Data)
    suspend fun newData(): Data
    suspend fun doSomethingSuspend() { doNothing() }
    fun callback(cb: (String) -> Int)
    fun taCallback(cb: BarCB)
    fun suspendCallback(cb: suspend (String) -> Int)
    fun <T: Comparable<T>> order(c: Iterable<T>) : List<T>
}

abstract class Abs(val i: Int)

sealed class SCls {
    @Suppress("CanSealedSubClassBeObject", "unused")
    class C : SCls()
    object O : SCls()
}

sealed interface SItf {
    class C : SItf {
        override fun toString(): String = "C"
    }
    object O : SItf {
        override fun toString(): String = "O"
    }
}
