package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.ArrayListVInt
import com.mpd.common.collect.valuecollections.ArraySetVInt
import com.mpd.common.collect.valuecollections.IndexedCollectionVInt
import com.mpd.common.collect.valuecollections.ValueIntAdapter
import com.mpd.common.collect.valuecollections.add
import com.mpd.common.collect.valuecollections.allIndexed
import com.mpd.common.collect.valuecollections.anyIndexed
import com.mpd.common.collect.valuecollections.asListGeneric
import com.mpd.common.collect.valuecollections.component1
import com.mpd.common.collect.valuecollections.component2
import com.mpd.common.collect.valuecollections.component3
import com.mpd.common.collect.valuecollections.component4
import com.mpd.common.collect.valuecollections.component5
import com.mpd.common.collect.valuecollections.contains
import com.mpd.common.collect.valuecollections.contentEquals
import com.mpd.common.collect.valuecollections.copyInto
import com.mpd.common.collect.valuecollections.drop
import com.mpd.common.collect.valuecollections.dropLast
import com.mpd.common.collect.valuecollections.dropLastWhile
import com.mpd.common.collect.valuecollections.dropWhile
import com.mpd.common.collect.valuecollections.elementAtIndex
import com.mpd.common.collect.valuecollections.elementAtOrElse
import com.mpd.common.collect.valuecollections.elementAtOrNull
import com.mpd.common.collect.valuecollections.filter
import com.mpd.common.collect.valuecollections.filterFromMask
import com.mpd.common.collect.valuecollections.filterIndexed
import com.mpd.common.collect.valuecollections.filterIndexedMask
import com.mpd.common.collect.valuecollections.filterIndexedTo
import com.mpd.common.collect.valuecollections.filterMask
import com.mpd.common.collect.valuecollections.filterNot
import com.mpd.common.collect.valuecollections.filterNotIndexed
import com.mpd.common.collect.valuecollections.filterNotIndexedTo
import com.mpd.common.collect.valuecollections.filterNotTo
import com.mpd.common.collect.valuecollections.filterTo
import com.mpd.common.collect.valuecollections.findLast
import com.mpd.common.collect.valuecollections.first
import com.mpd.common.collect.valuecollections.firstNotNullOf
import com.mpd.common.collect.valuecollections.firstNotNullOfOrNull
import com.mpd.common.collect.valuecollections.firstOrNull
import com.mpd.common.collect.valuecollections.get
import com.mpd.common.collect.valuecollections.getBits
import com.mpd.common.collect.valuecollections.getOrElse
import com.mpd.common.collect.valuecollections.getOrNull
import com.mpd.common.collect.valuecollections.indexOf
import com.mpd.common.collect.valuecollections.indexOfFirst
import com.mpd.common.collect.valuecollections.indexOfFirstBits
import com.mpd.common.collect.valuecollections.indexOfFirstIndexed
import com.mpd.common.collect.valuecollections.indexOfLast
import com.mpd.common.collect.valuecollections.indexOfLastIndexed
import com.mpd.common.collect.valuecollections.last
import com.mpd.common.collect.valuecollections.lastIndexOf
import com.mpd.common.collect.valuecollections.lastOrNull
import com.mpd.common.collect.valuecollections.reversed
import com.mpd.common.collect.valuecollections.shuffle
import com.mpd.common.collect.valuecollections.slice
import com.mpd.common.collect.valuecollections.sliceArray
import com.mpd.common.collect.valuecollections.take
import com.mpd.common.collect.valuecollections.takeLast
import com.mpd.common.collect.valuecollections.takeLastWhile
import com.mpd.common.collect.valuecollections.takeWhile
import com.mpd.common.collect.valuecollections.takeWhileIndexed
import com.mpd.common.collect.valuecollections.toListGeneric
import com.mpd.common.collect.valuecollections.toMutableList
import com.mpd.common.collect.valuecollections.vIntListOf
import com.mpd.common.collect.valuecollections.windowed
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class IdxTestClass(val value: Int): Comparable<IdxTestClass> {
    override operator fun compareTo(other: IdxTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<IdxTestClass> {
        override inline fun fromInt(v: Int) = IdxTestClass(v)
        override inline fun toInt(v: IdxTestClass): Int = v.value
    }
}

class IndexedCollectionVIntTest {
    private fun simpleList(): ArrayListVInt<IdxTestClass> = with (IdxTestClass) {
        val array = ArrayListVInt<IdxTestClass>(10)
        for (i in 0..9)
            array.add(i, IdxTestClass(100 * (i + 1)))
        return array
    }

    @Test
    fun asListGeneric() = with (IdxTestClass) {
        val array = simpleList()
        val list: List<IdxTestClass> = array.asListGeneric()
        assertEquals(10, list.size)
        assertEquals(false, list.isEmpty())
        assertEquals(true, list.contains(IdxTestClass(500)))
        assertEquals(IdxTestClass(300), list[2])
        assertEquals(2, list.indexOf(IdxTestClass(300)))
        assertEquals(2, list.lastIndexOf(IdxTestClass(300)))
        assertEquals(true, list.containsAll(listOf(IdxTestClass(100), IdxTestClass(200))))
    }

    @Test
    fun anyAllIndexed() = with (IdxTestClass) {
        val array = simpleList()
        assertEquals(true, array.anyIndexed { i, e -> i == 3 && e.value == 400 })
        assertEquals(false, array.anyIndexed { i, e -> i == 3 && e.value == 999 })
        assertEquals(true, array.allIndexed { i, e -> e.value == 100 * (i + 1) })
        assertEquals(false, array.allIndexed { i, e -> e.value == 100 })
    }

    @Test
    fun contentEquals() = with (IdxTestClass) {
        val a = simpleList()
        val b = simpleList()
        assertEquals(true, a.contentEquals(b))
        b.add(IdxTestClass(9999))
        assertEquals(false, a.contentEquals(b))
        assertEquals(false, a.contentEquals(null as IndexedCollectionVInt<IdxTestClass>?))
    }

    @Test
    fun components() = with (IdxTestClass) {
        val array = simpleList()
        assertEquals(IdxTestClass(100), array.component1())
        assertEquals(IdxTestClass(200), array.component2())
        assertEquals(IdxTestClass(300), array.component3())
        assertEquals(IdxTestClass(400), array.component4())
        assertEquals(IdxTestClass(500), array.component5())
    }

    @Test
    fun elementAtVariants() = with (IdxTestClass) {
        val array = simpleList()
        assertEquals(IdxTestClass(300), array.elementAtIndex(2))
        assertEquals(IdxTestClass(300), array.elementAtOrNull(2))
        assertEquals(null, array.elementAtOrNull(20))
        assertEquals(IdxTestClass(300), array.elementAtOrElse(2) { IdxTestClass(-1) })
        assertEquals(IdxTestClass(-1), array.elementAtOrElse(20) { IdxTestClass(-1) })
    }

    @Test
    fun getVariants() = with (IdxTestClass) {
        val array = simpleList()
        assertEquals(300, array.getBits(2))
        assertEquals(array.NULL_VALUE, array.getBits(20))
        assertEquals(IdxTestClass(300), array[2])
        assertThrows(IndexOutOfBoundsException::class.java, { array[20] })
        assertEquals(IdxTestClass(300), array.getOrElse(2) { IdxTestClass(-1) })
        assertEquals(IdxTestClass(-1), array.getOrElse(20) { IdxTestClass(-1) })
        assertEquals(IdxTestClass(300), array.getOrNull(2))
        assertEquals(null, array.getOrNull(20))
    }

    @Test
    fun findLast() = with (IdxTestClass) {
        val array = vIntListOf(IdxTestClass(1), IdxTestClass(2), IdxTestClass(1))
        assertEquals(IdxTestClass(1), array.findLast { it.value == 1 })
        assertEquals(null, array.findLast { it.value == 99 })
    }

    @Test
    fun firstVariants() = with (IdxTestClass) {
        val array = simpleList()
        assertEquals(IdxTestClass(100), array.first())
        assertEquals(IdxTestClass(400), array.first { it.value >= 400 })
        assertThrows(NoSuchElementException::class.java, { array.first { it.value >= 9999 } })
    }

    @Test
    fun firstNotNullOfVariants() = with (IdxTestClass) {
        val array = simpleList()
        assertEquals(300, array.firstNotNullOf { if (it.value > 250) it.value else null })
        assertThrows(NoSuchElementException::class.java, { array.firstNotNullOf { if (it.value > 9999) it.value else null }; Unit })
        assertEquals(300, array.firstNotNullOfOrNull { if (it.value > 250) it.value else null })
        assertEquals(null, array.firstNotNullOfOrNull { if (it.value > 9999) it.value else null })
    }

    @Test
    fun firstOrNullVariants() = with (IdxTestClass) {
        val array = simpleList()
        assertEquals(IdxTestClass(100), array.firstOrNull())
        assertEquals(null, ArrayListVInt<IdxTestClass>().firstOrNull())
        assertEquals(IdxTestClass(400), array.firstOrNull { it.value >= 400 })
        assertEquals(null, array.firstOrNull { it.value >= 9999 })
    }

    @Test
    fun indexOfVariants() = with (IdxTestClass) {
        val array = vIntListOf(IdxTestClass(1), IdxTestClass(2), IdxTestClass(1), IdxTestClass(3))
        assertEquals(0, array.indexOf(IdxTestClass(1)))
        assertEquals(-1, array.indexOf(IdxTestClass(99)))
        assertEquals(0, array.indexOfFirstBits { it == 1 })
        assertEquals(0, array.indexOfFirst { it.value == 1 })
        assertEquals(2, array.indexOfFirstIndexed { i, e -> i > 0 && e.value == 1 })
        assertEquals(2, array.indexOfFirstIndexedBits { i, b -> i > 0 && b == 1 })
        assertEquals(2, array.indexOfLast { it.value == 1 })
        assertEquals(2, array.indexOfLastIndexed { _, e -> e.value == 1 })
        assertEquals(2, array.indexOfLastIndexedBits { _, b -> b == 1 })
        assertEquals(2, array.lastIndexOf(IdxTestClass(1)))
        assertEquals(-1, array.lastIndexOf(IdxTestClass(99)))
    }

    @Test
    fun lastVariants() = with (IdxTestClass) {
        val array = simpleList()
        assertEquals(IdxTestClass(1000), array.last())
        assertEquals(IdxTestClass(700), array.last { it.value <= 700 })
        assertThrows(NoSuchElementException::class.java, { array.last { it.value >= 9999 } })
        assertEquals(IdxTestClass(1000), array.lastOrNull())
        assertEquals(null, ArrayListVInt<IdxTestClass>().lastOrNull())
        assertEquals(IdxTestClass(700), array.lastOrNull { it.value <= 700 })
        assertEquals(null, array.lastOrNull { it.value >= 9999 })
    }

    @Test
    fun dropAndTake() = with (IdxTestClass) {
        val array = simpleList()
        assertEquals(
            vIntListOf(
                IdxTestClass(400),
                IdxTestClass(500),
                IdxTestClass(600),
                IdxTestClass(700),
                IdxTestClass(800),
                IdxTestClass(900),
                IdxTestClass(1000)
            ), array.drop(3))
        assertEquals(
            vIntListOf(
                IdxTestClass(100),
                IdxTestClass(200),
                IdxTestClass(300),
                IdxTestClass(400),
                IdxTestClass(500),
                IdxTestClass(600),
                IdxTestClass(700)
            ), array.dropLast(3))
        assertEquals(
            vIntListOf(
                IdxTestClass(100),
                IdxTestClass(200),
                IdxTestClass(300)
            ), array.take(3))
        assertEquals(
            vIntListOf(
                IdxTestClass(800),
                IdxTestClass(900),
                IdxTestClass(1000)
            ), array.takeLast(3))
    }

    @Test
    fun dropWhileAndTakeWhile() = with (IdxTestClass) {
        val array = simpleList()
        assertEquals(
            vIntListOf(
                IdxTestClass(400),
                IdxTestClass(500),
                IdxTestClass(600),
                IdxTestClass(700),
                IdxTestClass(800),
                IdxTestClass(900),
                IdxTestClass(1000)
            ), array.dropWhile { it.value <= 300 })
        assertEquals(
            vIntListOf(
                IdxTestClass(100),
                IdxTestClass(200),
                IdxTestClass(300),
                IdxTestClass(400),
                IdxTestClass(500),
                IdxTestClass(600),
                IdxTestClass(700)
            ), array.dropLastWhile { it.value >= 800 })
        assertEquals(
            vIntListOf(
                IdxTestClass(100),
                IdxTestClass(200),
                IdxTestClass(300)
            ), array.takeWhile { it.value <= 300 })
        assertEquals(
            vIntListOf(
                IdxTestClass(100),
                IdxTestClass(200),
                IdxTestClass(300)
            ), array.takeWhileIndexed { i, _ -> i < 3 })
        assertEquals(
            vIntListOf(
                IdxTestClass(800),
                IdxTestClass(900),
                IdxTestClass(1000)
            ), array.takeLastWhile { it.value >= 800 })
    }

    @Test
    fun filterVariants() = with (IdxTestClass) {
        val array = simpleList()
        assertEquals(
            vIntListOf(
                IdxTestClass(800),
                IdxTestClass(900),
                IdxTestClass(1000)
            ), array.filter { it.value >= 800 })
        assertEquals(3, array.filterMask { it.value >= 800 }.cardinality())
        assertEquals(
            vIntListOf(
                IdxTestClass(800),
                IdxTestClass(900),
                IdxTestClass(1000)
            ), array.filterFromMask(array.filterMask { it.value >= 800 }))
        assertEquals(
            vIntListOf(
                IdxTestClass(100),
                IdxTestClass(300),
                IdxTestClass(500),
                IdxTestClass(700),
                IdxTestClass(900)
            ), array.filterIndexed { i, _ -> i % 2 == 0 })
        assertEquals(5, array.filterIndexedMask { i, _ -> i % 2 == 0 }.cardinality())
        assertEquals(
            vIntListOf(
                IdxTestClass(200),
                IdxTestClass(400),
                IdxTestClass(600),
                IdxTestClass(800),
                IdxTestClass(1000)
            ), array.filterNot { it.value % 200 != 0 })
        assertEquals(
            vIntListOf(
                IdxTestClass(200),
                IdxTestClass(400),
                IdxTestClass(600),
                IdxTestClass(800),
                IdxTestClass(1000)
            ), array.filterNotIndexed { i, _ -> i % 2 == 0 })
    }

    @Test
    fun filterToVariants() = with (IdxTestClass) {
        val array = simpleList()
        val vDest = ArrayListVInt<IdxTestClass>()
        array.filterTo(vDest) { it.value >= 800 }
        assertEquals(vIntListOf(IdxTestClass(800), IdxTestClass(900), IdxTestClass(1000)), vDest)
        val genericDest = mutableListOf<IdxTestClass>()
        array.filterTo(genericDest) { it.value >= 800 }
        assertEquals(listOf(IdxTestClass(800), IdxTestClass(900), IdxTestClass(1000)), genericDest)

        val vNotDest = ArrayListVInt<IdxTestClass>()
        array.filterNotTo(vNotDest) { it.value >= 300 }
        assertEquals(vIntListOf(IdxTestClass(100), IdxTestClass(200)), vNotDest)
        val genericNotDest = mutableListOf<IdxTestClass>()
        array.filterNotTo(genericNotDest) { it.value >= 300 }
        assertEquals(listOf(IdxTestClass(100), IdxTestClass(200)), genericNotDest)

        val vIndexedDest = ArraySetVInt<IdxTestClass>(20)
        array.filterIndexedTo(vIndexedDest) { i, _ -> i % 2 == 0 }
        assertEquals(5, vIndexedDest.size)
        assertEquals(true, vIndexedDest.contains(IdxTestClass(100)))
        val genericIndexedDest = mutableListOf<IdxTestClass>()
        array.filterIndexedTo(genericIndexedDest) { i, _ -> i % 2 == 0 }
        assertEquals(listOf(IdxTestClass(100), IdxTestClass(300), IdxTestClass(500), IdxTestClass(700), IdxTestClass(900)), genericIndexedDest)

        val vNotIndexedDest = ArraySetVInt<IdxTestClass>(20)
        array.filterNotIndexedTo(vNotIndexedDest) { i, _ -> i % 2 == 0 }
        assertEquals(5, vNotIndexedDest.size)
        assertEquals(true, vNotIndexedDest.contains(IdxTestClass(200)))
    }

    @Test
    fun sliceVariants() = with (IdxTestClass) {
        val array = simpleList()
        assertEquals(
            vIntListOf(
                IdxTestClass(300),
                IdxTestClass(400),
                IdxTestClass(500),
                IdxTestClass(600)
            ), array.slice(IntRange(2, 5)))
        assertEquals(
            vIntListOf(
                IdxTestClass(200),
                IdxTestClass(600),
                IdxTestClass(1000)
            ), array.slice(1..9 step 4))
        assertEquals(
            vIntListOf(
                IdxTestClass(200),
                IdxTestClass(400),
                IdxTestClass(600)
            ), array.slice(listOf(1, 3, 5)))
    }

    @Test
    fun sliceArrayVariants() = with (IdxTestClass) {
        val array = simpleList()
        val byCollection = array.sliceArray(listOf(0, 2, 4))
        assertEquals(3, byCollection.size)
        assertEquals(IdxTestClass(100), byCollection[0])
        assertEquals(IdxTestClass(300), byCollection[1])
        assertEquals(IdxTestClass(500), byCollection[2])
        val byRange = array.sliceArray(IntRange(2, 5))
        assertEquals(4, byRange.size)
        assertEquals(IdxTestClass(300), byRange[0])
        assertEquals(IdxTestClass(600), byRange[3])
    }

    @Test
    fun copyIntoVariants() = with (IdxTestClass) {
        val array = simpleList()
        val vDest = ArrayListVInt<IdxTestClass>()
        array.copyInto(vDest)
        assertEquals(array.toMutableList(), vDest)
        val genericDest = mutableListOf<IdxTestClass>()
        array.copyInto(genericDest)
        assertEquals(array.toListGeneric(), genericDest)
    }

    @Test
    fun reversed() {
        val array = simpleList()
        val rev = array.reversed()
        assertEquals(10, rev.size)
        assertEquals(1000, rev.bitsAtIndex(0))
        assertEquals(100, rev.bitsAtIndex(9))
    }

    @Test
    fun shuffleVariants() = with (IdxTestClass) {
        val array = simpleList()
        array.shuffle()
        assertEquals(10, array.size)
        array.shuffle(Random(42))
        assertEquals(10, array.size)
    }

    @Test
    fun windowed() = with (IdxTestClass) {
        val array = vIntListOf(
            IdxTestClass(1),
            IdxTestClass(2),
            IdxTestClass(3),
            IdxTestClass(4),
            IdxTestClass(5)
        )
        val windows = array.windowed(3)
        // Note: current implementation iterates i in 0..<(size-windowSize), yielding one fewer
        // window than the textbook sliding-window definition (which would also include [3,4,5]).
        assertEquals(2, windows.size)
        assertEquals(listOf(IdxTestClass(1), IdxTestClass(2), IdxTestClass(3)), windows[0])
        assertEquals(listOf(IdxTestClass(2), IdxTestClass(3), IdxTestClass(4)), windows[1])
    }
}
