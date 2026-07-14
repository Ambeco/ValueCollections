@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused", "RedundantNullableReturnType",
    "KotlinConstantConditions", "KotlinConstantConditions"
)

// TODO: Implement 'throw NotImplementedError' functions

package mpd.com.common.collect.valuecollections

import androidx.collection.IntList
import androidx.collection.IntSet
import androidx.collection.LongList
import androidx.collection.LongSet
import java.util.BitSet
import kotlin.Double
import kotlin.also
import kotlin.collections.all
import kotlin.collections.sort
import kotlin.collections.sortDescending
import kotlin.collections.set
import kotlin.collections.sortBy
import kotlin.collections.sortByDescending
import kotlin.collections.sortWith
import kotlin.math.min
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
    
    context(a: ValueIntAdapter<T>) fun asIterable(): Iterable<T>
    
    @JvmName("toVString") @Suppress("INAPPLICABLE_JVM_NAME")
    context(a: ValueIntAdapter<T>) fun toString(): String = toVString()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.asCollectionGeneric(): Collection<T> = object: Collection<T> {
    override val size: Int get() = this@asCollectionGeneric.size
    override inline fun isEmpty(): Boolean = this@asCollectionGeneric.size == 0
    override inline fun contains(element: T): Boolean = this@asCollectionGeneric.contains(element)
    override inline fun iterator(): Iterator<T> = this@asCollectionGeneric.asIterable().iterator()
    override inline fun containsAll(elements: Collection<T>): Boolean = this@asCollectionGeneric.containsAll(elements)
}

context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.fromInt(bits: IntBits) = if (bits==NULL_VALUE) throw NoSuchElementException() else a.fromInt(bits)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.fromIntOr(bits: IntBits, provider: ()->T): T = if (bits==NULL_VALUE) provider() else a.fromInt(bits)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.fromIntOrNull(bits: IntBits): T? = if (bits==NULL_VALUE) null else a.fromInt(bits)
 inline fun <T> VIntCollection<T>.allBits(crossinline predicate: (IntBits) -> Boolean): Boolean = anyBits{!predicate(it)} == NULL_VALUE
 inline fun <T> VIntCollection<T>.forEachBits(crossinline action: (bits:IntBits) -> Unit) { anyBits {action(it); false } }
inline  fun <T> VIntCollection<T>.singleBits(crossinline predicate: (bits:IntBits) -> Boolean): IntBits {
    // we use an anonymous object here to expose the mutated matchBits without extra allocations
    val matchPredicate = object : (IntBits) -> Boolean {
        var matchBits:IntBits = NULL_VALUE
        override inline fun invoke(bits: IntBits): Boolean {
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
 inline fun <T> VIntCollection<T>.anyIndexedBits(crossinline action: (index:Int, IntBits) -> Boolean) = anyBits(
    object: (IntBits) -> Boolean {
        var index = 0
        override inline fun invoke(v: IntBits) = action(index++, v)
    }
)
 inline fun <T> VIntCollection<T>.allIndexedBits(crossinline action: (index:Int, IntBits) -> Boolean) = allBits(
    object: (IntBits) -> Boolean {
        var index = 0
        override inline fun invoke(v: IntBits) = action(index++, v)
    }
)
 inline fun <T> VIntCollection<T>.forEachIndexedBits(crossinline action: (index:Int, bits:IntBits) -> Unit) = forEachBits(
    object: (IntBits) -> Unit {
        var index=0
        override inline fun invoke(bits: IntBits) = action(index++, bits)
    })

context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.any(crossinline predicate: (T) -> Boolean): Boolean = anyBits{predicate(a.fromInt(it))} != NULL_VALUE
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.all(crossinline predicate: (T) -> Boolean): Boolean = allBits {predicate(a.fromInt(it))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.forEach(crossinline action: (T) -> Unit) = forEachBits { action(a.fromInt(it)) }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.single(crossinline predicate: (T) -> Boolean): T = fromInt(singleBits {predicate(a.fromInt(it))})
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.contains(element: T) = containsBits(a.toInt(element))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.forEachIndexed(crossinline action: (index:Int, T) -> Unit) = forEachIndexedBits {i,e-> action(i,a.fromInt(e)) }

 inline fun <T> VIntCollection<T>.isEmpty() = size == 0
 inline fun <T> VIntCollection<T>.isNotEmpty() = size > 0
 inline fun <T> VIntCollection<T>.containsAll(bits: IntList): Boolean = bits.first { !containsBits(it) } == NULL_VALUE
 inline fun <T> VIntCollection<T>.containsAll(bits: IntSet): Boolean = bits.first { !containsBits(it) } == NULL_VALUE
 inline fun <T> VIntCollection<T>.containsAll(bits: VIntCollection<T>): Boolean = bits.anyBits({ !containsBits(it) }) == NULL_VALUE
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.containsAll(other: Collection<T>): Boolean = other.any({ !contains(it) })
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.single(): T = single {true}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOr(provider: ()->T): T = fromIntOr(singleBits {true}, provider)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOrElse(defaultValue:T): T = singleOr {defaultValue}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOrNull(): T? =  fromIntOrNull(singleBits { true })
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOrElse(crossinline predicate: (T) -> Boolean, defaultValue:T): T = singleOr(predicate) {defaultValue}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOr(crossinline predicate: (T) -> Boolean, provider: ()->T): T = fromIntOr(singleBits({ predicate(a.fromInt(it)) }), provider)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.singleOrNull(crossinline predicate: (T) -> Boolean): T? = fromIntOrNull(singleBits({ predicate(a.fromInt(it)) }))
 inline fun <T> VIntCollection<T>.findIndexedBits(crossinline predicate: (index:Int, bits:IntBits) -> Boolean): IntBits = anyBits(
    object: (IntBits) -> Boolean {
        var index=0
        override inline fun invoke(bits: IntBits) = predicate(index++, bits)
    })
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.findIndexed(crossinline predicate: (index:Int, T) -> Boolean): IntBits = findIndexedBits {i,b -> predicate(i, a.fromInt(b))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.find(crossinline predicate: (T) -> Boolean): T? = fromIntOrNull(anyBits{predicate(a.fromInt(it))})
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.findOrElse(crossinline predicate: (T) -> Boolean, defaultValue:T): T = findOr(predicate) {defaultValue}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.findOr(crossinline predicate: (T) -> Boolean, provider: ()->T): T = fromIntOr(anyBits{predicate(a.fromInt(it))}, provider)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.findOrThrow(crossinline predicate: (T) -> Boolean): T = fromInt(anyBits{predicate(a.fromInt(it))})
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.filter(crossinline predicate: (T) -> Boolean): ArrayVIntList<T> = filterTo(ArrayVIntList(), predicate)
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>> VIntCollection<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.filterNot(crossinline predicate: (T) -> Boolean): ArrayVIntList<T> = filter {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>> VIntCollection<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateVIntInt(crossinline transform: (T) -> VIntIntPair<K, V>): VIntIntMap<K, V> = associateTo(MutableVIntIntMap(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateVIntLong(crossinline transform: (T) -> VIntLongPair<K, V>): VIntLongMap<K, V> = associateTo(MutableVIntLongMap(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateVLongInt(crossinline transform: (T) -> VLongIntPair<K, V>): VLongIntMap<K, V> = associateTo(MutableVLongIntMap(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateVLongLong(crossinline transform: (T) -> VLongLongPair<K, V>): VLongLongMap<K, V> = associateTo(MutableVLongLongMap(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V> VIntCollection<T>.associateGeneric(crossinline transform: (T) -> Pair<K, V>): Map<K, V> = associateTo(HashMap(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateVIntInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableVIntIntMap<K,V> = associateTo(MutableVIntIntMap(size), keySelector,valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateVIntLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableVIntLongMap<K,V> = associateTo(MutableVIntLongMap(size), keySelector,valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateVLongInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableVLongIntMap<K,V> = associateTo(MutableVLongIntMap(size), keySelector,valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VIntCollection<T>.associateVLongLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableVLongLongMap<K,V> = associateTo(MutableVLongLongMap(size), keySelector,valueTransform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V> VIntCollection<T>.associateGeneric(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): Map<K, V> = associateTo(HashMap(size+size/4), keySelector, valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVIntIntMap<K,V>> VIntCollection<T>.associateTo(destination: C, crossinline transform: (T) -> VIntIntPair<K, V>): C = destination.also{c->forEach {c.putAll(this, transform)}}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVIntLongMap<K,V>> VIntCollection<T>.associateTo(destination: C, crossinline transform: (T) -> VIntLongPair<K, V>): C = destination.also{c->forEach {c.putAll(this, transform)}}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVLongIntMap<K,V>> VIntCollection<T>.associateTo(destination: C, crossinline transform: (T) -> VLongIntPair<K, V>): C = destination.also{c->forEach {c.putAll(this, transform)}}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVLongLongMap<K,V>> VIntCollection<T>.associateTo(destination: C, crossinline transform: (T) -> VLongLongPair<K, V>): C = destination.also{c->forEach {c.putAll(this, transform)}}
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>> VIntCollection<T>.associateTo(destination: M, crossinline transform: (T) -> Pair<K, V>): M = destination.also{c->forEach {val v=transform(it); c[v.first] = v.second}}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVIntIntMap<K,V>> VIntCollection<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVIntLongMap<K,V>> VIntCollection<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVLongIntMap<K,V>> VIntCollection<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVLongLongMap<K,V>> VIntCollection<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>> VIntCollection<T>.associateTo(destination: M, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): M = destination.also{c->forEach {c.put(keySelector(it),valueTransform(it))}}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>,) inline fun <T, K> VIntCollection<T>.associateByVIntInt(crossinline keySelector: (T) -> K): MutableVIntIntMap<K,T> = associateTo(MutableVIntIntMap<K,T>(size), keySelector, {it})
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>) inline fun <T, K> VIntCollection<T>.associateByVLongInt(crossinline keySelector: (T) -> K): MutableVLongIntMap<K,T> = associateTo(MutableVLongIntMap(size), keySelector,{it})
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntCollection<T>.associateByGeneric(crossinline keySelector: (T) -> K): Map<K, T> = associateTo(HashMap(size+size/4), keySelector, {it})
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>) inline fun <T, K, C:MutableVIntIntMap<K,T>> VIntCollection<T>.associateByVIntIntTo(destination: C, crossinline keySelector: (T) -> K): MutableVIntIntMap<K,T> = associateTo(MutableVIntIntMap<K,T>(size), keySelector, {it})
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>) inline fun <T, K, C:MutableVLongIntMap<K,T>> VIntCollection<T>.associateByVLongIntTo(destination: C, crossinline keySelector: (T) -> K): MutableVLongIntMap<K,T> = associateTo(MutableVLongIntMap(size), keySelector,{it})
context(a: ValueIntAdapter<T>) inline fun <T, K, C:MutableMap<K,T>> VIntCollection<T>.associateByGenericTo(destination: C, crossinline keySelector: (T) -> K): Map<K, T> = associateTo(HashMap(size+size/4), keySelector, {it})
 inline fun <T, C : MutableVIntCollection<T>> VIntCollection<T>.toCollection(destination: C): C = destination.also{c -> c.addAll(this) }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> VIntCollection<T>.toCollection(destination: C): C = destination.also{c->forEach {c.add(it)}}
 inline fun <T> VIntCollection<T>.toList(): VIntList<T> = this as? VIntList<T> ?: toMutableList()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toListGeneric(): List<T> = toMutableListGeneric()
 inline fun <T> VIntCollection<T>.toMutableList(): ArrayVIntList<T> = this as? ArrayVIntList<T> ?: toCollection(ArrayVIntList<T>(size, NULL_VALUE))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toMutableListGeneric(): MutableList<T> = toCollection(ArrayList(size))
 inline fun <T> VIntCollection<T>.toSet(): VIntSet<T> = this as? VIntSet<T> ?: toMutableSet()
 inline fun <T> VIntCollection<T>.toMutableSet(): MutableVIntSet<T> = this as? MutableVIntSet<T> ?: toCollection(ArrayVIntSet<T>(size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toSetGeneric(): Set<T> = toHashSet()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toHashSet(): HashSet<T> = toCollection(HashSet(size + size/4))
 inline fun <T> VIntCollection<T>.toIntArray(): IntArray = (this as? VIntArray<T>)?.collection ?: IntArray(size).also {c->forEachIndexedBits{i,e-> c[i]=e}}
 inline fun <T> VIntCollection<T>.toVIntArray(): VIntArray<T> = this as? VIntArray<T> ?: VIntArray(this)
 inline fun <T> VIntCollection<T>.toArrayGenericBits(): Array<IntBits> = (this as? VIntArray<T>)?.collection?.toTypedArray() ?: Array(size,{NULL_VALUE}).also {c->forEachIndexedBits{i,e-> c[i]=e}}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.asSequence(): Sequence<T> = asIterable().asSequence()
 inline fun <T> VIntCollection<T>.asList(): VIntList<T> = toList()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.asListGeneric(): List<T> = toListGeneric()
 inline fun <T> VIntCollection<T>.contentEquals(other: VIntCollection<T>?): Boolean = other != null && size == other.size && allBits { other.containsBits(it) }
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.flatMap(crossinline transform: (T) ->VIntCollection<R>): ArrayVIntList<R> = flatMapTo(ArrayVIntList(size*2), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.flatMap(crossinline transform: (T) ->VLongCollection<R>): ArrayVLongList<R> = flatMapTo(ArrayVLongList(size*2), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.flatMapTo(destination: C, crossinline transform: (T) ->VIntCollection<R>): C = destination.also{forEach { destination.addAll(transform(it)) }}
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.flatMapTo(destination: C, crossinline transform: (T) ->VLongCollection<R>): C = destination.also{forEach { destination.addAll(transform(it)) }}
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.flatMapIndexed(crossinline transform: (Int,T) ->VIntCollection<R>): ArrayVIntList<R> = flatMapIndexedTo(ArrayVIntList(size*2), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.flatMapIndexed(crossinline transform: (Int,T) ->VLongCollection<R>): ArrayVLongList<R> = flatMapIndexedTo(ArrayVLongList(size*2), transform)
context(a: ValueIntAdapter<T>) inline  fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int,T) ->VIntCollection<R>): C = destination.also{forEachIndexed {i,e-> destination.addAll(transform(i,e)) }}
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int,T) ->VLongCollection<R>): C = destination.also{forEachIndexed {i,e-> destination.addAll(transform(i,e)) }}
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntCollection<T>.groupBy(crossinline keySelector: (T) -> K): MutableMap<K, MutableVIntList<T>> = groupByTo(HashMap<K,MutableVIntList<T>>(), keySelector)
context(a: ValueIntAdapter<T>) inline fun <T, K, M : MutableMap<K, MutableVIntList<T>>> VIntCollection<T>.groupByTo(destination: M, crossinline keySelector: (T) -> K): M = destination.also{c-> forEach { c.getOrPut(keySelector(it),{ ArrayVIntList(size) }).add(it) }}
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntCollection<T>.mapVInt(crossinline transform: (T) -> R): ArrayVIntList<R> = mapTo(ArrayVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.mapVLong(crossinline transform: (T) -> R): ArrayVLongList<R> = mapTo(ArrayVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.mapGeneric(crossinline transform: (T) -> R): MutableList<R> = mapTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntCollection<T>.mapIndexedVInt(crossinline transform: (index: Int, T) -> R): ArrayVIntList<R> = mapIndexedTo(ArrayVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.mapIndexedVLong(crossinline transform: (index: Int, T) -> R): ArrayVLongList<R> = mapIndexedTo(ArrayVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.mapIndexedGeneric(crossinline transform: (index: Int, T) -> R): List<R> = mapIndexedTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntCollection<T>.mapIndexedVIntNotNull(crossinline transform: (index: Int, T) -> R?): ArrayVIntList<R> = mapIndexedNotNullTo(ArrayVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.mapIndexedVLongNotNull(crossinline transform: (index: Int, T) -> R?): ArrayVLongList<R> = mapIndexedNotNullTo(ArrayVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.mapIndexedGenericNotNull(crossinline transform: (index: Int, T) -> R?): List<R> = mapIndexedNotNullTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{i,e-> destination.add(transform(i,e)) } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{i,e-> destination.add(transform(i,e)) } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{i,e-> destination.add(transform(i,e)) } }
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.mapNotNull(crossinline transform: (T) -> R?): List<R> = mapNotNullTo(mutableListOf(), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.mapNotNullTo(destination: C, crossinline transform: (T) -> R?): C = destination.also {forEach{transform(it)?.also {destination.add(it) } }}
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.withIndex():VLongCollection<IndexedVInt<T>> = with(IndexedVInt.VLongAdapter<T>()) {mapIndexedVLong{i,e-> IndexedVInt.of(i,e)}}
 inline fun <T> VIntCollection<T>.distinct(): VIntSet<T> = ArrayVIntSet<T>(size).also{c-> forEachBits {c.addBits(it)}}
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntCollection<T>.distinctBy(crossinline selector: (T) -> K): VIntSet<T> {
    val distinct = HashSet<K>()
    val result = ArrayVIntSet<T>(size)
    forEach {
        val k = selector(it)
        if (!distinct.contains(k)) {
            distinct.add(k)
            result.add(it)
        }
    }
    return result
}
 inline infix fun <T> VIntCollection<T>.intersect(other:VIntCollection<T>): VIntSet<T> = ArrayVIntSet<T>(size).also {c-> forEachBits{ if (other.containsBits(it)) c.addBits(it)}}
 inline infix fun <T> VIntCollection<T>.subtract(other:VIntCollection<T>): VIntSet<T> = ArrayVIntSet<T>(size).also {c-> forEachBits{ if (!other.containsBits(it)) c.addBits(it)}}
 inline infix fun <T> VIntCollection<T>.union(other:VIntCollection<T>): VIntSet<T> = toMutableSet().also{c-> c.addAll(other)}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.any(): Boolean = size > 0
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.count(): Int = size
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.count(crossinline predicate: (T) -> Boolean): Int = fold(0,{acc,e->if(predicate(e)) acc+1 else acc})
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.fold(initial: R, crossinline operation: (acc: R, T) -> R): R = foldIndexed(initial,{_,acc,e->operation(acc,e)})
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.foldIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): R {
    val accumulator = object: (Int,T)->Unit {
        var index=0
        var acc = initial
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e) }
    }
    forEachIndexed(accumulator)
    return accumulator.acc
}
context(a: ValueIntAdapter<T>) inline fun <T, C: VIntCollection<T>> C.onEach(crossinline action: (T) -> Unit): C = apply{forEach(action)}
context(a: ValueIntAdapter<T>) inline fun <T, C: VIntCollection<T>> C.onEachIndexed(crossinline action: (Int,T) -> Unit): C = apply{forEachIndexed(action)}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.max(): T = reduce {l,r -> if(r>l) r else l}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.maxOrNull(): T? = if(isEmpty()) null else max()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.maxWith(comparator: Comparator<in T>): T = reduce {l,r -> if(comparator.compare(r,l)<0) r else l}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.maxWithOrNull(comparator: Comparator<in T>): T? = if(isEmpty()) null else maxWith(comparator)
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.maxBy(crossinline selector: (T) -> R): T {
    val accumulator = object: (Int,T,T)->T {
        var max = selector(findOrThrow {true})
        override inline fun invoke(index:Int, l:T, r: T):T {
            val newR = selector(r)
            if (newR > max) {
                max = newR
                return r
            }
            return l
        }
    }
    return reduceIndexed(accumulator)
}
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.maxByOrNull(crossinline selector: (T) -> R): T? = if(isEmpty()) null else maxBy(selector)
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.maxOf(crossinline selector: (T) -> R): R = mapReduce(selector) {max,e-> if (e>max) e else max}
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.maxOfOrNull(crossinline selector: (T) -> R): R? = if(isEmpty()) null else maxOf(selector)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.maxOfWith(comparator: Comparator<in R>, crossinline selector: (T) -> R): R = mapReduce(selector) {max,e-> if (comparator.compare(e,max)>1) e else max}
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.maxOfWithOrNull(comparator: Comparator<in R>, crossinline selector: (T) -> R): R? = if(isEmpty()) null else maxOfWith(comparator, selector)
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.min(): T = reduce {l,r -> if(r<l) r else l}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.minOrNull(): T? = if(isEmpty()) null else min()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minWith(comparator: Comparator<in T>): T = reduce {l,r -> if(comparator.compare(r,l)<0) r else l}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minWithOrNull(comparator: Comparator<in T>): T? = if(isEmpty()) null else minWith(comparator)
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.minBy(crossinline selector: (T) -> R): T {
    val accumulator = object: (Int,T,T)->T {
        var min = selector(findOrThrow {true})
        override inline fun invoke(index:Int, l:T, r: T):T {
            val newR = selector(r)
            if (newR < min) {
                min = newR
                return r
            }
            return l
        }
    }
    return reduceIndexed(accumulator)
}
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.minByOrNull(crossinline selector: (T) -> R): T? = if(isEmpty()) null else minBy(selector)
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.minOf(crossinline selector: (T) -> R): R = mapReduce(selector) {min,e-> if (e<min) e else min}
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.minOfOrNull(crossinline selector: (T) -> R): R? = if(isEmpty()) null else minOf(selector)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.minOfWith(comparator: Comparator<in R>, crossinline selector: (T) -> R): R = mapReduce(selector) {min,e-> if (comparator.compare(e,min)<1) e else min}
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.minOfWithOrNull(comparator: Comparator<in R>, crossinline selector: (T) -> R): R? = if(isEmpty()) null else minOfWith(comparator, selector)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.none(): Boolean = size==0
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.none(crossinline predicate: (T) -> Boolean): Boolean = any { !predicate(it) }
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.sorted(): ArrayVIntList<T> = ArrayVIntList<T>(this).also{it.sort()}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.sortedArray(): VIntArray<T> = toVIntArray().also{it.sort()}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.sortedArrayDescending(): VIntArray<T> = toVIntArray().also{it.sortDescending()}
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.sortedBy(crossinline selector: (T) -> R?): ArrayVIntList<T> =  toMutableList().also{it.sortBy(selector)}
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.sortedByDescending(crossinline selector: (T) -> R?): ArrayVIntList<T> = toMutableList().also{it.sortByDescending(selector)}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.sortedDescending(): ArrayVIntList<T> = toMutableList().also{it.sortDescending()}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sortedWith(comparator: Comparator<in T>): ArrayVIntList<T> = toMutableList().also{it.sortWith(comparator)}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumBy(crossinline selector: (T) -> Int): Int = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumByDouble(crossinline selector: (T) -> Double): Double = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOf(crossinline selector: (T) -> Double): Double = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOf(crossinline selector: (T) -> Int): Int = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOf(crossinline selector: (T) -> Long): Long = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOfUInt(crossinline selector: (T) -> UInt): UInt = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOfULong(crossinline selector: (T) -> ULong): ULong = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.chunked(size: Int): List<ArrayVIntList<T>>{
    val results = List<ArrayVIntList<T>>((this.size + size - 1)/size) {ArrayVIntList(size)}
    val acc = object: (Int,T)->Unit {
        var listIdx=0
        var rollover=size
        override inline fun invoke(index:Int, e: T) {
            results[listIdx].add(e)
            if (index==rollover) {
                listIdx++
                rollover+=size
            }
        }
    }
    forEachIndexed(acc)
    return results
}
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.chunked(size: Int, crossinline transform: (VIntList<T>) -> R): List<R> = chunked(size).map(transform)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.plusElement(element: T): VIntList<T> = plus(element)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.plus(element: T): VIntList<T> = ArrayVIntList<T>(size+1, NULL_VALUE).also {it.addAll(this); it.add(element) }
 inline operator fun <T> VIntCollection<T>.plus(elements: VIntCollection<T>): VIntList<T> = ArrayVIntList<T>(size+elements.size, NULL_VALUE).also {it.addAll(this); it.addAll(elements) }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.plus(elements: Iterable<T>): VIntList<T> = ArrayVIntList<T>(size+1, NULL_VALUE).also {it.addAll(this); it.addAll(elements) }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.plus(elements: Array<out T>): VIntList<T> = ArrayVIntList<T>(size+1, NULL_VALUE).also {it.addAll(this); it.addAll(elements) }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minus(element: T): VIntList<T> = ArrayVIntList<T>(size, NULL_VALUE).also {c-> forEach { if (it != element) c.add(it) } }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.minus(elements: Array<out T>): VIntList<T> = ArrayVIntList<T>(size, NULL_VALUE).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.minus(elements:VIntCollection<T>): VIntList<T> = ArrayVIntList<T>(size, NULL_VALUE).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.minus(elements:Iterable<T>): VIntList<T> = ArrayVIntList<T>(size, NULL_VALUE).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.minus(elements: Sequence<T>): VIntList<T> = ArrayVIntList<T>(size, NULL_VALUE).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minusElement(element: T): VIntList<T> = minus(element)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.partition(crossinline predicate: (T) -> Boolean): Pair<VIntList<T>, VIntList<T>> {
    val trueList = ArrayVIntList<T>(size, NULL_VALUE)
    val falseList = ArrayVIntList<T>(size, NULL_VALUE)
    forEach { if (predicate(it)) trueList.add(it) else falseList.add(it) }
    return trueList to falseList
}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.random(): T = random(Random.Default)
context(a: ValueIntAdapter<T>) fun <T> VIntCollection<T>.random(random: Random): T {
    if (size==0) throw NoSuchElementException()
    val findIdx = random.nextInt(size)
    return fromInt(findIndexedBits {i,e->i==findIdx})
}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.randomOrNull(): T? = randomOrNull(Random.Default)
context(a: ValueIntAdapter<T>) fun <T> VIntCollection<T>.randomOrNull(random: Random): T? = if (size==0) null else random(random)
context(a: ValueIntAdapter<T>) inline infix fun <T, R> VIntCollection<T>.zip(other: Array<out R>): MutableList<Pair<T, R>> = zip(other, {a,b->a to b})
context(a: ValueIntAdapter<T>) inline fun <T, R, V> VIntCollection<T>.zip(other: Array<out R>, crossinline transform: (a: T, b: R) -> V): MutableList<V> {
    val r = mutableListOf<V>()
    forEachIndexed { i, e -> if (i < other.size) r.add(i, transform(e, other[i])) }
    return r
}
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline infix fun <T, R> VIntCollection<T>.zip(other:VIntIndexedCollection<R>): MutableVLongList<VIntIntPair<T, R>> 
    = with<ValueLongAdapter<VIntIntPair<T, R>>, MutableVLongList<VIntIntPair<T, R>>>(VIntIntPair.VLongAdapter()) {zipVIntIntPair<T,R, VIntIntPair<T,R>>(other, { a, b->VIntIntPair.of(a,b)})}
context(ta: ValueIntAdapter<T>, ra: ValueIntAdapter<R>, va: ValueLongAdapter<V>) inline fun <T, R, V> VIntCollection<T>.zipVIntIntPair(other:VIntIndexedCollection<R>, crossinline transform: (a: T, b: R) -> V): MutableVLongList<V> {
    val r = ArrayVLongList<V>(min(size, other.size))
    forEachIndexed { i, e -> if (i < other.size) r.add(i, transform(e, other.get(i))) }
    return r
}
context(a: ValueIntAdapter<T>) inline fun <T, A : Appendable> VIntCollection<T>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence) = { it.toString() }): A {
    val appender = object: (Int,T)-> Boolean {
        var count=0
        override inline fun invoke(index: Int, e: T): Boolean {
            if (limit<0 || count++ < limit) {
                if (count != 1) buffer.append(separator)
                buffer.append(transform(e))
                if (count < limit)
                    return false
            }
            if (count >= limit)
                buffer.append(truncated)
            return true
        }
    }
    buffer.append(prefix)
    findIndexed(appender)
    buffer.append(postfix)
    return buffer
}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence) = { it.toString() }): String 
    = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toVString() = joinToString(", ","{","}")
context(a: ValueIntAdapter<T>) inline fun <S, R : S, T> VIntCollection<T>.mapReduce(crossinline map:(T)->S, crossinline operation: (acc: S, S) -> S): S = mapReduceIndexed(map){i,acc,e->operation(acc,e)}
context(a: ValueIntAdapter<T>) inline fun <S, R : S, T> VIntCollection<T>.mapReduceIndexed(crossinline map:(T)->R, crossinline operation: (index:Int, acc: S, next:R) -> S): S {
    val accumulator = object: (Int,T)->Unit {
        var acc: S = map(findOrThrow{true})
        override inline fun invoke(i:Int, e: T) { if (i>0) acc = operation(i, acc, map(e)) }
    }
    forEachIndexed(accumulator)
    return accumulator.acc
}
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.reduce(crossinline operation: (acc: S, T) -> S): S = reduceIndexed<S,T>{i,acc,e -> operation(acc,e) }
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.reduceIndexed(crossinline operation: (index: Int, acc: S, T) -> S): S = mapReduceIndexed({it}, operation)
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.reduceIndexedOrNull(crossinline operation: (index: Int, acc: S, T) -> S): S? = if (size==0) return null else reduceIndexed(operation)
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.reduceOrNull(crossinline operation: (acc: S, T) -> S): S? = if (size==0) return null else reduce(operation)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntCollection<T>.runningFoldVInt(initial: R, crossinline operation: (acc: R, T) -> R): MutableVIntList<R> = runningFoldVIntIndexed(initial) {i,acc,e->operation(acc,e)}
context(ta: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntCollection<T>.runningFoldVIntIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): MutableVIntList<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = ArrayVIntList<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.runningFoldVLong(initial: R, crossinline operation: (acc: R, T) -> R): MutableVLongList<R> = runningFoldVLongIndexed(initial) {i,acc,e->operation(acc,e)}
context(ta: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.runningFoldVLongIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): MutableVLongList<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = ArrayVLongList<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.runningFoldGeneric(initial: R, crossinline operation: (acc: R, T) -> R): List<R> = runningFoldGenericIndexed(initial) {i,acc,e->operation(acc,e)}
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.runningFoldGenericIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): List<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = mutableListOf<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<S>) inline fun <S, T : S> VIntCollection<T>.runningReduceVInt(crossinline operation: (acc: S, T) -> S): MutableVIntList<S> = with(ra){runningReduceVIntIndexed {i,acc,e -> operation(acc,e)}}
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<S>) inline fun <S, T : S> VIntCollection<T>.runningReduceVIntIndexed(crossinline operation: (index: Int, acc: S, T) -> S): MutableVIntList<S>  {
    if (size<=1) return ArrayVIntList()
    val accumulator = object: (Int,T)->Unit {
        var acc: S = findOrThrow{true}
        var result = ArrayVIntList<S>(size-1)
        override inline fun invoke(i:Int, e: T) {
            if (i ==0) return
            acc = operation(i-1, acc, e)
            result.add(acc)
        }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<S>) inline fun <S, T : S> VIntCollection<T>.runningReduceVLong(crossinline operation: (acc: S, T) -> S): MutableVLongList<S> = runningReduceVLongIndexed {i,acc,e -> operation(acc,e)}
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<S>) inline fun <S, T : S> VIntCollection<T>.runningReduceVLongIndexed(crossinline operation: (index: Int, acc: S, T) -> S): MutableVLongList<S> {
    if (size<=1) return ArrayVLongList()
    val accumulator = object: (Int,T)->Unit {
        var acc: S = findOrThrow{true}
        var result = ArrayVLongList<S>(size-1)
        override inline fun invoke(i:Int, e: T) { 
            if (i ==0) return
            acc = operation(i-1, acc, e)
            result.add(acc)
        }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.runningReduceGeneric(crossinline operation: (acc: S, T) -> S): List<S> = runningReduceGenericIndexed<S,T> {i,acc,e -> operation(acc,e)}
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntCollection<T>.runningReduceGenericIndexed(crossinline operation: (index: Int, acc: S, T) -> S): List<S> {
    if (size<=1) return listOf()
    val accumulator = object: (Int,T)->Unit {
        var acc: S = findOrThrow{true}
        var result = mutableListOf<S>()
        override inline fun invoke(i:Int, e: T) {
            if (i ==0) return
            acc = operation(i-1, acc, e)
            result.add(acc)
        }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.scan(initial: R, crossinline operation: (acc: R, T) -> R): List<R> = runningFoldGeneric(initial, operation)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.scanIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): List<R> = runningFoldGenericIndexed(initial, operation)


interface ModifiableVIntCollection<T>: VIntCollection<T> {
    context(a: ValueIntAdapter<T>) fun asModifiableIterable(): MutableIterable<T>
}

interface MutableVIntCollection<T>: ModifiableVIntCollection<T> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim(minCapacity: Int)
    fun addBits(bits: IntBits): Boolean
    fun removeBits(bits: IntBits): Boolean
    context(a: ValueIntAdapter<T>) fun removeAll(predicate: (T) -> Boolean): Boolean
    fun clear()
    context(a: ValueIntAdapter<T>) override fun asModifiableIterable(): MutableIterable<T> = asIterable()
    context(a: ValueIntAdapter<T>) override fun asIterable(): MutableIterable<T>
}
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.asCollectionGeneric(): MutableCollection<T> = object : MutableCollection<T> {
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

context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.add(element: T): Boolean = addBits(a.toInt(element))
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.addAll(elements: Collection<T>): Boolean {
    ensureCapacity(size+elements.size)
    return elements.all { add(it) }
}
 inline fun <T> MutableVIntCollection<T>.addAll(elements: VIntCollection<T>): Boolean {
    ensureCapacity(size+elements.size)
    return elements.allBits { addBits(it) }
}
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.addAll(elements: Array<out T>): Boolean {
    ensureCapacity(size+elements.size)
    return elements.all { add(it) }
}
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.addAll(elements: Iterable<T>): Boolean = elements.all { add(it) }
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntCollection<T>.plusAssign(elements: Array<out T>): Unit = check(addAll(elements))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntCollection<T>.plusAssign(elements: Collection<T>): Unit = check(addAll(elements))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntCollection<T>.plusAssign(elements: Iterable<T>): Unit = check(addAll(elements))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntCollection<T>.plusAssign(element: T): Unit = check(add(element))
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.remove(element: T): Boolean = removeBits(a.toInt(element))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntCollection<T>.minusAssign(element: T): Unit = check(remove(element))
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.removeAll(elements: VIntList<T>): Boolean = elements.all { remove(it)}
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.removeAll(elements: Array<T>): Boolean= elements.all { remove(it)}
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.removeAll(elements: Iterable<T>): Boolean= elements.all { remove(it)}
context(a: ValueIntAdapter<T>) fun <T> MutableVIntCollection<T>.removeAll(elements:Collection<T>): Boolean = elements.all { remove(it)}
fun <T> MutableVIntCollection<T>.removeAll(elements: VIntCollection<T>): Boolean = elements.allBits { removeBits(it) }
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntCollection<T>.minusAssign(elements: VIntList<T>): Unit = check(removeAll(elements))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntCollection<T>.minusAssign(elements: Array<T>): Unit = check(removeAll(elements))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntCollection<T>.minusAssign(elements: Collection<T>): Unit = check(removeAll(elements))
context(a: ValueIntAdapter<T>) inline operator fun <T> MutableVIntCollection<T>.minusAssign(elements: Iterable<T>): Unit = check(removeAll(elements))
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.retainAll(elements: Collection<T>): Boolean = removeAll {!elements.contains(it)}
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntCollection<T>.retainAll(elements: VIntList<T>): Unit = check(removeAll {!elements.contains(it)})









interface VLongCollection<T> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_VALUE: LongBits
    val size: Int
    fun anyBits(predicate: (bits:LongBits) -> Boolean): LongBits
    fun containsBits(bits: LongBits): Boolean

    context(a: ValueLongAdapter<T>) fun asIterable(): Iterable<T>
    context(a: ValueLongAdapter<T>) fun toString(): String = toVString()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.asCollectionGeneric(): Collection<T> = object: Collection<T> {
    override val size: Int get() = this@asCollectionGeneric.size
    override inline fun isEmpty(): Boolean = this@asCollectionGeneric.size == 0
    override inline fun contains(element: T): Boolean = this@asCollectionGeneric.contains(element)
    override inline fun iterator(): Iterator<T> = this@asCollectionGeneric.asIterable().iterator()
    override inline fun containsAll(elements: Collection<T>): Boolean = this@asCollectionGeneric.containsAll(elements)
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
        override inline fun invoke(bits: LongBits): Boolean {
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
 inline fun <T> VLongCollection<T>.anyIndexedBits(crossinline action: (index:Int, LongBits) -> Boolean) = anyBits(
    object: (LongBits) -> Boolean {
        var index = 0
        override inline fun invoke(v: LongBits) = action(index++, v)
    }
)
 inline fun <T> VLongCollection<T>.allIndexedBits(crossinline action: (index:Int, LongBits) -> Boolean) = allBits(
    object: (LongBits) -> Boolean {
        var index = 0
        override inline fun invoke(v: LongBits) = action(index++, v)
    }
)
 inline fun <T> VLongCollection<T>.forEachIndexedBits(crossinline action: (index:Int, bits:LongBits) -> Unit) = forEachBits(
    object: (LongBits) -> Unit {
        var index=0
        override inline fun invoke(bits: LongBits) = action(index++, bits)
    })

context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.any(crossinline predicate: (T) -> Boolean): Boolean = anyBits{predicate(a.fromLong(it))} != NULL_VALUE
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.all(crossinline predicate: (T) -> Boolean): Boolean = allBits {predicate(a.fromLong(it))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.forEach(crossinline action: (T) -> Unit) = forEachBits { action(a.fromLong(it)) }
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.single(crossinline predicate: (T) -> Boolean): T = fromLong(singleBits {predicate(a.fromLong(it))})
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.contains(element: T) = containsBits(a.toLong(element))
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.forEachIndexed(crossinline action: (index:Int, T) -> Unit) = forEachIndexedBits {i,e-> action(i,a.fromLong(e)) }

 inline fun <T> VLongCollection<T>.isEmpty() = size == 0
 inline fun <T> VLongCollection<T>.isNotEmpty() = size > 0
 inline fun <T> VLongCollection<T>.containsAll(bits: LongList): Boolean = bits.first { !containsBits(it) } == NULL_VALUE
 inline fun <T> VLongCollection<T>.containsAll(bits: LongSet): Boolean = bits.first { !containsBits(it) } == NULL_VALUE
 inline fun <T> VLongCollection<T>.containsAll(bits: VLongCollection<T>): Boolean = bits.anyBits({ !containsBits(it) }) == NULL_VALUE
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.containsAll(other: Collection<T>): Boolean = other.any({ !contains(it) })
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.single(): T = single {true}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.singleOr(provider: ()->T): T = fromLongOr(singleBits {true}, provider)
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.singleOrElse(defaultValue:T): T = singleOr {defaultValue}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.singleOrNull(): T? =  fromLongOrNull(singleBits { true })
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.singleOrElse(crossinline predicate: (T) -> Boolean, defaultValue:T): T = singleOr(predicate) {defaultValue}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.singleOr(crossinline predicate: (T) -> Boolean, provider: ()->T): T = fromLongOr(singleBits({ predicate(a.fromLong(it)) }), provider)
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.singleOrNull(crossinline predicate: (T) -> Boolean): T? = fromLongOrNull(singleBits({ predicate(a.fromLong(it)) }))
 inline fun <T> VLongCollection<T>.findIndexedBits(crossinline predicate: (index:Int, bits:LongBits) -> Boolean): LongBits = anyBits(
    object: (LongBits) -> Boolean {
        var index=0
        override inline fun invoke(bits: LongBits) = predicate(index++, bits)
    })
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.findIndexed(crossinline predicate: (index:Int, T) -> Boolean): LongBits = findIndexedBits {i,b -> predicate(i, a.fromLong(b))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.find(crossinline predicate: (T) -> Boolean): T? = fromLongOrNull(anyBits{predicate(a.fromLong(it))})
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.findOrElse(crossinline predicate: (T) -> Boolean, defaultValue:T): T = findOr(predicate) {defaultValue}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.findOr(crossinline predicate: (T) -> Boolean, provider: ()->T): T = fromLongOr(anyBits{predicate(a.fromLong(it))}, provider)
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.findOrThrow(crossinline predicate: (T) -> Boolean): T = fromLong(anyBits{predicate(a.fromLong(it))})
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.filter(crossinline predicate: (T) -> Boolean): ArrayVLongList<T> = filterTo(ArrayVLongList(), predicate)
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongCollection<T>> VLongCollection<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.filterNot(crossinline predicate: (T) -> Boolean): ArrayVLongList<T> = filter {!predicate(it)}
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongCollection<T>> VLongCollection<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VLongCollection<T>.associateVIntInt(crossinline transform: (T) -> VIntIntPair<K, V>): VIntIntMap<K, V> = associateTo(MutableVIntIntMap(size), transform)
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VLongCollection<T>.associateVIntLong(crossinline transform: (T) -> VIntLongPair<K, V>): VIntLongMap<K, V> = associateTo(MutableVIntLongMap(size), transform)
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VLongCollection<T>.associateVLongInt(crossinline transform: (T) -> VLongIntPair<K, V>): VLongIntMap<K, V> = associateTo(MutableVLongIntMap(size), transform)
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VLongCollection<T>.associateVLongLong(crossinline transform: (T) -> VLongLongPair<K, V>): VLongLongMap<K, V> = associateTo(MutableVLongLongMap(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, K, V> VLongCollection<T>.associateGeneric(crossinline transform: (T) -> Pair<K, V>): Map<K, V> = associateTo(HashMap(size), transform)
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VLongCollection<T>.associateVIntInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableVIntIntMap<K,V> = associateTo(MutableVIntIntMap(size), keySelector,valueTransform)
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VLongCollection<T>.associateVIntLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableVIntLongMap<K,V> = associateTo(MutableVIntLongMap(size), keySelector,valueTransform)
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> VLongCollection<T>.associateVLongInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableVLongIntMap<K,V> = associateTo(MutableVLongIntMap(size), keySelector,valueTransform)
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> VLongCollection<T>.associateVLongLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableVLongLongMap<K,V> = associateTo(MutableVLongLongMap(size), keySelector,valueTransform)
context(a: ValueLongAdapter<T>) inline fun <T, K, V> VLongCollection<T>.associateGeneric(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): Map<K, V> = associateTo(HashMap(size+size/4), keySelector, valueTransform)
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVIntIntMap<K,V>> VLongCollection<T>.associateTo(destination: C, crossinline transform: (T) -> VIntIntPair<K, V>): C = destination.also{c->forEach {c.putAll(this, transform)}}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVIntLongMap<K,V>> VLongCollection<T>.associateTo(destination: C, crossinline transform: (T) -> VIntLongPair<K, V>): C = destination.also{c->forEach {c.putAll(this, transform)}}
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVLongIntMap<K,V>> VLongCollection<T>.associateTo(destination: C, crossinline transform: (T) -> VLongIntPair<K, V>): C = destination.also{c->forEach {c.putAll(this, transform)}}
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVLongLongMap<K,V>> VLongCollection<T>.associateTo(destination: C, crossinline transform: (T) -> VLongLongPair<K, V>): C = destination.also{c->forEach {c.putAll(this, transform)}}
context(a: ValueLongAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>> VLongCollection<T>.associateTo(destination: M, crossinline transform: (T) -> Pair<K, V>): M = destination.also{c->forEach {val v=transform(it); c[v.first] = v.second}}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVIntIntMap<K,V>> VLongCollection<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVIntLongMap<K,V>> VLongCollection<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableVLongIntMap<K,V>> VLongCollection<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableVLongLongMap<K,V>> VLongCollection<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueLongAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>> VLongCollection<T>.associateTo(destination: M, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): M = destination.also{c->forEach {c.put(keySelector(it),valueTransform(it))}}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>,) inline fun <T, K> VLongCollection<T>.associateByVIntLong(crossinline keySelector: (T) -> K): MutableVIntLongMap<K,T> = associateTo(MutableVIntLongMap<K,T>(size), keySelector, {it})
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>) inline fun <T, K> VLongCollection<T>.associateByVLongLong(crossinline keySelector: (T) -> K): MutableVLongLongMap<K,T> = associateTo(MutableVLongLongMap<K,T>(size), keySelector,{it})
context(a: ValueLongAdapter<T>) inline fun <T, K> VLongCollection<T>.associateByGeneric(crossinline keySelector: (T) -> K): Map<K, T> = associateTo(HashMap(size+size/4), keySelector, {it})
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>) inline fun <T, K, C:MutableVIntLongMap<K,T>> VLongCollection<T>.associateByVIntLongTo(destination: C, crossinline keySelector: (T) -> K): MutableVIntLongMap<K,T> = associateTo(MutableVIntLongMap<K,T>(size), keySelector, {it})
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>) inline fun <T, K, C:MutableVLongIntMap<K,T>> VLongCollection<T>.associateByVLongLongTo(destination: C, crossinline keySelector: (T) -> K): MutableVLongLongMap<K,T> = associateTo(MutableVLongLongMap<K,T>(size), keySelector,{it})
context(a: ValueLongAdapter<T>) inline fun <T, K, C:MutableMap<K,T>> VLongCollection<T>.associateByGenericTo(destination: C, crossinline keySelector: (T) -> K): Map<K, T> = associateTo(HashMap(size+size/4), keySelector, {it})
 inline fun <T, C : MutableVLongCollection<T>> VLongCollection<T>.toCollection(destination: C): C = destination.also{c -> c.addAll(this) }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollection<T>> VLongCollection<T>.toCollection(destination: C): C = destination.also{c->forEach {c.add(it)}}
 inline fun <T> VLongCollection<T>.toList(): VLongList<T> = this as? VLongList<T> ?: toMutableList()
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.toListGeneric(): List<T> = toMutableListGeneric()
 inline fun <T> VLongCollection<T>.toMutableList(): ArrayVLongList<T> = this as? ArrayVLongList<T> ?: toCollection(ArrayVLongList<T>(size))
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.toMutableListGeneric(): MutableList<T> = toCollection(ArrayList(size))
 inline fun <T> VLongCollection<T>.toSet(): VLongSet<T> = this as? VLongSet<T> ?: toMutableSet()
 inline fun <T> VLongCollection<T>.toMutableSet(): MutableVLongSet<T> = this as? MutableVLongSet<T> ?: toCollection(ArrayVLongSet<T>(size))
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.toSetGeneric(): Set<T> = toHashSet()
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.toHashSet(): HashSet<T> = toCollection(HashSet(size + size/4))
 inline fun <T> VLongCollection<T>.toLongArray(): LongArray = (this as? VLongArray<T>)?.collection ?: LongArray(size).also {c->forEachIndexedBits{i,e-> c[i]=e}}
 inline fun <T> VLongCollection<T>.toVLongArray(): VLongArray<T> = this as? VLongArray<T> ?: VLongArray(this)
 inline fun <T> VLongCollection<T>.toArrayGenericBits(): Array<LongBits> = (this as? VLongArray<T>)?.collection?.toTypedArray() ?: Array(size,{NULL_VALUE}).also {c->forEachIndexedBits{i,e-> c[i]=e}}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.asSequence(): Sequence<T> = asIterable().asSequence()
 inline fun <T> VLongCollection<T>.asList(): VLongList<T> = toList()
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.asListGeneric(): List<T> = toListGeneric()
 inline fun <T> VLongCollection<T>.contentEquals(other: VLongCollection<T>?): Boolean = other != null && size == other.size && allBits { other.containsBits(it) }
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.flatMap(crossinline transform: (T) ->VIntCollection<R>): ArrayVIntList<R> = flatMapTo(ArrayVIntList(size*2), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.flatMap(crossinline transform: (T) ->VLongCollection<R>): ArrayVLongList<R> = flatMapTo(ArrayVLongList(size*2), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableVIntCollection<R>> VLongCollection<T>.flatMapTo(destination: C, crossinline transform: (T) ->VIntCollection<R>): C = destination.also{forEach { destination.addAll(transform(it)) }}
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableVLongCollection<R>> VLongCollection<T>.flatMapTo(destination: C, crossinline transform: (T) ->VLongCollection<R>): C = destination.also{forEach { destination.addAll(transform(it)) }}
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.flatMapIndexed(crossinline transform: (Int,T) ->VIntCollection<R>): ArrayVIntList<R> = flatMapIndexedTo(ArrayVIntList(size*2), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.flatMapIndexed(crossinline transform: (Int,T) ->VLongCollection<R>): ArrayVLongList<R> = flatMapIndexedTo(ArrayVLongList(size*2), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableVIntCollection<R>> VLongCollection<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int,T) ->VIntCollection<R>): C = destination.also{forEachIndexed {i,e-> destination.addAll(transform(i,e)) }}
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableVLongCollection<R>> VLongCollection<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int,T) ->VLongCollection<R>): C = destination.also{forEachIndexed {i,e-> destination.addAll(transform(i,e)) }}
context(a: ValueLongAdapter<T>) inline fun <T, K> VLongCollection<T>.groupBy(crossinline keySelector: (T) -> K): MutableMap<K, MutableVLongList<T>> = groupByTo(HashMap<K,MutableVLongList<T>>(), keySelector)
context(a: ValueLongAdapter<T>) inline fun <T, K, M : MutableMap<K, MutableVLongList<T>>> VLongCollection<T>.groupByTo(destination: M, crossinline keySelector: (T) -> K): M = destination.also{c-> forEach { c.getOrPut(keySelector(it),{ ArrayVLongList(size) }).add(it) }}
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VLongCollection<T>.mapVInt(crossinline transform: (T) -> R): ArrayVIntList<R> = mapTo(ArrayVIntList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VLongCollection<T>.mapVLong(crossinline transform: (T) -> R): ArrayVLongList<R> = mapTo(ArrayVLongList<R>(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.mapGeneric(crossinline transform: (T) -> R): MutableList<R> = mapTo(ArrayList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VLongCollection<T>.mapIndexedVLong(crossinline transform: (index: Int, T) -> R): ArrayVIntList<R> = mapIndexedTo(ArrayVIntList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VLongCollection<T>.mapIndexedVLong(crossinline transform: (index: Int, T) -> R): ArrayVLongList<R> = mapIndexedTo(ArrayVLongList<R>(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.mapIndexedGeneric(crossinline transform: (index: Int, T) -> R): List<R> = mapIndexedTo(ArrayList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VLongCollection<T>.mapIndexedVLongNotNull(crossinline transform: (index: Int, T) -> R?): ArrayVIntList<R> = mapIndexedNotNullTo(ArrayVIntList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VLongCollection<T>.mapIndexedVLongNotNull(crossinline transform: (index: Int, T) -> R?): ArrayVLongList<R> = mapIndexedNotNullTo(ArrayVLongList<R>(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.mapIndexedGenericNotNull(crossinline transform: (index: Int, T) -> R?): List<R> = mapIndexedNotNullTo(ArrayList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VLongCollection<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VLongCollection<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VLongCollection<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VLongCollection<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{i,e-> destination.add(transform(i,e)) } }
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VLongCollection<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{i,e-> destination.add(transform(i,e)) } }
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VLongCollection<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{i,e-> destination.add(transform(i,e)) } }
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.mapNotNull(crossinline transform: (T) -> R?): List<R> = mapNotNullTo(mutableListOf(), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VLongCollection<T>.mapNotNullTo(destination: C, crossinline transform: (T) -> R?): C = destination.also {forEach{transform(it)?.also {destination.add(it) } }}
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VLongCollection<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VLongCollection<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VLongCollection<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.withIndex():Collection<IndexedVLong<T>> = mapIndexedGeneric{i,e-> IndexedVLong.of(i,e)}
 inline fun <T> VLongCollection<T>.distinct(): VLongSet<T> = ArrayVLongSet<T>(size).also{c-> forEachBits {c.addBits(it)}}
context(a: ValueLongAdapter<T>) inline fun <T, K> VLongCollection<T>.distinctBy(crossinline selector: (T) -> K): VLongSet<T> {
    val distinct = HashSet<K>()
    val result = ArrayVLongSet<T>(size)
    forEach {
        val k = selector(it)
        if (!distinct.contains(k)) {
            distinct.add(k)
            result.add(it)
        }
    }
    return result
}
 inline infix fun <T> VLongCollection<T>.intersect(other:VLongCollection<T>): VLongSet<T> = ArrayVLongSet<T>(size).also {c-> forEachBits{ if (other.containsBits(it)) c.addBits(it)}}
 inline infix fun <T> VLongCollection<T>.subtract(other:VLongCollection<T>): VLongSet<T> = ArrayVLongSet<T>(size).also {c-> forEachBits{ if (!other.containsBits(it)) c.addBits(it)}}
 inline infix fun <T> VLongCollection<T>.union(other:VLongCollection<T>): VLongSet<T> = toMutableSet().also{c-> c.addAll(other)}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.any(): Boolean = size > 0
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.count(): Int = size
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.count(crossinline predicate: (T) -> Boolean): Int = fold(0,{acc,e->if(predicate(e)) acc+1 else acc})
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.fold(initial: R, crossinline operation: (acc: R, T) -> R): R = foldIndexed(initial,{_,acc,e->operation(acc,e)})
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.foldIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): R {
    val accumulator = object: (Int,T)->Unit {
        var index=0
        var acc = initial
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e) }
    }
    forEachIndexed(accumulator)
    return accumulator.acc
}
context(a: ValueLongAdapter<T>) inline fun <T, C: VLongCollection<T>> C.onEach(crossinline action: (T) -> Unit): C = apply{forEach(action)}
context(a: ValueLongAdapter<T>) inline fun <T, C: VLongCollection<T>> C.onEachIndexed(crossinline action: (Int,T) -> Unit): C = apply{forEachIndexed(action)}
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> VLongCollection<T>.max(): T = reduce {l,r -> if(r>l) r else l}
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> VLongCollection<T>.maxOrNull(): T? = if(isEmpty()) null else max()
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.maxWith(comparator: Comparator<in T>): T = reduce {l,r -> if(comparator.compare(r,l)<0) r else l}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.maxWithOrNull(comparator: Comparator<in T>): T? = if(isEmpty()) null else maxWith(comparator)
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> VLongCollection<T>.maxBy(crossinline selector: (T) -> R): T {
    val accumulator = object: (Int,T,T)->T {
        var max = selector(findOrThrow {true})
        override inline fun invoke(index:Int, l:T, r: T):T {
            val newR = selector(r)
            if (newR > max) {
                max = newR
                return r
            }
            return l
        }
    }
    return reduceIndexed(accumulator)
}
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> VLongCollection<T>.maxByOrNull(crossinline selector: (T) -> R): T? = if(isEmpty()) null else maxBy(selector)
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> VLongCollection<T>.maxOf(crossinline selector: (T) -> R): R = mapReduce(selector) {max,e-> if (e>max) e else max}
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> VLongCollection<T>.maxOfOrNull(crossinline selector: (T) -> R): R? = if(isEmpty()) null else maxOf(selector)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.maxOfWith(comparator: Comparator<in R>, crossinline selector: (T) -> R): R = mapReduce(selector) {max,e-> if (comparator.compare(e,max)>1) e else max}
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.maxOfWithOrNull(comparator: Comparator<in R>, crossinline selector: (T) -> R): R? = if(isEmpty()) null else maxOfWith(comparator, selector)
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> VLongCollection<T>.min(): T = reduce {l,r -> if(r<l) r else l}
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> VLongCollection<T>.minOrNull(): T? = if(isEmpty()) null else min()
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.minWith(comparator: Comparator<in T>): T = reduce {l,r -> if(comparator.compare(r,l)<0) r else l}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.minWithOrNull(comparator: Comparator<in T>): T? = if(isEmpty()) null else minWith(comparator)
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> VLongCollection<T>.minBy(crossinline selector: (T) -> R): T {
    val accumulator = object: (Int,T,T)->T {
        var min = selector(findOrThrow {true})
        override inline fun invoke(index:Int, l:T, r: T):T {
            val newR = selector(r)
            if (newR < min) {
                min = newR
                return r
            }
            return l
        }
    }
    return reduceIndexed(accumulator)
}
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> VLongCollection<T>.minByOrNull(crossinline selector: (T) -> R): T? = if(isEmpty()) null else minBy(selector)
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> VLongCollection<T>.minOf(crossinline selector: (T) -> R): R = mapReduce(selector) {min,e-> if (e<min) e else min}
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> VLongCollection<T>.minOfOrNull(crossinline selector: (T) -> R): R? = if(isEmpty()) null else minOf(selector)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.minOfWith(comparator: Comparator<in R>, crossinline selector: (T) -> R): R = mapReduce(selector) {min,e-> if (comparator.compare(e,min)<1) e else min}
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.minOfWithOrNull(comparator: Comparator<in R>, crossinline selector: (T) -> R): R? = if(isEmpty()) null else minOfWith(comparator, selector)
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.none(): Boolean = size==0
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.none(crossinline predicate: (T) -> Boolean): Boolean = any { !predicate(it) }
context(a: ValueLongAdapter<T>) inline fun <T: Comparable<T>> VLongCollection<T>.sorted(): ArrayVLongList<T> = ArrayVLongList<T>(this).also{it.sort()}
context(a: ValueLongAdapter<T>) inline fun <T: Comparable<T>> VLongCollection<T>.sortedArray(): VLongArray<T> = toVLongArray().also{it.sort()}
context(a: ValueLongAdapter<T>) inline fun <T: Comparable<T>> VLongCollection<T>.sortedArrayDescending(): VLongArray<T> = toVLongArray().also{it.sortDescending()}
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> VLongCollection<T>.sortedBy(crossinline selector: (T) -> R?): ArrayVLongList<T> =  toMutableList().also{it.sortBy(selector)}
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> VLongCollection<T>.sortedByDescending(crossinline selector: (T) -> R?): ArrayVLongList<T> = toMutableList().also{it.sortByDescending(selector)}
context(a: ValueLongAdapter<T>) inline fun <T: Comparable<T>> VLongCollection<T>.sortedDescending(): ArrayVLongList<T> = toMutableList().also{it.sortDescending()}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sortedWith(comparator: Comparator<in T>): ArrayVLongList<T> = toMutableList().also{it.sortWith(comparator)}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumBy(crossinline selector: (T) -> Int): Int = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumByDouble(crossinline selector: (T) -> Double): Double = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumOf(crossinline selector: (T) -> Double): Double = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumOf(crossinline selector: (T) -> Int): Int = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumOf(crossinline selector: (T) -> Long): Long = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumOfUInt(crossinline selector: (T) -> UInt): UInt = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumOfULong(crossinline selector: (T) -> ULong): ULong = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.chunked(size: Int): List<ArrayVLongList<T>>{
    val results = List<ArrayVLongList<T>>((this.size + size - 1)/size) {ArrayVLongList(size)}
    val acc = object: (Int,T)->Unit {
        var listIdx=0
        var rollover=size
        override inline fun invoke(index:Int, e: T) {
            results[listIdx].add(e)
            if (index==rollover) {
                listIdx++
                rollover+=size
            }
        }
    }
    forEachIndexed(acc)
    return results
}
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.chunked(size: Int, crossinline transform: (VLongList<T>) -> R): List<R> = chunked(size).map(transform)
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.plusElement(element: T): VLongList<T> = plus(element)
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.plus(element: T): VLongList<T> = ArrayVLongList<T>(size+1).also {it.addAll(this); it.add(element) }
 inline operator fun <T> VLongCollection<T>.plus(elements: VLongCollection<T>): VLongList<T> = ArrayVLongList<T>(size+elements.size).also {it.addAll(this); it.addAll(elements) }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.plus(elements: Iterable<T>): VLongList<T> = ArrayVLongList<T>(size+1).also {it.addAll(this); it.addAll(elements) }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.plus(elements: Array<out T>): VLongList<T> = ArrayVLongList<T>(size+1).also {it.addAll(this); it.addAll(elements) }
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.minus(element: T): VLongList<T> = ArrayVLongList<T>(size).also {c-> forEach { if (it != element) c.add(it) } }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.minus(elements: Array<out T>): VLongList<T> = ArrayVLongList<T>(size).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.minus(elements:VLongCollection<T>): VLongList<T> = ArrayVLongList<T>(size).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.minus(elements:Iterable<T>): VLongList<T> = ArrayVLongList<T>(size).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.minus(elements: Sequence<T>): VLongList<T> = ArrayVLongList<T>(size).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.minusElement(element: T): VLongList<T> = minus(element)
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.partition(crossinline predicate: (T) -> Boolean): Pair<VLongList<T>, VLongList<T>> {
    val trueList = ArrayVLongList<T>(size)
    val falseList = ArrayVLongList<T>(size)
    forEach { if (predicate(it)) trueList.add(it) else falseList.add(it) }
    return trueList to falseList
}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.random(): T = random(Random.Default)
context(a: ValueLongAdapter<T>) fun <T> VLongCollection<T>.random(random: Random): T {
    if (size==0) throw NoSuchElementException()
    val findIdx = random.nextInt(size)
    return fromLong(findIndexedBits {i,e->i==findIdx})
}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.randomOrNull(): T? = randomOrNull(Random.Default)
context(a: ValueLongAdapter<T>) fun <T> VLongCollection<T>.randomOrNull(random: Random): T? = if (size==0) null else random(random)
context(a: ValueLongAdapter<T>) inline infix fun <T, R> VLongCollection<T>.zip(other: Array<out R>): MutableList<Pair<T, R>> = zip(other, {a,b->a to b})
context(a: ValueLongAdapter<T>) inline fun <T, R, V> VLongCollection<T>.zip(other: Array<out R>, crossinline transform: (a: T, b: R) -> V): MutableList<V> {
    val r = mutableListOf<V>()
    forEachIndexed { i, e -> if (i < other.size) r.add(i, transform(e, other[i])) }
    return r
}
context(ta: ValueLongAdapter<T>, ra: ValueLongAdapter<R>, va: ValueLongAdapter<V>) inline fun <T, R, V> VLongCollection<T>.zipVLongIntPair(other:VLongIndexedCollection<R>, crossinline transform: (a: T, b: R) -> V): MutableVLongList<V> {
    val r = ArrayVLongList<V>(min(size, other.size))
    forEachIndexed { i, e -> if (i < other.size) r.add(i, transform(e, other.get(i))) }
    return r
}
context(a: ValueLongAdapter<T>) inline fun <T, A : Appendable> VLongCollection<T>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence) = { it.toString() }): A {
    val appender = object: (Int,T)-> Boolean {
        var count=0
        override inline fun invoke(index: Int, e: T): Boolean {
            if (limit<0 || count++ < limit) {
                if (count != 1) buffer.append(separator)
                buffer.append(transform(e))
                if (count < limit)
                    return false
            }
            if (count >= limit)
                buffer.append(truncated)
            return true
        }
    }
    buffer.append(prefix)
    findIndexed(appender)
    buffer.append(postfix)
    return buffer
}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence) = { it.toString() }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.toVString() = joinToString(", ","{","}")
context(a: ValueLongAdapter<T>) inline fun <S, R : S, T> VLongCollection<T>.mapReduce(crossinline map:(T)->S, crossinline operation: (acc: S, S) -> S): S = mapReduceIndexed(map){i,acc,e->operation(acc,e)}
context(a: ValueLongAdapter<T>) inline fun <S, R : S, T> VLongCollection<T>.mapReduceIndexed(crossinline map:(T)->R, crossinline operation: (index:Int, acc: S, next:R) -> S): S {
    val accumulator = object: (Int,T)->Unit {
        var acc: S = map(findOrThrow{true})
        override inline fun invoke(i:Int, e: T) { if (i>0) acc = operation(i, acc, map(e)) }
    }
    forEachIndexed(accumulator)
    return accumulator.acc
}
context(a: ValueLongAdapter<T>) inline fun <S, T : S> VLongCollection<T>.reduce(crossinline operation: (acc: S, T) -> S): S = reduceIndexed<S,T>{i,acc,e -> operation(acc,e) }
context(a: ValueLongAdapter<T>) inline fun <S, T : S> VLongCollection<T>.reduceIndexed(crossinline operation: (index: Int, acc: S, T) -> S): S = mapReduceIndexed({it}, operation)
context(a: ValueLongAdapter<T>) inline fun <S, T : S> VLongCollection<T>.reduceIndexedOrNull(crossinline operation: (index: Int, acc: S, T) -> S): S? = if (size==0) return null else reduceIndexed(operation)
context(a: ValueLongAdapter<T>) inline fun <S, T : S> VLongCollection<T>.reduceOrNull(crossinline operation: (acc: S, T) -> S): S? = if (size==0) return null else reduce(operation)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VLongCollection<T>.runningFoldVInt(initial: R, crossinline operation: (acc: R, T) -> R): MutableVIntList<R> = runningFoldVIntIndexed(initial) {i,acc,e->operation(acc,e)}
context(ta: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VLongCollection<T>.runningFoldVIntIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): MutableVIntList<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = ArrayVIntList<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VLongCollection<T>.runningFoldVLong(initial: R, crossinline operation: (acc: R, T) -> R): MutableVLongList<R> = runningFoldVLongIndexed(initial) {i,acc,e->operation(acc,e)}
context(ta: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VLongCollection<T>.runningFoldVLongIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): MutableVLongList<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = ArrayVLongList<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.runningFoldGeneric(initial: R, crossinline operation: (acc: R, T) -> R): List<R> = runningFoldGenericIndexed(initial) {i,acc,e->operation(acc,e)}
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.runningFoldGenericIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): List<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = mutableListOf<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<S>) inline fun <S, T : S> VLongCollection<T>.runningReduceVInt(crossinline operation: (acc: S, T) -> S): MutableVIntList<S> = with(ra){runningReduceVIntIndexed {i,acc,e -> operation(acc,e)}}
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<S>) inline fun <S, T : S> VLongCollection<T>.runningReduceVIntIndexed(crossinline operation: (index: Int, acc: S, T) -> S): MutableVIntList<S>  {
    if (size<=1) return ArrayVIntList()
    val accumulator = object: (Int,T)->Unit {
        var acc: S = findOrThrow{true}
        var result = ArrayVIntList<S>(size-1)
        override inline fun invoke(i:Int, e: T) {
            if (i ==0) return
            acc = operation(i-1, acc, e)
            result.add(acc)
        }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.runningReduceVLong(crossinline operation: (acc: T, T) -> T): MutableVLongList<T> = runningReduceVLongIndexed {i,acc,e -> operation(acc,e)}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.runningReduceVLongIndexed(crossinline operation: (index: Int, acc: T, T) -> T): MutableVLongList<T> {
    if (size<=1) return ArrayVLongList()
    val accumulator = object: (Int,T)->Unit {
        var acc: T = findOrThrow{true}
        var result = ArrayVLongList<T>(size-1)
        override inline fun invoke(i:Int, e: T) {
            if (i ==0) return
            acc = operation(i-1, acc, e)
            result.add(acc)
        }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueLongAdapter<T>) inline fun <S, T : S> VLongCollection<T>.runningReduceGeneric(crossinline operation: (acc: S, T) -> S): List<S> = runningReduceGenericIndexed<S,T> {i,acc,e -> operation(acc,e)}
context(a: ValueLongAdapter<T>) inline fun <S, T : S> VLongCollection<T>.runningReduceGenericIndexed(crossinline operation: (index: Int, acc: S, T) -> S): List<S> {
    if (size<=1) return listOf()
    val accumulator = object: (Int,T)->Unit {
        var acc: S = findOrThrow{true}
        var result = mutableListOf<S>()
        override inline fun invoke(i:Int, e: T) {
            if (i ==0) return
            acc = operation(i-1, acc, e)
            result.add(acc)
        }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.scan(initial: R, crossinline operation: (acc: R, T) -> R): List<R> = runningFoldGeneric(initial, operation)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.scanIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): List<R> = runningFoldGenericIndexed(initial, operation)


interface ModifiableVLongCollection<T>: VLongCollection<T> {
    context(a: ValueLongAdapter<T>) fun asModifiableIterable(): MutableIterable<T>
}

interface MutableVLongCollection<T>: ModifiableVLongCollection<T> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim(minCapacity: Int)
    fun addBits(bits: LongBits): Boolean
    fun removeBits(bits: LongBits): Boolean
    context(a: ValueLongAdapter<T>) fun removeAll(predicate: (T) -> Boolean): Boolean
    fun clear()
    context(a: ValueLongAdapter<T>) override fun asModifiableIterable(): MutableIterable<T> = asIterable()
    context(a: ValueLongAdapter<T>) override fun asIterable(): MutableIterable<T>
}
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.asCollectionGeneric(): MutableCollection<T> = object : MutableCollection<T> {
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

context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.add(element: T): Boolean = addBits(a.toLong(element))
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.addAll(elements: Collection<T>): Boolean {
    ensureCapacity(size+elements.size)
    return elements.all { add(it) }
}
 inline fun <T> MutableVLongCollection<T>.addAll(elements: VLongCollection<T>): Boolean {
    ensureCapacity(size+elements.size)
    return elements.allBits { addBits(it) }
}
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.addAll(elements: Array<out T>): Boolean {
    ensureCapacity(size+elements.size)
    return elements.all { add(it) }
}
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.addAll(elements: Iterable<T>): Boolean = elements.all { add(it) }
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableVLongCollection<T>.plusAssign(elements: Array<out T>): Unit = check(addAll(elements))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableVLongCollection<T>.plusAssign(elements: Collection<T>): Unit = check(addAll(elements))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableVLongCollection<T>.plusAssign(elements: Iterable<T>): Unit = check(addAll(elements))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableVLongCollection<T>.plusAssign(element: T): Unit = check(add(element))
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.remove(element: T): Boolean = removeBits(a.toLong(element))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableVLongCollection<T>.minusAssign(element: T): Unit = check(remove(element))
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.removeAll(elements: VLongList<T>): Boolean = elements.all { remove(it)}
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.removeAll(elements: Array<T>): Boolean= elements.all { remove(it)}
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.removeAll(elements: Iterable<T>): Boolean= elements.all { remove(it)}
context(a: ValueLongAdapter<T>) fun <T> MutableVLongCollection<T>.removeAll(elements:Collection<T>): Boolean = elements.all { remove(it)}
fun <T> MutableVLongCollection<T>.removeAll(elements: VLongCollection<T>): Boolean = elements.allBits { removeBits(it) }
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableVLongCollection<T>.minusAssign(elements: VLongList<T>): Unit = check(removeAll(elements))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableVLongCollection<T>.minusAssign(elements: Array<T>): Unit = check(removeAll(elements))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableVLongCollection<T>.minusAssign(elements: Collection<T>): Unit = check(removeAll(elements))
context(a: ValueLongAdapter<T>) inline operator fun <T> MutableVLongCollection<T>.minusAssign(elements: Iterable<T>): Unit = check(removeAll(elements))
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.retainAll(elements: Collection<T>): Boolean = removeAll {!elements.contains(it)}
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongCollection<T>.retainAll(elements: VLongList<T>): Unit = check(removeAll {!elements.contains(it)})


