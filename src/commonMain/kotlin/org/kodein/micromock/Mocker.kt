package org.kodein.micromock

import kotlin.jvm.JvmName


public class Mocker {
    public class MockingException(message: String) : Exception(message)

    private sealed class SpecialMode {
        val mapper = ReturnMapper()
        class DEFINITION : SpecialMode()
        class VERIFICATION : SpecialMode()
    }

    private var specialMode: SpecialMode? = null

    internal class CallDefinition(val receiver: Any, val name: String, val args: List<*>) : RuntimeException("This exception should have been caught!")

    internal val regs = HashMap<Pair<Any, String>, MutableList<Pair<List<ArgConstraint<*>>, (Array<*>) -> Any?>>>()

    private data class Call(val receiver: Any, val method: String, val arguments: Array<*>, val returnValue: Any?)

    private val calls = ArrayDeque<Call>()

    public fun clearCalls() { calls.clear() }

    public fun reset() {
        calls.clear()
        regs.clear()
    }

    @JvmName("toNotNullArgConstraint") private fun Any.toArgConstraint() = this as? ArgConstraint<*> ?: ArgConstraint.isEqual(this)
    @JvmName("toNullableArgConstraint") private fun Any?.toArgConstraint() = this?.toArgConstraint() ?: ArgConstraint.isNull()

    public fun <R> register(receiver: Any, method: String, vararg args: Any?): R {
        when (val mode = specialMode) {
            is SpecialMode.DEFINITION -> {
                throw CallDefinition(receiver, method, args.map { it?.let { mode.mapper.toProvided(it) } })
            }
            is SpecialMode.VERIFICATION -> {
                val constraints = args.map { it?.let { mode.mapper.toProvided(it) } } .map { it.toArgConstraint() }
                val call = calls.removeFirstOrNull() ?: throw AssertionError("Expected a call to ${receiver::class.simpleName}.$method but call list was empty")
                if (method != call.method || receiver !== receiver) throw AssertionError("Expected a call to ${receiver::class.simpleName}.$method, but was a call to ${call.receiver::class.simpleName}.${call.method}")
                if (constraints.size != call.arguments.size) throw AssertionError("Expected ${constraints.size} arguments to ${receiver::class.simpleName}.$method but got ${call.arguments.size}")
                @Suppress("UNCHECKED_CAST")
                constraints.forEachIndexed { i, constraint -> (constraint as ArgConstraint<Any?>).assert(call.arguments[i]) }
                @Suppress("UNCHECKED_CAST")
                args.forEachIndexed { i, a -> (constraints[i].capture as? MutableList<Any?>)?.add(a) }
                @Suppress("UNCHECKED_CAST")
                return call.returnValue as R
            }
            null -> {
                val list = regs[receiver to method] ?: throw MockingException("${receiver::class.simpleName}.$method has not been mocked")
                val (constraints, retFunction) = list
                    .firstOrNull { (constraints, _) ->
                        constraints.size == args.size && constraints.indices.all {
                            @Suppress("UNCHECKED_CAST")
                            (constraints[it] as ArgConstraint<Any?>).isValid(args[it])
                        }
                    }
                    ?: throw MockingException("${receiver::class.simpleName}.$method has not been mocked for arguments ${args.joinToString()}")
                @Suppress("UNCHECKED_CAST")
                args.forEachIndexed { i, a -> (constraints[i].capture as? MutableList<Any?>)?.add(a) }
                val ret = retFunction(args)
                calls.addLast(Call(receiver, method, args, ret))
                @Suppress("UNCHECKED_CAST") return ret as R
            }
        }
    }

    public inner class CallDsl<T> internal constructor(private val receiver: Any, private val name: String, private val args: List<Any?>) {
        public infix fun returns(ret: T) {
            regs.getOrPut(receiver to name) { ArrayList() }
                .add(args.map { it.toArgConstraint() } to { ret })
        }
        public infix fun runs(ret: (Array<*>) -> T) {
            regs.getOrPut(receiver to name) { ArrayList() }
                .add(args.map { it.toArgConstraint() } to ret)
        }
    }
    public fun <T> on(block: ArgConstraintsBuilder.() -> T) : CallDsl<T> {
        if (specialMode != null) error("Cannot be inside a definition block AND a verification block")
        val mode = SpecialMode.DEFINITION()
        specialMode = mode
        try {
            ArgConstraintsBuilder(mode.mapper).block()
            error("Expected a Mock call")
        } catch (ex: CallDefinition) {
            return CallDsl(ex.receiver, ex.name, ex.args)
        } finally {
            specialMode = null
        }
    }

    public fun verify(block: ArgConstraintsBuilder.() -> Unit) {
        if (specialMode != null) error("Cannot be inside a definition block AND a verification block")
        val mode = SpecialMode.VERIFICATION()
        specialMode = mode
        try {
            ArgConstraintsBuilder(mode.mapper).block()
            if (calls.isNotEmpty()) {
                val call = calls.first()
                throw AssertionError("Expected call list to be empty, but got a call to ${call.receiver::class.simpleName}.${call.method}")
            }
        } finally {
            specialMode = null
        }
    }
}
