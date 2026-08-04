@file:Suppress("unused", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package com.mpd.common.collect.valuecollections

import java.util.PrimitiveIterator
import java.util.function.Consumer

// PrimitiveIterator<T, Consumer<in T>> conformance is provided for free by MutableIteratorVIntGeneric/
// MutableIteratorVLongGeneric (IteratorVInt.kt/IteratorVLong.kt) for the Int/Long-backed key and value
// kinds; the Float/Object kinds have no bits to hand off to those wrapper classes, so their iterators
// implement PrimitiveIterator directly instead.

// =====================================================================================
// Key markers: hold the abstract surface needed by key-only operations (keyFromX, keySet)
// shared across all map shapes that use a given key representation.
// =====================================================================================

interface MapVIntKey<K> {
    val NULL_KEY_BITS: IntKeyBits
    val size: Int
    fun anyKeyBits(predicate: (IntKeyBits) -> Boolean): IntKeyBits
    fun removeKeyBits(bits: IntKeyBits): Boolean = throw UnsupportedOperationException()
    fun containsKeyBits(bits: IntKeyBits): Boolean = anyKeyBits { it == bits } != NULL_KEY_BITS
    fun forEachKeyBits(action: (IntKeyBits) -> Unit) { anyKeyBits { action(it); false } }
}
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntKey<K>.keyFromInt(bits: IntKeyBits): K = if (bits==NULL_KEY_BITS) throw NoSuchElementException() else ka.fromInt(bits)
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntKey<K>.keyFromIntOr(bits: IntKeyBits, provider: ()->K): K = if (bits==NULL_KEY_BITS) provider() else ka.fromInt(bits)
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntKey<K>.keyFromIntOrNull(bits: IntKeyBits): K? = if (bits==NULL_KEY_BITS) null else ka.fromInt(bits)
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntKey<K>.containsKey(k: K): Boolean = containsKeyBits(ka.toInt(k))
context(ka: ValueIntAdapter<K>) inline fun <K> MapVIntKey<K>.forEachKey(crossinline action: (K) -> Unit) = forEachKeyBits { action(ka.fromInt(it)) }
context(ka: ValueIntAdapter<K>) fun <K> MapVIntKey<K>.keySet(): SetVInt<K> = object : SetVInt<K> {
    override val NULL_VALUE: IntBits get() = NULL_KEY_BITS
    override val size: Int get() = this@keySet.size
    override fun anyBits(predicate: (IntBits) -> Boolean): IntBits = this@keySet.anyKeyBits(predicate)
    override fun containsBits(bits: IntBits): Boolean = this@keySet.containsKeyBits(bits)
    context(a: ValueIntAdapter<K>) override fun toIterable(): Iterable<K> {
        val arr = IntArray(size)
        var i = 0
        this@keySet.anyKeyBits { arr[i++] = it; false }
        return IteratorVIntKotlin(arr.iterator(), a)
    }
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String = "keySet"
}

interface MapVLongKey<K> {
    val NULL_KEY_BITS: LongKeyBits
    val size: Int
    fun anyKeyBits(predicate: (LongKeyBits) -> Boolean): LongKeyBits
    fun removeKeyBits(bits: LongKeyBits): Boolean = throw UnsupportedOperationException()
    fun containsKeyBits(bits: LongKeyBits): Boolean = anyKeyBits { it == bits } != NULL_KEY_BITS
    fun forEachKeyBits(action: (LongKeyBits) -> Unit) { anyKeyBits { action(it); false } }
}
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongKey<K>.keyFromInt(bits: LongKeyBits): K = if (bits==NULL_KEY_BITS) throw NoSuchElementException() else ka.fromLong(bits)
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongKey<K>.keyFromIntOr(bits: LongKeyBits, provider: ()->K): K = if (bits==NULL_KEY_BITS) provider() else ka.fromLong(bits)
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongKey<K>.keyFromIntOrNull(bits: LongKeyBits): K? = if (bits==NULL_KEY_BITS) null else ka.fromLong(bits)
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongKey<K>.containsKey(k: K): Boolean = containsKeyBits(ka.toLong(k))
context(ka: ValueLongAdapter<K>) inline fun <K> MapVLongKey<K>.forEachKey(crossinline action: (K) -> Unit) = forEachKeyBits { action(ka.fromLong(it)) }
context(ka: ValueLongAdapter<K>) fun <K> MapVLongKey<K>.keySet(): SetVLong<K> = object : SetVLong<K> {
    override val NULL_VALUE: LongBits get() = NULL_KEY_BITS
    override val size: Int get() = this@keySet.size
    override fun anyBits(predicate: (LongBits) -> Boolean): LongBits = this@keySet.anyKeyBits(predicate)
    override fun containsBits(bits: LongBits): Boolean = this@keySet.containsKeyBits(bits)
    context(a: ValueLongAdapter<K>) override fun toIterable(): Iterable<K> {
        val arr = LongArray(size)
        var i = 0
        this@keySet.anyKeyBits { arr[i++] = it; false }
        return IteratorVLongKotlin(arr.iterator(), a)
    }
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String = "keySet"
}

interface MapVFloatKey {
    val size: Int
    fun anyKeyOrNull(predicate: (Float) -> Boolean): Float?
    fun removeKey(key: Float): Boolean = throw UnsupportedOperationException()
    fun containsKey(key: Float): Boolean = anyKeyOrNull { it == key } != null
    fun forEachKey(action: (Float) -> Unit) { anyKeyOrNull { action(it); false } }
}
fun MapVFloatKey.keySet(): SetVInt<Float> = object : SetVInt<Float> {
    override val NULL_VALUE: IntBits = Int.MIN_VALUE
    override val size: Int get() = this@keySet.size
    override fun anyBits(predicate: (IntBits) -> Boolean): IntBits {
        val found = this@keySet.anyKeyOrNull { predicate(PrimitiveFloatAdapter.toInt(it)) }
        return if (found == null) NULL_VALUE else PrimitiveFloatAdapter.toInt(found)
    }
    override fun containsBits(bits: IntBits): Boolean = this@keySet.anyKeyOrNull { PrimitiveFloatAdapter.toInt(it) == bits } != null
    context(a: ValueIntAdapter<Float>) override fun toIterable(): Iterable<Float> {
        val arr = IntArray(size)
        var i = 0
        this@keySet.anyKeyOrNull { arr[i++] = PrimitiveFloatAdapter.toInt(it); false }
        return IteratorVIntKotlin(arr.iterator(), a)
    }
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String = "keySet"
}

interface MapVObjectKey<K> {
    val size: Int
    fun anyKeyOrNull(predicate: (K) -> Boolean): K?
    fun removeKey(key: K): Boolean = throw UnsupportedOperationException()
    fun containsKey(key: K): Boolean = anyKeyOrNull { it == key } != null
    fun forEachKey(action: (K) -> Unit) { anyKeyOrNull { action(it); false } }
}
fun <K> MapVObjectKey<K>.keySetGeneric(): Set<K> = object : Set<K> {
    override val size: Int get() = this@keySetGeneric.size
    override fun isEmpty(): Boolean = size == 0
    override fun contains(element: K): Boolean = this@keySetGeneric.anyKeyOrNull { it == element } != null
    override fun containsAll(elements: Collection<K>): Boolean = elements.all { contains(it) }
    override fun iterator(): Iterator<K> {
        val arr = ArrayList<K>(size)
        this@keySetGeneric.anyKeyOrNull { arr.add(it); false }
        return object : Iterator<K>, PrimitiveIterator<K, Consumer<in K>> {
            var idx = 0
            override fun hasNext(): Boolean = idx < arr.size
            override fun next(): K = arr[idx++]
            override fun remove(): Unit = throw UnsupportedOperationException("remove")
            override fun forEachRemaining(action: Consumer<in K>) { while (hasNext()) action.accept(next()) }
        }
    }
}

// =====================================================================================
// Value markers: hold the abstract surface needed by value-only operations (valueFromX,
// values()) shared across all map shapes that use a given value representation.
// =====================================================================================

interface MapVIntValue<V> {
    val NULL_VALUE_BITS: IntValueBits
    val size: Int
    fun forEachValueBits(action: (valueBits: IntValueBits) -> Unit)
    fun containsValueBits(bits: IntValueBits): Boolean { var found = false; forEachValueBits { if (!found && it == bits) found = true }; return found }
}
context(va: ValueIntAdapter<V>) inline fun <V> MapVIntValue<V>.valueFromInt(bits: IntValueBits): V = if (bits==NULL_VALUE_BITS) throw NoSuchElementException() else va.fromInt(bits)
context(va: ValueIntAdapter<V>) inline fun <V> MapVIntValue<V>.valueFromIntOr(bits: IntValueBits, provider: ()->V): V = if (bits==NULL_VALUE_BITS) provider() else va.fromInt(bits)
context(va: ValueIntAdapter<V>) inline fun <V> MapVIntValue<V>.valueFromIntOrNull(bits: IntValueBits): V? = if (bits==NULL_VALUE_BITS) null else va.fromInt(bits)
context(va: ValueIntAdapter<V>) inline fun <V> MapVIntValue<V>.containsValue(v: V): Boolean = containsValueBits(va.toInt(v))
context(va: ValueIntAdapter<V>) fun <V> MapVIntValue<V>.values(): CollectionVInt<V> = object : CollectionVInt<V> {
    override val NULL_VALUE: IntBits get() = this@values.NULL_VALUE_BITS
    override val size: Int get() = this@values.size
    override fun anyBits(predicate: (IntBits) -> Boolean): IntBits {
        var found = NULL_VALUE
        this@values.forEachValueBits { v -> if (found == NULL_VALUE && predicate(v)) found = v }
        return found
    }
    override fun containsBits(bits: IntBits): Boolean = this@values.containsValueBits(bits)
    context(a: ValueIntAdapter<V>) override fun toIterable(): Iterable<V> {
        val vals = IntArray(size)
        var i = 0
        this@values.forEachValueBits { v -> vals[i++] = v }
        return IteratorVIntKotlin(vals.iterator(), a)
    }
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String = "values"
}

interface MapVLongValue<V> {
    val NULL_VALUE_BITS: LongValueBits
    val size: Int
    fun forEachValueBits(action: (valueBits: LongValueBits) -> Unit)
    fun containsValueBits(bits: LongValueBits): Boolean { var found = false; forEachValueBits { if (!found && it == bits) found = true }; return found }
}
context(va: ValueLongAdapter<V>) inline fun <V> MapVLongValue<V>.valueFromLong(bits: LongValueBits): V = if (bits==NULL_VALUE_BITS) throw NoSuchElementException() else va.fromLong(bits)
context(va: ValueLongAdapter<V>) inline fun <V> MapVLongValue<V>.valueFromLongOr(bits: LongValueBits, provider: ()->V): V = if (bits==NULL_VALUE_BITS) provider() else va.fromLong(bits)
context(va: ValueLongAdapter<V>) inline fun <V> MapVLongValue<V>.valueFromLongOrNull(bits: LongValueBits): V? = if (bits==NULL_VALUE_BITS) null else va.fromLong(bits)
context(va: ValueLongAdapter<V>) inline fun <V> MapVLongValue<V>.containsValue(v: V): Boolean = containsValueBits(va.toLong(v))
context(va: ValueLongAdapter<V>) fun <V> MapVLongValue<V>.values(): CollectionVLong<V> = object : CollectionVLong<V> {
    override val NULL_VALUE: LongBits get() = this@values.NULL_VALUE_BITS
    override val size: Int get() = this@values.size
    override fun anyBits(predicate: (LongBits) -> Boolean): LongBits {
        var found = NULL_VALUE
        this@values.forEachValueBits { v -> if (found == NULL_VALUE && predicate(v)) found = v }
        return found
    }
    override fun containsBits(bits: LongBits): Boolean = this@values.containsValueBits(bits)
    context(a: ValueLongAdapter<V>) override fun toIterable(): Iterable<V> {
        val vals = LongArray(size)
        var i = 0
        this@values.forEachValueBits { v -> vals[i++] = v }
        return IteratorVLongKotlin(vals.iterator(), a)
    }
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String = "values"
}

interface MapVFloatValue {
    val size: Int
    fun forEachValue(action: (value: Float) -> Unit)
    fun containsValue(value: Float): Boolean { var found = false; forEachValue { if (!found && it == value) found = true }; return found }
}
inline fun MapVFloatValue.valueFromFloat(v: Float, nullValue: Float): Float = if (v==nullValue) throw NoSuchElementException() else v
fun MapVFloatValue.values(): CollectionVInt<Float> = object : CollectionVInt<Float> {
    override val NULL_VALUE: IntBits = Int.MIN_VALUE
    override val size: Int get() = this@values.size
    override fun anyBits(predicate: (IntBits) -> Boolean): IntBits {
        var found = NULL_VALUE
        this@values.forEachValue { v -> val bits = PrimitiveFloatAdapter.toInt(v); if (found == NULL_VALUE && predicate(bits)) found = bits }
        return found
    }
    override fun containsBits(bits: IntBits): Boolean = anyBits { it == bits } != NULL_VALUE
    context(a: ValueIntAdapter<Float>) override fun toIterable(): Iterable<Float> {
        val vals = ArrayList<Float>(size)
        this@values.forEachValue { v -> vals.add(v) }
        return object : Iterable<Float>, PrimitiveIterator<Float, Consumer<in Float>> {
            var idx = 0
            override fun iterator(): PrimitiveIterator<Float, Consumer<in Float>> = this
            override fun hasNext(): Boolean = idx < vals.size
            override fun next(): Float = vals[idx++]
            override fun remove(): Unit = throw UnsupportedOperationException("remove")
            override fun forEachRemaining(action: Consumer<in Float>) { while (hasNext()) action.accept(next()) }
        }
    }
    @Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
    @Deprecated("toString() prints Integers. Use toStringV() to print K.toString", ReplaceWith("toStringV()"))
    override fun toString(): String = "values"
}

interface MapVObjectValue<V> {
    val size: Int
    fun forEachValue(action: (value: V) -> Unit)
    fun containsValue(value: V): Boolean { var found = false; forEachValue { if (!found && it == value) found = true }; return found }
}
fun <V> MapVObjectValue<V>.valuesGeneric(): Collection<V> = object : Collection<V> {
    override val size: Int get() = this@valuesGeneric.size
    override fun isEmpty(): Boolean = size == 0
    override fun contains(element: V): Boolean { var found = false; this@valuesGeneric.forEachValue { v -> if (!found && v == element) found = true }; return found }
    override fun containsAll(elements: Collection<V>): Boolean = elements.all { contains(it) }
    override fun iterator(): Iterator<V> {
        val vals = ArrayList<V>(size)
        this@valuesGeneric.forEachValue { v -> vals.add(v) }
        return object : Iterator<V>, PrimitiveIterator<V, Consumer<in V>> {
            var idx = 0
            override fun hasNext(): Boolean = idx < vals.size
            override fun next(): V = vals[idx++]
            override fun remove(): Unit = throw UnsupportedOperationException("remove")
            override fun forEachRemaining(action: Consumer<in V>) { while (hasNext()) action.accept(next()) }
        }
    }
}
