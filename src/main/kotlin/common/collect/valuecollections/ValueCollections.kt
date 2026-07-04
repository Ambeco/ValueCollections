@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused", "RedundantNullableReturnType",
    "KotlinConstantConditions", "KotlinConstantConditions"
)

package mpd.com.common.collect.valuecollections

import androidx.collection.IntList
import androidx.collection.IntSet
import androidx.collection.LongList
import androidx.collection.LongSet
import java.util.BitSet
import kotlin.also
import kotlin.collections.all
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.random.Random

//
// 
// The collections are not directly :Iterable<T> because constructing the iterator requires a `context(a: ValueIntAdapter<T>)`
// because iterators use a lot of magic and I can't add context(a: ValueIntAdapter<T>) to the getNext itself
interface VIntCollection<T> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_VALUE: IntBits
    val size: Int
    fun anyBits(predicate: (bits:IntBits) -> Boolean): IntBits
    fun containsBits(bits: IntBits): Boolean
    
    context(a: ValueIntAdapter<T>) fun <T> asIterable(): Iterable<T>
    context(a: ValueIntAdapter<T>) fun toString(): String = throw NotImplementedError() // TODO: = toVString()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}

context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.fromInt(bits: IntBits) = if (bits==NULL_VALUE) throw NoSuchElementException() else a.fromInt(bits)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.fromIntOr(bits: IntBits, provider: ()->T): T = if (bits==NULL_VALUE) provider() else a.fromInt(bits)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.fromIntOrNull(bits: IntBits): T? = if (bits==NULL_VALUE) null else a.fromInt(bits)
inline fun <T> VIntCollection<T>.allBits(crossinline predicate: (IntBits) -> Boolean): Boolean = anyBits{!predicate(it)} == NULL_VALUE
inline fun <T> VIntCollection<T>.forEachBits(crossinline action: (bits:IntBits) -> Unit) { anyBits {action(it); false } }
inline fun <T> VIntCollection<T>.singleBits(crossinline predicate: (bits:IntBits) -> Boolean): IntBits {
    // we use an anonymous object here to expose the mutated matchBits without extra allocations
    val matchPredicate = object : (IntBits) -> Boolean {
        var matchBits:IntBits = NULL_VALUE
        override fun invoke(bits: IntBits): Boolean {
            if (predicate(bits)) {
                if (matchBits == NULL_VALUE)
                    matchBits = bits
                else
                    return true
            }
            return false
        }
    }
    val secondMatch = anyBits(matchPredicate)
    return if (secondMatch == NULL_VALUE) matchPredicate.matchBits else NULL_VALUE
}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.any(crossinline predicate: (T) -> Boolean): Boolean = anyBits{predicate(a.fromInt(it))} != NULL_VALUE
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.all(crossinline predicate: (T) -> Boolean): Boolean = allBits {predicate(a.fromInt(it))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.forEach(crossinline action: (T) -> Unit) = forEachBits { action(a.fromInt(it)) }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.single(crossinline predicate: (T) -> Boolean): T = fromInt(singleBits {predicate(a.fromInt(it))})
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.contains(element: T) = containsBits(a.toInt(element))

inline fun <T> VIntCollection<T>.isEmpty() = size == 0
inline fun <T> VIntCollection<T>.isNotEmpty() = size > 0
inline fun <T> VIntCollection<T>.containsAll(bits: IntList): Boolean = bits.first { !containsBits(it) } == NULL_VALUE
inline fun <T> VIntCollection<T>.containsAll(bits: IntSet): Boolean = bits.first { !containsBits(it) } == NULL_VALUE
inline fun <T> VIntCollection<T>.containsAll(bits: VIntCollection<T>): Boolean = bits.anyBits({ !containsBits(it) }) == NULL_VALUE
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.single(): T = single {true}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOr(provider: ()->T): T = fromIntOr(singleBits {true}, provider)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOrElse(defaultValue:T): T = singleOr {defaultValue}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOrNull(): T? =  fromIntOrNull(singleBits { true })
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOrElse(crossinline predicate: (T) -> Boolean, defaultValue:T): T = singleOr(predicate) {defaultValue}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOr(crossinline predicate: (T) -> Boolean, provider: ()->T): T = fromIntOr(singleBits({ predicate(a.fromInt(it)) }), provider)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOrNull(crossinline predicate: (T) -> Boolean): T? = fromIntOrNull(singleBits({ predicate(a.fromInt(it)) }))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.find(crossinline predicate: (T) -> Boolean): T? = fromIntOrNull(anyBits{predicate(a.fromInt(it))})
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.findOrElse(crossinline predicate: (T) -> Boolean, defaultValue:T): T = findOr(predicate) {defaultValue}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.findOr(crossinline predicate: (T) -> Boolean, provider: ()->T): T = fromIntOr(anyBits{predicate(a.fromInt(it))}, provider)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.findOrThrow(crossinline predicate: (T) -> Boolean): T = fromInt(anyBits{predicate(a.fromInt(it))})
// Next is #filter

interface ModifiableVIntCollection<T>: VIntCollection<T> 

interface MutableVIntCollection<T>: ModifiableVIntCollection<T> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim(minCapacity: Int)
    fun addBits(bits: IntBits): Boolean
    fun addAll(elements: IntArray): Boolean {
        ensureCapacity(size+elements.size)
        elements.forEach { addBits(it) }
        return true
    }
    fun removeBits(bits: IntBits): Boolean
    fun removeAll(elements: IntArray): Boolean = elements.all { removeBits(it) }
    context(a: ValueIntAdapter<T>) fun removeAll(elements:Collection<T>): Boolean = elements.all { removeBits(a.toInt(it)) }
    fun removeAll(elements: VIntCollection<T>): Boolean = elements.allBits { removeBits(it) }
    fun clear()
}

context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.add(element: T): Boolean = addBits(a.toInt(element))
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.addAll(elements: Collection<T>): Boolean {
    ensureCapacity(size+elements.size)
    elements.forEach { add(it) }
    return true
}
inline fun <T> MutableVIntCollection<T>.addAll(elements: VIntCollection<T>): Boolean {
    ensureCapacity(size+elements.size)
    elements.forEachBits { addBits(it) }
    return true
}



interface VIntSequence<T> : VIntCollection<T> {
    fun bitsAtIndex(index: Int): IntBits

    fun indexOfBits(bits: IntBits): Int
    fun indexOfFirstIndexedBits(predicate: (index:Int, bits:IntBits) -> Boolean): Int = indexOfFirstIndexedBitsDefault(predicate)
    fun indexOfLastIndexedBits(predicate: (index:Int, bits:IntBits) -> Boolean): Int = indexOfLastIndexedBitsDefault(predicate)

    fun <C: MutableVIntSequence<T>> copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C
    
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}

context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.component1(): T = elementAtIndex(0)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.component2(): T = elementAtIndex(1)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.component3(): T = elementAtIndex(2)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.component4(): T = elementAtIndex(3)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.component5(): T = elementAtIndex(4)
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.elementAtIndex(index: Int): T = a.fromInt(bitsAtIndex(index))
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.elementAtOrNull(index: Int): T? = if(index in 0..<size) elementAtIndex(index) else null
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.elementAtOrElse(index: Int, defaultValue: (index:Int) -> T): T = if(index in 0..<size)elementAtIndex(index) else defaultValue(index)
inline fun <T> VIntSequence<T>.getBits(index: Int): IntBits = if (index in 0..<size) bitsAtIndex(index) else NULL_VALUE
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.get(index: Int): T = if (index in 0..<size) elementAtIndex(index) else throw IndexOutOfBoundsException("$index not in 0..$size")
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.getOrElse(index: Int, defaultValue: (index:Int) -> T): T = if (index in 0..<size) elementAtIndex(index) else defaultValue(index)
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.getOrNull(index: Int): T? = if (index in 0..<size) elementAtIndex(index) else null
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.findLast(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate))
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.first(): T = elementAtIndex(0)
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.first(crossinline predicate: (T) -> Boolean): T = find(predicate) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.firstNotNullOf(crossinline transform: (T) -> R?): R = firstNotNullOfOrNull(transform) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.firstNotNullOfOrNull(crossinline transform: (T) -> R?): R? { for(i in 0 ..< size) return transform(elementAtIndex(i)) ?: continue; return null }
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.firstOrNull(): T? = elementAtOrNull(0)
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.firstOrNull(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfFirst(predicate))
context(a: ValueIntAdapter<T>)fun <T> VIntSequence<T>.indexOf(element: T): Int = indexOfFirst {it==element}
inline fun <T> VIntSequence<T>.indexOfFirstBits(crossinline predicate: (IntBits) -> Boolean): Int { for(i in 0 ..< size) if (predicate(bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.indexOfFirst(crossinline predicate: (T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.indexOfFirstIndexed(crossinline predicate: (index:Int,T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T> VIntSequence<T>.indexOfFirstIndexedBitsDefault(crossinline predicate: (index:Int, bits:IntBits) -> Boolean): Int { for(i in 0 ..< size) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.indexOfLast(crossinline predicate: (T) -> Boolean): Int { for(i in size-1..0) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.indexOfLastIndexed(crossinline predicate: (index:Int,T) -> Boolean): Int { for(i in size-1..0) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T> VIntSequence<T>.indexOfLastIndexedBitsDefault(crossinline predicate: (index:Int, bits:IntBits) -> Boolean): Int { for(i in size-1..0) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.last(): T = elementAtIndex(size-1)
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.last(crossinline predicate: (T) -> Boolean): T = findLast(predicate) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.lastIndexOf(element: T): Int = indexOfLast {it==element}
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.lastOrNull(): T? = elementAtOrNull(size - 1)
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.lastOrNull(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate)) 
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.drop(n: Int): FlatVIntList<T> = slice(IntRange(n,size-1))
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.dropLast(n: Int): FlatVIntList<T> = slice(IntRange(0,size-n))
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.dropWhile(crossinline predicate: (T) -> Boolean): FlatVIntList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) FlatVIntList(this) else slice(IntRange(i, size))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.dropLastWhile(crossinline predicate: (T) -> Boolean): FlatVIntList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(0, i))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.filter(crossinline predicate: (T) -> Boolean): FlatVIntList<T> = filterFromMask(filterMask(predicate))
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.filterMask(crossinline predicate: (T) -> Boolean): BitSet = filterIndexedMask {_,e->predicate(e)}
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.filterFromMask(mask: BitSet): FlatVIntList<T> = FlatVIntList<T>(mask.cardinality()).also {c-> forEachBitsIndexed {i,e-> if(mask[i]) c.addBits(e)} }
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.filterIndexed(crossinline predicate: (index: Int, T) -> Boolean): FlatVIntList<T> = filterFromMask(filterIndexedMask(predicate))
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntSequence<T>> VIntSequence<T>.filterIndexedTo(destination: C, crossinline predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> VIntSequence<T>.filterIndexedTo(destination: C, crossinline predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.filterIndexedMask(crossinline predicate: (index: Int, T) -> Boolean): BitSet {val destination=BitSet(size); forEachIndexed { i, e -> destination.set(i,predicate(i, e))}; return destination }
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.filterNot(crossinline predicate: (T) -> Boolean): VIntList<T> = filter {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntSequence<T>> VIntSequence<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> VIntSequence<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntSequence<T>> VIntSequence<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> VIntSequence<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.slice(indices: IntRange): FlatVIntList<T> = copyInto<FlatVIntList<T>>(FlatVIntList<T>(indices.last-indices.first), 0, indices.first, indices.last)
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.slice(indices: Iterable<Int>): FlatVIntList<T> = FlatVIntList<T>(if (indices is Collection<Int>) indices.size else size/8).also { for(i in indices) it.addBits(bitsAtIndex(i)) }
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sliceArray(indices: Collection<Int>): VIntArray<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sliceArray(indices: IntRange): VIntArray<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.take(n: Int): FlatVIntList<T> = slice(IntRange(0,n))
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.takeLast(n: Int): FlatVIntList<T> = slice(IntRange(size-n,size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.takeLastWhile(crossinline predicate: (T) -> Boolean): FlatVIntList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) FlatVIntList<T>(this) else slice(IntRange(0, i))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.takeWhile(crossinline predicate: (T) -> Boolean): FlatVIntList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) FlatVIntList<T>(this) else slice(IntRange(i,size))}
inline fun <T> VIntSequence<T>.reversed(): FlatVIntList<T> = FlatVIntList<T>(size).also {forEachBitsIndexed{i,e-> it.setBits(size-i-1, e) }}
//TODO: context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sorted(): FlatVIntList<T> = FlatVIntList<T>(this).also{it.sort()}
//TODO: context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntSequence<T>.sortedBy(crossinline selector: (T) -> R?): FlatVIntList<T> = FlatVIntList<T>(this).also{it.sortedBy(selector)}
//TODO: context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntSequence<T>.sortedByDescending(crossinline selector: (T) -> R?): VIntList<T> = FlatVIntList<T>(this).also{it.sortedBy(selector)}
//TODO: context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntSequence<T>.sortedDescending(): VIntList<T>
//TODO: context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sortedWith(comparator: Comparator<in T>): VIntList<T>
inline fun <T, C: MutableVIntSequence<T>> VIntSequence<T>.copyIntoDefault(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{for(i in startIndex..endIndex) destination.setBits(i+destinationOffset, bitsAtIndex(i))}
context(a: ValueIntAdapter<T>) inline fun <T, C: MutableList<T>> VIntSequence<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{for(i in startIndex..endIndex) destination.set(i+destinationOffset, elementAtIndex(i))}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VIntSequence<T>.associateVIntInt(crossinline transform: (T) -> VIntIntPair<K, V>): VIntIntMap<K, V> = associateTo(MutableVIntIntMap(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VIntSequence<T>.associateVIntLong(crossinline transform: (T) -> VIntLongPair<K, V>): VIntLongMap<K, V> = associateTo(MutableVIntLongMap(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VIntSequence<T>.associateVLongInt(crossinline transform: (T) -> VLongIntPair<K, V>): VLongIntMap<K, V> = associateTo(MutableVLongIntMap(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VIntSequence<T>.associateVLongLong(crossinline transform: (T) -> VLongLongPair<K, V>): VLongLongMap<K, V> = associateTo(MutableVLongLongMap(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V> VIntSequence<T>.associateGeneric(crossinline transform: (T) -> Pair<K, V>): Map<K, V> = associateTo(HashMap<K,V>(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>) inline fun <T, K> VIntSequence<T>.associateByVInt(crossinline keySelector: (T) -> K): MutableVIntIntMap<K, T> = associateByTo(MutableVIntIntMap<K,T>(size),keySelector,{it})
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>) inline fun <T, K> VIntSequence<T>.associateByVLong(crossinline keySelector: (T) -> K): MutableVLongIntMap<K, T> = associateByTo(MutableVLongIntMap(size),keySelector,{it})
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntSequence<T>.associateByGeneric(crossinline keySelector: (T) -> K): Map<K, T> = HashMap<K,T>(size).also{c->forEach {c.put(keySelector(it),it)}}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VIntSequence<T>.associateByVIntInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): VIntIntMap<K, V> = associateByTo(MutableVIntIntMap(size),keySelector,valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VIntSequence<T>.associateByVIntLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): VIntLongMap<K, V> = associateByTo(MutableVIntLongMap(size),keySelector,valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VIntSequence<T>.associateByVLongInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): VLongIntMap<K, V> = associateByTo(MutableVLongIntMap(size),keySelector,valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VIntSequence<T>.associateByVLongLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): VLongLongMap<K, V> = associateByTo(MutableVLongLongMap(size),keySelector,valueTransform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V> VIntSequence<T>.associateByGeneric(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): Map<K, V> = HashMap<K,V>(size).also{c->forEach {c.put(keySelector(it),valueTransform(it))}}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVIntIntMap<K,V>> VIntSequence<T>.associateByTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVIntLongMap<K,V>> VIntSequence<T>.associateByTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVLongIntMap<K,V>> VIntSequence<T>.associateByTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVLongLongMap<K,V>> VIntSequence<T>.associateByTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>> VIntSequence<T>.associateByTo(destination: M, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): M = destination.also{c->forEach {c.put(keySelector(it),valueTransform(it))}}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVIntIntMap<K,V>> VIntSequence<T>.associateTo(destination: C, crossinline transform: (T) -> VIntIntPair<K, V>): C = destination.also{c->c.putAll(this,transform)}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVIntLongMap<K,V>> VIntSequence<T>.associateTo(destination: C, crossinline transform: (T) -> VIntLongPair<K, V>): C = destination.also{c->c.putAll(this,transform)}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVLongIntMap<K,V>> VIntSequence<T>.associateTo(destination: C, crossinline transform: (T) -> VLongIntPair<K, V>): C = destination.also{c->c.putAll(this,transform)}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVLongLongMap<K,V>> VIntSequence<T>.associateTo(destination: C, crossinline transform: (T) -> VLongLongPair<K, V>): C = destination.also{c->c.putAll(this,transform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>> VIntSequence<T>.associateTo(destination: M, crossinline transform: (T) -> Pair<K, V>): M = destination.also{c->forEach {val p=transform(it); c[p.first] = p.second}}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>> VIntSequence<T>.toCollection(destination: C): C = destination.also { it.addAll(this) }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> VIntSequence<T>.toCollection(destination: C): C = destination.also{c->forEach {c.add(it)}}
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.toHashSet(): HashSet<T> = toCollection(HashSet(size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.toList(): VIntList<T> = this as? VIntList<T> ?: toMutableList()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.toListGeneric(): List<T> = toMutableListGeneric()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.toMutableList(): FlatVIntList<T> = if (this is FlatVIntList<T>) this else toCollection(FlatVIntList<T>(size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.toMutableListGeneric(): MutableList<T> = toCollection(ArrayList(size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.toSet(): VIntSet<T> = this as? VIntSet<T> ?: toMutableSet()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.toMutableSet(): MutableVIntSet<T> = this as? MutableVIntSet<T> ?: toCollection(FlatVIntSet<T>(size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.toSetGeneric(): Set<T> = toHashSet()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.toIntArray(): IntArray =  (this as? VIntArray<T>)?.collection ?: throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.toVIntArray(): VIntArray<T> = this as? VIntArray<T> ?: throw NotImplementedError()
inline fun <T> VIntSequence<T>.toArrayGenericBits(): Array<IntBits> = (this as? VIntArray<T>)?.collection?.toTypedArray() ?: throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.asSequence(): Sequence<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.asList(): VIntList<T> = toList()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.asListGeneric(): List<T> = toListGeneric()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.contentEquals(other: VIntSequence<T>?): Boolean = other != null && size == other.size && this.indexOfFirstIndexedBits {i,e-> other.bitsAtIndex(i) != e } == -1
/*
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.flatMap(crossinline transform: (T) ->VIntCollection<R>): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.flatMap(crossinline transform: (T) -> Sequence<R>): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.flatMapIndexed(crossinline transform: (index: Int, T) ->VIntCollection<R>): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.flatMapIndexed(crossinline transform: (index: Int, T) -> Sequence<R>): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntSequence<T>.flatMapIndexedTo(destination: C, crossinline transform: (index: Int, T) ->VIntCollection<R>): C
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntSequence<T>.flatMapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> Sequence<R>): C
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntSequence<T>.flatMapTo(destination: C, crossinline transform: (T) ->VIntCollection<R>): C
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntSequence<T>.flatMapTo(destination: C, crossinline transform: (T) -> Sequence<R>): C
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntSequence<T>.groupByVInt(crossinline keySelector: (T) -> K): MutableVIntObjectMap<K, List<T>> = groupByTo(MutableVIntObjectMap<K,List<T>>(), keySelector)
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntSequence<T>.groupByVLong(crossinline keySelector: (T) -> K): MutableVLongObjectMap<K, List<T>> = groupByTo(MutableVLongObjectMap<K,List<T>>(), keySelector)
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntSequence<T>.groupByGeneric(crossinline keySelector: (T) -> K): MutableMap<K, List<T>> = groupByTo(HashMap<K,List<T>>(), keySelector)
context(a: ValueIntAdapter<T>) inline fun <T, K, V> VIntSequence<T>.groupByVInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableVIntObjectMap<K, List<V>>
context(a: ValueIntAdapter<T>) inline fun <T, K, V> VIntSequence<T>.groupByVLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableVLongObjectMap<K, List<V>>
context(a: ValueIntAdapter<T>) inline fun <T, K, V> VIntSequence<T>.groupByGeneric(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableMap<K, List<V>>
context(a: ValueIntAdapter<T>) inline fun <T, K, M : MutableMap<in K, MutableList<T>>> VIntSequence<T>.groupByTo(destination: M, crossinline keySelector: (T) -> K): M
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, MutableList<V>>> VIntSequence<T>.groupByTo(destination: M, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): M
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntSequence<T>.groupingBy(crossinline keySelector: (T) -> K): Grouping<T, K>
 */
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntSequence<T>.mapVInt(crossinline transform: (T) -> R): FlatVIntList<R> = mapTo(FlatVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntSequence<T>.mapVLong(crossinline transform: (T) -> R): FlatVLongList<R> = mapTo(FlatVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.mapGeneric(crossinline transform: (T) -> R): MutableList<R> = mapTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntSequence<T>.mapIndexedVInt(crossinline transform: (index: Int, T) -> R): FlatVIntList<R> = mapIndexedTo(FlatVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntSequence<T>.mapIndexedVLong(crossinline transform: (index: Int, T) -> R): FlatVLongList<R> = mapIndexedTo(FlatVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.mapIndexedGeneric(crossinline transform: (index: Int, T) -> R): List<R> = mapIndexedTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntSequence<T>.mapIndexedVIntNotNull(crossinline transform: (index: Int, T) -> R?): FlatVIntList<R> = mapIndexedNotNullTo(FlatVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntSequence<T>.mapIndexedVLongNotNull(crossinline transform: (index: Int, T) -> R?): FlatVLongList<R> = mapIndexedNotNullTo(FlatVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.mapIndexedGenericNotNull(crossinline transform: (index: Int, T) -> R?): List<R> = mapIndexedNotNullTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntSequence<R>> VIntSequence<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VIntSequence<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntSequence<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntSequence<R>> VIntSequence<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = throw NotImplementedError()
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VIntSequence<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntSequence<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.mapNotNull(crossinline transform: (T) -> R?): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntSequence<T>.mapNotNullTo(destination: C, crossinline transform: (T) -> R?): C = throw NotImplementedError()
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntSequence<R>> VIntSequence<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VIntSequence<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntSequence<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.withIndex():VIntCollection<IndexedValue<T>> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.distinct(): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntSequence<T>.distinctBy(selector: (T) -> K): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline infix fun <T> VIntSequence<T>.intersect(other:VIntCollection<T>): Set<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline infix fun <T> VIntSequence<T>.subtract(other:VIntCollection<T>): Set<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline infix fun <T> VIntSequence<T>.union(other:VIntCollection<T>): Set<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.any(): Boolean = size > 0
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.count(): Int = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.count(crossinline predicate: (T) -> Boolean): Int = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.fold(initial: R, operation: (acc: R, T) -> R): R = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.foldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): R = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, C: VIntSequence<T>> C.onEach(crossinline action: (T) -> Unit): C = apply{forEach(action)}
context(a: ValueIntAdapter<T>) inline fun <T, C: VIntSequence<T>> C.onEachIndexed(crossinline action: (Int,T) -> Unit): C = apply{forEachIndexed(action)}
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.forEachIndexed(crossinline action: (index:Int, T) -> Unit) {for(i in 0..size) action(i, elementAtIndex(i))}
inline fun <T> VIntSequence<T>.forEachBitsIndexed(crossinline action: (index:Int, IntBits) -> Unit) {for(i in 0..size) action(i, bitsAtIndex(i))}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntSequence<T>.max(): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntSequence<T>.maxBy(selector: (T) -> R): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntSequence<T>.maxByOrNull(selector: (T) -> R): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.maxOf(selector: (T) -> Double): Double = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.maxOf(selector: (T) -> Float): Float = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntSequence<T>.maxOf(selector: (T) -> R): R = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.maxOfOrNull(selector: (T) -> Double): Double? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.maxOfOrNull(selector: (T) -> Float): Float? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntSequence<T>.maxOfOrNull(selector: (T) -> R): R? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.maxOfWith(comparator: Comparator<R>, selector: (T) -> R): R = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.maxOfWithOrNull(comparator: Comparator<R>, selector: (T) -> R): R? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntSequence<T>.maxOrNull(): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.maxWith(comparator: Comparator<in T>): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.maxWithOrNull(comparator: Comparator<in T>): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntSequence<T>.min(): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntSequence<T>.minBy(selector: (T) -> R): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntSequence<T>.minByOrNull(selector: (T) -> R): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.minOf(selector: (T) -> Double): Double = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.minOf(selector: (T) -> Float): Float = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntSequence<T>.minOf(selector: (T) -> R): R = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.minOfOrNull(selector: (T) -> Double): Double? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.minOfOrNull(selector: (T) -> Float): Float? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntSequence<T>.minOfOrNull(selector: (T) -> R): R? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.minOfWith(comparator: Comparator<R>, selector: (T) -> R): R = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.minOfWithOrNull(comparator: Comparator<R>, selector: (T) -> R): R? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntSequence<T>.minOrNull(): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.minWith(comparator: Comparator<in T>): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.minWithOrNull(comparator: Comparator<in T>): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.none(): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.none(crossinline predicate: (T) -> Boolean): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntSequence<T>.reduce(operation: (acc: S, T) -> S): S = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntSequence<T>.reduceIndexed(operation: (index: Int, acc: S, T) -> S): S = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntSequence<T>.reduceIndexedOrNull(operation: (index: Int, acc: S, T) -> S): S? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntSequence<T>.reduceOrNull(operation: (acc: S, T) -> S): S? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntSequence<T>.reduceRight(operation: (T, acc: T) -> T): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntSequence<T>.reduceRightIndexed(operation: (index: Int, T, acc: T) -> T): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntSequence<T>.reduceRightIndexedOrNull(operation: (index: Int, T, acc: T) -> T): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntSequence<T>.reduceRightOrNull(operation: (T, acc: T) -> T): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.runningFold(initial: R, operation: (acc: R, T) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.runningFoldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntSequence<T>.runningReduce(operation: (acc: S, T) -> S): List<S> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntSequence<T>.runningReduceIndexed(operation: (index: Int, acc: S, T) -> S): List<S> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.scan(initial: R, operation: (acc: R, T) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.scanIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.shuffle(): Unit = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.shuffle(random: Random): Unit = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sorted(): FlatVIntList<T> = toMutableList().also{it.sort()}
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sortedArray(): VIntArray<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sortedArrayDescending(): VIntArray<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntSequence<T>.sortedBy(crossinline selector: (T) -> R?): FlatVIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntSequence<T>.sortedByDescending(crossinline selector: (T) -> R?): FlatVIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sortedDescending(): FlatVIntList<T> = toMutableList().also{it.sortDescending()}
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sortedWith(comparator: Comparator<in IntBits>): FlatVIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sumBy(selector: (T) -> Int): Int = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sumByDouble(selector: (T) -> Double): Double = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sumOf(selector: (T) -> Double): Double = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sumOf(selector: (T) -> Int): Int = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sumOf(selector: (T) -> Long): Long = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sumOfUInt(selector: (T) -> UInt): UInt = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.sumOfULong(selector: (T) -> ULong): ULong = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.chunked(size: Int): List<List<T>> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.chunked(size: Int, crossinline transform: (List<T>) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.minus(element: T): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.minus(elements: Array<out T>): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.minus(elements:VIntCollection<T>): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.minus(elements: Sequence<T>): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.minusElement(element: T): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.partition(crossinline predicate: (T) -> Boolean): Pair<List<T>, List<T>> = throw NotImplementedError()
inline fun <T> ModifiableVIntSequence<T>.random(): T = throw NotImplementedError()
fun <T> ModifiableVIntSequence<T>.random(random: Random): T = throw NotImplementedError()
inline fun <T> ModifiableVIntSequence<T>.randomOrNull(): T? = throw NotImplementedError()
fun <T> ModifiableVIntSequence<T>.randomOrNull(random: Random): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.plus(element: T): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.plus(elements: Array<out T>): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.plus(elements:VIntCollection<T>): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntSequence<T>.plus(elements: Sequence<T>): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.plusElement(element: T): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.windowed(size: Int, step: Int = 1, partialWindows: Boolean = false): List<List<T>> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.windowed(size: Int, step: Int = 1, partialWindows: Boolean = false, crossinline transform: (List<T>) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline infix fun <T, R> VIntSequence<T>.zip(other: Array<out R>): List<Pair<T, R>> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R, V> VIntSequence<T>.zip(other: Array<out R>, crossinline transform: (a: T, b: R) -> V): List<V> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline infix fun <T, R> VIntSequence<T>.zip(other:VIntCollection<R>): List<Pair<T, R>> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R, V> VIntSequence<T>.zip(other:VIntCollection<R>, crossinline transform: (a: T, b: R) -> V): List<V> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.zipWithNext(): List<Pair<T, T>> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntSequence<T>.zipWithNext(crossinline transform: (a: T, b: T) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, A : Appendable> VIntSequence<T>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence) = { it.toString() }): A = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence) = { it.toString() }): String = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntSequence<T>.toVString() = joinToString(", ","{","}")



// can modify elements, but not add or remove
interface ModifiableVIntSequence<T>: VIntSequence<T>, ModifiableVIntCollection<T> {
    fun setBits(index: Int, bits: IntBits)
}
context(a: ValueIntAdapter<T>) inline fun <T> ModifiableVIntSequence<T>.set(index: Int, value: T) = setBits(index, a.toInt(value))
inline fun <T> ModifiableVIntSequence<T>.sort(): Unit = throw NotImplementedError()
inline fun <T> ModifiableVIntSequence<T>.sort(fromIndex: Int, toIndex: Int): Unit = throw NotImplementedError()
inline fun <T> ModifiableVIntSequence<T>.sortDescending(): Unit = throw NotImplementedError()
inline fun <T> ModifiableVIntSequence<T>.sortDescending(fromIndex: Int, toIndex: Int): Unit = throw NotImplementedError()



// can modify, add, and remove elements
interface MutableVIntSequence<T>: ModifiableVIntSequence<T>, MutableVIntCollection<T> {
    fun addBits(index: Int, bits: IntBits)
    fun addAll(index: Int, elements: VIntCollection<T>): Boolean
    context(a: ValueIntAdapter<T>) fun addAll(index: Int, elements: Collection<T>): Boolean

    fun removeAt(index: Int): Boolean
    fun removeAllIndexedBits(predicate: (index: Int, bits: IntBits) -> Boolean): Boolean
}
operator fun <T> MutableVIntSequence<T>.plus(element: T): VIntList<T> = throw NotImplementedError()
operator fun <T> MutableVIntSequence<T>.plus(elements: Iterable<T>): VIntList<T> = throw NotImplementedError()
operator fun <T> MutableVIntSequence<T>.plus(elements: Sequence<T>): VIntList<T> = throw NotImplementedError()
inline fun <T> MutableVIntSequence<T>.plusElement(element: T): VIntList<T>  = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.addAll(elements: IntArray): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.addAll(elements: Array<out T>): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.addAll(elements: Iterable<T>): Boolean = throw NotImplementedError()
inline operator fun <T> MutableVIntSequence<T>.plusAssign(elements: IntList): Unit = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntSequence<T>.plusAssign(elements: IntArray): Unit = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.plusAssign(elements: Array<out T>): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.plusAssign(elements: Collection<T>): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.plusAssign(elements: Iterable<T>): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntSequence<T>.plusAssign(element: T): Unit = throw NotImplementedError()

context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.add(index: Int, element: T): Unit = addBits(index, a.toInt(element))
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.addAll(index: Int, elements: IntArray): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.addAll(index: Int, elements: VIntCollection<T>) : Boolean= throw NotImplementedError()
inline fun <T> MutableVIntSequence<T>.addAll(index: Int, elements: IntList): Boolean = throw NotImplementedError()

context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.remove(element: T): Boolean = throw NotImplementedError()
inline fun <T> MutableVIntSequence<T>.removeBits(element: Int): Boolean = throw NotImplementedError()
inline fun <T> MutableVIntSequence<T>.removeAll(elements: VIntCollection<T>): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.removeAll(elements: Collection<T>): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntSequence<T>.minusAssign(element: T): Unit = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.removeAll(elements: VIntList<out T>): Boolean= throw NotImplementedError()
inline fun <T> MutableVIntSequence<T>.removeAll(elements: IntList): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.removeAll(elements: IntArray): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.removeAll(elements: Array<out T>): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.removeAll(elements: Iterable<T>): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.minusAssign(elements: VIntList<out T>): Boolean= throw NotImplementedError()
inline fun <T> MutableVIntSequence<T>.minusAssign(elements: IntList): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.minusAssign(elements: IntArray): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.minusAssign(elements: Array<out T>): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.minusAssign(elements: Collection<T>): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.minusAssign(elements: Iterable<T>): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.removeAt(index: Int): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.removeRange(start: Int, end: Int): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.retainAll(elements: IntArray): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntSequence<T>.retainAll(elements: VIntList<out T>): Boolean= throw NotImplementedError()
inline fun <T> MutableVIntSequence<T>.retainAll(elements: IntList): Boolean= throw NotImplementedError()






interface VLongCollection<T> {
    val NULL_VALUE: LongBits
    val size: Int
    fun anyBits(predicate: (bits:LongBits) -> Boolean): LongBits
    fun containsBits(bits: LongBits): Boolean

    context(a: ValueLongAdapter<T>) fun <T> asIterable(): Iterable<T>
    context(a: ValueLongAdapter<T>) fun toString(): String = throw NotImplementedError()// TODO: = toVString()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}

context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.fromLong(bits: LongBits) = if (bits==NULL_VALUE) throw NoSuchElementException() else a.fromLong(bits)
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.fromLongOr(bits: LongBits, provider: ()->T): T = if (bits==NULL_VALUE) provider() else a.fromLong(bits)
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.fromLongOrNull(bits: LongBits): T? = if (bits==NULL_VALUE) null else a.fromLong(bits)
inline fun <T> VLongCollection<T>.allBits(crossinline predicate: (LongBits) -> Boolean): Boolean = anyBits{!predicate(it)} == NULL_VALUE
inline fun <T> VLongCollection<T>.forEachBits(crossinline action: (bits:LongBits) -> Unit) { anyBits {action(it); false } }
inline fun <T> VLongCollection<T>.singleBits(crossinline predicate: (bits:LongBits) -> Boolean): LongBits {
    // we use an anonymous object here to expose the mutated matchBits without extra allocations
    val matchPredicate = object : (LongBits) -> Boolean {
        var matchBits:LongBits = NULL_VALUE
        override fun invoke(bits: LongBits): Boolean {
            if (predicate(bits)) {
                if (matchBits == NULL_VALUE)
                    matchBits = bits
                else
                    return true
            }
            return false
        }
    }
    val secondMatch = anyBits(matchPredicate)
    return if (secondMatch == NULL_VALUE) matchPredicate.matchBits else NULL_VALUE
}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.any(crossinline predicate: (T) -> Boolean): Boolean = anyBits{predicate(a.fromLong(it))} != NULL_VALUE
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.all(crossinline predicate: (T) -> Boolean): Boolean = allBits {predicate(a.fromLong(it))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.forEach(crossinline action: (T) -> Unit) = forEachBits { action(a.fromLong(it)) }
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.single(crossinline predicate: (T) -> Boolean): T = fromLong(singleBits {predicate(a.fromLong(it))})
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.contains(element: T) = containsBits(a.toLong(element))

inline fun <T> VLongCollection<T>.isEmpty() = size == 0
inline fun <T> VLongCollection<T>.isNotEmpty() = size > 0
inline fun <T> VLongCollection<T>.containsAll(bits: LongList): Boolean = bits.first { !containsBits(it) } == NULL_VALUE
inline fun <T> VLongCollection<T>.containsAll(bits: LongSet): Boolean = bits.first { !containsBits(it) } == NULL_VALUE
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.single(): T = single {true}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.singleOr(provider: ()->T): T = fromLongOr(singleBits {true}, provider)
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.singleOrElse(defaultValue:T): T = singleOr {defaultValue}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.singleOrNull(): T? =  fromLongOrNull(singleBits { true })
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.singleOrElse(crossinline predicate: (T) -> Boolean, defaultValue:T): T = singleOr(predicate) {defaultValue}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.singleOr(crossinline predicate: (T) -> Boolean, provider: ()->T): T = fromLongOr(singleBits({ predicate(a.fromLong(it)) }), provider)
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.singleOrNull(crossinline predicate: (T) -> Boolean): T? = fromLongOrNull(singleBits({ predicate(a.fromLong(it)) }))
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.find(crossinline predicate: (T) -> Boolean): T? = fromLongOrNull(anyBits{predicate(a.fromLong(it))})
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.findOrElse(crossinline predicate: (T) -> Boolean, defaultValue:T): T = findOr(predicate) {defaultValue}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.findOr(crossinline predicate: (T) -> Boolean, provider: ()->T): T = fromLongOr(anyBits{predicate(a.fromLong(it))}, provider)
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.findOrThrow(crossinline predicate: (T) -> Boolean): T = fromLong(anyBits{predicate(a.fromLong(it))})
// Next is #filter



interface ModifiableVLongCollection<T>: VLongCollection<T>

interface MutableVLongCollection<T>: ModifiableVLongCollection<T> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim(minCapacity: Int)
    fun addBits(bits: LongBits): Boolean
    fun addAll(elements: LongArray): Boolean {
        ensureCapacity(size+elements.size)
        elements.forEach { addBits(it) }
        return true
    }
    fun removeBits(bits: LongBits): Boolean
    fun removeAll(elements: LongArray): Boolean = elements.all { removeBits(it) }
    context(a: ValueLongAdapter<T>) fun removeAll(elements:Collection<T>): Boolean = elements.all { removeBits(a.toLong(it)) }
    fun removeAll(elements: VLongCollection<T>): Boolean = elements.allBits { removeBits(it) }
    fun clear()
}

context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.add(element: T): Boolean = addBits(a.toLong(element))
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.addAll(elements: Collection<T>): Boolean {
    ensureCapacity(size+elements.size)
    elements.forEach { add(it) }
    return true
}
inline fun <T> MutableVLongCollection<T>.addAll(elements: VLongCollection<T>): Boolean {
    ensureCapacity(size+elements.size)
    elements.forEachBits { addBits(it) }
    return true
}


interface VLongSequence<T> : VLongCollection<T>{
    fun bitsAtIndex(index: Int): LongBits
    
    fun indexOfBits(bits: LongBits): Int
    fun indexOfFirstIndexedBits(predicate: (index:Int, bits:LongBits) -> Boolean): Int
    fun indexOfLastIndexedBits(predicate: (index:Int, bits:LongBits) -> Boolean): Int

    fun <C: MutableVLongSequence<T>> copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Longs. Use toString(ValueLongAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.component1(): T = elementAtIndex(0)
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.component2(): T = elementAtIndex(1)
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.component3(): T = elementAtIndex(2)
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.component4(): T = elementAtIndex(3)
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.component5(): T = elementAtIndex(4)
inline fun <T>VLongSequence<T>.isEmpty() = size == 0
inline fun <T>VLongSequence<T>.isNotEmpty() = size > 0
context(a: ValueLongAdapter<T>) operator inline fun <T>VLongSequence<T>.contains(element: T) = indexOf(element) != -1
inline fun <T>VLongSequence<T>.containsAll(elements: VLongCollection<T>) = elements.allBits { indexOfBits(it) != -1 }
inline fun <T>VLongSequence<T>.containsAll(elements: LongList) = elements.forEach { indexOfBits(it) != -1 }
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.elementAtIndex(index: Int): T = a.fromLong(bitsAtIndex(index))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.elementAtOrNull(index: Int): T? = if(index in 0..<size) elementAtIndex(index) else null
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.elementAtOrElse(index: Int, defaultValue: (Int) -> T): T = if(index in 0..<size)elementAtIndex(index) else defaultValue(index)
inline fun <T> VLongSequence<T>.getBits(index: Int): LongBits = if (index in 0..<size) bitsAtIndex(index) else NULL_VALUE
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.getOrElse(index: Int, defaultValue: (Int) -> T): T = if (index in 0..<size) elementAtIndex(index) else defaultValue(index)
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.getOrNull(index: Int): T? = if (index in 0..<size) elementAtIndex(index) else null
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.find(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfFirst(predicate))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.findLast(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.first(): T = elementAtIndex(0)
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.first(crossinline predicate: (T) -> Boolean): T = find(predicate) ?: throw NoSuchElementException()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.firstNotNullOf(crossinline transform: (T) -> R?): R = firstNotNullOfOrNull(transform) ?: throw NoSuchElementException()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.firstNotNullOfOrNull(crossinline transform: (T) -> R?): R? { for(i in 0 ..< size) return transform(elementAtIndex(i)) ?: continue; return null }
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.firstOrNull(): T? = elementAtOrNull(0)
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.firstOrNull(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfFirst(predicate))
context(a: ValueLongAdapter<T>)fun <T>VLongSequence<T>.indexOf(element: T): Int = indexOfFirst {it==element}
inline fun <T>VLongSequence<T>.indexOfFirstBits(crossinline predicate: (LongBits) -> Boolean): Int { for(i in 0 ..< size) if (predicate(bitsAtIndex(i))) return i; return -1 }
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.indexOfFirst(crossinline predicate: (T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.indexOfFirstIndexed(crossinline predicate: (Int,T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T>VLongSequence<T>.indexOfFirstIndexedBitsDefault(crossinline predicate: (index:Int, bits:LongBits) -> Boolean): Int { for(i in 0 ..< size) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.indexOfLast(crossinline predicate: (T) -> Boolean): Int { for(i in size-1..0) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.indexOfLastIndexed(crossinline predicate: (Int,T) -> Boolean): Int { for(i in size-1..0) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T>VLongSequence<T>.indexOfLastIndexedBitsDefault(crossinline predicate: (index:Int, bits:LongBits) -> Boolean): Int { for(i in size-1..0) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.last(): T = elementAtIndex(size-1)
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.last(crossinline predicate: (T) -> Boolean): T = findLast(predicate) ?: throw NoSuchElementException()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.lastIndexOf(element: T): Int = indexOfLast {it==element}
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.lastOrNull(): T? = elementAtOrNull(size - 1)
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.lastOrNull(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.single(): T = elementAtIndex(0)
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.single(crossinline predicate: (T) -> Boolean): T = elementAtIndex(indexOfFirst(predicate))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.singleOrNull(): T? = elementAtOrNull(0)
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.singleOrNull(crossinline predicate: (T) -> Boolean): T? = if (size==1 && predicate(elementAtIndex(0))) elementAtIndex(0) else null
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.drop(n: Int): FlatVLongList<T> = slice(IntRange(n,size-1))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.dropLast(n: Int): FlatVLongList<T> = slice(IntRange(0,size-n))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.dropWhile(crossinline predicate: (T) -> Boolean): FlatVLongList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) FlatVLongList(this) else slice(IntRange(i, size))}
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.dropLastWhile(crossinline predicate: (T) -> Boolean): FlatVLongList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(0, i))}
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.filter(crossinline predicate: (T) -> Boolean): FlatVLongList<T> = filterFromMask(filterMask(predicate))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.filterMask(crossinline predicate: (T) -> Boolean): BitSet = filterIndexedMask {_,e->predicate(e)}
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.filterFromMask(mask: BitSet): FlatVLongList<T> = FlatVLongList<T>(mask.cardinality()).also {c-> forEachBitsIndexed {i,e-> if(mask[i]) c.addBits(e)} }
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.filterIndexed(crossinline predicate: (index: Int, T) -> Boolean): FlatVLongList<T> = filterFromMask(filterIndexedMask(predicate))
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongCollection<T>>VLongSequence<T>.filterIndexedTo(destination: C, crossinline predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollection<T>>VLongSequence<T>.filterIndexedTo(destination: C, crossinline predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.filterIndexedMask(crossinline predicate: (index: Int, T) -> Boolean): BitSet {val destination=BitSet(size); forEachIndexed { i, e -> destination.set(i,predicate(i, e))}; return destination }
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.filterNot(crossinline predicate: (T) -> Boolean): VLongList<T> = filter {!predicate(it)}
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongCollection<T>>VLongSequence<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollection<T>>VLongSequence<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongCollection<T>>VLongSequence<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollection<T>>VLongSequence<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.slice(indices: IntRange): FlatVLongList<T> = copyInto<FlatVLongList<T>>(FlatVLongList<T>(indices.last-indices.first), 0, indices.first, indices.last)
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.slice(indices: Iterable<Int>): FlatVLongList<T> = FlatVLongList<T>(if (indices is Collection<Int>) indices.size else size/8).also { for(i in indices) it.addBits(bitsAtIndex(i)) }
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sliceArray(indices: Collection<Int>): VLongArray<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sliceArray(indices: IntRange): VLongArray<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.take(n: Int): FlatVLongList<T> = slice(IntRange(0,n))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.takeLast(n: Int): FlatVLongList<T> = slice(IntRange(size-n,size))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.takeLastWhile(crossinline predicate: (T) -> Boolean): FlatVLongList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) FlatVLongList<T>(this) else slice(IntRange(0, i))}
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.takeWhile(crossinline predicate: (T) -> Boolean): FlatVLongList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) FlatVLongList<T>(this) else slice(IntRange(i,size))}
inline fun <T>VLongSequence<T>.reversed(): FlatVLongList<T> = FlatVLongList<T>(size).also {forEachBitsIndexed{i,e-> it.setBits(size-i-1, e) }}
//TODO: context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sorted(): FlatVLongList<T> = FlatVLongList<T>(this).also{it.sort()}
//TODO: context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>>VLongSequence<T>.sortedBy(crossinline selector: (T) -> R?): FlatVLongList<T> = FlatVLongList<T>(this).also{it.sortedBy(selector)}
//TODO: context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>>VLongSequence<T>.sortedByDescending(crossinline selector: (T) -> R?): VLongList<T> = FlatVLongList<T>(this).also{it.sortedBy(selector)}
//TODO: context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>>VLongSequence<T>.sortedDescending(): VLongList<T>
//TODO: context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sortedWith(comparator: Comparator<in T>): VLongList<T>
inline fun <T, C: MutableVLongSequence<T>> VLongSequence<T>.copyIntoDefault(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{for(i in startIndex..endIndex) destination.setBits(i+destinationOffset, bitsAtIndex(i))}
context(a: ValueLongAdapter<T>) inline fun <T, C: MutableList<T>>VLongSequence<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{for(i in startIndex..endIndex) destination.set(i+destinationOffset, elementAtIndex(i))}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V>VLongSequence<T>.associateVIntInt(crossinline transform: (T) -> VIntIntPair<K, V>): VIntIntMap<K, V> = associateTo(MutableVIntIntMap(size), transform)
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V>VLongSequence<T>.associateVIntLong(crossinline transform: (T) -> VIntLongPair<K, V>): VIntLongMap<K, V> = associateTo(MutableVIntLongMap(size), transform)
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V>VLongSequence<T>.associateVLongInt(crossinline transform: (T) -> VLongIntPair<K, V>): VLongIntMap<K, V> = associateTo(MutableVLongIntMap(size), transform)
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V>VLongSequence<T>.associateVLongLong(crossinline transform: (T) -> VLongLongPair<K, V>): VLongLongMap<K, V> = associateTo(MutableVLongLongMap(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, K, V>VLongSequence<T>.associateGeneric(crossinline transform: (T) -> Pair<K, V>): Map<K, V> = associateTo(HashMap<K,V>(size), transform)
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>) inline fun <T, K>VLongSequence<T>.associateByVInt(crossinline keySelector: (T) -> K): MutableVIntLongMap<K, T> = associateByTo(MutableVIntLongMap<K,T>(size),keySelector,{it})
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>) inline fun <T, K>VLongSequence<T>.associateByVLong(crossinline keySelector: (T) -> K): MutableVLongLongMap<K, T> = associateByTo(MutableVLongLongMap<K,T>(size),keySelector,{it})
context(a: ValueLongAdapter<T>) inline fun <T, K>VLongSequence<T>.associateByGeneric(crossinline keySelector: (T) -> K): Map<K, T> = HashMap<K,T>(size).also{c->forEach {c.put(keySelector(it),it)}}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V>VLongSequence<T>.associateByVIntInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): VIntIntMap<K, V> = associateByTo(MutableVIntIntMap(size),keySelector,valueTransform)
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V>VLongSequence<T>.associateByVIntLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): VIntLongMap<K, V> = associateByTo(MutableVIntLongMap(size),keySelector,valueTransform)
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V>VLongSequence<T>.associateByVLongInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): VLongIntMap<K, V> = associateByTo(MutableVLongIntMap(size),keySelector,valueTransform)
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V>VLongSequence<T>.associateByVLongLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): VLongLongMap<K, V> = associateByTo(MutableVLongLongMap(size),keySelector,valueTransform)
context(a: ValueLongAdapter<T>) inline fun <T, K, V>VLongSequence<T>.associateByGeneric(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): Map<K, V> = HashMap<K,V>(size).also{c->forEach {c.put(keySelector(it),valueTransform(it))}}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVIntIntMap<K,V>>VLongSequence<T>.associateByTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVIntLongMap<K,V>>VLongSequence<T>.associateByTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVLongIntMap<K,V>>VLongSequence<T>.associateByTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVLongLongMap<K,V>>VLongSequence<T>.associateByTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueLongAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>>VLongSequence<T>.associateByTo(destination: M, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): M = destination.also{c->forEach {c.put(keySelector(it),valueTransform(it))}}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVIntIntMap<K,V>>VLongSequence<T>.associateTo(destination: C, crossinline transform: (T) -> VIntIntPair<K, V>): C = destination.also{c->c.putAll(this,transform)}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVIntLongMap<K,V>>VLongSequence<T>.associateTo(destination: C, crossinline transform: (T) -> VIntLongPair<K, V>): C = destination.also{c->c.putAll(this,transform)}
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVLongIntMap<K,V>>VLongSequence<T>.associateTo(destination: C, crossinline transform: (T) -> VLongIntPair<K, V>): C = destination.also{c->c.putAll(this,transform)}
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVLongLongMap<K,V>>VLongSequence<T>.associateTo(destination: C, crossinline transform: (T) -> VLongLongPair<K, V>): C = destination.also{c->c.putAll(this,transform)}
context(a: ValueLongAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>>VLongSequence<T>.associateTo(destination: M, crossinline transform: (T) -> Pair<K, V>): M = destination.also{c->forEach {val p=transform(it); c[p.first] = p.second}}
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongCollection<T>>VLongSequence<T>.toCollection(destination: C): C = destination.also { it.addAll(this) }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollection<T>>VLongSequence<T>.toCollection(destination: C): C = destination.also{c->forEach {c.add(it)}}
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.toHashSet(): HashSet<T> = toCollection(HashSet(size))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.toList(): VLongList<T> = this as? VLongList<T> ?: toMutableList()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.toListGeneric(): List<T> = toMutableListGeneric()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.toMutableList(): FlatVLongList<T> = this as? FlatVLongList<T> ?: toCollection(FlatVLongList<T>(size))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.toMutableListGeneric(): MutableList<T> = toCollection(ArrayList(size))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.toSet(): VLongSet<T> = this as? VLongSet<T> ?: toMutableSet()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.toMutableSet(): MutableVLongSet<T> = this as? MutableVLongSet<T> ?: toCollection(FlatVLongSet(size))
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.toSetGeneric(): Set<T> = toHashSet()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.toLongArray(): LongArray =  (this as? VLongArray<T>)?.collection ?: throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.toVLongArray(): VLongArray<T> = this as? VLongArray<T> ?: throw NotImplementedError()
inline fun <T>VLongSequence<T>.toArrayGenericBits(): Array<LongBits> = (this as? VLongArray<T>)?.collection?.toTypedArray() ?: throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.asSequence(): Sequence<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.asList(): VLongList<T> = toList()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.asListGeneric(): List<T> = toListGeneric()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.contentEquals(other: VLongSequence<T>?): Boolean = other != null && size == other.size && this.indexOfFirstIndexedBits {i,e-> other.bitsAtIndex(i) != e } == -1
/*
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.flatMap(crossinline transform: (T) ->VLongCollection<R>): List<R>
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.flatMap(crossinline transform: (T) -> Sequence<R>): List<R>
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.flatMapIndexed(crossinline transform: (index: Int, T) ->VLongCollection<R>): List<R>
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.flatMapIndexed(crossinline transform: (index: Int, T) -> Sequence<R>): List<R>
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>>VLongSequence<T>.flatMapIndexedTo(destination: C, crossinline transform: (index: Int, T) ->VLongCollection<R>): C
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>>VLongSequence<T>.flatMapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> Sequence<R>): C
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>>VLongSequence<T>.flatMapTo(destination: C, crossinline transform: (T) ->VLongCollection<R>): C
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>>VLongSequence<T>.flatMapTo(destination: C, crossinline transform: (T) -> Sequence<R>): C
context(a: ValueLongAdapter<T>) inline fun <T, K>VLongSequence<T>.groupByVLong(crossinline keySelector: (T) -> K): MutableVLongObjectMap<K, List<T>> = groupByTo(MutableVLongObjectMap<K,List<T>>(), keySelector)
context(a: ValueLongAdapter<T>) inline fun <T, K>VLongSequence<T>.groupByVLong(crossinline keySelector: (T) -> K): MutableVLongObjectMap<K, List<T>> = groupByTo(MutableVLongObjectMap<K,List<T>>(), keySelector)
context(a: ValueLongAdapter<T>) inline fun <T, K>VLongSequence<T>.groupByGeneric(crossinline keySelector: (T) -> K): MutableMap<K, List<T>> = groupByTo(HashMap<K,List<T>>(), keySelector)
context(a: ValueLongAdapter<T>) inline fun <T, K, V>VLongSequence<T>.groupByVLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableVLongObjectMap<K, List<V>>
context(a: ValueLongAdapter<T>) inline fun <T, K, V>VLongSequence<T>.groupByVLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableVLongObjectMap<K, List<V>>
context(a: ValueLongAdapter<T>) inline fun <T, K, V>VLongSequence<T>.groupByGeneric(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableMap<K, List<V>>
context(a: ValueLongAdapter<T>) inline fun <T, K, M : MutableMap<in K, MutableList<T>>>VLongSequence<T>.groupByTo(destination: M, crossinline keySelector: (T) -> K): M
context(a: ValueLongAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, MutableList<V>>>VLongSequence<T>.groupByTo(destination: M, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): M
context(a: ValueLongAdapter<T>) inline fun <T, K>VLongSequence<T>.groupingBy(crossinline keySelector: (T) -> K): Grouping<T, K>
 */

context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R>VLongSequence<T>.mapVInt(crossinline transform: (T) -> R): FlatVIntList<R> = mapTo(FlatVIntList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R>VLongSequence<T>.mapVLong(crossinline transform: (T) -> R): FlatVLongList<R> = mapTo(FlatVLongList<R>(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.mapGeneric(crossinline transform: (T) -> R): MutableList<R> = mapTo(ArrayList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R>VLongSequence<T>.mapIndexedVInt(crossinline transform: (index: Int, T) -> R): FlatVIntList<R> = mapIndexedTo(FlatVIntList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R>VLongSequence<T>.mapIndexedVLong(crossinline transform: (index: Int, T) -> R): FlatVLongList<R> = mapIndexedTo(FlatVLongList<R>(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.mapIndexedGeneric(crossinline transform: (index: Int, T) -> R): List<R> = mapIndexedTo(ArrayList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R>VLongSequence<T>.mapIndexedVIntNotNull(crossinline transform: (index: Int, T) -> R?): FlatVIntList<R> = mapIndexedNotNullTo(FlatVIntList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R>VLongSequence<T>.mapIndexedVLongNotNull(crossinline transform: (index: Int, T) -> R?): FlatVLongList<R> = mapIndexedNotNullTo(FlatVLongList<R>(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.mapIndexedGenericNotNull(crossinline transform: (index: Int, T) -> R?): List<R> = mapIndexedNotNullTo(ArrayList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntSequence<R>>VLongSequence<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>>VLongSequence<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>>VLongSequence<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntSequence<R>>VLongSequence<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = throw NotImplementedError()
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>>VLongSequence<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>>VLongSequence<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.mapNotNull(crossinline transform: (T) -> R?): List<R> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>>VLongSequence<T>.mapNotNullTo(destination: C, crossinline transform: (T) -> R?): C = throw NotImplementedError()
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntSequence<R>>VLongSequence<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongSequence<R>>VLongSequence<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>>VLongSequence<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.withIndex():VLongCollection<IndexedValue<T>> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.distinct(): VLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, K>VLongSequence<T>.distinctBy(selector: (T) -> K): VLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline infix fun <T>VLongSequence<T>.intersect(other:VLongCollection<T>): Set<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline infix fun <T>VLongSequence<T>.subtract(other:VLongCollection<T>): Set<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline infix fun <T>VLongSequence<T>.union(other:VLongCollection<T>): Set<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.all(crossinline predicate: (T) -> Boolean): Boolean = indexOfFirst(predicate) != -1
inline fun <T>VLongSequence<T>.allBits(noinline predicate: (LongBits) -> Boolean): Boolean = indexOfFirstBits(predicate) != -1
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.any(): Boolean = size > 0
inline fun <T>VLongSequence<T>.anyBits(noinline predicate: (LongBits) -> Boolean): Boolean = indexOfFirstBits(predicate) > -1
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.count(): LongBits = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.count(crossinline predicate: (T) -> Boolean): LongBits = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.fold(initial: R, operation: (acc: R, T) -> R): R = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.foldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): R = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, C: VLongSequence<T>> C.onEach(crossinline action: (T) -> Unit): C = apply{forEach(action)}
context(a: ValueLongAdapter<T>) inline fun <T, C: VLongSequence<T>> C.onEachIndexed(crossinline action: (Int,T) -> Unit): C = apply{forEachIndexed(action)}
inline fun <T>VLongSequence<T>.forEachBits(crossinline action: (LongBits) -> Unit) {for(i in 0..size) action(bitsAtIndex(i))}
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.forEachIndexed(crossinline action: (index:Int, T) -> Unit) {for(i in 0..size) action(i, elementAtIndex(i))}
inline fun <T>VLongSequence<T>.forEachBitsIndexed(crossinline action: (index:Int, LongBits) -> Unit) {for(i in 0..size) action(i, bitsAtIndex(i))}
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>>VLongSequence<T>.max(): T = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>>VLongSequence<T>.maxBy(selector: (T) -> R): T = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>>VLongSequence<T>.maxByOrNull(selector: (T) -> R): T? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.maxOf(selector: (T) -> Double): Double = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.maxOf(selector: (T) -> Float): Float = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>>VLongSequence<T>.maxOf(selector: (T) -> R): R = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.maxOfOrNull(selector: (T) -> Double): Double? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.maxOfOrNull(selector: (T) -> Float): Float? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>>VLongSequence<T>.maxOfOrNull(selector: (T) -> R): R? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.maxOfWith(comparator: Comparator<R>, selector: (T) -> R): R = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.maxOfWithOrNull(comparator: Comparator<R>, selector: (T) -> R): R? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>>VLongSequence<T>.maxOrNull(): T? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.maxWith(comparator: Comparator<in T>): T = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.maxWithOrNull(comparator: Comparator<in T>): T? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>>VLongSequence<T>.min(): T = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>>VLongSequence<T>.minBy(selector: (T) -> R): T = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>>VLongSequence<T>.minByOrNull(selector: (T) -> R): T? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.minOf(selector: (T) -> Double): Double = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.minOf(selector: (T) -> Float): Float = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>>VLongSequence<T>.minOf(selector: (T) -> R): R = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.minOfOrNull(selector: (T) -> Double): Double? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.minOfOrNull(selector: (T) -> Float): Float? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>>VLongSequence<T>.minOfOrNull(selector: (T) -> R): R? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.minOfWith(comparator: Comparator<R>, selector: (T) -> R): R = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.minOfWithOrNull(comparator: Comparator<R>, selector: (T) -> R): R? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>>VLongSequence<T>.minOrNull(): T? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.minWith(comparator: Comparator<in T>): T = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.minWithOrNull(comparator: Comparator<in T>): T? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.none(): Boolean = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.none(crossinline predicate: (T) -> Boolean): Boolean = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <S, T : S>VLongSequence<T>.reduce(operation: (acc: S, T) -> S): S = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <S, T : S>VLongSequence<T>.reduceIndexed(operation: (index: Int, acc: S, T) -> S): S = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <S, T : S>VLongSequence<T>.reduceIndexedOrNull(operation: (index: Int, acc: S, T) -> S): S? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <S, T : S>VLongSequence<T>.reduceOrNull(operation: (acc: S, T) -> S): S? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <S, T : S>VLongSequence<T>.reduceRight(operation: (T, acc: T) -> T): T = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <S, T : S>VLongSequence<T>.reduceRightIndexed(operation: (index: Int, T, acc: T) -> T): T = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <S, T : S>VLongSequence<T>.reduceRightIndexedOrNull(operation: (index: Int, T, acc: T) -> T): T? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <S, T : S>VLongSequence<T>.reduceRightOrNull(operation: (T, acc: T) -> T): T? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.runningFold(initial: R, operation: (acc: R, T) -> R): List<R> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.runningFoldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <S, T : S>VLongSequence<T>.runningReduce(operation: (acc: S, T) -> S): List<S> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <S, T : S>VLongSequence<T>.runningReduceIndexed(operation: (index: Int, acc: S, T) -> S): List<S> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.scan(initial: R, operation: (acc: R, T) -> R): List<R> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.scanIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.shuffle(): Unit = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.shuffle(random: Random): Unit = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sorted(): FlatVLongList<T> = toMutableList().also{it.sort()}
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sortedArray(): VLongArray<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sortedArrayDescending(): VLongArray<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>>VLongSequence<T>.sortedBy(crossinline selector: (T) -> R?): FlatVLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>>VLongSequence<T>.sortedByDescending(crossinline selector: (T) -> R?): FlatVLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sortedDescending(): FlatVLongList<T> = toMutableList().also{it.sortDescending()}
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sortedWith(comparator: Comparator<in LongBits>): FlatVLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sumBy(selector: (T) -> LongBits): LongBits = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sumByDouble(selector: (T) -> Double): Double = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sumOf(selector: (T) -> Double): Double = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sumOf(selector: (T) -> Int): Int = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sumOf(selector: (T) -> LongBits): LongBits = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sumOfUInt(selector: (T) -> UInt): UInt = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.sumOfULong(selector: (T) -> ULong): ULong = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.chunked(size: LongBits): List<List<T>> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.chunked(size: LongBits, crossinline transform: (List<T>) -> R): List<R> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.minus(element: T): VLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.minus(elements: Array<out T>): VLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.minus(elements:VLongCollection<T>): VLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.minus(elements: Sequence<T>): VLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.minusElement(element: T): VLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.partition(crossinline predicate: (T) -> Boolean): Pair<List<T>, List<T>> = throw NotImplementedError()
inline fun <T> ModifiableVLongCollection<T>.random(): T = throw NotImplementedError()
fun <T> ModifiableVLongCollection<T>.random(random: Random): T = throw NotImplementedError()
inline fun <T> ModifiableVLongCollection<T>.randomOrNull(): T? = throw NotImplementedError()
fun <T> ModifiableVLongCollection<T>.randomOrNull(random: Random): T? = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.plus(element: T): VLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.plus(elements: Array<out T>): VLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.plus(elements:VLongCollection<T>): VLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline operator fun <T>VLongSequence<T>.plus(elements: Sequence<T>): VLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.plusElement(element: T): VLongList<T> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.windowed(size: LongBits, step: LongBits = 1, partialWindows: Boolean = false): List<List<T>> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.windowed(size: LongBits, step: LongBits = 1, partialWindows: Boolean = false, crossinline transform: (List<T>) -> R): List<R> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline infix fun <T, R>VLongSequence<T>.zip(other: Array<out R>): List<Pair<T, R>> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R, V>VLongSequence<T>.zip(other: Array<out R>, crossinline transform: (a: T, b: R) -> V): List<V> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline infix fun <T, R>VLongSequence<T>.zip(other:VLongCollection<R>): List<Pair<T, R>> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R, V>VLongSequence<T>.zip(other:VLongCollection<R>, crossinline transform: (a: T, b: R) -> V): List<V> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.zipWithNext(): List<Pair<T, T>> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, R>VLongSequence<T>.zipWithNext(crossinline transform: (a: T, b: T) -> R): List<R> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T, A : Appendable>VLongSequence<T>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: LongBits = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence) = { it.toString() }): A = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: LongBits = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence) = { it.toString() }): String = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T>VLongSequence<T>.toVString() = joinToString(", ","{","}")




// can modify elements, but not add or remove
interface ModifiableVLongSequence<T>: VLongSequence<T>, ModifiableVLongCollection<T> {
    fun setBits(index: Int, bits: LongBits)
}
context(a: ValueLongAdapter<T>) inline fun <T> ModifiableVLongSequence<T>.set(index: Int, value: T) = setBits(index, a.toLong(value))
inline fun <T> ModifiableVLongSequence<T>.sort(): Unit = throw NotImplementedError()
inline fun <T> ModifiableVLongSequence<T>.sort(fromIndex: Int, toIndex: Int): Unit = throw NotImplementedError()
inline fun <T> ModifiableVLongSequence<T>.sortDescending(): Unit = throw NotImplementedError()
inline fun <T> ModifiableVLongSequence<T>.sortDescending(fromIndex: Int, toIndex: Int): Unit = throw NotImplementedError()





// can modify, add, and remove elements
interface MutableVLongSequence<T>: ModifiableVLongSequence<T>, MutableVLongCollection<T> {
    fun addBits(index: Int, bits: LongBits)
    fun addAll(index: Int, elements: VLongCollection<T>): Boolean
    context(a: ValueLongAdapter<T>) fun addAll(index: Int, elements: Collection<T>): Boolean

    fun removeAt(index: Int): Boolean
    fun removeAllIndexedBits(predicate: (index: Int, bits: LongBits) -> Boolean): Boolean
}
operator fun <T> MutableVLongCollection<T>.plus(element: T): VLongList<T> = throw NotImplementedError()
operator fun <T> MutableVLongCollection<T>.plus(elements: Iterable<T>): VLongList<T> = throw NotImplementedError()
operator fun <T> MutableVLongCollection<T>.plus(elements: Sequence<T>): VLongList<T> = throw NotImplementedError()
inline fun <T> MutableVLongCollection<T>.plusElement(element: T): VLongList<T>  = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.addAll(elements: LongArray): Boolean = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.addAll(elements: Array<out T>): Boolean = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.addAll(elements: Iterable<T>): Boolean = throw NotImplementedError()
inline operator fun <T> MutableVLongCollection<T>.plusAssign(elements: LongList): Unit = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableVLongCollection<T>.plusAssign(elements: LongArray): Unit = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.plusAssign(elements: Array<out T>): Boolean = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.plusAssign(elements: Collection<T>): Boolean = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.plusAssign(elements: Iterable<T>): Boolean = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableVLongCollection<T>.plusAssign(element: T): Unit = throw NotImplementedError()

context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.add(index: Int, element: T): Unit = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.addAll(index: Int, elements: LongArray): Boolean = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.addAll(index: Int, elements: VLongCollection<T>) : Boolean= throw NotImplementedError()
inline fun <T> MutableVLongCollection<T>.addAll(index: Int, elements: LongList): Boolean = throw NotImplementedError()

context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.remove(element: T): Boolean = throw NotImplementedError()
inline fun <T> MutableVLongCollection<T>.removeBits(element: LongBits): Boolean = throw NotImplementedError()
inline fun <T> MutableVLongCollection<T>.removeAll(elements: VLongCollection<T>): Boolean = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.removeAll(elements: Collection<T>): Boolean = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableVLongCollection<T>.minusAssign(element: T): Unit = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.removeAll(elements: VLongList<out T>): Boolean= throw NotImplementedError()
inline fun <T> MutableVLongCollection<T>.removeAll(elements: LongList): Boolean= throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.removeAll(elements: LongArray): Boolean= throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.removeAll(elements: Array<out T>): Boolean= throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.removeAll(elements: Iterable<T>): Boolean= throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.minusAssign(elements: VLongList<out T>): Boolean= throw NotImplementedError()
inline fun <T> MutableVLongCollection<T>.minusAssign(elements: LongList): Boolean= throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.minusAssign(elements: LongArray): Boolean= throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.minusAssign(elements: Array<out T>): Boolean= throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.minusAssign(elements: Collection<T>): Boolean= throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.minusAssign(elements: Iterable<T>): Boolean= throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.removeAt(index: Int): Boolean= throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.removeRange(start: LongBits, end: LongBits): Boolean= throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.retainAll(elements: LongArray): Boolean= throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.retainAll(elements: VLongList<out T>): Boolean= throw NotImplementedError()
inline fun <T> MutableVLongCollection<T>.retainAll(elements: LongList): Boolean= throw NotImplementedError()

