package com.mpd.common.collect.valuecollections

import androidx.collection.MutableIntFloatMap
import androidx.collection.mutableIntSetOf
import com.mpd.common.collect.valuecollections.HashMapVIntFloat
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
value class MIFKeyClass(val value: Int): Comparable<MIFKeyClass> {
    override operator fun compareTo(other: MIFKeyClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MIFKeyClass> {
        override inline fun fromInt(v: Int) = MIFKeyClass(v)
        override inline fun toInt(v: MIFKeyClass): Int = v.value
    }
}

class MapVIntFloatTest {
    private fun simpleMap(): HashMapVIntFloat<MIFKeyClass> = with (MIFKeyClass) {
        HashMapVIntFloat<MIFKeyClass>().also {
            it[MIFKeyClass(1)] = 100f
            it[MIFKeyClass(2)] = 200f
            it[MIFKeyClass(3)] = 300f
        }
    }

    @Test
    fun constructors() {
        val primary = HashMapVIntFloat<MIFKeyClass>()
        assertEquals(0, primary.size)
        val bySize = HashMapVIntFloat<MIFKeyClass>(10)
        assertEquals(0, bySize.size)
    }

    @Test
    fun customNullValues() {
        val map = HashMapVIntFloat<MIFKeyClass>(10, -1, -999f)
        assertEquals(-1, map.NULL_KEY_BITS)
        assertEquals(-999f, map.NULL_VALUE)
    }

    @Test
    fun getSetBits() {
        val map = HashMapVIntFloat<MIFKeyClass>()
        assertEquals(map.NULL_VALUE, map.setBits(1, 100f, map.NULL_VALUE))
        assertEquals(100f, map.getBits(1))
        assertEquals(100f, map.setBits(1, 200f, map.NULL_VALUE))
        assertEquals(200f, map.getBits(1))
        assertEquals(map.NULL_VALUE, map.getBits(99))
    }

    @Test
    fun getOrPutBits() {
        val map = HashMapVIntFloat<MIFKeyClass>()
        assertEquals(100f, map.getOrPutBits(1) { 100f })
        assertEquals(100f, map.getOrPutBits(1) { 999f })
    }

    @Test
    fun removeBitsVariants() {
        val map = HashMapVIntFloat<MIFKeyClass>()
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
        val map = HashMapVIntFloat<MIFKeyClass>()
        map.setBits(1, 100f, map.NULL_VALUE)
        map.setBits(2, 200f, map.NULL_VALUE)
        map.removeIfBits { _, v -> v >= 200f }
        assertEquals(1, map.size)
        assertEquals(100f, map.getBits(1))
    }

    @Test
    fun anyBitsPredicate() {
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
        val map = HashMapVIntFloat<MIFKeyClass>()
        assertEquals(true, map.isEmpty)
        assertEquals(false, map.isNotEmpty())
        map.setBits(1, 100f, map.NULL_VALUE)
        assertEquals(false, map.isEmpty)
        assertEquals(true, map.isNotEmpty())
    }

    @Test
    fun typedGetVariants() = with (MIFKeyClass) {
        val map = simpleMap()
        assertEquals(200f, map[MIFKeyClass(2)])
        assertEquals(200f, map.getOr(MIFKeyClass(2)) { -1f })
        assertEquals(-1f, map.getOr(MIFKeyClass(99)) { -1f })
        assertEquals(200f, map.getOrNull(MIFKeyClass(2)))
        assertEquals(null, map.getOrNull(MIFKeyClass(99)))
    }

    @Test
    fun anyVariants() = with (MIFKeyClass) {
        val map = simpleMap()
        assertEquals(MIFKeyClass(2), map.any { _, v -> v == 200f })
        assertEquals(MIFKeyClass(2), map.anyOr({ _, v -> v == 200f }) { MIFKeyClass(-1) })
        assertEquals(MIFKeyClass(-1), map.anyOr({ _, v -> v == 999f }) { MIFKeyClass(-1) })
        assertEquals(MIFKeyClass(2), map.anyOrNull { _, v -> v == 200f })
        assertEquals(null, map.anyOrNull { _, v -> v == 999f })
    }

    @Test
    fun anyIndexedVariants() = with (MIFKeyClass) {
        val map = simpleMap()
        val visitedIndices = mutableSetOf<Int>()
        map.forEachIndexed { i, _, _ -> visitedIndices.add(i) }
        assertEquals(setOf(0, 1, 2), visitedIndices)
        assertEquals(MIFKeyClass(2), map.anyIndexed { _, _, v -> v == 200f })
        assertEquals(MIFKeyClass(2), map.anyIndexedOr({ _, _, v -> v == 200f }) { MIFKeyClass(-1) })
        assertEquals(MIFKeyClass(-1), map.anyIndexedOr({ _, _, v -> v == 999f }) { MIFKeyClass(-1) })
        assertEquals(null, map.anyIndexedOrNull { _, _, v -> v == 999f })
    }

    @Test
    fun forEachVariants() = with (MIFKeyClass) {
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
    fun containsKeyAndValue() = with (MIFKeyClass) {
        val map = simpleMap()
        assertEquals(true, map.containsKey(MIFKeyClass(2)))
        assertEquals(false, map.containsKey(MIFKeyClass(99)))
        assertEquals(true, map.containsValue(200f))
        assertEquals(false, map.containsValue(999f))
    }

    @Test
    fun toStringVSingleEntry() = with (MIFKeyClass) {
        val map = HashMapVIntFloat<MIFKeyClass>().also { it[MIFKeyClass(1)] = 100f }
        assertEquals("{(1:100.0)}", map.toStringV())
    }

    @Test
    fun joinToStringLimit() = with (MIFKeyClass) {
        val map = simpleMap()
        val full = map.joinToString()
        assertEquals(false, full.contains("..."))
        assertEquals(3, full.split(", ").size)
        val limited = map.joinToString(limit = 1)
        assertEquals(1, limited.split(", ").size)
    }

    @Test
    fun setOperatorAndWithDefault() = with (MIFKeyClass) {
        val map = HashMapVIntFloat<MIFKeyClass>()
        assertEquals(false, map.set(MIFKeyClass(1), 100f))
        assertEquals(true, map.set(MIFKeyClass(1), 200f))
        assertEquals(200f, map[MIFKeyClass(1)])

        assertEquals(-1f, map.set(MIFKeyClass(99), 50f, -1f))
        assertEquals(50f, map.set(MIFKeyClass(99), 60f, -1f))
    }

    @Test
    fun getOrPut() = with (MIFKeyClass) {
        val map = HashMapVIntFloat<MIFKeyClass>()
        assertEquals(100f, map.getOrPut(MIFKeyClass(1)) { 100f })
        assertEquals(100f, map.getOrPut(MIFKeyClass(1)) { 999f })
    }

    @Test
    fun putAllAndPlusAssign() = with (MIFKeyClass) {
        val map = HashMapVIntFloat<MIFKeyClass>()
        map.putAll(simpleMap())
        assertEquals(3, map.size)

        val map2 = HashMapVIntFloat<MIFKeyClass>()
        map2 plusAssign simpleMap()
        assertEquals(3, map2.size)
    }

    @Test
    fun putAllGeneric() = with (MIFKeyClass) {
        val map = HashMapVIntFloat<MIFKeyClass>()
        map.putAllGeneric(listOf(1 to 100f, 2 to 200f)) { (k, v) -> MIFKeyClass(k) to v }
        assertEquals(2, map.size)
        assertEquals(200f, map[MIFKeyClass(2)])
    }

    @Test
    fun removeVariants() = with (MIFKeyClass) {
        val map = simpleMap()
        map.remove(MIFKeyClass(1))
        assertEquals(2, map.size)
        assertEquals(true, map.remove(MIFKeyClass(2), 200f))
        assertEquals(1, map.size)
        map.removeIf { _, v -> v >= 300f }
        assertEquals(0, map.size)
    }

    @Test
    fun minusAssign() = with (MIFKeyClass) {
        val map = simpleMap()
        map minusAssign MIFKeyClass(1)
        assertEquals(2, map.size)
    }

    @Test
    fun asMapGenericNonIterating() = with (MIFKeyClass) {
        val map = simpleMap()
        val generic = map.asMapGeneric()
        assertEquals(3, generic.size)
        assertEquals(false, generic.isEmpty())
        assertEquals(true, generic.containsKey(MIFKeyClass(2)))
        assertEquals(true, generic.containsValue(200f))
        assertEquals(200f, generic[MIFKeyClass(2)])
        assertEquals(null, generic[MIFKeyClass(99)])
        assertThrows(NotImplementedError::class.java, { generic.keys })
    }

    @Test
    fun rawWrapperMethods() {
        val map = simpleMap()
        assertEquals(true, map.capacity() >= 3)
        assertEquals(true, map.anyBits())
        assertEquals(false, map.none())
        assertEquals(true, map.isNotEmptyBits())
        assertEquals(200f, map.getOrDefault(2, -1f))
        assertEquals(-1f, map.getOrDefault(99, -1f))
        assertEquals(200f, map.getOrElse(2) { -1f })
        assertEquals(-1f, map.getOrElse(99) { -1f })

        val keys = mutableSetOf<Int>()
        map.forEachKey { keys.add(it) }
        assertEquals(setOf(1, 2, 3), keys)

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

        val other = MutableIntFloatMap().also { it[10] = 1000f }
        map.putAllBits(other)
        assertEquals(1000f, map.getBits(10))

        val other2 = MutableIntFloatMap().also { it[11] = 1100f }
        map.plusAssignBits(other2)
        assertEquals(1100f, map.getBits(11))

        map.minusAssignBits(10)
        assertEquals(false, map.containsKeyBits(10))
        map.minusAssignBits(intArrayOf(11))
        assertEquals(false, map.containsKeyBits(11))
        map.setBits(20, 2000f)
        map.minusAssignBits(mutableIntSetOf(20))
        assertEquals(false, map.containsKeyBits(20))
    }
}
