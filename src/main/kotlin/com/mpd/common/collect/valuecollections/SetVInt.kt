@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package com.mpd.common.collect.valuecollections

import androidx.collection.IntSet
import androidx.collection.MutableIntSet

interface SetVInt<T>: CollectionVInt<T>

interface ModifiableSetVInt<T>: SetVInt<T>, ModifiableCollectionVInt<T>

interface MutableSetVInt<T>: ModifiableSetVInt<T>, MutableCollectionVInt<T>

class ArraySetVInt<T>(val collection: MutableIntSet, override val NULL_VALUE: IntBits=Int.MIN_VALUE): MutableSetVInt<T>, MutableCollectionVInt<T> {
    constructor(initialCapacity: Int, NO_VALUE: IntBits=Int.MIN_VALUE) : this(MutableIntSet(initialCapacity), NO_VALUE)

    override val size: Int get() = collection.size
    override inline fun anyBits(predicate: (bits: IntBits) -> Boolean): IntBits {
        var found = NULL_VALUE
        collection.forEach { if (found == NULL_VALUE && predicate(it)) found = it }
        return found
    }
    override inline fun containsBits(bits: IntBits): Boolean = collection.contains(bits)
    context(a: ValueIntAdapter<T>) override inline fun asIterable(): MutableIterable<T> {
        val list = ArrayList<T>(size)
        collection.forEach { list.add(a.fromInt(it)) }
        return list
    }

    override inline fun ensureCapacity(newCapacity: Int): Boolean = false
    override inline fun trim(minCapacity: Int) {}
    override inline fun addBits(bits: IntBits): Boolean = collection.add(bits)
    override inline fun removeBits(bits: IntBits): Boolean = collection.remove(bits)
    override inline fun removeAllBits(predicate: (IntBits) -> Boolean): Boolean {
        val removeList = MutableIntSet(size)
        collection.forEach { if (predicate(it)) removeList.add(it) }
        collection.removeAll(removeList)
        return true
    }

    override inline fun clear()  = collection.clear()

    // Thin wrappers for every public method of MutableIntSet.
    // Named with a "Bits" suffix (rather than matching MutableIntSet's names exactly) to avoid
    // shadowing the generic, adapter-based extension functions of the same name (any, forEach,
    // contains, add, remove, etc.) declared for CollectionVInt<T>/MutableCollectionVInt<T> - a
    // member function always wins overload resolution over an extension of the same name, which
    // would silently break every caller of those typed extensions.
    inline fun capacity(): Int = collection.capacity
    inline fun anyBits(): Boolean = collection.any()
    inline fun noneBits(): Boolean = collection.none()
    inline fun isEmpty(): Boolean = collection.isEmpty()
    inline fun isNotEmptyBits(): Boolean = collection.isNotEmpty()
    inline fun firstBits(): IntBits = collection.first()
    inline fun firstBits(predicate: (element: IntBits) -> Boolean): IntBits = collection.first(predicate)
    inline fun forEachBits(block: (element: IntBits) -> Unit) = collection.forEach(block)
    inline fun allBits(predicate: (element: IntBits) -> Boolean): Boolean = collection.all(predicate)
    inline fun count(): Int = collection.count()
    inline fun countBits(predicate: (element: IntBits) -> Boolean): Int = collection.count(predicate)
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
        crossinline transform: (IntBits) -> CharSequence,
    ): String = collection.joinToString(separator, prefix, postfix, limit, truncated, transform)
    inline fun plusAssignBits(element: IntBits) = collection.plusAssign(element)
    inline fun addAllBits(elements: IntArray): Boolean = collection.addAll(elements)
    inline fun plusAssignBits(elements: IntArray) = collection.plusAssign(elements)
    inline fun addAllBits(elements: IntSet): Boolean = collection.addAll(elements)
    inline fun plusAssignBits(elements: IntSet) = collection.plusAssign(elements)
    inline fun minusAssignBits(element: IntBits) = collection.minusAssign(element)
    inline fun removeAllBits(elements: IntArray): Boolean = collection.removeAll(elements)
    inline fun minusAssignBits(elements: IntArray) = collection.minusAssign(elements)
    inline fun removeAllBits(elements: IntSet): Boolean = collection.removeAll(elements)
    inline fun minusAssignBits(elements: IntSet) = collection.minusAssign(elements)

    override inline fun hashCode() = contentHashCode()
    @Suppress("UNCHECKED_CAST")
    override inline fun equals(other: Any?) = other is CollectionVInt<*> && contentEquals(other as CollectionVInt<T>)
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = collection.toString()
}

private val EmptySetVInt: SetVInt<Nothing> = ArraySetVInt(0)
@Suppress("UNCHECKED_CAST")
fun <T>emptySetVInt(): SetVInt<T> = EmptySetVInt as SetVInt<T>
@Suppress("UNCHECKED_CAST")
fun <T>vIntSetOf(): SetVInt<T> = EmptySetVInt as SetVInt<T>
context(a: ValueIntAdapter<T>) inline fun <T>vIntSetOf(element1: T): SetVInt<T> = mutableSetVIntOf(element1)
context(a: ValueIntAdapter<T>) inline fun <T>vIntSetOf(element1: T, element2: T): SetVInt<T> = mutableSetVIntOf(element1, element2)
context(a: ValueIntAdapter<T>) inline fun <T>vIntSetOf(element1: T, element2: T, element3: T): SetVInt<T> = mutableSetVIntOf(element1, element2, element3)
context(a: ValueIntAdapter<T>) inline fun <T>vIntSetOf(vararg elements: T): SetVInt<T> = ArraySetVInt<T>(elements.size).apply { plusAssign(elements) }
inline fun <T>mutableSetVIntOf(): ArraySetVInt<T> = ArraySetVInt(8)
context(a: ValueIntAdapter<T>) inline fun <T>mutableSetVIntOf(element1: T): ArraySetVInt<T>
        = ArraySetVInt<T>(1).also { it += element1 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableSetVIntOf(element1: T, element2: T): ArraySetVInt<T>
        = ArraySetVInt<T>(2).also { it += element1; it += element2 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableSetVIntOf(element1: T, element2: T, element3: T): ArraySetVInt<T>
        = ArraySetVInt<T>(3).also { it += element1; it += element2; it += element3 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableSetVIntOf(vararg elements: T): ArraySetVInt<T> = ArraySetVInt<T>(elements.size).apply { plusAssign(elements) }
