@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused", "RedundantNullableReturnType",
    "KotlinConstantConditions", "KotlinConstantConditions"
)
package mpd.com.common.collect.valuecollections

import kotlin.collections.sort
import kotlin.collections.sortBy
import kotlin.collections.sortByDescending
import kotlin.collections.sortDescending
import kotlin.collections.sortWith


// can modify elements, but not add or remove
interface ModifiableIndexedCollectionVLong<T>: IndexedCollectionVLong<T>, ModifiableCollectionVLong<T> {
    fun setBits(index: Int, bits: LongBits)
}
context(a: ValueLongAdapter<T>) inline fun <T> ModifiableIndexedCollectionVLong<T>.asListGeneric() = object: MutableList<T> {
    override val size: Int get() = this@asListGeneric.size
    override inline fun isEmpty(): Boolean = this@asListGeneric.size==0
    override inline fun contains(element: T): Boolean = this@asListGeneric.contains(element)
    override inline fun iterator(): MutableIterator<T> = this@asListGeneric.asModifiableIterable().iterator()
    override inline fun containsAll(elements: Collection<T>): Boolean = this@asListGeneric.containsAll(elements)
    override inline fun get(index: Int): T = this@asListGeneric.get(index)
    override inline fun indexOf(element: T): Int = this@asListGeneric.indexOf(element)
    override inline fun lastIndexOf(element: T): Int = this@asListGeneric.lastIndexOf(element)
    override inline fun add(element: T): Boolean = throw NotImplementedError("Collection elements are modifiable, but the collection itself is not mutable")
    override inline fun remove(element: T): Boolean = throw NotImplementedError("Collection elements are modifiable, but the collection itself is not mutable")
    override inline fun addAll(elements: Collection<T>): Boolean = throw NotImplementedError("Collection elements are modifiable, but the collection itself is not mutable")
    override inline fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError("Collection elements are modifiable, but the collection itself is not mutable")
    override inline fun removeAll(elements: Collection<T>): Boolean = throw NotImplementedError("Collection elements are modifiable, but the collection itself is not mutable")
    override inline fun retainAll(elements: Collection<T>): Boolean = throw NotImplementedError("Collection elements are modifiable, but the collection itself is not mutable")
    override inline fun clear() = throw NotImplementedError("Collection elements are modifiable, but the collection itself is not mutable")
    override inline fun set(index: Int, element: T): T = this@asListGeneric.set(index, element)
    override inline fun add(index: Int, element: T) = throw NotImplementedError("Collection elements are modifiable, but the collection itself is not mutable")
    override inline fun removeAt(index: Int): T = throw NotImplementedError("Collection elements are modifiable, but the collection itself is not mutable")
    override fun listIterator(): MutableListIterator<T> = listIterator(0)
    override fun listIterator(index: Int): MutableListIterator<T> = object : MutableListIterator<T> {
        var idx = index
        override fun hasNext(): Boolean = idx < size
        override fun next(): T = this@asListGeneric.get(idx++)
        override fun hasPrevious(): Boolean = idx > 0
        override fun previous(): T = this@asListGeneric.get(--idx)
        override fun nextIndex(): Int = idx
        override fun previousIndex(): Int = idx - 1
        override fun set(element: T) { this@asListGeneric.set(idx - 1, element) }
        override fun add(element: T) = throw UnsupportedOperationException()
        override fun remove() = throw UnsupportedOperationException()
    }
    override inline fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = throw NotImplementedError() //  this@asListGeneric.subList(fromIndex, toIndex)
}
context(a: ValueLongAdapter<T>) inline fun <T> ModifiableIndexedCollectionVLong<T>.set(index: Int, value: T): T {setBits(index, a.toLong(value)); return value}
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> ModifiableIndexedCollectionVLong<T>.sort(): Unit = asListGeneric().sort()
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> ModifiableIndexedCollectionVLong<T>.sort(fromIndex: Int, toIndex: Int): Unit = asListGeneric().subList(fromIndex, toIndex).sort()
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> ModifiableIndexedCollectionVLong<T>.sortDescending(): Unit = asListGeneric().sortDescending()
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> ModifiableIndexedCollectionVLong<T>.sortDescending(fromIndex: Int, toIndex: Int): Unit = asListGeneric().subList(fromIndex, toIndex).sortDescending()
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> ModifiableIndexedCollectionVLong<T>.sortBy(crossinline selector: (T) -> R?): Unit = asListGeneric().sortBy(selector)
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> ModifiableIndexedCollectionVLong<T>.sortByDescending(crossinline selector: (T) -> R?): Unit = asListGeneric().sortByDescending(selector)
context(a: ValueLongAdapter<T>) inline fun <T> ModifiableIndexedCollectionVLong<T>.sortWith(comparator: Comparator<in T>): Unit = asListGeneric().sortWith(comparator)


// can modify, add, and remove elements
interface MutableIndexedCollectionVLong<T>: ModifiableIndexedCollectionVLong<T>, MutableCollectionVLong<T> {
    fun addBits(index: Int, bits: LongBits)
    fun addAll(index: Int, elements: CollectionVLong<T>): Boolean
    context(a: ValueLongAdapter<T>) fun addAll(index: Int, elements: Collection<T>): Boolean

    context(a: ValueLongAdapter<T>) fun removeAt(index: Int): T
    fun removeRange(start: Int, end: Int)
    fun removeAllIndexedBits(predicate: (index: Int, bits: LongBits) -> Boolean): Boolean
}
context(a: ValueLongAdapter<T>) inline fun <T> MutableIndexedCollectionVLong<T>.asListGeneric() = object: MutableList<T> {
    override val size: Int get() = this@asListGeneric.size
    override inline fun isEmpty(): Boolean = this@asListGeneric.size==0
    override inline fun contains(element: T): Boolean = this@asListGeneric.contains(element)
    override inline fun iterator(): MutableIterator<T> = this@asListGeneric.asIterable().iterator()
    override inline fun containsAll(elements: Collection<T>): Boolean = this@asListGeneric.containsAll(elements)
    override inline fun get(index: Int): T = this@asListGeneric.get(index)
    override inline fun indexOf(element: T): Int = this@asListGeneric.indexOf(element)
    override inline fun lastIndexOf(element: T): Int = this@asListGeneric.lastIndexOf(element)
    override inline fun add(element: T): Boolean = this@asListGeneric.add(element)
    override inline fun remove(element: T): Boolean = this@asListGeneric.remove(element)
    override inline fun addAll(elements: Collection<T>): Boolean = this@asListGeneric.addAll(elements)
    override inline fun addAll(index: Int, elements: Collection<T>): Boolean = this@asListGeneric.addAll(index,elements)
    override inline fun removeAll(elements: Collection<T>): Boolean = this@asListGeneric.removeAll(elements)
    override inline fun retainAll(elements: Collection<T>): Boolean = this@asListGeneric.retainAll(elements)
    override inline fun clear() = this@asListGeneric.clear()
    override inline fun set(index: Int, element: T): T = this@asListGeneric.set(index, element)
    override inline fun add(index: Int, element: T) = this@asListGeneric.add(index, element)
    override inline fun removeAt(index: Int): T = this@asListGeneric.removeAt(index)
    override fun listIterator(): MutableListIterator<T> = listIterator(0)
    override fun listIterator(index: Int): MutableListIterator<T> = object : MutableListIterator<T> {
        var idx = index
        override fun hasNext(): Boolean = idx < size
        override fun next(): T = this@asListGeneric.get(idx++)
        override fun hasPrevious(): Boolean = idx > 0
        override fun previous(): T = this@asListGeneric.get(--idx)
        override fun nextIndex(): Int = idx
        override fun previousIndex(): Int = idx - 1
        override fun set(element: T) { this@asListGeneric.set(idx - 1, element) }
        override fun add(element: T) = throw UnsupportedOperationException()
        override fun remove() = throw UnsupportedOperationException()
    }
    override inline fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = throw NotImplementedError() // this@asListGeneric.subList(fromIndex, toIndex)
}

context(a: ValueLongAdapter<T>) inline fun <T> MutableIndexedCollectionVLong<T>.add(index: Int, element: T): Unit = addBits(index, a.toLong(element))
context(a: ValueLongAdapter<T>) inline fun <T> MutableIndexedCollectionVLong<T>.retainAll(elements: ListVLong<T>): Boolean = removeAllIndexedBits{ i, b-> !elements.containsBits(b)}
