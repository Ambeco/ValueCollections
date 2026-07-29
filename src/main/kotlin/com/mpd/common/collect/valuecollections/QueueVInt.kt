@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused")

package com.mpd.common.collect.valuecollections

import java.util.concurrent.TimeUnit

// java.util.Queue<T> -> QueueVInt<T>
// Like java.util.Queue, offer/poll/peek never throw on failure/empty (poll/peek use NULL_VALUE
// under the hood, exposed as T? via the typed layer); add/remove/element throw instead.
interface QueueVInt<T>: MutableCollectionVInt<T> {
    fun offerBits(bits: IntBits): Boolean
    fun pollBits(): IntBits
    fun peekBits(): IntBits
}

context(a: ValueIntAdapter<T>) inline fun <T> QueueVInt<T>.offer(value: T): Boolean = offerBits(a.toInt(value))
context(a: ValueIntAdapter<T>) inline fun <T> QueueVInt<T>.poll(): T? = fromIntOrNull(pollBits())
context(a: ValueIntAdapter<T>) inline fun <T> QueueVInt<T>.peek(): T? = fromIntOrNull(peekBits())
context(a: ValueIntAdapter<T>) inline fun <T> QueueVInt<T>.remove(): T = fromInt(pollBits())
context(a: ValueIntAdapter<T>) inline fun <T> QueueVInt<T>.element(): T = fromInt(peekBits())




// java.util.concurrent.BlockingQueue<T> -> BlockingQueueVInt<T>
interface BlockingQueueVInt<T>: QueueVInt<T> {
    fun putBits(bits: IntBits)
    fun takeBits(): IntBits
    fun offerBits(bits: IntBits, timeout: Long, unit: TimeUnit): Boolean
    fun pollBits(timeout: Long, unit: TimeUnit): IntBits
    fun remainingCapacity(): Int
    fun drainTo(destination: MutableCollectionVInt<T>): Int
    fun drainTo(destination: MutableCollectionVInt<T>, maxElements: Int): Int
}

context(a: ValueIntAdapter<T>) inline fun <T> BlockingQueueVInt<T>.put(value: T): Unit = putBits(a.toInt(value))
context(a: ValueIntAdapter<T>) inline fun <T> BlockingQueueVInt<T>.take(): T = fromInt(takeBits())
context(a: ValueIntAdapter<T>) inline fun <T> BlockingQueueVInt<T>.offer(value: T, timeout: Long, unit: TimeUnit): Boolean = offerBits(a.toInt(value), timeout, unit)
context(a: ValueIntAdapter<T>) inline fun <T> BlockingQueueVInt<T>.poll(timeout: Long, unit: TimeUnit): T? = fromIntOrNull(pollBits(timeout, unit))




// Marker interface: a QueueVInt whose poll()/peek() return elements in priority (comparator) order,
// analogous to how java.util.PriorityQueue relates to java.util.Queue.
interface PriorityQueueVInt<T>: QueueVInt<T>



// analogous to java.util.concurrent.PriorityBlockingQueue implementing both priority ordering and BlockingQueue
interface PriorityBlockingQueueVInt<T>: PriorityQueueVInt<T>, BlockingQueueVInt<T>