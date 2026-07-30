@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package com.mpd.common.collect.valuecollections

import androidx.collection.FloatLongMap
import androidx.collection.FloatList
import androidx.collection.FloatSet
import androidx.collection.MutableFloatLongMap

interface MapVFloatLong<V> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_VALUE_BITS: LongValueBits

    val size: Int
    fun getBits(k: Float): LongValueBits
    fun anyBits(predicate: (Float, LongValueBits) -> Boolean): Float?

    context(va: ValueLongAdapter<V>) fun asIterable(): Iterable<PairVObjLong<Float,V>>

    @JvmName("toStringV") @Suppress("INAPPLICABLE_JVM_NAME")
    context(va: ValueLongAdapter<V>) fun toString(): String = toStringV()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print V.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT V.toString()!
}
context(va: ValueLongAdapter<V>)  inline fun <V> MapVFloatLong<V>.asMapGeneric(): Map<Float,V> = object: Map<Float,V> {
    override inline val size: Int get() = this@asMapGeneric.size
    override inline val keys: Set<Float> get() = asIterable().mapTo(HashSet()) {e->e.key}
    override inline val values: Collection<V> get() = asIterable().mapTo(HashSet()) {e->e.value}
    override inline val entries: Set<Map.Entry<Float, V>> get() = asIterable().mapTo(HashSet()) {e->object:Map.Entry<Float,V>{
        override val key: Float get() = e.key
        override val value: V get() = e.value}
    }
    override inline fun isEmpty(): Boolean = this@asMapGeneric.isEmpty
    override inline fun containsKey(key: Float): Boolean = this@asMapGeneric.containsKey(key)
    override inline fun containsValue(value: V): Boolean = this@asMapGeneric.containsValue(value)
    override inline fun get(key: Float): V? = this@asMapGeneric.getOrNull(key)
}
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.valueFromInt(bits: LongValueBits): V = if (bits==NULL_VALUE_BITS) throw NoSuchElementException() else va.fromLong(bits)
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.valueFromIntOr(bits: LongValueBits, provider: ()->V): V = if (bits==NULL_VALUE_BITS) provider() else va.fromLong(bits)
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.valueFromIntOrNull(bits: LongValueBits): V? = if (bits==NULL_VALUE_BITS) null else va.fromLong(bits)
context(va: ValueLongAdapter<V>) inline operator fun <V> MapVFloatLong<V>.get(key: Float): V = valueFromInt(getBits(key))
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.getOr(key: Float, defaultResult:()->V): V = valueFromIntOr(getBits(key), defaultResult)
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.getOrNull(key: Float): V? = valueFromIntOrNull(getBits(key))
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.any(crossinline predicate:(Float, V)->Boolean):Float? = anyBits{ k, v-> predicate(k, va.fromLong(v))}
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.anyOr(crossinline predicate:(Float, V)->Boolean, defaultResult:()->Float):Float = anyBits{ k, v-> predicate(k, va.fromLong(v))} ?: defaultResult()
inline fun <V> MapVFloatLong<V>.anyIndexedBits(crossinline predicate:(index:Int, Float, LongValueBits)->Boolean):Float? {
    return anyBits(object: (Float,LongValueBits) -> Boolean {
        var index = 0
        override inline fun invoke(k: Float, v:LongValueBits) = predicate(index++, k,v)
    } )
}
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.anyIndexed(crossinline action:(index:Int, Float, V)->Boolean):Float? = anyIndexedBits{ index, k, v-> action(index, k, va.fromLong(v))}
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.anyIndexedOr(crossinline action:(index:Int, Float, V)->Boolean, defaultResult:()->Float):Float = anyIndexedBits{ index, k, v-> action(index, k, va.fromLong(v))} ?: defaultResult()
inline fun <V> MapVFloatLong<V>.forEachBits(crossinline action:(Float, LongValueBits)->Unit) {anyBits { k, v-> action(k,v); false} }
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.forEach(crossinline action:(Float, V)->Unit) = forEachBits { k, v-> action(k, va.fromLong(v)) }
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.forEachPair(crossinline action:(PairVObjLong<Float,V>)->Unit) {
    forEachBits(object: (Float, LongValueBits) -> Unit {
        var init = false
        lateinit var pair: PairVObjLong<Float,V>
        override inline fun invoke(k: Float, v: LongValueBits) {
            if (!init) {pair = PairVObjLong(k, v); init = true}
            pair.first = k
            pair.secondBits = v
            action(pair)
        }
    })
}
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.forEachIndexed(crossinline action:(index:Int, Float, V)->Unit) {
    forEachBits(object: (Float, LongValueBits) -> Unit {
        var index=0
        override inline fun invoke(k: Float, v: LongValueBits) = action(index++, k, va.fromLong(v))
    })
}
inline val <V> MapVFloatLong<V>.isEmpty get() = size == 0
inline fun <V> MapVFloatLong<V>.isNotEmpty() = size > 0
inline fun <V> MapVFloatLong<V>.containsKey(k: Float) = getBits(k) != NULL_VALUE_BITS
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.containsValue(findV: V) = anyBits { _, v-> v==va.toLong(findV)} != null
context(va: ValueLongAdapter<V>) inline fun <V, A : Appendable> MapVFloatLong<V>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: ((Float, V) -> CharSequence) = { k, v-> "($k:$v)" }): A {
    val appender = object: (Int,Float,V)-> Boolean {
        var count=0
        override inline fun invoke(index: Int, k:Float, v:V): Boolean {
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
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: ((Float, V) -> CharSequence) = { k, v-> "($k:$v)" }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(va: ValueLongAdapter<V>) inline fun <V> MapVFloatLong<V>.toStringV() = joinToString(", ","{","}")



interface MutableMapVFloatLong<V>: MapVFloatLong<V> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim()
    fun clear()

    fun setBits(k: Float, v: LongValueBits, defaultReturn: LongValueBits): LongValueBits
    fun getOrPutBits(k: Float, defaultSet: () -> LongValueBits): LongValueBits
    fun removeBits(k: Float)
    fun removeBits(k: Float, v: LongValueBits):Boolean
    fun removeIfBits(predicate:(Float,LongValueBits)->Boolean)
    context(va: ValueLongAdapter<V>) override fun asIterable(): MutableIterable<PairVObjLong<Float,V>>

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print V.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT V.toString()!
}
inline fun <V> MutableMapVFloatLong<V>.preallocateFor(newSize: Int) {ensureCapacity(newSize + newSize/4) }
context(va: ValueLongAdapter<V>) inline operator fun <V> MutableMapVFloatLong<V>.set(key: Float, value: V): Boolean = setBits(key, va.toLong(value), NULL_VALUE_BITS) != NULL_VALUE_BITS
context(va: ValueLongAdapter<V>) inline fun <V> MutableMapVFloatLong<V>.set(key: Float, value: V, defaultReturn: V): V = valueFromInt(setBits(key, va.toLong(value), va.toLong(defaultReturn)))
context(va: ValueLongAdapter<V>) inline fun <V> MutableMapVFloatLong<V>.getOrPut(key: Float, crossinline defaultValue: ()->V):V = valueFromInt(getOrPutBits(key, {va.toLong(defaultValue())}))
inline fun <V> MutableMapVFloatLong<V>.putAll(source: MapVFloatLong<V>) {preallocateFor(size+source.size + (size+source.size)/4); source.forEachBits { k, v-> setBits(k,v, NULL_VALUE_BITS)} }
context(va: ValueLongAdapter<V>, ska: ValueIntAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <V,SK,SV> MutableMapVFloatLong<V>.putAll(source: MapVIntInt<SK,SV>, crossinline keySelector: (PairVIntInt<SK,SV>) -> Float, crossinline valueTransform: (PairVIntInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <V,S> MutableMapVFloatLong<V>.putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> Float, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <V,S> MutableMapVFloatLong<V>.putAll(source: CollectionVInt<S>, crossinline transform: (S) -> PairVObjLong<Float, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <V,S> MutableMapVFloatLong<V>.putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> Float, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <V,S> MutableMapVFloatLong<V>.putAll(source: CollectionVLong<S>, crossinline transform: (S) -> PairVObjLong<Float, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(va: ValueLongAdapter<V>) inline fun <V,S> MutableMapVFloatLong<V>.putAllGeneric(source: Collection<S>, crossinline transform: (S) -> Pair<Float, V>) { preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
inline infix operator fun <V> MutableMapVFloatLong<V>.plusAssign(source: MapVFloatLong<V>) = putAll(source)
inline fun <V> MutableMapVFloatLong<V>.remove(key: Float) = removeBits(key)
context(va: ValueLongAdapter<V>) inline fun <V> MutableMapVFloatLong<V>.remove(key: Float, value:V): Boolean = removeBits(key, va.toLong(value))
context(va: ValueLongAdapter<V>) inline fun <V> MutableMapVFloatLong<V>.removeIf(crossinline predicate:(Float, V)->Boolean) = removeIfBits { k, v-> predicate(k, va.fromLong(v))}
inline infix operator fun <V> MutableMapVFloatLong<V>.minusAssign(key: Float) {remove(key)}




class HashMapVFloatLong<V>(val collection: MutableFloatLongMap =MutableFloatLongMap(), override val NULL_VALUE_BITS: LongValueBits=Long.MIN_VALUE)
    : MutableMapVFloatLong<V> {
    constructor(size: Int, NO_VALUE: LongValueBits=Long.MIN_VALUE) : this(MutableFloatLongMap(size), NO_VALUE)

    override val size: Int get() = collection.size
    override inline fun getBits(k: Float): LongValueBits = collection.getOrDefault(k, NULL_VALUE_BITS)
    override inline fun anyBits(predicate: (Float, LongValueBits) -> Boolean): Float? {
        var result: Float? = null
        var found = false
        collection.forEach { k, v -> if (!found && predicate(k, v)) { result = k; found = true } }
        return result
    }
    override inline fun trim() { collection.trim() }
    override inline fun clear() = collection.clear()
    override inline fun setBits(k: Float, v: LongValueBits, defaultReturn: LongValueBits): LongValueBits = collection.put(k,v, defaultReturn)
    override inline fun getOrPutBits(k: Float, defaultSet: () -> LongValueBits): LongValueBits = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: Float) = collection.remove(k)
    override inline fun removeBits(k: Float, v:LongValueBits): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (Float, LongValueBits) -> Boolean) = collection.removeIf(predicate)

    // Thin wrappers for every public method of MutableFloatLongMap.
    inline fun capacity(): Int = collection.capacity
    inline fun anyBits(): Boolean = collection.any()
    inline fun none(): Boolean = collection.none()
    inline fun isEmpty(): Boolean = collection.isEmpty()
    inline fun isNotEmptyBits(): Boolean = collection.isNotEmpty()
    inline fun getOrDefault(key: Float, defaultValue: LongValueBits): LongValueBits = collection.getOrDefault(key, defaultValue)
    inline fun getOrElse(key: Float, defaultValue: () -> LongValueBits): LongValueBits = collection.getOrElse(key, defaultValue)
    inline fun forEachBits(block: (key: Float, value: LongValueBits) -> Unit) = collection.forEach(block)
    inline fun forEachKey(block: (key: Float) -> Unit) = collection.forEachKey(block)
    inline fun forEachValue(block: (value: LongValueBits) -> Unit) = collection.forEachValue(block)
    inline fun all(predicate: (Float, LongValueBits) -> Boolean): Boolean = collection.all(predicate)
    inline fun count(): Int = collection.count()
    inline fun count(predicate: (Float, LongValueBits) -> Boolean): Int = collection.count(predicate)
    inline operator fun contains(key: Float): Boolean = collection.contains(key)
    inline fun containsKeyBits(key: Float): Boolean = collection.containsKey(key)
    inline fun containsValueBits(value: LongValueBits): Boolean = collection.containsValue(value)
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
        crossinline transform: (key: Float, value: LongValueBits) -> CharSequence,
    ): String = collection.joinToString(separator, prefix, postfix, limit, truncated, transform)
    inline fun setBits(key: Float, value: LongValueBits) = collection.set(key, value)
    inline fun put(key: Float, value: LongValueBits) = collection.put(key, value)
    inline fun put(key: Float, value: LongValueBits, default: LongValueBits): LongValueBits = collection.put(key, value, default)
    inline fun putAllBits(from: FloatLongMap) = collection.putAll(from)
    inline fun plusAssignBits(from: FloatLongMap) = collection.plusAssign(from)
    inline fun minusAssignBits(key: Float) = collection.minusAssign(key)
    inline fun minusAssignBits(keys: FloatArray) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: FloatSet) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: FloatList) = collection.minusAssign(keys)

    context(va: ValueLongAdapter<V>) override inline fun asIterable(): MutableIterable<PairVObjLong<Float,V>> = throw NotImplementedError()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print V.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = collection.toString()
}
