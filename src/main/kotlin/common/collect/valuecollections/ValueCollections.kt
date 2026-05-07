@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import java.util.PrimitiveIterator
import java.util.function.Consumer
import kotlin.random.Random


open class VIntIterator<T>(val delegate:Iterator<Int>, val a: ValueIntAdapter<T>): PrimitiveIterator<T, Consumer<T>> {
    final override inline fun hasNext(): Boolean = delegate.hasNext()
    override fun remove(): Unit = throw UnsupportedOperationException("remove")
    final override inline fun next(): T = nextInt()
    inline fun nextInt(): T = if (delegate is IntIterator) a.fromInt(delegate.nextInt()) else a.fromInt(delegate.next())
    final override inline fun forEachRemaining(op: Consumer<T>?) { while (delegate.hasNext()) op?.accept(nextInt()) }
}
class MutableVIntIterator<T>(delegate:MutableIterator<Int>, a: ValueIntAdapter<T>): VIntIterator<T>(delegate,a) {
    override inline fun remove(): Unit = (delegate as MutableIterator<Int>).remove()
}
class VIterableInt<T>(val delegate: Iterable<Int>, val adapter: ValueIntAdapter<T>): Iterable<T> {
    constructor(delegate: IntArray, adapter: ValueIntAdapter<T>): this(delegate.asIterable(), adapter)
    override fun iterator(): VIntIterator<T> = VIntIterator(delegate.iterator(), adapter)
}
class VIteratorIndexedValueInt<T>(val delegate:Iterator<IndexedValue<Int>>, val a: ValueIntAdapter<T>): Iterator<IndexedValue<T>> {
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun next(): IndexedValue<T> {val iv=delegate.next(); return IndexedValue(iv.index, a.fromInt(iv.value)) }
}
class VIterableIndexedValueInt<T>(val delegate: Iterable<IndexedValue<Int>>, val adapter: ValueIntAdapter<T>): Iterable<IndexedValue<T>> {
    override fun iterator(): VIteratorIndexedValueInt<T> = VIteratorIndexedValueInt(delegate.iterator(), adapter)
}





class VLongIterator<T>(val delegate:Iterator<Long>, val a: ValueLongAdapter<T>): PrimitiveIterator<T, Consumer<T>> {
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun remove(): Unit = throw UnsupportedOperationException("remove")
    override inline fun next(): T = nextLong()
    inline fun nextLong(): T = if (delegate is LongIterator) a.fromLong(delegate.nextLong()) else a.fromLong(delegate.next())
    override inline fun forEachRemaining(op: Consumer<T>?) { while (delegate.hasNext()) op?.accept(nextLong()) }
}
class VIterableLong<T>(val delegate: Iterable<Long>, val adapter: ValueLongAdapter<T>): Iterable<T> {
    override fun iterator(): VLongIterator<T> = VLongIterator(delegate.iterator(), adapter)
}
class VIteratorIndexedValueLong<T>(val delegate:Iterator<IndexedValue<Long>>, val a: ValueLongAdapter<T>): Iterator<IndexedValue<T>> {
    override inline fun hasNext(): Boolean = delegate.hasNext()
    override inline fun next(): IndexedValue<T> {val iv=delegate.next(); return IndexedValue(iv.index, a.fromLong(iv.value)) }
}
class VIterableIndexedValueLong<T>(val delegate: Iterable<IndexedValue<Long>>, val adapter: ValueLongAdapter<T>): Iterable<IndexedValue<T>> {
    override fun iterator(): VIteratorIndexedValueLong<T> = VIteratorIndexedValueLong(delegate.iterator(), adapter)
}


// Kotlin _Collections Iterator extension methods
interface VIntIterable<T> : Iterable<T> {
    val a: ValueIntAdapter<T>
    override operator fun iterator(): VIntIterator<T>
}
/*
operator fun <@kotlin.internal.OnlyInputTypes T> Iterable<T>.contains(element: T): Boolean
fun <T> Iterable<T>.elementAt(index: Int): T
fun <T> Iterable<T>.elementAtOrElse(index: Int, defaultValue: (Int) -> T): T
fun <T> Iterable<T>.elementAtOrNull(index: Int): T?
inline fun <T> Iterable<T>.find(predicate: (T) -> Boolean): T?
inline fun <T> Iterable<T>.findLast(predicate: (T) -> Boolean): T?
fun <T> Iterable<T>.first(): T
inline fun <T> Iterable<T>.first(predicate: (T) -> Boolean): T
inline fun <T, R : Any> Iterable<T>.firstNotNullOf(transform: (T) -> R?): R
inline fun <T, R : Any> Iterable<T>.firstNotNullOfOrNull(transform: (T) -> R?): R?
fun <T> Iterable<T>.firstOrNull(): T?
inline fun <T> Iterable<T>.firstOrNull(predicate: (T) -> Boolean): T?
fun <@kotlin.internal.OnlyInputTypes T> Iterable<T>.indexOf(element: T): Int
inline fun <T> Iterable<T>.indexOfFirst(predicate: (T) -> Boolean): Int
inline fun <T> Iterable<T>.indexOfLast(predicate: (T) -> Boolean): Int
fun <T> Iterable<T>.last(): T
inline fun <T> Iterable<T>.last(predicate: (T) -> Boolean): T
fun <@kotlin.internal.OnlyInputTypes T> Iterable<T>.lastIndexOf(element: T): Int
fun <T> Iterable<T>.lastOrNull(): T?
inline fun <T> Iterable<T>.lastOrNull(predicate: (T) -> Boolean): T?
fun <T> Iterable<T>.single(): T
inline fun <T> Iterable<T>.single(predicate: (T) -> Boolean): T
fun <T> Iterable<T>.singleOrNull(): T?
inline fun <T> Iterable<T>.singleOrNull(predicate: (T) -> Boolean): T?
fun <T> Iterable<T>.drop(n: Int): List<T>
inline fun <T> Iterable<T>.dropWhile(predicate: (T) -> Boolean): List<T>
inline fun <T> Iterable<T>.filter(predicate: (T) -> Boolean): List<T>
inline fun <T> Iterable<T>.filterIndexed(predicate: (index: Int, T) -> Boolean): List<T>
inline fun <T, C : MutableCollection<in T>> Iterable<T>.filterIndexedTo(destination: C, predicate: (index: Int, T) -> Boolean): C
inline fun <T> Iterable<T>.filterNot(predicate: (T) -> Boolean): List<T>
inline fun <T, C : MutableCollection<in T>> Iterable<T>.filterNotTo(destination: C, predicate: (T) -> Boolean): C
inline fun <T, C : MutableCollection<in T>> Iterable<T>.filterTo(destination: C, predicate: (T) -> Boolean): C
fun <T> Iterable<T>.take(n: Int): List<T>
inline fun <T> Iterable<T>.takeWhile(predicate: (T) -> Boolean): List<T>
fun <T> Iterable<T>.reversed(): List<T>
fun <T : Comparable<T>> Iterable<T>.sorted(): List<T>
inline fun <T, R : Comparable<R>> Iterable<T>.sortedBy(crossinline selector: (T) -> R?): List<T>
inline fun <T, R : Comparable<R>> Iterable<T>.sortedByDescending(crossinline selector: (T) -> R?): List<T>
fun <T : Comparable<T>> Iterable<T>.sortedDescending(): List<T>
fun <T> Iterable<T>.sortedWith(comparator: Comparator<in T>): List<T>
inline fun <T, K, V> Iterable<T>.associate(transform: (T) -> Pair<K, V>): Map<K, V>
inline fun <T, K> Iterable<T>.associateBy(keySelector: (T) -> K): Map<K, T>
inline fun <T, K, V> Iterable<T>.associateBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, V>
inline fun <T, K, M : MutableMap<in K, in T>> Iterable<T>.associateByTo(destination: M, keySelector: (T) -> K): M
inline fun <T, K, V, M : MutableMap<in K, in V>> Iterable<T>.associateByTo(destination: M, keySelector: (T) -> K, valueTransform: (T) -> V): M
inline fun <T, K, V, M : MutableMap<in K, in V>> Iterable<T>.associateTo(destination: M, transform: (T) -> Pair<K, V>): M
fun <T, C : MutableCollection<in T>> Iterable<T>.toCollection(destination: C): C
fun <T> Iterable<T>.toHashSet(): HashSet<T>
fun <T> Iterable<T>.toList(): List<T>
fun <T> Iterable<T>.toMutableList(): MutableList<T>
fun <T> Iterable<T>.toSet(): Set<T>
inline fun <T, R> Iterable<T>.flatMap(transform: (T) -> Iterable<R>): List<R>
inline fun <T, R> Iterable<T>.flatMap(transform: (T) -> Sequence<R>): List<R>
inline fun <T, R> Iterable<T>.flatMapIndexed(transform: (index: Int, T) -> Iterable<R>): List<R>
inline fun <T, R> Iterable<T>.flatMapIndexed(transform: (index: Int, T) -> Sequence<R>): List<R>
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapIndexedTo(destination: C, transform: (index: Int, T) -> Iterable<R>): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapIndexedTo(destination: C, transform: (index: Int, T) -> Sequence<R>): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapTo(destination: C, transform: (T) -> Iterable<R>): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapTo(destination: C, transform: (T) -> Sequence<R>): C
inline fun <T, K> Iterable<T>.groupBy(keySelector: (T) -> K): Map<K, List<T>>
inline fun <T, K, V> Iterable<T>.groupBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, List<V>>
inline fun <T, K, M : MutableMap<in K, MutableList<T>>> Iterable<T>.groupByTo(destination: M, keySelector: (T) -> K): M
inline fun <T, K, V, M : MutableMap<in K, MutableList<V>>> Iterable<T>.groupByTo(destination: M, keySelector: (T) -> K, valueTransform: (T) -> V): M
inline fun <T, K> Iterable<T>.groupingBy(crossinline keySelector: (T) -> K): Grouping<T, K>
inline fun <T, R> Iterable<T>.map(transform: (T) -> R): List<R>
inline fun <T, R> Iterable<T>.mapIndexed(transform: (index: Int, T) -> R): List<R>
inline fun <T, R : Any> Iterable<T>.mapIndexedNotNull(transform: (index: Int, T) -> R?): List<R>
inline fun <T, R : Any, C : MutableCollection<in R>> Iterable<T>.mapIndexedNotNullTo(destination: C, transform: (index: Int, T) -> R?): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.mapIndexedTo(destination: C, transform: (index: Int, T) -> R): C
inline fun <T, R : Any> Iterable<T>.mapNotNull(transform: (T) -> R?): List<R>
inline fun <T, R : Any, C : MutableCollection<in R>> Iterable<T>.mapNotNullTo(destination: C, transform: (T) -> R?): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.mapTo(destination: C, transform: (T) -> R): C
fun <T> Iterable<T>.withIndex(): Iterable<IndexedValue<T>>
fun <T> Iterable<T>.distinct(): List<T>
inline fun <T, K> Iterable<T>.distinctBy(selector: (T) -> K): List<T>
infix fun <T> Iterable<T>.intersect(other: Iterable<T>): Set<T>
infix fun <T> Iterable<T>.subtract(other: Iterable<T>): Set<T>
fun <T> Iterable<T>.toMutableSet(): MutableSet<T>
infix fun <T> Iterable<T>.union(other: Iterable<T>): Set<T>
inline fun <T> Iterable<T>.all(predicate: (T) -> Boolean): Boolean
fun <T> Iterable<T>.any(): Boolean
inline fun <T> Iterable<T>.any(predicate: (T) -> Boolean): Boolean
fun <T> Iterable<T>.count(): Int
inline fun <T> Iterable<T>.count(predicate: (T) -> Boolean): Int
inline fun <T, R> Iterable<T>.fold(initial: R, operation: (acc: R, T) -> R): R
inline fun <T, R> Iterable<T>.foldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): R
inline fun <T> Iterable<T>.forEach(action: (T) -> Unit): Unit
inline fun <T> Iterable<T>.forEachIndexed(action: (index: Int, T) -> Unit): Unit
fun <T : Comparable<T>> Iterable<T>.max(): T
inline fun <T, R : Comparable<R>> Iterable<T>.maxBy(selector: (T) -> R): T
inline fun <T, R : Comparable<R>> Iterable<T>.maxByOrNull(selector: (T) -> R): T?
inline fun <T> Iterable<T>.maxOf(selector: (T) -> Double): Double
inline fun <T> Iterable<T>.maxOf(selector: (T) -> Float): Float
inline fun <T, R : Comparable<R>> Iterable<T>.maxOf(selector: (T) -> R): R
inline fun <T> Iterable<T>.maxOfOrNull(selector: (T) -> Double): Double?
inline fun <T> Iterable<T>.maxOfOrNull(selector: (T) -> Float): Float?
inline fun <T, R : Comparable<R>> Iterable<T>.maxOfOrNull(selector: (T) -> R): R?
inline fun <T, R> Iterable<T>.maxOfWith(comparator: Comparator<in R>, selector: (T) -> R): R
inline fun <T, R> Iterable<T>.maxOfWithOrNull(comparator: Comparator<in R>, selector: (T) -> R): R?
fun <T : Comparable<T>> Iterable<T>.maxOrNull(): T?
fun <T> Iterable<T>.maxWith(comparator: Comparator<in T>): T
fun <T> Iterable<T>.maxWithOrNull(comparator: Comparator<in T>): T?
fun <T : Comparable<T>> Iterable<T>.min(): T
inline fun <T, R : Comparable<R>> Iterable<T>.minBy(selector: (T) -> R): T
inline fun <T, R : Comparable<R>> Iterable<T>.minByOrNull(selector: (T) -> R): T?
inline fun <T> Iterable<T>.minOf(selector: (T) -> Double): Double
inline fun <T> Iterable<T>.minOf(selector: (T) -> Float): Float
inline fun <T, R : Comparable<R>> Iterable<T>.minOf(selector: (T) -> R): R
inline fun <T> Iterable<T>.minOfOrNull(selector: (T) -> Double): Double?
inline fun <T> Iterable<T>.minOfOrNull(selector: (T) -> Float): Float?
inline fun <T, R : Comparable<R>> Iterable<T>.minOfOrNull(selector: (T) -> R): R?
inline fun <T, R> Iterable<T>.minOfWith(comparator: Comparator<in R>, selector: (T) -> R): R
inline fun <T, R> Iterable<T>.minOfWithOrNull(comparator: Comparator<in R>, selector: (T) -> R): R?
fun <T : Comparable<T>> Iterable<T>.minOrNull(): T?
fun <T> Iterable<T>.minWith(comparator: Comparator<in T>): T
fun <T> Iterable<T>.minWithOrNull(comparator: Comparator<in T>): T?
fun <T> Iterable<T>.none(): Boolean
inline fun <T> Iterable<T>.none(predicate: (T) -> Boolean): Boolean
inline fun <S, T : S> Iterable<T>.reduce(operation: (acc: S, T) -> S): S
inline fun <S, T : S> Iterable<T>.reduceIndexed(operation: (index: Int, acc: S, T) -> S): S
inline fun <S, T : S> Iterable<T>.reduceIndexedOrNull(operation: (index: Int, acc: S, T) -> S): S?
inline fun <S, T : S> Iterable<T>.reduceOrNull(operation: (acc: S, T) -> S): S?
inline fun <T, R> Iterable<T>.runningFold(initial: R, operation: (acc: R, T) -> R): List<R>
inline fun <T, R> Iterable<T>.runningFoldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R>
inline fun <S, T : S> Iterable<T>.runningReduce(operation: (acc: S, T) -> S): List<S>
inline fun <S, T : S> Iterable<T>.runningReduceIndexed(operation: (index: Int, acc: S, T) -> S): List<S>
inline fun <T, R> Iterable<T>.scan(initial: R, operation: (acc: R, T) -> R): List<R>
inline fun <T, R> Iterable<T>.scanIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R>
inline fun <T> Iterable<T>.sumBy(selector: (T) -> Int): Int
inline fun <T> Iterable<T>.sumByDouble(selector: (T) -> Double): Double
inline fun <T> Iterable<T>.sumOf(selector: (T) -> Double): Double
inline fun <T> Iterable<T>.sumOf(selector: (T) -> Int): Int
inline fun <T> Iterable<T>.sumOf(selector: (T) -> Long): Long
inline fun <T> Iterable<T>.sumOf(selector: (T) -> UInt): UInt
inline fun <T> Iterable<T>.sumOf(selector: (T) -> ULong): ULong
fun <T> Iterable<T>.chunked(size: Int): List<List<T>>
fun <T, R> Iterable<T>.chunked(size: Int, transform: (List<T>) -> R): List<R>
operator fun <T> Iterable<T>.minus(element: T): List<T>
operator fun <T> Iterable<T>.minus(elements: Array<out T>): List<T>
operator fun <T> Iterable<T>.minus(elements: Iterable<T>): List<T>
operator fun <T> Iterable<T>.minus(elements: Sequence<T>): List<T>
inline fun <T> Iterable<T>.minusElement(element: T): List<T>
inline fun <T> Iterable<T>.partition(predicate: (T) -> Boolean): Pair<List<T>, List<T>>
operator fun <T> Iterable<T>.plus(element: T): List<T>
operator fun <T> Iterable<T>.plus(elements: Array<out T>): List<T>
operator fun <T> Iterable<T>.plus(elements: Iterable<T>): List<T>
operator fun <T> Iterable<T>.plus(elements: Sequence<T>): List<T>
inline fun <T> Iterable<T>.plusElement(element: T): List<T>
fun <T> Iterable<T>.windowed(size: Int, step: Int = 1, partialWindows: Boolean = false): List<List<T>>
fun <T, R> Iterable<T>.windowed(size: Int, step: Int = 1, partialWindows: Boolean = false, transform: (List<T>) -> R): List<R>
infix fun <T, R> Iterable<T>.zip(other: Array<out R>): List<Pair<T, R>>
inline fun <T, R, V> Iterable<T>.zip(other: Array<out R>, transform: (a: T, b: R) -> V): List<V>
infix fun <T, R> Iterable<T>.zip(other: Iterable<R>): List<Pair<T, R>>
inline fun <T, R, V> Iterable<T>.zip(other: Iterable<R>, transform: (a: T, b: R) -> V): List<V>
fun <T> Iterable<T>.zipWithNext(): List<Pair<T, T>>
inline fun <T, R> Iterable<T>.zipWithNext(transform: (a: T, b: T) -> R): List<R>
fun <T, A : Appendable> Iterable<T>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): A
fun <T> Iterable<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String
inline fun <T> Iterable<T>.asIterable(): Iterable<T>
fun <T> Iterable<T>.asSequence(): Sequence<T>
*/
interface MutableVIntIterable<T>: VIntIterable<T> {
    override fun iterator(): MutableVIntIterator<T>
}

// Kotlin _Collections Collection extension methods
interface VIntCollection<T>: VIntIterable<T> {
    val size: Int
    fun isEmpty(): Boolean
    context(a: ValueIntAdapter<T>) operator fun contains(element: @UnsafeVariance T): Boolean
    context(a: ValueIntAdapter<T>) fun containsAll(elements: Collection<@UnsafeVariance T>): Boolean
}
/*
public inline fun <T> Collection<T>.random(): T
public fun <T> Collection<T>.random(random: Random): T
public inline fun <T> Collection<T>.randomOrNull(): T?
public fun <T> Collection<T>.randomOrNull(random: Random): T?
public fun <T> Collection<T>.toMutableList(): MutableList<T>
public inline fun <T> Collection<T>.count(): Int
public operator fun <T> Collection<T>.plus(element: T): List<T>
public operator fun <T> Collection<T>.plus(elements: Array<out T>): List<T>
public operator fun <T> Collection<T>.plus(elements: Iterable<T>): List<T>
public operator fun <T> Collection<T>.plus(elements: Sequence<T>): List<T>
public inline fun <T> Collection<T>.plusElement(element: T): List<T> 
*/
interface MutableVIntCollection<T>: VIntCollection<T>, MutableVIntIterable<T> {
    override fun iterator(): MutableVIntIterator<T>
    context(a: ValueIntAdapter<T>) fun add(element: T): Boolean
    context(a: ValueIntAdapter<T>) fun remove(element: T): Boolean
    context(a: ValueIntAdapter<T>) fun addAll(elements: Collection<T>): Boolean
    context(a: ValueIntAdapter<T>) fun removeAll(elements: Collection<T>): Boolean
    fun clear(): Unit    
}
/*
inline fun <T, C : MutableCollection<in T>> Iterable<T>.filterIndexedTo(destination: C, predicate: (index: Int, T) -> Boolean): C
inline fun <reified R, C : MutableCollection<in R>> Iterable<*>.filterIsInstanceTo(destination: C): C
fun <C : MutableCollection<in T>, T : Any> Iterable<T?>.filterNotNullTo(destination: C): C
inline fun <T, C : MutableCollection<in T>> Iterable<T>.filterNotTo(destination: C, predicate: (T) -> Boolean): C
inline fun <T, C : MutableCollection<in T>> Iterable<T>.filterTo(destination: C, predicate: (T) -> Boolean): C
fun <T, C : MutableCollection<in T>> Iterable<T>.toCollection(destination: C): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapIndexedTo(destination: C, transform: (index: Int, T) -> Iterable<R>): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapIndexedTo(destination: C, transform: (index: Int, T) -> Sequence<R>): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapTo(destination: C, transform: (T) -> Iterable<R>): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapTo(destination: C, transform: (T) -> Sequence<R>): C
inline fun <T, R : Any, C : MutableCollection<in R>> Iterable<T>.mapIndexedNotNullTo(destination: C, transform: (index: Int, T) -> R?): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.mapIndexedTo(destination: C, transform: (index: Int, T) -> R): C
inline fun <T, R : Any, C : MutableCollection<in R>> Iterable<T>.mapNotNullTo(destination: C, transform: (T) -> R?): C
inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.mapTo(destination: C, transform: (T) -> R): C
*/