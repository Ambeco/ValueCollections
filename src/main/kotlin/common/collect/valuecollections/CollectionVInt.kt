@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused", "RedundantNullableReturnType",
    "KotlinConstantConditions", "KotlinConstantConditions"
)

// TODO: Implement 'throw NotImplementedError' functions
// TODO: Fix .toX() vs .asX() methods.
// TODO: Reconsider adding compatibility with IntSet + IntList

package mpd.com.common.collect.valuecollections

import androidx.collection.IntList
import androidx.collection.IntSet
import kotlin.Double
import kotlin.also
import kotlin.collections.set
import kotlin.math.min
import kotlin.random.Random


//
// 
// The collections are not directly :Iterable<T> because constructing the iterator requires a `context(a: ValueIntAdapter<T>)`
// because iterators use a lot of magic and I can't add context(a: ValueIntAdapter<T>) to the getNext itself
interface CollectionVInt<T> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_VALUE: IntBits
    val size: Int
    fun anyBits(predicate: (bits:IntBits) -> Boolean): IntBits
    fun containsBits(bits: IntBits): Boolean
    
    context(a: ValueIntAdapter<T>) fun asIterable(): Iterable<T>
    
    @JvmName("toStringV") @Suppress("INAPPLICABLE_JVM_NAME")
    context(a: ValueIntAdapter<T>) fun toString(): String = toStringV()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.asCollectionGeneric(): Collection<T> = object: Collection<T> {
    override val size: Int get() = this@asCollectionGeneric.size
    override inline fun isEmpty(): Boolean = this@asCollectionGeneric.size == 0
    override inline fun contains(element: T): Boolean = this@asCollectionGeneric.contains(element)
    override inline fun iterator(): Iterator<T> = this@asCollectionGeneric.asIterable().iterator()
    override inline fun containsAll(elements: Collection<T>): Boolean = this@asCollectionGeneric.containsAll(elements)
}

context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.fromInt(bits: IntBits) = if (bits==NULL_VALUE) throw NoSuchElementException() else a.fromInt(bits)
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.fromIntOr(bits: IntBits, provider: ()->T): T = if (bits==NULL_VALUE) provider() else a.fromInt(bits)
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.fromIntOrNull(bits: IntBits): T? = if (bits==NULL_VALUE) null else a.fromInt(bits)
 inline fun <T> CollectionVInt<T>.allBits(crossinline predicate: (IntBits) -> Boolean): Boolean = anyBits{!predicate(it)} == NULL_VALUE
 inline fun <T> CollectionVInt<T>.forEachBits(crossinline action: (bits:IntBits) -> Unit) { anyBits {action(it); false } }
inline  fun <T> CollectionVInt<T>.singleBits(crossinline predicate: (bits:IntBits) -> Boolean): IntBits {
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
 inline fun <T> CollectionVInt<T>.anyIndexedBits(crossinline action: (index:Int, IntBits) -> Boolean) = anyBits(
    object: (IntBits) -> Boolean {
        var index = 0
        override inline fun invoke(v: IntBits) = action(index++, v)
    }
)
 inline fun <T> CollectionVInt<T>.allIndexedBits(crossinline action: (index:Int, IntBits) -> Boolean) = allBits(
    object: (IntBits) -> Boolean {
        var index = 0
        override inline fun invoke(v: IntBits) = action(index++, v)
    }
)
 inline fun <T> CollectionVInt<T>.forEachIndexedBits(crossinline action: (index:Int, bits:IntBits) -> Unit) = forEachBits(
    object: (IntBits) -> Unit {
        var index=0
        override inline fun invoke(bits: IntBits) = action(index++, bits)
    })

context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.any(crossinline predicate: (T) -> Boolean): Boolean = anyBits{predicate(a.fromInt(it))} != NULL_VALUE
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.all(crossinline predicate: (T) -> Boolean): Boolean = allBits {predicate(a.fromInt(it))}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.forEach(crossinline action: (T) -> Unit) = forEachBits { action(a.fromInt(it)) }
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.single(crossinline predicate: (T) -> Boolean): T = fromInt(singleBits {predicate(a.fromInt(it))})
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.contains(element: T) = containsBits(a.toInt(element))
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.forEachIndexed(crossinline action: (index:Int, T) -> Unit) = forEachIndexedBits { i, e-> action(i,a.fromInt(e)) }

 inline fun <T> CollectionVInt<T>.isEmpty() = size == 0
 inline fun <T> CollectionVInt<T>.isNotEmpty() = size > 0
 inline fun <T> CollectionVInt<T>.containsAll(bits: IntList): Boolean = bits.first { !containsBits(it) } == NULL_VALUE
 inline fun <T> CollectionVInt<T>.containsAll(bits: IntSet): Boolean = bits.first { !containsBits(it) } == NULL_VALUE
 inline fun <T> CollectionVInt<T>.containsAll(bits: CollectionVInt<T>): Boolean = bits.anyBits({ !containsBits(it) }) == NULL_VALUE
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.containsAll(other: Collection<T>): Boolean = other.all({ contains(it) })
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.single(): T = single {true}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.singleOr(provider: ()->T): T = fromIntOr(singleBits {true}, provider)
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.singleOrElse(defaultValue:T): T = singleOr {defaultValue}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.singleOrNull(): T? =  fromIntOrNull(singleBits { true })
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.singleOrElse(crossinline predicate: (T) -> Boolean, defaultValue:T): T = singleOr(predicate) {defaultValue}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.singleOr(crossinline predicate: (T) -> Boolean, provider: ()->T): T = fromIntOr(singleBits({ predicate(a.fromInt(it)) }), provider)
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.singleOrNull(crossinline predicate: (T) -> Boolean): T? = fromIntOrNull(singleBits({ predicate(a.fromInt(it)) }))
 inline fun <T> CollectionVInt<T>.findIndexedBits(crossinline predicate: (index:Int, bits:IntBits) -> Boolean): IntBits = anyBits(
    object: (IntBits) -> Boolean {
        var index=0
        override inline fun invoke(bits: IntBits) = predicate(index++, bits)
    })
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.findIndexed(crossinline predicate: (index:Int, T) -> Boolean): IntBits = findIndexedBits { i, b -> predicate(i, a.fromInt(b))}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.find(crossinline predicate: (T) -> Boolean): T? = fromIntOrNull(anyBits{predicate(a.fromInt(it))})
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.findOrElse(crossinline predicate: (T) -> Boolean, defaultValue:T): T = findOr(predicate) {defaultValue}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.findOr(crossinline predicate: (T) -> Boolean, provider: ()->T): T = fromIntOr(anyBits{predicate(a.fromInt(it))}, provider)
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.findOrThrow(crossinline predicate: (T) -> Boolean): T = fromInt(anyBits{predicate(a.fromInt(it))})
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.filter(crossinline predicate: (T) -> Boolean): ArrayListVInt<T> = filterTo(ArrayListVInt(), predicate)
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollectionVInt<T>> CollectionVInt<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.filterNot(crossinline predicate: (T) -> Boolean): ArrayListVInt<T> = filter {!predicate(it)}
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollectionVInt<T>> CollectionVInt<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> CollectionVInt<T>.associateVIntInt(crossinline transform: (T) -> PairVIntInt<K, V>): MapVIntInt<K, V> = associateTo(HashMapVIntInt(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> CollectionVInt<T>.associateVIntLong(crossinline transform: (T) -> PairVIntLong<K, V>): MapVIntLong<K, V> = associateTo(HashMapVIntLong<K,V>(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> CollectionVInt<T>.associateVLongInt(crossinline transform: (T) -> PairVLongInt<K, V>): MapVLongInt<K, V> = associateTo(HashMapVLongInt<K,V>(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> CollectionVInt<T>.associateVLongLong(crossinline transform: (T) -> PairVLongLong<K, V>): MapVLongLong<K, V> = associateTo(HashMapVLongLong<K,V>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V> CollectionVInt<T>.associateGeneric(crossinline transform: (T) -> Pair<K, V>): Map<K, V> = associateTo(HashMap(size), transform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> CollectionVInt<T>.associateVIntInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableMapVIntInt<K,V> = associateTo(HashMapVIntInt(size), keySelector,valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> CollectionVInt<T>.associateVIntLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableMapVIntLong<K,V> = associateTo(HashMapVIntLong(size), keySelector,valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> CollectionVInt<T>.associateVLongInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableMapVLongInt<K,V> = associateTo(HashMapVLongInt(size), keySelector,valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> CollectionVInt<T>.associateVLongLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableMapVLongLong<K,V> = associateTo(HashMapVLongLong(size), keySelector,valueTransform)
context(a: ValueIntAdapter<T>) inline fun <T, K, V> CollectionVInt<T>.associateGeneric(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): Map<K, V> = associateTo(HashMap(size+size/4), keySelector, valueTransform)
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableMapVIntInt<K,V>> CollectionVInt<T>.associateTo(destination: C, crossinline transform: (T) -> PairVIntInt<K, V>): C = destination.also{ c-> c.putAll(this, transform) }
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableMapVIntLong<K,V>> CollectionVInt<T>.associateTo(destination: C, crossinline transform: (T) -> PairVIntLong<K, V>): C = destination.also{ c-> c.putAll(this, transform) }
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableMapVLongInt<K,V>> CollectionVInt<T>.associateTo(destination: C, crossinline transform: (T) -> PairVLongInt<K, V>): C = destination.also{ c-> c.putAll(this, transform) }
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableMapVLongLong<K,V>> CollectionVInt<T>.associateTo(destination: C, crossinline transform: (T) -> PairVLongLong<K, V>): C = destination.also{ c-> c.putAll(this, transform) }
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>> CollectionVInt<T>.associateTo(destination: M, crossinline transform: (T) -> Pair<K, V>): M = destination.also{ c->forEach {val v=transform(it); c[v.first] = v.second}}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableMapVIntInt<K,V>> CollectionVInt<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{ c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableMapVIntLong<K,V>> CollectionVInt<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{ c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableMapVLongInt<K,V>> CollectionVInt<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{ c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableMapVLongLong<K,V>> CollectionVInt<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{ c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueIntAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>> CollectionVInt<T>.associateTo(destination: M, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): M = destination.also{ c->forEach {c.put(keySelector(it),valueTransform(it))}}
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>,) inline fun <T, K> CollectionVInt<T>.associateByVIntInt(crossinline keySelector: (T) -> K): MutableMapVIntInt<K,T> = associateTo(HashMapVIntInt<K,T>(size), keySelector, {it})
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>) inline fun <T, K> CollectionVInt<T>.associateByVLongInt(crossinline keySelector: (T) -> K): MutableMapVLongInt<K,T> = associateTo(HashMapVLongInt(size), keySelector,{it})
context(a: ValueIntAdapter<T>) inline fun <T, K> CollectionVInt<T>.associateByGeneric(crossinline keySelector: (T) -> K): Map<K, T> = associateTo(HashMap(size+size/4), keySelector, {it})
context(a: ValueIntAdapter<T>, ka: ValueIntAdapter<K>) inline fun <T, K, C:MutableMapVIntInt<K,T>> CollectionVInt<T>.associateByVIntIntTo(destination: C, crossinline keySelector: (T) -> K): MutableMapVIntInt<K,T> = associateTo(destination, keySelector, {it})
context(a: ValueIntAdapter<T>, ka: ValueLongAdapter<K>) inline fun <T, K, C:MutableMapVLongInt<K,T>> CollectionVInt<T>.associateByVLongIntTo(destination: C, crossinline keySelector: (T) -> K): MutableMapVLongInt<K,T> = associateTo(destination, keySelector,{it})
context(a: ValueIntAdapter<T>) inline fun <T, K, C:MutableMap<K,T>> CollectionVInt<T>.associateByGenericTo(destination: C, crossinline keySelector: (T) -> K): Map<K, T> = associateTo(destination, keySelector, {it})
 inline fun <T, C : MutableCollectionVInt<T>> CollectionVInt<T>.toCollection(destination: C): C = destination.also{ c -> c.addAll(this) }
context(a: ValueIntAdapter<T>) inline fun <T, C : MutableCollection<T>> CollectionVInt<T>.toCollection(destination: C): C = destination.also{ c->forEach {c.add(it)}}
 inline fun <T> CollectionVInt<T>.toList(): ListVInt<T> = this as? ListVInt<T> ?: toMutableList()
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.toListGeneric(): List<T> = toMutableListGeneric()
 inline fun <T> CollectionVInt<T>.toMutableList(): ArrayListVInt<T> = this as? ArrayListVInt<T> ?: toCollection(ArrayListVInt<T>(size, NULL_VALUE))
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.toMutableListGeneric(): MutableList<T> = toCollection(ArrayList(size))
 inline fun <T> CollectionVInt<T>.toSet(): SetVInt<T> = this as? SetVInt<T> ?: toMutableSet()
 inline fun <T> CollectionVInt<T>.toMutableSet(): MutableSetVInt<T> = this as? MutableSetVInt<T> ?: toCollection(ArraySetVInt<T>(size))
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.toSetGeneric(): Set<T> = toHashSet()
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.toHashSet(): HashSet<T> = toCollection(HashSet(size + size/4))
 inline fun <T> CollectionVInt<T>.toIntArray(): IntArray = (this as? ArrayVInt<T>)?.collection ?: IntArray(size).also { c->forEachIndexedBits{ i, e-> c[i]=e}}
 inline fun <T> CollectionVInt<T>.toVIntArray(): ArrayVInt<T> = this as? ArrayVInt<T> ?: ArrayVInt(this)
 inline fun <T> CollectionVInt<T>.toArrayGenericBits(): Array<IntBits> = (this as? ArrayVInt<T>)?.collection?.toTypedArray() ?: Array(size,{NULL_VALUE}).also { c->forEachIndexedBits{ i, e-> c[i]=e}}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.asSequence(): Sequence<T> = asIterable().asSequence()
 inline fun <T> CollectionVInt<T>.asList(): ListVInt<T> = this as? ListVInt<T> ?: toMutableList()
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.asListGeneric(): List<T> = toMutableListGeneric()
 inline fun <T> CollectionVInt<T>.contentEquals(other: CollectionVInt<T>?): Boolean = other != null && size == other.size && allBits { other.containsBits(it) }
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.contentEquals(other: Collection<T>?): Boolean = other != null && size == other.size && all { other.contains(it) }
inline fun <T> CollectionVInt<T>.contentHashCode(): Int { var h = 1; forEachBits { h = 31 * h + it }; return h }
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.flatMap(crossinline transform: (T) ->CollectionVInt<R>): ArrayListVInt<R> = flatMapTo(ArrayListVInt(size*2), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.flatMap(crossinline transform: (T) ->CollectionVLong<R>): ArrayListVLong<R> = flatMapTo(ArrayListVLong(size*2), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollectionVInt<R>> CollectionVInt<T>.flatMapTo(destination: C, crossinline transform: (T) ->CollectionVInt<R>): C = destination.also{forEach { destination.addAll(transform(it)) }}
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollectionVLong<R>> CollectionVInt<T>.flatMapTo(destination: C, crossinline transform: (T) ->CollectionVLong<R>): C = destination.also{forEach { destination.addAll(transform(it)) }}
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.flatMapIndexed(crossinline transform: (Int, T) ->CollectionVInt<R>): ArrayListVInt<R> = flatMapIndexedTo(ArrayListVInt(size*2), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.flatMapIndexed(crossinline transform: (Int, T) ->CollectionVLong<R>): ArrayListVLong<R> = flatMapIndexedTo(ArrayListVLong(size*2), transform)
context(a: ValueIntAdapter<T>) inline  fun <T, R, C : MutableCollectionVInt<R>> CollectionVInt<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int, T) ->CollectionVInt<R>): C = destination.also{forEachIndexed { i, e-> destination.addAll(transform(i,e)) }}
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollectionVLong<R>> CollectionVInt<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int, T) ->CollectionVLong<R>): C = destination.also{forEachIndexed { i, e-> destination.addAll(transform(i,e)) }}
context(a: ValueIntAdapter<T>) inline fun <T, K> CollectionVInt<T>.groupBy(crossinline keySelector: (T) -> K): MutableMap<K, MutableListVInt<T>> = groupByTo(HashMap<K,MutableListVInt<T>>(), keySelector)
context(a: ValueIntAdapter<T>) inline fun <T, K, M : MutableMap<K, MutableListVInt<T>>> CollectionVInt<T>.groupByTo(destination: M, crossinline keySelector: (T) -> K): M = destination.also{ c-> forEach { c.getOrPut(keySelector(it),{ ArrayListVInt(size) }).add(it) }}
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> CollectionVInt<T>.mapVInt(crossinline transform: (T) -> R): ArrayListVInt<R> = mapTo(ArrayListVInt<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> CollectionVInt<T>.mapVLong(crossinline transform: (T) -> R): ArrayListVLong<R> = mapTo(ArrayListVLong<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.mapGeneric(crossinline transform: (T) -> R): MutableList<R> = mapTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> CollectionVInt<T>.mapIndexedVInt(crossinline transform: (index: Int, T) -> R): ArrayListVInt<R> = mapIndexedTo(ArrayListVInt<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> CollectionVInt<T>.mapIndexedVLong(crossinline transform: (index: Int, T) -> R): ArrayListVLong<R> = mapIndexedTo(ArrayListVLong<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.mapIndexedGeneric(crossinline transform: (index: Int, T) -> R): List<R> = mapIndexedTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> CollectionVInt<T>.mapIndexedVIntNotNull(crossinline transform: (index: Int, T) -> R?): ArrayListVInt<R> = mapIndexedNotNullTo(ArrayListVInt<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> CollectionVInt<T>.mapIndexedVLongNotNull(crossinline transform: (index: Int, T) -> R?): ArrayListVLong<R> = mapIndexedNotNullTo(ArrayListVLong<R>(size), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.mapIndexedGenericNotNull(crossinline transform: (index: Int, T) -> R?): List<R> = mapIndexedNotNullTo(ArrayList<R>(size), transform)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableCollectionVInt<R>> CollectionVInt<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{ c->forEachIndexed{ i, e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableCollectionVLong<R>> CollectionVInt<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{ c->forEachIndexed{ i, e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> CollectionVInt<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{ c->forEachIndexed{ i, e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableCollectionVInt<R>> CollectionVInt<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{ i, e-> destination.add(transform(i,e)) } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableCollectionVLong<R>> CollectionVInt<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{ i, e-> destination.add(transform(i,e)) } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> CollectionVInt<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{ i, e-> destination.add(transform(i,e)) } }
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.mapNotNull(crossinline transform: (T) -> R?): List<R> = mapNotNullTo(mutableListOf(), transform)
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> CollectionVInt<T>.mapNotNullTo(destination: C, crossinline transform: (T) -> R?): C = destination.also {forEach{transform(it)?.also {destination.add(it) } }}
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableCollectionVInt<R>> CollectionVInt<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableCollectionVLong<R>> CollectionVInt<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>) inline fun <T, R, C : MutableCollection<R>> CollectionVInt<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.withIndex():MutableListVLong<IndexedVInt<T>> = with(IndexedVInt.VLongAdapter<T>()) {mapIndexedVLong{ i, e-> IndexedVInt.of(i,e)}}
 inline fun <T> CollectionVInt<T>.distinct(): SetVInt<T> = ArraySetVInt<T>(size).also{ c-> forEachBits {c.addBits(it)}}
context(a: ValueIntAdapter<T>) inline fun <T, K> CollectionVInt<T>.distinctBy(crossinline selector: (T) -> K): SetVInt<T> {
    val distinct = HashSet<K>()
    val result = ArraySetVInt<T>(size)
    forEach {
        val k = selector(it)
        if (!distinct.contains(k)) {
            distinct.add(k)
            result.add(it)
        }
    }
    return result
}
 inline infix fun <T> CollectionVInt<T>.intersect(other:CollectionVInt<T>): SetVInt<T> = ArraySetVInt<T>(size).also { c-> forEachBits{ if (other.containsBits(it)) c.addBits(it)}}
 inline infix fun <T> CollectionVInt<T>.subtract(other:CollectionVInt<T>): SetVInt<T> = ArraySetVInt<T>(size).also { c-> forEachBits{ if (!other.containsBits(it)) c.addBits(it)}}
 inline infix fun <T> CollectionVInt<T>.union(other:CollectionVInt<T>): SetVInt<T> = toMutableSet().also{ c-> c.addAll(other)}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.any(): Boolean = size > 0
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.count(): Int = size
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.count(crossinline predicate: (T) -> Boolean): Int = fold(0,{ acc, e->if(predicate(e)) acc+1 else acc})
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.fold(initial: R, crossinline operation: (acc: R, T) -> R): R = foldIndexed(initial,{ _, acc, e->operation(acc,e)})
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.foldIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): R {
    val accumulator = object: (Int,T)->Unit {
        var index=0
        var acc = initial
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e) }
    }
    forEachIndexed(accumulator)
    return accumulator.acc
}
inline fun <T, R> CollectionVInt<T>.foldBits(initial: R, crossinline operation: (acc: R, IntBits) -> R): R = foldIndexedBits(initial,{ _, acc, e->operation(acc,e)})
inline fun <T, R> CollectionVInt<T>.foldIndexedBits(initial: R, crossinline operation: (index: Int, acc: R, IntBits) -> R): R {
    val accumulator = object: (Int,IntBits)->Unit {
        var index=0
        var acc = initial
        override inline fun invoke(i:Int, e: IntBits) { acc = operation(i, acc, e) }
    }
    forEachIndexedBits(accumulator)
    return accumulator.acc
}
context(a: ValueIntAdapter<T>) inline fun <T, C: CollectionVInt<T>> C.onEach(crossinline action: (T) -> Unit): C = apply{forEach(action)}
context(a: ValueIntAdapter<T>) inline fun <T, C: CollectionVInt<T>> C.onEachIndexed(crossinline action: (Int, T) -> Unit): C = apply{forEachIndexed(action)}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> CollectionVInt<T>.max(): T = reduce { l, r -> if(r>l) r else l}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> CollectionVInt<T>.maxOrNull(): T? = if(isEmpty()) null else max()
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.maxWith(comparator: Comparator<in T>): T = reduce { l, r -> if(comparator.compare(r,l)>0) r else l}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.maxWithOrNull(comparator: Comparator<in T>): T? = if(isEmpty()) null else maxWith(comparator)
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVInt<T>.maxBy(crossinline selector: (T) -> R): T {
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
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVInt<T>.maxByOrNull(crossinline selector: (T) -> R): T? = if(isEmpty()) null else maxBy(selector)
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVInt<T>.maxOf(crossinline selector: (T) -> R): R = mapReduce(selector) { max, e-> if (e>max) e else max}
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVInt<T>.maxOfOrNull(crossinline selector: (T) -> R): R? = if(isEmpty()) null else maxOf(selector)
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.maxOfWith(comparator: Comparator<in R>, crossinline selector: (T) -> R): R = mapReduce(selector) { max, e-> if (comparator.compare(e,max)>0) e else max}
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.maxOfWithOrNull(comparator: Comparator<in R>, crossinline selector: (T) -> R): R? = if(isEmpty()) null else maxOfWith(comparator, selector)
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> CollectionVInt<T>.min(): T = reduce { l, r -> if(r<l) r else l}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> CollectionVInt<T>.minOrNull(): T? = if(isEmpty()) null else min()
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.minWith(comparator: Comparator<in T>): T = reduce { l, r -> if(comparator.compare(r,l)<0) r else l}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.minWithOrNull(comparator: Comparator<in T>): T? = if(isEmpty()) null else minWith(comparator)
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVInt<T>.minBy(crossinline selector: (T) -> R): T {
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
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVInt<T>.minByOrNull(crossinline selector: (T) -> R): T? = if(isEmpty()) null else minBy(selector)
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVInt<T>.minOf(crossinline selector: (T) -> R): R = mapReduce(selector) { min, e-> if (e<min) e else min}
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVInt<T>.minOfOrNull(crossinline selector: (T) -> R): R? = if(isEmpty()) null else minOf(selector)
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.minOfWith(comparator: Comparator<in R>, crossinline selector: (T) -> R): R = mapReduce(selector) { min, e-> if (comparator.compare(e,min)<0) e else min}
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.minOfWithOrNull(comparator: Comparator<in R>, crossinline selector: (T) -> R): R? = if(isEmpty()) null else minOfWith(comparator, selector)
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.none(): Boolean = size==0
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.none(crossinline predicate: (T) -> Boolean): Boolean = !any(predicate)
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> CollectionVInt<T>.sorted(): ArrayListVInt<T> = ArrayListVInt<T>(this).also{it.sort()}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> CollectionVInt<T>.sortedArray(): ArrayVInt<T> = toVIntArray().also{it.sort()}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> CollectionVInt<T>.sortedArrayDescending(): ArrayVInt<T> = toVIntArray().also{it.sortDescending()}
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVInt<T>.sortedBy(crossinline selector: (T) -> R?): ArrayListVInt<T> =  toMutableList().also{it.sortBy(selector)}
context(a: ValueIntAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVInt<T>.sortedByDescending(crossinline selector: (T) -> R?): ArrayListVInt<T> = toMutableList().also{it.sortByDescending(selector)}
context(a: ValueIntAdapter<T>) inline fun <T : Comparable<T>> CollectionVInt<T>.sortedDescending(): ArrayListVInt<T> = toMutableList().also{it.sortDescending()}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.sortedWith(comparator: Comparator<in T>): ArrayListVInt<T> = toMutableList().also{it.sortWith(comparator)}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.sumBy(crossinline selector: (T) -> Int): Int = mapReduce(selector) { acc, e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.sumByDouble(crossinline selector: (T) -> Double): Double = mapReduce(selector) { acc, e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.sumOf(crossinline selector: (T) -> Double): Double = mapReduce(selector) { acc, e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.sumOf(crossinline selector: (T) -> Int): Int = mapReduce(selector) { acc, e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.sumOf(crossinline selector: (T) -> Long): Long = mapReduce(selector) { acc, e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.sumOfUInt(crossinline selector: (T) -> UInt): UInt = mapReduce(selector) { acc, e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.sumOfULong(crossinline selector: (T) -> ULong): ULong = mapReduce(selector) { acc, e->acc+e}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.chunked(size: Int): List<ArrayListVInt<T>>{
    val results = List<ArrayListVInt<T>>((this.size + size - 1)/size) {ArrayListVInt(size)}
    val acc = object: (Int,T)->Unit {
        var listIdx=0
        var rollover=size
        override inline fun invoke(index:Int, e: T) {
            if (index==rollover) {
                listIdx++
                rollover+=size
            }
            results[listIdx].add(e)
        }
    }
    forEachIndexed(acc)
    return results
}
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.chunked(size: Int, crossinline transform: (ListVInt<T>) -> R): List<R> = chunked(size).map(transform)
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.plusElement(element: T): ListVInt<T> = plus(element)
context(a: ValueIntAdapter<T>) inline operator fun <T> CollectionVInt<T>.plus(element: T): ListVInt<T> = ArrayListVInt<T>(size+1, NULL_VALUE).also {it.addAll(this); it.add(element) }
 inline operator fun <T> CollectionVInt<T>.plus(elements: CollectionVInt<T>): ListVInt<T> = ArrayListVInt<T>(size+elements.size, NULL_VALUE).also {it.addAll(this); it.addAll(elements) }
context(a: ValueIntAdapter<T>) inline operator fun <T> CollectionVInt<T>.plus(elements: Iterable<T>): ListVInt<T> = ArrayListVInt<T>(size+1, NULL_VALUE).also {it.addAll(this); it.addAll(elements) }
context(a: ValueIntAdapter<T>) inline operator fun <T> CollectionVInt<T>.plus(elements: Array<out T>): ListVInt<T> = ArrayListVInt<T>(size+1, NULL_VALUE).also {it.addAll(this); it.addAll(elements) }
context(a: ValueIntAdapter<T>) inline operator fun <T> CollectionVInt<T>.minus(element: T): ListVInt<T> = ArrayListVInt<T>(size, NULL_VALUE).also { c-> forEach { if (it != element) c.add(it) } }
context(a: ValueIntAdapter<T>) inline operator fun <T> CollectionVInt<T>.minus(elements: Array<out T>): ListVInt<T> = ArrayListVInt<T>(size, NULL_VALUE).also { c-> forEach { if (!elements.contains(it)) c.add(it) } }
context(a: ValueIntAdapter<T>) inline operator fun <T> CollectionVInt<T>.minus(elements:CollectionVInt<T>): ListVInt<T> = ArrayListVInt<T>(size, NULL_VALUE).also { c-> forEach { if (!elements.contains(it)) c.add(it) } }
context(a: ValueIntAdapter<T>) inline operator fun <T> CollectionVInt<T>.minus(elements:Iterable<T>): ListVInt<T> = ArrayListVInt<T>(size, NULL_VALUE).also { c-> forEach { if (!elements.contains(it)) c.add(it) } }
context(a: ValueIntAdapter<T>) inline operator fun <T> CollectionVInt<T>.minus(elements: Sequence<T>): ListVInt<T> = ArrayListVInt<T>(size, NULL_VALUE).also { c-> forEach { if (!elements.contains(it)) c.add(it) } }
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.minusElement(element: T): ListVInt<T> = minus(element)
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.partition(crossinline predicate: (T) -> Boolean): Pair<ListVInt<T>, ListVInt<T>> {
    val trueList = ArrayListVInt<T>(size, NULL_VALUE)
    val falseList = ArrayListVInt<T>(size, NULL_VALUE)
    forEach { if (predicate(it)) trueList.add(it) else falseList.add(it) }
    return trueList to falseList
}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.random(): T = random(Random.Default)
context(a: ValueIntAdapter<T>) fun <T> CollectionVInt<T>.random(random: Random): T {
    if (size==0) throw NoSuchElementException()
    val findIdx = random.nextInt(size)
    return fromInt(findIndexedBits {i,e->i==findIdx})
}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.randomOrNull(): T? = randomOrNull(Random.Default)
context(a: ValueIntAdapter<T>) fun <T> CollectionVInt<T>.randomOrNull(random: Random): T? = if (size==0) null else random(random)
context(a: ValueIntAdapter<T>) inline infix fun <T, R> CollectionVInt<T>.zip(other: Array<out R>): MutableList<Pair<T, R>> = zip(other, { a, b->a to b})
context(a: ValueIntAdapter<T>) inline fun <T, R, V> CollectionVInt<T>.zip(other: Array<out R>, crossinline transform: (a: T, b: R) -> V): MutableList<V> {
    val r = mutableListOf<V>()
    forEachIndexed { i, e -> if (i < other.size) r.add(i, transform(e, other[i])) }
    return r
}
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline infix fun <T, R> CollectionVInt<T>.zip(other:IndexedCollectionVInt<R>): MutableListVLong<PairVIntInt<T, R>> 
    = with<ValueLongAdapter<PairVIntInt<T, R>>, MutableListVLong<PairVIntInt<T, R>>>(PairVIntInt.VLongAdapter()) {zipPairVIntInt<T,R, PairVIntInt<T,R>>(other, { a, b->PairVIntInt.of(a,b)})}
context(ta: ValueIntAdapter<T>, ra: ValueIntAdapter<R>, va: ValueLongAdapter<V>) inline fun <T, R, V> CollectionVInt<T>.zipPairVIntInt(other:IndexedCollectionVInt<R>, crossinline transform: (a: T, b: R) -> V): MutableListVLong<V> {
    val r = ArrayListVLong<V>(min(size, other.size))
    forEachIndexed { i, e -> if (i < other.size) r.add(i, transform(e, other.get(i))) }
    return r
}
context(a: ValueIntAdapter<T>) inline fun <T, A : Appendable> CollectionVInt<T>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence) = { it.toString() }): A {
    val appender = object: (Int,T)-> Boolean {
        var count=0
        override inline fun invoke(index: Int, e: T): Boolean {
            if (limit<0 || count < limit) {
                count++
                if (count != 1) buffer.append(separator)
                buffer.append(transform(e))
                if (limit<0 || count < limit)
                    return false
            }
            if (limit>=0 && count >= limit)
                buffer.append(truncated)
            return true
        }
    }
    buffer.append(prefix)
    findIndexed(appender)
    buffer.append(postfix)
    return buffer
}
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence) = { it.toString() }): String 
    = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(a: ValueIntAdapter<T>) inline fun <T> CollectionVInt<T>.toStringV() = joinToString(", ","{","}")
context(a: ValueIntAdapter<T>) inline fun <S, R : S, T> CollectionVInt<T>.mapReduce(crossinline map:(T)->S, crossinline operation: (acc: S, S) -> S): S = mapReduceIndexed(map){ i, acc, e->operation(acc,e)}
context(a: ValueIntAdapter<T>) inline fun <S, R : S, T> CollectionVInt<T>.mapReduceIndexed(crossinline map:(T)->R, crossinline operation: (index:Int, acc: S, next:R) -> S): S {
    val accumulator = object: (Int,T)->Unit {
        var acc: S = map(findOrThrow{true})
        override inline fun invoke(i:Int, e: T) { if (i>0) acc = operation(i, acc, map(e)) }
    }
    forEachIndexed(accumulator)
    return accumulator.acc
}
context(a: ValueIntAdapter<T>) inline fun <S, T : S> CollectionVInt<T>.reduce(crossinline operation: (acc: S, T) -> S): S = reduceIndexed<S,T>{ i, acc, e -> operation(acc,e) }
context(a: ValueIntAdapter<T>) inline fun <S, T : S> CollectionVInt<T>.reduceIndexed(crossinline operation: (index: Int, acc: S, T) -> S): S = mapReduceIndexed({it}, operation)
context(a: ValueIntAdapter<T>) inline fun <S, T : S> CollectionVInt<T>.reduceIndexedOrNull(crossinline operation: (index: Int, acc: S, T) -> S): S? = if (size==0) return null else reduceIndexed(operation)
context(a: ValueIntAdapter<T>) inline fun <S, T : S> CollectionVInt<T>.reduceOrNull(crossinline operation: (acc: S, T) -> S): S? = if (size==0) return null else reduce(operation)
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> CollectionVInt<T>.runningFoldVInt(initial: R, crossinline operation: (acc: R, T) -> R): MutableListVInt<R> = runningFoldVIntIndexed(initial) { i, acc, e->operation(acc,e)}
context(ta: ValueIntAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> CollectionVInt<T>.runningFoldVIntIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): MutableListVInt<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = ArrayListVInt<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> CollectionVInt<T>.runningFoldVLong(initial: R, crossinline operation: (acc: R, T) -> R): MutableListVLong<R> = runningFoldVLongIndexed(initial) { i, acc, e->operation(acc,e)}
context(ta: ValueIntAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> CollectionVInt<T>.runningFoldVLongIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): MutableListVLong<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = ArrayListVLong<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.runningFoldGeneric(initial: R, crossinline operation: (acc: R, T) -> R): List<R> = runningFoldGenericIndexed(initial) { i, acc, e->operation(acc,e)}
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.runningFoldGenericIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): List<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = mutableListOf<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<S>) inline fun <S, T : S> CollectionVInt<T>.runningReduceVInt(crossinline operation: (acc: S, T) -> S): MutableListVInt<S> = with(ra){runningReduceVIntIndexed { i, acc, e -> operation(acc,e)}}
context(a: ValueIntAdapter<T>, ra: ValueIntAdapter<S>) inline fun <S, T : S> CollectionVInt<T>.runningReduceVIntIndexed(crossinline operation: (index: Int, acc: S, T) -> S): MutableListVInt<S>  {
    if (size<=1) return ArrayListVInt()
    val accumulator = object: (Int,T)->Unit {
        var acc: S = findOrThrow{true}
        var result = ArrayListVInt<S>(size-1)
        override inline fun invoke(i:Int, e: T) {
            if (i ==0) return
            acc = operation(i-1, acc, e)
            result.add(acc)
        }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<S>) inline fun <S, T : S> CollectionVInt<T>.runningReduceVLong(crossinline operation: (acc: S, T) -> S): MutableListVLong<S> = runningReduceVLongIndexed { i, acc, e -> operation(acc,e)}
context(a: ValueIntAdapter<T>, ra: ValueLongAdapter<S>) inline fun <S, T : S> CollectionVInt<T>.runningReduceVLongIndexed(crossinline operation: (index: Int, acc: S, T) -> S): MutableListVLong<S> {
    if (size<=1) return ArrayListVLong()
    val accumulator = object: (Int,T)->Unit {
        var acc: S = findOrThrow{true}
        var result = ArrayListVLong<S>(size-1)
        override inline fun invoke(i:Int, e: T) { 
            if (i ==0) return
            acc = operation(i-1, acc, e)
            result.add(acc)
        }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueIntAdapter<T>) inline fun <S, T : S> CollectionVInt<T>.runningReduceGeneric(crossinline operation: (acc: S, T) -> S): List<S> = runningReduceGenericIndexed<S,T> { i, acc, e -> operation(acc,e)}
context(a: ValueIntAdapter<T>) inline fun <S, T : S> CollectionVInt<T>.runningReduceGenericIndexed(crossinline operation: (index: Int, acc: S, T) -> S): List<S> {
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
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.scan(initial: R, crossinline operation: (acc: R, T) -> R): List<R> = runningFoldGeneric(initial, operation)
context(a: ValueIntAdapter<T>) inline fun <T, R> CollectionVInt<T>.scanIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): List<R> = runningFoldGenericIndexed(initial, operation)

