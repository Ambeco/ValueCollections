@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused")

package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.ArrayPriorityQueueLong

// Wraps com.mpd.common.collect.ArrayPriorityQueueLong, a non-thread-safe fork of
// ArrayPriorityBlockingQueueLong. Since the wrapped queue does no internal locking, this wrapper
// doesn't need to either - every member here is a plain pass-through.
class ArrayPriorityQueueVLong<T>(
    val queue: ArrayPriorityQueueLong = ArrayPriorityQueueLong(),
    override val NULL_VALUE: LongBits = Long.MIN_VALUE
): PriorityQueueVLong<T> {
    constructor(
        initialCapacity: Int,
        comparator: ArrayPriorityQueueLong.Companion.Comparator = ArrayPriorityQueueLong.Companion.DefaultComparator,
        NULL_VALUE: LongBits = Long.MIN_VALUE
    ) : this(ArrayPriorityQueueLong(initialCapacity, comparator), NULL_VALUE)

    override val size: Int get() = queue.size()

    override inline fun anyBits(crossinline predicate: (LongBits) -> Boolean): LongBits {
        val finder = object : (LongBits) -> Unit {
            var result = NULL_VALUE
            var found = false
            override inline fun invoke(v: LongBits) { if (!found && predicate(v)) { result = v; found = true } }
        }
        queue.forEach(finder)
        return finder.result
    }
    override inline fun containsBits(bits: LongBits): Boolean = queue.contains(bits)

    context(a: ValueLongAdapter<T>) override inline fun asIterable(): MutableIterable<T> = MutableIteratorVLongGeneric(queue.iterator(), a)

    override inline fun ensureCapacity(newCapacity: Int): Boolean = false
    override inline fun trim(minCapacity: Int) { /* not supported: the wrapped queue does not expose a way to shrink its backing array */ }
    override inline fun addBits(bits: LongBits): Boolean = queue.offer(bits)
    override inline fun removeBits(bits: LongBits): Boolean = queue.remove(bits)
    override inline fun removeAllBits(crossinline predicate: (LongBits) -> Boolean): Boolean = queue.removeIf { predicate(it) }
    override inline fun clear() = queue.clear()

    override inline fun offerBits(bits: LongBits): Boolean = addBits(bits)
    override inline fun pollBits(): LongBits = try { queue.poll() } catch (e: NoSuchElementException) { NULL_VALUE }
    override inline fun peekBits(): LongBits = queue.peek() ?: NULL_VALUE

    override inline fun equals(other: Any?): Boolean = other is CollectionVLong<*> && contentEquals(other as CollectionVLong<T>)
    override inline fun hashCode(): Int = contentHashCode()
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print T.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = queue.toString()
}
