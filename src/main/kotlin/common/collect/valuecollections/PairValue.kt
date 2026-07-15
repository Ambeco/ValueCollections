@file:Suppress("NOTHING_TO_INLINE","OVERRIDE_BY_INLINE")

package mpd.com.common.collect.valuecollections

// This one is the only one that is immutable.  But since it's a value class, new instances incurs
// no overhead, so we deal with it.
@JvmInline
value class PairVIntInt<T,U>(val bits: LongBits) {
    constructor(first: IntBits, second: IntBits) : this((first.toLong() shl 32) or second.toLong())
    context(fa: ValueIntAdapter<T>) val first: T inline get() = fa.fromInt((bits shr 32).toInt())
    context(sa: ValueIntAdapter<U>) val second: U inline get() = sa.fromInt(bits.toInt())

    context(fa: ValueIntAdapter<T>, sa: ValueIntAdapter<U>) inline fun asPairGeneric(): Pair<T,U> = Pair(first, second)

    companion object {
        context(fa: ValueIntAdapter<T>, sa: ValueIntAdapter<U>) inline fun<T,U> of(first: T, second: U) = PairVIntInt<T,U>(fa.toInt(first), sa.toInt(second))
    }
    class VLongAdapter<T,U>: ValueLongAdapter<PairVIntInt<T,U>> {
        override inline fun fromLong(v: LongBits) = PairVIntInt<T,U>(v)
        override inline fun toLong(v: PairVIntInt<T,U>): LongBits = v.bits
    }
}





data class PairVIntLong<T,U>(var firstBits: IntBits, var secondBits: LongBits) {
    context(fa: ValueIntAdapter<T>) var first: T
        inline get() = fa.fromInt(firstBits) 
        inline set(value) {firstBits = fa.toInt(value)}
    context(sa: ValueLongAdapter<U>) var second: U
        inline get() = sa.fromLong(secondBits)
        inline set(value) {secondBits = sa.toLong(value)}

    context(fa: ValueIntAdapter<T>, sa: ValueLongAdapter<U>) inline fun asPairGeneric(): Pair<T,U> = Pair(first, second)

    companion object {
        context(fa: ValueIntAdapter<T>, sa: ValueLongAdapter<U>) inline fun<T,U> of(first: T, second: U) = PairVIntLong<T,U>(fa.toInt(first), sa.toLong(second))
    }
}





data class PairVIntObj<T,U>(var firstBits: IntBits, var second: U) {
    context(fa: ValueIntAdapter<T>) var first: T
        inline get() = fa.fromInt(firstBits)
        inline set(value) {firstBits = fa.toInt(value)}

    context(fa: ValueIntAdapter<T>) inline fun asPairGeneric(): Pair<T,U> = Pair(fa.fromInt(firstBits), second)

    companion object {
        context(fa: ValueIntAdapter<T>) inline fun<T,U> of(first: T, second: U) = PairVIntObj<T,U>(fa.toInt(first), second)
    }
}





data class PairVObjInt<T,U>(var first: T, var secondBits: IntBits) {
    context(sa: ValueIntAdapter<U>) var second: U
        inline get() = sa.fromInt(secondBits)
        inline set(value) {secondBits = sa.toInt(value)}

    context(sa: ValueIntAdapter<U>) inline fun asPairGeneric(): Pair<T,U> = Pair(first, second)

    companion object {
        context(sa: ValueIntAdapter<U>) inline fun<T,U> of(first: T, second: U) = PairVObjInt<T,U>(first, sa.toInt(second))
    }
}








data class PairVLongInt<T,U>(var firstBits: LongBits, var secondBits: IntBits) {
    context(fa: ValueLongAdapter<T>) var first: T
        inline get() = fa.fromLong(firstBits)
        inline set(value) {firstBits = fa.toLong(value)}
    context(sa: ValueIntAdapter<U>) var second: U
        inline get() = sa.fromInt(secondBits)
        inline set(value) {secondBits = sa.toInt(value)}

    context(fa: ValueLongAdapter<T>, sa: ValueIntAdapter<U>) inline fun asPairGeneric(): Pair<T,U> = Pair(first, second)

    companion object {
        context(fa: ValueLongAdapter<T>, sa: ValueIntAdapter<U>) inline fun<T,U> of(first: T, second: U) = PairVLongInt<T,U>(fa.toLong(first), sa.toInt(second))
    }
}




data class PairVLongLong<T,U>(var firstBits: LongBits, var secondBits: LongBits) {
    context(fa: ValueLongAdapter<T>) var first: T
        inline get() = fa.fromLong(firstBits)
        inline set(value) {firstBits = fa.toLong(value)}
    context(sa: ValueLongAdapter<U>) var second: U 
        inline get() = sa.fromLong(secondBits)
        inline set(value) {secondBits = sa.toLong(value)}

    context(fa: ValueLongAdapter<T>, sa: ValueLongAdapter<U>) inline fun asPairGeneric(): Pair<T,U> = Pair(first, second)

    companion object {
        context(fa: ValueLongAdapter<T>, sa: ValueLongAdapter<U>) inline fun<T,U> of(first: T, second: U) = PairVLongLong<T,U>(fa.toLong(first), sa.toLong(second))
    }
}




data class PairVLongObj<T,U>(var firstBits: LongBits, var second: U) {
    context(fa: ValueLongAdapter<T>) var first: T
        inline get() = fa.fromLong(firstBits)
        inline set(value) {firstBits = fa.toLong(value)}

    context(fa: ValueLongAdapter<T>) inline fun asPairGeneric(): Pair<T,U> = Pair(first, second)

    companion object {
        context(fa: ValueLongAdapter<T>) inline fun<T,U> of(first: T, second: U) = PairVLongObj<T,U>(fa.toLong(first), second)
    }
}




data class PairVObjLong<T,U>(var first: T, var secondBits: LongBits) {
    context(sa: ValueLongAdapter<U>) var second: U
        inline get() = sa.fromLong(secondBits)
        inline set(value) {secondBits = sa.toLong(value)}

    context(sa: ValueLongAdapter<U>) inline fun asPairGeneric(): Pair<T,U> = Pair(first, second)

    companion object {
        context(sa: ValueLongAdapter<U>) inline fun<T,U> of(first: T, second: U) = PairVObjLong<T,U>(first, sa.toLong(second))
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


data class IndexedVLong<T>(var index: Int, var secondBits: LongBits) {
    context(sa: ValueLongAdapter<T>) var second: T
        inline get() = sa.fromLong(secondBits)
        inline set(value) {secondBits = sa.toLong(value)}

    companion object {
        context(a: ValueLongAdapter<T>) inline fun<T> of(index:Int, value: T) = IndexedVLong<T>(index, a.toLong(value))
    }
}