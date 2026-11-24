import test from "node:test";
import assert from "node:assert/strict";
import { gzipSync } from "node:zlib";
import { assertRoadResizeSafe, validateRoadNetwork } from "./road-network.mjs";

function checkpoint(rows = [[5, 1030, 1031, 1035, 1], [20, 1030, 1033, 1035, 4]]) {
  const bytes = Buffer.alloc(24 + rows.length * 11);
  bytes.write("RDR1");
  [16, 16, -16, 32, rows.length].forEach((v, i) => bytes.writeInt32BE(v, 4 + i * 4));
  rows.forEach(([index, y, lo, hi, role], i) => {
    const at = 24 + i * 11;
    bytes.writeInt32BE(index, at); bytes.writeInt16BE(y, at + 4);
    bytes.writeInt16BE(lo, at + 6); bytes.writeInt16BE(hi, at + 8); bytes[at + 10] = role;
  });
  return { width: 16, depth: 16, origin: [-16, 32], min_y: 0, world_height: 1536,
    road_network: { version: 1, encoding: "gzip-base64-rdr1", columns: rows.length, data: gzipSync(bytes).toString("base64") } };
}

test("legacy map remains valid; a normal JSON save preserves road metadata", () => {
  assert.deepEqual(validateRoadNetwork({}), []);
  assert.deepEqual(validateRoadNetwork(JSON.parse(JSON.stringify(checkpoint()))), []);
});
test("dimension/origin changes cannot silently reinterpret the same road indices", () => {
  for (const override of [{ width: 32 }, { depth: 32 }, { origin: [0, 0] }]) {
    assert.equal(validateRoadNetwork({ ...checkpoint(), ...override }).length, 1);
  }
});
test("malformed, truncated, unsorted or overflowing road data is rejected", () => {
  for (const rows of [[[5, 1030, 1031, 1035, 1], [5, 1030, 1031, 1035, 1]],
    [[-1, 1030, 1031, 1035, 1]], [[256, 1030, 1031, 1035, 1]],
    [[5, 1030, 1030, 1035, 1]], [[5, 1030, 1031, 1536, 1]], [[5, 1030, 1031, 1035, 5]]]) {
    assert.equal(validateRoadNetwork(checkpoint(rows)).length, 1);
  }
  const map = checkpoint(); map.road_network.columns = 1;
  assert.equal(validateRoadNetwork(map).length, 1);
  map.road_network.data = "!!!!";
  assert.equal(validateRoadNetwork(map).length, 1);
});
test("resize only requires a road migration when dimensions actually change", () => {
  assert.doesNotThrow(() => assertRoadResizeSafe({}, 32, 32));
  assert.doesNotThrow(() => assertRoadResizeSafe(checkpoint(), 16, 16));
  assert.throws(() => assertRoadResizeSafe(checkpoint(), 32, 16), /道路/);
});
