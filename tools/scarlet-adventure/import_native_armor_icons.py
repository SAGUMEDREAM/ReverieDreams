"""Encode the reviewed, flat-cell ImageGen repaint into the final 16x16 palette.

This is a grid/limited-palette format export, not a pixel-painting replacement:
the repainted plate shapes and shading come from generated-16x16-r2. The original
vanilla alpha defines the exact native silhouette. No smoothing or dithering.
"""
from pathlib import Path
import argparse,io,json,zipfile
import numpy as np
from PIL import Image

ROOT=Path(__file__).resolve().parents[2]
NAMES=['helmet','chestplate','leggings','boots']
HEX=['2C2B35','45424F','625E70','817B91','A09AAF','C4BFD2','E3DFEB']
PALETTE=np.array([tuple(bytes.fromhex(c))for c in HEX],dtype=np.int32)
parser=argparse.ArgumentParser()
parser.add_argument('--minecraft-client',type=Path,default=Path('D:/Gradle/.gradle/caches/fabric-loom/26.1.2/minecraft-client.jar'))
args=parser.parse_args()
target=ROOT/'tools/scarlet-adventure/art/native_16x16';target.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(args.minecraft_client)as archive:
    for name in NAMES:
        reference=Image.open(io.BytesIO(archive.read('assets/minecraft/textures/item/netherite_'+name+'.png'))).convert('RGBA')
        repaint=Image.open(ROOT/'tools/scarlet-adventure/art/generated-16x16-r2'/(name+'.png')).convert('RGBA')
        assert reference.size==(16,16)
        bounds=reference.getbbox();generated_bounds=repaint.getchannel('A').point(lambda a:255 if a>=128 else 0).getbbox()
        assert generated_bounds,'Empty repaint'
        # Fit the occupied logical grid to the unchanged vanilla sprite's native grid.
        cells=np.array(repaint.crop(generated_bounds).resize((bounds[2]-bounds[0],bounds[3]-bounds[1]),Image.Resampling.NEAREST))
        valid=np.argwhere(cells[:,:,3]>=128)
        for y,x in np.argwhere(cells[:,:,3]<128):
            yy,xx=min(valid,key=lambda q:(int(q[0])-y)**2+(int(q[1])-x)**2);cells[y,x]=cells[yy,xx]
        rgb=cells[:,:,:3].astype(np.int32)
        nearest=np.square(rgb[:,:,None,:]-PALETTE[None,None,:,:]).sum(axis=3).argmin(axis=2)
        pixels=np.zeros((16,16,4),dtype=np.uint8)
        pixels[bounds[1]:bounds[3],bounds[0]:bounds[2],:3]=PALETTE[nearest].astype(np.uint8)
        pixels[:,:,3]=np.array(reference)[:,:,3]
        pixels[pixels[:,:,3]==0]=0
        Image.fromarray(pixels).save(target/(name+'.png'))
        print(name,'16x16','opaque colors',len({tuple(c[:3])for c in pixels.reshape(-1,4)if c[3]}))
