@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.IntList
import androidx.collection.LongList
import androidx.collection.MutableIntList
import androidx.collection.MutableLongList
import kotlin.also

// inline wrappers around androidx.collection.IntList and LongList

// IntList -> VIntList
interface VIntList<T>: VIntCollection<T> {
    val collection:IntList
}
val <T> VIntList<T>.lastIndex inline get() = collection.lastIndex
val <T> VIntList<T>.indices inline get() = collection.indices

interface ModifiableVIntList<T>: VIntList<T>, ModifiableVIntCollection<T> {
}

interface MutableVIntList<T>: ModifiableVIntList<T>, MutableVIntCollection<T> {
    val capacity: Int
}

class FlatVIntList<T>(override val collection: MutableIntList = MutableIntList()): MutableVIntList<T> {
    constructor(capacity: Int) : this(MutableIntList(capacity))
    constructor(other: VIntCollection<T>) : this(MutableIntList(other.size)) {other.copyInto(this,0,0,size)}
    constructor(other: VIntList<T>) : this(MutableIntList(other.size)) {other.copyInto(this,0,0,size)}
    override val size inline get() = collection.size
    override val capacity inline get() = collection.capacity
    override inline fun bitsAtIndex(index: Int): Int = collection[index]
    override inline fun setBits(index: Int, bits: Int) { collection[index] = bits }
    
    override inline fun indexOfBits(bits: Int): Int = collection.indexOf(bits)
    override inline fun indexOfFirstIndexedBits(predicate: (index: Int, bits: Int) -> Boolean): Int = indexOfFirstIndexedBitsDefault(predicate)
    override inline fun indexOfLastIndexedBits(predicate: (index: Int, bits: Int) -> Boolean): Int = indexOfLastIndexedBitsDefault(predicate)
    override inline fun <C : MutableVIntCollection<T>> copyInto(destination: C, destinationOffset: Int, startIndex: Int, endIndex: Int ): C = copyIntoDefault(destination, destinationOffset, startIndex, endIndex)
    
    override inline fun ensureCapacity(capacity: Int) = collection.ensureCapacity(capacity)
    override fun trim(minCapacity: Int) = collection.trim(minCapacity)

    override inline fun addBits(element: Int): Boolean = collection.add(element)
    override inline fun addAll(elements: VIntCollection<T>): Boolean { collection.ensureCapacity(size + elements.size); return elements.allBits { collection.add(it) } }
    context(a: ValueIntAdapter<T>) override inline fun addAll(elements: Collection<T>): Boolean = throw NotImplementedError()
    override fun addBits(index: Int, bits: Int) = collection.add(index, bits)

    override fun addAll(index: Int, elements: VIntCollection<T>): Boolean = throw NotImplementedError()
    context(a: ValueIntAdapter<T>) override fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError()
    override fun removeAt(index: Int): Boolean { collection.removeAt(index); return true}

    override fun removeAllIndexedBits(predicate: (index: Int, bits: Int) -> Boolean): Boolean = throw NotImplementedError()
    override inline fun clear() = collection.clear()

    context(a: ValueIntAdapter<T>) override fun <T> asIterable() = throw NotImplementedError()
    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = collection == other
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}

private val EmptyVIntList: VIntList<Nothing> = FlatVIntList(0)
@Suppress("UNCHECKED_CAST")
fun <T>emptyVIntList(): VIntList<T> = EmptyVIntList as VIntList<T>
@Suppress("UNCHECKED_CAST")
fun <T>vIntListOf(): VIntList<T> = EmptyVIntList as VIntList<T>
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T): VIntList<T> = mutableVIntListOf(element1)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T, element2: T): VIntList<T> = mutableVIntListOf(element1, element2)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T, element2: T, element3: T): VIntList<T> = mutableVIntListOf(element1, element2, element3)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(vararg elements: T): VIntList<T> = FlatVIntList<T>(elements.size).apply { plusAssign(elements) }
inline fun <T>mutableVIntListOf(): FlatVIntList<T> = FlatVIntList()
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T): FlatVIntList<T> 
        = FlatVIntList<T>(1).also { it += element1 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T, element2: T): FlatVIntList<T>
        = FlatVIntList<T>(2).also { it += element1; it += element2 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T, element2: T, element3: T): FlatVIntList<T>
        = FlatVIntList<T>(2).also { it += element1; it += element2; it += element3 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(vararg elements: T): FlatVIntList<T> = FlatVIntList<T>(elements.size).apply { plusAssign(elements) }




// LongList -> VLongList
interface VLongList<T>: VLongCollection<T> {
    val collection:LongList
}
val <T> VLongList<T>.lastIndex inline get() = collection.lastIndex
val <T> VLongList<T>.indices inline get() = collection.indices

interface ModifiableVLongList<T>: VLongList<T>, ModifiableVLongCollection<T> {
}

interface MutableVLongList<T>: ModifiableVLongList<T>, MutableVLongCollection<T> {
    val capacity: Int
}

class FlatVLongList<T>(override val collection: MutableLongList = MutableLongList()): MutableVLongList<T> {
    constructor(capacity: Int) : this(MutableLongList(capacity))
    constructor(other: VLongCollection<T>) : this(MutableLongList(other.size)) {other.copyInto(this,0,0,size)}
    constructor(other: VLongList<T>) : this(MutableLongList(other.size)) {other.copyInto(this,0,0,size)}
    override val size inline get() = collection.size
    override val capacity inline get() = collection.capacity
    override inline fun bitsAtIndex(index: Int): Long = collection[index]
    override inline fun setBits(index: Int, bits: Long) { collection[index] = bits }

    override inline fun indexOfBits(bits: Long): Int = collection.indexOf(bits)
    override inline fun indexOfFirstIndexedBits(predicate: (index: Int, bits: Long) -> Boolean): Int = throw NotImplementedError()
    override inline fun indexOfLastIndexedBits(predicate: (index: Int, bits: Long) -> Boolean): Int = throw NotImplementedError()
    override inline fun <C : MutableVLongCollection<T>> copyInto(destination: C, destinationOffset: Int, startIndex: Int, endIndex: Int ): C = throw NotImplementedError()

    override inline fun ensureCapacity(capacity: Int) = collection.ensureCapacity(capacity)
    override fun trim(minCapacity: Int) = collection.trim(minCapacity)

    override inline fun addBits(element: Long): Boolean = collection.add(element)
    override inline fun addAll(elements: VLongCollection<T>): Boolean = throw NotImplementedError()
    context(a: ValueLongAdapter<T>) override inline fun addAll(elements: Collection<T>): Boolean = throw NotImplementedError()
    override fun addBits(index: Int, bits: Long) = collection.add(index, bits)

    override fun addAll(index: Int, elements: VLongCollection<T>): Boolean = throw NotImplementedError()
    context(a: ValueLongAdapter<T>) override fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError()
    override fun removeAt(index: Int): Boolean { collection.removeAt(index); return true}

    override fun removeAllIndexedBits(predicate: (index: Int, bits: Long) -> Boolean): Boolean = throw NotImplementedError()
    override inline fun clear() = collection.clear()

    context(a: ValueLongAdapter<T>) override fun <T> asIterable() = throw NotImplementedError()
    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = collection == other
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Longs. Use toVString() to print K.toString", ReplaceWith("toVString()"))
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