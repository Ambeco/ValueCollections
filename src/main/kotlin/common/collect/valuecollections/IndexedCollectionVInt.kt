@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused", "RedundantNullableReturnType",
    "KotlinConstantConditions", "KotlinConstantConditions"
)
package mpd.com.common.collect.valuecollections

import java.util.BitSet
import kotlin.random.Random


interface IndexedCollectionVInt<T> : CollectionVInt<T> {
    fun bitsAtIndex(index: Int): IntBits

    fun indexOfBits(bits: IntBits): Int
    fun indexOfFirstIndexedBits(startIndex:Int=0, predicate: (index:Int, bits:IntBits) -> Boolean): Int = indexOfFirstIndexedBitsDefault(startIndex, predicate)
    fun indexOfLastIndexedBits(endIndex:Int=-1, predicate: (index:Int, bits:IntBits) -> Boolean): Int = indexOfLastIndexedBitsDefault(endIndex, predicate)

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.asListGeneric() = object: List<T> {
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
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.anyIndexed(crossinline action: (index:Int, T) -> Boolean) = any(
    object: (T) -> Boolean {
        var index = 0
        override inline fun invoke(v: T) = action(index++, v)
    }
)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.allIndexed(crossinline action: (index:Int, T) -> Boolean) = all(
    object: (T) -> Boolean {
        var index = 0
        override inline fun invoke(v: T) = action(index++, v)
    }
)
inline fun <T> IndexedCollectionVInt<T>.contentEquals(other: IndexedCollectionVInt<T>?): Boolean = other != null && size == other.size && allIndexedBits { i, b-> other.bitsAtIndex(i)==b }

context(a: ValueIntAdapter<T>) inline operator fun <T> IndexedCollectionVInt<T>.component1(): T = elementAtIndex(0)
context(a: ValueIntAdapter<T>) inline operator fun <T> IndexedCollectionVInt<T>.component2(): T = elementAtIndex(1)
context(a: ValueIntAdapter<T>) inline operator fun <T> IndexedCollectionVInt<T>.component3(): T = elementAtIndex(2)
context(a: ValueIntAdapter<T>) inline operator fun <T> IndexedCollectionVInt<T>.component4(): T = elementAtIndex(3)
context(a: ValueIntAdapter<T>) inline operator fun <T> IndexedCollectionVInt<T>.component5(): T = elementAtIndex(4)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.elementAtIndex(index: Int): T = fromInt(bitsAtIndex(index))
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.elementAtOrNull(index: Int): T? = if(index in 0..<size) elementAtIndex(index) else null
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.elementAtOrElse(index: Int, defaultValue: (index:Int) -> T): T = if(index in 0..<size)elementAtIndex(index) else defaultValue(index)
inline fun <T> IndexedCollectionVInt<T>.getBits(index: Int): IntBits = if (index in 0..<size) bitsAtIndex(index) else NULL_VALUE
context(a: ValueIntAdapter<T>) inline operator fun <T> IndexedCollectionVInt<T>.get(index: Int): T = if (index in 0..<size) elementAtIndex(index) else throw IndexOutOfBoundsException("$index not in 0..$size")
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.getOrElse(index: Int, defaultValue: (index:Int) -> T): T = if (index in 0..<size) elementAtIndex(index) else defaultValue(index)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.getOrNull(index: Int): T? = if (index in 0..<size) elementAtIndex(index) else null
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.findLast(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate))
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.first(): T = elementAtIndex(0)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.first(crossinline predicate: (T) -> Boolean): T = find(predicate) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T, R> IndexedCollectionVInt<T>.firstNotNullOf(crossinline transform: (T) -> R?): R = firstNotNullOfOrNull(transform) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T, R> IndexedCollectionVInt<T>.firstNotNullOfOrNull(crossinline transform: (T) -> R?): R? { for(i in 0 ..< size) return transform(elementAtIndex(i)) ?: continue; return null }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.firstOrNull(): T? = elementAtOrNull(0)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.firstOrNull(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfFirst(predicate))
context(a: ValueIntAdapter<T>)fun <T> IndexedCollectionVInt<T>.indexOf(element: T): Int = indexOfFirst {it==element}
inline fun <T> IndexedCollectionVInt<T>.indexOfFirstBits(crossinline predicate: (IntBits) -> Boolean): Int { for(i in 0 ..< size) if (predicate(bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.indexOfFirst(crossinline predicate: (T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.indexOfFirstIndexed(crossinline predicate: (index:Int, T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T> IndexedCollectionVInt<T>.indexOfFirstIndexedBitsDefault(startIndex:Int=0, crossinline predicate: (index:Int, bits:IntBits) -> Boolean): Int { for(i in startIndex ..< size) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.indexOfLast(crossinline predicate: (T) -> Boolean): Int { for(i in size-1..0) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.indexOfLastIndexed(crossinline predicate: (index:Int, T) -> Boolean): Int { for(i in size-1..0) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T> IndexedCollectionVInt<T>.indexOfLastIndexedBitsDefault(startIndex:Int=-1, crossinline predicate: (index:Int, bits:IntBits) -> Boolean): Int {val start=if(startIndex<0||startIndex>size-1)size-1 else startIndex; for(i in start..0) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.last(): T = elementAtIndex(size-1)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.last(crossinline predicate: (T) -> Boolean): T = findLast(predicate) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.lastIndexOf(element: T): Int = indexOfLast {it==element}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.lastOrNull(): T? = elementAtOrNull(size - 1)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.lastOrNull(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate))
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.drop(n: Int): ArrayListVInt<T> = slice(IntRange(n,size-1))
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.dropLast(n: Int): ArrayListVInt<T> = slice(IntRange(0,size-n))
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.dropWhile(crossinline predicate: (T) -> Boolean): ArrayListVInt<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) ArrayListVInt(this) else slice(IntRange(i, size))}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.dropLastWhile(crossinline predicate: (T) -> Boolean): ArrayListVInt<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(0, i))}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.filter(crossinline predicate: (T) -> Boolean): ArrayListVInt<T> = filterFromMask(filterMask(predicate))
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.filterMask(crossinline predicate: (T) -> Boolean): BitSet = filterIndexedMask { _, e->predicate(e)}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.filterFromMask(mask: BitSet): ArrayListVInt<T> = ArrayListVInt<T>(mask.cardinality(), NULL_VALUE).also { c-> forEachIndexedBits { i, e-> if(mask[i]) c.addBits(e)} }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.filterIndexed(crossinline predicate: (index: Int, T) -> Boolean): ArrayListVInt<T> = filterFromMask(filterIndexedMask(predicate))
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollectionVInt<T>> IndexedCollectionVInt<T>.filterIndexedTo(destination: C, crossinline predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> IndexedCollectionVInt<T>.filterIndexedTo(destination: C, crossinline predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.filterIndexedMask(crossinline predicate: (index: Int, T) -> Boolean): BitSet {val destination=BitSet(size); forEachIndexed { i, e -> destination.set(i,predicate(i, e))}; return destination }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.filterNot(crossinline predicate: (T) -> Boolean): ListVInt<T> = filter {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.filterNotIndexed(crossinline predicate: (Int, T) -> Boolean): ArrayListVInt<T> = filterIndexed { i, e->!predicate(i,e) }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollectionVInt<T>> IndexedCollectionVInt<T>.filterNotIndexedTo(destination: C, crossinline predicate: (Int, T) -> Boolean): C = filterIndexedTo(destination) { i, e->!predicate(i,e) }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableIndexedCollectionVInt<T>> IndexedCollectionVInt<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> IndexedCollectionVInt<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableIndexedCollectionVInt<T>> IndexedCollectionVInt<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> IndexedCollectionVInt<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.slice(indices: IntRange): ArrayListVInt<T> = copyInto(ArrayListVInt<T>(indices.last-indices.first, NULL_VALUE), 0, indices.first, indices.last)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.slice(indices: Iterable<Int>): ArrayListVInt<T> = ArrayListVInt<T>(if (indices is Collection<Int>) indices.size else size/8, NULL_VALUE).also { for(i in indices) it.addBits(bitsAtIndex(i)) }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.sliceArray(indices: Collection<Int>): ArrayVInt<T> = ArrayVInt<T>(indices.size, NULL_VALUE).also { c-> indices.forEachIndexed { i, ei-> c.set(i, get(ei))}}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.sliceArray(indices: IntRange): ArrayVInt<T> = ArrayVInt<T>(indices.last -indices.first +1, NULL_VALUE).also { c-> for (i in indices) c.set(i-indices.first, get(i))}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.take(n: Int): ArrayListVInt<T> = slice(IntRange(0,n))
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.takeLast(n: Int): ArrayListVInt<T> = slice(IntRange(size-n,size))
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.takeLastWhile(crossinline predicate: (T) -> Boolean): ArrayListVInt<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) ArrayListVInt<T>(this) else slice(IntRange(0, i))}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.slice(indices: Collection<Int>): ArrayListVInt<T> = filterIndexed { i, e-> indices.contains(i) }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.takeWhile(crossinline predicate: (T) -> Boolean): ArrayListVInt<T> = ArrayListVInt<T>().also { c-> any { val p=predicate(it); if (p) c.add(it); p } }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.takeWhileIndexed(crossinline predicate: (Int, T) -> Boolean): ArrayListVInt<T> = ArrayListVInt<T>().also { c-> anyIndexed { i, e-> val p=predicate(i,e); if (p) c.add(e); p } }
inline fun <T, C: MutableIndexedCollectionVInt<T>> IndexedCollectionVInt<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{forEachIndexedBits{ i, e-> if(i in startIndex..endIndex) destination.addBits(i-startIndex+destinationOffset, e)}}
context(a: ValueIntAdapter<T>) inline fun <T, C: MutableList<T>> IndexedCollectionVInt<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{forEachIndexed{ i, e-> if(i in startIndex..endIndex) destination.add(i-startIndex+destinationOffset, e)}}
inline fun <T> IndexedCollectionVInt<T>.reversed(): ArrayListVInt<T> = ArrayListVInt<T>(size, NULL_VALUE).also {forEachIndexedBits{ i, e-> it.setBits(size-i-1, e) }}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.shuffle(): Unit = shuffle(Random.Default)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.shuffle(random: Random): Unit {
}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.zipWithNext(): List<Pair<T, T>> = zipWithNext { l, r -> l to r}
context(a: ValueIntAdapter<T>) inline fun <T, R> IndexedCollectionVInt<T>.zipWithNext(crossinline transform: (a: T, b: T) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> IndexedCollectionVInt<T>.reduceRight(crossinline operation: (T, acc: T) -> T): T = reduceRightIndexed { i, e, acc -> operation(e,acc)}
context(a: ValueIntAdapter<T>) inline fun <S, T : S> IndexedCollectionVInt<T>.reduceRightIndexed(crossinline operation: (index: Int, T, acc: T) -> T): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> IndexedCollectionVInt<T>.reduceRightIndexedOrNull(crossinline operation: (index: Int, T, acc: T) -> T): T? = if (size<2) null else reduceRightIndexed(operation)
context(a: ValueIntAdapter<T>) inline fun <S, T : S> IndexedCollectionVInt<T>.reduceRightOrNull(crossinline operation: (T, acc: T) -> T): T? = if (size<2) null else reduceRight(operation)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.windowed(windowSize: Int, step: Int = 1, partialWindows: Boolean = false): MutableList<MutableList<T>> {
    val list = MutableList<MutableList<T>>(size-windowSize) { mutableListOf() }
    for (i in 0 ..< size-windowSize) {
        for (j in 0..<windowSize)
            list[i].add(elementAtIndex(i+j))
    }
    return list
}
context(a: ValueIntAdapter<T>) inline fun <T, R> IndexedCollectionVInt<T>.windowed(windowSize: Int, step: Int = 1, partialWindows: Boolean = false, crossinline transform: (List<T>) -> R): List<R> = throw NotImplementedError()
