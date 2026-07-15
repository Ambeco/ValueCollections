@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.MutableIntLongMap
import androidx.collection.MutableLongLongMap
import androidx.collection.MutableLongObjectMap

interface MapVLongObj<K,V> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_KEY_BITS: LongKeyBits

    val size: Int
    fun getBits(k: LongKeyBits): V?
    fun anyBits(predicate: (LongKeyBits, V) -> Boolean): LongKeyBits

    context(ka: ValueLongAdapter<K>) fun asIterable(): Iterable<PairVLongObj<K,V>>

    @JvmName("toVString") @Suppress("INAPPLICABLE_JVM_NAME")
    context(ka: ValueLongAdapter<K>) fun toString(): String = toVString()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(ka: ValueLongAdapter<K>)  inline fun <K,V> MapVLongObj<K,V>.asMapGeneric(): Map<K,V> = object: Map<K,V> {
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
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.keyFromInt(bits: LongKeyBits): K = if (bits==NULL_KEY_BITS) throw NoSuchElementException() else ka.fromLong(bits)
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.keyFromIntOr(bits: LongKeyBits, provider: ()->K): K = if (bits==NULL_KEY_BITS) provider() else ka.fromLong(bits)
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.keyFromIntOrNull(bits: LongKeyBits): K? = if (bits==NULL_KEY_BITS) null else ka.fromLong(bits)
context(ka: ValueLongAdapter<K>) inline operator fun <K,V> MapVLongObj<K,V>.get(key: K): V? = getBits(ka.toLong(key))
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.getOr(key: K, defaultResult:()->V): V = getBits(ka.toLong(key)) ?: defaultResult()
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.getOrNull(key: K): V? = getBits(ka.toLong(key))
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.any(crossinline predicate:(K, V)->Boolean):K = keyFromInt(anyBits{ k, v-> predicate(ka.fromLong(k), v)})
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.anyOr(crossinline predicate:(K, V)->Boolean, defaultResult:()->K):K = keyFromIntOr(anyBits{ k, v-> predicate(ka.fromLong(k), v)}, defaultResult)
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.anyOrNull(crossinline predicate:(K, V)->Boolean):K? = keyFromIntOrNull(anyBits{ k, v-> predicate(ka.fromLong(k), v)})
inline fun <K,V> MapVLongObj<K,V>.anyIndexedBits(crossinline predicate:(index:Int, LongKeyBits, V)->Boolean):LongKeyBits {
    return anyBits(object: (LongKeyBits,V) -> Boolean {
        var index = 0
        override inline fun invoke(k: LongKeyBits, v:V) = predicate(index++, k,v)
    } )
}
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.anyIndexed(crossinline action:(index:Int, K, V)->Boolean):K = keyFromInt(anyIndexedBits{ index, k, v-> action(index, ka.fromLong(k), v)})
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.anyIndexedOr(crossinline action:(index:Int, K, V)->Boolean, defaultResult:()->K):K = keyFromIntOr(anyIndexedBits{ index, k, v-> action(index, ka.fromLong(k), v)}, defaultResult)
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.anyIndexedOrNull(crossinline action:(index:Int, K, V)->Boolean):K? = keyFromIntOrNull(anyIndexedBits{ index, k, v-> action(index, ka.fromLong(k), v)})
inline fun <K,V> MapVLongObj<K,V>.forEachBits(crossinline action:(LongKeyBits, V)->Unit) {anyBits { k, v-> action(k,v); false} }
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.forEach(crossinline action:(K, V)->Unit) = forEachBits { k, v-> action(ka.fromLong(k), v) }
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.forEachPair(crossinline action:(PairVLongObj<K,V>)->Unit) {
    forEachBits(object: (LongKeyBits, V) -> Unit {
        var init = false
        lateinit var pair: PairVLongObj<K,V>
        override inline fun invoke(k: LongKeyBits, v: V) {
            if (!init) {pair = PairVLongObj(k, v); init = true}
            pair.firstBits = k
            pair.second = v
            action(pair)
        }
    })
}
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.forEachIndexed(crossinline action:(index:Int, K, V)->Unit) {
    forEachBits(object: (LongKeyBits, V) -> Unit {
        var index=0
        override inline fun invoke(k: LongKeyBits, v: V) = action(index++, ka.fromLong(k), v)
    })
}
inline val <K,V> MapVLongObj<K,V>.isEmpty get() = size == 0
inline fun <K,V> MapVLongObj<K,V>.isNotEmpty() = size > 0
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.containsKey(k: K) = getBits(ka.toLong(k)) != null
inline fun <K,V> MapVLongObj<K,V>.containsValue(findV: V) = anyBits { k, v-> v==findV} != NULL_KEY_BITS
context(ka: ValueLongAdapter<K>) inline fun <K,V, A : Appendable> MapVLongObj<K,V>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): A {
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
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongObj<K,V>.toVString() = joinToString(", ","{","}")


interface MutableMapVLongObj<K,V>: MapVLongObj<K,V> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim()
    fun clear()

    fun setBits(k: LongKeyBits, v: V): V?
    fun getOrPutBits(k: LongKeyBits, defaultSet: () -> V): V
    fun removeBits(k: LongKeyBits): V?
    fun removeBits(k: LongKeyBits, v: V):Boolean
    fun removeIfBits(predicate:(LongKeyBits,V)->Boolean)
    context(ka: ValueLongAdapter<K>) override fun asIterable(): MutableIterable<PairVLongObj<K,V>>

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
inline fun <K,V> MutableMapVLongObj<K,V>.preallocateFor(newSize: Int) {ensureCapacity(newSize + newSize/4) }
context(ka: ValueLongAdapter<K>) inline operator fun <K,V> MutableMapVLongObj<K,V>.set(key: K, value: V): Boolean = setBits(ka.toLong(key), value) != null
context(ka: ValueLongAdapter<K>) inline fun <K,V> MutableMapVLongObj<K,V>.getOrPut(key: K, crossinline defaultValue: ()->V):V = getOrPutBits(ka.toLong(key)) {defaultValue()}
inline fun <K,V> MutableMapVLongObj<K,V>.putAll(source: MapVLongObj<K,V>) {preallocateFor(size+source.size + (size+source.size)/4); source.forEachBits { k, v-> setBits(k,v)} }
context(ka: ValueLongAdapter<K>, ska: ValueIntAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVLongObj<K,V>.putAll(source: MapVIntInt<SK,SV>, crossinline keySelector: (PairVIntInt<SK,SV>) -> K, crossinline valueTransform: (PairVIntInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, ska: ValueIntAdapter<SK>, sva: ValueLongAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVLongObj<K,V>.putAll(source: MapVIntLong<SK,SV>, crossinline keySelector: (PairVIntLong<SK,SV>) -> K, crossinline valueTransform: (PairVIntLong<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, ska: ValueLongAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVLongObj<K,V>.putAll(source: MapVLongInt<SK,SV>, crossinline keySelector: (PairVLongInt<SK,SV>) -> K, crossinline valueTransform: (PairVLongInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, ska: ValueLongAdapter<SK>, sva: ValueLongAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVLongObj<K,V>.putAll(source: MapVLongObj<SK,SV>, crossinline keySelector: (PairVLongObj<SK,SV>) -> K, crossinline valueTransform: (PairVLongObj<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVLongObj<K,V>.putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVLongObj<K,V>.putAll(source: CollectionVInt<S>, crossinline transform: (S) -> PairVLongObj<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueLongAdapter<K>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVLongObj<K,V>.putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVLongObj<K,V>.putAll(source: CollectionVLong<S>, crossinline transform: (S) -> PairVLongObj<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueLongAdapter<K>) inline fun <K,V,S> MutableMapVLongObj<K,V>.putAllGeneric(source: Collection<S>, crossinline transform: (S) -> Pair<K, V>) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
inline operator fun <K,V> MutableMapVLongObj<K,V>.plusAssign(source: MapVLongObj<K,V>) = putAll(source)
context(ka: ValueLongAdapter<K>) inline fun <K,V> MutableMapVLongObj<K,V>.remove(key: K): V? = removeBits(ka.toLong(key))
context(ka: ValueLongAdapter<K>) inline fun <K,V> MutableMapVLongObj<K,V>.remove(key: K, value:V): Boolean = removeBits(ka.toLong(key), value)
context(ka: ValueLongAdapter<K>) inline fun <K,V> MutableMapVLongObj<K,V>.removeIf(crossinline predicate:(K, V)->Boolean) = removeIfBits { k, v-> predicate(ka.fromLong(k), v)}
context(ka: ValueLongAdapter<K>) inline operator fun <K,V> MutableMapVLongObj<K,V>.minusAssign(key: K) {remove(key)}


class HashMapVLongObj<K,V>(val collection: MutableLongObjectMap<V> =MutableLongObjectMap(), override val NULL_KEY_BITS: LongKeyBits=Long.MIN_VALUE)
    : MutableMapVLongObj<K,V> {
    constructor(size: Int, NULL_KEY_BITS: LongKeyBits=Long.MIN_VALUE) : this(MutableLongObjectMap(size), NULL_KEY_BITS)

    override val size: Int get() = collection.size
    override inline fun getBits(k: LongKeyBits): V? = collection.get(k)
    override inline fun anyBits(predicate: (LongKeyBits, V) -> Boolean): LongKeyBits {
        val searcher = object: (LongKeyBits, V) -> Boolean {
            var result:LongKeyBits = NULL_KEY_BITS
            override inline fun invoke(k: LongKeyBits, v: V): Boolean {
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
    override inline fun setBits(k: LongKeyBits, v: V): V? = collection.put(k,v)
    override inline fun getOrPutBits(k: LongKeyBits, defaultSet: () -> V): V = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: LongKeyBits): V? = collection.remove(k)
    override inline fun removeBits(k: LongKeyBits, v:V): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (LongKeyBits, V) -> Boolean) = collection.removeIf(predicate)
    context(ka: ValueLongAdapter<K>) override inline fun asIterable(): MutableIterable<PairVLongObj<K,V>> = throw NotImplementedError()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueLongAdapter) to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString(): String = collection.toString()
}
