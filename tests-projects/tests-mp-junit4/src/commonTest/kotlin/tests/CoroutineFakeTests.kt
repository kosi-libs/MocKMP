package tests

import data.Coroutines
import data.CoroutineService
import data.GenData
import org.kodein.mock.Mocker
import org.kodein.mock.UsesFakes
import org.kodein.mock.UsesMocks
import org.kodein.mock.generated.fake
import org.kodein.mock.generated.mock
import org.kodein.mock.generated.providePlaceholder
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Covers every kotlinx.coroutines type in the `builtins` map (see MocKMPProcessor): unlike List/Map,
 * most of these have identity equality, so — unlike FakeTests.testData() — each property is asserted
 * individually rather than compared against one literal instance.
 */
class CoroutineFakeTests {

    @Test
    @UsesFakes(Coroutines::class)
    fun testCoroutineTypesAreFaked() = runTest {
        val fake = fake<Coroutines>()

        assertEquals(EmptyCoroutineContext, fake.context)
        // CoroutineScope(EmptyCoroutineContext) is not itself EmptyCoroutineContext: the factory
        // function adds a fresh Job when the context passed in doesn't already have one — a genuine,
        // usable (if inert) scope, not a context-free literal.
        assertTrue(fake.scope.isActive)

        assertEquals(emptyList(), fake.flow.toList())
        assertEquals(emptyList(), fake.sharedFlow.replayCache)
        assertEquals(emptyList(), fake.mutableSharedFlow.replayCache)

        // StateFlow<T>'s value is a nested fake of T, not a context-free literal — unlike a nullable
        // T, which is just null.
        assertEquals(GenData("", 0), fake.stateFlow.value)
        assertEquals(GenData("", 0), fake.mutableStateFlow.value)
        assertNull(fake.nullableStateFlow.value)

        // A genuine, usable Channel() — default (rendezvous) capacity, so nothing to receive without
        // a concurrently suspended sender, and nothing to send without a concurrently suspended
        // receiver — not a context-free literal.
        assertTrue(fake.channel.tryReceive().isFailure)
        assertTrue(fake.receiveChannel.tryReceive().isFailure)
        assertTrue(fake.sendChannel.trySend("x").isFailure)

        assertFalse(fake.job.isCompleted)
        assertFalse(fake.completableJob.isCompleted)
        assertFalse(fake.deferred.isCompleted)
        assertFalse(fake.completableDeferred.isCompleted)

        assertFalse(fake.mutex.isLocked)
        assertEquals(1, fake.semaphore.availablePermits)
    }

    @Test
    fun testCoroutinePlaceholders() {
        // Direct providePlaceholder calls, the same way testGenericPlaceholderIsMostGeneral (in
        // FakeTests) exercises the GenData branch — these are the erased `Xxx::class -> ...` branches
        // generatePlaceholderAccessor emits, gated on kotlinx.coroutines being on this module's
        // classpath (it is, here).
        assertIs<Flow<*>>(providePlaceholder(Flow::class))
        assertIs<Job>(providePlaceholder(Job::class))
        val stateFlowPlaceholder = assertIs<StateFlow<*>>(providePlaceholder(StateFlow::class))
        assertNull(stateFlowPlaceholder.value)
    }

    @Test
    @UsesMocks(CoroutineService::class)
    fun testIsAnyResolvesAPlaceholderFlow() {
        val mocker = Mocker()
        val service = mocker.mock<CoroutineService>()
        // isAny<Flow<String>>() needs a stand-in Flow to register the constraint with — resolved
        // through Mocker's placeholder provider, i.e. the same generatePlaceholderAccessor branch
        // testCoroutinePlaceholders exercises directly, this time through the full isAny() path.
        mocker.every { service.consume(isAny()) } returns Unit

        service.consume(emptyFlow())
        mocker.verify { service.consume(isAny()) }
    }
}
