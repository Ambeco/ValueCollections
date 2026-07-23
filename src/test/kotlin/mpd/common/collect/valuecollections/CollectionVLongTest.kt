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
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals


@JvmInline
value class PrimaryVLongTestClass(val value: Long): Comparable<PrimaryVLongTestClass> {
    override operator fun compareTo(other: PrimaryVLongTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<PrimaryVLongTestClass> {
        override inline fun fromLong(v: Long) = PrimaryVLongTestClass(v)
        override inline fun toLong(v: PrimaryVLongTestClass): Long = v.value
    }
}

@JvmInline
value class SecondaryVLongTestClass(val value: Short): Comparable<SecondaryVLongTestClass> {
    override operator fun compareTo(other: SecondaryVLongTestClass): Int = value.compareTo(other.value)
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<SecondaryVLongTestClass> {
        override inline fun fromLong(v: Long) = SecondaryVLongTestClass(v.toShort())
        override inline fun toLong(v: SecondaryVLongTestClass): Long = v.value.toLong()
    }
}

@JvmInline
value class MyIntTestClass(val value: Int): Comparable<MyIntTestClass> {
    override operator fun compareTo(other: MyIntTestClass): Int = value.compareTo(other.value)
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MyIntTestClass> {
        override inline fun fromInt(v: Int) = MyIntTestClass(v.toInt())
        override inline fun toInt(v: MyIntTestClass): Int = v.value
    }
}

class CollectionVLongTest {
    private fun simpleList(): ArrayListVLong<PrimaryVLongTestClass> = with (PrimaryVLongTestClass) {
        val array = ArrayListVLong<PrimaryVLongTestClass>(10)
        for (i in 0..9)
            array.add(i, PrimaryVLongTestClass(100L*(i+1)))
        return array
    }
    
    private fun trackLongAllocations(op: ()->Unit) {
        RecordingStream().use { stream ->
            stream.enable("jdk.ObjectAllocationSample")
            // 2. Filter events for primitive int arrays or Longeger objects
            stream.onEvent("jdk.ObjectAllocationSample", Consumer { event: RecordedEvent? ->
                val objectClass = event!!.getClass("objectClass").getName()
                if (objectClass == "I" || objectClass.endsWith("Long") || objectClass.endsWith("Longeger") || objectClass.contains("valuecollections")) {
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
    fun any(): Unit = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(true, array.any {it.value < 800})
        assertEquals(false, array.any {it.value >= 10000})
    }

    @Test
    fun all(): Unit = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(false, array.all {it.value < 800})
        assertEquals(true, array.all {it.value >= 0})
    }

    @Test
    fun forEach(): Unit = with (PrimaryVLongTestClass) {
        val array = simpleList()
        var accumulator = 0L
        array.forEach {accumulator += it.value }
        assertEquals(5500L, accumulator)
    }

    @Test
    fun single(): Unit = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertTrue(400L == array.single {it.value == 400L}.value)
        assertThrows(NoSuchElementException::class.java, { array.single {it.value >= 400} })
    }

    @Test
    fun contains(): Unit = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(true, array.contains(PrimaryVLongTestClass(800)))
        assertEquals(false, array.contains(PrimaryVLongTestClass(850)))
    }
    
    @Test
    fun component() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(PrimaryVLongTestClass(100), array.component1())
        assertEquals(PrimaryVLongTestClass(200), array.component2())
        assertEquals(PrimaryVLongTestClass(300), array.component3())
        assertEquals(PrimaryVLongTestClass(400), array.component4())
        assertEquals(PrimaryVLongTestClass(500), array.component5())
    }

    @Test
    fun forEachIndexed() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        var accumulator = 0L
        array.forEachIndexed { i, e -> accumulator += i * e.value }
        assertEquals((0..9).sumOf { it * 100L * (it + 1) }, accumulator)
    }

    @Test
    fun containsAll() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(true, array.containsAll(listOf(PrimaryVLongTestClass(100), PrimaryVLongTestClass(500))))
        assertEquals(false, array.containsAll(listOf(PrimaryVLongTestClass(100), PrimaryVLongTestClass(150))))
    }

    @Test
    fun singleOr() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(PrimaryVLongTestClass(400), array.singleOr({ it.value == 400L }, { PrimaryVLongTestClass(-1) }))
        assertEquals(PrimaryVLongTestClass(-1), array.singleOr({ it.value >= 400 }, { PrimaryVLongTestClass(-1) }))
    }

    @Test
    fun singleOrNull() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(PrimaryVLongTestClass(400), array.singleOrNull { it.value == 400L })
        assertEquals(null, array.singleOrNull { it.value >= 400 })
    }

    @Test
    fun find() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(PrimaryVLongTestClass(400), array.find { it.value == 400L })
        assertEquals(null, array.find { it.value == 450L })
    }

    @Test
    fun findOr() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(PrimaryVLongTestClass(400), array.findOr({ it.value == 400L }, { PrimaryVLongTestClass(-1) }))
        assertEquals(PrimaryVLongTestClass(-1), array.findOr({ it.value == 450L }, { PrimaryVLongTestClass(-1) }))
    }

    @Test
    fun findOrThrow() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(PrimaryVLongTestClass(400), array.findOrThrow { it.value == 400L })
        assertThrows(NoSuchElementException::class.java, { array.findOrThrow { it.value == 450L } })
    }

    @Test
    fun filter() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val result = array.filter { it.value >= 800 }
        assertEquals(vLongListOf(PrimaryVLongTestClass(800), PrimaryVLongTestClass(900), PrimaryVLongTestClass(1000)), result)
    }

    @Test
    fun filterNot() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val result = array.filterNot { it.value >= 300 }
        assertEquals(vLongListOf(PrimaryVLongTestClass(100), PrimaryVLongTestClass(200)), result)
    }

    @Test
    fun isEmptyIsNotEmpty() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(false, array.isEmpty())
        assertEquals(true, array.isNotEmpty())
        assertEquals(true, ArrayListVLong<PrimaryVLongTestClass>().isEmpty())
    }

    @Test
    fun toListGeneric() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals((1..10).map { PrimaryVLongTestClass(100L * it) }, array.toListGeneric())
    }

    @Test
    fun toSetAndMutableSet() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val set = array.toSet()
        assertEquals(10, set.size)
        assertEquals(true, set.contains(PrimaryVLongTestClass(500)))
    }

    @Test
    fun contentEquals() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val other = simpleList()
        assertEquals(true, array.contentEquals(other))
        other.add(PrimaryVLongTestClass(9999))
        assertEquals(false, array.contentEquals(other))
    }

    @Test
    fun distinct() = with (PrimaryVLongTestClass) {
        val array = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(1), PrimaryVLongTestClass(2))
        assertEquals(2, array.distinct().size)
    }

    @Test
    fun intersectSubtractUnion() = with (PrimaryVLongTestClass) {
        val a = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2))
        val b = vLongListOf(PrimaryVLongTestClass(2), PrimaryVLongTestClass(3))
        assertEquals(vLongSetOf(PrimaryVLongTestClass(2)), a intersect b)
        assertEquals(vLongSetOf(PrimaryVLongTestClass(1)), a subtract b)
        assertEquals(vLongSetOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2), PrimaryVLongTestClass(3)), a union b)
    }

    @Test
    fun countAndFold() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(10, array.count())
        assertEquals(3, array.count { it.value >= 800 })
        assertEquals(5500L, array.fold(0L) { acc, e -> acc + e.value })
    }

    @Test
    fun maxAndMin() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(PrimaryVLongTestClass(1000), array.max())
        assertEquals(PrimaryVLongTestClass(100), array.min())
        assertEquals(PrimaryVLongTestClass(1000), array.maxByOrNull { it.value })
        assertEquals(PrimaryVLongTestClass(100), array.minByOrNull { it.value })
        assertEquals(1000L, array.maxOf { it.value })
        assertEquals(100L, array.minOf { it.value })
    }

    @Test
    fun none() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(false, array.none())
        assertEquals(true, array.none { it.value > 10000 })
        assertEquals(false, array.none { it.value == 400L })
    }

    @Test
    fun sortedAndSortedDescending() = with (PrimaryVLongTestClass) {
        val array = vLongListOf(PrimaryVLongTestClass(300), PrimaryVLongTestClass(100), PrimaryVLongTestClass(200))
        assertEquals(vLongListOf(PrimaryVLongTestClass(100), PrimaryVLongTestClass(200), PrimaryVLongTestClass(300)), array.sorted())
        assertEquals(vLongListOf(PrimaryVLongTestClass(300), PrimaryVLongTestClass(200), PrimaryVLongTestClass(100)), array.sortedDescending())
    }

    @Test
    fun sumOf() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val result = array.sumOf(fun(e: PrimaryVLongTestClass): Long { return e.value })
        assertEquals(5500, result)
    }

    @Test
    fun partition() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val (above, below) = array.partition { it.value >= 600 }
        assertEquals(5, above.size)
        assertEquals(5, below.size)
    }

    @Test
    fun plusAndMinus() = with (PrimaryVLongTestClass) {
        val array = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2))
        assertEquals(vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2), PrimaryVLongTestClass(3)), (array + PrimaryVLongTestClass(3)))
        assertEquals(vLongListOf(PrimaryVLongTestClass(2)), (array - PrimaryVLongTestClass(1)))
    }

    @Test
    fun joinToString() = with (PrimaryVLongTestClass) {
        val array = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2))
        assertEquals("1, 2", array.joinToString { it.value.toString() })
    }

    @Test
    fun groupBy() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val groups = array.groupBy { it.value % 200 }
        assertEquals(2, groups.size)
        assertEquals(5, groups[0]!!.size)
    }

    @Test
    fun mapGeneric() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals((1..10).map { 100L * it }, array.mapGeneric { it.value })
    }

    @Test
    fun bitsLayer() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(true, array.anyBits { it > 900 } != array.NULL_VALUE)
        assertEquals(true, array.anyBits { it > 5000 } == array.NULL_VALUE)
        assertEquals(true, array.allBits { it > 0 })
        var accumulator = 0L
        array.forEachBits { accumulator += it }
        assertEquals(5500L, accumulator)
        assertEquals(400L, array.singleBits { it == 400L })
        assertEquals(400L, array.anyIndexedBits { i, e -> i == 3 && e == 400L })
        assertEquals(true, array.allIndexedBits { i, e -> e == 100L * (i + 1) })
        var indexedAccumulator = 0L
        array.forEachIndexedBits { i, e -> indexedAccumulator += i * e }
        assertEquals(array.findIndexedBits { i, _ -> i == 3 }, 400L)
        assertEquals(PrimaryVLongTestClass(400), array.fromLong<PrimaryVLongTestClass>(400))
        assertThrows(NoSuchElementException::class.java, { array.fromLong<PrimaryVLongTestClass>(array.NULL_VALUE) })
        assertEquals(PrimaryVLongTestClass(400), array.fromLongOr(400) { PrimaryVLongTestClass(-1) })
        assertEquals(PrimaryVLongTestClass(-1), array.fromLongOr(array.NULL_VALUE) { PrimaryVLongTestClass(-1) })
        assertEquals(PrimaryVLongTestClass(400), array.fromLongOrNull(400))
        assertEquals(null, array.fromLongOrNull(array.NULL_VALUE))
        val genericArray = array.toArrayGenericBits()
        assertEquals(10, genericArray.size)
        assertEquals(400L, genericArray[3])
    }

    @Test
    fun asCollectionGeneric() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val collection: Collection<PrimaryVLongTestClass> = array.asCollectionGeneric()
        assertEquals(10, collection.size)
        assertEquals(true, collection.contains(PrimaryVLongTestClass(500)))
    }

    @Test
    fun singleOrElseAndFindOrElse() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(PrimaryVLongTestClass(400), array.singleOrElse({ it.value == 400L }, PrimaryVLongTestClass(-1)))
        assertEquals(PrimaryVLongTestClass(-1), array.singleOrElse({ it.value >= 400 }, PrimaryVLongTestClass(-1)))
        assertEquals(PrimaryVLongTestClass(400), array.findOrElse({ it.value == 400L }, PrimaryVLongTestClass(-1)))
        assertEquals(PrimaryVLongTestClass(-1), array.findOrElse({ it.value == 450L }, PrimaryVLongTestClass(-1)))
    }

    @Test
    fun filterToAndFilterNotTo() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val destination = ArrayListVLong<PrimaryVLongTestClass>()
        array.filterTo(destination) { it.value >= 800 }
        assertEquals(vLongListOf(PrimaryVLongTestClass(800), PrimaryVLongTestClass(900), PrimaryVLongTestClass(1000)), destination)
        val notDestination = ArrayListVLong<PrimaryVLongTestClass>()
        array.filterNotTo(notDestination) { it.value >= 300 }
        assertEquals(vLongListOf(PrimaryVLongTestClass(100), PrimaryVLongTestClass(200)), notDestination)
    }

    @Test
    fun associateVariants() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val m1 = array.associateVLongLong { PairVLongLong.of(it, PrimaryVLongTestClass(it.value * 2)) }
        assertEquals(10, m1.size)
        val m2 = array.associateVLongLong({ it }, { PrimaryVLongTestClass(it.value * 2) })
        assertEquals(10, m2.size)
        val byKey1 = array.associateByVLongLong { PrimaryVLongTestClass(it.value / 100) }
        assertEquals(10, byKey1.size)
        val byKeyDest1 = HashMapVLongLong<PrimaryVLongTestClass, PrimaryVLongTestClass>()
        array.associateByVLongLongTo(byKeyDest1) { PrimaryVLongTestClass(it.value / 100) }
        assertEquals(10, byKeyDest1.size)
        val generic1 = array.associateGeneric { it to it.value }
        assertEquals(1000, generic1[PrimaryVLongTestClass(1000)])
        val generic2 = array.associateGeneric({ it.value }, { it })
        assertEquals(PrimaryVLongTestClass(1000), generic2[1000])
        val byGeneric = array.associateByGeneric { it.value }
        assertEquals(PrimaryVLongTestClass(500), byGeneric[500])
        val byGenericDest = HashMap<Long, PrimaryVLongTestClass>()
        array.associateByGenericTo(byGenericDest) { it.value }
        assertEquals(PrimaryVLongTestClass(500), byGenericDest[500])
        val destination = HashMap<PrimaryVLongTestClass, Long>()
        array.associateTo(destination) { it to it.value }
        assertEquals(1000, destination[PrimaryVLongTestClass(1000)])
    }

    @Test
    fun associateIntVariants() = with (PrimaryVLongTestClass) {
        with (MyIntTestClass) {
            val array = simpleList()
            val vLongInt = array.associateVLongInt { PairVLongInt.of(it, MyIntTestClass(it.value.toInt())) }
            assertEquals(10, vLongInt.size)
            val vIntLong = array.associateVIntLong { PairVIntLong.of(MyIntTestClass(it.value.toInt()), it) }
            assertEquals(10, vIntLong.size)
            val vIntInt = array.associateVIntInt { PairVIntInt.of(MyIntTestClass(it.value.toInt()), MyIntTestClass(it.value.toInt() * 2)) }
            assertEquals(10, vIntInt.size)
            val byVIntLong = array.associateByVIntLong { MyIntTestClass(it.value.toInt()) }
            assertEquals(10, byVIntLong.size)
            val byVIntLongDest = HashMapVIntLong<MyIntTestClass, PrimaryVLongTestClass>()
            array.associateByVIntLongTo(byVIntLongDest) { MyIntTestClass(it.value.toInt()) }
            assertEquals(10, byVIntLongDest.size)
        }
    }

    @Test
    fun toCollectionVariants() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val vDestination = array.toCollection(ArraySetVLong<PrimaryVLongTestClass>(20))
        assertEquals(10, vDestination.size)
        val genericDestination = array.toCollection(mutableListOf<PrimaryVLongTestClass>())
        assertEquals(10, genericDestination.size)
    }

    @Test
    fun toListAndSetVariants() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(10, array.toList().size)
        assertEquals(10, array.toMutableList().size)
        assertEquals(10, array.toMutableListGeneric().size)
        assertEquals(10, array.toMutableSet().size)
        assertEquals(10, array.toSetGeneric().size)
        assertEquals(10, array.toHashSet().size)
        assertEquals(10, array.asList().size)
        assertEquals(10, array.asListGeneric().size)
    }

    @Test
    fun arrayConversions() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val intArray = array.toLongArray()
        assertEquals(400, intArray[3])
        val vLongArray = array.toVLongArray()
        assertEquals(PrimaryVLongTestClass(400), vLongArray[3])
    }

    @Test
    fun asSequence() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(5500, array.asSequence().sumOf { it.value })
    }

    @Test
    fun distinctBy() = with (PrimaryVLongTestClass) {
        val array = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(101), PrimaryVLongTestClass(2))
        assertEquals(2, array.distinctBy { it.value % 100 }.size)
    }

    @Test
    fun onEachAndOnEachIndexed() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        var accumulator = 0L
        array.onEach { accumulator += it.value }
        assertEquals(5500L, accumulator)
        var indexedAccumulator = 0
        array.onEachIndexed { i, e -> indexedAccumulator += i }
        assertEquals((0..9).sum(), indexedAccumulator)
    }

    @Test
    fun withIndex() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val indexed: Collection<IndexedVLong<PrimaryVLongTestClass>> = array.withIndex()
        val first = indexed.first()
        assertEquals(0, first.index)
        assertEquals(PrimaryVLongTestClass(100), first.second)
    }

    @Test
    fun flatMapVariants() {
        context (PrimaryVLongTestClass, MyIntTestClass) {
            val array = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2))
            val flatVLong: ArrayListVLong<PrimaryVLongTestClass> = array.flatMap(fun(e: PrimaryVLongTestClass): CollectionVLong<PrimaryVLongTestClass> = vLongListOf(e, PrimaryVLongTestClass(e.value * 10)))
            assertEquals(listOf(1L, 10L, 2L, 20L), flatVLong.mapGeneric { it.value })
            val flatVLongTo: ArrayListVLong<PrimaryVLongTestClass> = array.flatMapTo(ArrayListVLong<PrimaryVLongTestClass>()) { e: PrimaryVLongTestClass -> vLongListOf(e) }
            assertEquals(2, flatVLongTo.size)
            val flatIndexedVLong: ArrayListVLong<PrimaryVLongTestClass> = array.flatMapIndexed(fun(i: Int, e: PrimaryVLongTestClass): CollectionVLong<PrimaryVLongTestClass> = vLongListOf(PrimaryVLongTestClass(i.toLong()), e))
            assertEquals(4, flatIndexedVLong.size)
            val flatIndexedVLongTo: ArrayListVLong<PrimaryVLongTestClass> = array.flatMapIndexedTo(ArrayListVLong<PrimaryVLongTestClass>()) { i: Int, e: PrimaryVLongTestClass -> vLongListOf(e) }
            assertEquals(2, flatIndexedVLongTo.size)
            val flatVInt: ArrayListVInt<MyIntTestClass> = array.flatMap(fun(e: PrimaryVLongTestClass): CollectionVInt<MyIntTestClass> = vIntListOf(MyIntTestClass(e.value.toInt())))
            assertEquals(listOf(1, 2), flatVInt.mapGeneric { it.value })
            val flatVIntTo: ArrayListVInt<MyIntTestClass> = array.flatMapTo(ArrayListVInt<MyIntTestClass>()) { e: PrimaryVLongTestClass -> vIntListOf(MyIntTestClass(e.value.toInt())) }
            assertEquals(2, flatVIntTo.size)
            val flatIndexedVInt: ArrayListVInt<MyIntTestClass> = array.flatMapIndexed(fun(i: Int, e: PrimaryVLongTestClass): CollectionVInt<MyIntTestClass> = vIntListOf(MyIntTestClass((i + e.value).toInt())))
            assertEquals(2, flatIndexedVInt.size)
            val flatIndexedVIntTo: ArrayListVInt<MyIntTestClass> = array.flatMapIndexedTo(ArrayListVInt<MyIntTestClass>()) { i: Int, e: PrimaryVLongTestClass -> vIntListOf(MyIntTestClass(e.value.toInt())) }
            assertEquals(2, flatIndexedVIntTo.size)
        }
    }

    @Test
    fun groupByTo() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val destination = HashMap<Long, MutableListVLong<PrimaryVLongTestClass>>()
        array.groupByTo(destination) { it.value % 2 }
        assertEquals(1, destination.size)
    }

    @Test
    fun mapVariants() = context (PrimaryVLongTestClass, MyIntTestClass) {
        run {
            val array = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2))
            assertEquals(listOf(2L, 4L), array.mapVLong { PrimaryVLongTestClass(it.value * 2) }.mapGeneric { it.value })
            assertEquals(listOf(1, 2), array.mapVInt { MyIntTestClass(it.value.toInt()) }.mapGeneric { it.value })
            assertEquals(listOf(0L, 2L), array.mapIndexedVLong { i, e -> PrimaryVLongTestClass(i * e.value) }.mapGeneric { it.value })
            assertEquals(listOf(0, 2), array.mapIndexedVInt { i, e -> MyIntTestClass((i * e.value).toInt()) }.mapGeneric { it.value })
            assertEquals(listOf(0L, 2L), array.mapIndexedGeneric { i, e -> i * e.value })
            assertEquals(vLongListOf(PrimaryVLongTestClass(2)), array.mapIndexedVLongNotNull { i, e -> if (i == 1) e else null })
            assertEquals(vIntListOf(MyIntTestClass(2)), array.mapIndexedVIntNotNull { i, e -> if (i == 1) MyIntTestClass(e.value.toInt()) else null })
            assertEquals(listOf(2L), array.mapIndexedGenericNotNull { i, e -> if (i == 1) e.value else null })
            assertEquals(listOf(2L), array.mapNotNull { if (it.value == 2L) it.value else null })
            val mapNotNullDest = mutableListOf<Long>()
            array.mapNotNullTo(mapNotNullDest) { if (it.value == 2L) it.value else null }
            assertEquals(listOf(2L), mapNotNullDest)
        }
    }


    @Test
    fun comparatorVariants() = with (PrimaryVLongTestClass) {
        val array = vLongListOf(PrimaryVLongTestClass(300), PrimaryVLongTestClass(100), PrimaryVLongTestClass(200))
        val comparator = Comparator<PrimaryVLongTestClass> { l, r -> l.value.compareTo(r.value) }
        assertEquals(PrimaryVLongTestClass(300), array.maxWith(comparator))
        assertEquals(PrimaryVLongTestClass(300), array.maxWithOrNull(comparator))
        assertEquals(PrimaryVLongTestClass(100), array.minWith(comparator))
        assertEquals(PrimaryVLongTestClass(100), array.minWithOrNull(comparator))
        assertEquals(300L, array.maxOfWith(Comparator { l: Long, r: Long -> l.compareTo(r) }) { it.value })
        assertEquals(300L, array.maxOfWithOrNull(Comparator { l: Long, r: Long -> l.compareTo(r) }) { it.value })
        assertEquals(100L, array.minOfWith(Comparator { l: Long, r: Long -> l.compareTo(r) }) { it.value })
        assertEquals(100L, array.minOfWithOrNull(Comparator { l: Long, r: Long -> l.compareTo(r) }) { it.value })
        assertEquals(300L, array.maxOfOrNull { it.value })
        assertEquals(100L, array.minOfOrNull { it.value })
        assertEquals(PrimaryVLongTestClass(300), array.sortedWith(comparator).let { it[it.size - 1] })
    }

    /*
    TODO
    @Test
    fun sortedVariants() = with (PrimaryVLongTestClass) {
        val array = vLongListOf(PrimaryVLongTestClass(300), PrimaryVLongTestClass(100), PrimaryVLongTestClass(200))
        assertEquals(PrimaryVLongTestClass(100), array.sortedArray()[0])
        assertEquals(PrimaryVLongTestClass(300), array.sortedArrayDescending()[0])
        assertEquals(vLongListOf(PrimaryVLongTestClass(100), PrimaryVLongTestClass(200), PrimaryVLongTestClass(300)), array.sortedBy { it.value })
        assertEquals(vLongListOf(PrimaryVLongTestClass(300), PrimaryVLongTestClass(200), PrimaryVLongTestClass(100)), array.sortedByDescending { it.value })
    }
    */

    @Test
    fun sumVariants() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(5500, array.sumBy { it.value.toInt() })
        assertEquals(5500.0, array.sumByDouble { it.value.toDouble() })
        val sumDouble = array.sumOf(fun(e: PrimaryVLongTestClass): Double { return e.value.toDouble() })
        assertEquals(5500.0, sumDouble)
        val sumLong = array.sumOf(fun(e: PrimaryVLongTestClass): Long { return e.value })
        assertEquals(5500L, sumLong)
        assertEquals(5500uL, array.sumOfULong { it.value.toULong() })
        assertEquals(5500u, array.sumOfUInt { it.value.toUInt() })
    }

    @Test
    fun chunked() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        val chunks = array.chunked(4)
        assertEquals(3, chunks.size)
        assertEquals(4, chunks[0].size)
        val sums = array.chunked(4) { chunk -> chunk.sumOf(fun(e: PrimaryVLongTestClass): Long { return e.value }) }
        assertEquals(3, sums.size)
    }

    @Test
    fun plusMinusElementVariants() = with (PrimaryVLongTestClass) {
        val array = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2))
        assertEquals(vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2), PrimaryVLongTestClass(3)), array.plusElement(PrimaryVLongTestClass(3)))
        assertEquals(vLongListOf(PrimaryVLongTestClass(1)), array.minusElement(PrimaryVLongTestClass(2)))
        val otherVLong = vLongListOf(PrimaryVLongTestClass(3))
        assertEquals(vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2), PrimaryVLongTestClass(3)), (array + otherVLong))
        assertEquals(vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2), PrimaryVLongTestClass(3)), (array + listOf(PrimaryVLongTestClass(3))))
        assertEquals(vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2), PrimaryVLongTestClass(3)), (array + arrayOf(PrimaryVLongTestClass(3))))
        assertEquals(vLongListOf(PrimaryVLongTestClass(1)), (array - arrayOf(PrimaryVLongTestClass(2))))
        assertEquals(vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2)), (array - otherVLong))
        assertEquals(vLongListOf(PrimaryVLongTestClass(1)), (array - listOf(PrimaryVLongTestClass(2))))
        assertEquals(vLongListOf(PrimaryVLongTestClass(1)), (array - sequenceOf(PrimaryVLongTestClass(2))))
    }

    @Test
    fun randomVariants() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(true, array.contains(array.random()))
        assertEquals(true, array.contains(array.random(Random(42))))
        assertEquals(true, array.contains(array.randomOrNull()!!))
        assertEquals(null, ArrayListVLong<PrimaryVLongTestClass>().randomOrNull())
    }

    @Test
    fun zipVariants() = with (PrimaryVLongTestClass) {
        val array = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2))
        val zippedArray = array.zip(arrayOf("a", "b"))
        assertEquals(listOf(PrimaryVLongTestClass(1) to "a", PrimaryVLongTestClass(2) to "b"), zippedArray)
        val zippedTransform = array.zip(arrayOf(10, 20)) { a, b -> a.value + b }
        assertEquals(listOf(11L, 22L), zippedTransform)
        val other = vLongListOf(PrimaryVLongTestClass(10), PrimaryVLongTestClass(20))
        val zippedPair = array.zipPairVLongLong(other) { a, b -> PrimaryVLongTestClass(a.value + b.value) }
        assertEquals(PrimaryVLongTestClass(11), zippedPair[0])
        assertEquals(PrimaryVLongTestClass(22), zippedPair[1])
    }

    @Test
    fun joinToAndToVString() = with (PrimaryVLongTestClass) {
        val array = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2))
        val builder = StringBuilder()
        array.joinTo(builder, transform = { it.value.toString() })
        assertEquals("1, 2", builder.toString())
        assertEquals("{1, 2}", array.toStringV())
    }

    @Test
    fun mapReduceAndReduceVariants() = with (PrimaryVLongTestClass) {
        val array = simpleList()
        assertEquals(1000, array.mapReduce({ it.value }, { max, e -> if (e > max) e else max }))
        assertEquals(1000, array.mapReduceIndexed({ it.value }) { _, max, e -> if (e > max) e else max })
        assertEquals(PrimaryVLongTestClass(1000), array.reduce { acc, e -> if (e.value > acc.value) e else acc })
        assertEquals(PrimaryVLongTestClass(1000), array.reduceIndexed { _, acc, e -> if (e.value > acc.value) e else acc })
        assertEquals(PrimaryVLongTestClass(1000), array.reduceIndexedOrNull { _, acc, e -> if (e.value > acc.value) e else acc })
        assertEquals(PrimaryVLongTestClass(1000), array.reduceOrNull { acc, e -> if (e.value > acc.value) e else acc })
        assertEquals(null, ArrayListVLong<PrimaryVLongTestClass>().reduceOrNull { acc, e -> if (e.value > acc.value) e else acc })
    }

    @Test
    fun runningFoldVariants() = with (PrimaryVLongTestClass) {
        with (MyIntTestClass) {
            val array = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2), PrimaryVLongTestClass(3))
            assertEquals(listOf(1L, 3L, 6L), array.runningFoldVLong(PrimaryVLongTestClass(0)) { acc, e -> PrimaryVLongTestClass(acc.value + e.value) }.mapGeneric { it.value })
            assertEquals(listOf(1L, 3L, 6L), array.runningFoldVLongIndexed(PrimaryVLongTestClass(0)) { _, acc, e -> PrimaryVLongTestClass(acc.value + e.value) }.mapGeneric { it.value })
            assertEquals(listOf(1, 3, 6), array.runningFoldVInt(MyIntTestClass(0)) { acc, e -> MyIntTestClass(acc.value + e.value.toInt()) }.mapGeneric { it.value })
            assertEquals(listOf(1, 3, 6), array.runningFoldVIntIndexed(MyIntTestClass(0)) { _, acc, e -> MyIntTestClass(acc.value + e.value.toInt()) }.mapGeneric { it.value })
            assertEquals(listOf(1L, 3L, 6L), array.runningFoldGeneric(0L) { acc, e -> acc + e.value })
            assertEquals(listOf(1L, 3L, 6L), array.runningFoldGenericIndexed(0L) { _, acc, e -> acc + e.value })
        }
    }

    @Test
    fun runningReduceVariants() = with (PrimaryVLongTestClass) {
        val array = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2), PrimaryVLongTestClass(3))
        assertEquals(vLongListOf(PrimaryVLongTestClass(3), PrimaryVLongTestClass(6)), array.runningReduceVLong { acc, e -> PrimaryVLongTestClass(acc.value + e.value) })
        assertEquals(vLongListOf(PrimaryVLongTestClass(3), PrimaryVLongTestClass(6)), array.runningReduceVLongIndexed { _, acc, e -> PrimaryVLongTestClass(acc.value + e.value) })
        assertEquals(listOf(3L, 6L), array.runningReduceGeneric<PrimaryVLongTestClass, PrimaryVLongTestClass> { acc, e -> PrimaryVLongTestClass(acc.value + e.value) }.map { it.value })
        assertEquals(listOf(3L, 6L), array.runningReduceGenericIndexed<PrimaryVLongTestClass, PrimaryVLongTestClass> { _, acc, e -> PrimaryVLongTestClass(acc.value + e.value) }.map { it.value })
        val longAdapterForPrimaryVLongTestClass = object : ValueIntAdapter<PrimaryVLongTestClass> {
            override fun fromInt(v: Int) = PrimaryVLongTestClass(v.toLong())
            override fun toInt(v: PrimaryVLongTestClass): Int = v.value.toInt()
        }
        context (longAdapterForPrimaryVLongTestClass) {
            assertEquals(vIntListOf(PrimaryVLongTestClass(3), PrimaryVLongTestClass(6)), array.runningReduceVInt { acc, e -> PrimaryVLongTestClass(acc.value + e.value) })
            assertEquals(vIntListOf(PrimaryVLongTestClass(3), PrimaryVLongTestClass(6)), array.runningReduceVIntIndexed { _, acc, e -> PrimaryVLongTestClass(acc.value + e.value) })
        }
    }

    @Test
    fun scanVariants() = with (PrimaryVLongTestClass) {
        val array = vLongListOf(PrimaryVLongTestClass(1), PrimaryVLongTestClass(2), PrimaryVLongTestClass(3))
        assertEquals(listOf(1L, 3L, 6L), array.scan(0L) { acc, e -> acc + e.value })
        assertEquals(listOf(1L, 3L, 6L), array.scanIndexed(0L) { _, acc, e -> acc + e.value })
    }
}