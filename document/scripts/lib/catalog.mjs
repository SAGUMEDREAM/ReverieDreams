import { normalizeId } from './resources.mjs';

export function text(component, lang) {
  if (component == null) return '';
  if (typeof component === 'string') return component.replace(/§[0-9a-fk-or]/gi, '');
  if (Array.isArray(component)) return component.map(c => text(c, lang)).join('');
  let result = component.text ?? '';
  if (component.translate) {
    const args = (component.with ?? []).map(c => text(c, lang));
    let next = 0;
    result = (lang[component.translate] ?? component.fallback ?? component.translate).replace(/%(?:(\d+)\$)?([sd%])/g, (_, index, type) => type === '%' ? '%' : args[index ? +index - 1 : next++] ?? '');
  }
  return (result + (component.extra ?? []).map(c => text(c, lang)).join('')).replace(/§[0-9a-fk-or]/gi, '');
}

export function stack(value, count) {
  if (value == null) return null;
  if (typeof value === 'string') return value.startsWith('#') ? { tag: normalizeId(value.slice(1)), count: count ?? 1 } : { id: normalizeId(value), count: count ?? 1 };
  if (Array.isArray(value)) return { alternatives: value.map(v => stack(v)).filter(Boolean), count: count ?? 1 };
  if (value.item) return stack(value.item, count ?? value.count);
  if (value.tag) return { tag: normalizeId(value.tag), count: count ?? value.count ?? 1 };
  if (value.id) return { ...value, id: normalizeId(value.id), count: count ?? value.count ?? 1 };
  return { special: value, count: count ?? 1 };
}

export function normalizeRecipe(id, type, raw, source, runtime = false) {
  const recipe = { id, type: normalizeId(type), source, raw, inputs: [], output: null, layout: null };
  if (type === 'minecraft:crafting_shaped') {
    const rows = raw.pattern ?? [];
    const width = rows.reduce((size, row) => Math.max(size, row.length), 0);
    recipe.layout = [];
    for (let y = 0; y < 3; y++) for (let x = 0; x < 3; x++) {
      const key = rows[y]?.[x];
      recipe.layout.push(key && key !== ' ' && x < width ? stack(raw.key[key]) : null);
    }
    recipe.inputs = recipe.layout.filter(Boolean);
    recipe.output = stack(raw.result);
  } else if (type === 'minecraft:crafting_shapeless') {
    recipe.inputs = (raw.ingredients ?? []).map(v => stack(v)); recipe.output = stack(raw.result);
  } else if (['minecraft:smelting', 'minecraft:blasting', 'minecraft:smoking', 'minecraft:campfire_cooking', 'minecraft:stonecutting'].includes(type)) {
    recipe.inputs = [stack(raw.ingredient)]; recipe.output = stack(raw.result);
    if (raw.cookingtime != null) recipe.seconds = raw.cookingtime / 20;
    if (raw.experience != null) recipe.experience = raw.experience;
  } else if (type === 'minecraft:smithing_transform') {
    recipe.inputs = [stack(raw.template), stack(raw.base), stack(raw.addition)]; recipe.output = stack(raw.result);
  } else if (type === 'reverie_dreams:gensokyo_altar') {
    const outer = [...(raw.slots ?? [])];
    while (outer.length < 8) outer.push({ id: 'minecraft:air', count: 0 });
    // The actual GensokyoAltarGui assigns outer slots in row-major order, skipping its core.
    recipe.layout = [outer[0], outer[1], outer[2], outer[3], raw.core, outer[4], outer[5], outer[6], outer[7]].map(v => stack(v));
    recipe.inputs = [stack(raw.core), ...outer.map(v => stack(v))].filter(Boolean); recipe.output = stack(raw.output);
  } else if (type === 'reverie_dreams:barrel') {
    recipe.inputs = (raw.materials ?? []).map(v => stack(v)); recipe.output = stack(raw.output);
    recipe.seconds = (raw.cost_time ?? 6000) / 20;
  } else if (type === 'reverie_dreams:kitchen') {
    recipe.inputs = (raw.ingredients ?? []).map(v => stack(v)); recipe.output = stack(raw.output);
    recipe.station = raw.recipe_type;
    recipe.seconds = raw.cost_time ?? 10;
    recipe.timeNote = 'kitchen';
  } else if (type === 'reverie_dreams:danmaku') {
    recipe.inputs = ['material', 'core', 'dye', 'power', 'point'].map(key => stack(raw[key])).filter(Boolean);
    recipe.output = stack(raw.output);
    recipe.inputLabels = ['material', 'core', 'dye', 'power', 'point'];
  } else if (type === 'reverie_dreams:danmaku_shape_draw') {
    recipe.output = stack(raw.result);
    recipe.shape = raw.shape;
  } else if (type === 'reverie_dreams:strength_table') {
    recipe.inputs = [stack(raw.main_item), stack(raw.off_item)].filter(Boolean);
    recipe.output = stack(raw.output);
  } else if (type.startsWith('reverie_dreams:')) {
    recipe.inputs = (raw.ingredients ?? raw.materials ?? raw.inputs ?? raw.input ?? []).map?.(v => stack(v)) ?? [stack(raw.input)].filter(Boolean);
    recipe.output = stack(raw.output);
  } else {
    recipe.special = true;
    recipe.output = stack(raw.result ?? raw.output);
  }
  recipe.inputs = recipe.inputs.filter(v => v && v.id !== 'minecraft:air');
  recipe.runtime = runtime;
  return recipe;
}

export function ingredientIds(ingredient, tags) {
  if (!ingredient) return [];
  if (ingredient.id && ingredient.id !== 'minecraft:air') return [ingredient.id];
  if (ingredient.tag) return tags[ingredient.tag] ?? [];
  if (ingredient.alternatives) return ingredient.alternatives.flatMap(v => ingredientIds(v, tags));
  return [];
}

export function baseCategory(item) {
  const classes = item.classes ?? [];
  if (item.id.includes(':cuisine/')) return 'food';
  if (item.id.includes(':beverage/')) return 'beverage';
  if (item.id.includes(':ingredient/')) return 'ingredient';
  if (classes.some(c => c.includes('.item.weapon.') || /SwordItem$|BowItem$|CrossbowItem$|ShieldItem$/.test(c))) return 'weapon';
  if (item.components?.['minecraft:equippable']) return 'equipment';
  if (item.block) return 'block';
  if (classes.some(c => /SpawnEggItem$/.test(c))) return 'spawn_egg';
  if (classes.some(c => c.includes('Danmaku'))) return 'danmaku';
  if (classes.some(c => /RoleCardItem$|SoulCard|RoleFollowerArchive/.test(c))) return 'role';
  if (classes.some(c => c.includes('.item.prop.') || c.includes('.item.template.'))) return 'prop';
  return 'material';
}

export function dropReferences(value, list = []) {
  if (!value || typeof value !== 'object') return list;
  if (Array.isArray(value)) { value.forEach(v => dropReferences(v, list)); return list; }
  if (value.type === 'minecraft:item' && value.name) list.push(value.name);
  Object.values(value).forEach(v => dropReferences(v, list));
  return list;
}
