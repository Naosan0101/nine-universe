const path = require('path');
const fs = require('fs');
const http = require('http');
const https = require('https');
const { spawn } = require('child_process');
const { app, BrowserWindow, Menu, ipcMain, Notification, session, shell } = require('electron');

/* Windows でトースト通知にアプリ名を正しく出す（package.json の build.appId と一致） */
if (process.platform === 'win32') {
	try {
		app.setAppUserModelId('jp.nine-universe.desktop');
	} catch (_) {
		/* ignore */
	}
}

/**
 * ウィンドウ／タスクバー用（BrowserWindow）。exe 埋め込み／ショートカット表示は package.json の build.win.icon（build-resources/desktop_01.PNG）。
 * icon.png はアイコン01.PNG と同一画像（ASCII 名でランタイム読み込み互換）。
 * （Setup.exe のアイコンは package.json の nsis.installerIcon で別 ICO を使用）
 * パッケージ exe に desktop_01 を反映するには build.win.signAndEditExecutable が true である必要がある（false だと Electron 既定アイコンのまま）。
 */
function appIconPath() {
	var candidates = [
		path.join(__dirname, 'build-resources', 'desktop_01.PNG'),
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

/*
 * デプロイ後に「1 回目の起動だけ古い HTML / 古い ?v= の CSS」が残るのを防ぐ（Chromium の HTTP キャッシュ無効）。
 * 転送量は増える。ローカルでキャッシュを戻すときは NINE_UNIVERSE_ALLOW_HTTP_CACHE=1。
 */
if (process.env.NINE_UNIVERSE_ALLOW_HTTP_CACHE !== '1') {
	try {
		app.commandLine.appendSwitch('disable-http-cache');
	} catch (_) {
		// ignore
	}
}

/** 本番は nine-universe.jp。開発時は環境変数で上書き可能。 */
const START_URL = process.env.NINE_UNIVERSE_URL || 'https://nine-universe.jp';

/** サーバの JAR 差し替え（assetVersion 変化）を検知してウィンドウを取り直す間隔 */
const ASSET_VERSION_POLL_INTERVAL_MS = 3 * 60 * 1000;

var assetPollTimer = null;
var lastKnownAssetVersion = null;

function assetVersionEndpointUrl() {
	try {
		return new URL('/api/client-asset-version', START_URL).href;
	} catch (e) {
		return null;
	}
}

/** 初回 loadURL 用。同一 URL のメモリ／ディスクキャッシュを避ける（サーバは無視してよいクエリ）。 */
function coldStartNavigationUrl() {
	try {
		var u = new URL(START_URL);
		u.searchParams.set('_nu_cold', String(Date.now()));
		return u.href;
	} catch (e) {
		return START_URL;
	}
}

function fetchRemoteAssetVersion() {
	var u = assetVersionEndpointUrl();
	if (!u) {
		return Promise.resolve(null);
	}
	return fetch(u, { cache: 'no-store', method: 'GET' })
		.then(function (res) {
			if (!res.ok) {
				return null;
			}
			return res.text();
		})
		.then(function (text) {
			if (text == null) {
				return null;
			}
			var t = String(text).trim();
			return t.length ? t : null;
		})
		.catch(function () {
			return null;
		});
}

function maybeReloadForNewAssetVersion(win, remote) {
	if (!win || win.isDestroyed() || !remote) {
		return;
	}
	var wc = win.webContents;
	if (!wc || wc.isDestroyed()) {
		return;
	}
	try {
		var url = String(wc.getURL() || '');
		if (url.startsWith('data:') || url.startsWith('about:')) {
			return;
		}
	} catch (e) {
		return;
	}
	if (lastKnownAssetVersion === null) {
		lastKnownAssetVersion = remote;
		return;
	}
	if (remote !== lastKnownAssetVersion) {
		lastKnownAssetVersion = remote;
		wc.reloadIgnoringCache();
	}
}

function startAssetVersionPolling(win) {
	if (assetPollTimer) {
		clearInterval(assetPollTimer);
		assetPollTimer = null;
	}
	var poll = function () {
		if (!win || win.isDestroyed()) {
			return;
		}
		fetchRemoteAssetVersion().then(function (v) {
			maybeReloadForNewAssetVersion(win, v);
		});
	};
	assetPollTimer = setInterval(poll, ASSET_VERSION_POLL_INTERVAL_MS);
}

/* --- デスクトップ任意更新（Web オーバーレイ + main がインストーラを取得して起動） --- */
var lastInstallerDownloadPath = null;
var activeInstallerDownloadReq = null;

function desktopClientUpdateEndpointUrl() {
	try {
		return new URL('/api/desktop-client-update', START_URL).href;
	} catch (e) {
		return null;
	}
}

function semverParts(s) {
	var p = String(s || '0').split('.');
	var a = parseInt(p[0], 10);
	var b = parseInt(p[1], 10);
	var c = parseInt(p[2], 10);
	return [isFinite(a) ? a : 0, isFinite(b) ? b : 0, isFinite(c) ? c : 0];
}

/** @returns {-1|0|1} */
function compareSemver(a, b) {
	var pa = semverParts(a);
	var pb = semverParts(b);
	for (var i = 0; i < 3; i++) {
		if (pa[i] < pb[i]) {
			return -1;
		}
		if (pa[i] > pb[i]) {
			return 1;
		}
	}
	return 0;
}

async function fetchDesktopClientUpdatePayload() {
	var ep = desktopClientUpdateEndpointUrl();
	if (!ep) {
		return null;
	}
	var res;
	try {
		res = await fetch(ep, { cache: 'no-store', method: 'GET' });
	} catch (e) {
		return null;
	}
	if (!res.ok) {
		return null;
	}
	var data;
	try {
		data = await res.json();
	} catch (e) {
		return null;
	}
	var latest = typeof data.latestVersion === 'string' ? data.latestVersion.trim() : '';
	var installerUrl = typeof data.installerUrl === 'string' ? data.installerUrl.trim() : '';
	if (!latest) {
		return null;
	}
	/* 例: 0.1.3（プレリリース接尾辞はフォールバック対象外） */
	if (!/^\d+\.\d+\.\d+$/.test(latest)) {
		return null;
	}
	/* installer-url が空でも、既定の /downloads/nine-universe-setup-{version}.exe を試す（ローカル開発の取りこぼし防止） */
	if (!installerUrl) {
		try {
			installerUrl = new URL('/downloads/nine-universe-setup-' + latest + '.exe', START_URL).href;
		} catch (e) {
			return null;
		}
	}
	if (installerUrl.indexOf('http://') !== 0 && installerUrl.indexOf('https://') !== 0) {
		return null;
	}
	return { latestVersion: latest, installerUrl: installerUrl };
}

function isSafeRemoteInstallerUrl(urlStr) {
	try {
		var u = new URL(urlStr);
		if (u.protocol === 'https:') {
			return true;
		}
		if (u.protocol === 'http:') {
			var h = (u.hostname || '').toLowerCase();
			return h === 'localhost' || h === '127.0.0.1';
		}
		return false;
	} catch (e) {
		return false;
	}
}

/**
 * ログイン前ゲートの「インストーラ取得」用。任意の URL を開かせない。
 * ホストは START_URL と同一、またはローカル開発用 localhost のみ。
 */
function isAllowedInstallerOpenExternalUrl(href) {
	try {
		var u = new URL(href);
		var scheme = (u.protocol || '').toLowerCase();
		if (scheme !== 'https:' && scheme !== 'http:') {
			return false;
		}
		var h = (u.hostname || '').toLowerCase();
		var startHost = new URL(START_URL).hostname.toLowerCase();
		var hostOk = h === startHost || h === 'localhost' || h === '127.0.0.1';
		if (!hostOk && startHost.length > 0) {
			hostOk = h.endsWith('.' + startHost);
		}
		if (!hostOk) {
			return false;
		}
		if (scheme === 'http:' && h !== 'localhost' && h !== '127.0.0.1') {
			return false;
		}
		var path = (u.pathname || '').replace(/\\/g, '/').toLowerCase();
		if (path.indexOf('/downloads/') === -1) {
			return false;
		}
		if (!path.endsWith('.exe') && !path.endsWith('.dmg')) {
			return false;
		}
		return true;
	} catch (e) {
		return false;
	}
}

async function fetchDesktopUpdateInfoForRenderer() {
	var current = app.getVersion();
	var payload = await fetchDesktopClientUpdatePayload();
	if (!payload) {
		return { ok: true, needsUpdate: false, latestVersion: '', installerUrl: '', currentVersion: current };
	}
	var needs = compareSemver(current, payload.latestVersion) < 0;
	return {
		ok: true,
		needsUpdate: needs,
		latestVersion: payload.latestVersion,
		installerUrl: needs ? payload.installerUrl : '',
		currentVersion: current,
	};
}

function sendInstallerProgress(webContents, payload) {
	if (!webContents || webContents.isDestroyed()) {
		return;
	}
	try {
		webContents.send('nu-desktop-installer-progress', payload);
	} catch (e) {
		/* ignore */
	}
}

function downloadInstallerToTemp(installerUrl, webContents) {
	return new Promise(function (resolve, reject) {
		if (!isSafeRemoteInstallerUrl(installerUrl)) {
			reject(new Error('unsafe_installer_url'));
			return;
		}
		var baseName = path.basename(new URL(installerUrl).pathname) || 'nine-universe-setup.exe';
		if (baseName.indexOf('.') === -1) {
			baseName += '.exe';
		}
		baseName = baseName.replace(/[^a-zA-Z0-9._-]+/g, '_');
		var dest = path.join(app.getPath('temp'), 'nu-installer-' + Date.now() + '-' + baseName);
		var received = 0;
		var total = 0;

		function cleanupPartial() {
			try {
				if (fs.existsSync(dest)) {
					fs.unlinkSync(dest);
				}
			} catch (e) {
				/* ignore */
			}
		}

		function doRequest(url, redirectsLeft) {
			if (redirectsLeft <= 0) {
				cleanupPartial();
				reject(new Error('too_many_redirects'));
				return;
			}
			var lib = url.startsWith('https:') ? https : http;
			var req = lib.get(
				url,
				{
					headers: { 'Cache-Control': 'no-store' },
				},
				function (res) {
					if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
						var nextUrl;
						try {
							nextUrl = new URL(res.headers.location, url).href;
						} catch (e) {
							cleanupPartial();
							reject(e);
							return;
						}
						res.resume();
						doRequest(nextUrl, redirectsLeft - 1);
						return;
					}
					if (res.statusCode !== 200) {
						cleanupPartial();
						reject(new Error('http_' + res.statusCode));
						return;
					}
					total = parseInt(res.headers['content-length'] || '0', 10) || 0;
					var out = fs.createWriteStream(dest);
					activeInstallerDownloadReq = req;
					res.on('data', function (chunk) {
						received += chunk.length;
						sendInstallerProgress(webContents, {
							phase: 'downloading',
							received: received,
							total: total,
						});
					});
					res.pipe(out);
					out.on('finish', function () {
						activeInstallerDownloadReq = null;
						out.close(function (closeErr) {
							if (closeErr) {
								cleanupPartial();
								reject(closeErr);
								return;
							}
							sendInstallerProgress(webContents, {
								phase: 'complete',
								received: received,
								total: total || received,
							});
							resolve(dest);
						});
					});
					out.on('error', function (err) {
						activeInstallerDownloadReq = null;
						cleanupPartial();
						reject(err);
					});
					res.on('error', function (err) {
						activeInstallerDownloadReq = null;
						cleanupPartial();
						reject(err);
					});
				}
			);
			req.on('error', function (err) {
				activeInstallerDownloadReq = null;
				cleanupPartial();
				reject(err);
			});
		}

		doRequest(installerUrl, 12);
	});
}

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
			if (!flag) {
				/* ウィンドウ希望に切り替えた直後に遅延同期がフルスクリーンへ戻すのを防ぐ */
				clearFullscreenResyncTimers(win);
				setImmediate(function () {
					if (!win || win.isDestroyed()) {
						return;
					}
					if (win.isFullScreen()) {
						win.setFullScreen(false);
					}
					unmaximizeIfNeededForWindowed(win);
				});
			}
		})
		.catch(function () {});
}

/** 連続遷移時は直前の遅延同期をキャンセルし、最後の遷移からの再適用だけ残す */
var fullscreenResyncTimers = new WeakMap();

function clearFullscreenResyncTimers(win) {
	if (!win) {
		return;
	}
	var prev = fullscreenResyncTimers.get(win);
	if (prev && prev.length) {
		for (var i = 0; i < prev.length; i++) {
			try {
				clearTimeout(prev[i]);
			} catch (_) {
				/* ignore */
			}
		}
	}
	fullscreenResyncTimers.delete(win);
}

/** フルスクリーン解除後も最大化だけ残るとリサイズできない（Windows で多い） */
function unmaximizeIfNeededForWindowed(win) {
	if (!win || win.isDestroyed() || process.platform !== 'win32') {
		return;
	}
	try {
		if (win.isMaximized()) {
			win.unmaximize();
		}
	} catch (_) {
		/* ignore */
	}
}

/**
 * 遷移直後は OS がフルスクリーンを外すタイミングが executeJavaScript より遅いことがあるため、
 * 数回に分けて localStorage 希望を再適用する。
 */
function scheduleFullscreenSyncFromStorage(win) {
	if (!win || win.isDestroyed()) {
		return;
	}
	var prev = fullscreenResyncTimers.get(win);
	if (prev && prev.length) {
		for (var i = 0; i < prev.length; i++) {
			try {
				clearTimeout(prev[i]);
			} catch (_) {
				/* ignore */
			}
		}
	}
	function run() {
		if (!win || win.isDestroyed()) {
			return;
		}
		syncBrowserWindowFullscreenFromStorage(win);
	}
	var ids = [setTimeout(run, 0), setTimeout(run, 90), setTimeout(run, 240)];
	fullscreenResyncTimers.set(win, ids);
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

async function createWindow() {
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
	/* localStorage 反映前の最初の一瞬から既定どおりフルスクリーン（OFF 希望は初回 load 後の同期で解除） */
	if (INITIAL_FULLSCREEN_DEFAULT) {
		try {
			win.setFullScreen(true);
		} catch (_) {
			/* ignore */
		}
	}

	/* インストーラ等: WebView 内で .exe を開かず、既定ブラウザでダウンロードさせる */
	win.webContents.setWindowOpenHandler(function (details) {
		var raw = details.url;
		if (!raw) {
			return { action: 'deny' };
		}
		var resolved;
		try {
			resolved = new URL(raw, START_URL).href;
		} catch (e) {
			return { action: 'deny' };
		}
		try {
			var uo = new URL(resolved);
			if (uo.protocol === 'https:' || uo.protocol === 'http:') {
				try {
					shell.openExternal(resolved);
				} catch (e2) {
					/* ignore */
				}
				return { action: 'deny' };
			}
		} catch (e3) {
			/* ignore */
		}
		return { action: 'deny' };
	});
	win.webContents.on('will-navigate', function (event, url) {
		if (!url) {
			return;
		}
		try {
			var resolved = new URL(String(url), START_URL).href;
			var lower = resolved.toLowerCase();
			if (
				(lower.indexOf('/downloads/nine-universe-setup-') !== -1 && lower.endsWith('.exe')) ||
				(lower.indexOf('/downloads/nine-universe-') !== -1 && lower.endsWith('.dmg'))
			) {
				event.preventDefault();
				shell.openExternal(resolved);
			}
		} catch (e) {
			/* ignore */
		}
	});

	win.webContents.on('did-navigate', function (_event, url) {
		if (!url || (!String(url).startsWith('http://') && !String(url).startsWith('https://'))) {
			return;
		}
		scheduleFullscreenSyncFromStorage(win);
	});

	var assetPollStarted = false;

	win.on('closed', function () {
		if (assetPollTimer) {
			clearInterval(assetPollTimer);
			assetPollTimer = null;
		}
		lastKnownAssetVersion = null;
	});

	win.webContents.on('did-finish-load', function () {
		/* DOMContentLoaded より後。遷移直後に外れたフルスクリーンを希望どおり戻す */
		scheduleFullscreenSyncFromStorage(win);
		if (!assetPollStarted) {
			assetPollStarted = true;
			fetchRemoteAssetVersion().then(function (v) {
				if (v) {
					lastKnownAssetVersion = v;
				}
				startAssetVersionPolling(win);
			});
		}
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

	try {
		await win.webContents.session.clearCache();
	} catch (_) {
		/* ignore */
	}
	win.loadURL(coldStartNavigationUrl(), {
		extraHeaders: 'pragma: no-cache\ncache-control: no-cache\n',
	});
}

const gotSingleInstanceLock = app.requestSingleInstanceLock();
if (!gotSingleInstanceLock) {
	app.quit();
} else {
	/* preload が package.json を読めない構成でも、exe と同じバージョンを返す（ログイン前ゲートの nuElectron.appVersion 用） */
	ipcMain.on('nu-sync-app-version', function (event) {
		try {
			event.returnValue = app.getVersion();
		} catch (e) {
			event.returnValue = '';
		}
	});

	ipcMain.handle('nu-open-installer-download-url', async function (_event, rawUrl) {
		if (!rawUrl || typeof rawUrl !== 'string') {
			return { ok: false, error: 'bad_url' };
		}
		var trimmed = rawUrl.trim();
		var u;
		try {
			u = new URL(trimmed);
		} catch (e) {
			return { ok: false, error: 'bad_url' };
		}
		if (!isAllowedInstallerOpenExternalUrl(u.href)) {
			return { ok: false, error: 'forbidden' };
		}
		try {
			await shell.openExternal(u.href);
			return { ok: true };
		} catch (e2) {
			return { ok: false, error: String((e2 && e2.message) || e2) };
		}
	});

	app.on('second-instance', function (event, commandLine, workingDirectory) {
		var wins = BrowserWindow.getAllWindows();
		for (var i = 0; i < wins.length; i++) {
			var w = wins[i];
			if (!w || w.isDestroyed()) {
				continue;
			}
			if (w.isMinimized()) {
				w.restore();
			}
			w.focus();
			try {
				w.webContents.reloadIgnoringCache();
			} catch (e) {
				/* ignore */
			}
		}
	});

	ipcMain.on('nu-quit-app', function (event) {
		/*
		 * app.quit() だけだと、Windows ＋ ネイティブフルスクリーン等で
		 * ウィンドウ終了待ちに留まりプロセスが残ることがある。
		 * ユーザー明示の「終了」は即時に落とす。
		 */
		var senderWin = null;
		try {
			senderWin = BrowserWindow.fromWebContents(event.sender);
		} catch (_) {
			senderWin = null;
		}
		if (senderWin && !senderWin.isDestroyed()) {
			try {
				senderWin.setFullScreen(false);
			} catch (_) {
				/* ignore */
			}
		}
		function destroyAllAndExit() {
			try {
				var all = BrowserWindow.getAllWindows();
				for (var i = 0; i < all.length; i++) {
					var bw = all[i];
					if (bw && !bw.isDestroyed()) {
						bw.destroy();
					}
				}
			} catch (_) {
				/* ignore */
			}
			try {
				app.exit(0);
			} catch (_) {
				try {
					process.exit(0);
				} catch (_) {
					/* ignore */
				}
			}
		}
		/* setImmediate より setTimeout(0) の方が Windows 上でコールバックが抜ける報告が少ない */
		setTimeout(destroyAllAndExit, 0);
	});

	ipcMain.handle('nu-get-desktop-update-info', async function () {
		return fetchDesktopUpdateInfoForRenderer();
	});

	ipcMain.handle('nu-check-desktop-update', async function () {
		return fetchDesktopUpdateInfoForRenderer();
	});

	ipcMain.handle('nu-start-desktop-installer-download', async function (event) {
		var wc = event.sender;
		if (activeInstallerDownloadReq) {
			try {
				activeInstallerDownloadReq.destroy();
			} catch (e) {
				/* ignore */
			}
			activeInstallerDownloadReq = null;
		}
		if (lastInstallerDownloadPath) {
			try {
				if (fs.existsSync(lastInstallerDownloadPath)) {
					fs.unlinkSync(lastInstallerDownloadPath);
				}
			} catch (e) {
				/* ignore */
			}
			lastInstallerDownloadPath = null;
		}
		var payload = await fetchDesktopClientUpdatePayload();
		var current = app.getVersion();
		if (!payload || compareSemver(current, payload.latestVersion) >= 0) {
			return { ok: false, error: 'no_update' };
		}
		if (!isSafeRemoteInstallerUrl(payload.installerUrl)) {
			return { ok: false, error: 'bad_url' };
		}
		try {
			var dest = await downloadInstallerToTemp(payload.installerUrl, wc);
			lastInstallerDownloadPath = dest;
			return { ok: true, path: dest };
		} catch (e) {
			sendInstallerProgress(wc, { phase: 'error', message: String((e && e.message) || e) });
			return { ok: false, error: 'download_failed', message: String((e && e.message) || e) };
		}
	});

	ipcMain.handle('nu-run-downloaded-desktop-installer', async function (event, opts) {
		var p = lastInstallerDownloadPath;
		var tempRoot = app.getPath('temp');
		if (!p || typeof p !== 'string' || !p.startsWith(tempRoot) || !fs.existsSync(p)) {
			return { ok: false, error: 'no_file' };
		}
		var wc = event.sender;
		var navigateFirst = opts && opts.navigateToLoginFirst;
		if (navigateFirst && wc && !wc.isDestroyed()) {
			try {
				var loginHref = new URL('/login?nu_install=1', START_URL).href;
				wc.loadURL(loginHref, {
					extraHeaders: 'pragma: no-cache\ncache-control: no-cache\n',
				});
				await new Promise(function (resolve) {
					var settled = false;
					function settle() {
						if (settled) {
							return;
						}
						settled = true;
						resolve();
					}
					var timer = setTimeout(settle, 8000);
					wc.once('did-finish-load', function () {
						clearTimeout(timer);
						setTimeout(settle, 500);
					});
				});
			} catch (eNav) {
				/* ignore */
			}
		}
		try {
			spawn(p, [], { detached: true, stdio: 'ignore' });
		} catch (e) {
			return { ok: false, error: 'spawn_failed', message: String((e && e.message) || e) };
		}
		setTimeout(function () {
			try {
				app.exit(0);
			} catch (e2) {
				try {
					process.exit(0);
				} catch (e3) {
					/* ignore */
				}
			}
		}, 600);
		return { ok: true };
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
		 * ON も常に適用する（遷移直後の isFullScreen() 誤判定で掛からない件のため）。
		 */
		if (!flag) {
			clearFullscreenResyncTimers(w);
			w.setFullScreen(false);
			/* 非同期で状態が追いつかない環境向けに再試行。Windows では解除後に最大化だけ残ることもある。 */
			setImmediate(function () {
				if (!w || w.isDestroyed()) {
					return;
				}
				if (w.isFullScreen()) {
					w.setFullScreen(false);
				}
				unmaximizeIfNeededForWindowed(w);
			});
			return;
		}
		/*
		 * ON は常に適用する。遷移直後に実表示はウィンドウなのに isFullScreen() が true のまま残り、
		 * 差分判定で setFullScreen(true) がスキップされるとクリックなど別イベントまで直らない。
		 */
		w.setFullScreen(true);
	});

	app.whenReady().then(async () => {
		Menu.setApplicationMenu(null);
		try {
			await session.defaultSession.clearCache();
		} catch (_) {
			/* ignore */
		}
		await createWindow();
		app.on('activate', async () => {
			if (BrowserWindow.getAllWindows().length === 0) {
				await createWindow();
			}
		});
	});

	app.on('window-all-closed', () => {
		if (process.platform !== 'darwin') {
			app.quit();
		}
	});
}
