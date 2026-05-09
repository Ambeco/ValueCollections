@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import java.util.BitSet
import java.util.Collections.addAll
import kotlin.also
import kotlin.collections.set


interface VIntCollection<T> {
    context(a: ValueIntAdapter<T>) fun asIterable(): VIntIterator<T>
    val size: Int
    fun isEmpty(): Boolean
    context(a: ValueIntAdapter<T>) operator fun contains(element: @UnsafeVariance T): Boolean
    context(a: ValueIntAdapter<T>) fun containsAll(elements: VIntCollection<@UnsafeVariance T>): Boolean
    context(a: ValueIntAdapter<T>) fun elementAtIndex(index: Int): T
    context(a: ValueIntAdapter<T>) fun bitsAtIndex(index: Int): Int
}

// Kotlin _Collections Iterator extension methods
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.elementAtOrNull(index: Int): T? = if(index in 0..<size) elementAtIndex(index) else null
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.elementAtOrElse(index: Int, defaultValue: (Int) -> T): T = if(index in 0..<size)elementAtIndex(index) else defaultValue(index)
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.find(predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfFirst(predicate))
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.findLast(predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate))
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.first(): T = elementAtIndex(0)
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.first(predicate: (T) -> Boolean): T = find(predicate) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T, R : Any>VIntCollection<T>.firstNotNullOf(transform: (T) -> R?): R = firstNotNullOfOrNull(transform) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T, R : Any>VIntCollection<T>.firstNotNullOfOrNull(transform: (T) -> R?): R? { for(i in 0 ..< size) return transform(elementAtIndex(i)) ?: continue; return null }
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.firstOrNull(): T? = elementAtOrNull(0)
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.firstOrNull(predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfFirst(predicate))
context(a: ValueIntAdapter<T>)fun <T>VIntCollection<T>.indexOf(element: T): Int = indexOfFirst {it==element}
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.indexOfFirst(predicate: (T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.indexOfLast(predicate: (T) -> Boolean): Int { for(i in size-1..0) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.last(): T = elementAtIndex(size-1)
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.last(predicate: (T) -> Boolean): T = findLast(predicate) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.lastIndexOf(element: T): Int = indexOfLast {it==element}
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.lastOrNull(): T? = elementAtOrNull(size - 1)
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.lastOrNull(predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate)) 
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.single(): T = elementAtIndex(0)
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.single(predicate: (T) -> Boolean): T = elementAtIndex(indexOfFirst(predicate))
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.singleOrNull(): T? = elementAtOrNull(0)
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.singleOrNull(predicate: (T) -> Boolean): T? = if (size==1 && predicate(elementAtIndex(0))) elementAtIndex(0) else null
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.drop(n: Int): MutableVIntList<T> = slice(IntRange(n,size-1))
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.dropWhile(predicate: (T) -> Boolean): MutableVIntList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) MutableVIntList<T>(this) else slice(IntRange(i, size))}
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.filter(predicate: (T) -> Boolean): MutableVIntList<T> = filterFromMask(filterMask(predicate))
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.filterMask(predicate: (T) -> Boolean): BitSet = filterIndexedMask {_,e->predicate(e)}
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.filterFromMask(mask: BitSet): MutableVIntList<T> = MutableVIntList<T>(mask.cardinality()).also {c-> forEachIndexed {i,e-> if(mask[i]) c.add(e)} }
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.filterIndexed(predicate: (index: Int, T) -> Boolean): MutableVIntList<T> = filterFromMask(filterIndexedMask(predicate))
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>>VIntCollection<T>.filterIndexedTo(destination: C, predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>>VIntCollection<T>.filterIndexedTo(destination: C, predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.filterIndexedMask(predicate: (index: Int, T) -> Boolean): BitSet {val destination=BitSet(size); forEachIndexed { i, e -> destination.set(i,predicate(i, e))}; return destination }
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.filterNot(predicate: (T) -> Boolean): VIntList<T> = filter {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>>VIntCollection<T>.filterNotTo(destination: C, predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>>VIntCollection<T>.filterNotTo(destination: C, predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>>VIntCollection<T>.filterTo(destination: C, predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>>VIntCollection<T>.filterTo(destination: C, predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.slice(indices: IntRange): MutableVIntList<T> = copyInto(MutableVIntList(size-indices.last+indices.first), 0, indices.first, indices.last)
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.slice(indices: Iterable<Int>): MutableVIntList<T> = MutableVIntList<T>(if (indices is Collection<Int>) indices.size else size/8).also { for(i in indices) it.addBits(bitsAtIndex(i)) }
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.take(n: Int): MutableVIntList<T> = slice(IntRange(0,n))
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.takeLast(n: Int): MutableVIntList<T> = slice(IntRange(size-n,size))
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.takeLastWhile(predicate: (T) -> Boolean): MutableVIntList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) MutableVIntList<T>(this) else slice(IntRange(0, i))}
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.takeWhile(predicate: (T) -> Boolean): MutableVIntList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) MutableVIntList<T>(this) else slice(IntRange(i,size))}
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.reversed(): MutableVIntList<T> = MutableVIntList<T>(size).also {forEachIndexed{i,e-> it[size-i-1] = e }}
//TODO: context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.sorted(): MutableVIntList<T> = MutableVIntList<T>(this).also{it.sort()}
//TODO: context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>>VIntCollection<T>.sortedBy(crossinline selector: (T) -> R?): MutableVIntList<T> = MutableVIntList<T>(this).also{it.sortedBy(selector)}
//TODO: context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>>VIntCollection<T>.sortedByDescending(crossinline selector: (T) -> R?): VIntList<T> = MutableVIntList<T>(this).also{it.sortedBy(selector)}
//TODO: context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>>VIntCollection<T>.sortedDescending(): VIntList<T>
//TODO: context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.sortedWith(comparator: Comparator<T>): VIntList<T>
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.copyInto(destination: VIntArray<T>, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): VIntArray<T> = destination.also{for(i in startIndex..endIndex) destination.setBits(i+destinationOffset, bitsAtIndex(i))}
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.copyInto(destination: MutableVIntList<T>, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): MutableVIntList<T> = destination.also{for(i in startIndex..endIndex) destination.setBits(i+destinationOffset, bitsAtIndex(i))}
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.copyInto(destination: MutableList<T>, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): MutableList<T> = destination.also{for(i in startIndex..endIndex) destination.set(i+destinationOffset, elementAtIndex(i))}
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateVIntInt(transform: (T) -> VIntIntPair<K, V>): VIntIntMap<K, V> = associateTo(MutableVIntIntMap(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateVIntLong(transform: (T) -> VIntLongPair<K, V>): VIntLongMap<K, V> = associateTo(MutableVIntLongMap(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateVLongInt(transform: (T) -> VLongIntPair<K, V>): VLongIntMap<K, V> = associateTo(MutableVLongIntMap(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateVLongLong(transform: (T) -> VLongLongPair<K, V>): VLongLongMap<K, V> = associateTo(MutableVLongLongMap(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateGeneric(transform: (T) -> Pair<K, V>): Map<K, V> = associateTo(HashMap<K,V>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, K>VIntCollection<T>.associateByVInt(keySelector: (T) -> K): MutableVIntIntMap<K, T> = associateByTo(MutableVIntIntMap(size),keySelector,{it})
context(a: ValueIntAdapter<T>) inline fun <T, K>VIntCollection<T>.associateByVLong(keySelector: (T) -> K): MutableVLongIntMap<K, T> = associateByTo(MutableVLongIntMap(size),keySelector,{it})
context(a: ValueIntAdapter<T>) inline fun <T, K>VIntCollection<T>.associateByGeneric(keySelector: (T) -> K): Map<K, T> = HashMap<K,T>(size).also{c->forEach {c.put(keySelector(it),it)}}
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateByVIntInt(keySelector: (T) -> K, valueTransform: (T) -> V): VIntIntMap<K, V> = associateByTo(MutableVIntIntMap(size),keySelector,valueTransform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateByVIntLong(keySelector: (T) -> K, valueTransform: (T) -> V): VIntLongMap<K, V> = associateByTo(MutableVIntLongMap(size),keySelector,valueTransform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateByVLongInt(keySelector: (T) -> K, valueTransform: (T) -> V): VLongIntMap<K, V> = associateByTo(MutableVLongIntMap(size),keySelector,valueTransform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateByVLongLong(keySelector: (T) -> K, valueTransform: (T) -> V): VLongLongMap<K, V> = associateByTo(MutableVLongLongMap(size),keySelector,valueTransform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateByGeneric(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, V> = HashMap<K,V>(size).also{c->forEach {c.put(keySelector(it),valueTransform(it))}}
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateByTo(destination: MutableVIntIntMap<K,V>, keySelector: (T) -> K, valueTransform: (T) -> V): MutableVIntIntMap<K,V> = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateByTo(destination: MutableVIntLongMap<K,V>, keySelector: (T) -> K, valueTransform: (T) -> V): MutableVIntLongMap<K,V> = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateByTo(destination: MutableVLongIntMap<K,V>, keySelector: (T) -> K, valueTransform: (T) -> V): MutableVLongIntMap<K,V> = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateByTo(destination: MutableVLongLongMap<K,V>, keySelector: (T) -> K, valueTransform: (T) -> V): MutableVLongLongMap<K,V> = destination.also{c->c.putAll(this,keySelector,valueTransform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>>VIntCollection<T>.associateByTo(destination: M, keySelector: (T) -> K, valueTransform: (T) -> V): M = destination.also{c->forEach {c.put(keySelector(it),valueTransform(it))}}
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateTo(destination: MutableVIntIntMap<K,V>, transform: (T) -> VIntIntPair<K, V>): MutableVIntIntMap<K,V> = destination.also{c->c.putAll(this,transform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateTo(destination: MutableVIntLongMap<K,V>, transform: (T) -> VIntLongPair<K, V>): MutableVIntLongMap<K,V> = destination.also{c->c.putAll(this,transform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateTo(destination: MutableVLongIntMap<K,V>, transform: (T) -> VLongIntPair<K, V>): MutableVLongIntMap<K,V> = destination.also{c->c.putAll(this,transform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.associateTo(destination: MutableVLongLongMap<K,V>, transform: (T) -> VLongLongPair<K, V>): MutableVLongLongMap<K,V> = destination.also{c->c.putAll(this,transform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>>VIntCollection<T>.associateTo(destination: M, transform: (T) -> Pair<K, V>): M = destination.also{c->forEach {val p=transform(it); c[p.first] = p.second}}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>>VIntCollection<T>.toCollection(destination: C): C = destination.also { it.addAll(this) }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>>VIntCollection<T>.toCollection(destination: C): C = destination.also{c->forEach {c.add(it)}}
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.toHashSet(): HashSet<T> = toCollection(HashSet(size))
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.toListVInt(): VIntList<T> = if (this is VIntList<*>) this as VIntList<T> else toMutableListVInt()
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.toListGeneric(): List<T> = toMutableListGeneric()
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.toMutableListVInt(): MutableVIntList<T> = if (this is MutableVIntList<*>) this as MutableVIntList<T> else toCollection(MutableVIntList<T>(size))
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.toMutableListGeneric(): MutableList<T> = toCollection(ArrayList(size))
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.toSetVInt(): VIntSet<T> = if (this is VIntSet<*>) this as VIntSet<T> else toSetMutableVInt()
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.toSetMutableVInt(): MutableVIntSet<T> = if (this is MutableVIntSet<*>) this as MutableVIntSet<T> else toCollection(MutableVIntSet(size))
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.toSetGeneric(): Set<T> = toHashSet()
/*
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.flatMap(transform: (T) ->VIntCollection<R>): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.flatMap(transform: (T) -> Sequence<R>): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.flatMapIndexed(transform: (index: Int, T) ->VIntCollection<R>): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.flatMapIndexed(transform: (index: Int, T) -> Sequence<R>): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<in R>>VIntCollection<T>.flatMapIndexedTo(destination: C, transform: (index: Int, T) ->VIntCollection<R>): C
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<in R>>VIntCollection<T>.flatMapIndexedTo(destination: C, transform: (index: Int, T) -> Sequence<R>): C
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<in R>>VIntCollection<T>.flatMapTo(destination: C, transform: (T) ->VIntCollection<R>): C
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<in R>>VIntCollection<T>.flatMapTo(destination: C, transform: (T) -> Sequence<R>): C
context(a: ValueIntAdapter<T>) inline fun <T, K>VIntCollection<T>.groupByVInt(keySelector: (T) -> K): MutableVIntObjectMap<K, List<T>> = groupByTo(MutableVIntObjectMap<K,List<T>>(), keySelector)
context(a: ValueIntAdapter<T>) inline fun <T, K>VIntCollection<T>.groupByVLong(keySelector: (T) -> K): MutableVLongObjectMap<K, List<T>> = groupByTo(MutableVLongObjectMap<K,List<T>>(), keySelector)
context(a: ValueIntAdapter<T>) inline fun <T, K>VIntCollection<T>.groupByGeneric(keySelector: (T) -> K): MutableMap<K, List<T>> = groupByTo(HashMap<K,List<T>>(), keySelector)
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.groupByVInt(keySelector: (T) -> K, valueTransform: (T) -> V): MutableVIntObjectMap<K, List<V>>
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.groupByVLong(keySelector: (T) -> K, valueTransform: (T) -> V): MutableVLongObjectMap<K, List<V>>
context(a: ValueIntAdapter<T>) inline fun <T, K, V>VIntCollection<T>.groupByGeneric(keySelector: (T) -> K, valueTransform: (T) -> V): MutableMap<K, List<V>>
context(a: ValueIntAdapter<T>) inline fun <T, K, M : MutableMap<in K, MutableList<T>>>VIntCollection<T>.groupByTo(destination: M, keySelector: (T) -> K): M
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, MutableList<V>>>VIntCollection<T>.groupByTo(destination: M, keySelector: (T) -> K, valueTransform: (T) -> V): M
context(a: ValueIntAdapter<T>) inline fun <T, K>VIntCollection<T>.groupingBy(crossinline keySelector: (T) -> K): Grouping<T, K>
 */
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.mapVInt(transform: (T) -> R): MutableVIntList<R> = mapTo(MutableVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.mapVLong(transform: (T) -> R): MutableVLongList<R> = mapTo(MutableVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.mapGeneric(transform: (T) -> R): MutableList<R> = mapTo(MutableVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.mapIndexedVInt(transform: (index: Int, T) -> R): MutableVIntList<R> = mapIndexedTo(MutableVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.mapIndexedVLong(transform: (index: Int, T) -> R): MutableVLongList<R> = mapIndexedTo(MutableVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.mapIndexedGeneric(transform: (index: Int, T) -> R): List<R> = mapIndexedTo(MutableVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R : Any>VIntCollection<T>.mapIndexedVIntNotNull(transform: (index: Int, T) -> R?): MutableVIntList<R> = mapIndexedNotNullTo(MutableVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R : Any>VIntCollection<T>.mapIndexedVLongNotNull(transform: (index: Int, T) -> R?): MutableVLongList<R> = mapIndexedNotNullTo(MutableVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R : Any>VIntCollection<T>.mapIndexedGenericNotNull(transform: (index: Int, T) -> R?): List<R> = mapIndexedNotNullTo(MutableVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R : Any, C : MutableVIntCollection<R>>VIntCollection<T>.mapIndexedNotNullTo(destination: C, transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} }
context(a: ValueIntAdapter<T>) inline fun <T, R : Any, C : MutableVListCollection<R>>VIntCollection<T>.mapIndexedNotNullTo(destination: C, transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} }
context(a: ValueIntAdapter<T>) inline fun <T, R : Any, C : MutableCollection<in R>>VIntCollection<T>.mapIndexedNotNullTo(destination: C, transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<in R>>VIntCollection<T>.mapIndexedTo(destination: C, transform: (index: Int, T) -> R): C
context(a: ValueIntAdapter<T>) inline fun <T, R : Any>VIntCollection<T>.mapNotNull(transform: (T) -> R?): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R : Any, C : MutableCollection<in R>>VIntCollection<T>.mapNotNullTo(destination: C, transform: (T) -> R?): C
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<in R>>VIntCollection<T>.mapTo(destination: C, transform: (T) -> R): C
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.withIndex():VIntCollection<IndexedValue<T>>
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.distinct(): VIntList<T>
context(a: ValueIntAdapter<T>) inline fun <T, K>VIntCollection<T>.distinctBy(selector: (T) -> K): VIntList<T>
context(a: ValueIntAdapter<T>) inline infix fun <T>VIntCollection<T>.intersect(other:VIntCollection<T>): Set<T>
context(a: ValueIntAdapter<T>) inline infix fun <T>VIntCollection<T>.subtract(other:VIntCollection<T>): Set<T>
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.toMutableSet(): MutableSet<T>
context(a: ValueIntAdapter<T>) inline infix fun <T>VIntCollection<T>.union(other:VIntCollection<T>): Set<T>
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.all(predicate: (T) -> Boolean): Boolean
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.any(): Boolean
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.any(predicate: (T) -> Boolean): Boolean
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.count(): Int
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.count(predicate: (T) -> Boolean): Int
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.fold(initial: R, operation: (acc: R, T) -> R): R
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.foldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): R
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.forEach(action: (T) -> Unit) {for(i in 0..size) action(elementAtIndex(i))}
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.forEachIndexed(action: (index:Int, T) -> Unit) {for(i in 0..size) action(i, elementAtIndex(i))}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>>VIntCollection<T>.max(): T
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>>VIntCollection<T>.maxBy(selector: (T) -> R): T
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>>VIntCollection<T>.maxByOrNull(selector: (T) -> R): T?
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.maxOf(selector: (T) -> Double): Double
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.maxOf(selector: (T) -> Float): Float
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>>VIntCollection<T>.maxOf(selector: (T) -> R): R
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.maxOfOrNull(selector: (T) -> Double): Double?
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.maxOfOrNull(selector: (T) -> Float): Float?
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>>VIntCollection<T>.maxOfOrNull(selector: (T) -> R): R?
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.maxOfWith(comparator: Comparator<in R>, selector: (T) -> R): R
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.maxOfWithOrNull(comparator: Comparator<in R>, selector: (T) -> R): R?
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>>VIntCollection<T>.maxOrNull(): T?
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.maxWith(comparator: Comparator<T>): T
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.maxWithOrNull(comparator: Comparator<T>): T?
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>>VIntCollection<T>.min(): T
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>>VIntCollection<T>.minBy(selector: (T) -> R): T
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>>VIntCollection<T>.minByOrNull(selector: (T) -> R): T?
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.minOf(selector: (T) -> Double): Double
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.minOf(selector: (T) -> Float): Float
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>>VIntCollection<T>.minOf(selector: (T) -> R): R
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.minOfOrNull(selector: (T) -> Double): Double?
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.minOfOrNull(selector: (T) -> Float): Float?
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>>VIntCollection<T>.minOfOrNull(selector: (T) -> R): R?
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.minOfWith(comparator: Comparator<in R>, selector: (T) -> R): R
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.minOfWithOrNull(comparator: Comparator<in R>, selector: (T) -> R): R?
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>>VIntCollection<T>.minOrNull(): T?
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.minWith(comparator: Comparator<T>): T
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.minWithOrNull(comparator: Comparator<T>): T?
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.none(): Boolean
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.none(predicate: (T) -> Boolean): Boolean
context(a: ValueIntAdapter<T>) inline fun <S, T : S>VIntCollection<T>.reduce(operation: (acc: S, T) -> S): S
context(a: ValueIntAdapter<T>) inline fun <S, T : S>VIntCollection<T>.reduceIndexed(operation: (index: Int, acc: S, T) -> S): S
context(a: ValueIntAdapter<T>) inline fun <S, T : S>VIntCollection<T>.reduceIndexedOrNull(operation: (index: Int, acc: S, T) -> S): S?
context(a: ValueIntAdapter<T>) inline fun <S, T : S>VIntCollection<T>.reduceOrNull(operation: (acc: S, T) -> S): S?
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.runningFold(initial: R, operation: (acc: R, T) -> R): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.runningFoldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R>
context(a: ValueIntAdapter<T>) inline fun <S, T : S>VIntCollection<T>.runningReduce(operation: (acc: S, T) -> S): List<S>
context(a: ValueIntAdapter<T>) inline fun <S, T : S>VIntCollection<T>.runningReduceIndexed(operation: (index: Int, acc: S, T) -> S): List<S>
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.scan(initial: R, operation: (acc: R, T) -> R): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.scanIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R>
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.sumBy(selector: (T) -> Int): Int
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.sumByDouble(selector: (T) -> Double): Double
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.sumOf(selector: (T) -> Double): Double
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.sumOf(selector: (T) -> Int): Int
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.sumOf(selector: (T) -> Long): Long
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.sumOf(selector: (T) -> UInt): UInt
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.sumOf(selector: (T) -> ULong): ULong
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.chunked(size: Int): List<List<T>>
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.chunked(size: Int, transform: (List<T>) -> R): List<R>
context(a: ValueIntAdapter<T>) inline operator fun <T>VIntCollection<T>.minus(element: T): VIntList<T>
context(a: ValueIntAdapter<T>) inline operator fun <T>VIntCollection<T>.minus(elements: Array<out T>): VIntList<T>
context(a: ValueIntAdapter<T>) inline operator fun <T>VIntCollection<T>.minus(elements:VIntCollection<T>): VIntList<T>
context(a: ValueIntAdapter<T>) inline operator fun <T>VIntCollection<T>.minus(elements: Sequence<T>): VIntList<T>
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.minusElement(element: T): VIntList<T>
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.partition(predicate: (T) -> Boolean): Pair<List<T>, List<T>>
context(a: ValueIntAdapter<T>) inline operator fun <T>VIntCollection<T>.plus(element: T): VIntList<T>
context(a: ValueIntAdapter<T>) inline operator fun <T>VIntCollection<T>.plus(elements: Array<out T>): VIntList<T>
context(a: ValueIntAdapter<T>) inline operator fun <T>VIntCollection<T>.plus(elements:VIntCollection<T>): VIntList<T>
context(a: ValueIntAdapter<T>) inline operator fun <T>VIntCollection<T>.plus(elements: Sequence<T>): VIntList<T>
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.plusElement(element: T): VIntList<T>
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.windowed(size: Int, step: Int = 1, partialWindows: Boolean = false): List<List<T>>
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.windowed(size: Int, step: Int = 1, partialWindows: Boolean = false, transform: (List<T>) -> R): List<R>
context(a: ValueIntAdapter<T>) inline infix fun <T, R>VIntCollection<T>.zip(other: Array<out R>): List<Pair<T, R>>
context(a: ValueIntAdapter<T>) inline fun <T, R, V>VIntCollection<T>.zip(other: Array<out R>, transform: (a: T, b: R) -> V): List<V>
context(a: ValueIntAdapter<T>) inline infix fun <T, R>VIntCollection<T>.zip(other:VIntCollection<R>): List<Pair<T, R>>
context(a: ValueIntAdapter<T>) inline fun <T, R, V>VIntCollection<T>.zip(other:VIntCollection<R>, transform: (a: T, b: R) -> V): List<V>
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.zipWithNext(): List<Pair<T, T>>
context(a: ValueIntAdapter<T>) inline fun <T, R>VIntCollection<T>.zipWithNext(transform: (a: T, b: T) -> R): List<R>
context(a: ValueIntAdapter<T>) inline fun <T, A : Appendable>VIntCollection<T>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): A
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String
context(a: ValueIntAdapter<T>) inline fun <T>VIntCollection<T>.asSequence(): Sequence<T>

/*
public inline fun <T> Collection<T>.random(): T
public fun <T> Collection<T>.random(random: Random): T
public inline fun <T> Collection<T>.randomOrNull(): T?
public fun <T> Collection<T>.randomOrNull(random: Random): T?
public fun <T> Collection<T>.toMutableList(): MutableList<T>
public inline fun <T> Collection<T>.count(): Int
public operator fun <T> Collection<T>.plus(element: T): VIntList<T>
public operator fun <T> Collection<T>.plus(elements: Array<out T>): VIntList<T>
public operator fun <T> Collection<T>.plus(elements: Iterable<T>): VIntList<T>
public operator fun <T> Collection<T>.plus(elements: Sequence<T>): VIntList<T>
public inline fun <T> Collection<T>.plusElement(element: T): VIntList<T> 
*/
interface MutableVIntCollection<T>: VIntCollection<T> {
    context(a: ValueIntAdapter<T>) override fun asIterable(): MutableVIntIterator<T>
    context(a: ValueIntAdapter<T>) fun add(element: T): Boolean
    context(a: ValueIntAdapter<T>) fun addBits(element: Int): Boolean
    context(a: ValueIntAdapter<T>) fun addAll(elements: VIntCollection<T>): Boolean
    context(a: ValueIntAdapter<T>) fun addAll(elements: Collection<T>): Boolean
    context(a: ValueIntAdapter<T>) fun remove(element: T): Boolean
    context(a: ValueIntAdapter<T>) fun removeAll(elements: VIntCollection<T>): Boolean
    context(a: ValueIntAdapter<T>) fun removeAll(elements: Collection<T>): Boolean
    fun clear(): Unit    
}
/*
inline fun <T, C : MutableCollection<T>> Iterable<T>.filterIndexedTo(destination: C, predicate: (index: Int, T) -> Boolean): C
inline fun <reified R, C : MutableCollection<in R>> Iterable<*>.filterIsInstanceTo(destination: C): C
fun <C : MutableCollection<T>, T : Any> Iterable<T?>.filterNotNullTo(destination: C): C
inline fun <T, C : MutableCollection<T>> Iterable<T>.filterNotTo(destination: C, predicate: (T) -> Boolean): C
inline fun <T, C : MutableCollection<T>> Iterable<T>.filterTo(destination: C, predicate: (T) -> Boolean): C
fun <T, C : MutableCollection<T>> Iterable<T>.toCollection(destination: C): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapIndexedTo(destination: C, transform: (index: Int, T) -> Iterable<R>): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapIndexedTo(destination: C, transform: (index: Int, T) -> Sequence<R>): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapTo(destination: C, transform: (T) -> Iterable<R>): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapTo(destination: C, transform: (T) -> Sequence<R>): C
inline fun <T, R : Any, C : MutableCollection<in R>> Iterable<T>.mapIndexedNotNullTo(destination: C, transform: (index: Int, T) -> R?): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.mapIndexedTo(destination: C, transform: (index: Int, T) -> R): C
inline fun <T, R : Any, C : MutableCollection<in R>> Iterable<T>.mapNotNullTo(destination: C, transform: (T) -> R?): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.mapTo(destination: C, transform: (T) -> R): C
*/