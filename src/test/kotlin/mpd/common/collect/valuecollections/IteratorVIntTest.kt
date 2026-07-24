package mpd.common.collect.valuecollections

import mpd.com.common.collect.valuecollections.*
import org.junit.jupiter.api.Assertions.assertThrows
import java.util.Arrays
import java.util.PrimitiveIterator
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class IterVIntTestClass(val value: Int): Comparable<IterVIntTestClass> {
    override operator fun compareTo(other: IterVIntTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<IterVIntTestClass> {
        override inline fun fromInt(v: Int) = IterVIntTestClass(v)
        override inline fun toInt(v: IterVIntTestClass): Int = v.value
    }
}

// Test doubles: real stdlib iterators for IntArray/streams don't support removal,
// so these back a MutableList<Int> to exercise the remove() paths for real.
private class RemovableOfInt(private val list: MutableList<Int>) : PrimitiveIterator.OfInt {
    var idx = 0
    override fun hasNext() = idx < list.size
    override fun nextInt(): Int = list[idx++]
    override fun remove() { list.removeAt(--idx) }
}
private class RemovableIntIterator(private val list: MutableList<Int>) : IntIterator(), MutableIterator<Int> {
    var idx = 0
    override fun hasNext() = idx < list.size
    override fun nextInt(): Int = list[idx++]
    override fun remove() { list.removeAt(--idx) }
}

class IteratorVIntTest {
    @Test
    fun iteratorVIntJava() = with (IterVIntTestClass) {
        val delegate = Arrays.stream(intArrayOf(10, 20, 30)).iterator()
        val iter = IteratorVIntJava(delegate, IterVIntTestClass)
        assertEquals(true, iter === iter.iterator())
        assertEquals(true, iter.hasNext())
        assertEquals(IterVIntTestClass(10), iter.next())
        assertEquals(IterVIntTestClass(20), iter.nextInt())
        val collected = mutableListOf<IterVIntTestClass>()
        iter.forEachRemaining { collected.add(it) }
        assertEquals(listOf(IterVIntTestClass(30)), collected)
    }

    @Test
    fun mutableIteratorVIntJava() = with (IterVIntTestClass) {
        val list = mutableListOf(10, 20, 30)
        val iter = MutableIteratorVIntJava(RemovableOfInt(list), IterVIntTestClass)
        assertEquals(IterVIntTestClass(10), iter.next())
        iter.remove()
        assertEquals(listOf(20, 30), list)
        assertEquals(IterVIntTestClass(20), iter.nextInt())
    }

    @Test
    fun iteratorVIntKotlin() = with (IterVIntTestClass) {
        val delegate = intArrayOf(10, 20, 30).iterator()
        val iter = IteratorVIntKotlin(delegate, IterVIntTestClass)
        assertEquals(IterVIntTestClass(10), iter.next())
        assertThrows(UnsupportedOperationException::class.java, { iter.remove() })
        val collected = mutableListOf<IterVIntTestClass>()
        iter.forEachRemaining { collected.add(it) }
        assertEquals(listOf(IterVIntTestClass(20), IterVIntTestClass(30)), collected)
    }

    @Test
    fun mutableIteratorVIntKotlin() = with (IterVIntTestClass) {
        val list = mutableListOf(10, 20, 30)
        val iter = MutableIteratorVIntKotlin(RemovableIntIterator(list), IterVIntTestClass)
        assertEquals(IterVIntTestClass(10), iter.next())
        iter.remove()
        assertEquals(listOf(20, 30), list)
        assertEquals(IterVIntTestClass(20), iter.nextInt())
    }

    @Test
    fun iteratorVIntGeneric() = with (IterVIntTestClass) {
        val delegate = listOf(10, 20, 30).iterator()
        val iter = IteratorVIntGeneric(delegate, IterVIntTestClass)
        assertEquals(IterVIntTestClass(10), iter.next())
        assertThrows(UnsupportedOperationException::class.java, { iter.remove() })
        val collected = mutableListOf<IterVIntTestClass>()
        iter.forEachRemaining { collected.add(it) }
        assertEquals(listOf(IterVIntTestClass(20), IterVIntTestClass(30)), collected)
    }

    @Test
    fun mutableIteratorVIntGeneric() = with (IterVIntTestClass) {
        val list = mutableListOf(10, 20, 30)
        val iter = MutableIteratorVIntGeneric(list.iterator(), IterVIntTestClass)
        assertEquals(IterVIntTestClass(10), iter.next())
        iter.remove()
        assertEquals(listOf(20, 30), list)
        assertEquals(IterVIntTestClass(20), iter.nextInt())
    }

    @Test
    fun vIteratorFromDispatch() = with (IterVIntTestClass) {
        assertEquals(true, vIteratorFrom<IterVIntTestClass>(Arrays.stream(intArrayOf(1, 2)).iterator()) is IteratorVIntJava<IterVIntTestClass>)
        assertEquals(true, vIteratorFrom<IterVIntTestClass>(intArrayOf(1, 2).iterator()) is IteratorVIntKotlin<IterVIntTestClass>)
        assertEquals(true, vIteratorFrom<IterVIntTestClass>(listOf(1, 2).iterator()) is IteratorVIntGeneric<IterVIntTestClass>)
    }

    @Test
    fun mutableVIteratorFromDispatch() = with (IterVIntTestClass) {
        assertEquals(true, mutableVIteratorFrom<IterVIntTestClass>(RemovableOfInt(mutableListOf(1, 2))) is MutableIteratorVIntJava<IterVIntTestClass>)
        assertEquals(true, mutableVIteratorFrom<IterVIntTestClass>(RemovableIntIterator(mutableListOf(1, 2))) is MutableIteratorVIntKotlin<IterVIntTestClass>)
        assertEquals(true, mutableVIteratorFrom<IterVIntTestClass>(mutableListOf(1, 2).iterator()) is MutableIteratorVIntGeneric<IterVIntTestClass>)
    }

    @Test
    fun vIteratableFromDispatch() = with (IterVIntTestClass) {
        val iterable = vIteratableFrom<IterVIntTestClass>(listOf(10, 20).iterator())
        assertEquals(true, iterable is IteratorVIntGeneric<IterVIntTestClass>)
        assertEquals(listOf(IterVIntTestClass(10), IterVIntTestClass(20)), iterable.toList())
    }

    @Test
    fun mutableVIteratableFromDispatch() = with (IterVIntTestClass) {
        val iterable = mutableVIteratableFrom<IterVIntTestClass>(mutableListOf(10, 20).iterator())
        assertEquals(true, iterable is MutableIteratorVIntGeneric<IterVIntTestClass>)
        assertEquals(listOf(IterVIntTestClass(10), IterVIntTestClass(20)), iterable.toList())
    }

    @Test
    fun vIteratorIndexedValueInt() = with (IterVIntTestClass) {
        val delegate = listOf(10, 20, 30).withIndex().iterator()
        val iter = VIteratorIndexedValueInt(delegate, IterVIntTestClass)
        assertEquals(true, iter === iter.iterator())
        val collected = mutableListOf<IndexedValue<IterVIntTestClass>>()
        while (iter.hasNext()) collected.add(iter.next())
        assertEquals(3, collected.size)
        assertEquals(0, collected[0].index)
        assertEquals(IterVIntTestClass(10), collected[0].value)
        assertEquals(2, collected[2].index)
        assertEquals(IterVIntTestClass(30), collected[2].value)
    }

    @Test
    fun listIteratorVInt() {
        val list = listOf(IterVIntTestClass(10), IterVIntTestClass(20), IterVIntTestClass(30))
        val iter = ListIteratorVInt(list)
        assertEquals(true, iter.hasNext())
        assertEquals(false, iter.hasPrevious())
        assertEquals(0, iter.nextIndex())
        assertEquals(IterVIntTestClass(10), iter.next())
        assertEquals(1, iter.nextIndex())
        assertEquals(0, iter.previousIndex())
        assertEquals(IterVIntTestClass(20), iter.next())
        assertEquals(true, iter.hasPrevious())
        assertEquals(IterVIntTestClass(20), iter.previous())
        assertEquals(IterVIntTestClass(20), iter.next())
        assertEquals(IterVIntTestClass(30), iter.next())
        assertEquals(false, iter.hasNext())
    }

    @Test
    fun mutableListIteratorVInt() {
        val list = mutableListOf(IterVIntTestClass(10), IterVIntTestClass(20), IterVIntTestClass(30))
        val iter = MutableListIteratorVInt(list)
        assertEquals(IterVIntTestClass(10), iter.next())
        iter.set(IterVIntTestClass(-1))
        assertEquals(listOf(IterVIntTestClass(-1), IterVIntTestClass(20), IterVIntTestClass(30)), list)
        iter.add(IterVIntTestClass(99))
        assertEquals(listOf(IterVIntTestClass(-1), IterVIntTestClass(99), IterVIntTestClass(20), IterVIntTestClass(30)), list)
        assertEquals(IterVIntTestClass(20), iter.next())
        iter.remove()
        assertEquals(listOf(IterVIntTestClass(-1), IterVIntTestClass(99), IterVIntTestClass(30)), list)
    }
}
