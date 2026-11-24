import { readFileSync } from "node:fs";

function parseArgs(argv) {
  const options = {
    pngRaw: "",
    tiffRaw: "",
    classes: "",
    width: 0,
    depth: 0,
    sample: 1,
  };
  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i];
    const next = () => argv[++i];
    if (arg === "--png-raw") options.pngRaw = next();
    else if (arg === "--tiff-raw") options.tiffRaw = next();
    else if (arg === "--classes") options.classes = next();
    else if (arg === "--width") options.width = Number(next());
    else if (arg === "--depth") options.depth = Number(next());
    else if (arg === "--sample") options.sample = Math.max(1, Number(next()) || 1);
  }
  return options;
}

const CLASS_NAMES = ["water", "darkForest", "plains", "savanna", "desert", "snow", "stone", "red", "cherry"];

function main() {
  const options = parseArgs(process.argv.slice(2));
  const width = options.width;
  const depth = options.depth;
  if (!options.pngRaw || !options.tiffRaw || !options.classes || width <= 0 || depth <= 0) {
    console.error("usage: node compare-map-images.mjs --png-raw bgra --tiff-raw raw16le --classes biomes.bin --width W --depth D [--sample 17]");
    process.exit(2);
  }
  const cells = width * depth;
  const png = readFileSync(options.pngRaw);
  const tiff = readFileSync(options.tiffRaw);
  const classes = readFileSync(options.classes);
  if (png.length < cells * 4 || tiff.length < cells * 2 || classes.length < cells) {
    throw new Error("input size mismatch");
  }

  const classHeight = Array.from({ length: CLASS_NAMES.length }, () => new Array(13).fill(0));
  const classTotals = new Array(CLASS_NAMES.length).fill(0);
  const heightTotals = new Array(13).fill(0);
  let samples = 0;
  let waterClass = 0;
  let minHeight = 0;
  let waterIntersectionMin = 0;
  let waterUnionMin = 0;
  let waterIntersectionLe = 0;
  let waterUnionLe = 0;
  let blackPixels = 0;
  let blackOnHeightEdge = 0;
  let blackOnEdge4 = 0;

  for (let z = 0; z < depth; z += options.sample) {
    for (let x = 0; x < width; x += options.sample) {
      const index = z * width + x;
      const offset = index * 4;
      const b = png[offset];
      const g = png[offset + 1];
      const r = png[offset + 2];
      const height = tiff.readUInt16LE(index * 2);
      const cls = classes[index] < CLASS_NAMES.length ? classes[index] : 0;
      if (height >= 0 && height < 13) {
        classHeight[cls][height]++;
        classTotals[cls]++;
        heightTotals[height]++;
        if (height === 8) minHeight++;
        samples++;
      }
      const isWater = cls === 0;
      if (isWater) waterClass++;
      const hMin = height <= 8;
      if (isWater && hMin) waterIntersectionMin++;
      if (isWater || hMin) waterUnionMin++;
      const hLe = height <= 9;
      if (isWater && hLe) waterIntersectionLe++;
      if (isWater || hLe) waterUnionLe++;

      const luma = 0.299 * r + 0.587 * g + 0.114 * b;
      if (luma < 38 && Math.max(r, g, b) < 55) {
        blackPixels++;
        let min = 255;
        let max = 0;
        let min4 = 255;
        let max4 = 0;
        for (let dy = -2; dy <= 2; dy++) {
          const yy = z + dy;
          if (yy < 0 || yy >= depth) continue;
          for (let dx = -2; dx <= 2; dx++) {
            const xx = x + dx;
            if (xx < 0 || xx >= width) continue;
            const value = tiff.readUInt16LE((yy * width + xx) * 2);
            if (value < min) min = value;
            if (value > max) max = value;
            if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1) {
              if (value < min4) min4 = value;
              if (value > max4) max4 = value;
            }
          }
        }
        if (min !== max) blackOnHeightEdge++;
        if (min4 !== max4) blackOnEdge4++;
      }
    }
  }

  const heightStats = heightTotals.map((count, height) => ({
    height,
    count,
    percent: samples ? (count * 100 / samples) : 0,
  }));
  const classStats = CLASS_NAMES.map((name, cls) => {
    const total = classTotals[cls];
    let weighted = 0;
    const distribution = classHeight[cls].map((count, height) => ({ height, count }));
    for (const entry of distribution) weighted += entry.height * entry.count;
    return {
      cls,
      name,
      total,
      percent: samples ? (total * 100 / samples) : 0,
      meanHeight: total ? weighted / total : null,
      distribution,
    };
  });

  console.log(JSON.stringify({
    width,
    depth,
    sample: options.sample,
    samples,
    heightStats,
    classStats,
    water: {
      classPixels: waterClass,
      height8Pixels: minHeight,
      iouHeight8: waterUnionMin ? waterIntersectionMin / waterUnionMin : null,
      iouHeightLe9: waterUnionLe ? waterIntersectionLe / waterUnionLe : null,
    },
    contours: {
      blackPixels,
      alignedWithin2: blackPixels ? blackOnHeightEdge / blackPixels : null,
      alignedWithin1: blackPixels ? blackOnEdge4 / blackPixels : null,
    },
  }, null, 2));
}

main();