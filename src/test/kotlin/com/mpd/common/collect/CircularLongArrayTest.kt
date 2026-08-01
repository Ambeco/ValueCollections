package mpd.com.common.collect.valuecollections

import com.mpd.common.collect.CircularLongArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CircularLongArrayTest {
    @Test
    fun constructorDefaultsAndValidation() {
        val default = CircularLongArray()
        assertEquals(0, default.size())
        assertTrue(default.isEmpty())

        val byCapacity = CircularLongArray(5)
        assertEquals(0, byCapacity.size())

        assertFailsWith<IllegalArgumentException> { CircularLongArray(0) }
        assertFailsWith<IllegalArgumentException> { CircularLongArray(-1) }
        assertFailsWith<IllegalArgumentException> { CircularLongArray((2 shl 29) + 1) }
    }

    @Test
    fun addLastAndGet() {
        val a = CircularLongArray(4)
        a.addLast(1L)
        a.addLast(2L)
        a.addLast(3L)
        assertEquals(3, a.size())
        assertEquals(1L, a[0])
        assertEquals(2L, a[1])
        assertEquals(3L, a[2])
        assertEquals(1L, a.first)
        assertEquals(3L, a.last)
    }

    @Test
    fun addFirst() {
        val a = CircularLongArray(4)
        a.addFirst(1L)
        a.addFirst(2L)
        a.addFirst(3L)
        assertEquals(3, a.size())
        assertEquals(3L, a[0])
        assertEquals(2L, a[1])
        assertEquals(1L, a[2])
        assertEquals(3L, a.first)
        assertEquals(1L, a.last)
    }

    @Test
    fun popFirstAndPopLast() {
        val a = CircularLongArray(4)
        a.addLast(1L)
        a.addLast(2L)
        a.addLast(3L)
        assertEquals(1L, a.popFirst())
        assertEquals(2, a.size())
        assertEquals(3L, a.popLast())
        assertEquals(1, a.size())
        assertEquals(2L, a.popFirst())
        assertTrue(a.isEmpty())
    }

    @Test
    fun popOnEmptyThrows() {
        val a = CircularLongArray()
        assertFailsWith<IndexOutOfBoundsException> { a.popFirst() }
        assertFailsWith<IndexOutOfBoundsException> { a.popLast() }
    }

    @Test
    fun firstLastOnEmptyThrows() {
        val a = CircularLongArray()
        assertFailsWith<IndexOutOfBoundsException> { a.first }
        assertFailsWith<IndexOutOfBoundsException> { a.last }
    }

    @Test
    fun getOutOfBoundsThrows() {
        val a = CircularLongArray()
        a.addLast(1L)
        assertFailsWith<IndexOutOfBoundsException> { a[-1] }
        assertFailsWith<IndexOutOfBoundsException> { a[1] }
    }

    @Test
    fun clear() {
        val a = CircularLongArray()
        a.addLast(1L)
        a.addLast(2L)
        a.clear()
        assertEquals(0, a.size())
        assertTrue(a.isEmpty())
    }

    @Test
    fun removeFromStart() {
        val a = CircularLongArray()
        a.addLast(1L)
        a.addLast(2L)
        a.addLast(3L)
        a.removeFromStart(0)
        assertEquals(3, a.size())
        a.removeFromStart(-1)
        assertEquals(3, a.size())
        a.removeFromStart(2)
        assertEquals(1, a.size())
        assertEquals(3L, a.first)
        assertFailsWith<IndexOutOfBoundsException> { a.removeFromStart(2) }
    }

    @Test
    fun removeFromEnd() {
        val a = CircularLongArray()
        a.addLast(1L)
        a.addLast(2L)
        a.addLast(3L)
        a.removeFromEnd(0)
        assertEquals(3, a.size())
        a.removeFromEnd(-1)
        assertEquals(3, a.size())
        a.removeFromEnd(2)
        assertEquals(1, a.size())
        assertEquals(1L, a.first)
        assertFailsWith<IndexOutOfBoundsException> { a.removeFromEnd(2) }
    }

    @Test
    fun growsBeyondInitialCapacity() {
        val a = CircularLongArray(2)
        for (i in 0 until 100) a.addLast(i.toLong())
        assertEquals(100, a.size())
        for (i in 0 until 100) assertEquals(i.toLong(), a[i])
    }

    @Test
    fun growsWithWraparound() {
        // capacity 4: fill it, pop from front, then push more so head wraps around the backing array
        val a = CircularLongArray(4)
        a.addLast(1L)
        a.addLast(2L)
        a.addLast(3L)
        a.popFirst()
        a.addLast(4L)
        a.addLast(5L)
        a.addLast(6L)
        assertEquals(5, a.size())
        assertEquals(listOf(2L, 3L, 4L, 5L, 6L), (0 until a.size()).map { a[it] })
    }

    @Test
    fun addFirstGrowsCapacity() {
        val a = CircularLongArray(2)
        for (i in 0 until 100) a.addFirst(i.toLong())
        assertEquals(100, a.size())
        for (i in 0 until 100) assertEquals((99 - i).toLong(), a[i])
    }

    @Test
    fun nonPowerOfTwoCapacityRoundsUp() {
        // capacity 5 should round up to 8 internally; verify by growing exactly to the boundary
        // without an intervening resize changing observable behavior.
        val a = CircularLongArray(5)
        for (i in 0 until 8) a.addLast(i.toLong())
        assertEquals(8, a.size())
        for (i in 0 until 8) assertEquals(i.toLong(), a[i])
    }
}
