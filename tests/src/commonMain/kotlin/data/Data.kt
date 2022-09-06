package data

import kotlinx.datetime.Instant

expect class Date()
data class SubData<T>(
    val data: T,
    val int: Int
)

enum class Direction { LEFT, RIGHT }

data class SomeDirection(
    val dir: Direction
)

typealias NamesMap<K> = Map<K, Set<String>>

data class Data(
    val sub1: SubData<String>,
    val sub2: SubData<Int>,
    val sub3: SubData<Map<String, Set<String>>>,
    val nullDir: SomeDirection?,
    val dir1: SomeDirection,
    val dir2: SomeDirection,
    val special: Instant,
    val special2: Date,
    val list: List<String>,
    val map: NamesMap<Int>
)

class Funs(
    val cb: (String) -> Unit,
    val data: () -> SubData<String>,
    val combo: (String) -> SubData<String>
)
