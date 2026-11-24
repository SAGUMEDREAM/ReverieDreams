import { createHash, randomBytes, timingSafeEqual } from "node:crypto";

export const SESSION_COOKIE = "gensokyo_session";

const LOGIN_WINDOW_MS = 60_000;
const LOGIN_MAX_ATTEMPTS = 10;
const SESSION_CLEANUP_MS = 10 * 60_000;

function hashPassword(value) {
  return createHash("sha256").update(String(value ?? "")).digest();
}

function passwordMatches(input, expected) {
  if (!expected) {
    return false;
  }
  return timingSafeEqual(hashPassword(input), hashPassword(expected));
}

function parseCookies(req) {
  const header = req?.headers?.cookie;
  if (typeof header !== "string" || header.length === 0) {
    return {};
  }
  const cookies = {};
  for (const part of header.split(";")) {
    const index = part.indexOf("=");
    if (index <= 0) {
      continue;
    }
    const name = part.slice(0, index).trim();
    const value = part.slice(index + 1).trim();
    if (name) {
      cookies[name] = decodeURIComponent(value);
    }
  }
  return cookies;
}

/**
 * Optional room authentication.
 *
 * Auth is disabled unless at least one password is configured, which keeps the
 * existing local editor and its saved sessions fully backward compatible.
 */
export function createAuth(options = {}) {
  const editorPassword = options.editorPassword || "";
  const viewerPassword = options.viewerPassword || "";
  const adminPassword = options.adminPassword || "";
  const room = String(options.room || "gensokyo").slice(0, 80);
  const ttlMs = Math.max(60_000, Number(options.ttlMs) || 12 * 60 * 60 * 1000);
  const required = Boolean(editorPassword || viewerPassword || adminPassword);

  const roles = [];
  if (editorPassword) roles.push("editor");
  if (viewerPassword) roles.push("viewer");
  if (adminPassword) roles.push("admin");
  if (!required) roles.push("editor");

  const sessions = new Map();
  const attempts = new Map();

  function passwordFor(role) {
    if (role === "editor") return editorPassword;
    if (role === "viewer") return viewerPassword;
    if (role === "admin") return adminPassword;
    return "";
  }

  function tokenFrom(req) {
    return parseCookies(req)[SESSION_COOKIE] || "";
  }

  function getSession(req) {
    const token = tokenFrom(req);
    if (!token) {
      return null;
    }
    const session = sessions.get(token);
    if (!session) {
      return null;
    }
    if (session.expiresAt <= Date.now()) {
      sessions.delete(token);
      return null;
    }
    session.lastSeen = Date.now();
    return { ...session, token };
  }

  function login({ password, role, ip }) {
    if (!required) {
      return { ok: true, required: false, role: "editor", room };
    }
    const roleName = String(role || "").toLowerCase();
    if (!roles.includes(roleName)) {
      return { ok: false, status: 400, error: "无效的房间角色" };
    }

    const now = Date.now();
    const address = String(ip || "unknown");
    let attempt = attempts.get(address);
    if (!attempt || attempt.resetAt <= now) {
      attempt = { count: 0, resetAt: now + LOGIN_WINDOW_MS };
      attempts.set(address, attempt);
    }
    if (attempt.count >= LOGIN_MAX_ATTEMPTS) {
      return { ok: false, status: 429, error: "登录尝试过多，请一分钟后再试" };
    }
    attempt.count += 1;

    if (!passwordMatches(password, passwordFor(roleName))) {
      return { ok: false, status: 401, error: "房间密码错误" };
    }

    attempts.delete(address);
    const token = randomBytes(24).toString("hex");
    const session = {
      token,
      role: roleName,
      room,
      createdAt: now,
      lastSeen: now,
      expiresAt: now + ttlMs,
    };
    sessions.set(token, session);
    return {
      ok: true,
      required: true,
      token,
      role: roleName,
      room,
      expiresAt: session.expiresAt,
    };
  }

  function revoke(token) {
    if (token) {
      sessions.delete(token);
    }
  }

  function setSessionCookie(res, token) {
    const maxAge = Math.max(1, Math.floor(ttlMs / 1000));
    res.setHeader(
      "Set-Cookie",
      `${SESSION_COOKIE}=${encodeURIComponent(token)}; Path=/; HttpOnly; SameSite=Lax; Max-Age=${maxAge}`,
    );
  }

  function clearSessionCookie(res) {
    res.setHeader(
      "Set-Cookie",
      `${SESSION_COOKIE}=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0`,
    );
  }

  function status(req) {
    if (!required) {
      return {
        required: false,
        authenticated: true,
        role: "editor",
        room,
        roles: ["editor"],
      };
    }
    const session = getSession(req);
    return {
      required: true,
      authenticated: Boolean(session),
      role: session?.role ?? null,
      room,
      roles,
    };
  }

  const cleanup = setInterval(() => {
    const now = Date.now();
    for (const [token, session] of sessions) {
      if (session.expiresAt <= now) {
        sessions.delete(token);
      }
    }
    for (const [address, attempt] of attempts) {
      if (attempt.resetAt <= now) {
        attempts.delete(address);
      }
    }
  }, SESSION_CLEANUP_MS);
  cleanup.unref?.();

  return {
    required,
    room,
    roles,
    login,
    getSession,
    revoke,
    setSessionCookie,
    clearSessionCookie,
    status,
    close: () => clearInterval(cleanup),
  };
}