@file:Suppress("unused")

package mpd.com.common.collect

import java.util.Objects
import java.util.Spliterator.CONCURRENT
import java.util.Spliterator.IMMUTABLE
import java.util.Spliterator.NONNULL
import java.util.Spliterator.SIZED
import java.util.Spliterator.SUBSIZED
import java.util.function.LongConsumer
import java.util.stream.LongStream
import java.util.stream.StreamSupport

// A PriorityQueue<Long>, except that it minimizes memory allocations
//
// This does NOT extend any interfaces to avoid boxing Long to Long Objects.
//
// Thread safety: every method synchronizes on the dedicated `lock` object.
class ArrayPriorityBlockingQueueLong(
    initialCapacity: Int = 100,
    val comparator : Comparator = DefaultComparator
) {
    private val lock = Any()
    private var queue: LongArray = LongArray(initialCapacity)
    private var size : Int = 0
    private var mutCounter: Int = 0

    constructor(c: Collection<Long>, comparator : Comparator = DefaultComparator)
            : this(c.size, comparator) {
        addAll(c)
    }
    constructor(c: LongArray, comparator : Comparator = DefaultComparator)
            : this(c.size, comparator) {
        addAll(c)
    }

    fun clear(): Unit = synchronized(lock) {
        size = 0
        ++mutCounter
    }

    fun size() : Int = synchronized(lock) { size }

    fun isEmpty() : Boolean = synchronized(lock) { size == 0 }

    fun isNotEmpty() = synchronized(lock) { size != 0 }

    fun element() : Long = synchronized(lock) {
        if (size == 0) throw NoSuchElementException("Priority queue is empty.")
        queue[0]
    }

    operator fun get(index: Int) : Long = synchronized(lock) {
        if (index !in 0..<size) throw IndexOutOfBoundsException("Index $index out of bounds for priority queue of size $size.")
        queue[index]
    }

    fun contains(value: Long): Boolean = synchronized(lock) {
        for (i in 0..<size) {
            if (queue[i] == value) return@synchronized true
        }
        false
    }

    fun containsAll(c: Collection<Long>): Boolean = synchronized(lock) {
        for (item in c) {
            if (!contains(item)) return@synchronized false
        }
        true
    }

    fun containsAll(c: LongArray): Boolean = synchronized(lock) {
        for (item in c) {
            if (!contains(item)) return@synchronized false
        }
        true
    }

    operator fun plus(value: Long) = add(value)

    fun add(value: Long): Unit = synchronized(lock) {
        val newQueue = prepare_resize_if_needed()
        newQueue[size] = value
        ++size
        commit(newQueue)
        bubbleUp(size-1)
        ++mutCounter
    }

    fun offer(value: Long): Boolean = synchronized(lock) {
        add(value)
        true
    }

    fun addAll(c: Collection<Long>) : Boolean = synchronized(lock) {
        ++mutCounter
        val oldSize = size
        val newQueue = prepare_resize_if_needed(size + c.size)
        c.forEach { newQueue[size++] = it }
        commit(newQueue)
        // looks inefficient, but 50% do not bubble at all, 25% bubble once, etc.
        for (i in size-1 downTo  oldSize-1) {
            bubbleUp(i)
        }
        ++mutCounter
        c.isNotEmpty()
    }

    fun addAll(c: LongArray) : Boolean = synchronized(lock) {
        ++mutCounter
        val oldSize = size
        val newQueue = prepare_resize_if_needed(size + c.size)
        c.copyInto(newQueue, size)
        size = size + c.size
        commit(newQueue)
        // looks inefficient, but 50% do not bubble at all, 25% bubble once, etc.
        for (i in oldSize until size) {
            bubbleUp(i)
        }
        ++mutCounter
        c.isNotEmpty()
    }

    fun peek() : Long? = synchronized(lock) { if (size > 0) queue[0] else null }

    fun poll() : Long = synchronized(lock) {
        if (size == 0) throw NoSuchElementException("Priority queue is empty.")
        val top = queue[0]
        fillHole(0)
        ++mutCounter
        top
    }

    operator fun minus(value: Long) = remove(value)

    fun remove(value: Long) : Boolean = synchronized(lock) {
        for (i in 0..<size) {
            if (queue[i] == value) {
                fillHole(i)
                ++mutCounter
                return@synchronized true
            }
        }
        false
    }

    fun removeAll(c: Collection<Long>) : Boolean = synchronized(lock) {
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

    fun removeAll(c: LongArray) : Boolean = synchronized(lock) {
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

    fun removeIf(predicate: (Long)->Boolean) : Boolean = synchronized(lock) {
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

    fun retainAll(c: Collection<Long>) : Boolean = removeIf { !c.contains(it) }

    fun retainAll(c: LongArray) : Boolean = removeIf { !c.contains(it) }

    private inline fun prepare_resize_if_needed(): LongArray = if (size + 1 >= queue.size) prepare_resize_exact(queue.size * 3 / 2 + 1) else queue

    private inline fun prepare_resize_if_needed(newSize: Int): LongArray = if (newSize > queue.size) prepare_resize_exact(newSize) else queue

    private inline fun prepare_resize_exact(newSize: Int): LongArray {
        val newQueue = LongArray(newSize)
        queue.copyInto(newQueue)
        return newQueue
    }

    private inline fun commit(newQueue: LongArray) {
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
        queue[size-1] = Long.MIN_VALUE // unnecessary, but it makes "unused" elemnts more visible
        --size
        bubbleUp(index)
    }

    private fun swap(i: Int, j: Int) {
        val temp = queue[i]
        queue[i] = queue[j]
        queue[j] = temp
    }

    fun toArray(array: LongArray) : LongArray = synchronized(lock) {
        if (array.size >= size) {
            queue.copyInto(array, 0, 0, size)
            return@synchronized array
        }
        val newArray = LongArray(size)
        queue.copyInto(newArray, 0, 0, size)
        newArray
    }

    fun clone() : ArrayPriorityBlockingQueueLong = synchronized(lock) {
        val c = ArrayPriorityBlockingQueueLong(size, comparator)
        queue.copyInto(c.queue, 0, 0, size)
        c.size = size
        c
    }

    override fun equals(other: Any?) : Boolean {
        if (other !is ArrayPriorityBlockingQueueLong) return false
        return synchronized(lock) {
            if (comparator != other.comparator) return@synchronized false
            if (size != other.size) return@synchronized false
            for (i in 0..<size) {
                if (queue[i] != other.queue[i]) return@synchronized false
            }
            true
        }
    }

    override fun hashCode() : Int = synchronized(lock) {
        var hash = Objects.hash(comparator)
        for (i in 0..<size) {
            hash = Objects.hash(hash, queue[i])
        }
        hash
    }

    override fun toString() : String = synchronized(lock) {
        "LongPriorityBlockingArrayQueue[size=$size top=${if(size>0)queue[0] else "null"}]"
    }

    fun forEach(action: (Long) -> Unit): Unit = synchronized(lock) {
        for (i in 0 until size) {
            action(queue[i])
        }
    }

    fun iterator() : Iterator = synchronized(lock) { Iterator(mutCounter) }

    inner class Iterator(private var mutSnapshot: Int) : MutableIterator<Long> {
        private var index: Int = -1
        private var canRemove: Boolean = true

        override fun hasNext(): Boolean = synchronized(lock) {
            if (mutSnapshot != mutCounter) {
                throw ConcurrentModificationException("Priority queue modified during iteration.")
            }
            index < size - 1
        }

        override fun next(): Long = synchronized(lock) {
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

        override fun remove(): Unit = synchronized(lock) {
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

    fun spliterator() : Spliterator = synchronized(lock) { Spliterator(-1, size-1, mutCounter) }

    inner class Spliterator(var index: Int, var endIndex: Int, val mutSnapshot: Int) : java.util.Spliterator.OfLong {

        override fun tryAdvance(action: LongConsumer?): Boolean = synchronized(lock) {
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

        override fun trySplit(): Spliterator? = synchronized(lock) {
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

        override fun estimateSize(): Long = synchronized(lock) {
            endIndex - index.toLong()
        }

        override fun characteristics(): Int {
            return SIZED.or(NONNULL).or(IMMUTABLE).or(CONCURRENT).or(SUBSIZED)
        }
    }

    fun stream() : LongStream = StreamSupport.longStream(spliterator(), false)

    fun parallelStream() : LongStream = stream().parallel()

    companion object {
        interface Comparator {
            operator fun invoke(a: Long, b: Long): Int
        }
        object DefaultComparator : Comparator {
            override operator fun invoke(a: Long, b: Long): Int = a.compareTo(b)
        }
    }
}
