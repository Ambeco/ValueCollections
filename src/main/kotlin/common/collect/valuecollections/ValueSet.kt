@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.IntSet
import androidx.collection.LongSet
import androidx.collection.MutableIntIntMap
import androidx.collection.MutableIntSet
import androidx.collection.MutableLongSet
import mpd.com.common.collect.valuecollections.MutableVIntIntMap

interface VIntSet<T>: VIntCollection<T> { 
    val collection: IntSet
}
interface ModifiableVIntSet<T>: VIntSet<T>, MutableVIntCollection<T> {
}
interface MutableVIntSet<T>: VIntSet<T>, MutableVIntCollection<T> {
}
class FlatVIntSet<T>(override val collection: MutableIntSet, ): MutableVIntSet<T>, MutableVIntCollection<T> {
    constructor(initialCapacity: Int) : this(MutableIntSet(initialCapacity))

    override val size: Int = collection.size
    override inline fun bitsAtIndex(index: Int): Int = throw NotImplementedError()
    override inline fun indexOfBits(bits: Int): Int = throw NotImplementedError()
    override inline fun indexOfFirstIndexedBits(predicate: (index: Int, bits: Int) -> Boolean): Int = throw NotImplementedError()
    override inline fun indexOfLastIndexedBits(predicate: (index: Int, bits: Int) -> Boolean): Int = throw NotImplementedError()
    override inline fun <C : MutableVIntCollection<T>> copyInto(destination: C, destinationOffset: Int, startIndex: Int, endIndex: Int): C = throw NotImplementedError()

    override inline fun ensureCapacity(capacity: Int) = throw NotImplementedError()
    override inline fun trim(minCapacity: Int) = throw NotImplementedError()
    override inline fun addBits(element: Int): Boolean = collection.add(element)
    override inline fun addAll(elements: VIntCollection<T>) = throw NotImplementedError()
    context(a: ValueIntAdapter<T>) override inline fun addAll(elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun addBits(index: Int, bits: Int) = throw NotImplementedError()
    override inline fun addAll(index: Int, elements: VIntCollection<T>): Boolean = throw NotImplementedError()
    context(a: ValueIntAdapter<T>) override inline fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun removeAt(index: Int): Boolean = throw NotImplementedError()
    override inline fun removeAllIndexedBits(predicate: (index: Int, bits: Int) -> Boolean): Boolean = throw NotImplementedError()
    override inline fun clear()  = collection.clear()
    override inline fun setBits(index: Int, bits: Int) = throw UnsupportedOperationException()
    
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString(): String = collection.toString()
}


interface VLongSet<T>: VLongCollection<T> { 
    val collection: LongSet
}
interface ModifiableVLongSet<T>: VLongSet<T>, MutableVLongCollection<T> {
}
interface MutableVLongSet<T>: ModifiableVLongSet<T>, MutableVLongCollection<T> {
}
class FlatVLongSet<T>(override val collection: MutableLongSet): MutableVLongSet<T>, MutableVLongCollection<T> {
    constructor(initialCapacity: Int) : this(MutableLongSet(initialCapacity))

    override val size: Int = collection.size
    override inline fun bitsAtIndex(index: Int): Long = throw NotImplementedError()
    override inline fun indexOfBits(bits: Long): Int = throw NotImplementedError()
    override inline fun indexOfFirstIndexedBits(predicate: (index: Int, bits: Long) -> Boolean): Int = throw NotImplementedError()
    override inline fun indexOfLastIndexedBits(predicate: (index: Int, bits: Long) -> Boolean): Int = throw NotImplementedError()
    override inline fun <C : MutableVLongCollection<T>> copyInto(destination: C, destinationOffset: Int, startIndex: Int, endIndex: Int): C = throw NotImplementedError()
    
    override inline fun ensureCapacity(capacity: Int) = throw NotImplementedError()
    override inline fun trim(minCapacity: Int) = throw NotImplementedError()
    override inline fun addBits(element: Long): Boolean = collection.add(element)
    override inline fun addAll(elements: VLongCollection<T>) = throw NotImplementedError()
    context(a: ValueLongAdapter<T>)
    override inline fun addAll(elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun addBits(index: Int, bits: Long) = throw NotImplementedError()
    override inline fun addAll(index: Int, elements: VLongCollection<T>): Boolean = throw NotImplementedError()
    context(a: ValueLongAdapter<T>) override inline fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun removeAt(index: Int): Boolean = throw NotImplementedError()
    override inline fun removeAllIndexedBits(predicate: (index: Int, bits: Long) -> Boolean): Boolean = throw NotImplementedError()
    override inline fun clear()  = collection.clear()
    override inline fun setBits(index: Int, bits: Long) = throw UnsupportedOperationException()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString(): String = collection.toString()
}
