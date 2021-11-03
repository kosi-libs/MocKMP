package foo

import data.Data

interface Foo {
    fun doString(string: String)
    fun doInt(int: Int)
    fun doPrimitive(string: String, int: Int)
    fun newInt(): Int
    fun newString(): String
    fun newBar(): Bar
}

interface Bar : Foo {
    fun doNothing() {}
    fun newData(string: String, int: Int): Data
    fun doData(data: Data)
    fun doAll(string: String, int: Int, data: Data)
    fun newData(): Data
}
