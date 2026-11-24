"""North-up geographic review of actual terrain, authored structure and access routes."""
import base64
import json
import sys
from pathlib import Path

import numpy as np
from PIL import Image
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib import font_manager
from matplotlib.patches import Rectangle
from survey import load_litematic
from site_layout import ROOT, OUT, WORK, SOURCE, read, layers


def main():
    plan=read(OUT/'plan.json');m,h,s=layers(WORK if WORK.exists() else SOURCE)
    font_manager.fontManager.addfont('C:/Windows/Fonts/msyh.ttc');plt.rcParams['font.family']=font_manager.FontProperties(fname='C:/Windows/Fonts/msyh.ttc').get_name()
    plt.rcParams['axes.unicode_minus']=False
    oldroads=read(ROOT/'data/road-construction-preview-20261107/construction-preview.json')['roads']
    _,_,blocks,palette,_=load_litematic(Path('E:/Download/红魔馆.litematic'))
    top=np.max(np.where(blocks!=0,np.arange(blocks.shape[0])[:,None,None],-1),axis=0);zz,xx=np.indices(top.shape)
    colors=[]
    for state in palette:
        name=state['Name'];c=(170,167,158)
        if any(k in name for k in ('deepslate','blackstone','dark_oak')):c=(57,54,55)
        if any(k in name for k in ('quartz','diorite','white')):c=(218,214,205)
        if any(k in name for k in ('red_terracotta','red_nether','red_wool','red_carpet')):c=(154,51,60)
        if any(k in name for k in ('grass','leaves','moss','fern')):c=(83,137,67)
        if 'water' in name:c=(48,113,179)
        if name in ['minecraft:air','minecraft:cave_air']:c=(0,0,0)
        colors.append(c)
    mansion=np.asarray(colors,'uint8')[blocks[np.maximum(top,0),zz,xx]]
    rgba=np.dstack((mansion,np.where(top>=51,255,0).astype('uint8')));Image.fromarray(rgba).resize((1105,985),Image.NEAREST).save(OUT/'mansion-top.png')
    table=[]
    for state in m['surface_palette']:
        c=[.37,.53,.29]
        if state=='minecraft:water':c=[.16,.39,.64]
        elif state in ['minecraft:stone','minecraft:andesite'] or 'slab' in state:c=[.53,.53,.51]
        elif state=='minecraft:dirt_path':c=[.72,.59,.35]
        elif state=='minecraft:gravel':c=[.61,.60,.56]
        elif state=='minecraft:snow_block':c=[.91,.93,.94]
        elif state=='minecraft:lava':c=[.94,.34,.08]
        elif state=='minecraft:basalt':c=[.22,.24,.24]
        table.append(c)
    table=np.array(table)
    def background(box,step=2):
        x0,z0,x1,z1=box;hh=np.array(h[z0:z1:step,x0:x1:step],float);ss=s[z0:z1:step,x0:x1:step]
        gz,gx=np.gradient(hh,step);shade=np.clip(.85+.3*(-gx-gz)/np.sqrt(1+gx*gx+gz*gz),.42,1.15)
        rgb=np.clip(table[ss]*shade[:,:,None],0,1);rgb[ss==0]=table[0]
        return rgb,hh
    panels=[('mansion-site-plan.png',(4650,6900,5400,7400),'红魔馆：完整原稿、东向院门、D06接入'),
            ('shrine-back-mountain-plan.png',(11000,4700,12100,6250),'博丽神社预留地与后山通路：借用D03山腰段')]
    for filename,box,title in panels:
        x0,z0,x1,z1=box;rgb,hh=background(box)
        fig,ax=plt.subplots(figsize=(12,10 if filename.startswith('mansion') else 14),constrained_layout=True)
        ax.imshow(rgb,extent=[x0,x1,z1,z0]);cs=ax.contour(np.arange(x0,x1,2),np.arange(z0,z1,2),hh,levels=np.arange(1000,1400,10),colors='#173024',alpha=.35,linewidths=.55);ax.clabel(cs,fontsize=7)
        for road in oldroads:
            p=np.array(road['centerline_xyz']);ax.plot(p[:,0],p[:,2],c='#f4dba0',lw=2,zorder=3)
        for r in plan['roads']:
            p=np.array(r['centerline_xyz']);color={'M01':'#ff6976','H01':'#ff6976','H02':'#55e6ee'}[r['id']]
            ax.plot(p[:,0],p[:,2],c='#15212d',lw=4,zorder=4);ax.plot(p[:,0],p[:,2],c=color,lw=2.5,label=r['id']+' '+r['name']+' / '+str(r['length'])+'格',zorder=5)
        if filename.startswith('mansion'):
            bx0,bz0,bx1,bz1=plan['mansion']['bounds_xz'];ax.imshow(rgba,extent=[bx0,bx1,bz1,bz0],interpolation='nearest',zorder=6)
            ax.add_patch(Rectangle((bx0,bz0),bx1-bx0,bz1-bz0,fill=False,ec='#ff6976',lw=1.7,zorder=7))
            labels=[(4920,7144,'完整庭院中心\n地面方块 Y1055'),(5030,7144,'东院门\nX5030 / Z7144'),(5160,7096,'D06 原有端点'),(5104,7040,'旧推断代表点')]
        else:
            bx0,bz0,bx1,bz1=plan['shrine']['reservation_xz'];ax.add_patch(Rectangle((bx0,bz0),bx1-bx0,bz1-bz0,facecolor='#ffbf6544',ec='#ffd16c',lw=2,zorder=6))
            labels=[(11448,4976,'神社预留：160×200格\n原地表 Y1142，未建建筑/未整平'),(11448,5056,'参道预留入口'),(11518,5383,'从既有D03分出后山路'),(11600,6016,'后山入口 / 地表Y1088')]
        for x,z,text in labels:
            ax.scatter(x,z,c='white',s=20,ec='#182430',zorder=8);ax.annotate(text,(x,z),xytext=(15,-20 if '旧' in text else 15),textcoords='offset points',color='white',fontsize=9,bbox=dict(fc='#17242c',alpha=.88,pad=4),arrowprops=dict(arrowstyle='-',color='white',lw=.7),zorder=9)
        ax.set_xlim(x0,x1);ax.set_ylim(z1,z0);ax.set_aspect('equal');ax.set_xlabel('X → 东（格）');ax.set_ylabel('Z → 南；上方为北');ax.set_title(title+'\n真实 X/Z 等比例；等高距10格；建筑为原稿俯视投影，非实机航拍')
        ax.legend(loc='lower left',fontsize=8,framealpha=.95);fig.savefig(OUT/filename,dpi=145);plt.close(fig)
    rgb,_=background((0,0,m['width'],m['depth']),8);Image.fromarray((rgb*255).astype('uint8')).save(OUT/'terrain-plan.png')
    # A self-contained interactive SVG keeps true world coordinates and usable zoom.
    base='data:image/png;base64,'+base64.b64encode((OUT/'terrain-plan.png').read_bytes()).decode()
    building='data:image/png;base64,'+base64.b64encode((OUT/'mansion-top.png').read_bytes()).decode()
    old_svg=''.join('<polyline points="'+' '.join(str(p[0])+','+str(p[2]) for p in r['centerline_xyz'][::3])+'" fill="none" stroke="#f4dba0" stroke-width="3" vector-effect="non-scaling-stroke"/>' for r in oldroads)
    new_svg=''.join('<polyline points="'+' '.join(str(p[0])+','+str(p[2]) for p in r['centerline_xyz'])+'" fill="none" stroke="'+('#55e6ee' if r['id']=='H02' else '#ff6976')+'" stroke-width="3" vector-effect="non-scaling-stroke"/>' for r in plan['roads'])
    script="""const svg=document.querySelector('svg'), coord=document.querySelector('#coord');function at(e){let p=svg.createSVGPoint();p.x=e.clientX;p.y=e.clientY;return p.matrixTransform(svg.getScreenCTM().inverse())}let drag=null;svg.onpointerdown=e=>{drag=at(e);svg.setPointerCapture(e.pointerId)};svg.onpointerup=()=>drag=null;svg.onpointercancel=()=>drag=null;svg.onpointermove=e=>{let p=at(e);coord.textContent='X '+Math.round(p.x)+' / Z '+Math.round(p.y);if(drag){let b=svg.viewBox.baseVal;svg.setAttribute('viewBox',[b.x+drag.x-p.x,b.y+drag.y-p.y,b.width,b.height].join(' '))}};svg.addEventListener('wheel',e=>{e.preventDefault();let p=at(e),b=svg.viewBox.baseVal,f=e.deltaY>0?1.15:1/1.15;if(b.width*f<150||b.width*f>18000)return;svg.setAttribute('viewBox',[p.x+(b.x-p.x)*f,p.y+(b.y-p.y)*f,b.width*f,b.height*f].join(' '))},{passive:false});document.querySelectorAll('[data-box]').forEach(e=>e.onclick=()=>svg.setAttribute('viewBox',e.dataset.box));"""
    html='''<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>神社与红魔馆选址</title><style>body{margin:0;background:#16222f;color:#edf3f5;font:15px system-ui}header{padding:14px 20px}button{padding:8px 14px;margin:5px;cursor:pointer}svg{width:100%;height:76vh;touch-action:none}a{color:#ffda89}p{line-height:1.7}</style><header><b>幻想乡 · 建筑选址与道路（北朝上，X/Z等比例）</b><p>红魔馆按原稿221×165×197格，地牢低于庭院51格；红线为新增入口路，青线为新增后山步道，米色线为已施工道路。神社橙框仅预留160×200格，尚无建筑与最终庭院标高。</p><button data-box="4650 6900 750 500">红魔馆</button><button data-box="11000 4700 1100 1550">神社与后山</button><button data-box="0 0 12800 10240">全图</button><span id="coord">拖动平移 / 滚轮缩放</span> · <a href="plan.json">坐标和纵断面</a></header>'''
    html+='<svg viewBox="4650 6900 750 500" xmlns="http://www.w3.org/2000/svg"><image width="12800" height="10240" href="'+base+'"/>'+old_svg+new_svg+'<image x="4810" y="7046" width="221" height="197" href="'+building+'"/><rect x="11368" y="4876" width="160" height="200" fill="#ffbf65" fill-opacity=".25" stroke="#ffd16c" stroke-width="2" vector-effect="non-scaling-stroke"/>'
    for x,z,label in [(4920,7144,'红魔馆'),(11448,4976,'神社预留地'),(11600,6016,'后山入口')]:html+=f'<circle cx="{x}" cy="{z}" r="8" fill="white"/><text x="{x+16}" y="{z-16}" fill="white" font-size="27" stroke="#15212d" stroke-width="6" paint-order="stroke">{label}</text>'
    html+='</svg><header><p>建筑图是原稿方块的俯视投影；底图只表达当前地图高度、水面和铺装，不包含游戏光照与树木。旧同人图地标仅供关系参考，不用于强行移动山体或重画河流。最终修订号和实机检查见validation.json及native-results.json。</p></header><script>'+script+'</script></html>'
    (OUT/'review.html').write_text(html,encoding='utf8');(OUT/'review-script.js').write_text(script,encoding='utf8')
    print('RENDERED',*[p[0] for p in panels])

if __name__=='__main__':main()
