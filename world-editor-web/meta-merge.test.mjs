import test from "node:test";
import assert from "node:assert/strict";
import { spawn } from "node:child_process";
import { mkdtemp, mkdir, rm, writeFile } from "node:fs/promises";
import { createServer } from "node:net";
import { tmpdir } from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";

const here = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(here, "..");
const WIDTH = 4;
const DEPTH = 4;

async function freePort() {
  return await new Promise((resolve, reject) => {
    const server = createServer();
    server.once("error", reject);
    server.listen(0, "127.0.0.1", () => {
      const { port } = server.address();
      server.close(() => resolve(port));
    });
  });
}

function startServer(sourceDir, port) {
  const child = spawn(process.execPath, ["world-editor-web/server.mjs"], {
    cwd: repoRoot,
    env: { ...process.env, HOST: "127.0.0.1", PORT: String(port), GENSOYO_EDITOR_SOURCE: sourceDir },
    stdio: ["ignore", "pipe", "pipe"],
  });
  let output = "";
  child.stdout.setEncoding("utf8");
  child.stderr.setEncoding("utf8");
  child.stdout.on("data", (chunk) => { output += chunk; });
  child.stderr.on("data", (chunk) => { output += chunk; });
  return { child, output: () => output };
}

async function stopServer(child) {
  if (!child || child.exitCode !== null) return;
  child.kill();
  await new Promise((resolve) => {
    const timer = setTimeout(() => {
      child.kill("SIGKILL");
      resolve();
    }, 3000);
    child.once("exit", () => {
      clearTimeout(timer);
      resolve();
    });
  });
}

async function waitForHealth(base, server) {
  const deadline = Date.now() + 15000;
  while (Date.now() < deadline) {
    if (server.child.exitCode !== null) {
      throw new Error(`server exited with ${server.child.exitCode}\n${server.output()}`);
    }
    try {
      const response = await fetch(`${base}/api/health`);
      if (response.ok) return;
    } catch {
      // Not listening yet.
    }
    await new Promise((resolve) => setTimeout(resolve, 100));
  }
  throw new Error(`server did not become healthy\n${server.output()}`);
}

function anchor(id, x, z) {
  return {
    id,
    type: "template",
    template: "minecraft:oak",
    pos: [x, 64, z],
    rotation: "none",
    mirror: "none",
    integrity: 1,
  };
}

async function writeSource(dir) {
  await mkdir(dir, { recursive: true });
  const map = {
    format_version: 1,
    map_version: 2,
    id: "meta-merge-test",
    width: WIDTH,
    depth: DEPTH,
    origin: [0, 0],
    min_y: 0,
    world_height: 1024,
    sea_level: 32,
    dirt_depth: 3,
    biome_transition: 0,
    surface_palette: ["minecraft:stone"],
    biome_palette: ["minecraft:plains"],
    layers: { heightmap: "heightmap.bin", biomes: "biomes.bin", surfaces: "surfaces.bin" },
    anchors: [anchor("a", 0, 0), anchor("b", 2, 2)],
    spawn: { pos: [1, 65, 1], yaw: 0, pitch: 0 },
  };
  await writeFile(path.join(dir, "map.json"), `${JSON.stringify(map, null, 2)}\n`);
  const heights = Buffer.alloc(WIDTH * DEPTH * 2);
  for (let i = 0; i < WIDTH * DEPTH; i++) heights.writeInt16LE(64, i * 2);
  await writeFile(path.join(dir, "heightmap.bin"), heights);
  await writeFile(path.join(dir, "biomes.bin"), Buffer.alloc(WIDTH * DEPTH, 0));
  await writeFile(path.join(dir, "surfaces.bin"), Buffer.alloc(WIDTH * DEPTH * 2));
  await writeFile(path.join(dir, "underground.json"), `${JSON.stringify({ format_version: 1, blocks: [] }, null, 2)}\n`);
  return { anchors: map.anchors, map };
}

function parseSseFrame(frame) {
  let event = "message";
  let data = "";
  for (const line of frame.split("\n")) {
    if (line.startsWith("event:")) {
      event = line.slice(6).trim();
    } else if (line.startsWith("data:")) {
      data += line.slice(5).trim();
    }
  }
  if (!data) {
    return { event, data: null };
  }
  try {
    return { event, data: JSON.parse(data) };
  } catch {
    return { event, data };
  }
}

async function openSse(base, clientId, metaDelta = true) {
  const controller = new AbortController();
  const suffix = metaDelta ? "&metaDelta=1" : "";
  const response = await fetch(`${base}/api/collab/stream?clientId=${encodeURIComponent(clientId)}&name=${encodeURIComponent(clientId)}${suffix}`, {
    signal: controller.signal,
  });
  assert.equal(response.status, 200);
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";
  async function nextEvent(timeoutMs = 6000) {
    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
      const index = buffer.indexOf("\n\n");
      if (index >= 0) {
        const frame = buffer.slice(0, index);
        buffer = buffer.slice(index + 2);
        const parsed = parseSseFrame(frame);
        if (parsed.event === "message" && parsed.data === null) {
          continue;
        }
        return parsed;
      }
      const remaining = Math.max(50, deadline - Date.now());
      const read = reader.read();
      const timeout = new Promise((_, reject) => {
        setTimeout(() => reject(new Error("SSE read timeout")), remaining);
      });
      const { value, done } = await Promise.race([read, timeout]);
      if (value) buffer += decoder.decode(value, { stream: true });
      if (done) throw new Error("SSE stream ended");
    }
    throw new Error("SSE event timeout");
  }
  async function hello() {
    while (true) {
      const event = await nextEvent();
      if (event.event === "hello") return event.data;
    }
  }
  async function nextMeta() {
    while (true) {
      const event = await nextEvent();
      if (event.event === "collab" && event.data?.type === "meta") return event.data;
    }
  }
  return {
    hello,
    nextMeta,
    close() {
      controller.abort();
    },
  };
}

async function postMeta(base, clientId, payload) {
  const response = await fetch(`${base}/api/collab/op`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ clientId, type: "meta", baseRevision: 0, payload }),
  });
  return { status: response.status, body: await response.json() };
}

test("meta deltas merge disjoint anchors and report same-item conflicts", { timeout: 40000 }, async () => {
  const tempRoot = await mkdtemp(path.join(tmpdir(), "gensokyo-meta-merge-"));
  const sourceDir = path.join(tempRoot, "map");
  const { anchors } = await writeSource(sourceDir);
  const port = await freePort();
  const base = `http://127.0.0.1:${port}`;
  const server = startServer(sourceDir, port);
  let clientA;
  let clientB;
  let observer;
  try {
    await waitForHealth(base, server);
    clientA = await openSse(base, "meta-a");
    clientB = await openSse(base, "meta-b");
    const helloA = await clientA.hello();
    const helloB = await clientB.hello();
    assert.equal(helloA.meta.anchors.length, 2);
    assert.equal(helloB.meta.anchors.length, 2);
    assert.equal(helloA.meta.dirt_depth, 3);

    const aMoved = { ...anchors[0], rotation: "clockwise_90" };
    const bMoved = { ...anchors[1], rotation: "clockwise_180" };

    let result = await postMeta(base, "meta-a", {
      delta: true,
      anchors: [aMoved],
      baseAnchors: { [anchors[0].id]: anchors[0] },
      removedAnchors: [],
      baseFields: {},
    });
    assert.equal(result.status, 200, JSON.stringify(result.body));
    assert.equal(result.body.conflicts.length, 0);
    assert.equal(result.body.meta.anchors.length, 1);
    const eventB = await clientB.nextMeta();
    assert.equal(eventB.payload.anchors[0].id, "a");
    assert.equal(eventB.payload.anchors[0].rotation, "clockwise_90");

    result = await postMeta(base, "meta-b", {
      delta: true,
      anchors: [bMoved],
      baseAnchors: { [anchors[1].id]: anchors[1] },
      removedAnchors: [],
      baseFields: {},
    });
    assert.equal(result.status, 200, JSON.stringify(result.body));
    assert.equal(result.body.conflicts.length, 0);
    const eventA = await clientA.nextMeta();
    assert.equal(eventA.payload.anchors[0].id, "b");
    assert.equal(eventA.payload.anchors[0].rotation, "clockwise_180");

    result = await postMeta(base, "meta-b", {
      delta: true,
      anchors: [{ ...anchors[0], rotation: "counterclockwise_90" }],
      baseAnchors: { [anchors[0].id]: anchors[0] },
      removedAnchors: [],
      baseFields: {},
    });
    assert.equal(result.status, 200, JSON.stringify(result.body));
    assert.equal(result.body.conflicts.length, 1);
    assert.equal(result.body.conflicts[0].kind, "anchor");
    assert.equal(result.body.conflicts[0].id, "a");
    assert.equal(result.body.conflicts[0].server.rotation, "clockwise_90");
    assert.equal(result.body.meta.anchors.length, 0);

    result = await postMeta(base, "meta-a", {
      delta: true,
      anchors: [],
      removedAnchors: [],
      baseFields: { dirt_depth: 3 },
      dirt_depth: 4,
    });
    assert.equal(result.status, 200, JSON.stringify(result.body));
    assert.equal(result.body.conflicts.length, 0);

    result = await postMeta(base, "meta-b", {
      delta: true,
      anchors: [],
      removedAnchors: [],
      baseFields: { sea_level: 32 },
      sea_level: 40,
    });
    assert.equal(result.status, 200, JSON.stringify(result.body));
    assert.equal(result.body.conflicts.length, 0);

    result = await postMeta(base, "meta-b", {
      delta: true,
      anchors: [],
      removedAnchors: [],
      baseFields: { dirt_depth: 3 },
      dirt_depth: 5,
    });
    assert.equal(result.status, 200, JSON.stringify(result.body));
    assert.equal(result.body.conflicts.length, 1);
    assert.equal(result.body.conflicts[0].kind, "field");
    assert.equal(result.body.conflicts[0].field, "dirt_depth");
    assert.equal(result.body.conflicts[0].server, 4);

    observer = await openSse(base, "meta-observer");
    const snapshot = await observer.hello();
    const byId = new Map(snapshot.meta.anchors.map((entry) => [entry.id, entry]));
    assert.equal(byId.get("a").rotation, "clockwise_90");
    assert.equal(byId.get("b").rotation, "clockwise_180");
    assert.equal(snapshot.meta.dirt_depth, 4);
    assert.equal(snapshot.meta.sea_level, 40);
    assert.equal(snapshot.meta.biome_transition, 0);

    const legacy = await openSse(base, "meta-legacy", false);
    const legacyHello = await legacy.hello();
    const legacyFullMeta = [...(legacyHello.events ?? [])]
      .reverse()
      .find((event) => event.type === "meta" && event.payload?.delta === false);
    assert.ok(legacyFullMeta, "legacy clients should receive a full meta snapshot event");
    assert.equal(legacyFullMeta.payload.anchors.length, 2);
    assert.equal(legacyFullMeta.payload.dirt_depth, 4);
    legacy.close();

    result = await postMeta(base, "meta-a", {
      delta: false,
      anchors: [anchors[0]],
      spawn: { pos: [0, 66, 0], yaw: 90, pitch: 0 },
      surface_palette: ["minecraft:stone", "minecraft:dirt"],
      biome_palette: ["minecraft:plains"],
      sea_level: 41,
      dirt_depth: 2,
      biome_transition: 8,
    });
    assert.equal(result.status, 200, JSON.stringify(result.body));
    assert.equal(result.body.conflicts.length, 0);
    observer.close();
    observer = await openSse(base, "meta-observer-2");
    const fullSnapshot = await observer.hello();
    assert.equal(fullSnapshot.meta.anchors.length, 1);
    assert.equal(fullSnapshot.meta.anchors[0].rotation, "none");
    assert.deepEqual(fullSnapshot.meta.spawn.pos, [0, 66, 0]);
    assert.equal(fullSnapshot.meta.surface_palette.length, 2);
    assert.equal(fullSnapshot.meta.sea_level, 41);
    assert.equal(fullSnapshot.meta.dirt_depth, 2);
    assert.equal(fullSnapshot.meta.biome_transition, 8);
  } finally {
    clientA?.close();
    clientB?.close();
    observer?.close();
    await stopServer(server.child);
    await rm(tempRoot, { recursive: true, force: true });
  }
});