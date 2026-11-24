import test from "node:test";
import assert from "node:assert/strict";
import { createAuth, SESSION_COOKIE } from "./auth.mjs";

function requestWithCookie(token) {
  return { headers: { cookie: `${SESSION_COOKIE}=${encodeURIComponent(token)}` } };
}

function fakeResponse() {
  const headers = {};
  return {
    headers,
    setHeader(name, value) {
      headers[String(name).toLowerCase()] = value;
    },
  };
}

test("auth is disabled by default and remains backward compatible", () => {
  const auth = createAuth({});
  assert.equal(auth.required, false);
  assert.deepEqual(auth.roles, ["editor"]);
  const status = auth.status({});
  assert.equal(status.required, false);
  assert.equal(status.authenticated, true);
  assert.equal(status.role, "editor");
  const login = auth.login({ password: "", role: "editor", ip: "127.0.0.1" });
  assert.equal(login.ok, true);
  assert.equal(login.token, undefined);
  auth.close();
});

test("configured passwords create editor/viewer/admin sessions", () => {
  const auth = createAuth({
    editorPassword: "editor-pass",
    viewerPassword: "viewer-pass",
    adminPassword: "admin-pass",
    room: "test-room",
  });
  assert.equal(auth.required, true);
  assert.deepEqual(auth.roles, ["editor", "viewer", "admin"]);
  assert.equal(auth.status({}).authenticated, false);

  const wrong = auth.login({ password: "nope", role: "editor", ip: "127.0.0.1" });
  assert.equal(wrong.ok, false);
  assert.equal(wrong.status, 401);

  const editor = auth.login({ password: "editor-pass", role: "editor", ip: "127.0.0.1" });
  assert.equal(editor.ok, true);
  assert.equal(editor.role, "editor");
  assert.ok(editor.token);
  assert.equal(auth.status(requestWithCookie(editor.token)).role, "editor");

  const viewer = auth.login({ password: "viewer-pass", role: "viewer", ip: "127.0.0.2" });
  assert.equal(viewer.ok, true);
  assert.equal(viewer.role, "viewer");
  assert.equal(auth.status(requestWithCookie(viewer.token)).role, "viewer");

  const admin = auth.login({ password: "admin-pass", role: "admin", ip: "127.0.0.3" });
  assert.equal(admin.ok, true);
  assert.equal(admin.role, "admin");

  const res = fakeResponse();
  auth.setSessionCookie(res, editor.token);
  assert.match(res.headers["set-cookie"], new RegExp(`^${SESSION_COOKIE}=`));
  assert.match(res.headers["set-cookie"], /HttpOnly/);
  assert.match(res.headers["set-cookie"], /SameSite=Lax/);

  auth.revoke(viewer.token);
  assert.equal(auth.status(requestWithCookie(viewer.token)).authenticated, false);
  auth.close();
});

test("wrong role and repeated failures are rejected", () => {
  const auth = createAuth({ editorPassword: "editor-pass", room: "room" });
  const invalidRole = auth.login({ password: "editor-pass", role: "viewer", ip: "127.0.0.1" });
  assert.equal(invalidRole.ok, false);
  assert.equal(invalidRole.status, 400);

  let last = null;
  for (let i = 0; i < 12; i++) {
    last = auth.login({ password: "wrong", role: "editor", ip: "10.0.0.9" });
  }
  assert.equal(last.ok, false);
  assert.equal(last.status, 429);
  auth.close();
});