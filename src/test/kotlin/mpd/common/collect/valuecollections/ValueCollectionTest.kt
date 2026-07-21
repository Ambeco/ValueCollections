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
value class MyTestClass(val value: Int): Comparable<MyTestClass> {
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<MyTestClass> {
        override inline fun fromInt(v: Int) = MyTestClass(v)
        override inline fun toInt(v: MyTestClass): Int = v.value
    }
}

@JvmInline
value class SecondTestClass(val value: Short): Comparable<SecondTestClass> {
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveIntAdapter: ValueIntAdapter<SecondTestClass> {
        override inline fun fromInt(v: Int) = SecondTestClass(v.toShort())
        override inline fun toInt(v: SecondTestClass): Int = v.value.toInt()
    }
}

@JvmInline
value class MyLongTestClass(val value: Long): Comparable<MyLongTestClass> {
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<MyLongTestClass> {
        override inline fun fromLong(v: Long) = MyLongTestClass(v.toLong())
        override inline fun toLong(v: MyLongTestClass): Long = v.value
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

    @Test
    fun forEachIndexed() = with (MyTestClass) {
        val array = simpleList()
        var accumulator = 0
        array.forEachIndexed { i, e -> accumulator += i * e.value }
        assertEquals((0..9).sumOf { it * 100 * (it + 1) }, accumulator)
    }

    @Test
    fun containsAll() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(true, array.containsAll(listOf(MyTestClass(100), MyTestClass(500))))
        assertEquals(false, array.containsAll(listOf(MyTestClass(100), MyTestClass(150))))
    }

    @Test
    fun singleOr() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(MyTestClass(400), array.singleOr({ it.value == 400 }, { MyTestClass(-1) }))
        assertEquals(MyTestClass(-1), array.singleOr({ it.value >= 400 }, { MyTestClass(-1) }))
    }

    @Test
    fun singleOrNull() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(MyTestClass(400), array.singleOrNull { it.value == 400 })
        assertEquals(null, array.singleOrNull { it.value >= 400 })
    }

    @Test
    fun find() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(MyTestClass(400), array.find { it.value == 400 })
        assertEquals(null, array.find { it.value == 450 })
    }

    @Test
    fun findOr() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(MyTestClass(400), array.findOr({ it.value == 400 }, { MyTestClass(-1) }))
        assertEquals(MyTestClass(-1), array.findOr({ it.value == 450 }, { MyTestClass(-1) }))
    }

    @Test
    fun findOrThrow() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(MyTestClass(400), array.findOrThrow { it.value == 400 })
        assertThrows(NoSuchElementException::class.java, { array.findOrThrow { it.value == 450 } })
    }

    @Test
    fun filter() = with (MyTestClass) {
        val array = simpleList()
        val result = array.filter { it.value >= 800 }
        assertEquals(listOf(MyTestClass(800), MyTestClass(900), MyTestClass(1000)), result.toListGeneric())
    }

    @Test
    fun filterNot() = with (MyTestClass) {
        val array = simpleList()
        val result = array.filterNot { it.value >= 300 }
        assertEquals(listOf(MyTestClass(100), MyTestClass(200)), result.toListGeneric())
    }

    @Test
    fun isEmptyIsNotEmpty() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(false, array.isEmpty())
        assertEquals(true, array.isNotEmpty())
        assertEquals(true, ArrayVIntList<MyTestClass>().isEmpty())
    }

    @Test
    fun toListGeneric() = with (MyTestClass) {
        val array = simpleList()
        assertEquals((1..10).map { MyTestClass(100 * it) }, array.toListGeneric())
    }

    @Test
    fun toSetAndMutableSet() = with (MyTestClass) {
        val array = simpleList()
        val set = array.toSet()
        assertEquals(10, set.size)
        assertEquals(true, set.contains(MyTestClass(500)))
    }

    @Test
    fun contentEquals() = with (MyTestClass) {
        val array = simpleList()
        val other = simpleList()
        assertEquals(true, array.contentEquals(other))
        other.add(MyTestClass(9999))
        assertEquals(false, array.contentEquals(other))
    }

    @Test
    fun distinct() = with (MyTestClass) {
        val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(1)); it.add(MyTestClass(2)) }
        assertEquals(2, array.distinct().size)
    }

    @Test
    fun intersectSubtractUnion() = with (MyTestClass) {
        val a = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(2)) }
        val b = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(2)); it.add(MyTestClass(3)) }
        assertEquals(setOf(MyTestClass(2)), (a intersect b).toSetGeneric())
        assertEquals(setOf(MyTestClass(1)), (a subtract b).toSetGeneric())
        assertEquals(setOf(MyTestClass(1), MyTestClass(2), MyTestClass(3)), (a union b).toSetGeneric())
    }

    @Test
    fun countAndFold() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(10, array.count())
        assertEquals(3, array.count { it.value >= 800 })
        assertEquals(5500, array.fold(0) { acc, e -> acc + e.value })
    }

    @Test
    fun maxAndMin() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(MyTestClass(1000), array.max())
        assertEquals(MyTestClass(100), array.min())
        assertEquals(MyTestClass(1000), array.maxByOrNull { it.value })
        assertEquals(MyTestClass(100), array.minByOrNull { it.value })
        assertEquals(1000, array.maxOf { it.value })
        assertEquals(100, array.minOf { it.value })
    }

    @Test
    fun none() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(false, array.none())
        assertEquals(true, array.none { it.value > 10000 })
        assertEquals(false, array.none { it.value == 400 })
    }

    @Test
    fun sortedAndSortedDescending() = with (MyTestClass) {
        val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(300)); it.add(MyTestClass(100)); it.add(MyTestClass(200)) }
        assertEquals(listOf(MyTestClass(100), MyTestClass(200), MyTestClass(300)), array.sorted().toListGeneric())
        assertEquals(listOf(MyTestClass(300), MyTestClass(200), MyTestClass(100)), array.sortedDescending().toListGeneric())
    }

    @Test
    fun sumOf() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(5500, array.sumOf { it.value })
    }

    @Test
    fun partition() = with (MyTestClass) {
        val array = simpleList()
        val (above, below) = array.partition { it.value >= 600 }
        assertEquals(5, above.size)
        assertEquals(5, below.size)
    }

    @Test
    fun plusAndMinus() = with (MyTestClass) {
        val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(2)) }
        assertEquals(listOf(MyTestClass(1), MyTestClass(2), MyTestClass(3)), (array + MyTestClass(3)).toListGeneric())
        assertEquals(listOf(MyTestClass(2)), (array - MyTestClass(1)).toListGeneric())
    }

    @Test
    fun joinToString() = with (MyTestClass) {
        val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(2)) }
        assertEquals("1, 2", array.joinToString { it.value.toString() })
    }

    @Test
    fun groupBy() = with (MyTestClass) {
        val array = simpleList()
        val groups = array.groupBy { it.value % 200 }
        assertEquals(5, groups.size)
        assertEquals(2, groups[0]!!.size)
    }

    @Test
    fun mapGeneric() = with (MyTestClass) {
        val array = simpleList()
        assertEquals((1..10).map { 100 * it }, array.mapGeneric { it.value })
    }

    @Test
    fun bitsLayer() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(true, array.anyBits { it > 900 })
        assertEquals(false, array.anyBits { it > 5000 })
        assertEquals(true, array.allBits { it > 0 })
        var accumulator = 0
        array.forEachBits { accumulator += it }
        assertEquals(5500, accumulator)
        assertEquals(400, array.singleBits { it == 400 })
        assertEquals(true, array.anyIndexedBits { i, e -> i == 3 && e == 400 })
        assertEquals(true, array.allIndexedBits { i, e -> e == 100 * (i + 1) })
        var indexedAccumulator = 0
        array.forEachIndexedBits { i, e -> indexedAccumulator += i * e }
        assertEquals(array.findIndexedBits { i, _ -> i == 3 }, 400)
        assertEquals(400, array.fromInt(400))
        assertThrows(NoSuchElementException::class.java, { array.fromInt(array.NULL_VALUE) })
        assertEquals(MyTestClass(400), array.fromIntOr(400) { MyTestClass(-1) })
        assertEquals(MyTestClass(-1), array.fromIntOr(array.NULL_VALUE) { MyTestClass(-1) })
        assertEquals(MyTestClass(400), array.fromIntOrNull(400))
        assertEquals(null, array.fromIntOrNull(array.NULL_VALUE))
        val genericArray = array.toArrayGenericBits()
        assertEquals(10, genericArray.size)
        assertEquals(400, genericArray[3])
    }

    @Test
    fun asCollectionGeneric() = with (MyTestClass) {
        val array = simpleList()
        val collection: Collection<MyTestClass> = array.asCollectionGeneric()
        assertEquals(10, collection.size)
        assertEquals(true, collection.contains(MyTestClass(500)))
    }

    @Test
    fun singleOrElseAndFindOrElse() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(MyTestClass(400), array.singleOrElse({ it.value == 400 }, MyTestClass(-1)))
        assertEquals(MyTestClass(-1), array.singleOrElse({ it.value >= 400 }, MyTestClass(-1)))
        assertEquals(MyTestClass(400), array.findOrElse({ it.value == 400 }, MyTestClass(-1)))
        assertEquals(MyTestClass(-1), array.findOrElse({ it.value == 450 }, MyTestClass(-1)))
    }

    @Test
    fun filterToAndFilterNotTo() = with (MyTestClass) {
        val array = simpleList()
        val destination = ArrayVIntList<MyTestClass>()
        array.filterTo(destination) { it.value >= 800 }
        assertEquals(listOf(MyTestClass(800), MyTestClass(900), MyTestClass(1000)), destination.toListGeneric())
        val notDestination = ArrayVIntList<MyTestClass>()
        array.filterNotTo(notDestination) { it.value >= 300 }
        assertEquals(listOf(MyTestClass(100), MyTestClass(200)), notDestination.toListGeneric())
    }

    @Test
    fun associateVariants() = with (MyTestClass) {
        val array = simpleList()
        val m1 = array.associateVIntInt { PairVIntInt.of(it, MyTestClass(it.value * 2)) }
        assertEquals(10, m1.size)
        val m2 = array.associateVIntInt({ it }, { MyTestClass(it.value * 2) })
        assertEquals(10, m2.size)
        val byKey1 = array.associateByVIntInt { MyTestClass(it.value / 100) }
        assertEquals(10, byKey1.size)
        val byKeyDest1 = HashMapVIntInt<MyTestClass, MyTestClass>()
        array.associateByVIntIntTo(byKeyDest1) { MyTestClass(it.value / 100) }
        assertEquals(10, byKeyDest1.size)
        val generic1 = array.associateGeneric { it to it.value }
        assertEquals(1000, generic1[MyTestClass(1000)])
        val generic2 = array.associateGeneric({ it.value }, { it })
        assertEquals(MyTestClass(1000), generic2[1000])
        val byGeneric = array.associateByGeneric { it.value }
        assertEquals(MyTestClass(500), byGeneric[500])
        val byGenericDest = HashMap<Int, MyTestClass>()
        array.associateByGenericTo(byGenericDest) { it.value }
        assertEquals(MyTestClass(500), byGenericDest[500])
        val destination = HashMap<MyTestClass, Int>()
        array.associateTo(destination) { it to it.value }
        assertEquals(1000, destination[MyTestClass(1000)])
    }

    @Test
    fun associateLongVariants() = with (MyTestClass) {
        with (MyLongTestClass) {
            val array = simpleList()
            val vIntLong = array.associateVIntLong { PairVIntLong.of(it, MyLongTestClass(it.value.toLong())) }
            assertEquals(10, vIntLong.size)
            val vLongInt = array.associateVLongInt { PairVLongInt.of(MyLongTestClass(it.value.toLong()), it) }
            assertEquals(10, vLongInt.size)
            val vLongLong = array.associateVLongLong { PairVLongLong.of(MyLongTestClass(it.value.toLong()), MyLongTestClass(it.value.toLong() * 2)) }
            assertEquals(10, vLongLong.size)
            val byVLongInt = array.associateByVLongInt { MyLongTestClass(it.value.toLong()) }
            assertEquals(10, byVLongInt.size)
            val byVLongIntDest = HashMapVLongInt<MyLongTestClass, MyTestClass>()
            array.associateByVLongIntTo(byVLongIntDest) { MyLongTestClass(it.value.toLong()) }
            assertEquals(10, byVLongIntDest.size)
        }
    }

    @Test
    fun toCollectionVariants() = with (MyTestClass) {
        val array = simpleList()
        val vDestination = array.toCollection(ArraySetVInt<MyTestClass>(20))
        assertEquals(10, vDestination.size)
        val genericDestination = array.toCollection(mutableListOf<MyTestClass>())
        assertEquals(10, genericDestination.size)
    }

    @Test
    fun toListAndSetVariants() = with (MyTestClass) {
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
    fun arrayConversions() = with (MyTestClass) {
        val array = simpleList()
        val intArray = array.toIntArray()
        assertEquals(400, intArray[3])
        val vIntArray = array.toVIntArray()
        assertEquals(MyTestClass(400), vIntArray[3])
    }

    @Test
    fun asSequence() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(5500, array.asSequence().sumOf { it.value })
    }

    @Test
    fun distinctBy() = with (MyTestClass) {
        val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(101)); it.add(MyTestClass(2)) }
        assertEquals(2, array.distinctBy { it.value % 100 }.size)
    }

    @Test
    fun onEachAndOnEachIndexed() = with (MyTestClass) {
        val array = simpleList()
        var accumulator = 0
        array.onEach { accumulator += it.value }
        assertEquals(5500, accumulator)
        var indexedAccumulator = 0
        array.onEachIndexed { i, e -> indexedAccumulator += i }
        assertEquals((0..9).sum(), indexedAccumulator)
    }

    @Test
    fun withIndex() = with (MyTestClass) {
        val array = simpleList()
        with (IndexedVInt.VLongAdapter<MyTestClass>()) {
            val indexed = array.withIndex().toListGeneric()
            assertEquals(0, indexed[0].index)
            assertEquals(MyTestClass(100), indexed[0].value)
        }
    }

    @Test
    fun flatMapVariants() = with (MyTestClass) {
        with (MyLongTestClass) {
            val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(2)) }
            val flatVInt = array.flatMap { e -> ArrayVIntList<MyTestClass>().also { it.add(e); it.add(MyTestClass(e.value * 10)) } }
            assertEquals(listOf(1, 10, 2, 20), flatVInt.toListGeneric().map { it.value })
            val flatVLong = array.flatMap { e -> ArrayVLongList<MyLongTestClass>().also { it.add(MyLongTestClass(e.value.toLong())) } }
            assertEquals(listOf(1L, 2L), flatVLong.toListGeneric().map { it.value })
            val flatVIntTo = array.flatMapTo(ArrayVIntList<MyTestClass>()) { e -> ArrayVIntList<MyTestClass>().also { it.add(e) } }
            assertEquals(2, flatVIntTo.size)
            val flatVLongTo = array.flatMapTo(ArrayVLongList<MyLongTestClass>()) { e -> ArrayVLongList<MyLongTestClass>().also { it.add(MyLongTestClass(e.value.toLong())) } }
            assertEquals(2, flatVLongTo.size)
            val flatIndexedVInt = array.flatMapIndexed { i, e -> ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(i)); it.add(e) } }
            assertEquals(4, flatIndexedVInt.size)
            val flatIndexedVLong = array.flatMapIndexed { i, e -> ArrayVLongList<MyLongTestClass>().also { it.add(MyLongTestClass((i + e.value).toLong())) } }
            assertEquals(2, flatIndexedVLong.size)
            val flatIndexedVIntTo = array.flatMapIndexedTo(ArrayVIntList<MyTestClass>()) { i, e -> ArrayVIntList<MyTestClass>().also { it.add(e) } }
            assertEquals(2, flatIndexedVIntTo.size)
            val flatIndexedVLongTo = array.flatMapIndexedTo(ArrayVLongList<MyLongTestClass>()) { i, e -> ArrayVLongList<MyLongTestClass>().also { it.add(MyLongTestClass(e.value.toLong())) } }
            assertEquals(2, flatIndexedVLongTo.size)
        }
    }

    @Test
    fun groupByTo() = with (MyTestClass) {
        val array = simpleList()
        val destination = HashMap<Int, MutableVIntList<MyTestClass>>()
        array.groupByTo(destination) { it.value % 200 }
        assertEquals(5, destination.size)
    }

    @Test
    fun mapVariants() = with (MyTestClass) {
        with (MyLongTestClass) {
            val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(2)) }
            assertEquals(listOf(2, 4), array.mapVInt { MyTestClass(it.value * 2) }.toListGeneric().map { it.value })
            assertEquals(listOf(1L, 2L), array.mapVLong { MyLongTestClass(it.value.toLong()) }.toListGeneric().map { it.value })
            assertEquals(listOf(0, 3), array.mapIndexedVInt { i, e -> MyTestClass(i * e.value) }.toListGeneric().map { it.value })
            assertEquals(listOf(0L, 3L), array.mapIndexedVLong { i, e -> MyLongTestClass((i * e.value).toLong()) }.toListGeneric().map { it.value })
            assertEquals(listOf(0, 3), array.mapIndexedGeneric { i, e -> i * e.value })
            assertEquals(listOf(MyTestClass(2)), array.mapIndexedVIntNotNull { i, e -> if (i == 1) e else null }.toListGeneric())
            assertEquals(listOf(MyLongTestClass(2L)), array.mapIndexedVLongNotNull { i, e -> if (i == 1) MyLongTestClass(e.value.toLong()) else null }.toListGeneric())
            assertEquals(listOf(2), array.mapIndexedGenericNotNull { i, e -> if (i == 1) e.value else null })
            assertEquals(listOf(2), array.mapNotNull { if (it.value == 2) it.value else null })
            val mapNotNullDest = mutableListOf<Int>()
            array.mapNotNullTo(mapNotNullDest) { if (it.value == 2) it.value else null }
            assertEquals(listOf(2), mapNotNullDest)
        }
    }

    @Test
    fun comparatorVariants() = with (MyTestClass) {
        val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(300)); it.add(MyTestClass(100)); it.add(MyTestClass(200)) }
        val comparator = Comparator<MyTestClass> { l, r -> l.value - r.value }
        assertEquals(MyTestClass(300), array.maxWith(comparator))
        assertEquals(MyTestClass(300), array.maxWithOrNull(comparator))
        assertEquals(MyTestClass(100), array.minWith(comparator))
        assertEquals(MyTestClass(100), array.minWithOrNull(comparator))
        assertEquals(300, array.maxOfWith(Comparator { l: Int, r: Int -> l - r }) { it.value })
        assertEquals(300, array.maxOfWithOrNull(Comparator { l: Int, r: Int -> l - r }) { it.value })
        assertEquals(100, array.minOfWith(Comparator { l: Int, r: Int -> l - r }) { it.value })
        assertEquals(100, array.minOfWithOrNull(Comparator { l: Int, r: Int -> l - r }) { it.value })
        assertEquals(300, array.maxOfOrNull { it.value })
        assertEquals(100, array.minOfOrNull { it.value })
        assertEquals(MyTestClass(300), array.sortedWith(comparator).let { it[it.size - 1] })
    }

    @Test
    fun sortedVariants() = with (MyTestClass) {
        val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(300)); it.add(MyTestClass(100)); it.add(MyTestClass(200)) }
        assertEquals(MyTestClass(100), array.sortedArray()[0])
        assertEquals(MyTestClass(300), array.sortedArrayDescending()[0])
        assertEquals(listOf(MyTestClass(100), MyTestClass(200), MyTestClass(300)), array.sortedBy { it.value }.toListGeneric())
        assertEquals(listOf(MyTestClass(300), MyTestClass(200), MyTestClass(100)), array.sortedByDescending { it.value }.toListGeneric())
    }

    @Test
    fun sumVariants() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(5500, array.sumBy { it.value })
        assertEquals(5500.0, array.sumByDouble { it.value.toDouble() })
        assertEquals(5500.0, array.sumOf { it.value.toDouble() })
        assertEquals(5500L, array.sumOf { it.value.toLong() })
        assertEquals(5500u, array.sumOfUInt { it.value.toUInt() })
        assertEquals(5500uL, array.sumOfULong { it.value.toULong() })
    }

    @Test
    fun chunked() = with (MyTestClass) {
        val array = simpleList()
        val chunks = array.chunked(4)
        assertEquals(3, chunks.size)
        assertEquals(4, chunks[0].size)
        val sums = array.chunked(4) { chunk -> chunk.sumOf { it.value } }
        assertEquals(3, sums.size)
    }

    @Test
    fun plusMinusElementVariants() = with (MyTestClass) {
        val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(2)) }
        assertEquals(listOf(MyTestClass(1), MyTestClass(2), MyTestClass(3)), array.plusElement(MyTestClass(3)).toListGeneric())
        assertEquals(listOf(MyTestClass(1)), array.minusElement(MyTestClass(2)).toListGeneric())
        val otherVInt = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(3)) }
        assertEquals(listOf(MyTestClass(1), MyTestClass(2), MyTestClass(3)), (array + otherVInt).toListGeneric())
        assertEquals(listOf(MyTestClass(1), MyTestClass(2), MyTestClass(3)), (array + listOf(MyTestClass(3))).toListGeneric())
        assertEquals(listOf(MyTestClass(1), MyTestClass(2), MyTestClass(3)), (array + arrayOf(MyTestClass(3))).toListGeneric())
        assertEquals(listOf(MyTestClass(1)), (array - arrayOf(MyTestClass(2))).toListGeneric())
        assertEquals(listOf(MyTestClass(1)), (array - otherVInt).toListGeneric())
        assertEquals(listOf(MyTestClass(1)), (array - listOf(MyTestClass(2))).toListGeneric())
        assertEquals(listOf(MyTestClass(1)), (array - sequenceOf(MyTestClass(2))).toListGeneric())
    }

    @Test
    fun randomVariants() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(true, array.toListGeneric().contains(array.random()))
        assertEquals(true, array.toListGeneric().contains(array.random(java.util.Random(42).asKotlinRandom())))
        assertEquals(true, array.toListGeneric().contains(array.randomOrNull()!!))
        assertEquals(null, ArrayVIntList<MyTestClass>().randomOrNull())
    }

    @Test
    fun zipVariants() = with (MyTestClass) {
        val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(2)) }
        val zippedArray = array.zip(arrayOf("a", "b"))
        assertEquals(listOf(MyTestClass(1) to "a", MyTestClass(2) to "b"), zippedArray)
        val zippedTransform = array.zip(arrayOf(10, 20)) { a, b -> a.value + b }
        assertEquals(listOf(11, 22), zippedTransform)
        val other = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(10)); it.add(MyTestClass(20)) }
        with (PairVIntInt.VLongAdapter<MyTestClass, MyTestClass>()) {
            val zippedVInt = (array zip other).toListGeneric()
            assertEquals(MyTestClass(1), zippedVInt[0].first)
            assertEquals(MyTestClass(10), zippedVInt[0].second)
        }
        with (PairVIntInt.VLongAdapter<MyTestClass, MyTestClass>()) {
            val zippedPair = array.zipPairVIntInt(other) { a, b -> PairVIntInt.of(a, b) }.toListGeneric()
            assertEquals(MyTestClass(2), zippedPair[1].first)
            assertEquals(MyTestClass(20), zippedPair[1].second)
        }
    }

    @Test
    fun joinToAndToVString() = with (MyTestClass) {
        val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(2)) }
        val builder = StringBuilder()
        array.joinTo(builder, transform = { it.value.toString() })
        assertEquals("1, 2", builder.toString())
        assertEquals("{1, 2}", array.toStringV())
    }

    @Test
    fun mapReduceAndReduceVariants() = with (MyTestClass) {
        val array = simpleList()
        assertEquals(1000, array.mapReduce({ it.value }, { max, e -> if (e > max) e else max }))
        assertEquals(1000, array.mapReduceIndexed({ it.value }) { _, max, e -> if (e > max) e else max })
        assertEquals(5500, array.reduce<Int, MyTestClass> { acc, e -> acc + e.value })
        assertEquals(5500, array.reduceIndexed<Int, MyTestClass> { _, acc, e -> acc + e.value })
        assertEquals(5500, array.reduceIndexedOrNull<Int, MyTestClass> { _, acc, e -> acc + e.value })
        assertEquals(5500, array.reduceOrNull<Int, MyTestClass> { acc, e -> acc + e.value })
        assertEquals(null, ArrayVIntList<MyTestClass>().reduceOrNull<Int, MyTestClass> { acc, e -> acc + e.value })
    }

    @Test
    fun runningFoldVariants() = with (MyTestClass) {
        with (MyLongTestClass) {
            val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(2)); it.add(MyTestClass(3)) }
            assertEquals(listOf(1, 3, 6), array.runningFoldVInt(0) { acc, e -> acc + e.value }.toListGeneric())
            assertEquals(listOf(1, 3, 6), array.runningFoldVIntIndexed(0) { _, acc, e -> acc + e.value }.toListGeneric())
            assertEquals(listOf(1L, 3L, 6L), array.runningFoldVLong(0) { acc, e -> acc + e.value }.toListGeneric())
            assertEquals(listOf(1L, 3L, 6L), array.runningFoldVLongIndexed(0) { _, acc, e -> acc + e.value }.toListGeneric())
            assertEquals(listOf(1, 3, 6), array.runningFoldGeneric(0) { acc, e -> acc + e.value })
            assertEquals(listOf(1, 3, 6), array.runningFoldGenericIndexed(0) { _, acc, e -> acc + e.value })
        }
    }

    @Test
    fun runningReduceVariants() = with (MyTestClass) {
        with (MyLongTestClass) {
            val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(2)); it.add(MyTestClass(3)) }
            assertEquals(listOf(3, 6), array.runningReduceVInt<Int, MyTestClass> { acc, e -> acc + e.value }.toListGeneric())
            assertEquals(listOf(3, 6), array.runningReduceVIntIndexed<Int, MyTestClass> { _, acc, e -> acc + e.value }.toListGeneric())
            assertEquals(listOf(3L, 6L), array.runningReduceVLong<Long, MyTestClass> { acc, e -> acc + e.value }.toListGeneric())
            assertEquals(listOf(3L, 6L), array.runningReduceVLongIndexed<Long, MyTestClass> { _, acc, e -> acc + e.value }.toListGeneric())
            assertEquals(listOf(3, 6), array.runningReduceGeneric<Int, MyTestClass> { acc, e -> acc + e.value })
            assertEquals(listOf(3, 6), array.runningReduceGenericIndexed<Int, MyTestClass> { _, acc, e -> acc + e.value })
        }
    }

    @Test
    fun scanVariants() = with (MyTestClass) {
        val array = ArrayVIntList<MyTestClass>().also { it.add(MyTestClass(1)); it.add(MyTestClass(2)); it.add(MyTestClass(3)) }
        assertEquals(listOf(1, 3, 6), array.scan(0) { acc, e -> acc + e.value })
        assertEquals(listOf(1, 3, 6), array.scanIndexed(0) { _, acc, e -> acc + e.value })
    }
}