"""Render actual authored encounter/treasure coordinates over original room geometry."""
from pathlib import Path
import json,sys
import numpy as np
from PIL import Image,ImageDraw,ImageFont

ROOT=Path(__file__).resolve().parents[2]
layout=json.loads((ROOT/'common/src/generated/resources/data/reverie_dreams/gensokyo_adventure/scarlet_devil_mansion.json').read_text(encoding='utf-8'))
sys.path.insert(0,str(ROOT/'tools/gensokyo-buildings'))
from survey import load_litematic
_,_,states,palette,_=load_litematic(Path('E:/Download/红魔馆.litematic'))
air=np.isin(states,[i for i,s in enumerate(palette)if s['Name']in ('minecraft:air','minecraft:cave_air','minecraft:void_air')])
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',21)
small=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',14)
image=Image.new('RGB',(2040,1380),'#17202b');draw=ImageDraw.Draw(image)
draw.text((25,12),'红魔馆冒险布置 · 原稿平面核对图',font=font,fill='white')
draw.text((25,45),'红：敌对哥布林  绿：可营救哥布林  紫：女仆点  金：宝藏；方位为原稿 X 向右、Z 向下',font=font,fill='#bdcadd')
for index,y in enumerate([4,11,18,25,32,52]):
    ox=20+(index%3)*675;oy=90+(index//3)*630
    surface=np.where(air[y,:,:,None],np.array([48,64,74]),np.array([124,129,135])).astype('uint8')
    tile=Image.fromarray(surface).resize((619,552),Image.Resampling.NEAREST)
    image.paste(tile,(ox,oy+32));draw.text((ox,oy),f'Y={y+1004}'+(' · 地下牢区'if y<51 else' · 主楼入口层'),font=font,fill='white')
    def position(p):return ox+int((p[0]+.5)*619/states.shape[2]),oy+32+int((p[2]+.5)*552/states.shape[1])
    for q in layout['encounters']:
        if q['pos'][1]!=y:continue
        x,z=position(q['pos']);color='#b888ff'if q['kind']=='maid'else'#64e7ab'if q['friendly']else'#f17878'
        draw.ellipse((x-4,z-4,x+4,z+4),fill=color,outline='#111a21')
    for q in layout['treasures']:
        if q['pos'][1]!=y:continue
        x,z=position(q['pos']);draw.rectangle((x-5,z-5,x+5,z+5),fill='#ffcf5c',outline='#111a21');draw.text((x+6,z-15),q['id'],font=small,fill='#fff3c8')
    draw.text((ox,oy+592),'X 4810–5030 / Z 7046–7242',font=small,fill='#a6bacb')
draw.text((25,1350),'其余上层宝藏及全部点位三维坐标见 world-points.json；本图表示实际布置，不是实机截图。',font=small,fill='#bdcadd')
out=ROOT/'data/scarlet-adventure-20261119';out.mkdir(parents=True,exist_ok=True);image.save(out/'adventure-plan.png')
points=[]
for group in ['treasures','encounters']:
    for q in layout[group]:points.append({'id':q['id'],'type':q.get('kind',q.get('tier')),'friendly':q.get('friendly',False),'world':[v+o for v,o in zip(q['pos'],layout['origin'])]})
(out/'world-points.json').write_text(json.dumps(points,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('REVIEW_PLAN',len(points),'points')
