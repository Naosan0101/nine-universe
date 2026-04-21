/**
 * localStorage（nu_fullscreen_mode）でフルスクリーン希望を保存。未設定時は ON。
 * ブラウザでは Fullscreen API、Electron では BrowserWindow#setFullScreen を使う。
 * ページ遷移後はブラウザ仕様によりフルスクリーンは解除されます。
 */
(function () {
	var STORAGE_KEY = 'nu_fullscreen_mode';

	function defaultFromElectron() {
		if (
			typeof window.nuElectron !== 'undefined' &&
			typeof window.nuElectron.initialFullscreenPreference === 'boolean'
		) {
			return window.nuElectron.initialFullscreenPreference;
		}
		return true;
	}

	function readPreferred() {
		try {
			var v = localStorage.getItem(STORAGE_KEY);
			if (v === null || v === '') {
				return defaultFromElectron();
			}
			return v === '1' || v === 'true';
		} catch (e) {
			return true;
		}
	}

	function writePreferred(on) {
		try {
			localStorage.setItem(STORAGE_KEY, on ? '1' : '0');
		} catch (e) {
			/* ignore */
		}
	}

	function isFullscreen() {
		return !!(
			document.fullscreenElement ||
			document.webkitFullscreenElement ||
			document.msFullscreenElement
		);
	}

	function requestFullscreenEl(el) {
		if (!el) {
			return Promise.reject();
		}
		var req = el.requestFullscreen || el.webkitRequestFullscreen || el.msRequestFullscreen;
		if (req) {
			return Promise.resolve(req.call(el));
		}
		return Promise.reject();
	}

	function exitFullscreenDoc() {
		if (document.exitFullscreen) {
			return document.exitFullscreen();
		}
		if (document.webkitExitFullscreen) {
			return document.webkitExitFullscreen();
		}
		if (document.msExitFullscreen) {
			return document.msExitFullscreen();
		}
		return Promise.reject();
	}

	function isElectronShell() {
		return typeof window.nuElectron !== 'undefined' && typeof window.nuElectron.setFullScreen === 'function';
	}

	function applyPreferredFullscreen() {
		var want = readPreferred();
		if (isElectronShell()) {
			window.nuElectron.setFullScreen(want).catch(function () {});
			return;
		}
		if (!want) {
			exitFullscreenDoc().catch(function () {});
			return;
		}
		if (!isFullscreen()) {
			requestFullscreenEl(document.documentElement).catch(function () {});
		}
	}

	var firstGestureBound = false;
	function bindBrowserFullscreenFirstGesture() {
		if (firstGestureBound || isElectronShell()) {
			return;
		}
		firstGestureBound = true;
		function once() {
			document.removeEventListener('pointerdown', once, true);
			if (readPreferred() && !isFullscreen()) {
				requestFullscreenEl(document.documentElement).catch(function () {});
			}
		}
		document.addEventListener('pointerdown', once, true);
	}

	function syncSettingsSwitch(btn) {
		if (!btn) {
			return;
		}
		var on = readPreferred();
		btn.setAttribute('aria-checked', on ? 'true' : 'false');
		btn.classList.toggle('settings-toggle--on', on);
		btn.classList.toggle('settings-toggle--off', !on);
	}

	function wireSettingsSwitch() {
		var btn = document.getElementById('fullscreenSwitch');
		if (!btn) {
			return;
		}
		syncSettingsSwitch(btn);
		btn.addEventListener('click', function () {
			var next = !readPreferred();
			writePreferred(next);
			syncSettingsSwitch(btn);
			applyPreferredFullscreen();
			if (next && !isElectronShell()) {
				bindBrowserFullscreenFirstGesture();
			}
		});
	}

	function init() {
		wireSettingsSwitch();
		applyPreferredFullscreen();
		if (readPreferred() && !isElectronShell() && !isFullscreen()) {
			bindBrowserFullscreenFirstGesture();
		}
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	} else {
		init();
	}

	window.nuFullscreenMode = {
		isPreferred: readPreferred,
		setPreferred: writePreferred,
		applyPreferredFullscreen: applyPreferredFullscreen
	};
})();
