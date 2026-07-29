package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.HashMapVIntInt
import com.mpd.common.collect.valuecollections.ValueIntAdapter
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
value class MIIKeyClass(val value: Int): Comparable<MIIKeyClass> {
    override operator fun compareTo(other: MIIKeyClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MIIKeyClass> {
        override inline fun fromInt(v: Int) = MIIKeyClass(v)
        override inline fun toInt(v: MIIKeyClass): Int = v.value
    }
}

@JvmInline
value class MIIValClass(val value: Int): Comparable<MIIValClass> {
    override operator fun compareTo(other: MIIValClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MIIValClass> {
        override inline fun fromInt(v: Int) = MIIValClass(v)
        override inline fun toInt(v: MIIValClass): Int = v.value
    }
}

class MapVIntIntTest {
    private fun simpleMap(): HashMapVIntInt<MIIKeyClass, MIIValClass> = with (MIIKeyClass) {
        with (MIIValClass) {
            HashMapVIntInt<MIIKeyClass, MIIValClass>().also {
                it[MIIKeyClass(1)] = MIIValClass(100)
                it[MIIKeyClass(2)] = MIIValClass(200)
                it[MIIKeyClass(3)] = MIIValClass(300)
            }
        }
    }

    @Test
    fun constructors() {
        val primary = HashMapVIntInt<MIIKeyClass, MIIValClass>()
        assertEquals(0, primary.size)
        val bySize = HashMapVIntInt<MIIKeyClass, MIIValClass>(10)
        assertEquals(0, bySize.size)
    }

    @Test
    fun customNullValues() {
        val map = HashMapVIntInt<MIIKeyClass, MIIValClass>(10, -1, -2)
        assertEquals(-1, map.NULL_KEY_BITS)
        assertEquals(-2, map.NULL_VALUE_BITS)
    }

    @Test
    fun getSetBits() {
        val map = HashMapVIntInt<MIIKeyClass, MIIValClass>()
        assertEquals(map.NULL_VALUE_BITS, map.setBits(1, 100, map.NULL_VALUE_BITS))
        assertEquals(100, map.getBits(1))
        assertEquals(100, map.setBits(1, 200, map.NULL_VALUE_BITS))
        assertEquals(200, map.getBits(1))
        assertEquals(map.NULL_VALUE_BITS, map.getBits(99))
    }

    @Test
    fun getOrPutBits() {
        val map = HashMapVIntInt<MIIKeyClass, MIIValClass>()
        assertEquals(100, map.getOrPutBits(1) { 100 })
        assertEquals(100, map.getOrPutBits(1) { 999 })
    }

    @Test
    fun removeBitsVariants() {
        val map = HashMapVIntInt<MIIKeyClass, MIIValClass>()
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
        val map = HashMapVIntInt<MIIKeyClass, MIIValClass>()
        map.setBits(1, 100, map.NULL_VALUE_BITS)
        map.setBits(2, 200, map.NULL_VALUE_BITS)
        map.removeIfBits { _, v -> v >= 200 }
        assertEquals(1, map.size)
        assertEquals(100, map.getBits(1))
    }

    @Test
    fun anyBits() {
        val map = simpleMap()
        assertEquals(2, map.anyBits { k, _ -> k == 2 })
        assertEquals(map.NULL_KEY_BITS, map.anyBits { k, _ -> k == 999 })
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
        val map = HashMapVIntInt<MIIKeyClass, MIIValClass>()
        assertEquals(true, map.isEmpty)
        assertEquals(false, map.isNotEmpty())
        map.setBits(1, 100, map.NULL_VALUE_BITS)
        assertEquals(false, map.isEmpty)
        assertEquals(true, map.isNotEmpty())
    }

    @Test
    fun typedGetVariants() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = simpleMap()
            assertEquals(MIIValClass(200), map[MIIKeyClass(2)])
            assertEquals(MIIValClass(200), map.getOr(MIIKeyClass(2)) { MIIValClass(-1) })
            assertEquals(MIIValClass(-1), map.getOr(MIIKeyClass(99)) { MIIValClass(-1) })
            assertEquals(MIIValClass(200), map.getOrNull(MIIKeyClass(2)))
            assertEquals(null, map.getOrNull(MIIKeyClass(99)))
        }
    }

    @Test
    fun anyVariants() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = simpleMap()
            assertEquals(MIIKeyClass(2), map.any { _, v -> v.value == 200 })
            assertEquals(MIIKeyClass(2), map.anyOr({ _, v -> v.value == 200 }) { MIIKeyClass(-1) })
            assertEquals(MIIKeyClass(-1), map.anyOr({ _, v -> v.value == 999 }) { MIIKeyClass(-1) })
            assertEquals(MIIKeyClass(2), map.anyOrNull { _, v -> v.value == 200 })
            assertEquals(null, map.anyOrNull { _, v -> v.value == 999 })
        }
    }

    @Test
    fun anyIndexedVariants() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = simpleMap()
            val visitedIndices = mutableSetOf<Int>()
            map.forEachIndexed { i, _, _ -> visitedIndices.add(i) }
            assertEquals(setOf(0, 1, 2), visitedIndices)
            assertEquals(MIIKeyClass(2), map.anyIndexed { _, k, v -> v.value == 200 })
            assertEquals(MIIKeyClass(2), map.anyIndexedOr({ _, k, v -> v.value == 200 }) { MIIKeyClass(-1) })
            assertEquals(MIIKeyClass(-1), map.anyIndexedOr({ _, _, v -> v.value == 999 }) { MIIKeyClass(-1) })
            assertEquals(null, map.anyIndexedOrNull { _, _, v -> v.value == 999 })
        }
    }

    @Test
    fun forEachVariants() = with (MIIKeyClass) {
        with (MIIValClass) {
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
    fun containsKeyAndValue() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = simpleMap()
            assertEquals(true, map.containsKey(MIIKeyClass(2)))
            assertEquals(false, map.containsKey(MIIKeyClass(99)))
            assertEquals(true, map.containsValue(MIIValClass(200)))
            assertEquals(false, map.containsValue(MIIValClass(999)))
        }
    }

    @Test
    fun joinToStringAndToStringVSingleEntry() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = HashMapVIntInt<MIIKeyClass, MIIValClass>().also { it[MIIKeyClass(1)] = MIIValClass(100) }
            assertEquals("{(1:100)}", map.toStringV())
        }
    }

    @Test
    fun joinToStringLimit() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = simpleMap()
            val full = map.joinToString()
            assertEquals(false, full.contains("..."))
            assertEquals(3, full.split(", ").size)
            val limited = map.joinToString(limit = 1)
            assertEquals(1, limited.split(", ").size)
        }
    }

    @Test
    fun setOperatorAndWithDefault() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = HashMapVIntInt<MIIKeyClass, MIIValClass>()
            assertEquals(false, map.set(MIIKeyClass(1), MIIValClass(100)))
            assertEquals(true, map.set(MIIKeyClass(1), MIIValClass(200)))
            assertEquals(MIIValClass(200), map[MIIKeyClass(1)])

            assertEquals(MIIValClass(-1), map.set(MIIKeyClass(99), MIIValClass(50), MIIValClass(-1)))
            assertEquals(MIIValClass(50), map.set(MIIKeyClass(99), MIIValClass(60), MIIValClass(-1)))
        }
    }

    @Test
    fun getOrPut() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = HashMapVIntInt<MIIKeyClass, MIIValClass>()
            assertEquals(MIIValClass(100), map.getOrPut(MIIKeyClass(1)) { MIIValClass(100) })
            assertEquals(MIIValClass(100), map.getOrPut(MIIKeyClass(1)) { MIIValClass(999) })
        }
    }

    @Test
    fun putAllAndPlusAssign() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = HashMapVIntInt<MIIKeyClass, MIIValClass>()
            map.putAll(simpleMap())
            assertEquals(3, map.size)

            val map2 = HashMapVIntInt<MIIKeyClass, MIIValClass>()
            map2 plusAssign simpleMap()
            assertEquals(3, map2.size)
        }
    }

    @Test
    fun putAllWithTransform() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = HashMapVIntInt<MIIKeyClass, MIIValClass>()
            map.putAll(simpleMap(), { pair -> pair.first }, { pair -> MIIValClass(pair.second.value * 2) })
            assertEquals(3, map.size)
            assertEquals(MIIValClass(400), map[MIIKeyClass(2)])
        }
    }

    @Test
    fun putAllGeneric() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = HashMapVIntInt<MIIKeyClass, MIIValClass>()
            map.putAllGeneric(listOf(1 to 100, 2 to 200)) { (k, v) -> MIIKeyClass(k) to MIIValClass(v) }
            assertEquals(2, map.size)
            assertEquals(MIIValClass(200), map[MIIKeyClass(2)])
        }
    }

    @Test
    fun removeVariants() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = simpleMap()
            map.remove(MIIKeyClass(1))
            assertEquals(2, map.size)
            assertEquals(true, map.remove(MIIKeyClass(2), MIIValClass(200)))
            assertEquals(1, map.size)
            map.removeIf { _, v -> v.value >= 300 }
            assertEquals(0, map.size)
        }
    }

    @Test
    fun minusAssign() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = simpleMap()
            map minusAssign MIIKeyClass(1)
            assertEquals(2, map.size)
        }
    }

    @Test
    fun asMapGenericNonIterating() = with (MIIKeyClass) {
        with (MIIValClass) {
            val map = simpleMap()
            val generic = map.asMapGeneric()
            assertEquals(3, generic.size)
            assertEquals(false, generic.isEmpty())
            assertEquals(true, generic.containsKey(MIIKeyClass(2)))
            assertEquals(true, generic.containsValue(MIIValClass(200)))
            assertEquals(MIIValClass(200), generic[MIIKeyClass(2)])
            assertEquals(null, generic[MIIKeyClass(99)])
            // asIterable() is not implemented, so anything needing iteration throws
            assertThrows(NotImplementedError::class.java, { generic.keys })
        }
    }
}
