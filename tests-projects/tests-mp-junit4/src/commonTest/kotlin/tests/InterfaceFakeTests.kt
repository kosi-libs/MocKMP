package tests

import data.AbsService
import data.Box
import data.Direction
import data.GenData
import data.IdentifiedService
import data.Service
import data.SomeDirection
import kotlinx.coroutines.test.runTest
import org.kodein.mock.UsesFakes
import org.kodein.mock.generated.fake
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


// An interface (or abstract class) has no constructor to call, so its fake is a generated class
// implementing it: abstract functions do nothing, abstract properties hold fakes.
@UsesFakes(Service::class, Box::class, IdentifiedService::class, AbsService::class)
class InterfaceFakeTests {

    private val someDirection = SomeDirection(Direction.LEFT, SomeDirection.SubData(null))

    @Test
    fun testFakedProperties() {
        val service = fake<Service>()
        assertEquals("", service.name)
        assertEquals(0, service.count)
        assertEquals(someDirection, service.dir)
        // Nullable values are `null`, here as everywhere else in faking.
        assertNull(service.optional)
        assertEquals(GenData("", 0), service.callback(""))
    }

    @Test
    fun testFakedFunctions() {
        val service = fake<Service>()
        // A Unit-returning function is a genuine no-op: it neither throws nor records anything.
        service.record("entry")
        assertEquals(0, service.size())
        assertEquals(someDirection, service.latest())
        assertNull(service.missing())
    }

    @Test
    fun testFakedSuspendFunctions() = runTest {
        val service = fake<Service>()
        assertEquals(emptyList(), service.load())
        service.suspendCallback("")
    }

    @Test
    fun testFakedVarKeepsItsValue() {
        val service = fake<Service>()
        service.count = 42
        assertEquals(42, service.count)
    }

    @Test
    fun testDefaultMemberRunsOverFakedMembers() {
        // `describe` is not abstract, so it is not overridden: it runs, reading the faked members.
        val service = fake<Service>()
        service.count = 7
        assertEquals("/7", service.describe())
    }

    @Test
    fun testGenericReturnTypeReturnsTheMatchingParameter() {
        // `convert` returns whatever type its caller asks for — which is exactly what its parameter
        // holds, so that is what a fake can return.
        assertEquals("value", fake<Service>().convert("value"))
        assertEquals(someDirection, fake<Service>().convert(someDirection))
    }

    @Test
    fun testGenericReturnTypeThrowsWithoutAMatchingParameter() {
        val service = fake<Service>()
        // Nothing holds a T: `create` has no parameter at all, and `first`'s vararg is an Array<out T>.
        val created = assertFailsWith<IllegalStateException> { service.create<String>() }
        assertTrue("create()" in created.message!!, created.message)
        val first = assertFailsWith<IllegalStateException> { service.first("a", "b") }
        assertTrue("first()" in first.message!!, first.message)
    }

    @Test
    fun testNothingTypedMembersThrowWhenReached() {
        // No value of type Nothing exists, so the fake still constructs — only reaching one of these
        // members throws, exactly as a generic member with no matching parameter does above.
        val service = fake<Service>()
        assertFailsWith<UnsupportedOperationException> { service.impossible }
        assertFailsWith<UnsupportedOperationException> { service.fail() }
    }

    @Test
    fun testGenericInterfaceFake() {
        // A star projection is implemented as its parameter's bound, so `content` is an Any.
        val box = fake<Box<*>>()
        assertNotNull(box.content)
        assertEquals(Any::class, box.content::class)
    }

    @Test
    fun testIdentityMembersAreNotFaked() {
        // equals/hashCode are re-declared abstract here, so they must be implemented — but with
        // identity, never with a faked value, which would make every fake equal to every other.
        val one = fake<IdentifiedService>()
        val two = fake<IdentifiedService>()
        assertEquals(one, one)
        assertNotEquals(one, two)
        assertEquals(one.hashCode(), one.hashCode())
        one.doSomething()
    }

    @Test
    fun testAbstractClassFake() {
        val service = fake<AbsService>()
        // The superclass constructor is called with faked arguments...
        assertEquals(0, service.id)
        assertEquals(someDirection, service.dir)
        // ...and only its abstract members are overridden.
        assertEquals("", service.label)
        service.handle(Direction.LEFT)
        assertEquals("0:", service.describe())
    }

}
