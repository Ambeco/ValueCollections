@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.MutableIntObjectMap

interface MapVIntObj<K,V> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_KEY_BITS: IntKeyBits

    val size: Int
    fun getBits(k: IntKeyBits): V?
    fun anyBits(predicate: (IntKeyBits, V) -> Boolean): IntKeyBits

    context(ka: ValueIntAdapter<K>) fun asIterable(): Iterable<Pair<K,V>>

    @JvmName("toStringV") @Suppress("INAPPLICABLE_JVM_NAME")
    context(ka: ValueIntAdapter<K>) fun toString(): String = toStringV()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(ka: ValueIntAdapter<K>)  inline fun <K,V> MapVIntObj<K,V>.asMapGeneric(): Map<K,V> = object: Map<K,V> {
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
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.keyFromInt(bits: IntKeyBits): K = if (bits==NULL_KEY_BITS) throw NoSuchElementException() else ka.fromInt(bits)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.keyFromIntOr(bits: IntKeyBits, provider: ()->K): K = if (bits==NULL_KEY_BITS) provider() else ka.fromInt(bits)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.keyFromIntOrNull(bits: IntKeyBits): K? = if (bits==NULL_KEY_BITS) null else ka.fromInt(bits)
context(ka: ValueIntAdapter<K>) inline operator fun <K,V> MapVIntObj<K,V>.get(key: K): V? = getBits(ka.toInt(key))
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.getOr(key: K, defaultResult:()->V): V = getBits(ka.toInt(key)) ?: defaultResult()
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.getOrNull(key: K): V? = getBits(ka.toInt(key))
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.any(crossinline predicate:(K, V)->Boolean):K = keyFromInt(anyBits{ k, v-> predicate(ka.fromInt(k), v)})
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.anyOr(crossinline predicate:(K, V)->Boolean, defaultResult:()->K):K = keyFromIntOr(anyBits{ k, v-> predicate(ka.fromInt(k), v)}, defaultResult)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.anyOrNull(crossinline predicate:(K, V)->Boolean):K? = keyFromIntOrNull(anyBits{ k, v-> predicate(ka.fromInt(k), v)})
inline fun <K,V> MapVIntObj<K,V>.anyIndexedBits(crossinline predicate:(index:Int, IntKeyBits, V)->Boolean):IntKeyBits {
    return anyBits(object: (IntKeyBits,V) -> Boolean {
        var index = 0
        override inline fun invoke(k: IntKeyBits, v:V) = predicate(index++, k,v)
    } )
}
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.anyIndexed(crossinline action:(index:Int, K, V)->Boolean):K = keyFromInt(anyIndexedBits{ index, k, v-> action(index, ka.fromInt(k), v)})
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.anyIndexedOr(crossinline action:(index:Int, K, V)->Boolean, defaultResult:()->K):K = keyFromIntOr(anyIndexedBits{ index, k, v-> action(index, ka.fromInt(k), v)}, defaultResult)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.anyIndexedOrNull(crossinline action:(index:Int, K, V)->Boolean):K? = keyFromIntOrNull(anyIndexedBits{ index, k, v-> action(index, ka.fromInt(k), v)})
inline fun <K,V> MapVIntObj<K,V>.forEachBits(crossinline action:(IntKeyBits, V)->Unit) {anyBits { k, v-> action(k,v); false} }
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.forEach(crossinline action:(K, V)->Unit) = forEachBits { k, v-> action(ka.fromInt(k), v) }
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.forEachPair(crossinline action:(PairVIntObj<K,V>)->Unit) {
    forEachBits(object: (IntKeyBits, V) -> Unit {
        var init = false
        lateinit var pair: PairVIntObj<K,V>
        override inline fun invoke(k: IntKeyBits, v: V) {
            if (!init) {pair = PairVIntObj(k, v); init = true}
            pair.firstBits = k
            pair.second = v
            action(pair)
        }
    })
}
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.forEachIndexed(crossinline action:(index:Int, K, V)->Unit) {
    forEachBits(object: (IntKeyBits, V) -> Unit {
        var index=0
        override inline fun invoke(k: IntKeyBits, v: V) = action(index++, ka.fromInt(k), v)
    })
}
inline val <K,V> MapVIntObj<K,V>.isEmpty get() = size == 0
inline fun <K,V> MapVIntObj<K,V>.isNotEmpty() = size > 0
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.containsKey(k: K) = getBits(ka.toInt(k)) != null
inline fun <K,V> MapVIntObj<K,V>.containsValue(findV: V) = anyBits { k, v-> v==findV} != NULL_KEY_BITS
context(ka: ValueIntAdapter<K>) inline fun <K,V, A : Appendable> MapVIntObj<K,V>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): A {
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
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(ka: ValueIntAdapter<K>) inline fun <K,V> MapVIntObj<K,V>.toStringV() = joinToString(", ","{","}")


interface MutableMapVIntObj<K,V>: MapVIntObj<K,V> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim()
    fun clear()

    fun setBits(k: IntKeyBits, v: V, defaultReturn: V?): V?
    fun getOrPutBits(k: IntKeyBits, defaultSet: () -> V): V
    fun removeBits(k: IntKeyBits): V?
    fun removeBits(k: IntKeyBits, v: V):Boolean
    fun removeIfBits(predicate:(IntKeyBits,V)->Boolean)
    context(ka: ValueIntAdapter<K>) override fun asIterable(): MutableIterable<Pair<K,V>>

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
inline fun <K,V> MutableMapVIntObj<K,V>.preallocateFor(newSize: Int) {ensureCapacity(newSize + newSize/4) }
context(ka: ValueIntAdapter<K>) inline operator fun <K,V> MutableMapVIntObj<K,V>.set(key: K, value: V): Boolean = setBits(ka.toInt(key), value, null) != null
context(ka: ValueIntAdapter<K>) inline fun <K,V> MutableMapVIntObj<K,V>.set(key: K, value: V, defaultReturn: V): V? = setBits(ka.toInt(key), value, defaultReturn)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MutableMapVIntObj<K,V>.getOrPut(key: K, crossinline defaultValue: ()->V):V = getOrPutBits(ka.toInt(key)) {defaultValue()}
inline fun <K,V> MutableMapVIntObj<K,V>.putAll(source: MapVIntObj<K,V>) {preallocateFor(size+source.size + (size+source.size)/4); source.forEachBits { k, v-> setBits(k,v,null)} }
context(ka: ValueIntAdapter<K>, ska: ValueIntAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVIntObj<K,V>.putAll(source: MapVIntInt<SK,SV>, crossinline keySelector: (PairVIntInt<SK,SV>) -> K, crossinline valueTransform: (PairVIntInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVIntObj<K,V>.putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVIntObj<K,V>.putAll(source: CollectionVInt<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueIntAdapter<K>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVIntObj<K,V>.putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueIntAdapter<K>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVIntObj<K,V>.putAll(source: CollectionVLong<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueIntAdapter<K>) inline fun <K,V,S> MutableMapVIntObj<K,V>.putAllGeneric(source: Collection<S>, crossinline transform: (S) -> Pair<K, V>) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
inline operator fun <K,V> MutableMapVIntObj<K,V>.plusAssign(source: MapVIntObj<K,V>) = putAll(source)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MutableMapVIntObj<K,V>.remove(key: K) = removeBits(ka.toInt(key))
context(ka: ValueIntAdapter<K>) inline fun <K,V> MutableMapVIntObj<K,V>.remove(key: K, value:V): Boolean = removeBits(ka.toInt(key), value)
context(ka: ValueIntAdapter<K>) inline fun <K,V> MutableMapVIntObj<K,V>.removeIf(crossinline predicate:(K, V)->Boolean) = removeIfBits { k, v-> predicate(ka.fromInt(k), v)}
context(ka: ValueIntAdapter<K>) inline operator fun <K,V> MutableMapVIntObj<K,V>.minusAssign(key: K) {remove(key)}


class HashMapVIntObj<K,V>(val collection: MutableIntObjectMap<V> =MutableIntObjectMap<V>(), override val NULL_KEY_BITS: IntKeyBits=Int.MIN_VALUE)
    : MutableMapVIntObj<K,V> {
    constructor(size: Int, NO_VALUE: IntKeyBits=Int.MIN_VALUE) : this(MutableIntObjectMap(size), NO_VALUE)

    override val size: Int get() = collection.size
    override inline fun getBits(k: IntKeyBits): V? = collection.get(k)
    override inline fun anyBits(predicate: (IntKeyBits, V) -> Boolean): IntKeyBits {
        val searcher = object: (IntKeyBits, V) -> Boolean {
            var result:IntKeyBits = NULL_KEY_BITS
            override inline fun invoke(k: IntKeyBits, v: V): Boolean {
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
    override inline fun setBits(k: IntKeyBits, v: V, defaultReturn: V?): V? = collection.put(k,v) ?: defaultReturn
    override inline fun getOrPutBits(k: IntKeyBits, defaultSet: () -> V): V = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: IntKeyBits) = collection.remove(k)
    override inline fun removeBits(k: IntKeyBits, v:V): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (IntKeyBits, V) -> Boolean) = collection.removeIf(predicate)
    context(ka: ValueIntAdapter<K>) override inline fun asIterable(): MutableIterable<Pair<K,V>> = throw NotImplementedError()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString(): String = collection.toString()
}
