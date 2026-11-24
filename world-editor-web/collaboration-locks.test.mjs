import assert from "node:assert/strict";
import test from "node:test";
import { spawn } from "node:child_process";
import { setTimeout as delay } from "node:timers/promises";
import { CellLeases, validateCells, validateTerrainOperation } from "./collaboration-locks.mjs";

test("overlapping acquisition is atomic and expired leases cannot authorize writes", () => {
  let now = 0;
  const leases = new CellLeases(100, () => now);
  assert.deepEqual(leases.take("A", [{ x: 1, z: 1 }]).denied, []);
  const conflict = leases.take("B", [{ x: 2, z: 2 }, { x: 1, z: 1 }]);
  assert.equal(conflict.granted.length, 0);
  assert.equal(leases.owns("B", [{ x: 2, z: 2 }]), false);
  now = 80;
  leases.take("A", [{ x: 1, z: 1 }]);
  now = 120;
  assert.equal(leases.owns("A", [{ x: 1, z: 1 }]), true);
  now = 181;
  assert.equal(leases.owns("A", [{ x: 1, z: 1 }]), false);
  leases.take("B", [{ x: 1, z: 1 }]);
  leases.release("A", [{ x: 1, z: 1 }]);
  assert.equal(leases.owns("B", [{ x: 1, z: 1 }]), true);
  leases.release("B");
  assert.deepEqual(leases.list(), []);
});

test("cell and operation validation rejects invalid coordinates and values", () => {
  const map = { width: 64, depth: 64, min_y: 0, world_height: 1024 };
  assert.throws(() => validateCells([{ x: 64, z: 1 }], map));
  assert.throws(() => validateCells([{ x: 0.5, z: 1 }], map));
  assert.throws(() => validateTerrainOperation({ changes: [{ index: 4096, h: 40, b: 0, s: 1 }] }, map));
  assert.throws(() => validateTerrainOperation({ changes: [{ index: 0, h: 1024, b: 0, s: 1 }] }, map));
  assert.deepEqual(validateTerrainOperation({ undergroundChanges: [{ key: "1,20,2", block: null }] }, map), [{ x: 1, z: 2 }]);
});

test("HTTP: connected peers contend, unowned commits are refused, disconnect releases leases", { timeout: 20000 }, async () => {
  const port = 33219;
  const child = spawn(process.execPath, ["server.mjs"], {
    cwd: new URL(".", import.meta.url),
    env: { ...process.env, HOST: "127.0.0.1", PORT: String(port) },
    stdio: "ignore",
  });
  const base = `http://127.0.0.1:${port}`;
  const controllers = [];
  const readers = [];
  async function post(path, body) {
    const response = await fetch(`${base}${path}`, {
      method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body),
    });
    return { status: response.status, body: await response.json() };
  }
  async function connect(id) {
    const controller = new AbortController();
    controllers.push(controller);
    const response = await fetch(`${base}/api/collab/stream?clientId=${id}&name=${id}`, { signal: controller.signal });
    const reader = response.body.getReader();
    readers.push(reader);
    let data = "";
    while (!data.includes("event: hello")) data += new TextDecoder().decode((await reader.read()).value);
    return controller;
  }
  try {
    let ready = false;
    for (let i = 0; i < 100; i++) {
      try { ready = (await fetch(`${base}/api/health`)).ok; } catch { /* startup */ }
      if (ready) break;
      await delay(50);
    }
    assert.equal(ready, true);
    const map = await (await fetch(`${base}/api/map`)).json();
    const a = await connect("test-A");
    await connect("test-B");
    const cells = [{ x: 1, z: 1 }];
    assert.equal((await post("/api/collab/lock", { action: "take", clientId: "unknown", cells })).status, 403);
    assert.equal((await post("/api/collab/lock", { action: "take", clientId: "test-A", cells })).status, 200);
    const denied = await post("/api/collab/lock", { action: "take", clientId: "test-B", cells: [...cells, { x: 2, z: 2 }] });
    assert.equal(denied.status, 409);
    assert.deepEqual(denied.body.granted, []);
    const payload = { changes: [{ index: map.width + 1, h: 40, b: 0, s: 1 }] };
    assert.equal((await post("/api/collab/op", { clientId: "test-B", type: "terrain", payload })).status, 409);
    assert.equal((await post("/api/collab/op", { clientId: "test-A", type: "terrain", payload })).status, 200);
    assert.equal((await post("/api/collab/op", { clientId: "test-A", type: "terrain", payload: { changes: [{ index: -1, h: 40, b: 0, s: 1 }] } })).status, 400);
    a.abort();
    let remaining = [];
    for (let i = 0; i < 50; i++) {
      remaining = (await (await fetch(`${base}/api/collab/locks`)).json()).locks;
      if (!remaining.some((lock) => lock.owner === "test-A")) break;
      await delay(50);
    }
    assert.equal(remaining.some((lock) => lock.owner === "test-A"), false);
    assert.equal((await post("/api/collab/lock", { action: "take", clientId: "test-B", cells })).status, 200);
  } finally {
    for (const controller of controllers) controller.abort();
    for (const reader of readers) await reader.cancel().catch(() => {});
    child.kill();
  }
});
