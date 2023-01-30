package org.kodein.mock

import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty


private typealias RegistrationMap<E> = HashMap<Pair<Any?, String>, MutableList<Pair<List<ArgConstraint<*>>, E>>>

public class Mocker {
    public class MockingException(message: String) : Exception(message)

    private sealed class SpecialMode {
        object DEFINITION : SpecialMode()
        class VERIFICATION(val exhaustive: Boolean, val inOrder: Boolean, references: References) : SpecialMode() {
            val builder = VerificationBuilder(references)
        }
    }

    private var specialMode: SpecialMode? = null

    internal class CallDefinition(val isSuspend: Boolean, val receiver: Any?, val method: String, val args: Array<*>) : RuntimeException("This exception should have been caught!")

    private val regFuns = RegistrationMap<Every<*>>()
    private val regSuspendFuns = RegistrationMap<EverySuspend<*>>()

    @Suppress("ArrayInDataClass")
    private data class Call(val receiver: Any?, val method: String, val arguments: Array<*>, val returnValue: Result<Any?>)

    private val calls = ArrayDeque<Call>()

    private val references = References()

    public fun clearCalls() { calls.clear() }

    public fun reset() {
        calls.clear()
        regFuns.clear()
        regSuspendFuns.clear()
    }

    private fun methodName(receiver: Any?, methodName: String) = if (receiver == null) methodName else "${receiver::class.simpleName}.$methodName"

    private sealed class ProcessResult<R> {
        class Value<R>(val value: R) : ProcessResult<R>()
        object FromRegistration : ProcessResult<Nothing>()
    }

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
                            calls.indices.filter { calls[it].receiver == receiver && calls[it].method == method } .takeIf { it.isNotEmpty() }
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

    public inner class Every<T> internal constructor(receiver: Any?, method: String) {
        internal var mocked: (Array<*>) -> T = { throw MockingException("${methodName(receiver, method)} has not been mocked") }
        public infix fun returns(ret: T) {
            mocked = { ret }
        }
        public infix fun runs(ret: (Array<*>) -> T) {
            mocked = ret
        }
    }

    public inner class EverySuspend<T> internal constructor(receiver: Any?, method: String) {
        internal var mocked: suspend (Array<*>) -> T = { throw MockingException("${methodName(receiver, method)} has not been mocked") }
        public infix fun returns(ret: T) {
            mocked = { ret }
        }
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

    public fun <T> every(block: ArgConstraintsBuilder.() -> T) : Every<T> =
        everyImpl(false, ::Every, regFuns) { block() }

    public suspend fun <T> everySuspending(block: suspend ArgConstraintsBuilder.() -> T): EverySuspend<T> =
        everyImpl(true, ::EverySuspend, regSuspendFuns) { block() }

    public fun <R, T> backProperty(receiver: R, property: KMutableProperty1<R, T>, default: T) {
        var value = default
        every { register<Unit>(receiver, "set:${property.name}", isAny()) } runs {
            @Suppress("UNCHECKED_CAST")
            value = it[0] as T
        }
        every { register<T>(receiver, "get:${property.name}") } runs { value }
    }

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

    public fun verify(exhaustive: Boolean = true, inOrder: Boolean = true, block: VerificationBuilder.() -> Unit): Unit =
        verifyImpl(exhaustive, inOrder) { block() }

    public suspend fun verifyWithSuspend(exhaustive: Boolean = true, inOrder: Boolean = true, block: suspend VerificationBuilder.() -> Unit): Unit =
        verifyImpl(exhaustive, inOrder) { block() }

    public fun useReference(r: Any) {
        references.addReference(r)
    }
}
