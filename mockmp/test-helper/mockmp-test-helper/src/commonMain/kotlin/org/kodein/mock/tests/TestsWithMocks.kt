package org.kodein.mock.tests

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


public abstract class TestsWithMocks : ITestsWithMocks {
    final override val mocksState: ITestsWithMocks.State = ITestsWithMocks.State()
}
