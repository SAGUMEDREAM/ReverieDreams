const sourceFile = 'common/src/main/java/cc/thonly/reverie_dreams/block/props/GensokyoAltarBlock.java';

/** Extract checked coordinate, layer and material bindings from the multiblock rule. */
export function createAltarLayout(altarJava, woodJava, registryJava, blocks) {
  const fail = () => { throw Error('Altar structure rules changed. Review the Wiki structure extractor against ' + sourceFile); };
  const offsetsText = /\bOFFSETS\s*=\s*\{([\s\S]*?)\n\s*\};/.exec(altarJava)?.[1];
  if (!offsetsText) return fail();
  const offsets = [...offsetsText.matchAll(/\{\s*(-?\d+)\s*,\s*(-?\d+)\s*\}/g)].map(m => [+m[1], +m[2]]);
  const skipped = /if\s*\(\s*i\s*==\s*(\d+)\s*\)\s*continue\s*;/.exec(altarJava)?.[1];
  const height = /for\s*\(int dy\s*=\s*0;\s*dy\s*<=\s*(\d+);\s*dy\+\+\)/.exec(altarJava)?.[1];
  const top = /isTopLayer\s*=\s*\(dy\s*==\s*(\d+)\)/.exec(altarJava)?.[1];
  if (skipped == null || height == null || top !== height || offsets[+skipped]?.some(n => n !== 0)
    || !/blockType\s*=\s*RDWoodBlocks\.SPIRITUAL_BUNDLE\.strippedLog\(\)\.asBlock\(\)/.test(altarJava)
    || !/topBlockType\s*=\s*RDWoodBlocks\.BLESSED_SPIRITUAL_LOG\.asBlock\(\)/.test(altarJava)) return fail();
  const wood = /\bSPIRITUAL_BUNDLE\s*=\s*WoodBundle\.create\("([^"]+)"/.exec(woodJava)?.[1];
  const blessed = /\bBLESSED_SPIRITUAL_LOG\s*=\s*RDBlocks\.registerBlock\("([^"]+)"/.exec(woodJava)?.[1];
  const altar = /\bGENSOKYO_ALTAR\s*=\s*registerBlock\("([^"]+)"/.exec(registryJava)?.[1];
  if (!wood || !blessed || !altar) return fail();
  const ids = { altar: `reverie_dreams:${altar}`, log: `reverie_dreams:stripped_${wood}_log`, cap: `reverie_dreams:${blessed}` };
  const pillars = offsets.filter((_, i) => i !== +skipped);
  if (pillars.some(([x,z]) => x === 0 && z === 0) || new Set(pillars.map(p => p.join(','))).size !== pillars.length) return fail();
  const placements = [{ id: ids.altar, pos: [0,0,0] }];
  for (const [x,z] of pillars) for (let y=0;y<=+height;y++) placements.push({ id: y === +top ? ids.cap : ids.log, pos: [x,y,z] });
  const materials = Object.values(ids).map(id => {
    const block = blocks[id]; if (!block?.model) throw Error('Missing actual altar block model: ' + id);
    return { id, count: placements.filter(p => p.id === id).length, nameZh: block.nameZh, nameEn: block.nameEn, model: block.model };
  });
  const axis = index => placements.map(p => p.pos[index]);
  const bounds = { min: [0,1,2].map(i => Math.min(...axis(i))), max: [0,1,2].map(i => Math.max(...axis(i))) };
  return { schema: 1, source: sourceFile, origin: [0,0,0], ids, pillars, height: +height+1, bounds, placements, materials };
}
