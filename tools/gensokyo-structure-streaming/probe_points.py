"""First-visible and stairwell fixtures from the untouched authored source."""
import sys,json
from pathlib import Path
import numpy as np
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'tools/gensokyo-buildings'))
from survey import load_litematic,unpack_region
r,lo,a,p,stats=load_litematic(Path('E:/Download/红魔馆.litematic'))
selected=np.zeros(a.shape,bool)
for region in r['Regions'].values():
 origin,ar,_=unpack_region(region);x,y,z=origin-lo;selected[y:y+ar.shape[0],z:z+ar.shape[1],x:x+ar.shape[2]]=True
air=np.isin(a,[i for i,s in enumerate(p) if s['Name'] in ['minecraft:air','minecraft:cave_air','minecraft:void_air']])&selected
solid=np.isin(a,[i for i,s in enumerate(p) if s['Name'] in ['minecraft:quartz_bricks','minecraft:deepslate_bricks','minecraft:red_terracotta','minecraft:polished_blackstone']])
chunks={}
for cx in range(4810>>4,(5030>>4)+1):
 for cz in range(7046>>4,(7242>>4)+1):
  x0=max(0,(cx<<4)-4810);x1=min(221,((cx+1)<<4)-4810);z0=max(0,(cz<<4)-7046);z1=min(197,((cz+1)<<4)-7046);points=[]
  for kind,mask in [('air',air),('solid',solid)]:
   coordinates=np.argwhere(mask[:,z0:z1,x0:x1]);
   if len(coordinates):
    for i in np.linspace(0,len(coordinates)-1,6,dtype=int):
     y,z,x=map(int,coordinates[i]);x+=x0;z+=z0;points.append({'pos':[4810+x,1004+y,7046+z],'block':'air' if kind=='air' else p[a[y,z,x]]['Name']})
  chunks[str(cx)+','+str(cz)]=points
stair=None
for x in [4880,4879,4881,4878]:
 for z in [7113,7112,7114]:
  if air[40,z-7046,x-4810] and stair is None:stair={'pos':[x,1044,z],'block':'air'}
assert stair
attached=[]
for name in ['wall_torch','wall_sign','wall_hanging_sign','ladder','wall_banner','wall_head','wall_skull','tripwire_hook','stone_button','lever']:
 ids=[i for i,state in enumerate(p) if state['Name'].endswith(':'+name) or state['Name'].endswith('_'+name)]
 if not ids:continue
 for y,z,x in np.argwhere(np.isin(a[:,54:159,64:149],ids)):
  x,z=int(x)+64,int(z)+54;y=int(y)
  attached.append({'pos':[4810+x,1004+y,7046+z],'block':p[a[y,z,x]]['Name']})
out=ROOT/'data/structure-streaming-20261119';out.mkdir(exist_ok=True)
(out/'probe-points.json').write_text(json.dumps({'chunks':chunks,'stair':stair,'attachedBlocks':attached,'source_regions':stats},ensure_ascii=False,indent=2),encoding='utf8')
print('FIRST_VISIBLE_FIXTURES',len(chunks),'STAIR',stair)
