@file:Suppress("unused")

package com.unciv.utils

import java.util.Objects
import java.util.Spliterator.CONCURRENT
import java.util.Spliterator.IMMUTABLE
import java.util.Spliterator.NONNULL
import java.util.Spliterator.SIZED
import java.util.Spliterator.SUBSIZED
import java.util.function.IntConsumer
import java.util.stream.IntStream
import java.util.stream.StreamSupport

// A PriorityQueue<Int>, except that it minimizes memory allocations
//
// This does NOT extend any interfaces to avoid boxing Int to Int Objects.
//
// Thread safety: every method synchronizes on `queue`. Note that `queue` is reassigned whenever
// the backing array grows (see resize()); the thread doing the resize keeps holding the monitor of
// the array it originally locked on for the rest of its own call, but a *different* thread that
// enters a synchronized block after the swap will lock on the *new* array instead. During that
// (rare, brief) window two threads can technically be inside "synchronized" sections at once. This
// is a known, accepted limitation of locking on a mutable field rather than a dedicated lock object.
class ArrayPriorityBlockingQueueInt(
    initialCapacity: Int = 100,
    val comparator : Comparator = DefaultComparator
) {
    private var queue: IntArray = IntArray(initialCapacity)
    private var size : Int = 0
    private var mutCounter: Int = 0

    constructor(c: Collection<Int>, comparator : Comparator = DefaultComparator)
            : this(c.size, comparator) {
        addAll(c)
    }
    constructor(c: IntArray, comparator : Comparator = DefaultComparator)
            : this(c.size, comparator) {
        addAll(c)
    }

    fun clear(): Unit = synchronized(queue) {
        size = 0
        ++mutCounter
    }

    fun size() : Int = synchronized(queue) { size }

    fun isEmpty() : Boolean = synchronized(queue) { size == 0 }

    fun isNotEmpty() = synchronized(queue) { size != 0 }

    fun element() : Int = synchronized(queue) {
        if (size == 0) throw NoSuchElementException("Priority queue is empty.")
        queue[0]
    }

    operator fun get(index: Int) : Int = synchronized(queue) {
        if (index !in 0..<size) throw IndexOutOfBoundsException("Index $index out of bounds for priority queue of size $size.")
        queue[index]
    }

    fun contains(value: Int): Boolean = synchronized(queue) {
        for (i in 0..<size) {
            if (queue[i] == value) return@synchronized true
        }
        false
    }

    fun containsAll(c: Collection<Int>): Boolean = synchronized(queue) {
        for (item in c) {
            if (!contains(item)) return@synchronized false
        }
        true
    }

    fun containsAll(c: IntArray): Boolean = synchronized(queue) {
        for (item in c) {
            if (!contains(item)) return@synchronized false
        }
        true
    }

    operator fun plus(value: Int) = add(value)

    fun add(value: Int): Unit = synchronized(queue) {
        if (size == queue.size) {
            resizeUp()
        }
        queue[size] = value
        ++size
        bubbleUp(size-1)
        ++mutCounter
    }

    fun offer(value: Int): Boolean = synchronized(queue) {
        add(value)
        true
    }

    fun addAll(c: Collection<Int>) : Boolean = synchronized(queue) {
        ++mutCounter
        val oldSize = size
        if (size + c.size > queue.size) {
            resize(size + c.size)
        }
        c.forEach { queue[size++] = it }
        // looks inefficient, but 50% do not bubble at all, 25% bubble once, etc.
        for (i in size-1 downTo  oldSize-1) {
            bubbleUp(i)
        }
        ++mutCounter
        c.isNotEmpty()
    }

    fun addAll(c: IntArray) : Boolean = synchronized(queue) {
        ++mutCounter
        val oldSize = size
        if (size + c.size > queue.size) {
            resize(size + c.size)
        }
        c.copyInto(queue, size)
        size = size + c.size
        // looks inefficient, but 50% do not bubble at all, 25% bubble once, etc.
        for (i in oldSize until size) {
            bubbleUp(i)
        }
        ++mutCounter
        c.isNotEmpty()
    }

    fun peek() : Int? = synchronized(queue) { if (size > 0) queue[0] else null }

    fun poll() : Int = synchronized(queue) {
        if (size == 0) throw NoSuchElementException("Priority queue is empty.")
        val top = queue[0]
        fillHole(0)
        ++mutCounter
        top
    }

    operator fun minus(value: Int) = remove(value)

    fun remove(value: Int) : Boolean = synchronized(queue) {
        for (i in 0..<size) {
            if (queue[i] == value) {
                fillHole(i)
                ++mutCounter
                return@synchronized true
            }
        }
        false
    }

    fun removeAll(c: Collection<Int>) : Boolean = synchronized(queue) {
        ++mutCounter
        if (c.size > size) {
            return@synchronized removeIf { c.contains(it) }
        }
        for (item in c) {
            if (!remove(item)) return@synchronized false
        }
        ++mutCounter
        true
    }

    fun removeAll(c: IntArray) : Boolean = synchronized(queue) {
        ++mutCounter
        if (c.size > size) {
            return@synchronized removeIf { c.contains(it) }
        }
        for (item in c) {
            if (!remove(item)) return@synchronized false
        }
        ++mutCounter
        true
    }

    fun removeIf(predicate: (Int)->Boolean) : Boolean = synchronized(queue) {
        ++mutCounter
        var i = 0
        while (i < size) {
            if (predicate(queue[i])) {
                fillHole(i)
                // do not increment i, as we have a new item at index i
            } else {
                ++i
            }
        }
        ++mutCounter
        true
    }

    fun retainAll(c: Collection<Int>) : Boolean = removeIf { !c.contains(it) }

    fun retainAll(c: IntArray) : Boolean = removeIf { !c.contains(it) }

    private fun resizeUp() = resize(queue.size * 3 / 2 + 1)

    private fun resize(newSize: Int) {
        val newQueue = IntArray(newSize)
        queue.copyInto(newQueue)
        queue = newQueue
    }

    private fun bubbleUp(initialIndex: Int) {
        var index = initialIndex
        while (index > 0) {
            val parentIndex = (index - 1) / 2
            if (comparator(queue[parentIndex], queue[index]) < 0) {
                break
            }
            swap(parentIndex, index)
            index = parentIndex
        }
    }

    private fun fillHole(startIndex: Int) {
        var index = startIndex
        // bubble least child recursively to fill the hole
        while (true) {
            val child1 = index * 2 + 1
            val child2 = index * 2 + 2
            if (child1 >= size) {
                break
            }
            if (child2 >= size) {
                queue[index] = queue[child1]
                break
            }
            if (comparator(queue[child1], queue[child2]) <= 0) {
                queue[index] = queue[child1]
                index = child1
            } else {
                queue[index] = queue[child2]
                index = child2
            }
        }
        // if index is the last element, just reduce size
        if (index == size -1) {
            --size
            return
        }
        // otherwise, we still have a hole in the last layer, not at the end
        // move the last item to the hole, and bubble it up if needed.
        // This sounds inefficient, but in practice, this rarely bubbles even once.
        queue[index] = queue[size -1]
        queue[size-1] = Int.MIN_VALUE // unnecessary, but it makes "unused" elemnts more visible
        --size
        bubbleUp(index)
    }

    private fun swap(i: Int, j: Int) {
        val temp = queue[i]
        queue[i] = queue[j]
        queue[j] = temp
    }

    fun toArray(array: IntArray) : IntArray = synchronized(queue) {
        if (array.size >= size) {
            queue.copyInto(array, 0, 0, size)
            return@synchronized array
        }
        val newArray = IntArray(size)
        queue.copyInto(newArray, 0, 0, size)
        newArray
    }

    fun clone() : ArrayPriorityBlockingQueueInt = synchronized(queue) {
        val c = ArrayPriorityBlockingQueueInt(size, comparator)
        queue.copyInto(c.queue, 0, 0, size)
        c.size = size
        c
    }

    override fun equals(other: Any?) : Boolean {
        if (other !is ArrayPriorityBlockingQueueInt) return false
        return synchronized(queue) {
            if (comparator != other.comparator) return@synchronized false
            if (size != other.size) return@synchronized false
            for (i in 0..<size) {
                if (queue[i] != other.queue[i]) return@synchronized false
            }
            true
        }
    }

    override fun hashCode() : Int = synchronized(queue) {
        var hash = Objects.hash(comparator)
        for (i in 0..<size) {
            hash = Objects.hash(hash, queue[i])
        }
        hash
    }

    override fun toString() : String = synchronized(queue) {
        "IntPriorityBlockingArrayQueue[size=$size top=${if(size>0)queue[0] else "null"}]"
    }

    fun forEach(action: (Int) -> Unit): Unit = synchronized(queue) {
        for (i in 0 until size) {
            action(queue[i])
        }
    }

    fun iterator() : Iterator = synchronized(queue) { Iterator(mutCounter) }

    inner class Iterator(private var mutSnapshot: Int) : MutableIterator<Int> {
        private var index: Int = -1
        private var canRemove: Boolean = true

        override fun hasNext(): Boolean = synchronized(queue) {
            if (mutSnapshot != mutCounter) {
                throw ConcurrentModificationException("Priority queue modified during iteration.")
            }
            index < size - 1
        }

        override fun next(): Int = synchronized(queue) {
            if (mutSnapshot != mutCounter) {
                throw ConcurrentModificationException("Priority queue modified during iteration.")
            }
            if (index >= size) {
                throw NoSuchElementException("No more elements in priority queue.")
            }
            ++index
            canRemove = true
            queue[index]
        }

        override fun remove(): Unit = synchronized(queue) {
            if (mutSnapshot != mutCounter) {
                throw ConcurrentModificationException("Priority queue modified during iteration.")
            }
            if (!canRemove) {
                throw IllegalStateException("Cannot remove element before calling next().")
            }
            fillHole(index)
            ++mutCounter
            ++mutSnapshot
            canRemove = false
        }
    }

    fun spliterator() : Spliterator = synchronized(queue) { Spliterator(-1, size-1, mutCounter) }

    inner class Spliterator(var index: Int, var endIndex: Int, val mutSnapshot: Int) : java.util.Spliterator.OfInt {

        override fun tryAdvance(action: IntConsumer?): Boolean = synchronized(queue) {
            if (mutSnapshot != mutCounter) {
                throw ConcurrentModificationException("Priority queue modified during iteration.")
            }
            if (index >= endIndex) {
                return@synchronized false
            }
            ++index
            action?.accept(queue[index])
            true
        }

        override fun trySplit(): Spliterator? = synchronized(queue) {
            if (mutSnapshot != mutCounter) {
                throw ConcurrentModificationException("Priority queue modified during iteration.")
            }
            if (endIndex - index < 2) {
                return@synchronized null
            }
            val mid = (index + endIndex) / 2
            val split = Spliterator(mid, endIndex, mutSnapshot)
            endIndex = mid
            split
        }

        override fun estimateSize(): Long = synchronized(queue) {
            endIndex - index.toLong()
        }

        override fun characteristics(): Int {
            return SIZED.or(NONNULL).or(IMMUTABLE).or(CONCURRENT).or(SUBSIZED)
        }
    }

    fun stream() : IntStream = StreamSupport.intStream(spliterator(), false)

    fun parallelStream() : IntStream = stream().parallel()

    companion object {
        interface Comparator {
            operator fun invoke(a: Int, b: Int): Int
        }
        object DefaultComparator : Comparator {
            override operator fun invoke(a: Int, b: Int): Int = a.compareTo(b)
        }
    }
}
