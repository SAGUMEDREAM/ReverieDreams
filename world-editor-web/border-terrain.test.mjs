import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { createBorderTerrain, substrate } from './public/border-terrain.js';

const settings = JSON.parse(await readFile(new URL('../common/src/main/resources/data/reverie_dreams/gensokyo/border-settings.json', import.meta.url), 'utf8'));
const vectors = JSON.parse(await readFile(new URL('../common/src/test/resources/data/reverie_dreams/gensokyo/border-parity.json', import.meta.url), 'utf8'));

test('editor matches 2400 production Java samples across edges, corners, water, cold and negative coordinates', () => {
  let count = 0;
  for (const f of vectors.fixtures) {
    const terrain = createBorderTerrain(f.map, f.heights, f.surfaces, f.biomes, settings);
    for (const expected of f.samples) {
      const result = terrain.sample(expected.x, expected.z);
      for (const key of ['groundY', 'waterY', 'surface', 'biome']) assert.equal(result[key], expected[key], `${key} at ${expected.x},${expected.z}`);
      count++;
    }
  }
  assert.equal(count, 2400);
});

test('peripheral preview never changes authored layers or metadata', () => {
  for (const fixture of vectors.fixtures) {
    const before = JSON.stringify(fixture);
    const terrain = createBorderTerrain(fixture.map, fixture.heights, fixture.surfaces, fixture.biomes, settings);
    for (const { x, z } of fixture.samples) terrain.sample(x, z);
    assert.equal(JSON.stringify(fixture), before);
  }
});

test('farmland and flowers are not terrain surfaces; settings must match the supported schema', () => {
  assert.equal(substrate('minecraft:poppy'), 'minecraft:grass_block');
  assert.equal(substrate('reverie_dreams:paddy_field'), 'minecraft:grass_block');
  assert.throws(() => createBorderTerrain({}, [], [], [], { version: 1 }), /版本/);
});
