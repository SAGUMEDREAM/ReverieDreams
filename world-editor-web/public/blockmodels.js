import * as THREE from "./vendor/three.module.js";

const ASSET_ROOT = "/mc_assets";
const jsonCache = new Map();
const textureCache = new Map();

function splitId(id) {
  const text = String(id ?? "minecraft:stone");
  const index = text.indexOf(":");
  return index >= 0
    ? { namespace: text.slice(0, index), path: text.slice(index + 1) }
    : { namespace: "minecraft", path: text };
}

async function fetchJson(url) {
  if (jsonCache.has(url)) {
    return jsonCache.get(url);
  }
  const promise = fetch(url, { cache: "no-store" }).then((response) => {
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${url}`);
    }
    return response.json();
  });
  jsonCache.set(url, promise);
  try {
    return await promise;
  } catch (error) {
    jsonCache.delete(url);
    throw error;
  }
}

function modelUrl(path) {
  const id = splitId(path);
  const dir = id.path.startsWith("item/") ? "models" : "models";
  return `${ASSET_ROOT}/${id.namespace}/${dir}/${id.path}.json`;
}

function tintForBlock(blockId) {
  const key = String(blockId ?? "").split("[")[0].split(":").pop();
  if (key === "grass_block" || key === "grass" || key === "short_grass" || key === "tall_grass"
      || key === "fern" || key === "large_fern" || key === "vine" || key === "lily_pad"
      || key.endsWith("_leaves") || key.endsWith("_vine")) {
    return 0x6cab4d;
  }
  if (key === "water" || key === "bubble_column") {
    return 0x3f76e4;
  }
  return null;
}

function textureUrl(path) {
  const id = splitId(path);
  const clean = id.path.replace(/^textures\//, "");
  return `${ASSET_ROOT}/${id.namespace}/textures/${clean}.png`;
}

function loadTexture(path) {
  const url = textureUrl(path);
  if (textureCache.has(url)) {
    return textureCache.get(url);
  }
  const promise = new Promise((resolve, reject) => {
    new THREE.TextureLoader().load(
      url,
      (texture) => {
        texture.flipY = false;
        texture.magFilter = THREE.NearestFilter;
        texture.minFilter = THREE.NearestFilter;
        texture.generateMipmaps = false;
        texture.colorSpace = THREE.SRGBColorSpace;
        resolve(texture);
      },
      undefined,
      () => reject(new Error(`texture: ${url}`)),
    );
  });
  textureCache.set(url, promise);
  return promise;
}

function normalizeVariantRefs(blockstate, properties = null) {
  const refs = [];
  if (blockstate.variants && typeof blockstate.variants === "object") {
    const keys = Object.keys(blockstate.variants);
    let selectedKey;
    if (!properties || Object.keys(properties).length === 0) {
      const preferred = keys.find((key) =>
        key.includes("facing=north") && key.includes("half=bottom") && key.includes("shape=straight"));
      selectedKey = keys.includes("") ? "" : (preferred ?? keys.find((key) => key.includes("shape=straight")) ?? keys[0]);
    } else {
      let bestScore = -1;
      for (const key of keys) {
        const parsed = parseVariantKey(key);
        let score = 0;
        let matches = true;
        for (const [name, value] of Object.entries(parsed)) {
          if (properties[name] === undefined || String(properties[name]) !== value) {
            matches = false;
            break;
          }
          score++;
        }
        if (matches && score > bestScore) {
          bestScore = score;
          selectedKey = key;
        }
      }
      selectedKey ??= keys.includes("") ? "" : keys[0];
    }
    const value = blockstate.variants[selectedKey];
    const variants = Array.isArray(value) ? value : [value];
    const variant = variants[0];
    if (variant && typeof variant.model === "string") {
      refs.push(variant);
    }
  }
  if (blockstate.multipart && Array.isArray(blockstate.multipart)) {
    // With an unknown block state, combine all parts to get a useful full
    // preview. For structure blocks, the NBT properties let us pick exactly
    // the parts that Minecraft would render.
    const selectedParts = properties && Object.keys(properties).length > 0
      ? blockstate.multipart.filter((part) => !part || matchesWhen(part.when, properties))
      : blockstate.multipart;
    for (const part of selectedParts.length > 0 ? selectedParts : [blockstate.multipart[0]]) {
      if (!part) continue;
      for (const apply of Array.isArray(part.apply) ? part.apply : [part.apply]) {
        if (apply && typeof apply.model === "string") {
          refs.push(apply);
        }
      }
    }
  }
  return refs;
}

function parseVariantKey(key) {
  const result = {};
  if (!key) {
    return result;
  }
  for (const part of String(key).split(",")) {
    const [name, value] = part.split("=");
    if (name && value !== undefined) {
      result[name] = value;
    }
  }
  return result;
}

function collectWhen(when, add) {
  if (!when) {
    return;
  }
  if (typeof when === "string") {
    for (const alternative of when.split("|")) {
      for (const condition of alternative.split("&")) {
        const [name, value] = condition.split("=");
        if (name && value !== undefined) {
          add(name, value);
        }
      }
    }
    return;
  }
  if (Array.isArray(when)) {
    for (const entry of when) {
      collectWhen(entry, add);
    }
    return;
  }
  if (typeof when === "object") {
    for (const [name, value] of Object.entries(when)) {
      if (name === "OR" || name === "AND") {
        collectWhen(value, add);
      } else if (typeof value === "string" || typeof value === "number" || typeof value === "boolean") {
        add(name, value);
      } else if (Array.isArray(value)) {
        for (const entry of value) {
          if (typeof entry === "string" || typeof entry === "number" || typeof entry === "boolean") {
            add(name, entry);
          }
        }
      } else {
        collectWhen(value, add);
      }
    }
  }
}

/**
 * Collects the property names and possible values from a Mojang blockstate JSON.
 * Works for both variants and multipart blockstates.
 */
export function describeBlockstate(blockstate) {
  const values = new Map();
  const add = (name, value) => {
    const key = String(name);
    if (!values.has(key)) {
      values.set(key, new Set());
    }
    values.get(key).add(String(value));
  };
  if (blockstate?.variants && typeof blockstate.variants === "object") {
    for (const key of Object.keys(blockstate.variants)) {
      const parsed = parseVariantKey(key);
      for (const [name, value] of Object.entries(parsed)) {
        add(name, value);
      }
    }
  }
  if (Array.isArray(blockstate?.multipart)) {
    for (const part of blockstate.multipart) {
      collectWhen(part?.when, add);
    }
  }
  const result = {};
  for (const [name, set] of values) {
    result[name] = [...set];
  }
  return result;
}

export async function fetchBlockstate(blockId) {
  const id = splitId(String(blockId ?? "").split("[")[0]);
  try {
    return await fetchJson(`${ASSET_ROOT}/${id.namespace}/blockstates/${id.path}.json`);
  } catch {
    return null;
  }
}

function matchesWhen(when, properties) {
  if (!when) {
    return true;
  }
  if (typeof when === "string") {
    return when.split("|").some((alternative) =>
      alternative.split("&").every((condition) => {
        const [name, value] = condition.split("=");
        if (!name || value === undefined) {
          return true;
        }
        const actual = properties[name];
        return actual !== undefined && String(actual) === value;
      }));
  }
  if (Array.isArray(when)) {
    return when.every((entry) => matchesWhen(entry, properties));
  }
  if (typeof when === "object") {
    if (Array.isArray(when.OR) && !when.OR.some((entry) => matchesWhen(entry, properties))) {
      return false;
    }
    if (Array.isArray(when.AND) && !when.AND.every((entry) => matchesWhen(entry, properties))) {
      return false;
    }
    for (const [name, value] of Object.entries(when)) {
      if (name === "OR" || name === "AND") {
        continue;
      }
      if (typeof value === "object") {
        if (!matchesWhen(value, properties)) {
          return false;
        }
      } else if (properties[name] === undefined || String(properties[name]) !== String(value)) {
        return false;
      }
    }
  }
  return true;
}

function resolveTexture(textures, value, seen = new Set()) {
  if (typeof value !== "string" || seen.has(value)) {
    return null;
  }
  if (value.startsWith("#")) {
    const key = value.slice(1);
    const next = textures[key];
    if (!next) return null;
    seen.add(value);
    return resolveTexture(textures, next, seen);
  }
  return value;
}

function faceCorners(from, to, name) {
  const [x0, y0, z0] = from.map((value) => value / 16);
  const [x1, y1, z1] = to.map((value) => value / 16);
  switch (name) {
    case "north":
      return [[x0, y0, z0], [x0, y1, z0], [x1, y1, z0], [x1, y0, z0], [0, 0, -1]];
    case "south":
      return [[x1, y0, z1], [x1, y1, z1], [x0, y1, z1], [x0, y0, z1], [0, 0, 1]];
    case "west":
      return [[x0, y0, z1], [x0, y1, z1], [x0, y1, z0], [x0, y0, z0], [-1, 0, 0]];
    case "east":
      return [[x1, y0, z0], [x1, y1, z0], [x1, y1, z1], [x1, y0, z1], [1, 0, 0]];
    case "up":
      return [[x0, y1, z0], [x0, y1, z1], [x1, y1, z1], [x1, y1, z0], [0, 1, 0]];
    case "down":
      return [[x0, y0, z0], [x1, y0, z0], [x1, y0, z1], [x0, y0, z1], [0, -1, 0]];
    default:
      return null;
  }
}

export class BlockModelLoader {
  async resolveModel(path, seen = new Set()) {
    if (!path || seen.has(path)) {
      return { textures: {}, elements: null };
    }
    seen.add(path);
    const model = await fetchJson(modelUrl(path));
    let parent = { textures: {}, elements: null };
    if (model.parent) {
      try {
        parent = await this.resolveModel(model.parent, seen);
      } catch {
        // Keep the parentless fallback.
      }
    }
    return {
      textures: { ...parent.textures, ...(model.textures ?? {}) },
      elements: Array.isArray(model.elements) ? model.elements : parent.elements,
    };
  }

  async buildGroup(blockId, fallbackColor = 0x8b8b8b, tintColor = null, properties = null) {
    const blockstate = await fetchBlockstate(blockId);
    const refs = blockstate ? normalizeVariantRefs(blockstate, properties) : [];
    const group = new THREE.Group();
    if (refs.length === 0) {
      group.add(this.fallbackCube(fallbackColor));
      return group;
    }
    const effectiveTint = tintColor ?? tintForBlock(blockId);
    for (const ref of refs) {
      try {
        const model = await this.resolveModel(ref.model);
        const geometryGroups = await this.buildModelGeometry(model, fallbackColor, effectiveTint);
        const variant = new THREE.Group();
        // Minecraft model x/y rotations pivot around the block center, not
        // the world origin. Without the inner offset stairs/slabs get shifted
        // outside their block.
        variant.position.set(0.5, 0.5, 0.5);
        const inner = new THREE.Group();
        inner.position.set(-0.5, -0.5, -0.5);
        variant.rotation.x = -THREE.MathUtils.degToRad(Number(ref.x ?? 0));
        variant.rotation.y = -THREE.MathUtils.degToRad(Number(ref.y ?? 0));
        for (const entry of geometryGroups) {
          const mesh = new THREE.Mesh(entry.geometry, entry.material);
          mesh.castShadow = true;
          mesh.receiveShadow = true;
          inner.add(mesh);
        }
        variant.add(inner);
        group.add(variant);
      } catch {
        group.add(this.fallbackCube(fallbackColor));
      }
    }
    return group;
  }

  fallbackCube(color) {
    const geometry = new THREE.BoxGeometry(1, 1, 1);
    const material = new THREE.MeshStandardMaterial({
      color,
      roughness: 0.88,
      metalness: 0,
    });
    const mesh = new THREE.Mesh(geometry, material);
    mesh.position.set(0.5, 0.5, 0.5);
    mesh.castShadow = true;
    mesh.receiveShadow = true;
    const group = new THREE.Group();
    group.add(mesh);
    return group;
  }

  async buildModelGeometry(model, fallbackColor, tintColor = null) {
    const entries = new Map();
    const addFace = (texturePath, positions, normals, uvs, tint) => {
      const key = `${texturePath ?? "__color__"}|${tint == null ? "none" : tint.toString(16)}`;
      if (!entries.has(key)) {
        entries.set(key, { positions: [], normals: [], uvs: [], indices: [], tint, texturePath });
      }
      const entry = entries.get(key);
      const start = entry.positions.length / 3;
      for (let i = 0; i < 4; i++) {
        entry.positions.push(...positions[i]);
        entry.normals.push(...normals[i]);
        entry.uvs.push(...uvs[i]);
      }
      entry.indices.push(start, start + 1, start + 2, start, start + 2, start + 3);
    };

    const elements = Array.isArray(model.elements) && model.elements.length > 0
      ? model.elements
      : [{
        from: [0, 0, 0],
        to: [16, 16, 16],
        faces: {
          north: { texture: "#particle" },
          south: { texture: "#particle" },
          west: { texture: "#particle" },
          east: { texture: "#particle" },
          up: { texture: "#particle" },
          down: { texture: "#particle" },
        },
      }];

    for (const element of elements) {
      if (!Array.isArray(element.from) || !Array.isArray(element.to)) {
        continue;
      }
      const rotation = element.rotation;
      const rotationMatrix = new THREE.Matrix4();
      let hasRotation = false;
      if (rotation && Number.isFinite(rotation.angle)) {
        const origin = (rotation.origin ?? [8, 8, 8]).map((value) => value / 16);
        const axis = new THREE.Vector3(...(rotation.axis === "x" ? [1, 0, 0] : rotation.axis === "z" ? [0, 0, 1] : [0, 1, 0]));
        rotationMatrix.makeTranslation(origin[0], origin[1], origin[2]);
        rotationMatrix.multiply(new THREE.Matrix4().makeRotationAxis(axis, THREE.MathUtils.degToRad(rotation.angle)));
        rotationMatrix.multiply(new THREE.Matrix4().makeTranslation(-origin[0], -origin[1], -origin[2]));
        hasRotation = true;
      }

      for (const [faceName, face] of Object.entries(element.faces ?? {})) {
        const corners = faceCorners(element.from, element.to, faceName);
        if (!corners) continue;
        const positions = corners.slice(0, 4).map((point) => [...point]);
        const normal = corners[4];
        const normals = positions.map(() => [...normal]);
        if (hasRotation) {
          const vector = new THREE.Vector3();
          for (const point of positions) {
            vector.set(...point).applyMatrix4(rotationMatrix);
            point[0] = vector.x;
            point[1] = vector.y;
            point[2] = vector.z;
          }
          const n = new THREE.Vector3(...normal).transformDirection(rotationMatrix);
          for (const entry of normals) {
            entry[0] = n.x;
            entry[1] = n.y;
            entry[2] = n.z;
          }
        }
        let uv = face.uv ?? [0, 0, 16, 16];
        let [u1, v1, u2, v2] = uv;
        if (u1 > u2) [u1, u2] = [u2, u1];
        if (v1 > v2) [v1, v2] = [v2, v1];
        let faceUvs = [
          [u1 / 16, v1 / 16],
          [u2 / 16, v1 / 16],
          [u2 / 16, v2 / 16],
          [u1 / 16, v2 / 16],
        ];
        const rotationDegrees = Number(face.rotation ?? 0);
        if (rotationDegrees) {
          const turns = ((rotationDegrees % 360) + 360) % 360 / 90;
          for (let i = 0; i < turns; i++) {
            faceUvs = [faceUvs[3], faceUvs[0], faceUvs[1], faceUvs[2]];
          }
        }
        const texturePath = resolveTexture(model.textures ?? {}, face.texture);
        const usableTexture = texturePath && !texturePath.includes("missingno") ? texturePath : null;
        const faceTint = Number(face.tintindex) >= 0 ? tintColor : null;
        addFace(usableTexture, positions, normals, faceUvs, faceTint);
      }
    }

    const result = [];
    for (const entry of entries.values()) {
      const texturePath = entry.texturePath;
      const geometry = new THREE.BufferGeometry();
      geometry.setAttribute("position", new THREE.Float32BufferAttribute(entry.positions, 3));
      geometry.setAttribute("normal", new THREE.Float32BufferAttribute(entry.normals, 3));
      geometry.setAttribute("uv", new THREE.Float32BufferAttribute(entry.uvs, 2));
      geometry.setIndex(entry.indices);
      let material;
      if (texturePath) {
        try {
          const texture = await loadTexture(texturePath);
          const isOverlay = texturePath.includes("overlay");
          material = new THREE.MeshStandardMaterial({
            map: texture,
            ...(entry.tint != null ? { color: entry.tint } : {}),
            alphaTest: 0.5,
            transparent: true,
            side: THREE.DoubleSide,
            roughness: 0.92,
            metalness: 0,
            ...(isOverlay
              ? { polygonOffset: true, polygonOffsetFactor: -1, polygonOffsetUnits: -1, depthWrite: false }
              : {}),
          });
        } catch {
          material = new THREE.MeshStandardMaterial({
            color: entry.tint ?? fallbackColor,
            side: THREE.DoubleSide,
            roughness: 0.92,
          });
        }
      } else {
        material = new THREE.MeshStandardMaterial({
          color: entry.tint ?? fallbackColor,
          side: THREE.DoubleSide,
          roughness: 0.92,
        });
      }
      result.push({ geometry, material });
    }
    return result;
  }
}

export class BlockPreview {
  constructor(canvas) {
    this.canvas = canvas;
    this.renderer = new THREE.WebGLRenderer({
      canvas,
      alpha: true,
      antialias: true,
      preserveDrawingBuffer: true,
    });
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    this.renderer.outputColorSpace = THREE.SRGBColorSpace;
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
    this.renderer.toneMappingExposure = 1.1;
    this.scene = new THREE.Scene();
    this.camera = new THREE.PerspectiveCamera(32, 1, 0.01, 100);
    this.scene.add(new THREE.HemisphereLight(0xffffff, 0x334455, 1.9));
    const light = new THREE.DirectionalLight(0xffffff, 2.4);
    light.position.set(2, 4, 3);
    this.scene.add(light);
    const fill = new THREE.DirectionalLight(0x99bbff, 0.8);
    fill.position.set(-3, 2, -2);
    this.scene.add(fill);
    this.root = new THREE.Group();
    this.scene.add(this.root);
    this.loader = new BlockModelLoader();
    this.blockId = null;
    this.angle = 0;
    this.running = false;
    this.resize();
  }

  resize() {
    const width = this.canvas.clientWidth || 240;
    const height = this.canvas.clientHeight || 120;
    this.renderer.setSize(width, height, false);
    this.camera.aspect = width / height;
    this.camera.updateProjectionMatrix();
  }

  async show(blockId, fallbackColor = 0x8b8b8b, properties = null) {
    const propertyKey = properties && Object.keys(properties).length > 0
      ? Object.entries(properties)
        .sort(([a], [b]) => a.localeCompare(b))
        .map(([name, value]) => `${name}=${value}`)
        .join(",")
      : "";
    const stateKey = propertyKey ? `${blockId}[${propertyKey}]` : blockId;
    if (!blockId || stateKey === this.blockId) {
      return;
    }
    this.blockId = stateKey;
    let group;
    try {
      group = await this.loader.buildGroup(blockId, fallbackColor, fallbackColor, properties);
    } catch {
      group = this.loader.fallbackCube(fallbackColor);
    }
    if (stateKey !== this.blockId) {
      return;
    }
    this.root.clear();
    const box = new THREE.Box3().setFromObject(group);
    const center = box.getCenter(new THREE.Vector3());
    const size = box.getSize(new THREE.Vector3());
    const scale = 1.4 / Math.max(size.x, size.y, size.z, 0.01);
    group.position.sub(center);
    group.scale.setScalar(scale);
    this.root.add(group);
    this.camera.position.set(2.1, 1.8, 2.5);
    this.camera.lookAt(0, 0, 0);
    this.angle = 0;
    this.renderer.render(this.scene, this.camera);
    if (!this.running) {
      this.running = true;
      const animate = () => {
        if (!this.running) return;
        this.angle += 0.008;
        this.root.rotation.y = this.angle;
        this.renderer.render(this.scene, this.camera);
        this.animation = requestAnimationFrame(animate);
      };
      this.animation = requestAnimationFrame(animate);
    }
  }

  dispose() {
    this.running = false;
    if (this.animation) cancelAnimationFrame(this.animation);
  }
}
