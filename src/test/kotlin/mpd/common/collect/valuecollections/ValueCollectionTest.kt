package mpd.common.collect.valuecollections

import mpd.com.common.collect.valuecollections.VIntArray
import mpd.com.common.collect.valuecollections.ValueIntAdapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@JvmInline
value class MyTestClass(val value: Int) {
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MyTestClass> {
        override inline fun fromInt(v: Int) = MyTestClass(v)
        override inline fun toInt(v: MyTestClass): Int = v.value
    }
}

class ValueCollectionTest {
    @Test
    fun emailValidator_CorrectEmailSimple_ReturnsTrue() = with (MyTestClass) {
        val array = VIntArray<MyTestClass>(4)
        array[0] = MyTestClass(4)
        array[10] = MyTestClass(-3)
        assertEquals(MyTestClass(-3), array[10])
    }
}