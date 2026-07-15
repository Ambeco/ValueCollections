@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

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

    @JvmName("toVString") @Suppress("INAPPLICABLE_JVM_NAME")
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) fun toString(): String = toVString()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)  inline fun <K,V> MapVIntInt<K,V>.asMapGeneric(): Map<K,V> = object: Map<K,V> {
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
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.containsValue(findV: V) = anyBits { k, v-> v==findV} != NULL_KEY_BITS
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V, A : Appendable> MapVIntInt<K,V>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): A {
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
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVIntInt<K,V>.toVString() = joinToString(", ","{","}")



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
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
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
inline operator fun <K,V> MutableMapVIntInt<K,V>.plusAssign(source: MapVIntInt<K,V>) = putAll(source)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MutableMapVIntInt<K,V>.remove(key: K) = removeBits(ka.toInt(key))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVIntInt<K,V>.remove(key: K, value:V): Boolean = removeBits(ka.toInt(key), va.toInt(value))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVIntInt<K,V>.removeIf(crossinline predicate:(K, V)->Boolean) = removeIfBits { k, v-> predicate(ka.fromInt(k), va.fromInt(v))}
context(ka: ValueIntAdapter<K>) inline operator fun <K,V> MutableMapVIntInt<K,V>.minusAssign(key: K) {remove(key)}




class HashMapVIntInt<K,V>(val collection: MutableIntIntMap=MutableIntIntMap(), override val NULL_KEY_BITS: IntKeyBits=Int.MIN_VALUE, override val NULL_VALUE_BITS: IntValueBits=Int.MIN_VALUE)
    : MutableMapVIntInt<K,V> {
    constructor(size: Int, NULL_KEY_BITS: IntKeyBits=Int.MIN_VALUE, NULL_VALUE_BITS: IntValueBits=Int.MIN_VALUE) : this(MutableIntIntMap(size), NULL_KEY_BITS, NULL_VALUE_BITS)

    override val size: Int get() = collection.size
    override inline fun getBits(k: IntKeyBits): IntValueBits = collection.get(k)
    override inline fun anyBits(predicate: (IntKeyBits, IntValueBits) -> Boolean): IntKeyBits {
        val searcher = object: (IntKeyBits, IntValueBits) -> Boolean {
            var result:IntKeyBits = NULL_KEY_BITS
            override inline fun invoke(k: IntKeyBits, v: IntValueBits): Boolean {
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
    override inline fun setBits(k: IntKeyBits, v: IntValueBits, defaultReturn: IntValueBits): IntValueBits = collection.put(k,v, defaultReturn)
    override inline fun getOrPutBits(k: IntKeyBits, defaultSet: () -> IntValueBits): IntValueBits = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: IntKeyBits) = collection.remove(k)
    override inline fun removeBits(k: IntKeyBits, v:IntValueBits): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (IntKeyBits, IntValueBits) -> Boolean) = collection.removeIf(predicate)
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) override inline fun asIterable(): MutableIterable<PairVIntInt<K,V>> = throw NotImplementedError()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString(): String = collection.toString()
}

