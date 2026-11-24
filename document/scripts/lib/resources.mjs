import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import os from 'node:os';

export const normalizeId = (value, namespace = 'minecraft') => value.includes(':') ? value : `${namespace}:${value}`;
export const resourcePath = (id, kind, extension = '.json') => {
  const [namespace, name] = normalizeId(id).split(':');
  return `assets/${namespace}/${kind}/${name}${extension}`;
};

/** Read local game resources without fetching from a CDN or changing the game JAR. */
export class ZipReader {
  constructor(file) {
    this.file = file;
    this.buffer = fs.readFileSync(file);
    this.entries = new Map();
    let end = this.buffer.length - 22;
    for (; end >= Math.max(0, this.buffer.length - 65557); end--) if (this.buffer.readUInt32LE(end) === 0x06054b50) break;
    if (end < 0) throw new Error(`Invalid ZIP: ${file}`);
    const count = this.buffer.readUInt16LE(end + 10);
    let offset = this.buffer.readUInt32LE(end + 16);
    for (let i = 0; i < count; i++) {
      if (this.buffer.readUInt32LE(offset) !== 0x02014b50) throw new Error(`Invalid ZIP directory: ${file}`);
      const nameSize = this.buffer.readUInt16LE(offset + 28);
      const extraSize = this.buffer.readUInt16LE(offset + 30);
      const commentSize = this.buffer.readUInt16LE(offset + 32);
      const name = this.buffer.toString('utf8', offset + 46, offset + 46 + nameSize);
      this.entries.set(name, { method: this.buffer.readUInt16LE(offset + 10), size: this.buffer.readUInt32LE(offset + 20), offset: this.buffer.readUInt32LE(offset + 42) });
      offset += 46 + nameSize + extraSize + commentSize;
    }
  }
  read(name) {
    const entry = this.entries.get(name);
    if (!entry) return null;
    const pos = entry.offset;
    const start = pos + 30 + this.buffer.readUInt16LE(pos + 26) + this.buffer.readUInt16LE(pos + 28);
    const data = this.buffer.subarray(start, start + entry.size);
    if (entry.method === 0) return data;
    if (entry.method === 8) return zlib.inflateRawSync(data);
    throw new Error(`Unsupported ZIP compression ${entry.method}: ${name}`);
  }
}

export function filesUnder(root) {
  if (!fs.existsSync(root)) return [];
  return fs.readdirSync(root, { withFileTypes: true }).sort((a, b) => a.name.localeCompare(b.name)).flatMap(entry => {
    const file = path.join(root, entry.name);
    return entry.isDirectory() ? filesUnder(file) : [file];
  });
}

export class Resources {
  constructor(repo, jar, target) {
    this.repo = repo;
    this.jar = jar ? new ZipReader(jar) : null;
    this.target = target;
    this.files = new Map();
    this.copied = new Set();
    for (const folder of ['common/src/generated/resources', 'common/src/main/resources']) {
      const root = path.join(repo, folder);
      for (const file of filesUnder(root)) {
        const key = path.relative(root, file).replaceAll('\\', '/');
        if (key.startsWith('assets/') || key.startsWith('data/')) this.files.set(key, file);
      }
    }
  }
  read(key) { return this.files.has(key) ? fs.readFileSync(this.files.get(key)) : this.jar?.read(key) ?? null; }
  json(key, required = false) {
    const data = this.read(key);
    if (!data) {
      if (required) throw new Error(`Missing resource ${key}`);
      return null;
    }
    try { return JSON.parse(data.toString('utf8').replace(/^\uFEFF/, '')); }
    catch (error) { throw new Error(`Invalid JSON ${key}: ${error.message}`); }
  }
  source(key) { return this.files.has(key) ? path.relative(this.repo, this.files.get(key)).replaceAll('\\', '/') : `minecraft.jar!/${key}`; }
  copy(key) {
    if (this.copied.has(key)) return `/mc-assets/${key}`;
    const data = this.read(key);
    if (!data) return null;
    const destination = path.join(this.target, key);
    fs.mkdirSync(path.dirname(destination), { recursive: true });
    if (!fs.existsSync(destination) || !fs.readFileSync(destination).equals(data)) fs.writeFileSync(destination, data);
    this.copied.add(key);
    // Animation metadata is part of the texture resource, not another sprite sheet.
    const animation = this.read(key + '.mcmeta');
    if (animation) { fs.writeFileSync(destination + '.mcmeta', animation); this.copied.add(key + '.mcmeta'); }
    return `/mc-assets/${key}`;
  }
  missingTexture() {
    // The game's missing sprite, generated procedurally; never substitute a particle texture.
    const key = 'assets/wiki/textures/missing.png';
    if (!this.copied.has(key)) {
      const crc = buffer => { let n = 0xffffffff; for (const b of buffer) { n ^= b; for (let i=0;i<8;i++) n = (n>>>1)^((n&1)?0xedb88320:0); } return (n^0xffffffff)>>>0; };
      const chunk = (type, data) => { const label=Buffer.from(type), size=Buffer.alloc(4), check=Buffer.alloc(4); size.writeUInt32BE(data.length); check.writeUInt32BE(crc(Buffer.concat([label,data]))); return Buffer.concat([size,label,data,check]); };
      const head=Buffer.alloc(13);head.writeUInt32BE(16,0);head.writeUInt32BE(16,4);head[8]=8;head[9]=6;
      const raw=Buffer.alloc(16*65);
      for(let y=0;y<16;y++)for(let x=0;x<16;x++){const i=y*65+1+x*4, pink=(x<8)===(y<8);raw[i]=pink?248:0;raw[i+2]=pink?248:0;raw[i+3]=255;}
      const output=path.join(this.target,key);fs.mkdirSync(path.dirname(output),{recursive:true});
      fs.writeFileSync(output,Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]),chunk('IHDR',head),chunk('IDAT',zlib.deflateSync(raw)),chunk('IEND',Buffer.alloc(0))]));this.copied.add(key);
    }
    return '/mc-assets/'+key;
  }
}

export class ModelResolver {
  constructor(resources) { this.resources = resources; this.models = new Map(); this.missing = new Set(); this.textureInfo = new Map(); }
  model(id, chain = []) {
    id = normalizeId(id);
    if (this.models.has(id)) return this.models.get(id);
    if (chain.includes(id)) throw new Error(`Model inheritance cycle: ${[...chain, id].join(' -> ')}`);
    if (id === 'minecraft:builtin/generated' || id === 'minecraft:builtin/entity') return { builtin: id, textures: {}, display: {} };
    const key = resourcePath(id, 'models');
    const raw = this.resources.json(key);
    if (!raw) { this.missing.add(key); return null; }
    const parent = raw.parent ? this.model(raw.parent, [...chain, id]) : null;
    const model = { ...parent, ...raw, textures: { ...parent?.textures, ...raw.textures }, display: { ...parent?.display, ...raw.display }, source: this.resources.source(key) };
    if (!Object.hasOwn(raw, 'elements') && parent?.elements) model.elements = parent.elements;
    this.models.set(id, model);
    return model;
  }
  texture(reference, variables, chain = []) {
    if (!reference) return this.resources.missingTexture();
    if (typeof reference === 'object') reference = reference.sprite;
    if (!reference) return null;
    if (reference.startsWith('#')) {
      const variable = reference.slice(1);
      if (chain.includes(variable)) throw new Error(`Texture variable cycle: ${[...chain, variable].join(' -> ')}`);
      return this.texture(variables[variable], variables, [...chain, variable]);
    }
    const key = resourcePath(reference, 'textures', '.png');
    const url = this.resources.copy(key);
    if (!url) this.missing.add(key);
    else if (!this.textureInfo.has(url)) {
      const data = this.resources.read(key);
      this.textureInfo.set(url, { width: data.readUInt32BE(16), height: data.readUInt32BE(20), animation: this.resources.json(key + '.mcmeta')?.animation });
    }
    return url ?? this.resources.missingTexture();
  }
  bake(id, tints = [], components = {}) {
    const model = this.model(id);
    if (!model) return { kind: 'unavailable', reason: 'missing-model', source: id };
    const textures = Object.fromEntries(Object.keys(model.textures).map(key => [key, this.texture(`#${key}`, model.textures)]));
    const common = { display: model.display, guiLight: model.gui_light ?? 'side', source: model.source,
      tints: tints.map(t => tint(t, components, this.resources)),
      sourceIssues: [model.parent && !normalizeId(model.parent).startsWith('minecraft:builtin/') && !this.models.has(normalizeId(model.parent)) ? `missing-parent:${model.parent}` : null,
        Object.values(model.elements ?? {}).some(e => Object.values(e.faces ?? {}).some(f => this.texture(f.texture, model.textures) === '/mc-assets/assets/wiki/textures/missing.png')) ? 'missing-face-texture' : null].filter(Boolean),
      textureInfo: Object.fromEntries(Object.values(textures).filter(Boolean).map(url => [url, this.textureInfo.get(url)])) };
    if (model.builtin === 'minecraft:builtin/generated' || !model.elements && textures.layer0) {
      return { ...common, kind: 'layers', layers: Object.keys(textures).filter(k => /^layer\d+$/.test(k)).sort().map(k => textures[k]).filter(Boolean) };
    }
    if (model.elements) {
      return { ...common, kind: 'cuboid', elements: model.elements.map(element => ({ ...element, faces: Object.fromEntries(Object.entries(element.faces ?? {}).map(([face, data]) => [face, { ...data, texture: this.texture(data.texture, model.textures) }])) })) };
    }
    return { ...common, kind: 'unavailable', reason: model.builtin ?? 'special-renderer' };
  }
  definition(node, components = {}, active = false) {
    if (!node) return { kind: 'unavailable', reason: 'missing-item-definition' };
    const type = normalizeId(node.type ?? 'minecraft:model');
    if (type === 'minecraft:model') return this.bake(node.model, node.tints ?? [], components);
    if (type === 'minecraft:composite') return { kind: 'composite', models: node.models.map(child => this.definition(child, components, active)) };
    if (type === 'minecraft:empty') return { kind: 'empty' };
    if (type === 'minecraft:condition') {
      const property = normalizeId(node.property);
      const value = property === 'minecraft:using_item' ? active : property === 'minecraft:has_component' ? components[node.component] != null : false;
      return this.definition(value ? node.on_true : node.on_false, components, active);
    }
    if (type === 'minecraft:select') {
      const property = normalizeId(node.property);
      const value = property === 'minecraft:display_context' ? 'gui' : property === 'minecraft:main_hand' ? 'right' : null;
      const selected = (node.cases ?? []).find(c => (Array.isArray(c.when) ? c.when : [c.when]).includes(value));
      return this.definition(selected?.model ?? node.fallback, components, active);
    }
    if (type === 'minecraft:range_dispatch') {
      let value = 0;
      if (node.property === 'minecraft:damage') value = (components['minecraft:damage'] ?? 0) / Math.max(1, components['minecraft:max_damage'] ?? 1);
      const selected = (node.entries ?? []).filter(e => e.threshold <= value * (node.scale ?? 1)).at(-1);
      return this.definition(selected?.model ?? node.fallback, components, active);
    }
    if (type === 'minecraft:special') return { kind: 'unavailable', reason: node.model?.type ?? 'special-renderer', source: node.base };
    return { kind: 'unavailable', reason: type };
  }
  item(id, components = {}, active = false) {
    const model = components['minecraft:item_model'] ?? id;
    let definition = this.resources.json(resourcePath(model, 'items'));
    if (!definition && model.includes(':')) {
      const [ns, name] = model.split(':');
      definition = this.resources.json(`assets/${ns}/items/redirecting/${name}.json`);
    }
    if (definition) return this.definition(definition.model, components, active);
    // Resource definitions are authoritative in 26.x; no model-filename -> item-ID guessing.
    return { kind: 'unavailable', reason: 'missing-item-definition', source: model };
  }
  block(id, state = {}) {
    const definition = this.resources.json(resourcePath(id, 'blockstates'));
    if (!definition) return { kind: 'unavailable', reason: 'missing-blockstate', source: id };
    const matches = condition => !condition || Object.entries(condition).every(([key, value]) =>
      key === 'OR' ? value.some(matches) : key === 'AND' ? value.every(matches) : String(value).split('|').includes(String(state[key])));
    let nodes = [];
    if (definition.variants) {
      const entries = Object.entries(definition.variants);
      const selected = entries.find(([key]) => matches(Object.fromEntries(key ? key.split(',').map(v => v.split('=')) : []))) ?? entries[0];
      if (selected) nodes = [selected[1]];
    } else nodes = (definition.multipart ?? []).filter(part => matches(part.when)).map(part => part.apply);
    const packs = nodes.map(value => {
      const node = Array.isArray(value) ? value[0] : value;
      const pack = this.bake(node.model);
      return { ...pack, blockRotation: [node.x ?? 0, node.y ?? 0, 0] };
    });
    return { kind: 'composite', models: packs, state, source: this.resources.source(resourcePath(id, 'blockstates')) };
  }
}

function tint(tint, components, resources) {
  if (tint.type === 'minecraft:constant') return tint.value;
  if (tint.type === 'minecraft:dye') return components['minecraft:dyed_color']?.rgb ?? components['minecraft:dyed_color'] ?? tint.default ?? 0xffffff;
  if (tint.type === 'minecraft:custom_model_data') return components['minecraft:custom_model_data']?.colors?.[tint.index ?? 0] ?? tint.default ?? 0xffffff;
  if (tint.type === 'minecraft:potion') return components['minecraft:potion_contents']?.custom_color ?? tint.default ?? 0x385dc6;
  if (tint.type === 'minecraft:grass') return { ...tint, colorMap: resources.copy('assets/minecraft/textures/colormap/grass.png') };
  if (tint.type === 'minecraft:map_color') return components['minecraft:map_color'] ?? tint.default ?? 0xffffff;
  if (tint.type === 'minecraft:firework') {
    const colors = components['minecraft:firework_explosion']?.colors ?? [];
    if (colors.length) return [16,8,0].reduce((sum, shift) => sum | (Math.floor(colors.reduce((n,c) => n + ((c >> shift)&255),0)/colors.length) << shift), 0);
  }
  return tint.default ?? 0xffffff;
}

export function findMinecraftJar(repo, version) {
  if (process.env.MINECRAFT_CLIENT_JAR && fs.existsSync(process.env.MINECRAFT_CLIENT_JAR)) return process.env.MINECRAFT_CLIENT_JAR;
  const homes = [process.env.GRADLE_USER_HOME, path.resolve(repo, '..', '.gradle'), path.join(os.homedir(), '.gradle')].filter(Boolean);
  for (const home of homes) for (const relative of [`caches/fabric-loom/${version}/minecraft-client.jar`, `caches/neoformruntime/artifacts/minecraft_${version}_client.jar`]) {
    const file = path.join(home, relative); if (fs.existsSync(file)) return file;
  }
  throw new Error(`Minecraft ${version} client JAR not found. Set MINECRAFT_CLIENT_JAR to the local game JAR.`);
}
