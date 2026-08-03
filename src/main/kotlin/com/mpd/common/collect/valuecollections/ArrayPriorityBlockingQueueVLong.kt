@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused")

package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.ArrayPriorityBlockingQueueLong
import java.util.concurrent.TimeUnit

// Wraps com.unciv.utils.ArrayPriorityBlockingQueueLong, which is itself thread-safe (it synchronizes
// internally on its own backing array). Reads that don't remove anything (anyBits/containsBits/
// peekBits/asIterable) are therefore plain pass-throughs with no extra locking. Every operation that
// can remove an element (pollBits/removeBits/removeAllBits/clear/drainTo), though, additionally
// synchronizes on a dedicated `lock` - the same lock used to coordinate wait()/notifyAll() for
// take()/timed poll(). This guarantees a woken take()/poll(timeout) can never lose its element to a
// concurrent non-blocking removal: the "is it empty" check and the actual removal happen atomically
// under the same lock. (Kotlin doesn't expose Object.wait/notifyAll on ordinary class instances,
// hence the separate `lock` object rather than synchronizing on `queue` itself for that purpose.)
class ArrayPriorityBlockingQueueVLong<T>(
    val queue: ArrayPriorityBlockingQueueLong = ArrayPriorityBlockingQueueLong(),
    override val NULL_VALUE: LongBits = Long.MIN_VALUE
): PriorityBlockingQueueVLong<T> {
    constructor(
        initialCapacity: Int,
        comparator: ArrayPriorityBlockingQueueLong.Companion.Comparator = ArrayPriorityBlockingQueueLong.Companion.DefaultComparator,
        NULL_VALUE: LongBits = Long.MIN_VALUE
    ) : this(ArrayPriorityBlockingQueueLong(initialCapacity, comparator), NULL_VALUE)

    // `lock` exists purely for wait()/notifyAll() coordination between blocked consumers and
    // producers - it is NOT what makes `queue` thread-safe (the wrapped queue already synchronizes
    // itself internally on its own private lock for every operation).
    private val lock = Object()

    override val size: Int get() = queue.size()

    override fun anyBits(predicate: (LongBits) -> Boolean): LongBits {
        val finder = object : (LongBits) -> Unit {
            var result = NULL_VALUE
            var found = false
            override fun invoke(v: LongBits) { if (!found && predicate(v)) { result = v; found = true } }
        }
        queue.forEach(finder)
        return finder.result
    }
    override fun containsBits(bits: LongBits): Boolean = queue.contains(bits)

    context(a: ValueLongAdapter<T>) override fun asIterable(): MutableIterable<T> = MutableIteratorVLongGeneric(queue.iterator(), a)

    override fun ensureCapacity(newCapacity: Int): Boolean = false
    override fun trim(minCapacity: Int) { /* not supported: the wrapped queue does not expose a way to shrink its backing array */ }
    override fun addBits(bits: LongBits): Boolean {
        synchronized(lock) {
            val result = queue.offer(bits)
            lock.notifyAll()
            return result
        }
    }
    override fun removeBits(bits: LongBits): Boolean = synchronized(lock) { queue.remove(bits) }
    override fun removeAllBits(predicate: (LongBits) -> Boolean): Boolean = synchronized(lock) { queue.removeIf { predicate(it) } }
    override fun clear() = synchronized(lock) { queue.clear() }

    override fun offerBits(bits: LongBits): Boolean = addBits(bits)
    override fun pollBits(): LongBits = synchronized(lock) { try { queue.poll() } catch (e: NoSuchElementException) { NULL_VALUE } }
    override fun peekBits(): LongBits = queue.peek() ?: NULL_VALUE

    override fun putBits(bits: LongBits) { addBits(bits) }
    override fun takeBits(): LongBits = synchronized(lock) {
        while (queue.isEmpty()) lock.wait()
        queue.poll()
    }
    override fun offerBits(bits: LongBits, timeout: Long, unit: TimeUnit): Boolean =
        // unbounded: always succeeds immediately, no need to actually wait for space
        addBits(bits)
    override fun pollBits(timeout: Long, unit: TimeUnit): LongBits {
        val deadline = System.nanoTime() + unit.toNanos(timeout)
        return synchronized(lock) {
            var remainingNanos = deadline - System.nanoTime()
            while (queue.isEmpty()) {
                if (remainingNanos <= 0) return@synchronized NULL_VALUE
                lock.wait(remainingNanos / 1_000_000, (remainingNanos % 1_000_000).toInt())
                remainingNanos = deadline - System.nanoTime()
            }
            queue.poll()
        }
    }
    override fun remainingCapacity(): Int = Int.MAX_VALUE

    override fun drainTo(destination: MutableCollectionVLong<T>): Int = synchronized(lock) {
        var count = 0
        while (queue.isNotEmpty()) { destination.addBits(queue.poll()); count++ }
        count
    }
    override fun drainTo(destination: MutableCollectionVLong<T>, maxElements: Int): Int = synchronized(lock) {
        var count = 0
        while (count < maxElements && queue.isNotEmpty()) { destination.addBits(queue.poll()); count++ }
        count
    }

    override fun equals(other: Any?): Boolean = other is CollectionVLong<*> && contentEquals(other as CollectionVLong<T>)
    override fun hashCode(): Int = contentHashCode()
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print T.toString", ReplaceWith("toStringV()"))
    override fun toString(): String = queue.toString()
}
