@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused")

package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.ArrayPriorityBlockingQueueInt
import java.util.concurrent.TimeUnit

// Wraps com.unciv.utils.ArrayPriorityBlockingQueueInt, which is itself thread-safe (it synchronizes
// internally on its own backing array). Most members here are therefore plain pass-throughs with no
// extra locking. The exception is genuine BlockingQueue semantics (put/take, timed offer/poll): the
// wrapped queue has no concept of "wait until an element is available", so this class keeps a small
// dedicated lock purely to coordinate wait()/notifyAll() between producers and blocked consumers.
// (Kotlin doesn't expose Object.wait/notifyAll on ordinary class instances, hence the separate lock
// rather than synchronizing on `queue` itself for that purpose.)
// Note: because non-blocking calls like pollBits()/peekBits() intentionally do NOT go through this
// lock (they don't need to - the wrapped queue is already safe on its own), there's a narrow window
// where a woken take()/poll(timeout) can lose a race to an unrelated non-blocking poll() call. This
// mirrors the kind of small, accepted trade-off already documented on the wrapped queue itself.
class ArrayPriorityBlockingQueueVInt<T>(
    val queue: ArrayPriorityBlockingQueueInt = ArrayPriorityBlockingQueueInt(),
    override val NULL_VALUE: IntBits = Int.MIN_VALUE
): PriorityBlockingQueueVInt<T> {
    constructor(
        initialCapacity: Int,
        comparator: ArrayPriorityBlockingQueueInt.Companion.Comparator = ArrayPriorityBlockingQueueInt.Companion.DefaultComparator,
        NULL_VALUE: IntBits = Int.MIN_VALUE
    ) : this(ArrayPriorityBlockingQueueInt(initialCapacity, comparator), NULL_VALUE)

    private val lock = Object()

    override val size: Int get() = queue.size()

    override fun anyBits(predicate: (IntBits) -> Boolean): IntBits {
        var result = NULL_VALUE
        var found = false
        queue.forEach { if (!found && predicate(it)) { result = it; found = true } }
        return result
    }
    override fun containsBits(bits: IntBits): Boolean = queue.contains(bits)

    context(a: ValueIntAdapter<T>) override fun asIterable(): MutableIterable<T> = MutableIteratorVIntGeneric(queue.iterator(), a)

    override fun ensureCapacity(newCapacity: Int): Boolean = false
    override fun trim(minCapacity: Int) { /* not supported: the wrapped queue does not expose a way to shrink its backing array */ }
    override fun addBits(bits: IntBits): Boolean {
        val result = queue.offer(bits)
        synchronized(lock) { lock.notifyAll() }
        return result
    }
    override fun removeBits(bits: IntBits): Boolean = queue.remove(bits)
    context(a: ValueIntAdapter<T>) override fun removeAll(predicate: (T) -> Boolean): Boolean = queue.removeIf { predicate(a.fromInt(it)) }
    override fun clear() = queue.clear()

    override fun offerBits(bits: IntBits): Boolean = addBits(bits)
    override fun pollBits(): IntBits = try { queue.poll() } catch (e: NoSuchElementException) { NULL_VALUE }
    override fun peekBits(): IntBits = queue.peek() ?: NULL_VALUE

    override fun putBits(bits: IntBits) { addBits(bits) }
    override fun takeBits(): IntBits {
        synchronized(lock) {
            while (queue.isEmpty()) lock.wait()
        }
        return try { queue.poll() } catch (e: NoSuchElementException) { takeBits() }
    }
    override fun offerBits(bits: IntBits, timeout: Long, unit: TimeUnit): Boolean =
        // unbounded: always succeeds immediately, no need to actually wait for space
        addBits(bits)
    override fun pollBits(timeout: Long, unit: TimeUnit): IntBits {
        val deadline = System.nanoTime() + unit.toNanos(timeout)
        synchronized(lock) {
            var remainingNanos = deadline - System.nanoTime()
            while (queue.isEmpty()) {
                if (remainingNanos <= 0) return NULL_VALUE
                lock.wait(remainingNanos / 1_000_000, (remainingNanos % 1_000_000).toInt())
                remainingNanos = deadline - System.nanoTime()
            }
        }
        return try { queue.poll() } catch (e: NoSuchElementException) {
            val remainingNanos = deadline - System.nanoTime()
            if (remainingNanos <= 0) NULL_VALUE else pollBits(remainingNanos, TimeUnit.NANOSECONDS)
        }
    }
    override fun remainingCapacity(): Int = Int.MAX_VALUE

    override fun drainTo(destination: MutableCollectionVInt<T>): Int {
        var count = 0
        while (queue.isNotEmpty()) { destination.addBits(queue.poll()); count++ }
        return count
    }
    override fun drainTo(destination: MutableCollectionVInt<T>, maxElements: Int): Int {
        var count = 0
        while (count < maxElements && queue.isNotEmpty()) { destination.addBits(queue.poll()); count++ }
        return count
    }

    override fun equals(other: Any?): Boolean = other is CollectionVInt<*> && contentEquals(other as CollectionVInt<T>)
    override fun hashCode(): Int = contentHashCode()
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print T.toString", ReplaceWith("toStringV()"))
    override fun toString(): String = queue.toString()
}
