@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.MutableIntList
import kotlin.also

// inline wrappers around androidx.collection.IntList and LongList

// IntList -> VIntList
interface ListVInt<T>: IndexedCollectionVInt<T>

interface ModifiableListVInt<T>: ListVInt<T>, ModifiableIndexedCollectionVInt<T>
context(a: ValueIntAdapter<T>) inline operator fun <T> ModifiableListVInt<T>.set(index: Int, value: T) {setBits(index, a.toInt(value)) }

interface MutableListVInt<T>: ModifiableListVInt<T>, MutableIndexedCollectionVInt<T> {
    val capacity: Int
}

class ArrayListVInt<T>(val collection: MutableIntList = MutableIntList(), override val NULL_VALUE: IntBits=Int.MIN_VALUE): MutableListVInt<T> {
    constructor(capacity: Int, NO_VALUE:IntBits=Int.MIN_VALUE) : this(MutableIntList(capacity), NO_VALUE)
    constructor(other: CollectionVInt<T>, NO_VALUE:IntBits=Int.MIN_VALUE) : this(MutableIntList(other.size), NO_VALUE) { other.forEachBits { collection.add(it) } }
    constructor(other: ListVInt<T>, NO_VALUE:IntBits=Int.MIN_VALUE) : this(MutableIntList(other.size), NO_VALUE) {other.copyInto(this,0,0,size)}

    override val size inline get() = collection.size
    override inline fun anyBits(predicate: (bits: IntBits) -> Boolean): IntBits = getBits(collection.indexOfFirst { predicate(it) })
    override inline fun containsBits(bits: IntBits): Boolean = collection.contains(bits)
    context(a: ValueIntAdapter<T>) override fun asIterable(): MutableIterable<T> = object : MutableIterable<T> {
        override fun iterator(): MutableIterator<T> = object : MutableIterator<T> {
            var idx = 0
            override fun hasNext(): Boolean = idx < size
            override fun next(): T = a.fromInt(collection[idx++])
            override fun remove() { collection.removeAt(--idx) }
        }
    }
    context(a: ValueIntAdapter<T>) override inline fun toString(): String = toStringV()
    
    override val capacity inline get() = collection.capacity
    override inline fun ensureCapacity(newCapacity: Int): Boolean { collection.ensureCapacity(newCapacity); return true;}
    override inline fun trim(minCapacity: Int) = collection.trim(minCapacity)
    override inline fun addBits(bits: IntBits): Boolean = collection.add(bits)
    override inline fun removeBits(bits: IntBits): Boolean = collection.remove(bits)

    context(a: ValueIntAdapter<T>) override inline fun removeAll(predicate: (T) -> Boolean): Boolean  {
        val removeList = MutableIntList(size)
        collection.forEach { if (predicate(a.fromInt(it))) removeList.add(it) }
        collection.removeAll(removeList)
        return true
    }

    override inline fun clear() = collection.clear()

    override inline fun bitsAtIndex(index: Int): IntBits = if (index in 0..<size) collection[index] else NULL_VALUE
    override inline fun indexOfBits(bits: IntBits): Int = collection.indexOf(bits)
    
    override inline fun setBits(index: Int, bits: IntBits) { collection[index] = bits }

    override inline fun addBits(index: Int, bits: IntBits) = collection.add(index, bits)
    override inline fun addAll(index: Int, elements: CollectionVInt<T>): Boolean = throw NotImplementedError()
    context(a: ValueIntAdapter<T>) override inline fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError()
    context(a: ValueIntAdapter<T>) override inline fun removeAt(index: Int): T = a.fromInt(collection.removeAt(index))
    override inline fun removeRange(start: Int, end: Int) = collection.removeRange(start, end)

    override inline fun removeAllIndexedBits(crossinline predicate: (index: Int, bits: IntBits) -> Boolean): Boolean = throw NotImplementedError()
    
    override fun hashCode() = contentHashCode()
    @Suppress("UNCHECKED_CAST")
    override fun equals(other: Any?) = other is IndexedCollectionVInt<*> && contentEquals(other as IndexedCollectionVInt<T>)
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}

val <T> ArrayListVInt<T>.lastIndex inline get() = collection.lastIndex
val <T> ArrayListVInt<T>.indices inline get() = collection.indices

private val EmptyListVInt: ListVInt<Nothing> = ArrayListVInt(0)
@Suppress("UNCHECKED_CAST")
fun <T>emptyVIntList(): ListVInt<T> = EmptyListVInt as ListVInt<T>
@Suppress("UNCHECKED_CAST")
fun <T>vIntListOf(): ListVInt<T> = EmptyListVInt as ListVInt<T>
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T): ListVInt<T> = mutableVIntListOf(element1)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T, element2: T): ListVInt<T> = mutableVIntListOf(element1, element2)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T, element2: T, element3: T): ListVInt<T> = mutableVIntListOf(element1, element2, element3)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(vararg elements: T): ListVInt<T> = ArrayListVInt<T>(elements.size).apply { plusAssign(elements) }
inline fun <T>mutableVIntListOf(): ArrayListVInt<T> = ArrayListVInt(8)
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T): ArrayListVInt<T> 
        = ArrayListVInt<T>(1).also { it += element1 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T, element2: T): ArrayListVInt<T>
        = ArrayListVInt<T>(2).also { it += element1; it += element2 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T, element2: T, element3: T): ArrayListVInt<T>
        = ArrayListVInt<T>(3).also { it += element1; it += element2; it += element3 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(vararg elements: T): ArrayListVInt<T> = ArrayListVInt<T>(elements.size).apply { plusAssign(elements) }
