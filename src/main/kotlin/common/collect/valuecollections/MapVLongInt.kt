@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.MutableLongIntMap

interface MapVLongInt<K,V> {
    // Many operations require a NULL_VALUE in order to return an "Optional" result without a heap allocation.
    val NULL_KEY_BITS: LongKeyBits
    val NULL_VALUE_BITS: IntValueBits

    val size: Int
    fun getBits(k: LongKeyBits): IntValueBits
    fun anyBits(predicate: (LongKeyBits, IntValueBits) -> Boolean): LongKeyBits

    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) fun asIterable(): Iterable<PairVLongInt<K,V>>

    @JvmName("toVString") @Suppress("INAPPLICABLE_JVM_NAME")
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) fun toString(): String = toVString()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)  inline fun <K,V> MapVLongInt<K,V>.asMapGeneric(): Map<K,V> = object: Map<K,V> {
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
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongInt<K,V>.keyFromLong(bits: LongKeyBits): K = if (bits==NULL_KEY_BITS) throw NoSuchElementException() else ka.fromLong(bits)
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongInt<K,V>.keyFromLongOr(bits: LongKeyBits, provider: ()->K): K = if (bits==NULL_KEY_BITS) provider() else ka.fromLong(bits)
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongInt<K,V>.keyFromLongOrNull(bits: LongKeyBits): K? = if (bits==NULL_KEY_BITS) null else ka.fromLong(bits)
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.valueFromInt(bits: IntValueBits): V = if (bits==NULL_VALUE_BITS) throw NoSuchElementException() else va.fromInt(bits)
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.valueFromIntOr(bits: IntValueBits, provider: ()->V): V = if (bits==NULL_VALUE_BITS) provider() else va.fromInt(bits)
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.valueFromIntOrNull(bits: IntValueBits): V? = if (bits==NULL_VALUE_BITS) null else va.fromInt(bits)
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline operator fun <K,V> MapVLongInt<K,V>.get(key: K): V = valueFromInt(getBits(ka.toLong(key)))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.getOr(key: K, defaultResult:()->V): V = valueFromIntOr(getBits(ka.toLong(key)), defaultResult)
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.getOrNull(key: K): V? = valueFromIntOrNull(getBits(ka.toLong(key)))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.any(crossinline predicate:(K, V)->Boolean):K = keyFromLong(anyBits{ k, v-> predicate(ka.fromLong(k), va.fromInt(v))})
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.anyOr(crossinline predicate:(K, V)->Boolean, defaultResult:()->K):K = keyFromLongOr(anyBits{ k, v-> predicate(ka.fromLong(k), va.fromInt(v))}, defaultResult)
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.anyOrNull(crossinline predicate:(K, V)->Boolean):K? = keyFromLongOrNull(anyBits{ k, v-> predicate(ka.fromLong(k), va.fromInt(v))})
inline fun <K,V> MapVLongInt<K,V>.anyIndexedBits(crossinline predicate:(index:Int, LongKeyBits, IntValueBits)->Boolean):LongKeyBits {
    return anyBits(object: (LongKeyBits,IntValueBits) -> Boolean {
        var index = 0
        override inline fun invoke(k: LongKeyBits, v:IntValueBits) = predicate(index++, k,v)
    } )
}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.anyIndexed(crossinline action:(index:Int, K, V)->Boolean):K = keyFromLong(anyIndexedBits{ index, k, v-> action(index, ka.fromLong(k), va.fromInt(v))})
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.anyIndexedOr(crossinline action:(index:Int, K, V)->Boolean, defaultResult:()->K):K = keyFromLongOr(anyIndexedBits{ index, k, v-> action(index, ka.fromLong(k), va.fromInt(v))}, defaultResult)
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.anyIndexedOrNull(crossinline action:(index:Int, K, V)->Boolean):K? = keyFromLongOrNull(anyIndexedBits{ index, k, v-> action(index, ka.fromLong(k), va.fromInt(v))})
inline fun <K,V> MapVLongInt<K,V>.forEachBits(crossinline action:(LongKeyBits, IntValueBits)->Unit) {anyBits { k, v-> action(k,v); false} }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.forEach(crossinline action:(K, V)->Unit) = forEachBits { k, v-> action(ka.fromLong(k), va.fromInt(v)) }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.forEachPair(crossinline action:(PairVLongInt<K,V>)->Unit)  {
    forEachBits(object: (LongKeyBits, IntValueBits) -> Unit {
        val pair: PairVLongInt<K,V> = PairVLongInt(NULL_KEY_BITS, NULL_VALUE_BITS)
        override inline fun invoke(k: LongKeyBits, v: IntValueBits) { pair.firstBits = k; pair.secondBits = v; action(pair) }
    })
}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.forEachIndexed(crossinline action:(index:Int, K, V)->Unit) {
    forEachBits(object: (LongKeyBits, IntValueBits) -> Unit {
        var index=0
        override inline fun invoke(k: LongKeyBits, v: IntValueBits) = action(index++, ka.fromLong(k), va.fromInt(v))
    })
}
inline val <K,V> MapVLongInt<K,V>.isEmpty get() = size == 0
inline fun <K,V> MapVLongInt<K,V>.isNotEmpty() = size > 0
context(ka: ValueLongAdapter<K>) inline fun <K,V> MapVLongInt<K,V>.containsKey(k: K) = getBits(ka.toLong(k)) != NULL_VALUE_BITS
context(va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.containsValue(findV: V) = anyBits { k, v-> v==findV} != NULL_KEY_BITS
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V, A : Appendable> MapVLongInt<K,V>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): A {
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
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: ((K, V) -> CharSequence) = { k, v-> "($k:$v)" }): String
        = joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MapVLongInt<K,V>.toVString() = joinToString(", ","{","}")



interface MutableMapVLongInt<K,V>: MapVLongInt<K,V> {
    fun ensureCapacity(newCapacity: Int): Boolean = false
    fun trim()
    fun clear()

    fun setBits(k: LongKeyBits, v: IntValueBits, defaultReturn: IntValueBits): IntValueBits
    fun getOrPutBits(k: LongKeyBits, defaultSet: () -> IntValueBits): IntValueBits
    fun removeBits(k: LongKeyBits)
    fun removeBits(k: LongKeyBits, v: IntValueBits):Boolean
    fun removeIfBits(predicate:(LongKeyBits,IntValueBits)->Boolean)
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) override fun asIterable(): MutableIterable<PairVLongInt<K,V>>

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override fun toString(): String // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
}
inline fun <K,V> MutableMapVLongInt<K,V>.preallocateFor(newSize: Int) {ensureCapacity(newSize + newSize/4) }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline operator fun <K,V> MutableMapVLongInt<K,V>.set(key: K, value: V): Boolean = setBits(ka.toLong(key), va.toInt(value), NULL_VALUE_BITS) != NULL_VALUE_BITS
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVLongInt<K,V>.set(key: K, value: V, defaultReturn: V): V = valueFromInt(setBits(ka.toLong(key), va.toInt(value), va.toInt(defaultReturn)))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVLongInt<K,V>.getOrPut(key: K, crossinline defaultValue: ()->V):V = valueFromInt(getOrPutBits(ka.toLong(key), {va.toInt(defaultValue())}))
inline fun <K,V> MutableMapVLongInt<K,V>.putAll(source: MapVLongInt<K,V>) {preallocateFor(size+source.size + (size+source.size)/4); source.forEachBits { k, v-> setBits(k,v, NULL_VALUE_BITS)} }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, ska: ValueIntAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVLongInt<K,V>.putAll(source: MapVIntInt<SK,SV>, crossinline keySelector: (PairVIntInt<SK,SV>) -> K, crossinline valueTransform: (PairVIntInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, ska: ValueIntAdapter<SK>, sva: ValueLongAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVLongInt<K,V>.putAll(source: MapVIntLong<SK,SV>, crossinline keySelector: (PairVIntLong<SK,SV>) -> K, crossinline valueTransform: (PairVIntLong<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, ska: ValueLongAdapter<SK>, sva: ValueIntAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVLongInt<K,V>.putAll(source: MapVLongInt<SK,SV>, crossinline keySelector: (PairVLongInt<SK,SV>) -> K, crossinline valueTransform: (PairVLongInt<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, ska: ValueLongAdapter<SK>, sva: ValueLongAdapter<SV>) inline fun <K,V,SK,SV> MutableMapVLongInt<K,V>.putAll(source: MapVLongLong<SK,SV>, crossinline keySelector: (PairVLongLong<SK,SV>) -> K, crossinline valueTransform: (PairVLongLong<SK,SV>) -> V) {preallocateFor(size+source.size); source.forEachPair { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVLongInt<K,V>.putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <K,V,S> MutableMapVLongInt<K,V>.putAll(source: CollectionVInt<S>, crossinline transform: (S) -> PairVLongInt<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVLongInt<K,V>.putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> set(keySelector(e), valueTransform(e))}}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <K,V,S> MutableMapVLongInt<K,V>.putAll(source: CollectionVLong<S>, crossinline transform: (S) -> PairVLongInt<K, V>) = context(sa) {preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V,S> MutableMapVLongInt<K,V>.putAllGeneric(source: Collection<S>, crossinline transform: (S) -> Pair<K, V>) { preallocateFor(size+source.size); source.forEach { e-> val p = transform(e); set(p.first, p.second)}}
inline operator fun <K,V> MutableMapVLongInt<K,V>.plusAssign(source: MapVLongInt<K,V>) = putAll(source)
context(ka: ValueLongAdapter<K>) inline fun <K,V> MutableMapVLongInt<K,V>.remove(key: K) = removeBits(ka.toLong(key))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVLongInt<K,V>.remove(key: K, value:V): Boolean = removeBits(ka.toLong(key), va.toInt(value))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) inline fun <K,V> MutableMapVLongInt<K,V>.removeIf(crossinline predicate:(K, V)->Boolean) = removeIfBits { k, v-> predicate(ka.fromLong(k), va.fromInt(v))}
context(ka: ValueLongAdapter<K>) inline operator fun <K,V> MutableMapVLongInt<K,V>.minusAssign(key: K) {remove(key)}




class HashMapVLongInt<K,V>(val collection: MutableLongIntMap=MutableLongIntMap(), override val NULL_KEY_BITS: LongKeyBits=Long.MIN_VALUE, override val NULL_VALUE_BITS: IntValueBits=Int.MIN_VALUE)
    : MutableMapVLongInt<K,V> {
    constructor(size: Int, NO_VALUE: LongKeyBits=Long.MIN_VALUE) : this(MutableLongIntMap(size), NO_VALUE)

    override val size: Int get() = collection.size
    override inline fun getBits(k: LongKeyBits): IntValueBits = collection.get(k)
    override inline fun anyBits(predicate: (LongKeyBits, IntValueBits) -> Boolean): LongKeyBits {
        val searcher = object: (LongKeyBits, IntValueBits) -> Boolean {
            var result:LongKeyBits = NULL_KEY_BITS
            override inline fun invoke(k: LongKeyBits, v: IntValueBits): Boolean {
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
    override inline fun setBits(k: LongKeyBits, v: IntValueBits, defaultReturn: IntValueBits): IntValueBits = collection.put(k,v, defaultReturn)
    override inline fun getOrPutBits(k: LongKeyBits, defaultSet: () -> IntValueBits): IntValueBits = collection.getOrPut(k, defaultSet)
    override inline fun removeBits(k: LongKeyBits) = collection.remove(k)
    override inline fun removeBits(k: LongKeyBits, v:IntValueBits): Boolean = collection.remove(k,v)
    override inline fun removeIfBits(predicate: (LongKeyBits, IntValueBits) -> Boolean) = collection.removeIf(predicate)
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>) override inline fun asIterable(): MutableIterable<PairVLongInt<K,V>> = throw NotImplementedError()

    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString(): String = collection.toString()
}

