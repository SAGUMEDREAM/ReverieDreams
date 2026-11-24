"""Author reviewed, anchor-local adventure inputs from the unmodified mansion source.

This writes a provider INPUT, never a generated game resource. Flood fills are only
room candidates: counts, doors, standing clearance and the plan PNG must be reviewed.
"""
from pathlib import Path
import sys,json,collections
import numpy as np
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'tools/gensokyo-buildings'))
from survey import load_litematic

def survey():
    raw,origin,a,p,stats=load_litematic(Path('E:/Download/红魔馆.litematic'))
    air=np.isin(a,[i for i,s in enumerate(p)if s['Name'] in ('minecraft:air','minecraft:cave_air','minecraft:void_air')])
    lower=[i for i,s in enumerate(p)if s['Name']=='minecraft:iron_door' and s.get('Properties',{}).get('half')=='lower']
    rooms=[];all_regions=[]
    for y in sorted(set(map(int,np.argwhere(np.isin(a[:51],lower))[:,0]))):
        mask=air[y]&air[y+1]&~air[y-1];labels=np.zeros(mask.shape,'int32');regions={}
        for z,x in np.argwhere(mask):
            if labels[z,x]:continue
            label=len(regions)+1;q=collections.deque([(int(x),int(z))]);labels[z,x]=label;cells=[]
            while q:
                xx,zz=q.popleft();cells.append((xx,zz))
                for nx,nz in [(xx-1,zz),(xx+1,zz),(xx,zz-1),(xx,zz+1)]:
                    if 0<=nx<a.shape[2] and 0<=nz<a.shape[1] and mask[nz,nx] and labels[nz,nx]==0:
                        labels[nz,nx]=label;q.append((nx,nz))
            regions[label]={'cells':cells,'doors':[]}
        for z,x in np.argwhere(np.isin(a[y],lower)):
            for nx,nz in [(x-1,z),(x+1,z),(x,z-1),(x,z+1)]:
                lab=int(labels[nz,nx])
                if lab and [int(x),y,int(z)] not in regions[lab]['doors']:regions[lab]['doors'].append([int(x),y,int(z)])
        for reg in regions.values():
            if reg['doors']:all_regions.append({'y':y+1004,'area':len(reg['cells']),'doors':len(reg['doors'])})
            # Cells in this asset are compact rooms with one double iron doorway.
            if not(10<=len(reg['cells'])<=180 and 1<=len(reg['doors'])<=2):
                if len(reg['cells'])==648 and len(reg['doors'])==16:
                    # Eight iron-door bays in the large outer holding gallery.
                    doors=sorted(reg['doors']);pairs=[doors[i:i+2]for i in range(0,len(doors),2)]
                    for pair in pairs:
                        mx=sum(q[0]for q in pair)/2;mz=sum(q[2]for q in pair)/2
                        x,z=min((q for q in reg['cells'] if 9<=(q[0]-mx)**2+(q[1]-mz)**2<=25),key=lambda q:(q[0]-mx)**2+(q[1]-mz)**2)
                        rooms.append({'id':f'cell_{y}_{x}_{z}','kind':'goblin','pos':[x,y,z],'bounds':[min(xx for xx,zz in reg['cells']),y,min(zz for xx,zz in reg['cells']),max(xx for xx,zz in reg['cells']),y+4,max(zz for xx,zz in reg['cells'])],'doors':pair,'area':81})
                continue
            cells=reg['cells'];cx=sum(x for x,z in cells)/len(cells);cz=sum(z for x,z in cells)/len(cells)
            x,z=min(cells,key=lambda q:(q[0]-cx)**2+(q[1]-cz)**2)
            bounds=[min(x for x,z in cells),y,min(z for x,z in cells),max(x for x,z in cells),y+4,max(z for x,z in cells)]
            rooms.append({'id':f'cell_{y}_{x}_{z}','kind':'goblin','pos':[x,y,z],'bounds':bounds,'doors':reg['doors'],'area':len(cells)})
    rooms.sort(key=lambda r:(r['pos'][1],r['pos'][2],r['pos'][0]))
    for i,r in enumerate(rooms):
        r['friendly']=i%4==3
        x0,y,z0,x1,_,z1=r['bounds'];barriers=[]
        for z in range(max(0,z0-1),min(a.shape[1],z1+2)):
            for x in range(max(0,x0-1),min(a.shape[2],x1+2)):
                if x not in (x0-1,x1+1) and z not in (z0-1,z1+1):continue
                n=p[a[y,z,x]]['Name']
                if any(t in n for t in ['stone','brick','quartz','iron_bars']):barriers.append({'pos':[x,y,z],'block':n})
        r['barriers']=barriers
    print('ROOM_CANDIDATES',len(rooms),'FLOORS',dict(collections.Counter(r['pos'][1]+1004 for r in rooms)))
    print('AREAS',collections.Counter(r['area']for r in rooms));print('DOOR_REGIONS',collections.Counter((r['y'],r['area'],r['doors'])for r in all_regions))
    return raw,origin,a,p,air,rooms

if __name__=='__main__':
    raw,origin,a,p,air,rooms=survey()
    out=ROOT/'.agent/scarlet-adventure-20261119/room-candidates.json';out.write_text(json.dumps(rooms,indent=2),encoding='utf-8')
    candidates=[]
    for y,z,x in np.argwhere(np.isin(a,[i for i,s in enumerate(p)if s['Name'] in ['minecraft:chest','minecraft:barrel']])):
        y,z,x=map(int,(y,z,x));s=p[a[y,z,x]]
        if s['Name']=='minecraft:chest' and (s.get('Properties',{}).get('type')!='single' or not air[y+1,z,x]):continue
        for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
            if air[y,z+dz,x+dx] and air[y+1,z+dz,x+dx] and not air[y-1,z+dz,x+dx]:
                candidates.append({'pos':[x,y,z],'approach':[x+dx,y,z+dz],'block':s['Name']});break
    # Spread authored containers over distinct floor/wing routes; no new obstruction blocks.
    chosen=[]
    pool=[q for q in candidates if q['pos'][1]>=35]
    chosen.append(min(pool,key=lambda q:(q['pos'][0]-210)**2+(q['pos'][2]-98)**2+(q['pos'][1]-52)**2))
    while len(chosen)<18:
        next_point=max((q for q in pool if q not in chosen),key=lambda q:min((q['pos'][0]-v['pos'][0])**2+(q['pos'][2]-v['pos'][2])**2+4*(q['pos'][1]-v['pos'][1])**2 for v in chosen))
        chosen.append(next_point)
    chosen=sorted(chosen[:15],key=lambda q:(q['pos'][1],q['pos'][0]))
    entry=min(([x,52,z]for x in range(201,216)for z in range(104,114)if air[52,z,x]and air[53,z,x]and not air[51,z,x]),key=lambda q:(q[0]-209)**2+(q[2]-108)**2)
    approach=next([entry[0]+dx,52,entry[2]+dz]for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]if air[52,entry[2]+dz,entry[0]+dx]and air[53,entry[2]+dz,entry[0]+dx]and not air[51,entry[2]+dz,entry[0]+dx])
    chosen.insert(0,{'pos':entry,'approach':approach,'block':'minecraft:chest','new':True})
    for floor in [18,4]:
        cell=max((q for q in rooms if q['pos'][1]==floor and not q['friendly']),key=lambda q:q['area']);x0,y,z0,x1,_,z1=cell['bounds'];x,z=next((x,z)for z in range(z0+1,z1)for x in range(x0+1,x1)if air[y,z,x]and air[y+1,z,x]and not air[y-1,z,x]and abs(x-cell['pos'][0])+abs(z-cell['pos'][2])>2)
        chosen.append({'pos':[x,y,z],'approach':[x+1,y,z],'block':'minecraft:chest','new':True})
    chests=[];maid=[]
    for i,q in enumerate(chosen):
        tier='supply' if i<8 else 'guard' if i<13 or i==16 else 'secret' if i<17 else 'heart'
        chests.append({'id':f'{tier}_{i+1:02}','tier':tier,**q})
        if i<14:
            x,y,z=q['approach'];maid.append({'id':f'maid_{i+1:02}','kind':'maid','pos':[x,y,z],'bounds':[x-5,y-1,z-5,x+5,y+5,z+5],'doors':[],'friendly':False})
    stands=[]
    for reg in raw['Regions'].values():
        offset=np.array([reg['Position'][k]for k in ['x','y','z']])-origin
        for e in reg.get('Entities',[]):
            if e.get('id')=='minecraft:armor_stand':stands.append([float(e['Pos'][i])+int(offset[i])for i in range(3)])
    result={'version':1,'template':'reverie_dreams:gensokyo/scarlet_devil_mansion','origin':[4810,1004,7046],'treasures':chests,'encounters':rooms+maid,'armor_stands':stands}
    dest=ROOT/'.agent/workspaces/scarlet-survey/scarlet_adventure.json';dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('AUTHORED_LAYOUT',len(rooms),'cells',len(chests),'treasures',len(maid),'guards',len(stands),'stands')
