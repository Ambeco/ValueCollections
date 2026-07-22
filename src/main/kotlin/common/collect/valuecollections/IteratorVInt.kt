@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import java.util.PrimitiveIterator
import java.util.function.Consumer

class IteratorVIntJava<T>(val delegate:java.util.PrimitiveIterator.OfInt, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = delegate.remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.nextInt())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class MutableIteratorVIntJava<T>(val delegate:java.util.PrimitiveIterator.OfInt, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T>  {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = delegate.remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.nextInt())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class IteratorVIntKotlin<T>(val delegate:kotlin.collections.IntIterator, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.nextInt())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class MutableIteratorVIntKotlin<T>(val delegate:kotlin.collections.IntIterator, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = (delegate as MutableIterator<Int>).remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.nextInt())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class IteratorVIntGeneric<T>(val delegate:Iterator<Int>, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.next())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class MutableIteratorVIntGeneric<T>(val delegate:MutableIterator<Int>, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = delegate.remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.next())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
context(a: ValueIntAdapter<T>) fun <T> vIteratorFrom(delegate:Iterator<Int>): PrimitiveIterator<T, Consumer<in T>> = when(delegate) {
    is java.util.PrimitiveIterator.OfInt -> IteratorVIntJava(delegate, a)
    is kotlin.collections.IntIterator -> IteratorVIntKotlin(delegate, a)
    else -> IteratorVIntGeneric(delegate, a)
}
context(a: ValueIntAdapter<T>) fun <T> mutableVIteratorFrom(delegate:MutableIterator<Int>): MutableIterator<T> = when(delegate) {
    is java.util.PrimitiveIterator.OfInt -> MutableIteratorVIntJava(delegate, a)
    is kotlin.collections.IntIterator -> MutableIteratorVIntKotlin(delegate, a)
    else -> MutableIteratorVIntGeneric(delegate, a)
}
context(a: ValueIntAdapter<T>) fun <T> vIteratableFrom(delegate:Iterator<Int>): Iterable<T> = when(delegate) {
    is java.util.PrimitiveIterator.OfInt -> IteratorVIntJava(delegate, a)
    is kotlin.collections.IntIterator -> IteratorVIntKotlin(delegate, a)
    else -> IteratorVIntGeneric(delegate, a)
}
context(a: ValueIntAdapter<T>) fun <T> mutableVIteratableFrom(delegate:MutableIterator<Int>): MutableIterable<T> = when(delegate) {
    is java.util.PrimitiveIterator.OfInt -> MutableIteratorVIntJava(delegate, a)
    is kotlin.collections.IntIterator -> MutableIteratorVIntKotlin(delegate, a)
    else -> MutableIteratorVIntGeneric(delegate, a)
}
    
class VIteratorIndexedValueInt<T>(val delegate:Iterator<IndexedValue<Int>>, val a: ValueIntAdapter<T>): Iterator<IndexedValue<T>>, Iterable<IndexedValue<T>> {
    override inline fun iterator(): VIteratorIndexedValueInt<T> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun next(): IndexedValue<T> {val iv=delegate.next(); return IndexedValue(iv.index, a.fromInt(iv.value)) }
}

