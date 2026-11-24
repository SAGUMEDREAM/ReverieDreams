import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { readStructure } from "./nbt.mjs";

const here = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(here, "..");

test("structure NBT exposes template entities for preview and scaling", () => {
  const file = path.join(
    repoRoot,
    "common",
    "src",
    "main",
    "resources",
    "data",
    "reverie_dreams",
    "structure",
    "mini_bar.nbt",
  );
  const structure = readStructure(readFileSync(file));
  assert.ok(structure);
  assert.equal(structure.size.length, 3);
  assert.ok(structure.blocks.length > 0);
  assert.ok(structure.entities.length > 0);
  assert.equal(structure.entities[0].pos.length, 3);
  assert.ok(structure.entities[0].id.includes(":"));
});