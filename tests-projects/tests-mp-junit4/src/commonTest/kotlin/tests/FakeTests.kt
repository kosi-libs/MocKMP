package tests

import data.GenData
import org.kodein.mock.Fake
import org.kodein.mock.UsesFakes
import org.kodein.mock.generated.fake
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class FakeTests {

    @Test
    @UsesFakes(GenData::class)
    fun testDataOfStar() {
        val data = fake<GenData<*>>()
        assertNotNull(data.data)
        assertEquals(0, data.int)
    }

    @Suppress("unused")
    class Fakes {
        @Fake lateinit var longData: GenData<Long>
        @Fake lateinit var anyData: GenData<Any>
    }

    @Test
    @UsesFakes(GenData::class)
    fun testDataOfAny() {
        val data = fake<GenData<Any>>()
        assertNotNull(data.data)
        assertEquals(0, data.int)
    }

    @Test
    fun testDataOfLong() {
        val data = fake<GenData<Long>>()
        assertEquals(0L, data.data)
        assertEquals(0, data.int)
    }

}