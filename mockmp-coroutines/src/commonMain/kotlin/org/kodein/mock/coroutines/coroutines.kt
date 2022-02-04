package org.kodein.mock.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import org.kodein.mock.ArgConstraintsBuilder
import org.kodein.mock.Mocker


public suspend fun <T> Mocker.every(block: suspend ArgConstraintsBuilder.() -> T): Mocker.Every<T> {

}
