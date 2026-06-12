# ValueCollections

Super thin wrappers around primitive collections,
that have type-safe converters for supporting Kotlin `Int` and `Long` value classes.

The vast majority of functions and methods simply `inline` that use adapters to adapt the parameters,
then call the method on the underlying container, and then use the adapter on the return type. The
aggressive inlining should eliminate any boxing, and also any unnecessary captures, so the only
overhead should be one additional allocation per collection.

`IntArray` and `LongArray` extension methods that returned `List<X>` are reimplemented to instead return 
`VIntList` or `VLongList`, to reduce allocations. These are the `filter`, `take`, `drop`, `slice`, `sorted`,
`distinct`, `intersect`, `subtract`, and `union` methods.

## Java Collections
- Pairs
    - `VIntIntPair<F,S>`
    - `VIntLongPair<F,S>`
    - `VLongIntPair<F,S>`
    - `VLongLongPair<F,S>`
- Collections
    - `interface VIntCollection<T>`
    - `interface VLongCollection<T>` (TODO)
    - `interface ModifiableVIntCollection<T>`
    - `interface ModifiableVLongCollection<T>` (TODO)
    - `interface MutableVIntCollection<T>`
    - `interface MutableVLongCollection<T>` (TODO)
- Queues 
  - `IntArrayDeque` (actual `int` implementation, not a wrapper) (TODO)
  - `LongArrayDeque` (actual `int` implementation, not a wrapper) (TODO)
  - `VIntArrayDeque<T>` (TODO)
  - `VIntArrayDeque<T>` (TODO)
  - `IntPriorityBlockingArrayQueue` (actual `int` implementation, not a wrapper)
  - `LongPriorityBlockingArrayQueue` (actual `int` implementation, not a wrapper)
  - `VIntPriorityBlockingArrayQueue<T>` (TODO)
  - `VIntPriorityBlockingArrayQueue<T>` (TODO)

## Thin AndroidX wrappers
https://github.com/androidx/androidx/tree/0a6843ad7cd148b7128d4db9e3c9299c5e58fa6d/collection/collection/src/commonMain/kotlin/androidx/collection

- Arrays
    - `class VIntArray<T>`
    - `class VLongArray<T>` (TODO)
    - `class VIntSparseArray<T>` (TODO)
    - `class VLongSparseArray<T>` (TODO)
- Lists
  - `interface VIntList<T>`
  - `interface VLongList<T>` (TODO)
  - `interface ModifiableVIntList<T>`
  - `interface ModifiableVLongList<T>` (TODO)
  - `interface MutableVIntList<T>`
  - `interface MutableVLongList<T>` (TODO)
  - `class FlatVIntList<T>`
  - `class FlatVLongList<T>` (TODO)
- Sets
    - `interface VIntSet<T>` (TODO)
    - `interface VLongSet<T>` (TODO)
    - `interface ModifiableVIntSet<T>` (TODO)
    - `interface ModifiableVLongSet<T>` (TODO)
    - `interface MutableVIntSet<T>` (TODO)
    - `interface MutableVLongSet<T>` (TODO)
    - `class FlatVIntSet<T>` (TODO)
    - `class FlatVLongSet<T>` (TODO)
- Maps 
  - `VIntDoubleMap<K>` (TODO)
  - `VIntIntMap<K,V>` (TODO)
  - `VIntLongMap<K,V>` (TODO)
  - `VIntObjectMap<K,V>` (TODO)
  - `VLongDoubleMap<K>` (TODO)
  - `VLongIntMap<K,V>` (TODO)
  - `VLongLongMap<K,V>` (TODO)
  - `VLongObjectMap<K,V>` (TODO)
  - `ObjectVIntMap<K,V>` (TODO)
  - `ObjectVLongMap<K,V>` (TODO)
