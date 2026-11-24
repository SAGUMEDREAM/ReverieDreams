import test from "node:test";
import assert from "node:assert/strict";
import { gzipSync } from "node:zlib";
import { runtimeLayerFilesFor, decodeRawSurfaceLayer } from "./runtime-layer-format.mjs";

test("palette4 stays compact; a seventeenth surface uses the full uint16 format", () => {
  const map = { biome_palette: Array(16), surface_palette: Array(16) };
  assert.equal(runtimeLayerFilesFor(map).surfaces, "surfaces.pal4.gz");
  map.surface_palette.push("new");
  assert.equal(runtimeLayerFilesFor(map).surfaces, "surfaces.bin.gz");
  assert.equal(runtimeLayerFilesFor(map).biomes, "biomes.pal4.gz");
  map.biome_palette.push("new");
  assert.equal(runtimeLayerFilesFor(map).biomes, "biomes.bin.gz");
});
test("wide surface round trip keeps indices above 15 and 255, including odd cell counts", () => {
  const values = [0, 15, 16, 18, 255, 256, 65535];
  const raw = Buffer.alloc(values.length * 2);
  values.forEach((v, i) => raw.writeUInt16LE(v, i * 2));
  assert.deepEqual(decodeRawSurfaceLayer(gzipSync(raw), values.length, "surfaces.bin.gz"), raw);
  assert.deepEqual(decodeRawSurfaceLayer(raw, values.length, "surfaces.bin"), raw);
});
test("old byte layers still expand and malformed lengths are rejected", () => {
  const raw = Buffer.from([0, 15, 255]);
  const wide = decodeRawSurfaceLayer(raw, 3, "surfaces.bin");
  assert.deepEqual([0, 1, 2].map(i => wide.readUInt16LE(i * 2)), [0, 15, 255]);
  assert.throws(() => decodeRawSurfaceLayer(Buffer.alloc(4), 3, "surfaces.bin"), /size mismatch/);
});
