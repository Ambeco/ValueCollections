@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.MutableIntLongMap

interface MapVIntLong<K,V> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_KEY_BITS: IntKeyBits
    val NULL_VALUE_BITS: LongValueBits

    val size: Int
    fun getBits(k: IntKeyBits): LongValueBits
    fun anyBits(predicate: (IntKeyBits, LongValueBits) -> Boolean): IntKeyBits

    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) fun asIterable(): Iterable<PairVIntLong<K,V>>

    @JvmName("toStringV") @Suppress("INAPPLICABLE_JVM_NAME")
    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) fun toString(): String = toStringV()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>)  inline fun <K,V> MapVIntLong<K,V>.asMapGeneric(): Map<K,V> = object: Map<K,V> {
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
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntLong<K,V>.keyFromInt(bits: IntKeyBits): K = if (bits==NULL_KEY_BITS) throw NoSuchElementException() else ka.fromInt(bits)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntLong<K,V>.keyFromIntOr(bits: IntKeyBits, provider: ()->K): K = if (bits==NULL_KEY_BITS) provider() else ka.fromInt(bits)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntLong<K,V>.keyFromIntOrNull(bits: IntKeyBits): K? = if (bits==NULL_KEY_BITS) null else ka.fromInt(bits)
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.valueFromLong(bits: LongValueBits): V = if (bits==NULL_VALUE_BITS) throw NoSuchElementException() else va.fromLong(bits)
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.valueFromLongOr(bits: LongValueBits, provider: ()->V): V = if (bits==NULL_VALUE_BITS) provider() else va.fromLong(bits)
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.valueFromLongOrNull(bits: LongValueBits): V? = if (bits==NULL_VALUE_BITS) null else va.fromLong(bits)
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline operator fun <K,V> MapVIntLong<K,V>.get(key: K): V = valueFromLong(getBits(ka.toInt(key)))
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.getOr(key: K, defaultResult:()->V): V = valueFromLongOr(getBits(ka.toInt(key)), defaultResult)
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.getOrNull(key: K): V? = valueFromLongOrNull(getBits(ka.toInt(key)))
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.any(crossinline predicate:(K, V)->Boolean):K = keyFromInt(anyBits{ k, v-> predicate(ka.fromInt(k), va.fromLong(v))})
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.anyOr(crossinline predicate:(K, V)->Boolean, defaultResult:()->K):K = keyFromIntOr(anyBits{ k, v-> predicate(ka.fromInt(k), va.fromLong(v))}, defaultResult)
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.anyOrNull(crossinline predicate:(K, V)->Boolean):K? = keyFromIntOrNull(anyBits{ k, v-> predicate(ka.fromInt(k), va.fromLong(v))})
inline fun <K,V> MapVIntLong<K,V>.anyIndexedBits(crossinline predicate:(index:Int, IntKeyBits, LongValueBits)->Boolean):IntKeyBits {
    return anyBits(object: (IntKeyBits,LongValueBits) -> Boolean {
        var index = 0
        override inline fun invoke(k: IntKeyBits, v:LongValueBits) = predicate(index++, k,v)
    } )
}
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.anyIndexed(crossinline action:(index:Int, K, V)->Boolean):K = keyFromInt(anyIndexedBits{ index, k, v-> action(index, ka.fromInt(k), va.fromLong(v))})
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.anyIndexedOr(crossinline action:(index:Int, K, V)->Boolean, defaultResult:()->K):K = keyFromIntOr(anyIndexedBits{ index, k, v-> action(index, ka.fromInt(k), va.fromLong(v))}, defaultResult)
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.anyIndexedOrNull(crossinline action:(index:Int, K, V)->Boolean):K? = keyFromIntOrNull(anyIndexedBits{ index, k, v-> action(index, ka.fromInt(k), va.fromLong(v))})
inline fun <K,V> MapVIntLong<K,V>.forEachBits(crossinline action:(IntKeyBits, LongValueBits)->Unit) {anyBits { k, v-> action(k,v); false} }
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.forEach(crossinline action:(K, V)->Unit) = forEachBits { k, v-> action(ka.fromInt(k), va.fromLong(v)) }
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.forEachPair(crossinline action:(PairVIntLong<K,V>)->Unit) {
    forEachBits(object: (IntKeyBits, LongValueBits) -> Unit {
        val pair: PairVIntLong<K,V> = PairVIntLong(NULL_KEY_BITS, NULL_VALUE_BITS)
        override inline fun invoke(k: IntKeyBits, v: LongValueBits) { pair.firstBits = k; pair.secondBits = v; action(pair) }
    })
}
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.forEachIndexed(crossinline action:(index:Int, K, V)->Unit) {
    forEachBits(object: (IntKeyBits, LongValueBits) -> Unit {
        var index=0
        override inline fun invoke(k: IntKeyBits, v: LongValueBits) = action(index++, ka.fromInt(k), va.fromLong(v))
    })
}
inline val <K,V> MapVIntLong<K,V>.isEmpty get() = size == 0
inline fun <K,V> MapVIntLong<K,V>.isNotEmpty() = size > 0
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntLong<K,V>.containsKey(k: K) = getBits(ka.toInt(k)) != NULL_VALUE_BITS
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.containsValue(findV: V) = anyBits { k, v-> v==findV} != NULL_KEY_BITS
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V, A : Appendable> MapVIntLong<K,V>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): A {
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
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MapVIntLong<K,V>.toStringV() = joinToString(", ","{","}")


interface MutableMapVIntLong<K,V>: MapVIntLong<K,V> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim()
    fun clear()

    fun setBits(k: IntKeyBits, v: LongValueBits, defaultReturn: LongValueBits): LongValueBits
    fun getOrPutBits(k: IntKeyBits, defaultSet: () -> LongValueBits): LongValueBits
    fun removeBits(k: IntKeyBits)
    fun removeBits(k: IntKeyBits, v: LongValueBits):Boolean
    fun removeIfBits(predicate:(IntKeyBits,LongValueBits)->Boolean)
    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) override fun asIterable(): MutableIterable<PairVIntLong<K,V>>

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
inline fun <K,V> MutableMapVIntLong<K,V>.preallocateFor(newSize: Int) {ensureCapacity(newSize + newSize/4) }
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline operator fun <K,V> MutableMapVIntLong<K,V>.set(key: K, value: V): Boolean = setBits(ka.toInt(key), va.toLong(value), NULL_VALUE_BITS) != NULL_VALUE_BITS
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MutableMapVIntLong<K,V>.set(key: K, value: V, defaultReturn: V): V = valueFromLong(setBits(ka.toInt(key), va.toLong(value), va.toLong(defaultReturn)))
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MutableMapVIntLong<K,V>.getOrPut(key: K, crossinline defaultValue: ()->V):V = valueFromLong(getOrPutBits(ka.toInt(key), {va.toLong(defaultValue())}))
inline fun <K,V> MutableMapVIntLong<K,V>.putAll(source: MapVIntLong<K,V>) {preallocateFor(size+source.size + (size+source.size)/4); source.forEachBits { k, v-> setBits(k,v, NULL_VALUE_BITS)} }
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, ska: ValueIntAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVIntLong<K,V>.putAll(source: MapVIntInt<SK,SV>, crossinline keySelector: (PairVIntInt<SK,SV>) -> K, crossinline valueTransform: (PairVIntInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, ska: ValueIntAdapter<SK>, sva: ValueLongAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVIntLong<K,V>.putAll(source: MapVIntLong<SK,SV>, crossinline keySelector: (PairVIntLong<SK,SV>) -> K, crossinline valueTransform: (PairVIntLong<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, ska: ValueLongAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVIntLong<K,V>.putAll(source: MapVLongInt<SK,SV>, crossinline keySelector: (PairVLongInt<SK,SV>) -> K, crossinline valueTransform: (PairVLongInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, ska: ValueLongAdapter<SK>, sva: ValueLongAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVIntLong<K,V>.putAll(source: MapVLongLong<SK,SV>, crossinline keySelector: (PairVLongLong<SK,SV>) -> K, crossinline valueTransform: (PairVLongLong<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVIntLong<K,V>.putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVIntLong<K,V>.putAll(source: CollectionVInt<S>, crossinline transform: (S) -> PairVIntLong<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVIntLong<K,V>.putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVIntLong<K,V>.putAll(source: CollectionVLong<S>, crossinline transform: (S) -> PairVIntLong<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V,S> MutableMapVIntLong<K,V>.putAllGeneric(source: Collection<S>, crossinline transform: (S) -> Pair<K, V>) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
inline operator fun <K,V> MutableMapVIntLong<K,V>.plusAssign(source: MapVIntLong<K,V>) = putAll(source)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MutableMapVIntLong<K,V>.remove(key: K) = removeBits(ka.toInt(key))
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MutableMapVIntLong<K,V>.remove(key: K, value:V): Boolean = removeBits(ka.toInt(key), va.toLong(value))
context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) inline fun <K,V> MutableMapVIntLong<K,V>.removeIf(crossinline predicate:(K, V)->Boolean) = removeIfBits { k, v-> predicate(ka.fromInt(k), va.fromLong(v))}
context(ka: ValueIntAdapter<K>) inline operator fun <K,V> MutableMapVIntLong<K,V>.minusAssign(key: K) {remove(key)}


class HashMapVIntLong<K,V>(val collection: MutableIntLongMap=MutableIntLongMap(), override val NULL_KEY_BITS: IntKeyBits=Int.MIN_VALUE, override val NULL_VALUE_BITS: LongValueBits=Long.MIN_VALUE)
    : MutableMapVIntLong<K,V> {
    constructor(size: Int, NULL_KEY_BITS: IntKeyBits=Int.MIN_VALUE, NULL_VALUE_BITS: LongValueBits=Long.MIN_VALUE ) : this(MutableIntLongMap(size), NULL_KEY_BITS, NULL_VALUE_BITS)

    override val size: Int get() = collection.size
    override inline fun getBits(k: IntKeyBits): LongValueBits = collection.get(k)
    override inline fun anyBits(predicate: (IntKeyBits, LongValueBits) -> Boolean): IntKeyBits {
        val searcher = object: (IntKeyBits, LongValueBits) -> Boolean {
            var result:IntKeyBits = NULL_KEY_BITS
            override inline fun invoke(k: IntKeyBits, v: LongValueBits): Boolean {
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
    override inline fun setBits(k: IntKeyBits, v: LongValueBits, defaultReturn: LongValueBits): LongValueBits = collection.put(k,v, defaultReturn)
    override inline fun getOrPutBits(k: IntKeyBits, defaultSet: () -> LongValueBits): LongValueBits = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: IntKeyBits) = collection.remove(k)
    override inline fun removeBits(k: IntKeyBits, v:LongValueBits): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (IntKeyBits, LongValueBits) -> Boolean) = collection.removeIf(predicate)
    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>) override inline fun asIterable(): MutableIterable<PairVIntLong<K,V>> = throw NotImplementedError()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = collection.toString()
}
