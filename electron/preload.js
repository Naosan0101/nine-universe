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

/** パッケージ化後も main の app.getVersion() と一致させる（ファイル読みとズレる端末対策） */
function readAppVersionFromMainSync() {
	try {
		var v = ipcRenderer.sendSync('nu-sync-app-version');
		if (typeof v === 'string' && v.trim()) {
			return v.trim();
		}
	} catch (e) {
		/* ignore */
	}
	return null;
}

contextBridge.exposeInMainWorld('nuElectron', {
	/** invoke の応答待ちより前に WebContents が破棄される端末向けに send（main は ipcMain.on） */
	quitApp: () => ipcRenderer.send('nu-quit-app'),
	setFullScreen: (on) => ipcRenderer.invoke('nu-set-fullscreen', !!on),
	/** 対戦申し込みなど OS ネイティブのデスクトップ通知 */
	showPvpInviteDesktopNotification: (title, body) =>
		ipcRenderer.invoke('nu-show-pvp-invite-notification', {
			title: typeof title === 'string' ? title : '',
			body: typeof body === 'string' ? body : '',
		}),
	initialFullscreenPreference: initialFullscreenPreference,
	/** サーバーの {@code app.desktop-client.minimum-version} と照合（ログイン前ゲート） */
	appVersion: readAppVersionFromMainSync() || readPackageJsonVersion(),
	/** サーバの任意更新情報（Web オーバーレイ用）。従来の checkDesktopUpdate と同一応答。 */
	getDesktopUpdateInfo: () => ipcRenderer.invoke('nu-get-desktop-update-info'),
	checkDesktopUpdate: () => ipcRenderer.invoke('nu-get-desktop-update-info'),
	/** main がインストーラを temp に保存（進捗は onDesktopInstallerProgress） */
	startDesktopInstallerDownload: () => ipcRenderer.invoke('nu-start-desktop-installer-download'),
	/**
	 * 保存済みインストーラを起動してアプリを終了する。
	 * opts.navigateToLoginFirst が true のとき、先に START_URL の /login を読み込んでから起動する。
	 */
	runDownloadedDesktopInstaller: (opts) =>
		ipcRenderer.invoke('nu-run-downloaded-desktop-installer', opts && typeof opts === 'object' ? opts : {}),
	onDesktopInstallerProgress: function (listener) {
		if (typeof listener !== 'function') {
			return function () {};
		}
		var channel = 'nu-desktop-installer-progress';
		var wrapped = function (_event, payload) {
			listener(payload);
		};
		ipcRenderer.on(channel, wrapped);
		return function () {
			ipcRenderer.removeListener(channel, wrapped);
		};
	},
});
