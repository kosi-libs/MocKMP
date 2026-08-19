package foo

import data.Data
import data.Direction
import kotlin.jvm.JvmInline


@RequiresOptIn
annotation class ExperimentalTest

// Applicable to a getter but not to a property, so copying it without its use-site target does not
// compile.
@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class GetterOnly

typealias FooMap<T> = Map<T, List<Pair<Int, Set<String>>>>

@JvmInline
value class InlineString(val value: String)

interface Foo<out T : Any> {
    val roString: String
    var rwString: String
    fun doInt(int: Int)
    fun doPrimitive(string: String, int: Int)
    fun doInterface(bar: Bar)
    // A supertype-typed parameter: accepts a constraint narrower than the parameter itself.
    fun doAny(any: Any)
    fun doEnum(direction: Direction)
    fun doArray(array: Array<String>)
    // A star projection has no component type, so it gets no component-specific placeholder branch.
    fun doStarArray(array: Array<*>)
    fun doAbstract(abs: Abs)
    fun doSealedClass(s: SCls)
    fun doSealedInterface(s: SItf)
    fun doMap(m: FooMap<String>)
    fun newInt(): Int
    fun newString(): String
    fun newStringNullable(): String?
    fun newT(): T
    fun newIdentified(): Identified
    fun newAbsIdentified(): AbsIdentified
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

    @get:GetterOnly
    val annotatedGetter: String

    fun doSomethingInline(param: InlineString)

    interface Sub {
        fun doOp()
    }
}

typealias BarCB = (String) -> Int

interface Bar : Foo<Bar> {
    fun doNothing() {}
    fun doSomething() { doNothing() }
    fun newData(string: String, vararg int: Int): Data
    fun doData(data: Data)
    // The only mockable member with a nullable parameter, which is what isNull/isNotNull need to
    // constrain: doAny takes Any, newStringNullable returns rather than accepts, and equals(Any?) is
    // excluded from mocking by IDENTITY_MEMBERS.
    fun doNullable(s: String?)
    fun doAll(string: String, int: Int, data: Data)
    suspend fun newData(): Data
    suspend fun doSomethingSuspend() { doNothing() }
    suspend fun doSomethingSuspendWithString(str: String)
    fun callback(cb: (String) -> Int)
    fun taCallback(cb: BarCB)
    fun suspendCallback(cb: suspend (String) -> Int)
    // Declared after suspendCallback on purpose: its Function2 placeholder key is the one the
    // suspend callback above also claims as its JVM fallback.
    fun comboCallback(cb: (String, Int) -> Boolean)
    fun <T: Comparable<T>> order(c: Iterable<T>) : List<T>
    // Nothing has no values: unlike doNothing() above, this can never be given a `returns` stub, only
    // a `runs` one that itself throws.
    fun newNever(): Nothing

    interface Sub {
        fun doOp()
    }
}

// Re-declares its identity members as abstract, which Kotlin requires an implementation for.
// Only ever reached implicitly, through Foo.newIdentified below.
interface Identified {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    fun doSomething()
}

abstract class AbsIdentified {
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
    abstract fun doSomething()
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

// A permitted subclass may declare its own type parameters, in its own order.
sealed class SSwapped<out A : Any, out B : Any> {
    class Impl<out X : Any, out Y : Any>(val x: X, val y: Y) : SSwapped<Y, X>()
}

// Only ever reached through the sealed parent below.
class SDep(val s: String)

sealed class SDeps {
    class Impl(val dep: SDep) : SDeps()
}
