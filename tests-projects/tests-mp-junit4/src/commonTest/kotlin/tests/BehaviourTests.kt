@file:Suppress("DEPRECATION")

package tests

import foo.Bar
import foo.CardGame
import foo.Foo
import foo.PlayType
import foo.SItf
import foo.Tournament
import org.kodein.mock.Mocker
import org.kodein.mock.MocKMPNoPlaceholderException
import org.kodein.mock.UsesMocks
import org.kodein.mock.generated.mock
import org.kodein.mock.mockFunction0
import kotlin.test.*


@UsesMocks(Foo::class, Bar::class, CardGame::class)
class BehaviourTests {

    val mocker = Mocker()

    @BeforeTest
    fun setUp() {
        mocker.reset()
    }

    @Test
    fun testLambdaCaptureAtDefinition() {
        val bar = mocker.mock<Bar>()
        val captures = ArrayList<(String) -> Int>()
        mocker.every { bar.callback(isAny(captures)) } returns Unit

        var lambdaValue: String? = null
        bar.callback { lambdaValue = it ; 42 }

        assertNull(lambdaValue)
        captures.single().invoke("Some String")
        assertEquals("Some String", lambdaValue)
    }

    @Test
    fun testLambdaCaptureAtVerification() {
        val bar = mocker.mock<Bar>()
        mocker.every { bar.callback(isAny()) } returns Unit

        var lambdaValue: String? = null
        bar.callback { lambdaValue = it ; 42 }

        val captures = ArrayList<(String) -> Int>()
        mocker.verify { bar.callback(isAny(captures)) }

        assertNull(lambdaValue)
        captures.single().invoke("Some String")
        assertEquals("Some String", lambdaValue)
    }

    @Test
    fun testChangeBehaviour() {
        val foo = mocker.mock<Foo<Bar>>()
        val onNewInt = mocker.every { foo.newInt() }
        onNewInt returns 21
        assertEquals(21, foo.newInt())
        onNewInt returns 42
        assertEquals(42, foo.newInt())
    }

    @Test
    fun testThrows() {
        val bar = mocker.mock<Bar>()
        mocker.every { bar.doNothing() } runs { error("This is a test!") }

        val ex = assertFailsWith<IllegalStateException> { bar.doNothing() }
        assertEquals("This is a test!", ex.message)
    }

    @Test
    fun testNothingReturningFunctionCanOnlyRunAThrow() {
        // No value of type Nothing exists, so `newNever()` cannot be given a `returns` stub — only
        // one that itself never returns.
        val bar = mocker.mock<Bar>()
        mocker.every { bar.newNever() } runs { error("This is a test!") }

        val ex = assertFailsWith<IllegalStateException> { bar.newNever() }
        assertEquals("This is a test!", ex.message)
    }

    @Test
    fun testReturnsNull() {
        val foo = mocker.mock<Foo<Bar>>()
        mocker.every { foo.newStringNullable() } returns null

        assertNull(foo.newStringNullable())

        mocker.verify { assertNull(foo.newStringNullable()) }
    }

    @Test
    fun testProperty() {
        val foo = mocker.mock<Foo<Bar>>()

        mocker.backProperty(foo, Foo<Bar>::rwString, "")

        assertEquals("", foo.rwString)
        foo.rwString = "Test!"
        assertEquals("Test!", foo.rwString)
    }

    @Test
    fun test() {
        val f1: () -> Int = mockFunction0(mocker) { 1 }
        val f2: () -> Int = mockFunction0(mocker) { 2 }
        assertEquals(1 , f1.invoke())
        assertEquals(2 , f2.invoke())
    }

    // isAny<T>() hands back the placeholder inside the every block, so `also` both captures it and
    // passes it on as the argument.
    private fun capturePlaceholder(foo: Foo<Bar>): Bar {
        var captured: Bar? = null
        mocker.every { foo.doInterface(isAny<Bar>().also { captured = it }) } returns Unit
        return assertNotNull(captured)
    }

    @Test
    fun testResetClearsCachedPlaceholders() {
        val foo = mocker.mock<Foo<Bar>>()

        val before = capturePlaceholder(foo)
        mocker.reset()
        // Resolving at all here also proves the placeholder provider survived the reset.
        val after = capturePlaceholder(foo)

        assertNotSame(before, after)
    }

    @Test
    fun testResetClearsUsedReferences() {
        val foo = mocker.mock<Foo<Bar>>()
        val reference = mocker.mock<Bar>()

        mocker.useReference(reference)
        assertSame(reference, capturePlaceholder(foo))

        mocker.reset()

        assertNotSame(reference, capturePlaceholder(foo))
    }

    @Test
    fun testIsInstanceOfUnregisteredType() {
        val game = mocker.mock<CardGame>()
        mocker.every { game.start(isInstanceOf(PlayType.TurnByTurn::class)) } returns Unit

        game.start(PlayType.TurnByTurn())

        mocker.verify {
            game.start(isInstanceOf(PlayType.TurnByTurn::class))
        }
    }

    @Test
    fun testIsInstanceOfWithExplicitTypeArgumentHintsAtTheMistake() {
        val game = mocker.mock<CardGame>()

        // isInstanceOf<PlayType.TurnByTurn>(...) pins its placeholder lookup to PlayType.TurnByTurn
        // itself, which has no generated placeholder (only PlayType, the declared parameter type of
        // start(), does) — the same failure as testIsInstanceOfUnregisteredType, except caused by an
        // explicit type argument the user should never have written.
        val ex = assertFailsWith<RuntimeException> {
            mocker.every { game.start(isInstanceOf<PlayType.TurnByTurn>(PlayType.TurnByTurn::class)) } returns Unit
        }
        assertContains(ex.message!!, "explicit type argument")
    }

    @Test
    fun testIsAnyWithUnregisteredTypeDoesNotGetTheIsInstanceOfHint() {
        val game = mocker.mock<CardGame>()

        // Same missing-placeholder failure as above (PlayType.TurnByTurn has no placeholder), but
        // reached through isAny() rather than isInstanceOf() — the hint must not fire here.
        val ex = assertFailsWith<RuntimeException> {
            mocker.every { game.start(isAny<PlayType.TurnByTurn>()) } returns Unit
        }
        assertFalse("explicit type argument" in ex.message!!)
    }

    @Test
    fun testUnconstructiblePlaceholderThrowsMocKMPNoPlaceholderExceptionPointingAtUseReference() {
        val game = mocker.mock<CardGame>()

        // Tournament has no public constructor, so MocKMP could not generate a Placeholder for it at
        // all (see foo/CardGame.kt) — the generated placeholderTournament() throws
        // MocKMPNoPlaceholderException rather than the generic "Could not find a way to get a
        // reference" wrapper, and the message is specific to this failure, not a generic template.
        val ex = assertFailsWith<MocKMPNoPlaceholderException> {
            mocker.every { game.enter(isAny()) } returns Unit
        }
        assertContains(ex.message!!, "Could not generate a Placeholder for")
        assertContains(ex.message!!, "Tournament")
        assertContains(ex.message!!, "mocker.useReference")
        assertFalse("@FakeProvider" in ex.message!!)
        assertFalse("open an issue" in ex.message!!)

        // mocker.useReference(...) is the fix the message itself points at — prove it actually works.
        mocker.useReference(Tournament.of("worlds"))
        mocker.every { game.enter(isAny()) } returns Unit
        game.enter(Tournament.of("worlds"))
        mocker.verify { game.enter(isAny()) }
    }
}
