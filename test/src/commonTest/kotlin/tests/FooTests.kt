package tests

import foo.Foo
import foo.MockFoo
import org.kodein.micromock.Mocker
import org.kodein.micromock.UsesMocks
import kotlin.test.Test


@UsesMocks(Foo::class)
class FooTests {

    @Test
    fun testFoo() {
        val mocker = Mocker()
        val foo = MockFoo(mocker)

        mocker.on { foo.doInt(isAny()) } returns Unit

        foo.doInt(42)

        mocker.verify { foo.doInt(42) }
    }

}
