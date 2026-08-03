@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused", "RedundantNullableReturnType",
    "KotlinConstantConditions", "KotlinConstantConditions", "CONTEXTUAL_OVERLOAD_SHADOWED"
)

package com.mpd.common.collect.valuecollections

import java.util.BitSet


interface IndexedCollectionVInt<T> : CollectionVInt<T> {
    fun bitsAtIndex(index: Int): IntBits

    fun indexOfBits(bits: IntBits): Int
    fun indexOfFirstIndexedBits(startIndex:Int=0, predicate: (index:Int, bits:IntBits) -> Boolean): Int = indexOfFirstIndexedBitsDefault(startIndex, predicate)
    fun indexOfLastIndexedBits(endIndex:Int=-1, predicate: (index:Int, bits:IntBits) -> Boolean): Int = indexOfLastIndexedBitsDefault(endIndex, predicate)

    // Truly live, lazy iterator: genuinely indexed backing stores can read a fresh element on each
    // next() with no upfront snapshot copy. Only types that are declared IndexedCollectionVInt<T>
    // get to offer this - see CollectionVInt.toIterable() for the honest snapshot-based alternative.
    context(a: ValueIntAdapter<T>) fun asIterable(): Iterable<T> {
        val self = this
        return object : Iterable<T> {
            override fun iterator(): Iterator<T> = object : Iterator<T> {
                var idx = 0
                override fun hasNext(): Boolean = idx < self.size
                override fun next(): T = a.fromInt(self.bitsAtIndex(idx++))
            }
        }
    }
    context(a: ValueIntAdapter<T>) override fun toIterable(): Iterable<T> = toMutableList().asIterable()

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
    override inline fun get(index: Int): T = this@asListGeneric[index]
    override inline fun indexOf(element: T): Int = this@asListGeneric.indexOf(element)
    override inline fun lastIndexOf(element: T): Int = this@asListGeneric.lastIndexOf(element)
    override inline fun listIterator(): ListIterator<T> = ListIteratorVInt(this)
    override inline fun listIterator(index: Int): ListIterator<T> = ListIteratorVInt(this, index)
    override inline fun subList(fromIndex: Int, toIndex: Int): List<T> {
        val result = ArrayList<T>(toIndex-fromIndex)
        for (i in fromIndex ..< toIndex) result.add(this@asListGeneric[i])
        return result
    }
}
// Overrides of the CollectionVInt<T> versions of the same name: those wrap the caller's lambda in a
// stateful object to fake an index while scanning via anyBits/allBits/forEachBits (the only option for
// non-indexed collections). Here we have real O(1) random access via bitsAtIndex, so a plain for-loop
// is simpler and needs no wrapper object at all.
inline fun <T> IndexedCollectionVInt<T>.anyIndexedBits(crossinline action: (index:Int, IntBits) -> Boolean): IntBits {
    for (i in 0 ..< size) { val b = bitsAtIndex(i); if (action(i, b)) return b }
    return NULL_VALUE
}
inline fun <T> IndexedCollectionVInt<T>.allIndexedBits(crossinline action: (index:Int, IntBits) -> Boolean): Boolean {
    for (i in 0 ..< size) if (!action(i, bitsAtIndex(i))) return false
    return true
}
inline fun <T> IndexedCollectionVInt<T>.forEachIndexedBits(crossinline action: (index:Int, bits:IntBits) -> Unit) {
    for (i in 0 ..< size) action(i, bitsAtIndex(i))
}
inline fun <T> IndexedCollectionVInt<T>.findIndexedBits(crossinline predicate: (index:Int, bits:IntBits) -> Boolean): IntBits {
    for (i in 0 ..< size) { val b = bitsAtIndex(i); if (predicate(i, b)) return b }
    return NULL_VALUE
}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.anyIndexed(crossinline action: (index:Int, T) -> Boolean): Boolean = anyIndexedBits { i, bits -> action(i, a.fromInt(bits)) } != NULL_VALUE
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.allIndexed(crossinline action: (index:Int, T) -> Boolean): Boolean = allIndexedBits { i, bits -> action(i, a.fromInt(bits)) }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.forEachIndexed(crossinline action: (index:Int, T) -> Unit) = forEachIndexedBits { i, bits -> action(i, a.fromInt(bits)) }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.findIndexed(crossinline predicate: (index:Int, T) -> Boolean): IntBits = findIndexedBits { i, bits -> predicate(i, a.fromInt(bits)) }
inline fun <T, R> IndexedCollectionVInt<T>.foldIndexedBits(initial: R, crossinline operation: (index: Int, acc: R, IntBits) -> R): R {
    var acc = initial
    for (i in 0 ..< size) acc = operation(i, acc, bitsAtIndex(i))
    return acc
}
context(a: ValueIntAdapter<T>) inline fun <T, R> IndexedCollectionVInt<T>.foldIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): R =
    foldIndexedBits(initial) { i, acc, bits -> operation(i, acc, a.fromInt(bits)) }
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
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.indexOf(element: T): Int = indexOfBits(a.toInt(element))
inline fun <T> IndexedCollectionVInt<T>.indexOfFirstBits(crossinline predicate: (IntBits) -> Boolean): Int { for(i in 0 ..< size) if (predicate(bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.indexOfFirst(crossinline predicate: (T) -> Boolean): Int = indexOfFirstBits { predicate(a.fromInt(it)) }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.indexOfFirstIndexed(crossinline predicate: (index:Int, T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T> IndexedCollectionVInt<T>.indexOfFirstIndexedBitsDefault(startIndex:Int=0, crossinline predicate: (index:Int, bits:IntBits) -> Boolean): Int { for(i in startIndex ..< size) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.indexOfLast(crossinline predicate: (T) -> Boolean): Int { for(i in size-1 downTo 0) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.indexOfLastIndexed(crossinline predicate: (index:Int, T) -> Boolean): Int { for(i in size-1 downTo 0) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T> IndexedCollectionVInt<T>.indexOfLastIndexedBitsDefault(startIndex:Int=-1, crossinline predicate: (index:Int, bits:IntBits) -> Boolean): Int {val start=if(startIndex<0||startIndex>size-1)size-1 else startIndex; for(i in start downTo 0) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.last(): T = elementAtIndex(size-1)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.last(crossinline predicate: (T) -> Boolean): T = findLast(predicate) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.lastIndexOf(element: T): Int = indexOfLast {it==element}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.lastOrNull(): T? = elementAtOrNull(size - 1)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.lastOrNull(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate))
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.drop(n: Int): ArrayListVInt<T> = slice(IntRange(n,size-1))
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.dropLast(n: Int): ArrayListVInt<T> = slice(IntRange(0,size-n-1))
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
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.slice(indices: IntRange): ArrayListVInt<T> = copyInto(ArrayListVInt(indices.last-indices.first, NULL_VALUE), 0, indices.first, indices.last)
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.slice(indices: Iterable<Int>): ArrayListVInt<T> = ArrayListVInt<T>(if (indices is Collection<Int>) indices.size else size/8, NULL_VALUE).also { for(i in indices) it.addBits(bitsAtIndex(i)) }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.sliceArray(indices: Collection<Int>): ArrayVInt<T> = ArrayVInt<T>(indices.size, NULL_VALUE).also { c-> indices.forEachIndexed { i, ei-> c[i] = get(ei)}}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.sliceArray(indices: IntRange): ArrayVInt<T> = ArrayVInt<T>(indices.last -indices.first +1, NULL_VALUE).also { c-> for (i in indices) c[i-indices.first] = get(i)}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.take(n: Int): ArrayListVInt<T> = slice(IntRange(0,n-1))
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.takeLast(n: Int): ArrayListVInt<T> = slice(IntRange(size-n,size))
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.takeLastWhile(crossinline predicate: (T) -> Boolean): ArrayListVInt<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) ArrayListVInt(this) else slice(IntRange(i+1, size-1))}
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.slice(indices: Collection<Int>): ArrayListVInt<T> = filterIndexed { i, e-> indices.contains(i) }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.takeWhile(crossinline predicate: (T) -> Boolean): ArrayListVInt<T> = ArrayListVInt<T>().also { c-> any { val p=predicate(it); if (p) c.add(it); !p } }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.takeWhileIndexed(crossinline predicate: (Int, T) -> Boolean): ArrayListVInt<T> = ArrayListVInt<T>().also { c-> anyIndexed { i, e-> val p=predicate(i,e); if (p) c.add(e); !p } }
inline fun <T, C: MutableIndexedCollectionVInt<T>> IndexedCollectionVInt<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{forEachIndexedBits{ i, e-> if(i in startIndex..endIndex) destination.addBits(i-startIndex+destinationOffset, e)}}
context(a: ValueIntAdapter<T>) inline fun <T, C: MutableList<T>> IndexedCollectionVInt<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{forEachIndexed{ i, e-> if(i in startIndex..endIndex) destination.add(i-startIndex+destinationOffset, e)}}
inline fun <T> IndexedCollectionVInt<T>.reversed(): ArrayListVInt<T> = ArrayListVInt<T>(size, NULL_VALUE).also { dest -> for (i in size-1 downTo 0) dest.addBits(bitsAtIndex(i)) }
context(a: ValueIntAdapter<T>) inline fun <T> IndexedCollectionVInt<T>.zipWithNext(): List<Pair<T, T>> = zipWithNext { l, r -> l to r}
context(a: ValueIntAdapter<T>) inline fun <T, R> IndexedCollectionVInt<T>.zipWithNext(crossinline transform: (a: T, b: T) -> R): List<R> {
    if (size<=1) return emptyList()
    val result = ArrayList<R>(size-1)
    for (i in 0 ..< size-1) result.add(transform(elementAtIndex(i), elementAtIndex(i+1)))
    return result
}
context(a: ValueIntAdapter<T>) inline fun <S, T : S> IndexedCollectionVInt<T>.reduceRight(crossinline operation: (T, acc: T) -> T): T = reduceRightIndexed { i, e, acc -> operation(e,acc)}
context(a: ValueIntAdapter<T>) inline fun <S, T : S> IndexedCollectionVInt<T>.reduceRightIndexed(crossinline operation: (index: Int, T, acc: T) -> T): T {
    if (size==0) throw NoSuchElementException()
    var acc: T = elementAtIndex(size-1)
    for (i in size-2 downTo 0) acc = operation(i, elementAtIndex(i), acc)
    return acc
}
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
context(a: ValueIntAdapter<T>) inline fun <T, R> IndexedCollectionVInt<T>.windowed(windowSize: Int, step: Int = 1, partialWindows: Boolean = false, crossinline transform: (List<T>) -> R): List<R> {
    require(windowSize > 0) { "windowSize must be greater than zero, was $windowSize" }
    require(step > 0) { "step must be greater than zero, was $step" }
    val result = ArrayList<R>()
    var i = 0
    while (i < size) {
        val windowEnd = i + windowSize
        if (windowEnd > size) {
            if (partialWindows && i < size) {
                val window = ArrayList<T>(size - i)
                for (j in i ..< size) window.add(elementAtIndex(j))
                result.add(transform(window))
            }
            break
        }
        val window = ArrayList<T>(windowSize)
        for (j in i ..< windowEnd) window.add(elementAtIndex(j))
        result.add(transform(window))
        i += step
    }
    return result
}
