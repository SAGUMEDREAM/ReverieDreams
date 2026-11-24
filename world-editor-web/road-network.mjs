import { gunzipSync } from "node:zlib";

// Match GensokyoRoadNetwork.decode: old checkpoints have no road metadata.
export function validateRoadNetwork(map) {
  const road = map.road_network;
  if (road === undefined) return [];
  try {
    if (!road || road.version !== 1 || road.encoding !== "gzip-base64-rdr1"
        || !Number.isInteger(road.columns) || road.columns < 0 || road.columns > 1_000_000
        || typeof road.data !== "string" || road.data.length > 24_000_000
        || road.data.length % 4 !== 0 || !/^[A-Za-z0-9+/]*={0,2}$/.test(road.data)) {
      throw new Error("不支持或损坏的道路编码");
    }
    const bytes = gunzipSync(Buffer.from(road.data, "base64"), { maxOutputLength: 24 + road.columns * 11 });
    if (bytes.length !== 24 + road.columns * 11 || bytes.toString("ascii", 0, 4) !== "RDR1"
        || bytes.readInt32BE(4) !== map.width || bytes.readInt32BE(8) !== map.depth
        || bytes.readInt32BE(12) !== map.origin?.[0] || bytes.readInt32BE(16) !== map.origin?.[1]
        || bytes.readInt32BE(20) !== road.columns) {
      throw new Error("道路尺寸、原点或列数与地图不匹配；请重新生成道路数据");
    }
    let previous = -1;
    for (let at = 24; at < bytes.length; at += 11) {
      const index = bytes.readInt32BE(at), pavement = bytes.readInt16BE(at + 4);
      const from = bytes.readInt16BE(at + 6), to = bytes.readInt16BE(at + 8), role = bytes[at + 10];
      if (index <= previous || index >= map.width * map.depth || pavement < map.min_y
          || pavement >= map.min_y + map.world_height || from <= pavement || to < from
          || to >= map.min_y + map.world_height || role < 1 || role > 4) {
        throw new Error(`道路列 ${Math.floor((at - 24) / 11)} 的索引或净空高度不正确`);
      }
      previous = index;
    }
    return [];
  } catch (error) {
    return [`road_network: ${error.message}`];
  }
}

export function assertRoadResizeSafe(map, width, depth) {
  if (map.road_network && (width !== map.width || depth !== map.depth)) {
    throw new Error("地图含已施工道路；缩放尺寸前必须同步迁移道路索引、桥梁覆盖及净空数据，当前不支持直接 resize");
  }
}
