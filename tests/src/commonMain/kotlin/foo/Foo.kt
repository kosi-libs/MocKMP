package foo

import data.Data

interface Foo<out T : Any> {
    val roString: String
    var rwString: String
    fun doInt(int: Int)
    fun doPrimitive(string: String, int: Int)
    fun newInt(): Int
    fun newString(): String
    fun newT(): T
    val defaultT: T
    fun consume(bar: Bar)
}

interface Bar : Foo<Bar> {
    fun doNothing() {}
    fun newData(string: String, int: Int): Data
    fun doData(data: Data)
    fun doAll(string: String, int: Int, data: Data)
    suspend fun newData(): Data
    fun callback(cb: (String) -> Int)
    fun <T: Comparable<T>> order(c: Iterable<T>) : List<T>
}
