/**
 * Deterministic biome transition sampling shared by the editor preview.
 *
 * The Java BiomeSource samples at 4x4 quart resolution, while the editor can
 * preview per block. Both use the same hash/offset formula and transition
 * width; the editor preview is simply at a finer resolution.
 */

export function hash32(x, z, seed) {
  let h = (Math.imul(x | 0, 374761393)
    + Math.imul(z | 0, 668265263)
    + Math.imul(seed | 0, 1442695041)) | 0;
  h = Math.imul(h ^ (h >>> 13), 1274126177);
  return h ^ (h >>> 16);
}

export function transitionOffset(x, z, seed, width) {
  const safeWidth = Math.max(0, Math.min(64, Number(width) | 0));
  const span = safeWidth * 2 + 1;
  const value = hash32(x, z, seed);
  return ((value % span) + span) % span - safeWidth;
}

/**
 * Returns the biome index after transition sampling.
 *
 * @param {object} map map.json object (width/depth/origin/biome_transition)
 * @param {ArrayLike<number>} biomes biome layer
 * @param {number} x local cell x
 * @param {number} z local cell z
 */
export function transitionBiomeIndex(map, biomes, x, z) {
  if (!map || !biomes) {
    return 0;
  }
  const width = map.width | 0;
  const depth = map.depth | 0;
  const cellX = x | 0;
  const cellZ = z | 0;
  if (cellX < 0 || cellZ < 0 || cellX >= width || cellZ >= depth) {
    return 0;
  }
  const baseIndex = biomes[cellZ * width + cellX];
  const transition = Math.max(0, Math.min(64, Number(map.biome_transition ?? 0) | 0));
  if (transition <= 0) {
    return baseIndex;
  }
  const dx = transitionOffset(cellX, cellZ, 0, transition);
  const dz = transitionOffset(cellX, cellZ, 1, transition);
  const sampleX = cellX + dx;
  const sampleZ = cellZ + dz;
  if (sampleX < 0 || sampleZ < 0 || sampleX >= width || sampleZ >= depth) {
    return baseIndex;
  }
  return biomes[sampleZ * width + sampleX];
}