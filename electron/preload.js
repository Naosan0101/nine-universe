const path = require('path');
const fs = require('fs');
const { contextBridge, ipcRenderer } = require('electron');

/** main.js の NINE_UNIVERSE_START_FULLSCREEN と同じ（未設定・'0' 以外は true） */
const initialFullscreenPreference = process.env.NINE_UNIVERSE_START_FULLSCREEN !== '0';

function readPackageJsonVersion() {
	try {
		var p = path.join(__dirname, 'package.json');
		var raw = fs.readFileSync(p, 'utf8');
		var j = JSON.parse(raw);
		return typeof j.version === 'string' && j.version.trim() ? j.version.trim() : '0.0.0';
	} catch (e) {
		return '0.0.0';
	}
}

contextBridge.exposeInMainWorld('nuElectron', {
	quitApp: () => ipcRenderer.invoke('nu-quit-app'),
	setFullScreen: (on) => ipcRenderer.invoke('nu-set-fullscreen', !!on),
	/** 対戦申し込みなど OS ネイティブのデスクトップ通知 */
	showPvpInviteDesktopNotification: (title, body) =>
		ipcRenderer.invoke('nu-show-pvp-invite-notification', {
			title: typeof title === 'string' ? title : '',
			body: typeof body === 'string' ? body : '',
		}),
	initialFullscreenPreference: initialFullscreenPreference,
	/** サーバーの {@code app.desktop-client.minimum-version} と照合（ログイン前ゲート） */
	appVersion: readPackageJsonVersion(),
	/** ログイン後: サーバの最新版より古ければ「更新があります」ダイアログ（main が shell.openExternal） */
	checkDesktopUpdate: () => ipcRenderer.invoke('nu-check-desktop-update'),
});
