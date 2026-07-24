package mpd.common.collect.valuecollections

import androidx.collection.MutableIntSet
import mpd.com.common.collect.valuecollections.*
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class SVIntTestClass(val value: Int): Comparable<SVIntTestClass> {
    override operator fun compareTo(other: SVIntTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<SVIntTestClass> {
        override inline fun fromInt(v: Int) = SVIntTestClass(v)
        override inline fun toInt(v: SVIntTestClass): Int = v.value
    }
}

class SetVIntTest {
    @Test
    fun constructors() = with (SVIntTestClass) {
        val primary = ArraySetVInt<SVIntTestClass>(MutableIntSet().also { it.add(1); it.add(2) })
        assertEquals(2, primary.size)
        assertEquals(true, primary.contains(SVIntTestClass(1)))

        val byCapacity = ArraySetVInt<SVIntTestClass>(5)
        assertEquals(0, byCapacity.size)
    }

    @Test
    fun customNullValue() {
        val set = ArraySetVInt<SVIntTestClass>(5, -1)
        assertEquals(-1, set.NULL_VALUE)
    }

    @Test
    fun emptySetSingletonIsSharedAndEmpty() {
        assertEquals(0, emptySetVInt<SVIntTestClass>().size)
        assertEquals(0, vIntSetOf<SVIntTestClass>().size)
    }

    @Test
    fun vIntSetOfArities() = with (SVIntTestClass) {
        assertEquals(setOf(SVIntTestClass(1)), vIntSetOf(SVIntTestClass(1)).toSetGeneric())
        assertEquals(setOf(SVIntTestClass(1), SVIntTestClass(2)), vIntSetOf(SVIntTestClass(1), SVIntTestClass(2)).toSetGeneric())
        assertEquals(setOf(SVIntTestClass(1), SVIntTestClass(2), SVIntTestClass(3)), vIntSetOf(SVIntTestClass(1), SVIntTestClass(2), SVIntTestClass(3)).toSetGeneric())
        assertEquals(setOf(SVIntTestClass(1), SVIntTestClass(2), SVIntTestClass(3), SVIntTestClass(4)), vIntSetOf(SVIntTestClass(1), SVIntTestClass(2), SVIntTestClass(3), SVIntTestClass(4)).toSetGeneric())
    }

    @Test
    fun mutableSetVIntOfArities() = with (SVIntTestClass) {
        assertEquals(0, mutableSetVIntOf<SVIntTestClass>().size)
        val one = mutableSetVIntOf(SVIntTestClass(1))
        assertEquals(setOf(SVIntTestClass(1)), one.toSetGeneric())
        assertEquals(setOf(SVIntTestClass(1), SVIntTestClass(2)), mutableSetVIntOf(SVIntTestClass(1), SVIntTestClass(2)).toSetGeneric())
        assertEquals(setOf(SVIntTestClass(1), SVIntTestClass(2), SVIntTestClass(3)), mutableSetVIntOf(SVIntTestClass(1), SVIntTestClass(2), SVIntTestClass(3)).toSetGeneric())
        assertEquals(setOf(SVIntTestClass(1), SVIntTestClass(2), SVIntTestClass(3), SVIntTestClass(4)), mutableSetVIntOf(SVIntTestClass(1), SVIntTestClass(2), SVIntTestClass(3), SVIntTestClass(4)).toSetGeneric())

        // mutableSetVIntOf() results are actually mutable, unlike the shared vIntSetOf() singleton
        one.add(SVIntTestClass(99))
        assertEquals(2, one.size)
    }

    @Test
    fun equalsAndHashCodeIgnoreOrder() = with (SVIntTestClass) {
        val a = vIntSetOf(SVIntTestClass(1), SVIntTestClass(2), SVIntTestClass(3))
        val b = vIntSetOf(SVIntTestClass(3), SVIntTestClass(2), SVIntTestClass(1))
        assertEquals(true, a == b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toStringVSingleElement() = with (SVIntTestClass) {
        assertEquals("{1}", vIntSetOf(SVIntTestClass(1)).toStringV())
    }
}
