"""Export ImageGen originals to the engine's native pixel/UV grids.

Creative colors come from the generated art, never a brightness filter on vanilla.
The vanilla alpha stencil preserves exact silhouettes and armor UV seams. This is
an explicit format/UV export step, not a claim that the large AI originals are game-ready.
"""
from pathlib import Path
import argparse,io,json,zipfile
import numpy as np
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[2]
parser=argparse.ArgumentParser()
parser.add_argument('--minecraft-client',type=Path,default=Path('D:/Gradle/.gradle/caches/fabric-loom/26.1.2/minecraft-client.jar'))
args=parser.parse_args()
names=['helmet','chestplate','leggings','boots','sword','pickaxe','axe','shovel','hoe','humanoid','humanoid_leggings','spear','spear_in_hand']
assets=ROOT/'common/src/main/resources/assets/reverie_dreams/textures'
originals=ROOT/'tools/scarlet-adventure/art/generated';originals.mkdir(parents=True,exist_ok=True)
review=Image.new('RGBA',(len(names)*112,160),(35,39,48,255));draw=ImageDraw.Draw(review)
report=[]
archive=zipfile.ZipFile(args.minecraft_client)
for i,name in enumerate(names):
    uv=name.startswith('humanoid')
    ref='assets/minecraft/textures/'+('entity/equipment/'+name+'/netherite.png'if uv else'item/netherite_'+name+'.png')
    source=Image.open(io.BytesIO(archive.read(ref))).convert('RGBA');generated=Image.open(originals/(name+'.png')).convert('RGBA')
    mask=np.array(source)[:,:,3]
    native=ROOT/'tools/scarlet-adventure/art/native_16x16'/(name+'.png')
    if name in {'helmet','chestplate','leggings','boots'}:
        # Armor icons are final native pixel masters. Never resample the rejected large drafts.
        out=Image.open(native).convert('RGBA');target=np.array(out)
        assert out.size==(16,16) and np.array_equal(target[:,:,3],mask),name
        assert len({tuple(p[:3])for p in target.reshape(-1,4)if p[3]})<=7,name
    else:
        target=np.array(generated.resize(source.size,Image.Resampling.NEAREST))
        # Repair sampling gaps at the silhouette from the nearest generated opaque logical pixel.
        valid=np.argwhere(target[:,:,3]>=128)
        for y,x in np.argwhere((mask>0)&(target[:,:,3]<128)):
            yy,xx=min(valid,key=lambda q:(int(q[0])-y)**2+(int(q[1])-x)**2);target[y,x,:3]=target[yy,xx,:3]
        target[:,:,3]=mask;out=Image.fromarray(target)
    dest=assets/('entity/equipment/'+name+'/ancient_iron.png'if uv else 'item/ancient_iron_'+name+'.png');dest.parent.mkdir(parents=True,exist_ok=True);out.save(dest)
    tile=out.resize((112,56)if uv else (112,112),Image.Resampling.NEAREST);review.alpha_composite(tile,(i*112,0));draw.text((i*112,118),name,fill='white')
    source_rgb=np.array(source)[:,:,:3][mask>0];new_rgb=target[:,:,:3][mask>0]
    report.append({'asset':name,'generated':str((ROOT/'tools/scarlet-adventure/art/generated-16x16-r2'/(name+'.png')).relative_to(ROOT))if name in {'helmet','chestplate','leggings','boots'}else str(originals.relative_to(ROOT)/(name+'.png')),'nativeMaster':str(native.relative_to(ROOT))if name in {'helmet','chestplate','leggings','boots'}else None,'export':str(dest.relative_to(ROOT)),'size':source.size,'alphaIdentical':True,'meanBrightnessBefore':float(source_rgb.mean()),'meanBrightnessAfter':float(new_rgb.mean())})
dest=ROOT/'data/scarlet-adventure-20261119';dest.mkdir(parents=True,exist_ok=True);review.save(dest/'equipment-review.png');(dest/'texture-export.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print('EXPORTED',len(report),'ImageGen assets; original alpha/UV preserved')
