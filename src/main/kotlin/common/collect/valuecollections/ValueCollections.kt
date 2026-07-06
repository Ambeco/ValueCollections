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
inline fun <T> VIntCollection<T>.singleBits(crossinline predicate: (bits:IntBits) -> Boolean): IntBits {
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
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.filter(crossinline predicate: (T) -> Boolean): FlatVIntList<T> = filterTo(FlatVIntList(), predicate)
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>> VIntCollection<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.filterNot(crossinline predicate: (T) -> Boolean): FlatVIntList<T> = filter {!predicate(it)}
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
inline fun <T> VIntCollection<T>.toMutableList(): FlatVIntList<T> = this as? FlatVIntList<T> ?: toCollection(FlatVIntList<T>(size, NULL_VALUE))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toMutableListGeneric(): MutableList<T> = toCollection(ArrayList(size))
inline fun <T> VIntCollection<T>.toSet(): VIntSet<T> = this as? VIntSet<T> ?: toMutableSet()
inline fun <T> VIntCollection<T>.toMutableSet(): MutableVIntSet<T> = this as? MutableVIntSet<T> ?: toCollection(FlatVIntSet<T>(size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toSetGeneric(): Set<T> = toHashSet()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.toHashSet(): HashSet<T> = toCollection(HashSet(size + size/4))
inline fun <T> VIntCollection<T>.toIntArray(): IntArray = (this as? VIntArray<T>)?.collection ?: IntArray(size).also {c->forEachIndexedBits{i,e-> c[i]=e}}
inline fun <T> VIntCollection<T>.toVIntArray(): VIntArray<T> = this as? VIntArray<T> ?: VIntArray(this)
inline fun <T> VIntCollection<T>.toArrayGenericBits(): Array<IntBits> = (this as? VIntArray<T>)?.collection?.toTypedArray() ?: Array(size,{NULL_VALUE}).also {c->forEachIndexedBits{i,e-> c[i]=e}}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.asSequence(): Sequence<T> = asIterable().asSequence()
inline fun <T> VIntCollection<T>.asList(): VIntList<T> = toList()
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.asListGeneric(): List<T> = toListGeneric()
inline fun <T> VIntCollection<T>.contentEquals(other: VIntCollection<T>?): Boolean = other != null && size == other.size && allBits { other.containsBits(it) }
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.flatMap(crossinline transform: (T) ->VIntCollection<R>): FlatVIntList<R> = flatMapTo(FlatVIntList(size*2), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.flatMap(crossinline transform: (T) ->VLongCollection<R>): FlatVLongList<R> = flatMapTo(FlatVLongList(size*2), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.flatMapTo(destination: C, crossinline transform: (T) ->VIntCollection<R>): C = destination.also{forEach { destination.addAll(transform(it)) }}
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.flatMapTo(destination: C, crossinline transform: (T) ->VLongCollection<R>): C = destination.also{forEach { destination.addAll(transform(it)) }}
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.flatMapIndexed(crossinline transform: (Int,T) ->VIntCollection<R>): FlatVIntList<R> = flatMapIndexedTo(FlatVIntList(size*2), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.flatMapIndexed(crossinline transform: (Int,T) ->VLongCollection<R>): FlatVLongList<R> = flatMapIndexedTo(FlatVLongList(size*2), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int,T) ->VIntCollection<R>): C = destination.also{forEachIndexed {i,e-> destination.addAll(transform(i,e)) }}
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int,T) ->VLongCollection<R>): C = destination.also{forEachIndexed {i,e-> destination.addAll(transform(i,e)) }}
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntCollection<T>.groupBy(crossinline keySelector: (T) -> K): MutableMap<K, MutableVIntList<T>> = groupByTo(HashMap<K,MutableVIntList<T>>(), keySelector)
context(a: ValueIntAdapter<T>) inline fun <T, K, M : MutableMap<K, MutableVIntList<T>>> VIntCollection<T>.groupByTo(destination: M, crossinline keySelector: (T) -> K): M = destination.also{c-> forEach { c.getOrPut(keySelector(it),{ FlatVIntList(size) }).add(it) }}
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntCollection<T>.mapVInt(crossinline transform: (T) -> R): FlatVIntList<R> = mapTo(FlatVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.mapVLong(crossinline transform: (T) -> R): FlatVLongList<R> = mapTo(FlatVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.mapGeneric(crossinline transform: (T) -> R): MutableList<R> = mapTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntCollection<T>.mapIndexedVInt(crossinline transform: (index: Int, T) -> R): FlatVIntList<R> = mapIndexedTo(FlatVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.mapIndexedVLong(crossinline transform: (index: Int, T) -> R): FlatVLongList<R> = mapIndexedTo(FlatVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.mapIndexedGeneric(crossinline transform: (index: Int, T) -> R): List<R> = mapIndexedTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VIntCollection<T>.mapIndexedVIntNotNull(crossinline transform: (index: Int, T) -> R?): FlatVIntList<R> = mapIndexedNotNullTo(FlatVIntList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.mapIndexedVLongNotNull(crossinline transform: (index: Int, T) -> R?): FlatVLongList<R> = mapIndexedNotNullTo(FlatVLongList<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.mapIndexedGenericNotNull(crossinline transform: (index: Int, T) -> R?): List<R> = mapIndexedNotNullTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{i,e-> destination.add(transform(i,e)) } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{i,e-> destination.add(transform(i,e)) } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{i,e-> destination.add(transform(i,e)) } }
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntCollection<T>.mapNotNull(crossinline transform: (T) -> R?): List<R> = mapNotNullTo(mutableListOf(),  transform)
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.mapNotNullTo(destination: C, crossinline transform: (T) -> R?): C = destination.also {forEach{transform(it)?.also {destination.add(it) } }}
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VIntCollection<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VIntCollection<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VIntCollection<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.withIndex():VLongCollection<IndexedVInt<T>> = with(IndexedVInt.VLongAdapter<T>()) {mapIndexedVLong{i,e-> IndexedVInt.of(i,e)}}
inline fun <T> VIntCollection<T>.distinct(): VIntSet<T> = FlatVIntSet<T>(size).also{c-> forEachBits {c.addBits(it)}}
context(a: ValueIntAdapter<T>) inline fun <T, K> VIntCollection<T>.distinctBy(crossinline selector: (T) -> K): VIntSet<T> {
    val distinct = HashSet<K>()
    val result = FlatVIntSet<T>(size)
    forEach {
        val k = selector(it)
        if (!distinct.contains(k)) {
            distinct.add(k)
            result.add(it)
        }
    }
    return result
}
inline infix fun <T> VIntCollection<T>.intersect(other:VIntCollection<T>): VIntSet<T> = FlatVIntSet<T>(size).also {c-> forEachBits{ if (other.containsBits(it)) c.addBits(it)}}
inline infix fun <T> VIntCollection<T>.subtract(other:VIntCollection<T>): VIntSet<T> = FlatVIntSet<T>(size).also {c-> forEachBits{ if (!other.containsBits(it)) c.addBits(it)}}
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
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.sorted(): FlatVIntList<T> = FlatVIntList<T>(this).also{it.sort()}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.sortedArray(): VIntArray<T> = toVIntArray().also{it.sort()}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.sortedArrayDescending(): VIntArray<T> = toVIntArray().also{it.sortDescending()}
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.sortedBy(crossinline selector: (T) -> R?): FlatVIntList<T> =  toMutableList().also{it.sortBy(selector)}
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> VIntCollection<T>.sortedByDescending(crossinline selector: (T) -> R?): FlatVIntList<T> = toMutableList().also{it.sortByDescending(selector)}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> VIntCollection<T>.sortedDescending(): FlatVIntList<T> = toMutableList().also{it.sortDescending()}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sortedWith(comparator: Comparator<in T>): FlatVIntList<T> = toMutableList().also{it.sortWith(comparator)}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumBy(crossinline selector: (T) -> Int): Int = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumByDouble(crossinline selector: (T) -> Double): Double = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOf(crossinline selector: (T) -> Double): Double = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOf(crossinline selector: (T) -> Int): Int = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOf(crossinline selector: (T) -> Long): Long = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOfUInt(crossinline selector: (T) -> UInt): UInt = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.sumOfULong(crossinline selector: (T) -> ULong): ULong = mapReduce(selector) {acc,e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.chunked(size: Int): List<FlatVIntList<T>>{
    val results = List<FlatVIntList<T>>((this.size + size - 1)/size) {FlatVIntList(size)}
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
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.plus(element: T): VIntList<T> = FlatVIntList<T>(size+1, NULL_VALUE).also {it.addAll(this); it.add(element) }
inline operator fun <T> VIntCollection<T>.plus(elements: VIntCollection<T>): VIntList<T> = FlatVIntList<T>(size+elements.size, NULL_VALUE).also {it.addAll(this); it.addAll(elements) }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.plus(elements: Iterable<T>): VIntList<T> = FlatVIntList<T>(size+1, NULL_VALUE).also {it.addAll(this); it.addAll(elements) }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.plus(elements: Array<out T>): VIntList<T> = FlatVIntList<T>(size+1, NULL_VALUE).also {it.addAll(this); it.addAll(elements) }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minus(element: T): VIntList<T> = FlatVIntList<T>(size, NULL_VALUE).also {c-> forEach { if (it != element) c.add(it) } }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.minus(elements: Array<out T>): VIntList<T> = FlatVIntList<T>(size, NULL_VALUE).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.minus(elements:VIntCollection<T>): VIntList<T> = FlatVIntList<T>(size, NULL_VALUE).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.minus(elements:Iterable<T>): VIntList<T> = FlatVIntList<T>(size, NULL_VALUE).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntCollection<T>.minus(elements: Sequence<T>): VIntList<T> = FlatVIntList<T>(size, NULL_VALUE).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.minusElement(element: T): VIntList<T> = minus(element)
context(a: ValueIntAdapter<T>) inline fun <T> VIntCollection<T>.partition(crossinline predicate: (T) -> Boolean): Pair<VIntList<T>, VIntList<T>> {
    val trueList = FlatVIntList<T>(size, NULL_VALUE)
    val falseList = FlatVIntList<T>(size, NULL_VALUE)
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
    val r = FlatVLongList<V>(min(size, other.size))
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
        var result = FlatVIntList<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.runningFoldVLong(initial: R, crossinline operation: (acc: R, T) -> R): MutableVLongList<R> = runningFoldVLongIndexed(initial) {i,acc,e->operation(acc,e)}
context(ta: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VIntCollection<T>.runningFoldVLongIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): MutableVLongList<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = FlatVLongList<R>()
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
    if (size<=1) return FlatVIntList()
    val accumulator = object: (Int,T)->Unit {
        var acc: S = findOrThrow{true}
        var result = FlatVIntList<S>(size-1)
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
    if (size<=1) return FlatVLongList()
    val accumulator = object: (Int,T)->Unit {
        var acc: S = findOrThrow{true}
        var result = FlatVLongList<S>(size-1)
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



interface VIntIndexedCollection<T> : VIntCollection<T> {
    fun bitsAtIndex(index: Int): IntBits

    fun indexOfBits(bits: IntBits): Int
    fun indexOfFirstIndexedBits(startIndex:Int=0, predicate: (index:Int, bits:IntBits) -> Boolean): Int = indexOfFirstIndexedBitsDefault(startIndex, predicate)
    fun indexOfLastIndexedBits(endIndex:Int=-1, predicate: (index:Int, bits:IntBits) -> Boolean): Int = indexOfLastIndexedBitsDefault(endIndex, predicate)
    
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.asListGeneric() = object: List<T> {
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
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.anyIndexed(crossinline action: (index:Int, T) -> Boolean) = any(
    object: (T) -> Boolean {
        var index = 0
        override inline fun invoke(v: T) = action(index++, v)
    }
)
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.allIndexed(crossinline action: (index:Int, T) -> Boolean) = all(
    object: (T) -> Boolean {
        var index = 0
        override inline fun invoke(v: T) = action(index++, v)
    }
)
inline fun <T> VIntIndexedCollection<T>.contentEquals(other: VIntIndexedCollection<T>?): Boolean = other != null && size == other.size && allIndexedBits {i,b-> other.bitsAtIndex(i)==b }

context(a: ValueIntAdapter<T>) inline operator fun <T> VIntIndexedCollection<T>.component1(): T = elementAtIndex(0)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntIndexedCollection<T>.component2(): T = elementAtIndex(1)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntIndexedCollection<T>.component3(): T = elementAtIndex(2)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntIndexedCollection<T>.component4(): T = elementAtIndex(3)
context(a: ValueIntAdapter<T>) inline operator fun <T> VIntIndexedCollection<T>.component5(): T = elementAtIndex(4)
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.elementAtIndex(index: Int): T = fromInt(bitsAtIndex(index))
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.elementAtOrNull(index: Int): T? = if(index in 0..<size) elementAtIndex(index) else null
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.elementAtOrElse(index: Int, defaultValue: (index:Int) -> T): T = if(index in 0..<size)elementAtIndex(index) else defaultValue(index)
inline fun <T> VIntIndexedCollection<T>.getBits(index: Int): IntBits = if (index in 0..<size) bitsAtIndex(index) else NULL_VALUE
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.get(index: Int): T = if (index in 0..<size) elementAtIndex(index) else throw IndexOutOfBoundsException("$index not in 0..$size")
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.getOrElse(index: Int, defaultValue: (index:Int) -> T): T = if (index in 0..<size) elementAtIndex(index) else defaultValue(index)
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.getOrNull(index: Int): T? = if (index in 0..<size) elementAtIndex(index) else null
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.findLast(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate))
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.first(): T = elementAtIndex(0)
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.first(crossinline predicate: (T) -> Boolean): T = find(predicate) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntIndexedCollection<T>.firstNotNullOf(crossinline transform: (T) -> R?): R = firstNotNullOfOrNull(transform) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntIndexedCollection<T>.firstNotNullOfOrNull(crossinline transform: (T) -> R?): R? { for(i in 0 ..< size) return transform(elementAtIndex(i)) ?: continue; return null }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.firstOrNull(): T? = elementAtOrNull(0)
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.firstOrNull(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfFirst(predicate))
context(a: ValueIntAdapter<T>)fun <T> VIntIndexedCollection<T>.indexOf(element: T): Int = indexOfFirst {it==element}
inline fun <T> VIntIndexedCollection<T>.indexOfFirstBits(crossinline predicate: (IntBits) -> Boolean): Int { for(i in 0 ..< size) if (predicate(bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.indexOfFirst(crossinline predicate: (T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.indexOfFirstIndexed(crossinline predicate: (index:Int,T) -> Boolean): Int { for(i in 0 ..< size) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T> VIntIndexedCollection<T>.indexOfFirstIndexedBitsDefault(startIndex:Int=0, crossinline predicate: (index:Int, bits:IntBits) -> Boolean): Int { for(i in startIndex ..< size) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.indexOfLast(crossinline predicate: (T) -> Boolean): Int { for(i in size-1..0) if (predicate(elementAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.indexOfLastIndexed(crossinline predicate: (index:Int,T) -> Boolean): Int { for(i in size-1..0) if (predicate(i, elementAtIndex(i))) return i; return -1 }
inline fun <T> VIntIndexedCollection<T>.indexOfLastIndexedBitsDefault(startIndex:Int=-1, crossinline predicate: (index:Int, bits:IntBits) -> Boolean): Int {val start=if(startIndex<0||startIndex>size-1)size-1 else startIndex; for(i in start..0) if (predicate(i, bitsAtIndex(i))) return i; return -1 }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.last(): T = elementAtIndex(size-1)
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.last(crossinline predicate: (T) -> Boolean): T = findLast(predicate) ?: throw NoSuchElementException()
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.lastIndexOf(element: T): Int = indexOfLast {it==element}
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.lastOrNull(): T? = elementAtOrNull(size - 1)
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.lastOrNull(crossinline predicate: (T) -> Boolean): T? = elementAtOrNull(indexOfLast(predicate)) 
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.drop(n: Int): FlatVIntList<T> = slice(IntRange(n,size-1))
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.dropLast(n: Int): FlatVIntList<T> = slice(IntRange(0,size-n))
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.dropWhile(crossinline predicate: (T) -> Boolean): FlatVIntList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) FlatVIntList(this) else slice(IntRange(i, size))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.dropLastWhile(crossinline predicate: (T) -> Boolean): FlatVIntList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(0, i))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.filter(crossinline predicate: (T) -> Boolean): FlatVIntList<T> = filterFromMask(filterMask(predicate))
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.filterMask(crossinline predicate: (T) -> Boolean): BitSet = filterIndexedMask {_,e->predicate(e)}
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.filterFromMask(mask: BitSet): FlatVIntList<T> = FlatVIntList<T>(mask.cardinality(), NULL_VALUE).also {c-> forEachIndexedBits {i,e-> if(mask[i]) c.addBits(e)} }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.filterIndexed(crossinline predicate: (index: Int, T) -> Boolean): FlatVIntList<T> = filterFromMask(filterIndexedMask(predicate))
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>> VIntIndexedCollection<T>.filterIndexedTo(destination: C, crossinline predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> VIntIndexedCollection<T>.filterIndexedTo(destination: C, crossinline predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.filterIndexedMask(crossinline predicate: (index: Int, T) -> Boolean): BitSet {val destination=BitSet(size); forEachIndexed { i, e -> destination.set(i,predicate(i, e))}; return destination }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.filterNot(crossinline predicate: (T) -> Boolean): VIntList<T> = filter {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.filterNotIndexed(crossinline predicate: (Int,T) -> Boolean): FlatVIntList<T> = filterIndexed {i,e->!predicate(i,e) }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntCollection<T>> VIntIndexedCollection<T>.filterNotIndexedTo(destination: C, crossinline predicate: (Int,T) -> Boolean): C = filterIndexedTo(destination) {i,e->!predicate(i,e) }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntIndexedCollection<T>> VIntIndexedCollection<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> VIntIndexedCollection<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableVIntIndexedCollection<T>> VIntIndexedCollection<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> VIntIndexedCollection<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.slice(indices: IntRange): FlatVIntList<T> = copyInto(FlatVIntList<T>(indices.last-indices.first, NULL_VALUE), 0, indices.first, indices.last)
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.slice(indices: Iterable<Int>): FlatVIntList<T> = FlatVIntList<T>(if (indices is Collection<Int>) indices.size else size/8, NULL_VALUE).also { for(i in indices) it.addBits(bitsAtIndex(i)) }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.sliceArray(indices: Collection<Int>): VIntArray<T> = VIntArray<T>(indices.size, NULL_VALUE).also {c-> indices.forEachIndexed {i,ei-> c.set(i, get(ei))}}
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.sliceArray(indices: IntRange): VIntArray<T> = VIntArray<T>(indices.last -indices.first +1, NULL_VALUE).also { c-> for (i in indices) c.set(i-indices.first, get(i))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.take(n: Int): FlatVIntList<T> = slice(IntRange(0,n))
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.takeLast(n: Int): FlatVIntList<T> = slice(IntRange(size-n,size))
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.takeLastWhile(crossinline predicate: (T) -> Boolean): FlatVIntList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) FlatVIntList<T>(this) else slice(IntRange(0, i))}
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.slice(indices: Collection<Int>): FlatVIntList<T> = filterIndexed {i,e-> indices.contains(i) }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.takeWhile(crossinline predicate: (T) -> Boolean): FlatVIntList<T> = FlatVIntList<T>().also {c-> any { val p=predicate(it); if (p) c.add(it); p } }
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.takeWhileIndexed(crossinline predicate: (Int,T) -> Boolean): FlatVIntList<T> = FlatVIntList<T>().also {c-> anyIndexed {i,e-> val p=predicate(i,e); if (p) c.add(e); p } }
inline fun <T, C: MutableVIntIndexedCollection<T>> VIntIndexedCollection<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{forEachIndexedBits{i,e-> if(i in startIndex..endIndex) destination.addBits(i-startIndex+destinationOffset, e)}}
context(a: ValueIntAdapter<T>) inline fun <T, C: MutableList<T>> VIntIndexedCollection<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{forEachIndexed{i,e-> if(i in startIndex..endIndex) destination.add(i-startIndex+destinationOffset, e)}}
inline fun <T> VIntIndexedCollection<T>.reversed(): FlatVIntList<T> = FlatVIntList<T>(size, NULL_VALUE).also {forEachIndexedBits{i,e-> it.setBits(size-i-1, e) }}
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.contentEquals(other: VIntIndexedCollection<T>?): Boolean = other != null && size == other.size && this.indexOfFirstIndexedBits {i,e-> other.bitsAtIndex(i) != e } == -1
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.shuffle(): Unit = shuffle(Random.Default)
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.shuffle(random: Random): Unit {
}
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.zipWithNext(): List<Pair<T, T>> = zipWithNext {l,r -> l to r}
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntIndexedCollection<T>.zipWithNext(crossinline transform: (a: T, b: T) -> R): List<R> = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntIndexedCollection<T>.reduceRight(crossinline operation: (T, acc: T) -> T): T = reduceRightIndexed {i,e,acc -> operation(e,acc)}
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntIndexedCollection<T>.reduceRightIndexed(crossinline operation: (index: Int, T, acc: T) -> T): T = throw NotImplementedError()
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntIndexedCollection<T>.reduceRightIndexedOrNull(crossinline operation: (index: Int, T, acc: T) -> T): T? = if (size<2) null else reduceRightIndexed(operation)
context(a: ValueIntAdapter<T>) inline fun <S, T : S> VIntIndexedCollection<T>.reduceRightOrNull(crossinline operation: (T, acc: T) -> T): T? = if (size<2) null else reduceRight(operation)
context(a: ValueIntAdapter<T>) inline fun <T> VIntIndexedCollection<T>.windowed(windowSize: Int, step: Int = 1, partialWindows: Boolean = false): MutableList<MutableList<T>> {
    val list = MutableList<MutableList<T>>(size-windowSize) { mutableListOf() }
    for (i in 0 ..< size-windowSize) {
        for (j in 0..<windowSize)
            list[i].add(elementAtIndex(i+j))
    }
    return list
}
context(a: ValueIntAdapter<T>) inline fun <T, R> VIntIndexedCollection<T>.windowed(windowSize: Int, step: Int = 1, partialWindows: Boolean = false, crossinline transform: (List<T>) -> R): List<R> = throw NotImplementedError()



// can modify elements, but not add or remove
interface ModifiableVIntIndexedCollection<T>: VIntIndexedCollection<T>, ModifiableVIntCollection<T> {
    fun setBits(index: Int, bits: IntBits)
}
context(a: ValueIntAdapter<T>) inline fun <T> ModifiableVIntIndexedCollection<T>.asListGeneric() = object: MutableList<T> {
    override val size: Int get() = this@asListGeneric.size
    override inline fun isEmpty(): Boolean = this@asListGeneric.size==0
    override inline fun contains(element: T): Boolean = this@asListGeneric.contains(element)
    override inline fun iterator(): MutableIterator<T> = this@asListGeneric.asModifiableIterable().iterator()
    override inline fun containsAll(elements: Collection<T>): Boolean = this@asListGeneric.containsAll(elements)
    override inline fun get(index: Int): T = this@asListGeneric.get(index)
    override inline fun indexOf(element: T): Int = this@asListGeneric.indexOf(element)
    override inline fun lastIndexOf(element: T): Int = this@asListGeneric.lastIndexOf(element)
    override inline fun add(element: T): Boolean = throw NotImplementedError()
    override inline fun remove(element: T): Boolean = throw NotImplementedError()
    override inline fun addAll(elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun removeAll(elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun retainAll(elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun clear() = throw NotImplementedError()
    override inline fun set(index: Int, element: T): T = this@asListGeneric.set(index, element)
    override inline fun add(index: Int, element: T) = throw NotImplementedError()
    override inline fun removeAt(index: Int): T = throw NotImplementedError()
    override inline fun listIterator(): MutableListIterator<T> = throw NotImplementedError() // this@asListGeneric.listIterator()
    override inline fun listIterator(index: Int): MutableListIterator<T> = throw NotImplementedError() // this@asListGeneric.listIterator(index)
    override inline fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = throw NotImplementedError() // this@asListGeneric.subList(fromIndex, toIndex)
}
context(a: ValueIntAdapter<T>) inline fun <T> ModifiableVIntIndexedCollection<T>.set(index: Int, value: T): T {setBits(index, a.toInt(value)); return value}
context(a: ValueIntAdapter<T>)inline fun <T : Comparable<T>> ModifiableVIntIndexedCollection<T>.sort(): Unit = asListGeneric().sort()
context(a: ValueIntAdapter<T>)inline fun <T : Comparable<T>> ModifiableVIntIndexedCollection<T>.sort(fromIndex: Int, toIndex: Int): Unit = asListGeneric().subList(fromIndex, toIndex).sort()
context(a: ValueIntAdapter<T>)inline fun <T : Comparable<T>> ModifiableVIntIndexedCollection<T>.sortDescending(): Unit = asListGeneric().sortDescending()
context(a: ValueIntAdapter<T>)inline fun <T : Comparable<T>> ModifiableVIntIndexedCollection<T>.sortDescending(fromIndex: Int, toIndex: Int): Unit = asListGeneric().subList(fromIndex, toIndex).sortDescending()
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> ModifiableVIntIndexedCollection<T>.sortBy(crossinline selector: (T) -> R?): Unit = asListGeneric().sortBy(selector)
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> ModifiableVIntIndexedCollection<T>.sortByDescending(crossinline selector: (T) -> R?): Unit = asListGeneric().sortByDescending(selector)
context(a: ValueIntAdapter<T>) inline fun <T> ModifiableVIntIndexedCollection<T>.sortWith(comparator: Comparator<in T>): Unit = asListGeneric().sortWith(comparator)


// can modify, add, and remove elements
interface MutableVIntIndexedCollection<T>: ModifiableVIntIndexedCollection<T>, MutableVIntCollection<T> {
    fun addBits(index: Int, bits: IntBits)
    fun addAll(index: Int, elements: VIntCollection<T>): Boolean
    context(a: ValueIntAdapter<T>) fun addAll(index: Int, elements: Collection<T>): Boolean

    context(a: ValueIntAdapter<T>) fun removeAt(index: Int): T
    fun removeRange(start: Int, end: Int)
    fun removeAllIndexedBits(predicate: (index: Int, bits: IntBits) -> Boolean): Boolean
}
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntIndexedCollection<T>.asListGeneric() = object: MutableList<T> {
    override val size: Int get() = this@asListGeneric.size
    override inline fun isEmpty(): Boolean = this@asListGeneric.size==0
    override inline fun contains(element: T): Boolean = this@asListGeneric.contains(element)
    override inline fun iterator(): MutableIterator<T> = this@asListGeneric.asIterable().iterator()
    override inline fun containsAll(elements: Collection<T>): Boolean = this@asListGeneric.containsAll(elements)
    override inline fun get(index: Int): T = this@asListGeneric.get(index)
    override inline fun indexOf(element: T): Int = this@asListGeneric.indexOf(element)
    override inline fun lastIndexOf(element: T): Int = this@asListGeneric.lastIndexOf(element)
    override inline fun add(element: T): Boolean = this@asListGeneric.add(element)
    override inline fun remove(element: T): Boolean = this@asListGeneric.remove(element)
    override inline fun addAll(elements: Collection<T>): Boolean = this@asListGeneric.addAll(elements)
    override inline fun addAll(index: Int, elements: Collection<T>): Boolean = this@asListGeneric.addAll(index,elements)
    override inline fun removeAll(elements: Collection<T>): Boolean = this@asListGeneric.removeAll(elements)
    override inline fun retainAll(elements: Collection<T>): Boolean = this@asListGeneric.retainAll(elements)
    override inline fun clear() = this@asListGeneric.clear()
    override inline fun set(index: Int, element: T): T = this@asListGeneric.set(index, element)
    override inline fun add(index: Int, element: T) = this@asListGeneric.add(index, element)
    override inline fun removeAt(index: Int): T = this@asListGeneric.removeAt(index)
    override inline fun listIterator(): MutableListIterator<T> = throw NotImplementedError() // this@asListGeneric.listIterator()
    override inline fun listIterator(index: Int): MutableListIterator<T> = throw NotImplementedError() // this@asListGeneric.listIterator(index)
    override inline fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = throw NotImplementedError() // this@asListGeneric.subList(fromIndex, toIndex)
}

context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntIndexedCollection<T>.add(index: Int, element: T): Unit = addBits(index, a.toInt(element))
context(a: ValueIntAdapter<T>) inline fun <T> MutableVIntIndexedCollection<T>.retainAll(elements: VIntList<T>): Boolean = removeAllIndexedBits{i,b-> !elements.containsBits(b)}







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
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.filter(crossinline predicate: (T) -> Boolean): FlatVLongList<T> = filterTo(FlatVLongList(), predicate)
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongCollection<T>> VLongCollection<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.filterNot(crossinline predicate: (T) -> Boolean): FlatVLongList<T> = filter {!predicate(it)}
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
inline fun <T> VLongCollection<T>.toMutableList(): FlatVLongList<T> = this as? FlatVLongList<T> ?: toCollection(FlatVLongList<T>(size))
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.toMutableListGeneric(): MutableList<T> = toCollection(ArrayList(size))
inline fun <T> VLongCollection<T>.toSet(): VLongSet<T> = this as? VLongSet<T> ?: toMutableSet()
inline fun <T> VLongCollection<T>.toMutableSet(): MutableVLongSet<T> = this as? MutableVLongSet<T> ?: toCollection(FlatVLongSet<T>(size))
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.toSetGeneric(): Set<T> = toHashSet()
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.toHashSet(): HashSet<T> = toCollection(HashSet(size + size/4))
inline fun <T> VLongCollection<T>.toLongArray(): LongArray = (this as? VLongArray<T>)?.collection ?: LongArray(size).also {c->forEachIndexedBits{i,e-> c[i]=e}}
inline fun <T> VLongCollection<T>.toVLongArray(): VLongArray<T> = this as? VLongArray<T> ?: VLongArray(this)
inline fun <T> VLongCollection<T>.toArrayGenericBits(): Array<LongBits> = (this as? VLongArray<T>)?.collection?.toTypedArray() ?: Array(size,{NULL_VALUE}).also {c->forEachIndexedBits{i,e-> c[i]=e}}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.asSequence(): Sequence<T> = asIterable().asSequence()
inline fun <T> VLongCollection<T>.asList(): VLongList<T> = toList()
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.asListGeneric(): List<T> = toListGeneric()
inline fun <T> VLongCollection<T>.contentEquals(other: VLongCollection<T>?): Boolean = other != null && size == other.size && allBits { other.containsBits(it) }
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.flatMap(crossinline transform: (T) ->VIntCollection<R>): FlatVIntList<R> = flatMapTo(FlatVIntList(size*2), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.flatMap(crossinline transform: (T) ->VLongCollection<R>): FlatVLongList<R> = flatMapTo(FlatVLongList(size*2), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableVIntCollection<R>> VLongCollection<T>.flatMapTo(destination: C, crossinline transform: (T) ->VIntCollection<R>): C = destination.also{forEach { destination.addAll(transform(it)) }}
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableVLongCollection<R>> VLongCollection<T>.flatMapTo(destination: C, crossinline transform: (T) ->VLongCollection<R>): C = destination.also{forEach { destination.addAll(transform(it)) }}
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.flatMapIndexed(crossinline transform: (Int,T) ->VIntCollection<R>): FlatVIntList<R> = flatMapIndexedTo(FlatVIntList(size*2), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.flatMapIndexed(crossinline transform: (Int,T) ->VLongCollection<R>): FlatVLongList<R> = flatMapIndexedTo(FlatVLongList(size*2), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableVIntCollection<R>> VLongCollection<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int,T) ->VIntCollection<R>): C = destination.also{forEachIndexed {i,e-> destination.addAll(transform(i,e)) }}
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableVLongCollection<R>> VLongCollection<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int,T) ->VLongCollection<R>): C = destination.also{forEachIndexed {i,e-> destination.addAll(transform(i,e)) }}
context(a: ValueLongAdapter<T>) inline fun <T, K> VLongCollection<T>.groupBy(crossinline keySelector: (T) -> K): MutableMap<K, MutableVLongList<T>> = groupByTo(HashMap<K,MutableVLongList<T>>(), keySelector)
context(a: ValueLongAdapter<T>) inline fun <T, K, M : MutableMap<K, MutableVLongList<T>>> VLongCollection<T>.groupByTo(destination: M, crossinline keySelector: (T) -> K): M = destination.also{c-> forEach { c.getOrPut(keySelector(it),{ FlatVLongList(size) }).add(it) }}
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VLongCollection<T>.mapVInt(crossinline transform: (T) -> R): FlatVIntList<R> = mapTo(FlatVIntList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VLongCollection<T>.mapVLong(crossinline transform: (T) -> R): FlatVLongList<R> = mapTo(FlatVLongList<R>(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.mapGeneric(crossinline transform: (T) -> R): MutableList<R> = mapTo(ArrayList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VLongCollection<T>.mapIndexedVLong(crossinline transform: (index: Int, T) -> R): FlatVIntList<R> = mapIndexedTo(FlatVIntList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VLongCollection<T>.mapIndexedVLong(crossinline transform: (index: Int, T) -> R): FlatVLongList<R> = mapIndexedTo(FlatVLongList<R>(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.mapIndexedGeneric(crossinline transform: (index: Int, T) -> R): List<R> = mapIndexedTo(ArrayList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> VLongCollection<T>.mapIndexedVLongNotNull(crossinline transform: (index: Int, T) -> R?): FlatVIntList<R> = mapIndexedNotNullTo(FlatVIntList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VLongCollection<T>.mapIndexedVLongNotNull(crossinline transform: (index: Int, T) -> R?): FlatVLongList<R> = mapIndexedNotNullTo(FlatVLongList<R>(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.mapIndexedGenericNotNull(crossinline transform: (index: Int, T) -> R?): List<R> = mapIndexedNotNullTo(ArrayList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VLongCollection<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VLongCollection<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VLongCollection<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{c->forEachIndexed{i,e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VLongCollection<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{i,e-> destination.add(transform(i,e)) } }
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VLongCollection<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{i,e-> destination.add(transform(i,e)) } }
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VLongCollection<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{i,e-> destination.add(transform(i,e)) } }
context(a: ValueLongAdapter<T>) inline fun <T, R> VLongCollection<T>.mapNotNull(crossinline transform: (T) -> R?): List<R> = mapNotNullTo(mutableListOf(),  transform)
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VLongCollection<T>.mapNotNullTo(destination: C, crossinline transform: (T) -> R?): C = destination.also {forEach{transform(it)?.also {destination.add(it) } }}
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableVIntCollection<R>> VLongCollection<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableVLongCollection<R>> VLongCollection<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>> VLongCollection<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.withIndex():Collection<IndexedVLong<T>> = mapIndexedGeneric{i,e-> IndexedVLong.of(i,e)}
inline fun <T> VLongCollection<T>.distinct(): VLongSet<T> = FlatVLongSet<T>(size).also{c-> forEachBits {c.addBits(it)}}
context(a: ValueLongAdapter<T>) inline fun <T, K> VLongCollection<T>.distinctBy(crossinline selector: (T) -> K): VLongSet<T> {
    val distinct = HashSet<K>()
    val result = FlatVLongSet<T>(size)
    forEach {
        val k = selector(it)
        if (!distinct.contains(k)) {
            distinct.add(k)
            result.add(it)
        }
    }
    return result
}
inline infix fun <T> VLongCollection<T>.intersect(other:VLongCollection<T>): VLongSet<T> = FlatVLongSet<T>(size).also {c-> forEachBits{ if (other.containsBits(it)) c.addBits(it)}}
inline infix fun <T> VLongCollection<T>.subtract(other:VLongCollection<T>): VLongSet<T> = FlatVLongSet<T>(size).also {c-> forEachBits{ if (!other.containsBits(it)) c.addBits(it)}}
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
context(a: ValueLongAdapter<T>) inline fun <T: Comparable<T>> VLongCollection<T>.sorted(): FlatVLongList<T> = FlatVLongList<T>(this).also{it.sort()}
context(a: ValueLongAdapter<T>) inline fun <T: Comparable<T>> VLongCollection<T>.sortedArray(): VLongArray<T> = toVLongArray().also{it.sort()}
context(a: ValueLongAdapter<T>) inline fun <T: Comparable<T>> VLongCollection<T>.sortedArrayDescending(): VLongArray<T> = toVLongArray().also{it.sortDescending()}
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> VLongCollection<T>.sortedBy(crossinline selector: (T) -> R?): FlatVLongList<T> =  toMutableList().also{it.sortBy(selector)}
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> VLongCollection<T>.sortedByDescending(crossinline selector: (T) -> R?): FlatVLongList<T> = toMutableList().also{it.sortByDescending(selector)}
context(a: ValueLongAdapter<T>) inline fun <T: Comparable<T>> VLongCollection<T>.sortedDescending(): FlatVLongList<T> = toMutableList().also{it.sortDescending()}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sortedWith(comparator: Comparator<in T>): FlatVLongList<T> = toMutableList().also{it.sortWith(comparator)}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumBy(crossinline selector: (T) -> Int): Int = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumByDouble(crossinline selector: (T) -> Double): Double = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumOf(crossinline selector: (T) -> Double): Double = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumOf(crossinline selector: (T) -> Int): Int = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumOf(crossinline selector: (T) -> Long): Long = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumOfUInt(crossinline selector: (T) -> UInt): UInt = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.sumOfULong(crossinline selector: (T) -> ULong): ULong = mapReduce(selector) {acc,e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.chunked(size: Int): List<FlatVLongList<T>>{
    val results = List<FlatVLongList<T>>((this.size + size - 1)/size) {FlatVLongList(size)}
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
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.plus(element: T): VLongList<T> = FlatVLongList<T>(size+1).also {it.addAll(this); it.add(element) }
inline operator fun <T> VLongCollection<T>.plus(elements: VLongCollection<T>): VLongList<T> = FlatVLongList<T>(size+elements.size).also {it.addAll(this); it.addAll(elements) }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.plus(elements: Iterable<T>): VLongList<T> = FlatVLongList<T>(size+1).also {it.addAll(this); it.addAll(elements) }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.plus(elements: Array<out T>): VLongList<T> = FlatVLongList<T>(size+1).also {it.addAll(this); it.addAll(elements) }
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.minus(element: T): VLongList<T> = FlatVLongList<T>(size).also {c-> forEach { if (it != element) c.add(it) } }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.minus(elements: Array<out T>): VLongList<T> = FlatVLongList<T>(size).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.minus(elements:VLongCollection<T>): VLongList<T> = FlatVLongList<T>(size).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.minus(elements:Iterable<T>): VLongList<T> = FlatVLongList<T>(size).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueLongAdapter<T>) inline operator fun <T> VLongCollection<T>.minus(elements: Sequence<T>): VLongList<T> = FlatVLongList<T>(size).also {c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.minusElement(element: T): VLongList<T> = minus(element)
context(a: ValueLongAdapter<T>) inline fun <T> VLongCollection<T>.partition(crossinline predicate: (T) -> Boolean): Pair<VLongList<T>, VLongList<T>> {
    val trueList = FlatVLongList<T>(size)
    val falseList = FlatVLongList<T>(size)
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
    val r = FlatVLongList<V>(min(size, other.size))
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
        var result = FlatVIntList<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VLongCollection<T>.runningFoldVLong(initial: R, crossinline operation: (acc: R, T) -> R): MutableVLongList<R> = runningFoldVLongIndexed(initial) {i,acc,e->operation(acc,e)}
context(ta: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> VLongCollection<T>.runningFoldVLongIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): MutableVLongList<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = FlatVLongList<R>()
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
    if (size<=1) return FlatVIntList()
    val accumulator = object: (Int,T)->Unit {
        var acc: S = findOrThrow{true}
        var result = FlatVIntList<S>(size-1)
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
    if (size<=1) return FlatVLongList()
    val accumulator = object: (Int,T)->Unit {
        var acc: T = findOrThrow{true}
        var result = FlatVLongList<T>(size-1)
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



interface VLongIndexedCollection<T> : VLongCollection<T> {
    fun bitsAtIndex(index: Int): LongBits

    fun indexOfBits(bits: LongBits): Int
    fun indexOfFirstIndexedBits(startIndex:Int=0, predicate: (index:Int, bits:LongBits) -> Boolean): Int = indexOfFirstIndexedBitsDefault(startIndex, predicate)
    fun indexOfLastIndexedBits(endIndex:Int=-1, predicate: (index:Int, bits:LongBits) -> Boolean): Int = indexOfLastIndexedBitsDefault(endIndex, predicate)

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print K.toString", ReplaceWith("toVString()"))
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
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.drop(n: Int): FlatVLongList<T> = slice(IntRange(n,size-1))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.dropLast(n: Int): FlatVLongList<T> = slice(IntRange(0,size-n))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.dropWhile(crossinline predicate: (T) -> Boolean): FlatVLongList<T> {val i=indexOfFirst{!predicate(it)}; return if(i==-1) FlatVLongList(this) else slice(IntRange(i, size))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.dropLastWhile(crossinline predicate: (T) -> Boolean): FlatVLongList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) toMutableList() else slice(IntRange(0, i))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filter(crossinline predicate: (T) -> Boolean): FlatVLongList<T> = filterFromMask(filterMask(predicate))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filterMask(crossinline predicate: (T) -> Boolean): BitSet = filterIndexedMask {_,e->predicate(e)}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filterFromMask(mask: BitSet): FlatVLongList<T> = FlatVLongList<T>(mask.cardinality()).also {c-> forEachIndexedBits {i,e-> if(mask[i]) c.addBits(e)} }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filterIndexed(crossinline predicate: (index: Int, T) -> Boolean): FlatVLongList<T> = filterFromMask(filterIndexedMask(predicate))
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongCollection<T>> VLongIndexedCollection<T>.filterIndexedTo(destination: C, crossinline predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollection<T>> VLongIndexedCollection<T>.filterIndexedTo(destination: C, crossinline predicate: (index: Int, T) -> Boolean): C = destination.also { forEachIndexed { i, e -> if (predicate(i, e)) destination.add(e) } }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filterIndexedMask(crossinline predicate: (index: Int, T) -> Boolean): BitSet {val destination=BitSet(size); forEachIndexed { i, e -> destination.set(i,predicate(i, e))}; return destination }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filterNot(crossinline predicate: (T) -> Boolean): VLongList<T> = filter {!predicate(it)}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.filterNotIndexed(crossinline predicate: (Int,T) -> Boolean): FlatVLongList<T> = filterIndexed {i,e->!predicate(i,e) }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongCollection<T>> VLongIndexedCollection<T>.filterNotIndexedTo(destination: C, crossinline predicate: (Int,T) -> Boolean): C = filterIndexedTo(destination) {i,e->!predicate(i,e) }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongIndexedCollection<T>> VLongIndexedCollection<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollection<T>> VLongIndexedCollection<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableVLongIndexedCollection<T>> VLongIndexedCollection<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollection<T>> VLongIndexedCollection<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.slice(indices: IntRange): FlatVLongList<T> = copyInto(FlatVLongList<T>(indices.last-indices.first, NULL_VALUE), 0, indices.first, indices.last)
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.slice(indices: Iterable<Int>): FlatVLongList<T> = FlatVLongList<T>(if (indices is Collection<Int>) indices.size else size/8).also { for(i in indices) it.addBits(bitsAtIndex(i)) }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.sliceArray(indices: Collection<Int>): VLongArray<T> = VLongArray<T>(indices.size, NULL_VALUE).also {c-> indices.forEachIndexed {i,ei-> c.set(i, get(ei))}}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.sliceArray(indices: IntRange): VLongArray<T> = VLongArray<T>(indices.last -indices.first +1, NULL_VALUE).also { c-> for (i in indices) c.set(i-indices.first, get(i))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.take(n: Int): FlatVLongList<T> = slice(IntRange(0,n))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.takeLast(n: Int): FlatVLongList<T> = slice(IntRange(size-n,size))
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.takeLastWhile(crossinline predicate: (T) -> Boolean): FlatVLongList<T> {val i=indexOfLast{!predicate(it)}; return if(i==-1) FlatVLongList<T>(this) else slice(IntRange(0, i))}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.slice(indices: Collection<Int>): FlatVLongList<T> = filterIndexed {i,e-> indices.contains(i) }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.takeWhile(crossinline predicate: (T) -> Boolean): FlatVLongList<T> = FlatVLongList<T>().also {c-> any { val p=predicate(it); if (p) c.add(it); p } }
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.takeWhileIndexed(crossinline predicate: (Int,T) -> Boolean): FlatVLongList<T> = FlatVLongList<T>().also {c-> anyIndexed {i,e-> val p=predicate(i,e); if (p) c.add(e); p } }
inline fun <T, C: MutableVLongIndexedCollection<T>> VLongIndexedCollection<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{forEachIndexedBits{i,e-> if(i in startIndex..endIndex) destination.addBits(i-startIndex+destinationOffset, e)}}
context(a: ValueLongAdapter<T>) inline fun <T, C: MutableList<T>> VLongIndexedCollection<T>.copyInto(destination: C, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): C = destination.also{forEachIndexed{i,e-> if(i in startIndex..endIndex) destination.add(i-startIndex+destinationOffset, e)}}
inline fun <T> VLongIndexedCollection<T>.reversed(): FlatVLongList<T> = FlatVLongList<T>(size).also {forEachIndexedBits{i,e-> it.setBits(size-i-1, e) }}
context(a: ValueLongAdapter<T>) inline fun <T> VLongIndexedCollection<T>.contentEquals(other: VLongIndexedCollection<T>?): Boolean = other != null && size == other.size && this.indexOfFirstIndexedBits {i,e-> other.bitsAtIndex(i) != e } == -1
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



// can modify elements, but not add or remove
interface ModifiableVLongIndexedCollection<T>: VLongIndexedCollection<T>, ModifiableVLongCollection<T> {
    fun setBits(index: Int, bits: LongBits)
}
context(a: ValueLongAdapter<T>) inline fun <T> ModifiableVLongIndexedCollection<T>.asListGeneric() = object: MutableList<T> {
    override val size: Int get() = this@asListGeneric.size
    override inline fun isEmpty(): Boolean = this@asListGeneric.size==0
    override inline fun contains(element: T): Boolean = this@asListGeneric.contains(element)
    override inline fun iterator(): MutableIterator<T> = this@asListGeneric.asModifiableIterable().iterator()
    override inline fun containsAll(elements: Collection<T>): Boolean = this@asListGeneric.containsAll(elements)
    override inline fun get(index: Int): T = this@asListGeneric.get(index)
    override inline fun indexOf(element: T): Int = this@asListGeneric.indexOf(element)
    override inline fun lastIndexOf(element: T): Int = this@asListGeneric.lastIndexOf(element)
    override inline fun add(element: T): Boolean = throw NotImplementedError()
    override inline fun remove(element: T): Boolean = throw NotImplementedError()
    override inline fun addAll(elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun addAll(index: Int, elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun removeAll(elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun retainAll(elements: Collection<T>): Boolean = throw NotImplementedError()
    override inline fun clear() = throw NotImplementedError()
    override inline fun set(index: Int, element: T): T = this@asListGeneric.set(index, element)
    override inline fun add(index: Int, element: T) = throw NotImplementedError()
    override inline fun removeAt(index: Int): T = throw NotImplementedError()
    override inline fun listIterator(): MutableListIterator<T> = throw NotImplementedError() // this@asListGeneric.listIterator()
    override inline fun listIterator(index: Int): MutableListIterator<T> = throw NotImplementedError() // this@asListGeneric.listIterator(index)
    override inline fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = throw NotImplementedError() // this@asListGeneric.subList(fromIndex, toIndex)
}
context(a: ValueLongAdapter<T>) inline fun <T> ModifiableVLongIndexedCollection<T>.set(index: Int, value: T): T {setBits(index, a.toLong(value)); return value}
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> ModifiableVLongIndexedCollection<T>.sort(): Unit = asListGeneric().sort()
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> ModifiableVLongIndexedCollection<T>.sort(fromIndex: Int, toIndex: Int): Unit = asListGeneric().subList(fromIndex, toIndex).sort()
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> ModifiableVLongIndexedCollection<T>.sortDescending(): Unit = asListGeneric().sortDescending()
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> ModifiableVLongIndexedCollection<T>.sortDescending(fromIndex: Int, toIndex: Int): Unit = asListGeneric().subList(fromIndex, toIndex).sortDescending()
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> ModifiableVLongIndexedCollection<T>.sortBy(crossinline selector: (T) -> R?): Unit = asListGeneric().sortBy(selector)
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> ModifiableVLongIndexedCollection<T>.sortByDescending(crossinline selector: (T) -> R?): Unit = asListGeneric().sortByDescending(selector)
context(a: ValueLongAdapter<T>) inline fun <T> ModifiableVLongIndexedCollection<T>.sortWith(comparator: Comparator<in T>): Unit = asListGeneric().sortWith(comparator)


// can modify, add, and remove elements
interface MutableVLongIndexedCollection<T>: ModifiableVLongIndexedCollection<T>, MutableVLongCollection<T> {
    fun addBits(index: Int, bits: LongBits)
    fun addAll(index: Int, elements: VLongCollection<T>): Boolean
    context(a: ValueLongAdapter<T>) fun addAll(index: Int, elements: Collection<T>): Boolean

    context(a: ValueLongAdapter<T>) fun removeAt(index: Int): T
    fun removeRange(start: Int, end: Int)
    fun removeAllIndexedBits(predicate: (index: Int, bits: LongBits) -> Boolean): Boolean
}
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongIndexedCollection<T>.asListGeneric() = object: MutableList<T> {
    override val size: Int get() = this@asListGeneric.size
    override inline fun isEmpty(): Boolean = this@asListGeneric.size==0
    override inline fun contains(element: T): Boolean = this@asListGeneric.contains(element)
    override inline fun iterator(): MutableIterator<T> = this@asListGeneric.asIterable().iterator()
    override inline fun containsAll(elements: Collection<T>): Boolean = this@asListGeneric.containsAll(elements)
    override inline fun get(index: Int): T = this@asListGeneric.get(index)
    override inline fun indexOf(element: T): Int = this@asListGeneric.indexOf(element)
    override inline fun lastIndexOf(element: T): Int = this@asListGeneric.lastIndexOf(element)
    override inline fun add(element: T): Boolean = this@asListGeneric.add(element)
    override inline fun remove(element: T): Boolean = this@asListGeneric.remove(element)
    override inline fun addAll(elements: Collection<T>): Boolean = this@asListGeneric.addAll(elements)
    override inline fun addAll(index: Int, elements: Collection<T>): Boolean = this@asListGeneric.addAll(index,elements)
    override inline fun removeAll(elements: Collection<T>): Boolean = this@asListGeneric.removeAll(elements)
    override inline fun retainAll(elements: Collection<T>): Boolean = this@asListGeneric.retainAll(elements)
    override inline fun clear() = this@asListGeneric.clear()
    override inline fun set(index: Int, element: T): T = this@asListGeneric.set(index, element)
    override inline fun add(index: Int, element: T) = this@asListGeneric.add(index, element)
    override inline fun removeAt(index: Int): T = this@asListGeneric.removeAt(index)
    override inline fun listIterator(): MutableListIterator<T> = throw NotImplementedError() // this@asListGeneric.listIterator()
    override inline fun listIterator(index: Int): MutableListIterator<T> = throw NotImplementedError() // this@asListGeneric.listIterator(index)
    override inline fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = throw NotImplementedError() // this@asListGeneric.subList(fromIndex, toIndex)
}

context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongIndexedCollection<T>.add(index: Int, element: T): Unit = addBits(index, a.toLong(element))
context(a: ValueLongAdapter<T>) inline fun <T> MutableVLongIndexedCollection<T>.retainAll(elements: VLongList<T>): Boolean = removeAllIndexedBits{i,b-> !elements.containsBits(b)}
