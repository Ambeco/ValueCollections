@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused", "RedundantNullableReturnType",
    "KotlinConstantConditions", "KotlinConstantConditions"
)
package mpd.com.common.collect.valuecollections

import java.util.BitSet
import kotlin.collections.sort
import kotlin.collections.sortBy
import kotlin.collections.sortByDescending
import kotlin.collections.sortDescending
import kotlin.collections.sortWith
import kotlin.random.Random

interface VLongIndexedCollection<T> : CollectionVLong<T> {
    fun bitsAtIndex(index: Int): LongBits

    fun indexOfBits(bits: LongBits): Int
    fun indexOfFirstIndexedBits(startIndex:Int=0, predicate: (index:Int, bits:LongBits) -> Boolean): Int = indexOfFirstIndexedBitsDefault(startIndex, predicate)
    fun indexOfLastIndexedBits(endIndex:Int=-1, predicate: (index:Int, bits:LongBits) -> Boolean): Int = indexOfLastIndexedBitsDefault(endIndex, predicate)

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.asListGeneric() = object: List<T> {
    override val size: Int get() = this@asListGeneric.size
    override inline fun isEmpty(): Boolean = this@asListGeneric.size==0
    override inline fun contains(element: T): Boolean = this@asListGeneric.contains(element)
    override inline fun iterator(): Iterator<T> = this@asListGeneric.asIterable().iterator()
    override inline fun containsAll(elements: Collection<T>): Boolean = this@asListGeneric.containsAll(elements)
    override inline fun get(index: Int): T = this@asListGeneric.get(index)
    override inline fun indexOf(element: T): Int = this@asListGeneric.indexOf(element)
    override inline fun lastIndexOf(element: T): Int = this@asListGeneric.lastIndexOf(element)
    override inline fun listIterator(): ListIterator<T> = throw NotImplementedError() // this@asListGeneric.listIterator()
    override inline fun listIterator(index: Int): ListIterator<T> = throw NotImplementedError() // this@asListGeneric.listIterator(index)
    override inline fun subList(fromIndex: Int, toIndex: Int): List<T> = throw NotImplementedError() // this@asListGeneric.subList(fromIndex, toIndex)
}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.anyIndexed(crossinline action: (index:Int, T) -> Boolean) = any(
    object: (T) -> Boolean {
        var index = 0
        override inline fun invoke(v: T) = action(index++, v)
    }
)
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.allIndexed(crossinline action: (index:Int, T) -> Boolean) = all(
    object: (T) -> Boolean {
        var index = 0
        override inline fun invoke(v: T) = action(index++, v)
    }
)
inline fun <T> VLongIndexedCollection<T>.contentEquals(other: VLongIndexedCollection<T>?): Boolean = other != null && size == other.size && allIndexedBits {i,b-> other.bitsAtIndex(i)==b }

context(a: ValueLongAdapter<T>) inline operator fun <T> VLongIndexedCollection<T>.component1(): T = elementAtIndex(0)
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongIndexedCollection<T>.component2(): T = elementAtIndex(1)
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongIndexedCollection<T>.component3(): T = elementAtIndex(2)
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongIndexedCollection<T>.component4(): T = elementAtIndex(3)
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongIndexedCollection<T>.component5(): T = elementAtIndex(4)
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.elementAtIndex(index: Int): T = fromLong(bitsAtIndex(index))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.elementAtOrNull(index: Int): T? = if(index in 0..<size) elementAtIndex(index) else null
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.elementAtOrElse(index: Int, defaultValue: (index:Int) -> T): T = if(index in 0..<size)elementAtIndex(index) else defaultValue(index)
inline fun <T> VLongIndexedCollection<T>.getBits(index: Int): LongBits = if (index in 0..<size) bitsAtIndex(index) else NULL_VALUE
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.get(index: Int): T = if (index in 0..<size) elementAtIndex(index) else throw IndexOutOfBoundsException("$index not in 0..$size")
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.getOrElse(index: Int, defaultValue: (index:Int) -> T): T = if (index in 0..<size) elementAtIndex(index) else defaultValue(index)
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.getOrNull(index: Int): T? = if (index in 0..<size) elementAtIndex(index) else null
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.findLast(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.first(): T = elementAtIndex(0)
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.first(crossinline predicate: (T) -> Boolean): T = find(predicate) ?: throw NoSuchElementException()
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongIndexedCollection<T>.firstNotNullOf(crossinline transform: (T) -> R?): R = firstNotNullOfOrNull(transform) ?: throw NoSuchElementException()
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongIndexedCollection<T>.firstNotNullOfOrNull(crossinline transform: (T) -> R?): R? { for(i in 0 ..< size) return transform(elementAtIndex(i)) ?: continue; return null }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.firstOrNull(): T? = elementAtOrNull(0)
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.firstOrNull(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfFirst(predicate))
context(a: ValueLongAdapter<T>)fun <T> VLongIndexedCollection<T>.indexOf(element: T): Int = indexOfFirst {it==element}
inline fun <T> VLongIndexedCollection<T>.indexOfFirstBits(crossinline predicate: (LongBits) -> Boolean): Int { for(i in 0 ..< size) if (predicate(bitsAtIndex(i))) return i; return -1 }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.indexOfFirst(crossinline predicate: (T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.indexOfFirstIndexed(crossinline predicate: (index:Int,T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T> VLongIndexedCollection<T>.indexOfFirstIndexedBitsDefault(startIndex:Int=0, crossinline predicate: (index:Int, bits:LongBits) -> Boolean): Int { for(i in startIndex ..< size) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.indexOfLast(crossinline predicate: (T) -> Boolean): Int { for(i in size-1..0) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.indexOfLastIndexed(crossinline predicate: (index:Int,T) -> Boolean): Int { for(i in size-1..0) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T> VLongIndexedCollection<T>.indexOfLastIndexedBitsDefault(startIndex:Int=-1, crossinline predicate: (index:Int, bits:LongBits) -> Boolean): Int {val start=if(startIndex<0||startIndex>size-1)size-1 else startIndex; for(i in start..0) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.last(): T = elementAtIndex(size-1)
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.last(crossinline predicate: (T) -> Boolean): T = findLast(predicate) ?: throw NoSuchElementException()
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.lastIndexOf(element: T): Int = indexOfLast {it==element}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.lastOrNull(): T? = elementAtOrNull(size - 1)
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.lastOrNull(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.drop(n: Int): ArrayVLongList<T> = slice(IntRange(n,size-1))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.dropLast(n: Int): ArrayVLongList<T> = slice(IntRange(0,size-n))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.dropWhile(crossinline predicate: (T) -> Boolean): ArrayVLongList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) ArrayVLongList(this) else slice(IntRange(i, size))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.dropLastWhile(crossinline predicate: (T) -> Boolean): ArrayVLongList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(0, i))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filter(crossinline predicate: (T) -> Boolean): ArrayVLongList<T> = filterFromMask(filterMask(predicate))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filterMask(crossinline predicate: (T) -> Boolean): BitSet = filterIndexedMask {_,e->predicate(e)}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filterFromMask(mask: BitSet): ArrayVLongList<T> = ArrayVLongList<T>(mask.cardinality()).also {c-> forEachIndexedBits {i,e-> if(mask[i]) c.addBits(e)} }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filterIndexed(crossinline predicate: (index: Int, T) -> Boolean): ArrayVLongList<T> = filterFromMask(filterIndexedMask(predicate))
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollectionVLong<T>> VLongIndexedCollection<T>.filterIndexedTo(destination: C, crossinline predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollection<T>> VLongIndexedCollection<T>.filterIndexedTo(destination: C, crossinline predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filterIndexedMask(crossinline predicate: (index: Int, T) -> Boolean): BitSet {val destination=BitSet(size); forEachIndexed { i, e -> destination.set(i,predicate(i, e))}; return destination }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filterNot(crossinline predicate: (T) -> Boolean): VLongList<T> = filter {!predicate(it)}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filterNotIndexed(crossinline predicate: (Int,T) -> Boolean): ArrayVLongList<T> = filterIndexed {i,e->!predicate(i,e) }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollectionVLong<T>> VLongIndexedCollection<T>.filterNotIndexedTo(destination: C, crossinline predicate: (Int, T) -> Boolean): C = filterIndexedTo(destination) { i, e->!predicate(i,e) }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongIndexedCollection<T>> VLongIndexedCollection<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollection<T>> VLongIndexedCollection<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongIndexedCollection<T>> VLongIndexedCollection<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollection<T>> VLongIndexedCollection<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.slice(indices: IntRange): ArrayVLongList<T> = copyInto(ArrayVLongList<T>(indices.last-indices.first, NULL_VALUE), 0, indices.first, indices.last)
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.slice(indices: Iterable<Int>): ArrayVLongList<T> = ArrayVLongList<T>(if (indices is Collection<Int>) indices.size else size/8).also { for(i in indices) it.addBits(bitsAtIndex(i)) }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.sliceArray(indices: Collection<Int>): ArrayVLong<T> = ArrayVLong<T>(indices.size, NULL_VALUE).also { c-> indices.forEachIndexed { i, ei-> c.set(i, get(ei))}}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.sliceArray(indices: IntRange): ArrayVLong<T> = ArrayVLong<T>(indices.last -indices.first +1, NULL_VALUE).also { c-> for (i in indices) c.set(i-indices.first, get(i))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.take(n: Int): ArrayVLongList<T> = slice(IntRange(0,n))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.takeLast(n: Int): ArrayVLongList<T> = slice(IntRange(size-n,size))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.takeLastWhile(crossinline predicate: (T) -> Boolean): ArrayVLongList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) ArrayVLongList<T>(this) else slice(IntRange(0, i))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.slice(indices: Collection<Int>): ArrayVLongList<T> = filterIndexed {i,e-> indices.contains(i) }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.takeWhile(crossinline predicate: (T) -> Boolean): ArrayVLongList<T> = ArrayVLongList<T>().also {c-> any { val p=predicate(it); if (p) c.add(it); p } }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.takeWhileIndexed(crossinline predicate: (Int,T) -> Boolean): ArrayVLongList<T> = ArrayVLongList<T>().also {c-> anyIndexed {i,e-> val p=predicate(i,e); if (p) c.add(e); p } }
inline fun <T, C: MutableVLongIndexedCollection<T>> VLongIndexedCollection<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{forEachIndexedBits{i,e-> if(i in startIndex..endIndex) destination.addBits(i-startIndex+destinationOffset, e)}}
context(a: ValueLongAdapter<T>) inline fun <T, C: MutableList<T>> VLongIndexedCollection<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{forEachIndexed{i,e-> if(i in startIndex..endIndex) destination.add(i-startIndex+destinationOffset, e)}}
inline fun <T> VLongIndexedCollection<T>.reversed(): ArrayVLongList<T> = ArrayVLongList<T>(size).also {forEachIndexedBits{i,e-> it.setBits(size-i-1, e) }}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.shuffle(): Unit = shuffle(Random.Default)
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.shuffle(random: Random): Unit {
}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.zipWithNext(): List<Pair<T, T>> = zipWithNext {l,r -> l to r}
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongIndexedCollection<T>.zipWithNext(crossinline transform: (a: T, b: T) -> R): List<R> = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <S, T : S> VLongIndexedCollection<T>.reduceRight(crossinline operation: (T, acc: T) -> T): T = reduceRightIndexed {i,e,acc -> operation(e,acc)}
context(a: ValueLongAdapter<T>) inline fun <S, T : S> VLongIndexedCollection<T>.reduceRightIndexed(crossinline operation: (index: Int, T, acc: T) -> T): T = throw NotImplementedError()
context(a: ValueLongAdapter<T>) inline fun <S, T : S> VLongIndexedCollection<T>.reduceRightIndexedOrNull(crossinline operation: (index: Int, T, acc: T) -> T): T? = if (size<2) null else reduceRightIndexed(operation)
context(a: ValueLongAdapter<T>) inline fun <S, T : S> VLongIndexedCollection<T>.reduceRightOrNull(crossinline operation: (T, acc: T) -> T): T? = if (size<2) null else reduceRight(operation)
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.windowed(windowSize: Int, step: Int = 1, partialWindows: Boolean = false): MutableList<MutableList<T>> {
    val list = MutableList<MutableList<T>>(size-windowSize) { mutableListOf() }
    for (i in 0 ..< size-windowSize) {
        for (j in 0..<windowSize)
            list[i].add(elementAtIndex(i+j))
    }
    return list
}
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongIndexedCollection<T>.windowed(windowSize: Int, step: Int = 1, partialWindows: Boolean = false, crossinline transform: (List<T>) -> R): List<R> = throw NotImplementedError()
