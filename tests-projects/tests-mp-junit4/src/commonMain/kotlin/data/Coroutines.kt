package data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlin.coroutines.CoroutineContext

// Every kotlinx.coroutines type below is faked as a literal builtin (see the `builtins` map in
// MocKMPProcessor), not by generating a FakeXxx implementation — an inert, usable instance instead of
// a class that would have to override unstable/internal members like Flow.collect. One property per
// row of that map, so each is independently faked and independently assertable — most of these have
// identity equality, so, unlike Data, this class can't be compared for equality against a literal.
class Coroutines(
    val context: CoroutineContext,
    val scope: CoroutineScope,
    val flow: Flow<String>,
    val sharedFlow: SharedFlow<String>,
    val mutableSharedFlow: MutableSharedFlow<String>,
    // StateFlow<T>'s value isn't a context-free literal the way an empty collection is: it embeds a
    // faked T, exactly like a nested fakeXxx() call would (see fakeInitializerOf, needsLazyFake).
    val stateFlow: StateFlow<GenData<String>>,
    val mutableStateFlow: MutableStateFlow<GenData<String>>,
    val nullableStateFlow: StateFlow<GenData<String>?>,
    val channel: Channel<String>,
    val receiveChannel: ReceiveChannel<String>,
    val sendChannel: SendChannel<String>,
    val job: Job,
    val completableJob: CompletableJob,
    val deferred: Deferred<String>,
    val completableDeferred: CompletableDeferred<String>,
    val mutex: Mutex,
    val semaphore: Semaphore,
)

// Exercises the placeholder-accessor branch for a builtin (see generatePlaceholderAccessor): a mocked
// interface's own generated stub needs a placeholder value for an unstubbed call, not just a literal
// baked into a constructed fake.
interface CoroutineService {
    fun events(): Flow<String>
    fun state(): StateFlow<GenData<String>>
    fun consume(flow: Flow<String>)
}
