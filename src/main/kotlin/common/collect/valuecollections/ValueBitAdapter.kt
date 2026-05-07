@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")
 package mpd.com.common.collect.valuecollections


interface ValueIntAdapter<T> {
    fun fromInt(v:Int): T
    fun fromInt(v:Int?): T? = if (v == null) null else fromInt(v)
    fun toInt(v:T): Int
    fun toInt(v:T?): Int? = if (v == null) null else toInt(v)
    
    companion object PrimitiveIntAdapter: ValueIntAdapter<Int> {
        override inline fun fromInt(v: Int) = v
        override inline fun toInt(v: Int): Int = v        
    }
}


interface ValueLongAdapter<T> {
    fun fromLong(v:Long): T
    fun fromLong(v:Long?): T? = if (v == null) null else fromLong(v)
    fun toLong(v:T):Long
    fun toLong(v:T?): Long? = if (v == null) null else toLong(v)
    
    companion object PrimitiveLongAdapter: ValueLongAdapter<Long> {
        override inline fun fromLong(v: Long) = v
        override inline fun toLong(v: Long): Long = v
    }
}