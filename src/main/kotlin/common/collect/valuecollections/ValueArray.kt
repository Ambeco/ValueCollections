@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")
@file:OptIn(ExperimentalStdlibApi::class)

package mpd.com.common.collect.valuecollections


// inline wrappers around kotlin.IntArray and kotlin.LongArray

// VIntArray<T> -> VIntArray
class VIntArray<T>(val collection:IntArray, override val NULL_VALUE: IntBits=Int.MIN_VALUE): ModifiableVIntSequence<T> {
    override val size inline get() = collection.size
    
    constructor(size: Int, NO_VALUE: IntBits=Int.MIN_VALUE) : this(IntArray(size), NO_VALUE)
    constructor(size: Int, NO_VALUE: IntBits, init: (Int) -> IntBits) : this(IntArray(size, init), NO_VALUE)

    override inline fun anyBits(predicate: (IntBits) -> Boolean): IntBits = collection.first { predicate(it) }
    override inline fun containsBits(bits: IntBits): Boolean = collection.contains(bits)
    override inline fun <C : MutableVIntSequence<T>> copyInto(destination: C, destinationOffset: Int, startIndex: Int, endIndex: Int): C = copyIntoDefault(destination, destinationOffset, startIndex, endIndex)
    context(a: ValueIntAdapter<T>) override inline fun <T> asIterable() = VIteratableFrom(collection.iterator())

    context(a: ValueIntAdapter<T>) inline operator fun get(index: Int): T = a.fromInt(collection.get(index))
    context(a: ValueIntAdapter<T>) inline operator fun set(index: Int, value: T) = collection.set(index, a.toInt(value))
    
    override inline fun bitsAtIndex(index: Int): Int = collection[index]
    override inline fun setBits(index: Int, bits: IntBits) = collection.set(index, bits)
    override inline fun indexOfBits(bits: IntBits) = collection.indexOf(bits)
    override inline fun indexOfFirstIndexedBits(crossinline predicate: (index: Int, bits: IntBits) -> Boolean): Int = indexOfFirstIndexedBitsDefault(predicate)
    override inline fun indexOfLastIndexedBits(crossinline predicate: (index: Int, bits: IntBits) -> Boolean): Int = indexOfLastIndexedBitsDefault(predicate)
    
    override inline operator fun equals(other: Any?): Boolean = collection == other
    override inline fun hashCode(): Int = collection.hashCode()
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString(): String = collection.toString()
}

// VLongArray<T> -> VLongArray
class VLongArray<T>(val collection:LongArray, override val NULL_VALUE: LongBits=Long.MIN_VALUE): ModifiableVLongSequence<T> {
    override val size inline get() = collection.size
    
    constructor(size: Int, NO_VALUE: LongBits=Long.MIN_VALUE) : this(LongArray(size), NO_VALUE)
    constructor(size: Int, NO_VALUE: LongBits, init: (Int) -> LongBits) : this(LongArray(size, init), NO_VALUE)

    override inline fun anyBits(predicate: (LongBits) -> Boolean): LongBits = collection.first { predicate(it) }
    override inline fun containsBits(bits: LongBits): Boolean = collection.contains(bits)
    override inline fun <C : MutableVLongSequence<T>> copyInto(destination: C, destinationOffset: Int, startIndex: Int, endIndex: Int): C = destination.also{for(i in startIndex..endIndex) destination.setBits(i+destinationOffset, bitsAtIndex(i))}
    context(a: ValueLongAdapter<T>) override inline fun <T> asIterable() = VIteratableFrom(collection.iterator())

    context(a: ValueLongAdapter<T>) inline operator fun get(index: Int): T = a.fromLong(collection.get(index))
    context(a: ValueLongAdapter<T>) inline operator fun set(index: Int, value: T) = collection.set(index, a.toLong(value))
    override inline fun bitsAtIndex(index: Int): LongBits = collection[index]
    override inline fun setBits(index: Int, bits: LongBits) = collection.set(index, bits)
    override inline fun indexOfBits(bits: LongBits) = collection.indexOf(bits)
    override inline fun indexOfFirstIndexedBits(crossinline predicate: (index: Int, bits: LongBits) -> Boolean): Int { for(i in 0 ..< size) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
    override inline fun indexOfLastIndexedBits(crossinline predicate: (index: Int, bits: LongBits) -> Boolean): Int { for(i in size-1..0) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
        
    override inline operator fun equals(other: Any?): Boolean = collection == other
    override inline fun hashCode(): Int = collection.hashCode()
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Longs. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString(): String = collection.toString()
}