const scale = (v, s) => v.map((n, i) => n * (Array.isArray(s) ? s[i] : s));
const add = (v, b) => v.map((n, i) => n + b[i]);
export const normal = p => {
  const a = p[1].map((n, i) => n - p[0][i]), b = p[2].map((n, i) => n - p[0][i]);
  const n = [a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0]];
  const length = Math.hypot(...n) || 1; return n.map(x => x / length);
};

/** Minecraft ItemTransform uses JOML rotationXYZ: Rz is applied to a vertex first. */
export function rotate(v, degrees = [0,0,0]) {
  let [x,y,z] = v;
  const [a,b,c] = degrees.map(d => d * Math.PI / 180);
  [x,y] = [x*Math.cos(c)-y*Math.sin(c), x*Math.sin(c)+y*Math.cos(c)];
  [x,z] = [x*Math.cos(b)+z*Math.sin(b), -x*Math.sin(b)+z*Math.cos(b)];
  [y,z] = [y*Math.cos(a)-z*Math.sin(a), y*Math.sin(a)+z*Math.cos(a)];
  return [x,y,z];
}

export function rotateElement(v, r) {
  if (!r) return v;
  const angles = [0,0,0]; angles['xyz'.indexOf(r.axis)] = r.angle;
  let relative = add(v, scale(r.origin, -1));
  if (r.rescale) {
    const factor = 1 / Math.cos(r.angle * Math.PI / 180);
    relative = relative.map((n,i) => 'xyz'[i] === r.axis ? n : n * factor);
  }
  return add(rotate(relative, angles), r.origin);
}

export function defaultUV(face, a, b) {
  return ({ down:[a[0],16-b[2],b[0],16-a[2]], up:[a[0],a[2],b[0],b[2]],
    north:[16-b[0],16-b[1],16-a[0],16-a[1]], south:[a[0],16-b[1],b[0],16-a[1]],
    west:[a[2],16-b[1],b[2],16-a[1]], east:[16-b[2],16-b[1],16-a[2],16-a[1]] })[face];
}

export function cuboidFaces(model) {
  const result = [];
  for (const e of model.elements ?? []) {
    const [x,y,z] = e.from, [X,Y,Z] = e.to;
    const corners = {
      down:[[x,y,Z],[x,y,z],[X,y,z],[X,y,Z]], up:[[x,Y,z],[x,Y,Z],[X,Y,Z],[X,Y,z]],
      north:[[X,Y,z],[X,y,z],[x,y,z],[x,Y,z]], south:[[x,Y,Z],[x,y,Z],[X,y,Z],[X,Y,Z]],
      west:[[x,Y,z],[x,y,z],[x,y,Z],[x,Y,Z]], east:[[X,Y,Z],[X,y,Z],[X,y,z],[X,Y,z]]
    };
    for (const [direction, face] of Object.entries(e.faces ?? {})) {
      if (!face.texture || !corners[direction]) continue;
      const [u,v,U,V] = face.uv ?? defaultUV(direction, e.from, e.to);
      const uv = [[u,v],[u,V],[U,V],[U,v]];
      const shift = (face.rotation ?? 0) / 90;
      result.push({ points:corners[direction].map(p => rotateElement(p,e.rotation)), uv:uv.map((_,i) => uv[(i+shift)%4]),
        texture:face.texture, tint:face.tintindex == null ? null : model.tints?.[face.tintindex], shade:e.shade !== false && model.guiLight !== 'front' });
    }
  }
  return result;
}

export function itemFaces(pack) {
  if (pack.kind === 'composite') return pack.models.flatMap(itemFaces);
  let faces = pack.kind === 'cuboid' ? cuboidFaces(pack) : [];
  if (pack.kind === 'layers') {
    faces = (pack.layers ?? []).flatMap((texture, i) => {
      const z = 8.5 + i*.001;
      return [{ points:[[0,16,z],[0,0,z],[16,0,z],[16,16,z]], uv:[[0,0],[0,16],[16,16],[16,0]], texture, tint:pack.tints?.[i], shade:false },
        {points:[[16,16,16-z],[16,0,16-z],[0,0,16-z],[0,16,16-z]], uv:[[0,0],[0,16],[16,16],[16,0]], texture,tint:pack.tints?.[i],shade:false}];
    });
  }
  if (pack.blockRotation) faces = faces.map(f => ({...f, points:f.points.map(p => add(rotate(add(p,[-8,-8,-8]),pack.blockRotation),[8,8,8]))}));
  const display = pack.display?.gui ?? {};
  return faces.map(face => ({ ...face, points:face.points.map(p => add(rotate(scale(add(p,[-8,-8,-8]),display.scale ?? [1,1,1]),display.rotation),display.translation ?? [0,0,0])) }));
}

export function frameAt(image, animation, ticks = 0) {
  if (!animation) return {x:0,y:0,width:image.width,height:image.height};
  const minimum = Math.min(image.width,image.height);
  const width = animation.width ?? (animation.height ? image.width : minimum);
  const height = animation.height ?? (animation.width ? image.height : minimum);
  const columns = Math.floor(image.width/width), total = columns*Math.floor(image.height/height);
  const frames = animation.frames ?? Array.from({length:total},(_,i) => i);
  const durations = frames.map(f => typeof f === 'number' ? animation.frametime ?? 1 : f.time ?? animation.frametime ?? 1);
  let remaining = Math.floor(ticks) % (durations.reduce((a,b) => a+b,0) || 1), index = 0;
  while (index < frames.length-1 && remaining >= durations[index]) remaining -= durations[index++];
  const frame = typeof frames[index] === 'number' ? frames[index] : frames[index].index;
  return {x:frame%columns*width,y:Math.floor(frame/columns)*height,width,height};
}
