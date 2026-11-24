import { gunzipSync } from "node:zlib";

function decodeNbt(buffer) {
  let offset = 0;
  const u8 = () => buffer[offset++];
  const i16 = () => {
    const value = buffer.readInt16BE(offset);
    offset += 2;
    return value;
  };
  const i32 = () => {
    const value = buffer.readInt32BE(offset);
    offset += 4;
    return value;
  };
  const i64 = () => {
    const value = buffer.readBigInt64BE(offset);
    offset += 8;
    return value;
  };
  const f32 = () => {
    const value = buffer.readFloatBE(offset);
    offset += 4;
    return value;
  };
  const f64 = () => {
    const value = buffer.readDoubleBE(offset);
    offset += 8;
    return value;
  };
  const str = () => {
    const length = buffer.readUInt16BE(offset);
    offset += 2;
    const value = buffer.toString("utf8", offset, offset + length);
    offset += length;
    return value;
  };

  function payload(type) {
    switch (type) {
      case 1: return buffer.readInt8(offset++);
      case 2: return i16();
      case 3: return i32();
      case 4: return i64();
      case 5: return f32();
      case 6: return f64();
      case 7: {
        const length = i32();
        const value = buffer.subarray(offset, offset + length);
        offset += length;
        return value;
      }
      case 8: return str();
      case 9: {
        const elementType = u8();
        const length = i32();
        const value = [];
        for (let i = 0; i < length; i++) value.push(payload(elementType));
        return value;
      }
      case 10: {
        const value = {};
        while (true) {
          const childType = u8();
          if (childType === 0) break;
          const name = str();
          value[name] = payload(childType);
        }
        return value;
      }
      case 11: {
        const length = i32();
        const value = [];
        for (let i = 0; i < length; i++) value.push(i32());
        return value;
      }
      case 12: {
        const length = i32();
        const value = [];
        for (let i = 0; i < length; i++) value.push(i64());
        return value;
      }
      default:
        throw new Error(`Unsupported NBT tag type ${type}`);
    }
  }

  const rootType = u8();
  if (rootType !== 10) throw new Error("NBT root is not a compound");
  str();
  return payload(10);
}

export function readStructureSize(buffer) {
  const structure = readStructure(buffer);
  return structure ? structure.size : null;
}

export function readStructure(buffer) {
  let raw = buffer;
  try {
    raw = gunzipSync(buffer);
  } catch {
    // Some templates are already uncompressed.
  }
  const root = decodeNbt(raw);
  if (!Array.isArray(root.size) || root.size.length !== 3) {
    return null;
  }
  const paletteEntries = Array.isArray(root.palette)
    ? root.palette.map((entry) => ({
      name: String(entry?.Name ?? "minecraft:air"),
      properties: entry?.Properties && typeof entry.Properties === "object"
        ? Object.fromEntries(Object.entries(entry.Properties).map(([key, value]) => [key, String(value)]))
        : {},
    }))
    : [];
  const palette = paletteEntries.map((entry) => entry.name);
  const blocks = Array.isArray(root.blocks)
    ? root.blocks.map((entry) => {
      const state = Number(entry.state ?? 0);
      return {
        pos: Array.isArray(entry.pos) ? entry.pos.map(Number) : [],
        state,
        block: palette[state] ?? "minecraft:air",
        properties: paletteEntries[state]?.properties ?? {},
      };
    }).filter((entry) => entry.pos.length === 3)
    : [];
  const entities = Array.isArray(root.entities)
    ? root.entities.map((entry) => ({
      id: String(entry?.nbt?.id ?? ""),
      pos: Array.isArray(entry?.pos) ? entry.pos.map(Number) : [],
      blockPos: Array.isArray(entry?.blockPos) ? entry.blockPos.map(Number) : [],
    })).filter((entry) => entry.pos.length === 3)
    : [];
  return {
    size: root.size.map((value) => Number(value)),
    palette,
    blocks,
    entities,
  };
}
