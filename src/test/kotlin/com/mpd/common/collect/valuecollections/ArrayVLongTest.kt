package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.ArrayVLong
import com.mpd.common.collect.valuecollections.ValueLongAdapter
import com.mpd.common.collect.valuecollections.any
import com.mpd.common.collect.valuecollections.asListGeneric
import com.mpd.common.collect.valuecollections.component1
import com.mpd.common.collect.valuecollections.component2
import com.mpd.common.collect.valuecollections.contains
import com.mpd.common.collect.valuecollections.contentEquals
import com.mpd.common.collect.valuecollections.contentHashCode
import com.mpd.common.collect.valuecollections.first
import com.mpd.common.collect.valuecollections.indexOf
import com.mpd.common.collect.valuecollections.last
import com.mpd.common.collect.valuecollections.sort
import com.mpd.common.collect.valuecollections.sortDescending
import com.mpd.common.collect.valuecollections.toStringV
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class ArrVLongTestClass(val value: Long): Comparable<ArrVLongTestClass> {
    override operator fun compareTo(other: ArrVLongTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<ArrVLongTestClass> {
        override inline fun fromLong(v: Long) = ArrVLongTestClass(v)
        override inline fun toLong(v: ArrVLongTestClass): Long = v.value
    }
}

class ArrayVLongTest {
    private fun simpleArray(): ArrayVLong<ArrVLongTestClass> = with (ArrVLongTestClass) {
        ArrayVLong<ArrVLongTestClass>(10).also { arr -> for (i in 0..9) arr[i] = ArrVLongTestClass(100L * (i + 1)) }
    }

    @Test
    fun constructors() = with (ArrVLongTestClass) {
        val fromArray = ArrayVLong<ArrVLongTestClass>(longArrayOf(100, 200, 300))
        assertEquals(3, fromArray.size)
        assertEquals(ArrVLongTestClass(200), fromArray[1])

        val bySize = ArrayVLong<ArrVLongTestClass>(5)
        assertEquals(5, bySize.size)
        assertEquals(Long.MIN_VALUE, bySize.NULL_VALUE)
        assertEquals(0L, bySize.bitsAtIndex(0))

        val byInit = ArrayVLong<ArrVLongTestClass>(5, Long.MIN_VALUE) { i -> i * 10L }
        assertEquals(0L, byInit.bitsAtIndex(0))
        assertEquals(40L, byInit.bitsAtIndex(4))

        val src = simpleArray()
        val copy = ArrayVLong<ArrVLongTestClass>(src)
        assertEquals(true, copy.contentEquals(src))
        assertEquals(src.NULL_VALUE, copy.NULL_VALUE)
    }

    @Test
    fun customNullValue() {
        val array = ArrayVLong<ArrVLongTestClass>(3, -1)
        assertEquals(-1L, array.NULL_VALUE)
        assertEquals(0L, array.bitsAtIndex(0))
    }

    @Test
    fun getSetOperators() = with (ArrVLongTestClass) {
        val array = simpleArray()
        assertEquals(ArrVLongTestClass(300), array[2])
        array[2] = ArrVLongTestClass(9999)
        assertEquals(ArrVLongTestClass(9999), array[2])
    }

    @Test
    fun bitsAtIndexAndSetBits() {
        val array = simpleArray()
        assertEquals(300L, array.bitsAtIndex(2))
        array.setBits(2, 9999)
        assertEquals(9999L, array.bitsAtIndex(2))
    }

    @Test
    fun indexOfBits() {
        val array = simpleArray()
        assertEquals(2, array.indexOfBits(300))
        assertEquals(-1, array.indexOfBits(12345))
    }

    @Test
    fun anyBitsAndContainsBits() {
        val array = simpleArray()
        assertEquals(300L, array.anyBits { it == 300L })
        assertEquals(true, array.containsBits(300))
        assertEquals(false, array.containsBits(12345))
    }

    @Test
    fun typedAnyContains() = with (ArrVLongTestClass) {
        val array = simpleArray()
        assertEquals(true, array.any { it.value == 300L })
        assertEquals(true, array.contains(ArrVLongTestClass(300)))
        assertEquals(false, array.contains(ArrVLongTestClass(12345)))
    }

    @Test
    fun asIterableVariants() = with (ArrVLongTestClass) {
        val array = simpleArray()
        val values = array.asIterable().toList()
        assertEquals((1..10).map { ArrVLongTestClass(100L * it) }, values)
        val modifiableValues = array.asModifiableIterable().toList()
        assertEquals(values, modifiableValues)
    }

    @Test
    fun equalsAndHashCode() = with (ArrVLongTestClass) {
        val a = simpleArray()
        val b = simpleArray()
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())
        b[0] = ArrVLongTestClass(-1)
        assertEquals(false, a == b)
    }

    @Test
    fun toStringV() = with (ArrVLongTestClass) {
        val array = ArrayVLong<ArrVLongTestClass>(longArrayOf(100, 200, 300))
        assertEquals("{100, 200, 300}", array.toStringV())
    }

    @Test
    fun indexedExtensions() = with (ArrVLongTestClass) {
        val array = simpleArray()
        assertEquals(ArrVLongTestClass(100), array.component1())
        assertEquals(ArrVLongTestClass(200), array.component2())
        assertEquals(ArrVLongTestClass(100), array.first())
        assertEquals(ArrVLongTestClass(1000), array.last())
        assertEquals(2, array.indexOf(ArrVLongTestClass(300)))
    }

    @Test
    fun sortVariants() = with (ArrVLongTestClass) {
        val array = ArrayVLong<ArrVLongTestClass>(longArrayOf(300, 100, 200))
        array.sort()
        assertEquals(ArrVLongTestClass(100), array[0])
        assertEquals(ArrVLongTestClass(200), array[1])
        assertEquals(ArrVLongTestClass(300), array[2])
        array.sortDescending()
        assertEquals(ArrVLongTestClass(300), array[0])
        assertEquals(ArrVLongTestClass(100), array[2])
    }

    @Test
    fun listIteratorVariants() = with (ArrVLongTestClass) {
        val array = simpleArray()
        val list = array.asListGeneric()
        val it = list.listIterator()
        assertEquals(ArrVLongTestClass(100), it.next())
        assertEquals(ArrVLongTestClass(200), it.next())
        it.set(ArrVLongTestClass(-1))
        assertEquals(ArrVLongTestClass(-1), array[1])
        assertEquals(true, it.hasPrevious())
        assertEquals(ArrVLongTestClass(-1), it.previous())
    }

    @Test
    fun contentEqualsAndHashCode() = with (ArrVLongTestClass) {
        val a = simpleArray()
        val b = simpleArray()
        assertEquals(true, a.contentEquals(b))
        assertEquals(a.contentHashCode(), b.contentHashCode())
    }
}
