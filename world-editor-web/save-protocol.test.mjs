import test from "node:test";
import assert from "node:assert/strict";
import { spawn } from "node:child_process";
import { mkdtemp, mkdir, readFile, readdir, rm, writeFile } from "node:fs/promises";
import { createServer } from "node:net";
import { tmpdir } from "node:os";
import path from "node:path";
import http from "node:http";
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
    env: {
      ...process.env,
      HOST: "127.0.0.1",
      PORT: String(port),
      GENSOYO_EDITOR_SOURCE: sourceDir,
    },
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

async function holdWindowsRawLayers(files) {
  if (process.platform !== "win32") return null;
  const opens = files.map((file) => `[IO.File]::Open('${file.replaceAll("'", "''")}', 'Open', 'Read', 'ReadWrite')`).join(", ");
  const script = `$handles = @(${opens}); try { [Console]::WriteLine('RAW_LAYERS_OPEN'); [Console]::ReadLine() | Out-Null } finally { foreach ($handle in $handles) { $handle.Dispose() } }`;
  const child = spawn("powershell.exe", ["-NoProfile", "-NonInteractive", "-Command", script], {
    stdio: ["pipe", "pipe", "pipe"], windowsHide: true,
  });
  await new Promise((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error("raw layer lock helper did not become ready")), 10000);
    let text = "";
    child.stdout.on("data", (chunk) => {
      text += chunk;
      if (text.includes("RAW_LAYERS_OPEN")) { clearTimeout(timer); resolve(); }
    });
    child.once("error", (error) => { clearTimeout(timer); reject(error); });
    child.once("exit", (code) => { clearTimeout(timer); reject(new Error(`raw layer helper exited: ${code}`)); });
  }).catch(async (error) => { await stopServer(child); throw error; });
  return child;
}

async function waitForHealth(base, child) {
  const deadline = Date.now() + 15000;
  while (Date.now() < deadline) {
    if (child.exitCode !== null) {
      throw new Error(`editor server exited with code ${child.exitCode}`);
    }
    try {
      const response = await fetch(`${base}/api/health`);
      if (response.ok) return;
    } catch {
      // Server not listening yet.
    }
    await new Promise((resolve) => setTimeout(resolve, 100));
  }
  throw new Error("editor server did not become healthy");
}

async function requestJson(url, options = {}) {
  const response = await fetch(url, options);
  const text = await response.text();
  let json = null;
  try {
    json = text ? JSON.parse(text) : null;
  } catch {
    json = { raw: text };
  }
  return { response, json };
}

function postJson(url, body, headers = {}) {
  return requestJson(url, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...headers },
    body: JSON.stringify(body),
  });
}

function putRaw(url, body, headers = {}) {
  return requestJson(url, {
    method: "PUT",
    headers,
    body,
  });
}

function openSse(base, clientId) {
  return new Promise((resolve, reject) => {
    const request = http.get(
      `${base}/api/collab/stream?clientId=${encodeURIComponent(clientId)}&name=SaveTest`,
      (response) => {
        if (response.statusCode !== 200) {
          reject(new Error(`SSE handshake failed: ${response.statusCode}`));
          return;
        }
        response.setEncoding("utf8");
        let buffer = "";
        const timer = setTimeout(() => {
          reject(new Error("SSE hello event timed out"));
        }, 5000);
        response.on("data", (chunk) => {
          buffer += chunk;
          if (buffer.includes("event: hello")) {
            clearTimeout(timer);
            resolve({
              close() {
                response.destroy();
                request.destroy();
              },
            });
          }
        });
      },
    );
    request.once("error", reject);
  });
}

function layerBuffers(height) {
  const heights = Buffer.alloc(WIDTH * DEPTH * 2);
  for (let i = 0; i < WIDTH * DEPTH; i++) {
    heights.writeInt16LE(i === 0 ? height : 64, i * 2);
  }
  const biomes = Buffer.alloc(WIDTH * DEPTH, 0);
  const surfaces = Buffer.alloc(WIDTH * DEPTH * 2);
  return { heights, biomes, surfaces };
}

function testMap(spawn) {
  return {
    format_version: 1,
    map_version: 2,
    id: "save-protocol-test",
    width: WIDTH,
    depth: DEPTH,
    origin: [0, 0],
    min_y: 0,
    world_height: 1024,
    sea_level: 32,
    dirt_depth: 3,
    surface_palette: ["minecraft:stone"],
    biome_palette: ["minecraft:plains"],
    layers: {
      heightmap: "heightmap.bin",
      biomes: "biomes.bin",
      surfaces: "surfaces.bin",
    },
    anchors: [],
    spawn: { pos: spawn, yaw: 0, pitch: 0 },
    revision: 12345,
    liveRevision: 12345,
  };
}

function metaPayload(spawn) {
  return {
    anchors: [],
    spawn: { pos: spawn, yaw: 0, pitch: 0 },
    surface_palette: ["minecraft:stone"],
    biome_palette: ["minecraft:plains"],
    sea_level: 32,
    dirt_depth: 3,
  };
}

async function writeSource(dir, map, layers) {
  await mkdir(dir, { recursive: true });
  await writeFile(path.join(dir, "map.json"), `${JSON.stringify(map, null, 2)}\n`);
  await writeFile(path.join(dir, "heightmap.bin"), layers.heights);
  await writeFile(path.join(dir, "biomes.bin"), layers.biomes);
  await writeFile(path.join(dir, "surfaces.bin"), layers.surfaces);
  await writeFile(
    path.join(dir, "underground.json"),
    `${JSON.stringify({ format_version: 1, blocks: [] }, null, 2)}\n`,
  );
}

async function stageSave(base, token, map, layers) {
  let result = await requestJson(`${base}/api/map`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", "X-Save-Token": token },
    body: JSON.stringify(map),
  });
  assert.equal(result.response.status, 200, JSON.stringify(result.json));
  assert.equal(result.json.staged, true);

  result = await putRaw(`${base}/api/layers/heightmap`, layers.heights, {
    "Content-Type": "application/octet-stream",
    "X-Save-Token": token,
  });
  assert.equal(result.response.status, 200, JSON.stringify(result.json));

  result = await putRaw(`${base}/api/layers/biomes`, layers.biomes, {
    "Content-Type": "application/octet-stream",
    "X-Save-Token": token,
  });
  assert.equal(result.response.status, 200, JSON.stringify(result.json));

  result = await putRaw(`${base}/api/layers/surfaces`, layers.surfaces, {
    "Content-Type": "application/octet-stream",
    "X-Save-Token": token,
  });
  assert.equal(result.response.status, 200, JSON.stringify(result.json));

  result = await requestJson(`${base}/api/underground`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", "X-Save-Token": token },
    body: JSON.stringify({ format_version: 1, blocks: [] }),
  });
  assert.equal(result.response.status, 200, JSON.stringify(result.json));
}

test("save protocol rejects stale and concurrent saves, commits atomically, persists revision", async (t) => {
  const dir = await mkdtemp(path.join(tmpdir(), "gensokyo-save-protocol-"));
  t.after(async () => {
    await rm(dir, { recursive: true, force: true });
  });

  const baseMap = testMap([0, 64, 0]);
  const baseLayers = layerBuffers(64);
  await writeSource(dir, baseMap, baseLayers);

  let port = await freePort();
  let base = `http://127.0.0.1:${port}`;
  let { child } = startServer(dir, port);
  let sse = null;
  t.after(async () => {
    sse?.close();
    await stopServer(child);
  });

  try {
    await waitForHealth(base, child);

    let result = await requestJson(`${base}/api/map`);
    assert.equal(result.response.status, 200);
    assert.equal(result.json.revision, 0);
    assert.equal(result.json.liveRevision, 0);
    assert.deepEqual(result.json.spawn.pos, [0, 64, 0]);

    // A session reserves the revision while it uploads.
    result = await postJson(`${base}/api/save/begin`, { baseRevision: 0 });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    const tokenA = result.json.token;
    assert.ok(tokenA);

    result = await postJson(`${base}/api/save/begin`, { baseRevision: 0 });
    assert.equal(result.response.status, 409, JSON.stringify(result.json));
    assert.equal(result.json.currentRevision, 0);

    const mapA = testMap([1, 65, 1]);
    const layersA = layerBuffers(65);
    await stageSave(base, tokenA, mapA, layersA);

    // Reproduce a Windows reader that permits writes but refuses replacement.
    // The changed height layer must publish and the identical biome layer must
    // remain untouched while both reader handles are still open.
    const readers = await holdWindowsRawLayers([path.join(dir, "heightmap.bin"), path.join(dir, "biomes.bin")]);
    try {
      result = await postJson(`${base}/api/save/commit`, { token: tokenA });
      assert.equal(result.response.status, 200, JSON.stringify(result.json));
      assert.equal(result.json.revision, 1);
      assert.equal((await readFile(path.join(dir, "heightmap.bin"))).readInt16LE(0), 65);
      assert.deepEqual(await readFile(path.join(dir, "biomes.bin")), layersA.biomes);
    } finally {
      readers?.stdin.end("\n");
      await stopServer(readers);
    }

    result = await requestJson(`${base}/api/map`);
    assert.equal(result.json.revision, 1);
    assert.equal(result.json.liveRevision, 1);
    assert.deepEqual(result.json.spawn.pos, [1, 65, 1]);
    assert.equal((await readFile(path.join(dir, "heightmap.bin"))).readInt16LE(0), 65);

    // A client that loaded before the checkpoint is rejected.
    result = await postJson(`${base}/api/save/begin`, { baseRevision: 0 });
    assert.equal(result.response.status, 409, JSON.stringify(result.json));
    assert.equal(result.json.stale, true);
    assert.equal(result.json.currentRevision, 1);

    // Connect a collaborator and advance the live revision.
    sse = await openSse(base, "save-test-client");

    result = await postJson(`${base}/api/collab/op`, {
      clientId: "save-test-client",
      type: "meta",
      baseRevision: 1,
      payload: metaPayload([1, 65, 1]),
    });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    assert.equal(result.json.revision, 2);

    // A save session can start at the live revision, but a later edit invalidates it.
    result = await postJson(`${base}/api/save/begin`, {
      clientId: "save-test-client",
      baseRevision: 2,
    });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    const tokenB = result.json.token;

    const mapB = testMap([2, 66, 2]);
    const layersB = layerBuffers(66);
    await stageSave(base, tokenB, mapB, layersB);

    result = await postJson(`${base}/api/collab/op`, {
      clientId: "save-test-client",
      type: "meta",
      baseRevision: 2,
      payload: metaPayload([1, 65, 1]),
    });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    assert.equal(result.json.revision, 3);

    result = await postJson(`${base}/api/save/commit`, { token: tokenB });
    assert.equal(result.response.status, 409, JSON.stringify(result.json));
    assert.equal(result.json.stale, true);
    assert.equal(result.json.currentRevision, 3);

    // The rejected commit must not have touched the source.
    result = await requestJson(`${base}/api/map`);
    assert.deepEqual(result.json.spawn.pos, [1, 65, 1]);
    assert.equal((await readFile(path.join(dir, "heightmap.bin"))).readInt16LE(0), 65);
    assert.deepEqual(await readdir(path.join(dir, ".save-staging")), []);

    // Move to revision 4 and save the current logical state successfully.
    result = await postJson(`${base}/api/collab/op`, {
      clientId: "save-test-client",
      type: "meta",
      baseRevision: 3,
      payload: metaPayload([3, 67, 3]),
    });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    assert.equal(result.json.revision, 4);

    result = await postJson(`${base}/api/save/begin`, {
      clientId: "save-test-client",
      baseRevision: 4,
    });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    const tokenC = result.json.token;

    const mapC = testMap([3, 67, 3]);
    const layersC = layerBuffers(67);
    await stageSave(base, tokenC, mapC, layersC);

    result = await postJson(`${base}/api/save/commit`, { token: tokenC });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    assert.equal(result.json.revision, 5);

    result = await requestJson(`${base}/api/map`);
    assert.equal(result.json.revision, 5);
    assert.equal(result.json.liveRevision, 5);
    assert.deepEqual(result.json.spawn.pos, [3, 67, 3]);
    assert.equal((await readFile(path.join(dir, "heightmap.bin"))).readInt16LE(0), 67);

    const writtenMap = JSON.parse(await readFile(path.join(dir, "map.json"), "utf8"));
    assert.equal("revision" in writtenMap, false);
    assert.equal("liveRevision" in writtenMap, false);
    assert.deepEqual(
      JSON.parse(await readFile(path.join(dir, ".editor-revision.json"), "utf8")),
      { revision: 5 },
    );

    // Abort releases a session without writing.
    result = await postJson(`${base}/api/save/begin`, {
      clientId: "save-test-client",
      baseRevision: 5,
    });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    const tokenD = result.json.token;
    result = await postJson(`${base}/api/save/abort`, { token: tokenD });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    assert.deepEqual(await readdir(path.join(dir, ".save-staging")), []);

    // The same connected client can supersede its own abandoned session on retry.
    result = await postJson(`${base}/api/save/begin`, {
      clientId: "save-test-client",
      baseRevision: 5,
    });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    const tokenG = result.json.token;
    result = await postJson(`${base}/api/save/begin`, {
      clientId: "save-test-client",
      baseRevision: 5,
    });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    const tokenH = result.json.token;
    result = await postJson(`${base}/api/save/commit`, { token: tokenG });
    assert.equal(result.response.status, 409, JSON.stringify(result.json));
    result = await postJson(`${base}/api/save/abort`, { token: tokenH });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    assert.deepEqual(await readdir(path.join(dir, ".save-staging")), []);

    // Disconnecting aborts an active save session and cleans staging.
    result = await postJson(`${base}/api/save/begin`, {
      clientId: "save-test-client",
      baseRevision: 5,
    });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    assert.ok(result.json.token);
    sse.close();
    sse = null;
    await new Promise((resolve) => setTimeout(resolve, 500));
    assert.deepEqual(await readdir(path.join(dir, ".save-staging")), []);

    // Reconnecting can start a new session at the same revision.
    sse = await openSse(base, "save-test-client");
    result = await postJson(`${base}/api/save/begin`, {
      clientId: "save-test-client",
      baseRevision: 5,
    });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    const tokenF = result.json.token;
    result = await postJson(`${base}/api/save/abort`, { token: tokenF });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));

    // Revision survives a restart.
    sse.close();
    sse = null;
    await stopServer(child);

    port = await freePort();
    base = `http://127.0.0.1:${port}`;
    ({ child } = startServer(dir, port));
    await waitForHealth(base, child);

    result = await requestJson(`${base}/api/map`);
    assert.equal(result.response.status, 200);
    assert.equal(result.json.revision, 5);
    assert.equal(result.json.liveRevision, 5);
    assert.deepEqual(result.json.spawn.pos, [3, 67, 3]);
  } finally {
    sse?.close();
    await stopServer(child);
  }
});
