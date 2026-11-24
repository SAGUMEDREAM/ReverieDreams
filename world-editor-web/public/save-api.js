function jsonRequest(path, method, body, token) {
  const headers = { "Content-Type": "application/json" };
  if (token) headers["X-Save-Token"] = token;
  return fetch(path, { method, headers, body: JSON.stringify(body) });
}

export function beginSave(clientId, baseRevision) {
  return jsonRequest("/api/save/begin", "POST", { clientId, baseRevision });
}

export function uploadMap(map, token) {
  return jsonRequest("/api/map", "PUT", map, token);
}

export function uploadUnderground(blocks, token) {
  return jsonRequest("/api/underground", "PUT", { format_version: 1, blocks }, token);
}

export function commitSave(token) {
  return jsonRequest("/api/save/commit", "POST", { token });
}

export function abortSave(token) {
  return jsonRequest("/api/save/abort", "POST", { token });
}

export function syncRuntimeMap() {
  return fetch("/api/sync", { method: "POST" });
}

export async function putLayer(name, buffer, token = null) {
  let body = buffer;
  const headers = { "Content-Type": "application/octet-stream" };
  if (token) headers["X-Save-Token"] = token;
  if (typeof CompressionStream !== "undefined") {
    const stream = new Blob([buffer]).stream().pipeThrough(new CompressionStream("gzip"));
    body = await new Response(stream).arrayBuffer();
    headers["Content-Encoding"] = "gzip";
  }
  const response = await fetch(`/api/layers/${name}`, { method: "PUT", headers, body });
  const result = await response.json();
  if (!response.ok || !result.ok) {
    throw new Error((result.errors ?? [result.error ?? response.statusText]).join("; "));
  }
}
