/**
 * 設定フォームの「だれかと対戦」通知をフルスクリーンと同様のトグルにし、hidden でサーバーへ送る。
 * ブラウザでは ON にしたタイミング（クリック＝ユーザージェスチャー）で通知許可を求める。
 */
(function () {
	if (window.__nuSettingsPvpNotifyToggleLoaded) {
		return;
	}
	window.__nuSettingsPvpNotifyToggleLoaded = true;

	function isElectronShell() {
		return typeof window.nuElectron !== 'undefined' && window.nuElectron;
	}

	function syncSwitch(btn, on) {
		btn.setAttribute('aria-checked', on ? 'true' : 'false');
		btn.classList.toggle('settings-toggle--on', on);
		btn.classList.toggle('settings-toggle--off', !on);
	}

	function init() {
		var btn = document.getElementById('pvpInviteNotifySwitch');
		var hidden = document.getElementById('pvpInviteNotifyHidden');
		if (!btn || !hidden) {
			return;
		}
		function readOn() {
			return hidden.value === 'true';
		}
		syncSwitch(btn, readOn());
		btn.addEventListener('click', function () {
			var next = !readOn();
			hidden.value = next ? 'true' : 'false';
			syncSwitch(btn, next);
			if (next && !isElectronShell() && 'Notification' in window && Notification.permission === 'default') {
				try {
					Notification.requestPermission();
				} catch (e) {
					/* ignore */
				}
			}
		});
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	} else {
		init();
	}
})();
