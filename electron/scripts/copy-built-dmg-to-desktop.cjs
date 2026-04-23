/**
 * macOS で `npm run dist:mac` のあとに実行する。
 * electron-builder が dist-installer に出力した .dmg を
 * デスクトップの「Mac用インストーラー」フォルダ内へ nine-universe-0.1.1.dmg としてコピーする。
 */
'use strict';

const fs = require('fs');
const path = require('path');
const os = require('os');

const electronRoot = path.resolve(__dirname, '..');
const distInstaller = path.join(electronRoot, 'dist-installer');
const destName = 'nine-universe-0.1.1.dmg';
const macInstallerFolderName = 'Mac用インストーラー';

function resolveDesktopDir() {
	const home = os.homedir();
	for (const name of ['Desktop', 'デスクトップ']) {
		const d = path.join(home, name);
		try {
			if (fs.existsSync(d) && fs.statSync(d).isDirectory()) {
				return d;
			}
		} catch (_) {
			/* ignore */
		}
	}
	return null;
}

let dmgPath = null;
try {
	const names = fs.readdirSync(distInstaller);
	for (const n of names) {
		if (n.toLowerCase().endsWith('.dmg')) {
			dmgPath = path.join(distInstaller, n);
			break;
		}
	}
} catch (e) {
	console.error('dist-installer を読めません:', e.message);
	process.exit(1);
}

if (!dmgPath) {
	console.error('dist-installer に .dmg がありません。macOS で npm run dist:mac を先に実行してください。');
	process.exit(1);
}

const desktop = resolveDesktopDir();
if (!desktop) {
	console.error('デスクトップフォルダ（Desktop / デスクトップ）が見つかりません。');
	process.exit(1);
}

const macDir = path.join(desktop, macInstallerFolderName);
fs.mkdirSync(macDir, { recursive: true });
const dest = path.join(macDir, destName);
fs.copyFileSync(dmgPath, dest);
console.log('コピーしました:', dest);
