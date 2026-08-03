package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.ArrayListVLong
import com.mpd.common.collect.valuecollections.ArraySetVLong
import com.mpd.common.collect.valuecollections.CollectionVLong
import com.mpd.common.collect.valuecollections.MutableCollectionVLong
import com.mpd.common.collect.valuecollections.ValueLongAdapter
import com.mpd.common.collect.valuecollections.add
import com.mpd.common.collect.valuecollections.addAll
import com.mpd.common.collect.valuecollections.asCollectionGeneric
import com.mpd.common.collect.valuecollections.contains
import com.mpd.common.collect.valuecollections.minusAssign
import com.mpd.common.collect.valuecollections.plusAssign
import com.mpd.common.collect.valuecollections.remove
import com.mpd.common.collect.valuecollections.removeAll
import com.mpd.common.collect.valuecollections.retainAll
import com.mpd.common.collect.valuecollections.vLongListOf
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class MutColLongTestClass(val value: Long): Comparable<MutColLongTestClass> {
    override operator fun compareTo(other: MutColLongTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<MutColLongTestClass> {
        override inline fun fromLong(v: Long) = MutColLongTestClass(v)
        override inline fun toLong(v: MutColLongTestClass): Long = v.value
    }
}

class MutableCollectionVLongTest {
    // ArraySetVLong is a MutableCollectionVLong that is NOT indexed - the right concrete type
    // to exercise this interface's own surface without pulling in IndexedCollectionVLong behavior.
    private fun simpleSet(): ArraySetVLong<MutColLongTestClass> = with (MutColLongTestClass) {
        ArraySetVLong<MutColLongTestClass>(20).also { it plusAssign MutColLongTestClass(100); it plusAssign MutColLongTestClass(200); it plusAssign MutColLongTestClass(300) }
    }

    @Test
    fun addBitsAndRemoveBits() {
        val set = ArraySetVLong<MutColLongTestClass>(10)
        assertEquals(true, set.addBits(100))
        assertEquals(true, set.containsBits(100))
        assertEquals(false, set.addBits(100))
        assertEquals(true, set.removeBits(100))
        assertEquals(false, set.containsBits(100))
        assertEquals(false, set.removeBits(100))
    }

    @Test
    fun addAndContainsAndRemoveTyped() = with (MutColLongTestClass) {
        val set = ArraySetVLong<MutColLongTestClass>(10)
        assertEquals(true, set.add(MutColLongTestClass(100)))
        assertEquals(true, set.contains(MutColLongTestClass(100)))
        assertEquals(true, set.remove(MutColLongTestClass(100)))
        assertEquals(false, set.contains(MutColLongTestClass(100)))
    }

    @Test
    fun addAllVariants() = with (MutColLongTestClass) {
        val fromCollection = ArraySetVLong<MutColLongTestClass>(10)
        assertEquals(true, fromCollection.addAll(listOf(MutColLongTestClass(1), MutColLongTestClass(2))))
        assertEquals(2, fromCollection.size)

        val fromVCollection = ArraySetVLong<MutColLongTestClass>(10)
        assertEquals(true, fromVCollection.addAll(simpleSet()))
        assertEquals(3, fromVCollection.size)

        val fromArray = ArraySetVLong<MutColLongTestClass>(10)
        assertEquals(true, fromArray.addAll(arrayOf(MutColLongTestClass(1), MutColLongTestClass(2))))
        assertEquals(2, fromArray.size)

        val fromIterable = ArraySetVLong<MutColLongTestClass>(10)
        assertEquals(true, fromIterable.addAll(listOf(MutColLongTestClass(1), MutColLongTestClass(2)).asIterable()))
        assertEquals(2, fromIterable.size)
    }

    @Test
    fun plusAssignVariants() = with (MutColLongTestClass) {
        val set = ArraySetVLong<MutColLongTestClass>(10)
        set plusAssign MutColLongTestClass(1)
        assertEquals(1, set.size)
        set plusAssign arrayOf(MutColLongTestClass(2), MutColLongTestClass(3))
        assertEquals(3, set.size)
        set plusAssign listOf(MutColLongTestClass(4))
        assertEquals(4, set.size)
        set plusAssign listOf(MutColLongTestClass(5)).asIterable()
        assertEquals(5, set.size)
    }

    @Test
    fun removeAllVariants() = with (MutColLongTestClass) {
        val byList = ArraySetVLong<MutColLongTestClass>(10).also { it.addAll(listOf(MutColLongTestClass(1), MutColLongTestClass(2), MutColLongTestClass(3))) }
        assertEquals(true, byList.removeAll(
            vLongListOf(
                MutColLongTestClass(1),
                MutColLongTestClass(2)
            )
        ))
        assertEquals(1, byList.size)
        assertEquals(true, byList.contains(MutColLongTestClass(3)))

        val byArray = ArraySetVLong<MutColLongTestClass>(10).also { it.addAll(listOf(MutColLongTestClass(1), MutColLongTestClass(2))) }
        assertEquals(true, byArray.removeAll(arrayOf(MutColLongTestClass(1))))
        assertEquals(1, byArray.size)

        val byIterable = ArraySetVLong<MutColLongTestClass>(10).also { it.addAll(listOf(MutColLongTestClass(1), MutColLongTestClass(2))) }
        assertEquals(true, byIterable.removeAll(listOf(MutColLongTestClass(1)).asIterable()))
        assertEquals(1, byIterable.size)

        val byCollection = ArraySetVLong<MutColLongTestClass>(10).also { it.addAll(listOf(MutColLongTestClass(1), MutColLongTestClass(2))) }
        assertEquals(true, byCollection.removeAll(listOf(MutColLongTestClass(1))))
        assertEquals(1, byCollection.size)

        val byVCollection = ArraySetVLong<MutColLongTestClass>(10).also { it.addAll(listOf(MutColLongTestClass(1), MutColLongTestClass(2))) }
        assertEquals(true, byVCollection.removeAll(vLongListOf(MutColLongTestClass(1)) as CollectionVLong<MutColLongTestClass>))
        assertEquals(1, byVCollection.size)
    }

    @Test
    fun removeAllPredicate() = with (MutColLongTestClass) {
        val set = simpleSet()
        assertEquals(true, set.removeAll { it.value >= 200 })
        assertEquals(1, set.size)
        assertEquals(true, set.contains(MutColLongTestClass(100)))
    }

    @Test
    fun minusAssignVariants() = with (MutColLongTestClass) {
        val single = simpleSet()
        single minusAssign MutColLongTestClass(100)
        assertEquals(2, single.size)

        val byList = simpleSet()
        byList minusAssign vLongListOf(MutColLongTestClass(100), MutColLongTestClass(200))
        assertEquals(1, byList.size)

        val byArray = simpleSet()
        byArray minusAssign arrayOf(MutColLongTestClass(100))
        assertEquals(2, byArray.size)

        val byCollection = simpleSet()
        byCollection minusAssign listOf(MutColLongTestClass(100))
        assertEquals(2, byCollection.size)

        val byIterable = simpleSet()
        byIterable minusAssign listOf(MutColLongTestClass(100)).asIterable()
        assertEquals(2, byIterable.size)
    }

    @Test
    fun retainAllVariants() = with (MutColLongTestClass) {
        val byCollection = simpleSet()
        assertEquals(true, byCollection.retainAll(listOf(MutColLongTestClass(100))))
        assertEquals(1, byCollection.size)
        assertEquals(true, byCollection.contains(MutColLongTestClass(100)))

        val byVList = simpleSet()
        byVList.retainAll(vLongListOf(MutColLongTestClass(100)))
        assertEquals(1, byVList.size)
        assertEquals(true, byVList.contains(MutColLongTestClass(100)))
    }

    @Test
    fun ensureCapacityTrimAndClear() {
        val set = simpleSet()
        assertEquals(false, set.ensureCapacity(100))
        set.trim(0)
        assertEquals(3, set.size)
        set.clear()
        assertEquals(0, set.size)
    }

    @Test
    fun asCollectionGenericNonIterating() = with (MutColLongTestClass) {
        val set = simpleSet()
        val generic = set.asCollectionGeneric()
        assertEquals(3, generic.size)
        assertEquals(true, generic.contains(MutColLongTestClass(100)))
        assertEquals(true, generic.containsAll(listOf(MutColLongTestClass(100), MutColLongTestClass(200))))
        assertEquals(true, generic.add(MutColLongTestClass(400)))
        assertEquals(4, set.size)
        assertEquals(true, generic.remove(MutColLongTestClass(400)))
        assertEquals(3, set.size)
        assertEquals(true, generic.addAll(listOf(MutColLongTestClass(500))))
        assertEquals(true, generic.removeAll(listOf(MutColLongTestClass(500))))
        assertEquals(true, generic.retainAll(listOf(MutColLongTestClass(100), MutColLongTestClass(200), MutColLongTestClass(300))))
        generic.clear()
        assertEquals(0, set.size)
        // ArraySetVLong.asIterable() is not implemented, so the generic view's iterator() throws.
        assertThrows(NotImplementedError::class.java, { generic.iterator() })
    }

    @Test
    fun toIterableOnIndexedBacking() = with (MutColLongTestClass) {
        // ArrayListVLong is also a MutableCollectionVLong (via MutableIndexedCollectionVLong); at the
        // MutableCollectionVLong<T> static type, only toIterable() (a snapshot) is available - the live,
        // removal-supporting asIterable() is only exposed via the more specific
        // MutableIndexedCollectionVLong<T>/ArrayListVLong<T> types.
        val list: MutableCollectionVLong<MutColLongTestClass> = ArrayListVLong<MutColLongTestClass>().also { it plusAssign MutColLongTestClass(1); it plusAssign MutColLongTestClass(2) }
        val values = list.toIterable().toList()
        assertEquals(listOf(MutColLongTestClass(1), MutColLongTestClass(2)), values)
    }
}
