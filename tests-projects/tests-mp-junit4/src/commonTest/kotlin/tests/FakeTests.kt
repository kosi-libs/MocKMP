package tests

import data.Data
import data.Direction
import data.Error
import data.GenData
import data.SomeDirection
import org.kodein.mock.Fake
import org.kodein.mock.UsesFakes
import org.kodein.mock.generated.fake
import org.kodein.mock.generated.providePlaceholder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant


class FakeTests {

    @Test
    @UsesFakes(Data::class)
    fun testData() {
        val data = fake<Data>()
        assertEquals(
            Data(
                GenData("", 0),
                GenData(0, 0),
                GenData(emptyMap(), 0),
                Data.SubData(null),
                null,
                SomeDirection(Direction.LEFT, SomeDirection.SubData(null)),
                SomeDirection(Direction.LEFT, SomeDirection.SubData(null)),
                Instant.fromEpochSeconds(0),
                // java.lang.Exception has no structural equals(), so reuse the faked instance's own
                // exception reference here; `code` is still checked against the literal below.
                Error(0, data.special2.exception),
                emptyList(),
                ArrayList(),
                ArrayDeque(),
                emptySet(),
                HashSet(),
                LinkedHashSet(),
                emptyMap(),
                HashMap(),
                LinkedHashMap(),
            ),
            data
        )
    }

    class FakeAny(
        val data: GenData<Any>,
    )

    @Test
    @UsesFakes(FakeAny::class)
    fun testGenDataOfAny() {
        val fake = fake<FakeAny>()
        assertNotNull(fake.data.data)
        assertEquals(0, fake.data.int)
    }

    class FakeLong(
        val data: GenData<Long>,
    )

    @Test
    @UsesFakes(FakeLong::class)
    fun testDataOfLong() {
        val fake = fake<FakeLong>()
        assertEquals(0L, fake.data.data)
        assertEquals(0, fake.data.int)
    }

    class NullGenData<T>(
        val content: T,
    )

    class NonNullGenData<T : Any>(
        val content: T,
    )

    @Test
    @UsesFakes(NullGenData::class, NonNullGenData::class)
    fun testStarProjectedGenerics() {
        assertNull(fake<NullGenData<*>>().content)
        assertNotNull(fake<NonNullGenData<*>>().content)
    }

    @Test
    fun testGenericPlaceholderIsMostGeneral() {
        // `providePlaceholder` is keyed by KClass, which erases type arguments, so one of this
        // build's many GenData<...> fakes has to stand in for all of them: the most general one.
        val placeholder = assertIs<GenData<*>>(providePlaceholder(GenData::class))
        assertEquals(Any::class, placeholder.data::class)
    }

}