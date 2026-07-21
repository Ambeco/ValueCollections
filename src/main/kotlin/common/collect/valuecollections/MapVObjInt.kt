@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.MutableIntIntMap
import androidx.collection.MutableObjectIntMap

interface MapVObjInt<K,V> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_VALUE_BITS: IntValueBits

    val size: Int
    fun getBits(k: K): IntValueBits
    fun anyBits(predicate: (K, IntValueBits) -> Boolean): K?

    context(va: ValueIntAdapter<V>) fun asIterable(): Iterable<PairVIntInt<K,V>>

    @JvmName("toStringV") @Suppress("INAPPLICABLE_JVM_NAME")
    context(va: ValueIntAdapter<V>) fun toString(): String = toStringV()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(va: ValueIntAdapter<V>)  inline fun <K,V> MapVObjInt<K,V>.asMapGeneric(): Map<K,V> = object: Map<K,V> {
    override inline val size: Int get() = this@asMapGeneric.size
    override inline val keys: Set<K> get() = asIterable().mapTo(HashSet()) {e->e.key}
    override inline val values: Collection<V> get() = asIterable().mapTo(HashSet()) {e->e.value}
    override inline val entries: Set<Map.Entry<K, V>> get() = asIterable().mapTo(HashSet()) {e->object:Map.Entry<K,V>{
        override val key: K get() = e.key
        override val value: V get() = e.value}
    }
    override inline fun isEmpty(): Boolean = this@asMapGeneric.isEmpty
    override inline fun containsKey(key: K): Boolean = this@asMapGeneric.containsKey(key)
    override inline fun containsValue(value: V): Boolean = this@asMapGeneric.containsValue(value)
    override inline fun get(key: K): V? = this@asMapGeneric.getOrNull(key)
}
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.valueFromInt(bits: IntValueBits): V = if (bits==NULL_VALUE_BITS) throw NoSuchElementException() else va.fromInt(bits)
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.valueFromIntOr(bits: IntValueBits, provider: ()->V): V = if (bits==NULL_VALUE_BITS) provider() else va.fromInt(bits)
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.valueFromIntOrNull(bits: IntValueBits): V? = if (bits==NULL_VALUE_BITS) null else va.fromInt(bits)
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
inline fun <K,V> MapVObjInt<K,V>.containsKey(k: K) = getBits(k) != NULL_VALUE_BITS
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.containsValue(findV: V) = anyBits { k, v-> v==findV} != null
context(va: ValueIntAdapter<V>) inline fun <K,V, A : Appendable> MapVObjInt<K,V>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): A {
    val appender = object: (Int,K,V)-> Boolean {
        var count=0
        override inline fun invoke(index: Int, k:K, v:V): Boolean {
            if (limit<0 || count++ < limit) {
                if (count != 1) buffer.append(separator)
                buffer.append(transform(k,v))
                if (count < limit)
                    return false
            }
            if (count >= limit)
                buffer.append(truncated)
            return true
        }
    }
    buffer.append(prefix)
    anyIndexed(appender)
    buffer.append(postfix)
    return buffer
}
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVObjInt<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): String
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
    context(va: ValueIntAdapter<V>) override fun asIterable(): MutableIterable<PairVIntInt<K,V>>

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
inline operator fun <K,V> MutableMapVObjInt<K,V>.plusAssign(source: MapVObjInt<K,V>) = putAll(source)
inline fun <K,V> MutableMapVObjInt<K,V>.remove(key: K) = removeBits(key)
context(va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVObjInt<K,V>.remove(key: K, value:V): Boolean = removeBits(key, va.toInt(value))
context(va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVObjInt<K,V>.removeIf(crossinline predicate:(K, V)->Boolean) = removeIfBits { k, v-> predicate(k, va.fromInt(v))}
inline operator fun <K,V> MutableMapVObjInt<K,V>.minusAssign(key: K) {remove(key)}




class HashMapVObjInt<K,V>(val collection: MutableObjectIntMap<K> =MutableObjectIntMap(), override val NULL_VALUE_BITS: IntValueBits=Int.MIN_VALUE)
    : MutableMapVObjInt<K,V> {
    constructor(size: Int, NO_VALUE: IntValueBits=Int.MIN_VALUE) : this(MutableObjectIntMap(size), NO_VALUE)

    override val size: Int get() = collection.size
    override inline fun getBits(k: K): IntValueBits = collection.get(k)
    override inline fun anyBits(predicate: (K, IntValueBits) -> Boolean): K? {
        val searcher = object: (K, IntValueBits) -> Boolean {
            var result:K? = null
            override inline fun invoke(k: K, v: IntValueBits): Boolean {
                if (predicate(k,v)) {
                    result = k
                    return true
                }
                return false
            }

        }
        return searcher.result
    }
    override inline fun trim() { collection.trim() }
    override inline fun clear() = collection.clear()
    override inline fun setBits(k: K, v: IntValueBits, defaultReturn: IntValueBits): IntValueBits = collection.put(k,v, defaultReturn)
    override inline fun getOrPutBits(k: K, defaultSet: () -> IntValueBits): IntValueBits = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: K) = collection.remove(k)
    override inline fun removeBits(k: K, v:IntValueBits): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (K, IntValueBits) -> Boolean) = collection.removeIf(predicate)
    context(va: ValueIntAdapter<V>) override inline fun asIterable(): MutableIterable<PairVIntInt<K,V>> = throw NotImplementedError()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = collection.toString()
}

