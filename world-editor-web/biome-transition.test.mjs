import test from "node:test";
import assert from "node:assert/strict";
import {
  hash32,
  transitionBiomeIndex,
  transitionOffset,
} from "./public/biome-transition.js";

function halfMap(width, depth, transition = 0) {
  const biomes = new Uint8Array(width * depth);
  for (let z = 0; z < depth; z++) {
    for (let x = 0; x < width; x++) {
      biomes[z * width + x] = x < width / 2 ? 0 : 1;
    }
  }
  return {
    map: {
      width,
      depth,
      origin: [-Math.floor(width / 2), -Math.floor(depth / 2)],
      biome_transition: transition,
    },
    biomes,
  };
}

test("transition width 0 keeps the exact biome", () => {
  const { map, biomes } = halfMap(8, 8, 0);
  for (let z = 0; z < 8; z++) {
    for (let x = 0; x < 8; x++) {
      assert.equal(transitionBiomeIndex(map, biomes, x, z), biomes[z * 8 + x]);
    }
  }
});

test("transition offsets stay inside the configured radius and are deterministic", () => {
  for (const width of [0, 1, 2, 8, 32, 64]) {
    for (let i = 0; i < 200; i++) {
      const x = i * 17 - 500;
      const z = i * -31 + 900;
      const dx = transitionOffset(x, z, 0, width);
      const dz = transitionOffset(x, z, 1, width);
      assert.ok(dx >= -width && dx <= width, `dx ${dx} for width ${width}`);
      assert.ok(dz >= -width && dz <= width, `dz ${dz} for width ${width}`);
      assert.equal(dx, transitionOffset(x, z, 0, width));
      assert.equal(dz, transitionOffset(x, z, 1, width));
    }
  }
  assert.equal(typeof hash32(1, 2, 0), "number");
});

test("transition mixes both sides near a boundary and keeps far cells stable", () => {
  const { map, biomes } = halfMap(16, 16, 2);
  let sawLeft = false;
  let sawRight = false;
  for (let z = 0; z < 16; z++) {
    for (let x = 5; x <= 10; x++) {
      const value = transitionBiomeIndex(map, biomes, x, z);
      if (value === 0) sawLeft = true;
      if (value === 1) sawRight = true;
    }
  }
  assert.equal(sawLeft, true);
  assert.equal(sawRight, true);

  for (let z = 0; z < 16; z++) {
    for (let x = 0; x <= 1; x++) {
      assert.equal(transitionBiomeIndex(map, biomes, x, z), 0);
    }
    for (let x = 14; x <= 15; x++) {
      assert.equal(transitionBiomeIndex(map, biomes, x, z), 1);
    }
  }
});

test("out-of-bounds and missing transition fields are safe", () => {
  const { map, biomes } = halfMap(8, 8, 0);
  map.biome_transition = undefined;
  assert.equal(transitionBiomeIndex(map, biomes, 3, 3), 0);
  assert.equal(transitionBiomeIndex(map, biomes, -1, 0), 0);
  assert.equal(transitionBiomeIndex(map, biomes, 8, 0), 0);
  assert.equal(transitionBiomeIndex(null, biomes, 0, 0), 0);
});