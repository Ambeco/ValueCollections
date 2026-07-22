@file:Suppress("NOTHING_TO_INLINE", "UNUSED", "OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

import androidx.collection.IntFloatMap
import androidx.collection.IntList
import androidx.collection.IntSet
import androidx.collection.MutableIntFloatMap
import androidx.collection.mutableIntFloatMapOf
import kotlin.apply

// TODO: Redo to fit new Collection paradigm

// inline wrappers around androidx.collection.IntFloatMap and LongFloatMap

// IntFloatMap -> VIntFloatMap
private val EmptyMapVIntFloat:MapVIntFloat<Nothing> = MutMapVIntFloat()
@Suppress("UNCHECKED_CAST")
fun <K> emptyVIntFloatMap(): MapVIntFloat<K> = EmptyMapVIntFloat as MapVIntFloat<K>
@Suppress("UNCHECKED_CAST")
fun <K> vIntFloatMapOf(): MapVIntFloat<K> = EmptyMapVIntFloat as MapVIntFloat<K>
context(a: ValueIntAdapter<K>) inline fun <K> vIntFloatMapOf(key1:K,value1:Float): MapVIntFloat<K> =
    MutMapVIntFloat(mutableIntFloatMapOf(a.toInt(key1), value1))
context(a: ValueIntAdapter<K>) inline fun <K> vIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float): MapVIntFloat<K> =
    MutMapVIntFloat(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2))
context(a: ValueIntAdapter<K>) inline fun <K> vIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float): MapVIntFloat<K> =
    MutMapVIntFloat(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2, a.toInt(key3), value3))
context(a: ValueIntAdapter<K>) inline fun <K> vIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float): MapVIntFloat<K> =
    MutMapVIntFloat(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2, a.toInt(key3), value3, a.toInt(key4), value4))
context(a: ValueIntAdapter<K>) inline fun <K> vIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float, key5:K,value5:Float): MapVIntFloat<K> =
    MutMapVIntFloat(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2, a.toInt(key3), value3, a.toInt(key4), value4, a.toInt(key5), value5))

fun <K> mutVIntFloatMapOf() = MutMapVIntFloat<K>()
context(a: ValueIntAdapter<K>) inline fun <K> mutVIntFloatMapOf(key1:K,value1:Float) =
    MutMapVIntFloat<K>(mutableIntFloatMapOf(a.toInt(key1), value1))
context(a: ValueIntAdapter<K>) inline fun <K> mutVIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float) =
    MutMapVIntFloat<K>(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2))
context(a: ValueIntAdapter<K>) inline fun <K> mutVIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float) =
    MutMapVIntFloat<K>(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2, a.toInt(key3), value3))
context(a: ValueIntAdapter<K>) inline fun <K> mutVIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float) =
    MutMapVIntFloat<K>(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2, a.toInt(key3), value3, a.toInt(key4), value4))
context(a: ValueIntAdapter<K>) inline fun <K> mutVIntFloatMapOf(key1:K,value1:Float, key2:K,value2:Float, key3:K,value3:Float, key4:K,value4:Float, key5:K,value5:Float) =
    MutMapVIntFloat<K>(mutableIntFloatMapOf(a.toInt(key1), value1, a.toInt(key2), value2, a.toInt(key3), value3, a.toInt(key4), value4, a.toInt(key5), value5))

inline fun <K> buildVIntFloatMap(builderAction: MutMapVIntFloat<K>.() -> Unit): MapVIntFloat<K> = MutMapVIntFloat<K>().apply(builderAction)
inline fun <K> buildVIntFloatMap(initialCapacity:Int, builderAction: MutMapVIntFloat<K>.() -> Unit): MapVIntFloat<K> = MutMapVIntFloat<K>(initialCapacity).apply(builderAction)

interface MapVIntFloat<K> { val collection: IntFloatMap }
inline val <K> MapVIntFloat<K>.capacity inline get() = collection.capacity
inline val <K> MapVIntFloat<K>.size inline get() = collection.size
inline fun <K> MapVIntFloat<K>.any() = collection.any()
inline fun <K> MapVIntFloat<K>.none() = collection.none()
inline fun <K> MapVIntFloat<K>.isEmpty() = collection.isEmpty()
inline fun <K> MapVIntFloat<K>.isNotEmpty() = collection.isNotEmpty()
context(a: ValueIntAdapter<K>) inline operator fun <K> MapVIntFloat<K>.get(key: K) = collection[a.toInt(key)]
context(a: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.getOrDefault(key: K, defaultValue: Float) = collection.getOrDefault(a.toInt(key), defaultValue)
context(a: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.getOrElse(key: K, defaultValue: ()->Float) = collection.getOrElse(a.toInt(key), defaultValue)
context(a: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.forEach(block:(key:K, value:Float)->Unit) = collection.forEach{ k, v->block(a.fromInt(k),v)}
context(a: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.forEachKey(block:(key:K)->Unit) = collection.forEachKey{block(a.fromInt(it))}
inline fun <K> MapVIntFloat<K>.forEachValue(block:(value:Float)->Unit) = collection.forEachValue{block(it)}
context(a: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.all(predicate: (K, Float) -> Boolean) = collection.all{ k, v->predicate(a.fromInt(k),v)}
context(a: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.any(predicate: (K, Float) -> Boolean) = collection.any{ k, v->predicate(a.fromInt(k),v)}
inline fun <K> MapVIntFloat<K>.count() = collection.count()
context(a: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.count(predicate: (K, Float) -> Boolean) = collection.count{ k, v->predicate(a.fromInt(k),v)}
context(a: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.contains(key: K) = collection.contains(a.toInt(key))
context(a: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.containsKey(key: K) = collection.containsKey(a.toInt(key))
inline fun <K> MapVIntFloat<K>.containsValue(value: Float) = collection.containsValue(value)
context(a: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...")
    = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> a.fromInt(k).toString()+"="+v.toString()} 
context(a: ValueIntAdapter<K>) inline fun <K> MapVIntFloat<K>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", crossinline transform: (key: K, value: Float) -> CharSequence) 
    = collection.joinToString(separator, prefix, postfix, limit, truncated) { k, v -> transform(a.fromInt(k), v)}

class MutMapVIntFloat<K>(override val collection: MutableIntFloatMap = MutableIntFloatMap()): MapVIntFloat<K> {
    constructor(capacity: Int) : this(MutableIntFloatMap(capacity))
    context(a: ValueIntAdapter<K>) inline fun remove(key: K) = collection.remove(a.toInt(key))
    context(a: ValueIntAdapter<K>) inline fun getOrPut(key: K, defaultValue:()->Float) = collection.getOrPut(a.toInt(key),defaultValue)
    context(a: ValueIntAdapter<K>) inline fun set(key: K, value:Float) =  collection.set(a.toInt(key), value) 
    context(a: ValueIntAdapter<K>) inline fun put(key: K, value:Float) = collection.put(a.toInt(key),value)
    context(a: ValueIntAdapter<K>) inline fun put(key: K, value:Float, default:Float) = collection.put(a.toInt(key),value, default)
    inline fun putAll(from: MapVIntFloat<K>) = collection.putAll(from.collection)
    inline operator fun plusAssign(from: MapVIntFloat<K>) = collection.putAll(from.collection)
    context(a: ValueIntAdapter<K>) inline fun remove(key: K, value: Float) = collection.remove(a.toInt(key),value)
    context(a: ValueIntAdapter<K>) inline fun removeIf(predicate: (K, Float) -> Boolean) = collection.removeIf{k,v->predicate(a.fromInt(k),v)}
    context(a: ValueIntAdapter<K>) inline operator fun minusAssign(key: K):Unit = collection.minusAssign(a.toInt(key))
    inline operator fun minusAssign(keys: IntArray):Unit = collection.minusAssign(keys) // TODO: Add VIntArray/VIntCollection
    inline operator fun minusAssign(keys: IntSet):Unit = collection.minusAssign(keys)
    inline operator fun minusAssign(keys: IntList):Unit = collection.minusAssign(keys) 
    inline fun clear() = collection.clear()
    inline fun trim() = collection.trim()
    override inline fun hashCode() = collection.hashCode()
    override inline fun equals(other: Any?) = other is MutMapVIntFloat<*> && collection == other.collection
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override inline fun toString() = collection.toString() // WARNING: THIS PRINTS THE INTEGERS, NOT K.toString()!
    context(a: ValueIntAdapter<K>) inline fun toString() = joinToString(", ","{","}")
}
