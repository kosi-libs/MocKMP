package tests

import data.Data
import data.Direction
import data.GenData
import data.SomeDirection
import org.kodein.mock.Fake
import org.kodein.mock.UsesFakes
import org.kodein.mock.generated.fake
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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

}