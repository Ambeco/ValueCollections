package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.HashMapVLongLong
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
value class MLLKeyClass(val value: Long): Comparable<MLLKeyClass> {
    override operator fun compareTo(other: MLLKeyClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<MLLKeyClass> {
        override inline fun fromLong(v: Long) = MLLKeyClass(v)
        override inline fun toLong(v: MLLKeyClass): Long = v.value
    }
}

@JvmInline
value class MLLValClass(val value: Long): Comparable<MLLValClass> {
    override operator fun compareTo(other: MLLValClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<MLLValClass> {
        override inline fun fromLong(v: Long) = MLLValClass(v)
        override inline fun toLong(v: MLLValClass): Long = v.value
    }
}

class MapVLongLongTest {
    private fun simpleMap(): HashMapVLongLong<MLLKeyClass, MLLValClass> = with (MLLKeyClass) {
        with (MLLValClass) {
            HashMapVLongLong<MLLKeyClass, MLLValClass>().also {
                it[MLLKeyClass(1)] = MLLValClass(100)
                it[MLLKeyClass(2)] = MLLValClass(200)
                it[MLLKeyClass(3)] = MLLValClass(300)
            }
        }
    }

    @Test
    fun constructors() {
        val primary = HashMapVLongLong<MLLKeyClass, MLLValClass>()
        assertEquals(0, primary.size)
        val bySize = HashMapVLongLong<MLLKeyClass, MLLValClass>(10)
        assertEquals(0, bySize.size)
    }

    @Test
    fun customNullValues() {
        val map = HashMapVLongLong<MLLKeyClass, MLLValClass>(10, -1, -2)
        assertEquals(-1L, map.NULL_KEY_BITS)
        assertEquals(-2L, map.NULL_VALUE_BITS)
    }

    @Test
    fun getSetBits() {
        val map = HashMapVLongLong<MLLKeyClass, MLLValClass>()
        assertEquals(map.NULL_VALUE_BITS, map.setBits(1, 100, map.NULL_VALUE_BITS))
        assertEquals(100L, map.getBits(1))
        assertEquals(100L, map.setBits(1, 200, map.NULL_VALUE_BITS))
        assertEquals(200L, map.getBits(1))
        assertEquals(map.NULL_VALUE_BITS, map.getBits(99))
    }

    @Test
    fun getOrPutBits() {
        val map = HashMapVLongLong<MLLKeyClass, MLLValClass>()
        assertEquals(100L, map.getOrPutBits(1) { 100 })
        assertEquals(100L, map.getOrPutBits(1) { 999 })
    }

    @Test
    fun removeBitsVariants() {
        val map = HashMapVLongLong<MLLKeyClass, MLLValClass>()
        map.setBits(1, 100, map.NULL_VALUE_BITS)
        map.setBits(2, 200, map.NULL_VALUE_BITS)
        map.removeBits(1)
        assertEquals(1, map.size)
        assertEquals(true, map.removeBits(2, 200))
        assertEquals(0, map.size)
        assertEquals(false, map.removeBits(99, 999))
    }

    @Test
    fun removeIfBits() {
        val map = HashMapVLongLong<MLLKeyClass, MLLValClass>()
        map.setBits(1, 100, map.NULL_VALUE_BITS)
        map.setBits(2, 200, map.NULL_VALUE_BITS)
        map.removeIfBits { _, v -> v >= 200 }
        assertEquals(1, map.size)
        assertEquals(100L, map.getBits(1))
    }

    @Test
    fun anyBits() {
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
        val map = HashMapVLongLong<MLLKeyClass, MLLValClass>()
        assertEquals(true, map.isEmpty)
        assertEquals(false, map.isNotEmpty())
        map.setBits(1, 100, map.NULL_VALUE_BITS)
        assertEquals(false, map.isEmpty)
        assertEquals(true, map.isNotEmpty())
    }

    @Test
    fun typedGetVariants() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = simpleMap()
            assertEquals(MLLValClass(200), map[MLLKeyClass(2)])
            assertEquals(MLLValClass(200), map.getOr(MLLKeyClass(2)) { MLLValClass(-1) })
            assertEquals(MLLValClass(-1), map.getOr(MLLKeyClass(99)) { MLLValClass(-1) })
            assertEquals(MLLValClass(200), map.getOrNull(MLLKeyClass(2)))
            assertEquals(null, map.getOrNull(MLLKeyClass(99)))
        }
    }

    @Test
    fun anyVariants() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = simpleMap()
            assertEquals(MLLKeyClass(2), map.any { _, v -> v.value == 200L })
            assertEquals(MLLKeyClass(2), map.anyOr({ _, v -> v.value == 200L }) { MLLKeyClass(-1) })
            assertEquals(MLLKeyClass(-1), map.anyOr({ _, v -> v.value == 999L }) { MLLKeyClass(-1) })
            assertEquals(MLLKeyClass(2), map.anyOrNull { _, v -> v.value == 200L })
            assertEquals(null, map.anyOrNull { _, v -> v.value == 999L })
        }
    }

    @Test
    fun anyIndexedVariants() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = simpleMap()
            val visitedIndices = mutableSetOf<Int>()
            map.forEachIndexed { i, _, _ -> visitedIndices.add(i) }
            assertEquals(setOf(0, 1, 2), visitedIndices)
            assertEquals(MLLKeyClass(2), map.anyIndexed { _, k, v -> v.value == 200L })
            assertEquals(MLLKeyClass(2), map.anyIndexedOr({ _, k, v -> v.value == 200L }) { MLLKeyClass(-1) })
            assertEquals(MLLKeyClass(-1), map.anyIndexedOr({ _, _, v -> v.value == 999L }) { MLLKeyClass(-1) })
            assertEquals(null, map.anyIndexedOrNull { _, _, v -> v.value == 999L })
        }
    }

    @Test
    fun forEachVariants() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = simpleMap()
            var sum = 0L
            map.forEach { _, v -> sum += v.value }
            assertEquals(600L, sum)

            val pairValues = mutableListOf<Long>()
            map.forEachPair { p -> pairValues.add(p.second.value) }
            assertEquals(setOf(100L, 200L, 300L), pairValues.toSet())

            val bitsValues = mutableListOf<Long>()
            map.forEachBits { _, v -> bitsValues.add(v) }
            assertEquals(setOf(100L, 200L, 300L), bitsValues.toSet())
        }
    }

    @Test
    fun containsKeyAndValue() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = simpleMap()
            assertEquals(true, map.containsKey(MLLKeyClass(2)))
            assertEquals(false, map.containsKey(MLLKeyClass(99)))
            assertEquals(true, map.containsValue(MLLValClass(200)))
            assertEquals(false, map.containsValue(MLLValClass(999)))
        }
    }

    @Test
    fun toStringVSingleEntry() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = HashMapVLongLong<MLLKeyClass, MLLValClass>().also { it[MLLKeyClass(1)] = MLLValClass(100) }
            assertEquals("{(1:100)}", map.toStringV())
        }
    }

    @Test
    fun joinToStringLimit() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = simpleMap()
            val full = map.joinToString()
            assertEquals(false, full.contains("..."))
            assertEquals(3, full.split(", ").size)
            val limited = map.joinToString(limit = 1)
            assertEquals(1, limited.split(", ").size)
        }
    }

    @Test
    fun setOperatorAndWithDefault() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = HashMapVLongLong<MLLKeyClass, MLLValClass>()
            assertEquals(false, map.set(MLLKeyClass(1), MLLValClass(100)))
            assertEquals(true, map.set(MLLKeyClass(1), MLLValClass(200)))
            assertEquals(MLLValClass(200), map[MLLKeyClass(1)])

            assertEquals(MLLValClass(-1), map.set(MLLKeyClass(99), MLLValClass(50), MLLValClass(-1)))
            assertEquals(MLLValClass(50), map.set(MLLKeyClass(99), MLLValClass(60), MLLValClass(-1)))
        }
    }

    @Test
    fun getOrPut() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = HashMapVLongLong<MLLKeyClass, MLLValClass>()
            assertEquals(MLLValClass(100), map.getOrPut(MLLKeyClass(1)) { MLLValClass(100) })
            assertEquals(MLLValClass(100), map.getOrPut(MLLKeyClass(1)) { MLLValClass(999) })
        }
    }

    @Test
    fun putAllAndPlusAssign() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = HashMapVLongLong<MLLKeyClass, MLLValClass>()
            map.putAll(simpleMap())
            assertEquals(3, map.size)

            val map2 = HashMapVLongLong<MLLKeyClass, MLLValClass>()
            map2 plusAssign simpleMap()
            assertEquals(3, map2.size)
        }
    }

    @Test
    fun putAllWithTransform() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = HashMapVLongLong<MLLKeyClass, MLLValClass>()
            map.putAll(simpleMap(), { pair -> pair.first }, { pair -> MLLValClass(pair.second.value * 2) })
            assertEquals(3, map.size)
            assertEquals(MLLValClass(400), map[MLLKeyClass(2)])
        }
    }

    @Test
    fun putAllGeneric() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = HashMapVLongLong<MLLKeyClass, MLLValClass>()
            map.putAllGeneric(listOf(1L to 100L, 2L to 200L)) { (k, v) -> MLLKeyClass(k) to MLLValClass(v) }
            assertEquals(2, map.size)
            assertEquals(MLLValClass(200), map[MLLKeyClass(2)])
        }
    }

    @Test
    fun removeVariants() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = simpleMap()
            map.remove(MLLKeyClass(1))
            assertEquals(2, map.size)
            assertEquals(true, map.remove(MLLKeyClass(2), MLLValClass(200)))
            assertEquals(1, map.size)
            map.removeIf { _, v -> v.value >= 300 }
            assertEquals(0, map.size)
        }
    }

    @Test
    fun minusAssign() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = simpleMap()
            map minusAssign MLLKeyClass(1)
            assertEquals(2, map.size)
        }
    }

    @Test
    fun asMapGenericNonIterating() = with (MLLKeyClass) {
        with (MLLValClass) {
            val map = simpleMap()
            val generic = map.asMapGeneric()
            assertEquals(3, generic.size)
            assertEquals(false, generic.isEmpty())
            assertEquals(true, generic.containsKey(MLLKeyClass(2)))
            assertEquals(true, generic.containsValue(MLLValClass(200)))
            assertEquals(MLLValClass(200), generic[MLLKeyClass(2)])
            assertEquals(null, generic[MLLKeyClass(99)])
            assertThrows(NotImplementedError::class.java, { generic.keys })
        }
    }
}
