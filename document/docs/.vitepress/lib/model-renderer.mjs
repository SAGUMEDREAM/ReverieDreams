import { itemFaces, normal, rotate, frameAt } from './model-geometry.mjs';

const images = new Map();
const tinted = new Map();
const preparedModels = new Map();
export function loadModel(url, base) {
  if (!preparedModels.has(url)) preparedModels.set(url, fetch(base(url)).then(r => { if (!r.ok) throw Error(`Model ${r.status}`); return r.json(); }).then(pack => prepareModel(pack, base)));
  return preparedModels.get(url);
}
export async function loadImage(url, base) {
  if (!images.has(url)) images.set(url, new Promise(resolve => {
    const image = new Image(); image.onload = () => resolve(image); image.onerror = () => resolve(null); image.src = base(url);
  }));
  return images.get(url);
}

function tintImage(image, color) {
  if (color == null || color === 0xffffff || color === -1) return image;
  const key = `${image.src}:${color}`;
  if (tinted.has(key)) return tinted.get(key);
  const canvas = document.createElement('canvas'); canvas.width=image.width; canvas.height=image.height;
  const context=canvas.getContext('2d'); context.drawImage(image,0,0);
  context.globalCompositeOperation='multiply'; context.fillStyle=`#${(color&0xffffff).toString(16).padStart(6,'0')}`; context.fillRect(0,0,canvas.width,canvas.height);
  context.globalCompositeOperation='destination-in'; context.drawImage(image,0,0);
  if (tinted.size>256) tinted.delete(tinted.keys().next().value);
  tinted.set(key,canvas); return canvas;
}

export function texturedFace(ctx, image, p, uv, brightness) {
  const [a,b,c] = p, [u,v,w] = uv;
  const den=u[0]*(v[1]-w[1])+v[0]*(w[1]-u[1])+w[0]*(u[1]-v[1]);
  if (Math.abs(den)<1e-8) return;
  const transform = index => [
    (a[index]*(v[1]-w[1])+b[index]*(w[1]-u[1])+c[index]*(u[1]-v[1]))/den,
    (a[index]*(w[0]-v[0])+b[index]*(u[0]-w[0])+c[index]*(v[0]-u[0]))/den,
    (a[index]*(v[0]*w[1]-w[0]*v[1])+b[index]*(w[0]*u[1]-u[0]*w[1])+c[index]*(u[0]*v[1]-v[0]*u[1]))/den
  ];
  const x=transform(0),y=transform(1);
  ctx.save(); ctx.beginPath(); ctx.moveTo(...p[0]);for(const point of p.slice(1))ctx.lineTo(...point);ctx.closePath();ctx.clip();
  ctx.setTransform(x[0],y[0],x[1],y[1],x[2],y[2]); ctx.filter=brightness<.99?`brightness(${brightness})`:'none';
  ctx.drawImage(image,0,0);ctx.restore();
}

export async function prepareModel(pack, base) {
  const faces=itemFaces(pack);
  for (const face of faces) if (face.tint?.colorMap) {
    const image = await loadImage(face.tint.colorMap, base);
    if (image) {
      const canvas=document.createElement('canvas');canvas.width=image.width;canvas.height=image.height;
      const context=canvas.getContext('2d');context.drawImage(image,0,0);
      const temperature=face.tint.temperature??.5, humidity=(face.tint.downfall??1)*temperature;
      const color=context.getImageData(Math.floor((1-temperature)*255),Math.floor((1-humidity)*255),1,1).data;
      face.tint=(color[0]<<16)|(color[1]<<8)|color[2];
    } else face.tint=0xffffff;
  }
  const textures=new Map(await Promise.all([...new Set(faces.map(f=>f.texture))].map(async url=>[url,await loadImage(url,base)])));
  const metadata = node => Object.assign({}, node.textureInfo, ...(node.models??[]).map(metadata));
  return {faces,textures,textureInfo:metadata(pack), unavailable:pack.kind==='unavailable'||!faces.length||![...textures.values()].some(Boolean)};
}

export function drawModel(canvas, model, {yaw=0,pitch=0,zoom=1,ticks=0}={}) {
  const ctx=canvas.getContext('2d');if(!ctx)return;
  const W=canvas.width,H=canvas.height;ctx.setTransform(1,0,0,1,0,0);ctx.clearRect(0,0,W,H);ctx.imageSmoothingEnabled=false;
  const faces=model.faces.map(f=>({...f,points:f.points.map(p=>rotate(p,[pitch,yaw,0]))}));
  let extent=8.5;
  for(const f of faces)for(const p of f.points)extent=Math.max(extent,Math.abs(p[0]),Math.abs(p[1]));
  const size=Math.min(W,H)/(extent*2.25)*zoom;
  const visible=faces.filter(f=>normal(f.points)[2]>.00001).sort((a,b)=>a.points.reduce((n,p)=>n+p[2],0)-b.points.reduce((n,p)=>n+p[2],0));
  for(const face of visible){
    let image=model.textures.get(face.texture);if(!image)continue;
    image=tintImage(image,face.tint);
    const n=normal(face.points), brightness=face.shade ? .58+.42*Math.max(0,n[0]*-.3+n[1]*.8+n[2]*.5) : 1;
    const screen=face.points.map(p=>[W/2+p[0]*size,H/2-p[1]*size]);
    const frame=frameAt(image,model.textureInfo[face.texture]?.animation,ticks);
    const uv=face.uv.map(([u,v])=>[frame.x+u/16*frame.width,frame.y+v/16*frame.height]);
    // Orthographic projection preserves each rectangular face's affine UV map.
    // One clipped quad avoids anti-aliased seams along an unnecessary triangle diagonal.
    texturedFace(ctx,image,screen,uv,brightness);
  }
}
