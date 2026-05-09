package mpd.com.common.collect.valuecollections

import androidx.collection.IntSet
import androidx.collection.LongSet
import androidx.collection.MutableIntIntMap
import androidx.collection.MutableIntSet
import androidx.collection.MutableLongSet
import mpd.com.common.collect.valuecollections.MutableVIntIntMap

interface VIntSet<T>: VIntCollection<T> { 
    val collection: IntSet
}
class MutableVIntSet<T>(override val collection: MutableIntSet, ): VIntSet<T>, MutableVIntCollection<T> {
    constructor(initialCapacity: Int) : this(MutableIntSet(initialCapacity))
}


interface VLongSet<T>: VLongCollection<T> { 
    val collection: LongSet
}
class MutableVLongSet<T>(override val collection: MutableLongSet): VLongSet<T>, MutableVLongCollection<T> {
    constructor(initialCapacity: Int) : this(MutableLongSet(initialCapacity))
}
