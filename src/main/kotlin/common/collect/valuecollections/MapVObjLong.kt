@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.MutableIntLongMap
import androidx.collection.MutableObjectLongMap

interface MapVObjLong<K,V> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_VALUE_BITS: LongValueBits

    val size: Int
    fun getBits(k: K): LongValueBits
    fun anyBits(predicate: (K, LongValueBits) -> Boolean): K?

    context(va: ValueLongAdapter<V>) fun asIterable(): Iterable<PairVObjLong<K,V>>

    @JvmName("toVString") @Suppress("INAPPLICABLE_JVM_NAME")
    context(va: ValueLongAdapter<V>) fun toString(): String = toVString()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(va: ValueLongAdapter<V>)  inline fun <K,V> MapVObjLong<K,V>.asMapGeneric(): Map<K,V> = object: Map<K,V> {
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
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.valueFromLong(bits: LongValueBits): V = if (bits==NULL_VALUE_BITS) throw NoSuchElementException() else va.fromLong(bits)
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.valueFromLongOr(bits: LongValueBits, provider: ()->V): V = if (bits==NULL_VALUE_BITS) provider() else va.fromLong(bits)
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.valueFromLongOrNull(bits: LongValueBits): V? = if (bits==NULL_VALUE_BITS) null else va.fromLong(bits)
context(va: ValueLongAdapter<V>) inline operator fun <K,V> MapVObjLong<K,V>.get(key: K): V = valueFromLong(getBits(key))
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.getOr(key: K, defaultResult:()->V): V = valueFromLongOr(getBits(key), defaultResult)
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.getOrNull(key: K): V? = valueFromLongOrNull(getBits(key))
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.any(crossinline predicate:(K, V)->Boolean):K? = anyBits{ k, v-> predicate(k, va.fromLong(v))}
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.anyOr(crossinline predicate:(K, V)->Boolean, defaultResult:()->K):K = anyBits{ k, v-> predicate(k, va.fromLong(v))} ?: defaultResult()
inline fun <K,V> MapVObjLong<K,V>.anyIndexedBits(crossinline predicate:(index:Int, K, LongValueBits)->Boolean):K? {
    return anyBits(object: (K,LongValueBits) -> Boolean {
        var index = 0
        override inline fun invoke(k: K, v:LongValueBits) = predicate(index++, k,v)
    } )
}
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.anyIndexed(crossinline action:(index:Int, K, V)->Boolean):K? = anyIndexedBits{ index, k, v-> action(index,k, va.fromLong(v))}
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.anyIndexedOr(crossinline action:(index:Int, K, V)->Boolean, defaultResult:()->K):K = anyIndexedBits{ index, k, v-> action(index,k, va.fromLong(v))} ?: defaultResult()
inline fun <K,V> MapVObjLong<K,V>.forEachBits(crossinline action:(K, LongValueBits)->Unit) {anyBits { k, v-> action(k,v); false} }
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.forEach(crossinline action:(K, V)->Unit) = forEachBits { k, v-> action(k, va.fromLong(v)) }
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.forEachPair(crossinline action:(PairVObjLong<K,V>)->Unit) {
    forEachBits(object: (K, LongValueBits) -> Unit {
        var init = false
        lateinit var pair: PairVObjLong<K,V>
        override inline fun invoke(k: K, v: LongValueBits) {
            if (!init) {pair = PairVObjLong(k, v); init = true}
            pair.first = k
            pair.secondBits = v
            action(pair)
        }
    })
}
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.forEachIndexed(crossinline action:(index:Int, K, V)->Unit) {
    forEachBits(object: (K, LongValueBits) -> Unit {
        var index=0
        override inline fun invoke(k: K, v: LongValueBits) = action(index++,k, va.fromLong(v))
    })
}
inline val <K,V> MapVObjLong<K,V>.isEmpty get() = size == 0
inline fun <K,V> MapVObjLong<K,V>.isNotEmpty() = size > 0
inline fun <K,V> MapVObjLong<K,V>.containsKey(k: K) = getBits(k) != NULL_VALUE_BITS
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.containsValue(findV: V) = anyBits { k, v-> v==findV} != null
context(va: ValueLongAdapter<V>) inline fun <K,V, A : Appendable> MapVObjLong<K,V>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): A {
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
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(va: ValueLongAdapter<V>) inline fun <K,V> MapVObjLong<K,V>.toVString() = joinToString(", ","{","}")


interface MutableMapVObjLong<K,V>: MapVObjLong<K,V> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim()
    fun clear()

    fun setBits(k: K, v: LongValueBits, defaultReturn: LongValueBits): LongValueBits
    fun getOrPutBits(k: K, defaultSet: () -> LongValueBits): LongValueBits
    fun removeBits(k: K)
    fun removeBits(k: K, v: LongValueBits):Boolean
    fun removeIfBits(predicate:(K,LongValueBits)->Boolean)
    context(va: ValueLongAdapter<V>) override fun asIterable(): MutableIterable<PairVObjLong<K,V>>

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
inline fun <K,V> MutableMapVObjLong<K,V>.preallocateFor(newSize: Int) {ensureCapacity(newSize + newSize/4) }
context(va: ValueLongAdapter<V>) inline operator fun <K,V> MutableMapVObjLong<K,V>.set(key: K, value: V): Boolean = setBits(key, va.toLong(value), NULL_VALUE_BITS) != NULL_VALUE_BITS
context(va: ValueLongAdapter<V>) inline fun <K,V> MutableMapVObjLong<K,V>.set(key: K, value: V, defaultReturn: V): V = valueFromLong(setBits(key, va.toLong(value), va.toLong(defaultReturn)))
context(va: ValueLongAdapter<V>) inline fun <K,V> MutableMapVObjLong<K,V>.getOrPut(key: K, crossinline defaultValue: ()->V):V = valueFromLong(getOrPutBits(key, {va.toLong(defaultValue())}))
inline fun <K,V> MutableMapVObjLong<K,V>.putAll(source: MapVObjLong<K,V>) {preallocateFor(size+source.size + (size+source.size)/4); source.forEachBits { k, v-> setBits(k,v, NULL_VALUE_BITS)} }
context(va: ValueLongAdapter<V>, ska: ValueIntAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVObjLong<K,V>.putAll(source: MapVIntInt<SK,SV>, crossinline keySelector: (PairVIntInt<SK,SV>) -> K, crossinline valueTransform: (PairVIntInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueLongAdapter<V>, ska: ValueIntAdapter<SK>, sva: ValueLongAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVObjLong<K,V>.putAll(source: MapVObjLong<SK,SV>, crossinline keySelector: (PairVObjLong<SK,SV>) -> K, crossinline valueTransform: (PairVObjLong<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueLongAdapter<V>, ska: ValueLongAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVObjLong<K,V>.putAll(source: MapVLongInt<SK,SV>, crossinline keySelector: (PairVLongInt<SK,SV>) -> K, crossinline valueTransform: (PairVLongInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueLongAdapter<V>, ska: ValueLongAdapter<SK>, sva: ValueLongAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVObjLong<K,V>.putAll(source: MapVLongLong<SK,SV>, crossinline keySelector: (PairVLongLong<SK,SV>) -> K, crossinline valueTransform: (PairVLongLong<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVObjLong<K,V>.putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVObjLong<K,V>.putAll(source: CollectionVInt<S>, crossinline transform: (S) -> PairVObjLong<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVObjLong<K,V>.putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVObjLong<K,V>.putAll(source: CollectionVLong<S>, crossinline transform: (S) -> PairVObjLong<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(va: ValueLongAdapter<V>) inline fun <K,V,S> MutableMapVObjLong<K,V>.putAllGeneric(source: Collection<S>, crossinline transform: (S) -> Pair<K, V>) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
inline operator fun <K,V> MutableMapVObjLong<K,V>.plusAssign(source: MapVObjLong<K,V>) = putAll(source)
inline fun <K,V> MutableMapVObjLong<K,V>.remove(key: K) = removeBits(key)
context(va: ValueLongAdapter<V>) inline fun <K,V> MutableMapVObjLong<K,V>.remove(key: K, value:V): Boolean = removeBits(key, va.toLong(value))
context(va: ValueLongAdapter<V>) inline fun <K,V> MutableMapVObjLong<K,V>.removeIf(crossinline predicate:(K, V)->Boolean) = removeIfBits { k, v-> predicate(k, va.fromLong(v))}
inline operator fun <K,V> MutableMapVObjLong<K,V>.minusAssign(key: K) {remove(key)}


class HashMapVObjLong<K,V>(val collection: MutableObjectLongMap<K> =MutableObjectLongMap(), override val NULL_VALUE_BITS: LongValueBits=Long.MIN_VALUE)
    : MutableMapVObjLong<K,V> {
    constructor(size: Int, NULL_VALUE_BITS: LongValueBits =Long.MIN_VALUE) : this(MutableObjectLongMap(size), NULL_VALUE_BITS)

    override val size: Int get() = collection.size
    override inline fun getBits(k: K): LongValueBits = collection.get(k)
    override inline fun anyBits(predicate: (K, LongValueBits) -> Boolean): K? {
        val searcher = object: (K, LongValueBits) -> Boolean {
            var result:K? = null
            override inline fun invoke(k: K, v: LongValueBits): Boolean {
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
    override inline fun setBits(k: K, v: LongValueBits, defaultReturn: LongValueBits): LongValueBits = collection.put(k,v, defaultReturn)
    override inline fun getOrPutBits(k: K, defaultSet: () -> LongValueBits): LongValueBits = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: K) = collection.remove(k)
    override inline fun removeBits(k: K, v:LongValueBits): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (K, LongValueBits) -> Boolean) = collection.removeIf(predicate)
    context(va: ValueLongAdapter<V>) override inline fun asIterable(): MutableIterable<PairVObjLong<K,V>> = throw NotImplementedError()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString(): String = collection.toString()
}
