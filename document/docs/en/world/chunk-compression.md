# Gensokyo chunk compression

**enableGensokyoChunkZstd defaults to false.** It is optional storage behavior, not a requirement for entering Gensokyo and not a terrain change.

When enabled, eligible Gensokyo terrain chunks are written using Zstd. When disabled, new writes return to the normal method; existing Zstd chunks remain readable. Toggling does not immediately rewrite the entire world.

This applies to Gensokyo terrain chunk storage, not every dimension, entity or POI file. Level defaults to 8, supported range 1–19. Restart before relying on changed writer settings.

CPU and disk results depend on the machine and workload. The project intentionally keeps it disabled by default; compare actual results before enabling it.

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/world/gensokyo/storage/GensokyoChunkCompression.java","common/src/main/java/cc/thonly/reverie_dreams/ReverieDreamsConfiguration.java"]' />
