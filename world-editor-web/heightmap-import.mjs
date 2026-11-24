import { copyFileSync, existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import path from "node:path";

function parseArgs(argv) {
  const options = {
    heightmap: "",
    width: 0,
    depth: 0,
    scale: 8,
    base: "",
    out: "",
    seaLevel: 64,
    worldHeight: 0,
    id: "",
  };
  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i];
    const next = () => argv[++i];
    if (arg === "--heightmap") options.heightmap = next();
    else if (arg === "--width") options.width = Number(next());
    else if (arg === "--depth") options.depth = Number(next());
    else if (arg === "--scale") options.scale = Number(next());
    else if (arg === "--base") options.base = next();
    else if (arg === "--out") options.out = next();
    else if (arg === "--sea-level") options.seaLevel = Number(next());
    else if (arg === "--world-height") options.worldHeight = Number(next());
    else if (arg === "--id") options.id = next();
  }
  return options;
}

function main() {
  const options = parseArgs(process.argv.slice(2));
  const width = options.width;
  const depth = options.depth;
  if (!options.heightmap || !options.base || !options.out
      || !Number.isInteger(width) || !Number.isInteger(depth) || width <= 0 || depth <= 0) {
    console.error("usage: node heightmap-import.mjs --heightmap raw16le --width W --depth D --base <classified-map> --out <map> [--scale 8] [--sea-level 64] [--world-height 1536] [--id id]");
    process.exit(2);
  }
  if (!(options.scale > 0)) {
    throw new Error("scale must be > 0");
  }
  const cells = width * depth;
  const raw = readFileSync(options.heightmap);
  if (raw.length !== cells * 2) {
    throw new Error(`heightmap size mismatch: ${raw.length}, expected ${cells * 2}`);
  }
  const baseMap = JSON.parse(readFileSync(path.join(options.base, "map.json"), "utf8"));
  if (baseMap.width !== width || baseMap.depth !== depth) {
    throw new Error(`base map size mismatch: ${baseMap.width}x${baseMap.depth}`);
  }
  const worldHeight = options.worldHeight || Number(baseMap.world_height) || 1024;
  if (!(worldHeight > 1)) {
    throw new Error("world_height must be > 1");
  }
  const maxY = worldHeight - 1;
  if (options.seaLevel < (baseMap.min_y ?? 0) || options.seaLevel > maxY) {
    throw new Error(`sea-level must be in [${baseMap.min_y ?? 0}, ${maxY}]`);
  }
  mkdirSync(options.out, { recursive: true });

  const heightData = Buffer.allocUnsafe(cells * 2);
  let minValue = Infinity;
  let maxValue = -Infinity;
  const factor = 16;
  const overviewWidth = Math.ceil(width / factor);
  const overviewDepth = Math.ceil(depth / factor);
  const overview = Buffer.alloc(overviewWidth * overviewDepth * 2);
  for (let z = 0; z < depth; z++) {
    for (let x = 0; x < width; x++) {
      const index = z * width + x;
      const value = raw.readUInt16LE(index * 2) * options.scale;
      const y = Math.max(1, Math.min(maxY, Math.round(value)));
      minValue = Math.min(minValue, y);
      maxValue = Math.max(maxValue, y);
      heightData.writeInt16LE(y, index * 2);
      if (x % factor === 0 && z % factor === 0) {
        const ox = Math.floor(x / factor);
        const oz = Math.floor(z / factor);
        overview.writeInt16LE(y, (oz * overviewWidth + ox) * 2);
      }
    }
  }
  writeFileSync(path.join(options.out, "heightmap.bin"), heightData);
  writeFileSync(path.join(options.out, "heightmap.bin.overview16.bin"), overview);

  const biomes = path.join(options.base, "biomes.bin");
  const surfaces = path.join(options.base, "surfaces.bin");
  copyFileSync(biomes, path.join(options.out, "biomes.bin"));
  copyFileSync(surfaces, path.join(options.out, "surfaces.bin"));
  for (const filename of ["biomes.bin.overview16.bin", "surfaces.bin.overview16.bin"]) {
    const source = path.join(options.base, filename);
    if (existsSync(source)) {
      copyFileSync(source, path.join(options.out, filename));
    }
  }
  writeFileSync(
    path.join(options.out, "underground.json"),
    `${JSON.stringify({ format_version: 1, blocks: [] }, null, 2)}\n`,
  );

  const centerX = Math.floor(width / 2);
  const centerZ = Math.floor(depth / 2);
  const centerIndex = centerZ * width + centerX;
  const spawnY = heightData.readInt16LE(centerIndex * 2) + 1;
  const map = {
    ...baseMap,
    id: options.id || baseMap.id,
    width,
    depth,
    world_height: worldHeight,
    sea_level: options.seaLevel,
    spawn: {
      ...(baseMap.spawn ?? {}),
      pos: [(baseMap.origin?.[0] ?? 0) + centerX, spawnY, (baseMap.origin?.[1] ?? 0) + centerZ],
    },
    layers: {
      heightmap: "heightmap.bin",
      biomes: "biomes.bin",
      surfaces: "surfaces.bin",
    },
    anchors: baseMap.anchors ?? [],
  };
  writeFileSync(path.join(options.out, "map.json"), `${JSON.stringify(map, null, 2)}\n`);
  console.log(JSON.stringify({
    width,
    depth,
    scale: options.scale,
    minY: minValue,
    maxY: maxValue,
    seaLevel: options.seaLevel,
    out: options.out,
  }, null, 2));
}

main();