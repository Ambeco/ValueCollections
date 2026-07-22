# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

ValueCollections provides thin, zero(-ish)-overhead wrapper collections around `androidx.collection`'s
primitive collections (`MutableIntList`, `MutableIntSet`, `MutableIntIntMap`, etc.), giving type-safe
`List`/`Set`/`Map`/`Collection`-style APIs for Kotlin inline/value classes backed by `Int` or `Long`
primitives, without boxing. See `readme.md` for the full inventory of implemented/TODO types.

## Build & test commands

- Build: `./gradlew build` (Windows: `gradlew.bat build`)
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "mpd.common.collect.valuecollections.ValueCollectionTest"`
- Run a single test method: `./gradlew test --tests "mpd.common.collect.valuecollections.ValueCollectionTest.any"`

Tests use JUnit 5 (`useJUnitPlatform()`) plus `kotlin.test`. Kotlin toolchain is JVM 21, Kotlin
`2.4.20-Beta1` (pinned for specific compiler bugfixes — see comment in `build.gradle.kts`).

**Compiler flag**: the build passes `-Xcontext-parameters` (Kotlin's new context-parameter feature,
different from the older/deprecated context *receivers*). Nearly every extension function in this
codebase depends on it — don't remove it, and don't confuse `context(a: X)` syntax with `context(this: X)`.

## Core architecture: bits + adapters

Every "V-collection" stores raw primitives (`IntBits`/`LongBits`, which are just `typealias`es for
`Int`/`Long`) and converts to/from the logical element type `T` via a `ValueIntAdapter<T>` /
`ValueLongAdapter<T>` (`ValueBitAdapter.kt`). An adapter is normally the `companion object` of the
value class it converts, e.g.:

```kotlin
@JvmInline
value class MyId(val value: Int) {
    companion object : ValueIntAdapter<MyId> {
        override inline fun fromInt(v: Int) = MyId(v)
        override inline fun toInt(v: MyId): Int = v.value
    }
}
```

Adapters are threaded through the API via Kotlin **context parameters** (`context(a: ValueIntAdapter<T>)`),
not as explicit function arguments. Callers bring the adapter into scope with `with(MyId) { ... }` —
see `ValueCollectionTest.kt` for the pattern. This is why almost everything is `inline`: the adapter
calls are expected to fully inline away, leaving only the primitive collection operations.

Each collection interface therefore has two API layers:
1. **Bits layer** — primitive, adapter-free operations on raw `IntBits`/`LongBits` (`anyBits`,
   `containsBits`, `forEachBits`, `addBits`, ...). These are the actual abstract members / minimal
   surface each implementation must provide.
2. **Typed layer** — `context(a: ValueIntAdapter<T>)` extension functions (in files like
   `CollectionVInt.kt`) that build the familiar Kotlin-collections API (`map`, `filter`, `associate`,
   `fold`, `sortedBy`, ...) on top of the bits layer by converting through the adapter.

`NULL_VALUE` (or `NULL_KEY_BITS`/`NULL_VALUE_BITS` for maps) is a sentinel bit pattern (defaults to
`Int.MIN_VALUE`) used to represent "no result" (e.g. from `find`, `singleOrNull`) without an
`Optional`/nullable-boxing allocation. It's configurable per-instance in case `Int.MIN_VALUE` is a
valid element value.

## Naming conventions

- **`V<Type>` is a suffix, not a prefix** on interface names (e.g. `VIntList`, `SetVLong`,
  `MapVIntLong`) — this was deliberately renamed from an earlier `V*` prefix convention. For two-type
  containers (maps, pairs), the order is `<Key><Value>`, e.g. `MapVIntLong<K,V>` = int keys, long
  values wrapped as `K`/`V`; `PairVIntObj<T,U>` = int-backed first, object second.
- Interface hierarchy mirrors `androidx.collection`/Kotlin stdlib capability levels, from read-only to
  read-write:
  `CollectionVInt` → `VIntIndexedCollection` (adds index access) → `VIntList` →
  `ModifiableVIntList` (in-place `set`, no resize) → `MutableVIntList` (add/remove, resize).
  The same `Collection → Modifiable → Mutable` progression applies to sets and maps.
- Concrete implementations are prefixed by their backing structure, e.g. `ArrayVIntList` (wraps
  `MutableIntList`), `ArraySetVInt` (wraps `MutableIntSet`), `HashMapVIntInt` (wraps
  `MutableIntIntMap`).
- `Pair*`/`Indexed*` value types (`PairVIntInt`, `IndexedVLong`, ...) follow the same `<Key><Value>`
  ordering. Only the all-primitive combination (`PairVIntInt`) is a `@JvmInline value class`, since
  it's immutable and packs both fields into one `Long`; combinations involving an object component or
  requiring mutability are `data class`es instead.

## Known quirks (don't "fix" without checking first)

- Source files declare `package mpd.com.common.collect.valuecollections` (main) vs
  `package mpd.common.collect.valuecollections` (test, no `com`) — neither matches the actual
  directory layout (`src/main/kotlin/common/collect/valuecollections`, no `mpd`/`com` directories).
  This mismatch is pre-existing; match whichever package a file already declares rather than the
  directory path when adding new files nearby.
- `PriorityBlockingArrayQueueInt.kt` / `PriorityBlockingArrayQueueLong.kt` still carry
  `package com.unciv.utils` — leftover from wherever they were ported from. They're unfinished stubs
  (Queues are marked TODO in `readme.md`), not yet adapted to this project's conventions.
- `IntArray`/`LongArray` extension methods that would normally return a boxed `List<T>` (`filter`,
  `take`, `drop`, `slice`, `sorted`, `distinct`, `intersect`, `subtract`, `union`) are reimplemented
  here to return `VIntList`/`ListVLong` instead, to avoid the allocation — don't "simplify" these back
  to stdlib equivalents.
