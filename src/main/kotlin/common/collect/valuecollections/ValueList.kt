@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.LongList
import androidx.collection.MutableIntList
import androidx.collection.MutableLongList
import kotlin.also

// inline wrappers around androidx.collection.IntList and LongList

// IntList -> VIntList
interface VIntList<T>: VIntSequence<T>
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntList<T>.get(index: Int): T = if (index in 0..<size) elementAtIndex(index) else throw IndexOutOfBoundsException("$index not in 0..$size")

interface ModifiableVIntList<T>: VIntList<T>, ModifiableVIntSequence<T>
context(a: ValueIntAdapter<T>) inline operator fun <T> ModifiableVIntList<T>.set(index: Int, value: T) {setBits(index, a.toInt(value)) }

interface MutableVIntList<T>: ModifiableVIntList<T>, MutableVIntSequence<T> {
    val capacity: Int
}

class FlatVIntList<T>(val collection: MutableIntList = MutableIntList(), override val NULL_VALUE: IntBits=Int.MIN_VALUE): MutableVIntList<T> {
    constructor(capacity: Int, NO_VALUE:IntBits=Int.MIN_VALUE) : this(MutableIntList(capacity), NO_VALUE)
    constructor(other: VIntCollection<T>, NO_VALUE:IntBits=Int.MIN_VALUE) : this(MutableIntList(other.size), NO_VALUE) {}//TODO: other.copyInto(this,0,0,size)}
    constructor(other: VIntList<T>, NO_VALUE:IntBits=Int.MIN_VALUE) : this(MutableIntList(other.size), NO_VALUE) {other.copyInto(this,0,0,size)}

    override val size inline get() = collection.size
    override inline fun anyBits(predicate: (bits: IntBits) -> Boolean): IntBits = getBits(collection.indexOfFirst { predicate(it) })
    override inline fun containsBits(bits: IntBits): Boolean = collection.contains(bits)
    override inline fun <C : MutableVIntSequence<T>> copyInto(destination: C, destinationOffset: Int, startIndex: Int, endIndex: Int ): C = copyIntoDefault(destination, destinationOffset, startIndex, endIndex)
    context(a: ValueIntAdapter<T>) override inline fun <T> asIterable() = throw NotImplementedError()
    context(a: ValueIntAdapter<T>) override inline fun toString(): String = toVString()


    override val capacity inline get() = collection.capacity
    override inline fun ensureCapacity(newCapacity: Int): Boolean { collection.ensureCapacity(newCapacity); return true;}
    override inline fun trim(minCapacity: Int) = collection.trim(minCapacity)
    override inline fun addBits(bits: IntBits): Boolean = collection.add(bits)
    override inline fun addAll(elements: IntArray): Boolean = collection.addAll(elements)
    override inline fun removeBits(bits: IntBits): Boolean = collection.remove(bits)
    override inline fun removeAll(elements: IntArray): Boolean = collection.removeAll(elements)
    override inline fun clear() = collection.clear()
    
    override inline fun bitsAtIndex(index: Int): IntBits = if (index in 0..<size) collection[index] else NULL_VALUE
    override inline fun indexOfBits(bits: IntBits): Int = collection.indexOf(bits)
    override inline fun indexOfFirstIndexedBits(crossinline predicate: (index: Int, bits: IntBits) -> Boolean): Int = indexOfFirstIndexedBitsDefault(predicate)
    override inline fun indexOfLastIndexedBits(crossinline predicate: (index: Int, bits: IntBits) -> Boolean): Int = indexOfLastIndexedBitsDefault(predicate)
    
    override inline fun setBits(index: Int, bits: IntBits) { collection[index] = bits }

    override inline fun addBits(index: Int, bits: IntBits) = collection.add(index, bits)
    override inline fun addAll(index: Int, elements: VIntCollection<T>): Boolean = throw NotImplementedError()
    context(a: ValueIntAdapter<T>) override inline fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun removeAt(index: Int): Boolean { collection.removeAt(index); return true}
    override inline fun removeAllIndexedBits(crossinline predicate: (index: Int, bits: IntBits) -> Boolean): Boolean = throw NotImplementedError()
    
    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = collection == other
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}

val <T> FlatVIntList<T>.lastIndex inline get() = collection.lastIndex
val <T> FlatVIntList<T>.indices inline get() = collection.indices

private val EmptyVIntList: VIntList<Nothing> = FlatVIntList(0)
@Suppress("UNCHECKED_CAST")
fun <T>emptyVIntList(): VIntList<T> = EmptyVIntList as VIntList<T>
@Suppress("UNCHECKED_CAST")
fun <T>vIntListOf(): VIntList<T> = EmptyVIntList as VIntList<T>
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T): VIntList<T> = mutableVIntListOf(element1)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T, element2: T): VIntList<T> = mutableVIntListOf(element1, element2)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T, element2: T, element3: T): VIntList<T> = mutableVIntListOf(element1, element2, element3)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(vararg elements: T): VIntList<T> = FlatVIntList<T>(elements.size).apply { plusAssign(elements) }
inline fun <T>mutableVIntListOf(): FlatVIntList<T> = FlatVIntList(8)
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T): FlatVIntList<T> 
        = FlatVIntList<T>(1).also { it += element1 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T, element2: T): FlatVIntList<T>
        = FlatVIntList<T>(2).also { it += element1; it += element2 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T, element2: T, element3: T): FlatVIntList<T>
        = FlatVIntList<T>(3).also { it += element1; it += element2; it += element3 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(vararg elements: T): FlatVIntList<T> = FlatVIntList<T>(elements.size).apply { plusAssign(elements) }




// LongList -> VLongList
interface VLongList<T>: VLongSequence<T>
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongList<T>.get(index: Int): T = if (index in 0..<size) elementAtIndex(index) else throw IndexOutOfBoundsException("$index not in 0..$size")

interface ModifiableVLongList<T>: VLongList<T>, ModifiableVLongSequence<T>
context(a: ValueLongAdapter<T>) inline operator fun <T> ModifiableVLongList<T>.set(index: Int, value: T) {setBits(index, a.toLong(value)) }

interface MutableVLongList<T>: ModifiableVLongList<T>, MutableVLongSequence<T> {
    val capacity: Int
}

class FlatVLongList<T>(val collection: MutableLongList = MutableLongList(), override val NULL_VALUE: LongBits=Long.MIN_VALUE): MutableVLongList<T> {
    constructor(capacity: Int,  NO_VALUE: LongBits=Long.MIN_VALUE) : this(MutableLongList(capacity), NO_VALUE)
    constructor(other: VLongCollection<T>, NO_VALUE: LongBits=Long.MIN_VALUE) : this(MutableLongList(other.size), NO_VALUE) {}//TODO: other.copyInto(this,0,0,size)}
    constructor(other: VLongList<T>, NO_VALUE: LongBits) : this(MutableLongList(other.size), NO_VALUE) {other.copyInto(this,0,0,size)}

    override val size inline get() = collection.size
    override inline fun anyBits(predicate: (bits: LongBits) -> Boolean): LongBits = getBits(collection.indexOfFirst { predicate(it) })
    override inline fun containsBits(bits: LongBits): Boolean = collection.contains(bits)
    override inline fun <C : MutableVLongSequence<T>> copyInto(destination: C, destinationOffset: Int, startIndex: Int, endIndex: Int ): C = copyIntoDefault(destination, destinationOffset, startIndex, endIndex)
    context(a: ValueLongAdapter<T>) override inline fun <T> asIterable() = throw NotImplementedError()
    context(a: ValueLongAdapter<T>) override inline fun toString(): String = toVString()

    override inline fun setBits(index: Int, bits: LongBits) { collection[index] = bits }

    override val capacity inline get() = collection.capacity
    override inline fun ensureCapacity(newCapacity: Int): Boolean { collection.ensureCapacity(newCapacity); return true;}
    override inline fun trim(minCapacity: Int) = collection.trim(minCapacity)
    override inline fun addBits(bits: LongBits): Boolean = collection.add(bits)
    override inline fun addAll(elements: LongArray): Boolean = collection.addAll(elements)
    override inline fun removeBits(bits: LongBits): Boolean = collection.remove(bits)
    override inline fun removeAll(elements: LongArray): Boolean = collection.removeAll(elements)
    override inline fun clear() = collection.clear()

    override inline fun bitsAtIndex(index: Int): LongBits = collection[index]
    override inline fun indexOfBits(bits: LongBits): Int = collection.indexOf(bits)
    override inline fun indexOfFirstIndexedBits(crossinline predicate: (index: Int, bits: LongBits) -> Boolean): Int = indexOfFirstIndexedBitsDefault(predicate)
    override inline fun indexOfLastIndexedBits(crossinline predicate: (index: Int, bits: LongBits) -> Boolean): Int = indexOfLastIndexedBitsDefault(predicate)

    override inline fun addBits(index: Int, bits: LongBits) = collection.add(index, bits)
    override inline fun addAll(index: Int, elements: VLongCollection<T>): Boolean = throw NotImplementedError()
    context(a: ValueLongAdapter<T>) override inline fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun removeAt(index: Int): Boolean { collection.removeAt(index); return true}
    override inline fun removeAllIndexedBits(crossinline predicate: (index: Int, bits: LongBits) -> Boolean): Boolean = throw NotImplementedError()

    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = collection == other
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}

private val EmptyVLongList: VLongList<Nothing> = FlatVLongList(0)
@Suppress("UNCHECKED_CAST")
fun <T>emptyVLongList(): VLongList<T> = EmptyVLongList as VLongList<T>
@Suppress("UNCHECKED_CAST")
fun <T>vLongListOf(): VLongList<T> = EmptyVLongList as VLongList<T>
/*
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T): VLongList<T> = mutableVLongListOf(element1)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T, element2: T): VLongList<T> = mutableVLongListOf(element1, element2)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T, element2: T, element3: T): VLongList<T> = mutableVLongListOf(element1, element2, element3)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(vararg elements: T): VLongList<T> = FlatVLongList<T>(elements.size).apply { plusAssign(elements as Array<T>) }
inline fun <T>mutableVLongListOf(): FlatVLongList<T> = FlatVLongList()
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T): FlatVLongList<T>
        = FlatVLongList<T>(1).also { it += element1 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T, element2: T): FlatVLongList<T>
        = FlatVLongList<T>(2).also { it += element1; it += element2 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T, element2: T, element3: T): FlatVLongList<T>
        = FlatVLongList<T>(2).also { it += element1; it += element2; it += element3 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(vararg elements: T): FlatVLongList<T> = FlatVLongList<T>(elements.size).apply { plusAssign(elements as Array<T>) }
*/