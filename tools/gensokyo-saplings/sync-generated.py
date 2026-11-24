"""Validate actual Fabric provider output and copy only this sapling series verbatim."""
from pathlib import Path
import hashlib, json, shutil, sys, zipfile

ROOT = Path(__file__).resolve().parents[2]
DEST = ROOT/'common/src/generated/resources'
BEFORE = ROOT/'.agent/gensokyo-saplings-20261119/generated-before'
OUT = ROOT/'build/gensokyo-saplings-review'
TYPES = {'forest':'oak', 'birch_forest':'birch', 'taiga':'spruce', 'cherry_grove':'cherry',
         'dark_forest':'dark_oak', 'magic_forest':'pale_oak', 'maple_forest':'cherry', 'jungle':'jungle'}

def read(p): return json.loads(p.read_text(encoding='utf-8-sig'))
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest() if p.exists() else None

def main():
    source = Path(sys.argv[1]); selected = ['assets/reverie_dreams/lang/zh_cn.json',
        'data/minecraft/tags/block/saplings.json','data/minecraft/tags/item/saplings.json',
        'data/reverie_dreams/gensokyo/blocks.json','data/reverie_dreams/gensokyo/block_states.json']
    native = zipfile.ZipFile(ROOT/'common/build/moddev/artifacts/vanilla-26.1.2-1.jar')
    references = []
    for theme, material in TYPES.items():
        path = 'gensokyo_'+theme+'_sapling'; block_id = 'reverie_dreams:'+path
        states = 'assets/reverie_dreams/blockstates/'+path+'.json'
        item = 'assets/reverie_dreams/items/'+path+'.json'
        loot = 'data/reverie_dreams/loot_table/blocks/'+path+'.json'
        selected += [states,item,loot]
        b = read(source/states); model = b['variants']['']['model']
        assert model == 'minecraft:block/'+material+'_sapling'
        item_model = read(source/item)['model']
        assert item_model['type'] == 'minecraft:model' and item_model['model'] == 'minecraft:item/'+material+'_sapling'
        for key in [model,item_model['model']]: assert 'assets/minecraft/models/'+key.split(':')[1]+'.json' in native.namelist()
        for kind in ['block','item']:
            tag = read(source/('data/minecraft/tags/'+kind+'/saplings.json'))
            assert block_id in tag['values'], (path,kind)
        table = read(source/loot)
        assert table['pools'][0]['entries'][0]['name'] == block_id
        assert 'block.reverie_dreams.'+path in read(source/'assets/reverie_dreams/lang/zh_cn.json')
        assert not (source/'assets/reverie_dreams/models/block'/(path+'.json')).exists(), 'Use vanilla models directly'
        assert not (source/'assets/reverie_dreams/models/item'/(path+'.json')).exists()
        references.append({'block':block_id,'pool':'reverie_dreams:gensokyo/'+theme+'_trees','blockModel':model,
                           'itemModel':item_model['model'],'texture':'minecraft:block/'+material+'_sapling'})
    native.close()
    changes = []; translations = None
    for rel in selected:
        src, current, baseline = source/rel, DEST/rel, BEFORE/rel
        assert src.is_file(), rel
        assert sha(current) in (sha(baseline),sha(src)), 'Concurrent generated edit: '+rel
        if rel.endswith('/zh_cn.json'):
            old, new = read(current), read(src)
            removed = sorted(set(old)-set(new)); modified = sorted(k for k in set(old)&set(new) if old[k]!=new[k])
            assert not removed and not modified, ('Do not overwrite existing translations',removed,modified)
            translations = {'added':sorted(set(new)-set(old)),'removed':removed,'modified':modified}
        if sha(current) != sha(src): changes.append(rel)
    owners = []
    for p in (source/'.cache').glob('*'):
        text = p.read_text(encoding='utf-8',errors='replace').replace('\\','/')
        owned = [rel for rel in selected if rel in text]
        if owned: owners.append({'provider':text.splitlines()[0],'resources':owned})
    assert set(selected) <= {rel for owner in owners for rel in owner['resources']}
    for rel in changes:
        dst = DEST/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(source/rel,dst)
    for rel in selected: assert sha(DEST/rel)==sha(source/rel), rel
    report = {'status':'PASS','source':'actual isolated Fabric datagen, copied verbatim','selected':selected,'changed':changes,
              'providerOwnership':owners,'references':references,'translations':translations,'newTexturesOrModels':False,'formalDistUpdated':False}
    (OUT/'datagen-verification.json').write_bytes((json.dumps(report,indent=2)+'\n').encode())
    print('PASS:',len(TYPES),'saplings;',len(changes),'actual provider outputs; all vanilla model references resolve')

if __name__ == '__main__': main()
