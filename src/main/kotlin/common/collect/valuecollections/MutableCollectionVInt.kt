@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused", "RedundantNullableReturnType",
    "KotlinConstantConditions", "KotlinConstantConditions"
)

// TODO: Implement 'throw NotImplementedError' functions

package mpd.com.common.collect.valuecollections

import kotlin.collections.all


interface ModifiableCollectionVInt<T>: CollectionVInt<T> {
    context(a: ValueIntAdapter<T>) fun asModifiableIterable(): MutableIterable<T>
}

interface MutableCollectionVInt<T>: ModifiableCollectionVInt<T> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim(minCapacity: Int)
    fun addBits(bits: IntBits): Boolean
    fun removeBits(bits: IntBits): Boolean
    context(a: ValueIntAdapter<T>) fun removeAll(predicate: (T) -> Boolean): Boolean
    fun clear()
    context(a: ValueIntAdapter<T>) override fun asModifiableIterable(): MutableIterable<T> = asIterable()
    context(a: ValueIntAdapter<T>) override fun asIterable(): MutableIterable<T>
}
context(a: ValueIntAdapter<T>) inline fun <T> MutableCollectionVInt<T>.asCollectionGeneric(): MutableCollection<T> = object : MutableCollection<T> {
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

context(a: ValueIntAdapter<T>) inline fun <T> MutableCollectionVInt<T>.add(element: T): Boolean = addBits(a.toInt(element))
context(a: ValueIntAdapter<T>) inline fun <T> MutableCollectionVInt<T>.addAll(elements: Collection<T>): Boolean {
    ensureCapacity(size+elements.size)
    return elements.fold(true) {acc,e-> add(e) || acc }
}
 inline fun <T> MutableCollectionVInt<T>.addAll(elements: CollectionVInt<T>): Boolean {
    ensureCapacity(size+elements.size)
     return elements.foldBits(true) {acc,e-> addBits(e) || acc }
}
context(a: ValueIntAdapter<T>) inline fun <T> MutableCollectionVInt<T>.addAll(elements: Array<out T>): Boolean {
    ensureCapacity(size+elements.size)
    return elements.fold(true) {acc,e-> add(e) || acc }
}
context(a: ValueIntAdapter<T>) inline fun <T> MutableCollectionVInt<T>.addAll(elements: Iterable<T>): Boolean = elements.fold(true) {acc,e-> add(e) || acc }
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableCollectionVInt<T>.plusAssign(elements: Array<out T>): Unit = check(addAll(elements))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableCollectionVInt<T>.plusAssign(elements: Collection<T>): Unit = check(addAll(elements))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableCollectionVInt<T>.plusAssign(elements: Iterable<T>): Unit = check(addAll(elements))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableCollectionVInt<T>.plusAssign(element: T): Unit = check(add(element))
context(a: ValueIntAdapter<T>) inline fun <T> MutableCollectionVInt<T>.remove(element: T): Boolean = removeBits(a.toInt(element))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableCollectionVInt<T>.minusAssign(element: T): Unit = check(remove(element))
context(a: ValueIntAdapter<T>) inline fun <T> MutableCollectionVInt<T>.removeAll(elements: ListVInt<T>): Boolean = elements.all { remove(it)}
context(a: ValueIntAdapter<T>) inline fun <T> MutableCollectionVInt<T>.removeAll(elements: Array<T>): Boolean= elements.all { remove(it)}
context(a: ValueIntAdapter<T>) inline fun <T> MutableCollectionVInt<T>.removeAll(elements: Iterable<T>): Boolean= elements.all { remove(it)}
context(a: ValueIntAdapter<T>) fun <T> MutableCollectionVInt<T>.removeAll(elements:Collection<T>): Boolean = elements.all { remove(it)}
fun <T> MutableCollectionVInt<T>.removeAll(elements: CollectionVInt<T>): Boolean = elements.allBits { removeBits(it) }
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableCollectionVInt<T>.minusAssign(elements: ListVInt<T>): Unit = check(removeAll(elements))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableCollectionVInt<T>.minusAssign(elements: Array<T>): Unit = check(removeAll(elements))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableCollectionVInt<T>.minusAssign(elements: Collection<T>): Unit = check(removeAll(elements))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableCollectionVInt<T>.minusAssign(elements: Iterable<T>): Unit = check(removeAll(elements))
context(a: ValueIntAdapter<T>) inline fun <T> MutableCollectionVInt<T>.retainAll(elements: Collection<T>): Boolean = removeAll {!elements.contains(it)}
context(a: ValueIntAdapter<T>) inline fun <T> MutableCollectionVInt<T>.retainAll(elements: ListVInt<T>): Unit = check(removeAll {!elements.contains(it)})
