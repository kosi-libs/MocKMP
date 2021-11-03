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

    internal val regs = HashMap<Pair<Any, String>, MutableList<Pair<List<ArgConstraint>, Any?>>>()

    private val calls = ArrayDeque<Triple<String, List<Any?>, Any?>>()

    public fun clearCalls() { calls.clear() }

    public fun reset() {
        calls.clear()
        regs.clear()
    }

    @JvmName("toNotNullArgConstraint") private fun Any.toArgConstraint() = this as? ArgConstraint ?: ArgConstraint.isEqual(this)
    @JvmName("toNullableArgConstraint") private fun Any?.toArgConstraint() = this?.toArgConstraint() ?: ArgConstraint.isNull()

    public fun <R> register(receiver: Any, name: String, vararg args: Any?): R {
        when (val mode = specialMode) {
            is SpecialMode.DEFINITION -> {
                throw CallDefinition(receiver, name, args.map { it?.let { mode.mapper.toProvided(it) } })
            }
            is SpecialMode.VERIFICATION -> {
                val constraints = args.map { it?.let { mode.mapper.toProvided(it) } } .map { it.toArgConstraint() }
                val call = calls.removeFirstOrNull() ?: throw AssertionError("Expected a call to $name but call list was empty")
                val (callName, callArgs, callRet) = call
                if (name != callName) throw AssertionError("Expected a call to $name, but was a call to $callName")
                if (constraints.size != callArgs.size) throw AssertionError("Expected ${constraints.size} arguments to $name but got ${callArgs.size}")
                constraints.forEachIndexed { i, constraint -> constraint.assert(callArgs[i]) }
                @Suppress("UNCHECKED_CAST") return callRet as R
            }
            null -> {
                val list = regs[receiver to name] ?: throw MockingException("$name has not been mocked")
                val (_, ret) = list.firstOrNull { (constraints, _) -> constraints.size == args.size && constraints.indices.all { constraints[it].isValid(args[it]) } }
                    ?: throw MockingException("$name has not been mocked for arguments ${args.joinToString()}")
                calls.addLast(Triple(name, args.toList(), ret))
                @Suppress("UNCHECKED_CAST") return ret as R
            }
        }
    }

    public inner class CallDsl<T> internal constructor(private val receiver: Any, private val name: String, private val args: List<Any?>) {
        public infix fun returns(ret: T) {
            regs.getOrPut(receiver to name) { ArrayList() }
                .add(args.map { it.toArgConstraint() } to ret)
        }
    }
    public fun <T> every(block: ArgConstraintsBuilder.() -> T) : CallDsl<T> {
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
            if (calls.isNotEmpty()) throw AssertionError("Expected call list to be empty, but got a call to ${calls.firstOrNull()?.first}")
        } finally {
            specialMode = null
        }
    }
}
