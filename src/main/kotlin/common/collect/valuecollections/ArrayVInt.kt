@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")
@file:OptIn(ExperimentalStdlibApi::class)

package mpd.com.common.collect.valuecollections


class ArrayVInt<T>(val collection:IntArray, override val NULL_VALUE: IntBits=Int.MIN_VALUE): ModifiableVIntIndexedCollection<T> {
    override val size inline get() = collection.size
    
    constructor(size: Int, NO_VALUE: IntBits=Int.MIN_VALUE) : this(IntArray(size), NO_VALUE)
    constructor(size: Int, NO_VALUE: IntBits, init: (Int) -> IntBits) : this(IntArray(size, init), NO_VALUE)
    constructor(src: CollectionVInt<T>) : this(IntArray(src.size), src.NULL_VALUE) {
        src.forEachIndexedBits{i,e-> collection[i] = e }
    }

    override inline fun anyBits(predicate: (IntBits) -> Boolean): IntBits = collection.first { predicate(it) }
    override inline fun containsBits(bits: IntBits): Boolean = collection.contains(bits)

    context(a: ValueIntAdapter<T>) override inline fun asModifiableIterable(): MutableIterable<T> = MutableVIntIteratorKotlin(collection.iterator(), a)
    context(a: ValueIntAdapter<T>) override inline fun asIterable(): Iterable<T> = VIntIteratorKotlin(collection.iterator(),a)

    context(a: ValueIntAdapter<T>) inline operator fun get(index: Int): T = a.fromInt(collection.get(index))
    context(a: ValueIntAdapter<T>) inline operator fun set(index: Int, value: T) = collection.set(index, a.toInt(value))
    
    override inline fun bitsAtIndex(index: Int): Int = collection[index]
    override inline fun setBits(index: Int, bits: IntBits) = collection.set(index, bits)

    override inline fun indexOfBits(bits: IntBits) = collection.indexOf(bits)
    
    override inline operator fun equals(other: Any?): Boolean = collection == other
    override inline fun hashCode(): Int = collection.hashCode()
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = collection.toString()

}