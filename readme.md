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

## Thin Java wrappers
- `VIntConsumer<T>`
- `VLongConsumer<T>`
- `VIntIterator<T>`
- `VLongIterator<T>`
- `VIntIterable<T>`
- `VLongIterable<T>`
- `Value32Stream<T>`
- `Value64Stream<T>`

## Thin AndroidX wrappers
https://github.com/androidx/androidx/tree/0a6843ad7cd148b7128d4db9e3c9299c5e58fa6d/collection/collection/src/commonMain/kotlin/androidx/collection
- `Value32FloatMap<K>` 
- `Value32Value32Map<K,V>` 
- `Value32Value32Pair<F,S>` 
- `Value32ArrayList<T>` 
- `Value32Value64Map<K,V>`
- `Value32ObjectMap<K,V>`
- `Value32ArraySet<T>`
- `Value32SparseArray<T>`
- `Value64FloatMap<K>`
- `Value64Value32Map<K,V>`
- `Value64ArrayList<T>`
- `Value64Value64Map<K,V>`
- `Value64Value64Pair<F,S>`
- `Value64ObjectMap<K,V>`
- `Value64ArraySet<T>`
- `Value64SparseArray<T>`
- `ObjectValue32Map<V>`
- `ObjectValue64Map<V>`

## Extended Operations

### Collections
- `Value32PrimitiveIterator`
- `Value64PrimitiveIterator`
- `IntArrayDeque`
- `LongArrayDeque`
- `Value32ArrayDeque`
- `Value32ArrayDeque`
- `IntPriorityBlockingArrayDeque`
- `LongPriorityBlockingArrayDeque`
- `Value32PriorityBlockingArrayDeque`
- `Value32PriorityBlockingArrayDeque`

### Functions
- `Value32Supplier`
- `Value64Supplier`
- `Value32Consumer`
- `Value64Consumer`
- `Value32FloatFunction`
- `Value64FloatFunction`
- `Value32DoubleFunction`
- `Value64DoubleFunction`
- `Value32Value32Function`
- `Value64Value32Function`
- `Value32Value64Function`
- `Value64Value64Function`
- `Value32Value32Value32Function`
- `Value64Value32Value32Function`
- `Value32Value64Value32Function`
- `Value64Value64Value32Function`
- `Value32Value32Value64Function`
- `Value64Value32Value64Function`
- `Value32Value64Value64Function`
- `Value64Value64Value64Function`
