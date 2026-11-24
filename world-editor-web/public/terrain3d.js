import * as THREE from "./vendor/three.module.js";
import { BlockModelLoader } from "./blockmodels.js";
import { createBorderTerrain } from "./border-terrain.js";

const FOV = 45;
// Full-resolution block preview. The whole 12800x10240 map would be 131M cells,
// so 1:1 blocks are rendered only in a window around the 3D view target, while
// the coarse overview remains available for the whole map.
const FULL_BLOCK_VIEW = 512;
const FULL_BLOCK_SNAP = 64;

function colorToLinear(rgb) {
  const color = new THREE.Color();
  color.setRGB(rgb[0] / 255, rgb[1] / 255, rgb[2] / 255, THREE.SRGBColorSpace);
  return color;
}

export class Terrain3DView {
  constructor(canvas) {
    this.canvas = canvas;
    this.renderer = new THREE.WebGLRenderer({
      canvas,
      antialias: true,
      powerPreference: "high-performance",
    });
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    this.renderer.outputColorSpace = THREE.SRGBColorSpace;
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
    this.renderer.toneMappingExposure = 1.08;
    this.renderer.shadowMap.enabled = true;
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    this.renderer.setClearColor(0x0b1118, 1);

    this.scene = new THREE.Scene();
    this.scene.background = new THREE.Color(0x0b1118);
    this.scene.fog = new THREE.Fog(0x0b1118, 1, 10000);

    // World-space objects live under one group so vertical exaggeration can be
    // applied to the whole 3D preview without touching the saved height data.
    this.worldGroup = new THREE.Group();
    this.worldGroup.frustumCulled = false;
    this.scene.add(this.worldGroup);

    // Horizontal (XZ) preview scaling. Anything positioned in absolute map
    // coordinates but not vertically exaggerated (anchors, structures,
    // underground slices, remote cursors) lives under this group, so scaling XZ
    // keeps everything aligned and stays a pure preview transform.
    this.horizontalGroup = new THREE.Group();
    this.horizontalGroup.frustumCulled = false;
    this.scene.add(this.horizontalGroup);

    // Whole-map low-resolution backdrop used behind the 1:1 local window.
    this.backgroundGroup = new THREE.Group();
    this.backgroundGroup.frustumCulled = false;
    this.backgroundGroup.visible = false;
    this.worldGroup.add(this.backgroundGroup);
    this.backgroundMaterial = new THREE.MeshStandardMaterial({
      vertexColors: true,
      roughness: 0.95,
      metalness: 0,
      side: THREE.DoubleSide,
      depthWrite: false,
    });
    this.backgroundMesh = null;
    this.backgroundKey = null;

    this.camera = new THREE.PerspectiveCamera(FOV, 1, 0.1, 200000);

    const hemisphere = new THREE.HemisphereLight(0xdcecff, 0x202a32, 1.15);
    this.scene.add(hemisphere);
    const sun = new THREE.DirectionalLight(0xfff0d0, 2.0);
    sun.position.set(-1.2, 2.4, 1.5);
    sun.castShadow = true;
    sun.shadow.mapSize.set(2048, 2048);
    sun.shadow.bias = -0.0005;
    sun.shadow.normalBias = 0.02;
    this.scene.add(sun);
    sun.target = new THREE.Object3D();
    this.scene.add(sun.target);
    const fill = new THREE.DirectionalLight(0x7aa7ff, 0.55);
    fill.position.set(1.4, 1.0, -1.2);
    this.scene.add(fill);
    this.hemisphereLight = hemisphere;
    this.sunLight = sun;
    this.fillLight = fill;

    this.terrainMaterial = new THREE.MeshStandardMaterial({
      vertexColors: true,
      roughness: 0.92,
      metalness: 0,
      flatShading: false,
      side: THREE.DoubleSide,
    });
    this.terrainGroup = new THREE.Group();
    this.terrainGroup.frustumCulled = false;
    this.worldGroup.add(this.terrainGroup);
    this.borderGroup = new THREE.Group();
    this.worldGroup.add(this.borderGroup);
    this.borderMaterial = new THREE.MeshStandardMaterial({ vertexColors: true, roughness: 1, side: THREE.DoubleSide });
    this.borderDirty = true;
    this.borderSettings = null;
    this.terrainMeshes = new Map();
    this.terrainChunkSize = 32;
    this.terrainChunkCols = 0;
    this.terrainChunkRows = 0;
    this.dirtyChunks = null;
    this.step = 1;
    this.noiseTexture = this.createNoiseTexture();
    this.terrainMaterial.map = this.noiseTexture;
    this.terrainMaterial.needsUpdate = true;
    this.atlasTexture = null;
    new THREE.TextureLoader().load(
      "/assets/terrain_atlas.png",
      (texture) => {
        texture.flipY = false;
        texture.magFilter = THREE.NearestFilter;
        texture.minFilter = THREE.NearestFilter;
        texture.generateMipmaps = false;
        texture.colorSpace = THREE.SRGBColorSpace;
        this.atlasTexture = texture;
        this.dirty = true;
      },
      undefined,
      () => {
        this.atlasTexture = null;
      },
    );

    this.waterMaterial = new THREE.MeshStandardMaterial({
      color: 0x3472d6,
      transparent: true,
      opacity: 0.72,
      roughness: 0.22,
      metalness: 0.1,
      depthWrite: false,
    });
    this.water = new THREE.Mesh(new THREE.BufferGeometry(), this.waterMaterial);
    this.water.visible = false;
    this.water.renderOrder = 2;
    this.worldGroup.add(this.water);

    this.anchorGroup = new THREE.Group();
    this.horizontalGroup.add(this.anchorGroup);
    this.remoteCursorGroup = new THREE.Group();
    this.horizontalGroup.add(this.remoteCursorGroup);
    this.remoteCursors = new Map();
    this.remoteCursorGeometry = new THREE.CylinderGeometry(0.12, 0.12, 3, 6);
    this.remoteCursorRingGeometry = new THREE.RingGeometry(0.34, 0.48, 24);
    this.selectedAnchorIds = new Set();
    this.selectedAnchorMaterial = new THREE.MeshStandardMaterial({
      color: 0x7bd88f,
      emissive: 0x1c4a2a,
      emissiveIntensity: 0.7,
      roughness: 0.35,
      metalness: 0.05,
    });
    this.selectedFootprintMaterial = new THREE.LineBasicMaterial({
      color: 0x7bd88f,
      transparent: true,
      opacity: 0.9,
      depthTest: false,
    });
    this.anchorRotateRingGeometry = new THREE.TorusGeometry(1, 0.07, 8, 32);
    this.anchorRotateRingMaterial = new THREE.MeshBasicMaterial({
      color: 0x7bd88f,
      transparent: true,
      opacity: 0.85,
      depthTest: false,
      side: THREE.DoubleSide,
    });
    this.anchorYMaterial = new THREE.MeshBasicMaterial({
      color: 0x9bdcff,
      transparent: true,
      opacity: 0.9,
      depthTest: false,
    });
    this.anchorYShaftGeometry = new THREE.CylinderGeometry(0.12, 0.12, 1.6, 8);
    this.anchorYTipGeometry = new THREE.ConeGeometry(0.3, 0.6, 8);
    this.anchorScaleGeometry = new THREE.BoxGeometry(0.6, 0.6, 0.6);
    this.anchorScaleMaterial = new THREE.MeshBasicMaterial({
      color: 0xffb347,
      transparent: true,
      opacity: 0.95,
      depthTest: false,
    });
    this.entityMarkerGeometry = new THREE.OctahedronGeometry(0.24, 0);
    this.entityMarkerMaterial = new THREE.MeshBasicMaterial({
      color: 0xff8ad8,
      transparent: true,
      opacity: 0.78,
      depthTest: false,
    });
    this.structureGroup = new THREE.Group();
    this.horizontalGroup.add(this.structureGroup);
    this.structureLoader = new BlockModelLoader();
    this.structureTemplates = new Map();
    this.structureToken = 0;
    this.structureKey = null;
    this.placementStructureGroup = new THREE.Group();
    this.placementStructureGroup.visible = false;
    this.horizontalGroup.add(this.placementStructureGroup);
    this.placementStructureToken = 0;
    this.placementStructureTemplate = null;
    this.undergroundMode = false;
    this.undergroundY = 32;
    this.undergroundKey = null;
    this.undergroundGroup = new THREE.Group();
    this.undergroundGroup.visible = false;
    this.horizontalGroup.add(this.undergroundGroup);
    this.undergroundPlane = new THREE.Mesh(
      new THREE.PlaneGeometry(1, 1),
      new THREE.MeshBasicMaterial({
        color: 0x152536,
        transparent: true,
        opacity: 0.72,
        side: THREE.DoubleSide,
        depthWrite: false,
      }),
    );
    this.undergroundPlane.rotation.x = -Math.PI / 2;
    this.undergroundPlane.visible = false;
    this.undergroundPlane.renderOrder = 1;
    this.horizontalGroup.add(this.undergroundPlane);
    this.undergroundGrid = new THREE.GridHelper(1, 32, 0x3a607e, 0x24384b);
    this.undergroundGrid.visible = false;
    this.undergroundGrid.renderOrder = 2;
    this.horizontalGroup.add(this.undergroundGrid);
    this.undergroundYMin = 32;
    this.undergroundYMax = 32;
    this.undergroundTruncated = false;
    this.undergroundRangeMaterial = new THREE.LineBasicMaterial({
      color: 0x6fc7ff,
      transparent: true,
      opacity: 0.55,
      depthTest: false,
    });
    this.undergroundRangeBox = new THREE.LineSegments(
      new THREE.EdgesGeometry(new THREE.BoxGeometry(1, 1, 1)),
      this.undergroundRangeMaterial,
    );
    this.undergroundRangeBox.visible = false;
    this.undergroundRangeBox.renderOrder = 3;
    this.horizontalGroup.add(this.undergroundRangeBox);
    this.undergroundBrushMaterial = new THREE.LineBasicMaterial({
      color: 0xffd479,
      transparent: true,
      opacity: 0.8,
      depthTest: false,
    });
    this.undergroundBrushBox = new THREE.LineSegments(
      new THREE.EdgesGeometry(new THREE.BoxGeometry(1, 1, 1)),
      this.undergroundBrushMaterial,
    );
    this.undergroundBrushBox.visible = false;
    this.undergroundBrushBox.renderOrder = 4;
    this.horizontalGroup.add(this.undergroundBrushBox);
    this.undergroundMaterials = new Map();
    this.undergroundCubeGeometry = new THREE.BoxGeometry(1, 1, 1);
    this.anchorGeometry = new THREE.ConeGeometry(0.9, 2.2, 8);
    this.anchorMaterial = new THREE.MeshStandardMaterial({
      color: 0xffd479,
      emissive: 0x3b2600,
      emissiveIntensity: 0.35,
      roughness: 0.4,
      metalness: 0.05,
    });
    this.footprintMaterial = new THREE.LineBasicMaterial({
      color: 0xffd479,
      transparent: true,
      opacity: 0.65,
      depthTest: false,
    });
    this.placementFootprint = new THREE.LineSegments(new THREE.BufferGeometry(), this.footprintMaterial);
    this.placementFootprint.visible = false;
    this.placementFootprint.renderOrder = 7;
    this.worldGroup.add(this.placementFootprint);
    this.lastPlacementKey = null;

    this.cursorMaterial = new THREE.MeshBasicMaterial({
      color: 0xf6fbff,
      transparent: true,
      opacity: 0.95,
      depthTest: false,
      side: THREE.DoubleSide,
    });
    this.cursor = new THREE.Mesh(new THREE.RingGeometry(0.62, 0.82, 32), this.cursorMaterial);
    this.cursor.rotation.x = -Math.PI / 2;
    this.cursor.visible = false;
    this.cursor.renderOrder = 5;
    this.worldGroup.add(this.cursor);
    this.cursorOutlineMaterial = new THREE.LineBasicMaterial({
      color: 0xf6fbff,
      transparent: true,
      opacity: 0.95,
      depthTest: false,
    });
    this.cursorOutline = new THREE.LineLoop(new THREE.BufferGeometry(), this.cursorOutlineMaterial);
    this.cursorOutline.visible = false;
    this.cursorOutline.renderOrder = 6;
    this.worldGroup.add(this.cursorOutline);

    this.rectMaterial = new THREE.LineBasicMaterial({
      color: 0x9bdcff,
      transparent: true,
      opacity: 0.95,
      depthTest: false,
    });
    this.rectLine = new THREE.LineLoop(new THREE.BufferGeometry(), this.rectMaterial);
    this.rectLine.visible = false;
    this.rectLine.renderOrder = 6;
    this.worldGroup.add(this.rectLine);

    this.raycaster = new THREE.Raycaster();
    this.pointer = new THREE.Vector2();
    this.map = null;
    this.heights = null;
    this.surfaces = null;
    this.biomes = null;
    this.step = 1;
    this.actualMode = false;
    this.actualHeights = null;
    this.dirty = true;
    this.lastLayer = null;
    this.lastWater = null;
    this.lastAnchors = null;
    this.lastActualMode = false;
    this.lastMinHeight = 0;
    this.lastMaxHeight = 1;
    this.yScale = 1;
    this.xzScale = 1;
    this.lastDetailGrid = 384;
    this.fullBlocks = false;
    this.fullWindowX = 0;
    this.fullWindowZ = 0;
    this.fullWindowW = 0;
    this.fullWindowD = 0;
    this.gridOriginX = 0;
    this.gridOriginZ = 0;
  }

  createNoiseTexture() {
    const size = 64;
    const data = new Uint8Array(size * size * 4);
    for (let y = 0; y < size; y++) {
      for (let x = 0; x < size; x++) {
        const tile = ((x >> 2) + (y >> 2)) & 1;
        const hash = ((x * 37 + y * 17) ^ (x * y)) & 31;
        const value = Math.min(255, 214 + (tile ? 6 : 0) + hash);
        const index = (y * size + x) * 4;
        data[index] = value;
        data[index + 1] = value;
        data[index + 2] = value;
        data[index + 3] = 255;
      }
    }
    const texture = new THREE.DataTexture(data, size, size, THREE.RGBAFormat);
    texture.wrapS = THREE.RepeatWrapping;
    texture.wrapT = THREE.RepeatWrapping;
    texture.magFilter = THREE.NearestFilter;
    texture.minFilter = THREE.NearestFilter;
    texture.generateMipmaps = false;
    texture.colorSpace = THREE.SRGBColorSpace;
    texture.needsUpdate = true;
    return texture;
  }

  setCanvasSize(width, height) {
    this.canvas.width = width;
    this.canvas.height = height;
    this.renderer.setSize(width, height, false);
    this.camera.aspect = width / height;
    this.camera.updateProjectionMatrix();
  }

  setMap(map, heights, surfaces, biomes) {
    if (this.map !== map || this.heights !== heights || this.surfaces !== surfaces || this.biomes !== biomes) this.borderDirty = true;
    if (this.map === map && this.heights === heights && this.surfaces === surfaces && this.biomes === biomes) {
      return;
    }
    if (this.map !== map) {
      this.structureKey = null;
      this.structureToken++;
      this.structureGroup.clear();
      this.structureTemplates.clear();
      this.selectedAnchorIds.clear();
      this.fullWindowW = 0;
      this.fullWindowD = 0;
      this.gridOriginX = 0;
      this.gridOriginZ = 0;
      this.terrainGroup.position.set(0, 0, 0);
      this.water.position.set(0, 0, 0);
    }
    if (this.backgroundMesh) {
      this.backgroundGroup.remove(this.backgroundMesh);
      this.backgroundMesh.geometry.dispose();
      this.backgroundMesh = null;
    }
    this.backgroundKey = null;
    this.backgroundGroup.visible = false;
    this.map = map;
    this.heights = heights;
    this.surfaces = surfaces;
    this.biomes = biomes;
    this.dirty = true;
  }

  markDirty() {
    this.borderDirty = true;
    this.dirty = true;
    this.dirtyChunks = null;
  }

  displayY(y) {
    const minY = this.map?.min_y ?? 0;
    return minY + (Number(y) - minY) * (this.yScale || 1);
  }

  inverseDisplayY(y) {
    const minY = this.map?.min_y ?? 0;
    return minY + (Number(y) - minY) / (this.yScale || 1);
  }

  effectiveExaggeration(state) {
    const explicit = Number(state?.view3D?.exaggeration ?? 0);
    if (Number.isFinite(explicit) && explicit > 0) {
      return Math.max(1, Math.min(64, explicit));
    }
    const range = Math.max(1, Number(state?.heightMax ?? 1) - Number(state?.heightMin ?? 0));
    if (range > 16) {
      return 1;
    }
    const width = Math.max(1, this.map?.width ?? 1);
    // Keep the 1:1 height data untouched; only stretch the preview so low-relief
    // maps (for example the 8..12 preclassic heightmap) do not look like a plane.
    return Math.max(1, Math.min(64, Math.round(width / (range * 50))));
  }

  markDirtyRegion(x0, z0, x1, z1) {
    if (this.map && (Math.min(x0, x1) < 64 || Math.min(z0, z1) < 64
      || Math.max(x0, x1) >= this.map.width - 64 || Math.max(z0, z1) >= this.map.depth - 64)) this.borderDirty = true;
    if (this.dirty) {
      return;
    }
    const step = Math.max(1, this.step || 1);
    const chunkSize = Math.max(1, this.terrainChunkSize || 32);
    const minGx = Math.floor((Math.min(x0, x1) - this.gridOriginX) / step);
    const maxGx = Math.floor((Math.max(x0, x1) - this.gridOriginX) / step);
    const minGz = Math.floor((Math.min(z0, z1) - this.gridOriginZ) / step);
    const maxGz = Math.floor((Math.max(z0, z1) - this.gridOriginZ) / step);
    const cx0 = Math.floor(minGx / chunkSize);
    const cx1 = Math.floor(maxGx / chunkSize);
    const cz0 = Math.floor(minGz / chunkSize);
    const cz1 = Math.floor(maxGz / chunkSize);
    if (!this.dirtyChunks) {
      this.dirtyChunks = new Set();
    }
    for (let cx = cx0; cx <= cx1; cx++) {
      for (let cz = cz0; cz <= cz1; cz++) {
        this.dirtyChunks.add(`${cx},${cz}`);
      }
    }
  }

  setAnchorSelection(ids) {
    this.selectedAnchorIds = new Set(ids ?? []);
    this.dirty = true;
  }

  setLighting(light = {}) {
    this.lastLight = light;
    const azimuth = THREE.MathUtils.degToRad(Number(light.azimuth ?? 322));
    const elevation = THREE.MathUtils.degToRad(Number(light.elevation ?? 52));
    const intensity = Math.min(3.5, Math.max(0.2, Number(light.intensity ?? 2)));
    const shadows = light.shadows !== false;
    const width = this.map?.width ?? 64;
    const depth = this.map?.depth ?? 64;
    const minY = this.map?.min_y ?? 0;
    const centerY = this.displayY((minY + (this.map?.sea_level ?? 32)) * 0.5);
    const xzScale = this.xzScale || 1;
    const centerX = width * 0.5 * xzScale;
    const centerZ = depth * 0.5 * xzScale;
    const distance = Math.max(128, Math.max(width, depth) * xzScale * 2);
    const horizontal = Math.cos(elevation);
    this.sunLight.position.set(
      centerX + Math.sin(azimuth) * horizontal * distance,
      centerY + Math.sin(elevation) * distance,
      centerZ + Math.cos(azimuth) * horizontal * distance,
    );
    this.sunLight.target.position.set(centerX, centerY, centerZ);
    this.sunLight.target.updateMatrixWorld();
    this.sunLight.intensity = intensity;
    this.sunLight.castShadow = shadows;
    this.renderer.shadowMap.enabled = shadows;
    const shadowRadius = Math.max(32, Math.max(width, depth) * xzScale * 0.9);
    this.sunLight.shadow.camera.left = -shadowRadius;
    this.sunLight.shadow.camera.right = shadowRadius;
    this.sunLight.shadow.camera.top = shadowRadius;
    this.sunLight.shadow.camera.bottom = -shadowRadius;
    this.sunLight.shadow.camera.near = 1;
    this.sunLight.shadow.camera.far = distance + shadowRadius * 2;
    this.sunLight.shadow.camera.updateProjectionMatrix();
    const shadowSize = Math.max(width, depth) > 1024 ? 1024 : 2048;
    if (this.sunLight.shadow.mapSize.x !== shadowSize) {
      this.sunLight.shadow.mapSize.set(shadowSize, shadowSize);
      this.sunLight.shadow.map?.dispose();
      this.sunLight.shadow.map = null;
    }
    this.renderer.shadowMap.needsUpdate = shadows;
    const height = Math.sin(elevation);
    this.sunLight.color.setRGB(1.0, 0.78 + 0.2 * height, 0.58 + 0.34 * height);
    this.hemisphereLight.intensity = 0.82 + 0.16 * intensity;
    this.fillLight.intensity = 0.32 + 0.12 * intensity;
  }

  update(state, colorForCell) {
    if (!this.map || !this.heights) {
      return;
    }
    this.undergroundMode = Boolean(state.undergroundMode);
    this.undergroundY = Number(state.undergroundY ?? 0);
    const mapMin = this.map?.min_y ?? 0;
    const mapMax = (this.map?.min_y ?? 0) + (this.map?.world_height ?? 1024) - 1;
    this.undergroundYMin = Math.max(mapMin, Math.min(mapMax, Number(state.undergroundYMin ?? this.undergroundY)));
    this.undergroundYMax = Math.max(mapMin, Math.min(mapMax, Number(state.undergroundYMax ?? this.undergroundY)));
    this.actualMode = Boolean(state.actualMode);
    const exaggeration = this.effectiveExaggeration(state);
    if (exaggeration !== this.yScale) {
      this.yScale = exaggeration;
      this.dirty = true;
      this.dirtyChunks = null;
      // Anchor/structure Y positions are stored in display space, so rebuild them.
      this.lastAnchors = null;
      this.structureKey = null;
    }
    const fullBlocks = Number(state.view3D?.detail) === 0;
    if (fullBlocks !== this.fullBlocks) {
      this.fullBlocks = fullBlocks;
      this.dirty = true;
      this.dirtyChunks = null;
    }
    const detailGrid = fullBlocks ? 0 : Math.max(128, Math.min(1024, Number(state.view3D?.detail) || 384));
    if (detailGrid !== this.lastDetailGrid) {
      this.lastDetailGrid = detailGrid;
      this.dirty = true;
      this.dirtyChunks = null;
    }
    if (fullBlocks) {
      const mapWidth = this.map.width;
      const mapDepth = this.map.depth;
      const size = Math.min(FULL_BLOCK_VIEW, mapWidth, mapDepth);
      const targetX = Number(state.view3D?.targetX ?? mapWidth / 2);
      const targetZ = Number(state.view3D?.targetZ ?? mapDepth / 2);
      const margin = Math.min(64, Math.floor(size / 4));
      const needsRecenter = !this.fullWindowW
        || targetX < this.fullWindowX + margin
        || targetX > this.fullWindowX + size - margin
        || targetZ < this.fullWindowZ + margin
        || targetZ > this.fullWindowZ + size - margin;
      if (needsRecenter) {
        const wx = Math.max(0, Math.min(mapWidth - size, Math.floor((targetX - size / 2) / FULL_BLOCK_SNAP) * FULL_BLOCK_SNAP));
        const wz = Math.max(0, Math.min(mapDepth - size, Math.floor((targetZ - size / 2) / FULL_BLOCK_SNAP) * FULL_BLOCK_SNAP));
        if (wx !== this.fullWindowX || wz !== this.fullWindowZ
            || size !== this.fullWindowW || size !== this.fullWindowD) {
          this.fullWindowX = wx;
          this.fullWindowZ = wz;
          this.fullWindowW = size;
          this.fullWindowD = size;
          this.dirty = true;
          this.dirtyChunks = null;
        }
      }
    }
    const xzScale = Math.max(0.05, Math.min(16, Number(state.view3D?.xzScale) || 1));
    if (xzScale !== this.xzScale) {
      this.xzScale = xzScale;
      if (this.lastLight) {
        this.setLighting(this.lastLight);
      }
    }
    const worldScale = this.undergroundMode ? 1 : this.yScale;
    const worldOffset = (this.map?.min_y ?? 0) * (1 - worldScale);
    this.worldGroup.scale.set(this.xzScale, worldScale, this.xzScale);
    this.worldGroup.position.y = worldOffset;
    this.horizontalGroup.scale.set(this.xzScale, 1, this.xzScale);
    const layerChanged = this.lastLayer !== state.layer;
    const waterChanged = this.lastWater !== state.water;
    const actualChanged = this.lastActualMode !== this.actualMode;
    const heightChanged = this.lastMinHeight !== state.heightMin || this.lastMaxHeight !== state.heightMax;
    const anchorsChanged = this.lastAnchors !== JSON.stringify(state.map?.anchors ?? []);
    if (layerChanged || waterChanged || actualChanged || heightChanged || anchorsChanged) {
      this.dirty = true;
      this.dirtyChunks = null;
    }
    const hasDirtyChunks = Boolean(this.dirtyChunks && this.dirtyChunks.size > 0);
    if (this.dirty || hasDirtyChunks) {
      this.rebuild(state, colorForCell, this.dirty ? null : this.dirtyChunks);
      this.dirty = false;
      this.dirtyChunks = null;
      this.lastLayer = state.layer;
      this.lastWater = state.water;
      this.lastActualMode = this.actualMode;
      this.lastMinHeight = state.heightMin;
      this.lastMaxHeight = state.heightMax;
    }
    this.updateUnderground(state);
    this.rebuildBorder(state);
    if (this.borderGroup.children[1]) this.borderGroup.children[1].visible = state.water;
    this.updatePlacementStructure(state);
    this.updateCamera(state.view3D, state);
    this.updateOverlays(state);
  }

  setBorderSettings(settings) {
    this.borderSettings = settings;
    this.borderDirty = true;
  }

  rebuildBorder(state) {
    const visible = state.view3D?.border !== false && !state.undergroundMode && state.layersDetailed !== false && Boolean(this.borderSettings);
    this.borderGroup.visible = visible;
    if (!visible) return;
    const map = this.map;
    const step = Math.max(8, Math.ceil(Math.max(map.width, map.depth) / Math.max(192, this.lastDetailGrid || 384)));
    if (!this.borderDirty && this.borderStep === step) return;
    const terrain = createBorderTerrain(map, this.heights, this.surfaces, this.biomes, this.borderSettings);
    const width = terrain.width, [ox, oz] = map.origin ?? [0, 0];
    const samples = new Map();
    const sample = (x, z) => {
      const key = `${x},${z}`;
      if (!samples.has(key)) samples.set(key, terrain.sample(x + ox, z + oz));
      return samples.get(key);
    };
    const colors = {
      'minecraft:snow_block': [232, 240, 240], 'minecraft:stone': [130, 132, 130],
      'minecraft:gravel': [137, 129, 121], 'minecraft:sand': [211, 201, 146],
      'minecraft:red_sand': [170, 93, 47], 'minecraft:clay': [145, 157, 171],
      'minecraft:basalt': [76, 76, 78], 'minecraft:podzol': [123, 88, 51],
      'minecraft:mycelium': [117, 98, 105], 'minecraft:coarse_dirt': [130, 95, 65],
    };
    const positions = [], vertexColors = [], waterPositions = [];
    const quad = (points, target) => {
      for (const i of [0, 1, 2, 0, 2, 3]) target.push(...points[i]);
    };
    const rect = (x0, z0, x1, z1) => {
      for (let z = z0; z < z1; z += step) for (let x = x0; x < x1; x += step) {
        const right = Math.min(x1, x + step), south = Math.min(z1, z + step);
        const coords = [[x, z], [x, south], [right, south], [right, z]];
        const columns = coords.map(([px, pz]) => sample(px, pz));
        quad(coords.map(([px, pz], i) => [px, columns[i].groundY, pz]), positions);
        for (const i of [0, 1, 2, 0, 2, 3]) {
          const c = colorToLinear(colors[columns[i].surface] ?? [109, 139, 75]);
          vertexColors.push(c.r, c.g, c.b);
        }
        if (columns.every(c => c.water)) quad(coords.map(([px, pz], i) => [px, columns[i].waterY + .08, pz]), waterPositions);
      }
    };
    // Four non-overlapping strips. Peripheral meshes are excluded from the editing raycaster.
    rect(-width, -width, map.width + width, 0);
    rect(-width, map.depth, map.width + width, map.depth + width);
    rect(-width, 0, 0, map.depth);
    rect(map.width, 0, map.width + width, map.depth);
    for (const child of this.borderGroup.children) child.geometry.dispose();
    this.borderGroup.clear();
    const ground = new THREE.BufferGeometry();
    ground.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3));
    ground.setAttribute('color', new THREE.Float32BufferAttribute(vertexColors, 3));
    ground.computeVertexNormals();
    ground.computeBoundingBox();
    this.borderWidth = width;
    this.borderMinHeight = ground.boundingBox.min.y;
    this.borderMaxHeight = ground.boundingBox.max.y;
    this.borderGroup.add(new THREE.Mesh(ground, this.borderMaterial));
    const water = new THREE.BufferGeometry();
    water.setAttribute('position', new THREE.Float32BufferAttribute(waterPositions, 3));
    water.computeVertexNormals();
    const waterMesh = new THREE.Mesh(water, this.waterMaterial);
    waterMesh.visible = state.water;
    this.borderGroup.add(waterMesh);
    this.borderDirty = false;
    this.borderStep = step;
  }

  rebuild(state, colorForCell, dirtyChunks = null) {
    const map = this.map;
    const width = map.width;
    const depth = map.depth;
    const fullBlocks = this.fullBlocks && this.fullWindowW > 0;
    const originX = fullBlocks ? this.fullWindowX : 0;
    const originZ = fullBlocks ? this.fullWindowZ : 0;
    const spanW = fullBlocks ? this.fullWindowW : width;
    const spanD = fullBlocks ? this.fullWindowD : depth;
    const step = fullBlocks ? 1 : Math.max(1, Math.ceil(Math.max(width, depth) / (this.lastDetailGrid || 384)));
    this.step = step;
    this.gridOriginX = originX;
    this.gridOriginZ = originZ;
    this.terrainGroup.position.set(originX, 0, originZ);
    this.water.position.set(originX, 0, originZ);
    const gridW = fullBlocks ? spanW : Math.ceil(width / step);
    const gridD = fullBlocks ? spanD : Math.ceil(depth / step);
    const localEast = fullBlocks ? gridW : width;
    const localSouth = fullBlocks ? gridD : depth;
    const cols = gridW + 1;
    const heights = this.heights;
    const minY = map.min_y;
    const full = !dirtyChunks;

    const rawHeights = new Float32Array(cols * (gridD + 1));
    for (let gz = 0; gz <= gridD; gz++) {
      const z = Math.min(depth - 1, Math.max(0, originZ + gz * step));
      for (let gx = 0; gx <= gridW; gx++) {
        const x = Math.min(width - 1, Math.max(0, originX + gx * step));
        rawHeights[gz * cols + gx] = heights[z * width + x];
      }
    }
    const actualMode = Boolean(state.actualMode);
    let smoothHeights = rawHeights;
    if (!actualMode) {
      const heightRange = Math.max(1, Number(state.heightMax) - Number(state.heightMin));
      const smoothingPasses = heightRange <= 16 ? 1 : 6;
      for (let pass = 0; pass < smoothingPasses; pass++) {
        const next = new Float32Array(smoothHeights.length);
        for (let gz = 0; gz <= gridD; gz++) {
          for (let gx = 0; gx <= gridW; gx++) {
            const index = gz * cols + gx;
            const center = smoothHeights[index];
            if (center === -32768) {
              next[index] = center;
              continue;
            }
            let sum = center * 2;
            let count = 2;
            for (const [ox, oz] of [[-1, 0], [1, 0], [0, -1], [0, 1]]) {
              const nx = gx + ox;
              const nz = gz + oz;
              if (nx < 0 || nx > gridW || nz < 0 || nz > gridD) continue;
              const neighbor = smoothHeights[nz * cols + nx];
              if (neighbor !== -32768) {
                sum += neighbor;
                count++;
              }
            }
            next[index] = sum / count;
          }
        }
        smoothHeights = next;
      }
    }

    const heightAt = (gx, gz) => smoothHeights[gz * cols + gx];
    this.displayHeights = smoothHeights;
    this.displayCols = cols;
    this.displayGridW = gridW;
    this.displayGridD = gridD;
    this.displayStep = step;
    if (actualMode) {
      if (full || !(this.actualHeights instanceof Float32Array) || this.actualHeights.length !== gridW * gridD) {
        this.actualHeights = new Float32Array(gridW * gridD).fill(-32768);
      }
    } else {
      this.actualHeights = null;
    }

    const normalAt = (gx, gz) => {
      const left = heightAt(Math.max(0, gx - 1), gz);
      const right = heightAt(Math.min(gridW, gx + 1), gz);
      const down = heightAt(gx, Math.max(0, gz - 1));
      const up = heightAt(gx, Math.min(gridD, gz + 1));
      const slopeX = (right - left) / (2 * step);
      const slopeZ = (up - down) / (2 * step);
      const length = Math.hypot(slopeX, 1, slopeZ);
      return [-slopeX / length, 1 / length, -slopeZ / length];
    };

    const useAtlas = Boolean(this.atlasTexture && state.layer !== "biome");
    const materialMap = useAtlas ? this.atlasTexture : this.noiseTexture;
    if (this.terrainMaterial.map !== materialMap) {
      this.terrainMaterial.map = materialMap;
      this.terrainMaterial.needsUpdate = true;
    }

    const tileInfo = (blockId) => {
      const key = String(blockId ?? "").split("[")[0].split(":").pop();
      if (key.includes("grass")) return { top: 0, side: 1, grassTop: true, tint: false };
      if (key === "sand" || key === "red_sand") return { top: 2, side: 2, grassTop: false, tint: false };
      if (key.includes("stone") || key === "andesite" || key === "diorite" || key === "granite") {
        return { top: 3, side: 3, grassTop: false, tint: false };
      }
      if (key === "dirt" || key === "coarse_dirt" || key === "rooted_dirt" || key === "podzol") {
        return { top: 4, side: 4, grassTop: false, tint: false };
      }
      return { top: 4, side: 4, grassTop: false, tint: true };
    };
    const keyHasAirLike = (blockId) => {
      const key = String(blockId ?? "").split("[")[0].split(":").pop();
      return key === "air" || key === "cave_air" || key === "void_air" || key === "water";
    };
    const dirtDepth = Math.max(0, Number(state.map.dirt_depth ?? 3));
    const tileUV = (tile, u, v) => {
      const col = tile % 4;
      const row = Math.floor(tile / 4);
      return [
        (col * 16 + 0.5 + u * 15) / 64,
        (row * 16 + 0.5 + v * 15) / 32,
      ];
    };

    const buildChunkGeometry = (gx0, gx1, gz0, gz1) => {
      const positions = [];
      const colors = [];
      const uvs = [];
      const normals = [];
      const indices = [];
      const pushVertex = (x, y, z, nx, ny, nz, rgb, uv) => {
        positions.push(x, y, z);
        normals.push(nx, ny, nz);
        const linear = colorToLinear(rgb);
        colors.push(linear.r, linear.g, linear.b);
        uvs.push(uv[0], uv[1]);
      };
      const addQuad = (a, b, c, d, normal, color, tile, flip = false, vertical = false) => {
        const start = positions.length / 3;
        const uvsForQuad = vertical
          ? [
            [0, 0],
            [1, 0],
            [1, 1],
            [0, 1],
          ]
          : [
            [0, 0],
            [0, 1],
            [1, 1],
            [1, 0],
          ];
        for (let i = 0; i < 4; i++) {
          const point = [a, b, c, d][i];
          pushVertex(point[0], point[1], point[2], ...normal, color, tileUV(tile, uvsForQuad[i][0], uvsForQuad[i][1]));
        }
        if (flip) {
          indices.push(start, start + 2, start + 1, start, start + 3, start + 2);
        } else {
          indices.push(start, start + 1, start + 2, start, start + 2, start + 3);
        }
      };

      const addActualWall = (gx, gz, x0, x1, z0, z1, top, neighborHeight, normal) => {
        const sampleX = Math.min(width - 1, Math.max(0, originX + gx * step + Math.floor(step / 2)));
        const sampleZ = Math.min(depth - 1, Math.max(0, originZ + gz * step + Math.floor(step / 2)));
        const sampleIndex = sampleZ * width + sampleX;
        const blockId = state.layer === "biome" ? "" : map.surface_palette[state.surfaces[sampleIndex]];
        const info = tileInfo(blockId);
        const surfaceTile = keyHasAirLike(blockId) ? 3 : info.side;
        const dirtTile = tileInfo("minecraft:dirt").side;
        const stoneTile = tileInfo("minecraft:stone").side;
        const neighbor = Number.isFinite(neighborHeight) ? neighborHeight : Math.max(minY, top - 64);
        const bottomLimit = Math.max(minY, Math.max(neighbor, top - 64));
        const color = useAtlas ? [245, 245, 245] : [215, 215, 215];
        let currentTop = top;
        for (let level = 0; currentTop > bottomLimit + 0.0001; level++) {
          const nextBottom = Math.max(bottomLimit, currentTop - 1);
          const tile = level === 0 ? surfaceTile : level <= dirtDepth ? dirtTile : stoneTile;
          addQuad(
            [x0, currentTop, z0],
            [x1, currentTop, z1],
            [x1, nextBottom, z1],
            [x0, nextBottom, z0],
            normal,
            color,
            tile,
            false,
            true,
          );
          currentTop = nextBottom;
        }
      };

      const addEdge = (gx, gz, x0, x1, z0, z1, h0, h1, normal) => {
        const x = Math.min(width - 1, Math.max(0, originX + gx * step + Math.floor(step / 2)));
        const z = Math.min(depth - 1, Math.max(0, originZ + gz * step + Math.floor(step / 2)));
        const index = z * width + x;
        const blockId = state.layer === "biome" ? "" : map.surface_palette[state.surfaces[index]];
        const info = tileInfo(blockId);
        const surfaceTile = keyHasAirLike(blockId) ? 3 : info.side;
        const dirtTile = tileInfo("minecraft:dirt").side;
        const stoneTile = tileInfo("minecraft:stone").side;
        const baseBottom = Math.max(minY, Math.max(h0, h1) - 64);
        const depthBlocks = Math.ceil(Math.max(h0, h1) - baseBottom);
        for (let level = 0; level < depthBlocks; level++) {
          const top0 = h0 - level;
          const top1 = h1 - level;
          if (Math.max(top0, top1) <= baseBottom) {
            break;
          }
          const bottom0 = Math.max(baseBottom, top0 - 1);
          const bottom1 = Math.max(baseBottom, top1 - 1);
          if (bottom0 >= top0 && bottom1 >= top1) {
            break;
          }
          const tile = level === 0 ? surfaceTile : level <= dirtDepth ? dirtTile : stoneTile;
          const color = useAtlas ? [245, 245, 245] : [215, 215, 215];
          addQuad(
            [x0, top0, z0],
            [x1, top1, z1],
            [x1, bottom1, z1],
            [x0, bottom0, z0],
            normal,
            color,
            tile,
            false,
            true,
          );
        }
      };

      const neighborAt = (ngx, ngz) => {
        if (ngx < 0 || ngz < 0 || ngx >= gridW || ngz >= gridD) {
          return null;
        }
        const nx = Math.min(width - 1, originX + ngx * step + Math.floor(step / 2));
        const nz = Math.min(depth - 1, originZ + ngz * step + Math.floor(step / 2));
        const neighbor = heights[nz * width + nx];
        return neighbor === -32768 ? null : neighbor;
      };

      for (let gz = gz0; gz < gz1; gz++) {
        for (let gx = gx0; gx < gx1; gx++) {
          const x = Math.min(width - 1, Math.max(0, originX + gx * step + Math.floor(step / 2)));
          const z = Math.min(depth - 1, Math.max(0, originZ + gz * step + Math.floor(step / 2)));
          const index = z * width + x;
          const height = heights[index];
          if (height === -32768) {
            continue;
          }
          const x0 = Math.max(0, gx * step);
          const x1 = Math.min(width, (gx + 1) * step);
          const z0 = Math.max(0, gz * step);
          const z1 = Math.min(depth, (gz + 1) * step);
          const baseColor = colorForCell(index, state.layer, state);
          const blockId = state.layer === "biome" ? "" : state.map.surface_palette[state.surfaces[index]];
          const tile = tileInfo(blockId);
          const tint = !useAtlas || state.layer === "biome"
            ? baseColor
            : (tile.tint ? baseColor : [248, 248, 248]);
          const variation = 0.94 + ((gx * 13 + gz * 7) % 11) / 100;
          const topColor = [
            Math.min(255, tint[0] * variation),
            Math.min(255, tint[1] * variation),
            Math.min(255, tint[2] * variation),
          ];
          if (actualMode) {
            if (this.actualHeights) {
              this.actualHeights[gz * gridW + gx] = height;
            }
            addQuad(
              [x0, height, z0],
              [x0, height, z1],
              [x1, height, z1],
              [x1, height, z0],
              [0, 1, 0],
              topColor,
              tile.top,
              false,
            );
            const west = neighborAt(gx - 1, gz);
            const east = neighborAt(gx + 1, gz);
            const north = neighborAt(gx, gz - 1);
            const south = neighborAt(gx, gz + 1);
            if (west === null || height > west) {
              addActualWall(gx, gz, x0, x0, z0, z1, height, west, [-1, 0, 0]);
            }
            if (east === null || height > east) {
              addActualWall(gx, gz, x1, x1, z0, z1, height, east, [1, 0, 0]);
            }
            if (north === null || height > north) {
              addActualWall(gx, gz, x0, x1, z0, z0, height, north, [0, 0, -1]);
            }
            if (south === null || height > south) {
              addActualWall(gx, gz, x0, x1, z1, z1, height, south, [0, 0, 1]);
            }
          } else {
            const n0 = normalAt(gx, gz);
            const n1 = normalAt(gx, gz + 1);
            const n2 = normalAt(gx + 1, gz + 1);
            const n3 = normalAt(gx + 1, gz);
            addQuad(
              [x0, heightAt(gx, gz), z0],
              [x0, heightAt(gx, gz + 1), z1],
              [x1, heightAt(gx + 1, gz + 1), z1],
              [x1, heightAt(gx + 1, gz), z0],
              [0, 1, 0],
              topColor,
              tile.top,
              false,
            );
            const topStart = positions.length / 3 - 4;
            normals[topStart * 3] = n0[0];
            normals[topStart * 3 + 1] = n0[1];
            normals[topStart * 3 + 2] = n0[2];
            normals[(topStart + 1) * 3] = n1[0];
            normals[(topStart + 1) * 3 + 1] = n1[1];
            normals[(topStart + 1) * 3 + 2] = n1[2];
            normals[(topStart + 2) * 3] = n2[0];
            normals[(topStart + 2) * 3 + 1] = n2[1];
            normals[(topStart + 2) * 3 + 2] = n2[2];
            normals[(topStart + 3) * 3] = n3[0];
            normals[(topStart + 3) * 3 + 1] = n3[1];
            normals[(topStart + 3) * 3 + 2] = n3[2];
          }
        }
      }

      if (!actualMode) {
        if (originZ === 0 && gz0 === 0) {
          for (let gx = gx0; gx < gx1; gx++) {
            const x0 = gx * step;
            const x1 = Math.min(spanW, (gx + 1) * step);
            addEdge(gx, 0, x0, x1, 0, 0, heightAt(gx, 0), heightAt(gx + 1, 0), [0, 0, -1]);
          }
        }
        if (originZ + gridD === depth && gz1 === gridD) {
          for (let gx = gx0; gx < gx1; gx++) {
            const x0 = gx * step;
            const x1 = Math.min(spanW, (gx + 1) * step);
            addEdge(gx, gridD - 1, x0, x1, localSouth, localSouth, heightAt(gx, gridD), heightAt(gx + 1, gridD), [0, 0, 1]);
          }
        }
        if (originX === 0 && gx0 === 0) {
          for (let gz = gz0; gz < gz1; gz++) {
            const z0 = gz * step;
            const z1 = Math.min(spanD, (gz + 1) * step);
            addEdge(0, gz, 0, 0, z0, z1, heightAt(0, gz), heightAt(0, gz + 1), [-1, 0, 0]);
          }
        }
        if (originX + gridW === width && gx1 === gridW) {
          for (let gz = gz0; gz < gz1; gz++) {
            const z0 = gz * step;
            const z1 = Math.min(spanD, (gz + 1) * step);
            addEdge(gridW - 1, gz, localEast, localEast, z0, z1, heightAt(gridW, gz), heightAt(gridW, gz + 1), [1, 0, 0]);
          }
        }
      }

      const geometry = new THREE.BufferGeometry();
      geometry.setAttribute("position", new THREE.Float32BufferAttribute(positions, 3));
      geometry.setAttribute("color", new THREE.Float32BufferAttribute(colors, 3));
      geometry.setAttribute("uv", new THREE.Float32BufferAttribute(uvs, 2));
      geometry.setAttribute("normal", new THREE.Float32BufferAttribute(normals, 3));
      geometry.setIndex(indices);
      geometry.computeBoundingSphere();
      return geometry;
    };

    const chunkSize = Math.max(1, this.terrainChunkSize || 32);
    const chunkCols = Math.ceil(gridW / chunkSize);
    const chunkRows = Math.ceil(gridD / chunkSize);
    this.terrainChunkCols = chunkCols;
    this.terrainChunkRows = chunkRows;
    if (full) {
      for (const mesh of this.terrainMeshes.values()) {
        this.terrainGroup.remove(mesh);
        mesh.geometry.dispose();
      }
      this.terrainMeshes.clear();
    }
    const keys = [];
    if (full) {
      for (let cx = 0; cx < chunkCols; cx++) {
        for (let cz = 0; cz < chunkRows; cz++) {
          keys.push(`${cx},${cz}`);
        }
      }
    } else {
      for (const key of dirtyChunks) {
        keys.push(key);
      }
    }
    for (const key of keys) {
      const [cx, cz] = key.split(",").map(Number);
      if (!Number.isInteger(cx) || !Number.isInteger(cz)
          || cx < 0 || cz < 0 || cx >= chunkCols || cz >= chunkRows) {
        continue;
      }
      const gx0 = cx * chunkSize;
      const gx1 = Math.min(gridW, gx0 + chunkSize);
      const gz0 = cz * chunkSize;
      const gz1 = Math.min(gridD, gz0 + chunkSize);
      if (gx0 >= gx1 || gz0 >= gz1) {
        continue;
      }
      const geometry = buildChunkGeometry(gx0, gx1, gz0, gz1);
      const existing = this.terrainMeshes.get(key);
      if (existing) {
        existing.geometry.dispose();
        existing.geometry = geometry;
      } else {
        const mesh = new THREE.Mesh(geometry, this.terrainMaterial);
        mesh.frustumCulled = false;
        mesh.castShadow = true;
        mesh.receiveShadow = true;
        this.terrainGroup.add(mesh);
        this.terrainMeshes.set(key, mesh);
      }
    }

    const waterPositions = [];
    const waterIndices = [];
    const surfacePalette = map.surface_palette ?? [];
    for (let gz = 0; gz < gridD; gz++) {
      for (let gx = 0; gx < gridW; gx++) {
        const x = Math.min(width - 1, Math.max(0, originX + gx * step + Math.floor(step / 2)));
        const z = Math.min(depth - 1, Math.max(0, originZ + gz * step + Math.floor(step / 2)));
        const surfaceIndex = this.surfaces?.[z * width + x];
        const surfaceId = surfacePalette[surfaceIndex];
        if (surfaceId !== "minecraft:water") {
          continue;
        }
        const x0 = gx * step;
        const x1 = Math.min(width, (gx + 1) * step);
        const z0 = gz * step;
        const z1 = Math.min(depth, (gz + 1) * step);
        const start = waterPositions.length / 3;
        const y = heights[z * width + x] + 0.03;
        waterPositions.push(x0, y, z0, x0, y, z1, x1, y, z1, x1, y, z0);
        waterIndices.push(start, start + 1, start + 2, start, start + 2, start + 3);
      }
    }
    this.water.geometry.dispose();
    this.water.geometry = new THREE.BufferGeometry();
    this.water.geometry.setAttribute("position", new THREE.Float32BufferAttribute(waterPositions, 3));
    this.water.geometry.setIndex(waterIndices);
    this.water.geometry.computeVertexNormals();
    this.water.position.set(originX, 0, originZ);
    this.water.scale.set(1, 1, 1);
    this.water.visible = state.water;

    if (full) {
      this.rebuildAnchors(state, step);
      this.rebuildStructures(state);
      this.lastAnchors = JSON.stringify(map.anchors ?? []);
    }

    if (fullBlocks) {
      if (full) {
        this.rebuildBackground(state, colorForCell);
      }
      this.backgroundGroup.visible = true;
    } else {
      this.backgroundGroup.visible = false;
    }
  }

  rebuildBackground(state, colorForCell) {
    const map = this.map;
    const width = map.width;
    const depth = map.depth;
    const step = Math.max(16, Math.ceil(Math.max(width, depth) / 256));
    const gridW = Math.ceil(width / step);
    const gridD = Math.ceil(depth / step);
    const key = `${state.layer}:${width}x${depth}:${this.heights.length}:${step}`;
    if (this.backgroundKey === key && this.backgroundMesh) {
      return;
    }
    const positions = [];
    const colors = [];
    const indices = [];
    for (let gz = 0; gz < gridD; gz++) {
      for (let gx = 0; gx < gridW; gx++) {
        const x = Math.min(width - 1, gx * step + Math.floor(step / 2));
        const z = Math.min(depth - 1, gz * step + Math.floor(step / 2));
        const index = z * width + x;
        const height = this.heights[index];
        if (height === -32768) {
          continue;
        }
        const x0 = gx * step;
        const x1 = Math.min(width, (gx + 1) * step);
        const z0 = gz * step;
        const z1 = Math.min(depth, (gz + 1) * step);
        const rgb = colorForCell(index, state.layer, state);
        const color = colorToLinear(rgb);
        const start = positions.length / 3;
        positions.push(x0, height, z0, x0, height, z1, x1, height, z1, x1, height, z0);
        for (let i = 0; i < 4; i++) {
          colors.push(color.r, color.g, color.b);
        }
        indices.push(start, start + 1, start + 2, start, start + 2, start + 3);
      }
    }
    const geometry = new THREE.BufferGeometry();
    geometry.setAttribute("position", new THREE.Float32BufferAttribute(positions, 3));
    geometry.setAttribute("color", new THREE.Float32BufferAttribute(colors, 3));
    geometry.setIndex(indices);
    geometry.computeVertexNormals();
    if (this.backgroundMesh) {
      this.backgroundGroup.remove(this.backgroundMesh);
      this.backgroundMesh.geometry.dispose();
    }
    this.backgroundMesh = new THREE.Mesh(geometry, this.backgroundMaterial);
    this.backgroundMesh.position.y = 0;
    this.backgroundMesh.renderOrder = -2;
    this.backgroundMesh.frustumCulled = false;
    this.backgroundGroup.add(this.backgroundMesh);
    this.backgroundKey = key;
  }

  rebuildStructures(state) {
    const anchors = this.map?.anchors ?? [];
    const key = JSON.stringify(anchors.map((anchor) => [
      anchor.template,
      anchor.pos,
      anchor.rotation ?? "none",
      anchor.mirror ?? "none",
      anchor.integrity ?? 1,
      anchor.scale ?? 1,
    ]));
    if (key === this.structureKey) {
      return;
    }
    this.structureKey = key;
    const token = ++this.structureToken;
    this.structureGroup.clear();
    if (anchors.length === 0) {
      return;
    }
    this.loadStructures(anchors, token).catch((error) => {
      console.warn("结构模型加载失败", error);
    });
  }

  async loadStructures(anchors, token) {
    for (const anchor of anchors) {
      if (token !== this.structureToken) {
        return;
      }
      if (!anchor || !anchor.template || !Array.isArray(anchor.pos)) {
        continue;
      }
      const x = anchor.pos[0] - this.map.origin[0];
      const z = anchor.pos[2] - this.map.origin[1];
      if (x < 0 || z < 0 || x >= this.map.width || z >= this.map.depth) {
        continue;
      }
      const index = Math.floor(z) * this.map.width + Math.floor(x);
      const height = this.heights[index];
      if (height === -32768) {
        continue;
      }
      let source;
      try {
        source = await this.getTemplateGroup(anchor.template);
      } catch (error) {
        console.warn(`结构 ${anchor.template} 加载失败`, error);
        continue;
      }
      if (token !== this.structureToken) {
        return;
      }
      const group = source.clone(true);
      group.position.set(0, 0, 0);
      const pivot = new THREE.Group();
      pivot.add(group);
      const rotation = String(anchor.rotation ?? "none").toLowerCase();
      if (rotation === "clockwise_90") {
        pivot.rotation.y = -Math.PI / 2;
      } else if (rotation === "counterclockwise_90") {
        pivot.rotation.y = Math.PI / 2;
      } else if (rotation === "180" || rotation === "clockwise_180") {
        pivot.rotation.y = Math.PI;
      }
      const mirror = String(anchor.mirror ?? "none").toLowerCase();
      if (mirror === "left_right") {
        pivot.scale.x = -1;
      } else if (mirror === "front_back") {
        pivot.scale.z = -1;
      }
      pivot.updateMatrixWorld(true);
      const bounds = new THREE.Box3().setFromObject(pivot);
      const baseY = Number.isFinite(anchor.pos[1]) ? anchor.pos[1] : height;
      const scale = Math.max(0.25, Math.min(4, Number(anchor.scale) || 1));
      pivot.scale.multiplyScalar(scale);
      pivot.position.set(x - bounds.min.x * scale, this.displayY(baseY), z - bounds.min.z * scale);
      this.structureGroup.add(pivot);
      this.render();
    }
  }

  getTemplateGroup(template) {
    if (!this.structureTemplates.has(template)) {
      this.structureTemplates.set(template, this.buildTemplateGroup(template));
    }
    return this.structureTemplates.get(template);
  }

  async buildTemplateGroup(template) {
    const response = await fetch(`/api/template?id=${encodeURIComponent(template)}`, { cache: "no-store" });
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const data = await response.json();
    const group = new THREE.Group();
    const blockGroups = new Map();
    for (const block of data.blocks ?? []) {
      const blockId = String(block.block ?? "");
      const key = blockId.split("[")[0].split(":").pop();
      if (key === "air" || key === "cave_air" || key === "void_air" || key === "structure_void") {
        continue;
      }
      if (!Array.isArray(block.pos) || block.pos.length !== 3) {
        continue;
      }
      const properties = block.properties && typeof block.properties === "object" ? block.properties : {};
      const cacheKey = `${blockId}|${Object.keys(properties).sort().map((name) => `${name}=${properties[name]}`).join(",")}`;
      let blockGroup = blockGroups.get(cacheKey);
      if (!blockGroup) {
        blockGroup = await this.structureLoader.buildGroup(blockId, 0x8b8b8b, null, properties);
        blockGroups.set(cacheKey, blockGroup);
      }
      const clone = blockGroup.clone(true);
      clone.position.set(Number(block.pos[0]), Number(block.pos[1]), Number(block.pos[2]));
      clone.userData = { blockId, properties };
      group.add(clone);
    }
    for (const entity of data.entities ?? []) {
      if (!Array.isArray(entity.pos) || entity.pos.length !== 3) {
        continue;
      }
      const marker = new THREE.Mesh(this.entityMarkerGeometry, this.entityMarkerMaterial);
      marker.position.set(Number(entity.pos[0]), Number(entity.pos[1]), Number(entity.pos[2]));
      marker.renderOrder = 5;
      marker.userData = { entityId: entity.id || "" };
      group.add(marker);
    }
    return group;
  }

  async updatePlacementStructure(state) {
    const movingAnchor = state.movingAnchorId
      ? this.map?.anchors?.find((anchor) => anchor.id === state.movingAnchorId)
      : null;
    const template = state.placement?.template ?? movingAnchor?.template;
    if (!template || !state.cursor || state.undergroundMode) {
      this.placementStructureGroup.visible = false;
      return;
    }
    const cell = state.cursor;
    const index = cell.z * this.map.width + cell.x;
    const height = this.heights[index];
    if (height === -32768) {
      this.placementStructureGroup.visible = false;
      return;
    }
    if (template !== this.placementStructureTemplate) {
      this.placementStructureTemplate = template;
      const token = ++this.placementStructureToken;
      this.placementStructureGroup.clear();
      this.placementStructureGroup.visible = false;
      try {
        const source = await this.getTemplateGroup(template);
        if (token !== this.placementStructureToken || this.placementStructureTemplate !== template) {
          return;
        }
        this.placementStructureGroup.add(source.clone(true));
      } catch (error) {
        console.warn(`放置预览 ${template} 加载失败`, error);
        return;
      }
    }
    this.placementStructureGroup.position.set(
      cell.x,
      this.displayY(height + (template.split(":").pop().startsWith("tree_") || template.split(":").pop().startsWith("deco_") ? 1 : 0)),
      cell.z,
    );
    this.placementStructureGroup.visible = true;
    this.render();
  }
  rebuildAnchors(state, step) {
    this.anchorGroup.clear();
    const markerScale = Math.max(0.75, Math.min(5, step * 0.35));
    for (const anchor of this.map.anchors ?? []) {
      if (!Array.isArray(anchor.pos)) {
        continue;
      }
      const x = anchor.pos[0] - this.map.origin[0];
      const z = anchor.pos[2] - this.map.origin[1];
      if (x < 0 || z < 0 || x >= this.map.width || z >= this.map.depth) {
        continue;
      }
      const index = Math.floor(z) * this.map.width + Math.floor(x);
      const height = this.heights[index];
      if (height === -32768) {
        continue;
      }
      const baseY = Number.isFinite(anchor.pos[1]) ? anchor.pos[1] : height;
      const displayBaseY = this.displayY(baseY);
      const marker = new THREE.Mesh(
        this.anchorGeometry,
        this.selectedAnchorIds.has(anchor.id) ? this.selectedAnchorMaterial : this.anchorMaterial,
      );
      marker.userData = { anchorId: anchor.id, handle: "move" };
      marker.position.set(x + 0.5, displayBaseY + markerScale * 1.5, z + 0.5);
      marker.scale.setScalar(this.selectedAnchorIds.has(anchor.id) ? markerScale * 1.22 : markerScale);
      this.anchorGroup.add(marker);
      const anchorScaleValue = Math.max(0.25, Math.min(4, Number(anchor.scale) || 1));
      let size = state.templateSizes?.[anchor.template];
      if (Array.isArray(size) && size.length === 3) {
        size = size.map((value) => value * anchorScaleValue);
        const rotation = String(anchor.rotation ?? "none").toLowerCase();
        if (rotation === "clockwise_90" || rotation === "counterclockwise_90") {
          size = [size[2], size[1], size[0]];
        }
        const edges = new THREE.LineSegments(
          new THREE.EdgesGeometry(new THREE.BoxGeometry(size[0], size[1], size[2])),
          this.selectedAnchorIds.has(anchor.id) ? this.selectedFootprintMaterial : this.footprintMaterial,
        );
        edges.userData = { anchorId: anchor.id, handle: "move" };
        edges.position.set(x + size[0] / 2, displayBaseY + size[1] / 2, z + size[2] / 2);
        this.anchorGroup.add(edges);
      }
      if (this.selectedAnchorIds.has(anchor.id)) {
        const ringRadius = Math.max(1.2, markerScale * 1.7);
        const ring = new THREE.Mesh(this.anchorRotateRingGeometry, this.anchorRotateRingMaterial);
        ring.rotation.x = -Math.PI / 2;
        ring.position.set(x + 0.5, displayBaseY + 0.22, z + 0.5);
        ring.scale.setScalar(ringRadius);
        ring.renderOrder = 6;
        ring.userData = { anchorId: anchor.id, handle: "rotate" };
        this.anchorGroup.add(ring);

        const scaleHandle = new THREE.Mesh(this.anchorScaleGeometry, this.anchorScaleMaterial);
        scaleHandle.position.set(x + 0.5 + ringRadius * 1.3, displayBaseY + 0.35, z + 0.5);
        scaleHandle.scale.setScalar(Math.max(1.0, markerScale * 1.1));
        scaleHandle.renderOrder = 7;
        scaleHandle.userData = { anchorId: anchor.id, handle: "scale" };
        this.anchorGroup.add(scaleHandle);

        const yGroup = new THREE.Group();
        yGroup.position.set(x + 0.5, displayBaseY + markerScale * 1.6 + 1.8, z + 0.5);
        const shaft = new THREE.Mesh(this.anchorYShaftGeometry, this.anchorYMaterial);
        const tip = new THREE.Mesh(this.anchorYTipGeometry, this.anchorYMaterial);
        tip.position.y = 0.9;
        shaft.renderOrder = 6;
        tip.renderOrder = 6;
        yGroup.add(shaft, tip);
        yGroup.userData = { anchorId: anchor.id, handle: "y" };
        this.anchorGroup.add(yGroup);
      }
    }
  }

  updateUnderground(state) {
    const enabled = Boolean(state.undergroundMode);
    this.terrainGroup.visible = !enabled;
    this.water.visible = !enabled && Boolean(state.water);
    this.anchorGroup.visible = !enabled;
    this.structureGroup.visible = !enabled;
    this.backgroundGroup.visible = !enabled && this.fullBlocks && Boolean(this.backgroundMesh);
    this.undergroundPlane.visible = enabled;
    this.undergroundGrid.visible = enabled;
    this.undergroundGroup.visible = enabled;
    if (!enabled) {
      this.undergroundKey = null;
      this.undergroundRangeBox.visible = false;
      this.undergroundBrushBox.visible = false;
      this.undergroundTruncated = false;
      return;
    }
    const width = this.map.width;
    const depth = this.map.depth;
    const y = this.undergroundY;
    const yMin = Math.min(this.undergroundYMin, this.undergroundYMax);
    const yMax = Math.max(this.undergroundYMin, this.undergroundYMax);
    this.undergroundPlane.position.set(width / 2, y + 0.02, depth / 2);
    this.undergroundPlane.scale.set(width, depth, 1);
    this.undergroundGrid.position.set(width / 2, y + 0.04, depth / 2);
    this.undergroundGrid.scale.set(width, 1, depth);
    this.undergroundRangeBox.visible = yMax > yMin;
    if (this.undergroundRangeBox.visible) {
      this.undergroundRangeBox.position.set(width / 2, (yMin + yMax + 1) / 2, depth / 2);
      this.undergroundRangeBox.scale.set(width, yMax - yMin + 1, depth);
    }
    const key = `${y}:${yMin}:${yMax}:${state.undergroundRevision ?? 0}:${state.underground?.size ?? 0}`;
    if (key === this.undergroundKey) {
      return;
    }
    this.undergroundKey = key;
    this.undergroundGroup.clear();
    const entries = [];
    for (const [cellKey, blockId] of state.underground ?? []) {
      const [x, blockY, z] = cellKey.split(",").map(Number);
      if (blockY < yMin || blockY > yMax || x < 0 || z < 0 || x >= width || z >= depth) {
        continue;
      }
      entries.push({ x, y: blockY, z, blockId });
    }
    const limit = 6000;
    this.undergroundTruncated = entries.length > limit;
    const candidates = this.undergroundTruncated
      ? entries.filter((entry) => entry.y === y)
      : entries;
    if (candidates.length > limit) {
      this.undergroundTruncated = true;
    }
    for (const entry of candidates.slice(0, limit)) {
      const material = this.undergroundMaterial(state, entry.blockId);
      const mesh = new THREE.Mesh(this.undergroundCubeGeometry, material);
      mesh.position.set(entry.x + 0.5, entry.y + 0.5, entry.z + 0.5);
      this.undergroundGroup.add(mesh);
    }
  }

  undergroundMaterial(state, blockId) {
    const id = String(blockId ?? "minecraft:air");
    if (this.undergroundMaterials.has(id)) {
      return this.undergroundMaterials.get(id);
    }
    const baseId = id.split("[")[0];
    const key = baseId.split(":").pop();
    const entry = state.blockById?.get(baseId);
    let color = 0x8a8a8a;
    if (typeof entry?.color === "string" && /^#[0-9a-f]{6}$/i.test(entry.color)) {
      color = Number.parseInt(entry.color.slice(1), 16);
    } else if (key === "water") {
      color = 0x3f76e4;
    } else if (key === "air" || key === "cave_air" || key === "void_air") {
      color = 0x0b0f14;
    }
    const isAir = key === "air" || key === "cave_air" || key === "void_air";
    const material = new THREE.MeshStandardMaterial({
      color,
      roughness: 0.92,
      metalness: 0,
      transparent: isAir,
      opacity: isAir ? 0.28 : 1,
      depthWrite: !isAir,
    });
    this.undergroundMaterials.set(id, material);
    return material;
  }

  updateRemoteCursors(state) {
    const cursors = state.collab?.cursors;
    const active = new Set();
    if (cursors instanceof Map && !state.undergroundMode) {
      for (const [id, cursor] of cursors) {
        if (!cursor || !Number.isFinite(cursor.x) || !Number.isFinite(cursor.z)) {
          continue;
        }
        if (Date.now() - Number(cursor.at ?? 0) > 60000) {
          continue;
        }
        const x = Math.floor(cursor.x);
        const z = Math.floor(cursor.z);
        if (x < 0 || z < 0 || x >= this.map.width || z >= this.map.depth) {
          continue;
        }
        const height = this.heights[z * this.map.width + x];
        if (height === -32768) {
          continue;
        }
        active.add(id);
        let marker = this.remoteCursors.get(id);
        if (!marker) {
          const material = new THREE.MeshBasicMaterial({
            color: 0x7bd88f,
            transparent: true,
            opacity: 0.72,
            depthTest: false,
          });
          const ringMaterial = new THREE.MeshBasicMaterial({
            color: 0x7bd88f,
            transparent: true,
            opacity: 0.9,
            depthTest: false,
            side: THREE.DoubleSide,
          });
          const group = new THREE.Group();
          const beam = new THREE.Mesh(this.remoteCursorGeometry, material);
          beam.position.y = 1.5;
          const ring = new THREE.Mesh(this.remoteCursorRingGeometry, ringMaterial);
          ring.rotation.x = -Math.PI / 2;
          ring.position.y = 0.06;
          group.add(beam, ring);
          this.remoteCursorGroup.add(group);
          marker = { group, material, ringMaterial };
          this.remoteCursors.set(id, marker);
        }
        marker.group.position.set(x + 0.5, this.displayY(height), z + 0.5);
        try {
          const color = new THREE.Color().setStyle(cursor.color || "#7bd88f");
          marker.material.color.copy(color);
          marker.ringMaterial.color.copy(color);
        } catch {
          // Ignore malformed remote colors.
        }
      }
    }
    for (const [id, marker] of this.remoteCursors) {
      if (!active.has(id)) {
        this.remoteCursorGroup.remove(marker.group);
        marker.material.dispose();
        marker.ringMaterial.dispose();
        this.remoteCursors.delete(id);
      }
    }
    this.remoteCursorGroup.visible = active.size > 0;
  }

  updateOverlays(state) {
    const cell = state.cursor;
    const structureMode = state.toolPage === "objects" && !state.movingAnchorId && !state.placement && !state.forestMode;
    this.cursorOutline.visible = false;
    if (structureMode) {
      this.cursor.visible = false;
    } else if (state.viewMode === "3d" && cell) {
      if (state.undergroundMode) {
        this.cursor.visible = true;
        this.cursor.position.set(cell.x + 0.5, state.undergroundY + 0.42, cell.z + 0.5);
        this.cursor.scale.setScalar(Math.max(1.2, this.step * 0.9));
        this.cursorMaterial.color.set(state.undergroundClear ? 0xff8a8a : 0x9bdcff);
      } else if (cell.point) {
        this.cursor.visible = false;
        this.updateCursorOutline(state, cell);
      } else {
        const index = cell.z * this.map.width + cell.x;
        const height = this.heights[index];
        if (height !== -32768) {
          this.cursor.visible = true;
          this.cursor.position.set(cell.x + this.step / 2, height + 0.35, cell.z + this.step / 2);
          this.cursor.scale.setScalar(Math.max(1.2, this.step * 0.9));
          this.cursorMaterial.color.set(state.forestMode ? 0x7bd88f : state.movingAnchorId ? 0x7bd88f : state.placement ? 0xffd479 : 0xf6fbff);
        } else {
          this.cursor.visible = false;
        }
      }
    } else {
      this.cursor.visible = false;
    }

    if (state.undergroundMode && cell) {
      const yMin = Math.min(this.undergroundYMin, this.undergroundYMax);
      const yMax = Math.max(this.undergroundYMin, this.undergroundYMax);
      const radius = Math.max(0, Number(state.brushSize ?? 0));
      const size = radius * 2 + 1;
      const height = yMax - yMin + 1;
      this.undergroundBrushBox.visible = true;
      this.undergroundBrushBox.position.set(cell.x + 0.5, yMin + height / 2, cell.z + 0.5);
      this.undergroundBrushBox.scale.set(size, height, size);
      this.undergroundBrushMaterial.color.set(state.undergroundClear ? 0xff8a8a : 0xffd479);
    } else {
      this.undergroundBrushBox.visible = false;
    }

    const movingAnchor = state.movingAnchorId
      ? this.map.anchors?.find((anchor) => anchor.id === state.movingAnchorId)
      : null;
    const previewTemplate = state.placement?.template ?? movingAnchor?.template;
    const placementSize = previewTemplate ? state.templateSizes?.[previewTemplate] : null;
    if (!state.undergroundMode && (state.placement || movingAnchor) && cell && Array.isArray(placementSize) && placementSize.length === 3) {
      const index = cell.z * this.map.width + cell.x;
      const height = this.heights[index];
      if (height !== -32768) {
        const key = `${previewTemplate}:${movingAnchor ? "move" : "place"}:${cell.x}:${cell.z}`;
        if (this.lastPlacementKey !== key) {
          this.placementFootprint.geometry.dispose();
          this.placementFootprint.geometry = new THREE.EdgesGeometry(
            new THREE.BoxGeometry(placementSize[0], placementSize[1], placementSize[2]),
          );
          this.placementFootprint.position.set(
            cell.x + placementSize[0] / 2,
            height + placementSize[1] / 2,
            cell.z + placementSize[2] / 2,
          );
          this.footprintMaterial.color.set(movingAnchor ? 0x7bd88f : 0xffd479);
          this.lastPlacementKey = key;
        }
        this.placementFootprint.visible = true;
      } else {
        this.placementFootprint.visible = false;
      }
    } else {
      this.placementFootprint.visible = false;
      this.lastPlacementKey = null;
      this.footprintMaterial.color.set(0xffd479);
    }

    const activeRect = state.rect ?? state.selection;
    if (activeRect) {
      const minX = Math.min(activeRect.start.x, activeRect.end.x);
      const maxX = Math.max(activeRect.start.x, activeRect.end.x);
      const minZ = Math.min(activeRect.start.z, activeRect.end.z);
      const maxZ = Math.max(activeRect.start.z, activeRect.end.z);
      let maxHeight = this.map.min_y;
      for (let z = minZ; z <= maxZ; z += Math.max(1, this.step)) {
        for (let x = minX; x <= maxX; x += Math.max(1, this.step)) {
          const idx = Math.min(this.map.depth - 1, z) * this.map.width + Math.min(this.map.width - 1, x);
          const h = this.heights[idx];
          if (h !== -32768) {
            maxHeight = Math.max(maxHeight, h);
          }
        }
      }
      const y = maxHeight + 0.5;
      const points = [
        new THREE.Vector3(minX, y, minZ),
        new THREE.Vector3(maxX + 1, y, minZ),
        new THREE.Vector3(maxX + 1, y, maxZ + 1),
        new THREE.Vector3(minX, y, maxZ + 1),
      ];
      this.rectLine.geometry.dispose();
      this.rectLine.geometry = new THREE.BufferGeometry().setFromPoints(points);
      this.rectMaterial.color.set(state.selection && !state.rect ? 0x7bd88f : 0x78c8ff);
      this.rectLine.visible = true;
    } else {
      this.rectLine.visible = false;
    }
    this.updateRemoteCursors(state);
  }

  updateCamera(view, state) {
    const width = this.map.width;
    const depth = this.map.depth;
    const xzScale = this.xzScale || 1;
    const border = this.borderGroup.visible && !this.fullBlocks && !state.undergroundMode ? (this.borderWidth || 0) : 0;
    const viewWidth = this.fullBlocks ? Math.max(1, this.fullWindowW) : width + border * 2;
    const viewDepth = this.fullBlocks ? Math.max(1, this.fullWindowD) : depth + border * 2;
    const minHeight = border ? Math.min(state.heightMin, this.borderMinHeight ?? state.heightMin) : state.heightMin;
    const maxHeight = border ? Math.max(state.heightMax, this.borderMaxHeight ?? state.heightMax) : state.heightMax;
    const heightScale = state.undergroundMode ? 1 : this.yScale;
    const range = Math.max(1, (maxHeight - minHeight) * heightScale);
    const fit = Math.max(viewWidth * xzScale, viewDepth * xzScale, range * 2, 32);
    const targetX = (view.targetX ?? width / 2) * xzScale;
    const targetZ = (view.targetZ ?? depth / 2) * xzScale;
    const targetY = state.undergroundMode ? state.undergroundY : this.displayY((minHeight + maxHeight) * 0.5);
    const distance = (fit * 0.72) / Math.tan(THREE.MathUtils.degToRad(FOV) / 2) / Math.max(0.2, view.zoom);
    view.lastScale = this.canvas.height / (2 * distance * Math.tan(THREE.MathUtils.degToRad(FOV) / 2));
    const cosPitch = Math.cos(view.pitch);
    const sinPitch = Math.sin(view.pitch);
    this.camera.position.set(
      targetX + Math.sin(view.yaw) * cosPitch * distance,
      targetY + sinPitch * distance,
      targetZ + Math.cos(view.yaw) * cosPitch * distance,
    );
    this.camera.lookAt(targetX, targetY, targetZ);
    // Far/fog must follow the displayed extent, not the camera distance.
    // Otherwise high zoom (especially 100x) puts the fog wall only a few blocks
    // away and distant terrain turns black.
    const extent = this.fullBlocks
      ? Math.max(width, depth, range, 32)
      : Math.max(viewWidth, viewDepth, range, 32);
    this.camera.near = Math.max(0.1, distance / 2000);
    this.camera.far = Math.max(1000, distance * 12, extent * 4);
    this.camera.updateProjectionMatrix();
    this.scene.fog.near = Math.max(distance * 2, extent * 0.75);
    this.scene.fog.far = Math.max(distance * 4.2, extent * 3.5);
  }

  viewFootprint(state) {
    if (!this.map || !state?.view3D) {
      return null;
    }
    const width = this.map.width;
    const depth = this.map.depth;
    const viewWidth = this.fullBlocks ? Math.max(1, this.fullWindowW) : width;
    const viewDepth = this.fullBlocks ? Math.max(1, this.fullWindowD) : depth;
    const zoom = Math.max(0.05, Number(state.view3D.zoom) || 1);
    const fit = Math.max(viewWidth, viewDepth, 1);
    const aspect = Math.max(0.2, Number(this.camera?.aspect) || 1);
    const pitch = Math.max(0.12, Math.min(1.45, Number(state.view3D.pitch) || 0.55));
    const sinPitch = Math.max(0.2, Math.sin(pitch));
    const baseHalf = Math.min(fit, Math.max(viewWidth, viewDepth)) / (2 * zoom);
    const halfWidth = Math.min(viewWidth, fit * aspect) / (2 * zoom);

    // WC3-like finite view trapezoid: the near edge is narrower and closer;
    // the far edge grows as the camera pitches toward the horizon.
    const nearDistance = baseHalf * (0.55 - 0.2 * sinPitch);
    const farDistance = baseHalf * Math.min(1.8, 0.45 + 0.55 / sinPitch);
    const nearHalfWidth = halfWidth * (0.8 + 0.05 * sinPitch);
    const farHalfWidth = halfWidth * Math.min(1.8, 0.9 + 0.4 / sinPitch);

    const targetX = Number(state.view3D.targetX) || 0;
    const targetZ = Number(state.view3D.targetZ) || 0;
    const yaw = Number(state.view3D.yaw) || 0;
    // Camera sits at target + (sin(yaw), cos(yaw)) * distance. The view
    // forward direction on the ground is away from the camera.
    const forwardX = -Math.sin(yaw);
    const forwardZ = -Math.cos(yaw);
    const rightX = Math.cos(yaw);
    const rightZ = -Math.sin(yaw);
    const nearX = targetX - forwardX * nearDistance;
    const nearZ = targetZ - forwardZ * nearDistance;
    const farX = targetX + forwardX * farDistance;
    const farZ = targetZ + forwardZ * farDistance;
    return [
      { x: nearX - rightX * nearHalfWidth, z: nearZ - rightZ * nearHalfWidth },
      { x: nearX + rightX * nearHalfWidth, z: nearZ + rightZ * nearHalfWidth },
      { x: farX + rightX * farHalfWidth, z: farZ + rightZ * farHalfWidth },
      { x: farX - rightX * farHalfWidth, z: farZ - rightZ * farHalfWidth },
    ];
  }

  projectAnchor(anchor) {
    if (!this.map || !anchor || !Array.isArray(anchor.pos)) {
      return null;
    }
    const x = anchor.pos[0] - this.map.origin[0];
    const z = anchor.pos[2] - this.map.origin[1];
    if (x < 0 || z < 0 || x >= this.map.width || z >= this.map.depth) {
      return null;
    }
    const height = this.heights[Math.floor(z) * this.map.width + Math.floor(x)];
    if (height === -32768) {
      return null;
    }
    const baseY = Number.isFinite(anchor.pos[1]) ? anchor.pos[1] : height;
    this.camera.updateMatrixWorld();
    this.camera.matrixWorldInverse.copy(this.camera.matrixWorld).invert();
    const xzScale = this.xzScale || 1;
    const vector = new THREE.Vector3((x + 0.5) * xzScale, this.displayY(baseY) + 1, (z + 0.5) * xzScale).project(this.camera);
    const rect = this.canvas.getBoundingClientRect();
    return {
      x: rect.left + (vector.x + 1) * 0.5 * rect.width,
      y: rect.top + (1 - vector.y) * 0.5 * rect.height,
      visible: vector.z > -1 && vector.z < 1,
    };
  }

  heightAtDisplay(worldX, worldZ) {
    if (!this.displayHeights || !this.displayCols) {
      return null;
    }
    const localX = worldX - (this.gridOriginX || 0);
    const localZ = worldZ - (this.gridOriginZ || 0);
    if (this.actualMode && this.actualHeights) {
      const gx = THREE.MathUtils.clamp(Math.floor(localX / this.displayStep), 0, this.displayGridW - 1);
      const gz = THREE.MathUtils.clamp(Math.floor(localZ / this.displayStep), 0, this.displayGridD - 1);
      const height = this.actualHeights[gz * this.displayGridW + gx];
      return height === -32768 ? null : height;
    }
    const gx = THREE.MathUtils.clamp(localX / this.displayStep, 0, this.displayGridW);
    const gz = THREE.MathUtils.clamp(localZ / this.displayStep, 0, this.displayGridD);
    const x0 = Math.floor(gx);
    const z0 = Math.floor(gz);
    const x1 = Math.min(this.displayGridW, x0 + 1);
    const z1 = Math.min(this.displayGridD, z0 + 1);
    const tx = gx - x0;
    const tz = gz - z0;
    const value = (x, z) => this.displayHeights[z * this.displayCols + x];
    const a = value(x0, z0);
    const b = value(x1, z0);
    const c = value(x0, z1);
    const d = value(x1, z1);
    if (a === -32768 || b === -32768 || c === -32768 || d === -32768) {
      return null;
    }
    return (a * (1 - tx) + b * tx) * (1 - tz) + (c * (1 - tx) + d * tx) * tz;
  }

  updateCursorOutline(state, cell) {
    if (!cell?.point) {
      this.cursorOutline.visible = false;
      return;
    }
    const radius = Math.max(0.45, (state.brushSize + 0.5) * this.step);
    const points = [];
    const segments = 48;
    for (let i = 0; i < segments; i++) {
      const angle = (i / segments) * Math.PI * 2;
      const x = cell.point.x + Math.cos(angle) * radius;
      const z = cell.point.z + Math.sin(angle) * radius;
      const height = this.heightAtDisplay(x, z);
      points.push(new THREE.Vector3(x, (height ?? cell.point.y) + 0.12, z));
    }
    this.cursorOutline.geometry.dispose();
    this.cursorOutline.geometry = new THREE.BufferGeometry().setFromPoints(points);
    this.cursorOutlineMaterial.color.set(
      state.forestMode ? 0x7bd88f : state.placement ? 0xffd479 : state.movingAnchorId ? 0x7bd88f : 0xf6fbff,
    );
    this.cursorOutline.visible = true;
  }

  pick(event) {
    if (!this.map || !this.heights) {
      return null;
    }
    const rect = this.canvas.getBoundingClientRect();
    if (rect.width <= 0 || rect.height <= 0) {
      return null;
    }
    this.pointer.set(
      ((event.clientX - rect.left) / rect.width) * 2 - 1,
      -((event.clientY - rect.top) / rect.height) * 2 + 1,
    );
    this.raycaster.setFromCamera(this.pointer, this.camera);
    const target = this.undergroundMode ? this.undergroundPlane : this.terrainGroup;
    const hits = this.raycaster.intersectObject(target, !this.undergroundMode);
    if (hits.length === 0) {
      return null;
    }
    const worldPoint = hits[0].point;
    const group = this.undergroundMode ? this.horizontalGroup : this.worldGroup;
    group.updateWorldMatrix(true, false);
    const point = group.worldToLocal(worldPoint.clone());
    const x = Math.floor(point.x);
    const z = Math.floor(point.z);
    if (x < 0 || z < 0 || x >= this.map.width || z >= this.map.depth) {
      return null;
    }
    let normal = new THREE.Vector3(0, 1, 0);
    if (!this.undergroundMode && hits[0].face?.normal) {
      normal.copy(hits[0].face.normal).transformDirection(hits[0].object.matrixWorld);
    }
    const rawY = point.y;
    return {
      x,
      z,
      y: this.undergroundMode ? this.undergroundY : Math.floor(rawY),
      point: { x: point.x, y: rawY, z: point.z },
      normal: { x: normal.x, y: normal.y, z: normal.z },
    };
  }

  pickAnchor(event) {
    if (!this.map || !this.anchorGroup.visible) {
      return null;
    }
    const rect = this.canvas.getBoundingClientRect();
    if (rect.width <= 0 || rect.height <= 0) {
      return null;
    }
    this.pointer.set(
      ((event.clientX - rect.left) / rect.width) * 2 - 1,
      -((event.clientY - rect.top) / rect.height) * 2 + 1,
    );
    this.raycaster.setFromCamera(this.pointer, this.camera);
    this.raycaster.params.Line.threshold = 0.35;
    const hits = this.raycaster.intersectObject(this.anchorGroup, true);
    let best = null;
    for (const hit of hits) {
      let object = hit.object;
      while (object && object !== this.anchorGroup) {
        const data = object.userData;
        if (data && typeof data.anchorId === "string") {
          const handle = data.handle || "move";
          if (!best
              || (best.handle === "move" && handle !== "move")
              || (best.handle === handle && hit.distance < best.distance)) {
            best = {
              anchorId: data.anchorId,
              handle,
              distance: hit.distance,
              point: { x: hit.point.x, y: hit.point.y, z: hit.point.z },
            };
          }
          break;
        }
        object = object.parent;
      }
    }
    return best;
  }

  refreshAnchors(state) {
    if (!this.map) {
      return;
    }
    this.rebuildAnchors(state, this.step);
    this.rebuildStructures(state);
    this.lastAnchors = JSON.stringify(state.map?.anchors ?? []);
    this.updateOverlays(state);
  }

  render() {
    this.renderer.render(this.scene, this.camera);
  }
}
