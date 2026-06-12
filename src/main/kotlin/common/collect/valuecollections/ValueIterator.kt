@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import java.util.PrimitiveIterator
import java.util.function.Consumer

class VIntIteratorJava<T>(val delegate:java.util.PrimitiveIterator.OfInt, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.nextInt())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class VIntIteratorKotlin<T>(val delegate:kotlin.collections.IntIterator, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.nextInt())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class VIntIteratorGeneric<T>(val delegate:Iterator<Int>, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.next() as Int)
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
context(a: ValueIntAdapter<T>) fun <T> VIteratorFrom(delegate:Iterator<Int>): PrimitiveIterator<T, Consumer<in T>> = when(delegate) {
    is java.util.PrimitiveIterator.OfInt -> VIntIteratorJava(delegate, a)
    is kotlin.collections.IntIterator -> VIntIteratorKotlin(delegate, a)
    else -> VIntIteratorGeneric(delegate, a)
}
context(a: ValueIntAdapter<T>) fun <T> VIteratableFrom(delegate:Iterator<Int>): Iterable<T> = when(delegate) {
    is java.util.PrimitiveIterator.OfInt -> VIntIteratorJava(delegate, a)
    is kotlin.collections.IntIterator -> VIntIteratorKotlin(delegate, a)
    else -> VIntIteratorGeneric(delegate, a)
}
    
class VIteratorIndexedValueInt<T>(val delegate:Iterator<IndexedValue<Int>>, val a: ValueIntAdapter<T>): Iterator<IndexedValue<T>>, Iterable<IndexedValue<T>> {
    override inline fun iterator(): VIteratorIndexedValueInt<T> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun next(): IndexedValue<T> {val iv=delegate.next(); return IndexedValue(iv.index, a.fromInt(iv.value)) }
}





class VLongIteratorJava<T>(val delegate:java.util.PrimitiveIterator.OfLong, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextLong()
    inline fun nextLong(): T = a.fromLong(delegate.nextLong())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextLong()) }
}
class VLongIteratorKotlin<T>(val delegate:kotlin.collections.LongIterator, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextLong()
    inline fun nextLong(): T = a.fromLong(delegate.nextLong())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextLong()) }
}
class VLongIteratorGeneric<T>(val delegate:Iterator<Long>, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextLong()
    inline fun nextLong(): T = a.fromLong(delegate.next() as Long)
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextLong()) }
}
context(a: ValueLongAdapter<T>) fun <T> VIteratorFrom(delegate:Iterator<Long>): PrimitiveIterator<T, Consumer<in T>> = when(delegate) {
    is java.util.PrimitiveIterator.OfLong -> VLongIteratorJava(delegate, a)
    is kotlin.collections.LongIterator -> VLongIteratorKotlin(delegate, a)
    else -> VLongIteratorGeneric(delegate, a)
}
context(a: ValueLongAdapter<T>) fun <T> VIteratableFrom(delegate:Iterator<Long>): Iterable<T> = when(delegate) {
    is java.util.PrimitiveIterator.OfLong -> VLongIteratorJava(delegate, a)
    is kotlin.collections.LongIterator -> VLongIteratorKotlin(delegate, a)
    else -> VLongIteratorGeneric(delegate, a)
}
class VIteratorIndexedValueLong<T>(val delegate:Iterator<IndexedValue<Long>>, val a: ValueLongAdapter<T>): Iterator<IndexedValue<T>>, Iterable<IndexedValue<T>> {
    override inline fun iterator(): VIteratorIndexedValueLong<T> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun next(): IndexedValue<T> {val iv=delegate.next(); return IndexedValue(iv.index, a.fromLong(iv.value)) }
}