package com.mpd.common.collect.valuecollections

import androidx.collection.MutableLongList
import com.mpd.common.collect.valuecollections.ArrayListVLong
import com.mpd.common.collect.valuecollections.CollectionVLong
import com.mpd.common.collect.valuecollections.ValueLongAdapter
import com.mpd.common.collect.valuecollections.add
import com.mpd.common.collect.valuecollections.contains
import com.mpd.common.collect.valuecollections.emptyVLongList
import com.mpd.common.collect.valuecollections.get
import com.mpd.common.collect.valuecollections.mutableVLongListOf
import com.mpd.common.collect.valuecollections.plusAssign
import com.mpd.common.collect.valuecollections.toListGeneric
import com.mpd.common.collect.valuecollections.toStringV
import com.mpd.common.collect.valuecollections.vLongListOf
import com.mpd.common.collect.valuecollections.vLongSetOf
import mpd.com.common.collect.valuecollections.*
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class LVLongTestClass(val value: Long): Comparable<LVLongTestClass> {
    override operator fun compareTo(other: LVLongTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<LVLongTestClass> {
        override inline fun fromLong(v: Long) = LVLongTestClass(v)
        override inline fun toLong(v: LVLongTestClass): Long = v.value
    }
}

class ListVLongTest {
    @Test
    fun constructors() = with (LVLongTestClass) {
        val primary =
            ArrayListVLong<LVLongTestClass>(MutableLongList().also { it.add(1); it.add(2) })
        assertEquals(vLongListOf(LVLongTestClass(1), LVLongTestClass(2)), primary)

        val byCapacity = ArrayListVLong<LVLongTestClass>(5)
        assertEquals(0, byCapacity.size)

        val fromCollectionVLong: CollectionVLong<LVLongTestClass> =
            vLongSetOf(LVLongTestClass(1), LVLongTestClass(2))
        val fromCollection = ArrayListVLong(fromCollectionVLong)
        assertEquals(2, fromCollection.size)
        assertEquals(true, fromCollection.contains(LVLongTestClass(1)))
        assertEquals(true, fromCollection.contains(LVLongTestClass(2)))

        val source = vLongListOf(LVLongTestClass(10), LVLongTestClass(20), LVLongTestClass(30))
        val fromList = ArrayListVLong(source, -1)
        assertEquals(source, fromList)
    }

    @Test
    fun customNullValue() {
        val list = ArrayListVLong<LVLongTestClass>(5, -1)
        assertEquals(-1L, list.NULL_VALUE)
    }

    @Test
    fun capacity() = with (LVLongTestClass) {
        val list = ArrayListVLong<LVLongTestClass>(5)
        list plusAssign LVLongTestClass(1)
        assertEquals(true, list.capacity >= 1)
    }

    @Test
    fun setOperator() = with (LVLongTestClass) {
        val list = ArrayListVLong<LVLongTestClass>().also { it plusAssign LVLongTestClass(1); it plusAssign LVLongTestClass(2); it plusAssign LVLongTestClass(3) }
        list[1] = LVLongTestClass(20)
        assertEquals(LVLongTestClass(20), list[1])
    }

    @Test
    fun emptyListSingletonIsSharedAndEmpty() {
        assertEquals(0, emptyVLongList<LVLongTestClass>().size)
        assertEquals(0, vLongListOf<LVLongTestClass>().size)
    }

    @Test
    fun vLongListOfArities() = with (LVLongTestClass) {
        assertEquals(1, vLongListOf(LVLongTestClass(1)).size)
        assertEquals(listOf(LVLongTestClass(1)), vLongListOf(LVLongTestClass(1)).toListGeneric())

        val two = vLongListOf(LVLongTestClass(1), LVLongTestClass(2))
        assertEquals(listOf(LVLongTestClass(1), LVLongTestClass(2)), two.toListGeneric())

        val three = vLongListOf(LVLongTestClass(1), LVLongTestClass(2), LVLongTestClass(3))
        assertEquals(listOf(LVLongTestClass(1), LVLongTestClass(2), LVLongTestClass(3)), three.toListGeneric())

        val many = vLongListOf(
            LVLongTestClass(1),
            LVLongTestClass(2),
            LVLongTestClass(3),
            LVLongTestClass(4)
        )
        assertEquals(listOf(LVLongTestClass(1), LVLongTestClass(2), LVLongTestClass(3), LVLongTestClass(4)), many.toListGeneric())
    }

    @Test
    fun mutableVLongListOfArities() = with (LVLongTestClass) {
        assertEquals(0, mutableVLongListOf<LVLongTestClass>().size)

        val one = mutableVLongListOf(LVLongTestClass(1))
        assertEquals(listOf(LVLongTestClass(1)), one.toListGeneric())

        val two = mutableVLongListOf(LVLongTestClass(1), LVLongTestClass(2))
        assertEquals(listOf(LVLongTestClass(1), LVLongTestClass(2)), two.toListGeneric())

        val three = mutableVLongListOf(LVLongTestClass(1), LVLongTestClass(2), LVLongTestClass(3))
        assertEquals(listOf(LVLongTestClass(1), LVLongTestClass(2), LVLongTestClass(3)), three.toListGeneric())

        val many = mutableVLongListOf(
            LVLongTestClass(1),
            LVLongTestClass(2),
            LVLongTestClass(3),
            LVLongTestClass(4)
        )
        assertEquals(listOf(LVLongTestClass(1), LVLongTestClass(2), LVLongTestClass(3), LVLongTestClass(4)), many.toListGeneric())

        // mutableVLongListOf() results are actually mutable, unlike the shared vLongListOf() singleton
        one.add(LVLongTestClass(99))
        assertEquals(2, one.size)
    }

    @Test
    fun equalsHashCodeAndToStringV() = with (LVLongTestClass) {
        val a = vLongListOf(LVLongTestClass(1), LVLongTestClass(2))
        val b = vLongListOf(LVLongTestClass(1), LVLongTestClass(2))
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals("{1, 2}", a.toStringV())
    }
}
