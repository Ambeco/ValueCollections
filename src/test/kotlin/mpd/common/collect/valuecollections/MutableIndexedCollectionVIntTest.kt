package mpd.common.collect.valuecollections

import mpd.com.common.collect.valuecollections.*
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class MutIdxColTestClass(val value: Int): Comparable<MutIdxColTestClass> {
    override operator fun compareTo(other: MutIdxColTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MutIdxColTestClass> {
        override inline fun fromInt(v: Int) = MutIdxColTestClass(v)
        override inline fun toInt(v: MutIdxColTestClass): Int = v.value
    }
}

class MutableIndexedCollectionVIntTest {
    @Test
    fun addAtIndex() = with (MutIdxColTestClass) {
        val list = ArrayListVInt<MutIdxColTestClass>().also { it += MutIdxColTestClass(1); it += MutIdxColTestClass(3) }
        list.addBits(1, 2)
        assertEquals(vIntListOf(MutIdxColTestClass(1), MutIdxColTestClass(2), MutIdxColTestClass(3)), list)
        list.add(0, MutIdxColTestClass(0))
        assertEquals(vIntListOf(MutIdxColTestClass(0), MutIdxColTestClass(1), MutIdxColTestClass(2), MutIdxColTestClass(3)), list)
    }

    // ArrayListVInt.addAll(index, ...) is declared but not yet implemented for either overload.
    @Test
    fun addAllAtIndexNotYetImplemented() = with (MutIdxColTestClass) {
        val list = ArrayListVInt<MutIdxColTestClass>().also { it += MutIdxColTestClass(1) }
        assertThrows(NotImplementedError::class.java, { list.addAll(0, vIntListOf(MutIdxColTestClass(2))) })
        assertThrows(NotImplementedError::class.java, { list.addAll(0, listOf(MutIdxColTestClass(2))) })
    }

    @Test
    fun removeAtTyped() = with (MutIdxColTestClass) {
        val list = ArrayListVInt<MutIdxColTestClass>().also { it += MutIdxColTestClass(1); it += MutIdxColTestClass(2); it += MutIdxColTestClass(3) }
        assertEquals(MutIdxColTestClass(2), list.removeAt(1))
        assertEquals(2, list.size)
        assertEquals(vIntListOf(MutIdxColTestClass(1), MutIdxColTestClass(3)), list)
    }

    @Test
    fun removeRange() {
        val list = ArrayListVInt<MutIdxColTestClass>().also { it.addBits(1); it.addBits(2); it.addBits(3); it.addBits(4) }
        list.removeRange(1, 3)
        assertEquals(2, list.size)
        assertEquals(1, list.bitsAtIndex(0))
        assertEquals(4, list.bitsAtIndex(1))
    }

    // removeAllIndexedBits is declared but not yet implemented, so retainAll(ListVInt<T>) - which
    // is built on top of it - is not yet functional either.
    @Test
    fun removeAllIndexedBitsAndRetainAllNotYetImplemented() = with (MutIdxColTestClass) {
        val list = ArrayListVInt<MutIdxColTestClass>().also { it += MutIdxColTestClass(1); it += MutIdxColTestClass(2) }
        assertThrows(NotImplementedError::class.java, { list.removeAllIndexedBits { _, b -> b == 1 } })
        assertThrows(NotImplementedError::class.java, { list.retainAll(vIntListOf(MutIdxColTestClass(1))) })
    }

    @Test
    fun asListGenericFullCrud() = with (MutIdxColTestClass) {
        val backing = ArrayListVInt<MutIdxColTestClass>().also { it += MutIdxColTestClass(1); it += MutIdxColTestClass(2); it += MutIdxColTestClass(3) }
        val list = backing.asListGeneric()
        assertEquals(3, list.size)
        assertEquals(false, list.isEmpty())
        assertEquals(true, list.contains(MutIdxColTestClass(2)))
        assertEquals(true, list.containsAll(listOf(MutIdxColTestClass(1), MutIdxColTestClass(2))))
        assertEquals(MutIdxColTestClass(2), list[1])
        assertEquals(1, list.indexOf(MutIdxColTestClass(2)))
        assertEquals(1, list.lastIndexOf(MutIdxColTestClass(2)))

        assertEquals(MutIdxColTestClass(2), list.set(1, MutIdxColTestClass(20)))
        assertEquals(MutIdxColTestClass(20), backing[1])

        assertEquals(true, list.add(MutIdxColTestClass(4)))
        assertEquals(4, backing.size)
        assertEquals(true, list.remove(MutIdxColTestClass(4)))
        assertEquals(3, backing.size)

        assertEquals(true, list.addAll(listOf(MutIdxColTestClass(5), MutIdxColTestClass(6))))
        assertEquals(5, backing.size)
        assertEquals(true, list.removeAll(listOf(MutIdxColTestClass(5), MutIdxColTestClass(6))))
        assertEquals(3, backing.size)
        assertEquals(true, list.retainAll(listOf(MutIdxColTestClass(1), MutIdxColTestClass(20), MutIdxColTestClass(3))))
        assertEquals(3, backing.size)

        list.add(0, MutIdxColTestClass(0))
        assertEquals(4, backing.size)
        assertEquals(MutIdxColTestClass(0), backing[0])
        assertEquals(MutIdxColTestClass(0), list.removeAt(0))
        assertEquals(3, backing.size)

        val iter = list.listIterator()
        assertEquals(MutIdxColTestClass(1), iter.next())
        val iterFromIndex = list.listIterator(1)
        assertEquals(MutIdxColTestClass(20), iterFromIndex.next())

        // this delegates straight to the not-yet-implemented interface member
        assertThrows(NotImplementedError::class.java, { list.addAll(0, listOf(MutIdxColTestClass(99))) })
        assertThrows(NotImplementedError::class.java, { list.subList(0, 1) })

        list.clear()
        assertEquals(0, backing.size)
    }

    // ArrayVInt only implements ModifiableIndexedCollectionVInt (elements can be replaced via
    // set(), but the collection can't be structurally resized), so its asListGeneric() view must
    // reject every structural mutation while still supporting element replacement and iteration.
    @Test
    fun modifiableOnlyAsListGenericThrowsStructuralMutation() = with (MutIdxColTestClass) {
        val backing = ArrayVInt<MutIdxColTestClass>(intArrayOf(1, 2, 3))
        val list = backing.asListGeneric()
        assertEquals(3, list.size)
        assertEquals(MutIdxColTestClass(2), list[1])
        assertEquals(MutIdxColTestClass(2), list.set(1, MutIdxColTestClass(20)))
        assertEquals(MutIdxColTestClass(20), backing[1])
        val iter = list.listIterator()
        assertEquals(MutIdxColTestClass(1), iter.next())

        assertThrows(NotImplementedError::class.java, { list.add(MutIdxColTestClass(4)) })
        assertThrows(NotImplementedError::class.java, { list.remove(MutIdxColTestClass(1)) })
        assertThrows(NotImplementedError::class.java, { list.addAll(listOf(MutIdxColTestClass(4))) })
        assertThrows(NotImplementedError::class.java, { list.addAll(0, listOf(MutIdxColTestClass(4))) })
        assertThrows(NotImplementedError::class.java, { list.removeAll(listOf(MutIdxColTestClass(1))) })
        assertThrows(NotImplementedError::class.java, { list.retainAll(listOf(MutIdxColTestClass(1))) })
        assertThrows(NotImplementedError::class.java, { list.add(0, MutIdxColTestClass(4)) })
        assertThrows(NotImplementedError::class.java, { list.removeAt(0) })
        assertThrows(NotImplementedError::class.java, { list.subList(0, 1) })
        assertThrows(NotImplementedError::class.java, { list.clear() })
    }
}
