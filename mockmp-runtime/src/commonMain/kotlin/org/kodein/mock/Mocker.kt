package org.kodein.mock


public class Mocker {
    public class MockingException(message: String) : Exception(message)

    private sealed class SpecialMode {
        object DEFINITION : SpecialMode()
        class VERIFICATION(val exhaustive: Boolean, val inOrder: Boolean) : SpecialMode() {
            val builder = ArgConstraintsBuilder()
        }
    }

    private var specialMode: SpecialMode? = null

    internal class CallDefinition(val receiver: Any?, val method: String, val args: Array<*>) : RuntimeException("This exception should have been caught!")

    internal val regs = HashMap<Pair<Any?, String>, MutableList<Pair<List<ArgConstraint<*>>, Every<*>>>>()

    @Suppress("ArrayInDataClass")
    private data class Call(val receiver: Any?, val method: String, val arguments: Array<*>, val returnValue: Any?)

    private val calls = ArrayDeque<Call>()

    public fun clearCalls() { calls.clear() }

    public fun reset() {
        calls.clear()
        regs.clear()
    }

    private fun methodName(receiver: Any?, methodName: String) = if (receiver == null) methodName else "${receiver::class.simpleName}.$methodName"

    public fun <R> register(receiver: Any?, method: String, vararg args: Any?): R {
        when (val mode = specialMode) {
            is SpecialMode.DEFINITION -> {
                throw CallDefinition(receiver, method, args)
            }
            is SpecialMode.VERIFICATION -> {
                val constraints = mode.builder.getConstraints(args)
                val call = if (mode.exhaustive && mode.inOrder) {
                    val call = calls.removeFirstOrNull()
                        ?: throw AssertionError("Expected a call to ${methodName(receiver, method)} but call list was empty")
                    if (method != call.method || receiver !== receiver)
                        throw AssertionError("Expected a call to ${methodName(receiver, method)}, but was a call to ${methodName(call.receiver, call.method)}")
                    if (constraints.size != call.arguments.size)
                        throw AssertionError("Expected ${constraints.size} arguments to ${methodName(receiver, method)} but got ${call.arguments.size}")
                    @Suppress("UNCHECKED_CAST")
                    constraints.forEachIndexed { i, constraint -> (constraint as ArgConstraint<Any?>).assert("Argument ${i + 1}", call.arguments[i]) }
                    call
                } else {
                    val callIndices = (
                            calls.indices.filter { calls[it].receiver == receiver && calls[it].method == method } .takeIf { it.isNotEmpty() }
                                ?: throw AssertionError("Could not find a call to ${methodName(receiver, method)}")
                            ).filter { calls[it].arguments.size == constraints.size } .takeIf { it.isNotEmpty() }
                                ?: throw AssertionError("Could not find a call to ${methodName(receiver, method)} with ${constraints.size} arguments")
                    val callIndex = if (callIndices.size == 1) {
                        val call = calls[callIndices.single()]
                        @Suppress("UNCHECKED_CAST")
                        constraints.forEachIndexed { i, constraint -> (constraint as ArgConstraint<Any?>).assert("Argument ${i + 1}", call.arguments[i]) }
                        callIndices.single()
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        callIndices.firstOrNull { callIndex -> constraints.indices.all { (constraints[it] as ArgConstraint<Any?>).isValid(calls[callIndex].arguments[it]) } }
                            ?: throw AssertionError("Found ${callIndices.size} calls to ${methodName(receiver, method)}, but none that validates the constraints")
                    }
                    val call = calls[callIndex]
                    if (mode.inOrder) repeat(callIndex + 1) { calls.removeFirst() }
                    else calls.removeAt(callIndex)
                    call
                }
                @Suppress("UNCHECKED_CAST")
                constraints.forEachIndexed { i, constraint -> (constraint.capture as MutableList<Any?>?)?.add(call.arguments[i]) }
                @Suppress("UNCHECKED_CAST")
                return call.returnValue as R
            }
            null -> {
                val list = regs[receiver to method] ?: throw MockingException("${methodName(receiver, method)} has not been mocked")
                val (constraints, every) = list
                    .firstOrNull { (constraints, _) ->
                        constraints.size == args.size && constraints.indices.all {
                            @Suppress("UNCHECKED_CAST")
                            (constraints[it] as ArgConstraint<Any?>).isValid(args[it])
                        }
                    }
                    ?: throw MockingException(
                        "${methodName(receiver, method)} has not been mocked for arguments ${args.joinToString()}\n" +
                        "    Registered mocked:\n" + list.map { it.first.joinToString { it.description } } .joinToString("\n") { "        $it" }
                    )
                @Suppress("UNCHECKED_CAST")
                args.forEachIndexed { i, a -> (constraints[i].capture as? MutableList<Any?>)?.add(a) }
                val ret = every.mocked(args)
                calls.addLast(Call(receiver, method, args, ret))
                @Suppress("UNCHECKED_CAST") return ret as R
            }
        }
    }

    public inner class Every<T> internal constructor(receiver: Any?, method: String) {

        internal var mocked: (Array<*>) -> T = { throw MockingException("${methodName(receiver, method)} has not been mocked") }

        public infix fun returns(ret: T) {
            mocked = { ret }
        }
        public infix fun runs(ret: (Array<*>) -> T) {
            mocked = ret
        }
    }

    public fun <T> every(block: ArgConstraintsBuilder.() -> T) : Every<T> {
        if (specialMode != null) error("Cannot be inside a definition block AND a verification block")
        val mode = SpecialMode.DEFINITION
        specialMode = mode
        val builder = ArgConstraintsBuilder()
        try {
            builder.block()
            error("Expected a Mock call")
        } catch (ex: CallDefinition) {
            val every = Every<T>(ex.receiver, ex.method)
            regs.getOrPut(ex.receiver to ex.method) { ArrayList() }
                .add(builder.getConstraints(ex.args) to every)
            return every
        } finally {
            specialMode = null
        }
    }

    @Deprecated("Renamed every", ReplaceWith("every(block)"), level = DeprecationLevel.ERROR)
    public fun <T> on(block: ArgConstraintsBuilder.() -> T) : Every<T> = every(block)

    public fun verify(exhaustive: Boolean = true, inOrder: Boolean = true, block: ArgConstraintsBuilder.() -> Unit) {
        if (specialMode != null) error("Cannot be inside a definition block AND a verification block")
        val mode = SpecialMode.VERIFICATION(exhaustive, inOrder)
        specialMode = mode
        try {
            mode.builder.block()
            if (exhaustive && calls.isNotEmpty()) {
                val call = calls.first()
                throw AssertionError("Expected call list to be empty, but got a call to ${methodName(call.receiver, call.method)}")
            } else {
                calls.clear()
            }
        } finally {
            specialMode = null
        }
    }
}