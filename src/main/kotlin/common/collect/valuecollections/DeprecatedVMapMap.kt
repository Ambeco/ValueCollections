@file:Suppress("unused")

package mpd.com.common.collect.valuecollections

import androidx.collection.IntIntMap
import androidx.collection.IntLongMap
import androidx.collection.IntObjectMap
import androidx.collection.LongIntMap
import androidx.collection.LongLongMap
import androidx.collection.LongObjectMap
import androidx.collection.MutableIntIntMap
import androidx.collection.MutableIntLongMap
import androidx.collection.MutableIntObjectMap
import androidx.collection.MutableLongIntMap
import androidx.collection.MutableLongLongMap
import androidx.collection.MutableLongObjectMap
import androidx.collection.MutableObjectIntMap
import androidx.collection.MutableObjectLongMap
import androidx.collection.ObjectIntMap
import androidx.collection.ObjectLongMap

interface MapVIntInt<K,V> { 
    val collection: IntIntMap
}
class MutableMapVIntInt<K,V>(
    override val collection: MutableIntIntMap = MutableIntIntMap(),
): MapVIntInt<K,V> {
    constructor(initialCapacity: Int) : this(MutableIntIntMap(initialCapacity))

    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(ka.toInt(keySelector(e)), va.toInt(valueTransform(e)))}}
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline transform: (S) -> VIntIntPair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toInt(p.first), va.toInt(p.second))}}
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAllGeneric(source: CollectionVInt<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toInt(p.first), va.toInt(p.second))}}
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(ka.toInt(keySelector(e)), va.toInt(valueTransform(e)))}}
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline transform: (S) -> VIntIntPair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toInt(p.first), va.toInt(p.second))}}
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAllGeneric(source: CollectionVLong<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toInt(p.first), va.toInt(p.second))}}
}


interface MapVIntLong<K,V> {
    val collection: IntLongMap
}
class MutableMapVIntLong<K,V>(
    override val collection: MutableIntLongMap = MutableIntLongMap(),
): MapVIntLong<K,V> {
    constructor(initialCapacity: Int) : this(MutableIntLongMap(initialCapacity))

    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(ka.toInt(keySelector(e)), va.toLong(valueTransform(e)))}}
    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline transform: (S) -> VIntLongPair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toInt(p.first), va.toLong(p.second))}}
    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAllGeneric(source: CollectionVInt<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toInt(p.first), va.toLong(p.second))}}
    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(ka.toInt(keySelector(e)), va.toLong(valueTransform(e)))}}
    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline transform: (S) -> VIntLongPair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toInt(p.first), va.toLong(p.second))}}
    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAllGeneric(source: CollectionVLong<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toInt(p.first), va.toLong(p.second))}}
}


interface MapVIntObject<K,V> {
    val collection: IntObjectMap<V>
}
class MutableMapVIntObject<K,V>(
    override val collection: MutableIntObjectMap<V> = MutableIntObjectMap(),
): MapVIntObject<K,V> {
    constructor(initialCapacity: Int) : this(MutableIntObjectMap(initialCapacity))

    context(ka: ValueIntAdapter<K>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(ka.toInt(keySelector(e)), valueTransform(e))}}
    context(ka: ValueIntAdapter<K>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toInt(p.first), p.second)}}
    context(ka: ValueIntAdapter<K>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(ka.toInt(keySelector(e)), valueTransform(e))}}
    context(ka: ValueIntAdapter<K>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toInt(p.first), p.second)}}
}


interface MapVObjectInt<K,V> {
    val collection: ObjectIntMap<K>
}
class MutableMapVObjectInt<K,V>(
    override val collection: MutableObjectIntMap<K> = MutableObjectIntMap(),
): MapVObjectInt<K,V> {
    constructor(initialCapacity: Int) : this(MutableObjectIntMap(initialCapacity))
    
    context(va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(keySelector(e), va.toInt(valueTransform(e)))}}
    context(va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(p.first, va.toInt(p.second))}}
    context(va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(keySelector(e), va.toInt(valueTransform(e)))}}
    context(va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(p.first, va.toInt(p.second))}}
}


interface MapVLongInt<K,V> {
    val collection: LongIntMap
}
class MutableMapVLongInt<K,V>(
    override val collection: MutableLongIntMap = MutableLongIntMap())
    : MapVLongInt<K,V> {
    constructor(initialCapacity: Int) : this(MutableLongIntMap(initialCapacity))

    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(ka.toLong(keySelector(e)), va.toInt(valueTransform(e)))}}
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline transform: (S) -> VLongIntPair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toLong(p.first), va.toInt(p.second))}}
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAllGeneric(source: CollectionVInt<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toLong(p.first), va.toInt(p.second))}}
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(ka.toLong(keySelector(e)), va.toInt(valueTransform(e)))}}
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline transform: (S) -> VLongIntPair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toLong(p.first), va.toInt(p.second))}}
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAllGeneric(source: CollectionVLong<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toLong(p.first), va.toInt(p.second))}}
}

interface MapVLongLong<K,V> {
    val collection: LongLongMap
}
class MutableMapVLongLong<K,V>(
    override val collection: MutableLongLongMap = MutableLongLongMap())
    : MapVLongLong<K,V> {
    constructor(initialCapacity: Int) : this(MutableLongLongMap(initialCapacity))

    context(ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(ka.toLong(keySelector(e)), va.toLong(valueTransform(e)))}}
    context(ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline transform: (S) -> VLongLongPair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toLong(p.first), va.toLong(p.second))}}
    context(ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAllGeneric(source: CollectionVInt<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toLong(p.first), va.toLong(p.second))}}
    context(ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(ka.toLong(keySelector(e)), va.toLong(valueTransform(e)))}}
    context(ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline transform: (S) -> VLongLongPair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toLong(p.first), va.toLong(p.second))}}
    context(ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAllGeneric(source: CollectionVLong<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toLong(p.first), va.toLong(p.second))}}
}

interface MapVLongObject<K,V> {
    val collection: LongObjectMap<V>
}
class MutableMapVLongObject<K,V>(
    override val collection: MutableLongObjectMap<V> = MutableLongObjectMap())
    : MapVLongObject<K,V> {
    constructor(initialCapacity: Int) : this(MutableLongObjectMap(initialCapacity))

    context(ka: ValueLongAdapter<K>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(ka.toLong(keySelector(e)), valueTransform(e))}}
    context(ka: ValueLongAdapter<K>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toLong(p.first), p.second)}}
    context(ka: ValueLongAdapter<K>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(ka.toLong(keySelector(e)), valueTransform(e))}}
    context(ka: ValueLongAdapter<K>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(ka.toLong(p.first), p.second)}}
}


interface MapVObjectLong<K,V> {
    val collection: ObjectLongMap<K>
}
class MutableMapVObjectLong<K,V>(
    override val collection: MutableObjectLongMap<K> = MutableObjectLongMap(),
): MapVObjectLong<K,V> {
    constructor(initialCapacity: Int) : this(MutableObjectLongMap(initialCapacity))

    context(va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(keySelector(e), va.toLong(valueTransform(e)))}}
    context(va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: CollectionVInt<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(p.first, va.toLong(p.second))}}
    context(va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline keySelector: (S) -> K, crossinline valueTransform: (S) -> V) = context(sa) {source.forEach { e-> collection.put(keySelector(e), va.toLong(valueTransform(e)))}}
    context(va: ValueLongAdapter<V>, sa:ValueLongAdapter<S>) inline fun <S> putAll(source: CollectionVLong<S>, crossinline transform: (S) -> Pair<K, V>) = context(sa) {source.forEach { e-> val p = transform(e); collection.put(p.first, va.toLong(p.second))}}
}