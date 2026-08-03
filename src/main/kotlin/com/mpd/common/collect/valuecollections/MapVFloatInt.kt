@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package com.mpd.common.collect.valuecollections

import androidx.collection.FloatIntMap
import androidx.collection.FloatList
import androidx.collection.FloatSet
import androidx.collection.MutableFloatIntMap

interface MapVFloatInt<V>: MapVFloatKey, MapVIntValue<V> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.

    override val size: Int
    fun getBits(k: Float): IntValueBits
    fun anyBits(predicate: (Float, IntValueBits) -> Boolean): Float?

    context(va: ValueIntAdapter<V>) fun toIterable(): Iterable<PairVObjInt<Float,V>>

    @JvmName("toStringV") @Suppress("INAPPLICABLE_JVM_NAME")
    context(va: ValueIntAdapter<V>) fun toString(): String = toStringV()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print V.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT V.toString()!
}
context(va: ValueIntAdapter<V>)  inline fun <V> MapVFloatInt<V>.asMapGeneric(): Map<Float,V> = object: Map<Float,V> {
    override inline val size: Int get() = this@asMapGeneric.size
    override inline val keys: Set<Float> get() = HashSet<Float>(size).also { s -> forEach { k, _ -> s.add(k) } }
    override inline val values: Collection<V> get() = ArrayList<V>(size).also { l -> forEach { _, v -> l.add(v) } }
    override inline val entries: Set<Map.Entry<Float, V>> get() = HashSet<Map.Entry<Float,V>>(size).also { s -> forEach { k, v -> s.add(java.util.AbstractMap.SimpleImmutableEntry(k, v)) } }
    override inline fun isEmpty(): Boolean = this@asMapGeneric.isEmpty
    override inline fun containsKey(key: Float): Boolean = this@asMapGeneric.containsKey(key)
    override inline fun containsValue(value: V): Boolean = this@asMapGeneric.containsValue(value)
    override inline fun get(key: Float): V? = this@asMapGeneric.getOrNull(key)
}
context(va: ValueIntAdapter<V>) inline operator fun <V> MapVFloatInt<V>.get(key: Float): V = valueFromInt(getBits(key))
context(va: ValueIntAdapter<V>) inline fun <V> MapVFloatInt<V>.getOr(key: Float, defaultResult:()->V): V = valueFromIntOr(getBits(key), defaultResult)
context(va: ValueIntAdapter<V>) inline fun <V> MapVFloatInt<V>.getOrNull(key: Float): V? = valueFromIntOrNull(getBits(key))
context(va: ValueIntAdapter<V>) inline fun <V> MapVFloatInt<V>.any(crossinline predicate:(Float, V)->Boolean):Float? = anyBits{ k, v-> predicate(k, va.fromInt(v))}
context(va: ValueIntAdapter<V>) inline fun <V> MapVFloatInt<V>.anyOr(crossinline predicate:(Float, V)->Boolean, defaultResult:()->Float):Float = anyBits{ k, v-> predicate(k, va.fromInt(v))} ?: defaultResult()
inline fun <V> MapVFloatInt<V>.anyIndexedBits(crossinline predicate:(index:Int, Float, IntValueBits)->Boolean):Float? {
    return anyBits(object: (Float,IntValueBits) -> Boolean {
        var index = 0
        override inline fun invoke(k: Float, v:IntValueBits) = predicate(index++, k,v)
    } )
}
context(va: ValueIntAdapter<V>) inline fun <V> MapVFloatInt<V>.anyIndexed(crossinline action:(index:Int, Float, V)->Boolean):Float? = anyIndexedBits{ index, k, v-> action(index, k, va.fromInt(v))}
context(va: ValueIntAdapter<V>) inline fun <V> MapVFloatInt<V>.anyIndexedOr(crossinline action:(index:Int, Float, V)->Boolean, defaultResult:()->Float):Float = anyIndexedBits{ index, k, v-> action(index, k, va.fromInt(v))} ?: defaultResult()
inline fun <V> MapVFloatInt<V>.forEachBits(crossinline action:(Float, IntValueBits)->Unit) {anyBits { k, v-> action(k,v); false} }
context(va: ValueIntAdapter<V>) inline fun <V> MapVFloatInt<V>.forEach(crossinline action:(Float, V)->Unit) = forEachBits { k, v-> action(k, va.fromInt(v)) }
context(va: ValueIntAdapter<V>) inline fun <V> MapVFloatInt<V>.forEachPair(crossinline action:(PairVObjInt<Float,V>)->Unit) {
    forEachBits(object: (Float, IntValueBits) -> Unit {
        var init = false
        lateinit var pair: PairVObjInt<Float,V>
        override inline fun invoke(k: Float, v: IntValueBits) {
            if (!init) {pair = PairVObjInt(k, v); init = true}
            pair.first = k
            pair.secondBits = v
            action(pair)
        }
    })
}
context(va: ValueIntAdapter<V>) inline fun <V> MapVFloatInt<V>.forEachIndexed(crossinline action:(index:Int, Float, V)->Unit) {
    forEachBits(object: (Float, IntValueBits) -> Unit {
        var index=0
        override inline fun invoke(k: Float, v: IntValueBits) = action(index++, k, va.fromInt(v))
    })
}
inline val <V> MapVFloatInt<V>.isEmpty get() = size == 0
inline fun <V> MapVFloatInt<V>.isNotEmpty() = size > 0
context(va: ValueIntAdapter<V>) inline fun <V, A : Appendable> MapVFloatInt<V>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: ((Float, V) -> CharSequence) = { k, v-> "($k:$v)" }): A {
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
context(va: ValueIntAdapter<V>) inline fun <V> MapVFloatInt<V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = size, truncated: CharSequence = "...", crossinline transform: ((Float, V) -> CharSequence) = { k, v-> "($k:$v)" }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(va: ValueIntAdapter<V>) inline fun <V> MapVFloatInt<V>.toStringV() = joinToString(", ","{","}")



interface MutableMapVFloatInt<V>: MapVFloatInt<V> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim()
    fun clear()

    fun setBits(k: Float, v: IntValueBits, defaultReturn: IntValueBits): IntValueBits
    fun getOrPutBits(k: Float, defaultSet: () -> IntValueBits): IntValueBits
    fun removeBits(k: Float)
    fun removeBits(k: Float, v: IntValueBits):Boolean
    fun removeIfBits(predicate:(Float,IntValueBits)->Boolean)
    context(va: ValueIntAdapter<V>) override fun toIterable(): Iterable<PairVObjInt<Float,V>>

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print V.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT V.toString()!
}
inline fun <V> MutableMapVFloatInt<V>.preallocateFor(newSize: Int) {ensureCapacity(newSize + newSize/4) }
context(va: ValueIntAdapter<V>) inline operator fun <V> MutableMapVFloatInt<V>.set(key: Float, value: V): Boolean = setBits(key, va.toInt(value), NULL_VALUE_BITS) != NULL_VALUE_BITS
context(va: ValueIntAdapter<V>) inline fun <V> MutableMapVFloatInt<V>.set(key: Float, value: V, defaultReturn: V): V = valueFromInt(setBits(key, va.toInt(value), va.toInt(defaultReturn)))
context(va: ValueIntAdapter<V>) inline fun <V> MutableMapVFloatInt<V>.getOrPut(key: Float, crossinline defaultValue: ()->V):V = valueFromInt(getOrPutBits(key, {va.toInt(defaultValue())}))
inline fun <V> MutableMapVFloatInt<V>.putAll(source: MapVFloatInt<V>) {preallocateFor(size+source.size + (size+source.size)/4); source.forEachBits { k, v-> setBits(k,v, NULL_VALUE_BITS)} }
context(va: ValueIntAdapter<V>, ska: ValueIntAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <V,SK,SV> MutableMapVFloatInt<V>.putAll(source: MapVIntInt<SK,SV>, crossinline keySelector: (PairVIntInt<SK,SV>) -> Float, crossinline valueTransform: (PairVIntInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <V,S> MutableMapVFloatInt<V>.putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> Float, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <V,S> MutableMapVFloatInt<V>.putAll(source: CollectionVInt<S>, crossinline transform: (S) -> PairVObjInt<Float, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <V,S> MutableMapVFloatInt<V>.putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> Float, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <V,S> MutableMapVFloatInt<V>.putAll(source: CollectionVLong<S>, crossinline transform: (S) -> PairVObjInt<Float, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(va: ValueIntAdapter<V>) inline fun <V,S> MutableMapVFloatInt<V>.putAllGeneric(source: Collection<S>, crossinline transform: (S) -> Pair<Float, V>) { preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
inline infix operator fun <V> MutableMapVFloatInt<V>.plusAssign(source: MapVFloatInt<V>) = putAll(source)
inline fun <V> MutableMapVFloatInt<V>.remove(key: Float) = removeBits(key)
context(va: ValueIntAdapter<V>) inline fun <V> MutableMapVFloatInt<V>.remove(key: Float, value:V): Boolean = removeBits(key, va.toInt(value))
context(va: ValueIntAdapter<V>) inline fun <V> MutableMapVFloatInt<V>.removeIf(crossinline predicate:(Float, V)->Boolean) = removeIfBits { k, v-> predicate(k, va.fromInt(v))}
inline infix operator fun <V> MutableMapVFloatInt<V>.minusAssign(key: Float) {remove(key)}




class HashMapVFloatInt<V>(val collection: MutableFloatIntMap =MutableFloatIntMap(), override val NULL_VALUE_BITS: IntValueBits=Int.MIN_VALUE)
    : MutableMapVFloatInt<V> {
    constructor(size: Int, NO_VALUE: IntValueBits=Int.MIN_VALUE) : this(MutableFloatIntMap(size), NO_VALUE)

    override val size: Int get() = collection.size
    override inline fun getBits(k: Float): IntValueBits = collection.getOrDefault(k, NULL_VALUE_BITS)
    override inline fun anyBits(predicate: (Float, IntValueBits) -> Boolean): Float? {
        val finder = object : (Float, IntValueBits) -> Unit {
            var result: Float? = null
            var found = false
            override inline fun invoke(k: Float, v: IntValueBits) { if (!found && predicate(k, v)) { result = k; found = true } }
        }
        collection.forEach(finder)
        return finder.result
    }
    override inline fun trim() { collection.trim() }
    override inline fun clear() = collection.clear()
    override inline fun setBits(k: Float, v: IntValueBits, defaultReturn: IntValueBits): IntValueBits = collection.put(k,v, defaultReturn)
    override inline fun getOrPutBits(k: Float, defaultSet: () -> IntValueBits): IntValueBits = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: Float) = collection.remove(k)
    override inline fun removeBits(k: Float, v:IntValueBits): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (Float, IntValueBits) -> Boolean) = collection.removeIf(predicate)

    override inline fun anyKeyOrNull(predicate: (Float) -> Boolean): Float? = anyBits { k, _ -> predicate(k) }
    override inline fun removeKey(key: Float): Boolean { val had = collection.containsKey(key); removeBits(key); return had }
    override inline fun forEachValueBits(action: (valueBits: IntValueBits) -> Unit) = collection.forEachValue(action)

    // Thin wrappers for every public method of MutableFloatIntMap.
    inline fun capacity(): Int = collection.capacity
    inline fun none(): Boolean = collection.none()
    inline fun isEmpty(): Boolean = collection.isEmpty()
    inline fun isNotEmptyBits(): Boolean = collection.isNotEmpty()
    inline fun getOrDefault(key: Float, defaultValue: IntValueBits): IntValueBits = collection.getOrDefault(key, defaultValue)
    inline fun getOrElse(key: Float, defaultValue: () -> IntValueBits): IntValueBits = collection.getOrElse(key, defaultValue)
    inline fun forEachBits(block: (key: Float, value: IntValueBits) -> Unit) = collection.forEach(block)
    override inline fun forEachKey(block: (key: Float) -> Unit) = collection.forEachKey(block)
    inline fun forEachValue(block: (value: IntValueBits) -> Unit) = collection.forEachValue(block)
    inline fun all(predicate: (Float, IntValueBits) -> Boolean): Boolean = collection.all(predicate)
    inline fun count(): Int = collection.count()
    inline fun count(predicate: (Float, IntValueBits) -> Boolean): Int = collection.count(predicate)
    inline operator fun contains(key: Float): Boolean = collection.contains(key)
    inline fun containsKeyBits(key: Float): Boolean = collection.containsKey(key)
    override inline fun containsKey(key: Float): Boolean = containsKeyBits(key)
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
        crossinline transform: (key: Float, value: IntValueBits) -> CharSequence,
    ): String = collection.joinToString(separator, prefix, postfix, limit, truncated, transform)
    inline fun setBits(key: Float, value: IntValueBits) = collection.set(key, value)
    inline fun put(key: Float, value: IntValueBits) = collection.put(key, value)
    inline fun put(key: Float, value: IntValueBits, default: IntValueBits): IntValueBits = collection.put(key, value, default)
    inline fun putAllBits(from: FloatIntMap) = collection.putAll(from)
    inline fun plusAssignBits(from: FloatIntMap) = collection.plusAssign(from)
    inline fun minusAssignBits(key: Float) = collection.minusAssign(key)
    inline fun minusAssignBits(keys: FloatArray) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: FloatSet) = collection.minusAssign(keys)
    inline fun minusAssignBits(keys: FloatList) = collection.minusAssign(keys)

    context(va: ValueIntAdapter<V>) override inline fun toIterable(): Iterable<PairVObjInt<Float,V>> {
        val list = ArrayList<PairVObjInt<Float,V>>(size)
        collection.forEach { k, v -> list.add(PairVObjInt(k, v)) }
        return object : Iterable<PairVObjInt<Float,V>> {
            override inline fun iterator(): Iterator<PairVObjInt<Float,V>> = object : Iterator<PairVObjInt<Float,V>> {
                var idx = 0
                override inline fun hasNext(): Boolean = idx < list.size
                override inline fun next(): PairVObjInt<Float,V> { val p = list[idx++]; return p }
            }
        }
    }

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print V.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = collection.toString()
}
