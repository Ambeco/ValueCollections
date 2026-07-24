@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused")

package mpd.com.common.collect.valuecollections

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