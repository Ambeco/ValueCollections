package mpd.common.collect.valuecollections

import mpd.com.common.collect.valuecollections.*
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class MIOKeyClass(val value: Int): Comparable<MIOKeyClass> {
    override operator fun compareTo(other: MIOKeyClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MIOKeyClass> {
        override inline fun fromInt(v: Int) = MIOKeyClass(v)
        override inline fun toInt(v: MIOKeyClass): Int = v.value
    }
}

class MapVIntObjTest {
    private fun simpleMap(): HashMapVIntObj<MIOKeyClass, String> = with (MIOKeyClass) {
        HashMapVIntObj<MIOKeyClass, String>().also {
            it[MIOKeyClass(1)] = "one"
            it[MIOKeyClass(2)] = "two"
            it[MIOKeyClass(3)] = "three"
        }
    }

    @Test
    fun constructors() {
        val primary = HashMapVIntObj<MIOKeyClass, String>()
        assertEquals(0, primary.size)
        val bySize = HashMapVIntObj<MIOKeyClass, String>(10)
        assertEquals(0, bySize.size)
    }

    @Test
    fun customNullKey() {
        val map = HashMapVIntObj<MIOKeyClass, String>(10, -1)
        assertEquals(-1, map.NULL_KEY_BITS)
    }

    @Test
    fun getSetBits() {
        val map = HashMapVIntObj<MIOKeyClass, String>()
        assertEquals(null, map.setBits(1, "one", null))
        assertEquals("one", map.getBits(1))
        assertEquals("one", map.setBits(1, "uno", null))
        assertEquals("uno", map.getBits(1))
        assertEquals(null, map.getBits(99))
    }

    @Test
    fun getOrPutBits() {
        val map = HashMapVIntObj<MIOKeyClass, String>()
        assertEquals("one", map.getOrPutBits(1) { "one" })
        assertEquals("one", map.getOrPutBits(1) { "nope" })
    }

    @Test
    fun removeBitsVariants() {
        val map = HashMapVIntObj<MIOKeyClass, String>()
        map.setBits(1, "one", null)
        map.setBits(2, "two", null)
        assertEquals("one", map.removeBits(1))
        assertEquals(1, map.size)
        assertEquals(true, map.removeBits(2, "two"))
        assertEquals(0, map.size)
        assertEquals(false, map.removeBits(99, "nope"))
    }

    @Test
    fun removeIfBits() {
        val map = HashMapVIntObj<MIOKeyClass, String>()
        map.setBits(1, "one", null)
        map.setBits(2, "two", null)
        map.removeIfBits { _, v -> v == "two" }
        assertEquals(1, map.size)
        assertEquals("one", map.getBits(1))
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
        val map = HashMapVIntObj<MIOKeyClass, String>()
        assertEquals(true, map.isEmpty)
        assertEquals(false, map.isNotEmpty())
        map.setBits(1, "one", null)
        assertEquals(false, map.isEmpty)
        assertEquals(true, map.isNotEmpty())
    }

    @Test
    fun typedGetVariants() = with (MIOKeyClass) {
        val map = simpleMap()
        assertEquals("two", map[MIOKeyClass(2)])
        assertEquals("two", map.getOr(MIOKeyClass(2)) { "none" })
        assertEquals("none", map.getOr(MIOKeyClass(99)) { "none" })
        assertEquals("two", map.getOrNull(MIOKeyClass(2)))
        assertEquals(null, map.getOrNull(MIOKeyClass(99)))
    }

    @Test
    fun anyVariants() = with (MIOKeyClass) {
        val map = simpleMap()
        assertEquals(MIOKeyClass(2), map.any { _, v -> v == "two" })
        assertEquals(MIOKeyClass(2), map.anyOr({ _, v -> v == "two" }) { MIOKeyClass(-1) })
        assertEquals(MIOKeyClass(-1), map.anyOr({ _, v -> v == "none" }) { MIOKeyClass(-1) })
        assertEquals(MIOKeyClass(2), map.anyOrNull { _, v -> v == "two" })
        assertEquals(null, map.anyOrNull { _, v -> v == "none" })
    }

    @Test
    fun anyIndexedVariants() = with (MIOKeyClass) {
        val map = simpleMap()
        val visitedIndices = mutableSetOf<Int>()
        map.forEachIndexed { i, _, _ -> visitedIndices.add(i) }
        assertEquals(setOf(0, 1, 2), visitedIndices)
        assertEquals(MIOKeyClass(2), map.anyIndexed { _, k, v -> v == "two" })
        assertEquals(MIOKeyClass(2), map.anyIndexedOr({ _, k, v -> v == "two" }) { MIOKeyClass(-1) })
        assertEquals(MIOKeyClass(-1), map.anyIndexedOr({ _, _, v -> v == "none" }) { MIOKeyClass(-1) })
        assertEquals(null, map.anyIndexedOrNull { _, _, v -> v == "none" })
    }

    @Test
    fun forEachVariants() = with (MIOKeyClass) {
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
    fun containsKeyAndValue() = with (MIOKeyClass) {
        val map = simpleMap()
        assertEquals(true, map.containsKey(MIOKeyClass(2)))
        assertEquals(false, map.containsKey(MIOKeyClass(99)))
        assertEquals(true, map.containsValue("two"))
        assertEquals(false, map.containsValue("none"))
    }

    @Test
    fun toStringVSingleEntry() = with (MIOKeyClass) {
        val map = HashMapVIntObj<MIOKeyClass, String>().also { it[MIOKeyClass(1)] = "one" }
        assertEquals("{(1:one)}", map.toStringV())
    }

    @Test
    fun joinToStringLimit() = with (MIOKeyClass) {
        val map = simpleMap()
        val full = map.joinToString()
        assertEquals(false, full.contains("..."))
        assertEquals(3, full.split(", ").size)
        val limited = map.joinToString(limit = 1)
        assertEquals(1, limited.split(", ").size)
    }

    @Test
    fun setOperatorAndWithDefault() = with (MIOKeyClass) {
        val map = HashMapVIntObj<MIOKeyClass, String>()
        assertEquals(false, map.set(MIOKeyClass(1), "one"))
        assertEquals(true, map.set(MIOKeyClass(1), "uno"))
        assertEquals("uno", map[MIOKeyClass(1)])

        assertEquals("default", map.set(MIOKeyClass(99), "fifty", "default"))
        assertEquals("fifty", map.set(MIOKeyClass(99), "sixty", "default"))
    }

    @Test
    fun getOrPut() = with (MIOKeyClass) {
        val map = HashMapVIntObj<MIOKeyClass, String>()
        assertEquals("one", map.getOrPut(MIOKeyClass(1)) { "one" })
        assertEquals("one", map.getOrPut(MIOKeyClass(1)) { "nope" })
    }

    @Test
    fun putAllAndPlusAssign() = with (MIOKeyClass) {
        val map = HashMapVIntObj<MIOKeyClass, String>()
        map.putAll(simpleMap())
        assertEquals(3, map.size)

        val map2 = HashMapVIntObj<MIOKeyClass, String>()
        map2 += simpleMap()
        assertEquals(3, map2.size)
    }

    @Test
    fun putAllGeneric() = with (MIOKeyClass) {
        val map = HashMapVIntObj<MIOKeyClass, String>()
        map.putAllGeneric(listOf(1 to "one", 2 to "two")) { (k, v) -> MIOKeyClass(k) to v }
        assertEquals(2, map.size)
        assertEquals("two", map[MIOKeyClass(2)])
    }

    @Test
    fun removeVariants() = with (MIOKeyClass) {
        val map = simpleMap()
        map.remove(MIOKeyClass(1))
        assertEquals(2, map.size)
        assertEquals(true, map.remove(MIOKeyClass(2), "two"))
        assertEquals(1, map.size)
        map.removeIf { _, v -> v == "three" }
        assertEquals(0, map.size)
    }

    @Test
    fun minusAssign() = with (MIOKeyClass) {
        val map = simpleMap()
        map -= MIOKeyClass(1)
        assertEquals(2, map.size)
    }

    @Test
    fun asMapGenericNonIterating() = with (MIOKeyClass) {
        val map = simpleMap()
        val generic = map.asMapGeneric()
        assertEquals(3, generic.size)
        assertEquals(false, generic.isEmpty())
        assertEquals(true, generic.containsKey(MIOKeyClass(2)))
        assertEquals(true, generic.containsValue("two"))
        assertEquals("two", generic[MIOKeyClass(2)])
        assertEquals(null, generic[MIOKeyClass(99)])
        assertThrows(NotImplementedError::class.java, { generic.keys })
    }
}
