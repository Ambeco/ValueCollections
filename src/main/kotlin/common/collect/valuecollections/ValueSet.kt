@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.MutableIntSet
import androidx.collection.MutableLongList
import androidx.collection.MutableLongSet

interface VIntSet<T>: VIntCollection<T>

interface ModifiableVIntSet<T>: VIntSet<T>, ModifiableVIntCollection<T>

interface MutableVIntSet<T>: ModifiableVIntSet<T>, MutableVIntCollection<T>

class ArrayVIntSet<T>(val collection: MutableIntSet, override val NULL_VALUE: IntBits=Int.MIN_VALUE): MutableVIntSet<T>, MutableVIntCollection<T> {
    constructor(initialCapacity: Int, NO_VALUE: IntBits=Int.MIN_VALUE) : this(MutableIntSet(initialCapacity), NO_VALUE)

    override val size: Int = collection.size
    override inline fun anyBits(predicate: (bits: IntBits) -> Boolean): IntBits = collection.first { predicate(it) }
    override inline fun containsBits(bits: IntBits): Boolean = collection.contains(bits)
    context(a: ValueIntAdapter<T>) override inline fun asIterable(): MutableIterable<T> = throw NotImplementedError()

    override inline fun ensureCapacity(newCapacity: Int) = throw NotImplementedError()
    override inline fun trim(minCapacity: Int) = throw NotImplementedError()
    override inline fun addBits(bits: IntBits): Boolean = collection.add(bits)
    override inline fun removeBits(bits: IntBits): Boolean = collection.remove(bits)
    context(a: ValueIntAdapter<T>) override fun removeAll(predicate: (T) -> Boolean): Boolean {
        val removeList = MutableIntSet(size)
        collection.forEach { if (predicate(a.fromInt(it))) removeList.add(it) }
        collection.removeAll(removeList)
        return true
    }

    override inline fun clear()  = collection.clear()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString(): String = collection.toString()
}


interface VLongSet<T>: VLongCollection<T>

interface ModifiableVLongSet<T>: VLongSet<T>, ModifiableVLongCollection<T>

interface MutableVLongSet<T>: ModifiableVLongSet<T>, MutableVLongCollection<T>

class ArrayVLongSet<T>(val collection: MutableLongSet, override val NULL_VALUE: LongBits=Long.MIN_VALUE): MutableVLongSet<T>, MutableVLongCollection<T> {
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
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString(): String = collection.toString()
}
