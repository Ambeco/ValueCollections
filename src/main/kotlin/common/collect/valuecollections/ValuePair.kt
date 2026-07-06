@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

@JvmInline
value class VIntIntPair<T,U>(val bits: Long) {
    constructor(first: IntBits, second: IntBits) : this((first.toLong() shl 32) or second.toLong())
    context(fa: ValueIntAdapter<T>) val first: T inline get() = fa.fromInt((bits shr 32).toInt())
    context(sa: ValueIntAdapter<U>) val second: U inline get() = sa.fromInt(bits.toInt())

    companion object {
        context(fa: ValueIntAdapter<T>, sa: ValueIntAdapter<U>) inline fun<T,U> of(first: T, second: U) = VIntIntPair<T,U>(fa.toInt(first), sa.toInt(second))

    }
    class VLongAdapter<T,U>: ValueLongAdapter<VIntIntPair<T,U>> {
        override inline fun fromLong(v: LongBits) = VIntIntPair<T,U>(v)
        override inline fun toLong(v: VIntIntPair<T,U>): LongBits = v.bits
    }
}
data class VIntLongPair<T,U>(val firstBits: IntBits, val secondBits: LongBits) {
    context(fa: ValueIntAdapter<T>) val first: T inline get() = fa.fromInt(firstBits)
    context(sa: ValueLongAdapter<U>) val second: U inline get() = sa.fromLong(secondBits)

    companion object {
        context(fa: ValueIntAdapter<T>, sa: ValueLongAdapter<U>) inline fun<T,U> of(first: T, second: U) = VIntLongPair<T,U>(fa.toInt(first), sa.toLong(second))
    }
}
data class VLongIntPair<T,U>(val firstBits: LongBits, val secondBits: IntBits) {
    context(fa: ValueLongAdapter<T>) val first: T inline get() = fa.fromLong(firstBits)
    context(sa: ValueIntAdapter<U>) val second: U inline get() = sa.fromInt(secondBits)

    companion object {
        context(fa: ValueLongAdapter<T>, sa: ValueIntAdapter<U>) inline fun<T,U> of(first: T, second: U) = VLongIntPair<T,U>(fa.toLong(first), sa.toInt(second))
    }
}
data class VLongLongPair<T,U>(val firstBits: LongBits, val secondBits: LongBits) {
    context(fa: ValueLongAdapter<T>) val first: T inline get() = fa.fromLong(firstBits)
    context(sa: ValueLongAdapter<U>) val second: U inline get() = sa.fromLong(secondBits)

    companion object {
        context(fa: ValueLongAdapter<T>, sa: ValueLongAdapter<U>) inline fun<T,U> of(first: T, second: U) = VLongLongPair<T,U>(fa.toLong(first), sa.toLong(second))
    }
}

@JvmInline
value class IndexedVInt<T>(val bits: Long) {
    constructor(index: Int, value: IntBits) : this((index.toLong() shl 32) or value.toLong())
    val index: Int inline get() = (bits shr 32).toInt()
    context(sa: ValueIntAdapter<T>) val value: T inline get() = sa.fromInt(bits.toInt())

    companion object {
        context(a: ValueIntAdapter<T>) inline fun<T> of(index:Int, value: T) = IndexedVInt<T>(index, a.toInt(value))
    }
    
    class VLongAdapter<T>: ValueLongAdapter<IndexedVInt<T>> {
        override inline fun fromLong(v: LongBits) = IndexedVInt<T>(v)
        override inline fun toLong(v: IndexedVInt<T>): Long = v.bits
    }
}
data class IndexedVLong<T>(val index: Int, val secondBits: LongBits) {
    context(sa: ValueLongAdapter<T>) val second: T inline get() = sa.fromLong(secondBits)

    companion object {
        context(a: ValueLongAdapter<T>) inline fun<T> of(index:Int, value: T) = IndexedVLong<T>(index, a.toLong(value))
    }
}