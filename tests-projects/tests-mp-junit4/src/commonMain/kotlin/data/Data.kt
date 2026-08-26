package data

import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass
import kotlin.time.Instant

enum class Direction { LEFT, RIGHT }

// A bare reference to the singleton — objects are already their own single instance.
object Singleton {
    val x: Int = 42
}

// Faked by calling its constructor exactly as any other class's would be.
annotation class Labeled(val name: String, val count: Int)

// KClass<T>'s value has to be the actual T being faked — String::class for Typed<String>, not a
// context-free literal the way an empty collection is valid for any element type.
annotation class Typed<T : Any>(val cls: KClass<T>)
class HoldsTyped(val typed: Typed<String>)

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
    val exception: Exception,
    val serializer: KSerializer<T>,
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
    val serializer: KSerializer<String>,
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

// Faked by implementing rather than by constructing: every abstract member below is overridden with
// a faked value or a no-op, and `describe` — which is not abstract — is left to run over them.
interface Service {
    val name: String
    // A var keeps a backing field, so a faked value can be replaced.
    var count: Int
    val dir: SomeDirection
    // A var of a non-builtin type is backed by LazyFake too — its initializer is only deferred, not
    // dropped, so assigning to it still wins over the fake it would otherwise have built.
    var altDir: SomeDirection
    val optional: SomeDirection?
    // Nothing has no values: this getter throws instead of holding one, and the fake still constructs.
    val impossible: Nothing
    val callback: (String) -> GenData<String>
    val suspendCallback: suspend (String) -> Unit
    fun record(entry: String)
    fun size(): Int
    fun latest(): SomeDirection
    fun missing(): SomeDirection?
    suspend fun load(): List<String>
    // A parameter already holds a value of the type the caller chose, so it is the one returned.
    fun <T : Any> convert(value: T): T
    // Nothing holds a T here, so this one throws.
    fun <T : Any> create(): T
    // A vararg is an Array<out T>, not a T: it cannot stand in for the return value either.
    fun <T : Any> first(vararg values: T): T
    // Same as `impossible` above: no value of type Nothing exists, so this throws when called.
    fun fail(): Nothing
    fun describe(): String = "$name/$count"
}

// A property holding another fake (`parent` here) is built on first read rather than at construction
// (see LazyFake), which is what makes a self-referential type fakeable at all: an eager
// `override val parent: Node = fakeNode()` would recurse building its own `parent`, forever.
interface Node {
    val name: String
    val parent: Node
}

// One implementation is generated per faked instantiation, since a fake holds values and no value of
// a type parameter can be produced. A star projection implements the parameter's bound.
interface Box<T : Any> {
    val content: T
    fun replace(content: T): T
}

// A star-projected Container<*, *> is fully bound before content's type is resolved as a member
// (see KSType.withBoundArguments): T's bound is Any, U's is the undeclared-bound default Any?, so
// content ends up Content<Any?> — a concrete, ordinary fake target, not a bare type parameter.
class Content<T>(val value: T)
interface Container<T : Any, U> {
    abstract val content: Content<U>
}

interface Processor<T> {
    fun process(value: T)
}

interface NeverTouched {
    object Instance : NeverTouched
}

// Re-declares its identity members as abstract, which Kotlin requires an implementation for.
interface IdentifiedService {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    fun doSomething()
}

// An abstract class is extended, not implemented: its constructor is called with faked arguments,
// exactly as a concrete class fake would be.
abstract class AbsService(val id: Int, val dir: SomeDirection) {
    abstract val label: String
    abstract fun handle(direction: Direction)
    fun describe(): String = "$id:$label"
}
