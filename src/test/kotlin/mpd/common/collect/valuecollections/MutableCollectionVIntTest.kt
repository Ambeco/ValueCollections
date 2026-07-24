package mpd.common.collect.valuecollections

import mpd.com.common.collect.valuecollections.*
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class MutColTestClass(val value: Int): Comparable<MutColTestClass> {
    override operator fun compareTo(other: MutColTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MutColTestClass> {
        override inline fun fromInt(v: Int) = MutColTestClass(v)
        override inline fun toInt(v: MutColTestClass): Int = v.value
    }
}

class MutableCollectionVIntTest {
    // ArraySetVInt is a MutableCollectionVInt that is NOT indexed - the right concrete type
    // to exercise this interface's own surface without pulling in IndexedCollectionVInt behavior.
    private fun simpleSet(): ArraySetVInt<MutColTestClass> = with (MutColTestClass) {
        ArraySetVInt<MutColTestClass>(20).also { it += MutColTestClass(100); it += MutColTestClass(200); it += MutColTestClass(300) }
    }

    @Test
    fun addBitsAndRemoveBits() {
        val set = ArraySetVInt<MutColTestClass>(10)
        assertEquals(true, set.addBits(100))
        assertEquals(true, set.containsBits(100))
        assertEquals(false, set.addBits(100))
        assertEquals(true, set.removeBits(100))
        assertEquals(false, set.containsBits(100))
        assertEquals(false, set.removeBits(100))
    }

    @Test
    fun addAndContainsAndRemoveTyped() = with (MutColTestClass) {
        val set = ArraySetVInt<MutColTestClass>(10)
        assertEquals(true, set.add(MutColTestClass(100)))
        assertEquals(true, set.contains(MutColTestClass(100)))
        assertEquals(true, set.remove(MutColTestClass(100)))
        assertEquals(false, set.contains(MutColTestClass(100)))
    }

    @Test
    fun addAllVariants() = with (MutColTestClass) {
        val fromCollection = ArraySetVInt<MutColTestClass>(10)
        assertEquals(true, fromCollection.addAll(listOf(MutColTestClass(1), MutColTestClass(2))))
        assertEquals(2, fromCollection.size)

        val fromVCollection = ArraySetVInt<MutColTestClass>(10)
        assertEquals(true, fromVCollection.addAll(simpleSet()))
        assertEquals(3, fromVCollection.size)

        val fromArray = ArraySetVInt<MutColTestClass>(10)
        assertEquals(true, fromArray.addAll(arrayOf(MutColTestClass(1), MutColTestClass(2))))
        assertEquals(2, fromArray.size)

        val fromIterable = ArraySetVInt<MutColTestClass>(10)
        assertEquals(true, fromIterable.addAll(listOf(MutColTestClass(1), MutColTestClass(2)).asIterable()))
        assertEquals(2, fromIterable.size)
    }

    @Test
    fun plusAssignVariants() = with (MutColTestClass) {
        val set = ArraySetVInt<MutColTestClass>(10)
        set += MutColTestClass(1)
        assertEquals(1, set.size)
        set += arrayOf(MutColTestClass(2), MutColTestClass(3))
        assertEquals(3, set.size)
        set += listOf(MutColTestClass(4))
        assertEquals(4, set.size)
        set += listOf(MutColTestClass(5)).asIterable()
        assertEquals(5, set.size)
    }

    @Test
    fun removeAllVariants() = with (MutColTestClass) {
        val byList = ArraySetVInt<MutColTestClass>(10).also { it.addAll(listOf(MutColTestClass(1), MutColTestClass(2), MutColTestClass(3))) }
        assertEquals(true, byList.removeAll(vIntListOf(MutColTestClass(1), MutColTestClass(2))))
        assertEquals(1, byList.size)
        assertEquals(true, byList.contains(MutColTestClass(3)))

        val byArray = ArraySetVInt<MutColTestClass>(10).also { it.addAll(listOf(MutColTestClass(1), MutColTestClass(2))) }
        assertEquals(true, byArray.removeAll(arrayOf(MutColTestClass(1))))
        assertEquals(1, byArray.size)

        val byIterable = ArraySetVInt<MutColTestClass>(10).also { it.addAll(listOf(MutColTestClass(1), MutColTestClass(2))) }
        assertEquals(true, byIterable.removeAll(listOf(MutColTestClass(1)).asIterable()))
        assertEquals(1, byIterable.size)

        val byCollection = ArraySetVInt<MutColTestClass>(10).also { it.addAll(listOf(MutColTestClass(1), MutColTestClass(2))) }
        assertEquals(true, byCollection.removeAll(listOf(MutColTestClass(1))))
        assertEquals(1, byCollection.size)

        val byVCollection = ArraySetVInt<MutColTestClass>(10).also { it.addAll(listOf(MutColTestClass(1), MutColTestClass(2))) }
        assertEquals(true, byVCollection.removeAll(vIntListOf(MutColTestClass(1)) as CollectionVInt<MutColTestClass>))
        assertEquals(1, byVCollection.size)
    }

    @Test
    fun removeAllPredicate() = with (MutColTestClass) {
        val set = simpleSet()
        assertEquals(true, set.removeAll { it.value >= 200 })
        assertEquals(1, set.size)
        assertEquals(true, set.contains(MutColTestClass(100)))
    }

    @Test
    fun minusAssignVariants() = with (MutColTestClass) {
        val single = simpleSet()
        single -= MutColTestClass(100)
        assertEquals(2, single.size)

        val byList = simpleSet()
        byList -= vIntListOf(MutColTestClass(100), MutColTestClass(200))
        assertEquals(1, byList.size)

        val byArray = simpleSet()
        byArray -= arrayOf(MutColTestClass(100))
        assertEquals(2, byArray.size)

        val byCollection = simpleSet()
        byCollection -= listOf(MutColTestClass(100))
        assertEquals(2, byCollection.size)

        val byIterable = simpleSet()
        byIterable -= listOf(MutColTestClass(100)).asIterable()
        assertEquals(2, byIterable.size)
    }

    @Test
    fun retainAllVariants() = with (MutColTestClass) {
        val byCollection = simpleSet()
        assertEquals(true, byCollection.retainAll(listOf(MutColTestClass(100))))
        assertEquals(1, byCollection.size)
        assertEquals(true, byCollection.contains(MutColTestClass(100)))

        val byVList = simpleSet()
        byVList.retainAll(vIntListOf(MutColTestClass(100)))
        assertEquals(1, byVList.size)
        assertEquals(true, byVList.contains(MutColTestClass(100)))
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
    fun asCollectionGenericNonIterating() = with (MutColTestClass) {
        val set = simpleSet()
        val generic = set.asCollectionGeneric()
        assertEquals(3, generic.size)
        assertEquals(true, generic.contains(MutColTestClass(100)))
        assertEquals(true, generic.containsAll(listOf(MutColTestClass(100), MutColTestClass(200))))
        assertEquals(true, generic.add(MutColTestClass(400)))
        assertEquals(4, set.size)
        assertEquals(true, generic.remove(MutColTestClass(400)))
        assertEquals(3, set.size)
        assertEquals(true, generic.addAll(listOf(MutColTestClass(500))))
        assertEquals(true, generic.removeAll(listOf(MutColTestClass(500))))
        assertEquals(true, generic.retainAll(listOf(MutColTestClass(100), MutColTestClass(200), MutColTestClass(300))))
        generic.clear()
        assertEquals(0, set.size)
        // ArraySetVInt.asIterable() is not implemented, so the generic view's iterator() throws.
        assertThrows(NotImplementedError::class.java, { generic.iterator() })
    }

    @Test
    fun asIterableAndAsModifiableIterableOnIndexedBacking() = with (MutColTestClass) {
        // ArrayListVInt is also a MutableCollectionVInt (via MutableIndexedCollectionVInt), and unlike
        // ArraySetVInt it does implement asIterable(), so it's used here to cover that member.
        val list: MutableCollectionVInt<MutColTestClass> = ArrayListVInt<MutColTestClass>().also { it += MutColTestClass(1); it += MutColTestClass(2) }
        val values = list.asIterable().toList()
        assertEquals(listOf(MutColTestClass(1), MutColTestClass(2)), values)
        val modifiableIter = list.asModifiableIterable().iterator()
        modifiableIter.next()
        modifiableIter.remove()
        assertEquals(1, list.size)
    }
}
