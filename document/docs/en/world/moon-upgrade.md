# Moon updates and existing saves

The current Moon expands underground height and adds terrain regions. Existing areas need compatibility handling.

Converted chunks retain their surface, builds, entities and container data while lower sections and required height/light metadata are updated. Unexplored areas use the new generator; existing terrain is not promised new structures retroactively.

## Upgrade process

1. Stop the world/server and back up the **whole world**, including world metadata and the Moon dimension.
2. Start with matching mod/loader versions and let conversion finish. Do not remove staging files during the process.
3. Check for “Moon upgrade complete” in the log, then inspect important builds, containers and entities.

Automatic conversion stores originals and related metadata under **backups/moon-terrain-v2** inside the world. Its version marker prevents repeated conversion; it does not replace a full pre-upgrade backup.

Custom generator or dimension-height datapacks may be unsupported and cause migration to refuse the combination. Inspect the log and test on a world copy; do not bypass this by deleting version markers or clearing the Moon.

Moon migration and Gensokyo Zstd storage are independent mechanisms.

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/world/moon/compat/MoonSaveUpgrade.java","common/src/main/java/cc/thonly/reverie_dreams/world/moon/compat/MoonChunkUpgrade.java"]' />
