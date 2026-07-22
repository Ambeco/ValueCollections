@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import java.util.PrimitiveIterator
import java.util.function.Consumer

class IteratorVLongJava<T>(val delegate: PrimitiveIterator.OfLong, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextLong()
    inline fun nextLong(): T = a.fromLong(delegate.nextLong())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextLong()) }
}
class MutableIteratorVLongJava<T>(val delegate: PrimitiveIterator.OfLong, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T>  {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = delegate.remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromLong(delegate.nextLong())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class IteratorVLongKotlin<T>(val delegate: LongIterator, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextLong()
    inline fun nextLong(): T = a.fromLong(delegate.nextLong())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextLong()) }
}
class MutableIteratorVLongKotlin<T>(val delegate: LongIterator, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = (delegate as MutableIterator<Long>).remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromLong(delegate.nextLong())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class IteratorVLongGeneric<T>(val delegate:Iterator<Long>, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextLong()
    inline fun nextLong(): T = a.fromLong(delegate.next())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextLong()) }
}
class MutableIteratorVLongGeneric<T>(val delegate:MutableIterator<Long>, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = delegate.remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromLong(delegate.next())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
context(a: ValueLongAdapter<T>) fun <T> vIteratorFrom(delegate:Iterator<Long>): PrimitiveIterator<T, Consumer<in T>> = when(delegate) {
    is PrimitiveIterator.OfLong -> IteratorVLongJava(delegate, a)
    is LongIterator -> IteratorVLongKotlin(delegate, a)
    else -> IteratorVLongGeneric(delegate, a)
}
context(a: ValueLongAdapter<T>) fun <T> vIteratorFrom(delegate:MutableIterator<Long>): MutableIterator<T> = when(delegate) {
    is PrimitiveIterator.OfLong -> MutableIteratorVLongJava(delegate, a)
    is LongIterator -> MutableIteratorVLongKotlin(delegate, a)
    else -> MutableIteratorVLongGeneric(delegate, a)
}
context(a: ValueLongAdapter<T>) fun <T> mutableVIteratableFrom(delegate:Iterator<Long>): Iterable<T> = when(delegate) {
    is PrimitiveIterator.OfLong -> IteratorVLongJava(delegate, a)
    is LongIterator -> IteratorVLongKotlin(delegate, a)
    else -> IteratorVLongGeneric(delegate, a)
}
context(a: ValueLongAdapter<T>) fun <T> vIteratableFrom(delegate:MutableIterator<Long>): MutableIterable<T> = when(delegate) {
    is PrimitiveIterator.OfLong -> MutableIteratorVLongJava(delegate, a)
    is LongIterator -> MutableIteratorVLongKotlin(delegate, a)
    else -> MutableIteratorVLongGeneric(delegate, a)
}
class VIteratorIndexedValueLong<T>(val delegate:Iterator<IndexedValue<Long>>, val a: ValueLongAdapter<T>): Iterator<IndexedValue<T>>, Iterable<IndexedValue<T>> {
    override inline fun iterator(): VIteratorIndexedValueLong<T> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun next(): IndexedValue<T> {val iv=delegate.next(); return IndexedValue(iv.index, a.fromLong(iv.value)) }
}