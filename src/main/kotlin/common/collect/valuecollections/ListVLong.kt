@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.MutableLongList

// LongList -> VLongList
interface ListVLong<T>: IndexedCollectionVLong<T>

interface ModifiableListVLong<T>: ListVLong<T>, ModifiableIndexedCollectionVLong<T>
context(a: ValueLongAdapter<T>) inline operator fun <T> ModifiableListVLong<T>.set(index: Int, value: T) {setBits(index, a.toLong(value)) }

interface MutableListVLong<T>: ModifiableListVLong<T>, MutableIndexedCollectionVLong<T> {
    val capacity: Int
}

class ArrayListVLong<T>(val collection: MutableLongList = MutableLongList(), override val NULL_VALUE: LongBits=Long.MIN_VALUE): MutableListVLong<T> {
    constructor(capacity: Int,  NO_VALUE: LongBits=Long.MIN_VALUE) : this(MutableLongList(capacity), NO_VALUE)
    constructor(other: CollectionVLong<T>, NO_VALUE: LongBits=Long.MIN_VALUE) : this(MutableLongList(other.size), NO_VALUE) { other.forEachBits { collection.add(it) } }
    constructor(other: ListVLong<T>, NO_VALUE: LongBits) : this(MutableLongList(other.size), NO_VALUE) {other.copyInto(this,0,0,size)}

    override val size inline get() = collection.size
    override inline fun anyBits(predicate: (bits: LongBits) -> Boolean): LongBits = getBits(collection.indexOfFirst { predicate(it) })
    override inline fun containsBits(bits: LongBits): Boolean = collection.contains(bits)
    context(a: ValueLongAdapter<T>) override fun asIterable(): MutableIterable<T> = object : MutableIterable<T> {
        override fun iterator(): MutableIterator<T> = object : MutableIterator<T> {
            var idx = 0
            override fun hasNext(): Boolean = idx < size
            override fun next(): T = a.fromLong(collection[idx++])
            override fun remove() { collection.removeAt(--idx) }
        }
    }
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

    override fun hashCode() = contentHashCode()
    @Suppress("UNCHECKED_CAST")
    override fun equals(other: Any?) = other is IndexedCollectionVLong<*> && contentEquals(other as IndexedCollectionVLong<T>)
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}

private val EmptyVLongList: ListVLong<Nothing> = ArrayListVLong(0)
@Suppress("UNCHECKED_CAST")
fun <T>emptyVLongList(): ListVLong<T> = EmptyVLongList as ListVLong<T>
@Suppress("UNCHECKED_CAST")
fun <T>vLongListOf(): ListVLong<T> = EmptyVLongList as ListVLong<T>

context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T): ListVLong<T> = mutableVLongListOf(element1)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T, element2: T): ListVLong<T> = mutableVLongListOf(element1, element2)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T, element2: T, element3: T): ListVLong<T> = mutableVLongListOf(element1, element2, element3)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(vararg elements: T): ListVLong<T> = ArrayListVLong<T>(elements.size).apply { plusAssign(elements as Array<T>) }
inline fun <T>mutableVLongListOf(): ArrayListVLong<T> = ArrayListVLong()
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T): ArrayListVLong<T>
        = ArrayListVLong<T>(1).also { it += element1 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T, element2: T): ArrayListVLong<T>
        = ArrayListVLong<T>(2).also { it += element1; it += element2 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T, element2: T, element3: T): ArrayListVLong<T>
        = ArrayListVLong<T>(2).also { it += element1; it += element2; it += element3 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(vararg elements: T): ArrayListVLong<T> = ArrayListVLong<T>(elements.size).apply { plusAssign(elements as Array<T>) }
