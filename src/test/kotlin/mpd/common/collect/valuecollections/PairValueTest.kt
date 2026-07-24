package mpd.common.collect.valuecollections

import mpd.com.common.collect.valuecollections.*
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class PVIntTestClass(val value: Int): Comparable<PVIntTestClass> {
    override operator fun compareTo(other: PVIntTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<PVIntTestClass> {
        override inline fun fromInt(v: Int) = PVIntTestClass(v)
        override inline fun toInt(v: PVIntTestClass): Int = v.value
    }
}

@JvmInline
value class PVLongTestClass(val value: Long): Comparable<PVLongTestClass> {
    override operator fun compareTo(other: PVLongTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<PVLongTestClass> {
        override inline fun fromLong(v: Long) = PVLongTestClass(v)
        override inline fun toLong(v: PVLongTestClass): Long = v.value
    }
}

class PairValueTest {
    @Test
    fun pairVIntInt() = with (PVIntTestClass) {
        val pair = PairVIntInt.of(PVIntTestClass(5), PVIntTestClass(10))
        assertEquals(PVIntTestClass(5), pair.first)
        assertEquals(PVIntTestClass(10), pair.second)
        assertEquals(PVIntTestClass(5) to PVIntTestClass(10), pair.asPairGeneric())
        val direct = PairVIntInt<PVIntTestClass, PVIntTestClass>(5, 10)
        assertEquals(pair, direct)
    }

    @Test
    fun pairVIntIntVLongAdapter() = with (PVIntTestClass) {
        val adapter = PairVIntInt.VLongAdapter<PVIntTestClass, PVIntTestClass>()
        val pair = PairVIntInt.of(PVIntTestClass(5), PVIntTestClass(10))
        val bits = adapter.toLong(pair)
        assertEquals(pair, adapter.fromLong(bits))
        with (adapter) {
            val list = vLongListOf(pair, PairVIntInt.of(PVIntTestClass(1), PVIntTestClass(2)))
            assertEquals(2, list.size)
        }
    }

    @Test
    fun pairVIntLong() = with (PVIntTestClass) {
        with (PVLongTestClass) {
            val pair = PairVIntLong.of(PVIntTestClass(5), PVLongTestClass(100))
            assertEquals(PVIntTestClass(5), pair.first)
            assertEquals(PVLongTestClass(100), pair.second)
            pair.first = PVIntTestClass(6)
            pair.second = PVLongTestClass(200)
            assertEquals(PVIntTestClass(6), pair.first)
            assertEquals(PVLongTestClass(200), pair.second)
            assertEquals(PVIntTestClass(6) to PVLongTestClass(200), pair.asPairGeneric())
        }
    }

    @Test
    fun pairVIntObj() = with (PVIntTestClass) {
        val pair = PairVIntObj.of(PVIntTestClass(5), "hello")
        assertEquals(PVIntTestClass(5), pair.first)
        assertEquals("hello", pair.second)
        pair.first = PVIntTestClass(6)
        pair.second = "world"
        assertEquals(PVIntTestClass(6), pair.first)
        assertEquals("world", pair.second)
        assertEquals(PVIntTestClass(6) to "world", pair.asPairGeneric())
    }

    @Test
    fun pairVObjInt() = with (PVIntTestClass) {
        val pair = PairVObjInt.of("hello", PVIntTestClass(5))
        assertEquals("hello", pair.first)
        assertEquals(PVIntTestClass(5), pair.second)
        pair.first = "world"
        pair.second = PVIntTestClass(6)
        assertEquals("world", pair.first)
        assertEquals(PVIntTestClass(6), pair.second)
        assertEquals("world" to PVIntTestClass(6), pair.asPairGeneric())
    }

    @Test
    fun pairVLongInt() = with (PVLongTestClass) {
        with (PVIntTestClass) {
            val pair = PairVLongInt.of(PVLongTestClass(100), PVIntTestClass(5))
            assertEquals(PVLongTestClass(100), pair.first)
            assertEquals(PVIntTestClass(5), pair.second)
            pair.first = PVLongTestClass(200)
            pair.second = PVIntTestClass(6)
            assertEquals(PVLongTestClass(200), pair.first)
            assertEquals(PVIntTestClass(6), pair.second)
            assertEquals(PVLongTestClass(200) to PVIntTestClass(6), pair.asPairGeneric())
        }
    }

    @Test
    fun pairVLongLong() = with (PVLongTestClass) {
        val pair = PairVLongLong.of(PVLongTestClass(100), PVLongTestClass(200))
        assertEquals(PVLongTestClass(100), pair.first)
        assertEquals(PVLongTestClass(200), pair.second)
        pair.first = PVLongTestClass(300)
        pair.second = PVLongTestClass(400)
        assertEquals(PVLongTestClass(300), pair.first)
        assertEquals(PVLongTestClass(400), pair.second)
        assertEquals(PVLongTestClass(300) to PVLongTestClass(400), pair.asPairGeneric())
    }

    @Test
    fun pairVLongObj() = with (PVLongTestClass) {
        val pair = PairVLongObj.of(PVLongTestClass(100), "hello")
        assertEquals(PVLongTestClass(100), pair.first)
        assertEquals("hello", pair.second)
        pair.first = PVLongTestClass(200)
        pair.second = "world"
        assertEquals(PVLongTestClass(200), pair.first)
        assertEquals("world", pair.second)
        assertEquals(PVLongTestClass(200) to "world", pair.asPairGeneric())
    }

    @Test
    fun pairVObjLong() = with (PVLongTestClass) {
        val pair = PairVObjLong.of("hello", PVLongTestClass(100))
        assertEquals("hello", pair.first)
        assertEquals(PVLongTestClass(100), pair.second)
        pair.first = "world"
        pair.second = PVLongTestClass(200)
        assertEquals("world", pair.first)
        assertEquals(PVLongTestClass(200), pair.second)
        assertEquals("world" to PVLongTestClass(200), pair.asPairGeneric())
    }

    @Test
    fun indexedVInt() = with (PVIntTestClass) {
        val iv = IndexedVInt.of(3, PVIntTestClass(500))
        assertEquals(3, iv.index)
        assertEquals(PVIntTestClass(500), iv.value)
        val byBits = IndexedVInt.ofBits<PVIntTestClass>(3, 500)
        assertEquals(iv, byBits)
        val direct = IndexedVInt<PVIntTestClass>(3, 500)
        assertEquals(iv, direct)
    }

    @Test
    fun indexedVIntVLongAdapter() = with (PVIntTestClass) {
        val adapter = IndexedVInt.VLongAdapter<PVIntTestClass>()
        val iv = IndexedVInt.of(3, PVIntTestClass(500))
        val bits = adapter.toLong(iv)
        assertEquals(iv, adapter.fromLong(bits))
        with (adapter) {
            val list = vLongListOf(iv, IndexedVInt.of(1, PVIntTestClass(10)))
            assertEquals(2, list.size)
        }
    }

    @Test
    fun indexedVLong() = with (PVLongTestClass) {
        val iv = IndexedVLong.of(3, PVLongTestClass(500))
        assertEquals(3, iv.index)
        assertEquals(PVLongTestClass(500), iv.second)
        val byBits = IndexedVLong.ofBits<PVLongTestClass>(3, 500)
        assertEquals(iv, byBits)
        iv.index = 4
        iv.second = PVLongTestClass(600)
        assertEquals(4, iv.index)
        assertEquals(PVLongTestClass(600), iv.second)
    }
}
