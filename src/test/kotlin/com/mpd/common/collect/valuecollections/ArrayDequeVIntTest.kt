package com.mpd.common.collect.valuecollections

import androidx.collection.CircularIntArray
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@JvmInline
value class DVIntTestClass(val value: Int): Comparable<DVIntTestClass> {
    override operator fun compareTo(other: DVIntTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<DVIntTestClass> {
        override inline fun fromInt(v: Int) = DVIntTestClass(v)
        override inline fun toInt(v: DVIntTestClass): Int = v.value
    }
}

class ArrayDequeVIntTest {
    private fun simpleDeque(): ArrayDequeVInt<DVIntTestClass> = with (DVIntTestClass) {
        ArrayDequeVInt<DVIntTestClass>().also {
            it.addLast(DVIntTestClass(100))
            it.addLast(DVIntTestClass(200))
            it.addLast(DVIntTestClass(300))
        }
    }

    @Test
    fun constructors() {
        val default = ArrayDequeVInt<DVIntTestClass>()
        assertEquals(0, default.size)

        val byCapacity = ArrayDequeVInt<DVIntTestClass>(10)
        assertEquals(0, byCapacity.size)

        val wrapped = ArrayDequeVInt<DVIntTestClass>(CircularIntArray(4).also { it.addLast(1); it.addLast(2) })
        assertEquals(2, wrapped.size)
    }

    @Test
    fun customNullValue() {
        val q = ArrayDequeVInt<DVIntTestClass>(NULL_VALUE = -1)
        assertEquals(-1, q.NULL_VALUE)
    }

    @Test
    fun bitsLayerAddBitsAndSize() {
        val q = ArrayDequeVInt<DVIntTestClass>()
        assertEquals(true, q.addBits(100))
        assertEquals(true, q.addBits(200))
        assertEquals(2, q.size)
        assertEquals(100, q.peekFirstBits())
        assertEquals(200, q.peekLastBits())
    }

    @Test
    fun anyBitsAndContainsBits() {
        val q = simpleDeque()
        assertEquals(200, q.anyBits { it == 200 })
        assertEquals(q.NULL_VALUE, q.anyBits { it == 999 })
        assertEquals(true, q.containsBits(200))
        assertEquals(false, q.containsBits(999))
    }

    @Test
    fun bitsLayerFirstLastAddRemove() {
        val q = ArrayDequeVInt<DVIntTestClass>()
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
        val q = ArrayDequeVInt<DVIntTestClass>()
        assertEquals(true, q.offerFirstBits(200))
        assertEquals(true, q.offerFirstBits(100))
        assertEquals(true, q.offerLastBits(300))
        assertEquals(100, q.peekFirstBits())
        assertEquals(300, q.peekLastBits())
        assertEquals(3, q.size)
    }

    @Test
    fun bitsLayerPollFirstLastOnEmptyReturnsNull() {
        val q = ArrayDequeVInt<DVIntTestClass>()
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
        val q = ArrayDequeVInt<DVIntTestClass>()
        q.pushBits(100)
        q.pushBits(200)
        // push adds to the front, so 200 is popped first
        assertEquals(200, q.popBits())
        assertEquals(100, q.popBits())
        assertThrows(NoSuchElementException::class.java) { q.popBits() }
    }

    @Test
    fun bitsLayerQueueStyleOfferPollPeek() {
        val q = ArrayDequeVInt<DVIntTestClass>()
        assertEquals(true, q.offerBits(100))
        assertEquals(true, q.offerBits(200))
        assertEquals(100, q.peekBits())
        assertEquals(100, q.pollBits())
        assertEquals(200, q.pollBits())
        assertEquals(q.NULL_VALUE, q.pollBits())
    }

    @Test
    fun bitsAtIndexAndGet() = with (DVIntTestClass) {
        val q = simpleDeque()
        assertEquals(100, q.bitsAtIndex(0))
        assertEquals(200, q.bitsAtIndex(1))
        assertEquals(300, q.bitsAtIndex(2))
        assertEquals(DVIntTestClass(200), q[1])
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
    fun removeAllPredicateRemovesMatchingAndPreservesOrder() = with (DVIntTestClass) {
        val q = simpleDeque()
        assertEquals(true, q.removeAll { it.value >= 200 })
        assertEquals(1, q.size)
        assertEquals(DVIntTestClass(100), q[0])
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
    fun typedAddFirstLastAndPeekPollFirstLast() = with (DVIntTestClass) {
        val q = ArrayDequeVInt<DVIntTestClass>()
        q.addLast(DVIntTestClass(200))
        q.addFirst(DVIntTestClass(100))
        assertEquals(DVIntTestClass(100), q.peekFirst())
        assertEquals(DVIntTestClass(200), q.peekLast())
        assertEquals(DVIntTestClass(100), q.pollFirst())
        assertEquals(DVIntTestClass(200), q.pollLast())
        assertNull(q.pollFirst())
        assertNull(q.pollLast())
        assertNull(q.peekFirst())
        assertNull(q.peekLast())
    }

    @Test
    fun typedOfferFirstLast() = with (DVIntTestClass) {
        val q = ArrayDequeVInt<DVIntTestClass>()
        assertEquals(true, q.offerFirst(DVIntTestClass(100)))
        assertEquals(true, q.offerLast(DVIntTestClass(200)))
        assertEquals(2, q.size)
    }

    @Test
    fun typedRemoveFirstLastThrowWhenEmpty() = with (DVIntTestClass) {
        val q = simpleDeque()
        assertEquals(DVIntTestClass(100), q.removeFirst())
        assertEquals(DVIntTestClass(300), q.removeLast())
        assertEquals(1, q.size)

        val empty = ArrayDequeVInt<DVIntTestClass>()
        assertThrows(NoSuchElementException::class.java) { empty.removeFirst() }
        assertThrows(NoSuchElementException::class.java) { empty.removeLast() }
    }

    @Test
    fun typedPushPop() = with (DVIntTestClass) {
        val q = ArrayDequeVInt<DVIntTestClass>()
        q.push(DVIntTestClass(100))
        q.push(DVIntTestClass(200))
        assertEquals(DVIntTestClass(200), q.pop())
        assertEquals(DVIntTestClass(100), q.pop())
        assertThrows(NoSuchElementException::class.java) { q.pop() }
    }

    @Test
    fun typedQueueStyleOfferPollPeekElementAndRemove() = with (DVIntTestClass) {
        val q = simpleDeque()
        assertEquals(DVIntTestClass(100), q.peek())
        assertEquals(DVIntTestClass(100), q.element())
        assertEquals(true, q.offer(DVIntTestClass(400)))
        assertEquals(DVIntTestClass(100), q.poll())
        assertEquals(DVIntTestClass(200), q.remove())
        assertEquals(2, q.size)

        val empty = ArrayDequeVInt<DVIntTestClass>()
        assertNull(empty.poll())
        assertNull(empty.peek())
        assertThrows(NoSuchElementException::class.java) { empty.remove() }
        assertThrows(NoSuchElementException::class.java) { empty.element() }
    }

    @Test
    fun typedAddAndContainsAndRemove() = with (DVIntTestClass) {
        val q = ArrayDequeVInt<DVIntTestClass>()
        assertEquals(true, q.add(DVIntTestClass(5)))
        assertEquals(true, q.add(DVIntTestClass(3)))
        assertEquals(2, q.size)
        assertEquals(true, q.contains(DVIntTestClass(5)))
        assertEquals(false, q.contains(DVIntTestClass(999)))
        assertEquals(true, q.remove(DVIntTestClass(5)))
        assertEquals(false, q.contains(DVIntTestClass(5)))
        assertEquals(false, q.remove(DVIntTestClass(999)))
    }

    @Test
    fun asIterableVisitsAllElementsInOrder() = with (DVIntTestClass) {
        val q = simpleDeque()
        val visited = q.asIterable().toList()
        assertEquals(listOf(DVIntTestClass(100), DVIntTestClass(200), DVIntTestClass(300)), visited)
    }

    @Test
    fun asModifiableIterableRemoveRemovesCorrectElementAndPreservesOrder() = with (DVIntTestClass) {
        val q = simpleDeque()
        val iter = q.asModifiableIterable().iterator()
        assertEquals(DVIntTestClass(100), iter.next())
        assertEquals(DVIntTestClass(200), iter.next())
        iter.remove()
        assertEquals(2, q.size)
        assertEquals(listOf(DVIntTestClass(100), DVIntTestClass(300)), q.asIterable().toList())
    }

    @Test
    fun asCollectionGeneric() = with (DVIntTestClass) {
        val q = simpleDeque()
        val generic = q.asCollectionGeneric()
        assertEquals(3, generic.size)
        assertEquals(true, generic.contains(DVIntTestClass(200)))
        assertEquals(true, generic.add(DVIntTestClass(400)))
        assertEquals(4, q.size)
        assertEquals(true, generic.remove(DVIntTestClass(400)))
        assertEquals(3, q.size)
        generic.clear()
        assertEquals(0, q.size)
    }

    @Test
    fun equalsHashCodeAndToStringV() = with (DVIntTestClass) {
        val a = simpleDeque()
        val b = simpleDeque()
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals("{100, 200, 300}", a.toStringV())

        val c = ArrayDequeVInt<DVIntTestClass>().also { it.addLast(DVIntTestClass(1)) }
        assertEquals(false, a == c)
    }
}
