@file:Suppress("NOTHING_TO_INLINE", "UNUSED", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.LongFloatMap
import androidx.collection.LongList
import androidx.collection.LongSet
import androidx.collection.MutableLongFloatMap
import androidx.collection.mutableLongFloatMapOf
import kotlin.apply

// TODO: Redo to fit new Collection paradigm

// LongFloatMap -> VLongFloatMap
private val EmptyVLongFloatMap = MutMapVLongFloat<Nothing>()
@Suppress("UNCHECKED_CAST")
fun <K> emptyVLongFloatMap(): MapVLongFloat<K> = EmptyVLongFloatMap as MapVLongFloat<K>
@Suppress("UNCHECKED_CAST")
fun <K> vLongFloatMapOf(): MapVLongFloat<K> = EmptyVLongFloatMap as MapVLongFloat<K>
context(a: ValueLongAdapter<K>) inline fun <K> vLongFloatMapOf(key1:K,value1:Float): MapVLongFloat<K> =
    MutMapVLongFloat(mutableLongFloatMapOf(a.toLong(key1), value1))
context(a: ValueLongAdapter<K>) inline fun <K> vLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float): MapVLongFloat<K> =
    MutMapVLongFloat(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2))
context(a: ValueLongAdapter<K>) inline fun <K> vLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float): MapVLongFloat<K> =
    MutMapVLongFloat(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2, a.toLong(key3), value3))
context(a: ValueLongAdapter<K>) inline fun <K> vLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float): MapVLongFloat<K> =
    MutMapVLongFloat(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2, a.toLong(key3), value3, a.toLong(key4), value4))
context(a: ValueLongAdapter<K>) inline fun <K> vLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float, key5:K,value5:Float): MapVLongFloat<K> =
    MutMapVLongFloat(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2, a.toLong(key3), value3, a.toLong(key4), value4, a.toLong(key5), value5))

fun <K> mutVLongFloatMapOf() = MutMapVLongFloat<K>()
context(a: ValueLongAdapter<K>) inline fun <K> mutVLongFloatMapOf(key1:K,value1:Float) =
    MutMapVLongFloat<K>(mutableLongFloatMapOf(a.toLong(key1), value1))
context(a: ValueLongAdapter<K>) inline fun <K> mutVLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float) =
    MutMapVLongFloat<K>(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2))
context(a: ValueLongAdapter<K>) inline fun <K> mutVLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float) =
    MutMapVLongFloat<K>(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2, a.toLong(key3), value3))
context(a: ValueLongAdapter<K>) inline fun <K> mutVLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float) =
    MutMapVLongFloat<K>(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2, a.toLong(key3), value3, a.toLong(key4), value4))
context(a: ValueLongAdapter<K>) inline fun <K> mutVLongFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float, key5:K,value5:Float) =
    MutMapVLongFloat<K>(mutableLongFloatMapOf(a.toLong(key1), value1, a.toLong(key2), value2, a.toLong(key3), value3, a.toLong(key4), value4, a.toLong(key5), value5))

inline fun <K> buildVLongFloatMap(builderAction: MutMapVLongFloat<K>.() -> Unit): MapVLongFloat<K> = MutMapVLongFloat<K>().apply(builderAction)
inline fun <K> buildVLongFloatMap(initialCapacity:Int, builderAction: MutMapVLongFloat<K>.() -> Unit): MapVLongFloat<K> = MutMapVLongFloat<K>(initialCapacity).apply(builderAction)

interface MapVLongFloat<K> { val collection: LongFloatMap }
inline val <K> MapVLongFloat<K>.capacity inline get() = collection.capacity
inline val <K> MapVLongFloat<K>.size inline get() = collection.size
inline fun <K> MapVLongFloat<K>.any() = collection.any()
inline fun <K> MapVLongFloat<K>.none() = collection.none()
inline fun <K> MapVLongFloat<K>.isEmpty() = collection.isEmpty()
inline fun <K> MapVLongFloat<K>.isNotEmpty() = collection.isNotEmpty()
context(a: ValueLongAdapter<K>) inline operator fun <K> MapVLongFloat<K>.get(key: K) = collection[a.toLong(key)]
context(a: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.getOrDefault(key: K, defaultValue: Float) = collection.getOrDefault(a.toLong(key), defaultValue)
context(a: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.getOrElse(key: K, defaultValue: ()->Float) = collection.getOrElse(a.toLong(key), defaultValue)
context(a: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.forEach(block:(key:K, value:Float)->Unit) = collection.forEach{ k, v->block(a.fromLong(k),v)}
context(a: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.forEachKey(block:(key:K)->Unit) = collection.forEachKey{block(a.fromLong(it))}
inline fun <K> MapVLongFloat<K>.forEachValue(block:(value:Float)->Unit) = collection.forEachValue{block(it)}
context(a: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.all(predicate: (K, Float) -> Boolean) = collection.all{ k, v->predicate(a.fromLong(k),v)}
context(a: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.any(predicate: (K, Float) -> Boolean) = collection.any{ k, v->predicate(a.fromLong(k),v)}
inline fun <K> MapVLongFloat<K>.count() = collection.count()
context(a: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.count(predicate: (K, Float) -> Boolean) = collection.count{ k, v->predicate(a.fromLong(k),v)}
context(a: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.contains(key: K) = collection.contains(a.toLong(key))
context(a: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.containsKey(key: K) = collection.containsKey(a.toLong(key))
inline fun <K> MapVLongFloat<K>.containsValue(value: Float) = collection.containsValue(value)
context(a: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...")
        = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> a.fromLong(k).toString()+"="+v.toString()} 
context(a: ValueLongAdapter<K>) inline fun <K> MapVLongFloat<K>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: (key: K, value: Float) -> CharSequence, )
        = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> transform(a.fromLong(k), v)}

class MutMapVLongFloat<K>(override val collection: MutableLongFloatMap = MutableLongFloatMap()): MapVLongFloat<K> {
    constructor(capacity: Int) : this(MutableLongFloatMap(capacity))
    context(a: ValueLongAdapter<K>) inline fun remove(key: K) = collection.remove(a.toLong(key))
    context(a: ValueLongAdapter<K>) inline fun getOrPut(key: K, defaultValue:()->Float) = collection.getOrPut(a.toLong(key),defaultValue)
    context(a: ValueLongAdapter<K>) inline fun set(key: K, value:Float) =  collection.set(a.toLong(key), value)
    context(a: ValueLongAdapter<K>) inline fun put(key: K, value:Float) = collection.put(a.toLong(key),value)
    context(a: ValueLongAdapter<K>) inline fun put(key: K, value:Float, default:Float) = collection.put(a.toLong(key),value, default)
    inline fun putAll(from: MapVLongFloat<K>) = collection.putAll(from.collection)
    inline operator fun plusAssign(from: MapVLongFloat<K>) = collection.putAll(from.collection)
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
    @Deprecated("toString() prints Integers. Use toVString() to print K.toString", ReplaceWith("toVString()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
    context(a: ValueLongAdapter<K>) inline fun toString() = joinToString(", ","{","}")
}
