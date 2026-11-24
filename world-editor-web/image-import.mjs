import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import path from "node:path";

const CLASS = {
  water: 0,
  darkForest: 1,
  plains: 2,
  savanna: 3,
  desert: 4,
  snow: 5,
  stone: 6,
  red: 7,
  cherry: 8,
  unknown: 255,
};

const CLASS_INFO = [
  { key: "water", biome: "minecraft:river", surface: "minecraft:water", color: [45, 95, 225], minY: 62, maxY: 62 },
  { key: "darkForest", biome: "minecraft:forest", surface: "minecraft:grass_block", color: [34, 78, 42], minY: 64, maxY: 92 },
  { key: "plains", biome: "minecraft:plains", surface: "minecraft:grass_block", color: [108, 172, 98], minY: 66, maxY: 106 },
  { key: "savanna", biome: "minecraft:savanna", surface: "minecraft:grass_block", color: [92, 100, 48], minY: 70, maxY: 116 },
  { key: "desert", biome: "minecraft:desert", surface: "minecraft:sand", color: [200, 188, 42], minY: 72, maxY: 110 },
  { key: "snow", biome: "minecraft:snowy_plains", surface: "minecraft:snow_block", color: [238, 238, 238], minY: 120, maxY: 190 },
  { key: "stone", biome: "minecraft:stony_peaks", surface: "minecraft:stone", color: [128, 128, 128], minY: 90, maxY: 170 },
  { key: "red", biome: "minecraft:badlands", surface: "minecraft:red_sand", color: [164, 32, 44], minY: 70, maxY: 110 },
  { key: "cherry", biome: "minecraft:cherry_grove", surface: "minecraft:grass_block", color: [178, 98, 146], minY: 70, maxY: 100 },
];

function parseArgs(argv) {
  const options = {
    raw: "",
    rawWidth: 0,
    rawHeight: 0,
    out: "",
    id: "reverie_dreams:gensokyo_preclassic",
    seaLevel: 64,
    bgra: true,
  };
  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i];
    const next = () => argv[++i];
    if (arg === "--raw") options.raw = next();
    else if (arg === "--raw-width") options.rawWidth = Number(next());
    else if (arg === "--raw-height") options.rawHeight = Number(next());
    else if (arg === "--out") options.out = next();
    else if (arg === "--id") options.id = next();
    else if (arg === "--sea-level") options.seaLevel = Number(next());
    else if (arg === "--rgba") options.bgra = false;
  }
  return options;
}

function rgbToClass(r, g, b) {
  const max = Math.max(r, g, b);
  const min = Math.min(r, g, b);
  const sat = max - min;
  const luma = 0.299 * r + 0.587 * g + 0.114 * b;

  // Water is the only strongly blue class.
  if (b > 110 && b > r * 1.35 && b > g * 1.2) {
    return CLASS.water;
  }
  // Red and pink/magenta regions.
  if (r > 75 && r > g * 1.4 && r > b * 1.2) {
    if (g > 45 && b > 65 && b > g * 1.08) {
      return CLASS.cherry;
    }
    return CLASS.red;
  }
  // Yellow desert / dry grass.
  if (r > 105 && g > 105 && b < 95 && g > b * 1.45) {
    return CLASS.desert;
  }
  // White snow.
  if (luma > 172 && sat < 72) {
    return CLASS.snow;
  }
  // Neutral gray stone.
  if (sat < 28 && luma > 58) {
    return CLASS.stone;
  }
  // Olive/brown dry savanna. Check before generic green.
  if (r >= g * 0.8 && g > b * 1.15 && luma < 125) {
    return CLASS.savanna;
  }
  // Green classes split by brightness.
  if (g >= r * 1.08 && g >= b * 1.08) {
    return luma > 104 ? CLASS.plains : CLASS.darkForest;
  }
  if (max < 35) {
    return CLASS.unknown;
  }
  if (r > g && r > b) {
    return CLASS.red;
  }
  return luma > 120 ? CLASS.stone : CLASS.darkForest;
}

function main() {
  const options = parseArgs(process.argv.slice(2));
  if (!options.raw || !options.out || options.rawWidth <= 0 || options.rawHeight <= 0) {
    console.error("usage: node image-import.mjs --raw file.rgba --raw-width W --raw-height H --out dir [--bgra|--rgba] [--sea-level 64]");
    process.exit(2);
  }
  const width = options.rawWidth;
  const height = options.rawHeight;
  const pixels = width * height;
  const raw = readFileSync(options.raw);
  if (raw.length < pixels * 4) {
    throw new Error(`raw file too small: ${raw.length}, expected ${pixels * 4}`);
  }
  mkdirSync(options.out, { recursive: true });

  const classes = new Uint8Array(pixels);
  const luma = new Float32Array(pixels);
  const counts = new Array(CLASS_INFO.length).fill(0);
  let unknown = 0;

  for (let index = 0, offset = 0; index < pixels; index++, offset += 4) {
    const c0 = raw[offset];
    const c1 = raw[offset + 1];
    const c2 = raw[offset + 2];
    const r = options.bgra ? c2 : c0;
    const g = c1;
    const b = options.bgra ? c0 : c2;
    const value = 0.299 * r + 0.587 * g + 0.114 * b;
    luma[index] = value;
    const cls = rgbToClass(r, g, b);
    classes[index] = cls;
    if (cls === CLASS.unknown) unknown++;
    else counts[cls]++;
  }

  // Fill contour/grid black pixels from nearby non-unknown pixels.
  if (unknown > 0) {
    for (let y = 0; y < height; y++) {
      for (let x = 0; x < width; x++) {
        const index = y * width + x;
        if (classes[index] !== CLASS.unknown) continue;
        const local = new Array(CLASS_INFO.length).fill(0);
        for (let dy = -2; dy <= 2; dy++) {
          const yy = y + dy;
          if (yy < 0 || yy >= height) continue;
          for (let dx = -2; dx <= 2; dx++) {
            const xx = x + dx;
            if (xx < 0 || xx >= width) continue;
            const cls = classes[yy * width + xx];
            if (cls !== CLASS.unknown) local[cls]++;
          }
        }
        let best = CLASS.darkForest;
        let bestCount = -1;
        for (let cls = 0; cls < local.length; cls++) {
          if (local[cls] > bestCount) {
            best = cls;
            bestCount = local[cls];
          }
        }
        classes[index] = bestCount >= 0 ? best : CLASS.stone;
        counts[classes[index]]++;
      }
    }
  }

  // Integral image of luma for a broad, low-frequency height field.
  const integralWidth = width + 1;
  const integral = new Uint32Array(integralWidth * (height + 1));
  for (let y = 0; y < height; y++) {
    let rowSum = 0;
    const row = y * width;
    const integralRow = (y + 1) * integralWidth;
    const prevRow = y * integralWidth;
    for (let x = 0; x < width; x++) {
      rowSum += luma[row + x];
      integral[integralRow + x + 1] = integral[prevRow + x + 1] + rowSum;
    }
  }

  const heights = new Int16Array(pixels);
  const radius = 4;
  for (let y = 0; y < height; y++) {
    const y0 = Math.max(0, y - radius);
    const y1 = Math.min(height - 1, y + radius);
    for (let x = 0; x < width; x++) {
      const index = y * width + x;
      const cls = classes[index];
      const info = CLASS_INFO[cls];
      if (cls === CLASS.water) {
        heights[index] = options.seaLevel - 2;
        continue;
      }
      const x0 = Math.max(0, x - radius);
      const x1 = Math.min(width - 1, x + radius);
      const sum = integral[(y1 + 1) * integralWidth + x1 + 1]
        - integral[y0 * integralWidth + x1 + 1]
        - integral[(y1 + 1) * integralWidth + x0]
        + integral[y0 * integralWidth + x0];
      const area = (x1 - x0 + 1) * (y1 - y0 + 1);
      const average = sum / area;
      const t = Math.max(0, Math.min(1, average / 220));
      const value = Math.round(info.minY + (info.maxY - info.minY) * t);
      heights[index] = Math.max(1, Math.min(1000, value));
    }
  }

  const surfacePalette = CLASS_INFO.map((info) => info.surface);
  const biomePalette = CLASS_INFO.map((info) => info.biome);
  const surfaceIndex = new Uint16Array(pixels);
  const biomeIndex = new Uint8Array(pixels);
  for (let i = 0; i < pixels; i++) {
    surfaceIndex[i] = classes[i];
    biomeIndex[i] = classes[i];
  }

  const heightBuffer = Buffer.alloc(pixels * 2);
  const surfaceBuffer = Buffer.alloc(pixels * 2);
  for (let i = 0; i < pixels; i++) {
    heightBuffer.writeInt16LE(heights[i], i * 2);
    surfaceBuffer.writeUInt16LE(surfaceIndex[i], i * 2);
  }
  const biomeBuffer = Buffer.from(biomeIndex);
  writeFileSync(path.join(options.out, "heightmap.bin"), heightBuffer);
  writeFileSync(path.join(options.out, "biomes.bin"), biomeBuffer);
  writeFileSync(path.join(options.out, "surfaces.bin"), surfaceBuffer);
  writeFileSync(
    path.join(options.out, "underground.json"),
    `${JSON.stringify({ format_version: 1, blocks: [] }, null, 2)}\n`,
  );

  const origin = [-Math.floor(width / 2), -Math.floor(height / 2)];
  const centerX = Math.floor(width / 2);
  const centerZ = Math.floor(height / 2);
  const centerIndex = centerZ * width + centerX;
  const spawnY = Math.max(1, heights[centerIndex] + 1);
  const map = {
    format_version: 1,
    map_version: 2,
    id: options.id,
    width,
    depth: height,
    origin,
    min_y: 0,
    world_height: 1536,
    surface_bits: 16,
    sea_level: options.seaLevel,
    dirt_depth: 3,
    spawn: {
      pos: [origin[0] + centerX, spawnY, origin[1] + centerZ],
      yaw: 0,
      pitch: 0,
    },
    biome_palette: biomePalette,
    surface_palette: surfacePalette,
    layers: {
      heightmap: "heightmap.bin",
      biomes: "biomes.bin",
      surfaces: "surfaces.bin",
    },
    anchors: [],
  };
  writeFileSync(path.join(options.out, "map.json"), `${JSON.stringify(map, null, 2)}\n`);

  // Cropped RGB preview at 1/4 resolution.
  const step = 4;
  const previewWidth = Math.ceil(width / step);
  const previewHeight = Math.ceil(height / step);
  const preview = Buffer.alloc(previewWidth * previewHeight * 3);
  for (let py = 0; py < previewHeight; py++) {
    for (let px = 0; px < previewWidth; px++) {
      const cls = classes[Math.min(height - 1, py * step) * width + Math.min(width - 1, px * step)];
      const color = CLASS_INFO[cls].color;
      const offset = (py * previewWidth + px) * 3;
      preview[offset] = color[0];
      preview[offset + 1] = color[1];
      preview[offset + 2] = color[2];
    }
  }
  writeFileSync(path.join(options.out, "preview.rgb"), preview);
  writeFileSync(
    path.join(options.out, "preview.json"),
    `${JSON.stringify({ width: previewWidth, height: previewHeight }, null, 2)}\n`,
  );

  const summary = {};
  for (let i = 0; i < CLASS_INFO.length; i++) {
    summary[CLASS_INFO[i].key] = counts[i];
  }
  console.log(JSON.stringify({ width, height, unknown, classes: summary, out: options.out }, null, 2));
}

main();