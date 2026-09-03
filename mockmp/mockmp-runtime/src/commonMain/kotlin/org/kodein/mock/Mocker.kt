package org.kodein.mock

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty


private typealias RegistrationMap<E> = HashMap<Pair<Any?, String>, MutableList<Pair<List<ArgConstraint<*>>, E>>>

/**
 * Records and answers the calls made to the mocks created from it.
 *
 * A `Mocker` is the hub of a test: create one, obtain mocks from it (`mocker.mock<T>()`, or
 * `mocker.injectMocks(this)` to populate every [Mock]/[Fake] property), give them behaviour with
 * [every] / [everySuspending], exercise the code under test, then check the calls with [verify] /
 * [verifyWithSuspend]. Call [reset] in a `@BeforeTest` method so each test starts from a clean
 * mocker.
 *
 * There is no "relaxed" mode: a method that is called without having been mocked throws a
 * [MockingException].
 *
 * **Not thread-safe.** A `Mocker`, and every mock created from it, belong to a single thread: the
 * registrations, the log of recorded calls and the placeholder references are all held in plain,
 * unsynchronised collections. Calls arriving concurrently corrupt that state rather than failing
 * cleanly — calls go missing or land out of order, and a later [verify] fails for reasons that have
 * nothing to do with the code under test.
 *
 * This is a property of the API, not only of its implementation: [every] and [verify] set a single
 * mode on the mocker for the duration of their block and capture the call by throwing out of it, so
 * they could not be made concurrent by locking. Even a mocked call arriving from another thread
 * *while* such a block runs would be read as part of it.
 *
 * When the code under test dispatches, inject the dispatcher it uses so that mocked calls still run
 * on the test thread, rather than letting it reach a real background dispatcher. Where that is not
 * possible, give each thread its own `Mocker`.
 */
public class Mocker {
    /**
     * Thrown when a mock is used in a way that was never defined: a method with no matching
     * [every] / [everySuspending], a call whose arguments no registered behaviour accepts, a
     * [verify] of a method that was never mocked, or constraints mixed with bare values in one call.
     */
    public class MockingException(message: String) : Exception(message)

    private sealed class SpecialMode {
        object DEFINITION : SpecialMode()
        class VERIFICATION(val exhaustive: Boolean, val inOrder: Boolean, references: References) : SpecialMode() {
            val builder = VerificationBuilder(references)
        }
    }

    private var specialMode: SpecialMode? = null

    internal class CallDefinition(val isSuspend: Boolean, val receiver: Any?, val method: String, val args: Array<*>) : RuntimeNoSTException("This exception should have been caught!")

    private val regFuns = RegistrationMap<Every<*>>()
    private val regSuspendFuns = RegistrationMap<EverySuspend<*>>()

    @Suppress("ArrayInDataClass")
    private data class Call(val receiver: Any?, val method: String, val arguments: Array<*>, val returnValue: Result<Any?>)

    private val calls = ArrayDeque<Call>()

    private val references = References(this)

    /**
     * Drops the log of recorded calls, so a following [verify] only sees calls made after this
     * point. Leaves mocked behaviour and registered references in place.
     */
    public fun clearCalls() { calls.clear() }

    /**
     * Returns the mocker to a clean state: clears the call log, every [every] / [everySuspending]
     * registration, and every [useReference]. Call it in a `@BeforeTest` method so tests do not
     * leak state into one another.
     *
     * The generated placeholder provider is deliberately kept (see `References.reset`), so mocks
     * built once and reset per test keep resolving `isAny<T>()`.
     */
    public fun reset() {
        calls.clear()
        regFuns.clear()
        regSuspendFuns.clear()
        references.reset()
    }

    private fun methodName(receiver: Any?, methodName: String) = if (receiver == null) methodName else "${receiver::class.simpleName}.$methodName"

    private sealed class ProcessResult<R> {
        class Value<R>(val value: R) : ProcessResult<R>()
        object FromRegistration : ProcessResult<Nothing>()
    }

    /**
     * Receivers are matched by *identity*, in every verification mode: "verify the calls made on
     * this mock" means this instance, not one that merely compares equal to it. The generated mocks
     * never override `equals`/`hashCode` (the processor keeps those off the mocker), so this also
     * agrees with how [regFuns]/[regSuspendFuns] key their registrations.
     */
    private fun <E, R> process(isSuspend: Boolean, receiver: Any?, method: String, args: Array<*>, regs: RegistrationMap<E>): ProcessResult<R> {
        when (val mode = specialMode) {
            is SpecialMode.DEFINITION -> {
                throw CallDefinition(isSuspend, receiver, method, args)
            }
            is SpecialMode.VERIFICATION -> {
                val constraints = mode.builder.getConstraints(args)
                regs[receiver to method] ?: throw MockingException("Cannot verify ${methodName(receiver, method)} as it has not been mocked")
                val call = if (mode.exhaustive && mode.inOrder) {
                    val call = calls.removeFirstOrNull()
                        ?: throw MockerVerificationLazyAssertionError { "Expected a call to ${methodName(receiver, method)} but call list was empty" }
                    if (method != call.method)
                        throw MockerVerificationLazyAssertionError { "Expected a call to ${methodName(receiver, method)}, but was a call to ${methodName(call.receiver, call.method)}" }
                    if (receiver !== call.receiver) {
                        if (receiver != null && call.receiver != null && receiver::class == call.receiver::class) {
                            throw MockerVerificationLazyAssertionError { "Got a call to ${methodName(receiver, method)}, but expected a different ${receiver::class.simpleName} receiver" }
                        }
                        throw MockerVerificationLazyAssertionError { "Expected a call to ${methodName(receiver, method)}, but was a call to ${methodName(call.receiver, call.method)}" }
                    }
                    if (constraints.size != call.arguments.size)
                        throw MockerVerificationLazyAssertionError { "Expected ${constraints.size} arguments to ${methodName(receiver, method)} but got ${call.arguments.size}" }
                    @Suppress("UNCHECKED_CAST")
                    constraints.forEachIndexed { i, constraint -> (constraint as ArgConstraint<Any?>).assert("Argument ${i + 1}", call.arguments[i]) }
                    call
                } else {
                    val callIndices = (
                            calls.indices.filter { calls[it].receiver === receiver && calls[it].method == method } .takeIf { it.isNotEmpty() }
                                ?: throw MockerVerificationLazyAssertionError { "Could not find a call to ${methodName(receiver, method)}" }
                            ).filter { calls[it].arguments.size == constraints.size } .takeIf { it.isNotEmpty() }
                                ?: throw MockerVerificationLazyAssertionError { "Could not find a call to ${methodName(receiver, method)} with ${constraints.size} arguments" }
                    val callIndex = if (callIndices.size == 1) {
                        val call = calls[callIndices.single()]
                        @Suppress("UNCHECKED_CAST")
                        constraints.forEachIndexed { i, constraint -> (constraint as ArgConstraint<Any?>).assert("Argument ${i + 1}", call.arguments[i]) }
                        callIndices.single()
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        callIndices.firstOrNull { callIndex -> constraints.indices.all { (constraints[it] as ArgConstraint<Any?>).isValid(calls[callIndex].arguments[it]) } }
                            ?: throw MockerVerificationLazyAssertionError { "Found ${callIndices.size} calls to ${methodName(receiver, method)}, but none that validates the constraints" }
                    }
                    val call = calls[callIndex]
                    if (mode.inOrder) repeat(callIndex + 1) { calls.removeFirst() }
                    else calls.removeAt(callIndex)
                    call
                }
                @Suppress("UNCHECKED_CAST")
                constraints.forEachIndexed { i, constraint -> (constraint.capture as MutableList<Any?>?)?.add(call.arguments[i]) }
                val callReturnException = call.returnValue.exceptionOrNull()
                if (callReturnException != null) {
                    throw MockerVerificationThrownAssertionError(callReturnException) { methodName(receiver, method) }
                }
                @Suppress("UNCHECKED_CAST")
                return ProcessResult.Value(call.returnValue.getOrNull() as R)
            }
            null -> {
                @Suppress("UNCHECKED_CAST")
                return ProcessResult.FromRegistration as ProcessResult<R>
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <E, R> registerImpl(isSuspend: Boolean, regs: RegistrationMap<E>, run: E.(Array<*>) -> Any?, receiver: Any?, method: String, args: Array<*>, hasDefault: Boolean, default: () -> R): R {
        when (val result = process<E, R>(isSuspend, receiver, method, args, regs)) {
            is ProcessResult.Value<R> -> return result.value
            is ProcessResult.FromRegistration -> {
                val list = regs[receiver to method]
                val pair = list?.firstOrNull { (constraints, _) ->
                    constraints.size == args.size && constraints.indices.all {
                        (constraints[it] as ArgConstraint<Any?>).isValid(args[it])
                    }
                }
                return when {
                    pair != null -> {
                        val (constraints, every) = pair
                        args.forEachIndexed { i, a -> (constraints[i].capture as? MutableList<Any?>)?.add(a) }
                        val ret = kotlin.runCatching { every.run(args) }
                        calls.addLast(Call(receiver, method, args, ret))
                        ret.getOrThrow() as R
                    }
                    hasDefault -> default()
                    else -> {
                        if (list != null) {
                            throw MockingException(
                                "${methodName(receiver, method)} has not been mocked for arguments ${args.joinToString()}\n" +
                                        "    Registered mocked:\n" + list.map { (constraints, _) -> constraints.joinToString { it.description() } } .joinToString("\n") { "        $it" }
                            )
                        } else {
                            throw MockingException("${methodName(receiver, method)} has not been mocked")
                        }
                    }
                }
            }
        }
    }

    /**
     * Records a call to a non-suspending mocked function and returns the value its registered
     * behaviour produces.
     *
     * Called by generated `MockXxx` code (and by [mockFunction1] & co.) — you should not need to
     * call it yourself.
     *
     * @param receiver The mock the call was made on, or `null` for a mocked function.
     * @param method The mocked function's identifier, arguments types included.
     * @param args The call's arguments.
     * @param default Produces a value when the call matches no registered behaviour, instead of
     * throwing (used for a mocked interface's default method).
     * @throws MockingException if the call matches no registered behaviour and no [default] is given.
     */
    public fun <R> register(receiver: Any?, method: String, vararg args: Any?, default: (() -> R)? = null): R =
        registerImpl(
            isSuspend = false,
            regs = regFuns,
            run = { mocked(it) },
            receiver = receiver,
            method = method,
            args = args,
            hasDefault = default != null,
            default = { (default ?: error("Null default")).invoke() }
        )

    /**
     * [register] for a suspending mocked function.
     *
     * Called by generated `MockXxx` code (and by [mockSuspendFunction1] & co.) — you should not
     * need to call it yourself.
     *
     * @throws MockingException if the call matches no registered behaviour and no `default` is given.
     */
    public suspend fun <R> registerSuspend(receiver: Any?, method: String, vararg args: Any?, default: (suspend () -> R)? = null): R =
        registerImpl(
            isSuspend = true,
            regs = regSuspendFuns,
            run = { mocked(it) },
            receiver = receiver,
            method = method,
            args = args,
            hasDefault = default != null,
            default = { (default ?: error("Null default")).invoke() }
        )

    /**
     * The behaviour of one non-suspending mocked call, returned by [every].
     *
     * Give it a behaviour with [returns] or [runs]. Keep the reference to change that behaviour
     * later — the last one set wins for every subsequent matching call.
     */
    public inner class Every<T> internal constructor(receiver: Any?, method: String) {
        internal var mocked: (Array<*>) -> T = { throw MockingException("${methodName(receiver, method)} has not been mocked") }
        /** Mocks the call to return [ret] — the same instance every time. */
        public infix fun returns(ret: T) {
            mocked = { ret }
        }
        /**
         * Mocks the call to run [ret] and return its result. [ret] receives the call's arguments as
         * an untyped `Array<*>`; its **last expression** is the returned value (a `return` would
         * target the enclosing function and will not compile).
         */
        public infix fun runs(ret: (Array<*>) -> T) {
            mocked = ret
        }
    }

    /**
     * The behaviour of one suspending mocked call, returned by [everySuspending]. See [Every].
     */
    public inner class EverySuspend<T> internal constructor(receiver: Any?, method: String) {
        internal var mocked: suspend (Array<*>) -> T = { throw MockingException("${methodName(receiver, method)} has not been mocked") }
        /** Mocks the call to return [ret] — the same instance every time. */
        public infix fun returns(ret: T) {
            mocked = { ret }
        }
        /**
         * Mocks the call to run [ret] (which may suspend) and return its result. [ret] receives the
         * call's arguments as an untyped `Array<*>`; its **last expression** is the returned value.
         */
        public infix fun runs(ret: suspend (Array<*>) -> T) {
            mocked = ret
        }
    }

    // This will be inlined twice: once for regular functions, and once for suspend functions.
    private inline fun <T, E, ET : E> everyImpl(isSuspend: Boolean, newEvery: (Any?, String) -> ET, map: RegistrationMap<E>, block: ArgConstraintsBuilder.() -> T): ET {
        if (specialMode != null) error("Cannot be inside a definition block AND a verification block")
        specialMode = SpecialMode.DEFINITION
        val builder = ArgConstraintsBuilder(references)
        try {
            builder.block()
            error("Expected a Mock call")
        } catch (call: CallDefinition) {
            if (call.isSuspend != isSuspend) error("Calling a ${if (call.isSuspend) "suspend" else "non suspend"} function inside a ${if (isSuspend) "suspending" else "non suspending"} every block")
            val every = newEvery(call.receiver, call.method)
            map.getOrPut(call.receiver to call.method) { ArrayList() }
                .add(builder.getConstraints(call.args) to every)
            return every
        } finally {
            specialMode = null
        }
    }

    /**
     * Opens a *definition block* for a **non-suspending** mocked function.
     *
     * [block] must make exactly one call on a mock; the constraint functions of its
     * [ArgConstraintsBuilder] receiver ([isAny][ArgConstraintsBuilder.isAny],
     * [isEqual][ArgConstraintsBuilder.isEqual], …) describe which arguments this behaviour applies
     * to. Several `every` blocks can register different behaviours for the same function under
     * different constraints.
     *
     * Use [everySuspending] for a suspending function — mocking a suspending one here fails.
     *
     * @return An [Every] to give the call its behaviour with `returns` / `runs`, kept if you want
     * to change it later.
     */
    public fun <T> every(block: ArgConstraintsBuilder.() -> T) : Every<T> =
        everyImpl(false, ::Every, regFuns) { block() }

    /**
     * [every] for a **suspending** mocked function; [block] may call suspending mocks.
     *
     * You *must* use this — not [every] — for a suspending function, and [verifyWithSuspend] to
     * verify it.
     *
     * @return An [EverySuspend] to give the call its behaviour with `returns` / `runs`.
     */
    public suspend fun <T> everySuspending(block: suspend ArgConstraintsBuilder.() -> T): EverySuspend<T> =
        everyImpl(true, ::EverySuspend, regSuspendFuns) { block() }

    /**
     * Registers a catch-all behaviour for [receiver].[method] taking [argCount] arguments, without
     * needing a coroutine context.
     *
     * [everySuspending] reaches the same registration by *invoking* the suspend mock inside a
     * definition block, which is why it must be `suspend` — even though nothing there ever suspends,
     * the block throwing at the first mocked call. Skipping that dance is what lets
     * `mockSuspendFunctionN` build a mock outside a coroutine, and so lets a generated injector call
     * it. It also avoids `isAny<A>()`, which would require a placeholder for every argument type.
     */
    @PublishedApi
    internal fun <T> everySuspendingCall(receiver: Any?, method: String, argCount: Int): EverySuspend<T> {
        val every = EverySuspend<T>(receiver, method)
        regSuspendFuns.getOrPut(receiver to method) { ArrayList() }
            .add(List(argCount) { ArgConstraint.isAny<Any?>() } to every)
        return every
    }

    /**
     * Backs a `var` property of [receiver] by the mocker: reads return whatever was last written,
     * starting from [default], with no need to mock the getter and setter separately.
     *
     * @param property The mutable property to back, e.g. `Place::name`.
     * @param default The value reads return until the first write.
     */
    public fun <R, T> backProperty(receiver: R, property: KMutableProperty1<R, T>, default: T) {
        var value = default
        // addConstraint rather than isAny(): the setter's recorded argument is never read (only the
        // constraint matters), and isAny() would infer Any? and so require the project to have a
        // `kotlin.Any` placeholder — a dependency this has no business having, and which only holds
        // by coincidence.
        every {
            addConstraint(ArgConstraint.isAny<Any?>())
            register<Unit>(receiver, "set:${property.name}", null)
        } runs {
            @Suppress("UNCHECKED_CAST")
            value = it[0] as T
        }
        every { register<T>(receiver, "get:${property.name}") } runs { value }
    }

    /** Former name of [every]. */
    @Deprecated("Renamed every", ReplaceWith("every(block)"), level = DeprecationLevel.ERROR)
    public fun <T> on(block: ArgConstraintsBuilder.() -> T) : Every<T> = every(block)

    // This will be inlined twice: once for regular functions, and once for suspend functions.
    private inline fun verifyImpl(exhaustive: Boolean, inOrder: Boolean, block: VerificationBuilder.() -> Unit) {
        if (specialMode != null) error("Cannot be inside a definition block AND a verification block")
        val mode = SpecialMode.VERIFICATION(exhaustive, inOrder, references)
        specialMode = mode
        try {
            try {
                mode.builder.block()
                // After block(), so that an exception thrown out of it is not masked by this one. A
                // verify block, unlike an every block, runs to completion and can hold several calls,
                // so a constraint that reached none of them would otherwise vanish here.
                mode.builder.checkNoPendingConstraints()
                if (exhaustive && calls.isNotEmpty()) {
                    val call = calls.first()
                    throw MockerVerificationLazyAssertionError { "Expected call list to be empty, but got a call to ${methodName(call.receiver, call.method)}" }
                } else {
                    calls.clear()
                }
            } finally {
                specialMode = null
            }
        } catch (e: MockerVerificationLazyAssertionError) {
            throw MockerVerificationAssertionError(e.lazyMessage)
        }
    }

    /**
     * Checks the recorded calls against those listed in [block].
     *
     * Each statement in [block] is one expected call, described with the same constraint functions
     * as [every]. A call whose behaviour threw must be listed with [VerificationBuilder.threw] (or
     * [VerificationBuilder.called]).
     *
     * With the defaults, [block] must list **every** recorded call, **in order** — so `verify {}`
     * asserts that no mocked call was made. [clearCalls] narrows the window beforehand.
     *
     * @param exhaustive When `false`, unlisted calls are tolerated (the listed ones are still
     * checked in their relative order).
     * @param inOrder When `false`, the listed calls may be given in any order (all of them must
     * still be listed unless `exhaustive` is also `false`).
     * @throws MockerVerificationAssertionError if the recorded calls do not match [block].
     */
    public fun verify(exhaustive: Boolean = true, inOrder: Boolean = true, block: VerificationBuilder.() -> Unit): Unit =
        verifyImpl(exhaustive, inOrder) { block() }

    /**
     * [verify] run in a suspending context, so [block] can list suspending calls.
     *
     * Unlike [everySuspending], this handles suspending **and** non-suspending calls, so a test
     * that mixes both can verify them all here.
     *
     * @throws MockerVerificationAssertionError if the recorded calls do not match [block].
     */
    public suspend fun verifyWithSuspend(exhaustive: Boolean = true, inOrder: Boolean = true, block: suspend VerificationBuilder.() -> Unit): Unit =
        verifyImpl(exhaustive, inOrder) { block() }

    /**
     * Registers [r] as the instance to use wherever a value of its type is needed — the return of
     * an `isAny<T>()` and every other placeholder, whether [r]'s type is the one asked for directly
     * or one buried inside a placeholder MocKMP would otherwise build. Dropped by [reset].
     *
     * See [ArgConstraintsBuilder] for what a placeholder is and when one is needed.
     */
    public fun useReference(r: Any) {
        references.addReference(r)
    }

    /**
     * Resolves [cls] to a [useReference]-registered value or a builtin, falling back to [orElse]
     * when neither exists.
     *
     * Called by the generated `placeholderXxx()` functions to resolve each of their nested
     * constructor dependencies — a `useReference` for one of those nested types then wins over the
     * generated placeholder value. You should not need to call this yourself.
     */
    public fun getReference(cls: KClass<*>, orElse: () -> Any): Any =
        references.getReference(cls, orElse)

    /**
     * Registers the project-wide placeholder provider generated by the MocKMP KSP processor,
     * used to produce a real instance for a type that [isAny][ArgConstraintsBuilder.isAny] /
     * [isNull][ArgConstraintsBuilder.isNull] / etc. need to return from an `every { }` block but
     * that has no builtin and no [useReference]-registered value.
     *
     * Called automatically by every generated `MockXxx` class's constructor — you should not need
     * to call this yourself.
     */
    public fun registerPlaceholderProvider(provider: (KClass<*>, Mocker) -> Any) {
        references.placeholderProvider = provider
    }
}
