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

async function writeSource(dir) {
  await mkdir(dir, { recursive: true });
  const map = {
    format_version: 1,
    map_version: 2,
    id: "transfer-test",
    width: WIDTH,
    depth: DEPTH,
    origin: [0, 0],
    min_y: 0,
    world_height: 1024,
    sea_level: 32,
    dirt_depth: 3,
    surface_palette: ["minecraft:stone"],
    biome_palette: ["minecraft:plains"],
    layers: { heightmap: "heightmap.bin", biomes: "biomes.bin", surfaces: "surfaces.bin" },
    anchors: [],
    spawn: { pos: [0, 65, 0], yaw: 0, pitch: 0 },
  };
  await writeFile(path.join(dir, "map.json"), `${JSON.stringify(map, null, 2)}\n`);
  const heights = Buffer.alloc(WIDTH * DEPTH * 2);
  for (let i = 0; i < WIDTH * DEPTH; i++) heights.writeInt16LE(64, i * 2);
  await writeFile(path.join(dir, "heightmap.bin"), heights);
  await writeFile(path.join(dir, "biomes.bin"), Buffer.alloc(WIDTH * DEPTH, 0));
  await writeFile(path.join(dir, "surfaces.bin"), Buffer.alloc(WIDTH * DEPTH * 2));
  await writeFile(path.join(dir, "underground.json"), `${JSON.stringify({ format_version: 1, blocks: [] }, null, 2)}\n`);
}

test("large transfer paths use gzip and ETag revalidation", { timeout: 30000 }, async () => {
  const tempRoot = await mkdtemp(path.join(tmpdir(), "gensokyo-transfer-"));
  const sourceDir = path.join(tempRoot, "map");
  await writeSource(sourceDir);
  const port = await freePort();
  const base = `http://127.0.0.1:${port}`;
  const server = startServer(sourceDir, port);
  try {
    await waitForHealth(base, server);
    const gzipHeaders = { "Accept-Encoding": "gzip" };

    const blocks = await fetch(`${base}/api/blocks`, { headers: gzipHeaders });
    assert.equal(blocks.status, 200);
    assert.equal(blocks.headers.get("content-encoding"), "gzip");
    assert.ok(blocks.headers.get("etag"));
    const blockPalette = await blocks.json();
    assert.ok(Array.isArray(blockPalette.blocks));
    const blocksCached = await fetch(`${base}/api/blocks`, {
      headers: { ...gzipHeaders, "If-None-Match": blocks.headers.get("etag") },
    });
    assert.equal(blocksCached.status, 304);

    const biomes = await fetch(`${base}/api/biomes`, { headers: gzipHeaders });
    assert.equal(biomes.status, 200);
    assert.ok(biomes.headers.get("etag"));
    const biomesCached = await fetch(`${base}/api/biomes`, {
      headers: { ...gzipHeaders, "If-None-Match": biomes.headers.get("etag") },
    });
    assert.equal(biomesCached.status, 304);

    const map = await fetch(`${base}/api/map`, { headers: gzipHeaders });
    assert.equal(map.status, 200);
    assert.ok(map.headers.get("etag"));
    const mapCached = await fetch(`${base}/api/map`, {
      headers: { ...gzipHeaders, "If-None-Match": map.headers.get("etag") },
    });
    assert.equal(mapCached.status, 304);

    const layer = await fetch(`${base}/api/layers/heightmap`, { headers: gzipHeaders });
    assert.equal(layer.status, 200);
    assert.equal(layer.headers.get("content-encoding"), "gzip");
    assert.ok(layer.headers.get("etag"));
    const layerBytes = await layer.arrayBuffer();
    assert.equal(layerBytes.byteLength, WIDTH * DEPTH * 2);
    const layerCached = await fetch(`${base}/api/layers/heightmap`, {
      headers: { ...gzipHeaders, "If-None-Match": layer.headers.get("etag") },
    });
    assert.equal(layerCached.status, 304);

    const app = await fetch(`${base}/app.js`, { headers: gzipHeaders });
    assert.equal(app.status, 200);
    assert.equal(app.headers.get("content-encoding"), "gzip");
    assert.ok(app.headers.get("etag"));
    const appText = await app.text();
    assert.ok(appText.includes("Gensokyo") || appText.length > 1000);
    const appCached = await fetch(`${base}/app.js`, {
      headers: { ...gzipHeaders, "If-None-Match": app.headers.get("etag") },
    });
    assert.equal(appCached.status, 304);
  } finally {
    await stopServer(server.child);
    await rm(tempRoot, { recursive: true, force: true });
  }
});