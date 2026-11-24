// Companion to GensokyoBorderTerrain.java. Shared settings and Java-exported parity vectors prevent drift.
const clamp = (v, low, high) => Math.max(low, Math.min(high, v));
const lerp = (a, b, t) => a + (b - a) * t;
const smooth = (v) => { const t = clamp(v, 0, 1); return t * t * (3 - 2 * t); };
function hash(x, z) {
  let h = (Math.imul(x, 374761393) + Math.imul(z, 668265263)) | 0;
  h = Math.imul(h ^ (h >>> 13), 1274126177);
  h ^= h >>> 16;
  return (h & 0xffffff) / 8388607.5 - 1;
}
function noise(x, z) {
  const x0 = Math.floor(x), z0 = Math.floor(z);
  return lerp(lerp(hash(x0, z0), hash(x0 + 1, z0), smooth(x - x0)),
    lerp(hash(x0, z0 + 1), hash(x0 + 1, z0 + 1), smooth(x - x0)), smooth(z - z0));
}
function fbm(x, z) {
  let sum = 0, amplitude = 1, frequency = 1, total = 0;
  for (let i = 0; i < 4; i++) {
    sum += noise(x * frequency, z * frequency) * amplitude;
    total += amplitude; amplitude *= .5; frequency *= 2.03;
  }
  return sum / total;
}
function riverHash(x, z, salt) {
  let v = BigInt.asUintN(64, BigInt(x) * 341873128712n ^ BigInt(z) * 132897987541n ^ salt);
  v = BigInt.asUintN(64, (v ^ v >> 30n) * 0xbf58476d1ce4e5b9n);
  v = BigInt.asUintN(64, (v ^ v >> 27n) * 0x94d049bb133111ebn);
  v ^= v >> 31n;
  return Number(v >> 11n) * 2 ** -53 * 2 - 1;
}
function riverNoise(x, z, scale, salt) {
  const px = x / scale, pz = z / scale, ix = Math.floor(px), iz = Math.floor(pz);
  return lerp(lerp(riverHash(ix, iz, salt), riverHash(ix + 1, iz, salt), smooth(px - ix)),
    lerp(riverHash(ix, iz + 1, salt), riverHash(ix + 1, iz + 1, salt), smooth(px - ix)), smooth(pz - iz));
}
function riverDepth(x, z) {
  return clamp(Math.round(5 + riverNoise(x, z, 32, 0x6a09e667f3bcc909n) * 2.1
    + riverNoise(x, z, 10, 0xbb67ae8584caa73bn) * .65 + riverNoise(x, z, 3, 0x3c6ef372fe94f82bn) * .2), 3, 8);
}
function riverMaterial(x, z) {
  const deposit = riverNoise(x, z, 23, 0xa54ff53a5f1d36f1n);
  if (deposit < -.38) return 'minecraft:sand';
  if (deposit > .45) return 'minecraft:clay';
  return riverNoise(x, z, 9, 0x510e527fade682d1n) > .58 ? 'minecraft:stone' : 'minecraft:gravel';
}
export function substrate(surface) {
  if (new Set(['minecraft:stone', 'minecraft:andesite', 'minecraft:granite', 'minecraft:diorite', 'minecraft:gravel',
    'minecraft:sand', 'minecraft:red_sand', 'minecraft:podzol', 'minecraft:mycelium', 'minecraft:coarse_dirt',
    'minecraft:rooted_dirt', 'minecraft:snow_block', 'minecraft:basalt', 'minecraft:calcite', 'minecraft:moss_block']).has(surface)) return surface;
  if (surface === 'minecraft:snow') return 'minecraft:snow_block';
  return surface === 'minecraft:lava' ? 'minecraft:basalt' : 'minecraft:grass_block';
}
const coldBiome = (biome) => biome.includes('snow') || biome.includes('frozen') || biome.includes('ice_spikes') || biome.endsWith(':grove');
const terrestrial = (biome) => !['ocean', 'river', 'swamp', 'nether', 'end_'].some(v => biome.includes(v))
  && !biome.endsWith(':the_end') && !biome.endsWith(':the_void');

export function createBorderTerrain(map, heights, surfaces, biomes, settings) {
  if (!settings || settings.version !== 2) throw new Error('缺少匹配版本的外围地形参数');
  const [ox, oz] = map.origin ?? [0, 0], sea = map.sea_level, minY = map.min_y;
  const width = clamp(Math.floor(Math.min(map.width, map.depth) / settings.width_divisor), settings.min_width, settings.max_width);
  const ex = x => clamp(x, ox, ox + map.width - 1), ez = z => clamp(z, oz, oz + map.depth - 1);
  const index = (x, z) => (ez(z) - oz) * map.width + ex(x) - ox;
  const height = (x, z) => { const h = heights[index(x, z)]; return h === -32768 ? sea : h; };
  const surface = (x, z) => map.surface_palette[surfaces[index(x, z)]] ?? 'minecraft:grass_block';
  const biome = (x, z) => map.biome_palette[biomes[index(x, z)]] ?? 'minecraft:plains';
  function distance(x, z) {
    return Math.hypot(Math.max(0, ox - x, x - (ox + map.width - 1)), Math.max(0, oz - z, z - (oz + map.depth - 1)));
  }
  function sample(x, z) {
    const edgeX = ex(x), edgeZ = ez(z), dist = distance(x, z), d = Math.max(0, dist - 1);
    const edge = height(edgeX, edgeZ), edgeSurface = surface(edgeX, edgeZ), edgeBiome = biome(edgeX, edgeZ);
    const edgeWater = edgeSurface === 'minecraft:water';
    const cold = coldBiome(edgeBiome) || ['minecraft:snow_block', 'minecraft:snow'].includes(edgeSurface);
    const alongX = x === edgeX || (z !== edgeZ && Math.abs(x - edgeX) < Math.abs(z - edgeZ));
    const ax = alongX ? 1 : 0, az = alongX ? 0 : 1;
    let smoothEdge = (height(edgeX - ax * 24, edgeZ - az * 24) + height(edgeX - ax * 12, edgeZ - az * 12) * 2
      + edge * 4 + height(edgeX + ax * 12, edgeZ + az * 12) * 2 + height(edgeX + ax * 24, edgeZ + az * 24)) / 10;
    const nx = x - edgeX, nz = z - edgeZ, length = Math.hypot(nx, nz);
    if (nx !== 0 && nz !== 0) {
      const sx = (height(edgeX - 24, edgeZ) + height(edgeX - 12, edgeZ) * 2 + edge * 4 + height(edgeX + 12, edgeZ) * 2 + height(edgeX + 24, edgeZ)) / 10;
      const sz = (height(edgeX, edgeZ - 24) + height(edgeX, edgeZ - 12) * 2 + edge * 4 + height(edgeX, edgeZ + 12) * 2 + height(edgeX, edgeZ + 24)) / 10;
      smoothEdge = (sx * Math.abs(nz) + sz * Math.abs(nx)) / (Math.abs(nx) + Math.abs(nz));
    }
    const ix = ex(edgeX - Math.round(length === 0 ? 0 : nx / length * settings.context_depth));
    const iz = ez(edgeZ - Math.round(length === 0 ? 0 : nz / length * settings.context_depth));
    const gradient = edgeWater ? 0 : clamp((edge - height(ix, iz)) / settings.context_depth, -.65, .65);
    const base = lerp(edge, smoothEdge, smooth(d / 96)) + gradient * d * Math.exp(-d / 80);
    const warpX = x + fbm(x / 340, z / 340) * 70, warpZ = z + fbm(x / 340 + 71, z / 340 - 29) * 70;
    const broad = fbm(warpX / 420, warpZ / 420);
    const ridge = (1 - Math.abs(fbm(warpX / 145 + 31, warpZ / 145 - 11))) ** 2;
    const localWidth = width * (1 + fbm(x / 650 + 93, z / 650) * .85);
    const exponent = clamp(1 + fbm(warpX / 260 + 19, warpZ / 260 - 59) * .8, .65, 1.35);
    const progress = Math.pow(smooth(d / localWidth), exponent);
    const target = clamp(sea + settings.mountain_offset + broad * settings.broad_relief + ridge * settings.ridge_relief, minY + 8, minY + map.world_height - 13);
    const foothills = fbm(warpX / 65, warpZ / 65) * 20 * smooth(d / 100) * (1 - progress * .5);
    let terrain = lerp(base, target, progress) + foothills, waterY = -2147483648;
    if (edgeWater) {
      let shore = 32;
      for (let step = 1; step <= 32; step++) {
        if (surface(edgeX - ax * step, edgeZ - az * step) !== 'minecraft:water' || surface(edgeX + ax * step, edgeZ + az * step) !== 'minecraft:water') { shore = step; break; }
      }
      const reach = clamp(width * settings.water_reach_fraction, 48, 192) * (.45 + .55 * smooth(shore / 24))
        * (1 + noise(x / 80 + 17, z / 80) * .18);
      if (d < reach) {
        const depth = 3 + Math.round((riverDepth(x, z) - 3) * smooth(d / 24));
        terrain = lerp(edge - depth, terrain, smooth((d / reach - .55) / .45));
        if (Math.round(terrain) < edge) waterY = edge;
      }
    }
    const groundY = clamp(Math.round(terrain), minY + 1, minY + map.world_height - 13), water = waterY > groundY;
    let top;
    if (water) top = riverMaterial(x, z);
    else {
      const carry = 1 - smooth(d / clamp(width * settings.surface_blend_fraction, 48, 192));
      const patch = (noise(x / 13, z / 13) + 1) * .5;
      const snowLine = sea + (cold ? 20 : 300) + broad * 25;
      if (groundY >= snowLine) top = 'minecraft:snow_block';
      else if (dist <= 1 || patch < carry) top = substrate(edgeSurface);
      else if (groundY >= sea + 115 + broad * 45 || noise(x / 24 + 51, z / 24) > .55) top = 'minecraft:stone';
      else top = 'minecraft:grass_block';
    }
    const band = d / width + noise(x / 50 + 143, z / 50 - 71) * .07;
    let selected;
    if (water) selected = cold ? 'minecraft:frozen_river' : 'minecraft:river';
    else if (band < .22 && !edgeWater && terrestrial(edgeBiome)) selected = edgeBiome;
    else if (groundY > sea + 300) selected = 'minecraft:frozen_peaks';
    else if (groundY > sea + 285 || band > .76) selected = cold ? 'minecraft:frozen_peaks' : 'minecraft:stony_peaks';
    else if (groundY > sea + 150 || band > .45) selected = cold ? 'minecraft:snowy_slopes' : 'minecraft:meadow';
    else selected = cold ? 'minecraft:grove' : 'minecraft:windswept_forest';
    return { groundY, waterY, surface: top, biome: selected, visibleY: water ? waterY : groundY, water };
  }
  return { width, distance, sample };
}
