@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.MutableLongList

// LongList -> VLongList
interface VLongList<T>: VLongIndexedCollection<T>

interface ModifiableVLongList<T>: VLongList<T>, ModifiableVLongIndexedCollection<T>
context(a: ValueLongAdapter<T>) inline operator fun <T> ModifiableVLongList<T>.set(index: Int, value: T) {setBits(index, a.toLong(value)) }

interface MutableVLongList<T>: ModifiableVLongList<T>, MutableVLongIndexedCollection<T> {
    val capacity: Int
}

class ArrayVLongList<T>(val collection: MutableLongList = MutableLongList(), override val NULL_VALUE: LongBits=Long.MIN_VALUE): MutableVLongList<T> {
    constructor(capacity: Int,  NO_VALUE: LongBits=Long.MIN_VALUE) : this(MutableLongList(capacity), NO_VALUE)
    constructor(other: CollectionVLong<T>, NO_VALUE: LongBits=Long.MIN_VALUE) : this(MutableLongList(other.size), NO_VALUE) {}//TODO: other.copyInto(this,0,0,size)}
    constructor(other: VLongList<T>, NO_VALUE: LongBits) : this(MutableLongList(other.size), NO_VALUE) {other.copyInto(this,0,0,size)}

    override val size inline get() = collection.size
    override inline fun anyBits(predicate: (bits: LongBits) -> Boolean): LongBits = getBits(collection.indexOfFirst { predicate(it) })
    override inline fun containsBits(bits: LongBits): Boolean = collection.contains(bits)
    context(a: ValueLongAdapter<T>) override inline fun asIterable(): MutableIterable<T> = throw NotImplementedError()
    context(a: ValueLongAdapter<T>) override inline fun toString(): String = toStringV()

    override inline fun setBits(index: Int, bits: LongBits) { collection[index] = bits }

    override val capacity inline get() = collection.capacity
    override inline fun ensureCapacity(newCapacity: Int): Boolean { collection.ensureCapacity(newCapacity); return true;}
    override inline fun trim(minCapacity: Int) = collection.trim(minCapacity)
    override inline fun addBits(bits: LongBits): Boolean = collection.add(bits)
    override inline fun removeBits(bits: LongBits): Boolean = collection.remove(bits)

    context(a: ValueLongAdapter<T>) override inline fun removeAll(predicate: (T) -> Boolean): Boolean {
        val removeList = MutableLongList(size)
        collection.forEach { if (predicate(a.fromLong(it))) removeList.add(it) }
        collection.removeAll(removeList)
        return true
    }

    override inline fun clear() = collection.clear()
    override inline fun bitsAtIndex(index: Int): LongBits = collection[index]
    override inline fun indexOfBits(bits: LongBits): Int = collection.indexOf(bits)

    override inline fun addBits(index: Int, bits: LongBits) = collection.add(index, bits)
    override inline fun addAll(index: Int, elements: CollectionVLong<T>): Boolean = throw NotImplementedError()
    context(a: ValueLongAdapter<T>) override inline fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError()
    context(a: ValueLongAdapter<T>)  override inline fun removeAt(index: Int): T = a.fromLong(collection.removeAt(index))
    override inline fun removeRange(start: Int, end: Int) = collection.removeRange(start, end)
    override inline fun removeAllIndexedBits(crossinline predicate: (index: Int, bits: LongBits) -> Boolean): Boolean = throw NotImplementedError()

    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = other is ArrayVLongList<*> && collection == other.collection
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}

private val EmptyVLongList: VLongList<Nothing> = ArrayVLongList(0)
@Suppress("UNCHECKED_CAST")
fun <T>emptyVLongList(): VLongList<T> = EmptyVLongList as VLongList<T>
@Suppress("UNCHECKED_CAST")
fun <T>vLongListOf(): VLongList<T> = EmptyVLongList as VLongList<T>

context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T): VLongList<T> = mutableVLongListOf(element1)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T, element2: T): VLongList<T> = mutableVLongListOf(element1, element2)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T, element2: T, element3: T): VLongList<T> = mutableVLongListOf(element1, element2, element3)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(vararg elements: T): VLongList<T> = ArrayVLongList<T>(elements.size).apply { plusAssign(elements as Array<T>) }
inline fun <T>mutableVLongListOf(): ArrayVLongList<T> = ArrayVLongList()
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T): ArrayVLongList<T>
        = ArrayVLongList<T>(1).also { it += element1 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T, element2: T): ArrayVLongList<T>
        = ArrayVLongList<T>(2).also { it += element1; it += element2 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T, element2: T, element3: T): ArrayVLongList<T>
        = ArrayVLongList<T>(2).also { it += element1; it += element2; it += element3 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(vararg elements: T): ArrayVLongList<T> = ArrayVLongList<T>(elements.size).apply { plusAssign(elements as Array<T>) }
