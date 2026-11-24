import { open, readFile, rename, rm, writeFile } from "node:fs/promises";

export async function writeAtomic(file, data) {
  // Preserve unchanged layers, including raw files held open by map readers.
  const next = Buffer.isBuffer(data) ? data : Buffer.from(data);
  try {
    if ((await readFile(file)).equals(next)) return;
  } catch (error) {
    if (error?.code !== "ENOENT") throw error;
  }
  const temp = `${file}.${process.pid}.tmp`;
  await writeFile(temp, data);
  try {
    await rename(temp, file);
  } catch (error) {
    // Fixed-size raw layers may be mapped by another Windows reader. Such a
    // handle can allow writes while refusing rename/unlink. Keep its identity
    // and length; the save transaction already owns a verified rollback copy.
    if (process.platform === "win32" && ["EBUSY", "EPERM", "EACCES"].includes(error?.code)) {
      const existing = await open(file, "r+");
      try {
        if ((await existing.stat()).size !== next.length) throw error;
        await existing.writeFile(next);
        await existing.sync();
      } finally {
        await existing.close();
      }
      await rm(temp, { force: true });
      return;
    }
    // Windows 的 rename 不总是允许覆盖已有文件。
    await rm(file, { force: true });
    await rename(temp, file);
  }
}
