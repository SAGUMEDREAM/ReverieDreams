import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { filesUnder } from './resources.mjs';
export function sourceSnapshot(repo) {
  const files = ['gradle.properties', ...['common/src/main/java','fabric/src/main/java','common/src/main/resources/data','common/src/generated/resources/data'].flatMap(folder => filesUnder(path.join(repo, folder)).filter(f => /\.(java|json)$/.test(f) && !/[\\/]gensokyo(?:-backup)?[\\/]/.test(f)).map(f => path.relative(repo, f).replaceAll('\\','/')))];
  const hash = crypto.createHash('sha256');
  for (const file of files.sort()) { hash.update(file); hash.update('\0'); hash.update(fs.readFileSync(path.join(repo,file))); }
  return { schema: 1, sha256: hash.digest('hex'), files: files.length };
}
