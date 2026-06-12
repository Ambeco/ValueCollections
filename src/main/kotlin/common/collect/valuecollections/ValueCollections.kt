@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused")

package mpd.com.common.collect.valuecollections

import androidx.collection.IntList
import java.util.BitSet
import kotlin.also
import kotlin.collections.set
import kotlin.random.Random

//
// 
// The collections are not directly :Iterable<T> because constructing the iterator requires a `context(a: ValueIntAdapter<T>)`
// because iterators use a lot of magic and I can't add context(a: ValueIntAdapter<T>) to the getNext itself
interface VIntCollection<T> {
    val size: Int
    fun bitsAtIndex(index: Int): Int
    
    fun indexOfBits(bits: Int): Int
    fun indexOfFirstIndexedBits(predicate: (index:Int, bits:Int) -> Boolean): Int = indexOfFirstIndexedBitsDefault(predicate)
    fun indexOfLastIndexedBits(predicate: (index:Int, bits:Int) -> Boolean): Int = indexOfLastIndexedBitsDefault(predicate)

    fun <C: MutableVIntCollection<T>> copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = copyIntoDefault(destination, destinationOffset, startIndex, endIndex)
    
    context(a: ValueIntAdapter<T>) fun toString(): String = toVString()
    
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}

context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.component1(): T = elementAtIndex(0)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.component2(): T = elementAtIndex(1)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.component3(): T = elementAtIndex(2)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.component4(): T = elementAtIndex(3)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.component5(): T = elementAtIndex(4)
inline fun <T> VIntCollection<T>.isEmpty() = size == 0
inline fun <T> VIntCollection<T>.isNotEmpty() = size > 0
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.contains(element: T) = indexOf(element) != -1
inline fun <T> VIntCollection<T>.containsAll(elements: VIntCollection<T>) = elements.allBits { indexOfBits(it) != -1 }
inline fun <T> VIntCollection<T>.containsAll(elements: IntList) = elements.forEach { indexOfBits(it) != -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.elementAtIndex(index: Int): T = a.fromInt(bitsAtIndex(index))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.elementAtOrNull(index: Int): T? = if(index in 0..<size) elementAtIndex(index) else null
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.elementAtOrElse(index: Int, defaultValue: (Int) -> T): T = if(index in 0..<size)elementAtIndex(index) else defaultValue(index)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.getOrElse(index: Int, defaultValue: (Int) -> T): T = if (index<size) elementAtIndex(index) else defaultValue(index)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.getOrNull(index: Int): T? = if (index<size) elementAtIndex(index) else null
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.find(predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfFirst(predicate))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.findLast(predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.first(): T = elementAtIndex(0)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.first(predicate: (T) -> Boolean): T = find(predicate) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.firstNotNullOf(transform: (T) -> R?): R = firstNotNullOfOrNull(transform) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.firstNotNullOfOrNull(transform: (T) -> R?): R? { for(i in 0 ..< size) return transform(elementAtIndex(i)) ?: continue; return null }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.firstOrNull(): T? = elementAtOrNull(0)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.firstOrNull(predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfFirst(predicate))
context(a: ValueIntAdapter<T>)fun <T> VIntCollection<T>.indexOf(element: T): Int = indexOfFirst {it==element}
inline fun <T> VIntCollection<T>.indexOfFirstBits(predicate: (Int) -> Boolean): Int { for(i in 0 ..< size) if (predicate(bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.indexOfFirst(predicate: (T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.indexOfFirstIndexed(predicate: (Int,T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T> VIntCollection<T>.indexOfFirstIndexedBitsDefault(predicate: (index:Int, bits:Int) -> Boolean): Int { for(i in 0 ..< size) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.indexOfLast(predicate: (T) -> Boolean): Int { for(i in size-1..0) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.indexOfLastIndexed(predicate: (Int,T) -> Boolean): Int { for(i in size-1..0) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T> VIntCollection<T>.indexOfLastIndexedBitsDefault(predicate: (index:Int, bits:Int) -> Boolean): Int { for(i in size-1..0) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.last(): T = elementAtIndex(size-1)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.last(predicate: (T) -> Boolean): T = findLast(predicate) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.lastIndexOf(element: T): Int = indexOfLast {it==element}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.lastOrNull(): T? = elementAtOrNull(size - 1)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.lastOrNull(predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate)) 
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.single(): T = elementAtIndex(0)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.single(predicate: (T) -> Boolean): T = elementAtIndex(indexOfFirst(predicate))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOrNull(): T? = elementAtOrNull(0)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOrNull(predicate: (T) -> Boolean): T? = if (size==1 && predicate(elementAtIndex(0))) elementAtIndex(0) else null
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.drop(n: Int): FlatVIntList<T> = slice(IntRange(n,size-1))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.dropLast(n: Int): FlatVIntList<T> = slice(IntRange(0,size-n))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.dropWhile(predicate: (T) -> Boolean): FlatVIntList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) FlatVIntList(this) else slice(IntRange(i, size))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.dropLastWhile(predicate: (T) -> Boolean): FlatVIntList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(0, i))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.filter(predicate: (T) -> Boolean): FlatVIntList<T> = filterFromMask(filterMask(predicate))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.filterMask(predicate: (T) -> Boolean): BitSet = filterIndexedMask {_,e->predicate(e)}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.filterFromMask(mask: BitSet): FlatVIntList<T> = FlatVIntList<T>(mask.cardinality()).also {c-> forEachBitsIndexed {i,e-> if(mask[i]) c.addBits(e)} }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.filterIndexed(predicate: (index: Int, T) -> Boolean): FlatVIntList<T> = filterFromMask(filterIndexedMask(predicate))
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>> VIntCollection<T>.filterIndexedTo(destination: C, predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> VIntCollection<T>.filterIndexedTo(destination: C, predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.filterIndexedMask(predicate: (index: Int, T) -> Boolean): BitSet {val destination=BitSet(size); forEachIndexed { i, e -> destination.set(i,predicate(i, e))}; return destination }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.filterNot(predicate: (T) -> Boolean): VIntList<T> = filter {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>> VIntCollection<T>.filterNotTo(destination: C, predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> VIntCollection<T>.filterNotTo(destination: C, predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>> VIntCollection<T>.filterTo(destination: C, predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> VIntCollection<T>.filterTo(destination: C, predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.slice(indices: IntRange): FlatVIntList<T> = copyInto<FlatVIntList<T>>(FlatVIntList<T>(indices.last-indices.first), 0, indices.first, indices.last)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.slice(indices: Iterable<Int>): FlatVIntList<T> = FlatVIntList<T>(if (indices is Collection<Int>) indices.size else size/8).also { for(i in indices) it.addBits(bitsAtIndex(i)) }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sliceArray(indices: Collection<Int>): VIntArray<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sliceArray(indices: IntRange): VIntArray<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.take(n: Int): FlatVIntList<T> = slice(IntRange(0,n))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.takeLast(n: Int): FlatVIntList<T> = slice(IntRange(size-n,size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.takeLastWhile(predicate: (T) -> Boolean): FlatVIntList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) FlatVIntList<T>(this) else slice(IntRange(0, i))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.takeWhile(predicate: (T) -> Boolean): FlatVIntList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) FlatVIntList<T>(this) else slice(IntRange(i,size))}
inline fun <T> VIntCollection<T>.reversed(): FlatVIntList<T> = FlatVIntList<T>(size).also {forEachBitsIndexed{i,e-> it.setBits(size-i-1, e) }}
//TODO: context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sorted(): FlatVIntList<T> = FlatVIntList<T>(this).also{it.sort()}
//TODO: context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.sortedBy(crossinline selector: (T) -> R?): FlatVIntList<T> = FlatVIntList<T>(this).also{it.sortedBy(selector)}
//TODO: context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.sortedByDescending(crossinline selector: (T) -> R?): VIntList<T> = FlatVIntList<T>(this).also{it.sortedBy(selector)}
//TODO: context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.sortedDescending(): VIntList<T>
//TODO: context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sortedWith(comparator: Comparator<in T>): VIntList<T>
inline fun <T, C: MutableVIntCollection<T>> VIntCollection<T>.copyIntoDefault(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{for(i in startIndex..endIndex) destination.setBits(i+destinationOffset, bitsAtIndex(i))}
context(a: ValueIntAdapter<T>) inline fun <T, C: MutableList<T>> VIntCollection<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{for(i in startIndex..endIndex) destination.set(i+destinationOffset, elementAtIndex(i))}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateVIntInt(transform: (T) -> VIntIntPair<K, V>): VIntIntMap<K, V> = associateTo(MutableVIntIntMap(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateVIntLong(transform: (T) -> VIntLongPair<K, V>): VIntLongMap<K, V> = associateTo(MutableVIntLongMap(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateVLongInt(transform: (T) -> VLongIntPair<K, V>): VLongIntMap<K, V> = associateTo(MutableVLongIntMap(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateVLongLong(transform: (T) -> VLongLongPair<K, V>): VLongLongMap<K, V> = associateTo(MutableVLongLongMap(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V> VIntCollection<T>.associateGeneric(transform: (T) -> Pair<K, V>): Map<K, V> = associateTo(HashMap<K,V>(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>) inline fun <T, K> VIntCollection<T>.associateByVInt(keySelector: (T) -> K): MutableVIntIntMap<K, T> = associateByTo(MutableVIntIntMap<K,T>(size),keySelector,{it})
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>) inline fun <T, K> VIntCollection<T>.associateByVLong(keySelector: (T) -> K): MutableVLongIntMap<K, T> = associateByTo(MutableVLongIntMap(size),keySelector,{it})
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntCollection<T>.associateByGeneric(keySelector: (T) -> K): Map<K, T> = HashMap<K,T>(size).also{c->forEach {c.put(keySelector(it),it)}}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateByVIntInt(keySelector: (T) -> K, valueTransform: (T) -> V): VIntIntMap<K, V> = associateByTo(MutableVIntIntMap(size),keySelector,valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateByVIntLong(keySelector: (T) -> K, valueTransform: (T) -> V): VIntLongMap<K, V> = associateByTo(MutableVIntLongMap(size),keySelector,valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateByVLongInt(keySelector: (T) -> K, valueTransform: (T) -> V): VLongIntMap<K, V> = associateByTo(MutableVLongIntMap(size),keySelector,valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateByVLongLong(keySelector: (T) -> K, valueTransform: (T) -> V): VLongLongMap<K, V> = associateByTo(MutableVLongLongMap(size),keySelector,valueTransform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V> VIntCollection<T>.associateByGeneric(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, V> = HashMap<K,V>(size).also{c->forEach {c.put(keySelector(it),valueTransform(it))}}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVIntIntMap<K,V>> VIntCollection<T>.associateByTo(destination: C, keySelector: (T) -> K, valueTransform: (T) -> V): C = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVIntLongMap<K,V>> VIntCollection<T>.associateByTo(destination: C, keySelector: (T) -> K, valueTransform: (T) -> V): C = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVLongIntMap<K,V>> VIntCollection<T>.associateByTo(destination: C, keySelector: (T) -> K, valueTransform: (T) -> V): C = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVLongLongMap<K,V>> VIntCollection<T>.associateByTo(destination: C, keySelector: (T) -> K, valueTransform: (T) -> V): C = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>> VIntCollection<T>.associateByTo(destination: M, keySelector: (T) -> K, valueTransform: (T) -> V): M = destination.also{c->forEach {c.put(keySelector(it),valueTransform(it))}}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVIntIntMap<K,V>> VIntCollection<T>.associateTo(destination: C, transform: (T) -> VIntIntPair<K, V>): C = destination.also{c->c.putAll(this,transform)}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVIntLongMap<K,V>> VIntCollection<T>.associateTo(destination: C, transform: (T) -> VIntLongPair<K, V>): C = destination.also{c->c.putAll(this,transform)}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVLongIntMap<K,V>> VIntCollection<T>.associateTo(destination: C, transform: (T) -> VLongIntPair<K, V>): C = destination.also{c->c.putAll(this,transform)}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVLongLongMap<K,V>> VIntCollection<T>.associateTo(destination: C, transform: (T) -> VLongLongPair<K, V>): C = destination.also{c->c.putAll(this,transform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>> VIntCollection<T>.associateTo(destination: M, transform: (T) -> Pair<K, V>): M = destination.also{c->forEach {val p=transform(it); c[p.first] = p.second}}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>> VIntCollection<T>.toCollection(destination: C): C = destination.also { it.addAll(this) }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> VIntCollection<T>.toCollection(destination: C): C = destination.also{c->forEach {c.add(it)}}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toHashSet(): HashSet<T> = toCollection(HashSet(size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toList(): VIntList<T> = this as? VIntList<T> ?: toMutableList()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toListGeneric(): List<T> = toMutableListGeneric()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toMutableList(): FlatVIntList<T> = if (this is FlatVIntList<T>) this else toCollection(FlatVIntList<T>(size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toMutableListGeneric(): MutableList<T> = toCollection(ArrayList(size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toSet(): VIntSet<T> = this as? VIntSet<T> ?: toMutableSet()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toMutableSet(): MutableVIntSet<T> = this as? MutableVIntSet<T> ?: toCollection(FlatVIntSet(size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toSetGeneric(): Set<T> = toHashSet()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toIntArray(): IntArray =  (this as? VIntArray<T>)?.collection ?: throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toVIntArray(): VIntArray<T> = this as? VIntArray<T> ?: throw NotImplementedError()
inline fun <T> VIntCollection<T>.toArrayGenericBits(): Array<Int> = (this as? VIntArray<T>)?.collection?.toTypedArray() ?: throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.asSequence(): Sequence<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.asList(): VIntList<T> = toList()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.asListGeneric(): List<T> = toListGeneric()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.asIterable(): VIntIterator<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.contentEquals(other: VIntCollection<T>?): Boolean = other != null && size == other.size && this.indexOfFirstIndexedBits {i,e-> other.bitsAtIndex(i) != e } == -1
/*
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.flatMap(transform: (T) ->VIntCollection<R>): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.flatMap(transform: (T) -> Sequence<R>): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.flatMapIndexed(transform: (index: Int, T) ->VIntCollection<R>): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.flatMapIndexed(transform: (index: Int, T) -> Sequence<R>): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.flatMapIndexedTo(destination: C, transform: (index: Int, T) ->VIntCollection<R>): C
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.flatMapIndexedTo(destination: C, transform: (index: Int, T) -> Sequence<R>): C
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.flatMapTo(destination: C, transform: (T) ->VIntCollection<R>): C
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.flatMapTo(destination: C, transform: (T) -> Sequence<R>): C
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntCollection<T>.groupByVInt(keySelector: (T) -> K): MutableVIntObjectMap<K, List<T>> = groupByTo(MutableVIntObjectMap<K,List<T>>(), keySelector)
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntCollection<T>.groupByVLong(keySelector: (T) -> K): MutableVLongObjectMap<K, List<T>> = groupByTo(MutableVLongObjectMap<K,List<T>>(), keySelector)
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntCollection<T>.groupByGeneric(keySelector: (T) -> K): MutableMap<K, List<T>> = groupByTo(HashMap<K,List<T>>(), keySelector)
context(a: ValueIntAdapter<T>) inline fun <T, K, V> VIntCollection<T>.groupByVInt(keySelector: (T) -> K, valueTransform: (T) -> V): MutableVIntObjectMap<K, List<V>>
context(a: ValueIntAdapter<T>) inline fun <T, K, V> VIntCollection<T>.groupByVLong(keySelector: (T) -> K, valueTransform: (T) -> V): MutableVLongObjectMap<K, List<V>>
context(a: ValueIntAdapter<T>) inline fun <T, K, V> VIntCollection<T>.groupByGeneric(keySelector: (T) -> K, valueTransform: (T) -> V): MutableMap<K, List<V>>
context(a: ValueIntAdapter<T>) inline fun <T, K, M : MutableMap<in K, MutableList<T>>> VIntCollection<T>.groupByTo(destination: M, keySelector: (T) -> K): M
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, MutableList<V>>> VIntCollection<T>.groupByTo(destination: M, keySelector: (T) -> K, valueTransform: (T) -> V): M
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntCollection<T>.groupingBy(crossinline keySelector: (T) -> K): Grouping<T, K>
 */
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntCollection<T>.mapVInt(transform: (T) -> R): FlatVIntList<R> = mapTo(FlatVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.mapVLong(transform: (T) -> R): FlatVLongList<R> = mapTo(FlatVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.mapGeneric(transform: (T) -> R): MutableList<R> = mapTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntCollection<T>.mapIndexedVInt(transform: (index: Int, T) -> R): FlatVIntList<R> = mapIndexedTo(FlatVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.mapIndexedVLong(transform: (index: Int, T) -> R): FlatVLongList<R> = mapIndexedTo(FlatVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.mapIndexedGeneric(transform: (index: Int, T) -> R): List<R> = mapIndexedTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntCollection<T>.mapIndexedVIntNotNull(transform: (index: Int, T) -> R?): FlatVIntList<R> = mapIndexedNotNullTo(FlatVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.mapIndexedVLongNotNull(transform: (index: Int, T) -> R?): FlatVLongList<R> = mapIndexedNotNullTo(FlatVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.mapIndexedGenericNotNull(transform: (index: Int, T) -> R?): List<R> = mapIndexedNotNullTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.mapIndexedNotNullTo(destination: C, transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.mapIndexedNotNullTo(destination: C, transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.mapIndexedNotNullTo(destination: C, transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.mapIndexedTo(destination: C, transform: (index: Int, T) -> R): C = throw NotImplementedError()
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.mapIndexedTo(destination: C, transform: (index: Int, T) -> R): C = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.mapIndexedTo(destination: C, transform: (index: Int, T) -> R): C = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.mapNotNull(transform: (T) -> R?): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.mapNotNullTo(destination: C, transform: (T) -> R?): C = throw NotImplementedError()
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.mapTo(destination: C, transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.mapTo(destination: C, transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.mapTo(destination: C, transform: (T) -> R): C = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.withIndex():VIntCollection<IndexedValue<T>> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.distinct(): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntCollection<T>.distinctBy(selector: (T) -> K): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline infix fun <T> VIntCollection<T>.intersect(other:VIntCollection<T>): Set<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline infix fun <T> VIntCollection<T>.subtract(other:VIntCollection<T>): Set<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline infix fun <T> VIntCollection<T>.union(other:VIntCollection<T>): Set<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.all(predicate: (T) -> Boolean): Boolean = indexOfFirst(predicate) != -1
inline fun <T> VIntCollection<T>.allBits(noinline predicate: (Int) -> Boolean): Boolean = indexOfFirstBits(predicate) != -1
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.any(): Boolean = size > 0
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.any(predicate: (T) -> Boolean): Boolean = indexOfFirst(predicate) > -1
inline fun <T> VIntCollection<T>.anyBits(noinline predicate: (Int) -> Boolean): Boolean = indexOfFirstBits(predicate) > -1
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.count(): Int = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.count(predicate: (T) -> Boolean): Int = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.fold(initial: R, operation: (acc: R, T) -> R): R = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.foldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): R = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, C: VIntCollection<T>> C.onEach(action: (T) -> Unit): C = apply{forEach(action)}
context(a: ValueIntAdapter<T>) inline fun <T, C: VIntCollection<T>> C.onEachIndexed(action: (Int,T) -> Unit): C = apply{forEachIndexed(action)}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.forEach(action: (T) -> Unit) {for(i in 0..size) action(elementAtIndex(i))}
inline fun <T> VIntCollection<T>.forEachBits(action: (Int) -> Unit) {for(i in 0..size) action(bitsAtIndex(i))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.forEachIndexed(action: (index:Int, T) -> Unit) {for(i in 0..size) action(i, elementAtIndex(i))}
inline fun <T> VIntCollection<T>.forEachBitsIndexed(action: (index:Int, Int) -> Unit) {for(i in 0..size) action(i, bitsAtIndex(i))}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.max(): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.maxBy(selector: (T) -> R): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.maxByOrNull(selector: (T) -> R): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.maxOf(selector: (T) -> Double): Double = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.maxOf(selector: (T) -> Float): Float = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.maxOf(selector: (T) -> R): R = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.maxOfOrNull(selector: (T) -> Double): Double? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.maxOfOrNull(selector: (T) -> Float): Float? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.maxOfOrNull(selector: (T) -> R): R? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.maxOfWith(comparator: Comparator<R>, selector: (T) -> R): R = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.maxOfWithOrNull(comparator: Comparator<R>, selector: (T) -> R): R? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.maxOrNull(): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.maxWith(comparator: Comparator<in T>): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.maxWithOrNull(comparator: Comparator<in T>): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.min(): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.minBy(selector: (T) -> R): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.minByOrNull(selector: (T) -> R): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minOf(selector: (T) -> Double): Double = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minOf(selector: (T) -> Float): Float = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.minOf(selector: (T) -> R): R = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minOfOrNull(selector: (T) -> Double): Double? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minOfOrNull(selector: (T) -> Float): Float? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.minOfOrNull(selector: (T) -> R): R? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.minOfWith(comparator: Comparator<R>, selector: (T) -> R): R = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.minOfWithOrNull(comparator: Comparator<R>, selector: (T) -> R): R? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.minOrNull(): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minWith(comparator: Comparator<in T>): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minWithOrNull(comparator: Comparator<in T>): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.none(): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.none(predicate: (T) -> Boolean): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.reduce(operation: (acc: S, T) -> S): S = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.reduceIndexed(operation: (index: Int, acc: S, T) -> S): S = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.reduceIndexedOrNull(operation: (index: Int, acc: S, T) -> S): S? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.reduceOrNull(operation: (acc: S, T) -> S): S? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.reduceRight(operation: (T, acc: T) -> T): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.reduceRightIndexed(operation: (index: Int, T, acc: T) -> T): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.reduceRightIndexedOrNull(operation: (index: Int, T, acc: T) -> T): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.reduceRightOrNull(operation: (T, acc: T) -> T): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.runningFold(initial: R, operation: (acc: R, T) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.runningFoldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.runningReduce(operation: (acc: S, T) -> S): List<S> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.runningReduceIndexed(operation: (index: Int, acc: S, T) -> S): List<S> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.scan(initial: R, operation: (acc: R, T) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.scanIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.shuffle(): Unit = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.shuffle(random: Random): Unit = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sorted(): FlatVIntList<T> = toMutableList().also{it.sort()}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sortedArray(): VIntArray<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sortedArrayDescending(): VIntArray<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.sortedBy(crossinline selector: (T) -> R?): FlatVIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.sortedByDescending(crossinline selector: (T) -> R?): FlatVIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sortedDescending(): FlatVIntList<T> = toMutableList().also{it.sortDescending()}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sortedWith(comparator: Comparator<in Int>): FlatVIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumBy(selector: (T) -> Int): Int = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumByDouble(selector: (T) -> Double): Double = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOf(selector: (T) -> Double): Double = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOf(selector: (T) -> Int): Int = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOf(selector: (T) -> Long): Long = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOfUInt(selector: (T) -> UInt): UInt = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOfULong(selector: (T) -> ULong): ULong = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.chunked(size: Int): List<List<T>> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.chunked(size: Int, transform: (List<T>) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.minus(element: T): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.minus(elements: Array<out T>): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.minus(elements:VIntCollection<T>): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.minus(elements: Sequence<T>): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minusElement(element: T): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.partition(predicate: (T) -> Boolean): Pair<List<T>, List<T>> = throw NotImplementedError()
inline fun <T> ModifiableVIntCollection<T>.random(): T = throw NotImplementedError()
fun <T> ModifiableVIntCollection<T>.random(random: Random): T = throw NotImplementedError()
inline fun <T> ModifiableVIntCollection<T>.randomOrNull(): T? = throw NotImplementedError()
fun <T> ModifiableVIntCollection<T>.randomOrNull(random: Random): T? = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.plus(element: T): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.plus(elements: Array<out T>): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.plus(elements:VIntCollection<T>): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.plus(elements: Sequence<T>): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.plusElement(element: T): VIntList<T> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.windowed(size: Int, step: Int = 1, partialWindows: Boolean = false): List<List<T>> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.windowed(size: Int, step: Int = 1, partialWindows: Boolean = false, transform: (List<T>) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline infix fun <T, R> VIntCollection<T>.zip(other: Array<out R>): List<Pair<T, R>> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R, V> VIntCollection<T>.zip(other: Array<out R>, transform: (a: T, b: R) -> V): List<V> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline infix fun <T, R> VIntCollection<T>.zip(other:VIntCollection<R>): List<Pair<T, R>> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R, V> VIntCollection<T>.zip(other:VIntCollection<R>, transform: (a: T, b: R) -> V): List<V> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.zipWithNext(): List<Pair<T, T>> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.zipWithNext(transform: (a: T, b: T) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T, A : Appendable> VIntCollection<T>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence) = { it.toString() }): A = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence) = { it.toString() }): String = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toVString() = joinToString(", ","{","}")



// can modify elements, but not add or remove
interface ModifiableVIntCollection<T>: VIntCollection<T> {
    fun setBits(index: Int, bits: Int)
}
context(a: ValueIntAdapter<T>) inline fun <T> ModifiableVIntCollection<T>.set(index: Int, value: T) = setBits(index, a.toInt(value))
inline fun <T> ModifiableVIntCollection<T>.sort(): Unit = throw NotImplementedError()
inline fun <T> ModifiableVIntCollection<T>.sort(fromIndex: Int, toIndex: Int): Unit = throw NotImplementedError()
inline fun <T> ModifiableVIntCollection<T>.sortDescending(): Unit = throw NotImplementedError()
inline fun <T> ModifiableVIntCollection<T>.sortDescending(fromIndex: Int, toIndex: Int): Unit = throw NotImplementedError()



// can modify, add, and remove elements
interface MutableVIntCollection<T>: ModifiableVIntCollection<T> {
    fun ensureCapacity(capacity: Int)
    fun trim(minCapacity: Int)
    
    fun addBits(element: Int): Boolean
    fun addAll(elements: VIntCollection<T>): Boolean
    context(a: ValueIntAdapter<T>) fun addAll(elements: Collection<T>): Boolean
    
    fun addBits(index: Int, bits: Int)
    fun addAll(index: Int, elements: VIntCollection<T>): Boolean
    context(a: ValueIntAdapter<T>) fun addAll(index: Int, elements: Collection<T>): Boolean

    fun removeAt(index: Int): Boolean
    fun removeAllIndexedBits(predicate: (index: Int, bits: Int) -> Boolean): Boolean
    fun clear()
}
context(a: ValueIntAdapter<T>) fun <T> MutableVIntCollection<T>.add(element: T): Boolean = throw NotImplementedError()
operator fun <T> MutableVIntCollection<T>.plus(element: T): VIntList<T> = throw NotImplementedError()
operator fun <T> MutableVIntCollection<T>.plus(elements: Iterable<T>): VIntList<T> = throw NotImplementedError()
operator fun <T> MutableVIntCollection<T>.plus(elements: Sequence<T>): VIntList<T> = throw NotImplementedError()
inline fun <T> MutableVIntCollection<T>.plusElement(element: T): VIntList<T>  = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.addAll(elements: IntArray): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.addAll(elements: Array<out T>): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.addAll(elements: Iterable<T>): Boolean = throw NotImplementedError()
inline operator fun <T> MutableVIntCollection<T>.plusAssign(elements: IntList): Unit = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntCollection<T>.plusAssign(elements: IntArray): Unit = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.plusAssign(elements: Array<out T>): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.plusAssign(elements: Collection<T>): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.plusAssign(elements: Iterable<T>): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntCollection<T>.plusAssign(element: T): Unit = throw NotImplementedError()

inline fun <T> MutableVIntCollection<T>.add(index: Int, element: T): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.addAll(index: Int, elements: IntArray): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.addAll(index: Int, elements: VIntCollection<T>) : Boolean= throw NotImplementedError()
inline fun <T> MutableVIntCollection<T>.addAll(index: Int, elements: IntList): Boolean = throw NotImplementedError()

context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.remove(element: T): Boolean = throw NotImplementedError()
inline fun <T> MutableVIntCollection<T>.removeBits(element: Int): Boolean = throw NotImplementedError()
inline fun <T> MutableVIntCollection<T>.removeAll(elements: VIntCollection<T>): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.removeAll(elements: Collection<T>): Boolean = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntCollection<T>.minusAssign(element: T): Unit = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.removeAll(elements: VIntList<out T>): Boolean= throw NotImplementedError()
inline fun <T> MutableVIntCollection<T>.removeAll(elements: IntList): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.removeAll(elements: IntArray): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.removeAll(elements: Array<out T>): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.removeAll(elements: Iterable<T>): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.minusAssign(elements: VIntList<out T>): Boolean= throw NotImplementedError()
inline fun <T> MutableVIntCollection<T>.minusAssign(elements: IntList): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.minusAssign(elements: IntArray): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.minusAssign(elements: Array<out T>): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.minusAssign(elements: Collection<T>): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.minusAssign(elements: Iterable<T>): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.removeAt(index: Int): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.removeRange(start: Int, end: Int): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.retainAll(elements: IntArray): Boolean= throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.retainAll(elements: VIntList<out T>): Boolean= throw NotImplementedError()
inline fun <T> MutableVIntCollection<T>.retainAll(elements: IntList): Boolean= throw NotImplementedError()



interface VLongCollection<T> {
    val size: Int
    fun bitsAtIndex(index: Int): Long
    
    fun indexOfBits(bits: Long): Int
    fun indexOfFirstIndexedBits(predicate: (index:Int, bits:Long) -> Boolean): Int
    fun indexOfLastIndexedBits(predicate: (index:Int, bits:Long) -> Boolean): Int

    fun <C: MutableVLongCollection<T>> copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C
    
    context(a: ValueLongAdapter<T>) fun toString(): String = toVString()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Longs. Use toString(ValueLongAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.toVString(): String = throw NotImplementedError()




// can modify elements, but not add or remove
interface ModifiableVLongCollection<T>: VLongCollection<T> {
    fun setBits(index: Int, bits: Long)
}





// can modify, add, and remove elements
interface MutableVLongCollection<T>: ModifiableVLongCollection<T> {
    fun ensureCapacity(capacity: Int)
    fun trim(minCapacity: Int)

    fun addBits(element: Long): Boolean
    fun addAll(elements: VLongCollection<T>): Boolean
    context(a: ValueLongAdapter<T>) fun addAll(elements: Collection<T>): Boolean

    fun addBits(index: Int, bits: Long)
    fun addAll(index: Int, elements: VLongCollection<T>): Boolean
    context(a: ValueLongAdapter<T>) fun addAll(index: Int, elements: Collection<T>): Boolean

    fun removeAt(index: Int): Boolean
    fun removeAllIndexedBits(predicate: (index: Int, bits: Long) -> Boolean): Boolean
    fun clear()
}
context(a: ValueLongAdapter<T>) fun <T> MutableVLongCollection<T>.add(element: T): Boolean = throw NotImplementedError()
