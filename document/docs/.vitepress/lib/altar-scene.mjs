import {cuboidFaces, rotate, normal, frameAt} from './model-geometry.mjs';
import {loadImage, texturedFace} from './model-renderer.mjs';

/** Placed geometry uses blockstate rotation, never an inventory GUI transform. */
export function placedFaces(pack) {
  let faces = pack.kind === 'composite' ? pack.models.flatMap(placedFaces) : cuboidFaces({...pack,guiLight:'side'});
  if (pack.blockRotation) faces = faces.map(face => ({...face,points:face.points.map(p => {
    const rotated = rotate(p.map(n=>n-8),pack.blockRotation);return rotated.map(n=>n+8);
  })}));
  return faces;
}

export function visiblePlacements(layout, layer='all') {
  return layout.placements.filter(block => layer === 'all' || block.pos[1] === Number(layer));
}

const textureMetadata = node => Object.assign({},node.textureInfo,...(node.models??[]).map(textureMetadata));
export async function prepareAltarScene(layout, base) {
  const materials = await Promise.all(layout.materials.map(async material => {
    const response = await fetch(base(material.model));
    if (!response.ok) throw Error('Unable to load altar model: '+material.id);
    const pack = await response.json(), faces=placedFaces(pack);
    if (!faces.length || faces.some(f=>!f.texture)) throw Error('Incomplete altar geometry: '+material.id);
    return {id:material.id,faces,metadata:textureMetadata(pack)};
  }));
  const textures = new Map(await Promise.all([...new Set(materials.flatMap(m=>m.faces.map(f=>f.texture)))].map(async url=>[url,await loadImage(url,base)])));
  if ([...textures.values()].some(image=>!image)) throw Error('Unable to load an altar texture');
  return {materials:Object.fromEntries(materials.map(m=>[m.id,m])),textures,textureInfo:Object.assign({},...materials.map(m=>m.metadata))};
}

export function sceneFaces(layout, prepared, layer='all') {
  return visiblePlacements(layout,layer).flatMap(block=>prepared.materials[block.id].faces.map(face=>({
    ...face,block,key:block.pos.join(','),points:face.points.map(p=>[
      block.pos[0]-.5+p[0]/16,block.pos[1]+p[1]/16,block.pos[2]-.5+p[2]/16
    ])
  })));
}

export function insidePolygon([x,y], points) {
  let inside=false;
  for(let i=0,j=points.length-1;i<points.length;j=i++){
    const [a,b]=points[i],[c,d]=points[j];
    if((b>y)!==(d>y)&&x<(c-a)*(y-b)/(d-b)+a)inside=!inside;
  }
  return inside;
}

export function pickBlock(hits, point) {
  for(let i=hits.length-1;i>=0;i--)if(insidePolygon(point,hits[i].points))return hits[i].block;
  return null;
}

export function drawAltar(canvas, layout, scene, {yaw=-35,pitch=30,zoom=1,layer='all',grid=true,selected=null,dark=false,en=false}={}) {
  const ctx=canvas.getContext('2d');if(!ctx)return [];
  const W=canvas.width,H=canvas.height,dpr=W/Math.max(1,canvas.getBoundingClientRect().width);
  ctx.setTransform(1,0,0,1,0,0);ctx.clearRect(0,0,W,H);ctx.imageSmoothingEnabled=false;
  const [minX,,minZ]=layout.bounds.min,[maxX,,maxZ]=layout.bounds.max;
  const transform=p=>rotate(p,[pitch,yaw,0]);
  const compass=[{pos:[0,0,minZ-1.2],label:en?'N · −Z':'北 · −Z'},
    {pos:[maxX+1.2,0,0],label:en?'E · +X':'东 · +X'},
    {pos:[0,0,maxZ+1.2],label:en?'S · +Z':'南 · +Z'},
    {pos:[minX-1.2,0,0],label:en?'W · −X':'西 · −X'}];
  const extents=compass.map(c=>transform(c.pos));
  for(const x of [minX-.5,maxX+.5])for(const y of [0,layout.height])for(const z of [minZ-.5,maxZ+.5])extents.push(transform([x,y,z]));
  const left=Math.min(...extents.map(p=>p[0])),right=Math.max(...extents.map(p=>p[0]));
  const bottom=Math.min(...extents.map(p=>p[1])),top=Math.max(...extents.map(p=>p[1]));
  const unit=Math.min((W-70*dpr)/(right-left),(H-60*dpr)/(top-bottom))*zoom;
  const screen=p=>[W/2+(p[0]-(left+right)/2)*unit,H/2-(p[1]-(bottom+top)/2)*unit];
  const project=p=>screen(transform(p));
  const polygon=points=>{ctx.beginPath();ctx.moveTo(...points[0]);for(const p of points.slice(1))ctx.lineTo(...p);ctx.closePath()};
  if(grid){
    for(let x=minX;x<=maxX;x++)for(let z=minZ;z<=maxZ;z++){
      const points=[[x-.5,0,z-.5],[x-.5,0,z+.5],[x+.5,0,z+.5],[x+.5,0,z-.5]].map(project);
      polygon(points);ctx.fillStyle=(x+z)%2===0?(dark?'#ffffff05':'#00000003'):(dark?'#ffffff0b':'#00000008');ctx.fill();
    }
    ctx.strokeStyle=dark?'#a9b4c74d':'#9d948762';ctx.lineWidth=dpr*.75;
    for(let x=minX-.5;x<=maxX+.5;x++){ctx.beginPath();ctx.moveTo(...project([x,0,minZ-.5]));ctx.lineTo(...project([x,0,maxZ+.5]));ctx.stroke()}
    for(let z=minZ-.5;z<=maxZ+.5;z++){ctx.beginPath();ctx.moveTo(...project([minX-.5,0,z]));ctx.lineTo(...project([maxX+.5,0,z]));ctx.stroke()}
  }
  const faces=sceneFaces(layout,scene,layer).map(f=>({...f,rotated:f.points.map(transform)}))
    .filter(f=>normal(f.rotated)[2]>.00001)
    .sort((a,b)=>a.rotated.reduce((n,p)=>n+p[2],0)-b.rotated.reduce((n,p)=>n+p[2],0));
  const hits=[];
  for(const face of faces){
    const image=scene.textures.get(face.texture),points=face.rotated.map(screen);
    const n=normal(face.rotated),brightness=face.shade ? .58+.42*Math.max(0,n[0]*-.3+n[1]*.8+n[2]*.5) : 1;
    const frame=frameAt(image,scene.textureInfo[face.texture]?.animation);
    const uv=face.uv.map(([u,v])=>[frame.x+u/16*frame.width,frame.y+v/16*frame.height]);
    texturedFace(ctx,image,points,uv,brightness);
    if(face.key===selected){polygon(points);ctx.strokeStyle=dark?'#f2c975':'#b76a1f';ctx.lineWidth=1.5*dpr;ctx.stroke()}
    hits.push({points,block:face.block});
  }
  ctx.font=`${12*dpr}px "Segoe UI","Microsoft YaHei",sans-serif`;ctx.textAlign='center';ctx.textBaseline='middle';
  for(const c of compass){
    const [x,y]=project(c.pos),width=ctx.measureText(c.label).width+12*dpr,height=21*dpr;
    ctx.fillStyle=dark?'#23272ff2':'#fffdf8f2';ctx.fillRect(x-width/2,y-height/2,width,height);
    ctx.strokeStyle=dark?'#777f8b':'#c9c0b4';ctx.lineWidth=.75*dpr;ctx.strokeRect(x-width/2,y-height/2,width,height);
    ctx.fillStyle=dark?'#eee5d8':'#544b41';ctx.fillText(c.label,x,y);
  }
  return hits;
}
