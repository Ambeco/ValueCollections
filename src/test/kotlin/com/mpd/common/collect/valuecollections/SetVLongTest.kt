package com.mpd.common.collect.valuecollections

import androidx.collection.MutableLongSet
import com.mpd.common.collect.valuecollections.ArraySetVLong
import com.mpd.common.collect.valuecollections.ValueLongAdapter
import com.mpd.common.collect.valuecollections.add
import com.mpd.common.collect.valuecollections.contains
import com.mpd.common.collect.valuecollections.emptySetVLong
import com.mpd.common.collect.valuecollections.mutableSetVLongOf
import com.mpd.common.collect.valuecollections.toSetGeneric
import com.mpd.common.collect.valuecollections.toStringV
import com.mpd.common.collect.valuecollections.vLongSetOf
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class SVLongTestClass(val value: Long): Comparable<SVLongTestClass> {
    override operator fun compareTo(other: SVLongTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<SVLongTestClass> {
        override inline fun fromLong(v: Long) = SVLongTestClass(v)
        override inline fun toLong(v: SVLongTestClass): Long = v.value
    }
}

class SetVLongTest {
    @Test
    fun constructors() = with (SVLongTestClass) {
        val primary = ArraySetVLong<SVLongTestClass>(MutableLongSet().also { it.add(1); it.add(2) })
        assertEquals(2, primary.size)
        assertEquals(true, primary.contains(SVLongTestClass(1)))

        val byCapacity = ArraySetVLong<SVLongTestClass>(5)
        assertEquals(0, byCapacity.size)
    }

    @Test
    fun customNullValue() {
        val set = ArraySetVLong<SVLongTestClass>(5, -1)
        assertEquals(-1L, set.NULL_VALUE)
    }

    @Test
    fun emptySetSingletonIsSharedAndEmpty() {
        assertEquals(0, emptySetVLong<SVLongTestClass>().size)
        assertEquals(0, vLongSetOf<SVLongTestClass>().size)
    }

    @Test
    fun vLongSetOfArities() = with (SVLongTestClass) {
        assertEquals(setOf(SVLongTestClass(1)), vLongSetOf(SVLongTestClass(1)).toSetGeneric())
        assertEquals(setOf(SVLongTestClass(1), SVLongTestClass(2)), vLongSetOf(
            SVLongTestClass(1),
            SVLongTestClass(2)
        ).toSetGeneric())
        assertEquals(setOf(SVLongTestClass(1), SVLongTestClass(2), SVLongTestClass(3)), vLongSetOf(
            SVLongTestClass(1),
            SVLongTestClass(2),
            SVLongTestClass(3)
        ).toSetGeneric())
        assertEquals(setOf(SVLongTestClass(1), SVLongTestClass(2), SVLongTestClass(3), SVLongTestClass(4)), vLongSetOf(
            SVLongTestClass(1),
            SVLongTestClass(2),
            SVLongTestClass(3),
            SVLongTestClass(4)
        ).toSetGeneric())
    }

    @Test
    fun mutableSetVLongOfArities() = with (SVLongTestClass) {
        assertEquals(0, mutableSetVLongOf<SVLongTestClass>().size)
        val one = mutableSetVLongOf(SVLongTestClass(1))
        assertEquals(setOf(SVLongTestClass(1)), one.toSetGeneric())
        assertEquals(setOf(SVLongTestClass(1), SVLongTestClass(2)), mutableSetVLongOf(
            SVLongTestClass(1),
            SVLongTestClass(2)
        ).toSetGeneric())
        assertEquals(setOf(SVLongTestClass(1), SVLongTestClass(2), SVLongTestClass(3)), mutableSetVLongOf(
            SVLongTestClass(1),
            SVLongTestClass(2),
            SVLongTestClass(3)
        ).toSetGeneric())
        assertEquals(setOf(SVLongTestClass(1), SVLongTestClass(2), SVLongTestClass(3), SVLongTestClass(4)), mutableSetVLongOf(
            SVLongTestClass(1),
            SVLongTestClass(2),
            SVLongTestClass(3),
            SVLongTestClass(4)
        ).toSetGeneric())

        // mutableSetVLongOf() results are actually mutable, unlike the shared vLongSetOf() singleton
        one.add(SVLongTestClass(99))
        assertEquals(2, one.size)
    }

    @Test
    fun equalsAndHashCodeIgnoreOrder() = with (SVLongTestClass) {
        val a = vLongSetOf(SVLongTestClass(1), SVLongTestClass(2), SVLongTestClass(3))
        val b = vLongSetOf(SVLongTestClass(3), SVLongTestClass(2), SVLongTestClass(1))
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toStringVSingleElement() = with (SVLongTestClass) {
        assertEquals("{1}", vLongSetOf(SVLongTestClass(1)).toStringV())
    }
}
