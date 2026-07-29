package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.ArrayListVLong
import com.mpd.common.collect.valuecollections.ArrayPriorityBlockingQueueVLong
import com.mpd.common.collect.valuecollections.add
import com.mpd.common.collect.valuecollections.asCollectionGeneric
import com.mpd.common.collect.valuecollections.remove
import com.mpd.common.collect.ArrayPriorityBlockingQueueLong
import com.mpd.common.collect.valuecollections.ValueLongAdapter
import com.mpd.common.collect.valuecollections.contains
import com.mpd.common.collect.valuecollections.element
import com.mpd.common.collect.valuecollections.isEmpty
import com.mpd.common.collect.valuecollections.isNotEmpty
import com.mpd.common.collect.valuecollections.offer
import com.mpd.common.collect.valuecollections.peek
import com.mpd.common.collect.valuecollections.poll
import com.mpd.common.collect.valuecollections.put
import com.mpd.common.collect.valuecollections.take
import com.mpd.common.collect.valuecollections.toSetGeneric
import com.mpd.common.collect.valuecollections.toStringV
import org.junit.jupiter.api.Assertions.assertThrows
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@JvmInline
value class QVLongTestClass(val value: Long): Comparable<QVLongTestClass> {
    override operator fun compareTo(other: QVLongTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<QVLongTestClass> {
        override inline fun fromLong(v: Long) = QVLongTestClass(v)
        override inline fun toLong(v: QVLongTestClass): Long = v.value
    }
}

private val reverseComparator = object : ArrayPriorityBlockingQueueLong.Companion.Comparator {
    override fun invoke(a: Long, b: Long): Int = b.compareTo(a)
}

class ArrayPriorityBlockingQueueVLongTest {
    private fun simpleQueue(): ArrayPriorityBlockingQueueVLong<QVLongTestClass> = with (QVLongTestClass) {
        ArrayPriorityBlockingQueueVLong<QVLongTestClass>().also {
            it.offer(QVLongTestClass(300))
            it.offer(QVLongTestClass(100))
            it.offer(QVLongTestClass(200))
        }
    }

    @Test
    fun constructors() {
        val default = ArrayPriorityBlockingQueueVLong<QVLongTestClass>()
        assertEquals(0, default.size)

        val byCapacity = ArrayPriorityBlockingQueueVLong<QVLongTestClass>(10)
        assertEquals(0, byCapacity.size)

        val wrapped = ArrayPriorityBlockingQueueVLong<QVLongTestClass>(
            ArrayPriorityBlockingQueueLong(listOf(3L, 1L, 2L))
        )
        assertEquals(3, wrapped.size)
    }

    @Test
    fun customNullValue() {
        val q = ArrayPriorityBlockingQueueVLong<QVLongTestClass>(NULL_VALUE = -1)
        assertEquals(-1L, q.NULL_VALUE)
    }

    @Test
    fun isEmptyAndIsNotEmpty() {
        val q = ArrayPriorityBlockingQueueVLong<QVLongTestClass>()
        assertEquals(true, q.isEmpty())
        assertEquals(false, q.isNotEmpty())
        q.addBits(1)
        assertEquals(false, q.isEmpty())
        assertEquals(true, q.isNotEmpty())
    }

    @Test
    fun bitsLayerAddOfferPollPeek() {
        val q = ArrayPriorityBlockingQueueVLong<QVLongTestClass>()
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
    fun typedOfferPollPeek() = with (QVLongTestClass) {
        val q = simpleQueue()
        assertEquals(QVLongTestClass(100), q.peek())
        assertEquals(QVLongTestClass(100), q.poll())
        assertEquals(QVLongTestClass(200), q.poll())
        assertEquals(QVLongTestClass(300), q.poll())
        assertNull(q.poll())
        assertNull(q.peek())
    }

    @Test
    fun typedRemoveAndElementThrowWhenEmpty() = with (QVLongTestClass) {
        val q = simpleQueue()
        assertEquals(QVLongTestClass(100), q.element())
        assertEquals(QVLongTestClass(100), q.remove())
        assertEquals(2, q.size)

        val empty = ArrayPriorityBlockingQueueVLong<QVLongTestClass>()
        assertThrows(NoSuchElementException::class.java, { empty.remove() })
        assertThrows(NoSuchElementException::class.java, { empty.element() })
    }

    @Test
    fun typedAddAndOffer() = with (QVLongTestClass) {
        val q = ArrayPriorityBlockingQueueVLong<QVLongTestClass>()
        assertEquals(true, q.add(QVLongTestClass(5)))
        assertEquals(true, q.offer(QVLongTestClass(3)))
        assertEquals(2, q.size)
        assertEquals(QVLongTestClass(3), q.peek())
    }

    @Test
    fun containsAndRemoveTyped() = with (QVLongTestClass) {
        val q = simpleQueue()
        assertEquals(true, q.contains(QVLongTestClass(200)))
        assertEquals(false, q.contains(QVLongTestClass(999)))
        assertEquals(true, q.remove(QVLongTestClass(200)))
        assertEquals(false, q.contains(QVLongTestClass(200)))
        assertEquals(2, q.size)
        assertEquals(false, q.remove(QVLongTestClass(999)))
    }

    @Test
    fun removeAllPredicate() = with (QVLongTestClass) {
        val q = simpleQueue()
        assertEquals(true, q.removeAll { it.value >= 200 })
        assertEquals(1, q.size)
        assertEquals(true, q.contains(QVLongTestClass(100)))
    }

    @Test
    fun clear() = with (QVLongTestClass) {
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
    fun asIterableVisitsAllElements() = with (QVLongTestClass) {
        val q = simpleQueue()
        val visited = q.asIterable().toList()
        assertEquals(setOf(QVLongTestClass(100), QVLongTestClass(200), QVLongTestClass(300)), visited.toSet())

        val modIter = q.asModifiableIterable().iterator()
        modIter.next()
        modIter.remove()
        assertEquals(2, q.size)
    }

    @Test
    fun asCollectionGeneric() = with (QVLongTestClass) {
        val q = simpleQueue()
        val generic = q.asCollectionGeneric()
        assertEquals(3, generic.size)
        assertEquals(true, generic.contains(QVLongTestClass(200)))
        assertEquals(true, generic.add(QVLongTestClass(400)))
        assertEquals(4, q.size)
        assertEquals(true, generic.remove(QVLongTestClass(400)))
        assertEquals(3, q.size)
        generic.clear()
        assertEquals(0, q.size)
    }

    @Test
    fun equalsHashCodeAndToStringV() = with (QVLongTestClass) {
        val a = ArrayPriorityBlockingQueueVLong<QVLongTestClass>().also { it.offer(QVLongTestClass(1)); it.offer(QVLongTestClass(2)) }
        val b = ArrayPriorityBlockingQueueVLong<QVLongTestClass>().also { it.offer(QVLongTestClass(1)); it.offer(QVLongTestClass(2)) }
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals("{1, 2}", a.toStringV())
    }

    @Test
    fun priorityOrderingWithCustomComparator() = with (QVLongTestClass) {
        val maxHeap = ArrayPriorityBlockingQueueVLong<QVLongTestClass>(
            ArrayPriorityBlockingQueueLong(comparator = reverseComparator)
        )
        maxHeap.offer(QVLongTestClass(1))
        maxHeap.offer(QVLongTestClass(3))
        maxHeap.offer(QVLongTestClass(2))
        assertEquals(QVLongTestClass(3), maxHeap.poll())
        assertEquals(QVLongTestClass(2), maxHeap.poll())
        assertEquals(QVLongTestClass(1), maxHeap.poll())
    }

    @Test
    fun putAndTake() = with (QVLongTestClass) {
        val q = ArrayPriorityBlockingQueueVLong<QVLongTestClass>()
        q.put(QVLongTestClass(10))
        assertEquals(1, q.size)
        assertEquals(QVLongTestClass(10), q.take())
        assertEquals(0, q.size)
    }

    @Test
    fun offerAndPollWithTimeoutSucceedImmediatelyWhenAvailable() = with (QVLongTestClass) {
        val q = simpleQueue()
        assertEquals(true, q.offer(QVLongTestClass(50), 10, TimeUnit.MILLISECONDS))
        assertEquals(QVLongTestClass(50), q.poll(10, TimeUnit.MILLISECONDS))
    }

    @Test
    fun pollWithTimeoutReturnsNullAfterTimingOut() = with (QVLongTestClass) {
        val q = ArrayPriorityBlockingQueueVLong<QVLongTestClass>()
        val start = System.nanoTime()
        val result = q.poll(50, TimeUnit.MILLISECONDS)
        val elapsedMs = (System.nanoTime() - start) / 1_000_000
        assertNull(result)
        assertEquals(true, elapsedMs >= 40)
    }

    @Test
    fun takeBlocksUntilAnotherThreadOffers() = with (QVLongTestClass) {
        val q = ArrayPriorityBlockingQueueVLong<QVLongTestClass>()
        val producer = Thread {
            Thread.sleep(30)
            q.offer(QVLongTestClass(99))
        }
        producer.start()
        val taken = q.take()
        producer.join()
        assertEquals(QVLongTestClass(99), taken)
    }

    @Test
    fun remainingCapacityIsUnbounded() {
        val q = simpleQueue()
        assertEquals(Int.MAX_VALUE, q.remainingCapacity())
    }

    @Test
    fun drainToVariants() = with (QVLongTestClass) {
        val q = simpleQueue()
        val dest = ArrayListVLong<QVLongTestClass>()
        assertEquals(3, q.drainTo(dest))
        assertEquals(0, q.size)
        assertEquals(setOf(QVLongTestClass(100), QVLongTestClass(200), QVLongTestClass(300)), dest.toSetGeneric())

        val q2 = simpleQueue()
        val dest2 = ArrayListVLong<QVLongTestClass>()
        assertEquals(2, q2.drainTo(dest2, 2))
        assertEquals(1, q2.size)
        assertEquals(2, dest2.size)
    }
}
