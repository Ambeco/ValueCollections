@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.MutableLongSet

interface SetVLong<T>: CollectionVLong<T>

interface ModifiableSetVLong<T>: SetVLong<T>, ModifiableCollectionVLong<T>

interface MutableSetVLong<T>: ModifiableSetVLong<T>, MutableCollectionVLong<T>

class ArraySetVLong<T>(val collection: MutableLongSet, override val NULL_VALUE: LongBits=Long.MIN_VALUE): MutableSetVLong<T>, MutableCollectionVLong<T> {
    constructor(initialCapacity: Int, NO_VALUE: LongBits=Long.MIN_VALUE) : this(MutableLongSet(initialCapacity), NO_VALUE)

    override val size: Int = collection.size
    override inline fun anyBits(predicate: (bits: LongBits) -> Boolean): LongBits = collection.first { predicate(it) }
    override inline fun containsBits(bits: LongBits): Boolean = collection.contains(bits)
    context(a: ValueLongAdapter<T>) override inline fun asIterable(): MutableIterable<T> = throw NotImplementedError()

    override inline fun ensureCapacity(newCapacity: Int) = throw NotImplementedError()
    override inline fun trim(minCapacity: Int) = throw NotImplementedError()
    override inline fun addBits(bits: LongBits): Boolean = collection.add(bits)
    override inline fun removeBits(bits: LongBits): Boolean = collection.remove(bits)
    context(a: ValueLongAdapter<T>) override inline fun removeAll(predicate: (T) -> Boolean): Boolean {
        val removeList = MutableLongSet(size)
        collection.forEach { if (predicate(a.fromLong(it))) removeList.add(it) }
        collection.removeAll(removeList)
        return true
    }
    
    override inline fun clear()  = collection.clear()
    override fun hashCode() = contentHashCode()
    @Suppress("UNCHECKED_CAST")
    override fun equals(other: Any?) = other is CollectionVLong<*> && contentEquals(other as CollectionVLong<T>)
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
