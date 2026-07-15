@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused", "RedundantNullableReturnType",
    "KotlinConstantConditions", "KotlinConstantConditions"
)

// TODO: Implement 'throw NotImplementedError' functions

package mpd.com.common.collect.valuecollections

import kotlin.collections.all

interface ModifiableCollectionVLong<T>: CollectionVLong<T> {
    context(a: ValueLongAdapter<T>) fun asModifiableIterable(): MutableIterable<T>
}

interface MutableCollectionVLong<T>: ModifiableCollectionVLong<T> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim(minCapacity: Int)
    fun addBits(bits: LongBits): Boolean
    fun removeBits(bits: LongBits): Boolean
    context(a: ValueLongAdapter<T>) fun removeAll(predicate: (T) -> Boolean): Boolean
    fun clear()
    context(a: ValueLongAdapter<T>) override fun asModifiableIterable(): MutableIterable<T> = asIterable()
    context(a: ValueLongAdapter<T>) override fun asIterable(): MutableIterable<T>
}
context(a: ValueLongAdapter<T>) inline fun <T> MutableCollectionVLong<T>.asCollectionGeneric(): MutableCollection<T> = object : MutableCollection<T> {
    override val size: Int inline get() = this@asCollectionGeneric.size
    override inline fun isEmpty(): Boolean = this@asCollectionGeneric.size == 0
    override inline fun contains(element: T): Boolean = this@asCollectionGeneric.contains(element)
    override inline fun iterator(): MutableIterator<T> = this@asCollectionGeneric.asIterable().iterator()
    override inline fun add(element: T): Boolean = this@asCollectionGeneric.add(element)
    override inline fun remove(element: T): Boolean = this@asCollectionGeneric.remove(element)
    override inline fun addAll(elements: Collection<T>): Boolean = this@asCollectionGeneric.addAll(elements)
    override inline fun removeAll(elements: Collection<T>): Boolean = this@asCollectionGeneric.removeAll(elements)
    override inline fun retainAll(elements: Collection<T>): Boolean = this@asCollectionGeneric.retainAll(elements)
    override inline fun clear() = this@asCollectionGeneric.clear()
    override inline fun containsAll(elements: Collection<T>): Boolean = this@asCollectionGeneric.containsAll(elements)
}

context(a: ValueLongAdapter<T>) inline fun <T> MutableCollectionVLong<T>.add(element: T): Boolean = addBits(a.toLong(element))
context(a: ValueLongAdapter<T>) inline fun <T> MutableCollectionVLong<T>.addAll(elements: Collection<T>): Boolean {
    ensureCapacity(size+elements.size)
    return elements.all { add(it) }
}
 inline fun <T> MutableCollectionVLong<T>.addAll(elements: CollectionVLong<T>): Boolean {
    ensureCapacity(size+elements.size)
    return elements.allBits { addBits(it) }
}
context(a: ValueLongAdapter<T>) inline fun <T> MutableCollectionVLong<T>.addAll(elements: Array<out T>): Boolean {
    ensureCapacity(size+elements.size)
    return elements.all { add(it) }
}
context(a: ValueLongAdapter<T>) inline fun <T> MutableCollectionVLong<T>.addAll(elements: Iterable<T>): Boolean = elements.all { add(it) }
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableCollectionVLong<T>.plusAssign(elements: Array<out T>): Unit = check(addAll(elements))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableCollectionVLong<T>.plusAssign(elements: Collection<T>): Unit = check(addAll(elements))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableCollectionVLong<T>.plusAssign(elements: Iterable<T>): Unit = check(addAll(elements))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableCollectionVLong<T>.plusAssign(element: T): Unit = check(add(element))
context(a: ValueLongAdapter<T>) inline fun <T> MutableCollectionVLong<T>.remove(element: T): Boolean = removeBits(a.toLong(element))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableCollectionVLong<T>.minusAssign(element: T): Unit = check(remove(element))
context(a: ValueLongAdapter<T>) inline fun <T> MutableCollectionVLong<T>.removeAll(elements: VLongList<T>): Boolean = elements.all { remove(it)}
context(a: ValueLongAdapter<T>) inline fun <T> MutableCollectionVLong<T>.removeAll(elements: Array<T>): Boolean= elements.all { remove(it)}
context(a: ValueLongAdapter<T>) inline fun <T> MutableCollectionVLong<T>.removeAll(elements: Iterable<T>): Boolean= elements.all { remove(it)}
context(a: ValueLongAdapter<T>) fun <T> MutableCollectionVLong<T>.removeAll(elements:Collection<T>): Boolean = elements.all { remove(it)}
fun <T> MutableCollectionVLong<T>.removeAll(elements: CollectionVLong<T>): Boolean = elements.allBits { removeBits(it) }
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableCollectionVLong<T>.minusAssign(elements: VLongList<T>): Unit = check(removeAll(elements))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableCollectionVLong<T>.minusAssign(elements: Array<T>): Unit = check(removeAll(elements))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableCollectionVLong<T>.minusAssign(elements: Collection<T>): Unit = check(removeAll(elements))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableCollectionVLong<T>.minusAssign(elements: Iterable<T>): Unit = check(removeAll(elements))
context(a: ValueLongAdapter<T>) inline fun <T> MutableCollectionVLong<T>.retainAll(elements: Collection<T>): Boolean = removeAll {!elements.contains(it)}
context(a: ValueLongAdapter<T>) inline fun <T> MutableCollectionVLong<T>.retainAll(elements: VLongList<T>): Unit = check(removeAll {!elements.contains(it)})


