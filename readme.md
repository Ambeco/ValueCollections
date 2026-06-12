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
- Collections (Declared, ~76% Implemented)
    - `interface VIntCollection<T>`
    - `interface VLongCollection<T>`
    - `interface ModifiableVIntCollection<T>` 
    - `interface ModifiableVLongCollection<T>`
    - `interface MutableVIntCollection<T>` 
    - `interface MutableVLongCollection<T>`
- Queues  (TODO)
  - `IntArrayDeque` (actual `int` implementation, not a wrapper)
  - `LongArrayDeque` (actual `int` implementation, not a wrapper)
  - `VIntArrayDeque<T>`
  - `VIntArrayDeque<T>`
  - `IntPriorityBlockingArrayQueue` (actual `int` implementation, not a wrapper)
  - `LongPriorityBlockingArrayQueue` (actual `int` implementation, not a wrapper)
  - `VIntPriorityBlockingArrayQueue<T>` 
  - `VIntPriorityBlockingArrayQueue<T>`

## Thin AndroidX wrappers
https://github.com/androidx/androidx/tree/0a6843ad7cd148b7128d4db9e3c9299c5e58fa6d/collection/collection/src/commonMain/kotlin/androidx/collection

- Arrays
    - `class VIntArray<T>`
    - `class VLongArray<T>`
    - `class VIntSparseArray<T>` (TODO)
    - `class VLongSparseArray<T>` (TODO)
- Lists (Declared, ~78% Implemented)
  - `interface VIntList<T>`
  - `interface VLongList<T>` 
  - `interface ModifiableVIntList<T>`
  - `interface ModifiableVLongList<T>`
  - `interface MutableVIntList<T>`
  - `interface MutableVLongList<T>`
  - `class FlatVIntList<T>`
  - `class FlatVLongList<T>`
- Sets (TODO)
    - `interface VIntSet<T>`
    - `interface VLongSet<T>`
    - `interface ModifiableVIntSet<T>` 
    - `interface ModifiableVLongSet<T>` 
    - `interface MutableVIntSet<T>` 
    - `interface MutableVLongSet<T>` 
    - `class FlatVIntSet<T>`
    - `class FlatVLongSet<T>`
- Maps  (TODO)
  - `VIntDoubleMap<K>`
  - `VIntIntMap<K,V>`
  - `VIntLongMap<K,V>`
  - `VIntObjectMap<K,V>`
  - `VLongDoubleMap<K>`
  - `VLongIntMap<K,V>`
  - `VLongLongMap<K,V>`
  - `VLongObjectMap<K,V>`
  - `ObjectVIntMap<K,V>`
  - `ObjectVLongMap<K,V>`
