package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.CircularLongArray
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@JvmInline
value class DVLongTestClass(val value: Long): Comparable<DVLongTestClass> {
    override operator fun compareTo(other: DVLongTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<DVLongTestClass> {
        override inline fun fromLong(v: Long) = DVLongTestClass(v)
        override inline fun toLong(v: DVLongTestClass): Long = v.value
    }
}

class ArrayDequeVLongTest {
    private fun simpleDeque(): ArrayDequeVLong<DVLongTestClass> = with (DVLongTestClass) {
        ArrayDequeVLong<DVLongTestClass>().also {
            it.addLast(DVLongTestClass(100))
            it.addLast(DVLongTestClass(200))
            it.addLast(DVLongTestClass(300))
        }
    }

    @Test
    fun constructors() {
        val default = ArrayDequeVLong<DVLongTestClass>()
        assertEquals(0, default.size)

        val byCapacity = ArrayDequeVLong<DVLongTestClass>(10)
        assertEquals(0, byCapacity.size)

        val wrapped = ArrayDequeVLong<DVLongTestClass>(CircularLongArray(4).also { it.addLast(1); it.addLast(2) })
        assertEquals(2, wrapped.size)
    }

    @Test
    fun customNullValue() {
        val q = ArrayDequeVLong<DVLongTestClass>(NULL_VALUE = -1)
        assertEquals(-1, q.NULL_VALUE)
    }

    @Test
    fun bitsLayerAddBitsAndSize() {
        val q = ArrayDequeVLong<DVLongTestClass>()
        assertEquals(true, q.addBits(100))
        assertEquals(true, q.addBits(200))
        assertEquals(2, q.size)
        assertEquals(100, q.peekFirstBits())
        assertEquals(200, q.peekLastBits())
    }

    @Test
    fun anyBitsAndContainsBits() {
        val q = simpleDeque()
        assertEquals(200, q.anyBits { it == 200L })
        assertEquals(q.NULL_VALUE, q.anyBits { it == 999L })
        assertEquals(true, q.containsBits(200))
        assertEquals(false, q.containsBits(999))
    }

    @Test
    fun bitsLayerFirstLastAddRemove() {
        val q = ArrayDequeVLong<DVLongTestClass>()
        q.addFirstBits(200)
        q.addFirstBits(100)
        q.addLastBits(300)
        // order should be: 100, 200, 300
        assertEquals(100, q.peekFirstBits())
        assertEquals(300, q.peekLastBits())
        assertEquals(100, q.removeFirstBits())
        assertEquals(300, q.removeLastBits())
        assertEquals(200, q.removeFirstBits())
        assertEquals(0, q.size)
        assertThrows(NoSuchElementException::class.java) { q.removeFirstBits() }
        assertThrows(NoSuchElementException::class.java) { q.removeLastBits() }
    }

    @Test
    fun bitsLayerOfferFirstLast() {
        val q = ArrayDequeVLong<DVLongTestClass>()
        assertEquals(true, q.offerFirstBits(200))
        assertEquals(true, q.offerFirstBits(100))
        assertEquals(true, q.offerLastBits(300))
        assertEquals(100, q.peekFirstBits())
        assertEquals(300, q.peekLastBits())
        assertEquals(3, q.size)
    }

    @Test
    fun bitsLayerPollFirstLastOnEmptyReturnsNull() {
        val q = ArrayDequeVLong<DVLongTestClass>()
        assertEquals(q.NULL_VALUE, q.pollFirstBits())
        assertEquals(q.NULL_VALUE, q.pollLastBits())
        assertEquals(q.NULL_VALUE, q.peekFirstBits())
        assertEquals(q.NULL_VALUE, q.peekLastBits())
    }

    @Test
    fun bitsLayerPollFirstLast() {
        val q = simpleDeque()
        assertEquals(100, q.pollFirstBits())
        assertEquals(300, q.pollLastBits())
        assertEquals(200, q.pollFirstBits())
        assertEquals(q.NULL_VALUE, q.pollFirstBits())
    }

    @Test
    fun bitsLayerPushPop() {
        val q = ArrayDequeVLong<DVLongTestClass>()
        q.pushBits(100)
        q.pushBits(200)
        // push adds to the front, so 200 is popped first
        assertEquals(200, q.popBits())
        assertEquals(100, q.popBits())
        assertThrows(NoSuchElementException::class.java) { q.popBits() }
    }

    @Test
    fun bitsLayerQueueStyleOfferPollPeek() {
        val q = ArrayDequeVLong<DVLongTestClass>()
        assertEquals(true, q.offerBits(100))
        assertEquals(true, q.offerBits(200))
        assertEquals(100, q.peekBits())
        assertEquals(100, q.pollBits())
        assertEquals(200, q.pollBits())
        assertEquals(q.NULL_VALUE, q.pollBits())
    }

    @Test
    fun bitsAtIndexAndGet() = with (DVLongTestClass) {
        val q = simpleDeque()
        assertEquals(100, q.bitsAtIndex(0))
        assertEquals(200, q.bitsAtIndex(1))
        assertEquals(300, q.bitsAtIndex(2))
        assertEquals(DVLongTestClass(200), q[1])
    }

    @Test
    fun removeBitsRemovesFirstMatchAndPreservesOrder() {
        val q = simpleDeque()
        assertEquals(true, q.removeBits(200))
        assertEquals(2, q.size)
        assertEquals(100, q.bitsAtIndex(0))
        assertEquals(300, q.bitsAtIndex(1))
        assertEquals(false, q.removeBits(999))
    }

    @Test
    fun removeAllPredicateRemovesMatchingAndPreservesOrder() = with (DVLongTestClass) {
        val q = simpleDeque()
        assertEquals(true, q.removeAll { it.value >= 200 })
        assertEquals(1, q.size)
        assertEquals(DVLongTestClass(100), q[0])
        assertEquals(false, q.removeAll { it.value >= 999 })
    }

    @Test
    fun clear() {
        val q = simpleDeque()
        q.clear()
        assertEquals(0, q.size)
    }

    @Test
    fun ensureCapacityAndTrimAreNoOps() {
        val q = simpleDeque()
        assertEquals(false, q.ensureCapacity(1000))
        q.trim(0)
        assertEquals(3, q.size)
    }

    @Test
    fun typedAddFirstLastAndPeekPollFirstLast() = with (DVLongTestClass) {
        val q = ArrayDequeVLong<DVLongTestClass>()
        q.addLast(DVLongTestClass(200))
        q.addFirst(DVLongTestClass(100))
        assertEquals(DVLongTestClass(100), q.peekFirst())
        assertEquals(DVLongTestClass(200), q.peekLast())
        assertEquals(DVLongTestClass(100), q.pollFirst())
        assertEquals(DVLongTestClass(200), q.pollLast())
        assertNull(q.pollFirst())
        assertNull(q.pollLast())
        assertNull(q.peekFirst())
        assertNull(q.peekLast())
    }

    @Test
    fun typedOfferFirstLast() = with (DVLongTestClass) {
        val q = ArrayDequeVLong<DVLongTestClass>()
        assertEquals(true, q.offerFirst(DVLongTestClass(100)))
        assertEquals(true, q.offerLast(DVLongTestClass(200)))
        assertEquals(2, q.size)
    }

    @Test
    fun typedRemoveFirstLastThrowWhenEmpty() = with (DVLongTestClass) {
        val q = simpleDeque()
        assertEquals(DVLongTestClass(100), q.removeFirst())
        assertEquals(DVLongTestClass(300), q.removeLast())
        assertEquals(1, q.size)

        val empty = ArrayDequeVLong<DVLongTestClass>()
        assertThrows(NoSuchElementException::class.java) { empty.removeFirst() }
        assertThrows(NoSuchElementException::class.java) { empty.removeLast() }
    }

    @Test
    fun typedPushPop() = with (DVLongTestClass) {
        val q = ArrayDequeVLong<DVLongTestClass>()
        q.push(DVLongTestClass(100))
        q.push(DVLongTestClass(200))
        assertEquals(DVLongTestClass(200), q.pop())
        assertEquals(DVLongTestClass(100), q.pop())
        assertThrows(NoSuchElementException::class.java) { q.pop() }
    }

    @Test
    fun typedQueueStyleOfferPollPeekElementAndRemove() = with (DVLongTestClass) {
        val q = simpleDeque()
        assertEquals(DVLongTestClass(100), q.peek())
        assertEquals(DVLongTestClass(100), q.element())
        assertEquals(true, q.offer(DVLongTestClass(400)))
        assertEquals(DVLongTestClass(100), q.poll())
        assertEquals(DVLongTestClass(200), q.remove())
        assertEquals(2, q.size)

        val empty = ArrayDequeVLong<DVLongTestClass>()
        assertNull(empty.poll())
        assertNull(empty.peek())
        assertThrows(NoSuchElementException::class.java) { empty.remove() }
        assertThrows(NoSuchElementException::class.java) { empty.element() }
    }

    @Test
    fun typedAddAndContainsAndRemove() = with (DVLongTestClass) {
        val q = ArrayDequeVLong<DVLongTestClass>()
        assertEquals(true, q.add(DVLongTestClass(5)))
        assertEquals(true, q.add(DVLongTestClass(3)))
        assertEquals(2, q.size)
        assertEquals(true, q.contains(DVLongTestClass(5)))
        assertEquals(false, q.contains(DVLongTestClass(999)))
        assertEquals(true, q.remove(DVLongTestClass(5)))
        assertEquals(false, q.contains(DVLongTestClass(5)))
        assertEquals(false, q.remove(DVLongTestClass(999)))
    }

    @Test
    fun asIterableVisitsAllElementsInOrder() = with (DVLongTestClass) {
        val q = simpleDeque()
        val visited = q.asIterable().toList()
        assertEquals(listOf(DVLongTestClass(100), DVLongTestClass(200), DVLongTestClass(300)), visited)
    }

    @Test
    fun asModifiableIterableRemoveRemovesCorrectElementAndPreservesOrder() = with (DVLongTestClass) {
        val q = simpleDeque()
        val iter = q.asModifiableIterable().iterator()
        assertEquals(DVLongTestClass(100), iter.next())
        assertEquals(DVLongTestClass(200), iter.next())
        iter.remove()
        assertEquals(2, q.size)
        assertEquals(listOf(DVLongTestClass(100), DVLongTestClass(300)), q.asIterable().toList())
    }

    @Test
    fun asCollectionGeneric() = with (DVLongTestClass) {
        val q = simpleDeque()
        val generic = q.asCollectionGeneric()
        assertEquals(3, generic.size)
        assertEquals(true, generic.contains(DVLongTestClass(200)))
        assertEquals(true, generic.add(DVLongTestClass(400)))
        assertEquals(4, q.size)
        assertEquals(true, generic.remove(DVLongTestClass(400)))
        assertEquals(3, q.size)
        generic.clear()
        assertEquals(0, q.size)
    }

    @Test
    fun equalsHashCodeAndToStringV() = with (DVLongTestClass) {
        val a = simpleDeque()
        val b = simpleDeque()
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals("{100, 200, 300}", a.toStringV())

        val c = ArrayDequeVLong<DVLongTestClass>().also { it.addLast(DVLongTestClass(1)) }
        assertEquals(false, a == c)
    }
}
