@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package com.mpd.common.collect.valuecollections

import androidx.collection.LongSet
import androidx.collection.MutableLongSet

interface SetVLong<T>: CollectionVLong<T>

interface ModifiableSetVLong<T>: SetVLong<T>, ModifiableCollectionVLong<T>

interface MutableSetVLong<T>: ModifiableSetVLong<T>, MutableCollectionVLong<T>

class ArraySetVLong<T>(val collection: MutableLongSet, override val NULL_VALUE: LongBits=Long.MIN_VALUE): MutableSetVLong<T>, MutableCollectionVLong<T> {
    constructor(initialCapacity: Int, NO_VALUE: LongBits=Long.MIN_VALUE) : this(MutableLongSet(initialCapacity), NO_VALUE)

    override val size: Int get() = collection.size
    override inline fun anyBits(predicate: (bits: LongBits) -> Boolean): LongBits = try { collection.first { predicate(it) } } catch (e: NoSuchElementException) { NULL_VALUE }
    override inline fun containsBits(bits: LongBits): Boolean = collection.contains(bits)
    context(a: ValueLongAdapter<T>) override inline fun asIterable(): MutableIterable<T> {
        val list = ArrayList<T>(size)
        collection.forEach { list.add(a.fromLong(it)) }
        return list
    }

    override inline fun ensureCapacity(newCapacity: Int): Boolean = false
    override inline fun trim(minCapacity: Int) { }
    override inline fun addBits(bits: LongBits): Boolean = collection.add(bits)
    override inline fun removeBits(bits: LongBits): Boolean = collection.remove(bits)
    override inline fun removeAllBits(predicate: (LongBits) -> Boolean): Boolean {
        val removeList = MutableLongSet(size)
        collection.forEach { if (predicate(it)) removeList.add(it) }
        collection.removeAll(removeList)
        return true
    }

    override inline fun clear()  = collection.clear()

    // Thin wrappers for every public method of MutableLongSet.
    // Named with a "Bits" suffix (rather than matching MutableLongSet's names exactly) to avoid
    // shadowing the generic, adapter-based extension functions of the same name (any, forEach,
    // contains, add, remove, etc.) declared for CollectionVLong<T>/MutableCollectionVLong<T> - a
    // member function always wins overload resolution over an extension of the same name, which
    // would silently break every caller of those typed extensions.
    inline fun capacity(): Int = collection.capacity
    inline fun anyBits(): Boolean = collection.any()
    inline fun noneBits(): Boolean = collection.none()
    inline fun isEmpty(): Boolean = collection.isEmpty()
    inline fun isNotEmptyBits(): Boolean = collection.isNotEmpty()
    inline fun first(): LongBits = collection.first()
    inline fun first(predicate: (element: LongBits) -> Boolean): LongBits = collection.first(predicate)
    inline fun forEachBits(block: (element: LongBits) -> Unit) = collection.forEach(block)
    inline fun allBits(predicate: (element: LongBits) -> Boolean): Boolean = collection.all(predicate)
    inline fun countBits(): Int = collection.count()
    inline fun countBits(predicate: (element: LongBits) -> Boolean): Int = collection.count(predicate)
    inline fun joinToStringBits(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
    ): String = collection.joinToString(separator, prefix, postfix, limit, truncated)
    inline fun joinToStringBits(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        crossinline transform: (LongBits) -> CharSequence,
    ): String = collection.joinToString(separator, prefix, postfix, limit, truncated, transform)
    inline fun plusAssignBits(element: LongBits) = collection.plusAssign(element)
    inline fun addAllBits(elements: LongArray): Boolean = collection.addAll(elements)
    inline fun plusAssignBits(elements: LongArray) = collection.plusAssign(elements)
    inline fun addAllBits(elements: LongSet): Boolean = collection.addAll(elements)
    inline fun plusAssignBits(elements: LongSet) = collection.plusAssign(elements)
    inline fun minusAssignBits(element: LongBits) = collection.minusAssign(element)
    inline fun removeAllBits(elements: LongArray): Boolean = collection.removeAll(elements)
    inline fun minusAssignBits(elements: LongArray) = collection.minusAssign(elements)
    inline fun removeAllBits(elements: LongSet): Boolean = collection.removeAll(elements)
    inline fun minusAssignBits(elements: LongSet) = collection.minusAssign(elements)

    override inline fun hashCode() = contentHashCode()
    @Suppress("UNCHECKED_CAST")
    override inline fun equals(other: Any?) = other is CollectionVLong<*> && contentEquals(other as CollectionVLong<T>)
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = collection.toString()
}


private val EmptySetVLong: SetVLong<Nothing> = ArraySetVLong(0)
@Suppress("UNCHECKED_CAST")
fun <T>emptySetVLong(): SetVLong<T> = EmptySetVLong as SetVLong<T>
@Suppress("UNCHECKED_CAST")
fun <T>vLongSetOf(): SetVLong<T> = EmptySetVLong as SetVLong<T>
context(a: ValueLongAdapter<T>) inline fun <T>vLongSetOf(element1: T): SetVLong<T> = mutableSetVLongOf(element1)
context(a: ValueLongAdapter<T>) inline fun <T>vLongSetOf(element1: T, element2: T): SetVLong<T> = mutableSetVLongOf(element1, element2)
context(a: ValueLongAdapter<T>) inline fun <T>vLongSetOf(element1: T, element2: T, element3: T): SetVLong<T> = mutableSetVLongOf(element1, element2, element3)
context(a: ValueLongAdapter<T>) inline fun <T>vLongSetOf(vararg elements: T): SetVLong<T> = ArraySetVLong<T>(elements.size).apply { plusAssign(elements) }
inline fun <T>mutableSetVLongOf(): ArraySetVLong<T> = ArraySetVLong(8)
context(a: ValueLongAdapter<T>) inline fun <T>mutableSetVLongOf(element1: T): ArraySetVLong<T>
        = ArraySetVLong<T>(1).also { it += element1 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableSetVLongOf(element1: T, element2: T): ArraySetVLong<T>
        = ArraySetVLong<T>(2).also { it += element1; it += element2 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableSetVLongOf(element1: T, element2: T, element3: T): ArraySetVLong<T>
        = ArraySetVLong<T>(3).also { it += element1; it += element2; it += element3 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableSetVLongOf(vararg elements: T): ArraySetVLong<T> = ArraySetVLong<T>(elements.size).apply { plusAssign(elements) }
