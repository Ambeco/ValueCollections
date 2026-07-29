package com.mpd.common.collect.valuecollections

import com.sun.management.HotSpotDiagnosticMXBean
import jdk.jfr.consumer.RecordedEvent
import jdk.jfr.consumer.RecordingStream
import com.mpd.common.collect.valuecollections.ArrayListVInt
import com.mpd.common.collect.valuecollections.ArrayListVLong
import com.mpd.common.collect.valuecollections.ArraySetVLong
import com.mpd.common.collect.valuecollections.CollectionVInt
import com.mpd.common.collect.valuecollections.CollectionVLong
import com.mpd.common.collect.valuecollections.HashMapVIntLong
import com.mpd.common.collect.valuecollections.HashMapVLongLong
import com.mpd.common.collect.valuecollections.IndexedVLong
import com.mpd.common.collect.valuecollections.MutableListVLong
import com.mpd.common.collect.valuecollections.PairVIntInt
import com.mpd.common.collect.valuecollections.PairVIntLong
import com.mpd.common.collect.valuecollections.PairVLongInt
import com.mpd.common.collect.valuecollections.PairVLongLong
import com.mpd.common.collect.valuecollections.ValueIntAdapter
import com.mpd.common.collect.valuecollections.ValueLongAdapter
import com.mpd.common.collect.valuecollections.add
import com.mpd.common.collect.valuecollections.all
import com.mpd.common.collect.valuecollections.allBits
import com.mpd.common.collect.valuecollections.allIndexedBits
import com.mpd.common.collect.valuecollections.any
import com.mpd.common.collect.valuecollections.anyIndexedBits
import com.mpd.common.collect.valuecollections.asCollectionGeneric
import com.mpd.common.collect.valuecollections.asList
import com.mpd.common.collect.valuecollections.asListGeneric
import com.mpd.common.collect.valuecollections.asSequence
import com.mpd.common.collect.valuecollections.associateByGeneric
import com.mpd.common.collect.valuecollections.associateByGenericTo
import com.mpd.common.collect.valuecollections.associateByVIntLong
import com.mpd.common.collect.valuecollections.associateByVIntLongTo
import com.mpd.common.collect.valuecollections.associateByVLongLong
import com.mpd.common.collect.valuecollections.associateByVLongLongTo
import com.mpd.common.collect.valuecollections.associateGeneric
import com.mpd.common.collect.valuecollections.associateTo
import com.mpd.common.collect.valuecollections.associateVIntInt
import com.mpd.common.collect.valuecollections.associateVIntLong
import com.mpd.common.collect.valuecollections.associateVLongInt
import com.mpd.common.collect.valuecollections.associateVLongLong
import com.mpd.common.collect.valuecollections.chunked
import com.mpd.common.collect.valuecollections.component1
import com.mpd.common.collect.valuecollections.component2
import com.mpd.common.collect.valuecollections.component3
import com.mpd.common.collect.valuecollections.component4
import com.mpd.common.collect.valuecollections.component5
import com.mpd.common.collect.valuecollections.contains
import com.mpd.common.collect.valuecollections.containsAll
import com.mpd.common.collect.valuecollections.contentEquals
import com.mpd.common.collect.valuecollections.count
import com.mpd.common.collect.valuecollections.distinct
import com.mpd.common.collect.valuecollections.distinctBy
import com.mpd.common.collect.valuecollections.filter
import com.mpd.common.collect.valuecollections.filterNot
import com.mpd.common.collect.valuecollections.filterNotTo
import com.mpd.common.collect.valuecollections.filterTo
import com.mpd.common.collect.valuecollections.find
import com.mpd.common.collect.valuecollections.findIndexedBits
import com.mpd.common.collect.valuecollections.findOr
import com.mpd.common.collect.valuecollections.findOrElse
import com.mpd.common.collect.valuecollections.findOrThrow
import com.mpd.common.collect.valuecollections.flatMap
import com.mpd.common.collect.valuecollections.flatMapIndexed
import com.mpd.common.collect.valuecollections.flatMapIndexedTo
import com.mpd.common.collect.valuecollections.flatMapTo
import com.mpd.common.collect.valuecollections.fold
import com.mpd.common.collect.valuecollections.forEach
import com.mpd.common.collect.valuecollections.forEachBits
import com.mpd.common.collect.valuecollections.forEachIndexed
import com.mpd.common.collect.valuecollections.forEachIndexedBits
import com.mpd.common.collect.valuecollections.fromLong
import com.mpd.common.collect.valuecollections.fromLongOr
import com.mpd.common.collect.valuecollections.fromLongOrNull
import com.mpd.common.collect.valuecollections.get
import com.mpd.common.collect.valuecollections.groupBy
import com.mpd.common.collect.valuecollections.groupByTo
import com.mpd.common.collect.valuecollections.intersect
import com.mpd.common.collect.valuecollections.isEmpty
import com.mpd.common.collect.valuecollections.isNotEmpty
import com.mpd.common.collect.valuecollections.joinTo
import com.mpd.common.collect.valuecollections.joinToString
import com.mpd.common.collect.valuecollections.mapGeneric
import com.mpd.common.collect.valuecollections.mapIndexedGeneric
import com.mpd.common.collect.valuecollections.mapIndexedGenericNotNull
import com.mpd.common.collect.valuecollections.mapIndexedVInt
import com.mpd.common.collect.valuecollections.mapIndexedVIntNotNull
import com.mpd.common.collect.valuecollections.mapIndexedVLong
import com.mpd.common.collect.valuecollections.mapIndexedVLongNotNull
import com.mpd.common.collect.valuecollections.mapNotNull
import com.mpd.common.collect.valuecollections.mapNotNullTo
import com.mpd.common.collect.valuecollections.mapReduce
import com.mpd.common.collect.valuecollections.mapReduceIndexed
import com.mpd.common.collect.valuecollections.mapVInt
import com.mpd.common.collect.valuecollections.mapVLong
import com.mpd.common.collect.valuecollections.max
import com.mpd.common.collect.valuecollections.maxByOrNull
import com.mpd.common.collect.valuecollections.maxOf
import com.mpd.common.collect.valuecollections.maxOfOrNull
import com.mpd.common.collect.valuecollections.maxOfWith
import com.mpd.common.collect.valuecollections.maxOfWithOrNull
import com.mpd.common.collect.valuecollections.maxWith
import com.mpd.common.collect.valuecollections.maxWithOrNull
import com.mpd.common.collect.valuecollections.min
import com.mpd.common.collect.valuecollections.minByOrNull
import com.mpd.common.collect.valuecollections.minOf
import com.mpd.common.collect.valuecollections.minOfOrNull
import com.mpd.common.collect.valuecollections.minOfWith
import com.mpd.common.collect.valuecollections.minOfWithOrNull
import com.mpd.common.collect.valuecollections.minWith
import com.mpd.common.collect.valuecollections.minWithOrNull
import com.mpd.common.collect.valuecollections.minus
import com.mpd.common.collect.valuecollections.minusElement
import com.mpd.common.collect.valuecollections.none
import com.mpd.common.collect.valuecollections.onEach
import com.mpd.common.collect.valuecollections.onEachIndexed
import com.mpd.common.collect.valuecollections.partition
import com.mpd.common.collect.valuecollections.plus
import com.mpd.common.collect.valuecollections.plusElement
import com.mpd.common.collect.valuecollections.random
import com.mpd.common.collect.valuecollections.randomOrNull
import com.mpd.common.collect.valuecollections.reduce
import com.mpd.common.collect.valuecollections.reduceIndexed
import com.mpd.common.collect.valuecollections.reduceIndexedOrNull
import com.mpd.common.collect.valuecollections.reduceOrNull
import com.mpd.common.collect.valuecollections.runningFoldGeneric
import com.mpd.common.collect.valuecollections.runningFoldGenericIndexed
import com.mpd.common.collect.valuecollections.runningFoldVInt
import com.mpd.common.collect.valuecollections.runningFoldVIntIndexed
import com.mpd.common.collect.valuecollections.runningFoldVLong
import com.mpd.common.collect.valuecollections.runningFoldVLongIndexed
import com.mpd.common.collect.valuecollections.runningReduceGeneric
import com.mpd.common.collect.valuecollections.runningReduceGenericIndexed
import com.mpd.common.collect.valuecollections.runningReduceVInt
import com.mpd.common.collect.valuecollections.runningReduceVIntIndexed
import com.mpd.common.collect.valuecollections.runningReduceVLong
import com.mpd.common.collect.valuecollections.runningReduceVLongIndexed
import com.mpd.common.collect.valuecollections.scan
import com.mpd.common.collect.valuecollections.scanIndexed
import com.mpd.common.collect.valuecollections.single
import com.mpd.common.collect.valuecollections.singleBits
import com.mpd.common.collect.valuecollections.singleOr
import com.mpd.common.collect.valuecollections.singleOrElse
import com.mpd.common.collect.valuecollections.singleOrNull
import com.mpd.common.collect.valuecollections.sorted
import com.mpd.common.collect.valuecollections.sortedDescending
import com.mpd.common.collect.valuecollections.sortedWith
import com.mpd.common.collect.valuecollections.subtract
import com.mpd.common.collect.valuecollections.sumBy
import com.mpd.common.collect.valuecollections.sumByDouble
import com.mpd.common.collect.valuecollections.sumOf
import com.mpd.common.collect.valuecollections.sumOfUInt
import com.mpd.common.collect.valuecollections.sumOfULong
import com.mpd.common.collect.valuecollections.toArrayGenericBits
import com.mpd.common.collect.valuecollections.toCollection
import com.mpd.common.collect.valuecollections.toHashSet
import com.mpd.common.collect.valuecollections.toList
import com.mpd.common.collect.valuecollections.toListGeneric
import com.mpd.common.collect.valuecollections.toLongArray
import com.mpd.common.collect.valuecollections.toMutableList
import com.mpd.common.collect.valuecollections.toMutableListGeneric
import com.mpd.common.collect.valuecollections.toMutableSet
import com.mpd.common.collect.valuecollections.toSet
import com.mpd.common.collect.valuecollections.toSetGeneric
import com.mpd.common.collect.valuecollections.toStringV
import com.mpd.common.collect.valuecollections.toVLongArray
import com.mpd.common.collect.valuecollections.union
import com.mpd.common.collect.valuecollections.vIntListOf
import com.mpd.common.collect.valuecollections.vLongListOf
import com.mpd.common.collect.valuecollections.vLongSetOf
import com.mpd.common.collect.valuecollections.withIndex
import com.mpd.common.collect.valuecollections.zip
import com.mpd.common.collect.valuecollections.zipPairVLongLong
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
value class VLongTestClass(val value: Long): Comparable<VLongTestClass> {
    override operator fun compareTo(other: VLongTestClass): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    @Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")
    companion object PrimitiveLongAdapter: ValueLongAdapter<VLongTestClass> {
        override inline fun fromLong(v: Long) = VLongTestClass(v)
        override inline fun toLong(v: VLongTestClass): Long = v.value
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
    private fun simpleList(): ArrayListVLong<VLongTestClass> = with (VLongTestClass) {
        val array = ArrayListVLong<VLongTestClass>(10)
        for (i in 0..9)
            array.add(i, VLongTestClass(100L*(i+1)))
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
    fun any(): Unit = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(true, array.any {it.value < 800})
        assertEquals(false, array.any {it.value >= 10000})
    }

    @Test
    fun all(): Unit = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(false, array.all {it.value < 800})
        assertEquals(true, array.all {it.value >= 0})
    }

    @Test
    fun forEach(): Unit = with (VLongTestClass) {
        val array = simpleList()
        var accumulator = 0L
        array.forEach {accumulator += it.value }
        assertEquals(5500L, accumulator)
    }

    @Test
    fun single(): Unit = with (VLongTestClass) {
        val array = simpleList()
        assertTrue(400L == array.single {it.value == 400L}.value)
        assertThrows(NoSuchElementException::class.java, { array.single {it.value >= 400} })
    }

    @Test
    fun contains(): Unit = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(true, array.contains(VLongTestClass(800)))
        assertEquals(false, array.contains(VLongTestClass(850)))
    }
    
    @Test
    fun component() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(VLongTestClass(100), array.component1())
        assertEquals(VLongTestClass(200), array.component2())
        assertEquals(VLongTestClass(300), array.component3())
        assertEquals(VLongTestClass(400), array.component4())
        assertEquals(VLongTestClass(500), array.component5())
    }

    @Test
    fun forEachIndexed() = with (VLongTestClass) {
        val array = simpleList()
        var accumulator = 0L
        array.forEachIndexed { i, e -> accumulator += i * e.value }
        assertEquals((0..9).sumOf { it * 100L * (it + 1) }, accumulator)
    }

    @Test
    fun containsAll() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(true, array.containsAll(listOf(VLongTestClass(100), VLongTestClass(500))))
        assertEquals(false, array.containsAll(listOf(VLongTestClass(100), VLongTestClass(150))))
    }

    @Test
    fun singleOr() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(VLongTestClass(400), array.singleOr({ it.value == 400L }, { VLongTestClass(-1) }))
        assertEquals(VLongTestClass(-1), array.singleOr({ it.value >= 400 }, { VLongTestClass(-1) }))
    }

    @Test
    fun singleOrNull() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(VLongTestClass(400), array.singleOrNull { it.value == 400L })
        assertEquals(null, array.singleOrNull { it.value >= 400 })
    }

    @Test
    fun find() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(VLongTestClass(400), array.find { it.value == 400L })
        assertEquals(null, array.find { it.value == 450L })
    }

    @Test
    fun findOr() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(VLongTestClass(400), array.findOr({ it.value == 400L }, { VLongTestClass(-1) }))
        assertEquals(VLongTestClass(-1), array.findOr({ it.value == 450L }, { VLongTestClass(-1) }))
    }

    @Test
    fun findOrThrow() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(VLongTestClass(400), array.findOrThrow { it.value == 400L })
        assertThrows(NoSuchElementException::class.java, { array.findOrThrow { it.value == 450L } })
    }

    @Test
    fun filter() = with (VLongTestClass) {
        val array = simpleList()
        val result = array.filter { it.value >= 800 }
        assertEquals(vLongListOf(VLongTestClass(800), VLongTestClass(900), VLongTestClass(1000)), result)
    }

    @Test
    fun filterNot() = with (VLongTestClass) {
        val array = simpleList()
        val result = array.filterNot { it.value >= 300 }
        assertEquals(vLongListOf(VLongTestClass(100), VLongTestClass(200)), result)
    }

    @Test
    fun isEmptyIsNotEmpty() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(false, array.isEmpty())
        assertEquals(true, array.isNotEmpty())
        assertEquals(true, ArrayListVLong<VLongTestClass>().isEmpty())
    }

    @Test
    fun toListGeneric() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals((1..10).map { VLongTestClass(100L * it) }, array.toListGeneric())
    }

    @Test
    fun toSetAndMutableSet() = with (VLongTestClass) {
        val array = simpleList()
        val set = array.toSet()
        assertEquals(10, set.size)
        assertEquals(true, set.contains(VLongTestClass(500)))
    }

    @Test
    fun contentEquals() = with (VLongTestClass) {
        val array = simpleList()
        val other = simpleList()
        assertEquals(true, array.contentEquals(other))
        other.add(VLongTestClass(9999))
        assertEquals(false, array.contentEquals(other))
    }

    @Test
    fun distinct() = with (VLongTestClass) {
        val array = vLongListOf(VLongTestClass(1), VLongTestClass(1), VLongTestClass(2))
        assertEquals(2, array.distinct().size)
    }

    @Test
    fun intersectSubtractUnion() = with (VLongTestClass) {
        val a = vLongListOf(VLongTestClass(1), VLongTestClass(2))
        val b = vLongListOf(VLongTestClass(2), VLongTestClass(3))
        assertEquals(vLongSetOf(VLongTestClass(2)), a intersect b)
        assertEquals(vLongSetOf(VLongTestClass(1)), a subtract b)
        assertEquals(vLongSetOf(VLongTestClass(1), VLongTestClass(2), VLongTestClass(3)), a union b)
    }

    @Test
    fun countAndFold() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(10, array.count())
        assertEquals(3, array.count { it.value >= 800 })
        assertEquals(5500L, array.fold(0L) { acc, e -> acc + e.value })
    }

    @Test
    fun maxAndMin() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(VLongTestClass(1000), array.max())
        assertEquals(VLongTestClass(100), array.min())
        assertEquals(VLongTestClass(1000), array.maxByOrNull { it.value })
        assertEquals(VLongTestClass(100), array.minByOrNull { it.value })
        assertEquals(1000L, array.maxOf { it.value })
        assertEquals(100L, array.minOf { it.value })
    }

    @Test
    fun none() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(false, array.none())
        assertEquals(true, array.none { it.value > 10000 })
        assertEquals(false, array.none { it.value == 400L })
    }

    @Test
    fun sortedAndSortedDescending() = with (VLongTestClass) {
        val array = vLongListOf(VLongTestClass(300), VLongTestClass(100), VLongTestClass(200))
        assertEquals(vLongListOf(VLongTestClass(100), VLongTestClass(200), VLongTestClass(300)), array.sorted())
        assertEquals(vLongListOf(VLongTestClass(300), VLongTestClass(200), VLongTestClass(100)), array.sortedDescending())
    }

    @Test
    fun sumOf() = with (VLongTestClass) {
        val array = simpleList()
        val result = array.sumOf(fun(e: VLongTestClass): Long { return e.value })
        assertEquals(5500, result)
    }

    @Test
    fun partition() = with (VLongTestClass) {
        val array = simpleList()
        val (above, below) = array.partition { it.value >= 600 }
        assertEquals(5, above.size)
        assertEquals(5, below.size)
    }

    @Test
    fun plusAndMinus() = with (VLongTestClass) {
        val array = vLongListOf(VLongTestClass(1), VLongTestClass(2))
        assertEquals(
            vLongListOf(
                VLongTestClass(1),
                VLongTestClass(2),
                VLongTestClass(3)
            ), (array + VLongTestClass(3)))
        assertEquals(vLongListOf(VLongTestClass(2)), (array - VLongTestClass(1)))
    }

    @Test
    fun joinToString() = with (VLongTestClass) {
        val array = vLongListOf(VLongTestClass(1), VLongTestClass(2))
        assertEquals("1, 2", array.joinToString { it.value.toString() })
    }

    @Test
    fun groupBy() = with (VLongTestClass) {
        val array = simpleList()
        val groups = array.groupBy { it.value % 200 }
        assertEquals(2, groups.size)
        assertEquals(5, groups[0]!!.size)
    }

    @Test
    fun mapGeneric() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals((1..10).map { 100L * it }, array.mapGeneric { it.value })
    }

    @Test
    fun bitsLayer() = with (VLongTestClass) {
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
        assertEquals(VLongTestClass(400), array.fromLong<VLongTestClass>(400))
        assertThrows(NoSuchElementException::class.java, { array.fromLong<VLongTestClass>(array.NULL_VALUE) })
        assertEquals(VLongTestClass(400), array.fromLongOr(400) { VLongTestClass(-1) })
        assertEquals(VLongTestClass(-1), array.fromLongOr(array.NULL_VALUE) { VLongTestClass(-1) })
        assertEquals(VLongTestClass(400), array.fromLongOrNull(400))
        assertEquals(null, array.fromLongOrNull(array.NULL_VALUE))
        val genericArray = array.toArrayGenericBits()
        assertEquals(10, genericArray.size)
        assertEquals(400L, genericArray[3])
    }

    @Test
    fun asCollectionGeneric() = with (VLongTestClass) {
        val array = simpleList()
        val collection: Collection<VLongTestClass> = array.asCollectionGeneric()
        assertEquals(10, collection.size)
        assertEquals(true, collection.contains(VLongTestClass(500)))
    }

    @Test
    fun singleOrElseAndFindOrElse() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(VLongTestClass(400), array.singleOrElse({ it.value == 400L }, VLongTestClass(-1)))
        assertEquals(VLongTestClass(-1), array.singleOrElse({ it.value >= 400 }, VLongTestClass(-1)))
        assertEquals(VLongTestClass(400), array.findOrElse({ it.value == 400L }, VLongTestClass(-1)))
        assertEquals(VLongTestClass(-1), array.findOrElse({ it.value == 450L }, VLongTestClass(-1)))
    }

    @Test
    fun filterToAndFilterNotTo() = with (VLongTestClass) {
        val array = simpleList()
        val destination = ArrayListVLong<VLongTestClass>()
        array.filterTo(destination) { it.value >= 800 }
        assertEquals(vLongListOf(VLongTestClass(800), VLongTestClass(900), VLongTestClass(1000)), destination)
        val notDestination = ArrayListVLong<VLongTestClass>()
        array.filterNotTo(notDestination) { it.value >= 300 }
        assertEquals(vLongListOf(VLongTestClass(100), VLongTestClass(200)), notDestination)
    }

    @Test
    fun associateVariants() = with (VLongTestClass) {
        val array = simpleList()
        val m1 = array.associateVLongLong { PairVLongLong.of(it, VLongTestClass(it.value * 2)) }
        assertEquals(10, m1.size)
        val m2 = array.associateVLongLong({ it }, { VLongTestClass(it.value * 2) })
        assertEquals(10, m2.size)
        val byKey1 = array.associateByVLongLong { VLongTestClass(it.value / 100) }
        assertEquals(10, byKey1.size)
        val byKeyDest1 = HashMapVLongLong<VLongTestClass, VLongTestClass>()
        array.associateByVLongLongTo(byKeyDest1) { VLongTestClass(it.value / 100) }
        assertEquals(10, byKeyDest1.size)
        val generic1 = array.associateGeneric { it to it.value }
        assertEquals(1000, generic1[VLongTestClass(1000)])
        val generic2 = array.associateGeneric({ it.value }, { it })
        assertEquals(VLongTestClass(1000), generic2[1000])
        val byGeneric = array.associateByGeneric { it.value }
        assertEquals(VLongTestClass(500), byGeneric[500])
        val byGenericDest = HashMap<Long, VLongTestClass>()
        array.associateByGenericTo(byGenericDest) { it.value }
        assertEquals(VLongTestClass(500), byGenericDest[500])
        val destination = HashMap<VLongTestClass, Long>()
        array.associateTo(destination) { it to it.value }
        assertEquals(1000, destination[VLongTestClass(1000)])
    }

    @Test
    fun associateIntVariants() = with (VLongTestClass) {
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
            val byVIntLongDest = HashMapVIntLong<MyIntTestClass, VLongTestClass>()
            array.associateByVIntLongTo(byVIntLongDest) { MyIntTestClass(it.value.toInt()) }
            assertEquals(10, byVIntLongDest.size)
        }
    }

    @Test
    fun toCollectionVariants() = with (VLongTestClass) {
        val array = simpleList()
        val vDestination = array.toCollection(ArraySetVLong<VLongTestClass>(20))
        assertEquals(10, vDestination.size)
        val genericDestination = array.toCollection(mutableListOf<VLongTestClass>())
        assertEquals(10, genericDestination.size)
    }

    @Test
    fun toListAndSetVariants() = with (VLongTestClass) {
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
    fun arrayConversions() = with (VLongTestClass) {
        val array = simpleList()
        val intArray = array.toLongArray()
        assertEquals(400, intArray[3])
        val vLongArray = array.toVLongArray()
        assertEquals(VLongTestClass(400), vLongArray[3])
    }

    @Test
    fun asSequence() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(5500, array.asSequence().sumOf { it.value })
    }

    @Test
    fun distinctBy() = with (VLongTestClass) {
        val array = vLongListOf(VLongTestClass(1), VLongTestClass(101), VLongTestClass(2))
        assertEquals(2, array.distinctBy { it.value % 100 }.size)
    }

    @Test
    fun onEachAndOnEachIndexed() = with (VLongTestClass) {
        val array = simpleList()
        var accumulator = 0L
        array.onEach { accumulator += it.value }
        assertEquals(5500L, accumulator)
        var indexedAccumulator = 0
        array.onEachIndexed { i, e -> indexedAccumulator += i }
        assertEquals((0..9).sum(), indexedAccumulator)
    }

    @Test
    fun withIndex() = with (VLongTestClass) {
        val array = simpleList()
        val indexed: Collection<IndexedVLong<VLongTestClass>> = array.withIndex()
        val first = indexed.first()
        assertEquals(0, first.index)
        assertEquals(VLongTestClass(100), first.second)
    }

    @Test
    fun flatMapVariants() {
        context (VLongTestClass, MyIntTestClass) {
            val array = vLongListOf(VLongTestClass(1), VLongTestClass(2))
            val flatVLong: ArrayListVLong<VLongTestClass> = array.flatMap(fun(e: VLongTestClass): CollectionVLong<VLongTestClass> =
                vLongListOf(e, VLongTestClass(e.value * 10)))
            assertEquals(listOf(1L, 10L, 2L, 20L), flatVLong.mapGeneric { it.value })
            val flatVLongTo: ArrayListVLong<VLongTestClass> = array.flatMapTo(ArrayListVLong<VLongTestClass>()) { e: VLongTestClass ->
                vLongListOf(e)
            }
            assertEquals(2, flatVLongTo.size)
            val flatIndexedVLong: ArrayListVLong<VLongTestClass> = array.flatMapIndexed(fun(i: Int, e: VLongTestClass): CollectionVLong<VLongTestClass> =
                vLongListOf(VLongTestClass(i.toLong()), e))
            assertEquals(4, flatIndexedVLong.size)
            val flatIndexedVLongTo: ArrayListVLong<VLongTestClass> = array.flatMapIndexedTo(
                ArrayListVLong<VLongTestClass>()
            ) { i: Int, e: VLongTestClass -> vLongListOf(e) }
            assertEquals(2, flatIndexedVLongTo.size)
            val flatVInt: ArrayListVInt<MyIntTestClass> = array.flatMap(fun(e: VLongTestClass): CollectionVInt<MyIntTestClass> =
                vIntListOf(MyIntTestClass(e.value.toInt())))
            assertEquals(listOf(1, 2), flatVInt.mapGeneric { it.value })
            val flatVIntTo: ArrayListVInt<MyIntTestClass> = array.flatMapTo(ArrayListVInt<MyIntTestClass>()) { e: VLongTestClass ->
                vIntListOf(
                    MyIntTestClass(e.value.toInt())
                )
            }
            assertEquals(2, flatVIntTo.size)
            val flatIndexedVInt: ArrayListVInt<MyIntTestClass> = array.flatMapIndexed(fun(i: Int, e: VLongTestClass): CollectionVInt<MyIntTestClass> =
                vIntListOf(MyIntTestClass((i + e.value).toInt())))
            assertEquals(2, flatIndexedVInt.size)
            val flatIndexedVIntTo: ArrayListVInt<MyIntTestClass> = array.flatMapIndexedTo(
                ArrayListVInt<MyIntTestClass>()
            ) { i: Int, e: VLongTestClass -> vIntListOf(MyIntTestClass(e.value.toInt())) }
            assertEquals(2, flatIndexedVIntTo.size)
        }
    }

    @Test
    fun groupByTo() = with (VLongTestClass) {
        val array = simpleList()
        val destination = HashMap<Long, MutableListVLong<VLongTestClass>>()
        array.groupByTo(destination) { it.value % 2 }
        assertEquals(1, destination.size)
    }

    @Test
    fun mapVariants() = context (VLongTestClass, MyIntTestClass) {
        run {
            val array = vLongListOf(VLongTestClass(1), VLongTestClass(2))
            assertEquals(listOf(2L, 4L), array.mapVLong { VLongTestClass(it.value * 2) }.mapGeneric { it.value })
            assertEquals(listOf(1, 2), array.mapVInt { MyIntTestClass(it.value.toInt()) }.mapGeneric { it.value })
            assertEquals(listOf(0L, 2L), array.mapIndexedVLong { i, e -> VLongTestClass(i * e.value) }.mapGeneric { it.value })
            assertEquals(listOf(0, 2), array.mapIndexedVInt { i, e -> MyIntTestClass((i * e.value).toInt()) }.mapGeneric { it.value })
            assertEquals(listOf(0L, 2L), array.mapIndexedGeneric { i, e -> i * e.value })
            assertEquals(vLongListOf(VLongTestClass(2)), array.mapIndexedVLongNotNull { i, e -> if (i == 1) e else null })
            assertEquals(vIntListOf(MyIntTestClass(2)), array.mapIndexedVIntNotNull { i, e -> if (i == 1) MyIntTestClass(e.value.toInt()) else null })
            assertEquals(listOf(2L), array.mapIndexedGenericNotNull { i, e -> if (i == 1) e.value else null })
            assertEquals(listOf(2L), array.mapNotNull { if (it.value == 2L) it.value else null })
            val mapNotNullDest = mutableListOf<Long>()
            array.mapNotNullTo(mapNotNullDest) { if (it.value == 2L) it.value else null }
            assertEquals(listOf(2L), mapNotNullDest)
        }
    }


    @Test
    fun comparatorVariants() = with (VLongTestClass) {
        val array = vLongListOf(VLongTestClass(300), VLongTestClass(100), VLongTestClass(200))
        val comparator = Comparator<VLongTestClass> { l, r -> l.value.compareTo(r.value) }
        assertEquals(VLongTestClass(300), array.maxWith(comparator))
        assertEquals(VLongTestClass(300), array.maxWithOrNull(comparator))
        assertEquals(VLongTestClass(100), array.minWith(comparator))
        assertEquals(VLongTestClass(100), array.minWithOrNull(comparator))
        assertEquals(300L, array.maxOfWith(Comparator { l: Long, r: Long -> l.compareTo(r) }) { it.value })
        assertEquals(300L, array.maxOfWithOrNull(Comparator { l: Long, r: Long -> l.compareTo(r) }) { it.value })
        assertEquals(100L, array.minOfWith(Comparator { l: Long, r: Long -> l.compareTo(r) }) { it.value })
        assertEquals(100L, array.minOfWithOrNull(Comparator { l: Long, r: Long -> l.compareTo(r) }) { it.value })
        assertEquals(300L, array.maxOfOrNull { it.value })
        assertEquals(100L, array.minOfOrNull { it.value })
        assertEquals(VLongTestClass(300), array.sortedWith(comparator).let { it[it.size - 1] })
    }

    /*
    TODO
    @Test
    fun sortedVariants() = with (VLongTestClass) {
        val array = vLongListOf(VLongTestClass(300), VLongTestClass(100), VLongTestClass(200))
        assertEquals(VLongTestClass(100), array.sortedArray()[0])
        assertEquals(VLongTestClass(300), array.sortedArrayDescending()[0])
        assertEquals(vLongListOf(VLongTestClass(100), VLongTestClass(200), VLongTestClass(300)), array.sortedBy { it.value })
        assertEquals(vLongListOf(VLongTestClass(300), VLongTestClass(200), VLongTestClass(100)), array.sortedByDescending { it.value })
    }
    */

    @Test
    fun sumVariants() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(5500, array.sumBy { it.value.toInt() })
        assertEquals(5500.0, array.sumByDouble { it.value.toDouble() })
        val sumDouble = array.sumOf(fun(e: VLongTestClass): Double { return e.value.toDouble() })
        assertEquals(5500.0, sumDouble)
        val sumLong = array.sumOf(fun(e: VLongTestClass): Long { return e.value })
        assertEquals(5500L, sumLong)
        assertEquals(5500uL, array.sumOfULong { it.value.toULong() })
        assertEquals(5500u, array.sumOfUInt { it.value.toUInt() })
    }

    @Test
    fun chunked() = with (VLongTestClass) {
        val array = simpleList()
        val chunks = array.chunked(4)
        assertEquals(3, chunks.size)
        assertEquals(4, chunks[0].size)
        val sums = array.chunked(4) { chunk -> chunk.sumOf(fun(e: VLongTestClass): Long { return e.value }) }
        assertEquals(3, sums.size)
    }

    @Test
    fun plusMinusElementVariants() = with (VLongTestClass) {
        val array = vLongListOf(VLongTestClass(1), VLongTestClass(2))
        assertEquals(
            vLongListOf(
                VLongTestClass(1),
                VLongTestClass(2),
                VLongTestClass(3)
            ), array.plusElement(VLongTestClass(3)))
        assertEquals(vLongListOf(VLongTestClass(1)), array.minusElement(VLongTestClass(2)))
        val otherVLong = vLongListOf(VLongTestClass(3))
        assertEquals(vLongListOf(VLongTestClass(1), VLongTestClass(2), VLongTestClass(3)), (array + otherVLong))
        assertEquals(vLongListOf(VLongTestClass(1), VLongTestClass(2), VLongTestClass(3)), (array + listOf(VLongTestClass(3))))
        assertEquals(vLongListOf(VLongTestClass(1), VLongTestClass(2), VLongTestClass(3)), (array + arrayOf(VLongTestClass(3))))
        assertEquals(vLongListOf(VLongTestClass(1)), (array - arrayOf(VLongTestClass(2))))
        assertEquals(vLongListOf(VLongTestClass(1), VLongTestClass(2)), (array - otherVLong))
        assertEquals(vLongListOf(VLongTestClass(1)), (array - listOf(VLongTestClass(2))))
        assertEquals(vLongListOf(VLongTestClass(1)), (array - sequenceOf(VLongTestClass(2))))
    }

    @Test
    fun randomVariants() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(true, array.contains(array.random()))
        assertEquals(true, array.contains(array.random(Random(42))))
        assertEquals(true, array.contains(array.randomOrNull()!!))
        assertEquals(null, ArrayListVLong<VLongTestClass>().randomOrNull())
    }

    @Test
    fun zipVariants() = with (VLongTestClass) {
        val array = vLongListOf(VLongTestClass(1), VLongTestClass(2))
        val zippedArray = array.zip(arrayOf("a", "b"))
        assertEquals(listOf(VLongTestClass(1) to "a", VLongTestClass(2) to "b"), zippedArray)
        val zippedTransform = array.zip(arrayOf(10, 20)) { a, b -> a.value + b }
        assertEquals(listOf(11L, 22L), zippedTransform)
        val other = vLongListOf(VLongTestClass(10), VLongTestClass(20))
        val zippedPair = array.zipPairVLongLong(other) { a, b -> VLongTestClass(a.value + b.value) }
        assertEquals(VLongTestClass(11), zippedPair[0])
        assertEquals(VLongTestClass(22), zippedPair[1])
    }

    @Test
    fun joinToAndToVString() = with (VLongTestClass) {
        val array = vLongListOf(VLongTestClass(1), VLongTestClass(2))
        val builder = StringBuilder()
        array.joinTo(builder, transform = { it.value.toString() })
        assertEquals("1, 2", builder.toString())
        assertEquals("{1, 2}", array.toStringV())
    }

    @Test
    fun mapReduceAndReduceVariants() = with (VLongTestClass) {
        val array = simpleList()
        assertEquals(1000, array.mapReduce({ it.value }, { max, e -> if (e > max) e else max }))
        assertEquals(1000, array.mapReduceIndexed({ it.value }) { _, max, e -> if (e > max) e else max })
        assertEquals(VLongTestClass(1000), array.reduce { acc, e -> if (e.value > acc.value) e else acc })
        assertEquals(VLongTestClass(1000), array.reduceIndexed { _, acc, e -> if (e.value > acc.value) e else acc })
        assertEquals(VLongTestClass(1000), array.reduceIndexedOrNull { _, acc, e -> if (e.value > acc.value) e else acc })
        assertEquals(VLongTestClass(1000), array.reduceOrNull { acc, e -> if (e.value > acc.value) e else acc })
        assertEquals(null, ArrayListVLong<VLongTestClass>().reduceOrNull { acc, e -> if (e.value > acc.value) e else acc })
    }

    @Test
    fun runningFoldVariants() = with (VLongTestClass) {
        with (MyIntTestClass) {
            val array = vLongListOf(VLongTestClass(1), VLongTestClass(2), VLongTestClass(3))
            assertEquals(listOf(1L, 3L, 6L), array.runningFoldVLong(VLongTestClass(0)) { acc, e -> VLongTestClass(acc.value + e.value) }.mapGeneric { it.value })
            assertEquals(listOf(1L, 3L, 6L), array.runningFoldVLongIndexed(VLongTestClass(0)) { _, acc, e -> VLongTestClass(acc.value + e.value) }.mapGeneric { it.value })
            assertEquals(listOf(1, 3, 6), array.runningFoldVInt(MyIntTestClass(0)) { acc, e -> MyIntTestClass(acc.value + e.value.toInt()) }.mapGeneric { it.value })
            assertEquals(listOf(1, 3, 6), array.runningFoldVIntIndexed(MyIntTestClass(0)) { _, acc, e -> MyIntTestClass(acc.value + e.value.toInt()) }.mapGeneric { it.value })
            assertEquals(listOf(1L, 3L, 6L), array.runningFoldGeneric(0L) { acc, e -> acc + e.value })
            assertEquals(listOf(1L, 3L, 6L), array.runningFoldGenericIndexed(0L) { _, acc, e -> acc + e.value })
        }
    }

    @Test
    fun runningReduceVariants() = with (VLongTestClass) {
        val array = vLongListOf(VLongTestClass(1), VLongTestClass(2), VLongTestClass(3))
        assertEquals(
            vLongListOf(
                VLongTestClass(3),
                VLongTestClass(6)
            ), array.runningReduceVLong { acc, e -> VLongTestClass(acc.value + e.value) })
        assertEquals(
            vLongListOf(
                VLongTestClass(3),
                VLongTestClass(6)
            ), array.runningReduceVLongIndexed { _, acc, e -> VLongTestClass(acc.value + e.value) })
        assertEquals(listOf(3L, 6L), array.runningReduceGeneric<VLongTestClass, VLongTestClass> { acc, e -> VLongTestClass(acc.value + e.value) }.map { it.value })
        assertEquals(listOf(3L, 6L), array.runningReduceGenericIndexed<VLongTestClass, VLongTestClass> { _, acc, e -> VLongTestClass(acc.value + e.value) }.map { it.value })
        val longAdapterForVLongTestClass = object : ValueIntAdapter<VLongTestClass> {
            override fun fromInt(v: Int) = VLongTestClass(v.toLong())
            override fun toInt(v: VLongTestClass): Int = v.value.toInt()
        }
        context (longAdapterForVLongTestClass) {
            assertEquals(
                vIntListOf(
                    VLongTestClass(3),
                    VLongTestClass(6)
                ), array.runningReduceVInt { acc, e -> VLongTestClass(acc.value + e.value) })
            assertEquals(
                vIntListOf(
                    VLongTestClass(3),
                    VLongTestClass(6)
                ), array.runningReduceVIntIndexed { _, acc, e -> VLongTestClass(acc.value + e.value) })
        }
    }

    @Test
    fun scanVariants() = with (VLongTestClass) {
        val array = vLongListOf(VLongTestClass(1), VLongTestClass(2), VLongTestClass(3))
        assertEquals(listOf(1L, 3L, 6L), array.scan(0L) { acc, e -> acc + e.value })
        assertEquals(listOf(1L, 3L, 6L), array.scanIndexed(0L) { _, acc, e -> acc + e.value })
    }
}