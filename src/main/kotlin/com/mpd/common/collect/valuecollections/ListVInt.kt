@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")

package com.mpd.common.collect.valuecollections

import androidx.collection.MutableIntList
import kotlin.also

// inline wrappers around androidx.collection.IntList and LongList

// IntList -> VIntList
interface ListVInt<T>: IndexedCollectionVInt<T>

interface ModifiableListVInt<T>: ListVInt<T>, ModifiableIndexedCollectionVInt<T>
context(a: ValueIntAdapter<T>) inline operator fun <T> ModifiableListVInt<T>.set(index: Int, value: T) {setBits(index, a.toInt(value)) }

interface MutableListVInt<T>: ModifiableListVInt<T>, MutableIndexedCollectionVInt<T> {
    val capacity: Int
}

class ArrayListVInt<T>(val collection: MutableIntList = MutableIntList(), override val NULL_VALUE: IntBits=Int.MIN_VALUE): MutableListVInt<T> {
    constructor(capacity: Int, NO_VALUE:IntBits=Int.MIN_VALUE) : this(MutableIntList(capacity), NO_VALUE)
    constructor(other: CollectionVInt<T>, NO_VALUE:IntBits=Int.MIN_VALUE) : this(MutableIntList(other.size), NO_VALUE) { other.forEachBits { collection.add(it) } }
    constructor(other: ListVInt<T>, NO_VALUE:IntBits=Int.MIN_VALUE) : this(MutableIntList(other.size), NO_VALUE) {other.copyInto(this,0,0,other.size)}

    override val size inline get() = collection.size
    override inline fun anyBits(predicate: (bits: IntBits) -> Boolean): IntBits = getBits(collection.indexOfFirst { predicate(it) })
    override inline fun containsBits(bits: IntBits): Boolean = collection.contains(bits)
    context(a: ValueIntAdapter<T>) override inline fun asIterable(): MutableIterable<T> = object : MutableIterable<T> {
        override inline fun iterator(): MutableIterator<T> = object : MutableIterator<T> {
            var idx = 0
            override inline fun hasNext(): Boolean = idx < size
            override inline fun next(): T = a.fromInt(collection[idx++])
            override inline fun remove() { collection.removeAt(--idx) }
        }
    }
    context(a: ValueIntAdapter<T>) override inline fun toString(): String = toStringV()
    
    override val capacity inline get() = collection.capacity
    override inline fun ensureCapacity(newCapacity: Int): Boolean { collection.ensureCapacity(newCapacity); return true;}
    override inline fun trim(minCapacity: Int) = collection.trim(minCapacity)
    override inline fun addBits(bits: IntBits): Boolean = collection.add(bits)
    override inline fun removeBits(bits: IntBits): Boolean = collection.remove(bits)

    context(a: ValueIntAdapter<T>) override inline fun removeAll(predicate: (T) -> Boolean): Boolean  {
        val removeList = MutableIntList(size)
        collection.forEach { if (predicate(a.fromInt(it))) removeList.add(it) }
        collection.removeAll(removeList)
        return true
    }

    override inline fun clear() = collection.clear()

    override inline fun bitsAtIndex(index: Int): IntBits = if (index in 0..<size) collection[index] else NULL_VALUE
    override inline fun indexOfBits(bits: IntBits): Int = collection.indexOf(bits)
    
    override inline fun setBits(index: Int, bits: IntBits) { collection[index] = bits }

    override inline fun addBits(index: Int, bits: IntBits) = collection.add(index, bits)
    override inline fun addAll(index: Int, elements: CollectionVInt<T>): Boolean = throw NotImplementedError()
    context(a: ValueIntAdapter<T>) override inline fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError()
    context(a: ValueIntAdapter<T>) override inline fun removeAt(index: Int): T = a.fromInt(collection.removeAt(index))
    override inline fun removeRange(start: Int, end: Int) = collection.removeRange(start, end)

    override inline fun removeAllIndexedBits(crossinline predicate: (index: Int, bits: IntBits) -> Boolean): Boolean = throw NotImplementedError()
    
    override inline fun hashCode() = contentHashCode()
    @Suppress("UNCHECKED_CAST")
    override inline fun equals(other: Any?) = other is IndexedCollectionVInt<*> && contentEquals(other as IndexedCollectionVInt<T>)
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!

    // Fast-path members mirroring androidx.collection.IntList/MutableIntList, delegating directly
    // to `collection` instead of going through the generic bits-based CollectionVInt/
    // IndexedCollectionVInt extension functions. Kotlin prefers a member over an extension
    // function of the same name/signature, so these transparently speed up calls made directly on
    // an ArrayListVInt without requiring any call-site changes.
    inline fun none(): Boolean = collection.none()
    inline fun any(): Boolean = collection.any()
    context(a: ValueIntAdapter<T>) inline fun any(crossinline predicate: (T) -> Boolean): Boolean = collection.any { predicate(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun reversedAny(crossinline predicate: (T) -> Boolean): Boolean = collection.reversedAny { predicate(a.fromInt(it)) }
    inline fun containsAll(elements: ArrayListVInt<T>): Boolean = collection.containsAll(elements.collection)
    inline fun count(): Int = collection.count()
    context(a: ValueIntAdapter<T>) inline fun count(crossinline predicate: (T) -> Boolean): Int = collection.count { predicate(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun first(): T = a.fromInt(collection.first())
    context(a: ValueIntAdapter<T>) inline fun first(crossinline predicate: (T) -> Boolean): T = a.fromInt(collection.first { predicate(a.fromInt(it)) })
    context(a: ValueIntAdapter<T>) inline fun <R> fold(initial: R, crossinline operation: (acc: R, T) -> R): R = collection.fold(initial) { acc, e -> operation(acc, a.fromInt(e)) }
    context(a: ValueIntAdapter<T>) inline fun <R> foldIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): R = collection.foldIndexed(initial) { i, acc, e -> operation(i, acc, a.fromInt(e)) }
    context(a: ValueIntAdapter<T>) inline fun <R> foldRight(initial: R, crossinline operation: (T, acc: R) -> R): R = collection.foldRight(initial) { e, acc -> operation(a.fromInt(e), acc) }
    context(a: ValueIntAdapter<T>) inline fun <R> foldRightIndexed(initial: R, crossinline operation: (index: Int, T, acc: R) -> R): R = collection.foldRightIndexed(initial) { i, e, acc -> operation(i, a.fromInt(e), acc) }
    context(a: ValueIntAdapter<T>) inline fun forEach(crossinline block: (T) -> Unit) = collection.forEach { block(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun forEachIndexed(crossinline block: (index: Int, T) -> Unit) = collection.forEachIndexed { i, e -> block(i, a.fromInt(e)) }
    context(a: ValueIntAdapter<T>) inline fun forEachReversed(crossinline block: (T) -> Unit) = collection.forEachReversed { block(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun forEachReversedIndexed(crossinline block: (index: Int, T) -> Unit) = collection.forEachReversedIndexed { i, e -> block(i, a.fromInt(e)) }
    context(a: ValueIntAdapter<T>) inline fun elementAt(index: Int): T = a.fromInt(collection.elementAt(index))
    context(a: ValueIntAdapter<T>) inline fun elementAtOrElse(index: Int, crossinline defaultValue: (index: Int) -> T): T = a.fromInt(collection.elementAtOrElse(index) { a.toInt(defaultValue(it)) })
    context(a: ValueIntAdapter<T>) inline fun indexOfFirst(crossinline predicate: (T) -> Boolean): Int = collection.indexOfFirst { predicate(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun indexOfLast(crossinline predicate: (T) -> Boolean): Int = collection.indexOfLast { predicate(a.fromInt(it)) }
    inline fun isEmpty(): Boolean = collection.isEmpty()
    inline fun isNotEmpty(): Boolean = collection.isNotEmpty()
    context(a: ValueIntAdapter<T>) inline fun last(): T = a.fromInt(collection.last())
    context(a: ValueIntAdapter<T>) inline fun last(crossinline predicate: (T) -> Boolean): T = a.fromInt(collection.last { predicate(a.fromInt(it)) })
    context(a: ValueIntAdapter<T>) inline fun lastIndexOf(element: T): Int = collection.lastIndexOf(a.toInt(element))
    context(a: ValueIntAdapter<T>) inline fun binarySearch(element: T, fromIndex: Int = 0, toIndex: Int = size): Int = collection.binarySearch(a.toInt(element), fromIndex, toIndex)
    context(a: ValueIntAdapter<T>) inline fun joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: (T) -> CharSequence = { it.toString() }): String =
        collection.joinToString(separator, prefix, postfix, limit, truncated) { transform(a.fromInt(it)) }

    // MutableIntList members: bulk operations get a raw-IntArray overload and a fast-path overload
    // for when the other collection also happens to be backed by MutableIntList.
    inline fun addAllBits(index: Int, elements: IntArray): Boolean = collection.addAll(index, elements)
    inline fun addAllBits(elements: IntArray): Boolean = collection.addAll(elements)
    inline fun addAll(index: Int, elements: ArrayListVInt<T>): Boolean = collection.addAll(index, elements.collection)
    inline fun addAll(elements: ArrayListVInt<T>): Boolean = collection.addAll(elements.collection)
    inline operator fun plusAssign(elements: IntArray): Unit = collection.plusAssign(elements)
    inline operator fun plusAssign(elements: ArrayListVInt<T>): Unit = collection.plusAssign(elements.collection)
    inline fun removeAllBits(elements: IntArray): Boolean = collection.removeAll(elements)
    inline fun removeAll(elements: ArrayListVInt<T>): Boolean = collection.removeAll(elements.collection)
    inline operator fun minusAssign(elements: IntArray): Unit { collection.minusAssign(elements) }
    inline operator fun minusAssign(elements: ArrayListVInt<T>): Unit { collection.minusAssign(elements.collection) }
    inline fun retainAllBits(elements: IntArray): Boolean = collection.retainAll(elements)
    inline fun retainAll(elements: ArrayListVInt<T>): Boolean = collection.retainAll(elements.collection)
    context(a: ValueIntAdapter<T>) inline fun set(index: Int, element: T): T = a.fromInt(collection.set(index, a.toInt(element)))
    // Sorts by the raw underlying Int representation, NOT by T's logical Comparable order (those
    // may differ) - use the generic Comparable-constrained `sort()`/`sortDescending()` extensions
    // from MutableIndexedCollectionVInt.kt if T's natural order must be respected.
    inline fun sortByBits() = collection.sort()
    inline fun sortByBitsDescending() = collection.sortDescending()
}

val <T> ArrayListVInt<T>.lastIndex inline get() = collection.lastIndex
val <T> ArrayListVInt<T>.indices inline get() = collection.indices

private val EmptyListVInt: ListVInt<Nothing> = ArrayListVInt(0)
@Suppress("UNCHECKED_CAST")
fun <T>emptyVIntList(): ListVInt<T> = EmptyListVInt as ListVInt<T>
@Suppress("UNCHECKED_CAST")
fun <T>vIntListOf(): ListVInt<T> = EmptyListVInt as ListVInt<T>
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T): ListVInt<T> = mutableVIntListOf(element1)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T, element2: T): ListVInt<T> = mutableVIntListOf(element1, element2)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T, element2: T, element3: T): ListVInt<T> = mutableVIntListOf(element1, element2, element3)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(vararg elements: T): ListVInt<T> = ArrayListVInt<T>(elements.size).apply { plusAssign(elements) }
inline fun <T>mutableVIntListOf(): ArrayListVInt<T> = ArrayListVInt(8)
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T): ArrayListVInt<T> 
        = ArrayListVInt<T>(1).also { it += element1 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T, element2: T): ArrayListVInt<T>
        = ArrayListVInt<T>(2).also { it += element1; it += element2 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T, element2: T, element3: T): ArrayListVInt<T>
        = ArrayListVInt<T>(3).also { it += element1; it += element2; it += element3 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(vararg elements: T): ArrayListVInt<T> = ArrayListVInt<T>(elements.size).apply { plusAssign(elements) }
