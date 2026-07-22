# ValueCollections

Super thin wrappers around primitive collections,
that have type-safe converters for supporting Kotlin `Int` and `Long` value classes.

The vast majority of functions and methods simply `inline` that use adapters to adapt the parameters,
then call the method on the underlying container, and then use the adapter on the return type. The
aggressive inlining should eliminate any boxing, and also any unnecessary captures, so the only
overhead should be one additional allocation per collection.

`IntArray` and `LongArray` extension methods that returned `List<X>` are reimplemented to instead return 
`VIntList` or `ListVLong`, to reduce allocations. These are the `filter`, `take`, `drop`, `slice`, `sorted`,
`distinct`, `intersect`, `subtract`, and `union` methods.

## Java Collections
- Pairs
    - `PairVIntDouble<F,S>` (TODO)
    - `PairVIntFloat<F,S>` (TODO)
    - `PairVIntInt<F,S>`
    - `PairVIntLong<F,S>`
    - `PairVIntObject<F,S>`
    - `PairVLongDouble<F,S>` (TODO)
    - `PairVLongFloat<F,S>` (TODO)
    - `PairVLongInt<F,S>`
    - `PairVLongLong<F,S>`
    - `PairVLongObject<F,S>`
    - `PairVObjectInt<F,S>`
    - `PairVObjectLong<F,S>`
- Collections (~95% Implemented)
    - `interface CollectionVInt<T>`
    - `interface CollectionVLong<T>`
    - `interface ModifiableCollectionVInt<T>` 
    - `interface ModifiableCollectionVLong<T>`
    - `interface MutableCollectionVInt<T>` 
    - `interface MutableCollectionVLong<T>`
- Queues  (TODO)
  - `ArrayDequeInt` (actual `int` implementation, not a wrapper)
  - `ArrayDequeLong` (actual `int` implementation, not a wrapper)
  - `ArrayDequeVInt<T>`
  - `ArrayDequeVInt<T>`
  - `PriorityBlockingArrayQueueInt` (actual `int` implementation, not a wrapper)
  - `PriorityBlockingArrayQueueLong` (actual `int` implementation, not a wrapper)
  - `PriorityBlockingArrayQueueVInt<T>` 
  - `PriorityBlockingArrayQueueVLong<T>`

## Thin AndroidX wrappers
https://github.com/androidx/androidx/tree/0a6843ad7cd148b7128d4db9e3c9299c5e58fa6d/collection/collection/src/commonMain/kotlin/androidx/collection

- Arrays
  - `class ArrayVInt<T>`
  - `class ArrayVLong<T>`
  - `class SparseArrayVInt<T>` (TODO)
  - `class SparseArrayVLong<T>` (TODO)
- Lists (~95% Implemented)
  - `class MutableVIntList<T>`
  - `class ArrayListVLong<T>`
  - `class CircularArrayVInt<T>` (TODO)
  - `class CircularArrayVLong<T>` (TODO)
  - `interface VIntList<T>`
  - `interface ListVLong<T>` 
  - `interface VIntList<T>`
  - `interface ModifiableListVLong<T>`
  - `interface MutableVIntList<T>`
  - `interface MutableListVLong<T>`
- Sets  (~95% Implemented)
  - `class ArraySetVInt<T>`
  - `class ArraySetVLong<T>`
  - `interface ModifiableSetVInt<T>`
  - `interface ModifiableSetVLong<T>`
  - `interface MutableSetVInt<T>`
  - `interface MutableSetVLong<T>`
  - `interface SetVInt<T>`
  - `interface SetVLong<T>`
- Maps  (~25% Implemented)
  - `MapVDoubleInt<V>` (TODO)
  - `MapVDoubleLong<V>` (TODO)
  - `MapVIntDouble<K>` (TODO)
  - `MapVIntFloat<K>` (TODO)
  - `MapVIntInt<K,V>`
  - `MapVIntLong<K,V>`
  - `MapVIntObject<K,V>`
  - `MapVFloatInt<V>` (TODO)
  - `MapVFloatLong<V>` (TODO)
  - `MapVLongDouble<K>` (TODO)
  - `MapVLongFloat<K>` (TODO)
  - `MapVLongInt<K,V>`
  - `MapVLongLong<K,V>`
  - `MapVLongObject<K,V>`
  - `MapVObjectInt<K,V>`
  - `MapVObjectLong<K,V>`
