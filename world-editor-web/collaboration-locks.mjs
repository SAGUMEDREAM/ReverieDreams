/** Cell leases are atomic per request; an expired lease never authorizes a write. */
export class CellLeases {
  constructor(ttl = 20000, now = Date.now) {
    this.ttl = ttl;
    this.now = now;
    this.entries = new Map();
  }

  expire() {
    let changed = false;
    for (const [key, lease] of this.entries) {
      if (lease.expiresAt <= this.now()) {
        this.entries.delete(key);
        changed = true;
      }
    }
    return changed;
  }

  take(owner, cells) {
    this.expire();
    const denied = cells.filter(({ x, z }) => {
      const lease = this.entries.get(`${x},${z}`);
      return lease && lease.owner !== owner;
    }).map(({ x, z }) => ({ x, z, owner: this.entries.get(`${x},${z}`).owner }));
    if (denied.length) return { granted: [], denied };
    const expiresAt = this.now() + this.ttl;
    const granted = [...new Set(cells.map(({ x, z }) => `${x},${z}`))];
    for (const key of granted) this.entries.set(key, { owner, expiresAt });
    return { granted, denied: [] };
  }

  owns(owner, cells) {
    this.expire();
    return cells.every(({ x, z }) => this.entries.get(`${x},${z}`)?.owner === owner);
  }

  release(owner, cells = null) {
    const keys = cells ? cells.map(({ x, z }) => `${x},${z}`) : [...this.entries.keys()];
    for (const key of keys) {
      if (this.entries.get(key)?.owner === owner) this.entries.delete(key);
    }
  }

  list() {
    this.expire();
    return [...this.entries].map(([key, lease]) => ({ key, ...lease }));
  }

  clear() {
    this.entries.clear();
  }
}

export function validateCells(cells, map) {
  if (!Array.isArray(cells) || cells.length === 0 || cells.length > 1000000) {
    throw new Error("锁申请必须包含 1..1000000 个格子");
  }
  for (const cell of cells) {
    if (!Number.isInteger(cell?.x) || !Number.isInteger(cell?.z)
        || cell.x < 0 || cell.z < 0 || cell.x >= map.width || cell.z >= map.depth) {
      throw new Error("锁申请包含无效或越界坐标");
    }
  }
  return cells;
}

export function validateTerrainOperation(payload, map) {
  const changes = payload?.changes ?? [];
  const underground = payload?.undergroundChanges ?? [];
  if (!Array.isArray(changes) || !Array.isArray(underground)
      || changes.length + underground.length === 0 || changes.length + underground.length > 1000000) {
    throw new Error("地形提交必须包含 1..1000000 项修改");
  }
  const cells = new Map();
  for (const change of changes) {
    if (!Number.isInteger(change?.index) || change.index < 0 || change.index >= map.width * map.depth
        || !Number.isInteger(change.h) || (change.h !== -32768
          && (change.h < map.min_y || change.h >= map.min_y + map.world_height))
        || !Number.isInteger(change.b) || change.b < 0 || change.b > 255
        || !Number.isInteger(change.s) || change.s < 0 || change.s > 65535) {
      throw new Error("地形提交包含无效的索引、高度或调色板索引");
    }
    const x = change.index % map.width;
    const z = Math.floor(change.index / map.width);
    cells.set(`${x},${z}`, { x, z });
  }
  for (const change of underground) {
    if (typeof change?.key !== "string" || !/^-?\d+,-?\d+,-?\d+$/.test(change.key)
        || (change.block !== null && (typeof change.block !== "string" || !change.block.length))) {
      throw new Error("地下提交格式无效");
    }
    // Underground coordinates use the same local x/z as the editor's layer arrays.
    const [x, y, z] = change.key.split(",").map(Number);
    if (x < 0 || x >= map.width || z < 0 || z >= map.depth
        || y < map.min_y || y >= map.min_y + map.world_height) {
      throw new Error("地下提交坐标越界");
    }
    cells.set(`${x},${z}`, { x, z });
  }
  return [...cells.values()];
}
