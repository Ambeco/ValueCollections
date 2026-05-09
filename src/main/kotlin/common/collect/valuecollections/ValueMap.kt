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
}


interface VIntLongMap<K,V> {
    val collection: IntLongMap
}
class MutableVIntLongMap<K,V>(
    override val collection: MutableIntLongMap = MutableIntLongMap(),
): VIntLongMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableIntLongMap(initialCapacity))
}


interface VIntObjectMap<K,V> {
    val collection: IntObjectMap<V>
}
class MutableVIntObjectMap<K,V>(
    override val collection: MutableIntObjectMap<V> = MutableIntObjectMap(),
): VIntObjectMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableIntObjectMap(initialCapacity))
}


interface VObjectIntMap<K,V> {
    val collection: ObjectIntMap<K>
}
class MutableVObjectIntMap<K,V>(
    override val collection: MutableObjectIntMap<K> = MutableObjectIntMap(),
): VObjectIntMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableObjectIntMap(initialCapacity))
}


interface VLongIntMap<K,V> {
    val collection: LongIntMap
}
class MutableVLongIntMap<K,V>(
    override val collection: MutableLongIntMap = MutableLongIntMap())
    : VLongIntMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableLongIntMap(initialCapacity))

}

interface VLongLongMap<K,V> {
    val collection: LongLongMap
}
class MutableVLongLongMap<K,V>(
    override val collection: MutableLongLongMap = MutableLongLongMap())
    : VLongLongMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableLongLongMap(initialCapacity))
}

interface VLongObjectMap<K,V> {
    val collection: LongObjectMap<V>
}
class MutableVLongObjectMap<K,V>(
    override val collection: MutableLongObjectMap<V> = MutableLongObjectMap())
    : VLongObjectMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableLongObjectMap(initialCapacity))
}


interface VObjectLongMap<K,V> {
    val collection: ObjectLongMap<K>
}
class MutableVObjectLongMap<K,V>(
    override val collection: MutableObjectLongMap<K> = MutableObjectLongMap(),
): VObjectLongMap<K,V> {
    constructor(initialCapacity: Int) : this(MutableObjectLongMap(initialCapacity))
}