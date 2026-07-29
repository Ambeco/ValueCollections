package com.mpd.common.collect.valuecollections

import com.mpd.common.collect.valuecollections.IteratorVLongGeneric
import com.mpd.common.collect.valuecollections.IteratorVLongJava
import com.mpd.common.collect.valuecollections.IteratorVLongKotlin
import com.mpd.common.collect.valuecollections.ListIteratorVLong
import com.mpd.common.collect.valuecollections.MutableIteratorVLongGeneric
import com.mpd.common.collect.valuecollections.MutableIteratorVLongJava
import com.mpd.common.collect.valuecollections.MutableIteratorVLongKotlin
import com.mpd.common.collect.valuecollections.MutableListIteratorVLong
import com.mpd.common.collect.valuecollections.VIteratorIndexedValueLong
import com.mpd.common.collect.valuecollections.ValueLongAdapter
import com.mpd.common.collect.valuecollections.mutableVIteratableFrom
import com.mpd.common.collect.valuecollections.vIteratableFrom
import com.mpd.common.collect.valuecollections.vIteratorFrom
import org.junit.jupiter.api.Assertions.assertThrows
import java.util.Arrays
import java.util.PrimitiveIterator
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class IterVLongTestClass(val value: Long): Comparable<IterVLongTestClass> {
    override operator fun compareTo(other: IterVLongTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<IterVLongTestClass> {
        override inline fun fromLong(v: Long) = IterVLongTestClass(v)
        override inline fun toLong(v: IterVLongTestClass): Long = v.value
    }
}

// Test doubles: real stdlib iterators for LongArray/streams don't support removal,
// so these back a MutableList<Long> to exercise the remove() paths for real.
private class RemovableOfLong(private val list: MutableList<Long>) : PrimitiveIterator.OfLong {
    var idx = 0
    override fun hasNext() = idx < list.size
    override fun nextLong(): Long = list[idx++]
    override fun remove() { list.removeAt(--idx) }
}
private class RemovableLongIterator(private val list: MutableList<Long>) : LongIterator(), MutableIterator<Long> {
    var idx = 0
    override fun hasNext() = idx < list.size
    override fun nextLong(): Long = list[idx++]
    override fun remove() { list.removeAt(--idx) }
}

class IteratorVLongTest {
    @Test
    fun iteratorVLongJava() = with (IterVLongTestClass) {
        val delegate = Arrays.stream(longArrayOf(10, 20, 30)).iterator()
        val iter = IteratorVLongJava(delegate, IterVLongTestClass)
        assertEquals(true, iter === iter.iterator())
        assertEquals(true, iter.hasNext())
        assertEquals(IterVLongTestClass(10), iter.next())
        assertEquals(IterVLongTestClass(20), iter.nextLong())
        assertThrows(UnsupportedOperationException::class.java, { iter.remove() })
        val collected = mutableListOf<IterVLongTestClass>()
        iter.forEachRemaining { collected.add(it) }
        assertEquals(listOf(IterVLongTestClass(30)), collected)
    }

    @Test
    fun mutableIteratorVLongJava() = with (IterVLongTestClass) {
        val list = mutableListOf(10L, 20L, 30L)
        val iter = MutableIteratorVLongJava(RemovableOfLong(list), IterVLongTestClass)
        assertEquals(IterVLongTestClass(10), iter.next())
        iter.remove()
        assertEquals(listOf(20L, 30L), list)
        assertEquals(IterVLongTestClass(20), iter.nextInt())
    }

    @Test
    fun iteratorVLongKotlin() = with (IterVLongTestClass) {
        val delegate = longArrayOf(10, 20, 30).iterator()
        val iter = IteratorVLongKotlin(delegate, IterVLongTestClass)
        assertEquals(IterVLongTestClass(10), iter.next())
        assertThrows(UnsupportedOperationException::class.java, { iter.remove() })
        val collected = mutableListOf<IterVLongTestClass>()
        iter.forEachRemaining { collected.add(it) }
        assertEquals(listOf(IterVLongTestClass(20), IterVLongTestClass(30)), collected)
    }

    @Test
    fun mutableIteratorVLongKotlin() = with (IterVLongTestClass) {
        val list = mutableListOf(10L, 20L, 30L)
        val iter = MutableIteratorVLongKotlin(RemovableLongIterator(list), IterVLongTestClass)
        assertEquals(IterVLongTestClass(10), iter.next())
        iter.remove()
        assertEquals(listOf(20L, 30L), list)
        assertEquals(IterVLongTestClass(20), iter.nextInt())
    }

    @Test
    fun iteratorVLongGeneric() = with (IterVLongTestClass) {
        val delegate = listOf(10L, 20L, 30L).iterator()
        val iter = IteratorVLongGeneric(delegate, IterVLongTestClass)
        assertEquals(IterVLongTestClass(10), iter.next())
        assertThrows(UnsupportedOperationException::class.java, { iter.remove() })
        val collected = mutableListOf<IterVLongTestClass>()
        iter.forEachRemaining { collected.add(it) }
        assertEquals(listOf(IterVLongTestClass(20), IterVLongTestClass(30)), collected)
    }

    @Test
    fun mutableIteratorVLongGeneric() = with (IterVLongTestClass) {
        val list = mutableListOf(10L, 20L, 30L)
        val iter = MutableIteratorVLongGeneric(list.iterator(), IterVLongTestClass)
        assertEquals(IterVLongTestClass(10), iter.next())
        iter.remove()
        assertEquals(listOf(20L, 30L), list)
        assertEquals(IterVLongTestClass(20), iter.nextInt())
    }

    // Note: unlike IteratorVInt.kt (which has distinctly-named vIteratorFrom/mutableVIteratorFrom
    // and vIteratableFrom/mutableVIteratableFrom), IteratorVLong.kt overloads a single
    // "vIteratorFrom" name by parameter type for the read/mutable iterator variants, and its
    // "vIteratableFrom"/"mutableVIteratableFrom" names are swapped relative to which one is
    // actually mutable. The tests below exercise the real (current) signatures as named.
    @Test
    fun vIteratorFromDispatchReadOnly() = with (IterVLongTestClass) {
        // Declared as Iterator<Long> (not MutableIterator<Long>) so overload resolution picks the
        // read-only vIteratorFrom(Iterator<Long>) overload even though PrimitiveIterator.OfLong
        // also happens to satisfy MutableIterator<Long> from Kotlin's point of view.
        val javaDelegate: Iterator<Long> = Arrays.stream(longArrayOf(1, 2)).iterator()
        val kotlinDelegate: Iterator<Long> = longArrayOf(1, 2).iterator()
        val genericDelegate: Iterator<Long> = listOf(1L, 2L).iterator()
        assertEquals(true, vIteratorFrom<IterVLongTestClass>(javaDelegate) is IteratorVLongJava<IterVLongTestClass>)
        assertEquals(true, vIteratorFrom<IterVLongTestClass>(kotlinDelegate) is IteratorVLongKotlin<IterVLongTestClass>)
        assertEquals(true, vIteratorFrom<IterVLongTestClass>(genericDelegate) is IteratorVLongGeneric<IterVLongTestClass>)
    }

    @Test
    fun vIteratorFromDispatchMutable() = with (IterVLongTestClass) {
        assertEquals(true, vIteratorFrom<IterVLongTestClass>(
            RemovableOfLong(
                mutableListOf(
                    1,
                    2
                )
            ) as MutableIterator<Long>
        ) is MutableIteratorVLongJava<IterVLongTestClass>
        )
        assertEquals(true, vIteratorFrom<IterVLongTestClass>(
            RemovableLongIterator(
                mutableListOf(
                    1,
                    2
                )
            ) as MutableIterator<Long>
        ) is MutableIteratorVLongKotlin<IterVLongTestClass>
        )
        assertEquals(true, vIteratorFrom<IterVLongTestClass>(
            mutableListOf(
                1L,
                2L
            ).iterator()
        ) is MutableIteratorVLongGeneric<IterVLongTestClass>
        )
    }

    @Test
    fun mutableVIteratableFromDispatch_isActuallyReadOnly() = with (IterVLongTestClass) {
        val iterable = mutableVIteratableFrom<IterVLongTestClass>(listOf(10L, 20L).iterator())
        assertEquals(true, iterable is IteratorVLongGeneric<IterVLongTestClass>)
        assertEquals(listOf(IterVLongTestClass(10), IterVLongTestClass(20)), iterable.toList())
    }

    @Test
    fun vIteratableFromDispatch_isActuallyMutable() = with (IterVLongTestClass) {
        val iterable = vIteratableFrom<IterVLongTestClass>(mutableListOf(10L, 20L).iterator())
        assertEquals(true, iterable is MutableIteratorVLongGeneric<IterVLongTestClass>)
        assertEquals(listOf(IterVLongTestClass(10), IterVLongTestClass(20)), iterable.toList())
    }

    @Test
    fun vIteratorIndexedValueLong() = with (IterVLongTestClass) {
        val delegate = listOf(10L, 20L, 30L).withIndex().iterator()
        val iter = VIteratorIndexedValueLong(delegate, IterVLongTestClass)
        assertEquals(true, iter === iter.iterator())
        val collected = mutableListOf<IndexedValue<IterVLongTestClass>>()
        while (iter.hasNext()) collected.add(iter.next())
        assertEquals(3, collected.size)
        assertEquals(0, collected[0].index)
        assertEquals(IterVLongTestClass(10), collected[0].value)
        assertEquals(2, collected[2].index)
        assertEquals(IterVLongTestClass(30), collected[2].value)
    }

    @Test
    fun listIteratorVLong() {
        val list = listOf(IterVLongTestClass(10), IterVLongTestClass(20), IterVLongTestClass(30))
        val iter = ListIteratorVLong(list)
        assertEquals(true, iter.hasNext())
        assertEquals(false, iter.hasPrevious())
        assertEquals(0, iter.nextIndex())
        assertEquals(IterVLongTestClass(10), iter.next())
        assertEquals(1, iter.nextIndex())
        assertEquals(0, iter.previousIndex())
        assertEquals(IterVLongTestClass(20), iter.next())
        assertEquals(true, iter.hasPrevious())
        assertEquals(IterVLongTestClass(20), iter.previous())
        assertEquals(IterVLongTestClass(20), iter.next())
        assertEquals(IterVLongTestClass(30), iter.next())
        assertEquals(false, iter.hasNext())
    }

    @Test
    fun mutableListIteratorVLong() {
        val list = mutableListOf(IterVLongTestClass(10), IterVLongTestClass(20), IterVLongTestClass(30))
        val iter = MutableListIteratorVLong(list)
        assertEquals(IterVLongTestClass(10), iter.next())
        iter.set(IterVLongTestClass(-1))
        assertEquals(listOf(IterVLongTestClass(-1), IterVLongTestClass(20), IterVLongTestClass(30)), list)
        iter.add(IterVLongTestClass(99))
        assertEquals(listOf(IterVLongTestClass(-1), IterVLongTestClass(99), IterVLongTestClass(20), IterVLongTestClass(30)), list)
        assertEquals(IterVLongTestClass(20), iter.next())
        iter.remove()
        assertEquals(listOf(IterVLongTestClass(-1), IterVLongTestClass(99), IterVLongTestClass(30)), list)
    }
}
