"""Read the current version's original assets and render a reference sheet, without producing mod textures."""
from pathlib import Path
import io, json, zipfile, hashlib
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT/'build/gensokyo-saplings-review'

def main():
    OUT.mkdir(parents=True,exist_ok=True)
    materials = ['oak','birch','spruce','cherry','dark_oak','pale_oak','cherry','jungle']
    names = ['Forest','Birch forest','Taiga','Cherry grove','Dark forest','Magic forest','Maple forest','Jungle']
    image = Image.new('RGB',(1280,710),'#e6ecdf'); draw = ImageDraw.Draw(image)
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',22)
    small = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',16)
    draw.text((22,14),'Gensokyo saplings: original Minecraft 26.1.2 references',font=font,fill='#2d4736')
    records = []
    with zipfile.ZipFile(ROOT/'common/build/moddev/artifacts/vanilla-26.1.2-1.jar') as jar:
        for i,(material,name) in enumerate(zip(materials,names)):
            x,y=(i%4)*320,60+(i//4)*310
            texture='assets/minecraft/textures/block/'+material+'_sapling.png'
            raw=jar.read(texture);sprite=Image.open(io.BytesIO(raw)).convert('RGBA')
            assert sprite.size==(16,16)
            for n,background in enumerate(['#f2f3e9','#344b3a']):
                tile=Image.new('RGBA',(144,144),background)
                tile.alpha_composite(sprite.resize((128,128),Image.Resampling.NEAREST),(8,8))
                image.paste(tile.convert('RGB'),(x+10+n*154,y+42))
            image.paste(sprite,(x+150,y+204),sprite)
            draw.text((x+12,y+5),name,font=font,fill='#2d4736')
            draw.text((x+12,y+238),'minecraft:'+material+'_sapling',font=small,fill='#2d4736')
            models={k:json.loads(jar.read('assets/minecraft/models/'+k+'/'+material+'_sapling.json')) for k in ['block','item']}
            records.append({'theme':name,'material':material,'texture':texture,'sha256':hashlib.sha256(raw).hexdigest(),'size':[16,16],'nativeModels':models})
    draw.text((22,682),'Original pixels on light/dark backgrounds. Asset inspection, not an in-game screenshot.',font=small,fill='#2d4736')
    image.save(OUT/'vanilla-references.png')
    (OUT/'native-reference-audit.json').write_bytes((json.dumps(records,indent=2)+'\n').encode())

if __name__ == '__main__': main()
