@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import java.util.PrimitiveIterator
import java.util.function.Consumer

open class VIntIterator<T>(val delegate:Iterator<Int>, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override fun iterator(): VIntIterator<T> = this
    final override inline fun hasNext(): Boolean = delegate.hasNext()
    override fun remove(): Unit = throw UnsupportedOperationException("remove")
    final override inline fun next(): T = nextInt()
    inline fun nextInt(): T = if (delegate is IntIterator) a.fromInt(delegate.nextInt()) else a.fromInt(delegate.next())
    final override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class MutableVIntIterator<T>(delegate:MutableIterator<Int>, a: ValueIntAdapter<T>): VIntIterator<T>(delegate,a), MutableIterator<T>, MutableIterable<T> {
    override inline fun iterator(): MutableVIntIterator<T> = this
    override inline fun remove(): Unit = (delegate as MutableIterator<Int>).remove()
}
class VIteratorIndexedValueInt<T>(val delegate:Iterator<IndexedValue<Int>>, val a: ValueIntAdapter<T>): Iterator<IndexedValue<T>>, Iterable<IndexedValue<T>> {
    override inline fun iterator(): VIteratorIndexedValueInt<T> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun next(): IndexedValue<T> {val iv=delegate.next(); return IndexedValue(iv.index, a.fromInt(iv.value)) }
}





open class VLongIterator<T>(val delegate:Iterator<Long>, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override fun iterator(): VLongIterator<T> = this
    final override inline fun hasNext(): Boolean = delegate.hasNext()
    override fun remove(): Unit = throw UnsupportedOperationException("remove")
    final override inline fun next(): T = nextLong()
    inline fun nextLong(): T = if (delegate is LongIterator) a.fromLong(delegate.nextLong()) else a.fromLong(delegate.next())
    final override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextLong()) }
}
class MutableVLongIterator<T>(delegate:MutableIterator<Long>, a: ValueLongAdapter<T>): VLongIterator<T>(delegate,a), MutableIterator<T>, MutableIterable<T> {
    override inline fun iterator(): MutableVLongIterator<T> = this
    override inline fun remove(): Unit = (delegate as MutableIterator<Long>).remove()
}
class VIteratorIndexedValueLong<T>(val delegate:Iterator<IndexedValue<Long>>, val a: ValueLongAdapter<T>): Iterator<IndexedValue<T>>, Iterable<IndexedValue<T>> {
    override inline fun iterator(): VIteratorIndexedValueLong<T> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun next(): IndexedValue<T> {val iv=delegate.next(); return IndexedValue(iv.index, a.fromLong(iv.value)) }
}