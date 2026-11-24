import test from "node:test";
import assert from "node:assert/strict";
import { spawn } from "node:child_process";
import { mkdtemp, mkdir, readFile, readdir, rm, stat, writeFile } from "node:fs/promises";
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
  if (!child || child.exitCode !== null) {
    return;
  }
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

async function waitForHealth(base, child) {
  const deadline = Date.now() + 15000;
  while (Date.now() < deadline) {
    if (child.exitCode !== null) {
      throw new Error(`editor server exited with code ${child.exitCode}`);
    }
    try {
      const response = await fetch(`${base}/api/health`);
      if (response.ok) {
        return;
      }
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

function postJson(url, body) {
  return requestJson(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

test("regeneration endpoint writes and cancels a pending marker without deleting the dimension", async (t) => {
  const mapSource = await mkdtemp(path.join(tmpdir(), "gensokyo-regen-source-"));
  const worldDir = await mkdtemp(path.join(tmpdir(), "gensokyo-regen-world-"));
  t.after(async () => {
    await rm(mapSource, { recursive: true, force: true });
    await rm(worldDir, { recursive: true, force: true });
  });

  await writeFile(path.join(mapSource, "map.json"), "{}\n", "utf8");
  await writeFile(path.join(worldDir, "level.dat"), "", "utf8");
  const dimensionDir = path.join(worldDir, "dimensions", "reverie_dreams", "gensokyo");
  await mkdir(dimensionDir, { recursive: true });
  await writeFile(path.join(dimensionDir, "region.mca"), "old-region", "utf8");

  const port = await freePort();
  const base = `http://127.0.0.1:${port}`;
  const { child, output } = startServer(mapSource, port);
  t.after(async () => {
    await stopServer(child);
  });

  try {
    await waitForHealth(base, child);

    let result = await requestJson(
      `${base}/api/gensokyo/regenerate/status?world=${encodeURIComponent(worldDir)}`,
    );
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    assert.equal(result.json.ok, true);
    assert.equal(result.json.pending, false);
    assert.equal(result.json.dimensionExists, true);

    result = await postJson(`${base}/api/gensokyo/regenerate`, { world: worldDir });
    assert.equal(result.response.status, 400, JSON.stringify(result.json));
    assert.match(result.json.error, /confirm/);

    result = await postJson(`${base}/api/gensokyo/regenerate`, { world: worldDir, confirm: true });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    assert.equal(result.json.ok, true);
    assert.equal(result.json.alreadyPending, false);
    assert.equal(result.json.dimensionExists, true);
    assert.ok(result.json.mapBackup);

    const marker = path.join(worldDir, "reverie_dreams_gensokyo_regenerate.json");
    assert.equal((await stat(marker)).isFile(), true);
    const payload = JSON.parse(await readFile(marker, "utf8"));
    assert.equal(payload.dimension, "reverie_dreams:gensokyo");
    assert.equal(payload.requestedBy, "editor");
    assert.equal((await stat(path.join(dimensionDir, "region.mca"))).isFile(), true);

    const backups = await readdir(path.join(mapSource, "backups"));
    assert.equal(backups.length, 1);
    const metadata = JSON.parse(
      await readFile(path.join(mapSource, "backups", backups[0], "backup.json"), "utf8"),
    );
    assert.equal(metadata.label, "before-regenerate");

    result = await requestJson(
      `${base}/api/gensokyo/regenerate/status?world=${encodeURIComponent(worldDir)}`,
    );
    assert.equal(result.json.pending, true);

    result = await postJson(`${base}/api/gensokyo/regenerate`, { world: worldDir, confirm: true });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    assert.equal(result.json.alreadyPending, true);

    result = await postJson(`${base}/api/gensokyo/regenerate/cancel`, { world: worldDir });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    assert.equal(result.json.removed, true);
    await assert.rejects(stat(marker));

    result = await postJson(`${base}/api/gensokyo/regenerate/cancel`, { world: worldDir });
    assert.equal(result.response.status, 200, JSON.stringify(result.json));
    assert.equal(result.json.removed, false);

    const missingWorld = await mkdtemp(path.join(tmpdir(), "gensokyo-regen-missing-"));
    t.after(async () => {
      await rm(missingWorld, { recursive: true, force: true });
    });
    result = await postJson(`${base}/api/gensokyo/regenerate`, { world: missingWorld, confirm: true });
    assert.equal(result.response.status, 400, JSON.stringify(result.json));
    assert.match(result.json.error, /level\.dat/);
  } finally {
    await stopServer(child);
  }
});
