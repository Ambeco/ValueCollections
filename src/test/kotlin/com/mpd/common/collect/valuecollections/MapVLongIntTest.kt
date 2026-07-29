package com.mpd.common.collect.valuecollections

import androidx.collection.MutableLongIntMap
import com.mpd.common.collect.valuecollections.HashMapVLongInt
import com.mpd.common.collect.valuecollections.ValueIntAdapter
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
value class MLIKeyClass(val value: Long): Comparable<MLIKeyClass> {
    override operator fun compareTo(other: MLIKeyClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<MLIKeyClass> {
        override inline fun fromLong(v: Long) = MLIKeyClass(v)
        override inline fun toLong(v: MLIKeyClass): Long = v.value
    }
}

@JvmInline
value class MLIValClass(val value: Int): Comparable<MLIValClass> {
    override operator fun compareTo(other: MLIValClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MLIValClass> {
        override inline fun fromInt(v: Int) = MLIValClass(v)
        override inline fun toInt(v: MLIValClass): Int = v.value
    }
}

class MapVLongIntTest {
    private fun simpleMap(): HashMapVLongInt<MLIKeyClass, MLIValClass> = with (MLIKeyClass) {
        with (MLIValClass) {
            HashMapVLongInt<MLIKeyClass, MLIValClass>().also {
                it[MLIKeyClass(1)] = MLIValClass(100)
                it[MLIKeyClass(2)] = MLIValClass(200)
                it[MLIKeyClass(3)] = MLIValClass(300)
            }
        }
    }

    @Test
    fun constructors() {
        val primary = HashMapVLongInt<MLIKeyClass, MLIValClass>()
        assertEquals(0, primary.size)
        val bySize = HashMapVLongInt<MLIKeyClass, MLIValClass>(10)
        assertEquals(0, bySize.size)
    }

    @Test
    fun customNullValues() {
        val map = HashMapVLongInt<MLIKeyClass, MLIValClass>(MutableLongIntMap(), -1, -2)
        assertEquals(-1L, map.NULL_KEY_BITS)
        assertEquals(-2, map.NULL_VALUE_BITS)
    }

    @Test
    fun getSetBits() {
        val map = HashMapVLongInt<MLIKeyClass, MLIValClass>()
        assertEquals(map.NULL_VALUE_BITS, map.setBits(1, 100, map.NULL_VALUE_BITS))
        assertEquals(100, map.getBits(1))
        assertEquals(100, map.setBits(1, 200, map.NULL_VALUE_BITS))
        assertEquals(200, map.getBits(1))
        assertEquals(map.NULL_VALUE_BITS, map.getBits(99))
    }

    @Test
    fun getOrPutBits() {
        val map = HashMapVLongInt<MLIKeyClass, MLIValClass>()
        assertEquals(100, map.getOrPutBits(1) { 100 })
        assertEquals(100, map.getOrPutBits(1) { 999 })
    }

    @Test
    fun removeBitsVariants() {
        val map = HashMapVLongInt<MLIKeyClass, MLIValClass>()
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
        val map = HashMapVLongInt<MLIKeyClass, MLIValClass>()
        map.setBits(1, 100, map.NULL_VALUE_BITS)
        map.setBits(2, 200, map.NULL_VALUE_BITS)
        map.removeIfBits { _, v -> v >= 200 }
        assertEquals(1, map.size)
        assertEquals(100, map.getBits(1))
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
        val map = HashMapVLongInt<MLIKeyClass, MLIValClass>()
        assertEquals(true, map.isEmpty)
        assertEquals(false, map.isNotEmpty())
        map.setBits(1, 100, map.NULL_VALUE_BITS)
        assertEquals(false, map.isEmpty)
        assertEquals(true, map.isNotEmpty())
    }

    @Test
    fun typedGetVariants() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = simpleMap()
            assertEquals(MLIValClass(200), map[MLIKeyClass(2)])
            assertEquals(MLIValClass(200), map.getOr(MLIKeyClass(2)) { MLIValClass(-1) })
            assertEquals(MLIValClass(-1), map.getOr(MLIKeyClass(99)) { MLIValClass(-1) })
            assertEquals(MLIValClass(200), map.getOrNull(MLIKeyClass(2)))
            assertEquals(null, map.getOrNull(MLIKeyClass(99)))
        }
    }

    @Test
    fun anyVariants() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = simpleMap()
            assertEquals(MLIKeyClass(2), map.any { _, v -> v.value == 200 })
            assertEquals(MLIKeyClass(2), map.anyOr({ _, v -> v.value == 200 }) { MLIKeyClass(-1) })
            assertEquals(MLIKeyClass(-1), map.anyOr({ _, v -> v.value == 999 }) { MLIKeyClass(-1) })
            assertEquals(MLIKeyClass(2), map.anyOrNull { _, v -> v.value == 200 })
            assertEquals(null, map.anyOrNull { _, v -> v.value == 999 })
        }
    }

    @Test
    fun anyIndexedVariants() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = simpleMap()
            val visitedIndices = mutableSetOf<Int>()
            map.forEachIndexed { i, _, _ -> visitedIndices.add(i) }
            assertEquals(setOf(0, 1, 2), visitedIndices)
            assertEquals(MLIKeyClass(2), map.anyIndexed { _, k, v -> v.value == 200 })
            assertEquals(MLIKeyClass(2), map.anyIndexedOr({ _, k, v -> v.value == 200 }) { MLIKeyClass(-1) })
            assertEquals(MLIKeyClass(-1), map.anyIndexedOr({ _, _, v -> v.value == 999 }) { MLIKeyClass(-1) })
            assertEquals(null, map.anyIndexedOrNull { _, _, v -> v.value == 999 })
        }
    }

    @Test
    fun forEachVariants() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = simpleMap()
            var sum = 0
            map.forEach { _, v -> sum += v.value }
            assertEquals(600, sum)

            val pairValues = mutableListOf<Int>()
            map.forEachPair { p -> pairValues.add(p.second.value) }
            assertEquals(setOf(100, 200, 300), pairValues.toSet())

            val bitsValues = mutableListOf<Int>()
            map.forEachBits { _, v -> bitsValues.add(v) }
            assertEquals(setOf(100, 200, 300), bitsValues.toSet())
        }
    }

    @Test
    fun containsKeyAndValue() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = simpleMap()
            assertEquals(true, map.containsKey(MLIKeyClass(2)))
            assertEquals(false, map.containsKey(MLIKeyClass(99)))
            assertEquals(true, map.containsValue(MLIValClass(200)))
            assertEquals(false, map.containsValue(MLIValClass(999)))
        }
    }

    @Test
    fun toStringVSingleEntry() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = HashMapVLongInt<MLIKeyClass, MLIValClass>().also { it[MLIKeyClass(1)] = MLIValClass(100) }
            assertEquals("{(1:100)}", map.toStringV())
        }
    }

    @Test
    fun joinToStringLimit() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = simpleMap()
            val full = map.joinToString()
            assertEquals(false, full.contains("..."))
            assertEquals(3, full.split(", ").size)
            val limited = map.joinToString(limit = 1)
            assertEquals(1, limited.split(", ").size)
        }
    }

    @Test
    fun setOperatorAndWithDefault() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = HashMapVLongInt<MLIKeyClass, MLIValClass>()
            assertEquals(false, map.set(MLIKeyClass(1), MLIValClass(100)))
            assertEquals(true, map.set(MLIKeyClass(1), MLIValClass(200)))
            assertEquals(MLIValClass(200), map[MLIKeyClass(1)])

            assertEquals(MLIValClass(-1), map.set(MLIKeyClass(99), MLIValClass(50), MLIValClass(-1)))
            assertEquals(MLIValClass(50), map.set(MLIKeyClass(99), MLIValClass(60), MLIValClass(-1)))
        }
    }

    @Test
    fun getOrPut() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = HashMapVLongInt<MLIKeyClass, MLIValClass>()
            assertEquals(MLIValClass(100), map.getOrPut(MLIKeyClass(1)) { MLIValClass(100) })
            assertEquals(MLIValClass(100), map.getOrPut(MLIKeyClass(1)) { MLIValClass(999) })
        }
    }

    @Test
    fun putAllAndPlusAssign() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = HashMapVLongInt<MLIKeyClass, MLIValClass>()
            map.putAll(simpleMap())
            assertEquals(3, map.size)

            val map2 = HashMapVLongInt<MLIKeyClass, MLIValClass>()
            map2 plusAssign simpleMap()
            assertEquals(3, map2.size)
        }
    }

    @Test
    fun putAllWithTransform() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = HashMapVLongInt<MLIKeyClass, MLIValClass>()
            map.putAll(simpleMap(), { pair -> pair.first }, { pair -> MLIValClass(pair.second.value * 2) })
            assertEquals(3, map.size)
            assertEquals(MLIValClass(400), map[MLIKeyClass(2)])
        }
    }

    @Test
    fun putAllGeneric() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = HashMapVLongInt<MLIKeyClass, MLIValClass>()
            map.putAllGeneric(listOf(1L to 100, 2L to 200)) { (k, v) -> MLIKeyClass(k) to MLIValClass(v) }
            assertEquals(2, map.size)
            assertEquals(MLIValClass(200), map[MLIKeyClass(2)])
        }
    }

    @Test
    fun removeVariants() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = simpleMap()
            map.remove(MLIKeyClass(1))
            assertEquals(2, map.size)
            assertEquals(true, map.remove(MLIKeyClass(2), MLIValClass(200)))
            assertEquals(1, map.size)
            map.removeIf { _, v -> v.value >= 300 }
            assertEquals(0, map.size)
        }
    }

    @Test
    fun minusAssign() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = simpleMap()
            map minusAssign MLIKeyClass(1)
            assertEquals(2, map.size)
        }
    }

    @Test
    fun asMapGenericNonIterating() = with (MLIKeyClass) {
        with (MLIValClass) {
            val map = simpleMap()
            val generic = map.asMapGeneric()
            assertEquals(3, generic.size)
            assertEquals(false, generic.isEmpty())
            assertEquals(true, generic.containsKey(MLIKeyClass(2)))
            assertEquals(true, generic.containsValue(MLIValClass(200)))
            assertEquals(MLIValClass(200), generic[MLIKeyClass(2)])
            assertEquals(null, generic[MLIKeyClass(99)])
            assertThrows(NotImplementedError::class.java, { generic.keys })
        }
    }
}
