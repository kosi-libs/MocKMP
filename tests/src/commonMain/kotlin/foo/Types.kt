package foo

import data.Data
import data.Direction


typealias FooMap = Map<String, List<Pair<Int, Set<String>>>>

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
    fun doMap(m: FooMap)
    fun newInt(): Int
    fun newString(): String
    fun newT(): T
    val defaultT: T
    val map: FooMap
    val list: List<Set<Int>>
}

typealias BarCB = (String) -> Int

interface Bar : Foo<Bar> {
    fun doNothing() {}
    fun doSomething() { doNothing() }
    fun newData(string: String, int: Int): Data
    fun doData(data: Data)
    fun doAll(string: String, int: Int, data: Data)
    suspend fun newData(): Data
    fun callback(cb: (String) -> Int)
    fun taCallback(cb: BarCB)
    // TODO: This makes JS/IR crash. Should be fixed in Kotlin 1.4.20
//    fun suspendCallback(cb: suspend (String) -> Int)
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
