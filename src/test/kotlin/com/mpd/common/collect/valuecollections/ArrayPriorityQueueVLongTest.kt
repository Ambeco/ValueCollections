package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.ArrayPriorityQueueVLong
import com.mpd.common.collect.valuecollections.ValueLongAdapter
import com.mpd.common.collect.valuecollections.add
import com.mpd.common.collect.valuecollections.asCollectionGeneric
import com.mpd.common.collect.valuecollections.element
import com.mpd.common.collect.valuecollections.offer
import com.mpd.common.collect.valuecollections.peek
import com.mpd.common.collect.valuecollections.poll
import com.mpd.common.collect.valuecollections.remove
import com.mpd.common.collect.ArrayPriorityQueueLong
import com.mpd.common.collect.valuecollections.contains
import com.mpd.common.collect.valuecollections.isEmpty
import com.mpd.common.collect.valuecollections.isNotEmpty
import com.mpd.common.collect.valuecollections.toStringV
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@JvmInline
value class QVLong2TestClass(val value: Long): Comparable<QVLong2TestClass> {
    override operator fun compareTo(other: QVLong2TestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<QVLong2TestClass> {
        override inline fun fromLong(v: Long) = QVLong2TestClass(v)
        override inline fun toLong(v: QVLong2TestClass): Long = v.value
    }
}

private val reverseComparator = object : ArrayPriorityQueueLong.Companion.Comparator {
    override fun invoke(a: Long, b: Long): Int = b.compareTo(a)
}

class ArrayPriorityQueueVLongTest {
    private fun simpleQueue(): ArrayPriorityQueueVLong<QVLong2TestClass> = with (QVLong2TestClass) {
        ArrayPriorityQueueVLong<QVLong2TestClass>().also {
            it.offer(QVLong2TestClass(300))
            it.offer(QVLong2TestClass(100))
            it.offer(QVLong2TestClass(200))
        }
    }

    @Test
    fun constructors() {
        val default = ArrayPriorityQueueVLong<QVLong2TestClass>()
        assertEquals(0, default.size)

        val byCapacity = ArrayPriorityQueueVLong<QVLong2TestClass>(10)
        assertEquals(0, byCapacity.size)

        val wrapped = ArrayPriorityQueueVLong<QVLong2TestClass>(ArrayPriorityQueueLong(listOf(3L, 1L, 2L)))
        assertEquals(3, wrapped.size)
    }

    @Test
    fun customNullValue() {
        val q = ArrayPriorityQueueVLong<QVLong2TestClass>(NULL_VALUE = -1)
        assertEquals(-1L, q.NULL_VALUE)
    }

    @Test
    fun isEmptyAndIsNotEmpty() {
        val q = ArrayPriorityQueueVLong<QVLong2TestClass>()
        assertEquals(true, q.isEmpty())
        assertEquals(false, q.isNotEmpty())
        q.addBits(1)
        assertEquals(false, q.isEmpty())
        assertEquals(true, q.isNotEmpty())
    }

    @Test
    fun bitsLayerAddOfferPollPeek() {
        val q = ArrayPriorityQueueVLong<QVLong2TestClass>()
        assertEquals(true, q.addBits(300))
        assertEquals(true, q.offerBits(100))
        assertEquals(true, q.offerBits(200))
        assertEquals(100L, q.peekBits())
        assertEquals(100L, q.pollBits())
        assertEquals(200L, q.pollBits())
        assertEquals(300L, q.pollBits())
        assertEquals(q.NULL_VALUE, q.pollBits())
        assertEquals(q.NULL_VALUE, q.peekBits())
    }

    @Test
    fun anyBitsAndContainsBits() {
        val q = simpleQueue()
        assertEquals(200L, q.anyBits { it == 200L })
        assertEquals(q.NULL_VALUE, q.anyBits { it == 999L })
        assertEquals(true, q.containsBits(200))
        assertEquals(false, q.containsBits(999))
    }

    @Test
    fun typedOfferPollPeek() = with (QVLong2TestClass) {
        val q = simpleQueue()
        assertEquals(QVLong2TestClass(100), q.peek())
        assertEquals(QVLong2TestClass(100), q.poll())
        assertEquals(QVLong2TestClass(200), q.poll())
        assertEquals(QVLong2TestClass(300), q.poll())
        assertNull(q.poll())
        assertNull(q.peek())
    }

    @Test
    fun typedRemoveAndElementThrowWhenEmpty() = with (QVLong2TestClass) {
        val q = simpleQueue()
        assertEquals(QVLong2TestClass(100), q.element())
        assertEquals(QVLong2TestClass(100), q.remove())
        assertEquals(2, q.size)

        val empty = ArrayPriorityQueueVLong<QVLong2TestClass>()
        assertThrows(NoSuchElementException::class.java, { empty.remove() })
        assertThrows(NoSuchElementException::class.java, { empty.element() })
    }

    @Test
    fun typedAddAndOffer() = with (QVLong2TestClass) {
        val q = ArrayPriorityQueueVLong<QVLong2TestClass>()
        assertEquals(true, q.add(QVLong2TestClass(5)))
        assertEquals(true, q.offer(QVLong2TestClass(3)))
        assertEquals(2, q.size)
        assertEquals(QVLong2TestClass(3), q.peek())
    }

    @Test
    fun containsAndRemoveTyped() = with (QVLong2TestClass) {
        val q = simpleQueue()
        assertEquals(true, q.contains(QVLong2TestClass(200)))
        assertEquals(false, q.contains(QVLong2TestClass(999)))
        assertEquals(true, q.remove(QVLong2TestClass(200)))
        assertEquals(false, q.contains(QVLong2TestClass(200)))
        assertEquals(2, q.size)
        assertEquals(false, q.remove(QVLong2TestClass(999)))
    }

    @Test
    fun removeAllPredicate() = with (QVLong2TestClass) {
        val q = simpleQueue()
        assertEquals(true, q.removeAll { it.value >= 200 })
        assertEquals(1, q.size)
        assertEquals(true, q.contains(QVLong2TestClass(100)))
    }

    @Test
    fun clear() = with (QVLong2TestClass) {
        val q = simpleQueue()
        q.clear()
        assertEquals(0, q.size)
        assertEquals(true, q.isEmpty())
    }

    @Test
    fun ensureCapacityAndTrimAreNoOps() {
        val q = simpleQueue()
        assertEquals(false, q.ensureCapacity(1000))
        q.trim(0)
        assertEquals(3, q.size)
    }

    @Test
    fun toIterableVisitsAllElements() = with (QVLong2TestClass) {
        val q = simpleQueue()
        val visited = q.toIterable().toList()
        assertEquals(setOf(QVLong2TestClass(100), QVLong2TestClass(200), QVLong2TestClass(300)), visited.toSet())
    }

    @Test
    fun asCollectionGeneric() = with (QVLong2TestClass) {
        val q = simpleQueue()
        val generic = q.asCollectionGeneric()
        assertEquals(3, generic.size)
        assertEquals(true, generic.contains(QVLong2TestClass(200)))
        assertEquals(true, generic.add(QVLong2TestClass(400)))
        assertEquals(4, q.size)
        assertEquals(true, generic.remove(QVLong2TestClass(400)))
        assertEquals(3, q.size)
        generic.clear()
        assertEquals(0, q.size)
    }

    @Test
    fun equalsHashCodeAndToStringV() = with (QVLong2TestClass) {
        val a = ArrayPriorityQueueVLong<QVLong2TestClass>().also { it.offer(QVLong2TestClass(1)); it.offer(QVLong2TestClass(2)) }
        val b = ArrayPriorityQueueVLong<QVLong2TestClass>().also { it.offer(QVLong2TestClass(1)); it.offer(QVLong2TestClass(2)) }
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals("{1, 2}", a.toStringV())
    }

    @Test
    fun priorityOrderingWithCustomComparator() = with (QVLong2TestClass) {
        val maxHeap = ArrayPriorityQueueVLong<QVLong2TestClass>(
            ArrayPriorityQueueLong(comparator = reverseComparator)
        )
        maxHeap.offer(QVLong2TestClass(1))
        maxHeap.offer(QVLong2TestClass(3))
        maxHeap.offer(QVLong2TestClass(2))
        assertEquals(QVLong2TestClass(3), maxHeap.poll())
        assertEquals(QVLong2TestClass(2), maxHeap.poll())
        assertEquals(QVLong2TestClass(1), maxHeap.poll())
    }
}
