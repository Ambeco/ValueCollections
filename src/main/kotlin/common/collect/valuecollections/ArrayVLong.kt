@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")
@file:OptIn(ExperimentalStdlibApi::class)

package mpd.com.common.collect.valuecollections


class ArrayVLong<T>(val collection:LongArray, override val NULL_VALUE: LongBits=Long.MIN_VALUE): ModifiableVLongIndexedCollection<T> {
    override val size inline get() = collection.size
    
    constructor(size: Int, NO_VALUE: LongBits=Long.MIN_VALUE) : this(LongArray(size), NO_VALUE)
    constructor(size: Int, NO_VALUE: LongBits, init: (Int) -> LongBits) : this(LongArray(size, init), NO_VALUE)
    constructor(src: CollectionVLong<T>) : this(LongArray(src.size), src.NULL_VALUE) {
        src.forEachIndexedBits{i,e-> collection[i] = e }
    }

    override inline fun anyBits(predicate: (LongBits) -> Boolean): LongBits = collection.first { predicate(it) }
    override inline fun containsBits(bits: LongBits): Boolean = collection.contains(bits)
    
    context(a: ValueLongAdapter<T>) override inline fun asModifiableIterable(): MutableIterable<T> = MutableVLongIteratorKotlin(collection.iterator(), a)
    context(a: ValueLongAdapter<T>) override inline fun asIterable(): Iterable<T> = VLongIteratorKotlin(collection.iterator(), a)

    context(a: ValueLongAdapter<T>) inline operator fun get(index: Int): T = a.fromLong(collection.get(index))
    context(a: ValueLongAdapter<T>) inline operator fun set(index: Int, value: T) = collection.set(index, a.toLong(value))
    
    override inline fun bitsAtIndex(index: Int): LongBits = collection[index]
    override inline fun setBits(index: Int, bits: LongBits) = collection.set(index, bits)
    
    override inline fun indexOfBits(bits: LongBits) = collection.indexOf(bits)
    
    override inline operator fun equals(other: Any?): Boolean = collection == other
    override inline fun hashCode(): Int = collection.hashCode()
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Longs. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString(): String = collection.toString()
}