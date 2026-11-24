"""Inspect exact native assets at 1x and integer zoom; create review evidence only."""
from pathlib import Path
import argparse,hashlib,io,json,zipfile
import numpy as np
from PIL import Image,ImageDraw,ImageFont

ROOT=Path(__file__).resolve().parents[2]
parser=argparse.ArgumentParser()
parser.add_argument('--minecraft-client',type=Path,default=Path('D:/Gradle/.gradle/caches/fabric-loom/26.1.2/minecraft-client.jar'))
args=parser.parse_args()
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',20)
small=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',14)
canvas=Image.new('RGBA',(920,620),'#202630');draw=ImageDraw.Draw(canvas)
draw.text((18,12),'护甲物品图标 · 16×16 源图 / 6倍最近邻对照',font=font,fill='white')
draw.text((18,45),'每列：浅槽、深槽；右下角保留1倍源图。放大图只用于检查整像素色块。',font=small,fill='#bac8d8')
names=['helmet','chestplate','leggings','boots'];cn=['头盔','胸甲','护腿','靴子'];report=[]
assets=ROOT/'common/src/main/resources/assets/reverie_dreams/textures/item'
with zipfile.ZipFile(args.minecraft_client)as archive:
    for i,name in enumerate(names):
        vanilla=Image.open(io.BytesIO(archive.read('assets/minecraft/textures/item/netherite_'+name+'.png'))).convert('RGBA')
        current=Image.open(assets/('ancient_iron_'+name+'.png')).convert('RGBA')
        # Reconstruct the rejected export from its preserved original; no private backup needed.
        draft=Image.open(ROOT/'tools/scarlet-adventure/art/generated'/(name+'.png')).convert('RGBA')
        prior=np.array(draft.resize((16,16),Image.Resampling.NEAREST));mask=np.array(vanilla)[:,:,3]
        valid=np.argwhere(prior[:,:,3]>=128)
        for y,x in np.argwhere((mask>0)&(prior[:,:,3]<128)):
            yy,xx=min(valid,key=lambda q:(int(q[0])-y)**2+(int(q[1])-x)**2);prior[y,x,:3]=prior[yy,xx,:3]
        prior[:,:,3]=mask;old=Image.fromarray(prior)
        a=np.array(current);ref=np.array(vanilla);colors={tuple(p[:3])for p in a.reshape(-1,4)if p[3]}
        assert current.size==(16,16) and set(a[:,:,3].flatten())=={0,255}
        assert np.array_equal(a[:,:,3],ref[:,:,3]) and len(colors)<=7
        assert (ROOT/'tools/scarlet-adventure/art/native_16x16'/(name+'.png')).read_bytes()==(assets/('ancient_iron_'+name+'.png')).read_bytes()
        x=105+i*200;draw.text((x+45,78),cn[i],font=font,fill='white')
        for row,(label,picture)in enumerate([('原版合金',vanilla),('返修前',old),('返修后',current)]):
            y=111+row*150
            if i==0:draw.text((12,y+32),label,font=font,fill='white')
            for slot,color in enumerate(['#b4b4b4','#343a46']):
                xx=x+slot*98;draw.rectangle((xx,y,xx+95,y+95),fill=color)
                canvas.alpha_composite(picture.resize((96,96),Image.Resampling.NEAREST),(xx,y))
                canvas.alpha_composite(picture,(xx+76,y+100))
        oldcolors={p[:3]for p in old.getdata()if p[3]}
        draw.text((x,567),f'{len(oldcolors)} → {len(colors)} 色 | 16×16',font=small,fill='#cbd5e1')
        report.append({'item':name,'size':current.size,'visibleColorsBefore':len(oldcolors),'visibleColorsAfter':len(colors),'alphaOnly':[0,255],'silhouetteMatchesVanilla':True,'nativeMasterIdentical':True,'sha256':hashlib.sha256((assets/('ancient_iron_'+name+'.png')).read_bytes()).hexdigest()})
out=ROOT/'data/ancient-iron-icons-16-20261120';out.mkdir(parents=True,exist_ok=True)
canvas.convert('RGB').save(out/'comparison.png')
(out/'technical-check.json').write_text(json.dumps({'technicalStatus':'PASS','items':report,'artSelfReview':'requires visual inspection of comparison.png','userAcceptance':'pending','runtimeTests':'not run; PNG-only correction with unchanged size/alpha/model references','polymer':'not involved; not run','distUpdated':False},indent=2),encoding='utf-8')
print('NATIVE_ICON_TECHNICAL_CHECK_PASS')
