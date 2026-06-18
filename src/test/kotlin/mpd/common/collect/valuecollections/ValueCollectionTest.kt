package mpd.common.collect.valuecollections

import mpd.com.common.collect.valuecollections.FlatVIntList
import mpd.com.common.collect.valuecollections.ValueIntAdapter
import mpd.com.common.collect.valuecollections.add
import mpd.com.common.collect.valuecollections.all
import mpd.com.common.collect.valuecollections.any
import mpd.com.common.collect.valuecollections.component1
import mpd.com.common.collect.valuecollections.component2
import mpd.com.common.collect.valuecollections.component3
import mpd.com.common.collect.valuecollections.component4
import mpd.com.common.collect.valuecollections.component5
import mpd.com.common.collect.valuecollections.contains
import mpd.com.common.collect.valuecollections.forEach
import mpd.com.common.collect.valuecollections.single
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

@JvmInline
value class MyTestClass(val value: Int) {
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MyTestClass> {
        override inline fun fromInt(v: Int) = MyTestClass(v)
        override inline fun toInt(v: MyTestClass): Int = v.value
    }
}

class ValueCollectionTest {
    private fun simpleList(): FlatVIntList<MyTestClass> = with (MyTestClass) {
        val array = FlatVIntList<MyTestClass>(10)
        for (i in 0..9)
            array.add(i, MyTestClass(100*(i+1)))
        return array
    }

    @Test
    fun any(): Unit = with (MyTestClass) {
        val array = simpleList()
        assertEquals(true, array.any {it.value < 800})
        assertEquals(false, array.any {it.value >= 10000})
    }

    @Test
    fun all(): Unit = with (MyTestClass) {
        val array = simpleList()
        assertEquals(false, array.all {it.value < 800})
        assertEquals(true, array.all {it.value >= 0})
    }

    @Test
    fun forEach(): Unit = with (MyTestClass) {
        val array = simpleList()
        var accumulator = 0
        array.forEach {accumulator += it.value }
        assertEquals(5500, accumulator)
    }

    @Test
    fun single(): Unit = with (MyTestClass) {
        val array = simpleList()
        assertTrue(400 == array.single {it.value == 400}.value)
        assertThrows(NoSuchElementException::class.java, { array.single {it.value >= 400} })
    }

    @Test
    fun contains(): Unit = with (MyTestClass) {
        val array = simpleList()
        assertEquals(true, array.contains(MyTestClass(800)))
        assertEquals(false, array.contains(MyTestClass(850)))
    }
    
    @Test
    fun component() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(MyTestClass(100), array.component1())
        assertEquals(MyTestClass(200), array.component2())
        assertEquals(MyTestClass(300), array.component3())
        assertEquals(MyTestClass(400), array.component4())
        assertEquals(MyTestClass(500), array.component5())
    }
}