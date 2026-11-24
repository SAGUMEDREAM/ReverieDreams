"""Independent geometry/coverage checks for the authored adventure point catalogue."""
from pathlib import Path
import sys,json,collections
import numpy as np
ROOT=Path(__file__).resolve().parents[2];sys.path.insert(0,str(ROOT/'tools/gensokyo-buildings'))
from survey import load_litematic
r,lo,a,p,stats=load_litematic(Path('E:/Download/红魔馆.litematic'))
layout=json.loads((ROOT/'common/src/generated/resources/data/reverie_dreams/gensokyo_adventure/scarlet_devil_mansion.json').read_text(encoding='utf-8'))
generated=json.loads((ROOT/'common/src/generated/resources/data/reverie_dreams/gensokyo_adventure/scarlet_devil_mansion.json').read_text(encoding='utf-8'))
if '--source-only' not in sys.argv:assert generated==layout
def block(q):x,y,z=q;return p[a[y,z,x]]['Name']
def air(q):return block(q)in ['minecraft:air','minecraft:cave_air','minecraft:void_air']
seen=set();doors=[];by_floor=collections.Counter()
for q in layout['encounters']:
 assert q['id']not in seen;seen.add(q['id']);x,y,z=q['pos'];assert air([x,y,z])and air([x,y+1,z])and not air([x,y-1,z]),q
 if q['kind']=='goblin':
  by_floor[y+1004]+=1
  for d in q['doors']:assert block(d)=='minecraft:iron_door';doors.append(tuple(d))
  for b in q['barriers']:assert block(b['pos'])==b['block']
for q in layout['treasures']:
 x,y,z=q['pos'];assert q['id']not in seen;seen.add(q['id'])
 if q.get('new'):assert air(q['pos'])and air([x,y+1,z])and not air([x,y-1,z])
 else:assert block(q['pos'])==q['block']
 assert air(q['approach']),q['id']
iron_ids=[i for i,s in enumerate(p)if s['Name']=='minecraft:iron_door'and s.get('Properties',{}).get('half')=='lower']
all_doors={tuple(map(int,(x,y,z)))for y,z,x in np.argwhere(np.isin(a[:51],iron_ids))}
assert len(doors)==len(set(doors));assert set(doors)==all_doors,('Prison doorway coverage',len(doors),len(all_doors))
assert collections.Counter(q['tier']for q in layout['treasures'])=={'supply':8,'guard':6,'secret':3,'heart':1,'library':1}
library=next(q for q in layout['treasures'] if q['tier']=='library')
x,y,z=library['pos']; ax,ay,az=library['approach']
assert library['new'] and library['block']=='minecraft:chest'
assert y==ay and abs(x-ax)+abs(z-az)==1 and air([ax,ay+1,az]) and not air([ax,ay-1,az])
assert sum(block([x+dx,y,z+dz])=='minecraft:bookshelf' for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)))>=2
maids=[q for q in layout['encounters'] if q['kind']=='maid']
assert len({tuple(q['pos']) for q in maids})==len(maids)
report={'status':'PASS','providerOutputCompared':'--source-only' not in sys.argv,'cells':197,'doorBlocksCovered':len(doors),'floors':dict(by_floor),'friendly':sum(q['friendly']for q in layout['encounters']),'treasures':len(layout['treasures']),'libraryChest':list(map(sum,zip(library['pos'],layout['origin']))),'newContainers':sum(q.get('new',False)for q in layout['treasures']),'maidPoints':len(maids),'maidFloors':collections.Counter(q['pos'][1]+layout['origin'][1] for q in maids)}
output=next((Path(arg) for arg in sys.argv[1:] if not arg.startswith('--')),ROOT/'data/scarlet-adventure-20261119/layout-validation.json')
output.parent.mkdir(parents=True,exist_ok=True);output.write_text(json.dumps(report,indent=2),encoding='utf-8');print(report)
