@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package com.mpd.common.collect.valuecollections

import androidx.collection.IntFloatMap
import androidx.collection.IntList
import androidx.collection.IntSet
import androidx.collection.MutableIntFloatMap

interface MapVIntFloat<K>: MapVIntKey<K>, MapVFloatValue {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_VALUE: Float

    override val size: Int
    fun getBits(k: IntKeyBits): Float
    fun anyBits(predicate: (IntKeyBits, Float) -> Boolean): IntKeyBits

    context(ka: ValueIntAdapter<K>) fun toIterable(): Iterable<PairVIntObj<K,Float>>

    @JvmName("toStringV") @Suppress("INAPPLICABLE_JVM_NAME")
    context(ka: ValueIntAdapter<K>) fun toString(): String = toStringV()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(ka: ValueIntAdapter<K>)  inline fun <K> MapVIntFloat<K>.asMapGeneric(): Map<K,Float> = object: Map<K,Float> {
    override inline val size: Int get() = this@asMapGeneric.size
    override inline val keys: Set<K> get() = HashSet<K>(size).also { s -> forEach { k, _ -> s.add(k) } }
    override inline val values: Collection<Float> get() = ArrayList<Float>(size).also { l -> forEach { _, v -> l.add(v) } }
    override inline val entries: Set<Map.Entry<K, Float>> get() = HashSet<Map.Entry<K,Float>>(size).also { s -> forEach { k, v -> s.add(java.util.AbstractMap.SimpleImmutableEntry(k, v)) } }
    override inline fun isEmpty(): Boolean = this@asMapGeneric.isEmpty
    override inline fun containsKey(key: K): Boolean = this@asMapGeneric.containsKey(key)
    override inline fun containsValue(value: Float): Boolean = this@asMapGeneric.containsValue(value)
    override inline fun get(key: K): Float? = this@asMapGeneric.getOrNull(key)
}
inline fun <K> MapVIntFloat<K>.valueFromFloat(v: Float): Float = if (v==NULL_VALUE) throw NoSuchElementException() else v
inline fun <K> MapVIntFloat<K>.valueFromFloatOr(v: Float, provider: ()->Float): Float = if (v==NULL_VALUE) provider() else v
inline fun <K> MapVIntFloat<K>.valueFromFloatOrNull(v: Float): Float? = if (v==NULL_VALUE) null else v
context(ka: ValueIntAdapter<K>) inline operator fun <K> MapVIntFloat<K>.get(key: K): Float = valueFromFloat(getBits(ka.toInt(key)))
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.getOr(key: K, defaultResult:()->Float): Float = valueFromFloatOr(getBits(ka.toInt(key)), defaultResult)
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.getOrNull(key: K): Float? = valueFromFloatOrNull(getBits(ka.toInt(key)))
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.any(crossinline predicate:(K, Float)->Boolean):K = keyFromInt(anyBits{ k, v-> predicate(ka.fromInt(k), v)})
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.anyOr(crossinline predicate:(K, Float)->Boolean, defaultResult:()->K):K = keyFromIntOr(anyBits{ k, v-> predicate(ka.fromInt(k), v)}, defaultResult)
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.anyOrNull(crossinline predicate:(K, Float)->Boolean):K? = keyFromIntOrNull(anyBits{ k, v-> predicate(ka.fromInt(k), v)})
inline fun <K> MapVIntFloat<K>.anyIndexedBits(crossinline predicate:(index:Int, IntKeyBits, Float)->Boolean):IntKeyBits {
    return anyBits(object: (IntKeyBits,Float) -> Boolean {
        var index = 0
        override inline fun invoke(k: IntKeyBits, v:Float) = predicate(index++, k,v)
    } )
}
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.anyIndexed(crossinline action:(index:Int, K, Float)->Boolean):K = keyFromInt(anyIndexedBits{ index, k, v-> action(index, ka.fromInt(k), v)})
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.anyIndexedOr(crossinline action:(index:Int, K, Float)->Boolean, defaultResult:()->K):K = keyFromIntOr(anyIndexedBits{ index, k, v-> action(index, ka.fromInt(k), v)}, defaultResult)
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.anyIndexedOrNull(crossinline action:(index:Int, K, Float)->Boolean):K? = keyFromIntOrNull(anyIndexedBits{ index, k, v-> action(index, ka.fromInt(k), v)})
inline fun <K> MapVIntFloat<K>.forEachBits(crossinline action:(IntKeyBits, Float)->Unit) {anyBits { k, v-> action(k,v); false} }
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.forEach(crossinline action:(K, Float)->Unit) = forEachBits { k, v-> action(ka.fromInt(k), v) }
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.forEachPair(crossinline action:(PairVIntObj<K,Float>)->Unit) {
    forEachBits(object: (IntKeyBits, Float) -> Unit {
        var init = false
        lateinit var pair: PairVIntObj<K,Float>
        override inline fun invoke(k: IntKeyBits, v: Float) {
            if (!init) {pair = PairVIntObj(k, v); init = true}
            pair.firstBits = k
            pair.second = v
            action(pair)
        }
    })
}
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.forEachIndexed(crossinline action:(index:Int, K, Float)->Unit) {
    forEachBits(object: (IntKeyBits, Float) -> Unit {
        var index=0
        override inline fun invoke(k: IntKeyBits, v: Float) = action(index++, ka.fromInt(k), v)
    })
}
inline val <K> MapVIntFloat<K>.isEmpty get() = size == 0
inline fun <K> MapVIntFloat<K>.isNotEmpty() = size > 0
context(ka: ValueIntAdapter<K>) inline fun <K, A : Appendable> MapVIntFloat<K>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: ((K, Float) -> CharSequence) = { k, v-> "($k:$v)" }): A {
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
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: ((K, Float) -> CharSequence) = { k, v-> "($k:$v)" }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.toStringV() = joinToString(", ","{","}")


interface MutableMapVIntFloat<K>: MapVIntFloat<K> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim()
    fun clear()

    fun setBits(k: IntKeyBits, v: Float, defaultReturn: Float): Float
    fun getOrPutBits(k: IntKeyBits, defaultSet: () -> Float): Float
    fun removeBits(k: IntKeyBits)
    fun removeBits(k: IntKeyBits, v: Float):Boolean
    fun removeIfBits(predicate:(IntKeyBits,Float)->Boolean)
    context(ka: ValueIntAdapter<K>) override fun toIterable(): Iterable<PairVIntObj<K,Float>>

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
inline fun <K> MutableMapVIntFloat<K>.preallocateFor(newSize: Int) {ensureCapacity(newSize + newSize/4) }
context(ka: ValueIntAdapter<K>) inline operator fun <K> MutableMapVIntFloat<K>.set(key: K, value: Float): Boolean = setBits(ka.toInt(key), value, NULL_VALUE) != NULL_VALUE
context(ka: ValueIntAdapter<K>) inline fun <K> MutableMapVIntFloat<K>.set(key: K, value: Float, defaultReturn: Float): Float = valueFromFloat(setBits(ka.toInt(key), value, defaultReturn))
context(ka: ValueIntAdapter<K>) inline fun <K> MutableMapVIntFloat<K>.getOrPut(key: K, crossinline defaultValue: ()->Float):Float = valueFromFloat(getOrPutBits(ka.toInt(key), {defaultValue()}))
inline fun <K> MutableMapVIntFloat<K>.putAll(source: MapVIntFloat<K>) {preallocateFor(size+source.size + (size+source.size)/4); source.forEachBits { k, v-> setBits(k,v, NULL_VALUE)} }
context(ka: ValueIntAdapter<K>, ska: ValueIntAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,SK,SV> MutableMapVIntFloat<K>.putAll(source: MapVIntInt<SK,SV>, crossinline keySelector: (PairVIntInt<SK,SV>) -> K, crossinline valueTransform: (PairVIntInt<SK,SV>) -> Float) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, sa:ValueIntAdapter<S>) inline fun <K,S> MutableMapVIntFloat<K>.putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> Float) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, sa:ValueIntAdapter<S>) inline fun <K,S> MutableMapVIntFloat<K>.putAll(source: CollectionVInt<S>, crossinline transform: (S) -> PairVIntObj<K, Float>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueIntAdapter<K>, sa:ValueLongAdapter<S>) inline fun <K,S> MutableMapVIntFloat<K>.putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> Float) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, sa:ValueLongAdapter<S>) inline fun <K,S> MutableMapVIntFloat<K>.putAll(source: CollectionVLong<S>, crossinline transform: (S) -> PairVIntObj<K, Float>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueIntAdapter<K>) inline fun <K,S> MutableMapVIntFloat<K>.putAllGeneric(source: Collection<S>, crossinline transform: (S) -> Pair<K, Float>) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
inline infix operator fun <K> MutableMapVIntFloat<K>.plusAssign(source: MapVIntFloat<K>) = putAll(source)
context(ka: ValueIntAdapter<K>) inline fun <K> MutableMapVIntFloat<K>.remove(key: K) = removeBits(ka.toInt(key))
context(ka: ValueIntAdapter<K>) inline fun <K> MutableMapVIntFloat<K>.remove(key: K, value:Float): Boolean = removeBits(ka.toInt(key), value)
context(ka: ValueIntAdapter<K>) inline fun <K> MutableMapVIntFloat<K>.removeIf(crossinline predicate:(K, Float)->Boolean) = removeIfBits { k, v-> predicate(ka.fromInt(k), v)}
context(ka: ValueIntAdapter<K>) inline infix operator fun <K> MutableMapVIntFloat<K>.minusAssign(key: K) {remove(key)}


class HashMapVIntFloat<K>(val collection: MutableIntFloatMap=MutableIntFloatMap(), override val NULL_KEY_BITS: IntKeyBits=Int.MIN_VALUE, override val NULL_VALUE: Float=Float.NEGATIVE_INFINITY)
    : MutableMapVIntFloat<K> {
    constructor(size: Int, NULL_KEY_BITS: IntKeyBits=Int.MIN_VALUE, NULL_VALUE: Float=Float.NEGATIVE_INFINITY ) : this(MutableIntFloatMap(size), NULL_KEY_BITS, NULL_VALUE)

    override val size: Int get() = collection.size
    override inline fun getBits(k: IntKeyBits): Float = collection.getOrDefault(k, NULL_VALUE)
    override inline fun anyBits(predicate: (IntKeyBits, Float) -> Boolean): IntKeyBits {
        val finder = object : (IntKeyBits, Float) -> Unit {
            var result: IntKeyBits = NULL_KEY_BITS
            override inline fun invoke(k: IntKeyBits, v: Float) { if (result == NULL_KEY_BITS && predicate(k, v)) result = k }
        }
        collection.forEach(finder)
        return finder.result
    }
    override inline fun trim() { collection.trim() }
    override inline fun clear() = collection.clear()
    override inline fun setBits(k: IntKeyBits, v: Float, defaultReturn: Float): Float = collection.put(k,v, defaultReturn)
    override inline fun getOrPutBits(k: IntKeyBits, defaultSet: () -> Float): Float = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: IntKeyBits) = collection.remove(k)
    override inline fun removeBits(k: IntKeyBits, v:Float): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (IntKeyBits, Float) -> Boolean) = collection.removeIf(predicate)

    override inline fun anyKeyBits(predicate: (IntKeyBits) -> Boolean): IntKeyBits = anyBits { k, _ -> predicate(k) }
    override inline fun removeKeyBits(bits: IntKeyBits): Boolean { val had = collection.containsKey(bits); removeBits(bits); return had }
    // Thin wrappers for every public method of MutableIntFloatMap.
    inline fun capacity(): Int = collection.capacity
    inline fun none(): Boolean = collection.none()
    inline fun isEmpty(): Boolean = collection.isEmpty()
    inline fun isNotEmptyBits(): Boolean = collection.isNotEmpty()
    inline fun getOrDefault(key: IntKeyBits, defaultValue: Float): Float = collection.getOrDefault(key, defaultValue)
    inline fun getOrElse(key: IntKeyBits, defaultValue: () -> Float): Float = collection.getOrElse(key, defaultValue)
    inline fun forEachBits(block: (key: IntKeyBits, value: Float) -> Unit) = collection.forEach(block)
    inline fun forEachKey(block: (key: IntKeyBits) -> Unit) = collection.forEachKey(block)
    override inline fun forEachValue(block: (value: Float) -> Unit) = collection.forEachValue(block)
    inline fun all(predicate: (IntKeyBits, Float) -> Boolean): Boolean = collection.all(predicate)
    inline fun count(): Int = collection.count()
    inline fun count(predicate: (IntKeyBits, Float) -> Boolean): Int = collection.count(predicate)
    inline operator fun contains(key: IntKeyBits): Boolean = collection.contains(key)
    override inline fun containsKeyBits(bits: IntKeyBits): Boolean = collection.containsKey(bits)
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
        crossinline transform: (key: IntKeyBits, value: Float) -> CharSequence,
    ): String = collection.joinToString(separator, prefix, postfix, limit, truncated, transform)
    inline fun setBits(key: IntKeyBits, value: Float) = collection.set(key, value)
    inline fun put(key: IntKeyBits, value: Float) = collection.put(key, value)
    inline fun put(key: IntKeyBits, value: Float, default: Float): Float = collection.put(key, value, default)
    inline fun putAllBits(from: IntFloatMap) = collection.putAll(from)
    inline fun plusAssignBits(from: IntFloatMap) = collection.plusAssign(from)
    inline fun minusAssignBits(key: IntKeyBits) = collection.minusAssign(key)
    inline fun minusAssignBits(keys: IntArray) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: IntSet) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: IntList) = collection.minusAssign(keys)

    context(ka: ValueIntAdapter<K>) override inline fun toIterable(): Iterable<PairVIntObj<K,Float>> {
        val list = ArrayList<PairVIntObj<K,Float>>(size)
        collection.forEach { k, v -> list.add(PairVIntObj(k, v)) }
        return object : Iterable<PairVIntObj<K,Float>> {
            override inline fun iterator(): Iterator<PairVIntObj<K,Float>> = object : Iterator<PairVIntObj<K,Float>> {
                var idx = 0
                override inline fun hasNext(): Boolean = idx < list.size
                override inline fun next(): PairVIntObj<K,Float> { val p = list[idx++]; return p }
            }
        }
    }

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = collection.toString()
}
