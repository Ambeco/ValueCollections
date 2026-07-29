package com.mpd.common.collect.valuecollections

import androidx.collection.MutableIntList
import com.mpd.common.collect.valuecollections.ArrayListVInt
import com.mpd.common.collect.valuecollections.CollectionVInt
import com.mpd.common.collect.valuecollections.ValueIntAdapter
import com.mpd.common.collect.valuecollections.add
import com.mpd.common.collect.valuecollections.contains
import com.mpd.common.collect.valuecollections.emptyVIntList
import com.mpd.common.collect.valuecollections.get
import com.mpd.common.collect.valuecollections.indices
import com.mpd.common.collect.valuecollections.lastIndex
import com.mpd.common.collect.valuecollections.mutableVIntListOf
import com.mpd.common.collect.valuecollections.toListGeneric
import com.mpd.common.collect.valuecollections.toStringV
import com.mpd.common.collect.valuecollections.vIntListOf
import com.mpd.common.collect.valuecollections.vIntSetOf
import mpd.com.common.collect.valuecollections.*
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class LVIntTestClass(val value: Int): Comparable<LVIntTestClass> {
    override operator fun compareTo(other: LVIntTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<LVIntTestClass> {
        override inline fun fromInt(v: Int) = LVIntTestClass(v)
        override inline fun toInt(v: LVIntTestClass): Int = v.value
    }
}

class ListVIntTest {
    @Test
    fun constructors() = with (LVIntTestClass) {
        val primary = ArrayListVInt<LVIntTestClass>(MutableIntList().also { it.add(1); it.add(2) })
        assertEquals(vIntListOf(LVIntTestClass(1), LVIntTestClass(2)), primary)

        val byCapacity = ArrayListVInt<LVIntTestClass>(5)
        assertEquals(0, byCapacity.size)

        val fromCollectionVInt: CollectionVInt<LVIntTestClass> =
            vIntSetOf(LVIntTestClass(1), LVIntTestClass(2))
        val fromCollection = ArrayListVInt(fromCollectionVInt)
        assertEquals(2, fromCollection.size)
        assertEquals(true, fromCollection.contains(LVIntTestClass(1)))
        assertEquals(true, fromCollection.contains(LVIntTestClass(2)))

        val source = vIntListOf(LVIntTestClass(10), LVIntTestClass(20), LVIntTestClass(30))
        val fromList = ArrayListVInt(source)
        assertEquals(source, fromList)
    }

    @Test
    fun customNullValue() {
        val list = ArrayListVInt<LVIntTestClass>(5, -1)
        assertEquals(-1, list.NULL_VALUE)
    }

    @Test
    fun lastIndexAndIndices() = with (LVIntTestClass) {
        val list = vIntListOf(
            LVIntTestClass(1),
            LVIntTestClass(2),
            LVIntTestClass(3)
        ) as ArrayListVInt<LVIntTestClass>
        assertEquals(2, list.lastIndex)
        assertEquals(0..2, list.indices)
    }

    @Test
    fun setOperator() = with (LVIntTestClass) {
        val list = vIntListOf(
            LVIntTestClass(1),
            LVIntTestClass(2),
            LVIntTestClass(3)
        ) as ArrayListVInt<LVIntTestClass>
        list[1] = LVIntTestClass(20)
        assertEquals(LVIntTestClass(20), list[1])
    }

    @Test
    fun emptyListSingletonIsSharedAndEmpty() {
        assertEquals(0, emptyVIntList<LVIntTestClass>().size)
        assertEquals(0, vIntListOf<LVIntTestClass>().size)
    }

    @Test
    fun vIntListOfArities() = with (LVIntTestClass) {
        assertEquals(1, vIntListOf(LVIntTestClass(1)).size)
        assertEquals(listOf(LVIntTestClass(1)), vIntListOf(LVIntTestClass(1)).toListGeneric())

        val two = vIntListOf(LVIntTestClass(1), LVIntTestClass(2))
        assertEquals(listOf(LVIntTestClass(1), LVIntTestClass(2)), two.toListGeneric())

        val three = vIntListOf(LVIntTestClass(1), LVIntTestClass(2), LVIntTestClass(3))
        assertEquals(listOf(LVIntTestClass(1), LVIntTestClass(2), LVIntTestClass(3)), three.toListGeneric())

        val many =
            vIntListOf(LVIntTestClass(1), LVIntTestClass(2), LVIntTestClass(3), LVIntTestClass(4))
        assertEquals(listOf(LVIntTestClass(1), LVIntTestClass(2), LVIntTestClass(3), LVIntTestClass(4)), many.toListGeneric())
    }

    @Test
    fun mutableVIntListOfArities() = with (LVIntTestClass) {
        assertEquals(0, mutableVIntListOf<LVIntTestClass>().size)

        val one = mutableVIntListOf(LVIntTestClass(1))
        assertEquals(listOf(LVIntTestClass(1)), one.toListGeneric())

        val two = mutableVIntListOf(LVIntTestClass(1), LVIntTestClass(2))
        assertEquals(listOf(LVIntTestClass(1), LVIntTestClass(2)), two.toListGeneric())

        val three = mutableVIntListOf(LVIntTestClass(1), LVIntTestClass(2), LVIntTestClass(3))
        assertEquals(listOf(LVIntTestClass(1), LVIntTestClass(2), LVIntTestClass(3)), three.toListGeneric())

        val many = mutableVIntListOf(
            LVIntTestClass(1),
            LVIntTestClass(2),
            LVIntTestClass(3),
            LVIntTestClass(4)
        )
        assertEquals(listOf(LVIntTestClass(1), LVIntTestClass(2), LVIntTestClass(3), LVIntTestClass(4)), many.toListGeneric())

        // mutableVIntListOf() results are actually mutable, unlike the shared vIntListOf() singleton
        one.add(LVIntTestClass(99))
        assertEquals(2, one.size)
    }

    @Test
    fun equalsHashCodeAndToStringV() = with (LVIntTestClass) {
        val a = vIntListOf(LVIntTestClass(1), LVIntTestClass(2))
        val b = vIntListOf(LVIntTestClass(1), LVIntTestClass(2))
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals("{1, 2}", a.toStringV())
    }
}
