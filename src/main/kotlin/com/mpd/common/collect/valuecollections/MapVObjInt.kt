@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package com.mpd.common.collect.valuecollections

import androidx.collection.MutableObjectIntMap
import androidx.collection.ObjectIntMap
import androidx.collection.ScatterSet

interface MapVObjInt<K,V>: MapVObjectKey<K>, MapVIntValue<V> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.

    override val size: Int
    fun getBits(k: K): IntValueBits
    fun anyBits(predicate: (K, IntValueBits) -> Boolean): K?

    context(va: ValueIntAdapter<V>) fun toIterable(): Iterable<PairVObjInt<K,V>>

    @JvmName("toStringV") @Suppress("INAPPLICABLE_JVM_NAME")
    context(va: ValueIntAdapter<V>) fun toString(): String = toStringV()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(va: ValueIntAdapter<V>)  inline fun <K,V> MapVObjInt<K,V>.asMapGeneric(): Map<K,V> = object: Map<K,V> {
    override inline val size: Int get() = this@asMapGeneric.size
    override inline val keys: Set<K> get() = HashSet<K>(size).also { s -> forEach { k, _ -> s.add(k) } }
    override inline val values: Collection<V> get() = ArrayList<V>(size).also { l -> forEach { _, v -> l.add(v) } }
    override inline val entries: Set<Map.Entry<K, V>> get() = HashSet<Map.Entry<K,V>>(size).also { s -> forEach { k, v -> s.add(java.util.AbstractMap.SimpleImmutableEntry(k, v)) } }
    override inline fun isEmpty(): Boolean = this@asMapGeneric.isEmpty
    override inline fun containsKey(key: K): Boolean = this@asMapGeneric.containsKey(key)
    override inline fun containsValue(value: V): Boolean = this@asMapGeneric.containsValue(value)
    override inline fun get(key: K): V? = this@asMapGeneric.getOrNull(key)
}
context(va: ValueIntAdapter<V>) inline operator fun <K,V> MapVObjInt<K,V>.get(key: K): V = valueFromInt(getBits(key))
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.getOr(key: K, defaultResult:()->V): V = valueFromIntOr(getBits(key), defaultResult)
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.getOrNull(key: K): V? = valueFromIntOrNull(getBits(key))
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.any(crossinline predicate:(K, V)->Boolean):K? = anyBits{ k, v-> predicate(k, va.fromInt(v))}
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.anyOr(crossinline predicate:(K, V)->Boolean, defaultResult:()->K):K = anyBits{ k, v-> predicate(k, va.fromInt(v))} ?: defaultResult()
inline fun <K,V> MapVObjInt<K,V>.anyIndexedBits(crossinline predicate:(index:Int, K, IntValueBits)->Boolean):K? {
    return anyBits(object: (K,IntValueBits) -> Boolean {
        var index = 0
        override inline fun invoke(k: K, v:IntValueBits) = predicate(index++, k,v)
    } )
}
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.anyIndexed(crossinline action:(index:Int, K, V)->Boolean):K? = anyIndexedBits{ index, k, v-> action(index, k, va.fromInt(v))}
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.anyIndexedOr(crossinline action:(index:Int, K, V)->Boolean, defaultResult:()->K):K = anyIndexedBits{ index, k, v-> action(index, k, va.fromInt(v))} ?: defaultResult()
inline fun <K,V> MapVObjInt<K,V>.forEachBits(crossinline action:(K, IntValueBits)->Unit) {anyBits { k, v-> action(k,v); false} }
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.forEach(crossinline action:(K, V)->Unit) = forEachBits { k, v-> action(k, va.fromInt(v)) }
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.forEachPair(crossinline action:(PairVObjInt<K,V>)->Unit) {
    forEachBits(object: (K, IntValueBits) -> Unit {
        var init = false
        lateinit var pair: PairVObjInt<K,V>
        override inline fun invoke(k: K, v: IntValueBits) {
            if (!init) {pair = PairVObjInt(k, v); init = true}
            pair.first = k
            pair.secondBits = v
            action(pair)
        }
    })
}
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.forEachIndexed(crossinline action:(index:Int, K, V)->Unit) {
    forEachBits(object: (K, IntValueBits) -> Unit {
        var index=0
        override inline fun invoke(k: K, v: IntValueBits) = action(index++, k, va.fromInt(v))
    })
}
inline val <K,V> MapVObjInt<K,V>.isEmpty get() = size == 0
inline fun <K,V> MapVObjInt<K,V>.isNotEmpty() = size > 0
context(va: ValueIntAdapter<V>) inline fun <K,V, A : Appendable> MapVObjInt<K,V>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): A {
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
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.toStringV() = joinToString(", ","{","}")



interface MutableMapVObjInt<K,V>: MapVObjInt<K,V> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim()
    fun clear()

    fun setBits(k: K, v: IntValueBits, defaultReturn: IntValueBits): IntValueBits
    fun getOrPutBits(k: K, defaultSet: () -> IntValueBits): IntValueBits
    fun removeBits(k: K)
    fun removeBits(k: K, v: IntValueBits):Boolean
    fun removeIfBits(predicate:(K,IntValueBits)->Boolean)
    context(va: ValueIntAdapter<V>) override fun toIterable(): Iterable<PairVObjInt<K,V>>

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
inline fun <K,V> MutableMapVObjInt<K,V>.preallocateFor(newSize: Int) {ensureCapacity(newSize + newSize/4) }
context(va: ValueIntAdapter<V>) inline operator fun <K,V> MutableMapVObjInt<K,V>.set(key: K, value: V): Boolean = setBits(key, va.toInt(value), NULL_VALUE_BITS) != NULL_VALUE_BITS
context(va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVObjInt<K,V>.set(key: K, value: V, defaultReturn: V): V = valueFromInt(setBits(key, va.toInt(value), va.toInt(defaultReturn)))
context(va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVObjInt<K,V>.getOrPut(key: K, crossinline defaultValue: ()->V):V = valueFromInt(getOrPutBits(key, {va.toInt(defaultValue())}))
inline fun <K,V> MutableMapVObjInt<K,V>.putAll(source: MapVObjInt<K,V>) {preallocateFor(size+source.size + (size+source.size)/4); source.forEachBits { k, v-> setBits(k,v, NULL_VALUE_BITS)} }
context(va: ValueIntAdapter<V>, ska: ValueIntAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVObjInt<K,V>.putAll(source: MapVIntInt<SK,SV>, crossinline keySelector: (PairVIntInt<SK,SV>) -> K, crossinline valueTransform: (PairVIntInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueIntAdapter<V>, ska: ValueIntAdapter<SK>, sva: ValueLongAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVObjInt<K,V>.putAll(source: MapVIntLong<SK,SV>, crossinline keySelector: (PairVIntLong<SK,SV>) -> K, crossinline valueTransform: (PairVIntLong<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueIntAdapter<V>, ska: ValueLongAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVObjInt<K,V>.putAll(source: MapVLongInt<SK,SV>, crossinline keySelector: (PairVLongInt<SK,SV>) -> K, crossinline valueTransform: (PairVLongInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueIntAdapter<V>, ska: ValueLongAdapter<SK>, sva: ValueLongAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVObjInt<K,V>.putAll(source: MapVLongLong<SK,SV>, crossinline keySelector: (PairVLongLong<SK,SV>) -> K, crossinline valueTransform: (PairVLongLong<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVObjInt<K,V>.putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVObjInt<K,V>.putAll(source: CollectionVInt<S>, crossinline transform: (S) -> PairVObjInt<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVObjInt<K,V>.putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVObjInt<K,V>.putAll(source: CollectionVLong<S>, crossinline transform: (S) -> PairVObjInt<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(va: ValueIntAdapter<V>) inline fun <K,V,S> MutableMapVObjInt<K,V>.putAllGeneric(source: Collection<S>, crossinline transform: (S) -> Pair<K, V>) { preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
inline infix operator fun <K,V> MutableMapVObjInt<K,V>.plusAssign(source: MapVObjInt<K,V>) = putAll(source)
inline fun <K,V> MutableMapVObjInt<K,V>.remove(key: K) = removeBits(key)
context(va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVObjInt<K,V>.remove(key: K, value:V): Boolean = removeBits(key, va.toInt(value))
context(va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVObjInt<K,V>.removeIf(crossinline predicate:(K, V)->Boolean) = removeIfBits { k, v-> predicate(k, va.fromInt(v))}
inline infix operator fun <K,V> MutableMapVObjInt<K,V>.minusAssign(key: K) {remove(key)}




class HashMapVObjInt<K,V>(val collection: MutableObjectIntMap<K> =MutableObjectIntMap(), override val NULL_VALUE_BITS: IntValueBits=Int.MIN_VALUE)
    : MutableMapVObjInt<K,V> {
    constructor(size: Int, NO_VALUE: IntValueBits=Int.MIN_VALUE) : this(MutableObjectIntMap(size), NO_VALUE)

    override val size: Int get() = collection.size
    override inline fun getBits(k: K): IntValueBits = collection.getOrDefault(k, NULL_VALUE_BITS)
    override inline fun anyBits(predicate: (K, IntValueBits) -> Boolean): K? {
        val finder = object : (K, IntValueBits) -> Unit {
            var result: K? = null
            var found = false
            override inline fun invoke(k: K, v: IntValueBits) { if (!found && predicate(k, v)) { result = k; found = true } }
        }
        collection.forEach(finder)
        return finder.result
    }
    override inline fun trim() { collection.trim() }
    override inline fun clear() = collection.clear()
    override inline fun setBits(k: K, v: IntValueBits, defaultReturn: IntValueBits): IntValueBits = collection.put(k,v, defaultReturn)
    override inline fun getOrPutBits(k: K, defaultSet: () -> IntValueBits): IntValueBits = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: K) = collection.remove(k)
    override inline fun removeBits(k: K, v:IntValueBits): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (K, IntValueBits) -> Boolean) = collection.removeIf(predicate)

    override inline fun anyKeyOrNull(predicate: (K) -> Boolean): K? = anyBits { k, _ -> predicate(k) }
    override inline fun removeKey(key: K): Boolean { val had = collection.containsKey(key); removeBits(key); return had }
    override inline fun forEachValueBits(action: (valueBits: IntValueBits) -> Unit) = collection.forEachValue(action)

    // Thin wrappers for every public method of MutableObjectIntMap.
    inline fun capacity(): Int = collection.capacity
    inline fun none(): Boolean = collection.none()
    inline fun isEmpty(): Boolean = collection.isEmpty()
    inline fun isNotEmptyBits(): Boolean = collection.isNotEmpty()
    inline fun getOrDefault(key: K, defaultValue: IntValueBits): IntValueBits = collection.getOrDefault(key, defaultValue)
    inline fun getOrElse(key: K, defaultValue: () -> IntValueBits): IntValueBits = collection.getOrElse(key, defaultValue)
    inline fun forEachBits(block: (key: K, value: IntValueBits) -> Unit) = collection.forEach(block)
    override inline fun forEachKey(block: (key: K) -> Unit) = collection.forEachKey(block)
    inline fun forEachValue(block: (value: IntValueBits) -> Unit) = collection.forEachValue(block)
    inline fun all(predicate: (K, IntValueBits) -> Boolean): Boolean = collection.all(predicate)
    inline fun count(): Int = collection.count()
    inline fun count(predicate: (K, IntValueBits) -> Boolean): Int = collection.count(predicate)
    inline operator fun contains(key: K): Boolean = collection.contains(key)
    inline fun containsKeyBits(key: K): Boolean = collection.containsKey(key)
    override inline fun containsKey(key: K): Boolean = containsKeyBits(key)
    override inline fun containsValueBits(bits: IntValueBits): Boolean = collection.containsValue(bits)
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
        crossinline transform: (key: K, value: IntValueBits) -> CharSequence,
    ): String = collection.joinToString(separator, prefix, postfix, limit, truncated, transform)
    inline fun setBits(key: K, value: IntValueBits) = collection.set(key, value)
    inline fun put(key: K, value: IntValueBits) = collection.put(key, value)
    inline fun put(key: K, value: IntValueBits, default: IntValueBits): IntValueBits = collection.put(key, value, default)
    inline fun putAllBits(from: ObjectIntMap<K>) = collection.putAll(from)
    inline fun plusAssignBits(from: ObjectIntMap<K>) = collection.plusAssign(from)
    inline fun minusAssignBits(key: K) = collection.minusAssign(key)
    inline fun minusAssignBits(keys: Array<out K>) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: Iterable<K>) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: Sequence<K>) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: ScatterSet<K>) = collection.minusAssign(keys)

    context(va: ValueIntAdapter<V>) override inline fun toIterable(): Iterable<PairVObjInt<K,V>> {
        val list = ArrayList<PairVObjInt<K,V>>(size)
        collection.forEach { k, v -> list.add(PairVObjInt(k, v)) }
        return object : Iterable<PairVObjInt<K,V>> {
            override inline fun iterator(): Iterator<PairVObjInt<K,V>> = object : Iterator<PairVObjInt<K,V>> {
                var idx = 0
                override inline fun hasNext(): Boolean = idx < list.size
                override inline fun next(): PairVObjInt<K,V> { val p = list[idx++]; return p }
            }
        }
    }

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = collection.toString()
}

