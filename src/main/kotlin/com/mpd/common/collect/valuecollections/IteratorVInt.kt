@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")

package com.mpd.common.collect.valuecollections

import java.util.PrimitiveIterator
import java.util.function.Consumer

class IteratorVIntJava<T>(val delegate: PrimitiveIterator.OfInt, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = delegate.remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.nextInt())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class MutableIteratorVIntJava<T>(val delegate: PrimitiveIterator.OfInt, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T>  {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = delegate.remove()
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.nextInt())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class IteratorVIntKotlin<T>(val delegate: IntIterator, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, Iterable<T> {
    override inline fun iterator(): PrimitiveIterator<T, Consumer<in T>> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextInt()
    inline fun nextInt(): T = a.fromInt(delegate.nextInt())
    override inline fun forEachRemaining(op: Consumer<in T>) { while (delegate.hasNext()) op.accept(nextInt()) }
}
class MutableIteratorVIntKotlin<T>(val delegate: IntIterator, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<in T>>, MutableIterable<T> {
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
    is PrimitiveIterator.OfInt -> IteratorVIntJava(delegate, a)
    is IntIterator -> IteratorVIntKotlin(delegate, a)
    else -> IteratorVIntGeneric(delegate, a)
}
context(a: ValueIntAdapter<T>) fun <T> mutableVIteratorFrom(delegate:MutableIterator<Int>): MutableIterator<T> = when(delegate) {
    is PrimitiveIterator.OfInt -> MutableIteratorVIntJava(delegate, a)
    is IntIterator -> MutableIteratorVIntKotlin(delegate, a)
    else -> MutableIteratorVIntGeneric(delegate, a)
}
context(a: ValueIntAdapter<T>) fun <T> vIteratableFrom(delegate:Iterator<Int>): Iterable<T> = when(delegate) {
    is PrimitiveIterator.OfInt -> IteratorVIntJava(delegate, a)
    is IntIterator -> IteratorVIntKotlin(delegate, a)
    else -> IteratorVIntGeneric(delegate, a)
}
context(a: ValueIntAdapter<T>) fun <T> mutableVIteratableFrom(delegate:MutableIterator<Int>): MutableIterable<T> = when(delegate) {
    is PrimitiveIterator.OfInt -> MutableIteratorVIntJava(delegate, a)
    is IntIterator -> MutableIteratorVIntKotlin(delegate, a)
    else -> MutableIteratorVIntGeneric(delegate, a)
}
    
class VIteratorIndexedValueInt<T>(val delegate:Iterator<IndexedValue<Int>>, val a: ValueIntAdapter<T>): Iterator<IndexedValue<T>>, Iterable<IndexedValue<T>> {
    override inline fun iterator(): VIteratorIndexedValueInt<T> = this
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun next(): IndexedValue<T> {val iv=delegate.next(); return IndexedValue(iv.index, a.fromInt(iv.value)) }
}

open class ListIteratorVInt<T>(protected val list: List<T>, startIndex: Int = 0) : ListIterator<T> {
    protected var idx = startIndex
    override fun hasNext(): Boolean = idx < list.size
    override fun next(): T = list[idx++]
    override fun hasPrevious(): Boolean = idx > 0
    override fun previous(): T = list[--idx]
    override fun nextIndex(): Int = idx
    override fun previousIndex(): Int = idx - 1
}
class MutableListIteratorVInt<T>(private val mutableList: MutableList<T>, startIndex: Int = 0) : ListIteratorVInt<T>(mutableList, startIndex), MutableListIterator<T> {
    override fun set(element: T) { mutableList[idx - 1] = element }
    override fun add(element: T) { mutableList.add(idx, element); idx++ }
    override fun remove() { mutableList.removeAt(--idx) }
}

