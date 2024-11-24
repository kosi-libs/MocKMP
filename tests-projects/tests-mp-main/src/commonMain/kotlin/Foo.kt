import org.kodein.mock.Mocker
import org.kodein.mock.UsesMocks
import org.kodein.mock.generated.mock

interface Foo {
    fun bar()
}

@UsesMocks(Foo::class)
fun Mocker.mockFoo(
    onBar: () -> Unit = {},
): Foo = mock<Foo>().also {
    every { it.bar() } runs { onBar() }
}
