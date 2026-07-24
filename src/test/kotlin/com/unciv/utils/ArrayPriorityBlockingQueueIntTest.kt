package com.unciv.utils

import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private val reverseComparator = object : ArrayPriorityBlockingQueueInt.Companion.Comparator {
    override fun invoke(a: Int, b: Int): Int = b.compareTo(a)
}

private fun drain(q: ArrayPriorityBlockingQueueInt): List<Int> {
    val result = mutableListOf<Int>()
    while (q.isNotEmpty()) result.add(q.poll())
    return result
}

class ArrayPriorityBlockingQueueIntTest {
    @Test
    fun constructorsAndBasics() {
        val default = ArrayPriorityBlockingQueueInt()
        assertEquals(0, default.size())
        assertEquals(true, default.isEmpty())
        assertEquals(false, default.isNotEmpty())

        val byCapacity = ArrayPriorityBlockingQueueInt(5)
        assertEquals(0, byCapacity.size())

        val fromCollection = ArrayPriorityBlockingQueueInt(listOf(3, 1, 2))
        assertEquals(3, fromCollection.size())
        assertEquals(1, fromCollection.peek())

        val fromArray = ArrayPriorityBlockingQueueInt(intArrayOf(3, 1, 2))
        assertEquals(3, fromArray.size())
        assertEquals(1, fromArray.peek())

        val maxHeap = ArrayPriorityBlockingQueueInt(comparator = reverseComparator)
        maxHeap.add(1)
        maxHeap.add(3)
        maxHeap.add(2)
        assertEquals(3, maxHeap.peek())
    }

    @Test
    fun clear() {
        val q = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3))
        q.clear()
        assertEquals(0, q.size())
        assertEquals(true, q.isEmpty())
    }

    @Test
    fun elementAndPeekAndGet() {
        val q = ArrayPriorityBlockingQueueInt(listOf(3, 1, 2))
        assertEquals(1, q.element())
        assertEquals(1, q.peek())
        assertEquals(1, q[0])
        assertThrows(IndexOutOfBoundsException::class.java) { q[99] }

        val empty = ArrayPriorityBlockingQueueInt()
        assertThrows(NoSuchElementException::class.java) { empty.element() }
        assertNull(empty.peek())
    }

    @Test
    fun containsVariants() {
        val q = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3))
        assertEquals(true, q.contains(2))
        assertEquals(false, q.contains(99))
        assertEquals(true, q.containsAll(listOf(1, 2)))
        assertEquals(false, q.containsAll(listOf(1, 99)))
        assertEquals(true, q.containsAll(intArrayOf(1, 2)))
        assertEquals(false, q.containsAll(intArrayOf(1, 99)))
    }

    @Test
    fun addPlusOfferAndHeapOrder() {
        val q = ArrayPriorityBlockingQueueInt()
        q.add(5)
        q + 3
        q.offer(8)
        q.add(1)
        assertEquals(4, q.size())
        assertEquals(listOf(1, 3, 5, 8), drain(q))
    }

    @Test
    fun addAllVariantsReturnValueAndOrder() {
        val byCollection = ArrayPriorityBlockingQueueInt()
        assertEquals(true, byCollection.addAll(listOf(5, 3, 8, 1)))
        assertEquals(false, byCollection.addAll(emptyList()))
        assertEquals(listOf(1, 3, 5, 8), drain(byCollection))

        val byArray = ArrayPriorityBlockingQueueInt()
        assertEquals(true, byArray.addAll(intArrayOf(5, 3, 8, 1)))
        assertEquals(false, byArray.addAll(IntArray(0)))
        assertEquals(listOf(1, 3, 5, 8), drain(byArray))
    }

    @Test
    fun pollOrdersBySmallestFirstAndThrowsWhenEmpty() {
        val q = ArrayPriorityBlockingQueueInt(listOf(9, 4, 7, 1, 3))
        assertEquals(listOf(1, 3, 4, 7, 9), drain(q))
        assertThrows(NoSuchElementException::class.java) { q.poll() }
    }

    @Test
    fun removeAndMinusRemoveSpecificValue() {
        val q = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3))
        assertEquals(false, q.remove(99))
        assertEquals(3, q.size())
        assertEquals(true, q.remove(2))
        assertEquals(2, q.size())
        assertEquals(false, q.contains(2))
        assertEquals(true, q.contains(1))
        assertEquals(true, q.contains(3))

        val q2 = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3))
        q2 - 1
        assertEquals(2, q2.size())
        assertEquals(false, q2.contains(1))
    }

    @Test
    fun removeAllVariants() {
        // c.size <= size branch
        val small = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3, 4))
        assertEquals(true, small.removeAll(listOf(2, 4)))
        assertEquals(listOf(1, 3), drain(small))

        // c.size > size branch (delegates to removeIf)
        val big = ArrayPriorityBlockingQueueInt(listOf(1, 2))
        assertEquals(true, big.removeAll(listOf(1, 2, 99)))
        assertEquals(0, big.size())

        val byArraySmall = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3, 4))
        assertEquals(true, byArraySmall.removeAll(intArrayOf(2, 4)))
        assertEquals(listOf(1, 3), drain(byArraySmall))
    }

    @Test
    fun removeIf() {
        val q = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3, 4, 5))
        assertEquals(true, q.removeIf { it % 2 == 0 })
        assertEquals(listOf(1, 3, 5), drain(q))
    }

    @Test
    fun retainAllVariants() {
        val byCollection = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3, 4))
        assertEquals(true, byCollection.retainAll(listOf(2, 4)))
        assertEquals(listOf(2, 4), drain(byCollection))

        val byArray = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3, 4))
        assertEquals(true, byArray.retainAll(intArrayOf(2, 4)))
        assertEquals(listOf(2, 4), drain(byArray))
    }

    @Test
    fun toArrayVariants() {
        val q = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3))
        val bigEnough = IntArray(5)
        val returned = q.toArray(bigEnough)
        assertEquals(true, returned === bigEnough)
        assertEquals(3, returned.take(3).sorted().size)

        val tooSmall = IntArray(1)
        val newArray = q.toArray(tooSmall)
        assertEquals(false, newArray === tooSmall)
        assertEquals(3, newArray.size)
    }

    @Test
    fun cloneIsIndependentAndEqual() {
        val original = ArrayPriorityBlockingQueueInt(100, reverseComparator)
        original.addAll(listOf(5, 3, 8, 1, 9))
        val copy = original.clone()

        assertEquals(true, original == copy)
        assertEquals(original.hashCode(), copy.hashCode())

        copy.add(42)
        assertEquals(false, original.size() == copy.size())
        assertEquals(false, original.contains(42))
    }

    @Test
    fun equalsConsidersComparatorAndContent() {
        val a = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3))
        val b = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3))
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())

        val differentComparator = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3), reverseComparator)
        assertEquals(false, a == differentComparator)

        val differentContent = ArrayPriorityBlockingQueueInt(listOf(1, 2, 99))
        assertEquals(false, a == differentContent)

        assertEquals(false, a.equals("not a queue"))
    }

    @Test
    fun toStringContainsSizeAndTop() {
        val q = ArrayPriorityBlockingQueueInt(listOf(5, 3, 8))
        val s = q.toString()
        assertEquals(true, s.contains("size=3"))
        assertEquals(true, s.contains("top=3"))

        val empty = ArrayPriorityBlockingQueueInt()
        assertEquals(true, empty.toString().contains("top=null"))
    }

    @Test
    fun forEachVisitsAllElements() {
        val q = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3, 4))
        var sum = 0
        q.forEach { sum += it }
        assertEquals(10, sum)
    }

    @Test
    fun iteratorVisitsAllElementsAndSupportsRemove() {
        val q = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3))
        val iter = q.iterator()
        val visited = mutableListOf<Int>()
        while (iter.hasNext()) visited.add(iter.next())
        assertEquals(setOf(1, 2, 3), visited.toSet())

        val iter2 = q.iterator()
        assertEquals(1, iter2.next())
        iter2.remove()
        assertEquals(2, q.size())
        assertThrows(IllegalStateException::class.java) { iter2.remove() }
    }

    @Test
    fun iteratorThrowsOnConcurrentModification() {
        val q = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3))
        val iter = q.iterator()
        q.add(4)
        assertThrows(ConcurrentModificationException::class.java) { iter.hasNext() }
    }

    @Test
    fun spliteratorTryAdvanceAndSplitAndCharacteristics() {
        val q = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3, 4))
        val spliterator = q.spliterator()
        assertEquals(4L, spliterator.estimateSize())
        val split = spliterator.trySplit()
        assertNotNull(split)

        var count = 0
        while (spliterator.tryAdvance(java.util.function.IntConsumer { count++ })) { /* keep advancing */ }
        var splitCount = 0
        while (split!!.tryAdvance(java.util.function.IntConsumer { splitCount++ })) { /* keep advancing */ }
        assertEquals(4, count + splitCount)

        val tiny = ArrayPriorityBlockingQueueInt(listOf(1)).spliterator()
        assertNull(tiny.trySplit())

        val chars = q.spliterator().characteristics()
        assertEquals(true, chars and java.util.Spliterator.SIZED != 0)
        assertEquals(true, chars and java.util.Spliterator.NONNULL != 0)
    }

    @Test
    fun streamAndParallelStream() {
        val q = ArrayPriorityBlockingQueueInt(listOf(1, 2, 3, 4))
        assertEquals(10, q.stream().sum())
        assertEquals(10, ArrayPriorityBlockingQueueInt(listOf(1, 2, 3, 4)).parallelStream().sum())
    }
}
