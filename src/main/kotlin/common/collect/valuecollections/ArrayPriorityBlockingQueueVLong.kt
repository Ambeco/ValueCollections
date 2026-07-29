@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused")

package mpd.com.common.collect.valuecollections

import mpd.com.common.collect.ArrayPriorityBlockingQueueLong
import java.util.concurrent.TimeUnit

// Wraps com.unciv.utils.ArrayPriorityBlockingQueueLong, which is itself thread-safe (it synchronizes
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
class ArrayPriorityBlockingQueueVLong<T>(
    val queue: ArrayPriorityBlockingQueueLong = ArrayPriorityBlockingQueueLong(),
    override val NULL_VALUE: LongBits = Long.MIN_VALUE
): PriorityBlockingQueueVLong<T> {
    constructor(
        initialCapacity: Int,
        comparator: ArrayPriorityBlockingQueueLong.Companion.Comparator = ArrayPriorityBlockingQueueLong.Companion.DefaultComparator,
        NULL_VALUE: LongBits = Long.MIN_VALUE
    ) : this(ArrayPriorityBlockingQueueLong(initialCapacity, comparator), NULL_VALUE)

    private val lock = java.lang.Object()

    override val size: Int get() = queue.size()

    override fun anyBits(predicate: (LongBits) -> Boolean): LongBits {
        var result = NULL_VALUE
        var found = false
        queue.forEach { if (!found && predicate(it)) { result = it; found = true } }
        return result
    }
    override fun containsBits(bits: LongBits): Boolean = queue.contains(bits)

    context(a: ValueLongAdapter<T>) override fun asIterable(): MutableIterable<T> = MutableIteratorVLongGeneric(queue.iterator(), a)

    override fun ensureCapacity(newCapacity: Int): Boolean = false
    override fun trim(minCapacity: Int) { /* not supported: the wrapped queue does not expose a way to shrink its backing array */ }
    override fun addBits(bits: LongBits): Boolean {
        val result = queue.offer(bits)
        synchronized(lock) { lock.notifyAll() }
        return result
    }
    override fun removeBits(bits: LongBits): Boolean = queue.remove(bits)
    context(a: ValueLongAdapter<T>) override fun removeAll(predicate: (T) -> Boolean): Boolean = queue.removeIf { predicate(a.fromLong(it)) }
    override fun clear() = queue.clear()

    override fun offerBits(bits: LongBits): Boolean = addBits(bits)
    override fun pollBits(): LongBits = try { queue.poll() } catch (e: NoSuchElementException) { NULL_VALUE }
    override fun peekBits(): LongBits = queue.peek() ?: NULL_VALUE

    override fun putBits(bits: LongBits) { addBits(bits) }
    override fun takeBits(): LongBits {
        synchronized(lock) {
            while (queue.isEmpty()) lock.wait()
        }
        return try { queue.poll() } catch (e: NoSuchElementException) { takeBits() }
    }
    override fun offerBits(bits: LongBits, timeout: Long, unit: TimeUnit): Boolean =
        // unbounded: always succeeds immediately, no need to actually wait for space
        addBits(bits)
    override fun pollBits(timeout: Long, unit: TimeUnit): LongBits {
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

    override fun drainTo(destination: MutableCollectionVLong<T>): Int {
        var count = 0
        while (queue.isNotEmpty()) { destination.addBits(queue.poll()); count++ }
        return count
    }
    override fun drainTo(destination: MutableCollectionVLong<T>, maxElements: Int): Int {
        var count = 0
        while (count < maxElements && queue.isNotEmpty()) { destination.addBits(queue.poll()); count++ }
        return count
    }

    override fun equals(other: Any?): Boolean = other is CollectionVLong<*> && contentEquals(other as CollectionVLong<T>)
    override fun hashCode(): Int = contentHashCode()
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print T.toString", ReplaceWith("toStringV()"))
    override fun toString(): String = queue.toString()
}
