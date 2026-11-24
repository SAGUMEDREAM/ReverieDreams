import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { fileURLToPath } from 'node:url';
import { Resources, ModelResolver, findMinecraftJar, resourcePath, filesUnder } from './lib/resources.mjs';
import { text, stack, normalizeRecipe, ingredientIds, baseCategory, dropReferences } from './lib/catalog.mjs';
import { sourceSnapshot } from './lib/snapshot.mjs';
import { canonical, sameValue } from '../shared/value.mjs';
import { altarData } from './sync-altar.mjs';

const project = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const repo = path.resolve(project, '..');
const properties = fs.readFileSync(path.join(repo, 'gradle.properties'), 'utf8');
const minecraft = /^minecraft_version=(.+)$/m.exec(properties)[1].trim();
const version = /^version=(.+)$/m.exec(properties)[1].trim();
const runtimeFile = path.join(project, 'data/runtime.json');
if (!fs.existsSync(runtimeFile)) throw new Error('Missing runtime registry export. Run npm run sync:runtime first.');
const runtime = JSON.parse(fs.readFileSync(runtimeFile, 'utf8'));
const snapshotFile = path.join(project, 'data/runtime-source.json');
const snapshot = fs.existsSync(snapshotFile) ? JSON.parse(fs.readFileSync(snapshotFile, 'utf8')) : null;
const runtimeSourceStale = !snapshot || snapshot.stable === false || snapshot.sha256 !== sourceSnapshot(repo).sha256;
if (runtimeSourceStale) console.warn('Mod sources changed or were edited during export. Wiki uses the last actual runtime snapshot. Refresh with npm run sync:runtime; use npm run verify -- --strict-source before release.');
if (runtime.minecraft !== minecraft || runtime.version !== version) throw new Error(`Wiki runtime snapshot ${runtime.version}/${runtime.minecraft} differs from source ${version}/${minecraft}. Run npm run sync:runtime.`);
const target = path.join(project, 'docs/public/mc-assets');
const dataDir = path.join(target, 'data'); fs.mkdirSync(dataDir, { recursive: true });
const resources = new Resources(repo, findMinecraftJar(repo, minecraft), target);
const models = new ModelResolver(resources);
const langs = {};
const vanillaLanguageMeta = JSON.parse(fs.readFileSync(path.join(project, 'data/minecraft-zh_cn.meta.json'), 'utf8'));
if (vanillaLanguageMeta.minecraft !== minecraft || crypto.createHash('sha1').update(fs.readFileSync(path.join(project,'data/minecraft-zh_cn.json'))).digest('hex') !== vanillaLanguageMeta.sha1) throw new Error('Refresh the matching official Minecraft Chinese language asset and its metadata.');
for (const [short, locale] of Object.entries({ zh: 'zh_cn', en: 'en_us' })) {
  const vanillaLanguage = path.join(project, `data/minecraft-${locale}.json`);
  langs[short] = { ...resources.json('assets/minecraft/lang/en_us.json'), ...(fs.existsSync(vanillaLanguage) ? JSON.parse(fs.readFileSync(vanillaLanguage, 'utf8')) : resources.json(`assets/minecraft/lang/${locale}.json`)), ...resources.json(`assets/reverie_dreams/lang/${locale}.json`) };
}
const write = (file, data) => {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  const content = JSON.stringify(data);
  if (!fs.existsSync(file) || fs.readFileSync(file, 'utf8') !== content) {
    const temp = file + '.tmp'; fs.writeFileSync(temp, content); fs.renameSync(temp, file);
  }
};
const hash = data => crypto.createHash('sha256').update(JSON.stringify(canonical(data))).digest('hex').slice(0, 20);
const usedPreviews = new Set();
const records = [...runtime.items, ...(runtime.vanillaItems ?? [])];
const index = Object.fromEntries(records.map(item => [item.id, { ...item,
  nameZh: text(item.name, langs.zh), nameEn: text(item.name, langs.en),
  category: baseCategory(item), tabs: [], variants: [], recipes: [], uses: [], drops: [], sources: [],
  descriptions: { zh: item.descriptionKeys.map(key => langs.zh[key]).filter(Boolean), en: item.descriptionKeys.map(key => langs.en[key]).filter(Boolean) }
}]));
// A verified descriptive fallback is labeled separately from actual game localization.
const paddyNameSource = 'common/src/main/java/cc/thonly/reverie_dreams/block/PaddyFieldBlock.java';
if (index['reverie_dreams:paddy_field']?.nameZh === 'block.reverie_dreams.paddy_field') Object.assign(index['reverie_dreams:paddy_field'], {nameZh:'水田',nameEn:'Paddy Field',nameFromWiki:paddyNameSource});
const blueprintNameSource = 'common/src/main/java/cc/thonly/reverie_dreams/item/template/DanmakuShapeCreatorItem.java';
if (index['reverie_dreams:danmaku_recipe_creator']) {
  const bpNameZh = langs.zh['item.reverie_dreams.danmaku_shape_blueprint'] || '弹幕形状图纸';
  const bpNameEn = langs.en['item.reverie_dreams.danmaku_shape_blueprint'] || 'Danmaku Shape Blueprint';
  index['reverie_dreams:danmaku_recipe_creator'].components['minecraft:item_model'] = 'reverie_dreams:danmaku_shape_blueprint';
  Object.assign(index['reverie_dreams:danmaku_recipe_creator'], {
    nameZh: bpNameZh,
    nameEn: bpNameEn,
    nameFromWiki: blueprintNameSource
  });
}
if (index['reverie_dreams:ingredient/bamboo_shoots']) {
  index['reverie_dreams:ingredient/bamboo_shoots'].components['minecraft:item_model'] = 'reverie_dreams:large_bamboo_shoot';
}

function saveModel(id, components) {
  const model = models.item(id, components);
  const key = hash(model);
  const filename = path.join(target, 'previews', key + '.json'); write(filename, model); usedPreviews.add(path.basename(filename));
  return { model: `/mc-assets/previews/${key}.json`, previewKind: model.kind, previewReason: model.reason, previewIssues: model.sourceIssues ?? [] };
}
for (const item of Object.values(index)) Object.assign(item, saveModel(item.id, item.components));
for (const item of Object.values(index)) {
  const song = item.components['minecraft:jukebox_playable'];
  if (typeof song === 'string') {
    const [ns, name] = song.split(':'); const data = resources.json(`data/${ns}/jukebox_song/${name}.json`);
    if (data?.description) { item.song = data; item.nameZh += ' · ' + text(data.description, langs.zh); item.nameEn += ' · ' + text(data.description, langs.en); }
  }
  item.internal = item.id.startsWith('reverie_dreams:sgui/') || item.id.startsWith('reverie_dreams:holder/') || /_icon$|:icon$|:role_icon$|:battle_stick$|:owner_stick$/.test(item.id);
}
const tabs = runtime.tabs.map(tab => ({ id: tab.id, nameZh: text(tab.name, langs.zh), nameEn: text(tab.name, langs.en), items: [...new Set(tab.stacks.map(s => s.id))] }));
for (const tab of runtime.tabs) for (const entry of tab.stacks) {
  const item = index[entry.id]; if (!item) continue;
  if (!item.tabs.includes(tab.id)) item.tabs.push(tab.id);
  if (entry.components && Object.keys(entry.components).length) {
    const signature = hash(entry.components);
    if (!item.variants.some(v => v.key === signature)) item.variants.push({ key: signature, components: entry.components,
      nameZh: text(entry.name, langs.zh), nameEn: text(entry.name, langs.en), ...saveModel(item.id, { ...item.components, ...entry.components }) });
  }
}

const rawRecipes = [];
for (const [key] of resources.files) {
  const match = /^data\/([^/]+)\/recipe\/(.+)\.json$/.exec(key);
  if (!match || match[1] !== 'reverie_dreams') continue;
  const raw = resources.json(key, true);
  rawRecipes.push(normalizeRecipe(`${match[1]}:${match[2]}`, raw.type, raw, resources.source(key)));
}
for (const entry of runtime.customRecipes) {
  const folder = { gensokyo_altar: 'gensokyo_altar_recipe', barrel: 'barrel_recipe', kitchen: 'kitchen_recipe', danmaku: 'danmaku_recipe', strength_table: 'strength_table_recipe', danmaku_shape_draw: 'danmaku_shape_draw_recipe' }[entry.type.split(':')[1]];
  const key = folder ? `data/reverie_dreams/${folder}/${entry.id.split(':')[1]}.json` : '';
  rawRecipes.push({ ...normalizeRecipe(entry.id, entry.type, entry.data, key && resources.files.has(key) ? resources.source(key) : `runtime:${entry.type}`, true), virtual: entry.virtual });
}
const recipes = rawRecipes.filter(recipe => {
  for (const input of [...recipe.inputs, recipe.output].filter(Boolean)) {
    if (input.id && input.id !== 'minecraft:air' && !index[input.id]) return false;
    if (input.tag && !(runtime.itemTags ?? {})[input.tag]) return false;
  }
  return true;
});
const recipeIndex = {};
function preserveVariant(value) {
  if (!value) return;
  if (value.alternatives) return value.alternatives.forEach(preserveVariant);
  const item = index[value.id]; if (!item || !value.components) return;
  const patch = Object.fromEntries(Object.entries(value.components).filter(([key,v]) => !sameValue(item.components[key], v)));
  if (!Object.keys(patch).length) return;
  const signature = hash(patch);
  if (!item.variants.some(v => v.key === signature)) item.variants.push({ key: signature, components: patch,
    nameZh: text(patch['minecraft:custom_name'] ?? patch['minecraft:item_name'] ?? item.name, langs.zh),
    nameEn: text(patch['minecraft:custom_name'] ?? patch['minecraft:item_name'] ?? item.name, langs.en),
    ...saveModel(item.id, { ...item.components, ...patch }) });
}
for (const recipe of recipes) {
  [...recipe.inputs, recipe.output].forEach(preserveVariant);
  const key = `${recipe.type}/${recipe.id}`;
  recipe.key = key; recipeIndex[key] = recipe;
  for (const id of ingredientIds(recipe.output, runtime.itemTags ?? {})) if (index[id] && !index[id].recipes.includes(key)) index[id].recipes.push(key);
  for (const input of recipe.inputs) for (const id of ingredientIds(input, runtime.itemTags ?? {})) if (index[id] && !index[id].uses.includes(key)) index[id].uses.push(key);
}

const extra = { customers: [], foodProperties: {}, beverageProperties: {}, trades: [], biomes: [], structures: [], dimensions: [], advancements: [] };
const lootTables = {};
for (const [key] of resources.files) {
  const match = /^data\/reverie_dreams\/(.+)\.json$/.exec(key); if (!match) continue;
  const relative = match[1];
  if (relative.startsWith('gensokyo/') || relative.startsWith('gensokyo-backup/')) continue;
  if (relative.startsWith('loot_table/')) {
    const raw = resources.json(key, true);
    const id = `reverie_dreams:${relative.slice('loot_table/'.length)}`;
    lootTables[id] = { source: resources.source(key), data: raw };
    for (const itemId of new Set(dropReferences(raw))) if (index[itemId]) index[itemId].drops.push({ table: id, source: resources.source(key) });
  } else if (relative.startsWith('customer/')) extra.customers.push({ id: relative.slice(9), data: resources.json(key), source: resources.source(key) });
  else if (relative.startsWith('food_property/')) extra.foodProperties[`reverie_dreams:${relative.slice(14)}`] = resources.json(key);
  else if (relative.startsWith('beverage_property/')) extra.beverageProperties[`reverie_dreams:${relative.slice(18)}`] = resources.json(key);
  else if (relative.startsWith('villager_trade/')) extra.trades.push({ id: relative.slice(15), data: resources.json(key), source: resources.source(key) });
  else if (relative.startsWith('worldgen/biome/')) extra.biomes.push({ id: `reverie_dreams:${relative.slice(15)}`, data: resources.json(key), source: resources.source(key) });
  else if (relative.startsWith('worldgen/structure/')) extra.structures.push({ id: `reverie_dreams:${relative.slice(19)}`, data: resources.json(key), source: resources.source(key) });
  else if (relative.startsWith('dimension/')) extra.dimensions.push({ id: `reverie_dreams:${relative.slice(10)}`, data: resources.json(key), source: resources.source(key) });
  else if (relative.startsWith('advancement/')) extra.advancements.push({ id: relative.slice(12), data: resources.json(key), source: resources.source(key) });
}

for (const item of Object.values(index)) { item.foodProperties ??= []; item.beverageProperties ??= []; item.trades = []; }
for (const field of ['foodProperties', 'beverageProperties']) for (const [id, raw] of Object.entries(extra[field])) {
  for (const value of raw.properties?.values ?? []) {
    const ids = typeof value === 'string' && value.startsWith('#') ? runtime.itemTags[value.slice(1)] ?? [] : [value];
    for (const itemId of ids) if (index[itemId] && !index[itemId][field].includes(raw.registry_key ?? id)) index[itemId][field].push(raw.registry_key ?? id);
  }
}
for (const trade of extra.trades) {
  const output = stack(trade.data.gives);
  if (index[output?.id]) index[output.id].trades.push(trade);
}
const blocks = Object.fromEntries(runtime.blocks.map(b => {
  const pack = models.block(b.id, b.defaultState?.Properties ?? {});
  const model = `/mc-assets/previews/${hash(pack)}.json`; write(path.join(target, model.slice('/mc-assets/'.length)), pack);
  usedPreviews.add(path.basename(model));
  const block = { ...b, nameZh: langs.zh[b.translationKey] ?? index[b.id]?.nameZh ?? b.id, nameEn: langs.en[b.translationKey] ?? index[b.id]?.nameEn ?? b.id, model };
  if (index[b.id] && b.item === 'minecraft:air' && b.id.includes(':cuisine/')) index[b.id].placedBlock = block;
  return [b.id, block];
}));
const entities = runtime.entities.map(e => ({ ...e, nameZh: langs.zh[e.translationKey] ?? e.id, nameEn: langs.en[e.translationKey] ?? e.id }));
write(path.join(dataDir, 'altar-structure.json'), altarData(repo, blocks));
const missingReferences = [];
for (const recipe of recipes) for (const input of [...recipe.inputs, recipe.output].filter(Boolean)) {
  if (input.id && input.id !== 'minecraft:air' && !index[input.id]) missingReferences.push({ recipe: recipe.key, item: input.id });
  if (input.tag && !(runtime.itemTags ?? {})[input.tag]) missingReferences.push({ recipe: recipe.key, tag: input.tag });
}
const modItems = Object.values(index).filter(i => i.id.startsWith('reverie_dreams:'));
const report = { version, minecraft, items: runtime.items.length, blocks: runtime.blocks.length, entities: runtime.entities.length,
  recipes: recipes.length, customRecipes: runtime.customRecipes.length, tabs: tabs.length,
  categories: Object.fromEntries([...new Set(modItems.map(i => i.category))].map(c => [c, modItems.filter(i => i.category === c).length])),
  previews: Object.fromEntries([...new Set(modItems.map(i => i.previewKind))].map(c => [c, modItems.filter(i => i.previewKind === c).length])),
  missingReferences, missingResources: [...models.missing], runtimeWarnings: runtime.errors, runtimeSourceStale };
write(path.join(dataDir, 'catalog.json'), { version, minecraft, items: index, tabs, blocks, entities, tags: runtime.itemTags ?? {}, stats: report });
write(path.join(dataDir, 'recipes.json'), recipeIndex);
write(path.join(dataDir, 'world.json'), extra);
write(path.join(dataDir, 'loot-tables.json'), lootTables);
write(path.join(dataDir, 'lang-zh.json'), langs.zh); write(path.join(dataDir, 'lang-en.json'), langs.en);
write(path.join(project, 'data/coverage.json'), report);
resources.copy('assets/reverie_dreams/icon.png');
const previewDirectory = fs.realpathSync(path.join(target, 'previews'));
if (!previewDirectory.startsWith(fs.realpathSync(path.join(project, 'docs/public')) + path.sep)) throw new Error('Invalid generated preview directory');
for (const filename of fs.readdirSync(previewDirectory)) if (/^[a-f0-9]{20}\.json$/.test(filename) && !usedPreviews.has(filename)) {
  const obsolete = path.resolve(previewDirectory, filename);
  if (path.dirname(obsolete) !== previewDirectory) throw new Error('Invalid generated preview path');
  fs.unlinkSync(obsolete);
}
write(path.join(target, 'manifest.json'), { schema: 1, previews: [...usedPreviews].sort(), resources: [...resources.copied].sort() });
console.log(JSON.stringify(report, null, 2));
if (missingReferences.length) process.exitCode = 1;
