package org.kodein.mock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue


class LazyFakeTests {

    @Test
    fun testInitializerDoesNotRunAtConstruction() {
        var ran = false
        LazyFake { ran = true }
        assertFalse(ran)
    }

    @Test
    fun testFirstReadRunsTheInitializer() {
        var ran = false
        val lazy = LazyFake { ran = true; "value" }
        assertFalse(ran)
        assertEquals("value", lazy.value)
        assertTrue(ran)
    }

    @Test
    fun testValueIsBuiltOnlyOnce() {
        var count = 0
        val lazy = LazyFake { count++; Any() }
        val first = lazy.value
        val second = lazy.value
        assertEquals(1, count)
        assertSame(first, second)
    }

    @Test
    fun testInitializerReturningNullIsNotMistakenForUninitialized() {
        var count = 0
        val lazy = LazyFake<String?> { count++; null }
        assertNull(lazy.value)
        assertNull(lazy.value)
        // A second read must still be answered from the held value, not by running the initializer
        // again because a `null` result looks the same as "nothing built yet".
        assertEquals(1, count)
    }

    @Test
    fun testAssignmentBeforeFirstReadWinsAndTheInitializerNeverRuns() {
        var ran = false
        val lazy = LazyFake { ran = true; "initial" }
        lazy.value = "assigned"
        assertEquals("assigned", lazy.value)
        assertFalse(ran)
    }

    @Test
    fun testAssignmentAfterFirstReadWins() {
        val lazy = LazyFake { "initial" }
        assertEquals("initial", lazy.value)
        lazy.value = "assigned"
        assertEquals("assigned", lazy.value)
    }

    private class Holder {
        var count = 0
        val x: String by LazyFake { count++; "x" }
        var y: String by LazyFake { count++; "y" }
    }

    @Test
    fun testDelegateValReadsThroughGetValue() {
        val holder = Holder()
        assertEquals("x", holder.x)
        assertEquals("x", holder.x)
        assertEquals(1, holder.count)
    }

    @Test
    fun testDelegateVarKeepsAnAssignedValue() {
        val holder = Holder()
        holder.y = "assigned"
        assertEquals("assigned", holder.y)
        // The initializer must never have run: it would have bumped `count`.
        assertEquals(0, holder.count)
    }

    @Test
    fun testDelegateVarReassignmentAfterReadWins() {
        val holder = Holder()
        assertEquals("y", holder.y)
        holder.y = "reassigned"
        assertEquals("reassigned", holder.y)
        assertEquals(1, holder.count)
    }
}
