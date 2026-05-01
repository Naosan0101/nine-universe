/**
 * Runs ensure-win-codesign-cache.ps1 on Windows only so `npm run dist` works
 * without symlink privileges (electron-builder issue #8149).
 */
const { spawnSync } = require('child_process');
const path = require('path');

if (process.platform !== 'win32') {
  process.exit(0);
}

const ps1 = path.join(__dirname, 'ensure-win-codesign-cache.ps1');
const r = spawnSync(
  'powershell',
  ['-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', ps1],
  { stdio: 'inherit', shell: false }
);
process.exit(typeof r.status === 'number' ? r.status : 1);
