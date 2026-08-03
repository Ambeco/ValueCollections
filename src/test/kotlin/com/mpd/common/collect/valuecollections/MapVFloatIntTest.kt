package com.mpd.common.collect.valuecollections

import androidx.collection.MutableFloatIntMap
import androidx.collection.mutableFloatSetOf
import com.mpd.common.collect.valuecollections.HashMapVFloatInt
import com.mpd.common.collect.valuecollections.ValueIntAdapter
import com.mpd.common.collect.valuecollections.any
import com.mpd.common.collect.valuecollections.anyIndexed
import com.mpd.common.collect.valuecollections.anyIndexedOr
import com.mpd.common.collect.valuecollections.anyOr
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
value class MFIValClass(val value: Int): Comparable<MFIValClass> {
    override operator fun compareTo(other: MFIValClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MFIValClass> {
        override inline fun fromInt(v: Int) = MFIValClass(v)
        override inline fun toInt(v: MFIValClass): Int = v.value
    }
}

class MapVFloatIntTest {
    private fun simpleMap(): HashMapVFloatInt<MFIValClass> = with (MFIValClass) {
        HashMapVFloatInt<MFIValClass>().also {
            it[1f] = MFIValClass(1)
            it[2f] = MFIValClass(2)
            it[3f] = MFIValClass(3)
        }
    }

    @Test
    fun constructors() {
        val primary = HashMapVFloatInt<MFIValClass>()
        assertEquals(0, primary.size)
        val bySize = HashMapVFloatInt<MFIValClass>(10)
        assertEquals(0, bySize.size)
    }

    @Test
    fun customNullValue() {
        val map = HashMapVFloatInt<MFIValClass>(10, -1)
        assertEquals(-1, map.NULL_VALUE_BITS)
    }

    @Test
    fun getSetBits() {
        val map = HashMapVFloatInt<MFIValClass>()
        assertEquals(map.NULL_VALUE_BITS, map.setBits(1f, 1, map.NULL_VALUE_BITS))
        assertEquals(1, map.getBits(1f))
        assertEquals(1, map.setBits(1f, 11, map.NULL_VALUE_BITS))
        assertEquals(11, map.getBits(1f))
        assertEquals(map.NULL_VALUE_BITS, map.getBits(99f))
    }

    @Test
    fun getOrPutBits() {
        val map = HashMapVFloatInt<MFIValClass>()
        assertEquals(1, map.getOrPutBits(1f) { 1 })
        assertEquals(1, map.getOrPutBits(1f) { 999 })
    }

    @Test
    fun removeBitsVariants() {
        val map = HashMapVFloatInt<MFIValClass>()
        map.setBits(1f, 1, map.NULL_VALUE_BITS)
        map.setBits(2f, 2, map.NULL_VALUE_BITS)
        map.removeBits(1f)
        assertEquals(1, map.size)
        assertEquals(true, map.removeBits(2f, 2))
        assertEquals(0, map.size)
        assertEquals(false, map.removeBits(99f, 999))
    }

    @Test
    fun removeIfBits() {
        val map = HashMapVFloatInt<MFIValClass>()
        map.setBits(1f, 1, map.NULL_VALUE_BITS)
        map.setBits(2f, 2, map.NULL_VALUE_BITS)
        map.removeIfBits { _, v -> v >= 2 }
        assertEquals(1, map.size)
        assertEquals(1, map.getBits(1f))
    }

    @Test
    fun anyBitsPredicate() {
        val map = simpleMap()
        assertEquals(2f, map.anyBits { k, _ -> k == 2f })
        assertEquals(null, map.anyBits { k, _ -> k == 999f })
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
        val map = HashMapVFloatInt<MFIValClass>()
        assertEquals(true, map.isEmpty)
        assertEquals(false, map.isNotEmpty())
        map.setBits(1f, 1, map.NULL_VALUE_BITS)
        assertEquals(false, map.isEmpty)
        assertEquals(true, map.isNotEmpty())
    }

    @Test
    fun typedGetVariants() = with (MFIValClass) {
        val map = simpleMap()
        assertEquals(MFIValClass(2), map[2f])
        assertEquals(MFIValClass(2), map.getOr(2f) { MFIValClass(-1) })
        assertEquals(MFIValClass(-1), map.getOr(99f) { MFIValClass(-1) })
        assertEquals(MFIValClass(2), map.getOrNull(2f))
        assertEquals(null, map.getOrNull(99f))
    }

    @Test
    fun anyVariants() = with (MFIValClass) {
        val map = simpleMap()
        assertEquals(2f, map.any { _, v -> v.value == 2 })
        assertEquals(2f, map.anyOr({ _, v -> v.value == 2 }) { -1f })
        assertEquals(-1f, map.anyOr({ _, v -> v.value == 999 }) { -1f })
    }

    @Test
    fun anyIndexedVariants() = with (MFIValClass) {
        val map = simpleMap()
        val visitedIndices = mutableSetOf<Int>()
        map.forEachIndexed { i, _, _ -> visitedIndices.add(i) }
        assertEquals(setOf(0, 1, 2), visitedIndices)
        assertEquals(2f, map.anyIndexed { _, k, v -> v.value == 2 })
        assertEquals(2f, map.anyIndexedOr({ _, k, v -> v.value == 2 }) { -1f })
        assertEquals(-1f, map.anyIndexedOr({ _, _, v -> v.value == 999 }) { -1f })
    }

    @Test
    fun forEachVariants() = with (MFIValClass) {
        val map = simpleMap()
        var sum = 0
        map.forEach { _, v -> sum += v.value }
        assertEquals(6, sum)

        val pairValues = mutableListOf<Int>()
        map.forEachPair { p -> pairValues.add(p.second.value) }
        assertEquals(setOf(1, 2, 3), pairValues.toSet())

        val bitsValues = mutableListOf<Int>()
        map.forEachBits { _, v -> bitsValues.add(v) }
        assertEquals(setOf(1, 2, 3), bitsValues.toSet())
    }

    @Test
    fun containsKeyAndValue() = with (MFIValClass) {
        val map = simpleMap()
        assertEquals(true, map.containsKey(2f))
        assertEquals(false, map.containsKey(99f))
        assertEquals(true, map.containsValue(MFIValClass(2)))
        assertEquals(false, map.containsValue(MFIValClass(999)))
    }

    @Test
    fun toStringVSingleEntry() = with (MFIValClass) {
        val map = HashMapVFloatInt<MFIValClass>().also { it[1f] = MFIValClass(1) }
        assertEquals("{(1.0:1)}", map.toStringV())
    }

    @Test
    fun joinToStringLimit() = with (MFIValClass) {
        val map = simpleMap()
        val full = map.joinToString()
        assertEquals(false, full.contains("..."))
        assertEquals(3, full.split(", ").size)
        val limited = map.joinToString(limit = 1)
        assertEquals(1, limited.split(", ").size)
    }

    @Test
    fun setOperatorAndWithDefault() = with (MFIValClass) {
        val map = HashMapVFloatInt<MFIValClass>()
        assertEquals(false, map.set(1f, MFIValClass(1)))
        assertEquals(true, map.set(1f, MFIValClass(11)))
        assertEquals(MFIValClass(11), map[1f])

        assertEquals(MFIValClass(-1), map.set(99f, MFIValClass(50), MFIValClass(-1)))
        assertEquals(MFIValClass(50), map.set(99f, MFIValClass(60), MFIValClass(-1)))
    }

    @Test
    fun getOrPut() = with (MFIValClass) {
        val map = HashMapVFloatInt<MFIValClass>()
        assertEquals(MFIValClass(1), map.getOrPut(1f) { MFIValClass(1) })
        assertEquals(MFIValClass(1), map.getOrPut(1f) { MFIValClass(999) })
    }

    @Test
    fun putAllAndPlusAssign() = with (MFIValClass) {
        val map = HashMapVFloatInt<MFIValClass>()
        map.putAll(simpleMap())
        assertEquals(3, map.size)

        val map2 = HashMapVFloatInt<MFIValClass>()
        map2 plusAssign simpleMap()
        assertEquals(3, map2.size)
    }

    @Test
    fun putAllGeneric() = with (MFIValClass) {
        val map = HashMapVFloatInt<MFIValClass>()
        map.putAllGeneric(listOf(1f to 1, 2f to 2)) { (k, v) -> k to MFIValClass(v) }
        assertEquals(2, map.size)
        assertEquals(MFIValClass(2), map[2f])
    }

    @Test
    fun removeVariants() = with (MFIValClass) {
        val map = simpleMap()
        map.remove(1f)
        assertEquals(2, map.size)
        assertEquals(true, map.remove(2f, MFIValClass(2)))
        assertEquals(1, map.size)
        map.removeIf { _, v -> v.value >= 3 }
        assertEquals(0, map.size)
    }

    @Test
    fun minusAssign() = with (MFIValClass) {
        val map = simpleMap()
        map minusAssign 1f
        assertEquals(2, map.size)
    }

    @Test
    fun asMapGenericNonIterating() = with (MFIValClass) {
        val map = simpleMap()
        val generic = map.asMapGeneric()
        assertEquals(3, generic.size)
        assertEquals(false, generic.isEmpty())
        assertEquals(true, generic.containsKey(2f))
        assertEquals(true, generic.containsValue(MFIValClass(2)))
        assertEquals(MFIValClass(2), generic[2f])
        assertEquals(null, generic[99f])
        assertThrows(NotImplementedError::class.java, { generic.keys })
    }

    @Test
    fun rawWrapperMethods() {
        val map = simpleMap()
        assertEquals(true, map.capacity() >= 3)
        assertEquals(false, map.none())
        assertEquals(true, map.isNotEmptyBits())
        assertEquals(2, map.getOrDefault(2f, -1))
        assertEquals(-1, map.getOrDefault(99f, -1))
        assertEquals(2, map.getOrElse(2f) { -1 })
        assertEquals(-1, map.getOrElse(99f) { -1 })

        val keys = mutableSetOf<Float>()
        map.forEachKey { keys.add(it) }
        assertEquals(setOf(1f, 2f, 3f), keys)

        val values = mutableSetOf<Int>()
        map.forEachValue { values.add(it) }
        assertEquals(setOf(1, 2, 3), values)

        assertEquals(true, map.all { _, v -> v >= 1 })
        assertEquals(false, map.all { _, v -> v >= 2 })
        assertEquals(3, map.count())
        assertEquals(2, map.count { _, v -> v >= 2 })
        assertEquals(true, map.contains(2f))
        assertEquals(false, map.contains(99f))
        assertEquals(true, map.containsKeyBits(2f))
        assertEquals(false, map.containsKeyBits(99f))
        assertEquals(true, map.containsValueBits(2))
        assertEquals(false, map.containsValueBits(999))

        assertEquals(3, map.joinToStringBits().split(", ").size)
        assertEquals(true, map.joinToStringBits(transform = { k, v -> "$k=$v" }).contains("2.0=2"))

        map.setBits(4f, 4)
        assertEquals(4, map.getBits(4f))
        map.put(4f, 5)
        assertEquals(5, map.getBits(4f))
        assertEquals(5, map.put(4f, 6, -1))
        assertEquals(-1, map.put(5f, 5, -1))

        val other = MutableFloatIntMap().also { it[10f] = 1000 }
        map.putAllBits(other)
        assertEquals(1000, map.getBits(10f))

        val other2 = MutableFloatIntMap().also { it[11f] = 1100 }
        map.plusAssignBits(other2)
        assertEquals(1100, map.getBits(11f))

        map.minusAssignBits(10f)
        assertEquals(false, map.containsKeyBits(10f))
        map.minusAssignBits(floatArrayOf(11f))
        assertEquals(false, map.containsKeyBits(11f))
        map.setBits(20f, 20)
        map.minusAssignBits(mutableFloatSetOf(20f))
        assertEquals(false, map.containsKeyBits(20f))
    }
}
