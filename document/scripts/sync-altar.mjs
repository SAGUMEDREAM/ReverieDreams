import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {createAltarLayout} from './lib/altar-layout.mjs';

export function altarData(repo, blocks) {
  const source = relative => fs.readFileSync(path.join(repo, 'common/src/main/java/cc/thonly/reverie_dreams', relative), 'utf8');
  return createAltarLayout(source('block/props/GensokyoAltarBlock.java'), source('registry/content/block/RDWoodBlocks.java'), source('registry/content/block/RDBlocks.java'), blocks);
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const project = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
  const target = path.join(project, 'docs/public/mc-assets/data');
  const catalog = JSON.parse(fs.readFileSync(path.join(target, 'catalog.json'), 'utf8'));
  const data = altarData(path.resolve(project, '..'), catalog.blocks);
  fs.writeFileSync(path.join(target, 'altar-structure.json'), JSON.stringify(data));
  console.log(`Altar: ${data.placements.length} real blocks; ${data.pillars.length} pillars; ${data.height} layers.`);
}
