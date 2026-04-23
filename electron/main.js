const path = require('path');
const fs = require('fs');
const { app, BrowserWindow, Menu, ipcMain, Notification } = require('electron');

/* Windows でトースト通知にアプリ名を正しく出す（package.json の build.appId と一致） */
if (process.platform === 'win32') {
	try {
		app.setAppUserModelId('jp.nine-universe.desktop');
	} catch (_) {
		/* ignore */
	}
}

/**
 * ウィンドウ／タスクバー用（BrowserWindow）。デスクトップ／exe 埋め込みは package.json の build.win.icon（desktop_icon_01.PNG＝Web 静的の cards/desktop_icon_01.PNG と同一画像）。
 * icon.png はアイコン01.PNG と同一画像（ASCII 名でランタイム読み込み互換）。
 * （Setup.exe のアイコンは package.json の nsis.installerIcon で別 ICO を使用）
 */
function appIconPath() {
	var candidates = [
		path.join(__dirname, 'build-resources', 'icon.png'),
		path.join(__dirname, 'build-resources', 'アイコン01.PNG'),
	];
	for (var i = 0; i < candidates.length; i++) {
		try {
			if (fs.existsSync(candidates[i])) {
				return candidates[i];
			}
		} catch (e) {
			/* ignore */
		}
	}
	return undefined;
}

/**
 * Windows 等で「タイトルだけ更新されて中身が真っ黒／グリッドだけ」になることがある。
 * GPU ドライバとの相性問題が多いので既定ではオフ。必要なら ELECTRON_USE_GPU=1 で有効化。
 */
if (process.env.ELECTRON_USE_GPU !== '1') {
	app.disableHardwareAcceleration();
}

try {
	app.commandLine.appendSwitch('disk-cache-dir', path.join(app.getPath('userData'), 'browser-cache'));
} catch (_) {
	// ignore
}

/** 本番は nine-universe.jp。開発時は環境変数で上書き可能。 */
const START_URL = process.env.NINE_UNIVERSE_URL || 'https://nine-universe.jp';

/** preload の initialFullscreenPreference と同じ（localStorage 未設定時の既定） */
const INITIAL_FULLSCREEN_DEFAULT = process.env.NINE_UNIVERSE_START_FULLSCREEN !== '0';

/**
 * executeJavaScript の戻り値は環境によって boolean ではなく文字列等になることがある。
 * !!want だと "false" が truthy になり、OFF 希望でもフルスクリーンが掛かり続ける。
 */
function wantFullscreenFromPageSyncResult(raw) {
	if (raw === true || raw === 'true' || raw === 1 || raw === '1') {
		return true;
	}
	if (raw === false || raw === 'false' || raw === 0 || raw === '0') {
		return false;
	}
	return INITIAL_FULLSCREEN_DEFAULT;
}

function browserWindowFromIpcSender(sender) {
	if (!sender || sender.isDestroyed()) {
		return null;
	}
	let w = BrowserWindow.fromWebContents(sender);
	if (w && !w.isDestroyed()) {
		return w;
	}
	/* fromWebContents が null になる事例へのフォールバック（単一ウィンドウ想定） */
	try {
		w = BrowserWindow.getFocusedWindow();
		if (w && !w.isDestroyed()) {
			return w;
		}
	} catch (_) {
		/* ignore */
	}
	try {
		const all = BrowserWindow.getAllWindows();
		for (var i = 0; i < all.length; i++) {
			if (all[i] && !all[i].isDestroyed()) {
				return all[i];
			}
		}
	} catch (_) {
		/* ignore */
	}
	return null;
}

/**
 * ページ遷移のたびに OS がフルスクリーンを外すことがあるため、読み込み完了後に main 側で再同期する。
 * data: 等の非 HTTP ページではスキップ（接続エラー画面など）。
 */
function syncBrowserWindowFullscreenFromStorage(win) {
	if (!win || win.isDestroyed()) {
		return;
	}
	const wc = win.webContents;
	if (!wc || wc.isDestroyed()) {
		return;
	}
	var url = '';
	try {
		url = String(wc.getURL() || '');
	} catch (_) {
		return;
	}
	if (!url || url.startsWith('data:') || url.startsWith('about:')) {
		return;
	}
	var defLit = INITIAL_FULLSCREEN_DEFAULT ? 'true' : 'false';
	var js =
		'(function(){try{var v=localStorage.getItem("nu_fullscreen_mode");' +
		'if(v==="0"||v==="false")return false;if(v==="1"||v==="true")return true;' +
		'}catch(e){}return ' +
		defLit +
		';})()';
	wc.executeJavaScript(js, true)
		.then(function (want) {
			if (!win || win.isDestroyed()) {
				return;
			}
			var flag = wantFullscreenFromPageSyncResult(want);
			/*
			 * POST→302（ログイン直後など）では isFullScreen() が実表示とずれたまま true を返し、
			 * 「既にフルスクリーン」と誤判定して setFullScreen(true) がスキップされることがある。
			 * did-finish-load 経由はページあたり1回なので、ここでは常に希望どおり適用する。
			 */
			win.setFullScreen(flag);
		})
		.catch(function () {});
}

/**
 * 開発で最初からウィンドウ表示にしたいときは NINE_UNIVERSE_START_FULLSCREEN=0（preload の初期希望と一致）。
 * 実際のウィンドウ状態はページ側の localStorage と IPC で同期する。
 */
function escapeHtml(s) {
	return String(s)
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;');
}

/** 接続失敗時は黒画面のままにせず、原因のヒントを表示する */
function loadConnectionErrorPage(win, failedUrl, errorDescription) {
	const body = `<!DOCTYPE html><html lang="ja"><head><meta charset="utf-8"/><meta name="viewport" content="width=device-width,initial-scale=1"/><title>接続できません</title><style>
body{font-family:system-ui,"Segoe UI","Meiryo",sans-serif;background:#0f1118;color:#e8e8ec;padding:2rem;line-height:1.65;max-width:42rem;margin:0 auto}
h1{font-size:1.15rem;color:#e8a0a0;margin-top:0}
p{margin:0.75rem 0}
code{word-break:break-all;font-size:0.88rem;background:#1a1e2a;padding:0.15rem 0.4rem;border-radius:4px}
.box{background:#171b26;border:1px solid rgba(255,255,255,0.12);border-radius:8px;padding:1rem 1.1rem;margin:1rem 0}
ul{padding-left:1.2rem;margin:0.5rem 0}
</style></head><body>
<h1>ページを読み込めませんでした</h1>
<p>Electron から次の URL へ接続できませんでした。</p>
<div class="box"><strong>URL</strong><br/><code>${escapeHtml(failedUrl)}</code></div>
<p><strong>エラー</strong> <code>${escapeHtml(errorDescription)}</code></p>
<p><strong>よくある原因</strong></p>
<ul>
<li>本番サーバーがまだ起動していない、または VPS / DNS / パケットフィルターの設定が未完了</li>
<li>PC や回線の側でブロックされている（ブラウザでも同じ URL が開けないか確認）</li>
</ul>
<p><strong>ローカルで動かす場合</strong>：先に Spring Boot を起動し、次のいずれかで起動してください。</p>
<div class="box"><code>npm run start:local</code><br/>または PowerShell:<br/><code>$env:NINE_UNIVERSE_URL=&quot;http://127.0.0.1:8080&quot;; npm start</code></div>
</body></html>`;
	win.loadURL('data:text/html;charset=utf-8,' + encodeURIComponent(body));
}

function createWindow() {
	var icon = appIconPath();
	var winOpts = {
		width: 1280,
		height: 800,
		minWidth: 360,
		minHeight: 640,
		fullscreenable: true,
		autoHideMenuBar: true,
		backgroundColor: '#0f1118',
		webPreferences: {
			contextIsolation: true,
			nodeIntegration: false,
			sandbox: true,
			preload: path.join(__dirname, 'preload.js'),
		},
	};
	if (icon) {
		winOpts.icon = icon;
	}
	const win = new BrowserWindow(winOpts);

	win.webContents.on('did-finish-load', function () {
		/* DOMContentLoaded より後。遷移直後に外れたフルスクリーンを希望どおり戻す */
		setImmediate(function () {
			syncBrowserWindowFullscreenFromStorage(win);
		});
	});

	win.webContents.on('did-fail-load', (event, errorCode, errorDescription, validatedURL, isMainFrame) => {
		if (!isMainFrame) {
			return;
		}
		// ERR_ABORTED（遷移キャンセルなど）は無視
		if (errorCode === -3) {
			return;
		}
		loadConnectionErrorPage(win, validatedURL, errorDescription);
	});

	win.loadURL(START_URL);
}

ipcMain.handle('nu-quit-app', () => {
	app.quit();
});

ipcMain.handle('nu-show-pvp-invite-notification', (event, payload) => {
	if (!Notification.isSupported()) {
		return false;
	}
	var title =
		payload && typeof payload.title === 'string' && payload.title.trim()
			? payload.title.trim()
			: 'ナインユニバース：対戦の申し込み';
	var body =
		payload && typeof payload.body === 'string' && payload.body.trim()
			? payload.body.trim()
			: '「だれかと対戦」を開いて承諾してください。';
	var icon = appIconPath();
	try {
		var opts = { title: title, body: body };
		if (icon) {
			opts.icon = icon;
		}
		var n = new Notification(opts);
		n.show();
		return true;
	} catch (_) {
		return false;
	}
});

ipcMain.handle('nu-set-fullscreen', (event, on) => {
	const w = browserWindowFromIpcSender(event.sender);
	if (!w || w.isDestroyed()) {
		return;
	}
	const flag = !!on;
	/*
	 * isFullScreen() が実表示とずれていると、解除時に「既にウィンドウ」と誤判定して
	 * setFullScreen(false) がスキップされる（設定で OFF にしてもフルスクリーンのまま）。
	 * did-finish-load の sync と同様、OFF は常に適用する。
	 * ON 側は従来どおり差分があるときだけ呼び、Windows での過剰呼び出しを避ける。
	 */
	if (!flag) {
		const wasFs = w.isFullScreen();
		w.setFullScreen(false);
		/* 非同期で状態が追いつかない環境向けに再試行。Windows では解除後に最大化だけ残ることもある。 */
		setImmediate(function () {
			if (!w || w.isDestroyed()) {
				return;
			}
			if (w.isFullScreen()) {
				w.setFullScreen(false);
			}
			if (process.platform === 'win32' && wasFs && w.isMaximized()) {
				try {
					w.unmaximize();
				} catch (_) {
					/* ignore */
				}
			}
		});
		return;
	}
	if (w.isFullScreen() !== flag) {
		w.setFullScreen(flag);
	}
});

app.whenReady().then(() => {
	Menu.setApplicationMenu(null);
	createWindow();
	app.on('activate', () => {
		if (BrowserWindow.getAllWindows().length === 0) {
			createWindow();
		}
	});
});

app.on('window-all-closed', () => {
	if (process.platform !== 'darwin') {
		app.quit();
	}
});
