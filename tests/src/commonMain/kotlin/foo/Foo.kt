package foo

import data.Data
import data.Direction

interface Foo<out T : Any> {
    val roString: String
    var rwString: String
    fun doInt(int: Int)
    fun doPrimitive(string: String, int: Int)
    fun doInterface(bar: Bar)
    fun doEnum(direction: Direction)
    fun doArray(array: Array<String>)
    fun doAbstract(abs: Abs)
    fun newInt(): Int
    fun newString(): String
    fun newT(): T
    val defaultT: T
}

interface Bar : Foo<Bar> {
    fun doNothing() {}
    fun newData(string: String, int: Int): Data
    fun doData(data: Data)
    fun doAll(string: String, int: Int, data: Data)
    suspend fun newData(): Data
    fun callback(cb: (String) -> Int)
    fun suspendCallback(cb: suspend (String) -> Int)
    fun <T: Comparable<T>> order(c: Iterable<T>) : List<T>
}

abstract class Abs
