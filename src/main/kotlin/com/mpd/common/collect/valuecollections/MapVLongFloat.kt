@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package com.mpd.common.collect.valuecollections

import androidx.collection.LongFloatMap
import androidx.collection.LongList
import androidx.collection.LongSet
import androidx.collection.MutableLongFloatMap

interface MapVLongFloat<K>: MapVLongKey<K>, MapVFloatValue {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_VALUE: Float

    override val size: Int
    fun getBits(k: LongKeyBits): Float
    fun anyBits(predicate: (LongKeyBits, Float) -> Boolean): LongKeyBits

    context(ka: ValueLongAdapter<K>) fun toIterable(): Iterable<PairVLongObj<K,Float>>

    @JvmName("toStringV") @Suppress("INAPPLICABLE_JVM_NAME")
    context(ka: ValueLongAdapter<K>) fun toString(): String = toStringV()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(ka: ValueLongAdapter<K>)  inline fun <K> MapVLongFloat<K>.asMapGeneric(): Map<K,Float> = object: Map<K,Float> {
    override inline val size: Int get() = this@asMapGeneric.size
    override inline val keys: Set<K> get() = HashSet<K>(size).also { s -> forEach { k, _ -> s.add(k) } }
    override inline val values: Collection<Float> get() = ArrayList<Float>(size).also { l -> forEach { _, v -> l.add(v) } }
    override inline val entries: Set<Map.Entry<K, Float>> get() = HashSet<Map.Entry<K,Float>>(size).also { s -> forEach { k, v -> s.add(java.util.AbstractMap.SimpleImmutableEntry(k, v)) } }
    override inline fun isEmpty(): Boolean = this@asMapGeneric.isEmpty
    override inline fun containsKey(key: K): Boolean = this@asMapGeneric.containsKey(key)
    override inline fun containsValue(value: Float): Boolean = this@asMapGeneric.containsValue(value)
    override inline fun get(key: K): Float? = this@asMapGeneric.getOrNull(key)
}
inline fun <K> MapVLongFloat<K>.valueFromFloat(v: Float): Float = if (v==NULL_VALUE) throw NoSuchElementException() else v
inline fun <K> MapVLongFloat<K>.valueFromFloatOr(v: Float, provider: ()->Float): Float = if (v==NULL_VALUE) provider() else v
inline fun <K> MapVLongFloat<K>.valueFromFloatOrNull(v: Float): Float? = if (v==NULL_VALUE) null else v
context(ka: ValueLongAdapter<K>) inline operator fun <K> MapVLongFloat<K>.get(key: K): Float = valueFromFloat(getBits(ka.toLong(key)))
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.getOr(key: K, defaultResult:()->Float): Float = valueFromFloatOr(getBits(ka.toLong(key)), defaultResult)
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.getOrNull(key: K): Float? = valueFromFloatOrNull(getBits(ka.toLong(key)))
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.any(crossinline predicate:(K, Float)->Boolean):K = keyFromInt(anyBits{ k, v-> predicate(ka.fromLong(k), v)})
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.anyOr(crossinline predicate:(K, Float)->Boolean, defaultResult:()->K):K = keyFromIntOr(anyBits{ k, v-> predicate(ka.fromLong(k), v)}, defaultResult)
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.anyOrNull(crossinline predicate:(K, Float)->Boolean):K? = keyFromIntOrNull(anyBits{ k, v-> predicate(ka.fromLong(k), v)})
inline fun <K> MapVLongFloat<K>.anyIndexedBits(crossinline predicate:(index:Int, LongKeyBits, Float)->Boolean):LongKeyBits {
    return anyBits(object: (LongKeyBits,Float) -> Boolean {
        var index = 0
        override inline fun invoke(k: LongKeyBits, v:Float) = predicate(index++, k,v)
    } )
}
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.anyIndexed(crossinline action:(index:Int, K, Float)->Boolean):K = keyFromInt(anyIndexedBits{ index, k, v-> action(index, ka.fromLong(k), v)})
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.anyIndexedOr(crossinline action:(index:Int, K, Float)->Boolean, defaultResult:()->K):K = keyFromIntOr(anyIndexedBits{ index, k, v-> action(index, ka.fromLong(k), v)}, defaultResult)
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.anyIndexedOrNull(crossinline action:(index:Int, K, Float)->Boolean):K? = keyFromIntOrNull(anyIndexedBits{ index, k, v-> action(index, ka.fromLong(k), v)})
inline fun <K> MapVLongFloat<K>.forEachBits(crossinline action:(LongKeyBits, Float)->Unit) {anyBits { k, v-> action(k,v); false} }
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.forEach(crossinline action:(K, Float)->Unit) = forEachBits { k, v-> action(ka.fromLong(k), v) }
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.forEachPair(crossinline action:(PairVLongObj<K,Float>)->Unit) {
    forEachBits(object: (LongKeyBits, Float) -> Unit {
        var init = false
        lateinit var pair: PairVLongObj<K,Float>
        override inline fun invoke(k: LongKeyBits, v: Float) {
            if (!init) {pair = PairVLongObj(k, v); init = true}
            pair.firstBits = k
            pair.second = v
            action(pair)
        }
    })
}
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.forEachIndexed(crossinline action:(index:Int, K, Float)->Unit) {
    forEachBits(object: (LongKeyBits, Float) -> Unit {
        var index=0
        override inline fun invoke(k: LongKeyBits, v: Float) = action(index++, ka.fromLong(k), v)
    })
}
inline val <K> MapVLongFloat<K>.isEmpty get() = size == 0
inline fun <K> MapVLongFloat<K>.isNotEmpty() = size > 0
context(ka: ValueLongAdapter<K>) inline fun <K, A : Appendable> MapVLongFloat<K>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: ((K, Float) -> CharSequence) = { k, v-> "($k:$v)" }): A {
    val appender = object: (Int,K,Float)-> Boolean {
        var count=0
        override inline fun invoke(index: Int, k:K, v:Float): Boolean {
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
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: ((K, Float) -> CharSequence) = { k, v-> "($k:$v)" }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.toStringV() = joinToString(", ","{","}")


interface MutableMapVLongFloat<K>: MapVLongFloat<K> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim()
    fun clear()

    fun setBits(k: LongKeyBits, v: Float, defaultReturn: Float): Float
    fun getOrPutBits(k: LongKeyBits, defaultSet: () -> Float): Float
    fun removeBits(k: LongKeyBits)
    fun removeBits(k: LongKeyBits, v: Float):Boolean
    fun removeIfBits(predicate:(LongKeyBits,Float)->Boolean)
    context(ka: ValueLongAdapter<K>) override fun toIterable(): Iterable<PairVLongObj<K,Float>>

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
inline fun <K> MutableMapVLongFloat<K>.preallocateFor(newSize: Int) {ensureCapacity(newSize + newSize/4) }
context(ka: ValueLongAdapter<K>) inline operator fun <K> MutableMapVLongFloat<K>.set(key: K, value: Float): Boolean = setBits(ka.toLong(key), value, NULL_VALUE) != NULL_VALUE
context(ka: ValueLongAdapter<K>) inline fun <K> MutableMapVLongFloat<K>.set(key: K, value: Float, defaultReturn: Float): Float = valueFromFloat(setBits(ka.toLong(key), value, defaultReturn))
context(ka: ValueLongAdapter<K>) inline fun <K> MutableMapVLongFloat<K>.getOrPut(key: K, crossinline defaultValue: ()->Float):Float = valueFromFloat(getOrPutBits(ka.toLong(key), {defaultValue()}))
inline fun <K> MutableMapVLongFloat<K>.putAll(source: MapVLongFloat<K>) {preallocateFor(size+source.size + (size+source.size)/4); source.forEachBits { k, v-> setBits(k,v, NULL_VALUE)} }
context(ka: ValueLongAdapter<K>, ska: ValueIntAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,SK,SV> MutableMapVLongFloat<K>.putAll(source: MapVIntInt<SK,SV>, crossinline keySelector: (PairVIntInt<SK,SV>) -> K, crossinline valueTransform: (PairVIntInt<SK,SV>) -> Float) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, sa:ValueIntAdapter<S>) inline fun <K,S> MutableMapVLongFloat<K>.putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> Float) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, sa:ValueIntAdapter<S>) inline fun <K,S> MutableMapVLongFloat<K>.putAll(source: CollectionVInt<S>, crossinline transform: (S) -> PairVLongObj<K, Float>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueLongAdapter<K>, sa:ValueLongAdapter<S>) inline fun <K,S> MutableMapVLongFloat<K>.putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> Float) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, sa:ValueLongAdapter<S>) inline fun <K,S> MutableMapVLongFloat<K>.putAll(source: CollectionVLong<S>, crossinline transform: (S) -> PairVLongObj<K, Float>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueLongAdapter<K>) inline fun <K,S> MutableMapVLongFloat<K>.putAllGeneric(source: Collection<S>, crossinline transform: (S) -> Pair<K, Float>) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
inline infix operator fun <K> MutableMapVLongFloat<K>.plusAssign(source: MapVLongFloat<K>) = putAll(source)
context(ka: ValueLongAdapter<K>) inline fun <K> MutableMapVLongFloat<K>.remove(key: K) = removeBits(ka.toLong(key))
context(ka: ValueLongAdapter<K>) inline fun <K> MutableMapVLongFloat<K>.remove(key: K, value:Float): Boolean = removeBits(ka.toLong(key), value)
context(ka: ValueLongAdapter<K>) inline fun <K> MutableMapVLongFloat<K>.removeIf(crossinline predicate:(K, Float)->Boolean) = removeIfBits { k, v-> predicate(ka.fromLong(k), v)}
context(ka: ValueLongAdapter<K>) inline infix operator fun <K> MutableMapVLongFloat<K>.minusAssign(key: K) {remove(key)}


class HashMapVLongFloat<K>(val collection: MutableLongFloatMap=MutableLongFloatMap(), override val NULL_KEY_BITS: LongKeyBits=Long.MIN_VALUE, override val NULL_VALUE: Float=Float.NEGATIVE_INFINITY)
    : MutableMapVLongFloat<K> {
    constructor(size: Int, NULL_KEY_BITS: LongKeyBits=Long.MIN_VALUE, NULL_VALUE: Float=Float.NEGATIVE_INFINITY ) : this(MutableLongFloatMap(size), NULL_KEY_BITS, NULL_VALUE)

    override val size: Int get() = collection.size
    override inline fun getBits(k: LongKeyBits): Float = collection.getOrDefault(k, NULL_VALUE)
    override inline fun anyBits(predicate: (LongKeyBits, Float) -> Boolean): LongKeyBits {
        val finder = object : (LongKeyBits, Float) -> Unit {
            var result: LongKeyBits = NULL_KEY_BITS
            override inline fun invoke(k: LongKeyBits, v: Float) { if (result == NULL_KEY_BITS && predicate(k, v)) result = k }
        }
        collection.forEach(finder)
        return finder.result
    }
    override inline fun trim() { collection.trim() }
    override inline fun clear() = collection.clear()
    override inline fun setBits(k: LongKeyBits, v: Float, defaultReturn: Float): Float = collection.put(k,v, defaultReturn)
    override inline fun getOrPutBits(k: LongKeyBits, defaultSet: () -> Float): Float = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: LongKeyBits) = collection.remove(k)
    override inline fun removeBits(k: LongKeyBits, v:Float): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (LongKeyBits, Float) -> Boolean) = collection.removeIf(predicate)

    override inline fun anyKeyBits(predicate: (LongKeyBits) -> Boolean): LongKeyBits = anyBits { k, _ -> predicate(k) }
    override inline fun removeKeyBits(bits: LongKeyBits): Boolean { val had = collection.containsKey(bits); removeBits(bits); return had }
    // Thin wrappers for every public method of MutableLongFloatMap.
    inline fun capacity(): Int = collection.capacity
    inline fun none(): Boolean = collection.none()
    inline fun isEmpty(): Boolean = collection.isEmpty()
    inline fun isNotEmptyBits(): Boolean = collection.isNotEmpty()
    inline fun getOrDefault(key: LongKeyBits, defaultValue: Float): Float = collection.getOrDefault(key, defaultValue)
    inline fun getOrElse(key: LongKeyBits, defaultValue: () -> Float): Float = collection.getOrElse(key, defaultValue)
    inline fun forEachBits(block: (key: LongKeyBits, value: Float) -> Unit) = collection.forEach(block)
    inline fun forEachKey(block: (key: LongKeyBits) -> Unit) = collection.forEachKey(block)
    override inline fun forEachValue(block: (value: Float) -> Unit) = collection.forEachValue(block)
    inline fun all(predicate: (LongKeyBits, Float) -> Boolean): Boolean = collection.all(predicate)
    inline fun count(): Int = collection.count()
    inline fun count(predicate: (LongKeyBits, Float) -> Boolean): Int = collection.count(predicate)
    inline operator fun contains(key: LongKeyBits): Boolean = collection.contains(key)
    override inline fun containsKeyBits(bits: LongKeyBits): Boolean = collection.containsKey(bits)
    inline fun containsValueBits(value: Float): Boolean = collection.containsValue(value)
    override inline fun containsValue(value: Float): Boolean = containsValueBits(value)
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
        crossinline transform: (key: LongKeyBits, value: Float) -> CharSequence,
    ): String = collection.joinToString(separator, prefix, postfix, limit, truncated, transform)
    inline fun setBits(key: LongKeyBits, value: Float) = collection.set(key, value)
    inline fun put(key: LongKeyBits, value: Float) = collection.put(key, value)
    inline fun put(key: LongKeyBits, value: Float, default: Float): Float = collection.put(key, value, default)
    inline fun putAllBits(from: LongFloatMap) = collection.putAll(from)
    inline fun plusAssignBits(from: LongFloatMap) = collection.plusAssign(from)
    inline fun minusAssignBits(key: LongKeyBits) = collection.minusAssign(key)
    inline fun minusAssignBits(keys: LongArray) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: LongSet) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: LongList) = collection.minusAssign(keys)

    context(ka: ValueLongAdapter<K>) override inline fun toIterable(): Iterable<PairVLongObj<K,Float>> {
        val list = ArrayList<PairVLongObj<K,Float>>(size)
        collection.forEach { k, v -> list.add(PairVLongObj(k, v)) }
        return object : Iterable<PairVLongObj<K,Float>> {
            override inline fun iterator(): Iterator<PairVLongObj<K,Float>> = object : Iterator<PairVLongObj<K,Float>> {
                var idx = 0
                override inline fun hasNext(): Boolean = idx < list.size
                override inline fun next(): PairVLongObj<K,Float> { val p = list[idx++]; return p }
            }
        }
    }

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = collection.toString()
}
