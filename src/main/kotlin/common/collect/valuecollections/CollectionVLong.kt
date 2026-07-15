@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE", "unused", "RedundantNullableReturnType",
    "KotlinConstantConditions", "KotlinConstantConditions"
)

// TODO: Implement 'throw NotImplementedError' functions

package mpd.com.common.collect.valuecollections

import androidx.collection.LongList
import androidx.collection.LongSet
import kotlin.Double
import kotlin.also
import kotlin.collections.set
import kotlin.math.min
import kotlin.random.Random


interface CollectionVLong<T> {
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
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.asCollectionGeneric(): Collection<T> = object: Collection<T> {
    override val size: Int get() = this@asCollectionGeneric.size
    override inline fun isEmpty(): Boolean = this@asCollectionGeneric.size == 0
    override inline fun contains(element: T): Boolean = this@asCollectionGeneric.contains(element)
    override inline fun iterator(): Iterator<T> = this@asCollectionGeneric.asIterable().iterator()
    override inline fun containsAll(elements: Collection<T>): Boolean = this@asCollectionGeneric.containsAll(elements)
}

context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.fromLong(bits: LongBits) = if (bits==NULL_VALUE) throw NoSuchElementException() else a.fromLong(bits)
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.fromLongOr(bits: LongBits, provider: ()->T): T = if (bits==NULL_VALUE) provider() else a.fromLong(bits)
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.fromLongOrNull(bits: LongBits): T? = if (bits==NULL_VALUE) null else a.fromLong(bits)
 inline fun <T> CollectionVLong<T>.allBits(crossinline predicate: (LongBits) -> Boolean): Boolean = anyBits{!predicate(it)} == NULL_VALUE
 inline fun <T> CollectionVLong<T>.forEachBits(crossinline action: (bits:LongBits) -> Unit) { anyBits {action(it); false } }
 inline fun <T> CollectionVLong<T>.singleBits(crossinline predicate: (bits:LongBits) -> Boolean): LongBits {
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
 inline fun <T> CollectionVLong<T>.anyIndexedBits(crossinline action: (index:Int, LongBits) -> Boolean) = anyBits(
    object: (LongBits) -> Boolean {
        var index = 0
        override inline fun invoke(v: LongBits) = action(index++, v)
    }
)
 inline fun <T> CollectionVLong<T>.allIndexedBits(crossinline action: (index:Int, LongBits) -> Boolean) = allBits(
    object: (LongBits) -> Boolean {
        var index = 0
        override inline fun invoke(v: LongBits) = action(index++, v)
    }
)
 inline fun <T> CollectionVLong<T>.forEachIndexedBits(crossinline action: (index:Int, bits:LongBits) -> Unit) = forEachBits(
    object: (LongBits) -> Unit {
        var index=0
        override inline fun invoke(bits: LongBits) = action(index++, bits)
    })

context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.any(crossinline predicate: (T) -> Boolean): Boolean = anyBits{predicate(a.fromLong(it))} != NULL_VALUE
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.all(crossinline predicate: (T) -> Boolean): Boolean = allBits {predicate(a.fromLong(it))}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.forEach(crossinline action: (T) -> Unit) = forEachBits { action(a.fromLong(it)) }
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.single(crossinline predicate: (T) -> Boolean): T = fromLong(singleBits {predicate(a.fromLong(it))})
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.contains(element: T) = containsBits(a.toLong(element))
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.forEachIndexed(crossinline action: (index:Int, T) -> Unit) = forEachIndexedBits { i, e-> action(i,a.fromLong(e)) }

 inline fun <T> CollectionVLong<T>.isEmpty() = size == 0
 inline fun <T> CollectionVLong<T>.isNotEmpty() = size > 0
 inline fun <T> CollectionVLong<T>.containsAll(bits: LongList): Boolean = bits.first { !containsBits(it) } == NULL_VALUE
 inline fun <T> CollectionVLong<T>.containsAll(bits: LongSet): Boolean = bits.first { !containsBits(it) } == NULL_VALUE
 inline fun <T> CollectionVLong<T>.containsAll(bits: CollectionVLong<T>): Boolean = bits.anyBits({ !containsBits(it) }) == NULL_VALUE
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.containsAll(other: Collection<T>): Boolean = other.any({ !contains(it) })
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.single(): T = single {true}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.singleOr(provider: ()->T): T = fromLongOr(singleBits {true}, provider)
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.singleOrElse(defaultValue:T): T = singleOr {defaultValue}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.singleOrNull(): T? =  fromLongOrNull(singleBits { true })
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.singleOrElse(crossinline predicate: (T) -> Boolean, defaultValue:T): T = singleOr(predicate) {defaultValue}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.singleOr(crossinline predicate: (T) -> Boolean, provider: ()->T): T = fromLongOr(singleBits({ predicate(a.fromLong(it)) }), provider)
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.singleOrNull(crossinline predicate: (T) -> Boolean): T? = fromLongOrNull(singleBits({ predicate(a.fromLong(it)) }))
 inline fun <T> CollectionVLong<T>.findIndexedBits(crossinline predicate: (index:Int, bits:LongBits) -> Boolean): LongBits = anyBits(
    object: (LongBits) -> Boolean {
        var index=0
        override inline fun invoke(bits: LongBits) = predicate(index++, bits)
    })
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.findIndexed(crossinline predicate: (index:Int, T) -> Boolean): LongBits = findIndexedBits { i, b -> predicate(i, a.fromLong(b))}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.find(crossinline predicate: (T) -> Boolean): T? = fromLongOrNull(anyBits{predicate(a.fromLong(it))})
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.findOrElse(crossinline predicate: (T) -> Boolean, defaultValue:T): T = findOr(predicate) {defaultValue}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.findOr(crossinline predicate: (T) -> Boolean, provider: ()->T): T = fromLongOr(anyBits{predicate(a.fromLong(it))}, provider)
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.findOrThrow(crossinline predicate: (T) -> Boolean): T = fromLong(anyBits{predicate(a.fromLong(it))})
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.filter(crossinline predicate: (T) -> Boolean): ArrayVLongList<T> = filterTo(ArrayVLongList(), predicate)
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollectionVLong<T>> CollectionVLong<T>.filterTo(destination: C, crossinline predicate: (T) -> Boolean): C = destination.also { forEach { if (predicate(it)) destination.add(it) } }
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.filterNot(crossinline predicate: (T) -> Boolean): ArrayVLongList<T> = filter {!predicate(it)}
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollectionVLong<T>> CollectionVLong<T>.filterNotTo(destination: C, crossinline predicate: (T) -> Boolean): C = filterTo(destination) {!predicate(it)}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> CollectionVLong<T>.associateVIntInt(crossinline transform: (T) -> VIntIntPair<K, V>): MapVIntInt<K, V> = associateTo(MutableMapVIntInt(size), transform)
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> CollectionVLong<T>.associateVIntLong(crossinline transform: (T) -> VIntLongPair<K, V>): MapVIntLong<K, V> = associateTo(MutableMapVIntLong(size), transform)
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> CollectionVLong<T>.associateVLongInt(crossinline transform: (T) -> VLongIntPair<K, V>): MapVLongInt<K, V> = associateTo(MutableMapVLongInt(size), transform)
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> CollectionVLong<T>.associateVLongLong(crossinline transform: (T) -> VLongLongPair<K, V>): MapVLongLong<K, V> = associateTo(MutableMapVLongLong(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, K, V> CollectionVLong<T>.associateGeneric(crossinline transform: (T) -> Pair<K, V>): Map<K, V> = associateTo(HashMap(size), transform)
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> CollectionVLong<T>.associateVIntInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableMapVIntInt<K,V> = associateTo(MutableMapVIntInt(size), keySelector,valueTransform)
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> CollectionVLong<T>.associateVIntLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableMapVIntLong<K,V> = associateTo(MutableMapVIntLong(size), keySelector,valueTransform)
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V> CollectionVLong<T>.associateVLongInt(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableMapVLongInt<K,V> = associateTo(MutableMapVLongInt(size), keySelector,valueTransform)
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V> CollectionVLong<T>.associateVLongLong(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): MutableMapVLongLong<K,V> = associateTo(MutableMapVLongLong(size), keySelector,valueTransform)
context(a: ValueLongAdapter<T>) inline fun <T, K, V> CollectionVLong<T>.associateGeneric(crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): Map<K, V> = associateTo(HashMap(size+size/4), keySelector, valueTransform)
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableMapVIntInt<K,V>> CollectionVLong<T>.associateTo(destination: C, crossinline transform: (T) -> VIntIntPair<K, V>): C = destination.also{ c->forEach {c.putAll(this, transform)}}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableMapVIntLong<K,V>> CollectionVLong<T>.associateTo(destination: C, crossinline transform: (T) -> VIntLongPair<K, V>): C = destination.also{ c->forEach {c.putAll(this, transform)}}
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableMapVLongInt<K,V>> CollectionVLong<T>.associateTo(destination: C, crossinline transform: (T) -> VLongIntPair<K, V>): C = destination.also{ c->forEach {c.putAll(this, transform)}}
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableMapVLongLong<K,V>> CollectionVLong<T>.associateTo(destination: C, crossinline transform: (T) -> VLongLongPair<K, V>): C = destination.also{ c->forEach {c.putAll(this, transform)}}
context(a: ValueLongAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>> CollectionVLong<T>.associateTo(destination: M, crossinline transform: (T) -> Pair<K, V>): M = destination.also{ c->forEach {val v=transform(it); c[v.first] = v.second}}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableMapVIntInt<K,V>> CollectionVLong<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{ c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableMapVIntLong<K,V>> CollectionVLong<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{ c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <T, K, V, C:MutableMapVLongInt<K,V>> CollectionVLong<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{ c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>) inline fun <T, K, V, C:MutableMapVLongLong<K,V>> CollectionVLong<T>.associateTo(destination: C, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): C = destination.also{ c->c.putAll(this, keySelector,valueTransform)}
context(a: ValueLongAdapter<T>) inline fun <T, K, V, M : MutableMap<in K, in V>> CollectionVLong<T>.associateTo(destination: M, crossinline keySelector: (T) -> K, crossinline valueTransform: (T) -> V): M = destination.also{ c->forEach {c.put(keySelector(it),valueTransform(it))}}
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>,) inline fun <T, K> CollectionVLong<T>.associateByVIntLong(crossinline keySelector: (T) -> K): MutableMapVIntLong<K,T> = associateTo(MutableMapVIntLong<K,T>(size), keySelector, {it})
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>) inline fun <T, K> CollectionVLong<T>.associateByVLongLong(crossinline keySelector: (T) -> K): MutableMapVLongLong<K,T> = associateTo(MutableMapVLongLong<K,T>(size), keySelector,{it})
context(a: ValueLongAdapter<T>) inline fun <T, K> CollectionVLong<T>.associateByGeneric(crossinline keySelector: (T) -> K): Map<K, T> = associateTo(HashMap(size+size/4), keySelector, {it})
context(a: ValueLongAdapter<T>, ka: ValueIntAdapter<K>) inline fun <T, K, C:MutableMapVIntLong<K,T>> CollectionVLong<T>.associateByVIntLongTo(destination: C, crossinline keySelector: (T) -> K): MutableMapVIntLong<K,T> = associateTo(MutableMapVIntLong<K,T>(size), keySelector, {it})
context(a: ValueLongAdapter<T>, ka: ValueLongAdapter<K>) inline fun <T, K, C:MutableMapVLongInt<K,T>> CollectionVLong<T>.associateByVLongLongTo(destination: C, crossinline keySelector: (T) -> K): MutableMapVLongLong<K,T> = associateTo(MutableMapVLongLong<K,T>(size), keySelector,{it})
context(a: ValueLongAdapter<T>) inline fun <T, K, C:MutableMap<K,T>> CollectionVLong<T>.associateByGenericTo(destination: C, crossinline keySelector: (T) -> K): Map<K, T> = associateTo(HashMap(size+size/4), keySelector, {it})
 inline fun <T, C : MutableCollectionVLong<T>> CollectionVLong<T>.toCollection(destination: C): C = destination.also{ c -> c.addAll(this) }
context(a: ValueLongAdapter<T>) inline fun <T, C : MutableCollection<T>> CollectionVLong<T>.toCollection(destination: C): C = destination.also{ c->forEach {c.add(it)}}
 inline fun <T> CollectionVLong<T>.toList(): VLongList<T> = this as? VLongList<T> ?: toMutableList()
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.toListGeneric(): List<T> = toMutableListGeneric()
 inline fun <T> CollectionVLong<T>.toMutableList(): ArrayVLongList<T> = this as? ArrayVLongList<T> ?: toCollection(ArrayVLongList<T>(size))
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.toMutableListGeneric(): MutableList<T> = toCollection(ArrayList(size))
 inline fun <T> CollectionVLong<T>.toSet(): SetVLong<T> = this as? SetVLong<T> ?: toMutableSet()
 inline fun <T> CollectionVLong<T>.toMutableSet(): MutableSetVLong<T> = this as? MutableSetVLong<T> ?: toCollection(ArraySetVLong<T>(size))
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.toSetGeneric(): Set<T> = toHashSet()
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.toHashSet(): HashSet<T> = toCollection(HashSet(size + size/4))
 inline fun <T> CollectionVLong<T>.toLongArray(): LongArray = (this as? ArrayVLong<T>)?.collection ?: LongArray(size).also { c->forEachIndexedBits{ i, e-> c[i]=e}}
 inline fun <T> CollectionVLong<T>.toVLongArray(): ArrayVLong<T> = this as? ArrayVLong<T> ?: ArrayVLong(this)
 inline fun <T> CollectionVLong<T>.toArrayGenericBits(): Array<LongBits> = (this as? ArrayVLong<T>)?.collection?.toTypedArray() ?: Array(size,{NULL_VALUE}).also { c->forEachIndexedBits{ i, e-> c[i]=e}}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.asSequence(): Sequence<T> = asIterable().asSequence()
 inline fun <T> CollectionVLong<T>.asList(): VLongList<T> = toList()
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.asListGeneric(): List<T> = toListGeneric()
 inline fun <T> CollectionVLong<T>.contentEquals(other: CollectionVLong<T>?): Boolean = other != null && size == other.size && allBits { other.containsBits(it) }
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.flatMap(crossinline transform: (T) ->CollectionVInt<R>): ArrayVIntList<R> = flatMapTo(ArrayVIntList(size*2), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.flatMap(crossinline transform: (T) ->CollectionVLong<R>): ArrayVLongList<R> = flatMapTo(ArrayVLongList(size*2), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollectionVInt<R>> CollectionVLong<T>.flatMapTo(destination: C, crossinline transform: (T) ->CollectionVInt<R>): C = destination.also{forEach { destination.addAll(transform(it)) }}
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollectionVLong<R>> CollectionVLong<T>.flatMapTo(destination: C, crossinline transform: (T) ->CollectionVLong<R>): C = destination.also{forEach { destination.addAll(transform(it)) }}
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.flatMapIndexed(crossinline transform: (Int, T) ->CollectionVInt<R>): ArrayVIntList<R> = flatMapIndexedTo(ArrayVIntList(size*2), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.flatMapIndexed(crossinline transform: (Int, T) ->CollectionVLong<R>): ArrayVLongList<R> = flatMapIndexedTo(ArrayVLongList(size*2), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollectionVInt<R>> CollectionVLong<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int, T) ->CollectionVInt<R>): C = destination.also{forEachIndexed { i, e-> destination.addAll(transform(i,e)) }}
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollectionVLong<R>> CollectionVLong<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int, T) ->CollectionVLong<R>): C = destination.also{forEachIndexed { i, e-> destination.addAll(transform(i,e)) }}
context(a: ValueLongAdapter<T>) inline fun <T, K> CollectionVLong<T>.groupBy(crossinline keySelector: (T) -> K): MutableMap<K, MutableVLongList<T>> = groupByTo(HashMap<K,MutableVLongList<T>>(), keySelector)
context(a: ValueLongAdapter<T>) inline fun <T, K, M : MutableMap<K, MutableVLongList<T>>> CollectionVLong<T>.groupByTo(destination: M, crossinline keySelector: (T) -> K): M = destination.also{ c-> forEach { c.getOrPut(keySelector(it),{ ArrayVLongList(size) }).add(it) }}
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> CollectionVLong<T>.mapVInt(crossinline transform: (T) -> R): ArrayVIntList<R> = mapTo(ArrayVIntList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> CollectionVLong<T>.mapVLong(crossinline transform: (T) -> R): ArrayVLongList<R> = mapTo(ArrayVLongList<R>(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.mapGeneric(crossinline transform: (T) -> R): MutableList<R> = mapTo(ArrayList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> CollectionVLong<T>.mapIndexedVLong(crossinline transform: (index: Int, T) -> R): ArrayVIntList<R> = mapIndexedTo(ArrayVIntList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> CollectionVLong<T>.mapIndexedVLong(crossinline transform: (index: Int, T) -> R): ArrayVLongList<R> = mapIndexedTo(ArrayVLongList<R>(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.mapIndexedGeneric(crossinline transform: (index: Int, T) -> R): List<R> = mapIndexedTo(ArrayList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> CollectionVLong<T>.mapIndexedVLongNotNull(crossinline transform: (index: Int, T) -> R?): ArrayVIntList<R> = mapIndexedNotNullTo(ArrayVIntList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> CollectionVLong<T>.mapIndexedVLongNotNull(crossinline transform: (index: Int, T) -> R?): ArrayVLongList<R> = mapIndexedNotNullTo(ArrayVLongList<R>(size), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.mapIndexedGenericNotNull(crossinline transform: (index: Int, T) -> R?): List<R> = mapIndexedNotNullTo(ArrayList<R>(size), transform)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableCollectionVInt<R>> CollectionVLong<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{ c->forEachIndexed{ i, e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableCollectionVLong<R>> CollectionVLong<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{ c->forEachIndexed{ i, e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>> CollectionVLong<T>.mapIndexedNotNullTo(destination: C, crossinline transform: (index: Int, T) -> R?): C = destination.also{ c->forEachIndexed{ i, e->transform(i,e)?.also{c.add(it)} } }
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableCollectionVInt<R>> CollectionVLong<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{ i, e-> destination.add(transform(i,e)) } }
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableCollectionVLong<R>> CollectionVLong<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{ i, e-> destination.add(transform(i,e)) } }
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>> CollectionVLong<T>.mapIndexedTo(destination: C, crossinline transform: (index: Int, T) -> R): C = destination.also {forEachIndexed{ i, e-> destination.add(transform(i,e)) } }
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.mapNotNull(crossinline transform: (T) -> R?): List<R> = mapNotNullTo(mutableListOf(), transform)
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>> CollectionVLong<T>.mapNotNullTo(destination: C, crossinline transform: (T) -> R?): C = destination.also {forEach{transform(it)?.also {destination.add(it) } }}
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R, C : MutableCollectionVInt<R>> CollectionVLong<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R, C : MutableCollectionVLong<R>> CollectionVLong<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueLongAdapter<T>) inline fun <T, R, C : MutableCollection<R>> CollectionVLong<T>.mapTo(destination: C, crossinline transform: (T) -> R): C = destination.also {forEach{destination.add(transform(it)) } }
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.withIndex():Collection<IndexedVLong<T>> = mapIndexedGeneric{ i, e-> IndexedVLong.of(i,e)}
 inline fun <T> CollectionVLong<T>.distinct(): SetVLong<T> = ArraySetVLong<T>(size).also{ c-> forEachBits {c.addBits(it)}}
context(a: ValueLongAdapter<T>) inline fun <T, K> CollectionVLong<T>.distinctBy(crossinline selector: (T) -> K): SetVLong<T> {
    val distinct = HashSet<K>()
    val result = ArraySetVLong<T>(size)
    forEach {
        val k = selector(it)
        if (!distinct.contains(k)) {
            distinct.add(k)
            result.add(it)
        }
    }
    return result
}
 inline infix fun <T> CollectionVLong<T>.intersect(other:CollectionVLong<T>): SetVLong<T> = ArraySetVLong<T>(size).also { c-> forEachBits{ if (other.containsBits(it)) c.addBits(it)}}
 inline infix fun <T> CollectionVLong<T>.subtract(other:CollectionVLong<T>): SetVLong<T> = ArraySetVLong<T>(size).also { c-> forEachBits{ if (!other.containsBits(it)) c.addBits(it)}}
 inline infix fun <T> CollectionVLong<T>.union(other:CollectionVLong<T>): SetVLong<T> = toMutableSet().also{ c-> c.addAll(other)}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.any(): Boolean = size > 0
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.count(): Int = size
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.count(crossinline predicate: (T) -> Boolean): Int = fold(0,{ acc, e->if(predicate(e)) acc+1 else acc})
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.fold(initial: R, crossinline operation: (acc: R, T) -> R): R = foldIndexed(initial,{ _, acc, e->operation(acc,e)})
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.foldIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): R {
    val accumulator = object: (Int,T)->Unit {
        var index=0
        var acc = initial
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e) }
    }
    forEachIndexed(accumulator)
    return accumulator.acc
}
context(a: ValueLongAdapter<T>) inline fun <T, C: CollectionVLong<T>> C.onEach(crossinline action: (T) -> Unit): C = apply{forEach(action)}
context(a: ValueLongAdapter<T>) inline fun <T, C: CollectionVLong<T>> C.onEachIndexed(crossinline action: (Int, T) -> Unit): C = apply{forEachIndexed(action)}
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> CollectionVLong<T>.max(): T = reduce { l, r -> if(r>l) r else l}
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> CollectionVLong<T>.maxOrNull(): T? = if(isEmpty()) null else max()
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.maxWith(comparator: Comparator<in T>): T = reduce { l, r -> if(comparator.compare(r,l)<0) r else l}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.maxWithOrNull(comparator: Comparator<in T>): T? = if(isEmpty()) null else maxWith(comparator)
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVLong<T>.maxBy(crossinline selector: (T) -> R): T {
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
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVLong<T>.maxByOrNull(crossinline selector: (T) -> R): T? = if(isEmpty()) null else maxBy(selector)
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVLong<T>.maxOf(crossinline selector: (T) -> R): R = mapReduce(selector) { max, e-> if (e>max) e else max}
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVLong<T>.maxOfOrNull(crossinline selector: (T) -> R): R? = if(isEmpty()) null else maxOf(selector)
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.maxOfWith(comparator: Comparator<in R>, crossinline selector: (T) -> R): R = mapReduce(selector) { max, e-> if (comparator.compare(e,max)>1) e else max}
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.maxOfWithOrNull(comparator: Comparator<in R>, crossinline selector: (T) -> R): R? = if(isEmpty()) null else maxOfWith(comparator, selector)
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> CollectionVLong<T>.min(): T = reduce { l, r -> if(r<l) r else l}
context(a: ValueLongAdapter<T>) inline fun <T : Comparable<T>> CollectionVLong<T>.minOrNull(): T? = if(isEmpty()) null else min()
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.minWith(comparator: Comparator<in T>): T = reduce { l, r -> if(comparator.compare(r,l)<0) r else l}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.minWithOrNull(comparator: Comparator<in T>): T? = if(isEmpty()) null else minWith(comparator)
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVLong<T>.minBy(crossinline selector: (T) -> R): T {
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
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVLong<T>.minByOrNull(crossinline selector: (T) -> R): T? = if(isEmpty()) null else minBy(selector)
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVLong<T>.minOf(crossinline selector: (T) -> R): R = mapReduce(selector) { min, e-> if (e<min) e else min}
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVLong<T>.minOfOrNull(crossinline selector: (T) -> R): R? = if(isEmpty()) null else minOf(selector)
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.minOfWith(comparator: Comparator<in R>, crossinline selector: (T) -> R): R = mapReduce(selector) { min, e-> if (comparator.compare(e,min)<1) e else min}
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.minOfWithOrNull(comparator: Comparator<in R>, crossinline selector: (T) -> R): R? = if(isEmpty()) null else minOfWith(comparator, selector)
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.none(): Boolean = size==0
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.none(crossinline predicate: (T) -> Boolean): Boolean = any { !predicate(it) }
context(a: ValueLongAdapter<T>) inline fun <T: Comparable<T>> CollectionVLong<T>.sorted(): ArrayVLongList<T> = ArrayVLongList<T>(this).also{it.sort()}
context(a: ValueLongAdapter<T>) inline fun <T: Comparable<T>> CollectionVLong<T>.sortedArray(): ArrayVLong<T> = toVLongArray().also{it.sort()}
context(a: ValueLongAdapter<T>) inline fun <T: Comparable<T>> CollectionVLong<T>.sortedArrayDescending(): ArrayVLong<T> = toVLongArray().also{it.sortDescending()}
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVLong<T>.sortedBy(crossinline selector: (T) -> R?): ArrayVLongList<T> =  toMutableList().also{it.sortBy(selector)}
context(a: ValueLongAdapter<T>) inline fun <T, R : Comparable<R>> CollectionVLong<T>.sortedByDescending(crossinline selector: (T) -> R?): ArrayVLongList<T> = toMutableList().also{it.sortByDescending(selector)}
context(a: ValueLongAdapter<T>) inline fun <T: Comparable<T>> CollectionVLong<T>.sortedDescending(): ArrayVLongList<T> = toMutableList().also{it.sortDescending()}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.sortedWith(comparator: Comparator<in T>): ArrayVLongList<T> = toMutableList().also{it.sortWith(comparator)}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.sumBy(crossinline selector: (T) -> Int): Int = mapReduce(selector) { acc, e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.sumByDouble(crossinline selector: (T) -> Double): Double = mapReduce(selector) { acc, e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.sumOf(crossinline selector: (T) -> Double): Double = mapReduce(selector) { acc, e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.sumOf(crossinline selector: (T) -> Int): Int = mapReduce(selector) { acc, e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.sumOf(crossinline selector: (T) -> Long): Long = mapReduce(selector) { acc, e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.sumOfUInt(crossinline selector: (T) -> UInt): UInt = mapReduce(selector) { acc, e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.sumOfULong(crossinline selector: (T) -> ULong): ULong = mapReduce(selector) { acc, e->acc+e}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.chunked(size: Int): List<ArrayVLongList<T>>{
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
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.chunked(size: Int, crossinline transform: (VLongList<T>) -> R): List<R> = chunked(size).map(transform)
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.plusElement(element: T): VLongList<T> = plus(element)
context(a: ValueLongAdapter<T>) inline operator fun <T> CollectionVLong<T>.plus(element: T): VLongList<T> = ArrayVLongList<T>(size+1).also {it.addAll(this); it.add(element) }
 inline operator fun <T> CollectionVLong<T>.plus(elements: CollectionVLong<T>): VLongList<T> = ArrayVLongList<T>(size+elements.size).also {it.addAll(this); it.addAll(elements) }
context(a: ValueLongAdapter<T>) inline operator fun <T> CollectionVLong<T>.plus(elements: Iterable<T>): VLongList<T> = ArrayVLongList<T>(size+1).also {it.addAll(this); it.addAll(elements) }
context(a: ValueLongAdapter<T>) inline operator fun <T> CollectionVLong<T>.plus(elements: Array<out T>): VLongList<T> = ArrayVLongList<T>(size+1).also {it.addAll(this); it.addAll(elements) }
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.minus(element: T): VLongList<T> = ArrayVLongList<T>(size).also { c-> forEach { if (it != element) c.add(it) } }
context(a: ValueLongAdapter<T>) inline operator fun <T> CollectionVLong<T>.minus(elements: Array<out T>): VLongList<T> = ArrayVLongList<T>(size).also { c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueLongAdapter<T>) inline operator fun <T> CollectionVLong<T>.minus(elements:CollectionVLong<T>): VLongList<T> = ArrayVLongList<T>(size).also { c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueLongAdapter<T>) inline operator fun <T> CollectionVLong<T>.minus(elements:Iterable<T>): VLongList<T> = ArrayVLongList<T>(size).also { c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueLongAdapter<T>) inline operator fun <T> CollectionVLong<T>.minus(elements: Sequence<T>): VLongList<T> = ArrayVLongList<T>(size).also { c-> forEach { if (!c.contains(it)) c.add(it) } }
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.minusElement(element: T): VLongList<T> = minus(element)
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.partition(crossinline predicate: (T) -> Boolean): Pair<VLongList<T>, VLongList<T>> {
    val trueList = ArrayVLongList<T>(size)
    val falseList = ArrayVLongList<T>(size)
    forEach { if (predicate(it)) trueList.add(it) else falseList.add(it) }
    return trueList to falseList
}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.random(): T = random(Random.Default)
context(a: ValueLongAdapter<T>) fun <T> CollectionVLong<T>.random(random: Random): T {
    if (size==0) throw NoSuchElementException()
    val findIdx = random.nextInt(size)
    return fromLong(findIndexedBits {i,e->i==findIdx})
}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.randomOrNull(): T? = randomOrNull(Random.Default)
context(a: ValueLongAdapter<T>) fun <T> CollectionVLong<T>.randomOrNull(random: Random): T? = if (size==0) null else random(random)
context(a: ValueLongAdapter<T>) inline infix fun <T, R> CollectionVLong<T>.zip(other: Array<out R>): MutableList<Pair<T, R>> = zip(other, { a, b->a to b})
context(a: ValueLongAdapter<T>) inline fun <T, R, V> CollectionVLong<T>.zip(other: Array<out R>, crossinline transform: (a: T, b: R) -> V): MutableList<V> {
    val r = mutableListOf<V>()
    forEachIndexed { i, e -> if (i < other.size) r.add(i, transform(e, other[i])) }
    return r
}
context(ta: ValueLongAdapter<T>, ra: ValueLongAdapter<R>, va: ValueLongAdapter<V>) inline fun <T, R, V> CollectionVLong<T>.zipVLongIntPair(other:VLongIndexedCollection<R>, crossinline transform: (a: T, b: R) -> V): MutableVLongList<V> {
    val r = ArrayVLongList<V>(min(size, other.size))
    forEachIndexed { i, e -> if (i < other.size) r.add(i, transform(e, other.get(i))) }
    return r
}
context(a: ValueLongAdapter<T>) inline fun <T, A : Appendable> CollectionVLong<T>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence) = { it.toString() }): A {
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
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((T) -> CharSequence) = { it.toString() }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.toVString() = joinToString(", ","{","}")
context(a: ValueLongAdapter<T>) inline fun <S, R : S, T> CollectionVLong<T>.mapReduce(crossinline map:(T)->S, crossinline operation: (acc: S, S) -> S): S = mapReduceIndexed(map){ i, acc, e->operation(acc,e)}
context(a: ValueLongAdapter<T>) inline fun <S, R : S, T> CollectionVLong<T>.mapReduceIndexed(crossinline map:(T)->R, crossinline operation: (index:Int, acc: S, next:R) -> S): S {
    val accumulator = object: (Int,T)->Unit {
        var acc: S = map(findOrThrow{true})
        override inline fun invoke(i:Int, e: T) { if (i>0) acc = operation(i, acc, map(e)) }
    }
    forEachIndexed(accumulator)
    return accumulator.acc
}
context(a: ValueLongAdapter<T>) inline fun <S, T : S> CollectionVLong<T>.reduce(crossinline operation: (acc: S, T) -> S): S = reduceIndexed<S,T>{ i, acc, e -> operation(acc,e) }
context(a: ValueLongAdapter<T>) inline fun <S, T : S> CollectionVLong<T>.reduceIndexed(crossinline operation: (index: Int, acc: S, T) -> S): S = mapReduceIndexed({it}, operation)
context(a: ValueLongAdapter<T>) inline fun <S, T : S> CollectionVLong<T>.reduceIndexedOrNull(crossinline operation: (index: Int, acc: S, T) -> S): S? = if (size==0) return null else reduceIndexed(operation)
context(a: ValueLongAdapter<T>) inline fun <S, T : S> CollectionVLong<T>.reduceOrNull(crossinline operation: (acc: S, T) -> S): S? = if (size==0) return null else reduce(operation)
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> CollectionVLong<T>.runningFoldVInt(initial: R, crossinline operation: (acc: R, T) -> R): MutableVIntList<R> = runningFoldVIntIndexed(initial) { i, acc, e->operation(acc,e)}
context(ta: ValueLongAdapter<T>, ra: ValueIntAdapter<R>) inline fun <T, R> CollectionVLong<T>.runningFoldVIntIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): MutableVIntList<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = ArrayVIntList<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> CollectionVLong<T>.runningFoldVLong(initial: R, crossinline operation: (acc: R, T) -> R): MutableVLongList<R> = runningFoldVLongIndexed(initial) { i, acc, e->operation(acc,e)}
context(ta: ValueLongAdapter<T>, ra: ValueLongAdapter<R>) inline fun <T, R> CollectionVLong<T>.runningFoldVLongIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): MutableVLongList<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = ArrayVLongList<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.runningFoldGeneric(initial: R, crossinline operation: (acc: R, T) -> R): List<R> = runningFoldGenericIndexed(initial) { i, acc, e->operation(acc,e)}
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.runningFoldGenericIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): List<R> {
    val accumulator = object: (Int,T)->Unit {
        var acc = initial
        var result = mutableListOf<R>()
        override inline fun invoke(i:Int, e: T) { acc = operation(i, acc, e); result.add(acc) }
    }
    forEachIndexed(accumulator)
    return accumulator.result
}
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<S>) inline fun <S, T : S> CollectionVLong<T>.runningReduceVInt(crossinline operation: (acc: S, T) -> S): MutableVIntList<S> = with(ra){runningReduceVIntIndexed { i, acc, e -> operation(acc,e)}}
context(a: ValueLongAdapter<T>, ra: ValueIntAdapter<S>) inline fun <S, T : S> CollectionVLong<T>.runningReduceVIntIndexed(crossinline operation: (index: Int, acc: S, T) -> S): MutableVIntList<S>  {
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
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.runningReduceVLong(crossinline operation: (acc: T, T) -> T): MutableVLongList<T> = runningReduceVLongIndexed { i, acc, e -> operation(acc,e)}
context(a: ValueLongAdapter<T>) inline fun <T> CollectionVLong<T>.runningReduceVLongIndexed(crossinline operation: (index: Int, acc: T, T) -> T): MutableVLongList<T> {
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
context(a: ValueLongAdapter<T>) inline fun <S, T : S> CollectionVLong<T>.runningReduceGeneric(crossinline operation: (acc: S, T) -> S): List<S> = runningReduceGenericIndexed<S,T> { i, acc, e -> operation(acc,e)}
context(a: ValueLongAdapter<T>) inline fun <S, T : S> CollectionVLong<T>.runningReduceGenericIndexed(crossinline operation: (index: Int, acc: S, T) -> S): List<S> {
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
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.scan(initial: R, crossinline operation: (acc: R, T) -> R): List<R> = runningFoldGeneric(initial, operation)
context(a: ValueLongAdapter<T>) inline fun <T, R> CollectionVLong<T>.scanIndexed(initial: R, crossinline operation: (index: Int, acc: R, T) -> R): List<R> = runningFoldGenericIndexed(initial, operation)
