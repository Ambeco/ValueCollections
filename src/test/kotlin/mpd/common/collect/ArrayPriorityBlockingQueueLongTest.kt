package mpd.com.common.collect.valuecollections

import mpd.com.common.collect.ArrayPriorityBlockingQueueLong
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private val reverseComparator = object : ArrayPriorityBlockingQueueLong.Companion.Comparator {
    override fun invoke(a: Long, b: Long): Int = b.compareTo(a)
}

private fun drain(q: ArrayPriorityBlockingQueueLong): List<Long> {
    val result = mutableListOf<Long>()
    while (q.isNotEmpty()) result.add(q.poll())
    return result
}

class ArrayPriorityBlockingQueueLongTest {
    @Test
    fun constructorsAndBasics() {
        val default = ArrayPriorityBlockingQueueLong()
        assertEquals(0, default.size())
        assertEquals(true, default.isEmpty())
        assertEquals(false, default.isNotEmpty())

        val byCapacity = ArrayPriorityBlockingQueueLong(5)
        assertEquals(0, byCapacity.size())

        val fromCollection = ArrayPriorityBlockingQueueLong(listOf(3L, 1L, 2L))
        assertEquals(3, fromCollection.size())
        assertEquals(1L, fromCollection.peek())

        val fromArray = ArrayPriorityBlockingQueueLong(longArrayOf(3, 1, 2))
        assertEquals(3, fromArray.size())
        assertEquals(1L, fromArray.peek())

        val maxHeap = ArrayPriorityBlockingQueueLong(comparator = reverseComparator)
        maxHeap.add(1)
        maxHeap.add(3)
        maxHeap.add(2)
        assertEquals(3L, maxHeap.peek())
    }

    @Test
    fun clear() {
        val q = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L))
        q.clear()
        assertEquals(0, q.size())
        assertEquals(true, q.isEmpty())
    }

    @Test
    fun elementAndPeekAndGet() {
        val q = ArrayPriorityBlockingQueueLong(listOf(3L, 1L, 2L))
        assertEquals(1L, q.element())
        assertEquals(1L, q.peek())
        assertEquals(1L, q[0])
        assertThrows(IndexOutOfBoundsException::class.java) { q[99] }

        val empty = ArrayPriorityBlockingQueueLong()
        assertThrows(NoSuchElementException::class.java) { empty.element() }
        assertNull(empty.peek())
    }

    @Test
    fun containsVariants() {
        val q = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L))
        assertEquals(true, q.contains(2L))
        assertEquals(false, q.contains(99L))
        assertEquals(true, q.containsAll(listOf(1L, 2L)))
        assertEquals(false, q.containsAll(listOf(1L, 99L)))
        assertEquals(true, q.containsAll(longArrayOf(1, 2)))
        assertEquals(false, q.containsAll(longArrayOf(1, 99)))
    }

    @Test
    fun addPlusOfferAndHeapOrder() {
        val q = ArrayPriorityBlockingQueueLong()
        q.add(5)
        q + 3L
        q.offer(8)
        q.add(1)
        assertEquals(4, q.size())
        assertEquals(listOf(1L, 3L, 5L, 8L), drain(q))
    }

    @Test
    fun addAllVariantsReturnValueAndOrder() {
        val byCollection = ArrayPriorityBlockingQueueLong()
        assertEquals(true, byCollection.addAll(listOf(5L, 3L, 8L, 1L)))
        assertEquals(false, byCollection.addAll(emptyList()))
        assertEquals(listOf(1L, 3L, 5L, 8L), drain(byCollection))

        val byArray = ArrayPriorityBlockingQueueLong()
        assertEquals(true, byArray.addAll(longArrayOf(5, 3, 8, 1)))
        assertEquals(false, byArray.addAll(LongArray(0)))
        assertEquals(listOf(1L, 3L, 5L, 8L), drain(byArray))
    }

    @Test
    fun pollOrdersBySmallestFirstAndThrowsWhenEmpty() {
        val q = ArrayPriorityBlockingQueueLong(listOf(9L, 4L, 7L, 1L, 3L))
        assertEquals(listOf(1L, 3L, 4L, 7L, 9L), drain(q))
        assertThrows(NoSuchElementException::class.java) { q.poll() }
    }

    @Test
    fun removeAndMinusRemoveSpecificValue() {
        val q = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L))
        assertEquals(false, q.remove(99L))
        assertEquals(3, q.size())
        assertEquals(true, q.remove(2L))
        assertEquals(2, q.size())
        assertEquals(false, q.contains(2L))
        assertEquals(true, q.contains(1L))
        assertEquals(true, q.contains(3L))

        val q2 = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L))
        q2 - 1L
        assertEquals(2, q2.size())
        assertEquals(false, q2.contains(1L))
    }

    @Test
    fun removeAllVariants() {
        // c.size <= size branch
        val small = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L, 4L))
        assertEquals(true, small.removeAll(listOf(2L, 4L)))
        assertEquals(listOf(1L, 3L), drain(small))

        // c.size > size branch (delegates to removeIf)
        val big = ArrayPriorityBlockingQueueLong(listOf(1L, 2L))
        assertEquals(true, big.removeAll(listOf(1L, 2L, 99L)))
        assertEquals(0, big.size())

        val byArraySmall = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L, 4L))
        assertEquals(true, byArraySmall.removeAll(longArrayOf(2, 4)))
        assertEquals(listOf(1L, 3L), drain(byArraySmall))
    }

    @Test
    fun removeIf() {
        val q = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L, 4L, 5L))
        assertEquals(true, q.removeIf { it % 2 == 0L })
        assertEquals(listOf(1L, 3L, 5L), drain(q))
    }

    @Test
    fun retainAllVariants() {
        val byCollection = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L, 4L))
        assertEquals(true, byCollection.retainAll(listOf(2L, 4L)))
        assertEquals(listOf(2L, 4L), drain(byCollection))

        val byArray = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L, 4L))
        assertEquals(true, byArray.retainAll(longArrayOf(2, 4)))
        assertEquals(listOf(2L, 4L), drain(byArray))
    }

    @Test
    fun toArrayVariants() {
        val q = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L))
        val bigEnough = LongArray(5)
        val returned = q.toArray(bigEnough)
        assertEquals(true, returned === bigEnough)
        assertEquals(3, returned.take(3).sorted().size)

        val tooSmall = LongArray(1)
        val newArray = q.toArray(tooSmall)
        assertEquals(false, newArray === tooSmall)
        assertEquals(3, newArray.size)
    }

    @Test
    fun cloneIsIndependentAndEqual() {
        val original = ArrayPriorityBlockingQueueLong(100, reverseComparator)
        original.addAll(listOf(5L, 3L, 8L, 1L, 9L))
        val copy = original.clone()

        assertEquals(true, original == copy)
        assertEquals(original.hashCode(), copy.hashCode())

        copy.add(42)
        assertEquals(false, original.size() == copy.size())
        assertEquals(false, original.contains(42L))
    }

    @Test
    fun equalsConsidersComparatorAndContent() {
        val a = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L))
        val b = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L))
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())

        val differentComparator =
            ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L), reverseComparator)
        assertEquals(false, a == differentComparator)

        val differentContent = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 99L))
        assertEquals(false, a == differentContent)

        assertEquals(false, a.equals("not a queue"))
    }

    @Test
    fun toStringContainsSizeAndTop() {
        val q = ArrayPriorityBlockingQueueLong(listOf(5L, 3L, 8L))
        val s = q.toString()
        assertEquals(true, s.contains("size=3"))
        assertEquals(true, s.contains("top=3"))

        val empty = ArrayPriorityBlockingQueueLong()
        assertEquals(true, empty.toString().contains("top=null"))
    }

    @Test
    fun forEachVisitsAllElements() {
        val q = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L, 4L))
        var sum = 0L
        q.forEach { sum += it }
        assertEquals(10L, sum)
    }

    @Test
    fun iteratorVisitsAllElementsAndSupportsRemove() {
        val q = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L))
        val iter = q.iterator()
        val visited = mutableListOf<Long>()
        while (iter.hasNext()) visited.add(iter.next())
        assertEquals(setOf(1L, 2L, 3L), visited.toSet())

        val iter2 = q.iterator()
        assertEquals(1L, iter2.next())
        iter2.remove()
        assertEquals(2, q.size())
        assertThrows(IllegalStateException::class.java) { iter2.remove() }
    }

    @Test
    fun iteratorThrowsOnConcurrentModification() {
        val q = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L))
        val iter = q.iterator()
        q.add(4)
        assertThrows(ConcurrentModificationException::class.java) { iter.hasNext() }
    }

    @Test
    fun spliteratorTryAdvanceAndSplitAndCharacteristics() {
        val q = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L, 4L))
        val spliterator = q.spliterator()
        assertEquals(4L, spliterator.estimateSize())
        val split = spliterator.trySplit()
        assertNotNull(split)

        var count = 0
        while (spliterator.tryAdvance(java.util.function.LongConsumer { count++ })) { /* keep advancing */ }
        var splitCount = 0
        while (split!!.tryAdvance(java.util.function.LongConsumer { splitCount++ })) { /* keep advancing */ }
        assertEquals(4, count + splitCount)

        val tiny = ArrayPriorityBlockingQueueLong(listOf(1L)).spliterator()
        assertNull(tiny.trySplit())

        val chars = q.spliterator().characteristics()
        assertEquals(true, chars and java.util.Spliterator.SIZED != 0)
        assertEquals(true, chars and java.util.Spliterator.NONNULL != 0)
    }

    @Test
    fun streamAndParallelStream() {
        val q = ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L, 4L))
        assertEquals(10L, q.stream().sum())
        assertEquals(10L, ArrayPriorityBlockingQueueLong(listOf(1L, 2L, 3L, 4L)).parallelStream().sum())
    }
}
