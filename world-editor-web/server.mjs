import http from "node:http";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { copyFile, mkdir, open, readFile, readdir, rename, rm, stat, writeFile } from "node:fs/promises";
import { gzipSync, gunzipSync } from "node:zlib";
import { readStructure, readStructureSize } from "./nbt.mjs";
import { CellLeases, validateCells, validateTerrainOperation } from "./collaboration-locks.mjs";
import { createAuth } from "./auth.mjs";
import { SaveSessions } from "./save-sessions.mjs";
import { writeAtomic } from "./atomic-file.mjs";
import { assertRoadResizeSafe, validateRoadNetwork } from "./road-network.mjs";
import { runtimeLayerFilesFor, decodeRawSurfaceLayer } from "./runtime-layer-format.mjs";

const here = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(here, "..");
const publicDir = path.join(here, "public");
const sourceOverride = process.env.GENSOYO_EDITOR_SOURCE
  ? path.resolve(process.env.GENSOYO_EDITOR_SOURCE)
  : null;
const sourcePath = sourceOverride
  ? (path.extname(sourceOverride).toLowerCase() === ".json"
    ? sourceOverride
    : path.join(sourceOverride, "map.json"))
  : path.join(repoRoot, "map_source", "gensokyo", "map.json");
const undergroundSourcePath = path.join(path.dirname(sourcePath), "underground.json");
const runtimePath = path.join(
  repoRoot,
  "common",
  "src",
  "main",
  "resources",
  "data",
  "reverie_dreams",
  "gensokyo",
  "map.json",
);
const undergroundRuntimePath = path.join(path.dirname(runtimePath), "underground.json");
const structureDir = path.join(
  repoRoot,
  "common",
  "src",
  "main",
  "resources",
  "data",
  "reverie_dreams",
  "structure",
);
const dataRoot = path.join(repoRoot, "common", "src", "main", "resources", "data");
const generatedDataRoot = path.join(repoRoot, "common", "src", "generated", "resources", "data");
const blockPalettePath = path.join(generatedDataRoot, "reverie_dreams", "gensokyo", "blocks.json");
const blockStatesPath = path.join(generatedDataRoot, "reverie_dreams", "gensokyo", "block_states.json");
const biomePalettePath = path.join(generatedDataRoot, "reverie_dreams", "gensokyo", "biomes.json");
const biomeTranslationPath = path.join(repoRoot, "common", "src", "generated", "resources", "assets", "reverie_dreams", "lang", "zh_cn.json");
const backupRoot = path.join(path.dirname(sourcePath), "backups");
const mapFileNames = ["map.json", "heightmap.bin", "biomes.bin", "surfaces.bin", "underground.json"];
const revisionPath = path.join(path.dirname(sourcePath), ".editor-revision.json");
const stagingRoot = path.join(path.dirname(sourcePath), ".save-staging");
const GENSOKYO_REGENERATE_MARKER = "reverie_dreams_gensokyo_regenerate.json";
const defaultWorldDir = path.join(repoRoot, "fabric", "runs", "world");
const SAVE_TTL_MS = 60_000;
let workingRevision = 0;
let committedRevision = 0;
let lastSaveKind = null;
const saveSessions = new SaveSessions(stagingRoot, SAVE_TTL_MS);

const host = process.env.HOST || "127.0.0.1";
const port = Number(process.env.PORT || 3210);
const auth = createAuth({
  editorPassword: process.env.GENSOYO_EDITOR_PASSWORD,
  viewerPassword: process.env.GENSOYO_EDITOR_VIEWER_PASSWORD,
  adminPassword: process.env.GENSOYO_EDITOR_ADMIN_PASSWORD,
  room: process.env.GENSOYO_EDITOR_ROOM,
  ttlMs: Number(process.env.GENSOYO_EDITOR_SESSION_TTL_MS) || undefined,
});
const maxBodyBytes = Math.max(64, Number(process.env.GENSOYO_EDITOR_MAX_BODY_MB) || 256) * 1024 * 1024;
const MAX_MAP_SIZE = Math.max(4096, Number(process.env.GENSOYO_EDITOR_MAX_MAP_SIZE) || 32768);
const MAX_MAP_CELLS = Math.max(MAX_MAP_SIZE, Number(process.env.GENSOYO_EDITOR_MAX_MAP_CELLS) || 134_217_728);
const LAYER_NAMES = ["heightmap", "biomes", "surfaces"];
const collabClients = new Map();
const collabEvents = [];
const COLLAB_EVENT_LIMIT = 2000;
let collabSequence = 0;
const LOCK_TTL_MS = 20000;
const collabLocks = new CellLeases(LOCK_TTL_MS);
const activeLocks = () => collabLocks.list();
const META_FIELDS = ["spawn", "surface_palette", "biome_palette", "sea_level", "dirt_depth", "biome_transition"];
let collabMeta = null;
let collabMetaPromise = null;
setInterval(() => {
  if (collabLocks.expire()) collabBroadcast({ type: "locks", locks: activeLocks() });
}, 1000).unref();
setInterval(() => {
  void saveSessions.expire();
}, 5000).unref();

function collabColor(value) {
  let hash = 0;
  for (const character of String(value)) {
    hash = (hash * 31 + character.charCodeAt(0)) | 0;
  }
  const hue = Math.abs(hash) % 360;
  return `hsl(${hue} 70% 62%)`;
}

function collabUserList() {
  return [...collabClients.values()].map((client) => ({
    id: client.id,
    name: client.name,
    color: client.color,
    role: client.role || "editor",
  }));
}

function collabWrite(res, event, payload, id) {
  res.write(`${id ? `id: ${id}\n` : ""}event: ${event}\ndata: ${JSON.stringify(payload)}\n\n`);
}

function collabBroadcast(event, exceptId = null) {
  for (const client of collabClients.values()) {
    if (client.id === exceptId) {
      continue;
    }
    collabWrite(client.res, "collab", event);
  }
}

function collabBroadcastMeta(event) {
  const fullPayload = collabMeta ? { ...structuredClone(collabMeta), delta: false } : null;
  for (const client of collabClients.values()) {
    if (client.id === event.from) {
      continue;
    }
    const payload = client.metaDelta === true ? event.payload : fullPayload;
    if (!payload) {
      continue;
    }
    collabWrite(client.res, "collab", { ...event, payload });
  }
}

function collabBroadcastPresence() {
  collabBroadcast({ type: "presence", users: collabUserList() });
}

async function collabReset(reason = "reset") {
  invalidateCollabMeta();
  bumpWorkingRevision();
  committedRevision = workingRevision;
  lastSaveKind = "reset";
  await persistRevision();
  collabEvents.length = 0;
  collabLocks.clear();
  collabBroadcast({ type: "locks", locks: [] });
  collabBroadcast({ type: "reset", revision: workingRevision, reason });
}

function metaFromMap(map) {
  return {
    anchors: structuredClone(map.anchors ?? []),
    spawn: map.spawn ? structuredClone(map.spawn) : null,
    surface_palette: [...(map.surface_palette ?? [])],
    biome_palette: [...(map.biome_palette ?? [])],
    sea_level: map.sea_level,
    dirt_depth: map.dirt_depth ?? 3,
    biome_transition: map.biome_transition ?? 0,
  };
}

async function collabMetaSnapshot() {
  if (collabMeta) {
    return collabMeta;
  }
  if (!collabMetaPromise) {
    collabMetaPromise = readMapFromDisk()
      .then((map) => {
        collabMeta = metaFromMap(map);
        return collabMeta;
      })
      .finally(() => {
        collabMetaPromise = null;
      });
  }
  return await collabMetaPromise;
}

function invalidateCollabMeta() {
  collabMeta = null;
}

function canonicalMetaValue(value) {
  if (Array.isArray(value)) {
    return value.map(canonicalMetaValue);
  }
  if (value && typeof value === "object") {
    const result = {};
    for (const key of Object.keys(value).sort()) {
      result[key] = canonicalMetaValue(value[key]);
    }
    return result;
  }
  return value;
}

function metaValueEqual(left, right) {
  return JSON.stringify(canonicalMetaValue(left)) === JSON.stringify(canonicalMetaValue(right));
}

function failMeta(message) {
  throw new Error(message);
}

function validateMetaAnchor(anchor, label = "anchor") {
  if (!anchor || typeof anchor !== "object" || Array.isArray(anchor)) {
    failMeta(`${label} must be an object`);
  }
  if (typeof anchor.id !== "string" || anchor.id.length === 0 || anchor.id.length > 200) {
    failMeta(`${label}.id must be a non-empty string`);
  }
  if (!Array.isArray(anchor.pos) || anchor.pos.length !== 3 || !anchor.pos.every(Number.isInteger)) {
    failMeta(`${label}.pos must contain three integers`);
  }
  if (anchor.template !== undefined && typeof anchor.template !== "string") {
    failMeta(`${label}.template must be a string`);
  }
  if (anchor.structure !== undefined && typeof anchor.structure !== "string") {
    failMeta(`${label}.structure must be a string`);
  }
  if (anchor.scale !== undefined
      && (!Number.isFinite(Number(anchor.scale)) || Number(anchor.scale) < 0.25 || Number(anchor.scale) > 4)) {
    failMeta(`${label}.scale must be in 0.25..4`);
  }
  return anchor;
}

function validateMetaPayload(payload, delta) {
  if (!payload || typeof payload !== "object" || Array.isArray(payload)) {
    failMeta("meta payload must be an object");
  }
  const anchors = payload.anchors ?? [];
  if (!Array.isArray(anchors) || anchors.length > 10000) {
    failMeta("meta anchors must be an array of at most 10000 entries");
  }
  const ids = new Set();
  for (const anchor of anchors) {
    validateMetaAnchor(anchor);
    if (ids.has(anchor.id)) {
      failMeta(`duplicate anchor id: ${anchor.id}`);
    }
    ids.add(anchor.id);
  }
  const removed = payload.removedAnchors ?? [];
  if (!Array.isArray(removed) || removed.length > 10000 || !removed.every((id) => typeof id === "string")) {
    failMeta("removedAnchors must be an array of strings");
  }
  if (payload.baseAnchors !== undefined) {
    if (!payload.baseAnchors || typeof payload.baseAnchors !== "object" || Array.isArray(payload.baseAnchors)) {
      failMeta("baseAnchors must be an object");
    }
    for (const [id, base] of Object.entries(payload.baseAnchors)) {
      if (base !== null) {
        validateMetaAnchor(base, `baseAnchors.${id}`);
      }
    }
  }
  if (!delta) {
    if (!Array.isArray(payload.surface_palette) || !Array.isArray(payload.biome_palette)) {
      failMeta("full meta requires surface_palette and biome_palette arrays");
    }
    if (payload.spawn !== null && (typeof payload.spawn !== "object" || Array.isArray(payload.spawn))) {
      failMeta("spawn must be an object or null");
    }
  }
  for (const field of ["surface_palette", "biome_palette"]) {
    if (Object.prototype.hasOwnProperty.call(payload, field)) {
      const value = payload[field];
      if (!Array.isArray(value) || value.length > 65536 || !value.every((entry) => typeof entry === "string")) {
        failMeta(`${field} must be an array of at most 65536 strings`);
      }
    }
  }
  for (const field of ["sea_level", "dirt_depth", "biome_transition"]) {
    if (Object.prototype.hasOwnProperty.call(payload, field)) {
      const value = payload[field];
      if (!Number.isInteger(value)) {
        failMeta(`${field} must be an integer`);
      }
    }
  }
  if (typeof payload.dirt_depth === "number" && (payload.dirt_depth < 0 || payload.dirt_depth > 32)) {
    failMeta("dirt_depth must be in 0..32");
  }
  if (typeof payload.biome_transition === "number" && (payload.biome_transition < 0 || payload.biome_transition > 64)) {
    failMeta("biome_transition must be in 0..64");
  }
  if (payload.baseFields !== undefined
      && (!payload.baseFields || typeof payload.baseFields !== "object" || Array.isArray(payload.baseFields))) {
    failMeta("baseFields must be an object");
  }
  return payload;
}

function metaFullFromPayload(payload) {
  return {
    anchors: structuredClone(payload.anchors ?? []),
    spawn: payload.spawn ? structuredClone(payload.spawn) : null,
    surface_palette: [...(payload.surface_palette ?? [])],
    biome_palette: [...(payload.biome_palette ?? [])],
    sea_level: payload.sea_level,
    dirt_depth: Number.isInteger(payload.dirt_depth) ? payload.dirt_depth : 3,
    biome_transition: Number.isInteger(payload.biome_transition) ? payload.biome_transition : 0,
  };
}

async function applyCollabMetaPayload(payload) {
  const meta = await collabMetaSnapshot();
  if (payload?.delta !== true) {
    validateMetaPayload(payload, false);
    const full = metaFullFromPayload(payload);
    collabMeta = full;
    return {
      applied: { ...structuredClone(full), delta: false },
      conflicts: [],
      appliedAny: true,
    };
  }

  validateMetaPayload(payload, true);
  const anchors = new Map((meta.anchors ?? []).map((anchor) => [anchor.id, anchor]));
  const baseAnchors = payload.baseAnchors && typeof payload.baseAnchors === "object"
    ? payload.baseAnchors
    : null;
  const baseFields = payload.baseFields && typeof payload.baseFields === "object"
    ? payload.baseFields
    : null;
  const hasOwn = (object, key) => Boolean(object && Object.prototype.hasOwnProperty.call(object, key));
  const applied = { delta: true, anchors: [], removedAnchors: [] };
  const conflicts = [];

  for (const incoming of payload.anchors ?? []) {
    const current = anchors.get(incoming.id) ?? null;
    if (baseAnchors && hasOwn(baseAnchors, incoming.id)) {
      const base = baseAnchors[incoming.id];
      const matches = base === null
        ? current === null
        : current !== null && metaValueEqual(current, base);
      if (!matches) {
        conflicts.push({ kind: "anchor", id: incoming.id, server: structuredClone(current), client: structuredClone(incoming) });
        continue;
      }
    }
    const next = structuredClone(incoming);
    anchors.set(incoming.id, next);
    applied.anchors.push(structuredClone(next));
  }

  for (const id of payload.removedAnchors ?? []) {
    const current = anchors.get(id) ?? null;
    if (baseAnchors && hasOwn(baseAnchors, id)) {
      const base = baseAnchors[id];
      const matches = base === null
        ? current === null
        : current !== null && metaValueEqual(current, base);
      if (!matches) {
        conflicts.push({ kind: "anchor", id, server: structuredClone(current), client: null });
        continue;
      }
    }
    if (current !== null) {
      anchors.delete(id);
      applied.removedAnchors.push(id);
    }
  }
  meta.anchors = [...anchors.values()];

  for (const field of META_FIELDS) {
    if (!Object.prototype.hasOwnProperty.call(payload, field)) {
      continue;
    }
    if (baseFields && hasOwn(baseFields, field) && !metaValueEqual(meta[field], baseFields[field])) {
      conflicts.push({
        kind: "field",
        field,
        server: structuredClone(meta[field]),
        client: structuredClone(payload[field]),
      });
      continue;
    }
    meta[field] = structuredClone(payload[field]);
    applied[field] = structuredClone(payload[field]);
  }

  const appliedAny = applied.anchors.length > 0
    || applied.removedAnchors.length > 0
    || META_FIELDS.some((field) => Object.prototype.hasOwnProperty.call(applied, field));
  return { applied, conflicts, appliedAny };
}

async function loadRevision() {
  try {
    const data = JSON.parse(await readFile(revisionPath, "utf8"));
    committedRevision = Number.isInteger(data?.revision) && data.revision >= 0 ? data.revision : 0;
  } catch {
    committedRevision = 0;
  }
  workingRevision = committedRevision;
}

async function persistRevision() {
  await mkdir(path.dirname(revisionPath), { recursive: true });
  await writeAtomic(revisionPath, `${JSON.stringify({ revision: committedRevision }, null, 2)}\n`);
}

function bumpWorkingRevision() {
  workingRevision += 1;
  return workingRevision;
}

function saveTokenFrom(req) {
  const value = req.headers["x-save-token"];
  return typeof value === "string" ? value.trim() : "";
}

function rejectIfSaving(res) {
  if (!saveSessions.current) {
    return false;
  }
  sendJson(res, 409, {
    ok: false,
    error: saveSessions.current.committing ? "地图正在提交保存，请稍后重试" : "另一个编辑者正在保存，请稍后重试",
    currentRevision: workingRevision,
    saveBy: saveSessions.current.clientId || null,
  });
  return true;
}

const mimeTypes = {
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".svg": "image/svg+xml",
  ".png": "image/png",
};

function sendJson(res, status, value) {
  const body = Buffer.from(JSON.stringify(value));
  let payload = body;
  const headers = {
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": res.__cacheControl || "no-store",
  };
  if (body.length >= 1024 && res.__acceptGzip) {
    payload = gzipSync(body, { level: 6 });
    headers["Content-Encoding"] = "gzip";
    headers["Vary"] = "Accept-Encoding";
  }
  headers["Content-Length"] = payload.length;
  res.writeHead(status, headers);
  res.end(payload);
}

async function fileEtag(files) {
  const parts = [];
  for (const file of files) {
    try {
      const info = await stat(file);
      parts.push(`${info.size.toString(16)}-${Math.floor(info.mtimeMs).toString(16)}`);
    } catch {
      parts.push("missing");
    }
  }
  return `"${parts.join("-")}"`;
}

function sendNotModified(res, etag) {
  res.writeHead(304, {
    ETag: etag,
    "Cache-Control": "no-cache",
  });
  res.end();
}

function sendText(res, status, message) {
  res.writeHead(status, {
    "Content-Type": "text/plain; charset=utf-8",
    "Cache-Control": "no-store",
  });
  res.end(message);
}

async function readRequestBody(req) {
  const buffer = await readRequestBodyBuffer(req);
  return JSON.parse(buffer.toString("utf8"));
}

async function readRequestBodyBuffer(req) {
  let size = 0;
  const chunks = [];
  for await (const chunk of req) {
    size += chunk.length;
    if (size > maxBodyBytes) {
      throw new Error(`request body larger than ${maxBodyBytes} bytes`);
    }
    chunks.push(chunk);
  }
  if (chunks.length === 0) {
    throw new Error("request body is empty");
  }
  return Buffer.concat(chunks);
}

async function readMapFromDisk() {
  await saveSessions.waitForCommit();
  return JSON.parse(await readFile(sourcePath, "utf8"));
}

async function readUndergroundFromDisk() {
  await saveSessions.waitForCommit();
  try {
    return JSON.parse(await readFile(undergroundSourcePath, "utf8"));
  } catch (error) {
    if (error && error.code === "ENOENT") {
      return { format_version: 1, blocks: [] };
    }
    throw error;
  }
}

async function writeMapToDisk(file, map) {
  await mkdir(path.dirname(file), { recursive: true });
  const clean = { ...map };
  delete clean.revision;
  delete clean.liveRevision;
  await writeAtomic(file, `${JSON.stringify(clean, null, 2)}\n`);
}

function asInt(value) {
  return Number.isInteger(value) ? value : null;
}

function validateMap(map) {
  const errors = [];
  const fail = (message) => errors.push(message);

  if (!map || typeof map !== "object" || Array.isArray(map)) {
    return ["map 必须是一个 JSON 对象"];
  }

  if (map.format_version !== 1) {
    fail(`不支持的 format_version: ${map.format_version}`);
  }
  if (asInt(map.map_version) === null || map.map_version < 1) {
    fail("map_version 必须是 >= 1 的整数");
  }
  if (typeof map.id !== "string" || map.id.length === 0) {
    fail("id 必须是非空字符串");
  }

  const width = asInt(map.width);
  const depth = asInt(map.depth);
  if (width === null || width <= 0) fail("width 必须是正整数");
  if (depth === null || depth <= 0) fail("depth 必须是正整数");
  if (width !== null && width > MAX_MAP_SIZE) fail(`width 不能超过 ${MAX_MAP_SIZE}`);
  if (depth !== null && depth > MAX_MAP_SIZE) fail(`depth 不能超过 ${MAX_MAP_SIZE}`);
  if (errors.length > 0) return errors;

  const cells = width * depth;
  if (!Number.isSafeInteger(cells) || cells > MAX_MAP_CELLS) {
    return [`地图太大: ${width}x${depth}，总格数上限 ${MAX_MAP_CELLS}`];
  }

  if (!Array.isArray(map.origin) || map.origin.length !== 2 || map.origin.some((v) => asInt(v) === null)) {
    fail("origin 必须是 2 个整数");
  }

  const minY = asInt(map.min_y);
  const worldHeight = asInt(map.world_height);
  const seaLevel = asInt(map.sea_level);
  if (minY === null) fail("min_y 必须是整数");
  if (worldHeight === null || worldHeight <= 0) fail("world_height 必须是正整数");
  if (seaLevel === null) fail("sea_level 必须是整数");
  if (minY !== null && worldHeight !== null && seaLevel !== null) {
    if (seaLevel < minY || seaLevel >= minY + worldHeight) {
      fail("sea_level 必须在地图高度范围内");
    }
  }
  if (map.biome_transition !== undefined) {
    const transition = asInt(map.biome_transition);
    if (transition === null || transition < 0 || transition > 64) {
      fail("biome_transition 必须是 0..64 的整数");
    }
  }
  if (Array.isArray(map.anchors)) {
    for (const anchor of map.anchors) {
      if (anchor?.scale !== undefined
          && (!Number.isFinite(Number(anchor.scale)) || Number(anchor.scale) < 0.25 || Number(anchor.scale) > 4)) {
        fail("anchor.scale 必须在 0.25..4 之间");
      }
    }
  }

  const hasLayers = map.layers !== null && typeof map.layers === "object" && !Array.isArray(map.layers);
  const checkLayer = (name, value, maxValue, allowNegativeSentinel = false) => {
    if (!Array.isArray(value)) {
      fail(`${name} 必须是数组`);
      return;
    }
    if (value.length !== cells) {
      fail(`${name} 长度应为 ${cells}，实际为 ${value.length}`);
      return;
    }
    for (let i = 0; i < value.length; i++) {
      const v = value[i];
      if (!Number.isInteger(v)) {
        fail(`${name}[${i}] 不是整数`);
        return;
      }
      if (allowNegativeSentinel && v === -32768) continue;
      if (v < 0 || v > maxValue) {
        fail(`${name}[${i}] 超出范围 0..${maxValue}`);
        return;
      }
    }
  };

  if (Array.isArray(map.biome_palette)) {
    if (map.biome_palette.length === 0) fail("biome_palette 不能为空");
    if (!hasLayers) {
      checkLayer("biomes", map.biomes, Math.max(0, map.biome_palette.length - 1));
    }
  } else {
    fail("biome_palette 必须是数组");
  }

  if (Array.isArray(map.surface_palette)) {
    if (map.surface_palette.length === 0) fail("surface_palette 不能为空");
    if (!hasLayers) {
      checkLayer("surfaces", map.surfaces, Math.max(0, map.surface_palette.length - 1));
    }
  } else {
    fail("surface_palette 必须是数组");
  }

  if (hasLayers) {
    for (const name of LAYER_NAMES) {
      const filename = map.layers[name];
      if (typeof filename !== "string" || filename.length === 0) {
        fail(`layers.${name} 必须是非空文件名`);
      } else if (filename.includes("..") || filename.startsWith("/")) {
        fail(`layers.${name} 包含非法路径: ${filename}`);
      }
    }
  } else if (minY !== null && worldHeight !== null) {
    if (!Array.isArray(map.heights)) {
      fail("heights 必须是数组");
    } else if (map.heights.length !== cells) {
      fail(`heights 长度应为 ${cells}，实际为 ${map.heights.length}`);
    } else {
      const maxY = minY + worldHeight - 1;
      for (let i = 0; i < map.heights.length; i++) {
        const v = map.heights[i];
        if (!Number.isInteger(v)) {
          fail(`heights[${i}] 不是整数`);
          break;
        }
        if (v !== -32768 && (v < minY || v > maxY)) {
          fail(`heights[${i}]=${v} 超出 ${minY}..${maxY}`);
          break;
        }
      }
    }
  }

  const originX = Array.isArray(map.origin) ? asInt(map.origin[0]) : null;
  const originZ = Array.isArray(map.origin) ? asInt(map.origin[1]) : null;
  const inside = (x, z) =>
    originX !== null &&
    originZ !== null &&
    x >= originX &&
    x < originX + width &&
    z >= originZ &&
    z < originZ + depth;

  const spawnPos = map.spawn && Array.isArray(map.spawn.pos) ? map.spawn.pos : null;
  if (!spawnPos || spawnPos.length !== 3 || spawnPos.some((v) => asInt(v) === null)) {
    fail("spawn.pos 必须是 3 个整数");
  } else {
    if (!inside(spawnPos[0], spawnPos[2])) {
      fail("spawn 必须在地图范围内");
    }
    if (minY !== null && worldHeight !== null) {
      const maxY = minY + worldHeight;
      if (spawnPos[1] < minY || spawnPos[1] > maxY) {
        fail(`spawn Y 必须在 ${minY}..${maxY} 内`);
      }
    }
  }

  if (map.anchors !== undefined) {
    if (!Array.isArray(map.anchors)) {
      fail("anchors 必须是数组");
    } else {
      map.anchors.forEach((anchor, index) => {
        if (!anchor || typeof anchor !== "object") {
          fail(`anchors[${index}] 必须是对象`);
          return;
        }
        if (typeof anchor.id !== "string" || anchor.id.length === 0) {
          fail(`anchors[${index}].id 必须是非空字符串`);
        }
        const pos = Array.isArray(anchor.pos) ? anchor.pos : null;
        if (!pos || pos.length !== 3 || pos.some((v) => asInt(v) === null)) {
          fail(`anchors[${index}].pos 必须是 3 个整数`);
          return;
        }
        if (!inside(pos[0], pos[2])) {
          fail(`anchors[${index}] 必须在地图范围内`);
        }
      });
    }
  }

  errors.push(...validateRoadNetwork(map));
  return errors;
}

function validateUnderground(map, data) {
  const errors = [];
  if (!data || typeof data !== "object" || Array.isArray(data)) {
    return ["underground 必须是一个 JSON 对象"];
  }
  if (data.format_version !== 1) {
    errors.push(`不支持的 underground format_version: ${data.format_version}`);
  }
  if (!Array.isArray(data.blocks)) {
    return [...errors, "underground.blocks 必须是数组"];
  }
  if (data.blocks.length > 5000000) {
    return [...errors, "underground.blocks 数量过大（上限 5,000,000）"];
  }
  const minY = map.min_y;
  const maxY = map.min_y + map.world_height - 1;
  for (let i = 0; i < data.blocks.length; i++) {
    const entry = data.blocks[i];
    if (!entry || typeof entry !== "object") {
      errors.push(`blocks[${i}] 不是对象`);
      break;
    }
    if (typeof entry.block !== "string" || entry.block.length === 0) {
      errors.push(`blocks[${i}].block 必须是非空方块 ID`);
      break;
    }
    const pos = entry.pos;
    if (!Array.isArray(pos) || pos.length !== 3 || pos.some((value) => !Number.isInteger(value))) {
      errors.push(`blocks[${i}].pos 必须是 3 个整数`);
      break;
    }
    if (pos[0] < 0 || pos[0] >= map.width || pos[2] < 0 || pos[2] >= map.depth) {
      errors.push(`blocks[${i}] 在地图水平范围外`);
      break;
    }
    if (pos[1] < minY || pos[1] > maxY) {
      errors.push(`blocks[${i}] Y 超出维度高度`);
      break;
    }
  }
  return errors;
}

async function pruneBackups() {
  let entries;
  try {
    entries = await readdir(backupRoot, { withFileTypes: true });
  } catch (error) {
    if (error && error.code === "ENOENT") {
      return;
    }
    throw error;
  }
  const directories = entries
    .filter((entry) => entry.isDirectory())
    .map((entry) => entry.name)
    .sort()
    .reverse();
  for (const name of directories.slice(20)) {
    await rm(path.join(backupRoot, name), { recursive: true, force: true });
  }
}

async function createBackup(label = "manual") {
  const stamp = new Date().toISOString().replace(/[:.]/g, "-");
  const backupDir = path.join(backupRoot, stamp);
  await mkdir(backupDir, { recursive: true });
  const sourceDir = path.dirname(sourcePath);
  for (const name of mapFileNames) {
    try {
      await copyFile(path.join(sourceDir, name), path.join(backupDir, name));
    } catch (error) {
      if (!error || error.code !== "ENOENT") {
        throw error;
      }
    }
  }
  await writeFile(
    path.join(backupDir, "backup.json"),
    `${JSON.stringify({ createdAt: new Date().toISOString(), label }, null, 2)}\n`,
    "utf8",
  );
  await pruneBackups();
  return {
    name: stamp,
    createdAt: new Date().toISOString(),
    label,
    files: mapFileNames,
  };
}

function resolveWorldDir(value) {
  const raw = String(value || "").trim();
  if (!raw) {
    return defaultWorldDir;
  }
  return path.isAbsolute(raw) ? path.resolve(raw) : path.resolve(repoRoot, raw);
}

function displayPath(target) {
  const relative = path.relative(repoRoot, target);
  return relative && !relative.startsWith("..") && !path.isAbsolute(relative) ? relative : target;
}

async function regenerationMarkerStatus(worldDir) {
  const marker = path.join(worldDir, GENSOKYO_REGENERATE_MARKER);
  const dimension = path.join(worldDir, "dimensions", "reverie_dreams", "gensokyo");
  let pending = false;
  let dimensionExists = false;
  try {
    pending = (await stat(marker)).isFile();
  } catch {
    pending = false;
  }
  try {
    dimensionExists = (await stat(dimension)).isDirectory();
  } catch {
    dimensionExists = false;
  }
  return { marker, dimension, pending, dimensionExists };
}

async function listBackups() {
  let entries;
  try {
    entries = await readdir(backupRoot, { withFileTypes: true });
  } catch (error) {
    if (error && error.code === "ENOENT") {
      return [];
    }
    throw error;
  }
  const result = [];
  for (const entry of entries) {
    if (!entry.isDirectory()) {
      continue;
    }
    const dir = path.join(backupRoot, entry.name);
    let metadata = {};
    try {
      metadata = JSON.parse(await readFile(path.join(dir, "backup.json"), "utf8"));
    } catch {
      metadata = {};
    }
    result.push({
      name: entry.name,
      createdAt: metadata.createdAt ?? null,
      label: metadata.label ?? "",
    });
  }
  return result.sort((a, b) => b.name.localeCompare(a.name));
}

async function restoreBackup(name) {
  if (!/^[A-Za-z0-9._-]+$/.test(String(name))) {
    throw new Error("invalid backup name");
  }
  const backupDir = path.join(backupRoot, name);
  const sourceDir = path.dirname(sourcePath);
  await createBackup("before-restore");
  for (const file of mapFileNames) {
    try {
      await copyFile(path.join(backupDir, file), path.join(sourceDir, file));
    } catch (error) {
      if (!error || error.code !== "ENOENT") {
        throw error;
      }
    }
  }
  await collabReset("restore-backup");
  return await readMapFromDisk();
}

function createBlankMap(width, depth) {
  const worldHeight = 1536;
  const seaLevel = 32;
  const baseHeight = 40;
  const cells = width * depth;
  const heightBuffer = Buffer.alloc(cells * 2);
  const biomeBuffer = Buffer.alloc(cells);
  const surfaceBuffer = Buffer.alloc(cells * 2);
  for (let index = 0; index < cells; index++) {
    heightBuffer.writeInt16LE(baseHeight, index * 2);
    surfaceBuffer.writeUInt16LE(1, index * 2);
  }
  return {
    map: {
      format_version: 1,
      map_version: 2,
      id: "reverie_dreams:gensokyo",
      width,
      depth,
      origin: [-Math.floor(width / 2), -Math.floor(depth / 2)],
      min_y: 0,
      world_height: worldHeight,
      surface_bits: 16,
      sea_level: seaLevel,
      dirt_depth: 3,
      biome_transition: 0,
      spawn: {
        pos: [0, baseHeight + 1, 0],
        yaw: 0,
        pitch: 0,
      },
      biome_palette: ["minecraft:plains", "minecraft:forest", "minecraft:stony_peaks", "minecraft:river"],
      surface_palette: ["minecraft:air", "minecraft:grass_block", "minecraft:sand", "minecraft:stone"],
      layers: {
        heightmap: "heightmap.bin",
        biomes: "biomes.bin",
        surfaces: "surfaces.bin",
      },
      underground: "underground.json",
      anchors: [],
    },
    heightBuffer,
    biomeBuffer,
    surfaceBuffer,
  };
}

async function resizeMap(width, depth) {
  const oldMap = await readMapFromDisk();
  assertRoadResizeSafe(oldMap, width, depth);
  if (!oldMap.layers) {
    throw new Error("resize requires binary layers");
  }
  const sourceDir = path.dirname(sourcePath);
  const oldWidth = oldMap.width;
  const oldDepth = oldMap.depth;
  const oldHeightBytes = await readFile(path.join(sourceDir, oldMap.layers.heightmap));
  const oldBiomeBytes = await readFile(path.join(sourceDir, oldMap.layers.biomes));
  const oldSurfaceBytes = await readFile(path.join(sourceDir, oldMap.layers.surfaces));
  if (oldHeightBytes.length !== oldWidth * oldDepth * 2
      || oldBiomeBytes.length !== oldWidth * oldDepth
      || oldSurfaceBytes.length !== oldWidth * oldDepth * 2) {
    throw new Error("current layer sizes do not match map dimensions");
  }
  const cells = width * depth;
  const heightBuffer = Buffer.alloc(cells * 2);
  const biomeBuffer = Buffer.alloc(cells);
  const surfaceBuffer = Buffer.alloc(cells * 2);
  const defaultHeight = 40;
  const defaultSurface = Math.min(1, Math.max(0, (oldMap.surface_palette?.length ?? 1) - 1));
  for (let z = 0; z < depth; z++) {
    for (let x = 0; x < width; x++) {
      const target = z * width + x;
      heightBuffer.writeInt16LE(defaultHeight, target * 2);
      surfaceBuffer.writeUInt16LE(defaultSurface, target * 2);
      if (x < oldWidth && z < oldDepth) {
        const source = z * oldWidth + x;
        heightBuffer.writeInt16LE(oldHeightBytes.readInt16LE(source * 2), target * 2);
        biomeBuffer[target] = oldBiomeBytes[source];
        surfaceBuffer.writeUInt16LE(oldSurfaceBytes.readUInt16LE(source * 2), target * 2);
      }
    }
  }
  const map = {
    ...oldMap,
    width,
    depth,
    layers: {
      heightmap: "heightmap.bin",
      biomes: "biomes.bin",
      surfaces: "surfaces.bin",
    },
    anchors: (oldMap.anchors ?? []).filter((anchor) => {
      const x = anchor.pos?.[0] - oldMap.origin[0];
      const z = anchor.pos?.[2] - oldMap.origin[1];
      return x >= 0 && z >= 0 && x < width && z < depth;
    }),
  };
  const spawn = map.spawn?.pos ?? [0, defaultHeight + 1, 0];
  map.spawn = {
    ...(map.spawn ?? {}),
    pos: [
      Math.max(map.origin[0], Math.min(map.origin[0] + width - 1, spawn[0])),
      spawn[1],
      Math.max(map.origin[1], Math.min(map.origin[1] + depth - 1, spawn[2])),
    ],
  };
  let underground = { format_version: 1, blocks: [] };
  try {
    underground = JSON.parse(await readFile(undergroundSourcePath, "utf8"));
  } catch (error) {
    if (!error || error.code !== "ENOENT") {
      throw error;
    }
  }
  underground.blocks = (underground.blocks ?? []).filter((block) => {
    const x = block.pos?.[0] - map.origin[0];
    const z = block.pos?.[2] - map.origin[1];
    return x >= 0 && z >= 0 && x < width && z < depth;
  });
  const errors = validateMap(map);
  if (errors.length > 0) {
    throw new Error(errors.join("; "));
  }
  await createBackup("before-resize");
  await writeMapToDisk(sourcePath, map);
  await writeFile(path.join(sourceDir, "heightmap.bin"), heightBuffer);
  await writeFile(path.join(sourceDir, "biomes.bin"), biomeBuffer);
  await writeFile(path.join(sourceDir, "surfaces.bin"), surfaceBuffer);
  await writeFile(undergroundSourcePath, `${JSON.stringify(underground, null, 2)}\n`, "utf8");
  await collabReset("resize-map");
  return map;
}

async function createNewMap(width, depth) {
  const blank = createBlankMap(width, depth);
  const errors = validateMap(blank.map);
  if (errors.length > 0) {
    throw new Error(errors.join("; "));
  }
  await createBackup("before-new-map");
  const sourceDir = path.dirname(sourcePath);
  await writeMapToDisk(sourcePath, blank.map);
  await writeFile(path.join(sourceDir, "heightmap.bin"), blank.heightBuffer);
  await writeFile(path.join(sourceDir, "biomes.bin"), blank.biomeBuffer);
  await writeFile(path.join(sourceDir, "surfaces.bin"), blank.surfaceBuffer);
  await writeFile(
    path.join(sourceDir, "underground.json"),
    `${JSON.stringify({ format_version: 1, blocks: [] }, null, 2)}\n`,
    "utf8",
  );
  await collabReset("new-map");
  return blank.map;
}

async function saveMap(map, target) {
  const errors = validateMap(map);
  if (errors.length > 0) {
    return { ok: false, errors };
  }
  if (target === sourcePath) {
    try {
      await createBackup("before-save");
    } catch (error) {
      console.warn("Gensokyo editor backup failed:", error);
    }
  }
  await writeMapToDisk(target, map);
  return { ok: true };
}

function layerFilename(map, name) {
  const filename = map.layers?.[name];
  return typeof filename === "string" ? filename : null;
}

function layerExpectedBytes(map, name) {
  const cells = map.width * map.depth;
  return cells * (name === "heightmap" || name === "surfaces" ? 2 : 1);
}

async function validateLayerFiles(map, directory) {
  const errors = [];
  if (!map.layers || typeof map.layers !== "object") {
    return errors;
  }
  for (const name of LAYER_NAMES) {
    const filename = layerFilename(map, name);
    if (!filename) {
      errors.push(`缺少 layers.${name}`);
      continue;
    }
    const expected = layerExpectedBytes(map, name);
    try {
      const info = await stat(path.join(directory, filename));
      if (info.size !== expected) {
        errors.push(`${name} 文件大小应为 ${expected}，实际为 ${info.size}`);
      }
    } catch {
      errors.push(`找不到图层文件: ${filename}`);
    }
  }
  return errors;
}

async function copyLayerFiles(map, sourceDir, targetDir) {
  if (!map.layers || typeof map.layers !== "object") {
    return;
  }
  await mkdir(targetDir, { recursive: true });
  for (const name of LAYER_NAMES) {
    const filename = layerFilename(map, name);
    if (!filename) {
      continue;
    }
    await copyFile(path.join(sourceDir, filename), path.join(targetDir, filename));
  }
}

function packPalette4(raw, expected, elementSize) {
  const packed = Buffer.alloc((expected + 1) >> 1);
  for (let i = 0; i < expected; i++) {
    const value = elementSize === 2 ? raw.readUInt16LE(i * 2) : raw[i];
    if (value > 15) {
      throw new Error(`palette index ${value} at ${i} does not fit 4 bits`);
    }
    if ((i & 1) === 0) {
      packed[i >> 1] = value << 4;
    } else {
      packed[i >> 1] |= value;
    }
  }
  return packed;
}

function unpackPalette4(packed, expected) {
  if (packed.length !== ((expected + 1) >> 1)) {
    throw new Error(`palette4 size mismatch: expected ${(expected + 1) >> 1}, got ${packed.length}`);
  }
  const out = Buffer.alloc(expected);
  for (let i = 0; i < expected; i++) {
    const value = packed[i >> 1];
    out[i] = (i & 1) === 0 ? (value >> 4) & 15 : value & 15;
  }
  return out;
}

async function writeCompressedRuntime(map, sourceDir, targetDir) {
  const width = map.width;
  const depth = map.depth;
  const expected = width * depth;
  const sourceFiles = Object.fromEntries(LAYER_NAMES.map((name) => [name, layerFilename(map, name)]));
  const heightRaw = await readFile(path.join(sourceDir, sourceFiles.heightmap));
  const surfaceRaw = await readFile(path.join(sourceDir, sourceFiles.surfaces));
  const biomeRaw = await readFile(path.join(sourceDir, sourceFiles.biomes));
  if (heightRaw.length !== expected * 2 || surfaceRaw.length !== expected * 2 || biomeRaw.length !== expected) {
    throw new Error("source layers are not the expected raw sizes");
  }

  const delta = Buffer.alloc(expected * 2);
  for (let z = 0; z < depth; z++) {
    for (let x = 0; x < width; x++) {
      const i = z * width + x;
      const value = heightRaw.readInt16LE(i * 2);
      const previous = z === 0 ? 0 : heightRaw.readInt16LE((i - width) * 2);
      delta.writeInt16LE(z === 0 ? value : value - previous, i * 2);
    }
  }
  await mkdir(targetDir, { recursive: true });
  const runtimeLayers = runtimeLayerFilesFor(map);
  await writeAtomic(path.join(targetDir, runtimeLayers.heightmap), gzipSync(delta, { level: 6 }));
  await writeAtomic(
    path.join(targetDir, runtimeLayers.surfaces),
    gzipSync(runtimeLayers.surfaces.includes(".pal4") ? packPalette4(surfaceRaw, expected, 2) : surfaceRaw, { level: 6 }),
  );
  if (runtimeLayers.biomes.includes(".pal4")) {
    await writeAtomic(
      path.join(targetDir, runtimeLayers.biomes),
      gzipSync(packPalette4(biomeRaw, expected, 1), { level: 6 }),
    );
  } else {
    await writeAtomic(path.join(targetDir, runtimeLayers.biomes), gzipSync(biomeRaw, { level: 6 }));
  }

  const runtimeMap = structuredClone(map);
  runtimeMap.layers = { ...runtimeLayers };
  await writeMapToDisk(path.join(targetDir, "map.json"), runtimeMap);

  for (const name of ["heightmap.bin", "biomes.bin", "biomes.pal4.gz", "surfaces.bin", "biomes.bin.gz", "surfaces.pal4.gz", "surfaces.bin.gz"] ) {
    if (Object.values(runtimeLayers).includes(name)) continue;
    try {
      await rm(path.join(targetDir, name), { force: true });
    } catch {
      // Ignore missing legacy raw files.
    }
  }
  return runtimeMap;
}

async function readCompressedRuntimeLayers(directory, map) {
  const width = map.width;
  const depth = map.depth;
  const expected = width * depth;
  const names = {};
  for (const name of LAYER_NAMES) {
    names[name] = layerFilename(map, name);
  }

  const heightPacked = await readFile(path.join(directory, names.heightmap));
  let heightRaw;
  if (names.heightmap.endsWith(".gz") && names.heightmap.includes("delta")) {
    const delta = gunzipSync(heightPacked);
    if (delta.length !== expected * 2) {
      throw new Error(`delta heightmap size mismatch: ${delta.length}`);
    }
    heightRaw = Buffer.alloc(expected * 2);
    for (let z = 0; z < depth; z++) {
      for (let x = 0; x < width; x++) {
        const i = z * width + x;
        const value = z === 0
          ? delta.readInt16LE(i * 2)
          : heightRaw.readInt16LE((i - width) * 2) + delta.readInt16LE(i * 2);
        heightRaw.writeInt16LE(value, i * 2);
      }
    }
  } else {
    if (heightPacked.length !== expected * 2) {
      throw new Error(`heightmap size mismatch: ${heightPacked.length}`);
    }
    heightRaw = heightPacked;
  }

  const surfacePacked = await readFile(path.join(directory, names.surfaces));
  let surfaceRaw;
  if (names.surfaces.includes(".pal4")) {
    const indices = unpackPalette4(gunzipSync(surfacePacked), expected);
    surfaceRaw = Buffer.alloc(expected * 2);
    for (let i = 0; i < expected; i++) {
      surfaceRaw.writeUInt16LE(indices[i], i * 2);
    }
  } else {
    surfaceRaw = decodeRawSurfaceLayer(surfacePacked, expected, names.surfaces);
  }

  const biomePacked = await readFile(path.join(directory, names.biomes));
  const biomeRaw = names.biomes.includes(".pal4")
    ? unpackPalette4(gunzipSync(biomePacked), expected)
    : names.biomes.endsWith(".gz")
      ? gunzipSync(biomePacked)
      : biomePacked;
  if (biomeRaw.length !== expected) {
    throw new Error(`biomes size mismatch: ${biomeRaw.length}`);
  }
  return { heightRaw, surfaceRaw, biomeRaw };
}

async function copyUnderground(from, to) {
  try {
    await writeAtomic(to, await readFile(from));
  } catch (error) {
    if (error && error.code === "ENOENT") {
      await writeFile(to, `${JSON.stringify({ format_version: 1, blocks: [] })}\n`, "utf8");
      return;
    }
    throw error;
  }
}

function sendBinary(res, status, data, type = "application/octet-stream") {
  res.writeHead(status, {
    "Content-Type": type,
    "Content-Length": data.length,
    "Cache-Control": "no-store",
  });
  res.end(data);
}

async function listTemplateEntries() {
  const roots = [dataRoot, generatedDataRoot];
  const templates = new Map();
  for (const root of roots) {
    let namespaces;
    try {
      namespaces = await readdir(root, { withFileTypes: true });
    } catch {
      continue;
    }
    for (const namespaceEntry of namespaces) {
      if (!namespaceEntry.isDirectory()) {
        continue;
      }
      const namespace = namespaceEntry.name;
      const dir = path.join(root, namespace, "structure");
      let entries;
      try {
        entries = await readdir(dir, { withFileTypes: true });
      } catch {
        continue;
      }
      async function visit(directory, prefix = "") {
        const children = await readdir(directory, { withFileTypes: true });
        for (const entry of children) {
          const relative = prefix + entry.name;
          const file = path.join(directory, entry.name);
          if (entry.isDirectory()) {
            await visit(file, relative + "/");
          } else if (entry.isFile() && entry.name.endsWith(".nbt")) {
            const id = `${namespace}:${relative.slice(0, -4)}`;
            if (!templates.has(id)) {
              templates.set(id, file);
            }
          }
        }
      }
      await visit(dir);
    }
  }
  return [...templates.entries()].sort(([a], [b]) => a.localeCompare(b)).map(([id, file]) => ({ id, file }));
}

async function templateSizes(entries) {
  const sizes = {};
  for (const entry of entries) {
    try {
      sizes[entry.id] = readStructureSize(await readFile(entry.file));
    } catch {
      sizes[entry.id] = null;
    }
  }
  return sizes;
}

async function listTemplateIds() {
  return (await listTemplateEntries()).map((entry) => entry.id);
}

async function handleApi(req, res) {
  const url = new URL(req.url, `http://${req.headers.host || "localhost"}`);
  await saveSessions.waitForCommit();

  const authSession = auth.getSession(req);
  const publicAuthPaths = new Set([
    "/api/health",
    "/api/auth/status",
    "/api/auth/login",
    "/api/auth/logout",
  ]);
  if (auth.required && !publicAuthPaths.has(url.pathname)) {
    if (!authSession) {
      sendJson(res, 401, {
        ok: false,
        authRequired: true,
        error: "需要先进入协作房间",
      });
      return true;
    }
    const mutating = req.method !== "GET" && req.method !== "HEAD";
    if (authSession.role === "viewer" && mutating) {
      sendJson(res, 403, { ok: false, error: "只读角色不能修改地图" });
      return true;
    }
  }

  if (url.pathname === "/api/health") {
    sendJson(res, 200, { ok: true });
    return true;
  }

  if (url.pathname === "/api/auth/status" && req.method === "GET") {
    sendJson(res, 200, { ok: true, ...auth.status(req) });
    return true;
  }

  if (url.pathname === "/api/auth/login" && req.method === "POST") {
    let body;
    try {
      body = await readRequestBody(req);
    } catch {
      sendJson(res, 400, { ok: false, error: "登录请求格式错误" });
      return true;
    }
    const result = auth.login({
      password: body?.password,
      role: body?.role,
      ip: req.socket.remoteAddress || "",
    });
    if (!result.ok) {
      sendJson(res, result.status || 401, {
        ok: false,
        error: result.error,
        roles: auth.roles,
        room: auth.room,
      });
      return true;
    }
    if (result.token) {
      auth.setSessionCookie(res, result.token);
    }
    sendJson(res, 200, {
      ok: true,
      required: auth.required,
      role: result.role,
      room: auth.room,
      roles: auth.roles,
    });
    return true;
  }

  if (url.pathname === "/api/auth/logout" && req.method === "POST") {
    if (authSession) {
      auth.revoke(authSession.token);
    }
    auth.clearSessionCookie(res);
    sendJson(res, 200, { ok: true });
    return true;
  }

  if (url.pathname === "/api/collab/stream" && req.method === "GET") {
    const clientId = String(url.searchParams.get("clientId") || "").slice(0, 80);
    const name = String(url.searchParams.get("name") || "匿名编辑者").slice(0, 40);
    const metaDelta = url.searchParams.get("metaDelta") === "1";
    if (!clientId) {
      sendJson(res, 400, { ok: false, error: "missing clientId" });
      return true;
    }
    res.writeHead(200, {
      "Content-Type": "text/event-stream; charset=utf-8",
      "Cache-Control": "no-cache, no-transform",
      Connection: "keep-alive",
      "X-Accel-Buffering": "no",
    });
    res.write(": connected\n\n");
    const client = {
      id: clientId,
      name,
      color: collabColor(`${name}:${clientId}`),
      role: authSession?.role ?? "editor",
      sessionToken: authSession?.token ?? null,
      metaDelta,
      res,
      heartbeat: null,
    };
    collabClients.set(clientId, client);
    client.heartbeat = setInterval(() => {
      res.write(": ping\n\n");
    }, 15000);
    let metaSnapshot = null;
    try {
      metaSnapshot = await collabMetaSnapshot();
    } catch (error) {
      console.warn(`Gensokyo editor meta snapshot unavailable: ${error?.message ?? error}`);
    }
    const helloEvents = metaDelta
      ? collabEvents
      : collabEvents.filter((event) => event.type !== "meta" || event.payload?.delta === false);
    if (!metaDelta && metaSnapshot) {
      helloEvents.push({
        seq: ++collabSequence,
        type: "meta",
        payload: { ...structuredClone(metaSnapshot), delta: false },
        from: "server",
        name: "服务器",
        color: "#7bd88f",
        revision: workingRevision,
        at: Date.now(),
      });
    }
    collabWrite(res, "hello", {
      clientId,
      meta: metaSnapshot,
      users: collabUserList(),
      events: helloEvents,
      lastSeq: collabSequence,
      revision: workingRevision,
      committedRevision,
      lastSaveKind,
      save: saveSessions.current ? { clientId: saveSessions.current.clientId, expiresAt: saveSessions.current.expiresAt } : null,
      locks: activeLocks(),
    });
    collabBroadcastPresence();
    res.on("close", () => {
      clearInterval(client.heartbeat);
      if (collabClients.get(clientId) === client) {
        collabClients.delete(clientId);
        collabLocks.release(clientId);
        if (saveSessions.current && saveSessions.current.clientId === clientId && !saveSessions.current.committing) {
          void saveSessions.abort(saveSessions.current, "disconnect");
        }
        collabBroadcastPresence();
        collabBroadcast({ type: "locks", locks: activeLocks() });
      }
    });
    return true;
  }

  if (url.pathname === "/api/collab/users" && req.method === "GET") {
    sendJson(res, 200, { ok: true, users: collabUserList() });
    return true;
  }

  if (url.pathname === "/api/collab/kick" && req.method === "POST") {
    const actorRole = authSession?.role ?? (auth.required ? null : "editor");
    if (actorRole !== "admin") {
      sendJson(res, 403, { ok: false, error: "只有管理员可以移出协作者" });
      return true;
    }
    let body;
    try {
      body = await readRequestBody(req);
    } catch {
      sendJson(res, 400, { ok: false, error: "请求格式错误" });
      return true;
    }
    const targetId = String(body?.clientId || "");
    const target = collabClients.get(targetId);
    if (!target) {
      sendJson(res, 404, { ok: false, error: "该协作者已离线" });
      return true;
    }
    const reason = String(body?.reason || "管理员将你移出了房间").slice(0, 200);
    try {
      collabWrite(target.res, "kick", { reason });
      target.res.end();
    } catch {
      // The connection may already be closing.
    }
    collabClients.delete(targetId);
    collabLocks.release(targetId);
    if (target.sessionToken) {
      auth.revoke(target.sessionToken);
    }
    collabBroadcastPresence();
    collabBroadcast({ type: "locks", locks: activeLocks() });
    sendJson(res, 200, { ok: true, kicked: targetId });
    return true;
  }

  if (url.pathname === "/api/collab/locks" && req.method === "GET") {
    sendJson(res, 200, { ok: true, locks: activeLocks() });
    return true;
  }

  if (url.pathname === "/api/collab/lock" && req.method === "POST") {
    const body = await readRequestBody(req);
    const clientId = String(body?.clientId || "");
    if (!collabClients.has(clientId)) {
      sendJson(res, 403, { ok: false, error: "请先连接协作会话" });
      return true;
    }
    const cells = body?.cells ?? [];
    try {
      validateCells(cells, await readMapFromDisk());
    } catch (error) {
      sendJson(res, 400, { ok: false, error: error.message });
      return true;
    }
    if (body?.action === "take") {
      const { granted, denied } = collabLocks.take(clientId, cells);
      collabBroadcast({ type: "locks", locks: activeLocks() });
      sendJson(res, denied.length ? 409 : 200, { ok: !denied.length, granted, denied, ttl: LOCK_TTL_MS });
    } else if (body?.action === "release") {
      collabLocks.release(clientId, cells);
      collabBroadcast({ type: "locks", locks: activeLocks() });
      sendJson(res, 200, { ok: true });
    } else {
      sendJson(res, 200, { ok: true, locks: activeLocks() });
    }
    return true;
  }

  if (url.pathname === "/api/collab/op" && req.method === "POST") {
    const body = await readRequestBody(req);
    const type = String(body?.type || "");
    if (!["terrain", "meta", "cursor"].includes(type)) {
      sendJson(res, 400, { ok: false, error: "unknown collaboration operation" });
      return true;
    }
    const client = collabClients.get(String(body?.clientId || ""));
    if (!client) {
      sendJson(res, 403, { ok: false, error: "协作连接已断开，修改未提交" });
      return true;
    }
    const now = Date.now();
    if (!client.rate || now >= client.rate.resetAt) {
      client.rate = { count: 0, resetAt: now + 60_000 };
    }
    client.rate.count += 1;
    const opLimit = Math.max(60, Number(process.env.GENSOYO_EDITOR_OP_LIMIT) || 1200);
    if (client.rate.count > opLimit) {
      sendJson(res, 429, {
        ok: false,
        error: "协作操作过于频繁，请稍后再试",
        limit: opLimit,
        retryAfterMs: Math.max(0, client.rate.resetAt - now),
      });
      return true;
    }
    if (type !== "cursor" && saveSessions.current?.committing) {
      sendJson(res, 409, { ok: false, error: "地图正在提交保存，请稍后重试", currentRevision: workingRevision });
      return true;
    }
    if (type === "terrain") {
      try {
        const cells = validateTerrainOperation(body.payload, await readMapFromDisk());
        if (!collabLocks.owns(client.id, cells)) {
          sendJson(res, 409, { ok: false, error: "修改区域未持锁或锁已过期，整笔提交被拒绝" });
          return true;
        }
      } catch (error) {
        sendJson(res, 400, { ok: false, error: error.message });
        return true;
      }
      // Do not yield between ownership validation and sequencing/broadcast.
      if (!collabClients.has(client.id)) {
        sendJson(res, 403, { ok: false, error: "协作连接已断开" });
        return true;
      }
    }
    // Re-check after any awaited validation: a save may have started committing.
    if (type !== "cursor" && saveSessions.current?.committing) {
      sendJson(res, 409, { ok: false, error: "地图正在提交保存，请稍后重试", currentRevision: workingRevision });
      return true;
    }
    let metaResult = null;
    if (type === "meta") {
      try {
        metaResult = await applyCollabMetaPayload(body?.payload);
      } catch (error) {
        sendJson(res, 400, { ok: false, error: error.message });
        return true;
      }
      if (!metaResult.appliedAny) {
        sendJson(res, 200, {
          ok: true,
          revision: workingRevision,
          meta: metaResult.applied,
          conflicts: metaResult.conflicts,
        });
        return true;
      }
      if (saveSessions.current?.committing) {
        sendJson(res, 409, { ok: false, error: "地图正在提交保存，请稍后重试", currentRevision: workingRevision });
        return true;
      }
    }
    const revision = type === "cursor" ? workingRevision : bumpWorkingRevision();
    const event = {
      seq: ++collabSequence,
      type,
      payload: type === "meta" ? metaResult.applied : (body?.payload ?? null),
      from: client?.id ?? String(body?.clientId || "unknown"),
      name: client?.name ?? String(body?.name || "匿名编辑者").slice(0, 40),
      color: client?.color ?? collabColor(body?.name ?? "anonymous"),
      revision,
      at: Date.now(),
    };
    if (type === "cursor") {
      collabBroadcast(event, event.from);
      sendJson(res, 200, { ok: true });
      return true;
    }
    collabEvents.push(event);
    if (collabEvents.length > COLLAB_EVENT_LIMIT) {
      collabEvents.splice(0, collabEvents.length - COLLAB_EVENT_LIMIT);
    }
    if (type === "meta") {
      collabBroadcastMeta(event);
    } else {
      collabBroadcast(event, event.from);
    }
    sendJson(res, 200, {
      ok: true,
      seq: event.seq,
      revision,
      ...(type === "meta" ? { meta: metaResult.applied, conflicts: metaResult.conflicts } : {}),
    });
    return true;
  }

  if (url.pathname === "/api/backups" && req.method === "GET") {
    sendJson(res, 200, { ok: true, backups: await listBackups() });
    return true;
  }

  if (url.pathname === "/api/backups" && req.method === "POST") {
    const backup = await createBackup("manual");
    sendJson(res, 200, { ok: true, backup });
    return true;
  }

  if (url.pathname === "/api/backups/restore" && req.method === "POST") {
    if (rejectIfSaving(res)) return true;
    const body = await readRequestBody(req);
    if (!body?.name) {
      sendJson(res, 400, { ok: false, error: "missing backup name" });
      return true;
    }
    try {
      const map = await restoreBackup(body.name);
      sendJson(res, 200, { ok: true, map });
    } catch (error) {
      sendJson(res, 400, { ok: false, error: String(error?.message ?? error) });
    }
    return true;
  }

  if (url.pathname === "/api/gensokyo/regenerate/status" && req.method === "GET") {
    const worldDir = resolveWorldDir(url.searchParams.get("world"));
    const status = await regenerationMarkerStatus(worldDir);
    sendJson(res, 200, {
      ok: true,
      world: displayPath(worldDir),
      worldAbsolute: worldDir,
      marker: displayPath(status.marker),
      pending: status.pending,
      dimension: displayPath(status.dimension),
      dimensionExists: status.dimensionExists,
    });
    return true;
  }

  if (url.pathname === "/api/gensokyo/regenerate" && req.method === "POST") {
    const body = await readRequestBody(req);
    if (body?.confirm !== true) {
      sendJson(res, 400, { ok: false, error: "需要 confirm: true 才会写入重新生成标记" });
      return true;
    }
    const worldDir = resolveWorldDir(body?.world);
    try {
      await stat(path.join(worldDir, "level.dat"));
    } catch {
      sendJson(res, 400, {
        ok: false,
        error: `世界目录缺少 level.dat：${displayPath(worldDir)}`,
        world: displayPath(worldDir),
      });
      return true;
    }
    if (rejectIfSaving(res)) return true;
    const status = await regenerationMarkerStatus(worldDir);
    const backup = await createBackup("before-regenerate");
    const marker = path.join(worldDir, GENSOKYO_REGENERATE_MARKER);
    const payload = {
      requestedAt: new Date().toISOString(),
      requestedBy: "editor",
      dimension: "reverie_dreams:gensokyo",
      world: worldDir,
    };
    await writeAtomic(marker, `${JSON.stringify(payload, null, 2)}\n`);
    sendJson(res, 200, {
      ok: true,
      alreadyPending: status.pending,
      marker: displayPath(marker),
      world: displayPath(worldDir),
      dimension: displayPath(status.dimension),
      dimensionExists: status.dimensionExists,
      mapBackup: backup.name,
      note: "下次启动时旧维度会移动到世界目录下的 backups/，然后从当前 map.json 重新生成。",
    });
    return true;
  }

  if (url.pathname === "/api/gensokyo/regenerate/cancel" && req.method === "POST") {
    const body = await readRequestBody(req);
    const worldDir = resolveWorldDir(body?.world);
    const marker = path.join(worldDir, GENSOKYO_REGENERATE_MARKER);
    let removed = false;
    try {
      removed = (await stat(marker)).isFile();
    } catch {
      removed = false;
    }
    await rm(marker, { force: true });
    sendJson(res, 200, { ok: true, removed, marker: displayPath(marker), world: displayPath(worldDir) });
    return true;
  }

  if (url.pathname === "/api/map/new" && req.method === "POST") {
    if (rejectIfSaving(res)) return true;
    const body = await readRequestBody(req);
    const width = Number(body?.width);
    const depth = Number(body?.depth);
    if (!Number.isInteger(width) || !Number.isInteger(depth) || width < 1 || depth < 1
        || width > MAX_MAP_SIZE || depth > MAX_MAP_SIZE || width * depth > MAX_MAP_CELLS) {
      sendJson(res, 400, {
        ok: false,
        error: `width/depth must be integers in 1..${MAX_MAP_SIZE}, total cells <= ${MAX_MAP_CELLS}`,
      });
      return true;
    }
    try {
      const map = await createNewMap(width, depth);
      sendJson(res, 200, { ok: true, map });
    } catch (error) {
      sendJson(res, 400, { ok: false, error: String(error?.message ?? error) });
    }
    return true;
  }

  if (url.pathname === "/api/map/resize" && req.method === "POST") {
    if (rejectIfSaving(res)) return true;
    const body = await readRequestBody(req);
    const width = Number(body?.width);
    const depth = Number(body?.depth);
    if (!Number.isInteger(width) || !Number.isInteger(depth) || width < 1 || depth < 1
        || width > MAX_MAP_SIZE || depth > MAX_MAP_SIZE || width * depth > MAX_MAP_CELLS) {
      sendJson(res, 400, {
        ok: false,
        error: `width/depth must be integers in 1..${MAX_MAP_SIZE}, total cells <= ${MAX_MAP_CELLS}`,
      });
      return true;
    }
    try {
      const map = await resizeMap(width, depth);
      sendJson(res, 200, { ok: true, map });
    } catch (error) {
      sendJson(res, 400, { ok: false, error: String(error?.message ?? error) });
    }
    return true;
  }

  if (url.pathname === "/api/save/begin" && req.method === "POST") {
    const expiration = saveSessions.expire();
    if (expiration) await expiration;
    const body = await readRequestBody(req);
    const clientId = String(body?.clientId || "").slice(0, 80);
    const baseRevision = Number(body?.baseRevision);
    if (!Number.isInteger(baseRevision) || baseRevision < 0) {
      sendJson(res, 400, { ok: false, error: "baseRevision 必须是非负整数" });
      return true;
    }
    if (saveSessions.current && saveSessions.current.clientId && saveSessions.current.clientId === clientId && !saveSessions.current.committing) {
      await saveSessions.abort(saveSessions.current, "superseded");
    }
    if (rejectIfSaving(res)) {
      return true;
    }
    if (collabClients.size > 0 && !collabClients.has(clientId)) {
      sendJson(res, 409, { ok: false, error: "有其他编辑者在线，请先连接协作再保存" });
      return true;
    }
    if (baseRevision !== workingRevision) {
      sendJson(res, 409, {
        ok: false,
        stale: true,
        error: "地图已被其他编辑者修改，请重新载入后再保存",
        currentRevision: workingRevision,
      });
      return true;
    }
    const session = await saveSessions.create(clientId, baseRevision);
    sendJson(res, 200, {
      ok: true,
      token: session.token,
      revision: workingRevision,
      expiresAt: session.expiresAt,
    });
    return true;
  }

  if (url.pathname === "/api/save/commit" && req.method === "POST") {
    const body = await readRequestBody(req);
    const token = String(body?.token || "");
    const session = saveSessions.forToken(token);
    if (!session) {
      sendJson(res, 409, {
        ok: false,
        error: "保存会话已失效，请重新保存",
        currentRevision: workingRevision,
      });
      return true;
    }
    if (session.committing) {
      sendJson(res, 409, { ok: false, error: "保存正在提交，请稍后重试", currentRevision: workingRevision });
      return true;
    }
    if (workingRevision !== session.baseRevision) {
      await saveSessions.abort(session, "stale");
      sendJson(res, 409, {
        ok: false,
        stale: true,
        error: "保存期间地图被其他编辑者修改，请重新载入后再保存",
        currentRevision: workingRevision,
      });
      return true;
    }

    const errors = [];
    let stagedMap = null;
    try {
      stagedMap = JSON.parse(await readFile(path.join(session.dir, "map.json"), "utf8"));
    } catch {
      errors.push("缺少 map.json，请先上传地图元数据");
    }
    if (stagedMap) {
      errors.push(...validateMap(stagedMap));
      if (errors.length === 0) {
        errors.push(...await validateLayerFiles(stagedMap, session.dir));
        try {
          const current = await readMapFromDisk();
          if (current.width !== stagedMap.width || current.depth !== stagedMap.depth) {
            errors.push("保存不能改变地图尺寸，请使用“调整尺寸”功能");
          }
          if (current.layers && stagedMap.layers) {
            for (const name of LAYER_NAMES) {
              if (current.layers[name] !== stagedMap.layers[name]) {
                errors.push(`保存不能修改 layers.${name} 文件名`);
              }
            }
          }
        } catch (error) {
          errors.push(`无法读取当前地图：${error?.message ?? error}`);
        }
      }
      if (errors.length === 0) {
        try {
          const underground = JSON.parse(await readFile(path.join(session.dir, "underground.json"), "utf8"));
          errors.push(...validateUnderground(stagedMap, underground));
        } catch {
          errors.push("缺少 underground.json，请先上传地下覆盖数据");
        }
      }
    }
    if (errors.length > 0) {
      await saveSessions.abort(session, "invalid");
      sendJson(res, 400, { ok: false, errors });
      return true;
    }

    // 从这里开始到提交结束不再让新的地形/元数据操作进入，避免提交到一半时 revision 变化。
    session.committing = true;
    session.commitPromise = (async () => {
      const sourceDir = path.dirname(sourcePath);
      const previousCommitted = committedRevision;
      let backup = null;
      try {
        backup = await createBackup("before-save");
        for (const name of LAYER_NAMES) {
          const filename = stagedMap.layers?.[name];
          if (!filename) continue;
          // Publish via a new file, rather than truncating an existing raw layer.
          // Windows can reject copyFile-overwrite even when ordinary reads work.
          await writeAtomic(path.join(sourceDir, filename), await readFile(path.join(session.dir, filename)));
        }
        await writeAtomic(undergroundSourcePath, await readFile(path.join(session.dir, "underground.json")));
        await writeMapToDisk(sourcePath, stagedMap);
        const nextRevision = session.baseRevision + 1;
        committedRevision = nextRevision;
        await persistRevision();
        workingRevision = nextRevision;
        lastSaveKind = "session";
        invalidateCollabMeta();
        collabEvents.length = 0;
        return backup;
      } catch (error) {
        committedRevision = previousCommitted;
        if (backup) {
          for (const file of mapFileNames) {
            try {
              await writeAtomic(path.join(sourceDir, file), await readFile(path.join(backupRoot, backup.name, file)));
            } catch {
              // Best-effort rollback; the backup remains available for manual restore.
            }
          }
        }
        throw error;
      }
    })();

    try {
      await session.commitPromise;
    } catch (error) {
      await saveSessions.abort(session, "commit-failed");
      sendJson(res, 500, { ok: false, error: String(error?.message ?? error) });
      return true;
    }

    await saveSessions.abort(session, "committed");
    collabBroadcast(
      { type: "saved", revision: committedRevision, at: Date.now() },
      session.clientId || null,
    );
    sendJson(res, 200, {
      ok: true,
      revision: committedRevision,
      etag: `"rev-${committedRevision}"`,
      path: path.relative(repoRoot, sourcePath),
    });
    return true;
  }

  if (url.pathname === "/api/save/abort" && req.method === "POST") {
    const body = await readRequestBody(req);
    const session = saveSessions.forToken(String(body?.token || ""));
    if (!session) {
      sendJson(res, 200, { ok: true, alreadyGone: true });
      return true;
    }
    if (session.committing) {
      sendJson(res, 409, { ok: false, error: "保存正在提交，无法取消" });
      return true;
    }
    await saveSessions.abort(session, "client-abort");
    sendJson(res, 200, { ok: true });
    return true;
  }

  if (url.pathname === "/api/border-settings" && req.method === "GET") {
    const settingsPath = path.join(repoRoot, "common/src/main/resources/data/reverie_dreams/gensokyo/border-settings.json");
    sendJson(res, 200, JSON.parse(await readFile(settingsPath, "utf8")));
    return true;
  }

  if (url.pathname === "/api/map" && req.method === "GET") {
    const etag = `"rev-${committedRevision}"`;
    if (req.headers["if-none-match"] === etag) {
      sendNotModified(res, etag);
      return true;
    }
    const map = await readMapFromDisk();
    res.__cacheControl = "no-cache";
    res.setHeader("ETag", etag);
    res.setHeader("X-Map-Revision", String(committedRevision));
    sendJson(res, 200, { ...map, revision: committedRevision, liveRevision: workingRevision });
    return true;
  }

  if (url.pathname === "/api/map" && req.method === "PUT") {
    const map = await readRequestBody(req);
    const token = saveTokenFrom(req);
    if (token) {
      const session = saveSessions.forToken(token);
      if (!session) {
        sendJson(res, 409, { ok: false, error: "保存会话已失效，请重新保存", currentRevision: workingRevision });
        return true;
      }
      const errors = validateMap(map);
      if (errors.length > 0) {
        sendJson(res, 400, { ok: false, errors });
        return true;
      }
      await writeMapToDisk(path.join(session.dir, "map.json"), map);
      session.files.add("map.json");
      sendJson(res, 200, { ok: true, staged: true, revision: session.baseRevision });
      return true;
    }
    if (rejectIfSaving(res)) return true;
    if (collabClients.size > 0) {
      sendJson(res, 409, { ok: false, error: "协作模式下必须通过保存会话保存地图" });
      return true;
    }
    const result = await saveMap(map, sourcePath);
    if (!result.ok) {
      sendJson(res, 400, result);
      return true;
    }
    const revision = bumpWorkingRevision();
    committedRevision = revision;
    lastSaveKind = "direct";
    invalidateCollabMeta();
    await persistRevision();
    collabEvents.length = 0;
    collabBroadcast({ type: "saved", revision, at: Date.now() });
    sendJson(res, 200, {
      ok: true,
      path: path.relative(repoRoot, sourcePath),
      revision,
      etag: `"rev-${revision}"`,
    });
    return true;
  }

  const layerChunkRoute = url.pathname.match(/^\/api\/layers\/([^/]+)\/(overview|tile)$/);
  if (layerChunkRoute && req.method === "GET") {
    const layerName = layerChunkRoute[1];
    const chunkType = layerChunkRoute[2];
    if (!LAYER_NAMES.includes(layerName)) {
      sendText(res, 404, "Unknown layer");
      return true;
    }
    const map = await readMapFromDisk();
    const filename = layerFilename(map, layerName);
    if (!filename) {
      sendText(res, 404, "Layer is not configured");
      return true;
    }
    const file = path.join(path.dirname(sourcePath), filename);
    const bytesPerCell = layerName === "biomes" ? 1 : 2;
    const width = map.width;
    const depth = map.depth;

    if (chunkType === "overview") {
      const factor = Math.max(2, Math.min(64, Number(url.searchParams.get("factor")) || 16));
      const overviewWidth = Math.ceil(width / factor);
      const overviewHeight = Math.ceil(depth / factor);
      const cacheFile = `${file}.overview${factor}.bin`;
      let overview = null;
      try {
        const sourceInfo = await stat(file);
        const cacheInfo = await stat(cacheFile);
        if (cacheInfo.mtimeMs >= sourceInfo.mtimeMs) {
          overview = await readFile(cacheFile);
        }
      } catch {
        // No usable overview cache yet.
      }
      if (!overview) {
        const data = await readFile(file);
        overview = Buffer.allocUnsafe(overviewWidth * overviewHeight * bytesPerCell);
        for (let oy = 0; oy < overviewHeight; oy++) {
          const y = Math.min(depth - 1, oy * factor);
          for (let ox = 0; ox < overviewWidth; ox++) {
            const x = Math.min(width - 1, ox * factor);
            const source = (y * width + x) * bytesPerCell;
            const target = (oy * overviewWidth + ox) * bytesPerCell;
            if (bytesPerCell === 1) {
              overview[target] = data[source];
            } else {
              overview[target] = data[source];
              overview[target + 1] = data[source + 1];
            }
          }
        }
        writeFile(cacheFile, overview).catch(() => {});
      }
      res.writeHead(200, {
        "Content-Type": "application/octet-stream",
        "Content-Length": overview.length,
        "Cache-Control": "no-cache",
      });
      res.end(overview);
      return true;
    }

    const x = Math.max(0, Math.min(width - 1, Number(url.searchParams.get("x")) || 0));
    const z = Math.max(0, Math.min(depth - 1, Number(url.searchParams.get("z")) || 0));
    const size = Math.max(1, Math.min(1024, Number(url.searchParams.get("size")) || 512));
    const tileWidth = Math.min(size, width - x);
    const tileHeight = Math.min(size, depth - z);
    const rowBytes = tileWidth * bytesPerCell;
    const tile = Buffer.allocUnsafe(tileWidth * tileHeight * bytesPerCell);
    const handle = await open(file, "r");
    try {
      for (let row = 0; row < tileHeight; row++) {
        await handle.read(
          tile,
          row * rowBytes,
          rowBytes,
          ((z + row) * width + x) * bytesPerCell,
        );
      }
    } finally {
      await handle.close();
    }
    res.writeHead(200, {
      "Content-Type": "application/octet-stream",
      "Content-Length": tile.length,
      "Cache-Control": "no-cache",
    });
    res.end(tile);
    return true;
  }

  if (url.pathname.startsWith("/api/layers/")) {
    const name = url.pathname.slice("/api/layers/".length);
    if (!LAYER_NAMES.includes(name)) {
      sendText(res, 404, "Unknown layer");
      return true;
    }
    const token = saveTokenFrom(req);
    const session = token ? saveSessions.forToken(token) : null;
    if (req.method === "PUT" && token && !session) {
      sendJson(res, 409, { ok: false, error: "保存会话已失效，请重新保存", currentRevision: workingRevision });
      return true;
    }
    if (req.method === "PUT" && !token && collabClients.size > 0) {
      sendJson(res, 409, { ok: false, error: "协作模式下必须通过保存会话写入图层" });
      return true;
    }
    let map;
    let file;
    if (session && req.method === "PUT") {
      try {
        map = JSON.parse(await readFile(path.join(session.dir, "map.json"), "utf8"));
      } catch {
        sendJson(res, 400, { ok: false, error: "请先上传 map.json" });
        return true;
      }
      const filename = layerFilename(map, name);
      if (!filename) {
        sendText(res, 404, "Layer is not configured");
        return true;
      }
      file = path.join(session.dir, filename);
    } else {
      map = await readMapFromDisk();
      const filename = layerFilename(map, name);
      if (!filename) {
        sendText(res, 404, "Layer is not configured");
        return true;
      }
      file = path.join(path.dirname(sourcePath), filename);
    }
    if (req.method === "GET" || req.method === "HEAD") {
      const etag = await fileEtag([file]);
      if (req.headers["if-none-match"] === etag) {
        sendNotModified(res, etag);
        return true;
      }
      const raw = url.searchParams.get("raw") === "1";
      if (req.method === "HEAD") {
        const data = await readFile(file);
        res.writeHead(200, {
          "Content-Type": "application/octet-stream",
          "Content-Length": data.length,
          "Cache-Control": "no-cache",
          ETag: etag,
          Vary: "Accept-Encoding",
        });
        res.end();
      } else if (raw) {
        const data = await readFile(file);
        res.writeHead(200, {
          "Content-Type": "application/octet-stream",
          "Content-Length": data.length,
          "Cache-Control": "no-cache",
          ETag: etag,
        });
        res.end(data);
      } else {
        const gzipFile = `${file}.gz`;
        let compressed = null;
        try {
          const sourceInfo = await stat(file);
          const cacheInfo = await stat(gzipFile);
          if (cacheInfo.mtimeMs >= sourceInfo.mtimeMs) {
            compressed = await readFile(gzipFile);
          }
        } catch {
          // No usable gzip cache yet.
        }
        if (!compressed) {
          const data = await readFile(file);
          compressed = gzipSync(data, { level: 1 });
          writeFile(gzipFile, compressed).catch(() => {});
        }
        res.writeHead(200, {
          "Content-Type": "application/octet-stream",
          "Content-Encoding": "gzip",
          "Content-Length": compressed.length,
          "Cache-Control": "no-cache",
          ETag: etag,
          Vary: "Accept-Encoding",
        });
        res.end(compressed);
      }
      return true;
    }
    if (req.method === "PUT") {
      let data = await readRequestBodyBuffer(req);
      if (req.headers["content-encoding"] === "gzip") {
        data = gunzipSync(data);
      }
      const expected = layerExpectedBytes(map, name);
      if (data.length !== expected) {
        sendJson(res, 400, {
          ok: false,
          errors: [`${name} 文件大小应为 ${expected}，实际为 ${data.length}`],
        });
        return true;
      }
      await writeAtomic(file, data);
      if (session) {
        session.files.add(path.basename(file));
        sendJson(res, 200, {
          ok: true,
          staged: true,
          path: path.relative(repoRoot, file),
          bytes: data.length,
        });
      } else {
        const revision = bumpWorkingRevision();
        committedRevision = revision;
        lastSaveKind = "direct";
        await persistRevision();
        collabBroadcast({ type: "saved", revision, at: Date.now() });
        sendJson(res, 200, {
          ok: true,
          path: path.relative(repoRoot, file),
          bytes: data.length,
          revision,
        });
      }
      return true;
    }
    sendText(res, 405, "Method Not Allowed");
    return true;
  }

  if (url.pathname === "/api/underground" && req.method === "GET") {
    const etag = await fileEtag([undergroundSourcePath]);
    if (req.headers["if-none-match"] === etag) {
      sendNotModified(res, etag);
      return true;
    }
    const data = await readUndergroundFromDisk();
    res.__cacheControl = "no-cache";
    res.setHeader("ETag", etag);
    sendJson(res, 200, data);
    return true;
  }

  if (url.pathname === "/api/underground" && req.method === "PUT") {
    const token = saveTokenFrom(req);
    const session = token ? saveSessions.forToken(token) : null;
    if (token && !session) {
      sendJson(res, 409, { ok: false, error: "保存会话已失效，请重新保存", currentRevision: workingRevision });
      return true;
    }
    if (!token && collabClients.size > 0) {
      sendJson(res, 409, { ok: false, error: "协作模式下必须通过保存会话写入地下数据" });
      return true;
    }
    let map;
    if (session) {
      try {
        map = JSON.parse(await readFile(path.join(session.dir, "map.json"), "utf8"));
      } catch {
        sendJson(res, 400, { ok: false, error: "请先上传 map.json" });
        return true;
      }
    } else {
      map = await readMapFromDisk();
    }
    const data = await readRequestBody(req);
    const errors = validateUnderground(map, data);
    if (errors.length > 0) {
      sendJson(res, 400, { ok: false, errors });
      return true;
    }
    const target = session ? path.join(session.dir, "underground.json") : undergroundSourcePath;
    await writeAtomic(target, `${JSON.stringify(data)}\n`);
    if (session) {
      session.files.add("underground.json");
      sendJson(res, 200, {
        ok: true,
        staged: true,
        path: path.relative(repoRoot, target),
        blocks: data.blocks.length,
      });
    } else {
      const revision = bumpWorkingRevision();
      committedRevision = revision;
      lastSaveKind = "direct";
      await persistRevision();
      collabBroadcast({ type: "saved", revision, at: Date.now() });
      sendJson(res, 200, {
        ok: true,
        path: path.relative(repoRoot, undergroundSourcePath),
        blocks: data.blocks.length,
        revision,
      });
    }
    return true;
  }

  if (url.pathname === "/api/validate" && req.method === "POST") {
    const map = await readRequestBody(req);
    const errors = validateMap(map);
    sendJson(res, errors.length > 0 ? 400 : 200, { ok: errors.length === 0, errors });
    return true;
  }

  if (url.pathname === "/api/templates" && req.method === "GET") {
    const entries = await listTemplateEntries();
    const templates = entries.map((entry) => entry.id);
    const sizes = await templateSizes(entries);
    sendJson(res, 200, { templates, sizes });
    return true;
  }

  if (url.pathname === "/api/template" && req.method === "GET") {
    const id = url.searchParams.get("id");
    if (!id) {
      sendJson(res, 400, { ok: false, error: "missing id" });
      return true;
    }
    const entries = await listTemplateEntries();
    const entry = entries.find((candidate) => candidate.id === id);
    if (!entry) {
      sendJson(res, 404, { ok: false, error: "template not found" });
      return true;
    }
    const structure = readStructure(await readFile(entry.file));
    if (!structure) {
      sendJson(res, 422, { ok: false, error: "invalid structure" });
      return true;
    }
    sendJson(res, 200, structure);
    return true;
  }

  if (url.pathname === "/api/blocks" && req.method === "GET") {
    const etag = await fileEtag([blockPalettePath, blockStatesPath]);
    if (req.headers["if-none-match"] === etag) {
      sendNotModified(res, etag);
      return true;
    }
    res.__cacheControl = "no-cache";
    res.setHeader("ETag", etag);
    const palette = JSON.parse(await readFile(blockPalettePath, "utf8"));
    try {
      const states = JSON.parse(await readFile(blockStatesPath, "utf8"));
      const byId = states && typeof states.blocks === "object" && states.blocks !== null
        ? states.blocks
        : {};
      if (Array.isArray(palette.blocks)) {
        for (const block of palette.blocks) {
          const state = block && byId[block.id];
          if (state && typeof state === "object") {
            if (state.properties && typeof state.properties === "object") {
              block.properties = state.properties;
            }
            if (state.defaults && typeof state.defaults === "object") {
              block.defaults = state.defaults;
            }
          }
        }
      }
    } catch {
      // block_states.json is optional; the editor falls back to blockstate JSON.
    }
    sendJson(res, 200, palette);
    return true;
  }

  if (url.pathname === "/api/biomes" && req.method === "GET") {
    const etag = await fileEtag([biomePalettePath]);
    if (req.headers["if-none-match"] === etag) {
      sendNotModified(res, etag);
      return true;
    }
    try {
      const palette = JSON.parse(await readFile(biomePalettePath, "utf8"));
      try {
        const translations = JSON.parse(await readFile(biomeTranslationPath, "utf8"));
        for (const biome of palette.biomes ?? []) {
          const key = `biome.${biome.id.replace(":", ".")}`;
          if (typeof translations[key] === "string") biome.name = translations[key];
        }
      } catch {
        // Translation data is optional; the editor still exposes IDs.
      }
      res.__cacheControl = "no-cache";
      res.setHeader("ETag", etag);
      sendJson(res, 200, palette);
    } catch (error) {
      sendJson(res, 404, { ok: false, error: `biome palette unavailable: ${error}` });
    }
    return true;
  }

  if (url.pathname === "/api/sync" && req.method === "POST") {
    if (rejectIfSaving(res)) return true;
    const map = await readMapFromDisk();
    const errors = validateMap(map);
    if (errors.length === 0) {
      errors.push(...await validateLayerFiles(map, path.dirname(sourcePath)));
    }
    if (errors.length > 0) {
      sendJson(res, 400, { ok: false, errors });
      return true;
    }
    await writeCompressedRuntime(map, path.dirname(sourcePath), path.dirname(runtimePath));
    await copyUnderground(undergroundSourcePath, undergroundRuntimePath);
    sendJson(res, 200, {
      ok: true,
      path: path.relative(repoRoot, runtimePath),
      layers: Object.keys(runtimeLayerFilesFor(map)),
    });
    return true;
  }

  if (url.pathname === "/api/import-runtime" && req.method === "POST") {
    if (rejectIfSaving(res)) return true;
    const runtimeMap = JSON.parse(await readFile(runtimePath, "utf8"));
    const runtimeDir = path.dirname(runtimePath);
    const errors = validateMap(runtimeMap);
    if (errors.length > 0) {
      sendJson(res, 400, { ok: false, errors });
      return true;
    }
    let layers;
    try {
      layers = await readCompressedRuntimeLayers(runtimeDir, runtimeMap);
    } catch (error) {
      sendJson(res, 400, { ok: false, errors: [String(error?.message || error)] });
      return true;
    }
    const sourceMap = structuredClone(runtimeMap);
    sourceMap.layers = {
      heightmap: "heightmap.bin",
      biomes: "biomes.bin",
      surfaces: "surfaces.bin",
    };
    await writeMapToDisk(sourcePath, sourceMap);
    const sourceDir = path.dirname(sourcePath);
    await writeFile(path.join(sourceDir, "heightmap.bin"), layers.heightRaw);
    await writeFile(path.join(sourceDir, "biomes.bin"), layers.biomeRaw);
    await writeFile(path.join(sourceDir, "surfaces.bin"), layers.surfaceRaw);
    await copyUnderground(undergroundRuntimePath, undergroundSourcePath);
    const importErrors = validateMap(sourceMap);
    if (importErrors.length === 0) {
      importErrors.push(...await validateLayerFiles(sourceMap, sourceDir));
    }
    if (importErrors.length > 0) {
      sendJson(res, 400, { ok: false, errors: importErrors });
      return true;
    }
    await collabReset("import-runtime");
    sendJson(res, 200, {
      ok: true,
      path: path.relative(repoRoot, sourcePath),
      from: path.relative(repoRoot, runtimePath),
      layers: Object.keys(sourceMap.layers),
    });
    return true;
  }

  return false;
}

async function serveStatic(req, res) {
  const url = new URL(req.url, `http://${req.headers.host || "localhost"}`);
  if (req.method !== "GET" && req.method !== "HEAD") {
    sendText(res, 405, "Method Not Allowed");
    return;
  }

  const requestPath = url.pathname === "/" ? "/index.html" : url.pathname;
  const decoded = decodeURIComponent(requestPath);
  const resolved = path.resolve(publicDir, `.${decoded}`);
  if (resolved !== publicDir && !resolved.startsWith(`${publicDir}${path.sep}`)) {
    sendText(res, 403, "Forbidden");
    return;
  }

  try {
    const info = await stat(resolved);
    const etag = `"${info.size.toString(16)}-${Math.floor(info.mtimeMs).toString(16)}"`;
    if (req.headers["if-none-match"] === etag) {
      sendNotModified(res, etag);
      return;
    }
    const data = await readFile(resolved);
    const type = mimeTypes[path.extname(resolved)] || "application/octet-stream";
    const compressible = type.startsWith("text/")
      || type.includes("javascript")
      || type.includes("json")
      || type === "image/svg+xml";
    const useGzip = compressible
      && data.length >= 1024
      && req.method !== "HEAD"
      && String(req.headers["accept-encoding"] || "").toLowerCase().includes("gzip");
    const payload = useGzip ? gzipSync(data, { level: 6 }) : data;
    const headers = {
      "Content-Type": type,
      "Content-Length": payload.length,
      "Cache-Control": "no-cache",
      ETag: etag,
    };
    if (compressible) {
      headers.Vary = "Accept-Encoding";
    }
    if (useGzip) {
      headers["Content-Encoding"] = "gzip";
    }
    res.writeHead(200, headers);
    if (req.method === "HEAD") {
      res.end();
    } else {
      res.end(payload);
    }
  } catch {
    sendText(res, 404, "Not Found");
  }
}

const server = http.createServer(async (req, res) => {
  try {
    res.__acceptGzip = String(req.headers["accept-encoding"] || "").toLowerCase().includes("gzip");
    if (req.url?.startsWith("/api/")) {
      const handled = await handleApi(req, res);
      if (!handled) {
        sendText(res, 404, "Not Found");
      }
      return;
    }
    await serveStatic(req, res);
  } catch (error) {
    console.error(error);
    if (!res.headersSent) {
      sendText(res, 500, error instanceof Error ? error.message : String(error));
    } else {
      res.end();
    }
  }
});

await loadRevision();
await rm(stagingRoot, { recursive: true, force: true });

server.listen(port, host, () => {
  console.log(`Gensokyo map editor: http://${host}:${port}`);
  console.log(`source:  ${path.relative(repoRoot, sourcePath)}`);
  console.log(`runtime: ${path.relative(repoRoot, runtimePath)}`);
  console.log(`revision: ${workingRevision}`);
});

server.on("error", (error) => {
  if (error && error.code === "EADDRINUSE") {
    console.error(`Port ${port} is already in use. Set PORT to another port, e.g. PORT=3211 node server.mjs`);
    process.exit(1);
  }
  throw error;
});
