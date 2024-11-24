package data

import foo.Bar

class Control(val bar: Bar, val data: Data) {
    fun doIt() = bar.doData(data)
}
