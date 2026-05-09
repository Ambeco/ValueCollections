@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

@JvmInline
value class VIntIntPair<T,U>(val bits: Long) {
    constructor(first: Int, second: Int) : this((first.toLong() shl 32) or second.toLong())
    context(fa: ValueIntAdapter<T>) val first: T inline get() = fa.fromInt((bits shr 32).toInt())
    context(sa: ValueIntAdapter<U>) val second: U inline get() = sa.fromInt(bits.toInt())

    companion object {
        context(fa: ValueIntAdapter<T>, sa: ValueIntAdapter<U>) inline fun<T,U> of(first: T, second: U) = VIntIntPair<T,U>(fa.toInt(first), sa.toInt(second))
    }
}
data class VIntLongPair<T,U>(val firstBits: Int, val secondBits: Long) {
    context(fa: ValueIntAdapter<T>) val first: T inline get() = fa.fromInt(firstBits)
    context(sa: ValueLongAdapter<U>) val second: U inline get() = sa.fromLong(secondBits)

    companion object {
        context(fa: ValueIntAdapter<T>, sa: ValueLongAdapter<U>) inline fun<T,U> of(first: T, second: U) = VIntLongPair<T,U>(fa.toInt(first), sa.toLong(second))
    }
}
data class VLongIntPair<T,U>(val firstBits: Long, val secondBits: Int) {
    context(fa: ValueLongAdapter<T>) val first: T inline get() = fa.fromLong(firstBits)
    context(sa: ValueIntAdapter<U>) val second: U inline get() = sa.fromInt(secondBits)

    companion object {
        context(fa: ValueLongAdapter<T>, sa: ValueIntAdapter<U>) inline fun<T,U> of(first: T, second: U) = VLongIntPair<T,U>(fa.toLong(first), sa.toInt(second))
    }
}
data class VLongLongPair<T,U>(val firstBits: Long, val secondBits: Long) {
    context(fa: ValueLongAdapter<T>) val first: T inline get() = fa.fromLong(firstBits)
    context(sa: ValueLongAdapter<U>) val second: U inline get() = sa.fromLong(secondBits)

    companion object {
        context(fa: ValueLongAdapter<T>, sa: ValueLongAdapter<U>) inline fun<T,U> of(first: T, second: U) = VLongLongPair<T,U>(fa.toLong(first), sa.toLong(second))
    }
}