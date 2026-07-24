package mpd.common.collect.valuecollections

import mpd.com.common.collect.valuecollections.*
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class MILKeyClass(val value: Int): Comparable<MILKeyClass> {
    override operator fun compareTo(other: MILKeyClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MILKeyClass> {
        override inline fun fromInt(v: Int) = MILKeyClass(v)
        override inline fun toInt(v: MILKeyClass): Int = v.value
    }
}

@JvmInline
value class MILValClass(val value: Long): Comparable<MILValClass> {
    override operator fun compareTo(other: MILValClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<MILValClass> {
        override inline fun fromLong(v: Long) = MILValClass(v)
        override inline fun toLong(v: MILValClass): Long = v.value
    }
}

class MapVIntLongTest {
    private fun simpleMap(): HashMapVIntLong<MILKeyClass, MILValClass> = with (MILKeyClass) {
        with (MILValClass) {
            HashMapVIntLong<MILKeyClass, MILValClass>().also {
                it[MILKeyClass(1)] = MILValClass(100)
                it[MILKeyClass(2)] = MILValClass(200)
                it[MILKeyClass(3)] = MILValClass(300)
            }
        }
    }

    @Test
    fun constructors() {
        val primary = HashMapVIntLong<MILKeyClass, MILValClass>()
        assertEquals(0, primary.size)
        val bySize = HashMapVIntLong<MILKeyClass, MILValClass>(10)
        assertEquals(0, bySize.size)
    }

    @Test
    fun customNullValues() {
        val map = HashMapVIntLong<MILKeyClass, MILValClass>(10, -1, -2)
        assertEquals(-1, map.NULL_KEY_BITS)
        assertEquals(-2L, map.NULL_VALUE_BITS)
    }

    @Test
    fun getSetBits() {
        val map = HashMapVIntLong<MILKeyClass, MILValClass>()
        assertEquals(map.NULL_VALUE_BITS, map.setBits(1, 100, map.NULL_VALUE_BITS))
        assertEquals(100L, map.getBits(1))
        assertEquals(100L, map.setBits(1, 200, map.NULL_VALUE_BITS))
        assertEquals(200L, map.getBits(1))
        assertEquals(map.NULL_VALUE_BITS, map.getBits(99))
    }

    @Test
    fun getOrPutBits() {
        val map = HashMapVIntLong<MILKeyClass, MILValClass>()
        assertEquals(100L, map.getOrPutBits(1) { 100 })
        assertEquals(100L, map.getOrPutBits(1) { 999 })
    }

    @Test
    fun removeBitsVariants() {
        val map = HashMapVIntLong<MILKeyClass, MILValClass>()
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
        val map = HashMapVIntLong<MILKeyClass, MILValClass>()
        map.setBits(1, 100, map.NULL_VALUE_BITS)
        map.setBits(2, 200, map.NULL_VALUE_BITS)
        map.removeIfBits { _, v -> v >= 200 }
        assertEquals(1, map.size)
        assertEquals(100L, map.getBits(1))
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
        val map = HashMapVIntLong<MILKeyClass, MILValClass>()
        assertEquals(true, map.isEmpty)
        assertEquals(false, map.isNotEmpty())
        map.setBits(1, 100, map.NULL_VALUE_BITS)
        assertEquals(false, map.isEmpty)
        assertEquals(true, map.isNotEmpty())
    }

    @Test
    fun typedGetVariants() = with (MILKeyClass) {
        with (MILValClass) {
            val map = simpleMap()
            assertEquals(MILValClass(200), map[MILKeyClass(2)])
            assertEquals(MILValClass(200), map.getOr(MILKeyClass(2)) { MILValClass(-1) })
            assertEquals(MILValClass(-1), map.getOr(MILKeyClass(99)) { MILValClass(-1) })
            assertEquals(MILValClass(200), map.getOrNull(MILKeyClass(2)))
            assertEquals(null, map.getOrNull(MILKeyClass(99)))
        }
    }

    @Test
    fun anyVariants() = with (MILKeyClass) {
        with (MILValClass) {
            val map = simpleMap()
            assertEquals(MILKeyClass(2), map.any { _, v -> v.value == 200L })
            assertEquals(MILKeyClass(2), map.anyOr({ _, v -> v.value == 200L }) { MILKeyClass(-1) })
            assertEquals(MILKeyClass(-1), map.anyOr({ _, v -> v.value == 999L }) { MILKeyClass(-1) })
            assertEquals(MILKeyClass(2), map.anyOrNull { _, v -> v.value == 200L })
            assertEquals(null, map.anyOrNull { _, v -> v.value == 999L })
        }
    }

    @Test
    fun anyIndexedVariants() = with (MILKeyClass) {
        with (MILValClass) {
            val map = simpleMap()
            val visitedIndices = mutableSetOf<Int>()
            map.forEachIndexed { i, _, _ -> visitedIndices.add(i) }
            assertEquals(setOf(0, 1, 2), visitedIndices)
            assertEquals(MILKeyClass(2), map.anyIndexed { _, k, v -> v.value == 200L })
            assertEquals(MILKeyClass(2), map.anyIndexedOr({ _, k, v -> v.value == 200L }) { MILKeyClass(-1) })
            assertEquals(MILKeyClass(-1), map.anyIndexedOr({ _, _, v -> v.value == 999L }) { MILKeyClass(-1) })
            assertEquals(null, map.anyIndexedOrNull { _, _, v -> v.value == 999L })
        }
    }

    @Test
    fun forEachVariants() = with (MILKeyClass) {
        with (MILValClass) {
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
    fun containsKeyAndValue() = with (MILKeyClass) {
        with (MILValClass) {
            val map = simpleMap()
            assertEquals(true, map.containsKey(MILKeyClass(2)))
            assertEquals(false, map.containsKey(MILKeyClass(99)))
            assertEquals(true, map.containsValue(MILValClass(200)))
            assertEquals(false, map.containsValue(MILValClass(999)))
        }
    }

    @Test
    fun toStringVSingleEntry() = with (MILKeyClass) {
        with (MILValClass) {
            val map = HashMapVIntLong<MILKeyClass, MILValClass>().also { it[MILKeyClass(1)] = MILValClass(100) }
            assertEquals("{(1:100)}", map.toStringV())
        }
    }

    @Test
    fun joinToStringLimit() = with (MILKeyClass) {
        with (MILValClass) {
            val map = simpleMap()
            val full = map.joinToString()
            assertEquals(false, full.contains("..."))
            assertEquals(3, full.split(", ").size)
            val limited = map.joinToString(limit = 1)
            assertEquals(1, limited.split(", ").size)
        }
    }

    @Test
    fun setOperatorAndWithDefault() = with (MILKeyClass) {
        with (MILValClass) {
            val map = HashMapVIntLong<MILKeyClass, MILValClass>()
            assertEquals(false, map.set(MILKeyClass(1), MILValClass(100)))
            assertEquals(true, map.set(MILKeyClass(1), MILValClass(200)))
            assertEquals(MILValClass(200), map[MILKeyClass(1)])

            assertEquals(MILValClass(-1), map.set(MILKeyClass(99), MILValClass(50), MILValClass(-1)))
            assertEquals(MILValClass(50), map.set(MILKeyClass(99), MILValClass(60), MILValClass(-1)))
        }
    }

    @Test
    fun getOrPut() = with (MILKeyClass) {
        with (MILValClass) {
            val map = HashMapVIntLong<MILKeyClass, MILValClass>()
            assertEquals(MILValClass(100), map.getOrPut(MILKeyClass(1)) { MILValClass(100) })
            assertEquals(MILValClass(100), map.getOrPut(MILKeyClass(1)) { MILValClass(999) })
        }
    }

    @Test
    fun putAllAndPlusAssign() = with (MILKeyClass) {
        with (MILValClass) {
            val map = HashMapVIntLong<MILKeyClass, MILValClass>()
            map.putAll(simpleMap())
            assertEquals(3, map.size)

            val map2 = HashMapVIntLong<MILKeyClass, MILValClass>()
            map2 += simpleMap()
            assertEquals(3, map2.size)
        }
    }

    @Test
    fun putAllWithTransform() = with (MILKeyClass) {
        with (MILValClass) {
            val map = HashMapVIntLong<MILKeyClass, MILValClass>()
            map.putAll(simpleMap(), { pair -> pair.first }, { pair -> MILValClass(pair.second.value * 2) })
            assertEquals(3, map.size)
            assertEquals(MILValClass(400), map[MILKeyClass(2)])
        }
    }

    @Test
    fun putAllGeneric() = with (MILKeyClass) {
        with (MILValClass) {
            val map = HashMapVIntLong<MILKeyClass, MILValClass>()
            map.putAllGeneric(listOf(1 to 100L, 2 to 200L)) { (k, v) -> MILKeyClass(k) to MILValClass(v) }
            assertEquals(2, map.size)
            assertEquals(MILValClass(200), map[MILKeyClass(2)])
        }
    }

    @Test
    fun removeVariants() = with (MILKeyClass) {
        with (MILValClass) {
            val map = simpleMap()
            map.remove(MILKeyClass(1))
            assertEquals(2, map.size)
            assertEquals(true, map.remove(MILKeyClass(2), MILValClass(200)))
            assertEquals(1, map.size)
            map.removeIf { _, v -> v.value >= 300 }
            assertEquals(0, map.size)
        }
    }

    @Test
    fun minusAssign() = with (MILKeyClass) {
        with (MILValClass) {
            val map = simpleMap()
            map -= MILKeyClass(1)
            assertEquals(2, map.size)
        }
    }

    @Test
    fun asMapGenericNonIterating() = with (MILKeyClass) {
        with (MILValClass) {
            val map = simpleMap()
            val generic = map.asMapGeneric()
            assertEquals(3, generic.size)
            assertEquals(false, generic.isEmpty())
            assertEquals(true, generic.containsKey(MILKeyClass(2)))
            assertEquals(true, generic.containsValue(MILValClass(200)))
            assertEquals(MILValClass(200), generic[MILKeyClass(2)])
            assertEquals(null, generic[MILKeyClass(99)])
            assertThrows(NotImplementedError::class.java, { generic.keys })
        }
    }
}
