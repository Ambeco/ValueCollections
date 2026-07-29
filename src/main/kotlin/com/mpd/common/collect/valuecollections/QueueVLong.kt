@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused")

package com.mpd.common.collect.valuecollections

import java.util.concurrent.TimeUnit

// java.util.Queue<T> -> QueueVLong<T>
// Like java.util.Queue, offer/poll/peek never throw on failure/empty (poll/peek use NULL_VALUE
// under the hood, exposed as T? via the typed layer); add/remove/element throw instead.
interface QueueVLong<T>: MutableCollectionVLong<T> {
    fun offerBits(bits: LongBits): Boolean
    fun pollBits(): LongBits
    fun peekBits(): LongBits
}

context(a: ValueLongAdapter<T>) inline fun <T> QueueVLong<T>.offer(value: T): Boolean = offerBits(a.toLong(value))
context(a: ValueLongAdapter<T>) inline fun <T> QueueVLong<T>.poll(): T? = fromLongOrNull(pollBits())
context(a: ValueLongAdapter<T>) inline fun <T> QueueVLong<T>.peek(): T? = fromLongOrNull(peekBits())
context(a: ValueLongAdapter<T>) inline fun <T> QueueVLong<T>.remove(): T = fromLong(pollBits())
context(a: ValueLongAdapter<T>) inline fun <T> QueueVLong<T>.element(): T = fromLong(peekBits())





// java.util.Deque<T> -> DequeVLong<T>
// Like java.util.Deque, offerFirst/offerLast/pollFirst/pollLast/peekFirst/peekLast never throw on
// failure/empty (poll/peek use NULL_VALUE under the hood, exposed as T? via the typed layer);
// addFirst/addLast/removeFirst/removeLast/push/pop throw instead.
interface DequeVLong<T>: QueueVLong<T> {
    fun addFirstBits(bits: LongBits)
    fun addLastBits(bits: LongBits)
    fun offerFirstBits(bits: LongBits): Boolean
    fun offerLastBits(bits: LongBits): Boolean
    fun removeFirstBits(): LongBits
    fun removeLastBits(): LongBits
    fun pollFirstBits(): LongBits
    fun pollLastBits(): LongBits
    fun peekFirstBits(): LongBits
    fun peekLastBits(): LongBits
    fun pushBits(bits: LongBits)
    fun popBits(): LongBits
}

context(a: ValueLongAdapter<T>) inline fun <T> DequeVLong<T>.addFirst(value: T): Unit = addFirstBits(a.toLong(value))
context(a: ValueLongAdapter<T>) inline fun <T> DequeVLong<T>.addLast(value: T): Unit = addLastBits(a.toLong(value))
context(a: ValueLongAdapter<T>) inline fun <T> DequeVLong<T>.offerFirst(value: T): Boolean = offerFirstBits(a.toLong(value))
context(a: ValueLongAdapter<T>) inline fun <T> DequeVLong<T>.offerLast(value: T): Boolean = offerLastBits(a.toLong(value))
context(a: ValueLongAdapter<T>) inline fun <T> DequeVLong<T>.removeFirst(): T = fromLong(removeFirstBits())
context(a: ValueLongAdapter<T>) inline fun <T> DequeVLong<T>.removeLast(): T = fromLong(removeLastBits())
context(a: ValueLongAdapter<T>) inline fun <T> DequeVLong<T>.pollFirst(): T? = fromLongOrNull(pollFirstBits())
context(a: ValueLongAdapter<T>) inline fun <T> DequeVLong<T>.pollLast(): T? = fromLongOrNull(pollLastBits())
context(a: ValueLongAdapter<T>) inline fun <T> DequeVLong<T>.peekFirst(): T? = fromLongOrNull(peekFirstBits())
context(a: ValueLongAdapter<T>) inline fun <T> DequeVLong<T>.peekLast(): T? = fromLongOrNull(peekLastBits())
context(a: ValueLongAdapter<T>) inline fun <T> DequeVLong<T>.push(value: T): Unit = pushBits(a.toLong(value))
context(a: ValueLongAdapter<T>) inline fun <T> DequeVLong<T>.pop(): T = fromLong(popBits())



// java.util.concurrent.BlockingQueue<T> -> BlockingQueueVLong<T>
interface BlockingQueueVLong<T>: QueueVLong<T> {
    fun putBits(bits: LongBits)
    fun takeBits(): LongBits
    fun offerBits(bits: LongBits, timeout: Long, unit: TimeUnit): Boolean
    fun pollBits(timeout: Long, unit: TimeUnit): LongBits
    fun remainingCapacity(): Int
    fun drainTo(destination: MutableCollectionVLong<T>): Int
    fun drainTo(destination: MutableCollectionVLong<T>, maxElements: Int): Int
}

context(a: ValueLongAdapter<T>) inline fun <T> BlockingQueueVLong<T>.put(value: T): Unit = putBits(a.toLong(value))
context(a: ValueLongAdapter<T>) inline fun <T> BlockingQueueVLong<T>.take(): T = fromLong(takeBits())
context(a: ValueLongAdapter<T>) inline fun <T> BlockingQueueVLong<T>.offer(value: T, timeout: Long, unit: TimeUnit): Boolean = offerBits(a.toLong(value), timeout, unit)
context(a: ValueLongAdapter<T>) inline fun <T> BlockingQueueVLong<T>.poll(timeout: Long, unit: TimeUnit): T? = fromLongOrNull(pollBits(timeout, unit))






// Marker interface: a QueueVLong whose poll()/peek() return elements in priority (comparator) order,
// analogous to how java.util.PriorityQueue relates to java.util.Queue.
interface PriorityQueueVLong<T>: QueueVLong<T>


// analogous to java.util.concurrent.PriorityBlockingQueue implementing both priority ordering and BlockingQueue
interface PriorityBlockingQueueVLong<T>: PriorityQueueVLong<T>, BlockingQueueVLong<T>