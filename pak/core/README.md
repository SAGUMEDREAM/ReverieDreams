# PAK core

Public Java 25 API, runtime loader and PAK2 codec for Reverie Dreams. The main
project compiles this module from source. `common` depends on `:pak-core`, and the
Fabric/NeoForge source sets include its `clientRuntime` sources.

`NativePak` calls a prebuilt JNI library from `META-INF/native/<platform>/`.
The public build uses the supplied native binaries and encrypted resources in
`common/src/main/resources`. Native compilation and asset encryption are author
operations in separate private modules.

The native library descriptors, SHA-256 digests, build profiles, platform coverage
and public slot lists are checked by `gradle/native-resources.gradle`, which both
client loaders apply during resource processing.

`pak/native` and `pak/cli` are private, locally ignored directories. Gradle includes
`:pak-cli` when its local build file is present. A public checkout needs only this
core directory for the PAK Java module.

Core Java checks use `:pak-core:test`. Actual JNI tests additionally require an
explicit fixture resource tree. `:pak-core:testNativeEmpty` requires an explicit
empty-key native resource tree. These optional tests use synthetic data.

The `clientRuntime` source set excludes the Java author codec and key registry.
Production clients decrypt using JNI. Production keys are provisioned outside
the public repository.
