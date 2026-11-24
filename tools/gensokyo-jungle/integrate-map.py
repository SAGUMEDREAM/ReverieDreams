"""Publish the authored map metadata via the editor, never write provider-owned JSON.

The three vanilla jungle palette entries are audited. The currently populated
bamboo_jungle slot becomes gensokyo_jungle; raw layer indices stay unchanged.
"""
from pathlib import Path
import copy, gzip, hashlib, json, shutil, urllib.request

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'map_source/gensokyo'
RUNTIME = ROOT / 'common/src/main/resources/data/reverie_dreams/gensokyo'
BACKUP = ROOT / '.agent/gensokyo-jungle-20261119/map-before'
OUT = ROOT / 'build/gensokyo-jungle-review'
TARGET = 'reverie_dreams:gensokyo_jungle'
VANILLA = {'minecraft:jungle', 'minecraft:sparse_jungle', 'minecraft:bamboo_jungle'}

def read(p): return json.loads(p.read_text(encoding='utf-8-sig'))
def save(p, v):
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_bytes((json.dumps(v, ensure_ascii=False, indent=2) + '\n').encode('utf-8'))
def sha(p):
    h = hashlib.sha256()
    with p.open('rb') as f:
        for chunk in iter(lambda: f.read(8 << 20), b''): h.update(chunk)
    return h.hexdigest()
def snapshot():
    return {n: sha(SOURCE/n) for n in ['map.json', 'heightmap.bin', 'surfaces.bin', 'biomes.bin', 'underground.json', '.editor-revision.json']}
def api(route, data=None, method='GET', headers=None):
    headers = {'X-Reverie-Task': 'gensokyo-jungle-20261119', **(headers or {})}
    if data is not None and not isinstance(data, bytes):
        data = json.dumps(data, ensure_ascii=False).encode(); headers['Content-Type'] = 'application/json'
    req = urllib.request.Request('http://127.0.0.1:3210'+route, data=data, method=method, headers=headers)
    with urllib.request.urlopen(req, timeout=300) as response: v = json.load(response)
    assert v.get('ok') is not False, v
    return v

def main():
    assert not (OUT/'map-verification.json').exists(), 'Already published; do not replay'
    before = snapshot(); meta = read(SOURCE/'map.json'); runtime = read(RUNTIME/'map.json')
    assert meta['biome_palette'] == runtime['biome_palette'] and TARGET not in meta['biome_palette']
    layer = (SOURCE/'biomes.bin').read_bytes()
    counts = {name: layer.count(i) for i, name in enumerate(meta['biome_palette']) if name in VANILLA}
    populated = [name for name, count in counts.items() if count]
    assert populated == ['minecraft:bamboo_jungle'], 'Map changed: replan a multi-index migration before writing'
    index = meta['biome_palette'].index(populated[0]); changed = counts[populated[0]]
    palette = copy.deepcopy(meta['biome_palette']); palette[index] = TARGET
    state = api('/api/map'); revision = read(SOURCE/'.editor-revision.json')['revision']
    assert state['revision'] == state['liveRevision'] == revision and not api('/api/collab/users')['users']
    BACKUP.mkdir(parents=True, exist_ok=False)
    for p in [SOURCE/'map.json', SOURCE/'.editor-revision.json', RUNTIME/'map.json', RUNTIME/'underground.json',
              *[RUNTIME/v for v in runtime['layers'].values()]]:
        d = BACKUP/p.relative_to(ROOT); d.parent.mkdir(parents=True, exist_ok=True); shutil.copy2(p, d)
    plan = {'revisionBefore': revision, 'sourceSHA256Before': before, 'vanillaJungleColumnsBefore': counts,
            'target': TARGET, 'replacedPaletteIndex': index, 'changedBiomeColumns': changed,
            'strategy': 'Replace the populated authored palette slot; all raw indices, terrain, roads, underground and lost bamboo forest stay identical'}
    save(OUT/'map-plan.json', plan)
    edited = copy.deepcopy(meta); edited['biome_palette'] = palette
    assert snapshot() == before, 'Concurrent map edit before publication'
    token = api('/api/save/begin', {'baseRevision': revision, 'clientId': 'gensokyo-jungle-20261119'}, 'POST')['token']
    committed = False
    try:
        api('/api/map', edited, 'PUT', {'X-Save-Token': token})
        for name in ['heightmap', 'surfaces', 'biomes']:
            api('/api/layers/'+name, gzip.compress((SOURCE/(name+'.bin')).read_bytes(), compresslevel=1, mtime=0),
                'PUT', {'X-Save-Token': token, 'Content-Type': 'application/octet-stream', 'Content-Encoding': 'gzip'})
        api('/api/underground', read(SOURCE/'underground.json'), 'PUT', {'X-Save-Token': token})
        assert snapshot() == before, 'Concurrent source edit during publication'
        commit = api('/api/save/commit', {'token': token}, 'POST'); committed = True
        save(OUT/'map-commit.json', commit)
    finally:
        if not committed: api('/api/save/abort', {'token': token}, 'POST')
    sync = api('/api/sync', {}, 'POST'); save(OUT/'map-sync.json', sync)
    final = read(SOURCE/'map.json'); finalRuntime = read(RUNTIME/'map.json')
    assert final['biome_palette'] == finalRuntime['biome_palette'] == palette
    assert {k:v for k,v in final.items() if k != 'biome_palette'} == {k:v for k,v in meta.items() if k != 'biome_palette'}
    for n in ['heightmap.bin', 'surfaces.bin', 'biomes.bin', 'underground.json']: assert sha(SOURCE/n) == before[n], n
    assert gzip.decompress((RUNTIME/finalRuntime['layers']['biomes']).read_bytes()) == layer
    assert finalRuntime['layers'] == runtime['layers']
    for n in ['heightmap', 'surfaces']:
        p = RUNTIME/runtime['layers'][n]; old = BACKUP/p.relative_to(ROOT)
        assert gzip.decompress(p.read_bytes()) == gzip.decompress(old.read_bytes()), n
    assert read(RUNTIME/'underground.json') == read(SOURCE/'underground.json'), 'Underground runtime/source mismatch'
    verify = {**plan, 'status':'PASS', 'revisionAfter': read(SOURCE/'.editor-revision.json')['revision'],
              'vanillaJungleColumnsAfter': {name: layer.count(i) for i,name in enumerate(palette) if name in VANILLA},
              'rawLayersByteIdentical': True, 'heightSurfaceUndergroundRoadsUnchanged': True,
              'lostBambooColumnsUnchanged': layer.count(palette.index('reverie_dreams:gensokyo_bamboo_forest')),
              'runtimeBiomeBytesMatchSource': True, 'dimensionSource':'RDBuiltInDimensions: actual Fabric datagen follows publication',
              'userSaveModified': False, 'formalDistUpdated': False}
    save(OUT/'map-verification.json', verify)
    print('PASS:', changed, 'columns now resolve to', TARGET, '; revision', verify['revisionAfter'])

if __name__ == '__main__': main()
