@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import java.util.PrimitiveIterator
import java.util.function.Consumer

class VIntIteratorJava<T>(val delegate:java.util.PrimitiveIterator.OfInt, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = delegate.remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.nextInt())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class MutableVIntIteratorJava<T>(val delegate:java.util.PrimitiveIterator.OfInt, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T>  {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = delegate.remove()
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
class MutableVIntIteratorKotlin<T>(val delegate:kotlin.collections.IntIterator, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = (delegate as MutableIterator<Int>).remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.nextInt())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class VIntIteratorGeneric<T>(val delegate:Iterator<Int>, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.next())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class MutableVIntIteratorGeneric<T>(val delegate:MutableIterator<Int>, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = delegate.remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.next())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
context(a: ValueIntAdapter<T>) fun <T> vIteratorFrom(delegate:Iterator<Int>): PrimitiveIterator<T, Consumer<in T>> = when(delegate) {
    is java.util.PrimitiveIterator.OfInt -> VIntIteratorJava(delegate, a)
    is kotlin.collections.IntIterator -> VIntIteratorKotlin(delegate, a)
    else -> VIntIteratorGeneric(delegate, a)
}
context(a: ValueIntAdapter<T>) fun <T> mutableVIteratorFrom(delegate:MutableIterator<Int>): MutableIterator<T> = when(delegate) {
    is java.util.PrimitiveIterator.OfInt -> MutableVIntIteratorJava(delegate, a)
    is kotlin.collections.IntIterator -> MutableVIntIteratorKotlin(delegate, a)
    else -> MutableVIntIteratorGeneric(delegate, a)
}
context(a: ValueIntAdapter<T>) fun <T> vIteratableFrom(delegate:Iterator<Int>): Iterable<T> = when(delegate) {
    is java.util.PrimitiveIterator.OfInt -> VIntIteratorJava(delegate, a)
    is kotlin.collections.IntIterator -> VIntIteratorKotlin(delegate, a)
    else -> VIntIteratorGeneric(delegate, a)
}
context(a: ValueIntAdapter<T>) fun <T> mutableVIteratableFrom(delegate:MutableIterator<Int>): MutableIterable<T> = when(delegate) {
    is java.util.PrimitiveIterator.OfInt -> MutableVIntIteratorJava(delegate, a)
    is kotlin.collections.IntIterator -> MutableVIntIteratorKotlin(delegate, a)
    else -> MutableVIntIteratorGeneric(delegate, a)
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
class MutableVLongIteratorJava<T>(val delegate:java.util.PrimitiveIterator.OfLong, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T>  {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = delegate.remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromLong(delegate.nextLong())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class VLongIteratorKotlin<T>(val delegate:kotlin.collections.LongIterator, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextLong()
    inline fun nextLong(): T = a.fromLong(delegate.nextLong())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextLong()) }
}
class MutableVLongIteratorKotlin<T>(val delegate:kotlin.collections.LongIterator, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = (delegate as MutableIterator<Long>).remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromLong(delegate.nextLong())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class VLongIteratorGeneric<T>(val delegate:Iterator<Long>, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextLong()
    inline fun nextLong(): T = a.fromLong(delegate.next())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextLong()) }
}
class MutableVLongIteratorGeneric<T>(val delegate:MutableIterator<Long>, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = delegate.remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromLong(delegate.next())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
context(a: ValueLongAdapter<T>) fun <T> vIteratorFrom(delegate:Iterator<Long>): PrimitiveIterator<T, Consumer<in T>> = when(delegate) {
    is java.util.PrimitiveIterator.OfLong -> VLongIteratorJava(delegate, a)
    is kotlin.collections.LongIterator -> VLongIteratorKotlin(delegate, a)
    else -> VLongIteratorGeneric(delegate, a)
}
context(a: ValueLongAdapter<T>) fun <T> vIteratorFrom(delegate:MutableIterator<Long>): MutableIterator<T> = when(delegate) {
    is java.util.PrimitiveIterator.OfLong -> MutableVLongIteratorJava(delegate, a)
    is kotlin.collections.LongIterator -> MutableVLongIteratorKotlin(delegate, a)
    else -> MutableVLongIteratorGeneric(delegate, a)
}
context(a: ValueLongAdapter<T>) fun <T> mutableVIteratableFrom(delegate:Iterator<Long>): Iterable<T> = when(delegate) {
    is java.util.PrimitiveIterator.OfLong -> VLongIteratorJava(delegate, a)
    is kotlin.collections.LongIterator -> VLongIteratorKotlin(delegate, a)
    else -> VLongIteratorGeneric(delegate, a)
}
context(a: ValueLongAdapter<T>) fun <T> vIteratableFrom(delegate:MutableIterator<Long>): MutableIterable<T> = when(delegate) {
    is java.util.PrimitiveIterator.OfLong -> MutableVLongIteratorJava(delegate, a)
    is kotlin.collections.LongIterator -> MutableVLongIteratorKotlin(delegate, a)
    else -> MutableVLongIteratorGeneric(delegate, a)
}
class VIteratorIndexedValueLong<T>(val delegate:Iterator<IndexedValue<Long>>, val a: ValueLongAdapter<T>): Iterator<IndexedValue<T>>, Iterable<IndexedValue<T>> {
    override inline fun iterator(): VIteratorIndexedValueLong<T> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun next(): IndexedValue<T> {val iv=delegate.next(); return IndexedValue(iv.index, a.fromLong(iv.value)) }
}