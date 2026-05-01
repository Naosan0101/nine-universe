/**
 * Windows 用 exe 埋め込みアイコン（rcedit）向けに desktop_01.PNG から app-win.ico を生成する。
 * electron-builder は PNG でもよいが、環境によっては .ico の方が確実に反映される。
 */
'use strict';

const fs = require('fs');
const path = require('path');

const src = path.join(__dirname, '..', 'build-resources', 'desktop_01.PNG');
const out = path.join(__dirname, '..', 'build-resources', 'app-win.ico');

async function main() {
	if (!fs.existsSync(src)) {
		console.error('png-to-win-ico: missing source', src);
		process.exit(1);
	}
	let pngToIco;
	try {
		pngToIco = require('png-to-ico');
	} catch (e) {
		console.error('png-to-win-ico: run npm install in electron/ (need png-to-ico).', e.message || e);
		process.exit(1);
	}
	const input = fs.readFileSync(src);
	const buf = await pngToIco(input);
	fs.writeFileSync(out, buf);
	console.log('png-to-win-ico: wrote', out);
}

main().catch(function (e) {
	console.error('png-to-win-ico:', e);
	process.exit(1);
});
