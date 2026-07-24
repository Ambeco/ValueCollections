package mpd.common.collect.valuecollections

import mpd.com.common.collect.valuecollections.*
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class MOIValClass(val value: Int): Comparable<MOIValClass> {
    override operator fun compareTo(other: MOIValClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MOIValClass> {
        override inline fun fromInt(v: Int) = MOIValClass(v)
        override inline fun toInt(v: MOIValClass): Int = v.value
    }
}

class MapVObjIntTest {
    private fun simpleMap(): HashMapVObjInt<String, MOIValClass> = with (MOIValClass) {
        HashMapVObjInt<String, MOIValClass>().also {
            it["one"] = MOIValClass(1)
            it["two"] = MOIValClass(2)
            it["three"] = MOIValClass(3)
        }
    }

    @Test
    fun constructors() {
        val primary = HashMapVObjInt<String, MOIValClass>()
        assertEquals(0, primary.size)
        val bySize = HashMapVObjInt<String, MOIValClass>(10)
        assertEquals(0, bySize.size)
    }

    @Test
    fun customNullValue() {
        val map = HashMapVObjInt<String, MOIValClass>(10, -1)
        assertEquals(-1, map.NULL_VALUE_BITS)
    }

    @Test
    fun getSetBits() {
        val map = HashMapVObjInt<String, MOIValClass>()
        assertEquals(map.NULL_VALUE_BITS, map.setBits("one", 1, map.NULL_VALUE_BITS))
        assertEquals(1, map.getBits("one"))
        assertEquals(1, map.setBits("one", 11, map.NULL_VALUE_BITS))
        assertEquals(11, map.getBits("one"))
        assertEquals(map.NULL_VALUE_BITS, map.getBits("none"))
    }

    @Test
    fun getOrPutBits() {
        val map = HashMapVObjInt<String, MOIValClass>()
        assertEquals(1, map.getOrPutBits("one") { 1 })
        assertEquals(1, map.getOrPutBits("one") { 999 })
    }

    @Test
    fun removeBitsVariants() {
        val map = HashMapVObjInt<String, MOIValClass>()
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
        val map = HashMapVObjInt<String, MOIValClass>()
        map.setBits("one", 1, map.NULL_VALUE_BITS)
        map.setBits("two", 2, map.NULL_VALUE_BITS)
        map.removeIfBits { _, v -> v >= 2 }
        assertEquals(1, map.size)
        assertEquals(1, map.getBits("one"))
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
        val map = HashMapVObjInt<String, MOIValClass>()
        assertEquals(true, map.isEmpty)
        assertEquals(false, map.isNotEmpty())
        map.setBits("one", 1, map.NULL_VALUE_BITS)
        assertEquals(false, map.isEmpty)
        assertEquals(true, map.isNotEmpty())
    }

    @Test
    fun typedGetVariants() = with (MOIValClass) {
        val map = simpleMap()
        assertEquals(MOIValClass(2), map["two"])
        assertEquals(MOIValClass(2), map.getOr("two") { MOIValClass(-1) })
        assertEquals(MOIValClass(-1), map.getOr("none") { MOIValClass(-1) })
        assertEquals(MOIValClass(2), map.getOrNull("two"))
        assertEquals(null, map.getOrNull("none"))
    }

    @Test
    fun anyVariants() = with (MOIValClass) {
        val map = simpleMap()
        assertEquals("two", map.any { _, v -> v.value == 2 })
        assertEquals("two", map.anyOr({ _, v -> v.value == 2 }) { "none" })
        assertEquals("none", map.anyOr({ _, v -> v.value == 999 }) { "none" })
    }

    @Test
    fun anyIndexedVariants() = with (MOIValClass) {
        val map = simpleMap()
        val visitedIndices = mutableSetOf<Int>()
        map.forEachIndexed { i, _, _ -> visitedIndices.add(i) }
        assertEquals(setOf(0, 1, 2), visitedIndices)
        assertEquals("two", map.anyIndexed { _, k, v -> v.value == 2 })
        assertEquals("two", map.anyIndexedOr({ _, k, v -> v.value == 2 }) { "none" })
        assertEquals("none", map.anyIndexedOr({ _, _, v -> v.value == 999 }) { "none" })
    }

    @Test
    fun forEachVariants() = with (MOIValClass) {
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
    fun containsKeyAndValue() = with (MOIValClass) {
        val map = simpleMap()
        assertEquals(true, map.containsKey("two"))
        assertEquals(false, map.containsKey("none"))
        assertEquals(true, map.containsValue(MOIValClass(2)))
        assertEquals(false, map.containsValue(MOIValClass(999)))
    }

    @Test
    fun toStringVSingleEntry() = with (MOIValClass) {
        val map = HashMapVObjInt<String, MOIValClass>().also { it["one"] = MOIValClass(1) }
        assertEquals("{(one:1)}", map.toStringV())
    }

    @Test
    fun joinToStringLimit() = with (MOIValClass) {
        val map = simpleMap()
        val full = map.joinToString()
        assertEquals(false, full.contains("..."))
        assertEquals(3, full.split(", ").size)
        val limited = map.joinToString(limit = 1)
        assertEquals(1, limited.split(", ").size)
    }

    @Test
    fun setOperatorAndWithDefault() = with (MOIValClass) {
        val map = HashMapVObjInt<String, MOIValClass>()
        assertEquals(false, map.set("one", MOIValClass(1)))
        assertEquals(true, map.set("one", MOIValClass(11)))
        assertEquals(MOIValClass(11), map["one"])

        assertEquals(MOIValClass(-1), map.set("none", MOIValClass(50), MOIValClass(-1)))
        assertEquals(MOIValClass(50), map.set("none", MOIValClass(60), MOIValClass(-1)))
    }

    @Test
    fun getOrPut() = with (MOIValClass) {
        val map = HashMapVObjInt<String, MOIValClass>()
        assertEquals(MOIValClass(1), map.getOrPut("one") { MOIValClass(1) })
        assertEquals(MOIValClass(1), map.getOrPut("one") { MOIValClass(999) })
    }

    @Test
    fun putAllAndPlusAssign() = with (MOIValClass) {
        val map = HashMapVObjInt<String, MOIValClass>()
        map.putAll(simpleMap())
        assertEquals(3, map.size)

        val map2 = HashMapVObjInt<String, MOIValClass>()
        map2 += simpleMap()
        assertEquals(3, map2.size)
    }

    @Test
    fun putAllGeneric() = with (MOIValClass) {
        val map = HashMapVObjInt<String, MOIValClass>()
        map.putAllGeneric(listOf("one" to 1, "two" to 2)) { (k, v) -> k to MOIValClass(v) }
        assertEquals(2, map.size)
        assertEquals(MOIValClass(2), map["two"])
    }

    @Test
    fun removeVariants() = with (MOIValClass) {
        val map = simpleMap()
        map.remove("one")
        assertEquals(2, map.size)
        assertEquals(true, map.remove("two", MOIValClass(2)))
        assertEquals(1, map.size)
        map.removeIf { _, v -> v.value >= 3 }
        assertEquals(0, map.size)
    }

    @Test
    fun minusAssign() = with (MOIValClass) {
        val map = simpleMap()
        map -= "one"
        assertEquals(2, map.size)
    }

    @Test
    fun asMapGenericNonIterating() = with (MOIValClass) {
        val map = simpleMap()
        val generic = map.asMapGeneric()
        assertEquals(3, generic.size)
        assertEquals(false, generic.isEmpty())
        assertEquals(true, generic.containsKey("two"))
        assertEquals(true, generic.containsValue(MOIValClass(2)))
        assertEquals(MOIValClass(2), generic["two"])
        assertEquals(null, generic["none"])
        assertThrows(NotImplementedError::class.java, { generic.keys })
    }
}
