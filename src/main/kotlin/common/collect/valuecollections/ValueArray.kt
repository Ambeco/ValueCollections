@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")
@file:OptIn(ExperimentalStdlibApi::class)

package mpd.com.common.collect.valuecollections

import java.util.BitSet
import kotlin.random.Random

// inline wrappers around kotlin.VIntArray<T> and kotlin.LongArray


// VIntArray<T> -> VIntArray
class VIntArray<T>(val collection:IntArray): VIntCollection<T> {
    context(a: ValueIntAdapter<T>) inline operator fun get(index: Int): T = a.fromInt(collection.get(index))
    context(a: ValueIntAdapter<T>) override inline fun bitsAtIndex(index: Int): Int = collection[index]
    context(a: ValueIntAdapter<T>) inline operator fun set(index: Int, value: T) = collection.set(index, a.toInt(value))
    context(a: ValueIntAdapter<T>) inline fun setBits(index: Int, value: Int) = collection.set(index, value)
    override val size inline get() = collection.size
    context(a: ValueIntAdapter<T>) override inline fun asIterable(): VIntIterator<T> = VIntIterator<T>(collection.iterator(),a)
    override inline operator fun equals(other: Any?): Boolean = collection.equals(other)
    override inline fun hashCode(): Int = collection.hashCode()

    context(a: ValueIntAdapter<T>) inline operator fun component1(): T = a.fromInt(collection.component1())
    context(a: ValueIntAdapter<T>) inline operator fun component2(): T = a.fromInt(collection.component2())
    context(a: ValueIntAdapter<T>) inline operator fun component3(): T = a.fromInt(collection.component3())
    context(a: ValueIntAdapter<T>) inline operator fun component4(): T = a.fromInt(collection.component4())
    context(a: ValueIntAdapter<T>) inline operator fun component5(): T = a.fromInt(collection.component5())
    context(a: ValueIntAdapter<T>) override inline operator fun contains(element: T): Boolean = collection.contains(a.toInt(element))
    context(a: ValueIntAdapter<T>) override inline fun containsAll(elements: VIntCollection<T>): Boolean = elements.all {contains(it)}
    context(a: ValueIntAdapter<T>) override inline fun elementAtIndex(index: Int): T = a.fromInt(collection.elementAt(index))
    context(a: ValueIntAdapter<T>) inline fun elementAtOrElse(index: Int, defaultValue: (Int) -> T): T = a.fromInt(collection.elementAtOrElse(index, {a.toInt(defaultValue(it))}))
    context(a: ValueIntAdapter<T>) inline fun elementAtOrNull(index: Int): T? = a.fromInt(collection.elementAtOrNull(index))
    context(a: ValueIntAdapter<T>) inline fun find(predicate: (T) -> Boolean): T? = a.fromInt(collection.find{predicate(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun findLast(predicate: (T) -> Boolean): T? = a.fromInt(collection.findLast{predicate(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun first(): T = a.fromInt(collection.first())
    context(a: ValueIntAdapter<T>) inline fun first(predicate: (T) -> Boolean): T = a.fromInt(collection.first{predicate(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun firstOrNull(): T? = a.fromInt(collection.firstOrNull())
    context(a: ValueIntAdapter<T>) inline fun firstOrNull(predicate: (T) -> Boolean): T? = a.fromInt(collection.firstOrNull{predicate(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun getOrElse(index: Int, defaultValue: (Int) -> T): T = a.fromInt(collection.getOrElse(index, {a.toInt(defaultValue(it))}))
    context(a: ValueIntAdapter<T>) inline fun getOrNull(index: Int): T? = a.fromInt(collection.getOrNull(index))
    context(a: ValueIntAdapter<T>) inline fun indexOf(element: T): Int = collection.indexOf(a.toInt(element))
    context(a: ValueIntAdapter<T>) inline fun indexOfFirst(predicate: (T) -> Boolean): Int = collection.indexOfFirst{predicate(a.fromInt(it))}
    context(a: ValueIntAdapter<T>) inline fun indexOfLast(predicate: (T) -> Boolean): Int = collection.indexOfLast{predicate(a.fromInt(it))}
    context(a: ValueIntAdapter<T>) inline fun last(): T = a.fromInt(collection.last())
    context(a: ValueIntAdapter<T>) inline fun last(predicate: (T) -> Boolean): T = a.fromInt(collection.last{predicate(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun lastIndexOf(element: T): Int = collection.lastIndexOf(a.toInt(element))
    context(a: ValueIntAdapter<T>) inline fun lastOrNull(): T? = a.fromInt(collection.lastOrNull())
    context(a: ValueIntAdapter<T>) inline fun lastOrNull(predicate: (T) -> Boolean): T? = a.fromInt(collection.lastOrNull{predicate(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun random(): T = a.fromInt(collection.random())
    context(a: ValueIntAdapter<T>) inline fun random(random: Random): T = a.fromInt(collection.random(random))
    context(a: ValueIntAdapter<T>) inline fun randomOrNull(): T? = a.fromInt(collection.randomOrNull())
    context(a: ValueIntAdapter<T>) inline fun randomOrNull(random: Random): T? = a.fromInt(collection.randomOrNull(random))
    context(a: ValueIntAdapter<T>) inline fun single(): T = a.fromInt(collection.single())
    context(a: ValueIntAdapter<T>) inline fun single(predicate: (T) -> Boolean): T = a.fromInt(collection.single{predicate(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun singleOrNull(): T? = a.fromInt(collection.singleOrNull())
    context(a: ValueIntAdapter<T>) inline fun singleOrNull(predicate: (T) -> Boolean): T? = a.fromInt(collection.singleOrNull{predicate(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun drop(n: Int): MutableVIntList<T> = slice(IntRange(n,size-1))
    context(a: ValueIntAdapter<T>) inline fun dropLast(n: Int): MutableVIntList<T> = slice(IntRange(0,size-n))
    context(a: ValueIntAdapter<T>) inline fun dropLastWhile(predicate: (T) -> Boolean): MutableVIntList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(0, i))}
    context(a: ValueIntAdapter<T>) inline fun dropWhile(predicate: (T) -> Boolean): MutableVIntList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(i, size))}
    context(a: ValueIntAdapter<T>) inline fun filter(predicate: (T) -> Boolean): MutableVIntList<T> = filterFromMask(filterMask(predicate))
    context(a: ValueIntAdapter<T>) inline fun filterMask(predicate: (T) -> Boolean): BitSet = filterIndexedMask {_,e->predicate(e)}
    context(a: ValueIntAdapter<T>) inline fun filterFromMask(mask: BitSet): MutableVIntList<T> = MutableVIntList<T>(mask.cardinality()).also {c-> forEachIndexed {i,e-> if(mask[i]) c.add(e)} }
    context(a: ValueIntAdapter<T>) inline fun filterIndexed(predicate: (index: Int, T) -> Boolean): MutableVIntList<T> = filterFromMask(filterIndexedMask(predicate))
    context(a: ValueIntAdapter<T>) inline fun < C : MutableCollection<in T>> filterIndexedTo(destination: C, predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
    context(a: ValueIntAdapter<T>) inline fun filterIndexedTo(destination: MutableVIntList<T>, predicate: (index: Int, T) -> Boolean): MutableVIntList<T> = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
    context(a: ValueIntAdapter<T>) inline fun filterIndexedMask(predicate: (index: Int, T) -> Boolean): BitSet {val destination=BitSet(size); forEachIndexed { i, e -> destination.set(i,predicate(i, e))}; return destination }
    context(a: ValueIntAdapter<T>) inline fun filterNot(predicate: (T) -> Boolean): MutableVIntList<T> = filter {!predicate(it)}
    context(a: ValueIntAdapter<T>) inline fun < C : MutableCollection<in T>> filterNotTo(destination: C, predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
    context(a: ValueIntAdapter<T>) inline fun filterNotTo(destination: MutableVIntList<T>, predicate: (T) -> Boolean): MutableVIntList<T> = filterTo(destination) {!predicate(it)}
    context(a: ValueIntAdapter<T>) inline fun < C : MutableCollection<in T>> filterTo(destination: C, predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
    context(a: ValueIntAdapter<T>) inline fun filterTo(destination: MutableVIntList<T>, predicate: (T) -> Boolean): MutableVIntList<T> = destination.also { forEach { if (predicate(it)) destination.add(it) } }
    context(a: ValueIntAdapter<T>) inline fun slice(indices: IntRange): MutableVIntList<T> = copyInto(MutableVIntList(size-indices.last+indices.first), 0, indices.first, indices.last) 
    context(a: ValueIntAdapter<T>) inline fun slice(indices: Iterable<Int>): MutableVIntList<T> = MutableVIntList<T>(if (indices is Collection<Int>) indices.size else size/8).also { for(i in indices) it.addBits(collection[i]) }
    context(a: ValueIntAdapter<T>) inline fun sliceArray(indices: Collection<Int>): VIntArray<T> = VIntArray(collection.sliceArray(indices))
    context(a: ValueIntAdapter<T>) inline fun sliceArray(indices: IntRange): VIntArray<T> = VIntArray(collection.sliceArray(indices))
    context(a: ValueIntAdapter<T>) inline fun take(n: Int): MutableVIntList<T> = slice(IntRange(0,n))
    context(a: ValueIntAdapter<T>) inline fun takeLast(n: Int): MutableVIntList<T> = slice(IntRange(size-n,size))
    context(a: ValueIntAdapter<T>) inline fun takeLastWhile(predicate: (T) -> Boolean): MutableVIntList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(0, i))}
    context(a: ValueIntAdapter<T>) inline fun takeWhile(predicate: (T) -> Boolean): MutableVIntList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(i,size))}
    context(a: ValueIntAdapter<T>) inline fun reverse(): Unit = collection.reverse()
    context(a: ValueIntAdapter<T>) inline fun reverse(fromIndex: Int, toIndex: Int): Unit = collection.reverse(fromIndex, toIndex)
    context(a: ValueIntAdapter<T>) inline fun reversed(): MutableVIntList<T> = MutableVIntList<T>(size).also {forEachIndexed{i,e-> it[size-i-1] = e }}
    context(a: ValueIntAdapter<T>) inline fun reversedArray(): VIntArray<T> = apply{collection.reversedArray()}
    context(a: ValueIntAdapter<T>) inline fun shuffle(): Unit = collection.shuffle()
    context(a: ValueIntAdapter<T>) inline fun shuffle(random: Random): Unit = collection.shuffle(random)
    context(a: ValueIntAdapter<T>) inline fun sortDescending(): Unit = collection.sortDescending()
    context(a: ValueIntAdapter<T>) inline fun sorted(): MutableVIntList<T> = toMutableList().also{it.sort()}
    context(a: ValueIntAdapter<T>) inline fun sortedArray(): VIntArray<T> = VIntArray(collection.sortedArray())
    context(a: ValueIntAdapter<T>) inline fun sortedArrayDescending(): VIntArray<T> = VIntArray(collection.sortedArrayDescending())
    // TODO: context(a: ValueIntAdapter<T>) inline fun < R : Comparable<R>> sortedBy(crossinline selector: (T) -> R?): MutableVIntList<T> = MutableVIntList(collection.sortedBy{selector(a.fromInt(it))})
    // TODO: context(a: ValueIntAdapter<T>) inline fun < R : Comparable<R>> sortedByDescending(crossinline selector: (T) -> R?): MutableVIntList<T> = MutableVIntList(collection.sortedByDescending { selector(a.fromInt(it)) })
    context(a: ValueIntAdapter<T>) inline fun sortedDescending(): MutableVIntList<T> = toMutableList().also{it.sortDescending()}
    // TODO: context(a: ValueIntAdapter<T>) inline fun sortedWith(comparator: Comparator<in Int>): MutableVIntList<T> = MutableVIntList(collection.sortedWith(comparator))
    context(a: ValueIntAdapter<T>) inline fun asList(): MutableVIntList<T> = toList()
    context(a: ValueIntAdapter<T>) inline infix fun <T> VIntArray<T>?.contentEquals(other: VIntArray<T>?): Boolean = this?.collection?.contentEquals(other?.collection) ?: (other == null)
    context(a: ValueIntAdapter<T>) inline fun <T> VIntArray<T>?.contentHashCode(): Int = this?.collection?.contentHashCode() ?: 0
    context(a: ValueIntAdapter<T>) inline fun <T> VIntArray<T>?.contentToString(): String = this?.collection?.contentToString() ?: "(null)"
    context(a: ValueIntAdapter<T>) inline fun copyInto(destination: MutableList<T>, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): MutableList<T> = destination.also{for(i in startIndex..endIndex) destination.set(i+destinationOffset, get(i))}
    context(a: ValueIntAdapter<T>) inline fun copyInto(destination: VIntArray<T>, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): VIntArray<T> = destination.also{collection.copyInto(destination.collection, destinationOffset, startIndex, endIndex)}
    context(a: ValueIntAdapter<T>) inline fun copyInto(destination: MutableVIntList<T>, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): MutableVIntList<T> = destination.also{for(i in startIndex..endIndex) destination.setBits(i+destinationOffset, collection[i])}
    context(a: ValueIntAdapter<T>) inline fun copyOf(): VIntArray<T> = VIntArray(collection.copyOf())
    context(a: ValueIntAdapter<T>) inline fun copyOf(newSize: Int): VIntArray<T> = VIntArray(collection.copyOf(newSize))
    context(a: ValueIntAdapter<T>) inline fun copyOf(newSize: Int, init: (Int) -> T): VIntArray<T> = VIntArray(collection.copyOf(newSize, {a.toInt(init(it))}))
    context(a: ValueIntAdapter<T>) inline fun copyOfRange(fromIndex: Int, toIndex: Int): VIntArray<T> = VIntArray(collection.copyOfRange(fromIndex, toIndex))
    context(a: ValueIntAdapter<T>) inline fun fill(element: T, fromIndex: Int = 0, toIndex: Int = size): Unit = collection.fill(a.toInt(element), fromIndex, toIndex)
    override inline fun isEmpty(): Boolean = collection.isEmpty()
    context(a: ValueIntAdapter<T>) inline fun isNotEmpty(): Boolean = collection.isNotEmpty()
    context(a: ValueIntAdapter<T>) inline operator fun plus(element: T): VIntArray<T> = VIntArray(collection.plus(a.toInt(element)))
    context(a: ValueIntAdapter<T>) inline operator fun plus(elements: Collection<T>): VIntArray<T> = VIntArray(collection.plus(elements.map { a.toInt(it) }))
    context(a: ValueIntAdapter<T>) inline operator fun plus(elements: VIntArray<T>): VIntArray<T> = VIntArray(collection.plus(elements.collection))
    context(a: ValueIntAdapter<T>) inline fun sort(): Unit = collection.sort()
    context(a: ValueIntAdapter<T>) inline fun sort(fromIndex: Int = 0, toIndex: Int = size): Unit = collection.sort(fromIndex, toIndex)
    context(a: ValueIntAdapter<T>) inline fun sortDescending(fromIndex: Int, toIndex: Int): Unit = collection.sortDescending(fromIndex, toIndex)
    context(a: ValueIntAdapter<T>) inline fun toIntArray(): IntArray = collection
    context(a: ValueIntAdapter<T>) inline fun toVIntArray(): VIntArray<T> = this
    context(a: ValueIntAdapter<T>) inline fun toTypedArray(): Array<Int> = collection.toTypedArray()
    context(a: ValueIntAdapter<T>) inline fun <K,V> associate(transform: (T) -> Pair<K, V>): Map<K, V> = collection.associate { transform(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun <K> associateBy(keySelector: (T) -> K): Map<K, T> = collection.associateBy({ keySelector(a.fromInt(it)) }, {a.fromInt(it)})
    context(a: ValueIntAdapter<T>) inline fun <K,V> associateBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, V> = collection.associateBy({ keySelector(a.fromInt(it)) }, {valueTransform(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun <K, M : MutableMap<in K, in T>> associateByTo(destination: M, keySelector: (T) -> K): M = collection.associateByTo(destination, { keySelector(a.fromInt(it)) }, {a.fromInt(it)})
    context(a: ValueIntAdapter<T>) inline fun <K, V, M : MutableMap<in K, in V>> associateByTo(destination: M, keySelector: (T) -> K, valueTransform: (T) -> V): M = collection.associateByTo(destination, { keySelector(a.fromInt(it)) }, {valueTransform(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun <K, V, M : MutableMap<in K, in V>> associateTo(destination: M, transform: (T) -> Pair<K, V>): M = collection.associateTo(destination, { transform(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun <V> associateWith(valueSelector: (T) -> V): Map<T, V> = collection.associateBy( { a.fromInt(it)}, {valueSelector(a.fromInt(it)) })
    context(a: ValueIntAdapter<T>) inline fun <V, M : MutableMap<in T, in V>> associateWithTo(destination: M, valueSelector: (T) -> V): M = collection.associateByTo(destination, { a.fromInt(it)}, {valueSelector(a.fromInt(it)) })
    context(a: ValueIntAdapter<T>) inline fun <C : MutableCollection<in T>> toCollection(destination: C) = forEach { destination.add(it) }
    context(a: ValueIntAdapter<T>) inline fun toHashSet(): HashSet<T> = HashSet<T>(collection.size).also {c-> forEach { c.add(it) } }
    context(a: ValueIntAdapter<T>) inline fun toGenericList(): List<T> = toMutableGenericList()
    context(a: ValueIntAdapter<T>) inline fun toList(): MutableVIntList<T> = toMutableList()
    context(a: ValueIntAdapter<T>) inline fun toMutableGenericList(): MutableList<T> = ArrayList<T>(collection.size).also {copyInto(it,0,0,size) }
    context(a: ValueIntAdapter<T>) inline fun toMutableList(): MutableVIntList<T> = MutableVIntList<T>(collection.size).also {copyInto(it,0,0,size) }
    context(a: ValueIntAdapter<T>) inline fun toSet(): Set<T> = toHashSet()
    context(a: ValueIntAdapter<T>) inline fun <R> flatMap(transform: (T) -> Iterable<R>): List<R>  = collection.flatMap { transform(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun <R> flatMapIndexed(transform: (index: Int, T) -> Iterable<R>): List<R> = collection.flatMapIndexed {i,e-> transform(i,a.fromInt(e)) }
    context(a: ValueIntAdapter<T>) inline fun <R, C : MutableCollection<in R>> flatMapIndexedTo(destination: C, transform: (index: Int, T) -> Iterable<R>): C = collection.flatMapIndexedTo(destination) {i,e-> transform(i,a.fromInt(e)) }
    context(a: ValueIntAdapter<T>) inline fun <R, C : MutableCollection<in R>> flatMapTo(destination: C, transform: (T) -> Iterable<R>): C = collection.flatMapTo(destination) { transform(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun <K> groupBy(keySelector: (T) -> K): Map<K, List<T>> = collection.groupBy({ keySelector(a.fromInt(it)) }, {a.fromInt(it)})
    context(a: ValueIntAdapter<T>) inline fun <K,V> groupBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, List<V>> = collection.groupBy({ keySelector(a.fromInt(it)) }, {valueTransform(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun <K, M : MutableMap<in K, MutableList<T>>> groupByTo(destination: M, keySelector: (T) -> K): M = collection.groupByTo(destination, { keySelector(a.fromInt(it)) }, {a.fromInt(it)})
    context(a: ValueIntAdapter<T>) inline fun <K, V, M : MutableMap<in K, MutableList<V>>> groupByTo(destination: M, keySelector: (T) -> K, valueTransform: (T) -> V): M = collection.groupByTo(destination, { keySelector(a.fromInt(it)) }, {valueTransform(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun <R> map(transform: (T)->R): List<R> = collection.map { transform(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun <R> mapIndexed(transform: (index: Int, T) -> R): List<R> = collection.mapIndexed {i,e->transform(i,a.fromInt(e)) }
    context(a: ValueIntAdapter<T>) inline fun <R, C : MutableCollection<in R>> mapIndexedTo(destination:C, transform: (index: Int, T) -> R): C = collection.mapIndexedTo(destination) {i,e->transform(i,a.fromInt(e)) }
    context(a: ValueIntAdapter<T>) inline fun <R, C : MutableCollection<in R>> mapTo(destination:C, transform: (T)->R): C = collection.mapTo(destination) { transform(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun withIndex(): Iterable<IndexedValue<T>> = VIteratorIndexedValueInt(collection.withIndex().iterator(), a)
    // TODO: context(a: ValueIntAdapter<T>) inline fun distinct(): MutableVIntList<T> = MutableVIntList(collection.distinct())
    // TODO: context(a: ValueIntAdapter<T>) inline fun <K> distinctBy(selector: (T) -> K): List<T> = collection.distinctBy { selector(a.fromInt(it)) }
    // TODO: context(a: ValueIntAdapter<T>) inline infix fun intersect(other: Iterable<T>): Set<T> = collection.intersect(other)
    // TODO: context(a: ValueIntAdapter<T>) inline infix fun subtract(other: Iterable<T>): Set<T> = collection.subtract(other)
    // TODO: context(a: ValueIntAdapter<T>) inline fun toMutableSet(): MutableSet<T> = collection.toMutableSet()
    // TODO: context(a: ValueIntAdapter<T>) inline infix fun union(other: Iterable<T>): Set<T> = collection.union(other)
    context(a: ValueIntAdapter<T>) inline fun all(predicate: (T) -> Boolean): Boolean = collection.all{predicate(a.fromInt(it))}
    context(a: ValueIntAdapter<T>) inline fun any(): Boolean = collection.any()
    context(a: ValueIntAdapter<T>) inline fun any(predicate: (T) -> Boolean): Boolean = collection.any{predicate(a.fromInt(it))}
    context(a: ValueIntAdapter<T>) inline fun count(): Int = collection.count()
    context(a: ValueIntAdapter<T>) inline fun count(predicate: (T) -> Boolean): Int = collection.count{predicate(a.fromInt(it))}
    context(a: ValueIntAdapter<T>) inline fun <R> fold(initial: R, operation: (acc: R, T) -> R): R = collection.fold(initial, {acc,e->operation(acc,a.fromInt(e))})
    context(a: ValueIntAdapter<T>) inline fun <R> foldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): R  = collection.foldIndexed(initial, {i,acc,e->operation(i,acc,a.fromInt(e))})
    context(a: ValueIntAdapter<T>) inline fun <R> foldRight(initial: R, operation: (T, acc: R) -> R): R  = collection.foldRight(initial, {e,acc->operation(a.fromInt(e),acc)})
    context(a: ValueIntAdapter<T>) inline fun <R> foldRightIndexed(initial: R, operation: (index: Int, T, acc: R) -> R): R = collection.foldRightIndexed(initial, {i,e,acc->operation(i,a.fromInt(e),acc)})
    context(a: ValueIntAdapter<T>) inline fun forEach(action: (T) -> Unit): Unit = collection.forEach { action(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun forEachIndexed(action: (index: Int, T) -> Unit): Unit = collection.forEachIndexed {i,e->action(i,a.fromInt(e)) }
    context(a: ValueIntAdapter<T>) inline fun max(): T = a.fromInt(collection.max())
    context(a: ValueIntAdapter<T>) inline fun <R : Comparable<R>> maxBy(selector: (T)->R): T = a.fromInt(collection.maxBy{selector(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun <R : Comparable<R>> maxByOrNull(selector: (T)->R): T? = a.fromInt(collection.maxByOrNull{selector(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun maxOf(selector: (T) -> Double): Double = collection.maxOf { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun maxOf(selector: (T) -> Float): Float = collection.maxOf { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun <R : Comparable<R>> maxOf(selector: (T)->R): R = collection.maxOf { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun maxOfOrNull(selector: (T) -> Double): Double? = collection.maxOfOrNull{ selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun maxOfOrNull(selector: (T) -> Float): Float? = collection.maxOfOrNull{ selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun <R : Comparable<R>> maxOfOrNull(selector: (T)->R): R? = collection.maxOfOrNull { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun <R> maxOfWith(comparator: Comparator<in R>, selector: (T)->R): R = collection.maxOfWith(comparator) { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun <R> maxOfWithOrNull(comparator: Comparator<in R>, selector: (T)->R): R? = collection.maxOfWithOrNull(comparator) { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun maxOrNull(): T? = a.fromInt(collection.maxOrNull())
    context(a: ValueIntAdapter<T>) inline fun maxWith(comparator: Comparator<in T>): T = a.fromInt(collection.maxWith {l,r-> comparator.compare(a.fromInt(l), a.fromInt(r)) })
    context(a: ValueIntAdapter<T>) inline fun maxWithOrNull(comparator: Comparator<in T>): T? =  a.fromInt(collection.maxWithOrNull {l,r-> comparator.compare(a.fromInt(l), a.fromInt(r)) })
    context(a: ValueIntAdapter<T>) inline fun min(): T = a.fromInt(collection.min())
    context(a: ValueIntAdapter<T>) inline fun <R : Comparable<R>> minBy(selector: (T)->R): T = a.fromInt(collection.minBy{selector(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun <R : Comparable<R>> minByOrNull(selector: (T)->R): T? = a.fromInt(collection.minByOrNull{selector(a.fromInt(it))})
    context(a: ValueIntAdapter<T>) inline fun minOf(selector: (T) -> Double): Double = collection.minOf{ selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun minOf(selector: (T) -> Float): Float = collection.minOf{ selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun <R : Comparable<R>> minOf(selector: (T)->R): R = collection.minOf{ selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun minOfOrNull(selector: (T) -> Double): Double? = collection.minOfOrNull{ selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun minOfOrNull(selector: (T) -> Float): Float? = collection.minOfOrNull{ selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun <R : Comparable<R>> minOfOrNull(selector: (T)->R): R? = collection.minOfOrNull{ selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun <R> minOfWith(comparator: Comparator<in R>, selector: (T)->R): R = collection.minOfWith(comparator) { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun <R> minOfWithOrNull(comparator: Comparator<in R>, selector: (T)->R): R? = collection.minOfWithOrNull(comparator) { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun minOrNull(): T? = a.fromInt(collection.minOrNull())
    context(a: ValueIntAdapter<T>) inline fun minWith(comparator: Comparator<in T>): T = a.fromInt(collection.minWith({l,r-> comparator.compare(a.fromInt(l), a.fromInt(r)) }))
    context(a: ValueIntAdapter<T>) inline fun minWithOrNull(comparator: Comparator<in T>): T? = a.fromInt(collection.minWithOrNull({l,r-> comparator.compare(a.fromInt(l), a.fromInt(r)) }))
    context(a: ValueIntAdapter<T>) inline fun none(): Boolean = collection.none()
    context(a: ValueIntAdapter<T>) inline fun none(predicate: (T) -> Boolean): Boolean = collection.none{predicate(a.fromInt(it))}
    context(a: ValueIntAdapter<T>) inline fun onEach(action: (T) -> Unit): VIntArray<T> = apply { collection.onEach { action(a.fromInt(it)) }}
    context(a: ValueIntAdapter<T>) inline fun onEachIndexed(action: (index: Int, T) -> Unit): VIntArray<T> = apply{collection.onEachIndexed{i,e->action(i,a.fromInt(e)) }}
    context(a: ValueIntAdapter<T>) inline fun reduce(operation: (acc: T, T) -> T): T = a.fromInt(collection.reduce { acc, e -> a.toInt(operation(a.fromInt(acc), a.fromInt(e))) })
    context(a: ValueIntAdapter<T>) inline fun reduceIndexed(operation: (index: Int, acc: T, T) -> T): T = a.fromInt(collection.reduceIndexed { i, acc, e -> a.toInt(operation(i, a.fromInt(acc), a.fromInt(e))) })
    context(a: ValueIntAdapter<T>) inline fun reduceIndexedOrNull(operation: (index: Int, acc: T, T) -> T): T? = a.fromInt(collection.reduceIndexedOrNull { i, acc, e -> a.toInt(operation(i, a.fromInt(acc), a.fromInt(e))) })
    context(a: ValueIntAdapter<T>) inline fun reduceOrNull(operation: (acc: T, T) -> T): T? = a.fromInt(collection.reduceOrNull { acc, e -> a.toInt(operation(a.fromInt(acc), a.fromInt(e))) })
    context(a: ValueIntAdapter<T>) inline fun reduceRight(operation: (T, acc: T) -> T): T = a.fromInt(collection.reduceRight { e, acc -> a.toInt(operation(a.fromInt(e), a.fromInt(acc))) })
    context(a: ValueIntAdapter<T>) inline fun reduceRightIndexed(operation: (index: Int, T, acc: T) -> T): T = a.fromInt(collection.reduceRightIndexed { i, e, acc -> a.toInt(operation(i, a.fromInt(e), a.fromInt(acc))) })
    context(a: ValueIntAdapter<T>) inline fun reduceRightIndexedOrNull(operation: (index: Int, T, acc: T) -> T): T? = a.fromInt(collection.reduceRightIndexedOrNull { i, e, acc -> a.toInt(operation(i, a.fromInt(e), a.fromInt(acc))) })
    context(a: ValueIntAdapter<T>) inline fun reduceRightOrNull(operation: (T, acc: T) -> T): T? = a.fromInt(collection.reduceRightOrNull { e, acc -> a.toInt(operation(a.fromInt(e), a.fromInt(acc))) })
    context(a: ValueIntAdapter<T>) inline fun <R> runningFold(initial: R, operation: (acc: R, T) -> R): List<R> = collection.runningFold(initial, {acc,e->operation(acc,a.fromInt(e))})
    context(a: ValueIntAdapter<T>) inline fun <R> runningFoldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R> = collection.runningFoldIndexed(initial) {i,acc,e->operation(i,acc,a.fromInt(e))}
    context(a: ValueIntAdapter<T>) inline fun runningReduce(operation: (acc: T, T) -> T): MutableVIntList<T> = runningReduceIndexed { index, acc, t -> operation(acc,t) }
    context(a: ValueIntAdapter<T>) inline fun runningReduceIndexed(operation: (index: Int, acc: T, T) -> T): MutableVIntList<T> {
        if (collection.size < 2) return MutableVIntList()
        val r = MutableVIntList<T>(collection.size-1)
        var acc = a.fromInt(collection[0])
        for (i in 0 until collection.size-1) {
            acc = operation(i, acc, a.fromInt(collection[i]))
            r[i] = acc
        }
        return r
    }
    context(a: ValueIntAdapter<T>) inline fun <R> scan(initial: R, operation: (acc: R, T) -> R): List<R> = collection.scan(initial, {acc,e->operation(acc,a.fromInt(e))})
    context(a: ValueIntAdapter<T>) inline fun <R> scanIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R> = collection.scanIndexed(initial) {i,acc,e->operation(i,acc,a.fromInt(e))}
    context(a: ValueIntAdapter<T>) inline fun sumBy(selector: (T) -> Int): Int = collection.sumBy {selector(a.fromInt(it))}
    context(a: ValueIntAdapter<T>) inline fun sumByDouble(selector: (T) -> Double): Double = collection.sumByDouble { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun sumOf(selector: (T) -> Double): Double = collection.sumOf { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun sumOf(selector: (T) -> Int): Int = collection.sumOf { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun sumOf(selector: (T) -> T): T = a.fromInt(collection.sumOf { a.toInt(selector(a.fromInt(it))) })
    context(a: ValueIntAdapter<T>) inline fun sumOf(selector: (T) -> Long): Long = collection.sumOf { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun sumOf(selector: (T) -> UInt): UInt = collection.sumOf { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun sumOf(selector: (T) -> ULong): ULong = collection.sumOf { selector(a.fromInt(it)) }
    context(a: ValueIntAdapter<T>) inline fun partition(predicate: (T) -> Boolean): Pair<MutableVIntList<T>, MutableVIntList<T>> {
        val mask = filterMask(predicate)
        val left = filterFromMask(mask)
        mask.flip(0,mask.size())
        return Pair(left, filterFromMask(mask))
    }
    context(a: ValueIntAdapter<T>) infix fun <R> zip(other: Array<out R>): List<Pair<T, R>> = collection.zip(other) {l,r -> a.fromInt(l) to r}
    context(a: ValueIntAdapter<T>) inline fun <R, V> zip(other: Array<out R>, transform: (T,R) -> V): List<V> = collection.zip(other) {l,r -> transform(a.fromInt(l), r) }
    context(a: ValueIntAdapter<T>) infix fun <R> zip(other: Iterable<R>): List<Pair<T, R>> = collection.zip(other) {l,r -> a.fromInt(l) to r}
    context(a: ValueIntAdapter<T>) inline fun <R, V> zip(other: Iterable<R>, transform: (T,R) -> V): List<V> = collection.zip(other) {l,r -> transform(a.fromInt(l), r) }
    context(a: ValueIntAdapter<T>) inline infix fun zip(other: VIntArray<T>): List<Pair<T, T>> = collection.zip(other.collection) {l,r -> a.fromInt(l) to a.fromInt(r)}
    context(a: ValueIntAdapter<T>) inline fun <V> zip(other: VIntArray<T>, transform: (T,T) -> V): List<V> = collection.zip(other.collection) {l,r -> transform(a.fromInt(l), a.fromInt(r)) }
    context(a: ValueIntAdapter<T>) inline fun <A : Appendable> joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence)={ it.toString()}): A 
        = collection.joinTo(buffer, separator, prefix, postfix, limit, truncated, { transform(a.fromInt(it)) })
    context(a: ValueIntAdapter<T>) inline fun joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence)={ it.toString()}): String
        = collection.joinToString(separator, prefix, postfix, limit, truncated, { transform(a.fromInt(it)) })
    context(a: ValueIntAdapter<T>) inline fun asSequence(): Sequence<T> = collection.asSequence().map { a.fromInt(it) }
    context(a: ValueIntAdapter<T>) inline fun average(): Double = collection.average()
    context(a: ValueIntAdapter<T>) inline fun sum(): T = a.fromInt(collection.sum())
}



// VLongArray<T> -> VLongArray
class VLongArray<T>(val collection:LongArray) {
    context(a: ValueLongAdapter<T>) inline operator fun get(index: Int): T = a.fromLong(collection.get(index))
    context(a: ValueLongAdapter<T>) inline fun getBits(index: Int): Long = collection[index]
    context(a: ValueLongAdapter<T>) inline operator fun set(index: Int, value: T) = collection.set(index, a.toLong(value))
    context(a: ValueLongAdapter<T>) inline fun setBits(index: Int, value: Long) = collection.set(index, value)
    val <T> VLongArray<T>.size inline get() = collection.size
    context(a: ValueLongAdapter<T>) inline operator fun iterator() = VLongIterator(collection.iterator(),a)
    override inline operator fun equals(other: Any?): Boolean = collection.equals(other)
    override inline fun hashCode(): Int = collection.hashCode()

    context(a: ValueLongAdapter<T>) inline operator fun component1(): T = a.fromLong(collection.component1())
    context(a: ValueLongAdapter<T>) inline operator fun component2(): T = a.fromLong(collection.component2())
    context(a: ValueLongAdapter<T>) inline operator fun component3(): T = a.fromLong(collection.component3())
    context(a: ValueLongAdapter<T>) inline operator fun component4(): T = a.fromLong(collection.component4())
    context(a: ValueLongAdapter<T>) inline operator fun component5(): T = a.fromLong(collection.component5())
    context(a: ValueLongAdapter<T>) inline operator fun contains(element: T): Boolean = collection.contains(a.toLong(element))
    context(a: ValueLongAdapter<T>) inline fun elementAt(index: Int): T = a.fromLong(collection.elementAt(index))
    context(a: ValueLongAdapter<T>) inline fun elementAtOrElse(index: Int, defaultValue: (Int) -> T): T = a.fromLong(collection.elementAtOrElse(index, {a.toLong(defaultValue(it))}))
    context(a: ValueLongAdapter<T>) inline fun elementAtOrNull(index: Int): T? = a.fromLong(collection.elementAtOrNull(index))
    context(a: ValueLongAdapter<T>) inline fun find(predicate: (T) -> Boolean): T? = a.fromLong(collection.find{predicate(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun findLast(predicate: (T) -> Boolean): T? = a.fromLong(collection.findLast{predicate(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun first(): T = a.fromLong(collection.first())
    context(a: ValueLongAdapter<T>) inline fun first(predicate: (T) -> Boolean): T = a.fromLong(collection.first{predicate(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun firstOrNull(): T? = a.fromLong(collection.firstOrNull())
    context(a: ValueLongAdapter<T>) inline fun firstOrNull(predicate: (T) -> Boolean): T? = a.fromLong(collection.firstOrNull{predicate(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun getOrElse(index: Int, defaultValue: (Int) -> T): T = a.fromLong(collection.getOrElse(index,{a.toLong(defaultValue(it))}))
    context(a: ValueLongAdapter<T>) inline fun getOrNull(index: Int): T? = a.fromLong(collection.getOrNull(index))
    context(a: ValueLongAdapter<T>) inline fun indexOf(element: T): Int = collection.indexOf(a.toLong(element))
    context(a: ValueLongAdapter<T>) inline fun indexOfFirst(predicate: (T) -> Boolean): Int = collection.indexOfFirst{predicate(a.fromLong(it))}
    context(a: ValueLongAdapter<T>) inline fun indexOfLast(predicate: (T) -> Boolean): Int = collection.indexOfLast{predicate(a.fromLong(it))}
    context(a: ValueLongAdapter<T>) inline fun last(): T = a.fromLong(collection.last())
    context(a: ValueLongAdapter<T>) inline fun last(predicate: (T) -> Boolean): T = a.fromLong(collection.last{predicate(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun lastIndexOf(element: T): Int = collection.lastIndexOf(a.toLong(element))
    context(a: ValueLongAdapter<T>) inline fun lastOrNull(): T? = a.fromLong(collection.lastOrNull())
    context(a: ValueLongAdapter<T>) inline fun lastOrNull(predicate: (T) -> Boolean): T? = a.fromLong(collection.lastOrNull{predicate(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun random(): T = a.fromLong(collection.random())
    context(a: ValueLongAdapter<T>) inline fun random(random: Random): T = a.fromLong(collection.random(random))
    context(a: ValueLongAdapter<T>) inline fun randomOrNull(): T? = a.fromLong(collection.randomOrNull())
    context(a: ValueLongAdapter<T>) inline fun randomOrNull(random: Random): T? = a.fromLong(collection.randomOrNull(random))
    context(a: ValueLongAdapter<T>) inline fun single(): T = a.fromLong(collection.single())
    context(a: ValueLongAdapter<T>) inline fun single(predicate: (T) -> Boolean): T = a.fromLong(collection.single{predicate(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun singleOrNull(): T? = a.fromLong(collection.singleOrNull())
    context(a: ValueLongAdapter<T>) inline fun singleOrNull(predicate: (T) -> Boolean): T? = a.fromLong(collection.singleOrNull{predicate(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun drop(n: Int): MutableVLongList<T> = slice(IntRange(n,size-1))
    context(a: ValueLongAdapter<T>) inline fun dropLast(n: Int): MutableVLongList<T> = slice(IntRange(0,size-n))
    context(a: ValueLongAdapter<T>) inline fun dropLastWhile(predicate: (T) -> Boolean): MutableVLongList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(0, i))}
    context(a: ValueLongAdapter<T>) inline fun dropWhile(predicate: (T) -> Boolean): MutableVLongList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(i, size))}
    context(a: ValueLongAdapter<T>) inline fun filter(predicate: (T) -> Boolean): MutableVLongList<T> = filterFromMask(filterMask(predicate))
    context(a: ValueLongAdapter<T>) inline fun filterMask(predicate: (T) -> Boolean): BitSet = filterIndexedMask {_,e->predicate(e)}
    context(a: ValueLongAdapter<T>) inline fun filterFromMask(mask: BitSet): MutableVLongList<T> = MutableVLongList<T>(mask.cardinality()).also {c-> forEachIndexed {i,e-> if(mask[i]) c.add(e)} }
    context(a: ValueLongAdapter<T>) inline fun filterIndexed(predicate: (index: Int, T) -> Boolean): MutableVLongList<T> = filterFromMask(filterIndexedMask(predicate))
    context(a: ValueLongAdapter<T>) inline fun < C : MutableCollection<in T>> filterIndexedTo(destination: C, predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
    context(a: ValueLongAdapter<T>) inline fun filterIndexedTo(destination: MutableVLongList<T>, predicate: (index: Int, T) -> Boolean): MutableVLongList<T> = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
    context(a: ValueLongAdapter<T>) inline fun filterIndexedMask(predicate: (index: Int, T) -> Boolean): BitSet {val destination=BitSet(size); forEachIndexed { i, e -> destination.set(i,predicate(i, e))}; return destination }
    context(a: ValueLongAdapter<T>) inline fun filterNot(predicate: (T) -> Boolean): MutableVLongList<T> = filter {!predicate(it)}
    context(a: ValueLongAdapter<T>) inline fun < C : MutableCollection<in T>> filterNotTo(destination: C, predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
    context(a: ValueLongAdapter<T>) inline fun filterNotTo(destination: MutableVLongList<T>, predicate: (T) -> Boolean): MutableVLongList<T> = filterTo(destination) {!predicate(it)}
    context(a: ValueLongAdapter<T>) inline fun < C : MutableCollection<in T>> filterTo(destination: C, predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
    context(a: ValueLongAdapter<T>) inline fun filterTo(destination: MutableVLongList<T>, predicate: (T) -> Boolean): MutableVLongList<T> = destination.also { forEach { if (predicate(it)) destination.add(it) } }
    context(a: ValueLongAdapter<T>) inline fun slice(indices: IntRange): MutableVLongList<T> = copyInto(MutableVLongList(size-indices.last+indices.first), 0, indices.first, indices.last)
    context(a: ValueLongAdapter<T>) inline fun slice(indices: Iterable<Int>): MutableVLongList<T> = MutableVLongList<T>(if (indices is Collection<Int>) indices.size else size/8).also { for(i in indices) it.addBits(collection[i]) }
    context(a: ValueLongAdapter<T>) inline fun sliceArray(indices: Collection<Int>): VLongArray<T> = VLongArray(collection.sliceArray(indices))
    context(a: ValueLongAdapter<T>) inline fun sliceArray(indices: IntRange): VLongArray<T> = VLongArray(collection.sliceArray(indices))
    context(a: ValueLongAdapter<T>) inline fun take(n: Int): MutableVLongList<T> = slice(IntRange(0,n))
    context(a: ValueLongAdapter<T>) inline fun takeLast(n: Int): MutableVLongList<T> = slice(IntRange(size-n,size))
    context(a: ValueLongAdapter<T>) inline fun takeLastWhile(predicate: (T) -> Boolean): MutableVLongList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(0, i))}
    context(a: ValueLongAdapter<T>) inline fun takeWhile(predicate: (T) -> Boolean): MutableVLongList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(i,size))}
    context(a: ValueLongAdapter<T>) inline fun reverse(): Unit = collection.reverse()
    context(a: ValueLongAdapter<T>) inline fun reverse(fromIndex: Int, toIndex: Int): Unit = collection.reverse(fromIndex, toIndex)
    context(a: ValueLongAdapter<T>) inline fun reversed(): MutableVLongList<T> = MutableVLongList<T>(size).also {forEachIndexed{i,e-> it[size-i-1] = e }}
    context(a: ValueLongAdapter<T>) inline fun reversedArray(): VLongArray<T> = apply{collection.reversedArray()}
    context(a: ValueLongAdapter<T>) inline fun shuffle(): Unit = collection.shuffle()
    context(a: ValueLongAdapter<T>) inline fun shuffle(random: Random): Unit = collection.shuffle(random)
    context(a: ValueLongAdapter<T>) inline fun sortDescending(): Unit = collection.sortDescending()
    context(a: ValueLongAdapter<T>) inline fun sorted(): MutableVLongList<T> = toMutableList().also{it.sort()}
    context(a: ValueLongAdapter<T>) inline fun sortedArray(): VLongArray<T> = VLongArray(collection.sortedArray())
    context(a: ValueLongAdapter<T>) inline fun sortedArrayDescending(): VLongArray<T> = VLongArray(collection.sortedArrayDescending())
    // TODO: context(a: ValueLongAdapter<T>) inline fun < R : Comparable<R>> sortedBy(crossinline selector: (T) -> R?): MutableVLongList<T> = MutableVLongList(collection.sortedBy{selector(a.fromLong(it))})
    // TODO: context(a: ValueLongAdapter<T>) inline fun < R : Comparable<R>> sortedByDescending(crossinline selector: (T) -> R?): MutableVLongList<T> = MutableVLongList(collection.sortedByDescending { selector(a.fromLong(it)) })
    context(a: ValueLongAdapter<T>) inline fun sortedDescending(): MutableVLongList<T> = toMutableList().also{it.sortDescending()}
    // TODO: context(a: ValueLongAdapter<T>) inline fun sortedWith(comparator: Comparator<in Int>): MutableVLongList<T> = MutableVLongList(collection.sortedWith(comparator))
    context(a: ValueLongAdapter<T>) inline fun asList(): MutableVLongList<T> = toList()
    context(a: ValueLongAdapter<T>) inline infix fun <T> VLongArray<T>?.contentEquals(other: VLongArray<T>?): Boolean = this?.collection?.contentEquals(other?.collection) ?: (other == null)
    context(a: ValueLongAdapter<T>) inline fun <T> VLongArray<T>?.contentHashCode(): Int = this?.collection?.contentHashCode() ?: 0
    context(a: ValueLongAdapter<T>) inline fun <T> VLongArray<T>?.contentToString(): String = this?.collection?.contentToString() ?: "(null)"
    context(a: ValueLongAdapter<T>) inline fun copyInto(destination: MutableList<T>, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): MutableList<T> = destination.also{for(i in startIndex..endIndex) destination.set(i+destinationOffset, get(i))}
    context(a: ValueLongAdapter<T>) inline fun copyInto(destination: VLongArray<T>, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): VLongArray<T> = destination.also{collection.copyInto(destination.collection, destinationOffset, startIndex, endIndex)}
    context(a: ValueLongAdapter<T>) inline fun copyInto(destination: MutableVLongList<T>, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): MutableVLongList<T> = destination.also{for(i in startIndex..endIndex) destination.setBits(i+destinationOffset, collection[i])}
    context(a: ValueLongAdapter<T>) inline fun copyOf(): VLongArray<T> = VLongArray(collection.copyOf())
    context(a: ValueLongAdapter<T>) inline fun copyOf(newSize: Int): VLongArray<T> = VLongArray(collection.copyOf(newSize))
    context(a: ValueLongAdapter<T>) inline fun copyOf(newSize: Int, init: (Int) -> T): VLongArray<T> = VLongArray(collection.copyOf(newSize, {a.toLong(init(it))}))
    context(a: ValueLongAdapter<T>) inline fun copyOfRange(fromIndex: Int, toIndex: Int): VLongArray<T> = VLongArray(collection.copyOfRange(fromIndex, toIndex))
    context(a: ValueLongAdapter<T>) inline fun fill(element: T, fromIndex: Int = 0, toIndex: Int = size): Unit = collection.fill(a.toLong(element), fromIndex, toIndex)
    context(a: ValueLongAdapter<T>) inline fun isEmpty(): Boolean = collection.isEmpty()
    context(a: ValueLongAdapter<T>) inline fun isNotEmpty(): Boolean = collection.isNotEmpty()
    context(a: ValueLongAdapter<T>) inline operator fun plus(element: T): VLongArray<T> = VLongArray(collection.plus(a.toLong(element)))
    context(a: ValueLongAdapter<T>) inline operator fun plus(elements: Collection<T>): VLongArray<T> = VLongArray(collection.plus(elements.map { a.toLong(it) }))
    context(a: ValueLongAdapter<T>) inline operator fun plus(elements: VLongArray<T>): VLongArray<T> = VLongArray(collection.plus(elements.collection))
    context(a: ValueLongAdapter<T>) inline fun sort(): Unit = collection.sort()
    context(a: ValueLongAdapter<T>) inline fun sort(fromIndex: Int = 0, toIndex: Int = size): Unit = collection.sort(fromIndex, toIndex)
    context(a: ValueLongAdapter<T>) inline fun sortDescending(fromIndex: Int, toIndex: Int): Unit = collection.sortDescending(fromIndex, toIndex)
    context(a: ValueLongAdapter<T>) inline fun toLongArray(): LongArray = collection
    context(a: ValueLongAdapter<T>) inline fun toVLongArray(): VLongArray<T> = this
    context(a: ValueLongAdapter<T>) inline fun toTypedArray(): Array<Long> = collection.toTypedArray()
    context(a: ValueLongAdapter<T>) inline fun <K,V> associate(transform: (T) -> Pair<K, V>): Map<K, V> = collection.associate { transform(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun <K> associateBy(keySelector: (T) -> K): Map<K, T> = collection.associateBy({ keySelector(a.fromLong(it)) }, {a.fromLong(it)})
    context(a: ValueLongAdapter<T>) inline fun <K,V> associateBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, V> = collection.associateBy({ keySelector(a.fromLong(it)) }, {valueTransform(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun <K, M : MutableMap<in K, in T>> associateByTo(destination: M, keySelector: (T) -> K): M = collection.associateByTo(destination, { keySelector(a.fromLong(it)) }, {a.fromLong(it)})
    context(a: ValueLongAdapter<T>) inline fun <K, V, M : MutableMap<in K, in V>> associateByTo(destination: M, keySelector: (T) -> K, valueTransform: (T) -> V): M = collection.associateByTo(destination, { keySelector(a.fromLong(it)) }, {valueTransform(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun <K, V, M : MutableMap<in K, in V>> associateTo(destination: M, transform: (T) -> Pair<K, V>): M = collection.associateTo(destination, { transform(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun <V> associateWith(valueSelector: (T) -> V): Map<T, V> = collection.associateBy( { a.fromLong(it)}, {valueSelector(a.fromLong(it)) })
    context(a: ValueLongAdapter<T>) inline fun <V, M : MutableMap<in T, in V>> associateWithTo(destination: M, valueSelector: (T) -> V): M = collection.associateByTo(destination, { a.fromLong(it)}, {valueSelector(a.fromLong(it)) })
    context(a: ValueLongAdapter<T>) inline fun <C : MutableCollection<in T>> toCollection(destination: C) = forEach { destination.add(it) }
    context(a: ValueLongAdapter<T>) inline fun toHashSet(): HashSet<T> = HashSet<T>(collection.size).also {c-> forEach { c.add(it) } }
    context(a: ValueLongAdapter<T>) inline fun toGenericList(): List<T> = toMutableGenericList()
    context(a: ValueLongAdapter<T>) inline fun toList(): MutableVLongList<T> = toMutableList()
    context(a: ValueLongAdapter<T>) inline fun toMutableGenericList(): MutableList<T> = ArrayList<T>(collection.size).also {copyInto(it,0,0,size) }
    context(a: ValueLongAdapter<T>) inline fun toMutableList(): MutableVLongList<T> = MutableVLongList<T>(collection.size).also {copyInto(it,0,0,size) }
    context(a: ValueLongAdapter<T>) inline fun toSet(): Set<T> = toHashSet()
    context(a: ValueLongAdapter<T>) inline fun <R> flatMap(transform: (T) -> Iterable<R>): List<R>  = collection.flatMap { transform(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun <R> flatMapIndexed(transform: (index: Int, T) -> Iterable<R>): List<R> = collection.flatMapIndexed {i,e-> transform(i,a.fromLong(e)) }
    context(a: ValueLongAdapter<T>) inline fun <R, C : MutableCollection<in R>> flatMapIndexedTo(destination: C, transform: (index: Int, T) -> Iterable<R>): C = collection.flatMapIndexedTo(destination) {i,e-> transform(i,a.fromLong(e)) }
    context(a: ValueLongAdapter<T>) inline fun <R, C : MutableCollection<in R>> flatMapTo(destination: C, transform: (T) -> Iterable<R>): C = collection.flatMapTo(destination) { transform(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun <K> groupBy(keySelector: (T) -> K): Map<K, List<T>> = collection.groupBy({ keySelector(a.fromLong(it)) }, {a.fromLong(it)})
    context(a: ValueLongAdapter<T>) inline fun <K,V> groupBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, List<V>> = collection.groupBy({ keySelector(a.fromLong(it)) }, {valueTransform(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun <K, M : MutableMap<in K, MutableList<T>>> groupByTo(destination: M, keySelector: (T) -> K): M = collection.groupByTo(destination, { keySelector(a.fromLong(it)) }, {a.fromLong(it)})
    context(a: ValueLongAdapter<T>) inline fun <K, V, M : MutableMap<in K, MutableList<V>>> groupByTo(destination: M, keySelector: (T) -> K, valueTransform: (T) -> V): M = collection.groupByTo(destination, { keySelector(a.fromLong(it)) }, {valueTransform(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun <R> map(transform: (T)->R): List<R> = collection.map { transform(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun <R> mapIndexed(transform: (index: Int, T) -> R): List<R> = collection.mapIndexed {i,e->transform(i,a.fromLong(e)) }
    context(a: ValueLongAdapter<T>) inline fun <R, C : MutableCollection<in R>> mapIndexedTo(destination:C, transform: (index: Int, T) -> R): C = collection.mapIndexedTo(destination) {i,e->transform(i,a.fromLong(e)) }
    context(a: ValueLongAdapter<T>) inline fun <R, C : MutableCollection<in R>> mapTo(destination:C, transform: (T)->R): C = collection.mapTo(destination) { transform(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun withIndex(): Iterable<IndexedValue<T>> = VIteratorIndexedValueLong(collection.withIndex().iterator(), a)
    // TODO: context(a: ValueLongAdapter<T>) inline fun distinct(): MutableVLongList<T> = MutableVLongList(collection.distinct())
    // TODO: context(a: ValueLongAdapter<T>) inline fun <K> distinctBy(selector: (T) -> K): List<T> = collection.distinctBy { selector(a.fromLong(it)) }
    // TODO: context(a: ValueLongAdapter<T>) inline infix fun intersect(other: Iterable<T>): Set<T> = collection.intersect(other)
    // TODO: context(a: ValueLongAdapter<T>) inline infix fun subtract(other: Iterable<T>): Set<T> = collection.subtract(other)
    // TODO: context(a: ValueLongAdapter<T>) inline fun toMutableSet(): MutableSet<T> = collection.toMutableSet()
    // TODO: context(a: ValueLongAdapter<T>) inline infix fun union(other: Iterable<T>): Set<T> = collection.union(other)
    context(a: ValueLongAdapter<T>) inline fun all(predicate: (T) -> Boolean): Boolean = collection.all{predicate(a.fromLong(it))}
    context(a: ValueLongAdapter<T>) inline fun any(): Boolean = collection.any()
    context(a: ValueLongAdapter<T>) inline fun any(predicate: (T) -> Boolean): Boolean = collection.any{predicate(a.fromLong(it))}
    context(a: ValueLongAdapter<T>) inline fun count(): Int = collection.count()
    context(a: ValueLongAdapter<T>) inline fun count(predicate: (T) -> Boolean): Int = collection.count{predicate(a.fromLong(it))}
    context(a: ValueLongAdapter<T>) inline fun <R> fold(initial: R, operation: (acc: R, T) -> R): R = collection.fold(initial, {acc,e->operation(acc,a.fromLong(e))})
    context(a: ValueLongAdapter<T>) inline fun <R> foldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): R  = collection.foldIndexed(initial, {i,acc,e->operation(i,acc,a.fromLong(e))})
    context(a: ValueLongAdapter<T>) inline fun <R> foldRight(initial: R, operation: (T, acc: R) -> R): R  = collection.foldRight(initial, {e,acc->operation(a.fromLong(e),acc)})
    context(a: ValueLongAdapter<T>) inline fun <R> foldRightIndexed(initial: R, operation: (index: Int, T, acc: R) -> R): R = collection.foldRightIndexed(initial, {i,e,acc->operation(i,a.fromLong(e),acc)})
    context(a: ValueLongAdapter<T>) inline fun forEach(action: (T) -> Unit): Unit = collection.forEach { action(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun forEachIndexed(action: (index: Int, T) -> Unit): Unit = collection.forEachIndexed {i,e->action(i,a.fromLong(e)) }
    context(a: ValueLongAdapter<T>) inline fun max(): T = a.fromLong(collection.max())
    context(a: ValueLongAdapter<T>) inline fun <R : Comparable<R>> maxBy(selector: (T)->R): T = a.fromLong(collection.maxBy{selector(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun <R : Comparable<R>> maxByOrNull(selector: (T)->R): T? = a.fromLong(collection.maxByOrNull{selector(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun maxOf(selector: (T) -> Double): Double = collection.maxOf { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun maxOf(selector: (T) -> Float): Float = collection.maxOf { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun <R : Comparable<R>> maxOf(selector: (T)->R): R = collection.maxOf { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun maxOfOrNull(selector: (T) -> Double): Double? = collection.maxOfOrNull{ selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun maxOfOrNull(selector: (T) -> Float): Float? = collection.maxOfOrNull{ selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun <R : Comparable<R>> maxOfOrNull(selector: (T)->R): R? = collection.maxOfOrNull { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun <R> maxOfWith(comparator: Comparator<in R>, selector: (T)->R): R = collection.maxOfWith(comparator) { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun <R> maxOfWithOrNull(comparator: Comparator<in R>, selector: (T)->R): R? = collection.maxOfWithOrNull(comparator) { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun maxOrNull(): T? = a.fromLong(collection.maxOrNull())
    context(a: ValueLongAdapter<T>) inline fun maxWith(comparator: Comparator<in T>): T = a.fromLong(collection.maxWith {l,r-> comparator.compare(a.fromLong(l), a.fromLong(r)) })
    context(a: ValueLongAdapter<T>) inline fun maxWithOrNull(comparator: Comparator<in T>): T? =  a.fromLong(collection.maxWithOrNull {l,r-> comparator.compare(a.fromLong(l), a.fromLong(r)) })
    context(a: ValueLongAdapter<T>) inline fun min(): T = a.fromLong(collection.min())
    context(a: ValueLongAdapter<T>) inline fun <R : Comparable<R>> minBy(selector: (T)->R): T = a.fromLong(collection.minBy{selector(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun <R : Comparable<R>> minByOrNull(selector: (T)->R): T? = a.fromLong(collection.minByOrNull{selector(a.fromLong(it))})
    context(a: ValueLongAdapter<T>) inline fun minOf(selector: (T) -> Double): Double = collection.minOf{ selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun minOf(selector: (T) -> Float): Float = collection.minOf{ selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun <R : Comparable<R>> minOf(selector: (T)->R): R = collection.minOf{ selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun minOfOrNull(selector: (T) -> Double): Double? = collection.minOfOrNull{ selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun minOfOrNull(selector: (T) -> Float): Float? = collection.minOfOrNull{ selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun <R : Comparable<R>> minOfOrNull(selector: (T)->R): R? = collection.minOfOrNull{ selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun <R> minOfWith(comparator: Comparator<in R>, selector: (T)->R): R = collection.minOfWith(comparator) { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun <R> minOfWithOrNull(comparator: Comparator<in R>, selector: (T)->R): R? = collection.minOfWithOrNull(comparator) { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun minOrNull(): T? = a.fromLong(collection.minOrNull())
    context(a: ValueLongAdapter<T>) inline fun minWith(comparator: Comparator<in T>): T = a.fromLong(collection.minWith({l,r-> comparator.compare(a.fromLong(l), a.fromLong(r)) }))
    context(a: ValueLongAdapter<T>) inline fun minWithOrNull(comparator: Comparator<in T>): T? = a.fromLong(collection.minWithOrNull({l,r-> comparator.compare(a.fromLong(l), a.fromLong(r)) }))
    context(a: ValueLongAdapter<T>) inline fun none(): Boolean = collection.none()
    context(a: ValueLongAdapter<T>) inline fun none(predicate: (T) -> Boolean): Boolean = collection.none{predicate(a.fromLong(it))}
    context(a: ValueLongAdapter<T>) inline fun onEach(action: (T) -> Unit): VLongArray<T> = apply { collection.onEach { action(a.fromLong(it)) }}
    context(a: ValueLongAdapter<T>) inline fun onEachIndexed(action: (index: Int, T) -> Unit): VLongArray<T> = apply{collection.onEachIndexed{i,e->action(i,a.fromLong(e)) }}
    context(a: ValueLongAdapter<T>) inline fun reduce(operation: (acc: T, T) -> T): T = a.fromLong(collection.reduce { acc, e -> a.toLong(operation(a.fromLong(acc), a.fromLong(e))) })
    context(a: ValueLongAdapter<T>) inline fun reduceIndexed(operation: (index: Int, acc: T, T) -> T): T = a.fromLong(collection.reduceIndexed { i, acc, e -> a.toLong(operation(i, a.fromLong(acc), a.fromLong(e))) })
    context(a: ValueLongAdapter<T>) inline fun reduceIndexedOrNull(operation: (index: Int, acc: T, T) -> T): T? = a.fromLong(collection.reduceIndexedOrNull { i, acc, e -> a.toLong(operation(i, a.fromLong(acc), a.fromLong(e))) })
    context(a: ValueLongAdapter<T>) inline fun reduceOrNull(operation: (acc: T, T) -> T): T? = a.fromLong(collection.reduceOrNull { acc, e -> a.toLong(operation(a.fromLong(acc), a.fromLong(e))) })
    context(a: ValueLongAdapter<T>) inline fun reduceRight(operation: (T, acc: T) -> T): T = a.fromLong(collection.reduceRight { e, acc -> a.toLong(operation(a.fromLong(e), a.fromLong(acc))) })
    context(a: ValueLongAdapter<T>) inline fun reduceRightIndexed(operation: (index: Int, T, acc: T) -> T): T = a.fromLong(collection.reduceRightIndexed { i, e, acc -> a.toLong(operation(i, a.fromLong(e), a.fromLong(acc))) })
    context(a: ValueLongAdapter<T>) inline fun reduceRightIndexedOrNull(operation: (index: Int, T, acc: T) -> T): T? = a.fromLong(collection.reduceRightIndexedOrNull { i, e, acc -> a.toLong(operation(i, a.fromLong(e), a.fromLong(acc))) })
    context(a: ValueLongAdapter<T>) inline fun reduceRightOrNull(operation: (T, acc: T) -> T): T? = a.fromLong(collection.reduceRightOrNull { e, acc -> a.toLong(operation(a.fromLong(e), a.fromLong(acc))) })
    context(a: ValueLongAdapter<T>) inline fun <R> runningFold(initial: R, operation: (acc: R, T) -> R): List<R> = collection.runningFold(initial, {acc,e->operation(acc,a.fromLong(e))})
    context(a: ValueLongAdapter<T>) inline fun <R> runningFoldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R> = collection.runningFoldIndexed(initial) {i,acc,e->operation(i,acc,a.fromLong(e))}
    context(a: ValueLongAdapter<T>) inline fun runningReduce(operation: (acc: T, T) -> T): MutableVLongList<T> = runningReduceIndexed { index, acc, t -> operation(acc,t) }
    context(a: ValueLongAdapter<T>) inline fun runningReduceIndexed(operation: (index: Int, acc: T, T) -> T): MutableVLongList<T> {
        if (collection.size < 2) return MutableVLongList()
        val r = MutableVLongList<T>(collection.size-1)
        var acc = a.fromLong(collection[0])
        for (i in 0 until collection.size-1) {
            acc = operation(i, acc, a.fromLong(collection[i]))
            r[i] = acc
        }
        return r
    }
    context(a: ValueLongAdapter<T>) inline fun <R> scan(initial: R, operation: (acc: R, T) -> R): List<R> = collection.scan(initial, {acc,e->operation(acc,a.fromLong(e))})
    context(a: ValueLongAdapter<T>) inline fun <R> scanIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R> = collection.scanIndexed(initial) {i,acc,e->operation(i,acc,a.fromLong(e))}
    context(a: ValueLongAdapter<T>) inline fun sumBy(selector: (T) -> Int): Int = collection.sumBy{selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun sumByDouble(selector: (T) -> Double): Double = collection.sumByDouble { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun sumOf(selector: (T) -> Double): Double = collection.sumOf { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun sumOf(selector: (T) -> Int): Int = collection.sumOf { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun sumOf(selector: (T) -> T): T = a.fromLong(collection.sumOf { a.toLong(selector(a.fromLong(it))) })
    context(a: ValueLongAdapter<T>) inline fun sumOf(selector: (T) -> Long): Long = collection.sumOf { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun sumOf(selector: (T) -> UInt): UInt = collection.sumOf { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun sumOf(selector: (T) -> ULong): ULong = collection.sumOf { selector(a.fromLong(it)) }
    context(a: ValueLongAdapter<T>) inline fun partition(predicate: (T) -> Boolean): Pair<MutableVLongList<T>, MutableVLongList<T>> {
        val mask = filterMask(predicate)
        val left = filterFromMask(mask)
        mask.flip(0,mask.size())
        return Pair(left, filterFromMask(mask))
    }
    context(a: ValueLongAdapter<T>) infix fun <R> zip(other: Array<out R>): List<Pair<T, R>> = collection.zip(other) {l,r -> a.fromLong(l) to r}
    context(a: ValueLongAdapter<T>) inline fun <R, V> zip(other: Array<out R>, transform: (T,R) -> V): List<V> = collection.zip(other) {l,r -> transform(a.fromLong(l), r) }
    context(a: ValueLongAdapter<T>) infix fun <R> zip(other: Iterable<R>): List<Pair<T, R>> = collection.zip(other) {l,r -> a.fromLong(l) to r}
    context(a: ValueLongAdapter<T>) inline fun <R, V> zip(other: Iterable<R>, transform: (T,R) -> V): List<V> = collection.zip(other) {l,r -> transform(a.fromLong(l), r) }
    context(a: ValueLongAdapter<T>) inline infix fun zip(other: VLongArray<T>): List<Pair<T, T>> = collection.zip(other.collection) {l,r -> a.fromLong(l) to a.fromLong(r)}
    context(a: ValueLongAdapter<T>) inline fun <V> zip(other: VLongArray<T>, transform: (T,T) -> V): List<V> = collection.zip(other.collection) {l,r -> transform(a.fromLong(l), a.fromLong(r)) }
    context(a: ValueLongAdapter<T>) inline fun <A : Appendable> joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence)={ it.toString()}): A
            = collection.joinTo(buffer, separator, prefix, postfix, limit, truncated, { transform(a.fromLong(it)) })
    context(a: ValueLongAdapter<T>) inline fun joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence)={ it.toString()}): String
            = collection.joinToString(separator, prefix, postfix, limit, truncated, { transform(a.fromLong(it)) })
    context(a: ValueLongAdapter<T>) inline fun asIterable(): Iterable<T> = VLongIterator(collection.asIterable().iterator(), a)
    context(a: ValueLongAdapter<T>) inline fun asSequence(): Sequence<T> = collection.asSequence().map { a.fromLong(it) }
    context(a: ValueLongAdapter<T>) inline fun average(): Double = collection.average()
    context(a: ValueLongAdapter<T>) inline fun sum(): T = a.fromLong(collection.sum())
}