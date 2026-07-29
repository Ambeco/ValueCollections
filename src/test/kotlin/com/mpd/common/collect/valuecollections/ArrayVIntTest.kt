package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.ArrayVInt
import com.mpd.common.collect.valuecollections.ValueIntAdapter
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
value class ArrVIntTestClass(val value: Int): Comparable<ArrVIntTestClass> {
    override operator fun compareTo(other: ArrVIntTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<ArrVIntTestClass> {
        override inline fun fromInt(v: Int) = ArrVIntTestClass(v)
        override inline fun toInt(v: ArrVIntTestClass): Int = v.value
    }
}

class ArrayVIntTest {
    private fun simpleArray(): ArrayVInt<ArrVIntTestClass> = with (ArrVIntTestClass) {
        ArrayVInt<ArrVIntTestClass>(10).also { arr -> for (i in 0..9) arr[i] = ArrVIntTestClass(100 * (i + 1)) }
    }

    @Test
    fun constructors() = with (ArrVIntTestClass) {
        val fromArray = ArrayVInt<ArrVIntTestClass>(intArrayOf(100, 200, 300))
        assertEquals(3, fromArray.size)
        assertEquals(ArrVIntTestClass(200), fromArray[1])

        val bySize = ArrayVInt<ArrVIntTestClass>(5)
        assertEquals(5, bySize.size)
        assertEquals(Int.MIN_VALUE, bySize.NULL_VALUE)
        assertEquals(0, bySize.bitsAtIndex(0))

        val byInit = ArrayVInt<ArrVIntTestClass>(5, Int.MIN_VALUE) { i -> i * 10 }
        assertEquals(0, byInit.bitsAtIndex(0))
        assertEquals(40, byInit.bitsAtIndex(4))

        val src = simpleArray()
        val copy = ArrayVInt<ArrVIntTestClass>(src)
        assertEquals(true, copy.contentEquals(src))
        assertEquals(src.NULL_VALUE, copy.NULL_VALUE)
    }

    @Test
    fun customNullValue() {
        val array = ArrayVInt<ArrVIntTestClass>(3, -1)
        assertEquals(-1, array.NULL_VALUE)
        assertEquals(0, array.bitsAtIndex(0))
    }

    @Test
    fun getSetOperators() = with (ArrVIntTestClass) {
        val array = simpleArray()
        assertEquals(ArrVIntTestClass(300), array[2])
        array[2] = ArrVIntTestClass(9999)
        assertEquals(ArrVIntTestClass(9999), array[2])
    }

    @Test
    fun bitsAtIndexAndSetBits() {
        val array = simpleArray()
        assertEquals(300, array.bitsAtIndex(2))
        array.setBits(2, 9999)
        assertEquals(9999, array.bitsAtIndex(2))
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
        assertEquals(300, array.anyBits { it == 300 })
        assertEquals(true, array.containsBits(300))
        assertEquals(false, array.containsBits(12345))
    }

    @Test
    fun typedAnyContains() = with (ArrVIntTestClass) {
        val array = simpleArray()
        assertEquals(true, array.any { it.value == 300 })
        assertEquals(true, array.contains(ArrVIntTestClass(300)))
        assertEquals(false, array.contains(ArrVIntTestClass(12345)))
    }

    @Test
    fun asIterableVariants() = with (ArrVIntTestClass) {
        val array = simpleArray()
        val values = array.asIterable().toList()
        assertEquals((1..10).map { ArrVIntTestClass(100 * it) }, values)
        val modifiableValues = array.asModifiableIterable().toList()
        assertEquals(values, modifiableValues)
    }

    @Test
    fun equalsAndHashCode() = with (ArrVIntTestClass) {
        val a = simpleArray()
        val b = simpleArray()
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())
        b[0] = ArrVIntTestClass(-1)
        assertEquals(false, a == b)
    }

    @Test
    fun toStringV() = with (ArrVIntTestClass) {
        val array = ArrayVInt<ArrVIntTestClass>(intArrayOf(100, 200, 300))
        assertEquals("{100, 200, 300}", array.toStringV())
    }

    @Test
    fun indexedExtensions() = with (ArrVIntTestClass) {
        val array = simpleArray()
        assertEquals(ArrVIntTestClass(100), array.component1())
        assertEquals(ArrVIntTestClass(200), array.component2())
        assertEquals(ArrVIntTestClass(100), array.first())
        assertEquals(ArrVIntTestClass(1000), array.last())
        assertEquals(2, array.indexOf(ArrVIntTestClass(300)))
    }

    @Test
    fun sortVariants() = with (ArrVIntTestClass) {
        val array = ArrayVInt<ArrVIntTestClass>(intArrayOf(300, 100, 200))
        array.sort()
        assertEquals(ArrVIntTestClass(100), array[0])
        assertEquals(ArrVIntTestClass(200), array[1])
        assertEquals(ArrVIntTestClass(300), array[2])
        array.sortDescending()
        assertEquals(ArrVIntTestClass(300), array[0])
        assertEquals(ArrVIntTestClass(100), array[2])
    }

    @Test
    fun listIteratorVariants() = with (ArrVIntTestClass) {
        val array = simpleArray()
        val list = array.asListGeneric()
        val it = list.listIterator()
        assertEquals(ArrVIntTestClass(100), it.next())
        assertEquals(ArrVIntTestClass(200), it.next())
        it.set(ArrVIntTestClass(-1))
        assertEquals(ArrVIntTestClass(-1), array[1])
        assertEquals(true, it.hasPrevious())
        assertEquals(ArrVIntTestClass(-1), it.previous())
    }

    @Test
    fun contentEqualsAndHashCode() = with (ArrVIntTestClass) {
        val a = simpleArray()
        val b = simpleArray()
        assertEquals(true, a.contentEquals(b))
        assertEquals(a.contentHashCode(), b.contentHashCode())
    }
}
