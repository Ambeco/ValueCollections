@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")
 package mpd.com.common.collect.valuecollections


typealias IntBits = Int
interface ValueIntAdapter<T> {
    fun fromInt(v:IntBits): T
    fun fromInt(v:IntBits?): T? = if (v == null) null else fromInt(v)
    fun toInt(v:T): IntBits
    fun toInt(v:T?): IntBits? = if (v == null) null else toInt(v)
    
    companion object PrimitiveIntAdapter: ValueIntAdapter<Int> {
        override inline fun fromInt(v: Int): IntBits = v
        override inline fun toInt(v: IntBits): Int = v        
    }
}


typealias LongBits = Long
interface ValueLongAdapter<T> {
    fun fromLong(v:LongBits): T
    fun fromLong(v:LongBits?): T? = if (v == null) null else fromLong(v)
    fun toLong(v:T):LongBits
    fun toLong(v:T?): LongBits? = if (v == null) null else toLong(v)
    
    companion object PrimitiveLongAdapter: ValueLongAdapter<Long> {
        override inline fun fromLong(v: Long): LongBits = v
        override inline fun toLong(v: LongBits): Long = v
    }
}

object PrimitiveBoolAdapter: ValueIntAdapter<Boolean> {
    override inline fun fromInt(v: IntBits) = v != 0
    override inline fun toInt(v: Boolean): IntBits = if (v) 1 else 0
}