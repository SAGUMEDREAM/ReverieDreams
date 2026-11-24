import path from "node:path";
import { randomUUID } from "node:crypto";
import { mkdir, rm } from "node:fs/promises";

/** Owns the editor's single save session and its staging directory. */
export class SaveSessions {
  #current = null;
  #stagingRoot;
  #ttlMs;

  constructor(stagingRoot, ttlMs) {
    this.#stagingRoot = stagingRoot;
    this.#ttlMs = ttlMs;
  }

  get current() {
    return this.#current;
  }

  async create(clientId, baseRevision) {
    const token = randomUUID();
    const dir = path.join(this.#stagingRoot, token);
    await mkdir(dir, { recursive: true });
    this.#current = {
      token,
      clientId,
      baseRevision,
      dir,
      files: new Set(),
      expiresAt: Date.now() + this.#ttlMs,
      committing: false,
      commitPromise: null,
    };
    return this.#current;
  }

  forToken(token) {
    const session = this.#current;
    return session && token && session.token === token && Date.now() <= session.expiresAt ? session : null;
  }

  async waitForCommit() {
    if (this.#current?.commitPromise) {
      await this.#current.commitPromise.catch(() => {});
    }
  }

  expire() {
    const session = this.#current;
    if (session && !session.committing && Date.now() > session.expiresAt) {
      return this.abort(session, "expired");
    }
    return null;
  }

  async abort(session, reason) {
    if (!session) return;
    if (this.#current === session) this.#current = null;
    try {
      await rm(session.dir, { recursive: true, force: true });
    } catch {
      // Staging cleanup is best effort.
    }
    if (reason !== "committed" && reason !== "expired") {
      console.warn(`Gensokyo editor save session aborted: ${reason}`);
    }
  }
}
