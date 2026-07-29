package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.HashMapVObjLong
import com.mpd.common.collect.valuecollections.ValueLongAdapter
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
value class MOLValClass(val value: Long): Comparable<MOLValClass> {
    override operator fun compareTo(other: MOLValClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<MOLValClass> {
        override inline fun fromLong(v: Long) = MOLValClass(v)
        override inline fun toLong(v: MOLValClass): Long = v.value
    }
}

class MapVObjLongTest {
    private fun simpleMap(): HashMapVObjLong<String, MOLValClass> = with (MOLValClass) {
        HashMapVObjLong<String, MOLValClass>().also {
            it["one"] = MOLValClass(1)
            it["two"] = MOLValClass(2)
            it["three"] = MOLValClass(3)
        }
    }

    @Test
    fun constructors() {
        val primary = HashMapVObjLong<String, MOLValClass>()
        assertEquals(0, primary.size)
        val bySize = HashMapVObjLong<String, MOLValClass>(10)
        assertEquals(0, bySize.size)
    }

    @Test
    fun customNullValue() {
        val map = HashMapVObjLong<String, MOLValClass>(10, -1)
        assertEquals(-1L, map.NULL_VALUE_BITS)
    }

    @Test
    fun getSetBits() {
        val map = HashMapVObjLong<String, MOLValClass>()
        assertEquals(map.NULL_VALUE_BITS, map.setBits("one", 1, map.NULL_VALUE_BITS))
        assertEquals(1L, map.getBits("one"))
        assertEquals(1L, map.setBits("one", 11, map.NULL_VALUE_BITS))
        assertEquals(11L, map.getBits("one"))
        assertEquals(map.NULL_VALUE_BITS, map.getBits("none"))
    }

    @Test
    fun getOrPutBits() {
        val map = HashMapVObjLong<String, MOLValClass>()
        assertEquals(1L, map.getOrPutBits("one") { 1 })
        assertEquals(1L, map.getOrPutBits("one") { 999 })
    }

    @Test
    fun removeBitsVariants() {
        val map = HashMapVObjLong<String, MOLValClass>()
        map.setBits("one", 1, map.NULL_VALUE_BITS)
        map.setBits("two", 2, map.NULL_VALUE_BITS)
        map.removeBits("one")
        assertEquals(1, map.size)
        assertEquals(true, map.removeBits("two", 2))
        assertEquals(0, map.size)
        assertEquals(false, map.removeBits("none", 999))
    }

    @Test
    fun removeIfBits() {
        val map = HashMapVObjLong<String, MOLValClass>()
        map.setBits("one", 1, map.NULL_VALUE_BITS)
        map.setBits("two", 2, map.NULL_VALUE_BITS)
        map.removeIfBits { _, v -> v >= 2 }
        assertEquals(1, map.size)
        assertEquals(1L, map.getBits("one"))
    }

    @Test
    fun anyBits() {
        val map = simpleMap()
        assertEquals("two", map.anyBits { k, _ -> k == "two" })
        assertEquals(null, map.anyBits { k, _ -> k == "none" })
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
        val map = HashMapVObjLong<String, MOLValClass>()
        assertEquals(true, map.isEmpty)
        assertEquals(false, map.isNotEmpty())
        map.setBits("one", 1, map.NULL_VALUE_BITS)
        assertEquals(false, map.isEmpty)
        assertEquals(true, map.isNotEmpty())
    }

    @Test
    fun typedGetVariants() = with (MOLValClass) {
        val map = simpleMap()
        assertEquals(MOLValClass(2), map["two"])
        assertEquals(MOLValClass(2), map.getOr("two") { MOLValClass(-1) })
        assertEquals(MOLValClass(-1), map.getOr("none") { MOLValClass(-1) })
        assertEquals(MOLValClass(2), map.getOrNull("two"))
        assertEquals(null, map.getOrNull("none"))
    }

    @Test
    fun anyVariants() = with (MOLValClass) {
        val map = simpleMap()
        assertEquals("two", map.any { _, v -> v.value == 2L })
        assertEquals("two", map.anyOr({ _, v -> v.value == 2L }) { "none" })
        assertEquals("none", map.anyOr({ _, v -> v.value == 999L }) { "none" })
    }

    @Test
    fun anyIndexedVariants() = with (MOLValClass) {
        val map = simpleMap()
        val visitedIndices = mutableSetOf<Int>()
        map.forEachIndexed { i, _, _ -> visitedIndices.add(i) }
        assertEquals(setOf(0, 1, 2), visitedIndices)
        assertEquals("two", map.anyIndexed { _, k, v -> v.value == 2L })
        assertEquals("two", map.anyIndexedOr({ _, k, v -> v.value == 2L }) { "none" })
        assertEquals("none", map.anyIndexedOr({ _, _, v -> v.value == 999L }) { "none" })
    }

    @Test
    fun forEachVariants() = with (MOLValClass) {
        val map = simpleMap()
        var sum = 0L
        map.forEach { _, v -> sum += v.value }
        assertEquals(6L, sum)

        val pairValues = mutableListOf<Long>()
        map.forEachPair { p -> pairValues.add(p.second.value) }
        assertEquals(setOf(1L, 2L, 3L), pairValues.toSet())

        val bitsValues = mutableListOf<Long>()
        map.forEachBits { _, v -> bitsValues.add(v) }
        assertEquals(setOf(1L, 2L, 3L), bitsValues.toSet())
    }

    @Test
    fun containsKeyAndValue() = with (MOLValClass) {
        val map = simpleMap()
        assertEquals(true, map.containsKey("two"))
        assertEquals(false, map.containsKey("none"))
        assertEquals(true, map.containsValue(MOLValClass(2)))
        assertEquals(false, map.containsValue(MOLValClass(999)))
    }

    @Test
    fun toStringVSingleEntry() = with (MOLValClass) {
        val map = HashMapVObjLong<String, MOLValClass>().also { it["one"] = MOLValClass(1) }
        assertEquals("{(one:1)}", map.toStringV())
    }

    @Test
    fun joinToStringLimit() = with (MOLValClass) {
        val map = simpleMap()
        val full = map.joinToString()
        assertEquals(false, full.contains("..."))
        assertEquals(3, full.split(", ").size)
        val limited = map.joinToString(limit = 1)
        assertEquals(1, limited.split(", ").size)
    }

    @Test
    fun setOperatorAndWithDefault() = with (MOLValClass) {
        val map = HashMapVObjLong<String, MOLValClass>()
        assertEquals(false, map.set("one", MOLValClass(1)))
        assertEquals(true, map.set("one", MOLValClass(11)))
        assertEquals(MOLValClass(11), map["one"])

        assertEquals(MOLValClass(-1), map.set("none", MOLValClass(50), MOLValClass(-1)))
        assertEquals(MOLValClass(50), map.set("none", MOLValClass(60), MOLValClass(-1)))
    }

    @Test
    fun getOrPut() = with (MOLValClass) {
        val map = HashMapVObjLong<String, MOLValClass>()
        assertEquals(MOLValClass(1), map.getOrPut("one") { MOLValClass(1) })
        assertEquals(MOLValClass(1), map.getOrPut("one") { MOLValClass(999) })
    }

    @Test
    fun putAllAndPlusAssign() = with (MOLValClass) {
        val map = HashMapVObjLong<String, MOLValClass>()
        map.putAll(simpleMap())
        assertEquals(3, map.size)

        val map2 = HashMapVObjLong<String, MOLValClass>()
        map2 plusAssign simpleMap()
        assertEquals(3, map2.size)
    }

    @Test
    fun putAllGeneric() = with (MOLValClass) {
        val map = HashMapVObjLong<String, MOLValClass>()
        map.putAllGeneric(listOf("one" to 1L, "two" to 2L)) { (k, v) -> k to MOLValClass(v) }
        assertEquals(2, map.size)
        assertEquals(MOLValClass(2), map["two"])
    }

    @Test
    fun removeVariants() = with (MOLValClass) {
        val map = simpleMap()
        map.remove("one")
        assertEquals(2, map.size)
        assertEquals(true, map.remove("two", MOLValClass(2)))
        assertEquals(1, map.size)
        map.removeIf { _, v -> v.value >= 3 }
        assertEquals(0, map.size)
    }

    @Test
    fun minusAssign() = with (MOLValClass) {
        val map = simpleMap()
        map minusAssign "one"
        assertEquals(2, map.size)
    }

    @Test
    fun asMapGenericNonIterating() = with (MOLValClass) {
        val map = simpleMap()
        val generic = map.asMapGeneric()
        assertEquals(3, generic.size)
        assertEquals(false, generic.isEmpty())
        assertEquals(true, generic.containsKey("two"))
        assertEquals(true, generic.containsValue(MOLValClass(2)))
        assertEquals(MOLValClass(2), generic["two"])
        assertEquals(null, generic["none"])
        assertThrows(NotImplementedError::class.java, { generic.keys })
    }
}
