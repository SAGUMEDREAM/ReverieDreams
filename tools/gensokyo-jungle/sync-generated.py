"""Check actual isolated Fabric provider output, then copy owned resources verbatim.

This script never authors/patches generated JSON. Provider output is authoritative.
It also refuses to overwrite a concurrent edit to one of the selected paths.
"""
from pathlib import Path
import sys, json, shutil, hashlib

ROOT = Path(__file__).resolve().parents[2]
DEST = ROOT/'common/src/generated/resources'
BEFORE = ROOT/'.agent/gensokyo-jungle-20261119/generated-before'
OUT = ROOT/'build/gensokyo-jungle-review'

def read(p): return json.loads(p.read_text(encoding='utf-8-sig'))
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest() if p.exists() else None

def main():
    source = Path(sys.argv[1]); biome = 'reverie_dreams:gensokyo_jungle'
    selected = ['data/reverie_dreams/worldgen/biome/gensokyo_jungle.json',
                'data/reverie_dreams/dimension/gensokyo.json', 'data/reverie_dreams/gensokyo/biomes.json',
                'data/c/tags/worldgen/biome/is_forest.json', 'data/minecraft/tags/worldgen/biome/is_overworld.json',
                'data/reverie_dreams/tags/worldgen/biome/gensokyo.json', 'assets/reverie_dreams/lang/zh_cn.json']
    for kind in ['configured_feature', 'placed_feature']:
        folder = source/('data/reverie_dreams/worldgen/'+kind)
        for p in folder.rglob('*.json'):
            rel = p.relative_to(folder).as_posix()
            if rel.startswith('gensokyo/jungle') or '/jungle/' in rel:
                selected.append(p.relative_to(source).as_posix())
    profiles = ['canopy','forked','tiered','leaning','spreading','young']
    for profile in profiles:
        p = 'data/reverie_dreams/worldgen/configured_feature/gensokyo/jungle_'+profile+'.json'
        assert p in selected, p
    definition = read(source/selected[0]); features = [s for layer in definition['features'] for s in layer]
    assert 'reverie_dreams:gensokyo/jungle_trees' in features
    assert all(not (s.startswith('minecraft:') and ('bamboo' in s or 'jungle' in s)) for s in features)
    palette = read(ROOT/'common/src/main/resources/data/reverie_dreams/gensokyo/map.json')['biome_palette']
    assert read(source/'data/reverie_dreams/dimension/gensokyo.json')['generator']['biome_source']['biomes'] == palette
    assert any(e['id']==biome for e in read(source/'data/reverie_dreams/gensokyo/biomes.json')['biomes'])
    lang = read(source/'assets/reverie_dreams/lang/zh_cn.json')
    assert lang['biome.reverie_dreams.gensokyo_jungle'] == '\u5e7b\u60f3\u4e61\u4e1b\u6797'
    selected = sorted(set(selected)); changes = []; review = []
    # Verify first, copy only after every ownership/concurrency check has passed.
    for rel in selected:
        src = source/rel; current = DEST/rel; original = BEFORE/rel
        assert src.is_file(), rel
        assert sha(current) in (sha(original), sha(src)), 'Concurrent generated edit: '+rel
        if current.exists():
            old, new = read(current), read(src)
            if rel.endswith('/zh_cn.json'):
                removed = sorted(set(old)-set(new))
                added = sorted(set(new)-set(old))
                modified = sorted(k for k in set(old)&set(new) if old[k] != new[k])
                review.append({'path':rel,'addedKeys':added,'removedKeys':removed,'modifiedKeys':modified})
                assert not removed and not modified, 'Preserve existing translations; backfill provider before copying'
        if sha(current) != sha(src): changes.append(rel)
    ownership = []
    for p in (source/'.cache').glob('*'):
        body = p.read_text(encoding='utf-8',errors='replace').replace('\\','/')
        owned = [rel for rel in selected if rel in body]
        if owned: ownership.append({'provider':body.splitlines()[0],'resources':owned})
    assert set(selected) <= {rel for owner in ownership for rel in owner['resources']}, 'Every copied resource needs a Fabric HashCache owner'
    for rel in changes:
        p = DEST/rel; p.parent.mkdir(parents=True, exist_ok=True); shutil.copy2(source/rel, p)
    for rel in selected: assert sha(DEST/rel) == sha(source/rel), rel
    report = {'status':'PASS','source':'actual Fabric runData output, copied verbatim','selected':selected,
              'changed':changes,'providerOwnership':ownership,'translationReview':review,
              'rawDataHandAuthored':False,'formalDistUpdated':False}
    (OUT/'datagen-verification.json').write_bytes((json.dumps(report,indent=2)+'\n').encode())
    print('Copied', len(changes), 'actual provider outputs; verified',len(selected),'owned resources')

if __name__ == '__main__': main()
