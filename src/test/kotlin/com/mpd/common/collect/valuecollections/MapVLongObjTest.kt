package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.HashMapVLongObj
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
value class MLOKeyClass(val value: Long): Comparable<MLOKeyClass> {
    override operator fun compareTo(other: MLOKeyClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<MLOKeyClass> {
        override inline fun fromLong(v: Long) = MLOKeyClass(v)
        override inline fun toLong(v: MLOKeyClass): Long = v.value
    }
}

class MapVLongObjTest {
    private fun simpleMap(): HashMapVLongObj<MLOKeyClass, String> = with (MLOKeyClass) {
        HashMapVLongObj<MLOKeyClass, String>().also {
            it[MLOKeyClass(1)] = "one"
            it[MLOKeyClass(2)] = "two"
            it[MLOKeyClass(3)] = "three"
        }
    }

    @Test
    fun constructors() {
        val primary = HashMapVLongObj<MLOKeyClass, String>()
        assertEquals(0, primary.size)
        val bySize = HashMapVLongObj<MLOKeyClass, String>(10)
        assertEquals(0, bySize.size)
    }

    @Test
    fun customNullKey() {
        val map = HashMapVLongObj<MLOKeyClass, String>(10, -1)
        assertEquals(-1L, map.NULL_KEY_BITS)
    }

    @Test
    fun getSetBits() {
        val map = HashMapVLongObj<MLOKeyClass, String>()
        assertEquals(null, map.setBits(1, "one"))
        assertEquals("one", map.getBits(1))
        assertEquals("one", map.setBits(1, "uno"))
        assertEquals("uno", map.getBits(1))
        assertEquals(null, map.getBits(99))
    }

    @Test
    fun getOrPutBits() {
        val map = HashMapVLongObj<MLOKeyClass, String>()
        assertEquals("one", map.getOrPutBits(1) { "one" })
        assertEquals("one", map.getOrPutBits(1) { "nope" })
    }

    @Test
    fun removeBitsVariants() {
        val map = HashMapVLongObj<MLOKeyClass, String>()
        map.setBits(1, "one")
        map.setBits(2, "two")
        assertEquals("one", map.removeBits(1))
        assertEquals(1, map.size)
        assertEquals(true, map.removeBits(2, "two"))
        assertEquals(0, map.size)
        assertEquals(false, map.removeBits(99, "nope"))
    }

    @Test
    fun removeIfBits() {
        val map = HashMapVLongObj<MLOKeyClass, String>()
        map.setBits(1, "one")
        map.setBits(2, "two")
        map.removeIfBits { _, v -> v == "two" }
        assertEquals(1, map.size)
        assertEquals("one", map.getBits(1))
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
        val map = HashMapVLongObj<MLOKeyClass, String>()
        assertEquals(true, map.isEmpty)
        assertEquals(false, map.isNotEmpty())
        map.setBits(1, "one")
        assertEquals(false, map.isEmpty)
        assertEquals(true, map.isNotEmpty())
    }

    @Test
    fun typedGetVariants() = with (MLOKeyClass) {
        val map = simpleMap()
        assertEquals("two", map[MLOKeyClass(2)])
        assertEquals("two", map.getOr(MLOKeyClass(2)) { "none" })
        assertEquals("none", map.getOr(MLOKeyClass(99)) { "none" })
        assertEquals("two", map.getOrNull(MLOKeyClass(2)))
        assertEquals(null, map.getOrNull(MLOKeyClass(99)))
    }

    @Test
    fun anyVariants() = with (MLOKeyClass) {
        val map = simpleMap()
        assertEquals(MLOKeyClass(2), map.any { _, v -> v == "two" })
        assertEquals(MLOKeyClass(2), map.anyOr({ _, v -> v == "two" }) { MLOKeyClass(-1) })
        assertEquals(MLOKeyClass(-1), map.anyOr({ _, v -> v == "none" }) { MLOKeyClass(-1) })
        assertEquals(MLOKeyClass(2), map.anyOrNull { _, v -> v == "two" })
        assertEquals(null, map.anyOrNull { _, v -> v == "none" })
    }

    @Test
    fun anyIndexedVariants() = with (MLOKeyClass) {
        val map = simpleMap()
        val visitedIndices = mutableSetOf<Int>()
        map.forEachIndexed { i, _, _ -> visitedIndices.add(i) }
        assertEquals(setOf(0, 1, 2), visitedIndices)
        assertEquals(MLOKeyClass(2), map.anyIndexed { _, k, v -> v == "two" })
        assertEquals(MLOKeyClass(2), map.anyIndexedOr({ _, k, v -> v == "two" }) { MLOKeyClass(-1) })
        assertEquals(MLOKeyClass(-1), map.anyIndexedOr({ _, _, v -> v == "none" }) { MLOKeyClass(-1) })
        assertEquals(null, map.anyIndexedOrNull { _, _, v -> v == "none" })
    }

    @Test
    fun forEachVariants() = with (MLOKeyClass) {
        val map = simpleMap()
        val values = mutableListOf<String>()
        map.forEach { _, v -> values.add(v) }
        assertEquals(setOf("one", "two", "three"), values.toSet())

        val pairValues = mutableListOf<String>()
        map.forEachPair { p -> pairValues.add(p.second) }
        assertEquals(setOf("one", "two", "three"), pairValues.toSet())

        val bitsValues = mutableListOf<String>()
        map.forEachBits { _, v -> bitsValues.add(v) }
        assertEquals(setOf("one", "two", "three"), bitsValues.toSet())
    }

    @Test
    fun containsKeyAndValue() = with (MLOKeyClass) {
        val map = simpleMap()
        assertEquals(true, map.containsKey(MLOKeyClass(2)))
        assertEquals(false, map.containsKey(MLOKeyClass(99)))
        assertEquals(true, map.containsValue("two"))
        assertEquals(false, map.containsValue("none"))
    }

    @Test
    fun toStringVSingleEntry() = with (MLOKeyClass) {
        val map = HashMapVLongObj<MLOKeyClass, String>().also { it[MLOKeyClass(1)] = "one" }
        assertEquals("{(1:one)}", map.toStringV())
    }

    @Test
    fun joinToStringLimit() = with (MLOKeyClass) {
        val map = simpleMap()
        val full = map.joinToString()
        assertEquals(false, full.contains("..."))
        assertEquals(3, full.split(", ").size)
        val limited = map.joinToString(limit = 1)
        assertEquals(1, limited.split(", ").size)
    }

    @Test
    fun setOperator() = with (MLOKeyClass) {
        val map = HashMapVLongObj<MLOKeyClass, String>()
        assertEquals(false, map.set(MLOKeyClass(1), "one"))
        assertEquals(true, map.set(MLOKeyClass(1), "uno"))
        assertEquals("uno", map[MLOKeyClass(1)])
        // Note: unlike MutableMapVIntObj, MutableMapVLongObj has no 3-arg set(key, value,
        // defaultReturn) overload - its setBits(k, v) has no defaultReturn parameter to back it.
    }

    @Test
    fun getOrPut() = with (MLOKeyClass) {
        val map = HashMapVLongObj<MLOKeyClass, String>()
        assertEquals("one", map.getOrPut(MLOKeyClass(1)) { "one" })
        assertEquals("one", map.getOrPut(MLOKeyClass(1)) { "nope" })
    }

    @Test
    fun putAllAndPlusAssign() = with (MLOKeyClass) {
        val map = HashMapVLongObj<MLOKeyClass, String>()
        map.putAll(simpleMap())
        assertEquals(3, map.size)

        val map2 = HashMapVLongObj<MLOKeyClass, String>()
        map2 plusAssign simpleMap()
        assertEquals(3, map2.size)
    }

    @Test
    fun putAllGeneric() = with (MLOKeyClass) {
        val map = HashMapVLongObj<MLOKeyClass, String>()
        map.putAllGeneric(listOf(1L to "one", 2L to "two")) { (k, v) -> MLOKeyClass(k) to v }
        assertEquals(2, map.size)
        assertEquals("two", map[MLOKeyClass(2)])
    }

    @Test
    fun removeVariants() = with (MLOKeyClass) {
        val map = simpleMap()
        map.remove(MLOKeyClass(1))
        assertEquals(2, map.size)
        assertEquals(true, map.remove(MLOKeyClass(2), "two"))
        assertEquals(1, map.size)
        map.removeIf { _, v -> v == "three" }
        assertEquals(0, map.size)
    }

    @Test
    fun minusAssign() = with (MLOKeyClass) {
        val map = simpleMap()
        map minusAssign MLOKeyClass(1)
        assertEquals(2, map.size)
    }

    @Test
    fun asMapGenericNonIterating() = with (MLOKeyClass) {
        val map = simpleMap()
        val generic = map.asMapGeneric()
        assertEquals(3, generic.size)
        assertEquals(false, generic.isEmpty())
        assertEquals(true, generic.containsKey(MLOKeyClass(2)))
        assertEquals(true, generic.containsValue("two"))
        assertEquals("two", generic[MLOKeyClass(2)])
        assertEquals(null, generic[MLOKeyClass(99)])
        assertThrows(NotImplementedError::class.java, { generic.keys })
    }
}
