@file:Suppress("NOTHING_TO_INLINE", "unused", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.IntIntMap
import androidx.collection.IntList
import androidx.collection.IntSet
import androidx.collection.LongIntMap
import androidx.collection.LongList
import androidx.collection.LongSet
import androidx.collection.MutableIntIntMap
import androidx.collection.MutableLongIntMap
import kotlin.hashCode
import kotlin.toString

// inline wrappers around androidx.collection.IntIntMap, LongIntMap, IntLongMap, and LongLongMap

// IntIntMap -> VIntVIntMap
private val EmptyVIntVIntMap: VIntVIntMap<Nothing,Nothing> = MutableVIntVIntMap(0)
@Suppress("UNCHECKED_CAST")
fun <K,V>emptyVIntVIntMap(): VIntVIntMap<K,V> = EmptyVIntVIntMap as VIntVIntMap<K,V>
@Suppress("UNCHECKED_CAST")
fun <K,V>vIntVIntMapOf(): VIntVIntMap<K,V> = EmptyVIntVIntMap as VIntVIntMap<K,V>
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vIntVIntMapOf(key1: K, value1: V): VIntVIntMap<K,V> 
    = MutableVIntVIntMap<K,V>().also { map -> map[key1] = value1 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vIntVIntMapOf(key1: K, value1: V, key2: K, value2: V): VIntVIntMap<K,V> 
    = MutableVIntVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vIntVIntMapOf(key1: K, value1: V,  key2: K, value2: V, key3: K, value3: V): VIntVIntMap<K,V> =
    MutableVIntVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vIntVIntMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V): VIntVIntMap<K,V> =
    MutableVIntVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vIntVIntMapOf( key1: K, value1: V, key2: K, value2: V,  key3: K, value3: V, key4: K, value4: V, key5: K, value5: V): VIntVIntMap<K,V> =
    MutableVIntVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4; map[key5] = value5 }

inline fun <K,V>mutableVIntVIntMapOf(): MutableVIntVIntMap<K,V> = MutableVIntVIntMap()
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVIntVIntMapOf(key1: K, value1: V): MutableVIntVIntMap<K,V> =
    MutableVIntVIntMap<K,V>().also { map -> map[key1] = value1 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVIntVIntMapOf(key1: K, value1: V, key2: K, value2: V): MutableVIntVIntMap<K,V> =
    MutableVIntVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2  }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVIntVIntMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V) : MutableVIntVIntMap<K,V> =
    MutableVIntVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVIntVIntMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V) : MutableVIntVIntMap<K,V> =
    MutableVIntVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVIntVIntMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V, key5: K, value5: V) : MutableVIntVIntMap<K,V> =
    MutableVIntVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4; map[key5] = value5 }
inline fun <K,V>buildVIntVIntMap(builderAction: MutableVIntVIntMap<K,V>.() -> Unit): VIntVIntMap<K,V> = MutableVIntVIntMap<K,V>().apply(builderAction)
inline fun <K,V>buildVIntVIntMap(initialCapacity: Int, builderAction: MutableVIntVIntMap<K,V>.() -> Unit): VIntVIntMap<K,V> = MutableVIntVIntMap<K,V>(initialCapacity).apply(builderAction)

interface VIntVIntMap<K,V> { val collection: IntIntMap }
inline val <K,V> VIntVIntMap<K,V>.capacity: Int inline get() = collection.capacity
inline val <K,V> VIntVIntMap<K,V>.size: Int inline get() = collection.size
inline fun <K,V> VIntVIntMap<K,V>.any() = collection.any()
inline fun <K,V> VIntVIntMap<K,V>.none() = collection.none()
inline fun <K,V> VIntVIntMap<K,V>.isEmpty() = collection.isEmpty()
inline fun <K,V> VIntVIntMap<K,V>.isNotEmpty() = collection.isNotEmpty()
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>) 
inline operator fun <K,V> VIntVIntMap<K,V>.get(key: K): V? = va.fromInt(collection[ka.toInt(key)])
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVIntMap<K,V>.getOrDefault(key: K, defaultValue:V): V = va.fromInt(collection.getOrDefault(ka.toInt(key), va.toInt(defaultValue)))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVIntMap<K,V>.getOrElse(key: K, defaultValue: () -> V): V = va.fromInt(collection.getOrElse(ka.toInt(key), {va.toInt(defaultValue())}))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVIntMap<K,V>.forEach(block: (key: K, value: V) -> Unit) = collection.forEach {k,v-> block(ka.fromInt(k), va.fromInt(v))}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVIntMap<K,V>.forEachKey(block: (key: K) -> Unit) = collection.forEachKey {k-> block(ka.fromInt(k))}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVIntMap<K,V>.forEachValue(block: (value: V) -> Unit) = collection.forEachValue {v-> block(va.fromInt(v))}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVIntMap<K,V>.all(predicate: (K, V) -> Boolean): Boolean = collection.all {k,v-> predicate(ka.fromInt(k), va.fromInt(v))}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVIntMap<K,V>.any(predicate: (K, V) -> Boolean): Boolean = collection.any {k,v-> predicate(ka.fromInt(k), va.fromInt(v))}
inline fun <K,V> VIntVIntMap<K,V>.count() = collection.count()
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVIntMap<K,V>.count(predicate: (K, V) -> Boolean): Int = collection.count {k,v-> predicate(ka.fromInt(k), va.fromInt(v))}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVIntMap<K,V>.contains(key: K) = collection.contains(ka.toInt(key))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVIntMap<K,V>.containsValue(value: V): Boolean = collection.containsValue(va.toInt(value))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVIntMap<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...")
        = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> ka.fromInt(k).toString()+"="+va.fromInt(v).toString()}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVIntMap<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "",  postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: (key: K, value: V) -> CharSequence, )
        = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> transform(ka.fromInt(k), va.fromInt(v))}

class MutableVIntVIntMap<K,V>(override val collection: MutableIntIntMap = MutableIntIntMap()): VIntVIntMap<K,V> {
    constructor(capacity: Int) : this(MutableIntIntMap(capacity))

    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun getOrPut(key: K, defaultValue: () -> V): Int = collection.getOrPut(ka.toInt(key)) { va.toInt(defaultValue()) }
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline operator fun set(key: K, value: V) = collection.set(ka.toInt(key), va.toInt(value))
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun put(key: K, value: V) = collection.put(ka.toInt(key), va.toInt(value))
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun put(key: K, value: V, default: V) = collection.put(ka.toInt(key), va.toInt(value), va.toInt(default))
    inline fun putAll(from: VIntVIntMap<K,V>) = collection.putAll(from.collection)
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline operator fun plusAssign(from: VIntVIntMap<K,V>): Unit = putAll(from)
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun remove(key: K) = collection.remove(ka.toInt(key))
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun remove(key: K, value: V) = collection.remove(ka.toInt(key), va.toInt(value))
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun removeIf(predicate: (K, V) -> Boolean) = collection.removeIf { k, v -> predicate(ka.fromInt(k), va.fromInt(v)) }
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline operator fun minusAssign(key: K): Unit = collection.minusAssign(ka.toInt(key))
    inline operator fun minusAssign(@Suppress("ArrayReturn") keys: IntArray) = collection.minusAssign(keys)  // TODO: Add VIntArray/VIntCollection
    inline fun minusAssign(keys: IntSet) = collection.minusAssign(keys)
    inline fun minusAssign(keys: IntList) = collection.minusAssign(keys)
    inline fun clear() = collection.clear()
    inline fun trim() = collection.trim()
    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = collection == other
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun toString() = joinToString(", ","{","}")
}


// LongIntMap -> VLongVIntMap
private val EmptyVLongVIntMap: VLongVIntMap<Nothing,Nothing> = MutableVLongVIntMap(0)
@Suppress("UNCHECKED_CAST")
fun <K,V>emptyVLongVIntMap(): VLongVIntMap<K,V> = EmptyVLongVIntMap as VLongVIntMap<K,V>
@Suppress("UNCHECKED_CAST")
fun <K,V>vLongVIntMapOf(): VLongVIntMap<K,V> = EmptyVLongVIntMap as VLongVIntMap<K,V>
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vLongVIntMapOf(key1: K, value1: V): VLongVIntMap<K,V>
        = MutableVLongVIntMap<K,V>().also { map -> map[key1] = value1 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vLongVIntMapOf(key1: K, value1: V, key2: K, value2: V): VLongVIntMap<K,V>
        = MutableVLongVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vLongVIntMapOf(key1: K, value1: V,  key2: K, value2: V, key3: K, value3: V): VLongVIntMap<K,V> =
    MutableVLongVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vLongVIntMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V): VLongVIntMap<K,V> =
    MutableVLongVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vLongVIntMapOf( key1: K, value1: V, key2: K, value2: V,  key3: K, value3: V, key4: K, value4: V, key5: K, value5: V): VLongVIntMap<K,V> =
    MutableVLongVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4; map[key5] = value5 }

inline fun <K,V>mutableVLongVIntMapOf(): MutableVLongVIntMap<K,V> = MutableVLongVIntMap()
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVLongVIntMapOf(key1: K, value1: V): MutableVLongVIntMap<K,V> =
    MutableVLongVIntMap<K,V>().also { map -> map[key1] = value1 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVLongVIntMapOf(key1: K, value1: V, key2: K, value2: V): MutableVLongVIntMap<K,V> =
    MutableVLongVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2  }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVLongVIntMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V) : MutableVLongVIntMap<K,V> =
    MutableVLongVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVLongVIntMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V) : MutableVLongVIntMap<K,V> =
    MutableVLongVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVLongVIntMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V, key5: K, value5: V) : MutableVLongVIntMap<K,V> =
    MutableVLongVIntMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4; map[key5] = value5 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>buildVLongVIntMap(builderAction: MutableVLongVIntMap<K,V>.() -> Unit): VLongVIntMap<K,V> = MutableVLongVIntMap<K,V>().apply(builderAction)
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>buildVLongVIntMap(initialCapacity: Int, builderAction: MutableVLongVIntMap<K,V>.() -> Unit): VLongVIntMap<K,V> = MutableVLongVIntMap<K,V>(initialCapacity).apply(builderAction)

interface VLongVIntMap<K,V> { val collection: LongIntMap }
inline val <K,V> VLongVIntMap<K,V>.capacity: Int inline get() = collection.capacity
inline val <K,V> VLongVIntMap<K,V>.size: Int inline get() = collection.size
inline fun <K,V> VLongVIntMap<K,V>.any() = collection.any()
inline fun <K,V> VLongVIntMap<K,V>.none() = collection.none()
inline fun <K,V> VLongVIntMap<K,V>.isEmpty() = collection.isEmpty()
inline fun <K,V> VLongVIntMap<K,V>.isNotEmpty() = collection.isNotEmpty()
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline operator fun <K,V> VLongVIntMap<K,V>.get(key: K): V? = va.fromInt(collection[ka.toLong(key)])
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVIntMap<K,V>.getOrDefault(key: K, defaultValue:V): V = va.fromInt(collection.getOrDefault(ka.toLong(key), va.toInt(defaultValue)))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVIntMap<K,V>.getOrElse(key: K, defaultValue: () -> V): V = va.fromInt(collection.getOrElse(ka.toLong(key), {va.toInt(defaultValue())}))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVIntMap<K,V>.forEach(block: (key: K, value: V) -> Unit) = collection.forEach {k,v-> block(ka.fromLong(k), va.fromInt(v))}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVIntMap<K,V>.forEachKey(block: (key: K) -> Unit) = collection.forEachKey {k-> block(ka.fromLong(k))}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVIntMap<K,V>.forEachValue(block: (value: V) -> Unit) = collection.forEachValue {v-> block(va.fromInt(v))}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVIntMap<K,V>.all(predicate: (K, V) -> Boolean): Boolean = collection.all {k,v-> predicate(ka.fromLong(k), va.fromInt(v))}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVIntMap<K,V>.any(predicate: (K, V) -> Boolean): Boolean = collection.any {k,v-> predicate(ka.fromLong(k), va.fromInt(v))}
inline fun <K,V> VLongVIntMap<K,V>.count() = collection.count()
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVIntMap<K,V>.count(predicate: (K, V) -> Boolean): Int = collection.count {k,v-> predicate(ka.fromLong(k), va.fromInt(v))}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVIntMap<K,V>.contains(key: K) = collection.contains(ka.toLong(key))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVIntMap<K,V>.containsValue(value: V): Boolean = collection.containsValue(va.toInt(value))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVIntMap<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...")
        = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> ka.fromLong(k).toString()+"="+va.fromInt(v).toString()}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVIntMap<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "",  postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: (key: K, value: V) -> CharSequence, )
        = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> transform(ka.fromLong(k), va.fromInt(v))}

class MutableVLongVIntMap<K,V>(override val collection: MutableLongIntMap = MutableLongIntMap()): VLongVIntMap<K,V> {
    constructor(capacity: Int) : this(MutableLongIntMap(capacity))
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun getOrPut(key: K, defaultValue: () -> V): Int = collection.getOrPut(ka.toLong(key)) { va.toInt(defaultValue()) }
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline operator fun set(key: K, value: V) = collection.set(ka.toLong(key), va.toInt(value))
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun put(key: K, value: V) = collection.put(ka.toLong(key), va.toInt(value))
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun put(key: K, value: V, default: V) = collection.put(ka.toLong(key), va.toInt(value), va.toInt(default))
    inline fun putAll(from: VLongVIntMap<K,V>) = collection.putAll(from.collection)
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline operator fun plusAssign(from: VLongVIntMap<K,V>): Unit = putAll(from)
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun remove(key: K) = collection.remove(ka.toLong(key))
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun remove(key: K, value: V) = collection.remove(ka.toLong(key), va.toInt(value))
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun removeIf(predicate: (K, V) -> Boolean) = collection.removeIf { k, v -> predicate(ka.fromLong(k), va.fromInt(v)) }
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline operator fun minusAssign(key: K): Unit = collection.minusAssign(ka.toLong(key))
    inline operator fun minusAssign(@Suppress("ArrayReturn") keys: LongArray) = collection.minusAssign(keys)  // TODO: Add VLongArray/VIntCollection
    inline fun minusAssign(keys: LongSet) = collection.minusAssign(keys)
    inline fun minusAssign(keys: LongList) = collection.minusAssign(keys)
    inline fun clear() = collection.clear()
    inline fun trim() = collection.trim()
    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = collection == other
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun toString() = joinToString(", ","{","}")
}



// IntLongMap -> VIntVLongMap
private val EmptyVIntVLongMap: VIntVLongMap<Nothing,Nothing> = MutableVIntVLongMap(0)
@Suppress("UNCHECKED_CAST")
fun <K,V>emptyVIntVLongMap(): VIntVLongMap<K,V> = EmptyVIntVLongMap as VIntVLongMap<K,V>
@Suppress("UNCHECKED_CAST")
fun <K,V>vIntVLongMapOf(): VIntVLongMap<K,V> = EmptyVIntVLongMap as VIntVLongMap<K,V>
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vIntVLongMapOf(key1: K, value1: V): VIntVLongMap<K,V>
        = MutableVIntVLongMap<K,V>().also { map -> map[key1] = value1 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vIntVLongMapOf(key1: K, value1: V, key2: K, value2: V): VIntVLongMap<K,V>
        = MutableVIntVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vIntVLongMapOf(key1: K, value1: V,  key2: K, value2: V, key3: K, value3: V): VIntVLongMap<K,V> =
    MutableVIntVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vIntVLongMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V): VIntVLongMap<K,V> =
    MutableVIntVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vIntVLongMapOf( key1: K, value1: V, key2: K, value2: V,  key3: K, value3: V, key4: K, value4: V, key5: K, value5: V): VIntVLongMap<K,V> =
    MutableVIntVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4; map[key5] = value5 }

inline fun <K,V>mutableVIntVLongMapOf(): MutableVIntVLongMap<K,V> = MutableVIntVLongMap()
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVIntVLongMapOf(key1: K, value1: V): MutableVIntVLongMap<K,V> =
    MutableVIntVLongMap<K,V>().also { map -> map[key1] = value1 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVIntVLongMapOf(key1: K, value1: V, key2: K, value2: V): MutableVIntVLongMap<K,V> =
    MutableVIntVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2  }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVIntVLongMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V) : MutableVIntVLongMap<K,V> =
    MutableVIntVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVIntVLongMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V) : MutableVIntVLongMap<K,V> =
    MutableVIntVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVIntVLongMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V, key5: K, value5: V) : MutableVIntVLongMap<K,V> =
    MutableVIntVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4; map[key5] = value5 }
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>buildVIntVLongMap(builderAction: MutableVIntVLongMap<K,V>.() -> Unit): VIntVLongMap<K,V> = MutableVIntVLongMap<K,V>().apply(builderAction)
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>buildVIntVLongMap(initialCapacity: Int, builderAction: MutableVIntVLongMap<K,V>.() -> Unit): VIntVLongMap<K,V> = MutableVIntVLongMap<K,V>(initialCapacity).apply(builderAction)

interface VIntVLongMap<K,V> { val collection: IntIntMap }
inline val <K,V> VIntVLongMap<K,V>.capacity: Int inline get() = collection.capacity
inline val <K,V> VIntVLongMap<K,V>.size: Int inline get() = collection.size
inline fun <K,V> VIntVLongMap<K,V>.any() = collection.any()
inline fun <K,V> VIntVLongMap<K,V>.none() = collection.none()
inline fun <K,V> VIntVLongMap<K,V>.isEmpty() = collection.isEmpty()
inline fun <K,V> VIntVLongMap<K,V>.isNotEmpty() = collection.isNotEmpty()
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline operator fun <K,V> VIntVLongMap<K,V>.get(key: K): V? = va.fromInt(collection[ka.toInt(key)])
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVLongMap<K,V>.getOrDefault(key: K, defaultValue:V): V = va.fromInt(collection.getOrDefault(ka.toInt(key), va.toInt(defaultValue)))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVLongMap<K,V>.getOrElse(key: K, defaultValue: () -> V): V = va.fromInt(collection.getOrElse(ka.toInt(key), {va.toInt(defaultValue())}))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVLongMap<K,V>.forEach(block: (key: K, value: V) -> Unit) = collection.forEach {k,v-> block(ka.fromInt(k), va.fromInt(v))}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVLongMap<K,V>.forEachKey(block: (key: K) -> Unit) = collection.forEachKey {k-> block(ka.fromInt(k))}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVLongMap<K,V>.forEachValue(block: (value: V) -> Unit) = collection.forEachValue {v-> block(va.fromInt(v))}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVLongMap<K,V>.all(predicate: (K, V) -> Boolean): Boolean = collection.all {k,v-> predicate(ka.fromInt(k), va.fromInt(v))}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVLongMap<K,V>.any(predicate: (K, V) -> Boolean): Boolean = collection.any {k,v-> predicate(ka.fromInt(k), va.fromInt(v))}
inline fun <K,V> VIntVLongMap<K,V>.count() = collection.count()
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVLongMap<K,V>.count(predicate: (K, V) -> Boolean): Int = collection.count {k,v-> predicate(ka.fromInt(k), va.fromInt(v))}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVLongMap<K,V>.contains(key: K) = collection.contains(ka.toInt(key))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVLongMap<K,V>.containsValue(value: V): Boolean = collection.containsValue(va.toInt(value))
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVLongMap<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...")
        = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> ka.fromInt(k).toString()+"="+va.fromInt(v).toString()}
context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VIntVLongMap<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "",  postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: (key: K, value: V) -> CharSequence, )
        = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> transform(ka.fromInt(k), va.fromInt(v))}

class MutableVIntVLongMap<K,V>(override val collection: MutableIntIntMap = MutableIntIntMap()): VIntVLongMap<K,V> {
    constructor(capacity: Int) : this(MutableIntIntMap(capacity))
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun getOrPut(key: K, defaultValue: () -> V): Int = collection.getOrPut(ka.toInt(key)) { va.toInt(defaultValue()) }
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline operator fun set(key: K, value: V) = collection.set(ka.toInt(key), va.toInt(value))
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun put(key: K, value: V) = collection.put(ka.toInt(key), va.toInt(value))
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun put(key: K, value: V, default: V) = collection.put(ka.toInt(key), va.toInt(value), va.toInt(default))
    inline fun putAll(from: VIntVLongMap<K,V>) = collection.putAll(from.collection)
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline operator fun plusAssign(from: VIntVLongMap<K,V>): Unit = putAll(from)
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun remove(key: K) = collection.remove(ka.toInt(key))
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun remove(key: K, value: V) = collection.remove(ka.toInt(key), va.toInt(value))
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun removeIf(predicate: (K, V) -> Boolean) = collection.removeIf { k, v -> predicate(ka.fromInt(k), va.fromInt(v)) }
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline operator fun minusAssign(key: K): Unit = collection.minusAssign(ka.toInt(key))
    inline operator fun minusAssign(@Suppress("ArrayReturn") keys: IntArray) = collection.minusAssign(keys)  // TODO: Add VIntArray/VIntCollection
    inline fun minusAssign(keys: IntSet) = collection.minusAssign(keys)
    inline fun minusAssign(keys: IntList) = collection.minusAssign(keys)
    inline fun clear() = collection.clear()
    inline fun trim() = collection.trim()
    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = collection == other
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>)
    inline fun toString() = joinToString(", ","{","}")
}


// LongIntMap -> VLongVLongMap
private val EmptyVLongVLongMap: VLongVLongMap<Nothing,Nothing> = MutableVLongVLongMap(0)
@Suppress("UNCHECKED_CAST")
fun <K,V>emptyVLongVLongMap(): VLongVLongMap<K,V> = EmptyVLongVLongMap as VLongVLongMap<K,V>
@Suppress("UNCHECKED_CAST")
fun <K,V>vLongVLongMapOf(): VLongVLongMap<K,V> = EmptyVLongVLongMap as VLongVLongMap<K,V>
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vLongVLongMapOf(key1: K, value1: V): VLongVLongMap<K,V>
        = MutableVLongVLongMap<K,V>().also { map -> map[key1] = value1 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vLongVLongMapOf(key1: K, value1: V, key2: K, value2: V): VLongVLongMap<K,V>
        = MutableVLongVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vLongVLongMapOf(key1: K, value1: V,  key2: K, value2: V, key3: K, value3: V): VLongVLongMap<K,V> =
    MutableVLongVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vLongVLongMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V): VLongVLongMap<K,V> =
    MutableVLongVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>vLongVLongMapOf( key1: K, value1: V, key2: K, value2: V,  key3: K, value3: V, key4: K, value4: V, key5: K, value5: V): VLongVLongMap<K,V> =
    MutableVLongVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4; map[key5] = value5 }

inline fun <K,V>mutableVLongVLongMapOf(): MutableVLongVLongMap<K,V> = MutableVLongVLongMap()
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVLongVLongMapOf(key1: K, value1: V): MutableVLongVLongMap<K,V> =
    MutableVLongVLongMap<K,V>().also { map -> map[key1] = value1 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVLongVLongMapOf(key1: K, value1: V, key2: K, value2: V): MutableVLongVLongMap<K,V> =
    MutableVLongVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2  }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVLongVLongMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V) : MutableVLongVLongMap<K,V> =
    MutableVLongVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVLongVLongMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V) : MutableVLongVLongMap<K,V> =
    MutableVLongVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>mutableVLongVLongMapOf(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V, key5: K, value5: V) : MutableVLongVLongMap<K,V> =
    MutableVLongVLongMap<K,V>().also { map -> map[key1] = value1; map[key2] = value2; map[key3] = value3; map[key4] = value4; map[key5] = value5 }
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>buildVLongVLongMap(builderAction: MutableVLongVLongMap<K,V>.() -> Unit): VLongVLongMap<K,V> = MutableVLongVLongMap<K,V>().apply(builderAction)
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V>buildVLongVLongMap(initialCapacity: Int, builderAction: MutableVLongVLongMap<K,V>.() -> Unit): VLongVLongMap<K,V> = MutableVLongVLongMap<K,V>(initialCapacity).apply(builderAction)

interface VLongVLongMap<K,V> { val collection: LongIntMap }
inline val <K,V> VLongVLongMap<K,V>.capacity: Int inline get() = collection.capacity
inline val <K,V> VLongVLongMap<K,V>.size: Int inline get() = collection.size
inline fun <K,V> VLongVLongMap<K,V>.any() = collection.any()
inline fun <K,V> VLongVLongMap<K,V>.none() = collection.none()
inline fun <K,V> VLongVLongMap<K,V>.isEmpty() = collection.isEmpty()
inline fun <K,V> VLongVLongMap<K,V>.isNotEmpty() = collection.isNotEmpty()
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline operator fun <K,V> VLongVLongMap<K,V>.get(key: K): V? = va.fromInt(collection[ka.toLong(key)])
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVLongMap<K,V>.getOrDefault(key: K, defaultValue:V): V = va.fromInt(collection.getOrDefault(ka.toLong(key), va.toInt(defaultValue)))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVLongMap<K,V>.getOrElse(key: K, defaultValue: () -> V): V = va.fromInt(collection.getOrElse(ka.toLong(key), {va.toInt(defaultValue())}))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVLongMap<K,V>.forEach(block: (key: K, value: V) -> Unit) = collection.forEach {k,v-> block(ka.fromLong(k), va.fromInt(v))}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVLongMap<K,V>.forEachKey(block: (key: K) -> Unit) = collection.forEachKey {k-> block(ka.fromLong(k))}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVLongMap<K,V>.forEachValue(block: (value: V) -> Unit) = collection.forEachValue {v-> block(va.fromInt(v))}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVLongMap<K,V>.all(predicate: (K, V) -> Boolean): Boolean = collection.all {k,v-> predicate(ka.fromLong(k), va.fromInt(v))}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVLongMap<K,V>.any(predicate: (K, V) -> Boolean): Boolean = collection.any {k,v-> predicate(ka.fromLong(k), va.fromInt(v))}
inline fun <K,V> VLongVLongMap<K,V>.count() = collection.count()
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVLongMap<K,V>.count(predicate: (K, V) -> Boolean): Int = collection.count {k,v-> predicate(ka.fromLong(k), va.fromInt(v))}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVLongMap<K,V>.contains(key: K) = collection.contains(ka.toLong(key))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVLongMap<K,V>.containsValue(value: V): Boolean = collection.containsValue(va.toInt(value))
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVLongMap<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...")
        = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> ka.fromLong(k).toString()+"="+va.fromInt(v).toString()}
context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
inline fun <K,V> VLongVLongMap<K,V>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "",  postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: (key: K, value: V) -> CharSequence, )
        = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> transform(ka.fromLong(k), va.fromInt(v))}

class MutableVLongVLongMap<K,V>(override val collection: MutableLongIntMap = MutableLongIntMap()): VLongVLongMap<K,V> {
    constructor(capacity: Int) : this(MutableLongIntMap(capacity))
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun getOrPut(key: K, defaultValue: () -> V): Int = collection.getOrPut(ka.toLong(key)) { va.toInt(defaultValue()) }
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline operator fun set(key: K, value: V) = collection.set(ka.toLong(key), va.toInt(value))
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun put(key: K, value: V) = collection.put(ka.toLong(key), va.toInt(value))
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun put(key: K, value: V, default: V) = collection.put(ka.toLong(key), va.toInt(value), va.toInt(default))
    inline fun putAll(from: VLongVLongMap<K,V>) = collection.putAll(from.collection)
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline operator fun plusAssign(from: VLongVLongMap<K,V>): Unit = putAll(from)
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun remove(key: K) = collection.remove(ka.toLong(key))
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun remove(key: K, value: V) = collection.remove(ka.toLong(key), va.toInt(value))
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun removeIf(predicate: (K, V) -> Boolean) = collection.removeIf { k, v -> predicate(ka.fromLong(k), va.fromInt(v)) }
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline operator fun minusAssign(key: K): Unit = collection.minusAssign(ka.toLong(key))
    inline operator fun minusAssign(@Suppress("ArrayReturn") keys: LongArray) = collection.minusAssign(keys)  // TODO: Add VLongArray/VIntCollection
    inline fun minusAssign(keys: LongSet) = collection.minusAssign(keys)
    inline fun minusAssign(keys: LongList) = collection.minusAssign(keys)
    inline fun clear() = collection.clear()
    inline fun trim() = collection.trim()
    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = collection == other
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>)
    inline fun toString() = joinToString(", ","{","}")
}