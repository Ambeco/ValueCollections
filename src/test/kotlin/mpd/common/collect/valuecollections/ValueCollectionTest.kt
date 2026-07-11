package mpd.common.collect.valuecollections

import com.sun.management.HotSpotDiagnosticMXBean
import jdk.jfr.consumer.RecordedEvent
import jdk.jfr.consumer.RecordingStream
import mpd.com.common.collect.valuecollections.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import java.lang.management.ManagementFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Consumer
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
    private fun simpleList(): ArrayVIntList<MyTestClass> = with (MyTestClass) {
        val array = ArrayVIntList<MyTestClass>(10)
        for (i in 0..9)
            array.add(i, MyTestClass(100*(i+1)))
        return array
    }
    
    private fun trackIntAllocations(op: ()->Unit) {
        RecordingStream().use { stream ->
            stream.enable("jdk.ObjectAllocationSample")
            // 2. Filter events for primitive int arrays or Integer objects
            stream.onEvent("jdk.ObjectAllocationSample", Consumer { event: RecordedEvent? ->
                val objectClass = event!!.getClass("objectClass").getName()
                if (objectClass == "I" || objectClass.endsWith("Int") || objectClass.endsWith("Integer") || objectClass.contains("valuecollections")) {
                    val stack = event.stackTrace.frames.joinToString(transform= { "\n  at ${it.method.getClass("type").name}#${it.method.name}(${it.lineNumber}) [${it.type}]" })
                    println("allocated $objectClass at $stack")
                }
            })
            stream.startAsync()
            op()
            Thread.sleep(1000)
            stream.stop()
        }
    }

    private fun heap_dump() {
        val hotspotMBean = ManagementFactory.newPlatformMXBeanProxy<HotSpotDiagnosticMXBean?>(
            ManagementFactory.getPlatformMBeanServer(),
            "com.sun.management:type=HotSpotDiagnostic",
            HotSpotDiagnosticMXBean::class.java
        )!!
        val filename = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".hprof"
        hotspotMBean.dumpHeap(filename, false)
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