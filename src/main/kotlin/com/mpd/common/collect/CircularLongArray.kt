@file:Suppress("unused")

package com.mpd.common.collect

/**
 * [CircularLongArray] is a circular long array data structure that provides O(1) random read,
 * O(1) prepend and O(1) append. The CircularLongArray automatically grows its capacity when number
 * of added longs is over its capacity.
 *
 * @param minCapacity the minimum capacity, between 1 and 2^30 inclusive
 * @constructor Creates a circular array with capacity for at least [minCapacity] elements.
 */
class CircularLongArray(minCapacity: Int = 8) {
    private var elements: LongArray
    private var head = 0
    private var tail = 0
    private var capacityBitmask: Int

    init {
        require(minCapacity >= 1) { "capacity must be >= 1" }
        require(minCapacity <= 2 shl 29) { "capacity must be <= 2^30" }

        // If minCapacity isn't a power of 2, round up to the next highest
        // power of 2.
        val arrayCapacity: Int =
            if (minCapacity.countOneBits() != 1) {
                (minCapacity - 1).takeHighestOneBit() shl 1
            } else {
                minCapacity
            }
        capacityBitmask = arrayCapacity - 1
        elements = LongArray(arrayCapacity)
    }

    private fun doubleCapacity() {
        val n = elements.size
        val r = n - head
        val newCapacity = n shl 1
        if (newCapacity < 0) {
            throw RuntimeException("Max array capacity exceeded")
        }
        val a = LongArray(newCapacity)
        elements.copyInto(destination = a, destinationOffset = 0, startIndex = head, endIndex = n)
        elements.copyInto(destination = a, destinationOffset = r, startIndex = 0, endIndex = head)
        elements = a
        head = 0
        tail = n
        capacityBitmask = newCapacity - 1
    }

    /**
     * Add a long in front of the [CircularLongArray].
     *
     * @param element [Long] to add.
     */
    fun addFirst(element: Long) {
        head = (head - 1) and capacityBitmask
        elements[head] = element
        if (head == tail) {
            doubleCapacity()
        }
    }

    /**
     * Add a long at end of the [CircularLongArray].
     *
     * @param element [Long] to add.
     */
    fun addLast(element: Long) {
        elements[tail] = element
        tail = (tail + 1) and capacityBitmask
        if (tail == head) {
            doubleCapacity()
        }
    }

    /**
     * Remove first long from front of the [CircularLongArray] and return it.
     *
     * @return The long removed.
     * @throws IndexOutOfBoundsException if [CircularLongArray] is empty.
     */
    fun popFirst(): Long {
        if (head == tail) throw IndexOutOfBoundsException()
        val result = elements[head]
        head = (head + 1) and capacityBitmask
        return result
    }

    /**
     * Remove last long from end of the [CircularLongArray] and return it.
     *
     * @return The long removed.
     * @throws IndexOutOfBoundsException if [CircularLongArray] is empty.
     */
    fun popLast(): Long {
        if (head == tail) throw IndexOutOfBoundsException()
        val t = (tail - 1) and capacityBitmask
        val result = elements[t]
        tail = t
        return result
    }

    /** Remove all longs from the [CircularLongArray]. */
    fun clear() {
        tail = head
    }

    /**
     * Remove multiple longs from front of the [CircularLongArray], ignore when [count] is less
     * than or equals to 0.
     *
     * @param count Number of longs to remove.
     * @throws IndexOutOfBoundsException if numOfElements is larger than [size]
     */
    fun removeFromStart(count: Int) {
        if (count <= 0) {
            return
        }
        if (count > size()) {
            throw IndexOutOfBoundsException()
        }
        head = (head + count) and capacityBitmask
    }

    /**
     * Remove multiple elements from end of the [CircularLongArray], ignore when [count] is less than
     * or equals to 0.
     *
     * @param count Number of longs to remove.
     * @throws IndexOutOfBoundsException if [count] is larger than [size]
     */
    fun removeFromEnd(count: Int) {
        if (count <= 0) {
            return
        }
        if (count > size()) {
            throw IndexOutOfBoundsException()
        }
        tail = (tail - count) and capacityBitmask
    }

    /**
     * Get first long of the [CircularLongArray].
     *
     * @return The first long.
     * @throws [IndexOutOfBoundsException] if [CircularLongArray] is empty.
     */
    val first: Long
        get() {
            if (head == tail) throw IndexOutOfBoundsException()
            return elements[head]
        }

    /**
     * Get last long of the [CircularLongArray].
     *
     * @return The last long.
     * @throws [IndexOutOfBoundsException] if [CircularLongArray] is empty.
     */
    val last: Long
        get() {
            if (head == tail) throw IndexOutOfBoundsException()
            return elements[(tail - 1) and capacityBitmask]
        }

    /**
     * Get nth (0 <= n <= size()-1) long of the [CircularLongArray].
     *
     * @param index The zero based element index in the [CircularLongArray].
     * @return The nth long.
     * @throws [IndexOutOfBoundsException] if n < 0 or n >= size().
     */
    operator fun get(index: Int): Long {
        if (index < 0 || index >= size()) throw IndexOutOfBoundsException()
        return elements[(head + index) and capacityBitmask]
    }

    /**
     * Get number of longs in the [CircularLongArray].
     *
     * @return Number of longs in the [CircularLongArray].
     */
    fun size(): Int {
        return (tail - head) and capacityBitmask
    }

    /**
     * Return `true` if [size] is 0.
     *
     * @return `true` if [size] is 0.
     */
    fun isEmpty(): Boolean = head == tail
}
