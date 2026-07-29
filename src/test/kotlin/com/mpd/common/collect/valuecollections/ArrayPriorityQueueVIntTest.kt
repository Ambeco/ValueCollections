package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.ArrayPriorityQueueVInt
import com.mpd.common.collect.valuecollections.ValueIntAdapter
import com.mpd.common.collect.valuecollections.add
import com.mpd.common.collect.valuecollections.asCollectionGeneric
import com.mpd.common.collect.valuecollections.element
import com.mpd.common.collect.valuecollections.offer
import com.mpd.common.collect.valuecollections.peek
import com.mpd.common.collect.valuecollections.poll
import com.mpd.common.collect.valuecollections.remove
import com.mpd.common.collect.ArrayPriorityQueueInt
import com.mpd.common.collect.valuecollections.contains
import com.mpd.common.collect.valuecollections.isEmpty
import com.mpd.common.collect.valuecollections.isNotEmpty
import com.mpd.common.collect.valuecollections.toStringV
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@JvmInline
value class QVInt2TestClass(val value: Int): Comparable<QVInt2TestClass> {
    override operator fun compareTo(other: QVInt2TestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<QVInt2TestClass> {
        override inline fun fromInt(v: Int) = QVInt2TestClass(v)
        override inline fun toInt(v: QVInt2TestClass): Int = v.value
    }
}

private val reverseComparator = object : ArrayPriorityQueueInt.Companion.Comparator {
    override fun invoke(a: Int, b: Int): Int = b.compareTo(a)
}

class ArrayPriorityQueueVIntTest {
    private fun simpleQueue(): ArrayPriorityQueueVInt<QVInt2TestClass> = with (QVInt2TestClass) {
        ArrayPriorityQueueVInt<QVInt2TestClass>().also {
            it.offer(QVInt2TestClass(300))
            it.offer(QVInt2TestClass(100))
            it.offer(QVInt2TestClass(200))
        }
    }

    @Test
    fun constructors() {
        val default = ArrayPriorityQueueVInt<QVInt2TestClass>()
        assertEquals(0, default.size)

        val byCapacity = ArrayPriorityQueueVInt<QVInt2TestClass>(10)
        assertEquals(0, byCapacity.size)

        val wrapped = ArrayPriorityQueueVInt<QVInt2TestClass>(ArrayPriorityQueueInt(listOf(3, 1, 2)))
        assertEquals(3, wrapped.size)
    }

    @Test
    fun customNullValue() {
        val q = ArrayPriorityQueueVInt<QVInt2TestClass>(NULL_VALUE = -1)
        assertEquals(-1, q.NULL_VALUE)
    }

    @Test
    fun isEmptyAndIsNotEmpty() {
        val q = ArrayPriorityQueueVInt<QVInt2TestClass>()
        assertEquals(true, q.isEmpty())
        assertEquals(false, q.isNotEmpty())
        q.addBits(1)
        assertEquals(false, q.isEmpty())
        assertEquals(true, q.isNotEmpty())
    }

    @Test
    fun bitsLayerAddOfferPollPeek() {
        val q = ArrayPriorityQueueVInt<QVInt2TestClass>()
        assertEquals(true, q.addBits(300))
        assertEquals(true, q.offerBits(100))
        assertEquals(true, q.offerBits(200))
        assertEquals(100, q.peekBits())
        assertEquals(100, q.pollBits())
        assertEquals(200, q.pollBits())
        assertEquals(300, q.pollBits())
        assertEquals(q.NULL_VALUE, q.pollBits())
        assertEquals(q.NULL_VALUE, q.peekBits())
    }

    @Test
    fun anyBitsAndContainsBits() {
        val q = simpleQueue()
        assertEquals(200, q.anyBits { it == 200 })
        assertEquals(q.NULL_VALUE, q.anyBits { it == 999 })
        assertEquals(true, q.containsBits(200))
        assertEquals(false, q.containsBits(999))
    }

    @Test
    fun typedOfferPollPeek() = with (QVInt2TestClass) {
        val q = simpleQueue()
        assertEquals(QVInt2TestClass(100), q.peek())
        assertEquals(QVInt2TestClass(100), q.poll())
        assertEquals(QVInt2TestClass(200), q.poll())
        assertEquals(QVInt2TestClass(300), q.poll())
        assertNull(q.poll())
        assertNull(q.peek())
    }

    @Test
    fun typedRemoveAndElementThrowWhenEmpty() = with (QVInt2TestClass) {
        val q = simpleQueue()
        assertEquals(QVInt2TestClass(100), q.element())
        assertEquals(QVInt2TestClass(100), q.remove())
        assertEquals(2, q.size)

        val empty = ArrayPriorityQueueVInt<QVInt2TestClass>()
        assertThrows(NoSuchElementException::class.java, { empty.remove() })
        assertThrows(NoSuchElementException::class.java, { empty.element() })
    }

    @Test
    fun typedAddAndOffer() = with (QVInt2TestClass) {
        val q = ArrayPriorityQueueVInt<QVInt2TestClass>()
        assertEquals(true, q.add(QVInt2TestClass(5)))
        assertEquals(true, q.offer(QVInt2TestClass(3)))
        assertEquals(2, q.size)
        assertEquals(QVInt2TestClass(3), q.peek())
    }

    @Test
    fun containsAndRemoveTyped() = with (QVInt2TestClass) {
        val q = simpleQueue()
        assertEquals(true, q.contains(QVInt2TestClass(200)))
        assertEquals(false, q.contains(QVInt2TestClass(999)))
        assertEquals(true, q.remove(QVInt2TestClass(200)))
        assertEquals(false, q.contains(QVInt2TestClass(200)))
        assertEquals(2, q.size)
        assertEquals(false, q.remove(QVInt2TestClass(999)))
    }

    @Test
    fun removeAllPredicate() = with (QVInt2TestClass) {
        val q = simpleQueue()
        assertEquals(true, q.removeAll { it.value >= 200 })
        assertEquals(1, q.size)
        assertEquals(true, q.contains(QVInt2TestClass(100)))
    }

    @Test
    fun clear() = with (QVInt2TestClass) {
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
    fun asIterableVisitsAllElements() = with (QVInt2TestClass) {
        val q = simpleQueue()
        val visited = q.asIterable().toList()
        assertEquals(setOf(QVInt2TestClass(100), QVInt2TestClass(200), QVInt2TestClass(300)), visited.toSet())

        val modIter = q.asModifiableIterable().iterator()
        modIter.next()
        modIter.remove()
        assertEquals(2, q.size)
    }

    @Test
    fun asCollectionGeneric() = with (QVInt2TestClass) {
        val q = simpleQueue()
        val generic = q.asCollectionGeneric()
        assertEquals(3, generic.size)
        assertEquals(true, generic.contains(QVInt2TestClass(200)))
        assertEquals(true, generic.add(QVInt2TestClass(400)))
        assertEquals(4, q.size)
        assertEquals(true, generic.remove(QVInt2TestClass(400)))
        assertEquals(3, q.size)
        generic.clear()
        assertEquals(0, q.size)
    }

    @Test
    fun equalsHashCodeAndToStringV() = with (QVInt2TestClass) {
        val a = ArrayPriorityQueueVInt<QVInt2TestClass>().also { it.offer(QVInt2TestClass(1)); it.offer(QVInt2TestClass(2)) }
        val b = ArrayPriorityQueueVInt<QVInt2TestClass>().also { it.offer(QVInt2TestClass(1)); it.offer(QVInt2TestClass(2)) }
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals("{1, 2}", a.toStringV())
    }

    @Test
    fun priorityOrderingWithCustomComparator() = with (QVInt2TestClass) {
        val maxHeap = ArrayPriorityQueueVInt<QVInt2TestClass>(
            ArrayPriorityQueueInt(comparator = reverseComparator)
        )
        maxHeap.offer(QVInt2TestClass(1))
        maxHeap.offer(QVInt2TestClass(3))
        maxHeap.offer(QVInt2TestClass(2))
        assertEquals(QVInt2TestClass(3), maxHeap.poll())
        assertEquals(QVInt2TestClass(2), maxHeap.poll())
        assertEquals(QVInt2TestClass(1), maxHeap.poll())
    }
}
