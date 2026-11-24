# Asset sources

This migration pack reuses ReverieDreams assets under the repository's
GPL-3.0-or-later declaration. The root LICENSE is included by the pack build.

- The 24 shared models are copied by Gradle from
  `common/src/generated/resources/assets/reverie_dreams/`. The 25 selected PNG
  textures come from `common/src/main/resources/assets/reverie_dreams/`.
  The exact source paths are recorded in `paper/src/main/craftengine/shared-assets.json`.
- `models/block/chair.json` is adapted from the common main resource model.
  Its unresolved `table` parent is replaced with `minecraft:block/block` and
  the Blockbench `format_version` field is removed. Geometry, UVs, display
  transforms and the existing Blockbench credit are retained.
- The four language files contain only the 25 migrated translation keys,
  extracted from the existing main/generated language files. English names are
  used as fallback when a locale has no existing value.
- Mineral recipes and loot are adapted from the generated Mod data under
  `common/src/generated/resources/data/reverie_dreams/recipe/` and
  `loot_table/blocks/`. The source recipe list is recorded in
  `migration/minerals-batch.json`. The source cooking time, experience, loot
  conditions, independent bonus rolls and weighted selection are preserved.

The common resources remain unchanged. This pack does not include the third-party
skin, Fumo, hat or OST collections listed in the repository's CREDIT file.
