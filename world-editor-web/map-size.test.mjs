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

async function removeTemp(dir) {
  for (let attempt = 0; attempt < 12; attempt++) {
    try {
      await rm(dir, { recursive: true, force: true });
      return;
    } catch (error) {
      if (attempt === 11) {
        throw error;
      }
      await new Promise((resolve) => setTimeout(resolve, 200));
    }
  }
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
    id: "map-size-test",
    width: 2,
    depth: 2,
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
  const heights = Buffer.alloc(2 * 2 * 2);
  for (let i = 0; i < 4; i++) heights.writeInt16LE(64, i * 2);
  await writeFile(path.join(dir, "heightmap.bin"), heights);
  await writeFile(path.join(dir, "biomes.bin"), Buffer.alloc(4));
  await writeFile(path.join(dir, "surfaces.bin"), Buffer.alloc(8));
  await writeFile(path.join(dir, "underground.json"), `${JSON.stringify({ format_version: 1, blocks: [] }, null, 2)}\n`);
}

async function postJson(url, body) {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  return { status: response.status, body: await response.json() };
}

test("editor maps can exceed 4096 per axis within the total-cell guard", { timeout: 30000 }, async () => {
  const tempRoot = await mkdtemp(path.join(tmpdir(), "gensokyo-map-size-"));
  const sourceDir = path.join(tempRoot, "map");
  await writeSource(sourceDir);
  const port = await freePort();
  const base = `http://127.0.0.1:${port}`;
  const server = startServer(sourceDir, port);
  try {
    await waitForHealth(base, server);
    const created = await postJson(`${base}/api/map/new`, { width: 5000, depth: 2 });
    assert.equal(created.status, 200, JSON.stringify(created.body));
    assert.equal(created.body.map.width, 5000);
    assert.equal(created.body.map.depth, 2);

    const map = await (await fetch(`${base}/api/map`)).json();
    assert.equal(map.width, 5000);
    assert.equal(map.depth, 2);

    const tooWide = await postJson(`${base}/api/map/new`, { width: 32769, depth: 1 });
    assert.equal(tooWide.status, 400);

    const tooManyCells = await postJson(`${base}/api/map/new`, { width: 6000, depth: 30000 });
    assert.equal(tooManyCells.status, 400);
  } finally {
    await stopServer(server.child);
    await removeTemp(tempRoot);
  }
});