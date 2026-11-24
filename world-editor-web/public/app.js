import { Terrain3DView } from "./terrain3d.js";
import { BlockPreview, describeBlockstate, fetchBlockstate } from "./blockmodels.js";
import { transitionBiomeIndex } from "./biome-transition.js";
import { BlockIconRenderer } from "./blockicons.js";
import { abortSave, beginSave, commitSave, putLayer, syncRuntimeMap, uploadMap, uploadUnderground } from "./save-api.js";

const canvas = document.querySelector("#mapCanvas");
const ctx = canvas.getContext("2d");
const terrainCanvas = document.querySelector("#terrainCanvas");
const viewportStage = document.querySelector("#viewportStage");
const minimap = document.querySelector("#minimap");
const minimapCtx = minimap.getContext("2d");
let terrainView = null;
try {
  terrainView = new Terrain3DView(terrainCanvas);
  terrainView.setCanvasSize(canvas.width, canvas.height);
} catch (error) {
  console.error("WebGL 3D 初始化失败", error);
}
const blockPreviewCanvas = document.querySelector("#blockPreview");
let blockPreview = null;
try {
  blockPreview = new BlockPreview(blockPreviewCanvas);
} catch (error) {
  console.error("方块模型预览初始化失败", error);
}
let blockIcons = null;
try {
  blockIcons = new BlockIconRenderer();
} catch (error) {
  console.error("方块图标渲染器初始化失败", error);
}

const layerButtons = document.querySelector("#layerButtons");
const editModeButtons = document.querySelector("#editModeButtons");
const heightTools = document.querySelector("#heightTools");
const heightToolButtons = document.querySelector("#heightToolButtons");
const biomePaletteSection = document.querySelector("#biomePaletteSection");
const surfacePaletteSection = document.querySelector("#surfacePaletteSection");
const biomePaletteEl = document.querySelector("#biomePalette");
const biomeSearch = document.querySelector("#biomeSearch");
const biomeCount = document.querySelector("#biomeCount");
const biomeCandidatesEl = document.querySelector("#biomeCandidates");
const biomeCandidateSearch = document.querySelector("#biomeCandidateSearch");
const biomeCandidateCount = document.querySelector("#biomeCandidateCount");
const surfacePaletteEl = document.querySelector("#surfacePalette");
const brushStrengthSection = document.querySelector("#brushStrengthSection");
const brushStrengthInput = document.querySelector("#brushStrength");
const brushStrengthValue = document.querySelector("#brushStrengthValue");
const actualModeSection = document.querySelector("#actualModeSection");
const actualTerrainToggle = document.querySelector("#actualTerrainToggle");
const terrain3dDisplaySection = document.querySelector("#terrain3dDisplaySection");
const terrain3dExaggeration = document.querySelector("#terrain3dExaggeration");
const terrain3dExaggerationValue = document.querySelector("#terrain3dExaggerationValue");
const terrain3dXzScale = document.querySelector("#terrain3dXzScale");
const terrain3dXzScaleValue = document.querySelector("#terrain3dXzScaleValue");
const terrain3dDetail = document.querySelector("#terrain3dDetail");
const selectionInfo = document.querySelector("#selectionInfo");
const copySelectionBtn = document.querySelector("#copySelectionBtn");
const pasteSelectionBtn = document.querySelector("#pasteSelectionBtn");
const clearSelectionBtn = document.querySelector("#clearSelectionBtn");
const terrainTemplateName = document.querySelector("#terrainTemplateName");
const saveTerrainTemplateBtn = document.querySelector("#saveTerrainTemplateBtn");
const terrainTemplateList = document.querySelector("#terrainTemplateList");
const pasteTerrainTemplateBtn = document.querySelector("#pasteTerrainTemplateBtn");
const deleteTerrainTemplateBtn = document.querySelector("#deleteTerrainTemplateBtn");
const newMapWidth = document.querySelector("#newMapWidth");
const newMapDepth = document.querySelector("#newMapDepth");
const newMapBtn = document.querySelector("#newMapBtn");
const resizeMapBtn = document.querySelector("#resizeMapBtn");
const backupNowBtn = document.querySelector("#backupNowBtn");
const reloadMapBtn = document.querySelector("#reloadMapBtn");
const autoSaveToggle = document.querySelector("#autoSaveToggle");
const backupList = document.querySelector("#backupList");
const restoreBackupBtn = document.querySelector("#restoreBackupBtn");
const regenerateWorldPath = document.querySelector("#regenerateWorldPath");
const regenerateDimensionBtn = document.querySelector("#regenerateDimensionBtn");
const cancelRegenerateDimensionBtn = document.querySelector("#cancelRegenerateDimensionBtn");
const regenerateStatus = document.querySelector("#regenerateStatus");
const seaLevelInput = document.querySelector("#seaLevel");
const dirtDepthInput = document.querySelector("#dirtDepth");
const applyTerrainGlobalsBtn = document.querySelector("#applyTerrainGlobalsBtn");
const collabStatus = document.querySelector("#collabStatus");
const collabNameInput = document.querySelector("#collabName");
const collabConnectBtn = document.querySelector("#collabConnectBtn");
const collabUsers = document.querySelector("#collabUsers");
const collabCursors = document.querySelector("#collabCursors");
const blockSearch = document.querySelector("#blockSearch");
const blockList = document.querySelector("#blockList");
const blockUseBtn = document.querySelector("#blockUseBtn");
const blockGrid = document.querySelector("#blockGrid");
const blockCount = document.querySelector("#blockCount");
const blockStateSection = document.querySelector("#blockStateSection");
const blockStateName = document.querySelector("#blockStateName");
const blockStateProperties = document.querySelector("#blockStateProperties");
const biomeTransitionInput = document.querySelector("#biomeTransition");
const biomeTransitionValue = document.querySelector("#biomeTransitionValue");
const hotbar = document.querySelector("#hotbar");
const hotbarBlock = document.querySelector("#hotbarBlock");
let blockIconObserver = null;
let blockPrewarmTimer = null;
const undergroundToggle = document.querySelector("#undergroundToggle");
const undergroundYInput = document.querySelector("#undergroundY");
const undergroundYMinInput = document.querySelector("#undergroundYMin");
const undergroundYMaxInput = document.querySelector("#undergroundYMax");
const undergroundRangeFromSliceBtn = document.querySelector("#undergroundRangeFromSliceBtn");
const undergroundSliceFromRangeBtn = document.querySelector("#undergroundSliceFromRangeBtn");
const undergroundClearToggle = document.querySelector("#undergroundClearToggle");
const brushSizeInput = document.querySelector("#brushSize");
const brushSizeNumberInput = document.querySelector("#brushSizeInput");
const brushSizeValue = document.querySelector("#brushSizeValue");
const MAX_BRUSH_RADIUS = 512;
const gridToggle = document.querySelector("#gridToggle");
const waterToggle = document.querySelector("#waterToggle");
const saveBtn = document.querySelector("#saveBtn");
const syncBtn = document.querySelector("#syncBtn");
const importCommonBtn = document.querySelector("#importCommonBtn");
const undoBtn = document.querySelector("#undoBtn");
const redoBtn = document.querySelector("#redoBtn");
const placeModeBtn = document.querySelector("#placeModeBtn");
const toolTabs = document.querySelector("#toolTabs");
const dirtyState = document.querySelector("#dirtyState");
const metaEl = document.querySelector("#meta");
const logEl = document.querySelector("#log");
const cursorInfo = document.querySelector("#cursorInfo");
const spawnX = document.querySelector("#spawnX");
const spawnY = document.querySelector("#spawnY");
const spawnZ = document.querySelector("#spawnZ");
const spawnPickBtn = document.querySelector("#spawnPickBtn");
const anchorTemplate = document.querySelector("#anchorTemplate");
const anchorAddBtn = document.querySelector("#anchorAddBtn");
const anchorList = document.querySelector("#anchorList");
const anchorEditor = document.querySelector("#anchorEditor");
const anchorEditorName = document.querySelector("#anchorEditorName");
const anchorX = document.querySelector("#anchorX");
const anchorY = document.querySelector("#anchorY");
const anchorZ = document.querySelector("#anchorZ");
const anchorApplyPosBtn = document.querySelector("#anchorApplyPosBtn");
const anchorApplyTransformBtn = document.querySelector("#anchorApplyTransformBtn");
const anchorRotation = document.querySelector("#anchorRotation");
const anchorMirror = document.querySelector("#anchorMirror");
const anchorIntegrity = document.querySelector("#anchorIntegrity");
const anchorScale = document.querySelector("#anchorScale");
const anchorMoveBtn = document.querySelector("#anchorMoveBtn");
const anchorDeleteSelectedBtn = document.querySelector("#anchorDeleteSelectedBtn");
const anchorSelectAllBtn = document.querySelector("#anchorSelectAllBtn");
const anchorClearSelectionBtn = document.querySelector("#anchorClearSelectionBtn");
const structureSelectBox = document.querySelector("#structureSelectBox");
const decorationTemplate = document.querySelector("#decorationTemplate");
const decorationPlaceBtn = document.querySelector("#decorationPlaceBtn");
const forestModeBtn = document.querySelector("#forestModeBtn");
const forestDensity = document.querySelector("#forestDensity");
const forestDensityValue = document.querySelector("#forestDensityValue");
const forestAvoidStructures = document.querySelector("#forestAvoidStructures");
const forestAvoidWater = document.querySelector("#forestAvoidWater");
const viewModeButtons = document.querySelector("#viewModeButtons");
const fitViewBtn = document.querySelector("#fitViewBtn");
const zoomLabel = document.querySelector("#zoomLabel");
const lightAzimuth = document.querySelector("#lightAzimuth");
const lightElevation = document.querySelector("#lightElevation");
const lightIntensity = document.querySelector("#lightIntensity");
const lightAzimuthValue = document.querySelector("#lightAzimuthValue");
const lightElevationValue = document.querySelector("#lightElevationValue");
const lightIntensityValue = document.querySelector("#lightIntensityValue");
const shadowToggle = document.querySelector("#shadowToggle");
const lightResetBtn = document.querySelector("#lightResetBtn");
const titleMapName = document.querySelector("#titleMapName");
const statusPoint = document.querySelector("#statusPoint");
const statusBrush = document.querySelector("#statusBrush");
const statusSelection = document.querySelector("#statusSelection");
const statusTime = document.querySelector("#statusTime");
const statusMap = document.querySelector("#statusMap");
const authBadge = document.querySelector("#authBadge");
const logoutBtn = document.querySelector("#logoutBtn");
const loginOverlay = document.querySelector("#loginOverlay");
const loginForm = document.querySelector("#loginForm");
const loginRoom = document.querySelector("#loginRoom");
const loginRole = document.querySelector("#loginRole");
const loginPassword = document.querySelector("#loginPassword");
const loginSubmit = document.querySelector("#loginSubmit");
const loginError = document.querySelector("#loginError");

const VOID = -32768;
const LITTLE_ENDIAN = new Uint8Array(new Uint16Array([1]).buffer)[0] === 1;
const OVERVIEW_FACTOR = 16;
const DETAIL_TILE_SIZE = 512;
const MAX_MAP_AXIS = 32768;
const MAX_MAP_CELLS = 134_217_728;
const LAYER_NAMES = ["heightmap", "biomes", "surfaces"];
const META_FIELDS = ["spawn", "surface_palette", "biome_palette", "sea_level", "dirt_depth", "biome_transition"];
const LIGHT_STORAGE_KEY = "gensokyo-editor.light";
const DEFAULT_LIGHT = Object.freeze({ azimuth: 322, elevation: 52, intensity: 2.0, shadows: true });

function readStoredLight() {
  try {
    const stored = JSON.parse(localStorage.getItem(LIGHT_STORAGE_KEY) ?? "null");
    if (!stored || typeof stored !== "object") {
      return { ...DEFAULT_LIGHT };
    }
    const normalize = (value, min, max, fallback) => {
      const number = Number(value);
      return Number.isFinite(number) ? Math.min(max, Math.max(min, number)) : fallback;
    };
    return {
      azimuth: normalize(stored.azimuth, 0, 360, DEFAULT_LIGHT.azimuth),
      elevation: normalize(stored.elevation, 5, 85, DEFAULT_LIGHT.elevation),
      intensity: normalize(stored.intensity, 0.2, 3.5, DEFAULT_LIGHT.intensity),
      shadows: stored.shadows !== false,
    };
  } catch {
    return { ...DEFAULT_LIGHT };
  }
}

const state = {
  map: null,
  heights: null,
  biomes: null,
  surfaces: null,
  heightMin: 0,
  heightMax: 1,
  layer: "height",
  editMode: "brush",
  heightTool: "raise",
  pickedHeight: null,
  brushSize: 2,
  brushStrength: 1,
  actualMode: false,
  collab: {
    clientId: `client-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`,
    name: localStorage.getItem("gensokyo-editor.collab-name") || `编辑者-${Math.random().toString(36).slice(2, 6)}`,
    connected: false,
    source: null,
    users: [],
    cursors: new Map(),
    lastSeq: 0,
    cursorTimer: 0,
    applyingRemote: false,
    lockedCells: [],
    lockedCellSet: new Set(),
    remoteLocks: new Map(),
    editQueue: Promise.resolve(),
    busy: false,
    leaseTimer: null,
    leaseLost: false,
    session: false,
    stale: false,
    liveRevision: 0,
    metaQueue: Promise.resolve(),
    lastMeta: null,
    pendingMeta: null,
  },
  auth: {
    required: false,
    authenticated: true,
    role: "editor",
    room: null,
    roles: ["editor"],
  },
  selectedBiome: 0,
  biomeColorArray: null,
  surfaceColorArray: null,
  selectedSurface: 1,
  grid: true,
  water: true,
  light: readStoredLight(),
  viewMode: "2d",
  view: { centerX: 0, centerZ: 0, zoom: 1 },
  view3D: { yaw: Math.PI / 4, pitch: 0.55, zoom: 1, targetX: 0, targetZ: 0, exaggeration: 0, xzScale: 1, detail: 384, border: true },
  drawing: false,
  rect: null,
  rectDrawing: false,
  selection: null,
  clipboard: null,
  terrainTemplates: [],
  strokeLabel: null,
  panning: null,
  orbit: null,
  pan3D: null,
  placement: null,
  selectedAnchorId: null,
  selectedAnchorIds: new Set(),
  movingAnchorId: null,
  movingAnchorIds: [],
  movingAnchorOrigins: null,
  draggingAnchor: null,
  structureSelecting: false,
  structureSelectRect: null,
  toolPage: "terrain",
  minimapDrag: false,
  dirty: false,
  localDirty: false,
  layersDetailed: true,
  layerLoadToken: 0,
  mapRevision: 0,
  visited: new Set(),
  strokeChanges: null,
  flattenTarget: null,
  lastCell: null,
  cursor: null,
  undoStack: [],
  redoStack: [],
  templates: [],
  selectedTemplate: "reverie_dreams:small_shrine",
  templateSizes: {},
  decorations: [],
  selectedDecoration: "__mix__",
  forestMode: false,
  forestDensity: 0.35,
  forestAvoidStructures: true,
  forestAvoidWater: true,
  autoSave: localStorage.getItem("gensokyo-editor.auto-save") === "1",
  blocks: [],
  blockById: new Map(),
  availableBiomes: [],
  biomeById: new Map(),
  biomeCandidateQuery: "",
  underground: new Map(),
  undergroundMode: false,
  undergroundY: 32,
  undergroundYMin: 32,
  undergroundYMax: 32,
  undergroundClear: false,
  undergroundRevision: 0,
  undergroundChanges: null,
  renderQueued: false,
};

const clamp = (value, min, max) => Math.min(max, Math.max(min, value));
let biomeTransitionBefore = null;

function canEdit() {
  return state.auth?.role !== "viewer" && state.layersDetailed !== false;
}

state.terrainTemplates = readTerrainTemplates();

window.__gensokyoEditor = { state, terrainView, canvas, terrainCanvas, blockPreview, blockIcons, renderMinimap };

function parseBlockState(value) {
  const text = String(value ?? "").trim();
  const open = text.indexOf("[");
  const close = text.lastIndexOf("]");
  if (open < 0 || close <= open) {
    return { id: text, properties: {} };
  }
  const properties = {};
  for (const entry of text.slice(open + 1, close).split(",")) {
    const [name, rawValue] = entry.split("=", 2);
    if (name && rawValue !== undefined) {
      properties[name.trim()] = rawValue.trim();
    }
  }
  return { id: text.slice(0, open).trim(), properties };
}

function formatBlockState(id, properties = {}) {
  const entries = Object.entries(properties)
    .filter(([name, value]) => name && value !== undefined && value !== null && String(value) !== "")
    .sort(([a], [b]) => a.localeCompare(b));
  if (entries.length === 0) {
    return id;
  }
  return `${id}[${entries.map(([name, value]) => `${name}=${value}`).join(",")}]`;
}

function blockIdOf(value) {
  return parseBlockState(value).id;
}

function blockPropertiesOf(value) {
  return parseBlockState(value).properties;
}

function rgbString(color, factor = 1) {
  const r = clamp(Math.round(color[0] * factor), 0, 255);
  const g = clamp(Math.round(color[1] * factor), 0, 255);
  const b = clamp(Math.round(color[2] * factor), 0, 255);
  return `rgb(${r}, ${g}, ${b})`;
}

function mixColor(a, b, t) {
  return [
    a[0] + (b[0] - a[0]) * t,
    a[1] + (b[1] - a[1]) * t,
    a[2] + (b[2] - a[2]) * t,
  ];
}

function hslColor(hue, saturation, lightness) {
  const h = ((hue % 360) + 360) % 360 / 360;
  const s = saturation / 100;
  const l = lightness / 100;
  if (s === 0) {
    const v = Math.round(l * 255);
    return [v, v, v];
  }
  const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
  const p = 2 * l - q;
  const channel = (t) => {
    let value = t;
    if (value < 0) value += 1;
    if (value > 1) value -= 1;
    if (value < 1 / 6) return p + (q - p) * 6 * value;
    if (value < 1 / 2) return q;
    if (value < 2 / 3) return p + (q - p) * (2 / 3 - value) * 6;
    return p;
  };
  return [
    Math.round(channel(h + 1 / 3) * 255),
    Math.round(channel(h) * 255),
    Math.round(channel(h - 1 / 3) * 255),
  ];
}

// Palette swatch colors are resolved once per (index, id) and cached. This keeps
// the per-pixel viewport render cheap: no per-pixel table allocation or string parsing.
const BIOME_COLOR_TABLE = {
  plains: [111, 168, 79],
  sunflower_plains: [161, 179, 76],
  forest: [63, 122, 63],
  flower_forest: [93, 145, 82],
  birch_forest: [104, 143, 90],
  dark_forest: [45, 86, 48],
  cherry_grove: [205, 145, 174],
  bamboo_jungle: [91, 150, 70],
  swamp: [74, 105, 72],
  mangrove_swamp: [72, 110, 88],
  river: [69, 126, 196],
  ocean: [48, 100, 174],
  frozen_ocean: [120, 169, 210],
  beach: [220, 210, 157],
  snowy_plains: [222, 235, 242],
  snowy_taiga: [119, 151, 137],
  mountains: [128, 128, 128],
  stony_peaks: [145, 145, 145],
  meadow: [108, 166, 79],
  gensokyo_barrier: [36, 42, 52],
  gensokyo_lake: [57, 119, 201],
};
const SURFACE_COLOR_TABLE = {
  air: [28, 33, 40],
  grass_block: [106, 170, 76],
  dirt: [134, 96, 67],
  dirt_path: [176, 138, 88],
  coarse_dirt: [116, 84, 55],
  podzol: [92, 66, 36],
  mycelium: [128, 110, 128],
  sand: [226, 213, 154],
  red_sand: [188, 104, 55],
  gravel: [136, 126, 126],
  stone: [139, 139, 139],
  deepslate: [80, 80, 86],
  andesite: [132, 134, 133],
  snow_block: [240, 245, 250],
  snow: [240, 245, 250],
  water: [52, 110, 205],
  lava: [215, 92, 30],
  moss_block: [93, 126, 61],
  clay: [160, 166, 179],
};
const biomeColorCache = new Map();
const surfaceColorCache = new Map();

function resolveBiomeColor(index) {
  const id = state.map?.biome_palette?.[index] ?? `biome_${index}`;
  const cacheKey = `${index}|${id}`;
  const cached = biomeColorCache.get(cacheKey);
  if (cached) {
    return cached;
  }
  const key = id.includes(":") ? id.split(":").pop() : id;
  const tableColor = BIOME_COLOR_TABLE[key];
  if (tableColor) {
    biomeColorCache.set(cacheKey, tableColor);
    return tableColor;
  }
  const biomeEntry = state.biomeById.get(id);
  const biomeColorValue = biomeEntry ? hexToRgb(biomeEntry.color) : null;
  if (biomeColorValue) {
    biomeColorCache.set(cacheKey, biomeColorValue);
    return biomeColorValue;
  }
  const entry = state.blockById.get(id);
  const color = entry ? hexToRgb(entry.color) : null;
  const resolved = color ?? hslColor(index * 67 + 20, 42, 50);
  biomeColorCache.set(cacheKey, resolved);
  return resolved;
}

function biomeColor(index) {
  const cache = state.biomeColorArray;
  if (cache) {
    const cachedColor = cache[index];
    if (cachedColor) {
      return cachedColor;
    }
  }
  const resolved = resolveBiomeColor(index);
  if (cache && index < cache.length) {
    cache[index] = resolved;
  }
  return resolved;
}

function hexToRgb(hex) {
  const value = String(hex ?? "").replace("#", "");
  if (!/^[0-9a-fA-F]{6}$/.test(value)) {
    return null;
  }
  return [
    parseInt(value.slice(0, 2), 16),
    parseInt(value.slice(2, 4), 16),
    parseInt(value.slice(4, 6), 16),
  ];
}

function resolveSurfaceColor(index) {
  const id = state.map?.surface_palette?.[index] ?? `surface_${index}`;
  const cacheKey = `${index}|${id}`;
  const cached = surfaceColorCache.get(cacheKey);
  if (cached) {
    return cached;
  }
  const key = id.includes(":") ? id.split(":").pop() : id;
  const resolved = SURFACE_COLOR_TABLE[key] ?? hslColor(index * 53 + 90, 25, 52);
  surfaceColorCache.set(cacheKey, resolved);
  return resolved;
}

function surfaceColor(index) {
  const cache = state.surfaceColorArray;
  if (cache) {
    const cachedColor = cache[index];
    if (cachedColor) {
      return cachedColor;
    }
  }
  const resolved = resolveSurfaceColor(index);
  if (cache && index < cache.length) {
    cache[index] = resolved;
  }
  return resolved;
}

function heightColor(height) {
  if (height === VOID) return [14, 17, 22];
  const t = clamp(
    (height - state.heightMin) / Math.max(1, state.heightMax - state.heightMin),
    0,
    1,
  );
  return [
    48 + 122 * t,
    92 + 88 * t,
    54 + 112 * t,
  ];
}

function computeHeightRange() {
  let min = Infinity;
  let max = -Infinity;
  for (let i = 0; i < state.heights.length; i++) {
    const height = state.heights[i];
    if (height === VOID) {
      continue;
    }
    if (height < min) min = height;
    if (height > max) max = height;
  }
  if (!Number.isFinite(min) || !Number.isFinite(max)) {
    min = state.map.min_y;
    max = state.map.min_y + state.map.world_height - 1;
  }
  state.heightMin = min;
  state.heightMax = max;
}

function noteHeight(height) {
  if (height === VOID) {
    return;
  }
  if (height < state.heightMin) {
    state.heightMin = height;
  }
  if (height > state.heightMax) {
    state.heightMax = height;
  }
}

function shadeAt(x, z, height) {
  if (height === VOID) return 1;
  const width = state.map.width;
  const leftHeight = x > 0 ? state.heights[z * width + x - 1] : height;
  const upHeight = z > 0 ? state.heights[(z - 1) * width + x] : height;
  const left = leftHeight === VOID ? height : leftHeight;
  const up = upHeight === VOID ? height : upHeight;
  return clamp(1 + (height - left) * 0.07 + (height - up) * 0.04, 0.7, 1.35);
}

function undergroundColor(x, z) {
  const map = state.map;
  const y = state.undergroundY;
  const override = state.underground.get(`${x},${y},${z}`);
  if (override) {
    return blockColorById(override);
  }
  const index = z * map.width + x;
  const height = state.heights[index];
  if (height === VOID) {
    return [12, 16, 21];
  }
  if (y > height) {
    return y <= map.sea_level ? blockColorById("minecraft:water") : blockColorById("minecraft:air");
  }
  if (y === height) {
    return blockColorById(map.surface_palette[state.surfaces[index]]);
  }
  if (y === map.min_y) {
    return blockColorById("minecraft:bedrock");
  }
  if (y >= height - (map.dirt_depth ?? 3)) {
    return blockColorById("minecraft:dirt");
  }
  return blockColorById("minecraft:stone");
}

const MAX_UNDERGROUND_RANGE = 256;

function clampUndergroundY(value, fallback = 32) {
  const min = state.map?.min_y ?? 0;
  const max = (state.map?.min_y ?? 0) + (state.map?.world_height ?? 1024) - 1;
  const numeric = Number(value);
  const resolved = Number.isFinite(numeric) ? numeric : Number(fallback);
  return clamp(Math.round(Number.isFinite(resolved) ? resolved : min), min, max);
}

function undergroundRange() {
  const yMin = clampUndergroundY(state.undergroundYMin, state.undergroundY);
  const yMax = clampUndergroundY(state.undergroundYMax, state.undergroundY);
  return { min: Math.min(yMin, yMax), max: Math.max(yMin, yMax) };
}

function syncUndergroundInputs() {
  if (!state.map) {
    return;
  }
  const min = state.map.min_y;
  const max = state.map.min_y + state.map.world_height - 1;
  state.undergroundY = clampUndergroundY(state.undergroundY, min);
  state.undergroundYMin = clampUndergroundY(state.undergroundYMin, state.undergroundY);
  state.undergroundYMax = clampUndergroundY(state.undergroundYMax, state.undergroundY);
  if (state.undergroundYMin > state.undergroundYMax) {
    [state.undergroundYMin, state.undergroundYMax] = [state.undergroundYMax, state.undergroundYMin];
  }
  if (state.undergroundYMax - state.undergroundYMin + 1 > MAX_UNDERGROUND_RANGE) {
    state.undergroundYMax = Math.min(max, state.undergroundYMin + MAX_UNDERGROUND_RANGE - 1);
  }
  for (const [input, value] of [
    [undergroundYInput, state.undergroundY],
    [undergroundYMinInput, state.undergroundYMin],
    [undergroundYMaxInput, state.undergroundYMax],
  ]) {
    if (!input) {
      continue;
    }
    input.min = String(min);
    input.max = String(max);
    input.value = String(value);
  }
}

function cellColor(x, z) {
  if (state.undergroundMode) {
    return undergroundColor(x, z);
  }
  const index = z * state.map.width + x;
  const height = state.heights[index];
  if (height === VOID) {
    return [14, 17, 22];
  }
  const shade = shadeAt(x, z, height);
  let color;
  if (state.layer === "height") {
    color = heightColor(height);
  } else if (state.layer === "biome") {
    color = biomeColor(transitionBiomeIndex(state.map, state.biomes, x, z));
  } else {
    color = surfaceColor(state.surfaces[index]);
  }
  color = [color[0] * shade, color[1] * shade, color[2] * shade];
  if (state.water && height < state.map.sea_level) {
    color = mixColor(color, [51, 112, 208], 0.56);
  }
  return color;
}

function fitScale() {
  if (!state.map) {
    return 1;
  }
  return Math.min(canvas.width / state.map.width, canvas.height / state.map.depth);
}

function mapScale() {
  return fitScale() * state.view.zoom;
}

function mapToScreen(x, z) {
  const scale = mapScale();
  return {
    x: (x - state.view.centerX) * scale + canvas.width / 2,
    y: (z - state.view.centerZ) * scale + canvas.height / 2,
  };
}

function screenToCell(event) {
  if (!state.map) {
    return null;
  }
  const rect = canvas.getBoundingClientRect();
  const sx = (event.clientX - rect.left) / rect.width * canvas.width;
  const sz = (event.clientY - rect.top) / rect.height * canvas.height;
  const scale = mapScale();
  const x = Math.floor(state.view.centerX + (sx - canvas.width / 2) / scale);
  const z = Math.floor(state.view.centerZ + (sz - canvas.height / 2) / scale);
  if (x < 0 || x >= state.map.width || z < 0 || z >= state.map.depth) {
    return null;
  }
  return { x, z };
}

function pick3DCell(event) {
  if (!terrainView || !state.map || !state.heights) {
    return null;
  }
  return terrainView.pick(event);
}

function resizeViewport() {
  if (!viewportStage) {
    return;
  }
  const rect = viewportStage.getBoundingClientRect();
  const width = Math.max(1, Math.floor(rect.width));
  const height = Math.max(1, Math.floor(rect.height));
  if (width < 2 || height < 2) {
    return;
  }
  if (state.viewMode === "3d") {
    if (terrainView) {
      terrainView.setCanvasSize(width, height);
    }
  } else if (canvas.width !== width || canvas.height !== height) {
    canvas.width = width;
    canvas.height = height;
  }
  scheduleRender();
}

function scheduleRender() {
  if (state.renderQueued) {
    return;
  }
  state.renderQueued = true;
  requestAnimationFrame(() => {
    state.renderQueued = false;
    renderNow();
  });
}

function markTerrainDirty() {
  if (terrainView) {
    terrainView.markDirty();
  }
}

function markTerrainDirtyRegion(minX, minZ, maxX, maxZ) {
  if (terrainView) {
    terrainView.markDirtyRegion(minX, minZ, maxX, maxZ);
  }
}

function renderNow() {
  if (!state.map || !state.heights) {
    return;
  }
  if (state.viewMode === "3d") {
    render3D();
  } else {
    render2D();
  }
  renderMinimap();
  updateZoomLabel();
}

function minimapGeometry() {
  const cellPx = Math.min(minimap.width / state.map.width, minimap.height / state.map.depth);
  return {
    cellPx,
    offX: (minimap.width - state.map.width * cellPx) / 2,
    offY: (minimap.height - state.map.depth * cellPx) / 2,
  };
}

function renderMinimap() {
  if (!state.map || !state.heights) {
    return;
  }
  const width = state.map.width;
  const depth = state.map.depth;
  const { cellPx, offX, offY } = minimapGeometry();
  const image = minimapCtx.createImageData(minimap.width, minimap.height);
  const data = image.data;

  for (let sy = 0; sy < minimap.height; sy++) {
    const z = Math.floor((sy - offY) / cellPx);
    for (let sx = 0; sx < minimap.width; sx++) {
      const index = (sy * minimap.width + sx) * 4;
      const x = Math.floor((sx - offX) / cellPx);
      if (z < 0 || z >= depth || x < 0 || x >= width) {
        data[index] = 9;
        data[index + 1] = 13;
        data[index + 2] = 18;
        data[index + 3] = 255;
        continue;
      }
      const color = cellColor(x, z);
      data[index] = color[0];
      data[index + 1] = color[1];
      data[index + 2] = color[2];
      data[index + 3] = 255;
    }
  }
  minimapCtx.putImageData(image, 0, 0);

  for (const anchor of state.map.anchors ?? []) {
    if (!Array.isArray(anchor.pos)) {
      continue;
    }
    const x = anchor.pos[0] - state.map.origin[0];
    const z = anchor.pos[2] - state.map.origin[1];
    const color = anchor.type === "structure" ? "#ffb7f2" : "#ffd479";
    minimapCtx.beginPath();
    minimapCtx.arc(offX + x * cellPx, offY + z * cellPx, Math.max(1.5, cellPx * 1.1), 0, Math.PI * 2);
    minimapCtx.fillStyle = color;
    minimapCtx.fill();
    minimapCtx.strokeStyle = "rgba(20, 18, 10, 0.9)";
    minimapCtx.lineWidth = 1;
    minimapCtx.stroke();
  }

  if (state.viewMode === "2d") {
    const scale = mapScale();
    const left = state.view.centerX - canvas.width / 2 / scale;
    const top = state.view.centerZ - canvas.height / 2 / scale;
    const right = state.view.centerX + canvas.width / 2 / scale;
    const bottom = state.view.centerZ + canvas.height / 2 / scale;
    const x0 = offX + clamp(left, 0, width) * cellPx;
    const y0 = offY + clamp(top, 0, depth) * cellPx;
    const x1 = offX + clamp(right, 0, width) * cellPx;
    const y1 = offY + clamp(bottom, 0, depth) * cellPx;
    minimapCtx.strokeStyle = "rgba(255, 255, 255, 0.92)";
    minimapCtx.lineWidth = 1.5;
    minimapCtx.strokeRect(x0 + 0.5, y0 + 0.5, Math.max(1, x1 - x0), Math.max(1, y1 - y0));
  } else {
    const footprint = terrainView?.viewFootprint?.(state) ?? null;
    if (footprint && footprint.length >= 3) {
      minimapCtx.save();
      minimapCtx.beginPath();
      minimapCtx.rect(offX, offY, width * cellPx, depth * cellPx);
      minimapCtx.clip();
      minimapCtx.strokeStyle = "rgba(255, 255, 255, 0.92)";
      minimapCtx.lineWidth = 1.5;
      minimapCtx.beginPath();
      footprint.forEach((point, index) => {
        const px = offX + point.x * cellPx;
        const py = offY + point.z * cellPx;
        if (index === 0) {
          minimapCtx.moveTo(px, py);
        } else {
          minimapCtx.lineTo(px, py);
        }
      });
      minimapCtx.closePath();
      minimapCtx.stroke();
      minimapCtx.restore();
    } else {
      const x = offX + state.view3D.targetX * cellPx;
      const y = offY + state.view3D.targetZ * cellPx;
      minimapCtx.strokeStyle = "rgba(255, 255, 255, 0.95)";
      minimapCtx.lineWidth = 1.5;
      minimapCtx.beginPath();
      minimapCtx.moveTo(x - 5, y);
      minimapCtx.lineTo(x + 5, y);
      minimapCtx.moveTo(x, y - 5);
      minimapCtx.lineTo(x, y + 5);
      minimapCtx.stroke();
    }
  }

  minimapCtx.strokeStyle = "rgba(120, 200, 255, 0.22)";
  minimapCtx.lineWidth = 1;
  minimapCtx.strokeRect(0.5, 0.5, minimap.width - 1, minimap.height - 1);
}

function minimapEventToCell(event) {
  if (!state.map) {
    return null;
  }
  const rect = minimap.getBoundingClientRect();
  const sx = (event.clientX - rect.left) / rect.width * minimap.width;
  const sy = (event.clientY - rect.top) / rect.height * minimap.height;
  const { cellPx, offX, offY } = minimapGeometry();
  const x = Math.floor((sx - offX) / cellPx);
  const z = Math.floor((sy - offY) / cellPx);
  if (x < 0 || x >= state.map.width || z < 0 || z >= state.map.depth) {
    return null;
  }
  return { x, z };
}

function render2D() {
  const width = state.map.width;
  const depth = state.map.depth;
  const scale = mapScale();
  const image = ctx.createImageData(canvas.width, canvas.height);
  const data = image.data;
  const halfW = canvas.width / 2;
  const halfH = canvas.height / 2;
  const left = state.view.centerX - halfW / scale;
  const top = state.view.centerZ - halfH / scale;

  let lastX = -1;
  let lastZ = -1;
  let lastColor = null;
  for (let sy = 0; sy < canvas.height; sy++) {
    const z = Math.floor(top + sy / scale);
    const rowBase = sy * canvas.width * 4;
    for (let sx = 0; sx < canvas.width; sx++) {
      const index = rowBase + sx * 4;
      if (z < 0 || z >= depth) {
        data[index] = 10;
        data[index + 1] = 13;
        data[index + 2] = 18;
        data[index + 3] = 255;
        continue;
      }
      const x = Math.floor(left + sx / scale);
      if (x < 0 || x >= width) {
        data[index] = 10;
        data[index + 1] = 13;
        data[index + 2] = 18;
        data[index + 3] = 255;
        lastX = -1;
        lastZ = -1;
        continue;
      }
      if (x !== lastX || z !== lastZ) {
        lastColor = cellColor(x, z);
        lastX = x;
        lastZ = z;
      }
      const color = lastColor;
      data[index] = color[0];
      data[index + 1] = color[1];
      data[index + 2] = color[2];
      data[index + 3] = 255;
    }
  }

  ctx.putImageData(image, 0, 0);

  if (state.grid && scale >= 4) {
    const minX = Math.max(0, Math.floor(left));
    const maxX = Math.min(width, Math.ceil(left + canvas.width / scale) + 1);
    const minZ = Math.max(0, Math.floor(top));
    const maxZ = Math.min(depth, Math.ceil(top + canvas.height / scale) + 1);
    ctx.strokeStyle = "rgba(255, 255, 255, 0.10)";
    ctx.lineWidth = 1;
    ctx.beginPath();
    for (let x = minX; x <= maxX; x++) {
      const sx = Math.round(mapToScreen(x, 0).x) + 0.5;
      ctx.moveTo(sx, 0);
      ctx.lineTo(sx, canvas.height);
    }
    for (let z = minZ; z <= maxZ; z++) {
      const sy = Math.round(mapToScreen(0, z).y) + 0.5;
      ctx.moveTo(0, sy);
      ctx.lineTo(canvas.width, sy);
    }
    ctx.stroke();
  }

  drawAnchors2D();
  drawRemoteCursors2D(ctx);
  drawRemoteLocks2D(ctx);

  const movingAnchor = state.movingAnchorId
    ? state.map.anchors.find((anchor) => anchor.id === state.movingAnchorId)
    : null;
  if ((state.placement || movingAnchor) && state.cursor) {
    const p = mapToScreen(state.cursor.x + 0.5, state.cursor.z + 0.5);
    const template = state.placement?.template ?? movingAnchor?.template;
    const size = state.templateSizes[template];
    if (Array.isArray(size) && size.length === 3) {
      const a = mapToScreen(state.cursor.x, state.cursor.z);
      const b = mapToScreen(state.cursor.x + size[0], state.cursor.z + size[2]);
      ctx.fillStyle = movingAnchor ? "rgba(123, 216, 143, 0.16)" : "rgba(255, 212, 121, 0.18)";
      ctx.fillRect(a.x, a.y, b.x - a.x, b.y - a.y);
      ctx.strokeStyle = movingAnchor ? "rgba(123, 216, 143, 0.95)" : "rgba(255, 212, 121, 0.95)";
      ctx.lineWidth = 1.5;
      ctx.strokeRect(a.x + 0.5, a.y + 0.5, b.x - a.x, b.y - a.y);
    }
    ctx.beginPath();
    ctx.arc(p.x, p.y, Math.max(6, mapScale() * 0.55), 0, Math.PI * 2);
    ctx.fillStyle = movingAnchor ? "rgba(123, 216, 143, 0.28)" : "rgba(255, 212, 121, 0.28)";
    ctx.fill();
    ctx.strokeStyle = movingAnchor ? "#7bd88f" : "#ffd479";
    ctx.lineWidth = 2;
    ctx.stroke();
  }

  const activeRect = state.rect ?? state.selection;
  if (activeRect) {
    const minX = Math.min(activeRect.start.x, activeRect.end.x);
    const maxX = Math.max(activeRect.start.x, activeRect.end.x);
    const minZ = Math.min(activeRect.start.z, activeRect.end.z);
    const maxZ = Math.max(activeRect.start.z, activeRect.end.z);
    const a = mapToScreen(minX, minZ);
    const b = mapToScreen(maxX + 1, maxZ + 1);
    const x = Math.min(a.x, b.x);
    const y = Math.min(a.y, b.y);
    const widthRect = Math.abs(b.x - a.x);
    const heightRect = Math.abs(b.y - a.y);
    ctx.fillStyle = state.selection && !state.rect ? "rgba(123, 216, 143, 0.16)" : "rgba(120, 200, 255, 0.18)";
    ctx.fillRect(x, y, widthRect, heightRect);
    ctx.strokeStyle = state.selection && !state.rect ? "rgba(123, 216, 143, 0.95)" : "rgba(180, 225, 255, 0.9)";
    ctx.lineWidth = 1.5;
    ctx.strokeRect(x + 0.5, y + 0.5, widthRect, heightRect);
  }

  if (state.cursor && (state.toolPage !== "objects" || state.forestMode)) {
    const p = mapToScreen(state.cursor.x + 0.5, state.cursor.z + 0.5);
    ctx.beginPath();
    ctx.arc(p.x, p.y, Math.max(1, (state.brushSize + 0.5) * scale), 0, Math.PI * 2);
    ctx.strokeStyle = "rgba(255, 255, 255, 0.9)";
    ctx.lineWidth = 1.5;
    ctx.stroke();
  }
}

function render3D() {
  if (!terrainView) {
    ctx.fillStyle = "#090d12";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    return;
  }
  terrainView.setMap(state.map, state.heights, state.surfaces, state.biomes);
  terrainView.setLighting(state.light);
  terrainView.update(state, (index, layer, current) => {
    if (layer === "biome") {
      const width = current.map.width;
      const x = index % width;
      const z = Math.floor(index / width);
      return biomeColor(transitionBiomeIndex(current.map, current.biomes, x, z));
    }
    return surfaceColor(current.surfaces[index]);
  });
  updateTerrain3dControls();
  terrainView.render();
}
function anchorFootprintSize(anchor) {
  const size = state.templateSizes?.[anchor.template];
  if (!Array.isArray(size) || size.length !== 3) {
    return null;
  }
  const rotation = String(anchor.rotation ?? "none").toLowerCase();
  const rotated = rotation === "clockwise_90" || rotation === "counterclockwise_90"
    ? [size[2], size[1], size[0]]
    : size;
  const scale = normalizeAnchorScale(anchor.scale);
  return rotated.map((value) => value * scale);
}

function drawAnchors2D() {
  const anchors = state.map?.anchors ?? [];
  const radius = Math.max(3, Math.min(8, mapScale() * 0.42));
  for (const anchor of anchors) {
    if (!Array.isArray(anchor.pos)) {
      continue;
    }
    const x = anchor.pos[0] - state.map.origin[0];
    const z = anchor.pos[2] - state.map.origin[1];
    const selected = state.selectedAnchorIds.has(anchor.id);
    const size = anchorFootprintSize(anchor);
    if (Array.isArray(size) && size.length === 3) {
      const a = mapToScreen(x, z);
      const b = mapToScreen(x + size[0], z + size[2]);
      ctx.fillStyle = selected ? "rgba(123, 216, 143, 0.16)" : "rgba(255, 212, 121, 0.10)";
      ctx.fillRect(a.x, a.y, b.x - a.x, b.y - a.y);
      ctx.strokeStyle = selected ? "rgba(123, 216, 143, 0.95)" : "rgba(255, 212, 121, 0.55)";
      ctx.lineWidth = selected ? 2 : 1;
      ctx.strokeRect(a.x + 0.5, a.y + 0.5, b.x - a.x, b.y - a.y);
    }
    const p = mapToScreen(x + 0.5, z + 0.5);
    if (p.x < -radius || p.y < -radius || p.x > canvas.width + radius || p.y > canvas.height + radius) {
      continue;
    }
    ctx.beginPath();
    ctx.arc(p.x, p.y, selected ? radius * 1.2 : radius, 0, Math.PI * 2);
    ctx.fillStyle = selected ? "#7bd88f" : "#ffd479";
    ctx.fill();
    ctx.strokeStyle = selected ? "#17351f" : "#332400";
    ctx.lineWidth = 1.5;
    ctx.stroke();
  }
}

function updateZoomLabel() {
  zoomLabel.textContent = state.viewMode === "3d"
    ? `${state.view3D.zoom.toFixed(2)}×`
    : `${state.view.zoom.toFixed(2)}×`;
}

function updateStatusBar(cell = state.cursor) {
  const modeLabels = { brush: "笔刷", rect: "矩形", select: "选区", pick: "吸管" };
  if (!state.map) {
    statusPoint.textContent = "点: --";
    statusBrush.textContent = `工具: ${modeLabels[state.editMode] ?? state.editMode} · ${state.brushSize} × ${state.brushStrength}`;
    statusSelection.textContent = "选择: 无";
    statusMap.textContent = "地图: --";
    return;
  }
  if (cell && state.heights) {
    const index = cell.z * state.map.width + cell.x;
    const height = state.undergroundMode
      ? state.undergroundY
      : state.heights[index];
    const worldX = state.map.origin[0] + cell.x;
    const worldZ = state.map.origin[1] + cell.z;
    statusPoint.textContent = `点: ${worldX}, ${height === VOID ? "--" : height}, ${worldZ}`;
  } else {
    statusPoint.textContent = "点: --";
  }
  statusBrush.textContent = state.undergroundMode
    ? `工具: 地下 · ${state.brushSize} × ${state.brushStrength} · Y=${undergroundRange().min}..${undergroundRange().max}`
    : `工具: ${modeLabels[state.editMode] ?? state.editMode} · ${state.brushSize} × ${state.brushStrength}`;
  statusSelection.textContent = state.movingAnchorId
    ? `选择: 移动 ${state.movingAnchorIds.length || 1} 个结构`
    : state.forestMode
      ? "选择: 森林笔刷"
      : state.selectedAnchorIds.size > 0
        ? `选择: ${state.selectedAnchorIds.size} 个结构`
        : state.placement
          ? `选择: ${state.placement.template}`
          : state.undergroundMode
            ? `选择: 地下 Y=${undergroundRange().min}..${undergroundRange().max}（切片 ${state.undergroundY}）`
            : "选择: 无";
  if (state.undergroundMode && terrainView?.undergroundTruncated) {
    statusSelection.textContent += " · 3D 仅显示切片";
  }
  statusMap.textContent = `地图: ${state.map.width} × ${state.map.depth}`;
}

function updateStatusClock() {
  statusTime.textContent = `时间: ${new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`;
}

function setDirty(dirty, local = true) {
  state.dirty = dirty;
  if (!dirty) {
    state.localDirty = false;
  } else if (local) {
    state.localDirty = true;
  }
  dirtyState.textContent = state.collab.stale
    ? "版本冲突"
    : state.auth?.role === "viewer"
      ? "只读"
      : state.layersDetailed === false
        ? "加载细节"
        : dirty ? "未保存" : "未修改";
  dirtyState.classList.toggle("dirty", canEdit() && (dirty || state.collab.stale));
  updateHistoryButtons();
}

function setCollabStale(message) {
  state.collab.stale = true;
  dirtyState.textContent = "版本冲突";
  dirtyState.classList.add("dirty");
  log(message);
}

function log(message) {
  const time = new Date().toLocaleTimeString();
  const lines = logEl.textContent ? logEl.textContent.split("\n") : [];
  lines.push(`[${time}] ${message}`);
  logEl.textContent = lines.slice(-14).join("\n");
  logEl.scrollTop = logEl.scrollHeight;
}

function fillMeta() {
  const map = state.map;
  const transition = Math.max(0, Math.min(64, Number(map.biome_transition ?? 0) | 0));
  map.biome_transition = transition;
  titleMapName.textContent = `[${String(map.id).replace(/^.*:/, "")}]`;
  statusMap.textContent = `地图: ${map.width} × ${map.depth}`;
  metaEl.textContent = [
    `id: ${map.id}`,
    `format: ${map.format_version} / map: ${map.map_version}`,
    `size: ${map.width} x ${map.depth}`,
    `origin: [${map.origin.join(", ")}]`,
    `y: ${map.min_y}..${map.min_y + map.world_height - 1}`,
    `terrain: ${state.heightMin}..${state.heightMax}`,
    `world_height: ${map.world_height}`,
    `sea_level: ${map.sea_level}`,
    `dirt_depth: ${map.dirt_depth ?? 3}`,
    `biome_transition: ${transition}`,
    `biomes: ${map.biome_palette.length}`,
    `surfaces: ${map.surface_palette.length}`,
    `layers: ${map.layers ? "binary" : "inline"}`,
    `anchors: ${map.anchors?.length ?? 0}`,
  ].join("\n");
  seaLevelInput.value = String(map.sea_level);
  dirtDepthInput.value = String(map.dirt_depth ?? 3);
  if (biomeTransitionInput) {
    biomeTransitionInput.value = String(transition);
  }
  if (biomeTransitionValue) {
    biomeTransitionValue.textContent = String(transition);
  }
  biomeTransitionBefore = null;
}

function fillSpawn() {
  const pos = state.map?.spawn?.pos ?? [0, 0, 0];
  spawnX.value = String(pos[0]);
  spawnY.value = String(pos[1]);
  spawnZ.value = String(pos[2]);
}

function readSpawnInputs() {
  const x = Number(spawnX.value);
  const y = Number(spawnY.value);
  const z = Number(spawnZ.value);
  if (![x, y, z].every(Number.isInteger)) {
    return null;
  }
  return [x, y, z];
}

function onSpawnInput() {
  if (!canEdit()) {
    log("只读角色不能修改出生点");
    return;
  }
  const pos = readSpawnInputs();
  if (!pos || !state.map) {
    return;
  }
  const before = editorSnapshot();
  state.map.spawn = state.map.spawn ?? { yaw: 0, pitch: 0 };
  state.map.spawn.pos = pos;
  pushSnapshotHistory(before, "修改出生点");
  setDirty(true);
}

function setSpawnFromCursor() {
  if (!canEdit()) {
    log("只读角色不能修改出生点");
    return;
  }
  if (!state.map || !state.cursor) {
    log("请先把鼠标移到地图上，再设出生点");
    return;
  }
  const cell = state.cursor;
  const index = cell.z * state.map.width + cell.x;
  const height = state.heights[index];
  if (height === VOID) {
    log("该列是 void，不能作为出生点");
    return;
  }
  const maxY = state.map.min_y + state.map.world_height - 1;
  const spawnYValue = Math.min(maxY, height + 1);
  const pos = [
    state.map.origin[0] + cell.x,
    spawnYValue,
    state.map.origin[1] + cell.z,
  ];
  const before = editorSnapshot();
  state.map.spawn = state.map.spawn ?? { yaw: 0, pitch: 0 };
  state.map.spawn.pos = pos;
  pushSnapshotHistory(before, "设置出生点");
  fillSpawn();
  setDirty(true);
  log(`出生点已设为 ${pos.join(", ")}`);
}

function updatePaletteSelection() {
  for (const item of biomePaletteEl.children) {
    item.classList.toggle("active", Number(item.dataset.index) === state.selectedBiome);
  }
  for (const item of surfacePaletteEl.children) {
    item.classList.toggle("active", Number(item.dataset.index) === state.selectedSurface);
  }
  void buildBlockStateEditor(state.map?.surface_palette?.[state.selectedSurface] ?? "");
}

let blockStateEditorToken = 0;

function preferredPropertyValue(name, values) {
  const list = Array.isArray(values) ? values : [];
  const preferred = {
    facing: ["north", "south", "east", "west"],
    axis: ["y", "x", "z"],
    half: ["bottom", "top"],
    type: ["bottom", "top", "double"],
    shape: ["straight", "inner_left", "inner_right", "outer_left", "outer_right"],
    part: ["foot", "head"],
    hinge: ["left", "right"],
    attachment: ["wall", "floor", "ceiling", "single", "double"],
    block_half: ["lower", "upper"],
    tilt: ["none", "unstable", "partial", "full"],
  };
  for (const candidate of preferred[name] ?? []) {
    if (list.includes(candidate)) {
      return candidate;
    }
  }
  for (const candidate of ["false", "north", "bottom", "straight", "y", "foot", "left", "none", "0"]) {
    if (list.includes(candidate)) {
      return candidate;
    }
  }
  return list[0] ?? "";
}

async function buildBlockStateEditor(value) {
  if (!blockStateSection || !blockStateProperties || !blockStateName) {
    return;
  }
  const token = ++blockStateEditorToken;
  const { id, properties } = parseBlockState(value);
  if (!id) {
    blockStateSection.hidden = true;
    return;
  }
  blockStateSection.hidden = false;
  blockStateName.textContent = id.replace(/^.*:/, "");
  blockStateName.title = value;
  blockStateProperties.replaceChildren();
  const loading = document.createElement("span");
  loading.className = "hint";
  loading.textContent = "正在读取方块属性...";
  blockStateProperties.append(loading);

  const blockstate = await fetchBlockstate(id);
  if (token !== blockStateEditorToken) {
    return;
  }
  const meta = state.blockById.get(id);
  const metaProperties = meta?.properties && typeof meta.properties === "object" ? meta.properties : null;
  const metaDefaults = meta?.defaults && typeof meta.defaults === "object" ? meta.defaults : {};
  const available = metaProperties ?? (blockstate ? describeBlockstate(blockstate) : {});
  const names = Object.keys(available);
  blockStateProperties.replaceChildren();
  if (names.length === 0) {
    const hint = document.createElement("span");
    hint.className = "hint";
    hint.textContent = blockstate ? "该方块没有可编辑属性" : "未找到 blockstate JSON，使用默认方块";
    blockStateProperties.append(hint);
    return;
  }
  const current = { ...properties };
  for (const name of names) {
    const values = [...available[name]].sort();
    if (!values.includes(String(current[name]))) {
      current[name] = metaDefaults[name] ?? preferredPropertyValue(name, values);
    }
    const label = document.createElement("label");
    const title = document.createElement("span");
    title.textContent = name;
    const select = document.createElement("select");
    for (const optionValue of values) {
      const option = document.createElement("option");
      option.value = optionValue;
      option.textContent = optionValue;
      option.selected = String(current[name]) === optionValue;
      select.append(option);
    }
    select.addEventListener("change", () => {
      current[name] = select.value;
      commitBlockState(id, current);
    });
    label.append(title, select);
    blockStateProperties.append(label);
  }
}

function commitBlockState(id, properties) {
  if (!canEdit()) {
    log("只读角色不能修改方块属性");
    return;
  }
  if (!state.map) {
    return;
  }
  const index = state.selectedSurface;
  if (!Number.isInteger(index) || index < 0 || index >= state.map.surface_palette.length) {
    return;
  }
  const next = formatBlockState(id, properties);
  const previous = state.map.surface_palette[index];
  if (next === previous) {
    return;
  }
  const before = editorSnapshot();
  state.map.surface_palette[index] = next;
  pushSnapshotHistory(before, "修改方块属性");
  setDirty(true);
  previewBlock(next);
  if (blockStateName) {
    blockStateName.title = next;
  }
  updateHotbar();
  markTerrainDirty();
  scheduleRender();
  log(`方块属性：${next}`);
}

function blockColorById(blockId) {
  const id = blockIdOf(blockId);
  const key = id.split(":").pop();
  const table = {
    air: [18, 23, 29],
    cave_air: [18, 23, 29],
    void_air: [18, 23, 29],
    water: [51, 112, 208],
    stone: [139, 139, 139],
    dirt: [134, 96, 67],
    grass_block: [106, 170, 76],
    sand: [226, 213, 154],
    bedrock: [48, 48, 48],
  };
  if (table[key]) {
    return table[key];
  }
  const entry = state.blockById.get(id);
  return (entry && hexToRgb(entry.color)) || [120, 120, 120];
}

function previewBlock(value) {
  if (!blockPreview || !value) {
    return;
  }
  const { id, properties } = parseBlockState(value);
  const entry = state.blockById.get(id);
  const rgb = entry ? hexToRgb(entry.color) : null;
  const color = rgb ? ((rgb[0] << 16) | (rgb[1] << 8) | rgb[2]) : 0x8b8b8b;
  blockPreview.show(id, color, properties).catch((error) => {
    log(`方块模型预览失败：${error instanceof Error ? error.message : String(error)}`);
  });
}

function makePaletteItem(index, kind) {
  const id = kind === "biome" ? state.map.biome_palette[index] : state.map.surface_palette[index];
  const color = kind === "biome" ? biomeColor(index) : surfaceColor(index);
  const button = document.createElement("button");
  button.type = "button";
  button.className = "palette-item";
  button.dataset.index = String(index);
  button.dataset.kind = kind;

  const swatch = document.createElement("span");
  swatch.className = "swatch";
  swatch.style.background = rgbString(color);

  const label = document.createElement("span");
  const baseId = kind === "biome" ? String(id) : blockIdOf(id);
  const properties = kind === "biome" ? {} : blockPropertiesOf(id);
  label.textContent = `${index} · ${baseId.replace(/^.*:/, "")}${Object.keys(properties).length > 0 ? " · 属性" : ""}`;

  button.append(swatch, label);
  button.addEventListener("click", () => {
    if (kind === "biome") {
      state.selectedBiome = index;
    } else {
      state.selectedSurface = index;
      previewBlock(id);
    }
    updatePaletteSelection();
  });
  return button;
}

function makeSurfacePaletteItem(index) {
  const id = state.map.surface_palette[index];
  const color = surfaceColor(index);
  const row = document.createElement("div");
  row.className = "palette-row";
  row.dataset.index = String(index);
  row.dataset.kind = "surface";

  const button = document.createElement("button");
  button.type = "button";
  button.className = "palette-item";
  const swatch = document.createElement("span");
  swatch.className = "swatch";
  swatch.style.background = rgbString(color);
  const label = document.createElement("span");
  const baseId = blockIdOf(id);
  const properties = blockPropertiesOf(id);
  label.textContent = `${index} · ${baseId.replace(/^.*:/, "")}${Object.keys(properties).length > 0 ? " · 属性" : ""}`;
  button.append(swatch, label);
  button.addEventListener("click", () => {
    state.selectedSurface = index;
    previewBlock(id);
    updatePaletteSelection();
  });

  const actions = document.createElement("div");
  actions.className = "palette-actions";
  const up = document.createElement("button");
  up.type = "button";
  up.textContent = "↑";
  up.title = "上移一层";
  up.disabled = index === 0;
  up.addEventListener("click", (event) => {
    event.stopPropagation();
    moveSurfacePalette(index, -1);
  });
  const down = document.createElement("button");
  down.type = "button";
  down.textContent = "↓";
  down.title = "下移一层";
  down.disabled = index >= state.map.surface_palette.length - 1;
  down.addEventListener("click", (event) => {
    event.stopPropagation();
    moveSurfacePalette(index, 1);
  });
  const remove = document.createElement("button");
  remove.type = "button";
  remove.textContent = "×";
  remove.title = "从调色板删除";
  remove.className = "remove";
  remove.disabled = state.map.surface_palette.length <= 1;
  remove.addEventListener("click", (event) => {
    event.stopPropagation();
    deleteSurfacePalette(index);
  });
  actions.append(up, down, remove);
  row.append(button, actions);
  return row;
}

function buildPalettes() {
  if (!state.map) {
    return;
  }
  state.biomeColorArray = new Array(state.map.biome_palette.length);
  state.surfaceColorArray = new Array(state.map.surface_palette.length);
  biomePaletteEl.replaceChildren();
  surfacePaletteEl.replaceChildren();
  const query = String(biomeSearch?.value ?? "").trim().toLowerCase();
  let shownBiomes = 0;
  state.map.biome_palette.forEach((id, index) => {
    if (query && !String(id).toLowerCase().includes(query)) {
      return;
    }
    biomePaletteEl.append(makePaletteItem(index, "biome"));
    shownBiomes++;
  });
  if (biomeCount) {
    biomeCount.textContent = `${shownBiomes} / ${state.map.biome_palette.length}`;
  }
  state.map.surface_palette.forEach((_, index) => {
    surfacePaletteEl.append(makeSurfacePaletteItem(index));
  });
  state.selectedBiome = clamp(state.selectedBiome, 0, state.map.biome_palette.length - 1);
  state.selectedSurface = clamp(state.selectedSurface, 0, state.map.surface_palette.length - 1);
  updatePaletteSelection();
  buildBiomeCandidates();
}

function biomeGroup(id) {
  if (id.startsWith("reverie_dreams:gensokyo_")) return "幻想乡群系";
  if (id.startsWith("minecraft:")) return "原版群系";
  return "其他模组群系";
}

function biomeDisplayName(id) {
  const entry = state.biomeById.get(id);
  return entry?.name || id;
}

function buildBiomeCandidates() {
  if (!biomeCandidatesEl) return;
  biomeCandidatesEl.replaceChildren();
  const query = String(biomeCandidateSearch?.value ?? "").trim().toLowerCase();
  const current = new Set(state.map?.biome_palette ?? []);
  const groups = new Map();
  for (const biome of state.availableBiomes) {
    if (!biome || typeof biome.id !== "string" || current.has(biome.id)) continue;
    const haystack = `${biome.id} ${biomeDisplayName(biome.id)}`.toLowerCase();
    if (query && !haystack.includes(query)) continue;
    const group = biomeGroup(biome.id);
    if (!groups.has(group)) groups.set(group, []);
    groups.get(group).push(biome);
  }
  let shown = 0;
  for (const [group, biomes] of groups) {
    const heading = document.createElement("h3");
    heading.className = "biome-candidate-group";
    heading.textContent = group;
    biomeCandidatesEl.append(heading);
    for (const biome of biomes) {
      const button = document.createElement("button");
      button.type = "button";
      button.className = "palette-item biome-candidate";
      const swatch = document.createElement("span");
      swatch.className = "swatch";
      swatch.style.background = biome.color || "#777";
      const label = document.createElement("span");
      label.textContent = `${biomeDisplayName(biome.id)} · ${biome.id}`;
      button.append(swatch, label);
      button.addEventListener("click", () => addBiomeToPalette(biome.id));
      biomeCandidatesEl.append(button);
      shown++;
    }
  }
  if (biomeCandidateCount) {
    biomeCandidateCount.textContent = `${shown} / ${Math.max(0, state.availableBiomes.length - current.size)}`;
  }
}

function addBiomeToPalette(id) {
  if (!state.map || !Array.isArray(state.map.biome_palette)) return;
  if (state.map.biome_palette.includes(id)) {
    state.selectedBiome = state.map.biome_palette.indexOf(id);
    updatePaletteSelection();
    return;
  }
  if (state.map.biome_palette.length >= 256) {
    log("群系调色板已达到 256 项上限");
    return;
  }
  state.map.biome_palette.push(id);
  state.selectedBiome = state.map.biome_palette.length - 1;
  markTerrainDirty();
  buildPalettes();
  scheduleRender();
}

function remapSurfaceIndices(mapping) {
  for (let i = 0; i < state.surfaces.length; i++) {
    const oldIndex = state.surfaces[i];
    const next = mapping[oldIndex];
    state.surfaces[i] = Number.isInteger(next) ? next : 0;
  }
}

function createSurfaceBits(index) {
  const bits = new Uint8Array(Math.ceil(state.surfaces.length / 8));
  for (let i = 0; i < state.surfaces.length; i++) {
    if (state.surfaces[i] === index) {
      bits[i >> 3] |= 1 << (i & 7);
    }
  }
  return bits;
}

function applySurfaceBits(bits, value) {
  if (!bits) {
    return;
  }
  for (let i = 0; i < state.surfaces.length; i++) {
    if ((bits[i >> 3] & (1 << (i & 7))) !== 0) {
      state.surfaces[i] = value;
    }
  }
}

function pushPaletteHistory(beforePalette, afterPalette, forward, backward, selectedBefore, selectedAfter, label, extra = {}) {
  state.undoStack.push({
    paletteBefore: [...beforePalette],
    paletteAfter: [...afterPalette],
    surfaceMapping: forward,
    inverseSurfaceMapping: backward,
    selectedSurfaceBefore: selectedBefore,
    selectedSurfaceAfter: selectedAfter,
    label,
    ...extra,
  });
  if (state.undoStack.length > 60) {
    state.undoStack.shift();
  }
  state.redoStack = [];
  updateHistoryButtons();
  broadcastMapMeta();
}

function deleteSurfacePalette(index) {
  if (!canEdit()) {
    log("只读角色不能删除地表调色板");
    return;
  }
  const palette = state.map?.surface_palette;
  if (!palette || palette.length <= 1 || index < 0 || index >= palette.length) {
    return;
  }
  const usedBits = createSurfaceBits(index);
  let used = 0;
  for (let i = 0; i < state.surfaces.length; i++) {
    if (state.surfaces[i] === index) {
      used++;
    }
  }
  const before = [...palette];
  const selectedBefore = state.selectedSurface;
  const fallbackOld = (() => {
    const solid = palette.findIndex((id, candidate) =>
      candidate !== index && id !== "minecraft:air" && id !== "minecraft:cave_air" && id !== "minecraft:void_air");
    return solid >= 0 ? solid : index === 0 ? 1 : 0;
  })();
  palette.splice(index, 1);
  const fallbackNew = fallbackOld < index ? fallbackOld : fallbackOld - 1;
  const mapping = Array.from({ length: before.length }, (_, oldIndex) => {
    if (oldIndex === index) return fallbackNew;
    if (oldIndex > index) return oldIndex - 1;
    return oldIndex;
  });
  const inverse = Array.from({ length: palette.length }, (_, newIndex) =>
    newIndex < index ? newIndex : newIndex + 1);
  remapSurfaceIndices(mapping);
  const selectedAfter = selectedBefore > index ? selectedBefore - 1 : selectedBefore === index ? fallbackNew : selectedBefore;
  state.selectedSurface = clamp(selectedAfter, 0, palette.length - 1);
  pushPaletteHistory(
    before,
    palette,
    mapping,
    inverse,
    selectedBefore,
    state.selectedSurface,
    "删除地表调色板",
    used > 0
      ? { deletedSurfaceIndex: index, deletedFallbackIndex: fallbackNew, deletedSurfaceBits: usedBits }
      : {},
  );
  setDirty(true);
  buildPalettes();
  markTerrainDirty();
  scheduleRender();
  log(used > 0
    ? `已删除地表 palette #${index}，${used} 格地表替换为 palette #${fallbackNew}`
    : `已删除地表 palette #${index}`);
}

function moveSurfacePalette(index, direction) {
  const palette = state.map?.surface_palette;
  if (!palette || index < 0 || index >= palette.length) {
    return;
  }
  const target = index + direction;
  if (target < 0 || target >= palette.length) {
    return;
  }
  const before = [...palette];
  const selectedBefore = state.selectedSurface;
  [palette[index], palette[target]] = [palette[target], palette[index]];
  const mapping = Array.from({ length: palette.length }, (_, oldIndex) => oldIndex);
  mapping[index] = target;
  mapping[target] = index;
  remapSurfaceIndices(mapping);
  if (state.selectedSurface === index) {
    state.selectedSurface = target;
  } else if (state.selectedSurface === target) {
    state.selectedSurface = index;
  }
  pushPaletteHistory(before, palette, mapping, mapping, selectedBefore, state.selectedSurface, "调整地表调色板层");
  setDirty(true);
  buildPalettes();
  markTerrainDirty();
  scheduleRender();
  log(`地表 palette #${index} → #${target}`);
}

function blockDisplayColor(color) {
  if (typeof color !== "string" || !/^#[0-9a-f]{6}$/i.test(color)) {
    return "#7d7d7d";
  }
  const value = Number.parseInt(color.slice(1), 16);
  const red = (value >> 16) & 255;
  const green = (value >> 8) & 255;
  const blue = value & 255;
  return red + green + blue < 36 ? "#59615f" : color;
}

function blockColorInt(blockId) {
  return Number.parseInt(blockDisplayColor(state.blockById.get(blockId)?.color).slice(1), 16);
}

function updateBlockIconCount() {
  if (!blockCount) {
    return;
  }
  const base = blockCount.dataset.base ?? "";
  blockCount.textContent = blockIcons ? `${base} · 图标 ${blockIcons.cache.size}` : base;
}

function ensureBlockIconObserver() {
  if (!blockIcons || typeof IntersectionObserver === "undefined") {
    return null;
  }
  if (!blockIconObserver) {
    blockIconObserver = new IntersectionObserver((entries) => {
      for (const entry of entries) {
        if (!entry.isIntersecting) {
          continue;
        }
        blockIconObserver.unobserve(entry.target);
        loadBlockIcon(entry.target);
      }
    }, { root: blockGrid, rootMargin: "180px" });
  }
  return blockIconObserver;
}

async function loadBlockIcon(cell) {
  if (!blockIcons) {
    return;
  }
  const image = cell.querySelector(".block-cell-icon");
  const blockId = cell.dataset.block;
  if (!image || !blockId) {
    return;
  }
  const dataUrl = await blockIcons.get(blockId, blockColorInt(blockId));
  if (!dataUrl || !image.isConnected) {
    return;
  }
  image.src = dataUrl;
  image.hidden = false;
  cell.classList.add("has-icon");
  updateBlockIconCount();
}

function attachBlockIcon(blockId, dataUrl) {
  if (!blockId || !dataUrl) {
    return;
  }
  const cell = blockGrid.querySelector(`.block-cell[data-block="${blockId.replace(/"/g, '\\"')}"]`);
  if (!cell) {
    return;
  }
  const image = cell.querySelector(".block-cell-icon");
  if (!image) {
    return;
  }
  image.src = dataUrl;
  image.hidden = false;
  cell.classList.add("has-icon");
}

function refreshVisibleBlockIcons() {
  if (!blockIcons) {
    return;
  }
  for (const cell of blockGrid.querySelectorAll(".block-cell:not(.has-icon)")) {
    const dataUrl = blockIcons.cache.get(cell.dataset.block);
    if (dataUrl) {
      attachBlockIcon(cell.dataset.block, dataUrl);
    }
  }
  updateBlockIconCount();
}

function prewarmBlockIcons() {
  if (!blockIcons || !state.blocks.length) {
    return;
  }
  if (document.querySelector('.tool-page[data-page="blocks"]')?.hidden) {
    blockPrewarmTimer = null;
    return;
  }
  const index = Number(blockGrid.dataset.prewarmIndex ?? 0);
  if (index >= state.blocks.length || blockIcons.cache.size >= state.blocks.length) {
    blockPrewarmTimer = null;
    updateBlockIconCount();
    return;
  }
  const block = state.blocks[index];
  blockGrid.dataset.prewarmIndex = String(index + 1);
  blockIcons.get(block.id, blockColorInt(block.id)).then((dataUrl) => {
    if (dataUrl) {
      attachBlockIcon(block.id, dataUrl);
    }
    updateBlockIconCount();
  });
  blockPrewarmTimer = setTimeout(prewarmBlockIcons, 120);
}

function startBlockIconPrewarm() {
  if (!blockIcons || blockPrewarmTimer) {
    return;
  }
  blockPrewarmTimer = setTimeout(prewarmBlockIcons, 200);
}

function populateBlockList() {
  if (!state.blocks.length) {
    blockList.replaceChildren();
    blockGrid.replaceChildren();
    blockCount.textContent = "0 个方块";
    return;
  }
  const query = blockSearch.value.trim().toLowerCase();
  const matches = state.blocks
    .filter((block) => !query || block.id.toLowerCase().includes(query))
    .slice(0, 2000);
  blockIconObserver?.disconnect();
  blockIconObserver = null;
  blockList.replaceChildren();
  const fragment = document.createDocumentFragment();
  const observer = ensureBlockIconObserver();
  for (const block of matches) {
    const option = document.createElement("option");
    option.value = block.id;
    option.textContent = `${block.id}  ${block.color}`;
    blockList.append(option);

    const button = document.createElement("button");
    button.type = "button";
    button.className = "block-cell";
    button.dataset.block = block.id;
    button.title = block.id;
    button.setAttribute("role", "option");
    button.style.setProperty("--block-color", blockDisplayColor(block.color));
    const face = document.createElement("span");
    face.className = "block-cell-face";
    const top = document.createElement("span");
    top.className = "block-cell-top";
    const left = document.createElement("span");
    left.className = "block-cell-left";
    const right = document.createElement("span");
    right.className = "block-cell-right";
    face.append(top, left, right);
    const image = document.createElement("img");
    image.className = "block-cell-icon";
    image.alt = "";
    image.hidden = true;
    button.append(face, image);
    const cached = blockIcons?.cache.get(block.id);
    if (cached) {
      image.src = cached;
      image.hidden = false;
      button.classList.add("has-icon");
    } else if (observer) {
      observer.observe(button);
    }
    button.addEventListener("click", () => {
      blockList.value = block.id;
      blockList.dispatchEvent(new Event("change", { bubbles: true }));
    });
    button.addEventListener("dblclick", () => {
      ensureSurfaceBlock(block.id);
      previewBlock(state.map?.surface_palette?.[state.selectedSurface] ?? block.id);
    });
    fragment.append(button);
  }
  if (matches.length === 0) {
    const empty = document.createElement("p");
    empty.className = "block-empty";
    empty.textContent = "没有匹配的方块";
    fragment.append(empty);
  }
  blockGrid.replaceChildren(fragment);
  if (matches[0]) {
    blockList.value = matches[0].id;
  }
  blockGrid.scrollTop = 0;
  blockCount.dataset.base = `${matches.length} / ${state.blocks.length} 个方块`;
  updateBlockIconCount();
  updateBlockGridSelection();
}

function blockCssColor(blockId) {
  const entry = state.blockById.get(blockIdOf(blockId));
  return blockDisplayColor(entry?.color);
}

function updateBlockGridSelection() {
  if (!blockGrid) {
    return;
  }
  for (const cell of blockGrid.querySelectorAll(".block-cell")) {
    const selected = cell.dataset.block === blockList.value;
    cell.classList.toggle("active", selected);
    cell.setAttribute("aria-selected", String(selected));
  }
  updateHotbar();
}

function updateHotbar() {
  if (!hotbar) {
    return;
  }
  const selectedBlock = state.map?.surface_palette?.[state.selectedSurface] ?? blockList.value ?? "";
  hotbarBlock.style.setProperty("--hotbar-color", blockCssColor(selectedBlock));
  hotbarBlock.title = selectedBlock ? `当前方块：${selectedBlock}` : "当前方块";
  const mode = state.editMode;
  for (const slot of hotbar.querySelectorAll("[data-hotbar]")) {
    const action = slot.dataset.hotbar;
    let active = false;
    if (action === "brush" || action === "rect" || action === "pick") {
      active = mode === action;
    } else if (["raise", "lower", "smooth", "flatten", "set", "void"].includes(action)) {
      active = state.layer === "height" && state.heightTool === action;
    } else if (action === "block") {
      active = selectedBlock !== "";
    } else if (action === "underground") {
      active = state.undergroundMode;
    }
    slot.classList.toggle("active", active);
  }
}

function ensureSurfaceBlock(blockId) {
  if (!canEdit()) {
    log("只读角色不能添加地表方块");
    return;
  }
  if (!state.map || !blockId) {
    return;
  }
  const before = editorSnapshot();
  let index = state.map.surface_palette.indexOf(blockId);
  if (index < 0 && blockIdOf(state.map.surface_palette[state.selectedSurface]) === blockId) {
    index = state.selectedSurface;
  }
  if (index < 0) {
    if (state.map.surface_palette.length >= 65536) {
      log("surface_palette 已满，无法再加入方块");
      return;
    }
    state.map.surface_palette.push(blockId);
    index = state.map.surface_palette.length - 1;
    pushSnapshotHistory(before, "添加地表方块");
    setDirty(true);
    fillMeta();
  }
  state.selectedSurface = index;
  setLayer("surface");
  blockList.value = blockId;
  updateBlockGridSelection();
  buildPalettes();
  markTerrainDirty();
  scheduleRender();
  log(`地表方块：${blockId}（palette #${index}）`);
}

async function loadBlocks() {
  try {
    const response = await fetch("/api/blocks", { cache: "no-cache" });
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const data = await response.json();
    state.blocks = Array.isArray(data.blocks) ? data.blocks : [];
    state.blockById = new Map(state.blocks.map((block) => [block.id, block]));
    populateBlockList();
    if (state.map) {
      buildPalettes();
      markTerrainDirty();
      scheduleRender();
    }
    if (state.blocks[0]) {
      previewBlock(blockList.value || state.blocks[0].id);
    }
    log(`方块调色板：${state.blocks.length} 个已注册方块`);
  } catch (error) {
    log(`方块调色板加载失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

function updateLayerVisibility() {
  for (const button of layerButtons.children) {
    button.classList.toggle("active", button.dataset.layer === state.layer);
  }
  const painting = true;
  heightTools.hidden = !painting || state.layer !== "height";
  brushStrengthSection.hidden = !painting || state.layer !== "height";
  actualModeSection.hidden = state.viewMode !== "3d";
  if (terrain3dDisplaySection) {
    terrain3dDisplaySection.hidden = state.viewMode !== "3d";
  }
  biomePaletteSection.hidden = !painting || state.layer !== "biome";
  const blocksPage = document.querySelector('.tool-page[data-page="blocks"]');
  surfacePaletteSection.hidden = !painting || (state.layer !== "surface" && (!blocksPage || blocksPage.hidden));
  for (const button of heightToolButtons.children) {
    button.classList.toggle("active", button.dataset.tool === state.heightTool);
  }
  for (const button of editModeButtons.children) {
    button.classList.toggle("active", button.dataset.mode === state.editMode);
  }
  updateHotbar();
  scheduleRender();
}

function setLayer(layer) {
  state.layer = layer;
  markTerrainDirty();
  updateLayerVisibility();
  updateHud(state.cursor);
}

function setEditMode(mode) {
  state.editMode = mode;
  state.rect = null;
  state.rectDrawing = false;
  for (const button of editModeButtons.children) {
    button.classList.toggle("active", button.dataset.mode === mode);
  }
  updateHotbar();
  const labels = { brush: "笔刷", rect: "矩形", select: "选区", pick: "吸管" };
  log(`模式：${labels[mode] ?? mode}`);
  updateSelectionInfo();
  scheduleRender();
}

function setToolPage(page) {
  state.toolPage = page;
  viewportStage.classList.toggle("structure-mode", page === "objects");
  if (page !== "objects" && state.movingAnchorId) {
    state.movingAnchorId = null;
    state.movingAnchorIds = [];
    state.movingAnchorOrigins = null;
  }
  if (page !== "objects" && state.forestMode) {
    state.forestMode = false;
    forestModeBtn.classList.remove("active");
    forestModeBtn.textContent = "森林笔刷";
    viewportStage.classList.remove("forest-mode");
  }
  if (page !== "objects") {
    state.structureSelecting = false;
    state.structureSelectRect = null;
    structureSelectBox.hidden = true;
  }
  for (const button of toolTabs.children) {
    button.classList.toggle("active", button.dataset.page === page);
  }
  for (const section of document.querySelectorAll(".tool-page")) {
    section.hidden = section.dataset.page !== page;
  }
  updateLayerVisibility();
  if (!state.map) {
    return;
  }
  if (page === "blocks") {
    requestAnimationFrame(() => {
      blockPreview?.resize();
      refreshVisibleBlockIcons();
      updateBlockGridSelection();
    });
    startBlockIconPrewarm();
  }
  if (page === "objects") {
    updateHud(state.cursor);
  }
  if (page === "light") {
    scheduleRender();
  }
}

function updateHistoryButtons() {
  undoBtn.disabled = state.undoStack.length === 0;
  redoBtn.disabled = state.redoStack.length === 0;
}

function setHeightTool(tool) {
  state.heightTool = tool;
  updateLayerVisibility();
}

function setViewMode(mode) {
  state.viewMode = mode;
  for (const button of viewModeButtons.children) {
    button.classList.toggle("active", button.dataset.view === mode);
  }
  canvas.classList.toggle("preview", false);
  canvas.classList.remove("mode-3d");
  canvas.style.display = mode === "3d" ? "none" : "block";
  terrainCanvas.style.display = mode === "3d" ? "block" : "none";
  terrainCanvas.classList.toggle("mode-3d", mode === "3d");
  state.cursor = null;
  resizeViewport();
  updateLayerVisibility();
  updateTerrain3dControls();
  updateHud(null);
  if (mode === "3d") {
    log("3D 编辑：左键绘制，右键拖动旋转，中键拖动平移，滚轮缩放");
  } else {
    log("2D 编辑：滚轮缩放，中键/右键拖动平移");
  }
}

function updateTerrain3dControls() {
  if (!terrain3dExaggeration || !terrain3dDetail) {
    return;
  }
  const auto = !Number(state.view3D.exaggeration);
  const effective = terrainView?.effectiveExaggeration?.(state) ?? 1;
  const exaggerationValue = auto ? "auto" : String(state.view3D.exaggeration);
  if (terrain3dExaggeration.value !== exaggerationValue) {
    terrain3dExaggeration.value = exaggerationValue;
  }
  const detailValue = String(state.view3D.detail ?? 384);
  if (terrain3dDetail.value !== detailValue) {
    terrain3dDetail.value = detailValue;
  }
  const xzScaleValue = String(state.view3D.xzScale ?? 1);
  if (terrain3dXzScale && terrain3dXzScale.value !== xzScaleValue) {
    terrain3dXzScale.value = xzScaleValue;
  }
  if (terrain3dXzScaleValue) {
    terrain3dXzScaleValue.textContent = `XZ ${Number(xzScaleValue)}×`;
  }
  if (terrain3dExaggerationValue) {
    terrain3dExaggerationValue.textContent = `${effective}×${auto ? " 自动" : ""}`;
  }
}

function updateLightingLabels() {
  lightAzimuthValue.textContent = `${Math.round(state.light.azimuth)}°`;
  lightElevationValue.textContent = `${Math.round(state.light.elevation)}°`;
  lightIntensityValue.textContent = state.light.intensity.toFixed(1);
}

function applyLightingSettings(persist = true) {
  state.light.azimuth = clamp(Number(state.light.azimuth), 0, 360);
  state.light.elevation = clamp(Number(state.light.elevation), 5, 85);
  state.light.intensity = clamp(Number(state.light.intensity), 0.2, 3.5);
  state.light.shadows = state.light.shadows !== false;
  updateLightingLabels();
  if (terrainView) {
    terrainView.setLighting(state.light);
  }
  if (persist) {
    try {
      localStorage.setItem(LIGHT_STORAGE_KEY, JSON.stringify(state.light));
    } catch {
      // Editor still works when localStorage is unavailable.
    }
  }
  scheduleRender();
}

function syncLightingControls() {
  lightAzimuth.value = String(state.light.azimuth);
  lightElevation.value = String(state.light.elevation);
  lightIntensity.value = String(state.light.intensity);
  shadowToggle.checked = state.light.shadows !== false;
  updateLightingLabels();
}

function fitView() {
  state.view.centerX = state.map.width / 2;
  state.view.centerZ = state.map.depth / 2;
  state.view.zoom = 1;
  state.view3D.yaw = Math.PI / 4;
  state.view3D.pitch = 0.55;
  state.view3D.zoom = Number(state.view3D.detail) === 0 ? 2 : 1;
  state.view3D.targetX = state.map.width / 2;
  state.view3D.targetZ = state.map.depth / 2;
  scheduleRender();
}

function isDecorationTemplate(template) {
  const name = String(template ?? "").split(":").pop();
  return name.startsWith("tree_") || name.startsWith("deco_");
}

function decorationBaseY(template, height) {
  return height + (isDecorationTemplate(template) ? 1 : 0);
}

function selectedAnchors() {
  const ids = state.selectedAnchorIds instanceof Set ? state.selectedAnchorIds : new Set();
  return (state.map?.anchors ?? []).filter((anchor) => ids.has(anchor.id));
}

function selectedAnchor() {
  return state.map?.anchors?.find((anchor) => anchor.id === state.selectedAnchorId) ?? null;
}

const ANCHOR_ROTATIONS = ["none", "clockwise_90", "clockwise_180", "counterclockwise_90"];

function normalizeAnchorRotation(value) {
  const text = String(value ?? "none").toLowerCase();
  if (text === "180") {
    return "clockwise_180";
  }
  return ANCHOR_ROTATIONS.includes(text) ? text : "none";
}

const ANCHOR_SCALE_MIN = 0.25;
const ANCHOR_SCALE_MAX = 4;

function normalizeAnchorScale(value) {
  const numeric = Number(value);
  if (!Number.isFinite(numeric)) {
    return 1;
  }
  return Math.round(clamp(numeric, ANCHOR_SCALE_MIN, ANCHOR_SCALE_MAX) * 100) / 100;
}

function refreshAnchorEditor() {
  const anchors = selectedAnchors();
  const anchor = selectedAnchor();
  if (!anchor) {
    anchorEditor.hidden = true;
    return;
  }
  anchorEditor.hidden = false;
  const template = String(anchor.template ?? anchor.structure ?? "?");
  const prefix = anchors.length > 1 ? `已选 ${anchors.length} 个 · 主结构 ` : "";
  anchorEditorName.textContent = `${prefix}${anchor.id} · ${template}`;
  const pos = Array.isArray(anchor.pos) ? anchor.pos : [0, 0, 0];
  anchorX.value = String(pos[0] ?? 0);
  anchorY.value = String(pos[1] ?? 0);
  anchorZ.value = String(pos[2] ?? 0);
  anchorRotation.value = normalizeAnchorRotation(anchor.rotation);
  anchorMirror.value = ["none", "left_right", "front_back"].includes(anchor.mirror)
    ? anchor.mirror
    : "none";
  anchorIntegrity.value = String(Number.isFinite(Number(anchor.integrity)) ? anchor.integrity : 1);
  if (anchorScale) {
    anchorScale.value = String(normalizeAnchorScale(anchor.scale));
  }
}

function setAnchorSelection(ids, primary = null) {
  const available = new Set((state.map?.anchors ?? []).map((anchor) => anchor.id));
  state.selectedAnchorIds = new Set([...ids].filter((id) => available.has(id)));
  state.selectedAnchorId = primary && state.selectedAnchorIds.has(primary)
    ? primary
    : [...state.selectedAnchorIds].at(-1) ?? null;
  state.movingAnchorId = null;
  state.movingAnchorIds = [];
  state.movingAnchorOrigins = null;
  refreshAnchorList();
  refreshAnchorEditor();
  if (terrainView) {
    terrainView.setAnchorSelection(state.selectedAnchorIds);
  }
  updateHud(state.cursor);
  scheduleRender();
}

function selectAnchor(id, additive = false) {
  if (additive) {
    const next = new Set(state.selectedAnchorIds);
    if (next.has(id)) {
      next.delete(id);
    } else {
      next.add(id);
    }
    setAnchorSelection(next, next.has(id) ? id : null);
  } else {
    setAnchorSelection([id], id);
  }
}

function deleteAnchors(ids) {
  if (!state.map || !ids.length) {
    return;
  }
  const remove = new Set(ids);
  const existing = state.map.anchors.filter((anchor) => remove.has(anchor.id));
  if (existing.length === 0) {
    return;
  }
  const before = editorSnapshot();
  state.map.anchors = state.map.anchors.filter((anchor) => !remove.has(anchor.id));
  const remaining = [...state.selectedAnchorIds].filter((id) => !remove.has(id));
  const primary = remaining.at(-1) ?? null;
  pushSnapshotHistory(before, existing.length > 1 ? `删除 ${existing.length} 个结构` : "删除结构");
  setAnchorSelection(remaining, primary);
  setDirty(true);
  fillMeta();
  markTerrainDirty();
  scheduleRender();
  log(`已删除结构 ${existing.map((anchor) => anchor.id).join(", ")}`);
}

function deleteAnchorById(id) {
  deleteAnchors([id]);
}

function deleteSelectedAnchor() {
  if (!canEdit()) {
    log("只读角色不能删除结构");
    return;
  }
  deleteAnchors([...state.selectedAnchorIds]);
}

function beginAnchorMove() {
  if (!canEdit()) {
    log("只读角色不能移动结构");
    return;
  }
  const anchors = selectedAnchors();
  if (anchors.length === 0) {
    log("请先选中至少一个结构");
    return;
  }
  setForestMode(false);
  const anchor = selectedAnchor() ?? anchors[0];
  if (state.placement) {
    state.placement = null;
    placeModeBtn.textContent = "进入放置模式";
    placeModeBtn.classList.remove("active");
  }
  state.movingAnchorIds = anchors.map((entry) => entry.id);
  state.movingAnchorOrigins = new Map(anchors.map((entry) => [entry.id, [...entry.pos]]));
  state.movingAnchorId = anchor.id;
  state.selectedAnchorId = anchor.id;
  setToolPage("objects");
  updateHud(state.cursor);
  scheduleRender();
  log(`移动 ${anchors.length} 个结构：点击目标格确认，Esc 取消`);
}

function moveAnchorToCell(cell) {
  if (!canEdit()) {
    log("只读角色不能移动结构");
    return;
  }
  if (!state.map || !cell) {
    return;
  }
  const ids = state.movingAnchorIds.length > 0
    ? state.movingAnchorIds
    : state.movingAnchorId ? [state.movingAnchorId] : [];
  const anchors = (state.map.anchors ?? []).filter((anchor) => ids.includes(anchor.id));
  const primary = anchors.find((anchor) => anchor.id === state.movingAnchorId) ?? anchors[0];
  if (!primary) {
    return;
  }
  const origins = state.movingAnchorOrigins ?? new Map();
  const targetX = state.map.origin[0] + cell.x;
  const targetZ = state.map.origin[1] + cell.z;
  const base = origins.get(primary.id) ?? primary.pos;
  const dx = targetX - base[0];
  const dz = targetZ - base[2];
  const heightAt = (worldX, worldZ) => {
    const x = worldX - state.map.origin[0];
    const z = worldZ - state.map.origin[1];
    if (x < 0 || z < 0 || x >= state.map.width || z >= state.map.depth) {
      return null;
    }
    const value = state.heights[z * state.map.width + x];
    return value === VOID ? null : value;
  };
  const moves = [];
  for (const anchor of anchors) {
    const origin = origins.get(anchor.id) ?? anchor.pos;
    const newX = origin[0] + dx;
    const newZ = origin[2] + dz;
    const oldHeight = heightAt(origin[0], origin[2]);
    const newHeight = heightAt(newX, newZ);
    if (oldHeight == null || newHeight == null) {
      log("目标位置超出地图或为 void，不能移动结构组");
      return;
    }
    moves.push({
      anchor,
      pos: [newX, origin[1] + (newHeight - oldHeight), newZ],
    });
  }
  const before = editorSnapshot();
  for (const move of moves) {
    move.anchor.pos = move.pos;
  }
  state.movingAnchorId = null;
  state.movingAnchorIds = [];
  state.movingAnchorOrigins = null;
  pushSnapshotHistory(before, moves.length > 1 ? `移动 ${moves.length} 个结构` : "移动结构");
  setDirty(true);
  refreshAnchorList();
  fillMeta();
  markTerrainDirty();
  scheduleRender();
  log(`已移动 ${moves.length} 个结构`);
}

function anchorHeightAtWorld(worldX, worldZ) {
  if (!state.map) {
    return null;
  }
  const x = worldX - state.map.origin[0];
  const z = worldZ - state.map.origin[1];
  if (x < 0 || z < 0 || x >= state.map.width || z >= state.map.depth) {
    return null;
  }
  const value = state.heights[z * state.map.width + x];
  return value === VOID ? null : value;
}

function startAnchorHandleDrag(event, picked) {
  if (!canEdit()) {
    log("只读角色不能拖动结构");
    return;
  }
  if (!state.map || !picked) {
    return;
  }
  const anchor = (state.map.anchors ?? []).find((entry) => entry.id === picked.anchorId);
  if (!anchor) {
    return;
  }
  if (!state.selectedAnchorIds.has(anchor.id)) {
    selectAnchor(anchor.id, event.ctrlKey || event.shiftKey);
  }
  state.selectedAnchorId = anchor.id;
  refreshAnchorEditor();
  const anchors = selectedAnchors();
  if (anchors.length === 0) {
    return;
  }
  const primary = state.map.anchors.find((entry) => entry.id === state.selectedAnchorId) ?? anchor;
  const mode = picked.handle === "rotate"
    ? "rotate"
    : picked.handle === "y"
      ? "y"
      : picked.handle === "scale"
        ? "scale"
        : "move";
  const drag = {
    mode,
    pointerId: event.pointerId,
    startClientX: event.clientX,
    startClientY: event.clientY,
    anchorId: primary.id,
    origins: new Map(anchors.map((entry) => [entry.id, [...entry.pos]])),
    initialRotation: normalizeAnchorRotation(primary.rotation),
    initialScale: normalizeAnchorScale(primary.scale),
    before: editorSnapshot(),
    moved: false,
    lastCell: null,
    lastYStep: 0,
    lastRotateStep: 0,
    startAngle: 0,
    anchorScreen: null,
  };
  if (mode === "rotate") {
    const point = terrainView?.projectAnchor(primary);
    if (!point) {
      return;
    }
    drag.startAngle = Math.atan2(event.clientY - point.y, event.clientX - point.x);
    drag.anchorScreen = point;
  }
  state.draggingAnchor = drag;
  state.movingAnchorId = null;
  state.movingAnchorIds = [];
  state.movingAnchorOrigins = null;
  try {
    viewportStage.setPointerCapture(event.pointerId);
  } catch {
    // Synthetic events may not support capture.
  }
  updateHud(state.cursor);
  scheduleRender();
  log(mode === "rotate"
    ? "拖拽旋转结构"
    : mode === "y"
      ? "拖拽调整结构高度"
      : mode === "scale"
        ? "拖拽缩放结构"
        : `拖拽移动 ${anchors.length} 个结构`);
}

function updateAnchorHandleDrag(event) {
  const drag = state.draggingAnchor;
  if (!drag || !state.map) {
    return;
  }
  if (drag.mode === "move") {
    const cell = pick3DCell(event);
    if (!cell) {
      return;
    }
    if (drag.lastCell && drag.lastCell.x === cell.x && drag.lastCell.z === cell.z) {
      return;
    }
    drag.lastCell = cell;
    const primaryOrigin = drag.origins.get(drag.anchorId);
    if (!primaryOrigin) {
      return;
    }
    const targetX = state.map.origin[0] + cell.x;
    const targetZ = state.map.origin[1] + cell.z;
    const dx = targetX - primaryOrigin[0];
    const dz = targetZ - primaryOrigin[2];
    const updates = [];
    for (const [id, origin] of drag.origins) {
      const anchor = (state.map.anchors ?? []).find((entry) => entry.id === id);
      if (!anchor) {
        continue;
      }
      const oldHeight = anchorHeightAtWorld(origin[0], origin[2]);
      const newX = origin[0] + dx;
      const newZ = origin[2] + dz;
      const newHeight = anchorHeightAtWorld(newX, newZ);
      if (oldHeight === null || newHeight === null) {
        return;
      }
      updates.push({
        anchor,
        pos: [newX, origin[1] + (newHeight - oldHeight), newZ],
      });
    }
    for (const update of updates) {
      update.anchor.pos = update.pos;
    }
    drag.moved = drag.moved || dx !== 0 || dz !== 0;
    refreshAnchorEditor();
    terrainView?.refreshAnchors(state);
    updateHud(cell);
    scheduleRender();
    return;
  }
  if (drag.mode === "y") {
    const steps = Math.round((drag.startClientY - event.clientY) / 8);
    if (steps === drag.lastYStep) {
      return;
    }
    drag.lastYStep = steps;
    const minY = state.map.min_y;
    const maxY = state.map.min_y + state.map.world_height - 1;
    let changed = false;
    for (const [id, origin] of drag.origins) {
      const anchor = (state.map.anchors ?? []).find((entry) => entry.id === id);
      if (!anchor) {
        continue;
      }
      const y = clamp(origin[1] + steps, minY, maxY);
      if (y !== anchor.pos[1]) {
        changed = true;
      }
      anchor.pos = [origin[0], y, origin[2]];
    }
    drag.moved = drag.moved || changed;
    refreshAnchorEditor();
    terrainView?.refreshAnchors(state);
    scheduleRender();
    return;
  }
  if (drag.mode === "scale") {
    const anchor = (state.map.anchors ?? []).find((entry) => entry.id === drag.anchorId);
    if (!anchor) {
      return;
    }
    const nextScale = normalizeAnchorScale(drag.initialScale + (event.clientX - drag.startClientX) / 120);
    if (nextScale === normalizeAnchorScale(anchor.scale)) {
      return;
    }
    anchor.scale = nextScale;
    drag.moved = drag.moved || nextScale !== drag.initialScale;
    refreshAnchorEditor();
    terrainView?.refreshAnchors(state);
    scheduleRender();
    return;
  }
  if (drag.mode === "rotate") {
    const anchor = (state.map.anchors ?? []).find((entry) => entry.id === drag.anchorId);
    if (!anchor) {
      return;
    }
    const point = drag.anchorScreen ?? terrainView?.projectAnchor(anchor);
    if (!point) {
      return;
    }
    const angle = Math.atan2(event.clientY - point.y, event.clientX - point.x);
    let delta = angle - drag.startAngle;
    delta = Math.atan2(Math.sin(delta), Math.cos(delta));
    const steps = Math.round(delta / (Math.PI / 2));
    if (steps === drag.lastRotateStep) {
      return;
    }
    drag.lastRotateStep = steps;
    const startIndex = Math.max(0, ANCHOR_ROTATIONS.indexOf(normalizeAnchorRotation(drag.initialRotation)));
    const nextIndex = ((startIndex + steps) % ANCHOR_ROTATIONS.length + ANCHOR_ROTATIONS.length) % ANCHOR_ROTATIONS.length;
    const next = ANCHOR_ROTATIONS[nextIndex];
    if (anchor.rotation !== next) {
      anchor.rotation = next;
      drag.moved = true;
    }
    refreshAnchorEditor();
    terrainView?.refreshAnchors(state);
    scheduleRender();
  }
}

function finishAnchorHandleDrag(event) {
  const drag = state.draggingAnchor;
  if (!drag) {
    return;
  }
  state.draggingAnchor = null;
  try {
    if (event?.pointerId !== undefined && viewportStage.hasPointerCapture(event.pointerId)) {
      viewportStage.releasePointerCapture(event.pointerId);
    }
  } catch {
    // Ignore capture errors.
  }
  if (drag.moved) {
    const label = drag.mode === "rotate"
      ? "3D 旋转结构"
      : drag.mode === "y"
        ? "3D 调整结构高度"
        : drag.mode === "scale"
          ? "3D 缩放结构"
          : "3D 拖拽结构";
    if (pushSnapshotHistory(drag.before, label)) {
      setDirty(true);
      fillMeta();
      refreshAnchorList();
      refreshAnchorEditor();
      terrainView?.refreshAnchors(state);
      scheduleRender();
      log(drag.mode === "rotate"
        ? "已旋转结构"
        : drag.mode === "y"
          ? "已调整结构高度"
          : drag.mode === "scale"
            ? "已缩放结构"
            : "已拖拽结构");
    }
  } else {
    refreshAnchorEditor();
    terrainView?.refreshAnchors(state);
    scheduleRender();
  }
  updateHud(state.cursor);
}

function cancelAnchorHandleDrag() {
  const drag = state.draggingAnchor;
  if (!drag || !state.map) {
    return;
  }
  for (const [id, pos] of drag.origins) {
    const anchor = (state.map.anchors ?? []).find((entry) => entry.id === id);
    if (anchor) {
      anchor.pos = [...pos];
    }
  }
  if (drag.mode === "rotate") {
    const anchor = (state.map.anchors ?? []).find((entry) => entry.id === drag.anchorId);
    if (anchor) {
      anchor.rotation = drag.initialRotation;
    }
  }
  if (drag.mode === "scale") {
    const anchor = (state.map.anchors ?? []).find((entry) => entry.id === drag.anchorId);
    if (anchor) {
      anchor.scale = drag.initialScale;
    }
  }
  state.draggingAnchor = null;
  try {
    if (drag.pointerId !== undefined && viewportStage.hasPointerCapture(drag.pointerId)) {
      viewportStage.releasePointerCapture(drag.pointerId);
    }
  } catch {
    // Ignore capture errors.
  }
  refreshAnchorList();
  refreshAnchorEditor();
  fillMeta();
  terrainView?.refreshAnchors(state);
  updateHud(state.cursor);
  scheduleRender();
  log("已取消 3D 结构拖拽");
}

function applyAnchorCoordinates() {
  if (!canEdit()) {
    log("只读角色不能修改结构坐标");
    return;
  }
  const anchor = selectedAnchor();
  if (!anchor || !state.map) {
    return;
  }
  const pos = [Number(anchorX.value), Number(anchorY.value), Number(anchorZ.value)];
  if (!pos.every(Number.isInteger)) {
    log("结构坐标必须是三个整数");
    return;
  }
  const minY = state.map.min_y;
  const maxY = state.map.min_y + state.map.world_height - 1;
  const localX = pos[0] - state.map.origin[0];
  const localZ = pos[2] - state.map.origin[1];
  if (localX < 0 || localX >= state.map.width || localZ < 0 || localZ >= state.map.depth || pos[1] < minY || pos[1] > maxY) {
    log("结构坐标超出地图范围");
    return;
  }
  const before = editorSnapshot();
  anchor.pos = pos;
  state.selectedAnchorId = anchor.id;
  state.movingAnchorId = null;
  state.movingAnchorIds = [];
  state.movingAnchorOrigins = null;
  pushSnapshotHistory(before, "修改结构坐标");
  setDirty(true);
  refreshAnchorList();
  fillMeta();
  markTerrainDirty();
  scheduleRender();
  log(`结构 ${anchor.id} 坐标更新为 ${pos.join(", ")}`);
}

function applyAnchorTransform() {
  if (!canEdit()) {
    log("只读角色不能修改结构变换");
    return;
  }
  const anchor = selectedAnchor();
  if (!anchor || !state.map) {
    return;
  }
  const before = editorSnapshot();
  anchor.rotation = normalizeAnchorRotation(anchorRotation.value);
  anchor.mirror = anchorMirror.value;
  anchor.integrity = clamp(Number(anchorIntegrity.value), 0, 1);
  anchor.scale = normalizeAnchorScale(anchorScale?.value);
  if (!pushSnapshotHistory(before, "修改结构变换")) {
    log("结构变换没有变化");
    return;
  }
  setDirty(true);
  refreshAnchorList();
  fillMeta();
  markTerrainDirty();
  scheduleRender();
  log(`结构 ${anchor.id} 变换：旋转=${anchor.rotation}，镜像=${anchor.mirror}，完整性=${anchor.integrity}，缩放=${anchor.scale}`);
}

function refreshAnchorList() {
  anchorList.replaceChildren();
  const anchors = state.map?.anchors ?? [];
  const available = new Set(anchors.map((anchor) => anchor.id));
  state.selectedAnchorIds = new Set([...state.selectedAnchorIds].filter((id) => available.has(id)));
  if (state.selectedAnchorId && !available.has(state.selectedAnchorId)) {
    state.selectedAnchorId = [...state.selectedAnchorIds].at(-1) ?? null;
  }
  if (anchors.length === 0) {
    state.selectedAnchorId = null;
    state.selectedAnchorIds.clear();
    const empty = document.createElement("p");
    empty.className = "hint";
    empty.textContent = "暂无锚点";
    anchorList.append(empty);
    refreshAnchorEditor();
    return;
  }
  anchors.forEach((anchor, index) => {
    const row = document.createElement("div");
    row.className = "anchor-item";
    if (state.selectedAnchorIds.has(anchor.id)) {
      row.classList.add("selected");
    }
    row.addEventListener("click", (event) => selectAnchor(anchor.id, event.ctrlKey || event.shiftKey));
    const label = document.createElement("span");
    label.className = "anchor-label";
    const pos = Array.isArray(anchor.pos) ? anchor.pos.join(", ") : "?";
    const template = String(anchor.template ?? anchor.structure ?? "").replace(/^.*:/, "");
    const size = anchorFootprintSize(anchor);
    const sizeText = Array.isArray(size) ? ` [${size.join("×")}]` : "";
    label.textContent = `${anchor.id} · ${template}${sizeText} @ ${pos}`;
    const actions = document.createElement("div");
    actions.className = "anchor-actions";
    const move = document.createElement("button");
    move.type = "button";
    move.className = "move";
    move.textContent = "移动";
    move.addEventListener("click", (event) => {
      event.stopPropagation();
      selectAnchor(anchor.id);
      beginAnchorMove();
    });
    const remove = document.createElement("button");
    remove.type = "button";
    remove.textContent = "删除";
    remove.addEventListener("click", (event) => {
      event.stopPropagation();
      deleteAnchorById(anchor.id);
    });
    actions.append(move, remove);
    row.append(label, actions);
    anchorList.append(row);
  });
  refreshAnchorEditor();
}

async function loadBiomes() {
  try {
    const response = await fetch("/api/biomes", { cache: "no-cache" });
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const data = await response.json();
    state.availableBiomes = Array.isArray(data.biomes)
      ? data.biomes.filter((biome) => biome && typeof biome.id === "string")
      : [];
    state.biomeById = new Map(state.availableBiomes.map((biome) => [biome.id, biome]));
    buildBiomeCandidates();
    log(`群系库：${state.availableBiomes.length} 个注册群系（原版 + 模组）；候选库与当前地图调色板已分离`);
  } catch (error) {
    log(`群系库读取失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

async function loadTemplates() {
  let templates = [];
  try {
    const response = await fetch("/api/templates", { cache: "no-store" });
    if (response.ok) {
      const result = await response.json();
      templates = Array.isArray(result.templates) ? result.templates : [];
      state.templateSizes = result.sizes && typeof result.sizes === "object" ? result.sizes : {};
    }
  } catch {
    // 保留 fallback
  }
  if (templates.length === 0) {
    templates = [
      "reverie_dreams:small_shrine",
      "reverie_dreams:outer_shrine",
      "reverie_dreams:mini_bar",
      "reverie_dreams:sakurazuka",
    ];
    state.templateSizes = {};
  }
  state.templates = templates;
  anchorTemplate.replaceChildren();
  for (const template of templates) {
    const option = document.createElement("option");
    option.value = template;
    const size = state.templateSizes[template];
    const sizeText = Array.isArray(size) ? `  [${size.join("×")}]` : "";
    option.textContent = template.replace(/^.*:/, "") + sizeText;
    anchorTemplate.append(option);
  }
  state.selectedTemplate = templates.includes(state.selectedTemplate) ? state.selectedTemplate : templates[0];
  anchorTemplate.value = state.selectedTemplate;
  const decorations = templates.filter((id) => {
    const name = id.split(":").pop();
    return name.startsWith("tree_") || name.startsWith("deco_");
  });
  state.decorations = decorations;
  decorationTemplate.replaceChildren();
  const mix = document.createElement("option");
  mix.value = "__mix__";
  mix.textContent = "随机树木（混合）";
  decorationTemplate.append(mix);
  for (const template of decorations) {
    const option = document.createElement("option");
    option.value = template;
    option.textContent = template.split(":").pop();
    decorationTemplate.append(option);
  }
  if (state.selectedDecoration !== "__mix__" && !decorations.includes(state.selectedDecoration)) {
    state.selectedDecoration = "__mix__";
  }
  decorationTemplate.value = state.selectedDecoration;
  refreshAnchorList();
  markTerrainDirty();
  scheduleRender();
}

function placeAnchorAtCell(cell, templateOverride = null) {
  if (!state.map || !state.heights || !cell) {
    return false;
  }
  const height = state.heights[cell.z * state.map.width + cell.x];
  if (height === VOID) {
    log("该列是 void，不能放锚点");
    return false;
  }
  const template = templateOverride || state.selectedTemplate || state.templates[0];
  if (!template) {
    log("没有可用的结构模板");
    return false;
  }
  const worldX = state.map.origin[0] + cell.x;
  const worldZ = state.map.origin[1] + cell.z;
  const baseY = decorationBaseY(template, height);
  const id = `anchor_${Date.now().toString(36)}`;
  const before = editorSnapshot();
  state.map.anchors = state.map.anchors ?? [];
  state.map.anchors.push({
    id,
    type: "template",
    template,
    pos: [worldX, baseY, worldZ],
    rotation: "none",
    mirror: "none",
    integrity: 1.0,
    scale: 1,
  });
  state.selectedAnchorId = id;
  pushSnapshotHistory(before, "放置结构");
  setDirty(true);
  refreshAnchorList();
  fillMeta();
  markTerrainDirty();
  scheduleRender();
  log(`已放置结构 ${id} (${template}) @ ${worldX}, ${baseY}, ${worldZ}`);
  return true;
}

function setForestMode(active) {
  const next = Boolean(active);
  if (state.forestMode === next) {
    return;
  }
  state.forestMode = next;
  viewportStage.classList.toggle("forest-mode", state.forestMode);
  if (state.forestMode) {
    state.placement = null;
    state.movingAnchorId = null;
    state.movingAnchorIds = [];
    state.movingAnchorOrigins = null;
    placeModeBtn.textContent = "进入放置模式";
    placeModeBtn.classList.remove("active");
    setToolPage("objects");
  }
  forestModeBtn.classList.toggle("active", state.forestMode);
  forestModeBtn.textContent = state.forestMode ? "退出森林笔刷" : "森林笔刷";
  updateHud(state.cursor);
  scheduleRender();
  log(state.forestMode ? "森林笔刷：点击地图种植随机树木" : "已退出森林笔刷");
}

function plantForest(cell) {
  if (!canEdit()) {
    log("只读角色不能种植结构");
    return;
  }
  if (!state.map || !cell || !state.heights) {
    return;
  }
  const trees = state.decorations.filter((id) => id.split(":").pop().startsWith("tree_"));
  if (trees.length === 0) {
    log("没有可用的树木模板");
    return;
  }
  const chosen = state.selectedDecoration !== "__mix__" && trees.includes(state.selectedDecoration)
    ? state.selectedDecoration
    : null;
  const radius = Math.max(2, state.brushSize + 2);
  const density = clamp(Number(state.forestDensity), 0.05, 1);
  const count = Math.max(1, Math.round((2 * radius + 1) ** 2 * density / 8));
  const before = editorSnapshot();
  const existingAnchors = [...(state.map.anchors ?? [])];
  const placed = [];
  const occupied = new Set();
  let attempts = 0;
  while (placed.length < count && attempts < count * 12) {
    attempts++;
    const x = cell.x + Math.floor(Math.random() * (radius * 2 + 1)) - radius;
    const z = cell.z + Math.floor(Math.random() * (radius * 2 + 1)) - radius;
    if (x < 0 || z < 0 || x >= state.map.width || z >= state.map.depth) {
      continue;
    }
    const key = `${x},${z}`;
    if (occupied.has(key)) {
      continue;
    }
    let tooClose = false;
    for (const other of placed) {
      if (Math.abs(other.x - x) + Math.abs(other.z - z) < 2) {
        tooClose = true;
        break;
      }
    }
    if (tooClose) {
      continue;
    }
    const height = state.heights[z * state.map.width + x];
    if (height === VOID) {
      continue;
    }
    if (state.forestAvoidWater && height < (state.map.sea_level ?? 0)) {
      continue;
    }
    if (state.forestAvoidStructures) {
      const worldX = state.map.origin[0] + x;
      const worldZ = state.map.origin[1] + z;
      const blocked = existingAnchors.some((anchor) => {
        if (!Array.isArray(anchor.pos)) {
          return false;
        }
        const size = state.templateSizes?.[anchor.template];
        const radiusX = Math.max(1, (size?.[0] ?? 1) / 2 + 1);
        const radiusZ = Math.max(1, (size?.[2] ?? 1) / 2 + 1);
        return Math.abs(anchor.pos[0] - worldX) < radiusX && Math.abs(anchor.pos[2] - worldZ) < radiusZ;
      });
      if (blocked) {
        continue;
      }
    }
    occupied.add(key);
    const template = chosen ?? trees[Math.floor(Math.random() * trees.length)];
    const anchor = {
      id: `tree_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 5)}_${placed.length}`,
      type: "template",
      template,
      pos: [state.map.origin[0] + x, decorationBaseY(template, height), state.map.origin[1] + z],
      rotation: "none",
      mirror: "none",
      integrity: 1,
      scale: 1,
    };
    state.map.anchors.push(anchor);
    placed.push({ x, z, id: anchor.id });
  }
  if (placed.length === 0) {
    log("森林笔刷没有找到可种植的位置");
    return;
  }
  pushSnapshotHistory(before, `种植 ${placed.length} 棵树`);
  setAnchorSelection(placed.map((entry) => entry.id), placed.at(-1).id);
  setDirty(true);
  fillMeta();
  markTerrainDirty();
  scheduleRender();
  log(`森林笔刷种下 ${placed.length} 棵树（半径 ${radius}）`);
}

function addAnchorAtCursor() {
  if (!canEdit()) {
    log("只读角色不能添加锚点");
    return;
  }
  if (!state.cursor) {
    log("请先把鼠标移到地图上，再点添加锚点");
    return;
  }
  placeAnchorAtCell(state.cursor);
}

function setPlacementMode(active) {
  if (active) {
    const template = state.selectedTemplate || state.templates[0];
    if (!template) {
      log("没有可用的结构模板");
      return;
    }
    state.placement = { template };
    placeModeBtn.textContent = "退出放置模式";
    placeModeBtn.classList.add("active");
    state.editMode = "brush";
    updateLayerVisibility();
    log(`进入放置模式：${template}；点击地图放置，Esc 退出`);
  } else {
    const wasActive = Boolean(state.placement);
    state.placement = null;
    placeModeBtn.textContent = "进入放置模式";
    placeModeBtn.classList.remove("active");
    if (wasActive) {
      log("已退出放置模式");
    }
  }
  updateHud(state.cursor);
  scheduleRender();
}

function togglePlacementMode() {
  setPlacementMode(!state.placement);
}

function initMap(map, heights, biomes, surfaces, underground) {
  state.map = map;
  state.collab.lastMeta = metaSnapshotFromMap(map);
  state.mapRevision = Number.isInteger(map.revision) ? map.revision : 0;
  state.collab.liveRevision = Number.isInteger(map.liveRevision) ? map.liveRevision : state.mapRevision;
  state.collab.stale = false;
  state.collab.metaQueue = Promise.resolve();
  if (state.collab.liveRevision > state.mapRevision) {
    log("地图有未保存的协作修改；请连接协作或等待协作者保存");
  }
  state.map.anchors = Array.isArray(map.anchors) ? map.anchors : [];
  state.heights = heights;
  state.biomes = biomes;
  state.surfaces = surfaces;
  state.underground = underground instanceof Map ? underground : new Map();
  state.undergroundRevision = 0;
  state.undergroundY = clamp(state.undergroundY, map.min_y, map.min_y + map.world_height - 1);
  state.undergroundYMin = state.undergroundY;
  state.undergroundYMax = state.undergroundY;
  syncUndergroundInputs();
  state.undergroundMode = false;
  undergroundToggle.checked = false;
  state.undergroundClear = false;
  undergroundClearToggle.checked = false;
  state.undergroundChanges = null;
  if (terrainView) {
    terrainView.setMap(map, heights, surfaces, biomes);
    terrainView.markDirty();
  }
  computeHeightRange();
  state.selectedBiome = 0;
  state.selectedSurface = Math.min(1, Math.max(0, map.surface_palette.length - 1));
  state.undoStack = [];
  state.redoStack = [];
  state.strokeChanges = null;
  state.undergroundChanges = null;
  state.rect = null;
  state.rectDrawing = false;
  state.selection = null;
  state.strokeLabel = null;
  updateSelectionInfo();
  state.pickedHeight = null;
  state.cursor = null;
  state.drawing = false;
  state.panning = null;
  state.view.centerX = map.width / 2;
  state.view.centerZ = map.depth / 2;
  state.view.zoom = 1;
  state.view3D.yaw = Math.PI / 4;
  state.view3D.pitch = 0.55;
  state.view3D.zoom = Number(state.view3D.detail) === 0 ? 2 : 1;
  state.view3D.targetX = map.width / 2;
  state.view3D.targetZ = map.depth / 2;
  state.orbit = null;
  state.pan3D = null;
  state.placement = null;
  state.selectedAnchorId = null;
  state.selectedAnchorIds = new Set();
  state.movingAnchorId = null;
  state.movingAnchorIds = [];
  state.movingAnchorOrigins = null;
  state.structureSelecting = false;
  state.structureSelectRect = null;
  structureSelectBox.hidden = true;
  state.toolPage = "terrain";
  state.forestMode = false;
  forestModeBtn.classList.remove("active");
  forestModeBtn.textContent = "森林笔刷";
  viewportStage.classList.remove("structure-mode", "forest-mode");
  canvas.style.display = state.viewMode === "3d" ? "none" : "block";
  terrainCanvas.style.display = state.viewMode === "3d" ? "block" : "none";
  placeModeBtn.textContent = "进入放置模式";
  placeModeBtn.classList.remove("active");
  fillMeta();
  fillSpawn();
  buildPalettes();
  refreshAnchorList();
  loadTemplates();
  updateLayerVisibility();
  updateHotbar();
  setDirty(false);
  const pendingMeta = state.collab.pendingMeta;
  state.collab.pendingMeta = null;
  if (pendingMeta) {
    applyRemoteMeta({ ...pendingMeta, delta: false });
  }
  updateStatusBar(null);
  renderNow();
  void refreshRegenerationStatus();
  log(`已加载 ${map.id}，${map.width}×${map.depth}，${map.layers ? "二进制图层" : "内联 JSON"}`);
}

function currentValues(index) {
  return {
    h: state.heights[index],
    b: state.biomes[index],
    s: state.surfaces[index],
  };
}

function captureCell(index) {
  if (!state.strokeChanges || state.strokeChanges.has(index)) {
    return;
  }
  state.strokeChanges.set(index, currentValues(index));
}

function captureUnderground(x, z, y = state.undergroundY) {
  if (!state.undergroundChanges) {
    return;
  }
  const key = `${x},${y},${z}`;
  if (state.undergroundChanges.has(key)) {
    return;
  }
  state.undergroundChanges.set(key, state.underground.has(key) ? state.underground.get(key) : null);
}

function applyUndergroundChanges(changes) {
  for (const change of changes) {
    if (change.block === null) {
      state.underground.delete(change.key);
    } else {
      state.underground.set(change.key, change.block);
    }
    state.undergroundRevision += 1;
  }
}

function applyPatchChanges(changes) {
  for (const change of changes) {
    state.heights[change.index] = change.h;
    state.biomes[change.index] = change.b;
    state.surfaces[change.index] = change.s;
  }
}

function cloneJson(value) {
  return value == null ? value : JSON.parse(JSON.stringify(value));
}

function editorSnapshot() {
  return {
    anchors: cloneJson(state.map?.anchors ?? []),
    spawn: cloneJson(state.map?.spawn ?? null),
    surfacePalette: [...(state.map?.surface_palette ?? [])],
    seaLevel: state.map?.sea_level,
    dirtDepth: state.map?.dirt_depth ?? 3,
    biomeTransition: state.map?.biome_transition ?? 0,
  };
}

function applyEditorSnapshot(snapshot) {
  if (!state.map || !snapshot) {
    return;
  }
  state.map.anchors = cloneJson(snapshot.anchors ?? []);
  state.map.spawn = cloneJson(snapshot.spawn ?? null);
  state.map.surface_palette = [...(snapshot.surfacePalette ?? [])];
  if (snapshot.seaLevel !== undefined) {
    state.map.sea_level = snapshot.seaLevel;
  }
  if (snapshot.dirtDepth !== undefined) {
    state.map.dirt_depth = snapshot.dirtDepth;
  }
  if (snapshot.biomeTransition !== undefined) {
    state.map.biome_transition = clamp(Number(snapshot.biomeTransition) || 0, 0, 64);
  }
  if (!state.map.anchors.some((anchor) => anchor.id === state.selectedAnchorId)) {
    state.selectedAnchorId = null;
  }
  state.movingAnchorId = null;
  state.movingAnchorIds = [];
  state.movingAnchorOrigins = null;
  state.selectedSurface = clamp(state.selectedSurface, 0, state.map.surface_palette.length - 1);
  buildPalettes();
  refreshAnchorList();
  fillSpawn();
  fillMeta();
  updateBlockGridSelection();
  markTerrainDirty();
  scheduleRender();
}

function pushSnapshotHistory(before, label) {
  if (!before || !state.map) {
    return false;
  }
  const after = editorSnapshot();
  if (JSON.stringify(before) === JSON.stringify(after)) {
    return false;
  }
  state.undoStack.push({ snapshotBefore: before, snapshotAfter: after, label });
  if (state.undoStack.length > 60) {
    state.undoStack.shift();
  }
  state.redoStack = [];
  updateHistoryButtons();
  broadcastMapMeta();
  return true;
}

async function commitStroke() {
  const hasCells = state.strokeChanges && state.strokeChanges.size > 0;
  const hasUnderground = state.undergroundChanges && state.undergroundChanges.size > 0;
  if (!hasCells && !hasUnderground) {
    state.strokeChanges = null;
    state.undergroundChanges = null;
    state.strokeLabel = null;
    await collabReleaseStrokeLocks();
    return;
  }
  const undergroundCount = state.undergroundChanges?.size ?? 0;
  if (undergroundCount > 200000) {
    const rollbackCells = hasCells
      ? Array.from(state.strokeChanges, ([index, values]) => ({ index, ...values }))
      : [];
    const rollbackUnderground = hasUnderground
      ? Array.from(state.undergroundChanges, ([key, block]) => ({ key, block }))
      : [];
    applyPatchChanges(rollbackCells);
    applyUndergroundChanges(rollbackUnderground);
    state.strokeChanges = null;
    state.undergroundChanges = null;
    state.strokeLabel = null;
    await collabReleaseStrokeLocks();
    markTerrainDirty();
    scheduleRender();
    log(`本次地下修改过多（${undergroundCount} 项），已回滚；请缩小 Y 范围或笔刷半径`);
    return false;
  }
  const beforeChanges = hasCells
    ? Array.from(state.strokeChanges, ([index, values]) => ({ index, ...values }))
    : [];
  const beforeUnderground = hasUnderground
    ? Array.from(state.undergroundChanges, ([key, block]) => ({ key, block }))
    : [];
  const afterChanges = hasCells
    ? Array.from(state.strokeChanges.keys(), (index) => ({ index, ...currentValues(index) }))
    : [];
  const afterUnderground = hasUnderground
    ? Array.from(state.undergroundChanges.keys(), (key) => ({
      key,
      block: state.underground.has(key) ? state.underground.get(key) : null,
    }))
    : [];
  const patch = {
    changes: beforeChanges,
    undergroundChanges: beforeUnderground,
    label: state.strokeLabel || undefined,
  };
  state.strokeChanges = null;
  state.undergroundChanges = null;
  state.strokeLabel = null;
  if (state.collab.session) {
    try {
      if (state.collab.leaseLost) throw new Error("编辑锁已失效");
      await collabPost("terrain", { changes: afterChanges, undergroundChanges: afterUnderground });
    } catch (error) {
      applyPatchChanges(beforeChanges);
      applyUndergroundChanges(beforeUnderground);
      markTerrainDirty();
      scheduleRender();
      log(`整笔修改已回滚：${error.message}`);
      return false;
    } finally {
      await collabReleaseStrokeLocks();
    }
  }
  state.undoStack.push(patch);
  if (state.undoStack.length > 60) {
    state.undoStack.shift();
  }
  state.redoStack = [];
  state.strokeChanges = null;
  state.undergroundChanges = null;
  state.strokeLabel = null;
  updateHistoryButtons();
  return true;
}

async function undo() {
  if (!canEdit()) {
    log("只读角色不能撤销编辑");
    return;
  }
  if (state.collab.busy || state.drawing) return;
  const patch = state.undoStack.at(-1);
  if (!patch) {
    return;
  }
  if (state.collab.session && (patch.snapshotBefore || patch.paletteBefore)) {
    log("协作期间暂不撤销全局元数据；请直接编辑对应属性");
    return;
  }
  if (state.collab.session) {
    state.collab.busy = true;
    try {
      await acquireCollabCells(patchCells(patch));
      await collabPost("terrain", { changes: patch.changes ?? [], undergroundChanges: patch.undergroundChanges ?? [] });
    } catch (error) {
      log(`撤销未执行：${error.message}`);
      return;
    } finally {
      await collabReleaseStrokeLocks();
      state.collab.busy = false;
    }
  }
  state.undoStack.pop();
  if (patch.snapshotBefore) {
    applyEditorSnapshot(patch.snapshotBefore);
    state.redoStack.push(patch);
    setDirty(true);
    updateHistoryButtons();
    broadcastMapMeta();
    log(`撤销${patch.label ? `：${patch.label}` : ""}`);
    return;
  }
  if (patch.paletteBefore) {
    state.map.surface_palette = [...patch.paletteBefore];
    remapSurfaceIndices(patch.inverseSurfaceMapping);
    applySurfaceBits(patch.deletedSurfaceBits, patch.deletedSurfaceIndex);
    state.selectedSurface = clamp(patch.selectedSurfaceBefore ?? 0, 0, state.map.surface_palette.length - 1);
    state.redoStack.push(patch);
    buildPalettes();
    updateBlockGridSelection();
    markTerrainDirty();
    setDirty(true);
    scheduleRender();
    updateHistoryButtons();
    broadcastMapMeta();
    log(`撤销${patch.label ? `：${patch.label}` : ""}`);
    return;
  }
  const redoChanges = (patch.changes ?? []).map((change) => ({
    index: change.index,
    ...currentValues(change.index),
  }));
  const redoUnderground = (patch.undergroundChanges ?? []).map((change) => ({
    key: change.key,
    block: state.underground.has(change.key) ? state.underground.get(change.key) : null,
  }));
  applyPatchChanges(patch.changes ?? []);
  applyUndergroundChanges(patch.undergroundChanges ?? []);
  state.redoStack.push({ changes: redoChanges, undergroundChanges: redoUnderground });
  markTerrainDirty();
  setDirty(true);
  scheduleRender();
  updateHistoryButtons();
  log("撤销");
}

async function redo() {
  if (!canEdit()) {
    log("只读角色不能重做编辑");
    return;
  }
  if (state.collab.busy || state.drawing) return;
  const patch = state.redoStack.at(-1);
  if (!patch) {
    return;
  }
  if (state.collab.session && (patch.snapshotBefore || patch.paletteBefore)) {
    log("协作期间暂不重做全局元数据");
    return;
  }
  if (state.collab.session) {
    state.collab.busy = true;
    try {
      await acquireCollabCells(patchCells(patch));
      await collabPost("terrain", { changes: patch.changes ?? [], undergroundChanges: patch.undergroundChanges ?? [] });
    } catch (error) {
      log(`重做未执行：${error.message}`);
      return;
    } finally {
      await collabReleaseStrokeLocks();
      state.collab.busy = false;
    }
  }
  state.redoStack.pop();
  if (patch.snapshotBefore) {
    applyEditorSnapshot(patch.snapshotAfter);
    state.undoStack.push(patch);
    setDirty(true);
    updateHistoryButtons();
    broadcastMapMeta();
    log(`重做${patch.label ? `：${patch.label}` : ""}`);
    return;
  }
  if (patch.paletteBefore) {
    state.map.surface_palette = [...patch.paletteAfter];
    remapSurfaceIndices(patch.surfaceMapping);
    applySurfaceBits(patch.deletedSurfaceBits, patch.deletedFallbackIndex);
    state.selectedSurface = clamp(patch.selectedSurfaceAfter ?? 0, 0, state.map.surface_palette.length - 1);
    state.undoStack.push(patch);
    buildPalettes();
    updateBlockGridSelection();
    markTerrainDirty();
    setDirty(true);
    scheduleRender();
    updateHistoryButtons();
    broadcastMapMeta();
    log(`重做${patch.label ? `：${patch.label}` : ""}`);
    return;
  }
  const undoChanges = (patch.changes ?? []).map((change) => ({
    index: change.index,
    ...currentValues(change.index),
  }));
  const undoUnderground = (patch.undergroundChanges ?? []).map((change) => ({
    key: change.key,
    block: state.underground.has(change.key) ? state.underground.get(change.key) : null,
  }));
  applyPatchChanges(patch.changes ?? []);
  applyUndergroundChanges(patch.undergroundChanges ?? []);
  state.undoStack.push({ changes: undoChanges, undergroundChanges: undoUnderground });
  markTerrainDirty();
  setDirty(true);
  scheduleRender();
  updateHistoryButtons();
  log("重做");
}

function syncMetadata() {
  if (!state.map) {
    return false;
  }
  const spawn = readSpawnInputs();
  if (!spawn) {
    log("出生点必须是三个整数");
    return false;
  }
  const previousSpawn = Array.isArray(state.map.spawn?.pos) ? [...state.map.spawn.pos] : null;
  state.map.spawn = state.map.spawn ?? { yaw: 0, pitch: 0 };
  state.map.spawn.pos = spawn;
  if (JSON.stringify(previousSpawn) !== JSON.stringify(spawn)) {
    broadcastMapMeta();
  }
  if (!state.map.layers) {
    state.map.heights = Array.from(state.heights);
    state.map.biomes = Array.from(state.biomes);
    state.map.surfaces = Array.from(state.surfaces);
  }
  return true;
}

function encodeHeightmap() {
  if (LITTLE_ENDIAN && state.heights instanceof Int16Array) {
    return state.heights.buffer;
  }
  const buffer = new ArrayBuffer(state.heights.length * 2);
  const view = new DataView(buffer);
  for (let i = 0; i < state.heights.length; i++) {
    view.setInt16(i * 2, state.heights[i], true);
  }
  return buffer;
}

function encodeBytes(values) {
  if (values instanceof Uint8Array) {
    return values.buffer;
  }
  return new Uint8Array(values).buffer;
}

function encodeUint16(values) {
  if (LITTLE_ENDIAN && values instanceof Uint16Array) {
    return values.buffer;
  }
  const buffer = new ArrayBuffer(values.length * 2);
  const view = new DataView(buffer);
  for (let i = 0; i < values.length; i++) {
    view.setUint16(i * 2, values[i], true);
  }
  return buffer;
}

let saveInFlight = null;

async function saveMap() {
  if (state.layersDetailed === false) {
    log("高精度图层仍在后台加载，暂不能保存；请等待“加载细节”完成");
    return false;
  }
  if (!canEdit()) {
    log("只读角色不能保存地图");
    return false;
  }
  if (saveInFlight) {
    log("保存已在进行中");
    return saveInFlight;
  }
  if (state.collab.busy || state.drawing) {
    log("请等待当前笔画提交完成后保存");
    return false;
  }
  if (state.collab.stale) {
    log("服务器地图已更新，请先重新载入地图再保存");
    return false;
  }
  if (state.collab.session && !state.collab.connected) {
    log("协作连接已断开，请先重连再保存");
    return false;
  }
  if (!syncMetadata()) {
    return false;
  }
  saveInFlight = performSave();
  try {
    return await saveInFlight;
  } finally {
    saveInFlight = null;
  }
}

async function performSave() {
  let token = null;
  let committed = false;
  state.collab.busy = true;
  try {
    if (state.collab.session) {
      try {
        await state.collab.metaQueue;
      } catch (error) {
        log(`元数据同步失败，已取消保存：${error instanceof Error ? error.message : String(error)}`);
        return false;
      }
    }

    const beginResponse = await beginSave(state.collab.connected ? state.collab.clientId : null, state.mapRevision);
    const beginResult = await beginResponse.json();
    if (!beginResponse.ok || !beginResult.ok) {
      if (beginResult.stale) {
        if (state.localDirty) {
          setCollabStale("地图已被其他编辑者修改，本地还有未保存修改；请先重新载入地图");
        } else {
          log("地图已被其他编辑者修改，正在重新载入");
          await loadMap();
        }
      } else {
        log(`保存失败：${beginResult.error ?? beginResponse.statusText}`);
      }
      return false;
    }

    token = beginResult.token;
    const mapResponse = await uploadMap(state.map, token);
    const mapResult = await mapResponse.json();
    if (!mapResponse.ok || !mapResult.ok) {
      throw new Error((mapResult.errors ?? [mapResult.error ?? mapResponse.statusText]).join("; "));
    }

    if (state.map.layers) {
      await putLayer("heightmap", encodeHeightmap(), token);
      await putLayer("biomes", encodeBytes(state.biomes), token);
      await putLayer("surfaces", encodeUint16(state.surfaces), token);
    }
    await saveUnderground(token);

    const commitResponse = await commitSave(token);
    const commitResult = await commitResponse.json();
    if (!commitResponse.ok || !commitResult.ok) {
      if (commitResult.stale) {
        if (state.localDirty) {
          setCollabStale("保存期间地图被其他编辑者修改，本地修改仍保留；请先重新载入地图再保存");
        } else {
          log("保存期间地图被其他编辑者修改，正在重新载入");
          await loadMap();
        }
      } else {
        log(`保存失败：${(commitResult.errors ?? [commitResult.error ?? commitResponse.statusText]).join("; ")}`);
      }
      return false;
    }

    committed = true;
    const revisionBeforeCommit = state.mapRevision;
    const commitRevision = Number.isInteger(commitResult.revision)
      ? commitResult.revision
      : revisionBeforeCommit;
    state.mapRevision = Math.max(revisionBeforeCommit, commitRevision);
    state.collab.liveRevision = Math.max(state.collab.liveRevision, commitRevision);
    state.collab.stale = false;
    if (state.mapRevision > commitRevision) {
      setDirty(true, false);
    } else {
      setDirty(false);
    }
    log(`已保存 ${commitResult.path}（revision ${commitResult.revision}）`);
    refreshBackups();
    return true;
  } catch (error) {
    log(`保存失败：${error instanceof Error ? error.message : String(error)}`);
    return false;
  } finally {
    if (token && !committed) {
      await abortSave(token).catch(() => {});
    }
    state.collab.busy = false;
  }
}

async function saveUnderground(token = null) {
  const blocks = [];
  for (const [key, block] of state.underground) {
    const [x, y, z] = key.split(",").map(Number);
    if (![x, y, z].every(Number.isInteger) || typeof block !== "string") {
      continue;
    }
    blocks.push({ pos: [x, y, z], block });
  }
  const response = await uploadUnderground(blocks, token);
  const result = await response.json();
  if (!response.ok || !result.ok) {
    throw new Error((result.errors ?? [result.error ?? response.statusText]).join("; "));
  }
  log(`已保存地下覆盖 ${blocks.length} 块`);
}

async function syncRuntime() {
  if (!canEdit()) {
    log("只读角色不能同步运行时地图");
    return;
  }
  if (state.dirty) {
    const saved = await saveMap();
    if (!saved) {
      return;
    }
  }
  try {
    const response = await syncRuntimeMap();
    const result = await response.json();
    if (!response.ok || !result.ok) {
      log(`同步失败：${(result.errors ?? [result.error ?? response.statusText]).join("; ")}`);
      return;
    }
    log(`已同步到 ${result.path}`);
  } catch (error) {
    log(`同步失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

async function importCommonMap() {
  if (!canEdit()) {
    log("只读角色不能导入地图");
    return;
  }
  if (state.dirty && !window.confirm("当前地图有未保存修改，仍从 common 导入并覆盖 map_source 吗？")) {
    return;
  }
  importCommonBtn.disabled = true;
  try {
    const response = await fetch("/api/import-runtime", { method: "POST" });
    const result = await response.json();
    if (!response.ok || !result.ok) {
      log(`从 common 导入失败：${(result.errors ?? [result.error ?? response.statusText]).join("; ")}`);
      return;
    }
    log(`已从 ${result.from ?? result.path} 导入 common 地图`);
    await loadMap();
  } catch (error) {
    log(`从 common 导入失败：${error instanceof Error ? error.message : String(error)}`);
  } finally {
    importCommonBtn.disabled = false;
  }
}

function pickCellAt(cell) {
  if (!cell || !state.map || !state.heights) {
    return;
  }
  const index = cell.z * state.map.width + cell.x;
  if (state.layer === "height") {
    const height = state.heights[index];
    if (height === VOID) {
      log("该列是 void，无法吸取高度");
      return;
    }
    state.pickedHeight = height;
    state.heightTool = "set";
    updateLayerVisibility();
    log(`吸取高度 ${height}，当前高度工具切换为“设为”`);
  } else if (state.layer === "biome") {
    state.selectedBiome = state.biomes[index];
    updatePaletteSelection();
    log(`吸取群系 ${state.map.biome_palette[state.selectedBiome]}`);
  } else {
    state.selectedSurface = state.surfaces[index];
    updatePaletteSelection();
    log(`吸取地表 ${state.map.surface_palette[state.selectedSurface]}`);
  }
  updateHud(cell);
}

function applyBrush(centerX, centerZ, radius = state.brushSize) {
  radius = Math.max(0, Math.round(Number(radius) || 0));
  const width = state.map.width;
  const depth = state.map.depth;
  const radiusSquared = radius * radius + 0.25;

  // Fast path: biome/surface writes are idempotent, so skip the per-stroke
  // `visited` set and the height falloff math, and iterate each row only over
  // its true disc width. Keeps very large biome brushes interactive.
  if (!state.undergroundMode && (state.layer === "biome" || state.layer === "surface")) {
    const isBiome = state.layer === "biome";
    const target = isBiome ? state.selectedBiome : state.selectedSurface;
    const cells = isBiome ? state.biomes : state.surfaces;
    let changed = 0;
    let touchedMinX = Infinity;
    let touchedMinZ = Infinity;
    let touchedMaxX = -Infinity;
    let touchedMaxZ = -Infinity;
    const minZ = Math.max(0, centerZ - radius);
    const maxZ = Math.min(depth - 1, centerZ + radius);
    for (let z = minZ; z <= maxZ; z++) {
      const dz = z - centerZ;
      const lim = Math.floor(Math.sqrt(radiusSquared - dz * dz));
      const minX = Math.max(0, centerX - lim);
      const maxX = Math.min(width - 1, centerX + lim);
      if (maxX < minX) {
        continue;
      }
      const base = z * width;
      for (let x = minX; x <= maxX; x++) {
        const index = base + x;
        if (cells[index] === target) {
          continue;
        }
        captureCell(index);
        cells[index] = target;
        changed += 1;
      }
      if (minX < touchedMinX) touchedMinX = minX;
      if (maxX > touchedMaxX) touchedMaxX = maxX;
      if (z < touchedMinZ) touchedMinZ = z;
      if (z > touchedMaxZ) touchedMaxZ = z;
    }
    if (changed > 0) {
      markTerrainDirtyRegion(touchedMinX, touchedMinZ, touchedMaxX, touchedMaxZ);
      setDirty(true);
    }
    return;
  }
  const minY = state.map.min_y;
  const maxY = state.map.min_y + state.map.world_height - 1;
  const strength = clamp(Number(state.brushStrength) || 1, 1, 10);
  const layer = state.layer;
  const heightTool = state.heightTool;
  const undergroundMode = state.undergroundMode;
  const selectedBiome = state.selectedBiome;
  const selectedSurface = state.selectedSurface;
  const collabActive = Boolean(state.collab.session);
  const range = undergroundMode ? undergroundRange() : null;
  const undergroundBlock = undergroundMode
    ? (state.map.surface_palette[selectedSurface] ?? "minecraft:stone")
    : null;
  const centerIndex = centerZ * width + centerX;
  let dirtyMinX = Infinity;
  let dirtyMinZ = Infinity;
  let dirtyMaxX = -Infinity;
  let dirtyMaxZ = -Infinity;
  if (!undergroundMode && layer === "height" && heightTool === "flatten" && state.flattenTarget === null) {
    const centerHeight = state.heights[centerIndex];
    if (centerHeight !== VOID) {
      state.flattenTarget = centerHeight;
    }
  }

  for (let dz = -radius; dz <= radius; dz++) {
    const z = centerZ + dz;
    if (z < 0 || z >= depth) {
      continue;
    }
    for (let dx = -radius; dx <= radius; dx++) {
      const dist2 = dx * dx + dz * dz;
      if (dist2 > radiusSquared) {
        continue;
      }
      const x = centerX + dx;
      if (x < 0 || x >= width) {
        continue;
      }
      const index = z * width + x;
      if (state.visited.has(index)) {
        continue;
      }
      if (collabActive && collabCellIsLocked(x, z)) {
        state.visited.add(index);
        continue;
      }
      state.visited.add(index);

      if (undergroundMode) {
        for (let y = range.min; y <= range.max; y++) {
          const undergroundKey = `${x},${y},${z}`;
          const current = state.underground.get(undergroundKey);
          if (state.undergroundClear) {
            if (current === undefined) {
              continue;
            }
            captureUnderground(x, z, y);
            state.underground.delete(undergroundKey);
          } else {
            if (current === undergroundBlock) {
              continue;
            }
            captureUnderground(x, z, y);
            state.underground.set(undergroundKey, undergroundBlock);
          }
        }
        state.undergroundRevision += 1;
        continue;
      }

      dirtyMinX = Math.min(dirtyMinX, x);
      dirtyMinZ = Math.min(dirtyMinZ, z);
      dirtyMaxX = Math.max(dirtyMaxX, x);
      dirtyMaxZ = Math.max(dirtyMaxZ, z);

      if (layer === "biome") {
        if (state.biomes[index] === selectedBiome) {
          continue;
        }
        captureCell(index);
        state.biomes[index] = selectedBiome;
        continue;
      }
      if (layer !== "height") {
        if (state.surfaces[index] === selectedSurface) {
          continue;
        }
        captureCell(index);
        state.surfaces[index] = selectedSurface;
        continue;
      }

      let height = state.heights[index];
      if (height === VOID) {
        continue;
      }
      const distance = Math.sqrt(dist2);
      const falloff = radius > 0 ? clamp(1 - distance / (radius + 0.5), 0, 1) : 1;
      const edgeFactor = 0.35 + 0.65 * falloff;
      const heightDelta = Math.max(1, Math.round(strength * edgeFactor));
      const blend = (0.2 + 0.8 * (strength / 10)) * edgeFactor;
      captureCell(index);
      if (heightTool === "raise") {
        height += heightDelta;
      } else if (heightTool === "lower") {
        height -= heightDelta;
      } else if (heightTool === "smooth") {
        let sum = 0;
        let count = 0;
        for (let oz = -1; oz <= 1; oz++) {
          for (let ox = -1; ox <= 1; ox++) {
            const nx = x + ox;
            const nz = z + oz;
            if (nx < 0 || nx >= width || nz < 0 || nz >= depth) {
              continue;
            }
            const neighbor = state.heights[nz * width + nx];
            if (neighbor !== VOID) {
              sum += neighbor;
              count += 1;
            }
          }
        }
        if (count > 0) {
          const average = sum / count;
          height = Math.round(height + (average - height) * blend);
        }
      } else if (heightTool === "flatten") {
        if (state.flattenTarget === null) {
          state.flattenTarget = height;
        }
        height = Math.round(height + (state.flattenTarget - height) * blend);
      } else if (heightTool === "set") {
        height = state.pickedHeight ?? height;
      } else if (heightTool === "void") {
        state.heights[index] = VOID;
        continue;
      }
      const clampedHeight = clamp(height, minY, maxY);
      state.heights[index] = clampedHeight;
      noteHeight(clampedHeight);
    }
  }
  if (!undergroundMode && Number.isFinite(dirtyMinX)) {
    markTerrainDirtyRegion(dirtyMinX, dirtyMinZ, dirtyMaxX, dirtyMaxZ);
  }
  setDirty(true);
}

function applyStrokeLine(from, to) {
  const dx = to.x - from.x;
  const dz = to.z - from.z;
  const steps = Math.max(Math.abs(dx), Math.abs(dz));
  if (steps === 0) {
    applyBrush(to.x, to.z);
    return;
  }
  for (let step = 1; step <= steps; step++) {
    const t = step / steps;
    applyBrush(Math.round(from.x + dx * t), Math.round(from.z + dz * t));
  }
}

function selectionBounds(selection = state.selection) {
  if (!state.map || !selection?.start || !selection?.end) {
    return null;
  }
  const minX = clamp(Math.min(selection.start.x, selection.end.x), 0, state.map.width - 1);
  const maxX = clamp(Math.max(selection.start.x, selection.end.x), 0, state.map.width - 1);
  const minZ = clamp(Math.min(selection.start.z, selection.end.z), 0, state.map.depth - 1);
  const maxZ = clamp(Math.max(selection.start.z, selection.end.z), 0, state.map.depth - 1);
  return { minX, maxX, minZ, maxZ, width: maxX - minX + 1, depth: maxZ - minZ + 1 };
}

function updateSelectionInfo() {
  if (!selectionInfo) {
    return;
  }
  const bounds = selectionBounds();
  if (!bounds) {
    selectionInfo.textContent = "无";
    selectionInfo.title = "";
    return;
  }
  selectionInfo.textContent = `${bounds.width}×${bounds.depth}`;
  selectionInfo.title = `x=${bounds.minX}..${bounds.maxX}, z=${bounds.minZ}..${bounds.maxZ}`;
}

function extractSelection(selection = state.selection) {
  const bounds = selectionBounds(selection);
  if (!bounds) {
    return null;
  }
  const { minX, minZ, width, depth } = bounds;
  const heights = new Int16Array(width * depth);
  const biomes = new Uint8Array(width * depth);
  const surfaces = new Uint16Array(width * depth);
  for (let z = 0; z < depth; z++) {
    for (let x = 0; x < width; x++) {
      const source = (minZ + z) * state.map.width + minX + x;
      const target = z * width + x;
      heights[target] = state.heights[source];
      biomes[target] = state.biomes[source];
      surfaces[target] = state.surfaces[source];
    }
  }
  return { width, depth, heights, biomes, surfaces };
}

function updateTemplateList() {
  if (!terrainTemplateList) {
    return;
  }
  const selected = terrainTemplateList.value;
  terrainTemplateList.replaceChildren();
  if (state.terrainTemplates.length === 0) {
    const option = document.createElement("option");
    option.value = "";
    option.textContent = "（暂无地形模板）";
    terrainTemplateList.append(option);
    pasteTerrainTemplateBtn.disabled = true;
    deleteTerrainTemplateBtn.disabled = true;
    return;
  }
  state.terrainTemplates.forEach((template, index) => {
    const option = document.createElement("option");
    option.value = String(index);
    option.textContent = `${template.name} · ${template.width}×${template.depth}`;
    terrainTemplateList.append(option);
  });
  if (state.terrainTemplates[Number(selected)]) {
    terrainTemplateList.value = selected;
  }
  pasteTerrainTemplateBtn.disabled = false;
  deleteTerrainTemplateBtn.disabled = false;
}

function readTerrainTemplates() {
  try {
    const raw = localStorage.getItem("gensokyo-editor.terrain-templates-v1");
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed)
      ? parsed.filter((entry) => entry && entry.name && Number.isInteger(entry.width) && Number.isInteger(entry.depth))
      : [];
  } catch (error) {
    log(`读取地形模板失败：${error instanceof Error ? error.message : String(error)}`);
    return [];
  }
}

function writeTerrainTemplates() {
  try {
    localStorage.setItem("gensokyo-editor.terrain-templates-v1", JSON.stringify(state.terrainTemplates));
    return true;
  } catch (error) {
    log(`保存地形模板失败（可能超出浏览器存储限制）：${error instanceof Error ? error.message : String(error)}`);
    return false;
  }
}

function copySelection() {
  const clip = extractSelection();
  if (!clip) {
    log("请先用选区模式框选地形");
    return false;
  }
  state.clipboard = clip;
  log(`已复制地形选区 ${clip.width}×${clip.depth}`);
  return true;
}

async function pasteTerrainClip(clip, label = "粘贴地形") {
  if (!canEdit()) {
    log("只读角色不能粘贴地形");
    return false;
  }
  if (state.collab.busy || state.drawing) return false;
  if (!clip || !state.map || !state.cursor) {
    log("请先把鼠标移到地图上再粘贴");
    return false;
  }
  const originX = state.cursor.x;
  const originZ = state.cursor.z;
  const width = Math.min(clip.width, state.map.width - originX);
  const depth = Math.min(clip.depth, state.map.depth - originZ);
  if (width <= 0 || depth <= 0) {
    log("粘贴位置超出地图范围");
    return false;
  }
  state.collab.busy = true;
  try {
    await acquireCollabCells(rectangleCells(originX, originZ, width, depth));
  } catch (error) {
    state.collab.busy = false;
    log(`粘贴未执行：${error.message}`);
    return false;
  }
  state.visited.clear();
  state.strokeChanges = new Map();
  state.undergroundChanges = new Map();
  state.strokeLabel = label;
  let changed = 0;
  for (let z = 0; z < depth; z++) {
    for (let x = 0; x < width; x++) {
      const source = z * clip.width + x;
      const index = (originZ + z) * state.map.width + originX + x;
      captureCell(index);
      state.heights[index] = clip.heights[source];
      state.biomes[index] = Math.min(clip.biomes[source], Math.max(0, state.map.biome_palette.length - 1));
      state.surfaces[index] = Math.min(clip.surfaces[source], Math.max(0, state.map.surface_palette.length - 1));
      changed++;
    }
  }
  const accepted = await commitStroke();
  state.collab.busy = false;
  if (accepted === false) return false;
  state.strokeLabel = null;
  state.selection = {
    start: { x: originX, z: originZ },
    end: { x: originX + width - 1, z: originZ + depth - 1 },
  };
  state.rect = null;
  state.rectDrawing = false;
  updateSelectionInfo();
  setDirty(true);
  markTerrainDirty();
  scheduleRender();
  log(`已粘贴地形 ${width}×${depth}，${changed} 格`);
  return true;
}

function pasteSelection() {
  if (!state.clipboard) {
    log("剪贴板中没有地形");
    return;
  }
  pasteTerrainClip(state.clipboard, "粘贴地形");
}

function saveTerrainTemplate() {
  const clip = extractSelection();
  if (!clip) {
    log("请先框选要保存的地形");
    return;
  }
  if (clip.width * clip.depth > 65536) {
    log("地形模板最多保存 65536 格，请缩小选区");
    return;
  }
  const name = terrainTemplateName.value.trim() || `地形 ${state.terrainTemplates.length + 1}`;
  const serialized = {
    name,
    width: clip.width,
    depth: clip.depth,
    heights: Array.from(clip.heights),
    biomes: Array.from(clip.biomes),
    surfaces: Array.from(clip.surfaces),
  };
  const existing = state.terrainTemplates.findIndex((template) => template.name === name);
  if (existing >= 0) {
    state.terrainTemplates[existing] = serialized;
  } else {
    state.terrainTemplates.push(serialized);
  }
  if (!writeTerrainTemplates()) {
    return;
  }
  updateTemplateList();
  log(`已保存地形模板“${name}”（${clip.width}×${clip.depth}）`);
}

function pasteTerrainTemplate() {
  const index = Number(terrainTemplateList.value);
  const template = state.terrainTemplates[index];
  if (!template) {
    log("请选择一个地形模板");
    return;
  }
  pasteTerrainClip(template, `粘贴模板 ${template.name}`);
}

function deleteTerrainTemplate() {
  const index = Number(terrainTemplateList.value);
  if (!state.terrainTemplates[index]) {
    return;
  }
  const [removed] = state.terrainTemplates.splice(index, 1);
  if (writeTerrainTemplates()) {
    updateTemplateList();
    log(`已删除地形模板“${removed.name}”`);
  }
}

function clearTerrainSelection() {
  if (!state.selection) {
    return;
  }
  state.selection = null;
  updateSelectionInfo();
  scheduleRender();
  log("已清除地形选区");
}

async function applyRectangle(start, end) {
  if (state.collab.busy || state.drawing) return;
  if (!start || !end) {
    return;
  }
  const minX = Math.max(0, Math.min(start.x, end.x));
  const maxX = Math.min(state.map.width - 1, Math.max(start.x, end.x));
  const minZ = Math.max(0, Math.min(start.z, end.z));
  const maxZ = Math.min(state.map.depth - 1, Math.max(start.z, end.z));
  if (minX > maxX || minZ > maxZ) {
    return;
  }
  const area = (maxX - minX + 1) * (maxZ - minZ + 1);
  if (area > 1000000) {
    log(`矩形区域 ${area} 格过大，已取消（上限 1,000,000 格）`);
    state.rect = null;
    state.rectDrawing = false;
    scheduleRender();
    return;
  }
  if (area > 250000 && !window.confirm(`矩形区域有 ${area} 个格子，确定继续吗？`)) {
    state.rect = null;
    state.rectDrawing = false;
    scheduleRender();
    return;
  }
  if (state.undergroundMode) {
    const range = undergroundRange();
    const volume = area * (range.max - range.min + 1);
    if (volume > 200000) {
      log(`地下体积 ${volume} 格过大，已取消（上限 200,000 格）；请缩小矩形或 Y 范围`);
      state.rect = null;
      state.rectDrawing = false;
      scheduleRender();
      return;
    }
  }

  state.collab.busy = true;
  try {
    await acquireCollabCells(rectangleCells(minX, minZ, maxX - minX + 1, maxZ - minZ + 1));
  } catch (error) {
    state.collab.busy = false;
    log(`矩形未执行：${error.message}`);
    return;
  }
  state.visited.clear();
  state.flattenTarget = null;
  state.strokeChanges = new Map();
  state.undergroundChanges = new Map();
  const oldBrushSize = state.brushSize;
  state.brushSize = 0;
  for (let z = minZ; z <= maxZ; z++) {
    for (let x = minX; x <= maxX; x++) {
      applyBrush(x, z);
    }
  }
  state.brushSize = oldBrushSize;
  const accepted = await commitStroke();
  state.collab.busy = false;
  if (accepted === false) return;
  state.rect = null;
  state.rectDrawing = false;
  setDirty(true);
  scheduleRender();
  log(`已应用矩形 ${minX},${minZ} → ${maxX},${maxZ}（${area} 格）`);
}

function updateHud(cell) {
  state.cursor = cell;
  if (cell && state.collab.connected) {
    queueCollabCursor(cell);
  }
  if (!cell || !state.map || !state.heights) {
    cursorInfo.textContent = state.movingAnchorId
      ? "移动结构：点击目标格确认，Esc 取消"
      : state.placement
        ? `放置模式：${state.placement.template}；点击地图放置，Esc 退出`
        : state.forestMode
          ? "森林笔刷：点击地图种树；Esc 或再点按钮退出"
          : state.toolPage === "objects"
            ? "结构选择：拖动框选结构，Ctrl/Shift 追加选择；地形笔刷已暂停"
            : state.undergroundMode
              ? `地下切片：Y=${state.undergroundY}；左键编辑，Esc 关闭后返回地表`
              : state.viewMode === "3d"
                ? "3D 编辑：左键绘制，右键拖动旋转，中键拖动平移，滚轮缩放"
                : "移动鼠标查看地图信息；滚轮缩放，中键/右键平移";
    updateStatusBar(null);
    return;
  }
  if (state.forestMode) {
    const worldX = state.map.origin[0] + cell.x;
    const worldZ = state.map.origin[1] + cell.z;
    cursorInfo.textContent = `森林笔刷：${worldX}, ${state.heights[cell.z * state.map.width + cell.x]}, ${worldZ}；点击种树，Esc 退出`;
    updateStatusBar(cell);
    return;
  }
  if (state.toolPage === "objects" && !state.placement && !state.movingAnchorId) {
    const selected = state.selectedAnchorIds.size;
    cursorInfo.textContent = selected > 0
      ? `已选 ${selected} 个结构；拖动框选，Ctrl/Shift 追加，Esc 清空`
      : "结构选择：拖动框选结构；地形笔刷已暂停";
    updateStatusBar(cell);
    return;
  }
  if (state.movingAnchorId) {
    const worldX = state.map.origin[0] + cell.x;
    const worldZ = state.map.origin[1] + cell.z;
    cursorInfo.textContent = `移动结构：目标 ${worldX}, ${state.heights[cell.z * state.map.width + cell.x]}, ${worldZ}；点击确认，Esc 取消`;
    updateStatusBar(cell);
    return;
  }
  if (state.undergroundMode) {
    const worldX = state.map.origin[0] + cell.x;
    const worldZ = state.map.origin[1] + cell.z;
    const range = undergroundRange();
    const block = state.underground.get(`${cell.x},${state.undergroundY},${cell.z}`) ?? "(自动生成)";
    cursorInfo.textContent = `地下 Y=${range.min}..${range.max}（切片 ${state.undergroundY}） x=${worldX} z=${worldZ} block=${block}`;
    updateStatusBar(cell);
    return;
  }
  const index = cell.z * state.map.width + cell.x;
  const height = state.heights[index];
  const worldX = state.map.origin[0] + cell.x;
  const worldZ = state.map.origin[1] + cell.z;
  const biome = state.map.biome_palette[state.biomes[index]] ?? "?";
  const surface = state.map.surface_palette[state.surfaces[index]] ?? "?";
  const zoom = state.viewMode === "3d" ? state.view3D.zoom : state.view.zoom;
  cursorInfo.textContent =
    `x=${worldX} z=${worldZ}  h=${height === VOID ? "void" : height}  ` +
    `biome=${biome.replace(/^.*:/, "")}  surface=${surface.replace(/^.*:/, "")}  ` +
    `zoom=${zoom.toFixed(2)}×`;
  updateStatusBar(cell);
}

function anchorScreenPoint(anchor) {
  if (!state.map || !anchor || !Array.isArray(anchor.pos)) {
    return null;
  }
  const x = anchor.pos[0] - state.map.origin[0];
  const z = anchor.pos[2] - state.map.origin[1];
  if (x < 0 || z < 0 || x >= state.map.width || z >= state.map.depth) {
    return null;
  }
  if (state.viewMode === "3d") {
    return terrainView?.projectAnchor(anchor) ?? null;
  }
  const point = mapToScreen(x + 0.5, z + 0.5);
  const rect = canvas.getBoundingClientRect();
  return {
    x: rect.left + (point.x / canvas.width) * rect.width,
    y: rect.top + (point.y / canvas.height) * rect.height,
    visible: true,
  };
}

function startStructureSelection(event) {
  if (!canEdit()) {
    log("只读角色不能框选/编辑结构");
    return;
  }
  if (!state.map) {
    return;
  }
  const additive = event.ctrlKey || event.shiftKey;
  state.structureSelecting = true;
  state.structureSelectRect = {
    startX: event.clientX,
    startY: event.clientY,
    currentX: event.clientX,
    currentY: event.clientY,
    additive,
  };
  if (!additive) {
    setAnchorSelection([], null);
  }
  const stageRect = viewportStage.getBoundingClientRect();
  structureSelectBox.hidden = false;
  structureSelectBox.style.left = `${event.clientX - stageRect.left}px`;
  structureSelectBox.style.top = `${event.clientY - stageRect.top}px`;
  structureSelectBox.style.width = "0px";
  structureSelectBox.style.height = "0px";
  state.cursor = null;
  updateHud(null);
  try {
    viewportStage.setPointerCapture(event.pointerId);
  } catch {
    // Synthetic events may not support capture.
  }
  scheduleRender();
}

function updateStructureSelection(event) {
  const box = state.structureSelectRect;
  if (!box) {
    return;
  }
  box.currentX = event.clientX;
  box.currentY = event.clientY;
  const left = Math.min(box.startX, box.currentX);
  const top = Math.min(box.startY, box.currentY);
  const stageRect = viewportStage.getBoundingClientRect();
  structureSelectBox.style.left = `${left - stageRect.left}px`;
  structureSelectBox.style.top = `${top - stageRect.top}px`;
  structureSelectBox.style.width = `${Math.abs(box.currentX - box.startX)}px`;
  structureSelectBox.style.height = `${Math.abs(box.currentY - box.startY)}px`;
}

function finishStructureSelection(event) {
  const box = state.structureSelectRect;
  if (!box) {
    return;
  }
  state.structureSelecting = false;
  state.structureSelectRect = null;
  structureSelectBox.hidden = true;
  try {
    if (event?.pointerId !== undefined && viewportStage.hasPointerCapture(event.pointerId)) {
      viewportStage.releasePointerCapture(event.pointerId);
    }
  } catch {
    // Ignore capture errors.
  }
  const anchors = state.map?.anchors ?? [];
  const candidates = [];
  const width = Math.abs(box.currentX - box.startX);
  const height = Math.abs(box.currentY - box.startY);
  if (width < 5 && height < 5) {
    let nearest = null;
    let nearestDistance = 28;
    for (const anchor of anchors) {
      const point = anchorScreenPoint(anchor);
      if (!point || point.visible === false) {
        continue;
      }
      const distance = Math.hypot(point.x - box.currentX, point.y - box.currentY);
      if (distance < nearestDistance) {
        nearest = anchor;
        nearestDistance = distance;
      }
    }
    if (nearest) {
      candidates.push(nearest);
    }
  } else {
    const left = Math.min(box.startX, box.currentX);
    const right = Math.max(box.startX, box.currentX);
    const top = Math.min(box.startY, box.currentY);
    const bottom = Math.max(box.startY, box.currentY);
    for (const anchor of anchors) {
      const point = anchorScreenPoint(anchor);
      if (point && point.visible !== false && point.x >= left && point.x <= right && point.y >= top && point.y <= bottom) {
        candidates.push(anchor);
      }
    }
  }
  const base = box.additive ? [...state.selectedAnchorIds] : [];
  const next = new Set(base);
  for (const anchor of candidates) {
    next.add(anchor.id);
  }
  const primary = candidates.at(-1)?.id ?? [...next].at(-1) ?? null;
  setAnchorSelection(next, primary);
  log(candidates.length > 0
    ? `已选择 ${state.selectedAnchorIds.size} 个结构`
    : "已清空结构选择");
}

function runEditorCommand(command) {
  switch (command) {
    case "save":
      void saveMap();
      break;
    case "undo":
      undo();
      break;
    case "redo":
      redo();
      break;
    case "import-common":
      importCommonBtn.click();
      break;
    case "sync":
      syncBtn.click();
      break;
    case "view-2d":
      setViewMode("2d");
      break;
    case "view-3d":
      setViewMode("3d");
      break;
    case "fit":
      fitView();
      break;
    case "toggle-grid":
      gridToggle.checked = !gridToggle.checked;
      gridToggle.dispatchEvent(new Event("change", { bubbles: true }));
      break;
    case "toggle-water":
      waterToggle.checked = !waterToggle.checked;
      waterToggle.dispatchEvent(new Event("change", { bubbles: true }));
      break;
    case "focus-spawn":
      setToolPage("map");
      requestAnimationFrame(() => {
        spawnX.closest("section")?.scrollIntoView({ block: "center", behavior: "smooth" });
        spawnX.focus();
      });
      break;
    case "focus-meta":
      setToolPage("map");
      requestAnimationFrame(() => {
        metaEl.closest("section")?.scrollIntoView({ block: "center", behavior: "smooth" });
      });
      break;
    case "module-terrain":
      setToolPage("terrain");
      break;
    case "module-blocks":
      setToolPage("blocks");
      break;
    case "module-objects":
      setToolPage("objects");
      break;
    case "module-view":
      setToolPage("light");
      break;
    case "module-map":
      setToolPage("map");
      break;
    case "layer-height":
      setToolPage("terrain");
      setLayer("height");
      break;
    case "layer-biome":
      setToolPage("terrain");
      setLayer("biome");
      break;
    case "layer-surface":
      setToolPage("terrain");
      setLayer("surface");
      break;
    case "light-reset":
      lightResetBtn.click();
      break;
    case "shortcuts":
      setToolPage("light");
      log("快捷键：T/D/V 模块；1/2/3 图层；B/R/I 笔刷/矩形/吸管；F 适应；G 网格；W 水面；Ctrl+Z/Y/S");
      break;
    case "about":
      log("Gensokyo World Editor P4 · 固定地形地图 + Mojang 方块模型预览");
      break;
    default:
      break;
  }
}

const AUTH_ROLE_LABELS = {
  editor: "编辑者",
  viewer: "只读",
  admin: "管理员",
};

function roleLabel(role) {
  return AUTH_ROLE_LABELS[role] || String(role || "未知");
}

function refreshLoginRoles() {
  if (!loginRole) {
    return;
  }
  const roles = Array.isArray(state.auth?.roles) && state.auth.roles.length
    ? state.auth.roles
    : ["editor"];
  const previous = loginRole.value;
  loginRole.replaceChildren();
  for (const role of roles) {
    const option = document.createElement("option");
    option.value = role;
    option.textContent = roleLabel(role);
    loginRole.append(option);
  }
  loginRole.value = roles.includes(previous) ? previous : (roles.includes("editor") ? "editor" : roles[0]);
}

function applyAuthUi() {
  const required = Boolean(state.auth?.required);
  const viewer = state.auth?.role === "viewer";
  if (authBadge) {
    authBadge.hidden = !required;
    authBadge.textContent = required
      ? `${state.auth.room || "gensokyo"} · ${roleLabel(state.auth.role)}`
      : "";
  }
  if (logoutBtn) {
    logoutBtn.hidden = !required;
  }
  document.body.classList.toggle("viewer-mode", viewer);
  const readonlyButtons = [
    "saveBtn",
    "syncBtn",
    "importCommonBtn",
    "newMapBtn",
    "resizeMapBtn",
    "backupNowBtn",
    "restoreBackupBtn",
    "regenerateDimensionBtn",
    "cancelRegenerateDimensionBtn",
    "applyTerrainGlobalsBtn",
    "anchorApplyTransformBtn",
    "anchorApplyPosBtn",
    "anchorMoveBtn",
    "anchorDeleteSelectedBtn",
    "anchorDeleteBtn",
    "undoBtn",
    "redoBtn",
  ];
  for (const id of readonlyButtons) {
    const element = document.getElementById(id);
    if (element) {
      element.disabled = viewer;
    }
  }
  const readonlyInputs = [
    "biomeTransition",
    "seaLevelInput",
    "dirtDepthInput",
    "anchorX",
    "anchorY",
    "anchorZ",
    "anchorRotation",
    "anchorMirror",
    "anchorIntegrity",
    "anchorScale",
    "spawnX",
    "spawnY",
    "spawnZ",
  ];
  for (const id of readonlyInputs) {
    const element = document.getElementById(id);
    if (element) {
      element.disabled = viewer;
    }
  }
  if (loginRoom) {
    loginRoom.textContent = required ? `房间：${state.auth.room || "gensokyo"}` : "";
  }
  refreshLoginRoles();
  if (loginError && !required) {
    loginError.textContent = "";
  }
}

function showLogin(message = "") {
  if (!loginOverlay) {
    return;
  }
  loginOverlay.hidden = false;
  if (loginError) {
    loginError.textContent = message;
  }
  if (loginSubmit) {
    loginSubmit.disabled = false;
  }
  if (loginRoom) {
    loginRoom.textContent = `房间：${state.auth?.room || "gensokyo"}`;
  }
  refreshLoginRoles();
  window.setTimeout(() => loginPassword?.focus(), 50);
}

function hideLogin() {
  if (loginOverlay) {
    loginOverlay.hidden = true;
  }
}

async function fetchAuthStatus() {
  const response = await fetch("/api/auth/status", { cache: "no-store" });
  if (response.status === 404 || response.status === 405) {
    // Rolling update: new static files against an older server.
    return {
      required: false,
      authenticated: true,
      role: "editor",
      room: null,
      roles: ["editor"],
    };
  }
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }
  return await response.json();
}

async function submitLogin(event) {
  event.preventDefault();
  if (!loginSubmit) {
    return;
  }
  loginSubmit.disabled = true;
  if (loginError) {
    loginError.textContent = "";
  }
  try {
    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        password: loginPassword?.value ?? "",
        role: loginRole?.value ?? "editor",
      }),
    });
    const result = await response.json().catch(() => ({}));
    if (!response.ok || !result.ok) {
      throw new Error(result.error || "登录失败");
    }
    state.auth = {
      required: Boolean(result.required),
      authenticated: true,
      role: result.role || "editor",
      room: result.room || state.auth.room,
      roles: Array.isArray(result.roles) ? result.roles : state.auth.roles,
    };
    applyAuthUi();
    hideLogin();
    await initApp();
  } catch (error) {
    if (loginError) {
      loginError.textContent = error instanceof Error ? error.message : String(error);
    }
    if (loginSubmit) {
      loginSubmit.disabled = false;
    }
    loginPassword?.focus();
  }
}

async function logoutRoom() {
  if (state.dirty && !window.confirm("当前有未保存修改，仍然退出房间吗？")) {
    return;
  }
  await fetch("/api/auth/logout", { method: "POST" }).catch(() => {});
  window.location.reload();
}

function bindAuthEvents() {
  loginForm?.addEventListener("submit", submitLogin);
  logoutBtn?.addEventListener("click", logoutRoom);
}

let appInitialized = false;

async function initApp() {
  if (appInitialized) {
    return;
  }
  appInitialized = true;
  hideLogin();
  bindEvents();
  updateTemplateList();
  updateSelectionInfo();
  updateStatusBar(null);
  updateStatusClock();
  setInterval(updateStatusClock, 30000);
  resizeViewport();
  if (typeof ResizeObserver !== "undefined") {
    new ResizeObserver(resizeViewport).observe(viewportStage);
  }
  window.addEventListener("resize", resizeViewport);
  loadBiomes();
  loadBlocks();
  loadMap();
  refreshBackups();
  setInterval(() => {
    if (canEdit() && state.autoSave && state.dirty && state.map && !state.drawing && !state.rectDrawing && !state.collab.stale) {
      log("自动保存中...");
      saveMap();
    }
  }, 60000);
}

async function bootstrap() {
  bindAuthEvents();
  try {
    const authStatus = await fetchAuthStatus();
    state.auth = {
      required: Boolean(authStatus.required),
      authenticated: Boolean(authStatus.authenticated),
      role: authStatus.role || (authStatus.required ? null : "editor"),
      room: authStatus.room || null,
      roles: Array.isArray(authStatus.roles) && authStatus.roles.length ? authStatus.roles : ["editor"],
    };
    applyAuthUi();
    if (state.auth.required && !state.auth.authenticated) {
      showLogin();
      return;
    }
    await initApp();
  } catch (error) {
    showLogin(`无法连接服务器：${error instanceof Error ? error.message : String(error)}`);
  }
}

function bindEvents() {
  syncLightingControls();
  document.querySelectorAll("[data-command]").forEach((button) => {
    button.addEventListener("click", (event) => {
      event.stopPropagation();
      const command = button.dataset.command;
      runEditorCommand(command);
      button.closest("details")?.removeAttribute("open");
    });
  });
  document.addEventListener("click", (event) => {
    for (const menu of document.querySelectorAll("details.menu")) {
      if (!menu.contains(event.target)) {
        menu.removeAttribute("open");
      }
    }
  });
  lightAzimuth.addEventListener("input", () => {
    state.light.azimuth = Number(lightAzimuth.value);
    applyLightingSettings();
  });
  lightElevation.addEventListener("input", () => {
    state.light.elevation = Number(lightElevation.value);
    applyLightingSettings();
  });
  lightIntensity.addEventListener("input", () => {
    state.light.intensity = Number(lightIntensity.value);
    applyLightingSettings();
  });
  shadowToggle.addEventListener("change", () => {
    state.light.shadows = shadowToggle.checked;
    applyLightingSettings();
  });
  lightResetBtn.addEventListener("click", () => {
    state.light = { ...DEFAULT_LIGHT };
    syncLightingControls();
    applyLightingSettings();
  });

  toolTabs.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-page]");
    if (button) {
      setToolPage(button.dataset.page);
    }
  });

  undoBtn.addEventListener("click", undo);
  redoBtn.addEventListener("click", redo);

  layerButtons.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-layer]");
    if (button) {
      setLayer(button.dataset.layer);
    }
  });

  heightToolButtons.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-tool]");
    if (button) {
      setHeightTool(button.dataset.tool);
    }
  });

  editModeButtons.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-mode]");
    if (button) {
      setEditMode(button.dataset.mode);
    }
  });

  copySelectionBtn.addEventListener("click", copySelection);
  pasteSelectionBtn.addEventListener("click", pasteSelection);
  clearSelectionBtn.addEventListener("click", clearTerrainSelection);
  saveTerrainTemplateBtn.addEventListener("click", saveTerrainTemplate);
  pasteTerrainTemplateBtn.addEventListener("click", pasteTerrainTemplate);
  deleteTerrainTemplateBtn.addEventListener("click", deleteTerrainTemplate);
  terrainTemplateList.addEventListener("change", () => {
    const index = Number(terrainTemplateList.value);
    const template = state.terrainTemplates[index];
    terrainTemplateName.value = template?.name ?? "";
  });

  viewModeButtons.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-view]");
    if (button) {
      setViewMode(button.dataset.view);
    }
  });

  fitViewBtn.addEventListener("click", fitView);

  const applyBrushSizeInput = (value) => {
    const next = clamp(Math.round(Number(value) || 0), 0, MAX_BRUSH_RADIUS);
    state.brushSize = next;
    brushSizeInput.value = String(next);
    if (brushSizeNumberInput) {
      brushSizeNumberInput.value = String(next);
    }
    brushSizeValue.textContent = String(next);
    updateStatusBar(state.cursor);
    scheduleRender();
  };
  brushSizeInput.addEventListener("input", () => {
    applyBrushSizeInput(brushSizeInput.value);
  });
  if (brushSizeNumberInput) {
    brushSizeNumberInput.addEventListener("change", () => {
      applyBrushSizeInput(brushSizeNumberInput.value);
    });
  }

  brushStrengthInput.addEventListener("input", () => {
    state.brushStrength = clamp(Number(brushStrengthInput.value), 1, 10);
    brushStrengthValue.textContent = String(state.brushStrength);
    updateStatusBar(state.cursor);
    scheduleRender();
  });

  actualTerrainToggle.addEventListener("change", () => {
    state.actualMode = actualTerrainToggle.checked;
    terrainView?.markDirty();
    scheduleRender();
    log(state.actualMode ? "3D 实际方块模式：已关闭坡度平滑" : "3D 实际方块模式：已恢复平滑地形");
  });

  terrain3dExaggeration?.addEventListener("change", () => {
    const value = terrain3dExaggeration.value;
    state.view3D.exaggeration = value === "auto" ? 0 : Number(value);
    terrainView?.markDirty();
    updateTerrain3dControls();
    scheduleRender();
    const effective = terrainView?.effectiveExaggeration?.(state) ?? 1;
    log(`3D 垂直夸张：${effective}×（仅预览，不改地图高度）`);
  });

  terrain3dDetail?.addEventListener("change", () => {
    const value = Number(terrain3dDetail.value);
    state.view3D.detail = Number.isFinite(value) ? value : 384;
    if (state.view3D.detail === 0) {
      state.view3D.zoom = Math.max(2, state.view3D.zoom);
    }
    terrainView?.markDirty();
    updateTerrain3dControls();
    scheduleRender();
    log(state.view3D.detail === 0
      ? "3D 1:1 方块：仅渲染视图中心 512×512；整图请切回标准/高/超高"
      : `3D 网格精度：${state.view3D.detail} 格（仅预览，重建 3D 网格）`);
  });

  document.querySelector('#terrain3dBorder')?.addEventListener('change', (event) => {
    state.view3D.border = event.target.checked;
    scheduleRender();
  });

  terrain3dXzScale?.addEventListener("change", () => {
    state.view3D.xzScale = clamp(Number(terrain3dXzScale.value) || 1, 0.05, 16);
    updateTerrain3dControls();
    scheduleRender();
    log(`3D 横向缩放（XZ）：${state.view3D.xzScale}×（仅预览，不改地图坐标/数据）`);
  });

  biomeSearch.addEventListener("input", buildPalettes);
  biomeCandidateSearch?.addEventListener("input", buildBiomeCandidates);
  blockSearch.addEventListener("input", populateBlockList);
  blockList.addEventListener("change", () => {
    previewBlock(blockList.value);
    updateBlockGridSelection();
  });
  blockUseBtn.addEventListener("click", () => {
    const blockId = blockList.value || blockList.options[0]?.value;
    if (!blockId) {
      log("请先在方块物品栏中选择一个方块");
      return;
    }
    ensureSurfaceBlock(blockId);
    previewBlock(state.map?.surface_palette?.[state.selectedSurface] ?? blockId);
  });

  hotbar.addEventListener("click", (event) => {
    const slot = event.target.closest("[data-hotbar]");
    if (!slot) {
      return;
    }
    const action = slot.dataset.hotbar;
    if (action === "brush" || action === "rect" || action === "pick") {
      setToolPage("terrain");
      setEditMode(action);
    } else if (["raise", "lower", "smooth", "flatten", "set", "void"].includes(action)) {
      setToolPage("terrain");
      setLayer("height");
      setHeightTool(action);
    } else if (action === "block") {
      setToolPage("blocks");
    } else if (action === "underground") {
      undergroundToggle.checked = !state.undergroundMode;
      undergroundToggle.dispatchEvent(new Event("change", { bubbles: true }));
    }
    updateHotbar();
  });

  gridToggle.addEventListener("change", () => {
    state.grid = gridToggle.checked;
    scheduleRender();
  });

  waterToggle.addEventListener("change", () => {
    state.water = waterToggle.checked;
    markTerrainDirty();
    scheduleRender();
  });

  undergroundToggle.addEventListener("change", () => {
    state.undergroundMode = undergroundToggle.checked;
    if (state.undergroundMode) {
      syncUndergroundInputs();
    }
    updateHud(state.cursor);
    updateHotbar();
    scheduleRender();
    log(state.undergroundMode
      ? `地下编辑：Y=${undergroundRange().min}..${undergroundRange().max}，切片 ${state.undergroundY}（${state.viewMode === "3d" ? "3D" : "2D"}）`
      : "关闭地下编辑");
  });

  undergroundYInput.addEventListener("input", () => {
    if (!state.map) {
      return;
    }
    const value = Number(undergroundYInput.value);
    if (!Number.isInteger(value)) {
      return;
    }
    state.undergroundY = clamp(value, state.map.min_y, state.map.min_y + state.map.world_height - 1);
    if (value !== state.undergroundY) {
      undergroundYInput.value = String(state.undergroundY);
    }
    if (state.undergroundMode) {
      updateHud(state.cursor);
    }
    scheduleRender();
  });

  undergroundYMinInput.addEventListener("change", () => {
    if (!state.map) {
      return;
    }
    state.undergroundYMin = clampUndergroundY(Number(undergroundYMinInput.value), state.undergroundYMin);
    if (state.undergroundYMin > state.undergroundYMax) {
      state.undergroundYMax = state.undergroundYMin;
    }
    syncUndergroundInputs();
    updateHud(state.cursor);
    scheduleRender();
  });

  undergroundYMaxInput.addEventListener("change", () => {
    if (!state.map) {
      return;
    }
    state.undergroundYMax = clampUndergroundY(Number(undergroundYMaxInput.value), state.undergroundYMax);
    if (state.undergroundYMax < state.undergroundYMin) {
      state.undergroundYMin = state.undergroundYMax;
    }
    syncUndergroundInputs();
    updateHud(state.cursor);
    scheduleRender();
  });

  undergroundRangeFromSliceBtn.addEventListener("click", () => {
    if (!state.map) {
      return;
    }
    state.undergroundYMin = state.undergroundY;
    state.undergroundYMax = state.undergroundY;
    syncUndergroundInputs();
    updateHud(state.cursor);
    scheduleRender();
    log(`地下笔刷范围设为 Y=${state.undergroundY}`);
  });

  undergroundSliceFromRangeBtn.addEventListener("click", () => {
    if (!state.map) {
      return;
    }
    state.undergroundY = clampUndergroundY(state.undergroundYMax, state.undergroundYMax);
    syncUndergroundInputs();
    updateHud(state.cursor);
    scheduleRender();
    log(`地下切片移到 Y=${state.undergroundY}`);
  });

  undergroundClearToggle.addEventListener("change", () => {
    state.undergroundClear = undergroundClearToggle.checked;
  });

  saveBtn.addEventListener("click", saveMap);
  syncBtn.addEventListener("click", syncRuntime);
  importCommonBtn.addEventListener("click", importCommonMap);
  spawnX.addEventListener("change", onSpawnInput);
  spawnY.addEventListener("change", onSpawnInput);
  spawnZ.addEventListener("change", onSpawnInput);
  spawnPickBtn.addEventListener("click", setSpawnFromCursor);
  applyTerrainGlobalsBtn.addEventListener("click", applyTerrainGlobals);
  if (biomeTransitionInput) {
    biomeTransitionInput.addEventListener("input", () => {
      if (!canEdit()) {
        return;
      }
      if (!state.map) {
        return;
      }
      if (biomeTransitionBefore === null) {
        biomeTransitionBefore = state.map.biome_transition ?? 0;
      }
      const value = clamp(Number(biomeTransitionInput.value) || 0, 0, 64);
      state.map.biome_transition = value;
      if (biomeTransitionValue) {
        biomeTransitionValue.textContent = String(value);
      }
      setDirty(true);
      markTerrainDirty();
      scheduleRender();
    });
    biomeTransitionInput.addEventListener("change", () => {
      if (!canEdit() || !state.map || biomeTransitionBefore === null) {
        return;
      }
      const before = biomeTransitionBefore;
      biomeTransitionBefore = null;
      const after = state.map.biome_transition ?? 0;
      if (before === after) {
        return;
      }
      const snapshot = editorSnapshot();
      snapshot.biomeTransition = before;
      pushSnapshotHistory(snapshot, "调整群系过渡");
      log(`群系过渡：${after} 格`);
    });
  }
  newMapBtn.addEventListener("click", createNewMapFromUI);
  resizeMapBtn.addEventListener("click", resizeMapFromUI);
  backupNowBtn.addEventListener("click", createBackupNow);
  restoreBackupBtn.addEventListener("click", restoreSelectedBackup);
  regenerateDimensionBtn.addEventListener("click", scheduleDimensionRegeneration);
  cancelRegenerateDimensionBtn.addEventListener("click", cancelDimensionRegeneration);
  regenerateWorldPath.addEventListener("change", refreshRegenerationStatus);
  reloadMapBtn.addEventListener("click", () => {
    if (state.dirty && !window.confirm("当前地图有未保存修改，重新载入会丢失这些修改，继续？")) {
      return;
    }
    loadMap();
    refreshBackups();
  });
  autoSaveToggle.checked = state.autoSave;
  autoSaveToggle.addEventListener("change", () => {
    state.autoSave = autoSaveToggle.checked;
    localStorage.setItem("gensokyo-editor.auto-save", state.autoSave ? "1" : "0");
    log(state.autoSave ? "自动保存已开启（每 60 秒）" : "自动保存已关闭");
  });
  collabNameInput.value = state.collab.name;
  collabNameInput.addEventListener("input", () => {
    state.collab.name = collabNameInput.value.trim() || state.collab.name;
  });
  collabConnectBtn.addEventListener("click", connectCollab);
  collabCursors.addEventListener("change", scheduleRender);
  setCollabStatus("离线", false);
  updateCollabUsers();
  window.addEventListener("beforeunload", () => {
    if (state.collab.connected) {
      state.collab.source?.close();
    }
  });
  anchorTemplate.addEventListener("change", () => {
    state.selectedTemplate = anchorTemplate.value;
    if (state.placement) {
      state.placement.template = state.selectedTemplate;
    }
  });
  decorationTemplate.addEventListener("change", () => {
    state.selectedDecoration = decorationTemplate.value;
  });
  decorationPlaceBtn.addEventListener("click", () => {
    const trees = state.decorations.filter((id) => id.split(":").pop().startsWith("tree_"));
    const template = state.selectedDecoration === "__mix__"
      ? trees[Math.floor(Math.random() * trees.length)]
      : state.selectedDecoration;
    if (!template) {
      log("没有可用的装饰物模板");
      return;
    }
    setForestMode(false);
    state.selectedTemplate = template;
    anchorTemplate.value = template;
    setPlacementMode(true);
    log(`进入装饰物放置：${template.split(":").pop()}`);
  });
  forestModeBtn.addEventListener("click", () => setForestMode(!state.forestMode));
  forestDensity.addEventListener("input", () => {
    state.forestDensity = Number(forestDensity.value);
    forestDensityValue.textContent = state.forestDensity.toFixed(2);
  });
  forestAvoidStructures.checked = state.forestAvoidStructures;
  forestAvoidStructures.addEventListener("change", () => {
    state.forestAvoidStructures = forestAvoidStructures.checked;
  });
  forestAvoidWater.checked = state.forestAvoidWater;
  forestAvoidWater.addEventListener("change", () => {
    state.forestAvoidWater = forestAvoidWater.checked;
  });
  anchorAddBtn.addEventListener("click", addAnchorAtCursor);
  anchorSelectAllBtn.addEventListener("click", () => {
    const anchors = state.map?.anchors ?? [];
    setAnchorSelection(anchors.map((anchor) => anchor.id), anchors.at(-1)?.id ?? null);
  });
  anchorClearSelectionBtn.addEventListener("click", () => {
    setAnchorSelection([], null);
  });
  anchorApplyPosBtn.addEventListener("click", applyAnchorCoordinates);
  anchorApplyTransformBtn.addEventListener("click", applyAnchorTransform);
  anchorMoveBtn.addEventListener("click", beginAnchorMove);
  anchorDeleteSelectedBtn.addEventListener("click", deleteSelectedAnchor);
  placeModeBtn.addEventListener("click", togglePlacementMode);

  viewportStage.addEventListener("pointerdown", (event) => {
    if (event.target.closest(".hotbar")) {
      return;
    }
    if (event.button === 1 || event.button === 2) {
      if (state.viewMode === "3d") {
        const cosYaw = Math.cos(state.view3D.yaw);
        const sinYaw = Math.sin(state.view3D.yaw);
        const sinPitch = Math.sin(state.view3D.pitch);
        if (event.button === 1) {
          state.pan3D = {
            pointerId: event.pointerId,
            clientX: event.clientX,
            clientY: event.clientY,
            targetX: state.view3D.targetX,
            targetZ: state.view3D.targetZ,
            scale: Math.max(0.001, state.view3D.lastScale || 1),
            xzScale: Math.max(0.05, Number(state.view3D.xzScale) || 1),
            cosYaw,
            sinYaw,
            sinPitch: Math.max(0.12, sinPitch),
          };
        } else {
          state.orbit = {
            pointerId: event.pointerId,
            clientX: event.clientX,
            clientY: event.clientY,
            yaw: state.view3D.yaw,
            pitch: state.view3D.pitch,
          };
        }
      } else {
        state.panning = {
          pointerId: event.pointerId,
          clientX: event.clientX,
          clientY: event.clientY,
          centerX: state.view.centerX,
          centerZ: state.view.centerZ,
        };
      }
      viewportStage.setPointerCapture(event.pointerId);
      event.preventDefault();
      return;
    }
    if (event.button !== 0) {
      return;
    }
    if (state.collab.busy || state.drawing) return;
    if (!canEdit()) {
      const cell = state.viewMode === "3d" ? pick3DCell(event) : screenToCell(event);
      if (cell) {
        updateHud(cell);
      }
      event.preventDefault();
      return;
    }
    if (state.forestMode && !state.movingAnchorId) {
      const cell = state.viewMode === "3d" ? pick3DCell(event) : screenToCell(event);
      if (cell) {
        plantForest(cell);
        updateHud(cell);
        event.preventDefault();
        return;
      }
    }
    if (state.toolPage === "objects" && state.viewMode === "3d" && !state.placement && !state.movingAnchorId) {
      const picked = terrainView?.pickAnchor(event);
      if (picked) {
        startAnchorHandleDrag(event, picked);
        event.preventDefault();
        return;
      }
    }
    if (state.toolPage === "objects" && !state.placement && !state.movingAnchorId) {
      startStructureSelection(event);
      event.preventDefault();
      return;
    }
    const cell = state.viewMode === "3d" ? pick3DCell(event) : screenToCell(event);
    if (!cell) {
      return;
    }
    if (state.movingAnchorId) {
      moveAnchorToCell(cell);
      event.preventDefault();
      return;
    }
    if (state.placement) {
      placeAnchorAtCell(cell, state.placement.template);
      event.preventDefault();
      return;
    }
    if (state.editMode === "pick") {
      pickCellAt(cell);
      event.preventDefault();
      return;
    }
    viewportStage.setPointerCapture(event.pointerId);
    if (state.editMode === "rect" || state.editMode === "select") {
      state.drawing = false;
      state.rectDrawing = true;
      state.rect = { start: cell, end: cell };
      if (state.editMode === "select") {
        state.selection = null;
        updateSelectionInfo();
      }
      state.visited.clear();
      state.flattenTarget = null;
      updateHud(cell);
      scheduleRender();
      event.preventDefault();
      return;
    }
    state.drawing = true;
    state.visited.clear();
    state.flattenTarget = null;
    state.strokeChanges = new Map();
    state.undergroundChanges = new Map();
    queueBrushStamp(cell);
    state.lastCell = cell;
    updateHud(cell);
    scheduleRender();
    event.preventDefault();
  });

  viewportStage.addEventListener("pointermove", (event) => {
    if (state.draggingAnchor) {
      updateAnchorHandleDrag(event);
      event.preventDefault();
      return;
    }
    if (state.structureSelecting) {
      updateStructureSelection(event);
      event.preventDefault();
      return;
    }
    if (state.pan3D) {
      const pan = state.pan3D;
      const dx = event.clientX - pan.clientX;
      const dy = event.clientY - pan.clientY;
      const screenX = dx / pan.scale / pan.xzScale;
      const screenZ = dy / (pan.scale * pan.sinPitch) / pan.xzScale;
      const dX = screenX * pan.cosYaw + screenZ * pan.sinYaw;
      const dZ = -screenX * pan.sinYaw + screenZ * pan.cosYaw;
      state.view3D.targetX = clamp(pan.targetX - dX, 0, state.map.width);
      state.view3D.targetZ = clamp(pan.targetZ - dZ, 0, state.map.depth);
      scheduleRender();
      return;
    }
    if (state.orbit) {
      const dx = event.clientX - state.orbit.clientX;
      const dy = event.clientY - state.orbit.clientY;
      state.view3D.yaw = state.orbit.yaw - dx * 0.008;
      state.view3D.pitch = clamp(state.orbit.pitch + dy * 0.006, 0.12, 1.45);
      scheduleRender();
      return;
    }
    if (state.panning) {
      const scale = mapScale();
      const dx = event.clientX - state.panning.clientX;
      const dz = event.clientY - state.panning.clientY;
      state.view.centerX = state.panning.centerX - dx / scale;
      state.view.centerZ = state.panning.centerZ - dz / scale;
      scheduleRender();
      return;
    }
    if (state.rectDrawing) {
      const cell = state.viewMode === "3d" ? pick3DCell(event) : screenToCell(event);
      if (cell) {
        state.rect.end = cell;
        updateHud(cell);
        scheduleRender();
      }
      return;
    }
    const cell = state.viewMode === "3d" ? pick3DCell(event) : screenToCell(event);
    updateHud(cell);
    if (state.drawing && cell) {
      const from = state.lastCell ?? cell;
      const steps = Math.max(Math.abs(cell.x - from.x), Math.abs(cell.z - from.z));
      for (let i = 1; i <= Math.max(1, steps); i++) {
        const t = steps ? i / steps : 1;
        queueBrushStamp({ x: Math.round(from.x + (cell.x - from.x) * t), z: Math.round(from.z + (cell.z - from.z) * t) });
      }
      state.lastCell = cell;
      scheduleRender();
    } else if (state.viewMode === "2d" || state.viewMode === "3d") {
      scheduleRender();
    }
  });

  const endStroke = (event) => {
    if (state.draggingAnchor) {
      finishAnchorHandleDrag(event);
      updateHud(state.cursor);
      scheduleRender();
      return;
    }
    if (state.structureSelecting) {
      finishStructureSelection(event);
      updateHud(state.cursor);
      scheduleRender();
      return;
    }
    if (state.pan3D) {
      state.pan3D = null;
      if (event?.pointerId !== undefined && viewportStage.hasPointerCapture(event.pointerId)) {
        viewportStage.releasePointerCapture(event.pointerId);
      }
      scheduleRender();
      return;
    }
    if (state.orbit) {
      state.orbit = null;
      if (event?.pointerId !== undefined && viewportStage.hasPointerCapture(event.pointerId)) {
        viewportStage.releasePointerCapture(event.pointerId);
      }
      return;
    }
    if (state.panning) {
      state.panning = null;
      if (event?.pointerId !== undefined && viewportStage.hasPointerCapture(event.pointerId)) {
        viewportStage.releasePointerCapture(event.pointerId);
      }
      scheduleRender();
      return;
    }
    if (state.rectDrawing) {
      const rect = state.rect;
      if (rect) {
        if (state.editMode === "select") {
          state.selection = {
            start: { x: Math.min(rect.start.x, rect.end.x), z: Math.min(rect.start.z, rect.end.z) },
            end: { x: Math.max(rect.start.x, rect.end.x), z: Math.max(rect.start.z, rect.end.z) },
          };
          updateSelectionInfo();
          log(`已选择地形 ${state.selection.end.x - state.selection.start.x + 1}×${state.selection.end.z - state.selection.start.z + 1}`);
        } else {
          applyRectangle(rect.start, rect.end);
        }
      }
      state.rect = null;
      state.rectDrawing = false;
      if (event?.pointerId !== undefined && viewportStage.hasPointerCapture(event.pointerId)) {
        viewportStage.releasePointerCapture(event.pointerId);
      }
      scheduleRender();
      return;
    }
    if (!state.drawing) {
      return;
    }
    state.drawing = false;
    state.collab.busy = true;
    state.collab.editQueue = state.collab.editQueue.then(async () => {
      try {
        await commitStroke();
      } finally {
        await collabReleaseStrokeLocks();
        state.visited.clear();
        state.flattenTarget = null;
        state.lastCell = null;
        state.collab.busy = false;
        scheduleRender();
      }
    });
    if (event?.pointerId !== undefined && viewportStage.hasPointerCapture(event.pointerId)) {
      viewportStage.releasePointerCapture(event.pointerId);
    }
    scheduleRender();
  };
  viewportStage.addEventListener("pointerup", endStroke);
  viewportStage.addEventListener("pointercancel", endStroke);
  viewportStage.addEventListener("pointerleave", () => {
    if (!state.drawing && !state.panning && !state.orbit && !state.pan3D) {
      scheduleRender();
    }
  });
  viewportStage.addEventListener("contextmenu", (event) => event.preventDefault());

  minimap.addEventListener("pointerdown", (event) => {
    if (event.button !== 0) {
      return;
    }
    const cell = minimapEventToCell(event);
    if (!cell) {
      return;
    }
    state.minimapDrag = true;
    state.view.centerX = cell.x + 0.5;
    state.view.centerZ = cell.z + 0.5;
    state.view3D.targetX = cell.x + 0.5;
    state.view3D.targetZ = cell.z + 0.5;
    minimap.setPointerCapture(event.pointerId);
    scheduleRender();
    event.preventDefault();
  });
  minimap.addEventListener("pointermove", (event) => {
    if (!state.minimapDrag) {
      return;
    }
    const cell = minimapEventToCell(event);
    if (!cell) {
      return;
    }
    state.view.centerX = cell.x + 0.5;
    state.view.centerZ = cell.z + 0.5;
    state.view3D.targetX = cell.x + 0.5;
    state.view3D.targetZ = cell.z + 0.5;
    scheduleRender();
  });
  const endMinimapDrag = (event) => {
    if (!state.minimapDrag) {
      return;
    }
    state.minimapDrag = false;
    if (event?.pointerId !== undefined && minimap.hasPointerCapture(event.pointerId)) {
      minimap.releasePointerCapture(event.pointerId);
    }
  };
  minimap.addEventListener("pointerup", endMinimapDrag);
  minimap.addEventListener("pointercancel", endMinimapDrag);
  minimap.addEventListener("contextmenu", (event) => event.preventDefault());

  viewportStage.addEventListener("wheel", (event) => {
    event.preventDefault();
    const factor = event.deltaY < 0 ? 1.25 : 0.8;
    if (state.viewMode === "3d") {
      if (state.undergroundMode && event.shiftKey) {
        const delta = event.deltaY < 0 ? 1 : -1;
        state.undergroundY = clampUndergroundY(state.undergroundY + delta);
        syncUndergroundInputs();
        updateHud(state.cursor);
        scheduleRender();
        return;
      }
      const minZoom = Number(state.view3D.detail) === 0 ? 2 : 0.05;
      state.view3D.zoom = clamp(state.view3D.zoom * factor, minZoom, 100);
      scheduleRender();
      return;
    }
    const rect = canvas.getBoundingClientRect();
    const sx = (event.clientX - rect.left) / rect.width * canvas.width;
    const sz = (event.clientY - rect.top) / rect.height * canvas.height;
    const oldScale = mapScale();
    const cellX = state.view.centerX + (sx - canvas.width / 2) / oldScale;
    const cellZ = state.view.centerZ + (sz - canvas.height / 2) / oldScale;
    state.view.zoom = clamp(state.view.zoom * factor, 0.05, 64);
    const newScale = mapScale();
    state.view.centerX = cellX - (sx - canvas.width / 2) / newScale;
    state.view.centerZ = cellZ - (sz - canvas.height / 2) / newScale;
    scheduleRender();
  }, { passive: false });

  window.addEventListener("keydown", (event) => {
    const target = event.target;
    const inField = target instanceof HTMLElement && target.matches("input, textarea, select");
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "s") {
      event.preventDefault();
      saveMap();
      return;
    }
    if (event.key === "Escape") {
      if (state.draggingAnchor) {
        cancelAnchorHandleDrag();
        return;
      }
      setPlacementMode(false);
      if (state.structureSelecting) {
        state.structureSelecting = false;
        state.structureSelectRect = null;
        structureSelectBox.hidden = true;
        log("已取消结构框选");
      }
      if (state.movingAnchorId) {
        state.movingAnchorId = null;
        state.movingAnchorIds = [];
        state.movingAnchorOrigins = null;
        log("已取消结构移动");
        updateHud(state.cursor);
        scheduleRender();
      }
      if (state.forestMode) {
        setForestMode(false);
      }
      if (state.undergroundMode) {
        state.undergroundMode = false;
        undergroundToggle.checked = false;
        log("已退出地下切片模式");
        scheduleRender();
      }
      if (state.selectedAnchorIds.size > 0) {
        setAnchorSelection([], null);
        log("已清空结构选择");
      }
      return;
    }
    if (inField) {
      return;
    }
    if (state.undergroundMode && (event.key === "PageUp" || event.key === "PageDown")) {
      event.preventDefault();
      const delta = event.key === "PageUp" ? 1 : -1;
      if (event.shiftKey) {
        state.undergroundYMin = clampUndergroundY(state.undergroundYMin + delta);
        state.undergroundYMax = clampUndergroundY(state.undergroundYMax + delta);
      }
      state.undergroundY = clampUndergroundY(state.undergroundY + delta);
      syncUndergroundInputs();
      updateHud(state.cursor);
      scheduleRender();
      return;
    }
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "c") {
      if (state.selection) {
        event.preventDefault();
        copySelection();
      }
      return;
    }
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "v") {
      if (state.clipboard) {
        event.preventDefault();
        pasteSelection();
      }
      return;
    }
    if (event.key === "ArrowUp" || event.key === "ArrowDown" || event.key === "ArrowLeft" || event.key === "ArrowRight") {
      event.preventDefault();
      const width = state.map?.width ?? 64;
      const depth = state.map?.depth ?? 64;
      if (state.viewMode === "3d") {
        const step = Math.max(1, Math.max(width, depth) * 0.02 / Math.max(0.5, state.view3D.zoom));
        const yaw = state.view3D.yaw;
        const forwardX = -Math.sin(yaw);
        const forwardZ = -Math.cos(yaw);
        const rightX = Math.cos(yaw);
        const rightZ = -Math.sin(yaw);
        let dx = 0;
        let dz = 0;
        if (event.key === "ArrowUp") {
          dx = forwardX * step;
          dz = forwardZ * step;
        } else if (event.key === "ArrowDown") {
          dx = -forwardX * step;
          dz = -forwardZ * step;
        } else if (event.key === "ArrowLeft") {
          dx = -rightX * step;
          dz = -rightZ * step;
        } else {
          dx = rightX * step;
          dz = rightZ * step;
        }
        state.view3D.targetX = clamp(state.view3D.targetX + dx, 0, width);
        state.view3D.targetZ = clamp(state.view3D.targetZ + dz, 0, depth);
      } else {
        const step = 8 / Math.max(1, state.view.zoom);
        if (event.key === "ArrowUp") state.view.centerZ -= step;
        if (event.key === "ArrowDown") state.view.centerZ += step;
        if (event.key === "ArrowLeft") state.view.centerX -= step;
        if (event.key === "ArrowRight") state.view.centerX += step;
        state.view.centerX = clamp(state.view.centerX, 0, width);
        state.view.centerZ = clamp(state.view.centerZ, 0, depth);
      }
      scheduleRender();
      return;
    }
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "z") {
      event.preventDefault();
      undo();
      return;
    }
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "y") {
      event.preventDefault();
      redo();
      return;
    }
    if (event.key === "1") setLayer("height");
    if (event.key === "2") setLayer("biome");
    if (event.key === "3") setLayer("surface");
    const key = event.key.toLowerCase();
    if (key === "b") setEditMode("brush");
    if (key === "r") setEditMode("rect");
    if (key === "s") setEditMode("select");
    if (key === "i") setEditMode("pick");
    if (key === "t") setToolPage("terrain");
    if (key === "d") setToolPage("objects");
    if (key === "v") setToolPage("light");
    if (key === "e") setToolPage("blocks");
    if (key === "m") setToolPage("map");
    if (key === "j") {
      setToolPage("objects");
      setForestMode(!state.forestMode);
    }
    if (key === "x") {
      setToolPage("terrain");
      setLayer("height");
      setHeightTool("void");
    }
    if (key === "f") fitView();
    if (key === "[") {
      applyBrushSizeInput(state.brushSize - 1);
    }
    if (key === "]") {
      applyBrushSizeInput(state.brushSize + 1);
    }
    if (key === "g") {
      state.grid = !state.grid;
      gridToggle.checked = state.grid;
      scheduleRender();
    }
    if (key === "w") {
      state.water = !state.water;
      waterToggle.checked = state.water;
      markTerrainDirty();
      scheduleRender();
    }
  });

  window.addEventListener("beforeunload", (event) => {
    if (!state.dirty) {
      return;
    }
    event.preventDefault();
    event.returnValue = "";
  });
}

async function readLayer(name) {
  const response = await fetch(`/api/layers/${name}`, { cache: "no-cache" });
  if (!response.ok) {
    throw new Error(`无法读取图层 ${name}: ${response.status}`);
  }
  const buffer = await response.arrayBuffer();
  const cells = state.map.width * state.map.depth;
  if (name === "heightmap") {
    if (buffer.byteLength !== cells * 2) {
      throw new Error(`heightmap 大小错误: ${buffer.byteLength}`);
    }
    if (LITTLE_ENDIAN) {
      return new Int16Array(buffer);
    }
    const view = new DataView(buffer);
    const values = new Int16Array(cells);
    for (let i = 0; i < cells; i++) {
      values[i] = view.getInt16(i * 2, true);
    }
    return values;
  }
  if (name === "surfaces") {
    if (buffer.byteLength !== cells * 2) {
      throw new Error(`${name} 大小错误: ${buffer.byteLength}`);
    }
    if (LITTLE_ENDIAN) {
      return new Uint16Array(buffer);
    }
    const view = new DataView(buffer);
    const values = new Uint16Array(cells);
    for (let i = 0; i < cells; i++) {
      values[i] = view.getUint16(i * 2, true);
    }
    return values;
  }
  if (buffer.byteLength !== cells) {
    throw new Error(`${name} 大小错误: ${buffer.byteLength}`);
  }
  return new Uint8Array(buffer);
}

async function readUnderground() {
  const response = await fetch("/api/underground", { cache: "no-cache" });
  if (!response.ok) {
    throw new Error(`无法读取地下数据: ${response.status}`);
  }
  const data = await response.json();
  const result = new Map();
  for (const entry of data.blocks ?? []) {
    if (!Array.isArray(entry.pos) || entry.pos.length !== 3 || typeof entry.block !== "string") {
      continue;
    }
    result.set(`${entry.pos[0]},${entry.pos[1]},${entry.pos[2]}`, entry.block);
  }
  return result;
}

function applyTerrainGlobals() {
  if (!canEdit()) {
    log("只读角色不能修改地形全局参数");
    return;
  }
  if (!state.map) {
    return;
  }
  const seaLevel = Number(seaLevelInput.value);
  const dirtDepth = Number(dirtDepthInput.value);
  const maxY = state.map.min_y + state.map.world_height - 1;
  if (!Number.isInteger(seaLevel) || seaLevel < state.map.min_y || seaLevel > maxY) {
    log(`海平面必须是 ${state.map.min_y}..${maxY} 的整数`);
    return;
  }
  if (!Number.isInteger(dirtDepth) || dirtDepth < 0 || dirtDepth > 32) {
    log("泥土层厚度必须是 0..32 的整数");
    return;
  }
  const before = editorSnapshot();
  state.map.sea_level = seaLevel;
  state.map.dirt_depth = dirtDepth;
  if (!pushSnapshotHistory(before, "修改地形全局参数")) {
    return;
  }
  setDirty(true);
  fillMeta();
  markTerrainDirty();
  scheduleRender();
  log(`地形全局参数：海平面 Y=${seaLevel}，泥土层 ${dirtDepth}`);
}

function setCollabStatus(text, online = false) {
  collabStatus.textContent = text;
  collabStatus.classList.toggle("online", online);
  collabStatus.classList.toggle("offline", !online);
}

function updateCollabUsers() {
  const users = state.collab.users;
  if (!users || users.length === 0) {
    collabUsers.textContent = state.collab.connected ? "暂无其他编辑者" : "未连接";
    return;
  }
  collabUsers.replaceChildren();
  for (const user of users) {
    const row = document.createElement("span");
    row.className = "collab-user";
    row.style.setProperty("--user-color", user.color || "#7bd88f");
    const self = user.id === state.collab.clientId;
    const role = user.role === "viewer" ? " · 只读" : user.role === "admin" ? " · 管理员" : "";
    row.textContent = `${user.name}${self ? "（我）" : ""}${role}`;
    if (state.auth?.role === "admin" && !self) {
      const kick = document.createElement("button");
      kick.type = "button";
      kick.className = "collab-kick";
      kick.textContent = "移出";
      kick.title = "将该协作者移出房间";
      kick.addEventListener("click", (event) => {
        event.stopPropagation();
        void kickCollabUser(user.id);
      });
      row.append(kick);
    }
    collabUsers.append(row);
  }
}

async function kickCollabUser(clientId) {
  if (state.auth?.role !== "admin" || !clientId) {
    return;
  }
  if (!window.confirm("将该协作者移出房间？其登录会话也会失效。")) {
    return;
  }
  try {
    const response = await fetch("/api/collab/kick", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ clientId }),
    });
    const result = await response.json();
    if (!response.ok || !result.ok) {
      throw new Error(result.error || response.statusText);
    }
    log("已将该协作者移出房间");
  } catch (error) {
    log(`移出协作者失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

async function collabPost(type, payload) {
  if (!canEdit()) {
    throw new Error("只读角色不能修改地图");
  }
  if (!state.collab.connected) {
    throw new Error("协作连接已断开，请重连后再编辑");
  }
  const response = await fetch("/api/collab/op", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      clientId: state.collab.clientId,
      name: state.collab.name,
      type,
      baseRevision: state.mapRevision,
      payload,
    }),
  });
  const result = await response.json();
  if (!response.ok || !result.ok) throw new Error(result.error || "协作提交被拒绝");
  if (Number.isInteger(result.revision)) {
    state.mapRevision = Math.max(state.mapRevision, result.revision);
    state.collab.liveRevision = Math.max(state.collab.liveRevision, result.revision);
  }
  return result;
}

function collabCellIsLocked(x, z) {
  if (!state.collab.session) return false;
  return state.collab.leaseLost || !state.collab.lockedCellSet.has(`${x},${z}`);
}

function brushCells(cell, radius = state.brushSize) {
  const cells = [];
  for (let dz = -radius; dz <= radius; dz++) {
    for (let dx = -radius; dx <= radius; dx++) {
      const x = cell.x + dx;
      const z = cell.z + dz;
      if (x < 0 || z < 0 || x >= state.map.width || z >= state.map.depth) {
        continue;
      }
      if (dx * dx + dz * dz > radius * radius + 0.25) {
        continue;
      }
      cells.push({ x, z });
    }
  }
  return cells;
}

function rectangleCells(x, z, width, depth) {
  const cells = [];
  for (let dz = 0; dz < depth; dz++) {
    for (let dx = 0; dx < width; dx++) cells.push({ x: x + dx, z: z + dz });
  }
  return cells;
}

function patchCells(patch) {
  const cells = new Map();
  for (const change of patch.changes ?? []) {
    const x = change.index % state.map.width;
    const z = Math.floor(change.index / state.map.width);
    cells.set(`${x},${z}`, { x, z });
  }
  for (const change of patch.undergroundChanges ?? []) {
    const [x, , z] = change.key.split(",").map(Number);
    cells.set(`${x},${z}`, { x, z });
  }
  return [...cells.values()];
}

async function requestCellLeases(cells) {
  if (!state.collab.connected) throw new Error("协作连接已断开");
  const response = await fetch("/api/collab/lock", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ action: "take", clientId: state.collab.clientId, cells }),
  });
  const result = await response.json();
  if (!response.ok || !result.ok) {
    const owner = state.collab.users.find((user) => user.id === result.denied?.[0]?.owner);
    throw new Error(result.denied?.length
      ? `区域被${owner?.name ?? "其他协作者"}占用，本次申请未获取任何格子`
      : result.error || "申请锁失败");
  }
  return result.granted;
}

async function acquireCollabCells(cells) {
  if (!state.collab.session) return;
  if (!cells.length) return;
  if (state.collab.leaseLost) throw new Error("编辑锁已失效，请完成或取消当前笔画");
  const granted = await requestCellLeases(cells);
  state.collab.lockedCells = [...new Set([...state.collab.lockedCells, ...granted])];
  state.collab.lockedCellSet = new Set(state.collab.lockedCells);
  if (!state.collab.leaseTimer) {
    state.collab.leaseTimer = window.setInterval(async () => {
      if (!state.collab.lockedCells.length) return;
      try {
        await requestCellLeases(state.collab.lockedCells.map((key) => {
          const [x, z] = key.split(",").map(Number);
          return { x, z };
        }));
      } catch (error) {
        state.collab.leaseLost = true;
        clearInterval(state.collab.leaseTimer);
        state.collab.leaseTimer = null;
        log(`续锁失败，当前笔画将回滚：${error.message}`);
      }
    }, 5000);
  }
}

function queueBrushStamp(cell) {
  const radius = state.brushSize;
  state.collab.editQueue = state.collab.editQueue.then(async () => {
    try {
      if (state.collab.session) {
        await acquireCollabCells(brushCells(cell, radius));
      }
      applyBrush(cell.x, cell.z, radius);
      scheduleRender();
    } catch (error) {
      log(`跳过笔刷落点：${error.message}`);
    }
  });
}

async function collabReleaseStrokeLocks() {
  clearInterval(state.collab.leaseTimer);
  state.collab.leaseTimer = null;
  const cells = state.collab.lockedCells.map((key) => {
    const [x, z] = key.split(",").map(Number);
    return { x, z };
  });
  state.collab.lockedCells = [];
  state.collab.lockedCellSet.clear();
  state.collab.leaseLost = false;
  if (!cells.length) return;
  await fetch("/api/collab/lock", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ action: "release", clientId: state.collab.clientId, cells }),
  }).catch(() => {});
}

function canonicalMetaValue(value) {
  if (Array.isArray(value)) {
    return value.map(canonicalMetaValue);
  }
  if (value && typeof value === "object") {
    const result = {};
    for (const key of Object.keys(value).sort()) {
      result[key] = canonicalMetaValue(value[key]);
    }
    return result;
  }
  return value;
}

function metaValueEqual(left, right) {
  return JSON.stringify(canonicalMetaValue(left)) === JSON.stringify(canonicalMetaValue(right));
}

function metaHas(object, key) {
  return Boolean(object && Object.prototype.hasOwnProperty.call(object, key));
}

function metaSnapshotFromMap(map) {
  return {
    anchors: cloneJson(map.anchors ?? []),
    spawn: cloneJson(map.spawn ?? null),
    surface_palette: [...(map.surface_palette ?? [])],
    biome_palette: [...(map.biome_palette ?? [])],
    sea_level: map.sea_level,
    dirt_depth: map.dirt_depth ?? 3,
    biome_transition: map.biome_transition ?? 0,
  };
}

function metaSnapshotFromPayload(meta) {
  return {
    anchors: cloneJson(meta.anchors ?? []),
    spawn: cloneJson(meta.spawn ?? null),
    surface_palette: [...(meta.surface_palette ?? [])],
    biome_palette: [...(meta.biome_palette ?? [])],
    sea_level: meta.sea_level,
    dirt_depth: meta.dirt_depth ?? 3,
    biome_transition: meta.biome_transition ?? 0,
  };
}

function patchMetaBase(base, delta) {
  if (!base || !delta) {
    return;
  }
  if (delta.delta === false) {
    Object.assign(base, metaSnapshotFromPayload(delta));
    return;
  }
  const byId = new Map((base.anchors ?? []).map((anchor) => [anchor.id, cloneJson(anchor)]));
  for (const anchor of delta.anchors ?? []) {
    byId.set(anchor.id, cloneJson(anchor));
  }
  for (const id of delta.removedAnchors ?? []) {
    byId.delete(id);
  }
  base.anchors = [...byId.values()];
  for (const field of META_FIELDS) {
    if (metaHas(delta, field)) {
      base[field] = cloneJson(delta[field]);
    }
  }
}

function patchMapMetaAnchors(changed, removed) {
  if (!state.map) {
    return;
  }
  const byId = new Map((state.map.anchors ?? []).map((anchor) => [anchor.id, anchor]));
  for (const anchor of changed ?? []) {
    byId.set(anchor.id, cloneJson(anchor));
  }
  for (const id of removed ?? []) {
    byId.delete(id);
  }
  state.map.anchors = [...byId.values()];
}

function buildMetaDelta() {
  if (!state.map) {
    return null;
  }
  const current = metaSnapshotFromMap(state.map);
  const base = state.collab.lastMeta;
  if (!base) {
    return { delta: false, ...current };
  }
  const baseById = new Map((base.anchors ?? []).map((anchor) => [anchor.id, anchor]));
  const currentById = new Map((current.anchors ?? []).map((anchor) => [anchor.id, anchor]));
  const anchors = [];
  const baseAnchors = {};
  for (const [id, anchor] of currentById) {
    const previous = baseById.get(id);
    if (!previous || !metaValueEqual(previous, anchor)) {
      anchors.push(cloneJson(anchor));
      baseAnchors[id] = previous ? cloneJson(previous) : null;
    }
  }
  const removedAnchors = [];
  for (const [id, previous] of baseById) {
    if (!currentById.has(id)) {
      removedAnchors.push(id);
      baseAnchors[id] = cloneJson(previous);
    }
  }
  const fields = {};
  const baseFields = {};
  for (const field of META_FIELDS) {
    if (!metaValueEqual(current[field], base[field])) {
      fields[field] = cloneJson(current[field]);
      baseFields[field] = cloneJson(base[field]);
    }
  }
  if (anchors.length === 0 && removedAnchors.length === 0 && Object.keys(fields).length === 0) {
    return null;
  }
  return {
    delta: true,
    anchors,
    baseAnchors,
    removedAnchors,
    ...fields,
    baseFields,
  };
}

function applyOwnMetaResult(result) {
  if (!state.map) {
    return;
  }
  const applied = result?.meta;
  const conflicts = Array.isArray(result?.conflicts) ? result.conflicts : [];
  if (!state.collab.lastMeta) {
    state.collab.lastMeta = metaSnapshotFromMap(state.map);
  }
  if (applied) {
    patchMetaBase(state.collab.lastMeta, applied);
  }
  let conflictApplied = false;
  for (const conflict of conflicts) {
    if (conflict.kind === "anchor" && typeof conflict.id === "string") {
      const server = conflict.server ?? null;
      patchMapMetaAnchors(server ? [server] : [], server ? [] : [conflict.id]);
      patchMetaBase(state.collab.lastMeta, {
        delta: true,
        anchors: server ? [server] : [],
        removedAnchors: server ? [] : [conflict.id],
      });
      conflictApplied = true;
    } else if (conflict.kind === "field" && META_FIELDS.includes(conflict.field)) {
      state.map[conflict.field] = cloneJson(conflict.server);
      state.collab.lastMeta[conflict.field] = cloneJson(conflict.server);
      conflictApplied = true;
    }
  }
  if (conflictApplied) {
    state.selectedSurface = clamp(state.selectedSurface, 0, state.map.surface_palette.length - 1);
    state.selectedBiome = clamp(state.selectedBiome, 0, state.map.biome_palette.length - 1);
    buildPalettes();
    refreshAnchorList();
    fillMeta();
    fillSpawn();
    markTerrainDirty();
    scheduleRender();
  }
  if (conflicts.length > 0) {
    log(`元数据冲突：${conflicts.length} 项已被其他编辑者修改，已保留服务器版本`);
  }
}

function broadcastMapMeta() {
  if (!canEdit()) {
    return Promise.resolve();
  }
  if (!state.collab.connected || state.collab.applyingRemote || !state.map) {
    return Promise.resolve();
  }
  state.collab.metaQueue = state.collab.metaQueue
    .catch(() => {})
    .then(async () => {
      const delta = buildMetaDelta();
      if (!delta) {
        return null;
      }
      const result = await collabPost("meta", delta);
      applyOwnMetaResult(result);
      return result;
    })
    .catch((error) => {
      log(`元数据同步失败：${error.message}`);
      throw error;
    });
  return state.collab.metaQueue;
}

function queueCollabCursor(cell) {
  if (!canEdit() || !state.collab.connected || !cell || state.collab.cursorTimer) {
    return;
  }
  state.collab.cursorTimer = window.setTimeout(() => {
    state.collab.cursorTimer = 0;
    collabPost("cursor", { x: cell.x, z: cell.z }).catch(() => {});
  }, 90);
}

function applyRemoteTerrainChanges(changes, undergroundChanges = []) {
  if (!state.map) {
    return;
  }
  const hasCells = Array.isArray(changes) && changes.length > 0;
  const hasUnderground = Array.isArray(undergroundChanges) && undergroundChanges.length > 0;
  if (!hasCells && !hasUnderground) {
    return;
  }
  state.collab.applyingRemote = true;
  for (const change of changes ?? []) {
    if (!Number.isInteger(change.index)) {
      continue;
    }
    if (Number.isInteger(change.h)) {
      state.heights[change.index] = change.h;
    }
    if (Number.isInteger(change.b)) {
      state.biomes[change.index] = change.b;
    }
    if (Number.isInteger(change.s)) {
      state.surfaces[change.index] = change.s;
    }
  }
  for (const change of undergroundChanges ?? []) {
    if (typeof change.key === "string") {
      if (typeof change.block === "string") {
        state.underground.set(change.key, change.block);
      } else {
        state.underground.delete(change.key);
      }
    }
  }
  if (hasUnderground) {
    state.undergroundRevision += 1;
  }
  state.collab.applyingRemote = false;
  setDirty(true, false);
  markTerrainDirty();
  scheduleRender();
  log(`同步了远端地形修改（${(changes?.length ?? 0) + (undergroundChanges?.length ?? 0)} 项）`);
}

function applyRemoteMeta(meta, options = {}) {
  if (!state.map || !meta) {
    return;
  }
  if (options.baseOnly) {
    state.collab.lastMeta = metaSnapshotFromPayload(meta);
    return;
  }
  const delta = meta.delta === true;
  const beforeMeta = metaSnapshotFromMap(state.map);
  state.collab.applyingRemote = true;
  if (!delta) {
    if (Array.isArray(meta.anchors)) {
      state.map.anchors = cloneJson(meta.anchors);
    }
    for (const field of META_FIELDS) {
      if (!metaHas(meta, field)) {
        continue;
      }
      state.map[field] = cloneJson(meta[field]);
    }
    state.collab.lastMeta = metaSnapshotFromMap(state.map);
  } else {
    if (!state.collab.lastMeta) {
      state.collab.lastMeta = metaSnapshotFromMap(state.map);
    }
    patchMapMetaAnchors(meta.anchors, meta.removedAnchors);
    for (const field of META_FIELDS) {
      if (metaHas(meta, field)) {
        state.map[field] = cloneJson(meta[field]);
      }
    }
    patchMetaBase(state.collab.lastMeta, meta);
  }
  state.selectedSurface = clamp(state.selectedSurface, 0, state.map.surface_palette.length - 1);
  state.selectedBiome = clamp(state.selectedBiome, 0, state.map.biome_palette.length - 1);
  state.collab.applyingRemote = false;
  buildPalettes();
  refreshAnchorList();
  fillMeta();
  fillSpawn();
  markTerrainDirty();
  if (!metaValueEqual(beforeMeta, metaSnapshotFromMap(state.map))) {
    setDirty(true, false);
  }
  scheduleRender();
  log(delta ? "同步了远端结构/地图元数据增量" : "同步了远端结构/地图元数据");
}

function applyCollabEvent(event) {
  if (!event) {
    return;
  }
  if (event.type === "presence") {
    state.collab.users = Array.isArray(event.users) ? event.users : [];
    const active = new Set(state.collab.users.map((user) => user.id));
    for (const id of [...state.collab.cursors.keys()]) {
      if (!active.has(id)) {
        state.collab.cursors.delete(id);
      }
    }
    updateCollabUsers();
    return;
  }
  if (event.type === "reset") {
    state.collab.leaseLost = true;
    collabReleaseStrokeLocks();
    state.collab.stale = false;
    log("协作者重置了地图，正在重新载入");
    loadMap();
    return;
  }
  if (event.type === "saved") {
    const revision = Number.isInteger(event.revision) ? event.revision : state.mapRevision;
    state.mapRevision = Math.max(state.mapRevision, revision);
    state.collab.liveRevision = Math.max(state.collab.liveRevision, revision);
    log("协作者已保存地图");
    return;
  }
  if (event.type === "locks") {
    state.collab.remoteLocks = new Map(
      (Array.isArray(event.locks) ? event.locks : []).map((lock) => [lock.key, lock.owner]),
    );
    scheduleRender();
    return;
  }
  if (event.type === "cursor") {
    if (event.from === state.collab.clientId) {
      return;
    }
    state.collab.cursors.set(event.from, {
      x: Number(event.payload?.x),
      z: Number(event.payload?.z),
      name: event.name || "协作者",
      color: event.color || "#7bd88f",
      at: Date.now(),
    });
    scheduleRender();
    return;
  }
  if (Number.isInteger(event.seq) && event.seq <= state.collab.lastSeq) {
    return;
  }
  if (Number.isInteger(event.seq)) {
    state.collab.lastSeq = event.seq;
  }
  if (event.type === "terrain") {
    applyRemoteTerrainChanges(event.payload?.changes ?? [], event.payload?.undergroundChanges ?? []);
  } else if (event.type === "meta") {
    applyRemoteMeta(event.payload);
  }
  if (Number.isInteger(event.revision)) {
    state.mapRevision = Math.max(state.mapRevision, event.revision);
    state.collab.liveRevision = Math.max(state.collab.liveRevision, event.revision);
  }
}

function connectCollab() {
  if (state.collab.connected) {
    disconnectCollab();
    return;
  }
  const name = collabNameInput.value.trim() || state.collab.name;
  state.collab.name = name;
  localStorage.setItem("gensokyo-editor.collab-name", name);
  const query = new URLSearchParams({
    clientId: state.collab.clientId,
    name,
    metaDelta: "1",
  });
  const source = new EventSource(`/api/collab/stream?${query.toString()}`);
  state.collab.source = source;
  state.collab.session = true;
  state.collab.connected = true;
  state.collab.lastSeq = 0;
  state.collab.cursors.clear();
  setCollabStatus("连接中", false);
  collabConnectBtn.textContent = "断开";
  source.addEventListener("hello", (message) => {
    const data = JSON.parse(message.data);
    state.collab.users = Array.isArray(data.users) ? data.users : [];
    state.collab.lastSeq = 0;
    if (!state.drawing && !state.collab.busy) state.collab.leaseLost = false;
    state.collab.remoteLocks = new Map(
      (Array.isArray(data.locks) ? data.locks : []).map((lock) => [lock.key, lock.owner]),
    );
    for (const event of data.events ?? []) {
      applyCollabEvent(event);
    }
    const serverRevision = Number.isInteger(data.revision) ? data.revision : state.mapRevision;
    const serverCommitted = Number.isInteger(data.committedRevision) ? data.committedRevision : null;
    state.collab.liveRevision = Math.max(state.collab.liveRevision, serverRevision);
    let reloading = false;
    if (serverRevision > state.mapRevision) {
      const pureCheckpoint = data.lastSaveKind === "session"
        && serverCommitted !== null
        && serverCommitted === state.mapRevision + 1
        && serverRevision === serverCommitted;
      if (pureCheckpoint) {
        state.mapRevision = serverRevision;
        log("协作服务器已保存新检查点");
      } else if (state.localDirty) {
        setCollabStale("协作服务器上的地图版本较新；本地还有未保存修改，请先重新载入地图");
      } else {
        log("协作服务器上的地图版本较新，正在重新载入");
        reloading = true;
        loadMap();
      }
    } else {
      state.mapRevision = Math.max(state.mapRevision, serverRevision);
    }
    if (data.meta) {
      if (state.localDirty) {
        applyRemoteMeta(data.meta, { baseOnly: true });
      } else if (reloading) {
        state.collab.pendingMeta = data.meta;
      } else {
        applyRemoteMeta({ ...data.meta, delta: false });
      }
    }
    state.collab.connected = true;
    if (state.localDirty && !state.collab.stale) {
      broadcastMapMeta();
    }
    setCollabStatus("在线", true);
    updateCollabUsers();
    log(`已连接协作服务器：${state.collab.users.length} 位编辑者`);
  });
  source.addEventListener("kick", (message) => {
    let reason = "你已被管理员移出房间";
    try {
      const data = JSON.parse(message.data);
      if (data?.reason) {
        reason = String(data.reason);
      }
    } catch {
      // Keep the default reason.
    }
    log(reason);
    if (state.collab.source) {
      state.collab.source.close();
      state.collab.source = null;
    }
    state.collab.connected = false;
    state.collab.session = false;
    state.collab.stale = true;
    setCollabStatus("已移出", false);
    updateCollabUsers();
    state.auth = { ...state.auth, authenticated: false, role: null };
    applyAuthUi();
    showLogin(reason);
  });
  source.addEventListener("collab", (message) => {
    try {
      applyCollabEvent(JSON.parse(message.data));
    } catch (error) {
      console.warn("忽略无效协作消息", error);
    }
  });
  source.onopen = () => {
    state.collab.connected = true;
    setCollabStatus("在线", true);
  };
  source.onerror = () => {
    state.collab.connected = false;
    state.collab.leaseLost = true;
    setCollabStatus("重连中", false);
    updateCollabUsers();
  };
}

function disconnectCollab() {
  if (state.drawing || state.collab.busy) {
    log("请先结束当前笔画再断开协作");
    return;
  }
  collabReleaseStrokeLocks();
  if (state.collab.source) {
    state.collab.source.close();
    state.collab.source = null;
  }
  state.collab.connected = false;
  state.collab.session = false;
  state.collab.cursors.clear();
  state.collab.users = [];
  state.collab.remoteLocks.clear();
  setCollabStatus("离线", false);
  collabConnectBtn.textContent = "连接";
  updateCollabUsers();
  scheduleRender();
}

function drawRemoteCursors2D(ctx) {
  if (!state.collab.connected || !collabCursors.checked) {
    return;
  }
  for (const cursor of state.collab.cursors.values()) {
    if (Date.now() - cursor.at > 60000 || !Number.isFinite(cursor.x) || !Number.isFinite(cursor.z)) {
      continue;
    }
    const point = mapToScreen(cursor.x + 0.5, cursor.z + 0.5);
    ctx.beginPath();
    ctx.arc(point.x, point.y, 6, 0, Math.PI * 2);
    ctx.fillStyle = cursor.color;
    ctx.fill();
    ctx.strokeStyle = "rgba(10, 14, 18, 0.9)";
    ctx.lineWidth = 1.5;
    ctx.stroke();
    ctx.font = "10px sans-serif";
    ctx.fillStyle = cursor.color;
    ctx.fillText(cursor.name, point.x + 9, point.y - 7);
  }
}

function drawRemoteLocks2D(ctx) {
  if (!state.collab.connected || !state.collab.remoteLocks?.size || !collabCursors.checked) {
    return;
  }
  for (const [key, owner] of state.collab.remoteLocks) {
    if (owner === state.collab.clientId) {
      continue;
    }
    const [x, z] = key.split(",").map(Number);
    if (!Number.isInteger(x) || !Number.isInteger(z)) {
      continue;
    }
    const a = mapToScreen(x, z);
    const b = mapToScreen(x + 1, z + 1);
    ctx.fillStyle = "rgba(255, 120, 90, 0.24)";
    ctx.fillRect(a.x, a.y, b.x - a.x, b.y - a.y);
    ctx.strokeStyle = "rgba(255, 120, 90, 0.6)";
    ctx.lineWidth = 1;
    ctx.strokeRect(a.x + 0.5, a.y + 0.5, b.x - a.x, b.y - a.y);
  }
}

async function refreshBackups() {
  try {
    const response = await fetch("/api/backups", { cache: "no-store" });
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const result = await response.json();
    const backups = Array.isArray(result.backups) ? result.backups : [];
    const previous = backupList.value;
    backupList.replaceChildren();
    if (backups.length === 0) {
      const option = document.createElement("option");
      option.value = "";
      option.textContent = "（暂无备份）";
      backupList.append(option);
      restoreBackupBtn.disabled = true;
      return;
    }
    for (const backup of backups) {
      const option = document.createElement("option");
      option.value = backup.name;
      option.textContent = `${backup.name}${backup.label ? ` · ${backup.label}` : ""}`;
      backupList.append(option);
    }
    if ([...backupList.options].some((option) => option.value === previous)) {
      backupList.value = previous;
    }
    restoreBackupBtn.disabled = false;
  } catch (error) {
    log(`读取备份失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

async function createBackupNow() {
  if (!canEdit()) {
    log("只读角色不能创建备份");
    return;
  }
  try {
    const response = await fetch("/api/backups", { method: "POST" });
    const result = await response.json();
    if (!response.ok || !result.ok) {
      throw new Error(result.error ?? response.statusText);
    }
    log(`已创建备份 ${result.backup?.name ?? ""}`);
    await refreshBackups();
  } catch (error) {
    log(`创建备份失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

async function restoreSelectedBackup() {
  if (!canEdit()) {
    log("只读角色不能恢复备份");
    return;
  }
  const name = backupList.value;
  if (!name) {
    log("请选择一个备份");
    return;
  }
  if (!window.confirm(`恢复备份 ${name}？当前地图会先自动备份。`)) {
    return;
  }
  try {
    const response = await fetch("/api/backups/restore", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name }),
    });
    const result = await response.json();
    if (!response.ok || !result.ok) {
      throw new Error(result.error ?? response.statusText);
    }
    await loadMap();
    await refreshBackups();
    log(`已恢复备份 ${name}`);
  } catch (error) {
    log(`恢复备份失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

async function refreshRegenerationStatus() {
  if (!regenerateStatus || !regenerateWorldPath) {
    return;
  }
  const world = regenerateWorldPath.value.trim() || "fabric/runs/world";
  try {
    const response = await fetch(
      `/api/gensokyo/regenerate/status?world=${encodeURIComponent(world)}`,
      { cache: "no-store" },
    );
    const result = await response.json();
    if (!response.ok || !result.ok) {
      throw new Error(result.error ?? response.statusText);
    }
    regenerateStatus.textContent = result.pending
      ? `待处理：下次启动会把 ${result.dimension} 移动到世界目录下的 backups/`
      : `当前无待处理标记；世界目录：${result.world}${result.dimensionExists ? "" : "（尚未生成维度）"}`;
  } catch (error) {
    regenerateStatus.textContent = `检查重新生成状态失败：${error instanceof Error ? error.message : String(error)}`;
  }
}

async function scheduleDimensionRegeneration() {
  if (!canEdit()) {
    log("只读角色不能重新生成维度");
    return;
  }
  if (!state.map) {
    return;
  }
  if (state.dirty) {
    log("请先保存地图，再安排重新生成维度");
    return;
  }
  const world = regenerateWorldPath.value.trim() || "fabric/runs/world";
  if (!window.confirm(
    `将安排重新生成幻想乡维度。\n\n世界目录：${world}\n\n请确认：\n`
    + "1. 已保存地图并同步到 resources\n"
    + "2. 已重新构建/更新模组\n"
    + "3. 所有玩家已离开幻想乡\n\n"
    + "旧区域文件会在下次启动时移动到世界目录下的 backups/，不会直接删除。\n\n继续吗？",
  )) {
    return;
  }
  try {
    const response = await fetch("/api/gensokyo/regenerate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ world, confirm: true }),
    });
    const result = await response.json();
    if (!response.ok || !result.ok) {
      throw new Error((result.errors ?? [result.error ?? response.statusText]).join("; "));
    }
    log(`已安排重新生成幻想乡维度：${result.marker}`);
    log(`地图源备份：${result.mapBackup ?? "--"}；请完全重启 Minecraft。`);
    await refreshRegenerationStatus();
  } catch (error) {
    log(`安排重新生成失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

async function cancelDimensionRegeneration() {
  if (!canEdit()) {
    log("只读角色不能取消维度重生成");
    return;
  }
  const world = regenerateWorldPath.value.trim() || "fabric/runs/world";
  try {
    const response = await fetch("/api/gensokyo/regenerate/cancel", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ world }),
    });
    const result = await response.json();
    if (!response.ok || !result.ok) {
      throw new Error(result.error ?? response.statusText);
    }
    log(result.removed ? "已取消待处理的幻想乡重新生成" : "当前没有待处理的重新生成标记");
    await refreshRegenerationStatus();
  } catch (error) {
    log(`取消重新生成失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

async function createNewMapFromUI() {
  if (!canEdit()) {
    log("只读角色不能新建地图");
    return;
  }
  const width = Number(newMapWidth.value);
  const depth = Number(newMapDepth.value);
  if (!Number.isInteger(width) || !Number.isInteger(depth) || width < 1 || depth < 1
      || width > MAX_MAP_AXIS || depth > MAX_MAP_AXIS || width * depth > MAX_MAP_CELLS) {
    log(`地图尺寸必须为 1..${MAX_MAP_AXIS} 的整数，且总格数不超过 ${MAX_MAP_CELLS}`);
    return;
  }
  if (!window.confirm(`新建 ${width}×${depth} 地图？当前地图会先自动备份。`)) {
    return;
  }
  try {
    const response = await fetch("/api/map/new", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ width, depth }),
    });
    const result = await response.json();
    if (!response.ok || !result.ok) {
      throw new Error(result.error ?? response.statusText);
    }
    await loadMap();
    await refreshBackups();
    log(`已新建 ${width}×${depth} 地图`);
  } catch (error) {
    log(`新建地图失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

async function resizeMapFromUI() {
  if (!canEdit()) {
    log("只读角色不能调整地图尺寸");
    return;
  }
  const width = Number(newMapWidth.value);
  const depth = Number(newMapDepth.value);
  if (!Number.isInteger(width) || !Number.isInteger(depth) || width < 1 || depth < 1
      || width > MAX_MAP_AXIS || depth > MAX_MAP_AXIS || width * depth > MAX_MAP_CELLS) {
    log(`地图尺寸必须为 1..${MAX_MAP_AXIS} 的整数，且总格数不超过 ${MAX_MAP_CELLS}`);
    return;
  }
  if (!window.confirm(`调整地图到 ${width}×${depth}？超出范围的锚点和地下方块会被裁掉，当前地图会先备份。`)) {
    return;
  }
  try {
    const response = await fetch("/api/map/resize", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ width, depth }),
    });
    const result = await response.json();
    if (!response.ok || !result.ok) {
      throw new Error(result.error ?? response.statusText);
    }
    await loadMap();
    await refreshBackups();
    log(`地图尺寸已调整为 ${width}×${depth}`);
  } catch (error) {
    log(`调整地图尺寸失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

function upsampleOverview(values, Type, width, depth, factor) {
  const overviewWidth = Math.ceil(width / factor);
  const overviewHeight = Math.ceil(depth / factor);
  const full = new Type(width * depth);
  const rowBuffer = new Type(width);
  let cachedRow = -1;
  for (let z = 0; z < depth; z++) {
    const overviewRow = Math.min(overviewHeight - 1, Math.floor(z / factor));
    if (overviewRow !== cachedRow) {
      cachedRow = overviewRow;
      for (let ox = 0; ox < overviewWidth; ox++) {
        const value = values[overviewRow * overviewWidth + ox];
        const startX = ox * factor;
        const endX = Math.min(width, startX + factor);
        for (let x = startX; x < endX; x++) {
          rowBuffer[x] = value;
        }
      }
    }
    full.set(rowBuffer, z * width);
  }
  return full;
}

async function readOverviewLayers(map) {
  const width = map.width;
  const depth = map.depth;
  const factor = OVERVIEW_FACTOR;
  const overviewWidth = Math.ceil(width / factor);
  const overviewHeight = Math.ceil(depth / factor);
  const names = ["heightmap", "biomes", "surfaces"];
  const buffers = await Promise.all(names.map(async (name) => {
    const response = await fetch(`/api/layers/${name}/overview?factor=${factor}`, { cache: "no-cache" });
    if (!response.ok) {
      return null;
    }
    return await response.arrayBuffer();
  }));
  if (buffers.some((buffer) => !buffer)) {
    return null;
  }
  const expected = overviewWidth * overviewHeight;
  if (buffers[0].byteLength !== expected * 2
      || buffers[1].byteLength !== expected
      || buffers[2].byteLength !== expected * 2) {
    return null;
  }
  const heights = LITTLE_ENDIAN ? new Int16Array(buffers[0]) : (() => {
    const view = new DataView(buffers[0]);
    const result = new Int16Array(expected);
    for (let i = 0; i < expected; i++) result[i] = view.getInt16(i * 2, true);
    return result;
  })();
  const biomes = new Uint8Array(buffers[1]);
  const surfaces = LITTLE_ENDIAN ? new Uint16Array(buffers[2]) : (() => {
    const view = new DataView(buffers[2]);
    const result = new Uint16Array(expected);
    for (let i = 0; i < expected; i++) result[i] = view.getUint16(i * 2, true);
    return result;
  })();
  return [
    upsampleOverview(heights, Int16Array, width, depth, factor),
    upsampleOverview(biomes, Uint8Array, width, depth, factor),
    upsampleOverview(surfaces, Uint16Array, width, depth, factor),
  ];
}

async function loadDetailedTiles(map, token) {
  if (!map.layers) {
    state.layersDetailed = true;
    return;
  }
  const width = map.width;
  const depth = map.depth;
  const tileSize = DETAIL_TILE_SIZE;
  const layers = ["heightmap", "biomes", "surfaces"];
  const tasks = [];
  for (const layer of layers) {
    for (let z = 0; z < depth; z += tileSize) {
      for (let x = 0; x < width; x += tileSize) {
        const tileWidth = Math.min(tileSize, width - x);
        const tileHeight = Math.min(tileSize, depth - z);
        tasks.push({ layer, x, z, tileWidth, tileHeight });
      }
    }
  }
  const total = tasks.length;
  let cursor = 0;
  let done = 0;
  let failed = 0;

  async function loadTile(task) {
    const response = await fetch(
      `/api/layers/${task.layer}/tile?x=${task.x}&z=${task.z}&size=${tileSize}`,
      { cache: "no-cache" },
    );
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const buffer = await response.arrayBuffer();
    const bytesPerCell = task.layer === "biomes" ? 1 : 2;
    if (buffer.byteLength !== task.tileWidth * task.tileHeight * bytesPerCell) {
      throw new Error(`tile size mismatch: ${buffer.byteLength}`);
    }
    let view;
    let target;
    if (task.layer === "heightmap") {
      view = new Int16Array(buffer);
      target = state.heights;
    } else if (task.layer === "surfaces") {
      view = new Uint16Array(buffer);
      target = state.surfaces;
    } else {
      view = new Uint8Array(buffer);
      target = state.biomes;
    }
    for (let row = 0; row < task.tileHeight; row++) {
      target.set(
        view.subarray(row * task.tileWidth, (row + 1) * task.tileWidth),
        (task.z + row) * width + task.x,
      );
    }
  }

  async function worker() {
    while (cursor < total) {
      if (token !== state.layerLoadToken) {
        return;
      }
      const index = cursor++;
      const task = tasks[index];
      let complete = false;
      for (let attempt = 0; attempt < 2 && !complete; attempt++) {
        try {
          await loadTile(task);
          complete = true;
        } catch {
          if (attempt === 1) {
            failed++;
          }
        }
      }
      done++;
      if (done % 100 === 0 || done === total) {
        log(`高精度图层加载中：${done}/${total}`);
      }
      if (done % 25 === 0) {
        scheduleRender();
      }
      if (done % 8 === 0) {
        await new Promise((resolve) => setTimeout(resolve, 0));
      }
    }
  }

  await Promise.all(Array.from({ length: 4 }, () => worker()));
  if (token !== state.layerLoadToken) {
    return;
  }
  if (failed > 0) {
    log(`高精度图层有 ${failed} 块加载失败；当前仍是概览数据，暂不能编辑/保存`);
    return;
  }
  state.layersDetailed = true;
  computeHeightRange();
  markTerrainDirty();
  setDirty(state.dirty);
  scheduleRender();
  log("高精度图层加载完成，现在可以编辑/保存");
}

async function loadDetailedLayers(map, token) {
  if (!map.layers) {
    state.layersDetailed = true;
    return;
  }
  const names = ["heightmap", "biomes", "surfaces"];
  try {
    for (let i = 0; i < names.length; i++) {
      if (token !== state.layerLoadToken) {
        return;
      }
      const name = names[i];
      log(`高精度图层 ${i + 1}/${names.length} 加载中：${name}...`);
      const values = await readLayer(name);
      if (token !== state.layerLoadToken) {
        return;
      }
      if (name === "heightmap") {
        state.heights = values;
      } else if (name === "surfaces") {
        state.surfaces = values;
      } else {
        state.biomes = values;
      }
    }
    state.layersDetailed = true;
    if (terrainView) {
      terrainView.setMap(state.map, state.heights, state.surfaces, state.biomes);
    }
    computeHeightRange();
    markTerrainDirty();
    setDirty(state.dirty);
    scheduleRender();
    log("高精度图层加载完成，现在可以编辑/保存");
  } catch (error) {
    log(`高精度图层加载失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

async function loadMap() {
  try {
    const [response, borderResponse] = await Promise.all([
      fetch("/api/map", { cache: "no-store" }), fetch("/api/border-settings", { cache: "no-store" }),
    ]);
    if (borderResponse.ok) terrainView?.setBorderSettings(await borderResponse.json());
    if (!response.ok) {
      throw new Error(await response.text());
    }
    const map = await response.json();
    const token = ++state.layerLoadToken;
    state.map = map;
    log(`正在加载 ${map.width}×${map.depth}（${map.layers ? "二进制图层" : "内联 JSON"}）...`);
    dirtyState.textContent = "加载中";
    let heights;
    let biomes;
    let surfaces;
    let underground;
    if (map.layers) {
      const [overview, undergroundData] = await Promise.all([
        readOverviewLayers(map),
        readUnderground(),
      ]);
      underground = undergroundData;
      if (overview) {
        state.layersDetailed = false;
        [heights, biomes, surfaces] = overview;
        initMap(map, heights, biomes, surfaces, underground);
        log(`已显示 1/${OVERVIEW_FACTOR} 概览；完整高精度图层正在后台加载，完成后才可编辑/保存`);
        void loadDetailedLayers(map, token);
        return;
      }
      log("概览图层不可用，回退为完整图层加载...");
      state.layersDetailed = true;
      [heights, biomes, surfaces] = await Promise.all([
        readLayer("heightmap"),
        readLayer("biomes"),
        readLayer("surfaces"),
      ]);
    } else {
      state.layersDetailed = true;
      heights = Int16Array.from(map.heights ?? []);
      biomes = Uint8Array.from(map.biomes ?? []);
      surfaces = Uint16Array.from(map.surfaces ?? []);
      underground = new Map();
    }
    initMap(map, heights, biomes, surfaces, underground);
  } catch (error) {
    dirtyState.textContent = "加载失败";
    log(`加载失败：${error instanceof Error ? error.message : String(error)}`);
  }
}

bootstrap();
