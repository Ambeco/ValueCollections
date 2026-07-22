@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package mpd.com.common.collect.valuecollections

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
    context(a: ValueIntAdapter<T>) override inline fun asIterable(): MutableIterable<T> = throw NotImplementedError()

    override inline fun ensureCapacity(newCapacity: Int): Boolean = false
    override inline fun trim(minCapacity: Int) {}
    override inline fun addBits(bits: IntBits): Boolean = collection.add(bits)
    override inline fun removeBits(bits: IntBits): Boolean = collection.remove(bits)
    context(a: ValueIntAdapter<T>) override fun removeAll(predicate: (T) -> Boolean): Boolean {
        val removeList = MutableIntSet(size)
        collection.forEach { if (predicate(a.fromInt(it))) removeList.add(it) }
        collection.removeAll(removeList)
        return true
    }

    override inline fun clear()  = collection.clear()

    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = other is ArraySetVInt<*> && collection == other.collection
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
