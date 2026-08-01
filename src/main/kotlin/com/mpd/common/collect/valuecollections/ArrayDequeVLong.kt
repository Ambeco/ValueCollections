@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused")

package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.CircularLongArray

// Wraps com.mpd.common.collect.CircularLongArray, which provides O(1) random read plus O(1)
// addFirst/addLast/popFirst/popLast, but no arbitrary insert/remove/write by index. Operations that
// CircularLongArray can service directly (the Deque-shaped ones) are plain pass-throughs. Operations
// that require removing/inspecting an arbitrary element (containsBits, anyBits, removeBits,
// removeAll, and iterator.remove()) are O(n): they walk the array, or - for removal - pop every
// element off the front and push back everything except the one(s) being dropped.
// Not thread-safe: unlike ArrayPriorityBlockingQueueLong, CircularLongArray does no internal locking.
class ArrayDequeVLong<T>(
    val deque: CircularLongArray = CircularLongArray(),
    override val NULL_VALUE: LongBits = Long.MIN_VALUE
): DequeVLong<T> {
    constructor(minCapacity: Int, NULL_VALUE: LongBits = Long.MIN_VALUE) : this(CircularLongArray(minCapacity), NULL_VALUE)

    override val size: Int inline get() = deque.size()

    override inline fun anyBits(predicate: (LongBits) -> Boolean): LongBits {
        for (i in 0 until deque.size()) {
            val bits = deque[i]
            if (predicate(bits)) return bits
        }
        return NULL_VALUE
    }
    override inline fun containsBits(bits: LongBits): Boolean {
        for (i in 0 until deque.size()) if (deque[i] == bits) return true
        return false
    }

    context(a: ValueLongAdapter<T>) override fun asIterable(): MutableIterable<T> = MutableIteratorVLongGeneric(DequeIterator(), a)

    override inline fun ensureCapacity(newCapacity: Int): Boolean = false
    override inline fun trim(minCapacity: Int) { /* not supported: CircularLongArray does not expose a way to shrink its backing array */ }
    override inline fun addBits(bits: LongBits): Boolean { deque.addLast(bits); return true }
    override inline fun removeBits(bits: LongBits): Boolean {
        var removed = false
        for (i in 0 until deque.size()) {
            val v = deque.popFirst()
            if (!removed && v == bits) removed = true else deque.addLast(v)
        }
        return removed
    }
    override inline fun removeAllBits(predicate: (LongBits) -> Boolean): Boolean {
        var removed = false
        for (i in 0 until deque.size()) {
            val v = deque.popFirst()
            if (predicate(v)) removed = true else deque.addLast(v)
        }
        return removed
    }
    override inline fun clear() = deque.clear()

    override inline fun offerBits(bits: LongBits): Boolean = addBits(bits)
    override inline fun pollBits(): LongBits = pollFirstBits()
    override inline fun peekBits(): LongBits = peekFirstBits()

    override inline fun addFirstBits(bits: LongBits) = deque.addFirst(bits)
    override inline fun addLastBits(bits: LongBits) = deque.addLast(bits)
    override inline fun offerFirstBits(bits: LongBits): Boolean { deque.addFirst(bits); return true }
    override inline fun offerLastBits(bits: LongBits): Boolean { deque.addLast(bits); return true }
    override inline fun removeFirstBits(): LongBits = if (deque.isEmpty()) throw NoSuchElementException() else deque.popFirst()
    override inline fun removeLastBits(): LongBits = if (deque.isEmpty()) throw NoSuchElementException() else deque.popLast()
    override inline fun pollFirstBits(): LongBits = if (deque.isEmpty()) NULL_VALUE else deque.popFirst()
    override inline fun pollLastBits(): LongBits = if (deque.isEmpty()) NULL_VALUE else deque.popLast()
    override inline fun peekFirstBits(): LongBits = if (deque.isEmpty()) NULL_VALUE else deque.first
    override inline fun peekLastBits(): LongBits = if (deque.isEmpty()) NULL_VALUE else deque.last
    override inline fun pushBits(bits: LongBits) = deque.addFirst(bits)
    override inline fun popBits(): LongBits = removeFirstBits()

    inline fun bitsAtIndex(index: Int): LongBits = deque[index]
    context(a: ValueLongAdapter<T>) inline operator fun get(index: Int): T = a.fromLong(deque[index])

    override inline fun equals(other: Any?): Boolean = other is CollectionVLong<*> && contentEquals(other as CollectionVLong<T>)
    override inline fun hashCode(): Int = contentHashCode()
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print T.toString", ReplaceWith("toStringV()"))
    override fun toString(): String {
        val sb = StringBuilder("ArrayDequeVLong[")
        for (i in 0 until deque.size()) { if (i > 0) sb.append(", "); sb.append(deque[i]) }
        return sb.append(']').toString()
    }

    private inner class DequeIterator : MutableIterator<Long> {
        private var index = -1
        override inline fun hasNext(): Boolean = index + 1 < deque.size()
        override inline fun next(): Long {
            if (!hasNext()) throw NoSuchElementException()
            ++index
            return deque[index]
        }
        override inline fun remove() {
            if (index < 0) throw IllegalStateException("Cannot remove element before calling next().")
            removeAtIndex(index)
            --index
        }
    }
    private inline fun removeAtIndex(index: Int) {
        val n = deque.size()
        for (i in 0 until n) {
            val v = deque.popFirst()
            if (i != index) deque.addLast(v)
        }
    }
}
