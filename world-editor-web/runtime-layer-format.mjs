import { gunzipSync } from "node:zlib";

export function runtimeLayerFilesFor(map) {
  return {
    heightmap: "heightmap.delta.gz",
    biomes: map.biome_palette?.length > 16 ? "biomes.bin.gz" : "biomes.pal4.gz",
    surfaces: map.surface_palette?.length > 16 ? "surfaces.bin.gz" : "surfaces.pal4.gz",
  };
}

/** Decode the non-pal4 formats: old byte/raw layers and full uint16 gzip. */
export function decodeRawSurfaceLayer(packed, expected, filename) {
  const raw = filename.endsWith(".gz") ? gunzipSync(packed) : packed;
  if (raw.length === expected * 2) return raw;
  if (raw.length === expected) {
    const result = Buffer.alloc(expected * 2);
    for (let i = 0; i < expected; i++) result.writeUInt16LE(raw[i], i * 2);
    return result;
  }
  throw new Error(`surfaces size mismatch: ${raw.length}`);
}
