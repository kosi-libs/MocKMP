package data

import kotlin.time.Instant

enum class Direction { LEFT, RIGHT }

data class SomeDirection(
    val dir: Direction,
    val data: SubData
) {
    data class SubData(
        val nDir: Direction?,
    )
}

typealias NamesMap<K> = Map<K, Set<String>>

data class Error<T>(
    val code: T,
    val exception: Exception
)

data class Data(
    val gen1: GenData<String>,
    val gen2: GenData<Int>,
    val gen3: GenData<Map<String, Set<String>>>,
    val sub: SubData,
    val nullDir: SomeDirection?,
    val dir1: SomeDirection,
    val dir2: SomeDirection,
    val special: Instant,
    val special2: Error<Int>,
    val list: List<String>,
    val arrayList: ArrayList<String>,
    val arrayDeque: ArrayDeque<String>,
    val set: Set<String>,
    val hashSet: HashSet<String>,
    val linkedHashSet: LinkedHashSet<String>,
    val map: NamesMap<Int>,
    val hashMap: HashMap<String, Long>,
    val linkedHashMap: LinkedHashMap<String, Long>,
) {
    data class SubData(
        val nStr: String?,
    )
}

data class GenData<out T: Any>(
    val data: T,
    val int: Int
)

class Wrap<T : Any>(
    val direct: T,
    val inner: GenData<T>,
    val nested: GenData<GenData<T>>,
    val maker: () -> GenData<T>,
)

class Arrays(
    val bytes: ByteArray,
    val strings: Array<String>
)

class Funs(
    val cb: (String) -> Unit,
    val data: () -> GenData<String>,
    val combo: (String) -> GenData<String>
)
