"""Terrain/road siting and a versioned editor checkpoint for two Gensokyo sites."""
import argparse
import base64
import gzip
import hashlib
import heapq
import importlib.util
import json
import math
import shutil
import struct
import urllib.request
import urllib.error
from pathlib import Path
from datetime import datetime

import cv2
import numpy as np
from scipy import ndimage as nd
from scipy.spatial import cKDTree

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT/'map_source/gensokyo'
OUT = ROOT/'data/shrine-mansion-20261119'
LOCAL = ROOT/'.agent/shrine-mansion-20261119'
WORK = LOCAL/'work'
FILES = ['map.json', 'heightmap.bin', 'surfaces.bin', 'biomes.bin', 'underground.json', '.editor-revision.json']
MANSION = {'center_xz': [4920, 7144], 'bounds_xz': [4810, 7046, 5031, 7243], 'ground_block_y': 1055,
           'anchor_xyz': [4810, 1004, 7046], 'size_xyz': [221, 165, 197], 'gate_xyz': [5030, 1055, 7144]}
SHRINE = {'center_xz': [11448, 4976], 'reservation_xz': [11368, 4876, 11528, 5076],
          'front_gate_xz': [11448, 5056], 'rear_region_xz': [11600, 6016], 'has_template': False}


def read(p): return json.loads(Path(p).read_text(encoding='utf-8-sig'))
def save(p, o):
    p=Path(p); p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(o, ensure_ascii=False, indent=2, allow_nan=False)+'\n', encoding='utf8')
def sha(p):
    h=hashlib.sha256()
    with Path(p).open('rb') as f:
        for b in iter(lambda:f.read(8<<20), b''): h.update(b)
    return h.hexdigest()
def snapshot(): return {n:sha(SOURCE/n) for n in FILES}
def layers(directory=SOURCE):
    m=read(directory/'map.json');shape=(m['depth'],m['width'])
    return m, np.memmap(directory/'heightmap.bin','<i2','r',shape=shape), np.memmap(directory/'surfaces.bin','<u2','r',shape=shape)
def decode_roads(meta):
    raw=gzip.decompress(base64.b64decode(meta['road_network']['data']))
    return {i:(y,lo,hi,role) for i,y,lo,hi,role in struct.iter_unpack('>ihhhB',raw[24:])}
def encode_roads(meta, roads):
    b=bytearray(struct.pack('>4siiiii', b'RDR1', meta['width'], meta['depth'], *meta['origin'], len(roads)))
    for i,q in sorted(roads.items()):b.extend(struct.pack('>ihhhB',i,*q))
    return {'version':1,'encoding':'gzip-base64-rdr1','columns':len(roads),'data':base64.b64encode(gzip.compress(b,mtime=0)).decode()}


def helpers():
    spec=importlib.util.spec_from_file_location('old_road_planner', ROOT/'tools/gensokyo-map/plan-road-construction-preview-20261107.py')
    module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
    return module


def plan():
    assert not (OUT/'publish.json').exists(),'This historical plan was published; use a new task output for a new proposal'
    OUT.mkdir(parents=True,exist_ok=True)
    previous=read(OUT/'plan.json') if (OUT/'plan.json').exists() else {}
    baseline=snapshot();m,h,s=layers(); old=decode_roads(m); helper=helpers();step=4;helper.STEP=step
    oldroutes=read(ROOT/'data/road-construction-preview-20261107/construction-preview.json')['roads']
    d03=np.array(next(r for r in oldroutes if r['id']=='D03')['centerline_xyz'])
    branch=d03[np.argmin(np.linalg.norm(d03[:,[0,2]]-[11518,5383],axis=1)),[0,2]].round().astype(int).tolist()
    definitions=[('M01','红魔馆院门接入 D06',[5160,7096],MANSION['gate_xyz'][::2],5),
                 ('H01','神社参道入口接入 D03',[11464,5056],SHRINE['front_gate_xz'],5),
                 ('H02','兽道分叉至博丽后山入口',branch,SHRINE['rear_region_xz'],3)]
    roadplans=[]; outputs={}
    for rid,name,start,end,width in definitions:
        margin=110
        x0=max(0,(min(start[0],end[0])-margin)//step*step);z0=max(0,(min(start[1],end[1])-margin)//step*step)
        x1=min(m['width'],(max(start[0],end[0])+margin)//step*step);z1=min(m['depth'],(max(start[1],end[1])+margin)//step*step)
        hh=np.array(h[z0:z1:step,x0:x1:step],float);ss=s[z0:z1:step,x0:x1:step]
        smooth=nd.gaussian_filter(hh,1);gz,gx=np.gradient(smooth,step)
        cost=1+10*(gx*gx+gz*gz)+(nd.maximum_filter(hh,3)-nd.minimum_filter(hh,3))*.1
        water_distance=nd.distance_transform_edt(ss!=0)*step;cost[water_distance<width/2+5]=np.inf
        cost[np.isin(ss,[5,7,11,12,14])]=np.inf
        # Preserve the complete authored mansion, except for its outside entrance.
        xx,zz=np.meshgrid(np.arange(x0,x1,step),np.arange(z0,z1,step))
        bx0,bz0,bx1,bz1=MANSION['bounds_xz'];cost[(xx<bx1-1)&(xx>=bx0)&(zz>=bz0)&(zz<bz1)]=np.inf
        a=np.rint((np.array(start)-[x0,z0])/step).astype(int);b=np.rint((np.array(end)-[x0,z0])/step).astype(int)
        # The exact endpoints are preserved after the coarse-grid search.
        cost[a[1],a[0]]=1;cost[b[1],b[0]]=1
        poly=helper.astar(a,b,cost,hh)*step+[x0,z0]
        poly=nd.gaussian_filter1d(poly.astype(float),1.2,axis=0);poly[0]=start;poly[-1]=end
        p=helper.resample(poly,.75);ij=np.rint(p).astype(int);terrain=np.array(h[ij[:,1],ij[:,0]],float)
        # Plan after the mansion foundation has been tied to its original ground plane.
        if rid=='M01':
            distance=np.hypot(np.maximum(bx0-ij[:,0],np.maximum(ij[:,0]-(bx1-1),0)),np.maximum(bz0-ij[:,1],np.maximum(ij[:,1]-(bz1-1),0)))
            w=np.clip(1-distance/40,0,1);w=w*w*(3-2*w);terrain=terrain*(1-w)+MANSION['ground_block_y']*w
        st=np.r_[0,np.cumsum(np.linalg.norm(np.diff(p,axis=0),axis=1))]
        fixed={0:float(h[start[1],start[0]]),len(p)-1:float(h[end[1],end[0]])}
        if rid=='M01':fixed[len(p)-1]=MANSION['ground_block_y']
        y=helper.profile(terrain,st,fixed,np.full(len(p)-1,1/3))
        # Rasterize full road width and then solve the 4-neighbor half-step constraints.
        mask=np.zeros((z1-z0,x1-x0),'uint8');cv2.polylines(mask,[(p-[x0,z0]).round().astype('int32')],False,1,width)
        rz,rx=np.where(mask);x,z=rx+x0,rz+z0;distance_to_line,nearest=cKDTree(p).query(np.column_stack((x,z)))
        inside=distance_to_line<=width/2
        x,z,nearest=x[inside],z[inside],nearest[inside]
        desired=np.rint(y[nearest]*2).astype(int)
        nodes={int(pz)*m['width']+int(px):int(value) for px,pz,value in zip(x,z,desired)}
        fixed_nodes={}
        for index in list(nodes):
            for neighbor in [index,index-1,index+1,index-m['width'],index+m['width']]:
                if neighbor in old and old[neighbor][3] in (1,2):
                    ox,oz=neighbor%m['width'],neighbor//m['width'];py=old[neighbor][0]
                    state=m['surface_palette'][int(s[oz,ox])]
                    value=py*2-(1 if 'slab[type=bottom' in state else 0)
                    fixed_nodes[neighbor]=value;nodes[neighbor]=value
        if rid=='M01':
            for index in nodes:
                px,pz=index%m['width'],index//m['width']
                if 5030<=px<=5033 and abs(pz-7144)<=2:
                    fixed_nodes[index]=MANSION['ground_block_y']*2
                    nodes[index]=MANSION['ground_block_y']*2
        def envelope(initial, sign):
            vals={i:sign*v for i,v in initial.items()};q=[(v,i) for i,v in vals.items()];heapq.heapify(q)
            while q:
                v,i=heapq.heappop(q)
                if v!=vals.get(i):continue
                for j in [i-1,i+1,i-m['width'],i+m['width']]:
                    if j in nodes and v+1<vals.get(j,10**9):vals[j]=v+1;heapq.heappush(q,(v+1,j))
            return {i:sign*v for i,v in vals.items()}
        low=envelope(fixed_nodes,-1);high=envelope(fixed_nodes,1)
        for i in nodes:nodes[i]=max(low.get(i,-10**8),min(high.get(i,10**8),nodes[i]))
        solved=envelope(nodes,1)
        assert all(solved[i]==v for i,v in fixed_nodes.items()), ('Conflicting existing road endpoints',rid,[(i,solved[i],v,old.get(i)) for i,v in fixed_nodes.items() if solved[i]!=v][:6])
        rows=[]
        for i,value in solved.items():
            if i in old and old[i][3] in (1,2,4):continue
            px,pz=i%m['width'],i//m['width'];assert s[pz,px]!=0
            if rid=='M01' and px<=MANSION['bounds_xz'][2]-1 and MANSION['bounds_xz'][0]<=px and bz0<=pz<bz1:continue
            rows.append([px,pz,value])
        array=np.array(rows,'int32');outputs[rid]=array
        cut=np.abs((array[:,2]+1)//2-h[array[:,1],array[:,0]])
        # Foundation transition contributes to M01; the exact combined earthwork is validated below.
        result={'id':rid,'name':name,'width':width,'start_xz':start,'end_xz':end,'length':round(float(st[-1]),1),
                'centerline_xyz':np.column_stack((p[:,0],y,p[:,1])).round(3).tolist(),'new_road_columns':len(rows),
                'raw_terrain_max_earthwork':int(cut.max()),'existing_road_columns_preserved':len(fixed_nodes)}
        roadplans.append(result)
    np.savez_compressed(OUT/'road-columns.npz',**outputs)
    bx0,bz0,bx1,bz1=MANSION['bounds_xz'];patch=np.array(h[bz0:bz1,bx0:bx1],int)
    assert not np.any(s[bz0:bz1,bx0:bx1]==0)
    result={'status':'PLANNED','source_revision':read(SOURCE/'.editor-revision.json')['revision'],'source_sha256':baseline,
            'mansion':MANSION,'shrine':{**SHRINE,'terrain_block_y':int(h[4976,11448]),'front_gate_block_y':int(h[5056,11448]),'rear_region_block_y':int(h[6016,11600])},
            'mansion_existing_ground_range':[int(patch.min()),int(patch.max())], 'mansion_max_cut':int(np.maximum(patch-1055,0).max()),
            'mansion_max_fill':int(np.maximum(1055-patch,0).max()),'roads':roadplans,'back_mountain_existing_road_reuse':'H01 → D03 existing mountain bend → H02',
            'template':'reverie_dreams:gensokyo/scarlet_devil_mansion','formal_map_written':False}
    if previous.get('source_sha256')==baseline and previous.get('backup'):result['backup']=previous['backup']
    save(OUT/'plan.json',result);assert snapshot()==baseline,'Concurrent map change; replan before publication'
    print(json.dumps({r['id']:{k:v for k,v in r.items() if k not in ['centerline_xyz','name']} for r in roadplans},ensure_ascii=False))


def prepare():
    assert not (OUT/'publish.json').exists(),'Never replay an already published checkpoint'
    p=read(OUT/'plan.json');assert snapshot()==p['source_sha256'],'Map changed; replan before prepare'
    assert not WORK.exists(),'Existing working checkpoint requires review'
    backup=Path(p['backup']) if p.get('backup') else ROOT/'.agent/backups'/('2026-11-19-before-shrine-mansion-rev'+str(p['source_revision'])+'-'+datetime.now().strftime('%H%M%S'))
    runtime=ROOT/'common/src/main/resources/data/reverie_dreams/gensokyo';rm=read(runtime/'map.json')
    files=[SOURCE/n for n in FILES]+[runtime/'map.json',runtime/'underground.json']+[runtime/n for n in rm['layers'].values()]
    if (backup/'manifest.json').exists():
        for entry in read(backup/'manifest.json')['files']:assert sha(entry['source'])==entry['sha256']==sha(entry['backup'])
    else:
        manifest=[]
        for file in files:
            dst=backup/file.relative_to(ROOT);dst.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(file,dst)
            digest=sha(file);assert sha(dst)==digest;manifest.append({'source':str(file),'backup':str(dst),'sha256':digest})
        save(backup/'manifest.json',{'files':manifest,'status':'VERIFIED'})
    WORK.mkdir(parents=True)
    for name in FILES:shutil.copy2(SOURCE/name,WORK/name)
    m,original,surface=layers();shape=original.shape
    h=np.memmap(WORK/'heightmap.bin','<i2','r+',shape=shape);s=np.memmap(WORK/'surfaces.bin','<u2','r+',shape=shape)
    x0,z0,x1,z1=MANSION['bounds_xz'];blend=40
    zz,xx=np.mgrid[z0-blend:z1+blend,x0-blend:x1+blend]
    distance=np.hypot(np.maximum(x0-xx,np.maximum(xx-(x1-1),0)),np.maximum(z0-zz,np.maximum(zz-(z1-1),0)))
    w=np.clip(1-distance/blend,0,1);w=w*w*(3-2*w)
    prior=np.array(original[zz,xx],float);target=np.rint(prior*(1-w)+1055*w).astype('int16')
    changed=(w>0)&(target!=prior);assert not np.any(surface[zz[changed],xx[changed]]==0),'Foundation must not reclaim water'
    h[zz[changed],xx[changed]]=target[changed]
    old=decode_roads(m);roads=dict(old);tables=dict(np.load(OUT/'road-columns.npz'))
    newcore={}
    path=m['surface_palette'].index('minecraft:dirt_path');slab=m['surface_palette'].index('minecraft:andesite_slab[type=bottom,waterlogged=false]')
    for rid,a in tables.items():
        for x,z,y2 in a:
            i=int(z)*m['width']+int(x);py=int((y2+1)//2)
            if i in newcore:assert newcore[i]==int(y2),'New road height conflict'
            assert i not in old or old[i][3]==3,'Preserve all existing road cores and bridge edges'
            assert abs(int(h[z,x])-py)<=6,('Excess road cut/fill',rid,x,z,int(h[z,x]),py)
            newcore[i]=int(y2);h[z,x]=py;s[z,x]=slab if y2%2 else path;roads[i]=(py,py+1,py+5,1)
    # Existing roads and all source/runtime biome work are protected.
    for i in old:
        if i not in newcore:
            x,z=i%m['width'],i//m['width'];assert h[z,x]==original[z,x] and s[z,x]==surface[z,x]
    ug=read(WORK/'underground.json')
    for q in ug['blocks']:
        x,y,z=q['pos'];i=z*m['width']+x
        if i in newcore and roads[i][1]<=y<=roads[i][2]:assert q['block'] in ['minecraft:air','minecraft:cave_air','minecraft:void_air'],('Authored obstruction',q)
        assert not (x0<=x<x1 and z0<=z<z1),'Mansion intersects an existing authored structure'
    m['anchors'].append({'id':'reverie_dreams:scarlet_devil_mansion','type':'template','template':p['template'],'pos':MANSION['anchor_xyz'],
                         'rotation':'NONE','mirror':'NONE','integrity':1.0,'scale':1.0})
    m['road_network']=encode_roads(m,roads);m['map_version']+=1;save(WORK/'map.json',m)
    h.flush();s.flush();assert sha(WORK/'biomes.bin')==p['source_sha256']['biomes.bin']
    # Exact collision tops, including each new/existing join, not just centerline slopes.
    def top(i):
        px,pz=i%m['width'],i//m['width'];py=roads[i][0]
        state=m['surface_palette'][int(s[pz,px])]
        return py*16+(8 if 'slab[type=bottom' in state else 15 if state=='minecraft:dirt_path' else 16)
    pairs=0;maximum=0
    for i in newcore:
        for j in [i-1,i+1,i-m['width'],i+m['width']]:
            if j in roads and roads[j][3] in (1,2):pairs+=1;maximum=max(maximum,abs(top(i)-top(j)))
    assert maximum<=9,('Road needs a jump',maximum/16)
    differences=[];height_changes=surface_changes=0
    for z in range(shape[0]):
        xs=np.where((h[z]!=original[z])|(s[z]!=surface[z]))[0]
        if len(xs):differences.extend((int(x),z) for x in xs)
        height_changes+=int(np.sum(h[z]!=original[z]));surface_changes+=int(np.sum(s[z]!=surface[z]))
        wet=surface[z]==0;assert np.array_equal(h[z][wet],original[z][wet]) and np.array_equal(s[z][wet],surface[z][wet])
    np.savez_compressed(OUT/'changed-columns.npz',xz=np.asarray(differences,'int32'))
    p.update({'status':'PREPARED','backup':str(backup),'work':str(WORK),'height_changes':height_changes,'surface_changes':surface_changes,
              'new_road_columns':len(newcore),'existing_core_columns_preserved':sum(q[3] in (1,2) for q in old.values()),
              'old_verge_columns_joined':sum(i in old for i in newcore),'collision_adjacent_pairs':pairs,
              'max_collision_step':maximum/16,'work_sha256':{n:sha(WORK/n) for n in FILES}})
    save(OUT/'plan.json',p);assert snapshot()==p['source_sha256']
    for name in ['heightmap','surfaces','biomes']:
        with (WORK/(name+'.bin')).open('rb') as a,gzip.GzipFile(filename=str(WORK/(name+'.bin.gz')),mode='wb',compresslevel=1,mtime=0) as b:shutil.copyfileobj(a,b,8<<20)
    print('PREPARED',height_changes,surface_changes,len(newcore),'step',maximum/16,'backup',backup)


def api(route,data=None,method='GET',headers=None):
    headers={'X-Reverie-Task':'shrine-mansion-20261119',**(headers or {})}
    if data is not None and not isinstance(data,bytes):data=json.dumps(data).encode('utf8');headers['Content-Type']='application/json'
    req=urllib.request.Request('http://127.0.0.1:3210'+route,data=data,method=method,headers=headers)
    try:
        with urllib.request.urlopen(req,timeout=300) as response:return json.load(response)
    except urllib.error.HTTPError as error:raise RuntimeError(error.read().decode('utf8')) from error


def publish():
    p=read(OUT/'plan.json');assert not (OUT/'publish.json').exists(),'Do not replay a committed checkpoint'
    assert read(OUT/'template-validation.json')['status']=='PASS' and read(OUT/'native-results.json')['status']=='PASS'
    assert snapshot()==p['source_sha256'],'Concurrent source change; rebase explicitly'
    assert all(sha(WORK/n)==v for n,v in p['work_sha256'].items()),'Work changed after validation'
    live=api('/api/map');assert live['revision']==live['liveRevision']==p['source_revision']
    assert not api('/api/collab/users')['users'],'An editor user is active'
    token=api('/api/save/begin',{'baseRevision':p['source_revision'],'clientId':None},'POST')['token'];committed=False
    try:
        headers={'X-Save-Token':token};api('/api/map',read(WORK/'map.json'),'PUT',headers)
        for layer in ['heightmap','surfaces','biomes']:
            api('/api/layers/'+layer,(WORK/(layer+'.bin.gz')).read_bytes(),'PUT',{**headers,'Content-Type':'application/octet-stream','Content-Encoding':'gzip'})
        api('/api/underground',read(WORK/'underground.json'),'PUT',headers)
        commit=api('/api/save/commit',{'token':token},'POST');committed=True
        save(OUT/'publish.json',{'status':'COMMITTED_PENDING_SYNC','commit':commit})
    finally:
        if not committed:api('/api/save/abort',{'token':token},'POST')
    finish_sync()


def finish_sync():
    pub=read(OUT/'publish.json');assert api('/api/map')['revision']==pub['commit']['revision']
    pub['sync']=api('/api/sync',{},'POST');pub['status']='PUBLISHED';save(OUT/'publish.json',pub)
    verify()


def verify():
    p=read(OUT/'plan.json');m,h,s=layers();before=Path(p['backup'])/'map_source/gensokyo';old,oh,os=layers(before)
    expected=set(map(tuple,np.load(OUT/'changed-columns.npz')['xz'].tolist()));actual=set()
    for z in range(m['depth']):
        xs=np.where((h[z]!=oh[z])|(s[z]!=os[z]))[0];actual.update((int(x),z) for x in xs)
        water=os[z]==0;assert np.array_equal(h[z][water],oh[z][water]) and np.array_equal(s[z][water],os[z][water])
    assert actual==expected
    assert m['biome_palette']==old['biome_palette'] and sha(SOURCE/'biomes.bin')==p['source_sha256']['biomes.bin']
    assert sha(SOURCE/'underground.json')==p['source_sha256']['underground.json']
    assert m['surface_palette']==old['surface_palette']
    for name in ['heightmap.bin','surfaces.bin','biomes.bin','underground.json']:assert sha(SOURCE/name)==p['work_sha256'][name]
    # The standard editor serializes equivalent metadata with its own whitespace/number formatting.
    if (WORK/'map.json').exists():assert read(SOURCE/'map.json')==read(WORK/'map.json')
    else:assert sha(SOURCE/'map.json')==p['published_map_sha256']
    roads=decode_roads(m);prior=decode_roads(old);tables=dict(np.load(OUT/'road-columns.npz'));touch={}
    for name,a in tables.items():
        ids=set((a[:,1]*m['width']+a[:,0]).tolist())
        touch[name]=sum(any(j in prior and prior[j][3] in (1,2) for j in [i-1,i+1,i-m['width'],i+m['width']]) for i in ids)
        assert touch[name]>0,('New paving does not touch existing road core',name)
    # Every original core is bit-identical; only 23 verge columns became intersection paving.
    for i,q in prior.items():
        if q[3] in (1,2,4):assert roads[i]==q
    assert all(sha(e['backup'])==e['sha256'] for e in read(Path(p['backup'])/'manifest.json')['files'])
    runtime=ROOT/'common/src/main/resources/data/reverie_dreams/gensokyo';rm=read(runtime/'map.json')
    assert {k:v for k,v in rm.items() if k!='layers'}=={k:v for k,v in m.items() if k!='layers'}
    assert sha(runtime/'underground.json')==sha(SOURCE/'underground.json')
    for layer in ['heightmap','surfaces','biomes']:
        filename=rm['layers'][layer]
        with gzip.open(runtime/filename,'rb') as stream:
            if filename.endswith('.delta.gz'):
                row=np.zeros(m['width'],'int32')
                for z in range(m['depth']):row+=np.frombuffer(stream.read(m['width']*2),'<i2').astype('int32');assert np.array_equal(row,h[z])
            else:
                digest=hashlib.sha256()
                for chunk in iter(lambda:stream.read(8<<20),b''):digest.update(chunk)
                assert digest.hexdigest()==sha(SOURCE/(layer+'.bin'))
            assert not stream.read(1)
    report={'status':'PASS','revision':read(SOURCE/'.editor-revision.json')['revision'],'source_runtime_bit_exact':True,
            'backup_sha256_verified':True,'actual_changed_columns':len(actual),'biomes_palette_and_underground_preserved':True,
            'all_original_road_cores_preserved':True,'new_roads_touch_existing_core_columns':touch,'former_verges_paved_at_junctions':p['old_verge_columns_joined'],
            'new_road_columns':p['new_road_columns'],'max_collision_step':p['max_collision_step'],'water_changes':0,
            'shrine_structure_placed':False,'mansion_anchor':m['anchors'][-1],'native_checks':len(read(OUT/'native-results.json')['checks']),
            'formal_dist_updated':False,'real_user_saves_modified':False}
    save(OUT/'validation.json',report);print(json.dumps(report,ensure_ascii=False))


if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('action',choices=['plan','prepare','publish','finish-sync','verify']);args=parser.parse_args();globals()[args.action.replace('-','_')]()
