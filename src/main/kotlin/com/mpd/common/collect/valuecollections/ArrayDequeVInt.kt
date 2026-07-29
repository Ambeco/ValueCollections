@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused")

package com.mpd.common.collect.valuecollections

import androidx.collection.CircularIntArray

// Wraps androidx.collection.CircularIntArray, which provides O(1) random read plus O(1)
// addFirst/addLast/popFirst/popLast, but no arbitrary insert/remove/write by index. Operations that
// CircularIntArray can service directly (the Deque-shaped ones) are plain pass-throughs. Operations
// that require removing/inspecting an arbitrary element (containsBits, anyBits, removeBits,
// removeAll, and iterator.remove()) are O(n): they walk the array, or - for removal - pop every
// element off the front and push back everything except the one(s) being dropped.
// Not thread-safe: unlike ArrayPriorityBlockingQueueInt, CircularIntArray does no internal locking.
class ArrayDequeVInt<T>(
    val deque: CircularIntArray = CircularIntArray(),
    override val NULL_VALUE: IntBits = Int.MIN_VALUE
): DequeVInt<T> {
    constructor(minCapacity: Int, NULL_VALUE: IntBits = Int.MIN_VALUE) : this(CircularIntArray(minCapacity), NULL_VALUE)

    override val size: Int inline get() = deque.size()

    override inline fun anyBits(predicate: (IntBits) -> Boolean): IntBits {
        for (i in 0 until deque.size()) {
            val bits = deque[i]
            if (predicate(bits)) return bits
        }
        return NULL_VALUE
    }
    override inline fun containsBits(bits: IntBits): Boolean {
        for (i in 0 until deque.size()) if (deque[i] == bits) return true
        return false
    }

    context(a: ValueIntAdapter<T>) override fun asIterable(): MutableIterable<T> = MutableIteratorVIntGeneric(DequeIterator(), a)

    override inline fun ensureCapacity(newCapacity: Int): Boolean = false
    override inline fun trim(minCapacity: Int) { /* not supported: CircularIntArray does not expose a way to shrink its backing array */ }
    override inline fun addBits(bits: IntBits): Boolean { deque.addLast(bits); return true }
    override inline fun removeBits(bits: IntBits): Boolean {
        var removed = false
        for (i in 0 until deque.size()) {
            val v = deque.popFirst()
            if (!removed && v == bits) removed = true else deque.addLast(v)
        }
        return removed
    }
    context(a: ValueIntAdapter<T>) override inline fun removeAll(predicate: (T) -> Boolean): Boolean {
        var removed = false
        for (i in 0 until deque.size()) {
            val v = deque.popFirst()
            if (predicate(a.fromInt(v))) removed = true else deque.addLast(v)
        }
        return removed
    }
    override inline fun clear() = deque.clear()

    override inline fun offerBits(bits: IntBits): Boolean = addBits(bits)
    override inline fun pollBits(): IntBits = pollFirstBits()
    override inline fun peekBits(): IntBits = peekFirstBits()

    override inline fun addFirstBits(bits: IntBits) = deque.addFirst(bits)
    override inline fun addLastBits(bits: IntBits) = deque.addLast(bits)
    override inline fun offerFirstBits(bits: IntBits): Boolean { deque.addFirst(bits); return true }
    override inline fun offerLastBits(bits: IntBits): Boolean { deque.addLast(bits); return true }
    override inline fun removeFirstBits(): IntBits = if (deque.isEmpty()) throw NoSuchElementException() else deque.popFirst()
    override inline fun removeLastBits(): IntBits = if (deque.isEmpty()) throw NoSuchElementException() else deque.popLast()
    override inline fun pollFirstBits(): IntBits = if (deque.isEmpty()) NULL_VALUE else deque.popFirst()
    override inline fun pollLastBits(): IntBits = if (deque.isEmpty()) NULL_VALUE else deque.popLast()
    override inline fun peekFirstBits(): IntBits = if (deque.isEmpty()) NULL_VALUE else deque.first
    override inline fun peekLastBits(): IntBits = if (deque.isEmpty()) NULL_VALUE else deque.last
    override inline fun pushBits(bits: IntBits) = deque.addFirst(bits)
    override inline fun popBits(): IntBits = removeFirstBits()

    inline fun bitsAtIndex(index: Int): IntBits = deque[index]
    context(a: ValueIntAdapter<T>) inline operator fun get(index: Int): T = a.fromInt(deque[index])

    override inline fun equals(other: Any?): Boolean = other is CollectionVInt<*> && contentEquals(other as CollectionVInt<T>)
    override inline fun hashCode(): Int = contentHashCode()
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print T.toString", ReplaceWith("toStringV()"))
    override fun toString(): String {
        val sb = StringBuilder("ArrayDequeVInt[")
        for (i in 0 until deque.size()) { if (i > 0) sb.append(", "); sb.append(deque[i]) }
        return sb.append(']').toString()
    }

    private inner class DequeIterator : MutableIterator<Int> {
        private var index = -1
        override inline fun hasNext(): Boolean = index + 1 < deque.size()
        override inline fun next(): Int {
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
