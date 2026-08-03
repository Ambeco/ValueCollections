package com.mpd.common.collect.valuecollections

import androidx.collection.MutableLongFloatMap
import androidx.collection.mutableLongSetOf
import com.mpd.common.collect.valuecollections.HashMapVLongFloat
import com.mpd.common.collect.valuecollections.ValueLongAdapter
import com.mpd.common.collect.valuecollections.any
import com.mpd.common.collect.valuecollections.anyIndexed
import com.mpd.common.collect.valuecollections.anyIndexedOr
import com.mpd.common.collect.valuecollections.anyIndexedOrNull
import com.mpd.common.collect.valuecollections.anyOr
import com.mpd.common.collect.valuecollections.anyOrNull
import com.mpd.common.collect.valuecollections.asMapGeneric
import com.mpd.common.collect.valuecollections.containsKey
import com.mpd.common.collect.valuecollections.containsValue
import com.mpd.common.collect.valuecollections.forEach
import com.mpd.common.collect.valuecollections.forEachBits
import com.mpd.common.collect.valuecollections.forEachIndexed
import com.mpd.common.collect.valuecollections.forEachPair
import com.mpd.common.collect.valuecollections.get
import com.mpd.common.collect.valuecollections.getOr
import com.mpd.common.collect.valuecollections.getOrNull
import com.mpd.common.collect.valuecollections.getOrPut
import com.mpd.common.collect.valuecollections.isEmpty
import com.mpd.common.collect.valuecollections.isNotEmpty
import com.mpd.common.collect.valuecollections.joinToString
import com.mpd.common.collect.valuecollections.minusAssign
import com.mpd.common.collect.valuecollections.plusAssign
import com.mpd.common.collect.valuecollections.putAll
import com.mpd.common.collect.valuecollections.putAllGeneric
import com.mpd.common.collect.valuecollections.remove
import com.mpd.common.collect.valuecollections.removeIf
import com.mpd.common.collect.valuecollections.set
import com.mpd.common.collect.valuecollections.toStringV
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class MLFKeyClass(val value: Long): Comparable<MLFKeyClass> {
    override operator fun compareTo(other: MLFKeyClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<MLFKeyClass> {
        override inline fun fromLong(v: Long) = MLFKeyClass(v)
        override inline fun toLong(v: MLFKeyClass): Long = v.value
    }
}

class MapVLongFloatTest {
    private fun simpleMap(): HashMapVLongFloat<MLFKeyClass> = with (MLFKeyClass) {
        HashMapVLongFloat<MLFKeyClass>().also {
            it[MLFKeyClass(1L)] = 100f
            it[MLFKeyClass(2L)] = 200f
            it[MLFKeyClass(3L)] = 300f
        }
    }

    @Test
    fun constructors() {
        val primary = HashMapVLongFloat<MLFKeyClass>()
        assertEquals(0, primary.size)
        val bySize = HashMapVLongFloat<MLFKeyClass>(10)
        assertEquals(0, bySize.size)
    }

    @Test
    fun customNullValues() {
        val map = HashMapVLongFloat<MLFKeyClass>(10, -1, -999f)
        assertEquals(-1, map.NULL_KEY_BITS)
        assertEquals(-999f, map.NULL_VALUE)
    }

    @Test
    fun getSetBits() {
        val map = HashMapVLongFloat<MLFKeyClass>()
        assertEquals(map.NULL_VALUE, map.setBits(1, 100f, map.NULL_VALUE))
        assertEquals(100f, map.getBits(1))
        assertEquals(100f, map.setBits(1, 200f, map.NULL_VALUE))
        assertEquals(200f, map.getBits(1))
        assertEquals(map.NULL_VALUE, map.getBits(99))
    }

    @Test
    fun getOrPutBits() {
        val map = HashMapVLongFloat<MLFKeyClass>()
        assertEquals(100f, map.getOrPutBits(1) { 100f })
        assertEquals(100f, map.getOrPutBits(1) { 999f })
    }

    @Test
    fun removeBitsVariants() {
        val map = HashMapVLongFloat<MLFKeyClass>()
        map.setBits(1, 100f, map.NULL_VALUE)
        map.setBits(2, 200f, map.NULL_VALUE)
        map.removeBits(1)
        assertEquals(1, map.size)
        assertEquals(true, map.removeBits(2, 200f))
        assertEquals(0, map.size)
        assertEquals(false, map.removeBits(99, 999f))
    }

    @Test
    fun removeIfBits() {
        val map = HashMapVLongFloat<MLFKeyClass>()
        map.setBits(1, 100f, map.NULL_VALUE)
        map.setBits(2, 200f, map.NULL_VALUE)
        map.removeIfBits { _, v -> v >= 200f }
        assertEquals(1, map.size)
        assertEquals(100f, map.getBits(1))
    }

    @Test
    fun anyBitsPredicate() {
        val map = simpleMap()
        assertEquals(2L, map.anyBits { k, _ -> k == 2L })
        assertEquals(map.NULL_KEY_BITS, map.anyBits { k, _ -> k == 999L })
    }

    @Test
    fun ensureCapacityTrimAndClear() {
        val map = simpleMap()
        assertEquals(false, map.ensureCapacity(100))
        map.trim()
        assertEquals(3, map.size)
        map.clear()
        assertEquals(0, map.size)
    }

    @Test
    fun isEmptyAndIsNotEmpty() {
        val map = HashMapVLongFloat<MLFKeyClass>()
        assertEquals(true, map.isEmpty)
        assertEquals(false, map.isNotEmpty())
        map.setBits(1, 100f, map.NULL_VALUE)
        assertEquals(false, map.isEmpty)
        assertEquals(true, map.isNotEmpty())
    }

    @Test
    fun typedGetVariants() = with (MLFKeyClass) {
        val map = simpleMap()
        assertEquals(200f, map[MLFKeyClass(2L)])
        assertEquals(200f, map.getOr(MLFKeyClass(2L)) { -1f })
        assertEquals(-1f, map.getOr(MLFKeyClass(99L)) { -1f })
        assertEquals(200f, map.getOrNull(MLFKeyClass(2L)))
        assertEquals(null, map.getOrNull(MLFKeyClass(99L)))
    }

    @Test
    fun anyVariants() = with (MLFKeyClass) {
        val map = simpleMap()
        assertEquals(MLFKeyClass(2L), map.any { _, v -> v == 200f })
        assertEquals(MLFKeyClass(2L), map.anyOr({ _, v -> v == 200f }) { MLFKeyClass(-1L) })
        assertEquals(MLFKeyClass(-1L), map.anyOr({ _, v -> v == 999f }) { MLFKeyClass(-1L) })
        assertEquals(MLFKeyClass(2L), map.anyOrNull { _, v -> v == 200f })
        assertEquals(null, map.anyOrNull { _, v -> v == 999f })
    }

    @Test
    fun anyIndexedVariants() = with (MLFKeyClass) {
        val map = simpleMap()
        val visitedIndices = mutableSetOf<Int>()
        map.forEachIndexed { i, _, _ -> visitedIndices.add(i) }
        assertEquals(setOf(0, 1, 2), visitedIndices)
        assertEquals(MLFKeyClass(2L), map.anyIndexed { _, _, v -> v == 200f })
        assertEquals(MLFKeyClass(2L), map.anyIndexedOr({ _, _, v -> v == 200f }) { MLFKeyClass(-1L) })
        assertEquals(MLFKeyClass(-1L), map.anyIndexedOr({ _, _, v -> v == 999f }) { MLFKeyClass(-1L) })
        assertEquals(null, map.anyIndexedOrNull { _, _, v -> v == 999f })
    }

    @Test
    fun forEachVariants() = with (MLFKeyClass) {
        val map = simpleMap()
        var sum = 0f
        map.forEach { _, v -> sum += v }
        assertEquals(600f, sum)

        val pairValues = mutableListOf<Float>()
        map.forEachPair { p -> pairValues.add(p.second) }
        assertEquals(setOf(100f, 200f, 300f), pairValues.toSet())

        val bitsValues = mutableListOf<Float>()
        map.forEachBits { _, v -> bitsValues.add(v) }
        assertEquals(setOf(100f, 200f, 300f), bitsValues.toSet())
    }

    @Test
    fun containsKeyAndValue() = with (MLFKeyClass) {
        val map = simpleMap()
        assertEquals(true, map.containsKey(MLFKeyClass(2L)))
        assertEquals(false, map.containsKey(MLFKeyClass(99L)))
        assertEquals(true, map.containsValue(200f))
        assertEquals(false, map.containsValue(999f))
    }

    @Test
    fun toStringVSingleEntry() = with (MLFKeyClass) {
        val map = HashMapVLongFloat<MLFKeyClass>().also { it[MLFKeyClass(1L)] = 100f }
        assertEquals("{(1:100.0)}", map.toStringV())
    }

    @Test
    fun joinToStringLimit() = with (MLFKeyClass) {
        val map = simpleMap()
        val full = map.joinToString()
        assertEquals(false, full.contains("..."))
        assertEquals(3, full.split(", ").size)
        val limited = map.joinToString(limit = 1)
        assertEquals(1, limited.split(", ").size)
    }

    @Test
    fun setOperatorAndWithDefault() = with (MLFKeyClass) {
        val map = HashMapVLongFloat<MLFKeyClass>()
        assertEquals(false, map.set(MLFKeyClass(1L), 100f))
        assertEquals(true, map.set(MLFKeyClass(1L), 200f))
        assertEquals(200f, map[MLFKeyClass(1L)])

        assertEquals(-1f, map.set(MLFKeyClass(99L), 50f, -1f))
        assertEquals(50f, map.set(MLFKeyClass(99L), 60f, -1f))
    }

    @Test
    fun getOrPut() = with (MLFKeyClass) {
        val map = HashMapVLongFloat<MLFKeyClass>()
        assertEquals(100f, map.getOrPut(MLFKeyClass(1L)) { 100f })
        assertEquals(100f, map.getOrPut(MLFKeyClass(1L)) { 999f })
    }

    @Test
    fun putAllAndPlusAssign() = with (MLFKeyClass) {
        val map = HashMapVLongFloat<MLFKeyClass>()
        map.putAll(simpleMap())
        assertEquals(3, map.size)

        val map2 = HashMapVLongFloat<MLFKeyClass>()
        map2 plusAssign simpleMap()
        assertEquals(3, map2.size)
    }

    @Test
    fun putAllGeneric() = with (MLFKeyClass) {
        val map = HashMapVLongFloat<MLFKeyClass>()
        map.putAllGeneric(listOf(1L to 100f, 2L to 200f)) { (k, v) -> MLFKeyClass(k) to v }
        assertEquals(2, map.size)
        assertEquals(200f, map[MLFKeyClass(2L)])
    }

    @Test
    fun removeVariants() = with (MLFKeyClass) {
        val map = simpleMap()
        map.remove(MLFKeyClass(1L))
        assertEquals(2, map.size)
        assertEquals(true, map.remove(MLFKeyClass(2L), 200f))
        assertEquals(1, map.size)
        map.removeIf { _, v -> v >= 300f }
        assertEquals(0, map.size)
    }

    @Test
    fun minusAssign() = with (MLFKeyClass) {
        val map = simpleMap()
        map minusAssign MLFKeyClass(1L)
        assertEquals(2, map.size)
    }

    @Test
    fun asMapGenericNonIterating() = with (MLFKeyClass) {
        val map = simpleMap()
        val generic = map.asMapGeneric()
        assertEquals(3, generic.size)
        assertEquals(false, generic.isEmpty())
        assertEquals(true, generic.containsKey(MLFKeyClass(2L)))
        assertEquals(true, generic.containsValue(200f))
        assertEquals(200f, generic[MLFKeyClass(2L)])
        assertEquals(null, generic[MLFKeyClass(99L)])
        assertThrows(NotImplementedError::class.java, { generic.keys })
    }

    @Test
    fun rawWrapperMethods() {
        val map = simpleMap()
        assertEquals(true, map.capacity() >= 3)
        assertEquals(false, map.none())
        assertEquals(true, map.isNotEmptyBits())
        assertEquals(200f, map.getOrDefault(2, -1f))
        assertEquals(-1f, map.getOrDefault(99, -1f))
        assertEquals(200f, map.getOrElse(2) { -1f })
        assertEquals(-1f, map.getOrElse(99) { -1f })

        val keys = mutableSetOf<Long>()
        map.forEachKey { keys.add(it) }
        assertEquals(setOf(1L, 2L, 3L), keys)

        val values = mutableSetOf<Float>()
        map.forEachValue { values.add(it) }
        assertEquals(setOf(100f, 200f, 300f), values)

        assertEquals(true, map.all { _, v -> v >= 100f })
        assertEquals(false, map.all { _, v -> v >= 200f })
        assertEquals(3, map.count())
        assertEquals(2, map.count { _, v -> v >= 200f })
        assertEquals(true, map.contains(2))
        assertEquals(false, map.contains(99))
        assertEquals(true, map.containsKeyBits(2))
        assertEquals(false, map.containsKeyBits(99))
        assertEquals(true, map.containsValueBits(200f))
        assertEquals(false, map.containsValueBits(999f))

        assertEquals(3, map.joinToStringBits().split(", ").size)
        assertEquals(true, map.joinToStringBits(transform = { k, v -> "$k=$v" }).contains("2=200.0"))

        map.setBits(4, 400f)
        assertEquals(400f, map.getBits(4))
        map.put(4, 450f)
        assertEquals(450f, map.getBits(4))
        assertEquals(450f, map.put(4, 460f, -1f))
        assertEquals(-1f, map.put(5, 500f, -1f))

        val other = MutableLongFloatMap().also { it[10] = 1000f }
        map.putAllBits(other)
        assertEquals(1000f, map.getBits(10))

        val other2 = MutableLongFloatMap().also { it[11] = 1100f }
        map.plusAssignBits(other2)
        assertEquals(1100f, map.getBits(11))

        map.minusAssignBits(10)
        assertEquals(false, map.containsKeyBits(10))
        map.minusAssignBits(longArrayOf(11))
        assertEquals(false, map.containsKeyBits(11))
        map.setBits(20, 2000f)
        map.minusAssignBits(mutableLongSetOf(20))
        assertEquals(false, map.containsKeyBits(20))
    }
}
