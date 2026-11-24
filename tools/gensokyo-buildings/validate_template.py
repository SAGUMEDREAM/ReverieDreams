"""Independent streaming comparison of Fabric datagen NBT against the original asset."""
import hashlib
import json
from collections import deque
from pathlib import Path
import numpy as np
from survey import load_litematic, read_nbt

ROOT=Path(__file__).resolve().parents[2]
SOURCE=Path('E:/Download/红魔馆.litematic')
TEMPLATE=ROOT/'common/src/main/resources/data/reverie_dreams/structure/gensokyo/scarlet_devil_mansion.nbt'
METADATA=ROOT/'common/src/generated/resources/data/reverie_dreams/gensokyo_structure_metadata/gensokyo/scarlet_devil_mansion.json'
OUT=ROOT/'data/shrine-mansion-20261119'

def canonical(value):
    if isinstance(value,dict):return {k:canonical(v) for k,v in value.items()}
    if isinstance(value,list):return [canonical(v) for v in value]
    if isinstance(value,np.ndarray):return value.tolist()
    if isinstance(value,bytes):return list(value)
    return value

def main():
    import sys
    output=Path(sys.argv[1]) if len(sys.argv)>1 else OUT
    output.mkdir(parents=True,exist_ok=True)
    original,minimum,expected,palette,stats=load_litematic(SOURCE)
    observed=np.full(expected.shape,65535,'uint16')
    expected_tiles={};actual_tiles={};total=0
    for region in original['Regions'].values():
        pos=np.array([region['Position'][k] for k in ['x','y','z']]);size=np.array([region['Size'][k] for k in ['x','y','z']]);low=pos+np.minimum(size+1,0)-minimum
        for tile in region.get('TileEntities',[]):
            key=tuple(np.array([tile[k] for k in ['x','y','z']])+low)
            expected_tiles[key]=canonical(tile)
    def visit(block):
        nonlocal total
        x,y,z=block['pos'];assert observed[y,z,x]==65535,('Duplicate template position',x,y,z)
        observed[y,z,x]=block['state'];total+=1
        if 'nbt' in block:actual_tiles[(x,y,z)]=canonical(block['nbt'])
    result=read_nbt(TEMPLATE,visit)
    assert result['size']==[221,165,197]
    lookup={json.dumps(p,sort_keys=True):i for i,p in enumerate(palette)}
    conversion=np.array([lookup[json.dumps(p,sort_keys=True)] for p in result['palette']]+[0]*(65536-len(result['palette'])),'uint16')
    present=observed!=65535
    assert np.array_equal(conversion[observed[present]],expected[present]),'Any emitted block/state changed during conversion'
    air=[i for i,p in enumerate(palette) if p['Name'] in ['minecraft:air','minecraft:cave_air','minecraft:void_air']]
    assert np.all(np.isin(expected[~present],air)),'A non-air source block was omitted'
    selection=np.zeros(expected.shape,bool)
    for region in original['Regions'].values():
        pos=np.array([region['Position'][k] for k in ['x','y','z']]);signed=np.array([region['Size'][k] for k in ['x','y','z']])
        low=pos+np.minimum(signed+1,0)-minimum;size=np.abs(signed);x,y,z=low
        selection[y:y+size[1],z:z+size[2],x:x+size[0]]=True
    metadata=json.loads(METADATA.read_text(encoding='utf8'))
    ground=metadata['exterior_ground_y']
    depth,width=expected.shape[1:]
    empty=np.all(np.isin(expected[ground:],air),axis=0)
    exterior=set()
    edge=deque((z,x) for z,x in zip(*np.where(empty)) if z in (0,depth-1) or x in (0,width-1))
    while edge:
        z,x=edge.popleft()
        if (z,x) in exterior or not (0<=z<depth and 0<=x<width and empty[z,x]):
            continue
        exterior.add((z,x))
        edge.extend(((z-1,x),(z+1,x),(z,x-1),(z,x+1)))
    assert {z*width+x for z,x in exterior}==set(metadata['exterior_ground_columns'])
    for z,x in exterior:
        selection[ground:,z,x]=False
    assert np.array_equal(present,selection),'Air coverage differed from the authored interior and exterior mask'
    assert actual_tiles==expected_tiles,'Block entity inventory/components or source positions changed'
    expected_entities=[]
    actual_by_pos={tuple(v['pos']):v['nbt']for v in result['entities']}
    converted_equipment=0
    manifest={'entity_slots':[{'type':v['nbt']['id'],'pos':v['pos'],'id':v['nbt']['reverie_structure_id'],'kind':v['nbt']['reverie_structure_kind']} for v in result['entities']]}
    slots={(v['type'],tuple(v['pos'])):v for v in manifest['entity_slots']}
    used=set()
    for region in original['Regions'].values():
        origin=np.array([region['Position'][k] for k in ['x','y','z']])-minimum
        for entity in region.get('Entities',[]):
            e=canonical(entity);e.pop('UUID',None)
            pos=np.array(e['Pos'])+origin;e['Pos']=pos.tolist()
            block_pos=np.array(e.get('block_pos',np.floor(np.array(entity['Pos'])).astype(int)))+origin;e['block_pos']=block_pos.tolist()
            for k,axis in enumerate(['TileX','TileY','TileZ']):
                if axis in e:e[axis]+=int(origin[k])
            slot=slots[(e['id'],tuple(pos.tolist()))]
            assert slot['id'] not in used,'Duplicated actor identity'
            used.add(slot['id']);e['reverie_structure_id']=slot['id'];e['reverie_structure_kind']=slot['kind']
            # The adventure explicitly replaces only authored stand equipment; independently
            # check the new ID and requested durability interval rather than sharing Java's RNG.
            if e.get('id')=='minecraft:armor_stand':
                actual=actual_by_pos[tuple(pos.tolist())]
                assert actual.get('ShowArms')==1,'Authored display must show its arms'
                e['ShowArms']=1
                for slot,stack in e.get('equipment',{}).items():
                    item=stack.get('id','')
                    if not item.startswith('minecraft:netherite_'):continue
                    suffix=item[len('minecraft:netherite_'):];found=actual['equipment'][slot]
                    assert found['id']=='reverie_dreams:ancient_iron_'+suffix,('Wrong stand conversion',slot)
                    maximum={'helmet':165,'chestplate':240,'leggings':225,'boots':195}.get(suffix,250)
                    damage=found['components']['minecraft:damage'];assert .70<=(maximum-damage)/maximum<=.88
                    stack['id']=found['id'];stack['components']={'minecraft:damage':damage};converted_equipment+=1
            expected_entities.append({'pos':pos.tolist(),'blockPos':block_pos.tolist(),'nbt':e})
    key=lambda d:json.dumps(canonical(d),sort_keys=True)
    assert sorted(map(key,result['entities']))==sorted(map(key,expected_entities)),'Entity position/rotation/NBT mismatch'
    assert len(used)==len(manifest['entity_slots'])==442
    # Five-wide main gate remains open through the boundary, including interior approaches.
    for x in range(218,221):
        for z in range(96,101):
            for y in range(52,57):assert int(expected[y,z,x]) in air,('Blocked mansion gate',x,y,z)
    report={'status':'PASS','source_sha256':hashlib.sha256(SOURCE.read_bytes()).hexdigest(),
            'template_sha256':hashlib.sha256(TEMPLATE.read_bytes()).hexdigest(),'size':[221,165,197],
            'emitted_blocks':total,'preserved_non_air_blocks':int(np.sum(~np.isin(expected,air))),
            'preserved_cave_air':int(np.sum(expected==palette.index({'Name':'minecraft:cave_air'}))),
            'preserved_authored_air':int(np.sum(present & np.isin(expected,air))),
            'omitted_exterior_columns':len(exterior),'omitted_exterior_air':len(exterior)*(expected.shape[0]-ground),
            'omitted_total_volume':int(np.sum(~present)),'preserved_block_entities':len(actual_tiles),'preserved_entities':len(result['entities']),
            'five_wide_five_high_gate':'PASS','source_coordinates':'block arrays/block entities use minimum corner; entities use signed region Position',
            'authored_stand_equipment_converted':converted_equipment,
            'native_world_test':'separate validation required'}
    (output/'template-validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    print(json.dumps(report,ensure_ascii=False))

if __name__=='__main__':main()
