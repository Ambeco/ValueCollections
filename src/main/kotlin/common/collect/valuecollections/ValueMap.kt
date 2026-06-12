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

interface VIntIntMap<K,V> { 
    val collection: IntIntMap
}
class MutableVIntIntMap<K,V>(
    override val collection: MutableIntIntMap = MutableIntIntMap(),
): VIntIntMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableIntIntMap(initialCapacity))

    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, keySelector: (S) -> K, valueTransform: (S) -> V) = context(sa) {source.forEach {e-> collection.put(ka.toInt(keySelector(e)), va.toInt(valueTransform(e)))}}
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, transform: (S) -> VIntIntPair<K, V>) = context(sa) {source.forEach {e-> val p = transform(e); collection.put(ka.toInt(p.first), va.toInt(p.second))}}
    context(ka: ValueIntAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAllGeneric(source: VIntCollection<S>, transform: (S) -> Pair<K, V>) = context(sa) {source.forEach {e-> val p = transform(e); collection.put(ka.toInt(p.first), va.toInt(p.second))}}
}


interface VIntLongMap<K,V> {
    val collection: IntLongMap
}
class MutableVIntLongMap<K,V>(
    override val collection: MutableIntLongMap = MutableIntLongMap(),
): VIntLongMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableIntLongMap(initialCapacity))

    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, keySelector: (S) -> K, valueTransform: (S) -> V) = context(sa) {source.forEach {e-> collection.put(ka.toInt(keySelector(e)), va.toLong(valueTransform(e)))}}
    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, transform: (S) -> VIntLongPair<K, V>) = context(sa) {source.forEach {e-> val p = transform(e); collection.put(ka.toInt(p.first), va.toLong(p.second))}}
    context(ka: ValueIntAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAllGeneric(source: VIntCollection<S>, transform: (S) -> Pair<K, V>) = context(sa) {source.forEach {e-> val p = transform(e); collection.put(ka.toInt(p.first), va.toLong(p.second))}}
}


interface VIntObjectMap<K,V> {
    val collection: IntObjectMap<V>
}
class MutableVIntObjectMap<K,V>(
    override val collection: MutableIntObjectMap<V> = MutableIntObjectMap(),
): VIntObjectMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableIntObjectMap(initialCapacity))

    context(ka: ValueIntAdapter<K>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, keySelector: (S) -> K, valueTransform: (S) -> V) = context(sa) {source.forEach {e-> collection.put(ka.toInt(keySelector(e)), valueTransform(e))}}
    context(ka: ValueIntAdapter<K>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, transform: (S) -> Pair<K, V>) = context(sa) {source.forEach {e-> val p = transform(e); collection.put(ka.toInt(p.first), p.second)}}
}


interface VObjectIntMap<K,V> {
    val collection: ObjectIntMap<K>
}
class MutableVObjectIntMap<K,V>(
    override val collection: MutableObjectIntMap<K> = MutableObjectIntMap(),
): VObjectIntMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableObjectIntMap(initialCapacity))
    
    context(va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, keySelector: (S) -> K, valueTransform: (S) -> V) = context(sa) {source.forEach {e-> collection.put(keySelector(e), va.toInt(valueTransform(e)))}}
    context(va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, transform: (S) -> Pair<K, V>) = context(sa) {source.forEach {e-> val p = transform(e); collection.put(p.first, va.toInt(p.second))}}
}


interface VLongIntMap<K,V> {
    val collection: LongIntMap
}
class MutableVLongIntMap<K,V>(
    override val collection: MutableLongIntMap = MutableLongIntMap())
    : VLongIntMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableLongIntMap(initialCapacity))

    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, keySelector: (S) -> K, valueTransform: (S) -> V) = context(sa) {source.forEach {e-> collection.put(ka.toLong(keySelector(e)), va.toInt(valueTransform(e)))}}
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, transform: (S) -> VLongIntPair<K, V>) = context(sa) {source.forEach {e-> val p = transform(e); collection.put(ka.toLong(p.first), va.toInt(p.second))}}
    context(ka: ValueLongAdapter<K>, va: ValueIntAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAllGeneric(source: VIntCollection<S>, transform: (S) -> Pair<K, V>) = context(sa) {source.forEach {e-> val p = transform(e); collection.put(ka.toLong(p.first), va.toInt(p.second))}}
}

interface VLongLongMap<K,V> {
    val collection: LongLongMap
}
class MutableVLongLongMap<K,V>(
    override val collection: MutableLongLongMap = MutableLongLongMap())
    : VLongLongMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableLongLongMap(initialCapacity))

    context(ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, keySelector: (S) -> K, valueTransform: (S) -> V) = context(sa) {source.forEach {e-> collection.put(ka.toLong(keySelector(e)), va.toLong(valueTransform(e)))}}
    context(ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, transform: (S) -> VLongLongPair<K, V>) = context(sa) {source.forEach {e-> val p = transform(e); collection.put(ka.toLong(p.first), va.toLong(p.second))}}
    context(ka: ValueLongAdapter<K>, va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAllGeneric(source: VIntCollection<S>, transform: (S) -> Pair<K, V>) = context(sa) {source.forEach {e-> val p = transform(e); collection.put(ka.toLong(p.first), va.toLong(p.second))}}
}

interface VLongObjectMap<K,V> {
    val collection: LongObjectMap<V>
}
class MutableVLongObjectMap<K,V>(
    override val collection: MutableLongObjectMap<V> = MutableLongObjectMap())
    : VLongObjectMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableLongObjectMap(initialCapacity))

    context(ka: ValueLongAdapter<K>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, keySelector: (S) -> K, valueTransform: (S) -> V) = context(sa) {source.forEach {e-> collection.put(ka.toLong(keySelector(e)), valueTransform(e))}}
    context(ka: ValueLongAdapter<K>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, transform: (S) -> Pair<K, V>) = context(sa) {source.forEach {e-> val p = transform(e); collection.put(ka.toLong(p.first), p.second)}}
}


interface VObjectLongMap<K,V> {
    val collection: ObjectLongMap<K>
}
class MutableVObjectLongMap<K,V>(
    override val collection: MutableObjectLongMap<K> = MutableObjectLongMap(),
): VObjectLongMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableObjectLongMap(initialCapacity))

    context(va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, keySelector: (S) -> K, valueTransform: (S) -> V) = context(sa) {source.forEach {e-> collection.put(keySelector(e), va.toLong(valueTransform(e)))}}
    context(va: ValueLongAdapter<V>, sa:ValueIntAdapter<S>) inline fun <S> putAll(source: VIntCollection<S>, transform: (S) -> Pair<K, V>) = context(sa) {source.forEach {e-> val p = transform(e); collection.put(p.first, va.toLong(p.second))}}
}