import * as THREE from "./vendor/three.module.js";
import { BlockModelLoader } from "./blockmodels.js";

const DB_NAME = "gensokyo-editor-block-icons-v5";
const DB_STORE = "icons";
const ICON_SIZE = 64;

/**
 * Renders a block's Mojang model into a small isometric PNG and caches the
 * result in memory and IndexedDB. A single offscreen WebGL renderer is shared
 * by every icon so the inventory does not create one WebGL context per block.
 */
export class BlockIconRenderer {
  constructor(size = ICON_SIZE) {
    this.size = size;
    this.canvas = document.createElement("canvas");
    this.canvas.width = size;
    this.canvas.height = size;
    this.renderer = new THREE.WebGLRenderer({
      canvas: this.canvas,
      alpha: true,
      antialias: true,
      preserveDrawingBuffer: true,
    });
    this.renderer.setPixelRatio(1);
    this.renderer.setSize(size, size, false);
    this.renderer.outputColorSpace = THREE.SRGBColorSpace;
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
    this.renderer.toneMappingExposure = 1.1;
    this.renderer.setClearColor(0x000000, 0);
    this.scene = new THREE.Scene();
    this.camera = new THREE.OrthographicCamera(-1.13, 1.13, 1.13, -1.13, 0.05, 30);
    this.camera.position.set(2.4, 2.05, 3.0);
    this.camera.lookAt(0, 0, 0);
    this.scene.add(new THREE.AmbientLight(0xffffff, 0.8));
    this.scene.add(new THREE.HemisphereLight(0xffffff, 0x364049, 2.2));
    const top = new THREE.DirectionalLight(0xffffff, 1.8);
    top.position.set(0, 5, 0);
    this.scene.add(top);
    const sun = new THREE.DirectionalLight(0xfff5e0, 2.6);
    sun.position.set(2.0, 3.0, 2.5);
    this.scene.add(sun);
    const fill = new THREE.DirectionalLight(0x9fc4ff, 0.85);
    fill.position.set(-2.5, 1.0, -2.0);
    this.scene.add(fill);
    this.loader = new BlockModelLoader();
    this.cache = new Map();
    this.pending = new Map();
    this.queue = [];
    this.active = 0;
    this.maxActive = 2;
    this.rendered = 0;
    this.database = null;
    this.databaseReady = this.openDatabase();
  }

  openDatabase() {
    if (typeof indexedDB === "undefined") {
      return Promise.resolve(null);
    }
    return new Promise((resolve) => {
      try {
        const request = indexedDB.open(DB_NAME, 1);
        request.onupgradeneeded = () => {
          const db = request.result;
          if (!db.objectStoreNames.contains(DB_STORE)) {
            db.createObjectStore(DB_STORE);
          }
        };
        request.onsuccess = () => {
          this.database = request.result;
          resolve(this.database);
        };
        request.onerror = () => resolve(null);
      } catch {
        resolve(null);
      }
    });
  }

  async readDisk(blockId) {
    if (!this.databaseReady) {
      return null;
    }
    const db = await this.databaseReady;
    if (!db) {
      return null;
    }
    return new Promise((resolve) => {
      try {
        const request = db.transaction(DB_STORE, "readonly").objectStore(DB_STORE).get(blockId);
        request.onsuccess = () => resolve(typeof request.result === "string" ? request.result : null);
        request.onerror = () => resolve(null);
      } catch {
        resolve(null);
      }
    });
  }

  async writeDisk(blockId, dataUrl) {
    if (!this.databaseReady) {
      return;
    }
    const db = await this.databaseReady;
    if (!db) {
      return;
    }
    try {
      const transaction = db.transaction(DB_STORE, "readwrite");
      transaction.objectStore(DB_STORE).put(dataUrl, blockId);
    } catch {
      // The icon still lives in memory.
    }
  }

  async get(blockId, color) {
    if (this.cache.has(blockId)) {
      return this.cache.get(blockId);
    }
    if (this.pending.has(blockId)) {
      return this.pending.get(blockId);
    }
    const promise = (async () => {
      const disk = await this.readDisk(blockId);
      if (disk) {
        this.cache.set(blockId, disk);
        return disk;
      }
      return new Promise((resolve) => {
        this.queue.push({ blockId, color, resolve });
        this.drain();
      });
    })();
    this.pending.set(blockId, promise);
    try {
      return await promise;
    } finally {
      this.pending.delete(blockId);
    }
  }

  drain() {
    while (this.active < this.maxActive && this.queue.length > 0) {
      const task = this.queue.shift();
      this.active += 1;
      this.renderIcon(task.blockId, task.color)
        .then((dataUrl) => {
          if (dataUrl) {
            this.cache.set(task.blockId, dataUrl);
            this.rendered = this.cache.size;
            this.writeDisk(task.blockId, dataUrl);
          }
          task.resolve(dataUrl);
        })
        .catch(() => task.resolve(null))
        .finally(() => {
          this.active -= 1;
          this.drain();
        });
    }
  }

  async renderIcon(blockId, color) {
    const group = await this.loader.buildGroup(blockId, color, color);
    const box = new THREE.Box3().setFromObject(group);
    const center = box.getCenter(new THREE.Vector3());
    const size = box.getSize(new THREE.Vector3());
    const maxSize = Math.max(size.x, size.y, size.z, 0.01);
    const scale = 1.55 / maxSize;
    group.scale.setScalar(scale);
    group.position.set(-center.x * scale, -center.y * scale, -center.z * scale);
    this.scene.add(group);
    this.renderer.clear();
    this.renderer.render(this.scene, this.camera);
    const dataUrl = this.renderer.domElement.toDataURL("image/png");
    this.scene.remove(group);
    group.traverse((object) => {
      if (object.isMesh) {
        object.geometry?.dispose();
      }
    });
    return dataUrl;
  }
}
