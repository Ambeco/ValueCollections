@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused")

package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.ArrayPriorityQueueInt

// Wraps com.mpd.common.collect.ArrayPriorityQueueInt, a non-thread-safe fork of
// ArrayPriorityBlockingQueueInt. Since the wrapped queue does no internal locking, this wrapper
// doesn't need to either - every member here is a plain pass-through.
class ArrayPriorityQueueVInt<T>(
    val queue: ArrayPriorityQueueInt = ArrayPriorityQueueInt(),
    override val NULL_VALUE: IntBits = Int.MIN_VALUE
): PriorityQueueVInt<T> {
    constructor(
        initialCapacity: Int,
        comparator: ArrayPriorityQueueInt.Companion.Comparator = ArrayPriorityQueueInt.Companion.DefaultComparator,
        NULL_VALUE: IntBits = Int.MIN_VALUE
    ) : this(ArrayPriorityQueueInt(initialCapacity, comparator), NULL_VALUE)

    override val size: Int get() = queue.size()

    override inline fun anyBits(crossinline predicate: (IntBits) -> Boolean): IntBits {
        val finder = object : (IntBits) -> Unit {
            var result = NULL_VALUE
            var found = false
            override inline fun invoke(v: IntBits) { if (!found && predicate(v)) { result = v; found = true } }
        }
        queue.forEach(finder)
        return finder.result
    }
    override inline fun containsBits(bits: IntBits): Boolean = queue.contains(bits)

    context(a: ValueIntAdapter<T>) override inline fun asIterable(): MutableIterable<T> = MutableIteratorVIntGeneric(queue.iterator(), a)

    override inline fun ensureCapacity(newCapacity: Int): Boolean = false
    override inline fun trim(minCapacity: Int) { /* not supported: the wrapped queue does not expose a way to shrink its backing array */ }
    override inline fun addBits(bits: IntBits): Boolean = queue.offer(bits)
    override inline fun removeBits(bits: IntBits): Boolean = queue.remove(bits)
    override inline fun removeAllBits(crossinline predicate: (IntBits) -> Boolean): Boolean = queue.removeIf { predicate(it) }
    override inline fun clear() = queue.clear()

    override inline fun offerBits(bits: IntBits): Boolean = addBits(bits)
    override inline fun pollBits(): IntBits = try { queue.poll() } catch (e: NoSuchElementException) { NULL_VALUE }
    override inline fun peekBits(): IntBits = queue.peek() ?: NULL_VALUE

    override inline fun equals(other: Any?): Boolean = other is CollectionVInt<*> && contentEquals(other as CollectionVInt<T>)
    override inline fun hashCode(): Int = contentHashCode()
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print T.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = queue.toString()
}
