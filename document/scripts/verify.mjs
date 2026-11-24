import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { sourceSnapshot } from './lib/snapshot.mjs';
const project=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..'),repo=path.resolve(project,'..');
const read=file=>JSON.parse(fs.readFileSync(path.join(project,file),'utf8'));
const report=read('data/coverage.json'),runtime=read('data/runtime.json'),catalog=read('docs/public/mc-assets/data/catalog.json');
const saved=read('data/runtime-source.json'),current=sourceSnapshot(repo);
if(saved.stable === false || saved.sha256!==current.sha256) {
  const message='Runtime source snapshot needs refresh: concurrent mod edits were detected. Run npm run sync:runtime, then npm run sync.';
  if(process.argv.includes('--strict-source')) throw Error(message);
  console.warn(message);
}
if(report.items!==runtime.items.length||report.blocks!==runtime.blocks.length||report.entities!==runtime.entities.length)throw Error('Registry coverage differs');
if(report.missingReferences.length||report.runtimeWarnings.length)throw Error('Unresolved recipe/registry references');
const known=new Set(['assets/minecraft/models/keyboard.json','assets/minecraft/models/table.json','assets/minecraft/textures/missingno.png','assets/minecraft/textures/all.png']);
for(const file of report.missingResources)if(!known.has(file))throw Error('New source resource issue: '+file);
for(const item of Object.values(catalog.items).filter(i=>i.id.startsWith('reverie_dreams:')))if(!['layers','cuboid','composite'].includes(item.previewKind))throw Error('Missing mod item preview: '+item.id);
const knownModels=new Set(['violin','chair','silver_chest','brewing_barrel','fumo/alice','fumo/flandre','fumo/shou'].map(id=>'reverie_dreams:'+id));
for(const item of Object.values(catalog.items))if(item.id.startsWith('reverie_dreams:')&&item.previewIssues?.length&&!knownModels.has(item.id))throw Error('New model source issue: '+item.id);
console.log(`Verified ${report.items} items, ${report.blocks} blocks, ${report.entities} entities and ${report.recipes} recipes. Source snapshot: ${saved.sha256.slice(0,12)}.`);
console.log('Four existing mod-resource references are documented in README; missing textures use the game-style missing sprite.');
