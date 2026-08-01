@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package com.mpd.common.collect.valuecollections

import androidx.collection.IntIntMap
import androidx.collection.IntList
import androidx.collection.IntSet
import androidx.collection.MutableIntIntMap


typealias IntKeyBits = Int
typealias IntValueBits = Int
typealias LongKeyBits = Long
typealias LongValueBits = Long


interface MapVIntInt<K,V> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_KEY_BITS: IntKeyBits
    val NULL_VALUE_BITS: IntValueBits

    val size: Int
    fun getBits(k: IntKeyBits): IntValueBits
    fun anyBits(predicate: (IntKeyBits, IntValueBits) -> Boolean): IntKeyBits

    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) fun asIterable(): Iterable<PairVIntInt<K,V>>

    @JvmName("toStringV") @Suppress("INAPPLICABLE_JVM_NAME")
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) fun toString(): String = toStringV()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)  inline fun <K,V> MapVIntInt<K,V>.asMapGeneric(): Map<K,V> = object: Map<K,V> {
    override inline val size: Int get() = this@asMapGeneric.size
    override inline val keys: Set<K> get() = HashSet<K>(size).also { s -> forEach { k, _ -> s.add(k) } }
    override inline val values: Collection<V> get() = ArrayList<V>(size).also { l -> forEach { _, v -> l.add(v) } }
    override inline val entries: Set<Map.Entry<K, V>> get() = HashSet<Map.Entry<K,V>>(size).also { s -> forEach { k, v -> s.add(java.util.AbstractMap.SimpleImmutableEntry(k, v)) } }
    override inline fun isEmpty(): Boolean = this@asMapGeneric.isEmpty
    override inline fun containsKey(key: K): Boolean = this@asMapGeneric.containsKey(key)
    override inline fun containsValue(value: V): Boolean = this@asMapGeneric.containsValue(value)
    override inline fun get(key: K): V? = this@asMapGeneric.getOrNull(key)
}
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntInt<K,V>.keyFromInt(bits: IntKeyBits): K = if (bits==NULL_KEY_BITS) throw NoSuchElementException() else ka.fromInt(bits)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntInt<K,V>.keyFromIntOr(bits: IntKeyBits, provider: ()->K): K = if (bits==NULL_KEY_BITS) provider() else ka.fromInt(bits)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntInt<K,V>.keyFromIntOrNull(bits: IntKeyBits): K? = if (bits==NULL_KEY_BITS) null else ka.fromInt(bits)
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.valueFromInt(bits: IntValueBits): V = if (bits==NULL_VALUE_BITS) throw NoSuchElementException() else va.fromInt(bits)
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.valueFromIntOr(bits: IntValueBits, provider: ()->V): V = if (bits==NULL_VALUE_BITS) provider() else va.fromInt(bits)
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.valueFromIntOrNull(bits: IntValueBits): V? = if (bits==NULL_VALUE_BITS) null else va.fromInt(bits)
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline operator fun <K,V> MapVIntInt<K,V>.get(key: K): V = valueFromInt(getBits(ka.toInt(key)))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.getOr(key: K, defaultResult:()->V): V = valueFromIntOr(getBits(ka.toInt(key)), defaultResult)
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.getOrNull(key: K): V? = valueFromIntOrNull(getBits(ka.toInt(key)))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.any(crossinline predicate:(K, V)->Boolean):K = keyFromInt(anyBits{ k, v-> predicate(ka.fromInt(k), va.fromInt(v))})
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.anyOr(crossinline predicate:(K, V)->Boolean, defaultResult:()->K):K = keyFromIntOr(anyBits{ k, v-> predicate(ka.fromInt(k), va.fromInt(v))}, defaultResult)
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.anyOrNull(crossinline predicate:(K, V)->Boolean):K? = keyFromIntOrNull(anyBits{ k, v-> predicate(ka.fromInt(k), va.fromInt(v))})
inline fun <K,V> MapVIntInt<K,V>.anyIndexedBits(crossinline predicate:(index:Int, IntKeyBits, IntValueBits)->Boolean):IntKeyBits {
    return anyBits(object: (IntKeyBits,IntValueBits) -> Boolean {
        var index = 0
        override inline fun invoke(k: IntKeyBits, v:IntValueBits) = predicate(index++, k,v)
    } )
}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.anyIndexed(crossinline action:(index:Int, K, V)->Boolean):K = keyFromInt(anyIndexedBits{ index, k, v-> action(index, ka.fromInt(k), va.fromInt(v))})
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.anyIndexedOr(crossinline action:(index:Int, K, V)->Boolean, defaultResult:()->K):K = keyFromIntOr(anyIndexedBits{ index, k, v-> action(index, ka.fromInt(k), va.fromInt(v))}, defaultResult)
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.anyIndexedOrNull(crossinline action:(index:Int, K, V)->Boolean):K? = keyFromIntOrNull(anyIndexedBits{ index, k, v-> action(index, ka.fromInt(k), va.fromInt(v))})
inline fun <K,V> MapVIntInt<K,V>.forEachBits(crossinline action:(IntKeyBits, IntValueBits)->Unit) {anyBits { k, v-> action(k,v); false} }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.forEach(crossinline action:(K, V)->Unit) = forEachBits { k, v-> action(ka.fromInt(k), va.fromInt(v)) }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.forEachPair(crossinline action:(PairVIntInt<K,V>)->Unit) = forEachBits { k, v-> action(PairVIntInt(k,v))}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.forEachIndexed(crossinline action:(index:Int, K, V)->Unit) {
    forEachBits(object: (IntKeyBits, IntValueBits) -> Unit {
        var index=0
        override inline fun invoke(k: IntKeyBits, v: IntValueBits) = action(index++, ka.fromInt(k), va.fromInt(v))
    })
}
inline val <K,V> MapVIntInt<K,V>.isEmpty get() = size == 0
inline fun <K,V> MapVIntInt<K,V>.isNotEmpty() = size > 0
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntInt<K,V>.containsKey(k: K) = getBits(ka.toInt(k)) != NULL_VALUE_BITS
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.containsValue(findV: V) = anyBits { _, v-> v==va.toInt(findV)} != NULL_KEY_BITS
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V, A : Appendable> MapVIntInt<K,V>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): A {
    val appender = object: (Int,K,V)-> Boolean {
        var count=0
        override inline fun invoke(index: Int, k:K, v:V): Boolean {
            if (limit<0 || count++ < limit) {
                if (count != 1) buffer.append(separator)
                buffer.append(transform(k,v))
                if (count < limit)
                    return false
            }
            if (count > limit)
                buffer.append(truncated)
            return true
        }
    }
    buffer.append(prefix)
    anyIndexed(appender)
    buffer.append(postfix)
    return buffer
}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.toStringV() = joinToString(", ","{","}")



interface MutableMapVIntInt<K,V>: MapVIntInt<K,V> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim()
    fun clear()

    fun setBits(k: IntKeyBits, v: IntValueBits, defaultReturn: IntValueBits): IntValueBits
    fun getOrPutBits(k: IntKeyBits, defaultSet: () -> IntValueBits): IntValueBits
    fun removeBits(k: IntKeyBits)
    fun removeBits(k: IntKeyBits, v: IntValueBits):Boolean
    fun removeIfBits(predicate:(IntKeyBits,IntValueBits)->Boolean)
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) override fun asIterable(): MutableIterable<PairVIntInt<K,V>>

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
inline fun <K,V> MutableMapVIntInt<K,V>.preallocateFor(newSize: Int) {ensureCapacity(newSize + newSize/4) }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline operator fun <K,V> MutableMapVIntInt<K,V>.set(key: K, value: V): Boolean = setBits(ka.toInt(key), va.toInt(value), NULL_VALUE_BITS) != NULL_VALUE_BITS
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVIntInt<K,V>.set(key: K, value: V, defaultReturn: V): V = valueFromInt(setBits(ka.toInt(key), va.toInt(value), va.toInt(defaultReturn)))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVIntInt<K,V>.getOrPut(key: K, crossinline defaultValue: ()->V):V = valueFromInt(getOrPutBits(ka.toInt(key), {va.toInt(defaultValue())}))
inline fun <K,V> MutableMapVIntInt<K,V>.putAll(source: MapVIntInt<K,V>) {preallocateFor(size+source.size + (size+source.size)/4); source.forEachBits { k, v-> setBits(k,v, NULL_VALUE_BITS)} }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, ska: ValueIntAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVIntInt<K,V>.putAll(source: MapVIntInt<SK,SV>, crossinline keySelector: (PairVIntInt<SK,SV>) -> K, crossinline valueTransform: (PairVIntInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, ska: ValueIntAdapter<SK>, sva: ValueLongAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVIntInt<K,V>.putAll(source: MapVIntLong<SK,SV>, crossinline keySelector: (PairVIntLong<SK,SV>) -> K, crossinline valueTransform: (PairVIntLong<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, ska: ValueLongAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVIntInt<K,V>.putAll(source: MapVLongInt<SK,SV>, crossinline keySelector: (PairVLongInt<SK,SV>) -> K, crossinline valueTransform: (PairVLongInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, ska: ValueLongAdapter<SK>, sva: ValueLongAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVIntInt<K,V>.putAll(source: MapVLongLong<SK,SV>, crossinline keySelector: (PairVLongLong<SK,SV>) -> K, crossinline valueTransform: (PairVLongLong<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVIntInt<K,V>.putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVIntInt<K,V>.putAll(source: CollectionVInt<S>, crossinline transform: (S) -> PairVIntInt<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVIntInt<K,V>.putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVIntInt<K,V>.putAll(source: CollectionVLong<S>, crossinline transform: (S) -> PairVIntInt<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V,S> MutableMapVIntInt<K,V>.putAllGeneric(source: Collection<S>, crossinline transform: (S) -> Pair<K, V>) { preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
inline infix operator fun <K,V> MutableMapVIntInt<K,V>.plusAssign(source: MapVIntInt<K,V>) = putAll(source)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MutableMapVIntInt<K,V>.remove(key: K) = removeBits(ka.toInt(key))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVIntInt<K,V>.remove(key: K, value:V): Boolean = removeBits(ka.toInt(key), va.toInt(value))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVIntInt<K,V>.removeIf(crossinline predicate:(K, V)->Boolean) = removeIfBits { k, v-> predicate(ka.fromInt(k), va.fromInt(v))}
context(ka: ValueIntAdapter<K>) inline infix operator fun <K,V> MutableMapVIntInt<K,V>.minusAssign(key: K) {remove(key)}




class HashMapVIntInt<K,V>(val collection: MutableIntIntMap=MutableIntIntMap(), override val NULL_KEY_BITS: IntKeyBits=Int.MIN_VALUE, override val NULL_VALUE_BITS: IntValueBits=Int.MIN_VALUE)
    : MutableMapVIntInt<K,V> {
    constructor(size: Int, NULL_KEY_BITS: IntKeyBits=Int.MIN_VALUE, NULL_VALUE_BITS: IntValueBits=Int.MIN_VALUE) : this(MutableIntIntMap(size), NULL_KEY_BITS, NULL_VALUE_BITS)

    override val size: Int get() = collection.size
    override inline fun getBits(k: IntKeyBits): IntValueBits = collection.getOrDefault(k, NULL_VALUE_BITS)
    override inline fun anyBits(predicate: (IntKeyBits, IntValueBits) -> Boolean): IntKeyBits {
        var result: IntKeyBits = NULL_KEY_BITS
        collection.forEach { k, v -> if (result == NULL_KEY_BITS && predicate(k, v)) result = k }
        return result
    }
    override inline fun trim() { collection.trim() }
    override inline fun clear() = collection.clear()
    override inline fun setBits(k: IntKeyBits, v: IntValueBits, defaultReturn: IntValueBits): IntValueBits = collection.put(k,v, defaultReturn)
    override inline fun getOrPutBits(k: IntKeyBits, defaultSet: () -> IntValueBits): IntValueBits = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: IntKeyBits) = collection.remove(k)
    override inline fun removeBits(k: IntKeyBits, v:IntValueBits): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (IntKeyBits, IntValueBits) -> Boolean) = collection.removeIf(predicate)

    // Thin wrappers for every public method of MutableIntIntMap.
    inline fun capacity(): Int = collection.capacity
    inline fun anyBits(): Boolean = collection.any()
    inline fun none(): Boolean = collection.none()
    inline fun isEmpty(): Boolean = collection.isEmpty()
    inline fun isNotEmptyBits(): Boolean = collection.isNotEmpty()
    inline fun getOrDefault(key: IntKeyBits, defaultValue: IntValueBits): IntValueBits = collection.getOrDefault(key, defaultValue)
    inline fun getOrElse(key: IntKeyBits, defaultValue: () -> IntValueBits): IntValueBits = collection.getOrElse(key, defaultValue)
    inline fun forEachBits(block: (key: IntKeyBits, value: IntValueBits) -> Unit) = collection.forEach(block)
    inline fun forEachKey(block: (key: IntKeyBits) -> Unit) = collection.forEachKey(block)
    inline fun forEachValue(block: (value: IntValueBits) -> Unit) = collection.forEachValue(block)
    inline fun all(predicate: (IntKeyBits, IntValueBits) -> Boolean): Boolean = collection.all(predicate)
    inline fun count(): Int = collection.count()
    inline fun count(predicate: (IntKeyBits, IntValueBits) -> Boolean): Int = collection.count(predicate)
    inline operator fun contains(key: IntKeyBits): Boolean = collection.contains(key)
    inline fun containsKeyBits(key: IntKeyBits): Boolean = collection.containsKey(key)
    inline fun containsValueBits(value: IntValueBits): Boolean = collection.containsValue(value)
    inline fun joinToStringBits(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
    ): String = collection.joinToString(separator, prefix, postfix, limit, truncated)
    inline fun joinToStringBits(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        crossinline transform: (key: IntKeyBits, value: IntValueBits) -> CharSequence,
    ): String = collection.joinToString(separator, prefix, postfix, limit, truncated, transform)
    inline fun setBits(key: IntKeyBits, value: IntValueBits) = collection.set(key, value)
    inline fun put(key: IntKeyBits, value: IntValueBits) = collection.put(key, value)
    inline fun put(key: IntKeyBits, value: IntValueBits, default: IntValueBits): IntValueBits = collection.put(key, value, default)
    inline fun putAllBits(from: IntIntMap) = collection.putAll(from)
    inline fun plusAssignBits(from: IntIntMap) = collection.plusAssign(from)
    inline fun minusAssignBits(key: IntKeyBits) = collection.minusAssign(key)
    inline fun minusAssignBits(keys: IntArray) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: IntSet) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: IntList) = collection.minusAssign(keys)

    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) override inline fun asIterable(): MutableIterable<PairVIntInt<K,V>> {
        val list = ArrayList<PairVIntInt<K,V>>(size)
        collection.forEach { k, v -> list.add(PairVIntInt(k, v)) }
        return list
    }

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = collection.toString()
}

