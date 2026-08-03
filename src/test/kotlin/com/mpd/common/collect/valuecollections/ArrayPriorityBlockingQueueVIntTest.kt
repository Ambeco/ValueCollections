package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.ArrayListVInt
import com.mpd.common.collect.valuecollections.ArrayPriorityBlockingQueueVInt
import com.mpd.common.collect.valuecollections.ValueIntAdapter
import com.mpd.common.collect.valuecollections.add
import com.mpd.common.collect.valuecollections.asCollectionGeneric
import com.mpd.common.collect.valuecollections.element
import com.mpd.common.collect.valuecollections.offer
import com.mpd.common.collect.valuecollections.peek
import com.mpd.common.collect.valuecollections.poll
import com.mpd.common.collect.valuecollections.put
import com.mpd.common.collect.valuecollections.remove
import com.mpd.common.collect.valuecollections.take
import com.mpd.common.collect.ArrayPriorityBlockingQueueInt
import com.mpd.common.collect.valuecollections.contains
import com.mpd.common.collect.valuecollections.isEmpty
import com.mpd.common.collect.valuecollections.isNotEmpty
import com.mpd.common.collect.valuecollections.toSetGeneric
import com.mpd.common.collect.valuecollections.toStringV
import mpd.com.common.collect.valuecollections.*
import org.junit.jupiter.api.Assertions.assertThrows
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@JvmInline
value class QVIntTestClass(val value: Int): Comparable<QVIntTestClass> {
    override operator fun compareTo(other: QVIntTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<QVIntTestClass> {
        override inline fun fromInt(v: Int) = QVIntTestClass(v)
        override inline fun toInt(v: QVIntTestClass): Int = v.value
    }
}

private val reverseComparator = object : ArrayPriorityBlockingQueueInt.Companion.Comparator {
    override fun invoke(a: Int, b: Int): Int = b.compareTo(a)
}

class ArrayPriorityBlockingQueueVIntTest {
    private fun simpleQueue(): ArrayPriorityBlockingQueueVInt<QVIntTestClass> = with (QVIntTestClass) {
        ArrayPriorityBlockingQueueVInt<QVIntTestClass>().also {
            it.offer(QVIntTestClass(300))
            it.offer(QVIntTestClass(100))
            it.offer(QVIntTestClass(200))
        }
    }

    @Test
    fun constructors() {
        val default = ArrayPriorityBlockingQueueVInt<QVIntTestClass>()
        assertEquals(0, default.size)

        val byCapacity = ArrayPriorityBlockingQueueVInt<QVIntTestClass>(10)
        assertEquals(0, byCapacity.size)

        val wrapped = ArrayPriorityBlockingQueueVInt<QVIntTestClass>(
            ArrayPriorityBlockingQueueInt(
                listOf(3, 1, 2)
            )
        )
        assertEquals(3, wrapped.size)
    }

    @Test
    fun customNullValue() {
        val q = ArrayPriorityBlockingQueueVInt<QVIntTestClass>(NULL_VALUE = -1)
        assertEquals(-1, q.NULL_VALUE)
    }

    @Test
    fun isEmptyAndIsNotEmpty() {
        val q = ArrayPriorityBlockingQueueVInt<QVIntTestClass>()
        assertEquals(true, q.isEmpty())
        assertEquals(false, q.isNotEmpty())
        q.addBits(1)
        assertEquals(false, q.isEmpty())
        assertEquals(true, q.isNotEmpty())
    }

    @Test
    fun bitsLayerAddOfferPollPeek() {
        val q = ArrayPriorityBlockingQueueVInt<QVIntTestClass>()
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
    fun typedOfferPollPeek() = with (    QVIntTestClass) {
        val q = simpleQueue()
        assertEquals(    QVIntTestClass(100), q.peek())
        assertEquals(    QVIntTestClass(100), q.poll())
        assertEquals(    QVIntTestClass(200), q.poll())
        assertEquals(    QVIntTestClass(300), q.poll())
        assertNull(q.poll())
        assertNull(q.peek())
    }

    @Test
    fun typedRemoveAndElementThrowWhenEmpty() = with (    QVIntTestClass) {
        val q = simpleQueue()
        assertEquals(    QVIntTestClass(100), q.element())
        assertEquals(    QVIntTestClass(100), q.remove())
        assertEquals(2, q.size)

        val empty = ArrayPriorityBlockingQueueVInt<QVIntTestClass>()
        assertThrows(NoSuchElementException::class.java, { empty.remove() })
        assertThrows(NoSuchElementException::class.java, { empty.element() })
    }

    @Test
    fun typedAddAndOffer() = with (    QVIntTestClass) {
        val q = ArrayPriorityBlockingQueueVInt<QVIntTestClass>()
        assertEquals(true, q.add(
            QVIntTestClass(
                5
            )
        ))
        assertEquals(true, q.offer(
            QVIntTestClass(
                3
            )
        ))
        assertEquals(2, q.size)
        assertEquals(    QVIntTestClass(3), q.peek())
    }

    @Test
    fun containsAndRemoveTyped() = with (    QVIntTestClass) {
        val q = simpleQueue()
        assertEquals(true, q.contains(
            QVIntTestClass(
                200
            )
        ))
        assertEquals(false, q.contains(
            QVIntTestClass(
                999
            )
        ))
        assertEquals(true, q.remove(
            QVIntTestClass(
                200
            )
        ))
        assertEquals(false, q.contains(
            QVIntTestClass(
                200
            )
        ))
        assertEquals(2, q.size)
        assertEquals(false, q.remove(
            QVIntTestClass(
                999
            )
        ))
    }

    @Test
    fun removeAllPredicate() = with (    QVIntTestClass) {
        val q = simpleQueue()
        assertEquals(true, q.removeAll { it.value >= 200 })
        assertEquals(1, q.size)
        assertEquals(true, q.contains(
            QVIntTestClass(
                100
            )
        ))
    }

    @Test
    fun clear() = with (    QVIntTestClass) {
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
    fun toIterableVisitsAllElements() = with (QVIntTestClass) {
        val q = simpleQueue()
        val visited = q.toIterable().toList()
        assertEquals(setOf(QVIntTestClass(100), QVIntTestClass(200), QVIntTestClass(300)), visited.toSet())
    }

    @Test
    fun asCollectionGeneric() = with (QVIntTestClass) {
        val q = simpleQueue()
        val generic = q.asCollectionGeneric()
        assertEquals(3, generic.size)
        assertEquals(true, generic.contains(QVIntTestClass(200)))
        assertEquals(true, generic.add(QVIntTestClass(400)))
        assertEquals(4, q.size)
        assertEquals(true, generic.remove(QVIntTestClass(400)))
        assertEquals(3, q.size)
        generic.clear()
        assertEquals(0, q.size)
    }

    @Test
    fun equalsHashCodeAndToStringV() = with (QVIntTestClass) {
        val a = ArrayPriorityBlockingQueueVInt<QVIntTestClass>().also { it.offer(QVIntTestClass(1)); it.offer(QVIntTestClass(2)) }
        val b = ArrayPriorityBlockingQueueVInt<QVIntTestClass>().also { it.offer(QVIntTestClass(1)); it.offer(QVIntTestClass(2)) }
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals("{1, 2}", a.toStringV())
    }

    @Test
    fun priorityOrderingWithCustomComparator() = with (QVIntTestClass) {
        val maxHeap = ArrayPriorityBlockingQueueVInt<QVIntTestClass>(
            ArrayPriorityBlockingQueueInt(
                comparator = reverseComparator
            )
        )
        maxHeap.offer(QVIntTestClass(1))
        maxHeap.offer(QVIntTestClass(3))
        maxHeap.offer(QVIntTestClass(2))
        assertEquals(QVIntTestClass(3), maxHeap.poll())
        assertEquals(QVIntTestClass(2), maxHeap.poll())
        assertEquals(QVIntTestClass(1), maxHeap.poll())
    }

    @Test
    fun putAndTake() = with (QVIntTestClass) {
        val q = ArrayPriorityBlockingQueueVInt<QVIntTestClass>()
        q.put(QVIntTestClass(10))
        assertEquals(1, q.size)
        assertEquals(QVIntTestClass(10), q.take())
        assertEquals(0, q.size)
    }

    @Test
    fun offerAndPollWithTimeoutSucceedImmediatelyWhenAvailable() = with (QVIntTestClass) {
        val q = simpleQueue()
        assertEquals(true, q.offer(QVIntTestClass(50), 10, TimeUnit.MILLISECONDS))
        assertEquals(QVIntTestClass(50), q.poll(10, TimeUnit.MILLISECONDS))
    }

    @Test
    fun pollWithTimeoutReturnsNullAfterTimingOut() = with (QVIntTestClass) {
        val q = ArrayPriorityBlockingQueueVInt<QVIntTestClass>()
        val start = System.nanoTime()
        val result = q.poll(50, TimeUnit.MILLISECONDS)
        val elapsedMs = (System.nanoTime() - start) / 1_000_000
        assertNull(result)
        assertEquals(true, elapsedMs >= 40)
    }

    @Test
    fun takeBlocksUntilAnotherThreadOffers() = with (QVIntTestClass) {
        val q = ArrayPriorityBlockingQueueVInt<QVIntTestClass>()
        val producer = Thread {
            Thread.sleep(30)
            q.offer(QVIntTestClass(99))
        }
        producer.start()
        val taken = q.take()
        producer.join()
        assertEquals(QVIntTestClass(99), taken)
    }

    @Test
    fun remainingCapacityIsUnbounded() {
        val q = simpleQueue()
        assertEquals(Int.MAX_VALUE, q.remainingCapacity())
    }

    @Test
    fun drainToVariants() = with (QVIntTestClass) {
        val q = simpleQueue()
        val dest = ArrayListVInt<QVIntTestClass>()
        assertEquals(3, q.drainTo(dest))
        assertEquals(0, q.size)
        assertEquals(setOf(QVIntTestClass(100), QVIntTestClass(200), QVIntTestClass(300)), dest.toSetGeneric())

        val q2 = simpleQueue()
        val dest2 = ArrayListVInt<QVIntTestClass>()
        assertEquals(2, q2.drainTo(dest2, 2))
        assertEquals(1, q2.size)
        assertEquals(2, dest2.size)
    }
}
