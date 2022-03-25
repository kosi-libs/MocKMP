package data

import kotlinx.datetime.Instant

data class SubData(
    val string: String,
    val int: Int
)

enum class Direction { LEFT, RIGHT }

data class SomeDirection(
    val dir: Direction
)

data class Data(
    val sub1: SubData,
    val sub2: SubData,
    val nullDir: SomeDirection?,
    val dir: SomeDirection,
    val special: Instant
)

class Funs(
    val cb: (String) -> Unit,
    val data: () -> SubData,
    val combo: (String) -> SubData
)
