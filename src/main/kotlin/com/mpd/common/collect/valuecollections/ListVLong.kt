@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")

package com.mpd.common.collect.valuecollections

import androidx.collection.MutableLongList

// LongList -> VLongList
interface ListVLong<T>: IndexedCollectionVLong<T>

interface ModifiableListVLong<T>: ListVLong<T>, ModifiableIndexedCollectionVLong<T>
context(a: ValueLongAdapter<T>) inline operator fun <T> ModifiableListVLong<T>.set(index: Int, value: T) {setBits(index, a.toLong(value)) }

interface MutableListVLong<T>: ModifiableListVLong<T>, MutableIndexedCollectionVLong<T> {
    val capacity: Int
}

class ArrayListVLong<T>(val collection: MutableLongList = MutableLongList(), override val NULL_VALUE: LongBits=Long.MIN_VALUE): MutableListVLong<T> {
    constructor(capacity: Int,  NO_VALUE: LongBits=Long.MIN_VALUE) : this(MutableLongList(capacity), NO_VALUE)
    constructor(other: CollectionVLong<T>, NO_VALUE: LongBits=Long.MIN_VALUE) : this(MutableLongList(other.size), NO_VALUE) { other.forEachBits { collection.add(it) } }
    constructor(other: ListVLong<T>, NO_VALUE: LongBits) : this(MutableLongList(other.size), NO_VALUE) {other.copyInto(this,0,0,other.size)}

    override val size inline get() = collection.size
    override inline fun anyBits(predicate: (bits: LongBits) -> Boolean): LongBits = getBits(collection.indexOfFirst { predicate(it) })
    override inline fun containsBits(bits: LongBits): Boolean = collection.contains(bits)
    context(a: ValueLongAdapter<T>) override inline fun asIterable(): MutableIterable<T> = object : MutableIterable<T> {
        override inline fun iterator(): MutableIterator<T> = object : MutableIterator<T> {
            var idx = 0
            override inline fun hasNext(): Boolean = idx < size
            override inline fun next(): T = a.fromLong(collection[idx++])
            override inline fun remove() { collection.removeAt(--idx) }
        }
    }
    context(a: ValueLongAdapter<T>) override inline fun toString(): String = toStringV()

    override inline fun setBits(index: Int, bits: LongBits) { collection[index] = bits }

    override val capacity inline get() = collection.capacity
    override inline fun ensureCapacity(newCapacity: Int): Boolean { collection.ensureCapacity(newCapacity); return true;}
    override inline fun trim(minCapacity: Int) = collection.trim(minCapacity)
    override inline fun addBits(bits: LongBits): Boolean = collection.add(bits)
    override inline fun removeBits(bits: LongBits): Boolean = collection.remove(bits)

    context(a: ValueLongAdapter<T>) override inline fun removeAll(predicate: (T) -> Boolean): Boolean {
        val removeList = MutableLongList(size)
        collection.forEach { if (predicate(a.fromLong(it))) removeList.add(it) }
        collection.removeAll(removeList)
        return true
    }

    override inline fun clear() = collection.clear()
    override inline fun bitsAtIndex(index: Int): LongBits = collection[index]
    override inline fun indexOfBits(bits: LongBits): Int = collection.indexOf(bits)

    override inline fun addBits(index: Int, bits: LongBits) = collection.add(index, bits)
    override inline fun addAll(index: Int, elements: CollectionVLong<T>): Boolean = throw NotImplementedError()
    context(a: ValueLongAdapter<T>) override inline fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError()
    context(a: ValueLongAdapter<T>)  override inline fun removeAt(index: Int): T = a.fromLong(collection.removeAt(index))
    override inline fun removeRange(start: Int, end: Int) = collection.removeRange(start, end)
    override inline fun removeAllIndexedBits(crossinline predicate: (index: Int, bits: LongBits) -> Boolean): Boolean = throw NotImplementedError()

    override inline fun hashCode() = contentHashCode()
    @Suppress("UNCHECKED_CAST")
    override inline fun equals(other: Any?) = other is IndexedCollectionVLong<*> && contentEquals(other as IndexedCollectionVLong<T>)
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!

    // Fast-path members mirroring androidx.collection.LongList/MutableLongList, delegating directly
    // to `collection` instead of going through the generic bits-based CollectionVLong/
    // IndexedCollectionVLong extension functions. Kotlin prefers a member over an extension
    // function of the same name/signature, so these transparently speed up calls made directly on
    // an ArrayListVLong without requiring any call-site changes.
    inline fun none(): Boolean = collection.none()
    inline fun any(): Boolean = collection.any()
    context(a: ValueLongAdapter<T>) inline fun any(crossinline predicate: (T) -> Boolean): Boolean = collection.any { predicate(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun reversedAny(crossinline predicate: (T) -> Boolean): Boolean = collection.reversedAny { predicate(a.fromLong(it)) }
    inline fun containsAll(elements: ArrayListVLong<T>): Boolean = collection.containsAll(elements.collection)
    inline fun count(): Int = collection.count()
    context(a: ValueLongAdapter<T>) inline fun count(crossinline predicate: (T) -> Boolean): Int = collection.count { predicate(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun first(): T = a.fromLong(collection.first())
    context(a: ValueLongAdapter<T>) inline fun first(crossinline predicate: (T) -> Boolean): T = a.fromLong(collection.first { predicate(a.fromLong(it)) })
    context(a: ValueLongAdapter<T>) inline fun <R> fold(initial: R, crossinline operation: (acc: R, T) -> R): R = collection.fold(initial) { acc, e -> operation(acc, a.fromLong(e)) }
    context(a: ValueLongAdapter<T>) inline fun <R> foldIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): R = collection.foldIndexed(initial) { i, acc, e -> operation(i, acc, a.fromLong(e)) }
    context(a: ValueLongAdapter<T>) inline fun <R> foldRight(initial: R, crossinline operation: (T, acc: R) -> R): R = collection.foldRight(initial) { e, acc -> operation(a.fromLong(e), acc) }
    context(a: ValueLongAdapter<T>) inline fun <R> foldRightIndexed(initial: R, crossinline operation: (index: Int, T, acc: R) -> R): R = collection.foldRightIndexed(initial) { i, e, acc -> operation(i, a.fromLong(e), acc) }
    context(a: ValueLongAdapter<T>) inline fun forEach(crossinline block: (T) -> Unit) = collection.forEach { block(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun forEachIndexed(crossinline block: (index: Int, T) -> Unit) = collection.forEachIndexed { i, e -> block(i, a.fromLong(e)) }
    context(a: ValueLongAdapter<T>) inline fun forEachReversed(crossinline block: (T) -> Unit) = collection.forEachReversed { block(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun forEachReversedIndexed(crossinline block: (index: Int, T) -> Unit) = collection.forEachReversedIndexed { i, e -> block(i, a.fromLong(e)) }
    context(a: ValueLongAdapter<T>) inline fun elementAt(index: Int): T = a.fromLong(collection.elementAt(index))
    context(a: ValueLongAdapter<T>) inline fun elementAtOrElse(index: Int, crossinline defaultValue: (index: Int) -> T): T = a.fromLong(collection.elementAtOrElse(index) { a.toLong(defaultValue(it)) })
    context(a: ValueLongAdapter<T>) inline fun indexOfFirst(crossinline predicate: (T) -> Boolean): Int = collection.indexOfFirst { predicate(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun indexOfLast(crossinline predicate: (T) -> Boolean): Int = collection.indexOfLast { predicate(a.fromLong(it)) }
    inline fun isEmpty(): Boolean = collection.isEmpty()
    inline fun isNotEmpty(): Boolean = collection.isNotEmpty()
    context(a: ValueLongAdapter<T>) inline fun last(): T = a.fromLong(collection.last())
    context(a: ValueLongAdapter<T>) inline fun last(crossinline predicate: (T) -> Boolean): T = a.fromLong(collection.last { predicate(a.fromLong(it)) })
    context(a: ValueLongAdapter<T>) inline fun lastIndexOf(element: T): Int = collection.lastIndexOf(a.toLong(element))
    // Note: androidx.collection.MutableLongList.binarySearch() takes an Int element (an upstream
    // quirk/bug, not a typo here) - it can't correctly search for arbitrary Long values, so it's
    // deliberately not wrapped here to avoid silently truncating T's underlying Long.
    context(a: ValueLongAdapter<T>) inline fun joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: (T) -> CharSequence = { it.toString() }): String =
        collection.joinToString(separator, prefix, postfix, limit, truncated) { transform(a.fromLong(it)) }

    // MutableLongList members: bulk operations get a raw-LongArray overload and a fast-path overload
    // for when the other collection also happens to be backed by MutableLongList.
    inline fun addAllBits(index: Int, elements: LongArray): Boolean = collection.addAll(index, elements)
    inline fun addAllBits(elements: LongArray): Boolean = collection.addAll(elements)
    inline fun addAll(index: Int, elements: ArrayListVLong<T>): Boolean = collection.addAll(index, elements.collection)
    inline fun addAll(elements: ArrayListVLong<T>): Boolean = collection.addAll(elements.collection)
    inline operator fun plusAssign(elements: LongArray): Unit = collection.plusAssign(elements)
    inline operator fun plusAssign(elements: ArrayListVLong<T>): Unit = collection.plusAssign(elements.collection)
    inline fun removeAllBits(elements: LongArray): Boolean = collection.removeAll(elements)
    inline fun removeAll(elements: ArrayListVLong<T>): Boolean = collection.removeAll(elements.collection)
    inline operator fun minusAssign(elements: LongArray): Unit { collection.minusAssign(elements) }
    inline operator fun minusAssign(elements: ArrayListVLong<T>): Unit { collection.minusAssign(elements.collection) }
    inline fun retainAllBits(elements: LongArray): Boolean = collection.retainAll(elements)
    inline fun retainAll(elements: ArrayListVLong<T>): Boolean = collection.retainAll(elements.collection)
    context(a: ValueLongAdapter<T>) inline fun set(index: Int, element: T): T = a.fromLong(collection.set(index, a.toLong(element)))
    // Sorts by the raw underlying Long representation, NOT by T's logical Comparable order (those
    // may differ) - use the generic Comparable-constrained `sort()`/`sortDescending()` extensions
    // from MutableIndexedCollectionVLong.kt if T's natural order must be respected.
    inline fun sortByBits() = collection.sort()
    inline fun sortByBitsDescending() = collection.sortDescending()
}

private val EmptyVLongList: ListVLong<Nothing> = ArrayListVLong(0)
@Suppress("UNCHECKED_CAST")
fun <T>emptyVLongList(): ListVLong<T> = EmptyVLongList as ListVLong<T>
@Suppress("UNCHECKED_CAST")
fun <T>vLongListOf(): ListVLong<T> = EmptyVLongList as ListVLong<T>

context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T): ListVLong<T> = mutableVLongListOf(element1)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T, element2: T): ListVLong<T> = mutableVLongListOf(element1, element2)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T, element2: T, element3: T): ListVLong<T> = mutableVLongListOf(element1, element2, element3)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(vararg elements: T): ListVLong<T> = ArrayListVLong<T>(elements.size).apply { plusAssign(elements as Array<T>) }
inline fun <T>mutableVLongListOf(): ArrayListVLong<T> = ArrayListVLong()
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T): ArrayListVLong<T>
        = ArrayListVLong<T>(1).also { it += element1 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T, element2: T): ArrayListVLong<T>
        = ArrayListVLong<T>(2).also { it += element1; it += element2 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T, element2: T, element3: T): ArrayListVLong<T>
        = ArrayListVLong<T>(2).also { it += element1; it += element2; it += element3 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(vararg elements: T): ArrayListVLong<T> = ArrayListVLong<T>(elements.size).apply { plusAssign(elements as Array<T>) }
