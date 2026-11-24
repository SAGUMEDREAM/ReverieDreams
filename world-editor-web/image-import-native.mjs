import { closeSync, mkdirSync, openSync, readSync, writeFileSync, writeSync } from "node:fs";
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

const TILE_ROWS = 128;
const BLUR_RADIUS = 4;
const NEIGHBOR_RADIUS = 2;
const PREVIEW_STEP = 8;

function parseArgs(argv) {
  const options = {
    raw: "",
    width: 0,
    height: 0,
    out: "",
    id: "reverie_dreams:gensokyo_preclassic",
    seaLevel: 64,
    bgra: true,
  };
  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i];
    const next = () => argv[++i];
    if (arg === "--raw") options.raw = next();
    else if (arg === "--width") options.width = Number(next());
    else if (arg === "--height") options.height = Number(next());
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

  if (b > 110 && b > r * 1.35 && b > g * 1.2) return CLASS.water;
  if (r > 75 && r > g * 1.4 && r > b * 1.2) {
    if (g > 45 && b > 65 && b > g * 1.08) return CLASS.cherry;
    return CLASS.red;
  }
  if (r > 105 && g > 105 && b < 95 && g > b * 1.45) return CLASS.desert;
  if (luma > 172 && sat < 72) return CLASS.snow;
  if (sat < 28 && luma > 58) return CLASS.stone;
  if (r >= g * 0.8 && g > b * 1.15 && luma < 125) return CLASS.savanna;
  if (g >= r * 1.08 && g >= b * 1.08) return luma > 104 ? CLASS.plains : CLASS.darkForest;
  if (max < 35) return CLASS.unknown;
  if (r > g && r > b) return CLASS.red;
  return luma > 120 ? CLASS.stone : CLASS.darkForest;
}

function fillUnknown(classes, rows, width) {
  for (let y = 0; y < rows; y++) {
    for (let x = 0; x < width; x++) {
      const index = y * width + x;
      if (classes[index] !== CLASS.unknown) continue;
      const local = new Array(CLASS_INFO.length).fill(0);
      for (let dy = -NEIGHBOR_RADIUS; dy <= NEIGHBOR_RADIUS; dy++) {
        const yy = y + dy;
        if (yy < 0 || yy >= rows) continue;
        for (let dx = -NEIGHBOR_RADIUS; dx <= NEIGHBOR_RADIUS; dx++) {
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
    }
  }
}

function main() {
  const options = parseArgs(process.argv.slice(2));
  const width = options.width;
  const height = options.height;
  if (!options.raw || !options.out || !Number.isInteger(width) || !Number.isInteger(height) || width <= 0 || height <= 0) {
    console.error("usage: node image-import-native.mjs --raw image.rgba --width 12800 --height 10240 --out dir [--rgba] [--sea-level 64]");
    process.exit(2);
  }
  const stride = width * 4;
  mkdirSync(options.out, { recursive: true });
  const rawFd = openSync(options.raw, "r");
  const heightFd = openSync(path.join(options.out, "heightmap.bin"), "w");
  const biomeFd = openSync(path.join(options.out, "biomes.bin"), "w");
  const surfaceFd = openSync(path.join(options.out, "surfaces.bin"), "w");

  const previewWidth = Math.ceil(width / PREVIEW_STEP);
  const previewHeight = Math.ceil(height / PREVIEW_STEP);
  const preview = Buffer.alloc(previewWidth * previewHeight * 3);
  const counts = new Array(CLASS_INFO.length).fill(0);
  let unknown = 0;

  const tileCount = Math.ceil(height / TILE_ROWS);
  for (let tile = 0; tile < tileCount; tile++) {
    const outY0 = tile * TILE_ROWS;
    const outY1 = Math.min(height, outY0 + TILE_ROWS);
    const readY0 = Math.max(0, outY0 - BLUR_RADIUS);
    const readY1 = Math.min(height, outY1 + BLUR_RADIUS);
    const rows = readY1 - readY0;
    const rawBuffer = Buffer.allocUnsafe(rows * stride);
    readSync(rawFd, rawBuffer, 0, rawBuffer.length, readY0 * stride);

    const classes = new Uint8Array(rows * width);
    const luma = new Float32Array(rows * width);
    for (let y = 0; y < rows; y++) {
      const rowOffset = y * stride;
      const classOffset = y * width;
      for (let x = 0; x < width; x++) {
        const offset = rowOffset + x * 4;
        const c0 = rawBuffer[offset];
        const c1 = rawBuffer[offset + 1];
        const c2 = rawBuffer[offset + 2];
        const r = options.bgra ? c2 : c0;
        const g = c1;
        const b = options.bgra ? c0 : c2;
        const value = 0.299 * r + 0.587 * g + 0.114 * b;
        luma[classOffset + x] = value;
        const cls = rgbToClass(r, g, b);
        classes[classOffset + x] = cls;
        if (cls === CLASS.unknown) unknown++;
      }
    }
    fillUnknown(classes, rows, width);

    const integralWidth = width + 1;
    const integral = new Uint32Array(integralWidth * (rows + 1));
    for (let y = 0; y < rows; y++) {
      let rowSum = 0;
      const row = y * width;
      const integralRow = (y + 1) * integralWidth;
      const prevRow = y * integralWidth;
      for (let x = 0; x < width; x++) {
        rowSum += luma[row + x];
        integral[integralRow + x + 1] = integral[prevRow + x + 1] + rowSum;
      }
    }

    const outRows = outY1 - outY0;
    const heightBuffer = Buffer.allocUnsafe(outRows * width * 2);
    const surfaceBuffer = Buffer.allocUnsafe(outRows * width * 2);
    const biomeBuffer = Buffer.allocUnsafe(outRows * width);

    for (let oy = outY0; oy < outY1; oy++) {
      const localY = oy - readY0;
      const outRow = oy - outY0;
      const globalY0 = Math.max(0, oy - BLUR_RADIUS);
      const globalY1 = Math.min(height - 1, oy + BLUR_RADIUS);
      const localY0 = globalY0 - readY0;
      const localY1 = globalY1 - readY0;
      for (let x = 0; x < width; x++) {
        const cls = classes[localY * width + x];
        const info = CLASS_INFO[cls];
        let value;
        if (cls === CLASS.water) {
          value = options.seaLevel - 2;
        } else {
          const globalX0 = Math.max(0, x - BLUR_RADIUS);
          const globalX1 = Math.min(width - 1, x + BLUR_RADIUS);
          const sum = integral[(localY1 + 1) * integralWidth + globalX1 + 1]
            - integral[localY0 * integralWidth + globalX1 + 1]
            - integral[(localY1 + 1) * integralWidth + globalX0]
            + integral[localY0 * integralWidth + globalX0];
          const area = (globalX1 - globalX0 + 1) * (globalY1 - globalY0 + 1);
          const average = sum / area;
          const t = Math.max(0, Math.min(1, average / 220));
          value = Math.round(info.minY + (info.maxY - info.minY) * t);
          value = Math.max(1, Math.min(1000, value));
        }
        const index = outRow * width + x;
        heightBuffer.writeInt16LE(value, index * 2);
        surfaceBuffer.writeUInt16LE(cls, index * 2);
        biomeBuffer[index] = cls;
        counts[cls]++;

        if (oy % PREVIEW_STEP === 0 && x % PREVIEW_STEP === 0) {
          const px = Math.floor(x / PREVIEW_STEP);
          const py = Math.floor(oy / PREVIEW_STEP);
          const color = info.color;
          const offset = (py * previewWidth + px) * 3;
          preview[offset] = color[0];
          preview[offset + 1] = color[1];
          preview[offset + 2] = color[2];
        }
      }
    }

    writeSync(heightFd, heightBuffer);
    writeSync(surfaceFd, surfaceBuffer);
    writeSync(biomeFd, biomeBuffer);
    if (tile % 16 === 0 || tile === tileCount - 1) {
      console.log(`tile ${tile + 1}/${tileCount} rows ${outY0}..${outY1 - 1}`);
    }
  }

  closeSync(rawFd);
  closeSync(heightFd);
  closeSync(surfaceFd);
  closeSync(biomeFd);

  const origin = [0, 0];
  const centerX = Math.floor(width / 2);
  const centerZ = Math.floor(height / 2);
  const centerIndex = centerZ * width + centerX;
  // The center height is read from the finished heightmap rather than kept in memory.
  const heightFdRead = openSync(path.join(options.out, "heightmap.bin"), "r");
  const centerBytes = Buffer.alloc(2);
  readSync(heightFdRead, centerBytes, 0, 2, centerIndex * 2);
  closeSync(heightFdRead);
  const spawnY = Math.max(1, centerBytes.readInt16LE(0) + 1);
  const surfacePalette = CLASS_INFO.map((info) => info.surface);
  const biomePalette = CLASS_INFO.map((info) => info.biome);
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
  writeFileSync(path.join(options.out, "underground.json"), `${JSON.stringify({ format_version: 1, blocks: [] }, null, 2)}\n`);
  writeFileSync(path.join(options.out, "preview.rgb"), preview);
  writeFileSync(path.join(options.out, "preview.json"), `${JSON.stringify({ width: previewWidth, height: previewHeight }, null, 2)}\n`);

  const summary = {};
  for (let i = 0; i < CLASS_INFO.length; i++) summary[CLASS_INFO[i].key] = counts[i];
  console.log(JSON.stringify({ width, height, unknown, classes: summary, out: options.out }, null, 2));
}

main();