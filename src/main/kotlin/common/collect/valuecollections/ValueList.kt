@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.IntList
import androidx.collection.LongList
import androidx.collection.MutableIntList
import androidx.collection.MutableLongList

// inline wrappers around androidx.collection.IntList and LongList

// IntList -> VIntList
interface VIntList<T>: VIntCollection<T> { val collection:IntList }
val <T> VIntList<T>.size inline get() = collection.size
val <T> VIntList<T>.lastIndex inline get() = collection.lastIndex
val <T> VIntList<T>.indices inline get() = collection.indices
inline fun <T> VIntList<T>.none() = collection.none()
inline fun <T> VIntList<T>.any() = collection.any()
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.any(predicate: (T) -> Boolean):Boolean = collection.any{predicate(a.fromInt(it))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.reversedAny(predicate: (T) -> Boolean):Boolean = collection.any{predicate(a.fromInt(it))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.contains(element: T): Boolean = collection.contains(a.toInt(element))
inline fun <T> VIntList<T>.containsAll(elements: IntList): Boolean = collection.containsAll(elements)
inline fun <T> VIntList<T>.containsAll(elements: VIntList<T>): Boolean = collection.containsAll(elements.collection)
inline fun <T> VIntList<T>.count() = collection.count()
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.count(predicate: (element: T) -> Boolean): Int = collection.count{predicate(a.fromInt(it))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.first(): T = a.fromInt(collection.first())
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.first(predicate: (element: T) -> Boolean): T = a.fromInt(collection.first {predicate(a.fromInt(it))})
context(a: ValueIntAdapter<T>) inline fun <T,R> VIntList<T>.fold(initial: R, operation: (acc: R, element: T) -> R): R = collection.fold(initial, {acc, e -> operation(acc, a.fromInt(e))})
context(a: ValueIntAdapter<T>) inline fun <T,R> VIntList<T>.foldIndexed(initial: R, operation: (index: Int, acc: R, element: T) -> R): R = collection.foldIndexed(initial, {i,acc,e -> operation(i,acc, a.fromInt(e))})
context(a: ValueIntAdapter<T>) inline fun <T,R> VIntList<T>.foldRight(initial: R, operation: (element: T, acc: R) -> R): R = collection.foldRight(initial, {e,acc -> operation(a.fromInt(e), acc)})
context(a: ValueIntAdapter<T>) inline fun <T,R> VIntList<T>.foldRightIndexed(initial: R, operation: (index: Int, element: T, acc: R) -> R): R = collection.foldRightIndexed(initial, {i,e,acc -> operation(i,a.fromInt(e), acc)})
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.forEach(block: (element: T) -> Unit) = collection.forEach { block(a.fromInt(it)) }
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.forEachIndexed(block: (index: Int, element: T) -> Unit) = collection.forEachIndexed { i, e -> block(i, a.fromInt(e)) }
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.forEachReversed(block: (element: T) -> Unit) = collection.forEachReversed { block(a.fromInt(it)) }
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.forEachReversedIndexed(block: (index: Int, element: T) -> Unit) = collection.forEachReversedIndexed { i, e -> block(i, a.fromInt(e)) }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntList<T>.get(@androidx.annotation.IntRange(from = 0) index: Int): T = a.fromInt(collection.get(index))
inline fun <T> VIntList<T>.bitsAtIndex(@androidx.annotation.IntRange(from = 0) index: Int): Int = collection.get(index)
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.elementAtIndex(@androidx.annotation.IntRange(from = 0) index: Int): T = a.fromInt(collection.elementAt(index))
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.elementAtOrElse(@androidx.annotation.IntRange(from = 0) index: Int, defaultValue: (index: Int) -> T): T = a.fromInt(collection.elementAtOrElse(index, {a.toInt(defaultValue(it))}))
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.indexOf(element: T): Int = collection.indexOf(a.toInt(element))
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.indexOfFirst(predicate: (element: T) -> Boolean): Int = collection.indexOfFirst { predicate(a.fromInt(it)) }
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.indexOfLast(predicate: (element: T) -> Boolean): Int = collection.indexOfLast { predicate(a.fromInt(it)) }
inline fun <T> VIntList<T>.isEmpty() = collection.isEmpty()
inline fun <T> VIntList<T>.isNotEmpty() = collection.isNotEmpty()
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.last(): T = a.fromInt(collection.last())
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.last(predicate: (element: T) -> Boolean): T = a.fromInt(collection.last {predicate(a.fromInt(it))})
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.lastIndexOf(element: T): Int = collection.lastIndexOf(a.toInt(element))
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "..."): String 
    = collection.joinToString(separator, prefix, postfix, limit, truncated, {a.fromInt(it).toString()})
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: (T) -> CharSequence): String
        = collection.joinToString(separator, prefix, postfix, limit, truncated, {transform(a.fromInt(it))})
context(a: ValueIntAdapter<T>) inline fun <T> VIntList<T>.toString() = joinToString(", ","{","}")

class MutableVIntList<T>(override val collection: MutableIntList = MutableIntList()): VIntList<T> {
    constructor(capacity: Int) : this(MutableIntList(capacity))
    constructor(other: VIntCollection<T>) : this(MutableIntList(other.size)) {other.copyInto(this,0,0,size)}
    constructor(other: VIntList<T>) : this(MutableIntList(other.collection))
    val capacity inline get() = collection.capacity
    context(a: ValueIntAdapter<T>) inline fun add(element: T): Boolean = collection.add(a.toInt(element))
    inline fun addBits(element: Int): Boolean = collection.add(element)
    context(a: ValueIntAdapter<T>) inline fun add(@androidx.annotation.IntRange(from = 0) index: Int, element: T) = collection.add(index, a.toInt(element))
    inline fun add(@androidx.annotation.IntRange(from = 0) index: Int, element: Int) = collection.add(index, element)
    context(a: ValueIntAdapter<T>) inline fun addAll(@androidx.annotation.IntRange(from = 0) index: Int, elements: IntArray) = collection.addAll(index, elements) // TODO: Add VIntArray
    context(a: ValueIntAdapter<T>) inline fun addAll(@androidx.annotation.IntRange(from = 0) index: Int, elements: IntList) = collection.addAll(index, elements)
    context(a: ValueIntAdapter<T>) inline fun addAll(elements: VIntList<T>) = collection.addAll(elements.collection)
    context(a: ValueIntAdapter<T>) inline fun addAll(elements: IntList) = collection.addAll(elements)
    context(a: ValueIntAdapter<T>) inline fun addAll(elements: IntArray) = collection.addAll(elements)
    context(a: ValueIntAdapter<T>) inline fun addAll(elements: Array<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { add(it) }}
    context(a: ValueIntAdapter<T>) inline fun addAll(elements: Collection<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { add(it) }}
    context(a: ValueIntAdapter<T>) inline fun addAll(elements: Iterable<T>) = elements.forEach { add(it) }
    context(a: ValueIntAdapter<T>) inline operator fun plusAssign(elements: IntList) = collection.plusAssign(elements)
    context(a: ValueIntAdapter<T>) inline operator fun plusAssign(elements: IntArray) = collection.plusAssign(elements)
    context(a: ValueIntAdapter<T>) inline fun plusAssign(elements: Array<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { plusAssign(it) }}
    context(a: ValueIntAdapter<T>) inline fun plusAssign(elements: Collection<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { plusAssign(it) }}
    context(a: ValueIntAdapter<T>) inline fun plusAssign(elements: Iterable<T>) = elements.forEach { plusAssign(it) }
    inline fun clear() = collection.clear()
    inline fun trim(minCapacity: Int = size) = collection.trim(minCapacity)
    inline fun ensureCapacity(capacity: Int = size) = collection.ensureCapacity(capacity)
    context(a: ValueIntAdapter<T>) inline operator fun plusAssign(element: T) = collection.plusAssign(a.toInt(element))
    context(a: ValueIntAdapter<T>) inline operator fun minusAssign(element: T) = collection.minusAssign(a.toInt(element))
    context(a: ValueIntAdapter<T>) inline fun remove(element: T) = collection.remove(a.toInt(element))
    context(a: ValueIntAdapter<T>) inline fun removeAll(elements: VIntList<T>) = collection.removeAll(elements.collection)
    context(a: ValueIntAdapter<T>) inline fun removeAll(elements: IntList) = collection.removeAll(elements)
    context(a: ValueIntAdapter<T>) inline fun removeAll(elements: IntArray) = collection.removeAll(elements)
    context(a: ValueIntAdapter<T>) inline fun removeAll(elements: Array<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { remove(it) }}
    context(a: ValueIntAdapter<T>) inline fun removeAll(elements: Collection<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { remove(it) }}
    context(a: ValueIntAdapter<T>) inline fun removeAll(elements: Iterable<T>) = elements.forEach { remove(it) }
    context(a: ValueIntAdapter<T>) inline fun minusAssign(elements: VIntList<T>) = collection.minusAssign(elements.collection)
    context(a: ValueIntAdapter<T>) inline fun minusAssign(elements: IntList) = collection.minusAssign(elements)
    context(a: ValueIntAdapter<T>) inline fun minusAssign(elements: IntArray) = collection.minusAssign(elements)
    context(a: ValueIntAdapter<T>) inline fun minusAssign(elements: Array<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { minusAssign(it) }}
    context(a: ValueIntAdapter<T>) inline fun minusAssign(elements: Collection<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { minusAssign(it) }}
    context(a: ValueIntAdapter<T>) inline fun minusAssign(elements: Iterable<T>) = elements.forEach { minusAssign(it) }
    context(a: ValueIntAdapter<T>) inline fun removeAt(@androidx.annotation.IntRange(from = 0) index: Int): T = a.fromInt(collection.removeAt(index))
    context(a: ValueIntAdapter<T>) inline fun removeRange(@androidx.annotation.IntRange(from = 0) start: Int, @androidx.annotation.IntRange(from = 0) end: Int) = collection.removeRange(start, end)
    context(a: ValueIntAdapter<T>) inline fun retainAll(elements: IntArray) = collection.retainAll(elements)
    context(a: ValueIntAdapter<T>) inline fun retainAll(elements: VIntList<T>) = collection.retainAll(elements.collection)
    context(a: ValueIntAdapter<T>) inline fun retainAll(elements: IntList) = collection.retainAll(elements)
    context(a: ValueIntAdapter<T>) inline operator fun set(@androidx.annotation.IntRange(from = 0) index: Int, element: T): T = a.fromInt(collection.set(index, a.toInt(element)))
    inline fun setBits(@androidx.annotation.IntRange(from = 0) index: Int, element: Int): Int = collection.set(index,element)
    context(a: ValueIntAdapter<T>) inline fun sort() = collection.sort()
    context(a: ValueIntAdapter<T>) inline fun sortDescending() = collection.sortDescending()
    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = collection == other
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toString(K.ValueIntAdapter)"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun <K,V> VIntVIntMap<K,V>.toString() = joinToString(", ","{","}")
}

private val EmptyVIntList: VIntList<Nothing> = MutableVIntList(0)
@Suppress("UNCHECKED_CAST")
fun <T>emptyVIntList(): VIntList<T> = EmptyVIntList as VIntList<T>
@Suppress("UNCHECKED_CAST")
fun <T>vIntListOf(): VIntList<T> = EmptyVIntList as VIntList<T>
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T): VIntList<T> = mutableVIntListOf(element1)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T, element2: T): VIntList<T> = mutableVIntListOf(element1, element2)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(element1: T, element2: T, element3: T): VIntList<T> = mutableVIntListOf(element1, element2, element3)
context(a: ValueIntAdapter<T>) inline fun <T>vIntListOf(vararg elements: T): VIntList<T> = MutableVIntList<T>(elements.size).apply { plusAssign(elements as Array<T>) }
inline fun <T>mutableVIntListOf(): MutableVIntList<T> = MutableVIntList()
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T): MutableVIntList<T> 
        = MutableVIntList<T>(1).also { it += element1 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T, element2: T): MutableVIntList<T>
        = MutableVIntList<T>(2).also { it += element1; it += element2 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(element1: T, element2: T, element3: T): MutableVIntList<T>
        = MutableVIntList<T>(2).also { it += element1; it += element2; it += element3 }
context(a: ValueIntAdapter<T>) inline fun <T>mutableVIntListOf(vararg elements: T): MutableVIntList<T> = MutableVIntList<T>(elements.size).apply { plusAssign(elements as Array<T>) }

// LongList -> VLongList
interface VLongList<T> { val collection: LongList }
val <T> VLongList<T>.size inline get() = collection.size
val <T> VLongList<T>.lastIndex inline get() = collection.lastIndex
val <T> VLongList<T>.indices inline get() = collection.indices
inline fun <T> VLongList<T>.none() = collection.none()
inline fun <T> VLongList<T>.any() = collection.any()
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.any(predicate: (T) -> Boolean):Boolean = collection.any{predicate(a.fromLong(it))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.reversedAny(predicate: (T) -> Boolean):Boolean = collection.any{predicate(a.fromLong(it))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.contains(element: T): Boolean = collection.contains(a.toLong(element))
inline fun <T> VLongList<T>.containsAll(elements: LongList): Boolean = collection.containsAll(elements)
inline fun <T> VLongList<T>.containsAll(elements: VLongList<T>): Boolean = collection.containsAll(elements.collection)
inline fun <T> VLongList<T>.count() = collection.count()
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.count(predicate: (element: T) -> Boolean): Int = collection.count{predicate(a.fromLong(it))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.first(): T = a.fromLong(collection.first())
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.first(predicate: (element: T) -> Boolean): T = a.fromLong(collection.first {predicate(a.fromLong(it))})
context(a: ValueLongAdapter<T>) inline fun <T,R> VLongList<T>.fold(initial: R, operation: (acc: R, element: T) -> R): R = collection.fold(initial, {acc, e -> operation(acc, a.fromLong(e))})
context(a: ValueLongAdapter<T>) inline fun <T,R> VLongList<T>.foldIndexed(initial: R, operation: (index: Int, acc: R, element: T) -> R): R = collection.foldIndexed(initial, {i,acc,e -> operation(i,acc, a.fromLong(e))})
context(a: ValueLongAdapter<T>) inline fun <T,R> VLongList<T>.foldRight(initial: R, operation: (element: T, acc: R) -> R): R = collection.foldRight(initial, {e,acc -> operation(a.fromLong(e), acc)})
context(a: ValueLongAdapter<T>) inline fun <T,R> VLongList<T>.foldRightIndexed(initial: R, operation: (index: Int, element: T, acc: R) -> R): R = collection.foldRightIndexed(initial, {i,e,acc -> operation(i,a.fromLong(e), acc)})
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.forEach(block: (element: T) -> Unit) = collection.forEach { block(a.fromLong(it)) }
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.forEachIndexed(block: (index: Int, element: T) -> Unit) = collection.forEachIndexed { i, e -> block(i, a.fromLong(e)) }
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.forEachReversed(block: (element: T) -> Unit) = collection.forEachReversed { block(a.fromLong(it)) }
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.forEachReversedIndexed(block: (index: Int, element: T) -> Unit) = collection.forEachReversedIndexed { i, e -> block(i, a.fromLong(e)) }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongList<T>.get(@androidx.annotation.IntRange(from = 0) index: Int): T = a.fromLong(collection.get(index))
inline fun <T> VLongList<T>.bitsAtIndex(@androidx.annotation.IntRange(from = 0) index: Int): Long = collection.get(index)
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.elementAtIndex(@androidx.annotation.IntRange(from = 0) index: Int): T = a.fromLong(collection.elementAt(index))
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.elementAtOrElse(@androidx.annotation.IntRange(from = 0) index: Int, defaultValue: (index: Int) -> T): T = a.fromLong(collection.elementAtOrElse(index, {a.toLong(defaultValue(it))}))
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.indexOf(element: T): Int = collection.indexOf(a.toLong(element))
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.indexOfFirst(predicate: (element: T) -> Boolean): Int = collection.indexOfFirst { predicate(a.fromLong(it)) }
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.indexOfLast(predicate: (element: T) -> Boolean): Int = collection.indexOfLast { predicate(a.fromLong(it)) }
inline fun <T> VLongList<T>.isEmpty() = collection.isEmpty()
inline fun <T> VLongList<T>.isNotEmpty() = collection.isNotEmpty()
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.last(): T = a.fromLong(collection.last())
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.last(predicate: (element: T) -> Boolean): T = a.fromLong(collection.last {predicate(a.fromLong(it))})
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.lastIndexOf(element: T): Int = collection.lastIndexOf(a.toLong(element))
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "..."): String
        = collection.joinToString(separator, prefix, postfix, limit, truncated, {a.fromLong(it).toString()})
context(a: ValueLongAdapter<T>) inline fun <T> VLongList<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: (T) -> CharSequence): String
        = collection.joinToString(separator, prefix, postfix, limit, truncated, {transform(a.fromLong(it))})

class MutableVLongList<T>(override val collection: MutableLongList = MutableLongList()): VLongList<T> {
    constructor(capacity: Int) : this(MutableLongList(capacity))
    val capacity inline get() = collection.capacity
    context(a: ValueLongAdapter<T>) inline fun add(element: T): Boolean = collection.add(a.toLong(element))
    inline fun addBits(element: Long): Boolean = collection.add(element)
    context(a: ValueLongAdapter<T>) inline fun add(@androidx.annotation.IntRange(from = 0) index: Int, element: T) = collection.add(index, a.toLong(element))
    inline fun addBits(@androidx.annotation.IntRange(from = 0) index: Int, element: Long) = collection.add(index, element)
    context(a: ValueLongAdapter<T>) inline fun addAll(@androidx.annotation.IntRange(from = 0) index: Int, elements: LongArray) = collection.addAll(index, elements) // TODO: Add VLongArray
    context(a: ValueLongAdapter<T>) inline fun addAll(@androidx.annotation.IntRange(from = 0) index: Int, elements: LongList) = collection.addAll(index, elements)
    context(a: ValueLongAdapter<T>) inline fun addAll(elements: VLongList<T>) = collection.addAll(elements.collection)
    context(a: ValueLongAdapter<T>) inline fun addAll(elements: LongList) = collection.addAll(elements)
    context(a: ValueLongAdapter<T>) inline fun addAll(elements: LongArray) = collection.addAll(elements)
    context(a: ValueLongAdapter<T>) inline fun addAll(elements: Array<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { add(it) }}
    context(a: ValueLongAdapter<T>) inline fun addAll(elements: Collection<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { add(it) }}
    context(a: ValueLongAdapter<T>) inline fun addAll(elements: Iterable<T>) = elements.forEach { add(it) }
    context(a: ValueLongAdapter<T>) inline operator fun plusAssign(elements: LongList) = collection.plusAssign(elements)
    context(a: ValueLongAdapter<T>) inline operator fun plusAssign(elements: LongArray) = collection.plusAssign(elements)
    context(a: ValueLongAdapter<T>) inline fun plusAssign(elements: Array<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { plusAssign(it) }}
    context(a: ValueLongAdapter<T>) inline fun plusAssign(elements: Collection<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { plusAssign(it) }}
    context(a: ValueLongAdapter<T>) inline fun plusAssign(elements: Iterable<T>) = elements.forEach { plusAssign(it) }
    inline fun clear() = collection.clear()
    inline fun trim(minCapacity: Int = size) = collection.trim(minCapacity)
    inline fun ensureCapacity(capacity: Int = size) = collection.ensureCapacity(capacity)
    context(a: ValueLongAdapter<T>) inline operator fun plusAssign(element: T) = collection.plusAssign(a.toLong(element))
    context(a: ValueLongAdapter<T>) inline operator fun minusAssign(element: T) = collection.minusAssign(a.toLong(element))
    context(a: ValueLongAdapter<T>) inline fun remove(element: T) = collection.remove(a.toLong(element))
    context(a: ValueLongAdapter<T>) inline fun removeAll(elements: VLongList<T>) = collection.removeAll(elements.collection)
    context(a: ValueLongAdapter<T>) inline fun removeAll(elements: LongList) = collection.removeAll(elements)
    context(a: ValueLongAdapter<T>) inline fun removeAll(elements: LongArray) = collection.removeAll(elements)
    context(a: ValueLongAdapter<T>) inline fun removeAll(elements: Array<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { remove(it) }}
    context(a: ValueLongAdapter<T>) inline fun removeAll(elements: Collection<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { remove(it) }}
    context(a: ValueLongAdapter<T>) inline fun removeAll(elements: Iterable<T>) = elements.forEach { remove(it) }
    context(a: ValueLongAdapter<T>) inline fun minusAssign(elements: VLongList<T>) = collection.minusAssign(elements.collection)
    context(a: ValueLongAdapter<T>) inline fun minusAssign(elements: LongList) = collection.minusAssign(elements)
    context(a: ValueLongAdapter<T>) inline fun minusAssign(elements: LongArray) = collection.minusAssign(elements)
    context(a: ValueLongAdapter<T>) inline fun minusAssign(elements: Array<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { minusAssign(it) }}
    context(a: ValueLongAdapter<T>) inline fun minusAssign(elements: Collection<T>) {collection.ensureCapacity(size+elements.size); elements.forEach { minusAssign(it) }}
    context(a: ValueLongAdapter<T>) inline fun minusAssign(elements: Iterable<T>) = elements.forEach { minusAssign(it) }
    context(a: ValueLongAdapter<T>) inline fun removeAt(@androidx.annotation.IntRange(from = 0) index: Int): T = a.fromLong(collection.removeAt(index))
    context(a: ValueLongAdapter<T>) inline fun removeRange(@androidx.annotation.IntRange(from = 0) start: Int, @androidx.annotation.IntRange(from = 0) end: Int) = collection.removeRange(start, end)
    context(a: ValueLongAdapter<T>) inline fun retainAll(elements: LongArray) = collection.retainAll(elements)
    context(a: ValueLongAdapter<T>) inline fun retainAll(elements: VLongList<T>) = collection.retainAll(elements.collection)
    context(a: ValueLongAdapter<T>) inline fun retainAll(elements: LongList) = collection.retainAll(elements)
    context(a: ValueLongAdapter<T>) inline operator fun set(@androidx.annotation.IntRange(from = 0) index: Int, element: T): T = a.fromLong(collection.set(index, a.toLong(element)))
    inline fun setBits(@androidx.annotation.IntRange(from = 0) index: Int, element: Long): Long = collection.set(index, element)
    context(a: ValueLongAdapter<T>) inline fun sort() = collection.sort()
    context(a: ValueLongAdapter<T>) inline fun sortDescending() = collection.sortDescending()
    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = collection == other
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print K.toString", ReplaceWith("toString(K.ValueLongAdapter)"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
    context(a: ValueLongAdapter<T>) inline fun toString() = joinToString(", ","{","}")
}

private val EmptyVLongList: VLongList<Nothing> = MutableVLongList(0)
@Suppress("UNCHECKED_CAST")
fun <T>emptyVLongList(): VLongList<T> = EmptyVLongList as VLongList<T>
@Suppress("UNCHECKED_CAST")
fun <T>vLongListOf(): VLongList<T> = EmptyVLongList as VLongList<T>
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T): VLongList<T> = mutableVLongListOf(element1)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T, element2: T): VLongList<T> = mutableVLongListOf(element1, element2)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(element1: T, element2: T, element3: T): VLongList<T> = mutableVLongListOf(element1, element2, element3)
context(a: ValueLongAdapter<T>) inline fun <T>vLongListOf(vararg elements: T): VLongList<T> = MutableVLongList<T>(elements.size).apply { plusAssign(elements as Array<T>) }
inline fun <T>mutableVLongListOf(): MutableVLongList<T> = MutableVLongList()
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T): MutableVLongList<T>
        = MutableVLongList<T>(1).also { it += element1 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T, element2: T): MutableVLongList<T>
        = MutableVLongList<T>(2).also { it += element1; it += element2 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(element1: T, element2: T, element3: T): MutableVLongList<T>
        = MutableVLongList<T>(2).also { it += element1; it += element2; it += element3 }
context(a: ValueLongAdapter<T>) inline fun <T>mutableVLongListOf(vararg elements: T): MutableVLongList<T> = MutableVLongList<T>(elements.size).apply { plusAssign(elements as Array<T>) }

