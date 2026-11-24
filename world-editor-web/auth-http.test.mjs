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

function startServer(sourceDir, port, extraEnv = {}) {
  const child = spawn(process.execPath, ["world-editor-web/server.mjs"], {
    cwd: repoRoot,
    env: {
      ...process.env,
      HOST: "127.0.0.1",
      PORT: String(port),
      GENSOYO_EDITOR_SOURCE: sourceDir,
      ...extraEnv,
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
      throw new Error(`editor server exited with code ${server.child.exitCode}\n${server.output()}`);
    }
    try {
      const response = await fetch(`${base}/api/health`);
      if (response.ok) return;
    } catch {
      // Server is not listening yet.
    }
    await new Promise((resolve) => setTimeout(resolve, 100));
  }
  throw new Error(`editor server did not become healthy\n${server.output()}`);
}

function cookieFrom(response) {
  const value = response.headers.get("set-cookie");
  return value ? value.split(";")[0] : "";
}

async function writeSource(dir) {
  await mkdir(dir, { recursive: true });
  const map = {
    format_version: 1,
    map_version: 2,
    id: "auth-test",
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
    spawn: { pos: [0, 0, 0], yaw: 0, pitch: 0 },
  };
  await writeFile(path.join(dir, "map.json"), `${JSON.stringify(map, null, 2)}\n`);
  const heights = Buffer.alloc(WIDTH * DEPTH * 2);
  for (let i = 0; i < WIDTH * DEPTH; i++) {
    heights.writeInt16LE(64, i * 2);
  }
  await writeFile(path.join(dir, "heightmap.bin"), heights);
  await writeFile(path.join(dir, "biomes.bin"), Buffer.alloc(WIDTH * DEPTH, 0));
  await writeFile(path.join(dir, "surfaces.bin"), Buffer.alloc(WIDTH * DEPTH * 2));
  await writeFile(
    path.join(dir, "underground.json"),
    `${JSON.stringify({ format_version: 1, blocks: [] }, null, 2)}\n`,
  );
}

async function login(base, password, role) {
  const response = await fetch(`${base}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ password, role }),
  });
  const json = await response.json();
  return { response, json, cookie: cookieFrom(response) };
}

async function readSseUntil(reader, marker, initial = "") {
  const decoder = new TextDecoder();
  let buffer = initial;
  if (!buffer.includes(marker)) {
    const deadline = Date.now() + 6000;
    while (Date.now() < deadline) {
      const { value, done } = await reader.read();
      if (value) {
        buffer += decoder.decode(value, { stream: true });
        if (buffer.includes(marker)) {
          return { buffer, reader };
        }
      }
      if (done) {
        return { buffer, reader, done: true };
      }
    }
  }
  return { buffer, reader, timedOut: !buffer.includes(marker) };
}

test("optional room auth protects the API and supports viewer/admin roles", { timeout: 40000 }, async () => {
  const tempRoot = await mkdtemp(path.join(tmpdir(), "gensokyo-auth-test-"));
  const sourceDir = path.join(tempRoot, "map");
  await writeSource(sourceDir);
  const port = await freePort();
  const base = `http://127.0.0.1:${port}`;
  const server = startServer(sourceDir, port, {
    GENSOYO_EDITOR_PASSWORD: "editor-pass",
    GENSOYO_EDITOR_VIEWER_PASSWORD: "viewer-pass",
    GENSOYO_EDITOR_ADMIN_PASSWORD: "admin-pass",
    GENSOYO_EDITOR_ROOM: "auth-test-room",
  });
  try {
    await waitForHealth(base, server);

    const status = await (await fetch(`${base}/api/auth/status`)).json();
    assert.equal(status.required, true);
    assert.equal(status.authenticated, false);
    assert.deepEqual(status.roles, ["editor", "viewer", "admin"]);
    assert.equal(status.room, "auth-test-room");

    const unauthMap = await fetch(`${base}/api/map`);
    assert.equal(unauthMap.status, 401);

    const wrong = await login(base, "wrong", "editor");
    assert.equal(wrong.response.status, 401);
    assert.equal(wrong.cookie, "");

    const editor = await login(base, "editor-pass", "editor");
    assert.equal(editor.response.status, 200);
    assert.equal(editor.json.role, "editor");
    assert.ok(editor.cookie.startsWith("gensokyo_session="));
    const editorMap = await fetch(`${base}/api/map`, { headers: { Cookie: editor.cookie } });
    assert.equal(editorMap.status, 200);

    const viewer = await login(base, "viewer-pass", "viewer");
    assert.equal(viewer.response.status, 200);
    assert.equal(viewer.json.role, "viewer");
    const viewerMap = await fetch(`${base}/api/map`, { headers: { Cookie: viewer.cookie } });
    assert.equal(viewerMap.status, 200);
    const viewerWrite = await fetch(`${base}/api/backups`, {
      method: "POST",
      headers: { Cookie: viewer.cookie },
    });
    assert.equal(viewerWrite.status, 403);

    const admin = await login(base, "admin-pass", "admin");
    assert.equal(admin.response.status, 200);
    assert.equal(admin.json.role, "admin");

    const controller = new AbortController();
    const stream = await fetch(`${base}/api/collab/stream?clientId=viewer-1&name=Viewer`, {
      headers: { Cookie: viewer.cookie },
      signal: controller.signal,
    });
    assert.equal(stream.status, 200);
    const reader = stream.body.getReader();
    const hello = await readSseUntil(reader, "event: hello");
    assert.match(hello.buffer, /event: hello/);

    const kick = await fetch(`${base}/api/collab/kick`, {
      method: "POST",
      headers: { "Content-Type": "application/json", Cookie: admin.cookie },
      body: JSON.stringify({ clientId: "viewer-1" }),
    });
    assert.equal(kick.status, 200);
    const kicked = await readSseUntil(reader, "event: kick", hello.buffer);
    assert.match(kicked.buffer, /event: kick/);
    controller.abort();

    const viewerAfterKick = await fetch(`${base}/api/map`, { headers: { Cookie: viewer.cookie } });
    assert.equal(viewerAfterKick.status, 401);
    const adminAfterKick = await fetch(`${base}/api/map`, { headers: { Cookie: admin.cookie } });
    assert.equal(adminAfterKick.status, 200);
  } finally {
    await stopServer(server.child);
    await removeTemp(tempRoot);
  }
});

test("auth remains disabled without passwords", { timeout: 25000 }, async () => {
  const tempRoot = await mkdtemp(path.join(tmpdir(), "gensokyo-auth-off-"));
  const sourceDir = path.join(tempRoot, "map");
  await writeSource(sourceDir);
  const port = await freePort();
  const base = `http://127.0.0.1:${port}`;
  const server = startServer(sourceDir, port);
  try {
    await waitForHealth(base, server);
    const status = await (await fetch(`${base}/api/auth/status`)).json();
    assert.equal(status.required, false);
    assert.equal(status.authenticated, true);
    assert.equal(status.role, "editor");
    const map = await fetch(`${base}/api/map`);
    assert.equal(map.status, 200);
  } finally {
    await stopServer(server.child);
    await removeTemp(tempRoot);
  }
});