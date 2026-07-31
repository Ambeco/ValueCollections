@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused", "RedundantNullableReturnType",
    "KotlinConstantConditions", "KotlinConstantConditions"
)

package com.mpd.common.collect.valuecollections

// can modify elements, but not add or remove
interface ModifiableIndexedCollectionVInt<T>: IndexedCollectionVInt<T>, ModifiableCollectionVInt<T> {
    fun setBits(index: Int, bits: IntBits)
}
context(a: ValueIntAdapter<T>) inline fun <T> ModifiableIndexedCollectionVInt<T>.asListGeneric() = object: MutableList<T> {
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
    override inline fun listIterator(): MutableListIterator<T> = MutableListIteratorVInt(this)
    override inline fun listIterator(index: Int): MutableListIterator<T> = MutableListIteratorVInt(this, index)
    override inline fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = throw NotImplementedError() //  this@asListGeneric.subList(fromIndex, toIndex)
}
context(a: ValueIntAdapter<T>) inline fun <T> ModifiableIndexedCollectionVInt<T>.set(index: Int, value: T): T {val old = a.fromInt(bitsAtIndex(index)); setBits(index, a.toInt(value)); return old}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> ModifiableIndexedCollectionVInt<T>.sort(): Unit = asListGeneric().sort()
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> ModifiableIndexedCollectionVInt<T>.sort(fromIndex: Int, toIndex: Int): Unit = asListGeneric().subList(fromIndex, toIndex).sort()
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> ModifiableIndexedCollectionVInt<T>.sortDescending(): Unit = asListGeneric().sortDescending()
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> ModifiableIndexedCollectionVInt<T>.sortDescending(fromIndex: Int, toIndex: Int): Unit = asListGeneric().subList(fromIndex, toIndex).sortDescending()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> ModifiableIndexedCollectionVInt<T>.sortBy(crossinline selector: (T) -> R?): Unit = asListGeneric().sortBy(selector)
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> ModifiableIndexedCollectionVInt<T>.sortByDescending(crossinline selector: (T) -> R?): Unit = asListGeneric().sortByDescending(selector)
context(a: ValueIntAdapter<T>) inline fun <T> ModifiableIndexedCollectionVInt<T>.sortWith(comparator: Comparator<in T>): Unit = asListGeneric().sortWith(comparator)


// can modify, add, and remove elements
interface MutableIndexedCollectionVInt<T>: ModifiableIndexedCollectionVInt<T>, MutableCollectionVInt<T> {
    fun addBits(index: Int, bits: IntBits)
    fun addAll(index: Int, elements: CollectionVInt<T>): Boolean
    context(a: ValueIntAdapter<T>) fun addAll(index: Int, elements: Collection<T>): Boolean

    fun removeAtBits(index: Int): IntBits
    fun removeRange(start: Int, end: Int)
    fun removeAllIndexedBits(predicate: (index: Int, bits: IntBits) -> Boolean): Boolean
}
context(a: ValueIntAdapter<T>) inline fun <T> MutableIndexedCollectionVInt<T>.asListGeneric() = object: MutableList<T> {
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
    override inline fun listIterator(): MutableListIterator<T> = MutableListIteratorVInt(this)
    override inline fun listIterator(index: Int): MutableListIterator<T> = MutableListIteratorVInt(this, index)
    override inline fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = throw NotImplementedError() // this@asListGeneric.subList(fromIndex, toIndex)
}

context(a: ValueIntAdapter<T>) inline fun <T> MutableIndexedCollectionVInt<T>.add(index: Int, element: T): Unit = addBits(index, a.toInt(element))
context(a: ValueIntAdapter<T>) inline fun <T> MutableIndexedCollectionVInt<T>.removeAt(index: Int): T = a.fromInt(removeAtBits(index))
context(a: ValueIntAdapter<T>) inline fun <T> MutableIndexedCollectionVInt<T>.retainAll(elements: ListVInt<T>): Boolean = removeAllIndexedBits { i, b -> !elements.containsBits(b) }





