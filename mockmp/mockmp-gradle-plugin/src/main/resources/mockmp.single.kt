package {PACKAGE}

import org.kodein.mock.Mocker
import kotlin.reflect.typeOf

{VISIBILITY} inline fun <reified T : Any> Mocker.mock() = mock(T::class)

{VISIBILITY} inline fun <reified T : Any> fake(): T = fake(typeOf<T>())
