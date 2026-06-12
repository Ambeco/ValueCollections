@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")
@file:OptIn(ExperimentalStdlibApi::class)

package mpd.com.common.collect.valuecollections

import mpd.com.common.collect.valuecollections.VIntArray
import java.util.PrimitiveIterator
import java.util.function.Consumer

// inline wrappers around kotlin.IntArray and kotlin.LongArray

// VIntArray<T> -> VIntArray
class VIntArray<T>(val collection:IntArray): ModifiableVIntCollection<T> {
    constructor(size: Int) : this(IntArray(size))
    constructor(size: Int, init: (Int) -> Int) : this(IntArray(size, init))
    
    context(a: ValueIntAdapter<T>) inline operator fun get(index: Int): T = a.fromInt(collection.get(index))
    context(a: ValueIntAdapter<T>) inline operator fun set(index: Int, value: T) = collection.set(index, a.toInt(value))
    
    override val size inline get() = collection.size
    override inline fun bitsAtIndex(index: Int): Int = collection[index]
    override inline fun setBits(index: Int, bits: Int) = collection.set(index, bits)
    override inline fun indexOfBits(bits: Int) = collection.indexOf(bits)
    override inline fun indexOfFirstIndexedBits(predicate: (index: Int, bits: Int) -> Boolean): Int = indexOfFirstIndexedBitsDefault(predicate)
    override inline fun indexOfLastIndexedBits(predicate: (index: Int, bits: Int) -> Boolean): Int = indexOfLastIndexedBitsDefault(predicate)
    override inline fun <C : MutableVIntCollection<T>> copyInto(destination: C, destinationOffset: Int, startIndex: Int, endIndex: Int): C = copyIntoDefault(destination, destinationOffset, startIndex, endIndex)

    context(a: ValueIntAdapter<T>) override fun <T> asIterable() = VIteratableFrom(collection.iterator())
    override inline operator fun equals(other: Any?): Boolean = collection == other
    override inline fun hashCode(): Int = collection.hashCode()
    
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String = collection.toString()
}

// VLongArray<T> -> VLongArray
class VLongArray<T>(val collection:LongArray): ModifiableVLongCollection<T> {
    constructor(size: Int) : this(LongArray(size))
    constructor(size: Int, init: (Int) -> Long) : this(LongArray(size, init))

    context(a: ValueLongAdapter<T>) inline operator fun get(index: Int): T = a.fromLong(collection.get(index))
    context(a: ValueLongAdapter<T>) inline operator fun set(index: Int, value: T) = collection.set(index, a.toLong(value))

    override val size inline get() = collection.size
    override inline fun bitsAtIndex(index: Int): Long = collection[index]
    override inline fun setBits(index: Int, bits: Long) = collection.set(index, bits)
    override inline fun indexOfBits(bits: Long) = collection.indexOf(bits)
    override inline fun indexOfFirstIndexedBits(predicate: (index: Int, bits: Long) -> Boolean): Int { for(i in 0 ..< size) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
    override inline fun indexOfLastIndexedBits(predicate: (index: Int, bits: Long) -> Boolean): Int { for(i in size-1..0) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
    override inline fun <C : MutableVLongCollection<T>> copyInto(destination: C, destinationOffset: Int, startIndex: Int, endIndex: Int): C = destination.also{for(i in startIndex..endIndex) destination.setBits(i+destinationOffset, bitsAtIndex(i))}
    context(a: ValueLongAdapter<T>) override fun <T> asIterable() = VIteratableFrom(collection.iterator())
    override inline operator fun equals(other: Any?): Boolean = collection == other
    override inline fun hashCode(): Int = collection.hashCode()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String = collection.toString()
}