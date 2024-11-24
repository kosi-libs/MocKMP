package {PACKAGE}

import org.kodein.mock.Mocker
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

{VISIBILITY} expect fun <T : Any> Mocker.mock(type: KClass<T>): T
{VISIBILITY} inline fun <reified T : Any> Mocker.mock() = mock(T::class)

{VISIBILITY} expect fun <T : Any> fake(type: KType): T
{VISIBILITY} inline fun <reified T : Any> fake(): T = fake(typeOf<T>())

{VISIBILITY} expect fun Mocker.injectMocks(receiver: Any)
