package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.ArrayListVLong
import com.mpd.common.collect.valuecollections.ArrayVLong
import com.mpd.common.collect.valuecollections.ValueLongAdapter
import com.mpd.common.collect.valuecollections.add
import com.mpd.common.collect.valuecollections.asListGeneric
import com.mpd.common.collect.valuecollections.get
import com.mpd.common.collect.valuecollections.plusAssign
import com.mpd.common.collect.valuecollections.retainAll
import com.mpd.common.collect.valuecollections.vLongListOf
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class MutIdxColLongTestClass(val value: Long): Comparable<MutIdxColLongTestClass> {
    override operator fun compareTo(other: MutIdxColLongTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<MutIdxColLongTestClass> {
        override inline fun fromLong(v: Long) = MutIdxColLongTestClass(v)
        override inline fun toLong(v: MutIdxColLongTestClass): Long = v.value
    }
}

class MutableIndexedCollectionVLongTest {
    @Test
    fun addAtIndex() = with (MutIdxColLongTestClass) {
        val list = ArrayListVLong<MutIdxColLongTestClass>().also { it plusAssign MutIdxColLongTestClass(1); it plusAssign MutIdxColLongTestClass(3) }
        list.addBits(1, 2)
        assertEquals(
            vLongListOf(
                MutIdxColLongTestClass(1),
                MutIdxColLongTestClass(2),
                MutIdxColLongTestClass(3)
            ), list)
        list.add(0, MutIdxColLongTestClass(0))
        assertEquals(
            vLongListOf(
                MutIdxColLongTestClass(0),
                MutIdxColLongTestClass(1),
                MutIdxColLongTestClass(2),
                MutIdxColLongTestClass(3)
            ), list)
    }

    // ArrayListVLong.addAll(index, ...) is declared but not yet implemented for either overload.
    @Test
    fun addAllAtIndexNotYetImplemented() = with (MutIdxColLongTestClass) {
        val list = ArrayListVLong<MutIdxColLongTestClass>().also { it plusAssign MutIdxColLongTestClass(1) }
        assertThrows(NotImplementedError::class.java, { list.addAll(0,
            vLongListOf(MutIdxColLongTestClass(2))
        ) })
        assertThrows(NotImplementedError::class.java, { list.addAll(0, listOf(MutIdxColLongTestClass(2))) })
    }

    @Test
    fun removeAtTyped() = with (MutIdxColLongTestClass) {
        val list = ArrayListVLong<MutIdxColLongTestClass>().also { it plusAssign MutIdxColLongTestClass(1); it plusAssign MutIdxColLongTestClass(2); it plusAssign MutIdxColLongTestClass(3) }
        assertEquals(MutIdxColLongTestClass(2), list.removeAt(1))
        assertEquals(2, list.size)
        assertEquals(vLongListOf(MutIdxColLongTestClass(1), MutIdxColLongTestClass(3)), list)
    }

    @Test
    fun removeRange() {
        val list = ArrayListVLong<MutIdxColLongTestClass>().also { it.addBits(1); it.addBits(2); it.addBits(3); it.addBits(4) }
        list.removeRange(1, 3)
        assertEquals(2, list.size)
        assertEquals(1L, list.bitsAtIndex(0))
        assertEquals(4L, list.bitsAtIndex(1))
    }

    // removeAllIndexedBits is declared but not yet implemented, so retainAll(ListVLong<T>) - which
    // is built on top of it - is not yet functional either.
    @Test
    fun removeAllIndexedBitsAndRetainAllNotYetImplemented() = with (MutIdxColLongTestClass) {
        val list = ArrayListVLong<MutIdxColLongTestClass>().also { it plusAssign MutIdxColLongTestClass(1); it plusAssign MutIdxColLongTestClass(2) }
        assertThrows(NotImplementedError::class.java, { list.removeAllIndexedBits { _, b -> b == 1L } })
        assertThrows(NotImplementedError::class.java, { list.retainAll(
            vLongListOf(
                MutIdxColLongTestClass(1)
            )
        ) })
    }

    @Test
    fun asListGenericFullCrud() = with (MutIdxColLongTestClass) {
        val backing = ArrayListVLong<MutIdxColLongTestClass>().also { it plusAssign MutIdxColLongTestClass(1); it plusAssign MutIdxColLongTestClass(2); it plusAssign MutIdxColLongTestClass(3) }
        val list = backing.asListGeneric()
        assertEquals(3, list.size)
        assertEquals(false, list.isEmpty())
        assertEquals(true, list.contains(MutIdxColLongTestClass(2)))
        assertEquals(true, list.containsAll(listOf(MutIdxColLongTestClass(1), MutIdxColLongTestClass(2))))
        assertEquals(MutIdxColLongTestClass(2), list[1])
        assertEquals(1, list.indexOf(MutIdxColLongTestClass(2)))
        assertEquals(1, list.lastIndexOf(MutIdxColLongTestClass(2)))

        assertEquals(MutIdxColLongTestClass(2), list.set(1, MutIdxColLongTestClass(20)))
        assertEquals(MutIdxColLongTestClass(20), backing[1])

        assertEquals(true, list.add(MutIdxColLongTestClass(4)))
        assertEquals(4, backing.size)
        assertEquals(true, list.remove(MutIdxColLongTestClass(4)))
        assertEquals(3, backing.size)

        assertEquals(true, list.addAll(listOf(MutIdxColLongTestClass(5), MutIdxColLongTestClass(6))))
        assertEquals(5, backing.size)
        assertEquals(true, list.removeAll(listOf(MutIdxColLongTestClass(5), MutIdxColLongTestClass(6))))
        assertEquals(3, backing.size)
        assertEquals(true, list.retainAll(listOf(MutIdxColLongTestClass(1), MutIdxColLongTestClass(20), MutIdxColLongTestClass(3))))
        assertEquals(3, backing.size)

        list.add(0, MutIdxColLongTestClass(0))
        assertEquals(4, backing.size)
        assertEquals(MutIdxColLongTestClass(0), backing[0])
        assertEquals(MutIdxColLongTestClass(0), list.removeAt(0))
        assertEquals(3, backing.size)

        val iter = list.listIterator()
        assertEquals(MutIdxColLongTestClass(1), iter.next())
        val iterFromIndex = list.listIterator(1)
        assertEquals(MutIdxColLongTestClass(20), iterFromIndex.next())

        // this delegates straight to the not-yet-implemented interface member
        assertThrows(NotImplementedError::class.java, { list.addAll(0, listOf(MutIdxColLongTestClass(99))) })
        assertThrows(NotImplementedError::class.java, { list.subList(0, 1) })

        list.clear()
        assertEquals(0, backing.size)
    }

    // ArrayVLong only implements ModifiableIndexedCollectionVLong (elements can be replaced via
    // set(), but the collection can't be structurally resized), so its asListGeneric() view must
    // reject every structural mutation while still supporting element replacement and iteration.
    @Test
    fun modifiableOnlyAsListGenericThrowsStructuralMutation() = with (MutIdxColLongTestClass) {
        val backing = ArrayVLong<MutIdxColLongTestClass>(longArrayOf(1, 2, 3))
        val list = backing.asListGeneric()
        assertEquals(3, list.size)
        assertEquals(MutIdxColLongTestClass(2), list[1])
        assertEquals(MutIdxColLongTestClass(2), list.set(1, MutIdxColLongTestClass(20)))
        assertEquals(MutIdxColLongTestClass(20), backing[1])
        val iter = list.listIterator()
        assertEquals(MutIdxColLongTestClass(1), iter.next())

        assertThrows(NotImplementedError::class.java, { list.add(MutIdxColLongTestClass(4)) })
        assertThrows(NotImplementedError::class.java, { list.remove(MutIdxColLongTestClass(1)) })
        assertThrows(NotImplementedError::class.java, { list.addAll(listOf(MutIdxColLongTestClass(4))) })
        assertThrows(NotImplementedError::class.java, { list.addAll(0, listOf(MutIdxColLongTestClass(4))) })
        assertThrows(NotImplementedError::class.java, { list.removeAll(listOf(MutIdxColLongTestClass(1))) })
        assertThrows(NotImplementedError::class.java, { list.retainAll(listOf(MutIdxColLongTestClass(1))) })
        assertThrows(NotImplementedError::class.java, { list.add(0, MutIdxColLongTestClass(4)) })
        assertThrows(NotImplementedError::class.java, { list.removeAt(0) })
        assertThrows(NotImplementedError::class.java, { list.subList(0, 1) })
        assertThrows(NotImplementedError::class.java, { list.clear() })
    }
}
