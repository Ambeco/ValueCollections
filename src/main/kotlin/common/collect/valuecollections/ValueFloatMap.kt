@file:Suppress("NOTHING_TO_INLINE", "UNUSED", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.IntFloatMap
import androidx.collection.IntList
import androidx.collection.IntSet
import androidx.collection.LongFloatMap
import androidx.collection.LongList
import androidx.collection.LongSet
import androidx.collection.MutableIntFloatMap
import androidx.collection.MutableLongFloatMap
import androidx.collection.mutableIntFloatMapOf
import androidx.collection.mutableLongFloatMapOf
import kotlin.apply

// inline wrappers around androidx.collection.IntFloatMap and LongFloatMap

// IntFloatMap -> VIntFloatMap
private val EmptyVIntFloatMap:VIntFloatMap<Nothing> = MutVIntFloatMap()
@Suppress("UNCHECKED_CAST")
fun <K> emptyVIntFloatMap(): VIntFloatMap<K> = EmptyVIntFloatMap as VIntFloatMap<K>
@Suppress("UNCHECKED_CAST")
fun <K> vIntFloatMapOf(): VIntFloatMap<K> = EmptyVIntFloatMap as VIntFloatMap<K>
context(a: ValueIntAdapter<K>) inline fun <K> vIntFloatMapOf(key1:K,value1:Float): VIntFloatMap<K> =
    MutVIntFloatMap(mutableIntFloatMapOf(a.toInt(key1), value1))
context(a: ValueIntAdapter<K>) inline fun <K> vIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float): VIntFloatMap<K> =
    MutVIntFloatMap(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2))
context(a: ValueIntAdapter<K>) inline fun <K> vIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float): VIntFloatMap<K> =
    MutVIntFloatMap(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2, a.toInt(key3), value3))
context(a: ValueIntAdapter<K>) inline fun <K> vIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float): VIntFloatMap<K> =
    MutVIntFloatMap(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2, a.toInt(key3), value3, a.toInt(key4), value4))
context(a: ValueIntAdapter<K>) inline fun <K> vIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float, key5:K,value5:Float): VIntFloatMap<K> =
    MutVIntFloatMap(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2, a.toInt(key3), value3, a.toInt(key4), value4, a.toInt(key5), value5))

fun <K> mutVIntFloatMapOf() = MutVIntFloatMap<K>()
context(a: ValueIntAdapter<K>) inline fun <K> mutVIntFloatMapOf(key1:K,value1:Float) =
    MutVIntFloatMap<K>(mutableIntFloatMapOf(a.toInt(key1), value1))
context(a: ValueIntAdapter<K>) inline fun <K> mutVIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float) =
    MutVIntFloatMap<K>(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2))
context(a: ValueIntAdapter<K>) inline fun <K> mutVIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float) =
    MutVIntFloatMap<K>(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2, a.toInt(key3), value3))
context(a: ValueIntAdapter<K>) inline fun <K> mutVIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float) =
    MutVIntFloatMap<K>(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2, a.toInt(key3), value3, a.toInt(key4), value4))
context(a: ValueIntAdapter<K>) inline fun <K> mutVIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float, key5:K,value5:Float) =
    MutVIntFloatMap<K>(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2, a.toInt(key3), value3, a.toInt(key4), value4, a.toInt(key5), value5))

inline fun <K> buildVIntFloatMap(builderAction: MutVIntFloatMap<K>.() -> Unit): VIntFloatMap<K> = MutVIntFloatMap<K>().apply(builderAction)
inline fun <K> buildVIntFloatMap(initialCapacity:Int, builderAction: MutVIntFloatMap<K>.() -> Unit): VIntFloatMap<K> = MutVIntFloatMap<K>(initialCapacity).apply(builderAction)

interface VIntFloatMap<K> { val collection: IntFloatMap }
inline val <K> VIntFloatMap<K>.capacity inline get() = collection.capacity
inline val <K> VIntFloatMap<K>.size inline get() = collection.size
inline fun <K> VIntFloatMap<K>.any() = collection.any()
inline fun <K> VIntFloatMap<K>.none() = collection.none()
inline fun <K> VIntFloatMap<K>.isEmpty() = collection.isEmpty()
inline fun <K> VIntFloatMap<K>.isNotEmpty() = collection.isNotEmpty()
context(a: ValueIntAdapter<K>) inline operator fun <K> VIntFloatMap<K>.get(key: K) = collection[a.toInt(key)]
context(a: ValueIntAdapter<K>) inline fun <K> VIntFloatMap<K>.getOrDefault(key: K, defaultValue: Float) = collection.getOrDefault(a.toInt(key), defaultValue)
context(a: ValueIntAdapter<K>) inline fun <K> VIntFloatMap<K>.getOrElse(key: K, defaultValue: ()->Float) = collection.getOrElse(a.toInt(key), defaultValue)
context(a: ValueIntAdapter<K>) inline fun <K> VIntFloatMap<K>.forEach(block:(key:K,value:Float)->Unit) = collection.forEach{k,v->block(a.fromInt(k),v)}
context(a: ValueIntAdapter<K>) inline fun <K> VIntFloatMap<K>.forEachKey(block:(key:K)->Unit) = collection.forEachKey{block(a.fromInt(it))}
inline fun <K> VIntFloatMap<K>.forEachValue(block:(value:Float)->Unit) = collection.forEachValue{block(it)}
context(a: ValueIntAdapter<K>) inline fun <K> VIntFloatMap<K>.all(predicate: (K, Float) -> Boolean) = collection.all{k,v->predicate(a.fromInt(k),v)}
context(a: ValueIntAdapter<K>) inline fun <K> VIntFloatMap<K>.any(predicate: (K, Float) -> Boolean) = collection.any{k,v->predicate(a.fromInt(k),v)}
inline fun <K> VIntFloatMap<K>.count() = collection.count()
context(a: ValueIntAdapter<K>) inline fun <K> VIntFloatMap<K>.count(predicate: (K, Float) -> Boolean) = collection.count{k,v->predicate(a.fromInt(k),v)}
context(a: ValueIntAdapter<K>) inline fun <K> VIntFloatMap<K>.contains(key: K) = collection.contains(a.toInt(key))
context(a: ValueIntAdapter<K>) inline fun <K> VIntFloatMap<K>.containsKey(key: K) = collection.containsKey(a.toInt(key))
inline fun <K> VIntFloatMap<K>.containsValue(value: Float) = collection.containsValue(value)
context(a: ValueIntAdapter<K>) inline fun <K> VIntFloatMap<K>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...")
    = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> a.fromInt(k).toString()+"="+v.toString()} 
context(a: ValueIntAdapter<K>) inline fun <K> VIntFloatMap<K>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: (key: K, value: Float) -> CharSequence) 
    = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> transform(a.fromInt(k), v)}

class MutVIntFloatMap<K>(override val collection: MutableIntFloatMap = MutableIntFloatMap()): VIntFloatMap<K> {
    constructor(capacity: Int) : this(MutableIntFloatMap(capacity))
    context(a: ValueIntAdapter<K>) inline fun remove(key: K) = collection.remove(a.toInt(key))
    context(a: ValueIntAdapter<K>) inline fun getOrPut(key: K, defaultValue:()->Float) = collection.getOrPut(a.toInt(key),defaultValue)
    context(a: ValueIntAdapter<K>) inline fun set(key: K, value:Float) =  collection.set(a.toInt(key), value) 
    context(a: ValueIntAdapter<K>) inline fun put(key: K, value:Float) = collection.put(a.toInt(key),value)
    context(a: ValueIntAdapter<K>) inline fun put(key: K, value:Float, default:Float) = collection.put(a.toInt(key),value, default)
    inline fun putAll(from: VIntFloatMap<K>) = collection.putAll(from.collection)
    inline operator fun plusAssign(from: VIntFloatMap<K>) = collection.putAll(from.collection)
    context(a: ValueIntAdapter<K>) inline fun remove(key: K, value: Float) = collection.remove(a.toInt(key),value)
    context(a: ValueIntAdapter<K>) inline fun removeIf(predicate: (K, Float) -> Boolean) = collection.removeIf{k,v->predicate(a.fromInt(k),v)}
    context(a: ValueIntAdapter<K>) inline operator fun minusAssign(key: K):Unit = collection.minusAssign(a.toInt(key))
    inline operator fun minusAssign(keys: IntArray):Unit = collection.minusAssign(keys) // TODO: Add VIntArray/VIntCollection
    inline operator fun minusAssign(keys: IntSet):Unit = collection.minusAssign(keys)
    inline operator fun minusAssign(keys: IntList):Unit = collection.minusAssign(keys) 
    inline fun clear() = collection.clear()
    inline fun trim() = collection.trim()
    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = collection == other
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toString(K.ValueIntAdapter)"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
    context(a: ValueIntAdapter<K>) inline fun toString() = joinToString(", ","{","}")
}

// LongFloatMap -> VLongFloatMap
private val EmptyVLongFloatMap = MutVLongFloatMap<Nothing>()
@Suppress("UNCHECKED_CAST")
fun <K> emptyVLongFloatMap(): VLongFloatMap<K> = EmptyVLongFloatMap as VLongFloatMap<K>
@Suppress("UNCHECKED_CAST")
fun <K> vLongFloatMapOf(): VLongFloatMap<K> = EmptyVLongFloatMap as VLongFloatMap<K>
context(a: ValueLongAdapter<K>) inline fun <K> vLongFloatMapOf(key1:K,value1:Float): VLongFloatMap<K> =
    MutVLongFloatMap(mutableLongFloatMapOf(a.toLong(key1), value1))
context(a: ValueLongAdapter<K>) inline fun <K> vLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float): VLongFloatMap<K> =
    MutVLongFloatMap(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2))
context(a: ValueLongAdapter<K>) inline fun <K> vLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float): VLongFloatMap<K> =
    MutVLongFloatMap(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2, a.toLong(key3), value3))
context(a: ValueLongAdapter<K>) inline fun <K> vLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float): VLongFloatMap<K> =
    MutVLongFloatMap(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2, a.toLong(key3), value3, a.toLong(key4), value4))
context(a: ValueLongAdapter<K>) inline fun <K> vLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float, key5:K,value5:Float): VLongFloatMap<K> =
    MutVLongFloatMap(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2, a.toLong(key3), value3, a.toLong(key4), value4, a.toLong(key5), value5))

fun <K> mutVLongFloatMapOf() = MutVLongFloatMap<K>()
context(a: ValueLongAdapter<K>) inline fun <K> mutVLongFloatMapOf(key1:K,value1:Float) =
    MutVLongFloatMap<K>(mutableLongFloatMapOf(a.toLong(key1), value1))
context(a: ValueLongAdapter<K>) inline fun <K> mutVLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float) =
    MutVLongFloatMap<K>(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2))
context(a: ValueLongAdapter<K>) inline fun <K> mutVLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float) =
    MutVLongFloatMap<K>(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2, a.toLong(key3), value3))
context(a: ValueLongAdapter<K>) inline fun <K> mutVLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float) =
    MutVLongFloatMap<K>(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2, a.toLong(key3), value3, a.toLong(key4), value4))
context(a: ValueLongAdapter<K>) inline fun <K> mutVLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float, key5:K,value5:Float) =
    MutVLongFloatMap<K>(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2, a.toLong(key3), value3, a.toLong(key4), value4, a.toLong(key5), value5))

inline fun <K> buildVLongFloatMap(builderAction: MutVLongFloatMap<K>.() -> Unit): VLongFloatMap<K> = MutVLongFloatMap<K>().apply(builderAction)
inline fun <K> buildVLongFloatMap(initialCapacity:Int, builderAction: MutVLongFloatMap<K>.() -> Unit): VLongFloatMap<K> = MutVLongFloatMap<K>(initialCapacity).apply(builderAction)

interface VLongFloatMap<K> { val collection: LongFloatMap }
inline val <K> VLongFloatMap<K>.capacity inline get() = collection.capacity
inline val <K> VLongFloatMap<K>.size inline get() = collection.size
inline fun <K> VLongFloatMap<K>.any() = collection.any()
inline fun <K> VLongFloatMap<K>.none() = collection.none()
inline fun <K> VLongFloatMap<K>.isEmpty() = collection.isEmpty()
inline fun <K> VLongFloatMap<K>.isNotEmpty() = collection.isNotEmpty()
context(a: ValueLongAdapter<K>) inline operator fun <K> VLongFloatMap<K>.get(key: K) = collection[a.toLong(key)]
context(a: ValueLongAdapter<K>) inline fun <K> VLongFloatMap<K>.getOrDefault(key: K, defaultValue: Float) = collection.getOrDefault(a.toLong(key), defaultValue)
context(a: ValueLongAdapter<K>) inline fun <K> VLongFloatMap<K>.getOrElse(key: K, defaultValue: ()->Float) = collection.getOrElse(a.toLong(key), defaultValue)
context(a: ValueLongAdapter<K>) inline fun <K> VLongFloatMap<K>.forEach(block:(key:K,value:Float)->Unit) = collection.forEach{k,v->block(a.fromLong(k),v)}
context(a: ValueLongAdapter<K>) inline fun <K> VLongFloatMap<K>.forEachKey(block:(key:K)->Unit) = collection.forEachKey{block(a.fromLong(it))}
inline fun <K> VLongFloatMap<K>.forEachValue(block:(value:Float)->Unit) = collection.forEachValue{block(it)}
context(a: ValueLongAdapter<K>) inline fun <K> VLongFloatMap<K>.all(predicate: (K, Float) -> Boolean) = collection.all{k,v->predicate(a.fromLong(k),v)}
context(a: ValueLongAdapter<K>) inline fun <K> VLongFloatMap<K>.any(predicate: (K, Float) -> Boolean) = collection.any{k,v->predicate(a.fromLong(k),v)}
inline fun <K> VLongFloatMap<K>.count() = collection.count()
context(a: ValueLongAdapter<K>) inline fun <K> VLongFloatMap<K>.count(predicate: (K, Float) -> Boolean) = collection.count{k,v->predicate(a.fromLong(k),v)}
context(a: ValueLongAdapter<K>) inline fun <K> VLongFloatMap<K>.contains(key: K) = collection.contains(a.toLong(key))
context(a: ValueLongAdapter<K>) inline fun <K> VLongFloatMap<K>.containsKey(key: K) = collection.containsKey(a.toLong(key))
inline fun <K> VLongFloatMap<K>.containsValue(value: Float) = collection.containsValue(value)
context(a: ValueLongAdapter<K>) inline fun <K> VLongFloatMap<K>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...")
        = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> a.fromLong(k).toString()+"="+v.toString()} 
context(a: ValueLongAdapter<K>) inline fun <K> VLongFloatMap<K>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "",  postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: (key: K, value: Float) -> CharSequence, )
        = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> transform(a.fromLong(k), v)}

class MutVLongFloatMap<K>(override val collection: MutableLongFloatMap = MutableLongFloatMap()): VLongFloatMap<K> {
    constructor(capacity: Int) : this(MutableLongFloatMap(capacity))
    context(a: ValueLongAdapter<K>) inline fun remove(key: K) = collection.remove(a.toLong(key))
    context(a: ValueLongAdapter<K>) inline fun getOrPut(key: K, defaultValue:()->Float) = collection.getOrPut(a.toLong(key),defaultValue)
    context(a: ValueLongAdapter<K>) inline fun set(key: K, value:Float) =  collection.set(a.toLong(key), value)
    context(a: ValueLongAdapter<K>) inline fun put(key: K, value:Float) = collection.put(a.toLong(key),value)
    context(a: ValueLongAdapter<K>) inline fun put(key: K, value:Float, default:Float) = collection.put(a.toLong(key),value, default)
    inline fun putAll(from: VLongFloatMap<K>) = collection.putAll(from.collection)
    inline operator fun plusAssign(from: VLongFloatMap<K>) = collection.putAll(from.collection)
    context(a: ValueLongAdapter<K>) fun remove(key: K, value: Float) = collection.remove(a.toLong(key),value)
    context(a: ValueLongAdapter<K>) inline fun removeIf(predicate: (K, Float) -> Boolean) = collection.removeIf{k,v->predicate(a.fromLong(k),v)}
    context(a: ValueLongAdapter<K>) inline operator fun minusAssign(key: K):Unit = collection.minusAssign(a.toLong(key))
    inline operator fun minusAssign(keys: LongArray):Unit = collection.minusAssign(keys) // TODO: Add VIntArray/VIntCollection
    inline operator fun minusAssign(keys: LongSet):Unit = collection.minusAssign(keys)
    inline operator fun minusAssign(keys: LongList):Unit = collection.minusAssign(keys)
    fun clear() = collection.clear()
    fun trim() = collection.trim()
    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = collection == other
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toString(ValueIntAdapter) to print K.toString", ReplaceWith("toString(K.ValueIntAdapter)"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
    context(a: ValueLongAdapter<K>) inline fun toString() = joinToString(", ","{","}")
}
