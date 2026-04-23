/**
 * 届いている対戦申し込みをポーリングし、右下にバナーを出す。
 * クリックで「だれかと対戦」へ。× は今の件数まで非表示（新規が来たら再表示）。
 * 設定で ON のとき、ブラウザのデスクトップ通知も送る（許可が必要）。
 */
(function () {
	if (window.__nuPvpInviteBannerLoaded) {
		return;
	}
	window.__nuPvpInviteBannerLoaded = true;

	var sc = document.currentScript;
	var pollUrl = sc && sc.dataset && sc.dataset.pollUrl ? String(sc.dataset.pollUrl) : '';
	var pvpMenuUrl = sc && sc.dataset && sc.dataset.pvpMenuUrl ? String(sc.dataset.pvpMenuUrl) : '';
	var rawNotify = sc && sc.dataset && sc.dataset.pvpInviteNotifyEnabled;
	/* 属性省略時は従来どおり ON */
	var notifyEnabled = rawNotify !== 'false';
	var notifyIconUrl = sc && sc.dataset && sc.dataset.pvpNotifyIconUrl ? String(sc.dataset.pvpNotifyIconUrl) : '';
	if (!pollUrl || !pvpMenuUrl) {
		return;
	}

	var STORAGE_KEY = 'nu_pvp_invite_banner_dismissed_count';
	var POLL_MS = 18000;
	var banner = null;
	var closeBtn = null;
	var pollTimer = null;
	var lastPolledCount = null;

	function pathForMatch() {
		var p = window.location.pathname || '';
		return p;
	}

	function isOnPvpSection() {
		return pathForMatch().indexOf('/battle/pvp') !== -1;
	}

	function readDismissed() {
		try {
			var v = sessionStorage.getItem(STORAGE_KEY);
			if (v == null || v === '') {
				return 0;
			}
			var n = parseInt(v, 10);
			return isNaN(n) ? 0 : Math.max(0, n);
		} catch (e) {
			return 0;
		}
	}

	function writeDismissed(n) {
		try {
			sessionStorage.setItem(STORAGE_KEY, String(Math.max(0, Math.floor(n))));
		} catch (e) {
			/* ignore */
		}
	}

	function tryDesktopNotify(count) {
		if (!notifyEnabled) {
			return;
		}
		var title = 'ナインユニバース：対戦の申し込み';
		var body = '「だれかと対戦」を開いて承諾してください。';
		/* Electron シェルはメインプロセスのネイティブ通知（Windows のトースト等） */
		if (
			typeof window.nuElectron !== 'undefined' &&
			window.nuElectron &&
			typeof window.nuElectron.showPvpInviteDesktopNotification === 'function'
		) {
			try {
				var p = window.nuElectron.showPvpInviteDesktopNotification(title, body);
				if (p && typeof p.then === 'function') {
					p.catch(function () {});
				}
			} catch (e) {
				/* ignore */
			}
			return;
		}
		if (!('Notification' in window)) {
			return;
		}
		function fire() {
			if (Notification.permission !== 'granted') {
				return;
			}
			try {
				var opts = {
					body: body,
					tag: 'nu-pvp-invite',
					renotify: true,
				};
				if (notifyIconUrl) {
					opts.icon = notifyIconUrl;
				}
				new Notification(title, opts);
			} catch (e) {
				/* ignore */
			}
		}
		if (Notification.permission === 'granted') {
			fire();
			return;
		}
		if (Notification.permission === 'denied') {
			return;
		}
		Notification.requestPermission().then(function (p) {
			if (p === 'granted') {
				fire();
			}
		});
	}

	function ensureBannerEl() {
		if (banner) {
			return;
		}
		banner = document.createElement('button');
		banner.type = 'button';
		banner.id = 'nu-pvp-invite-banner';
		banner.className = 'pvp-invite-banner';
		banner.setAttribute('hidden', '');
		banner.setAttribute('aria-live', 'polite');

		var body = document.createElement('span');
		body.className = 'pvp-invite-banner__body';
		var title = document.createElement('p');
		title.className = 'pvp-invite-banner__title';
		title.textContent = '対戦の申し込みを受け取りました';
		var hint = document.createElement('p');
		hint.className = 'pvp-invite-banner__hint';
		hint.textContent = 'タップして「だれかと対戦」へ';
		body.appendChild(title);
		body.appendChild(hint);

		closeBtn = document.createElement('button');
		closeBtn.type = 'button';
		closeBtn.className = 'pvp-invite-banner__close';
		closeBtn.setAttribute('aria-label', '閉じる');
		closeBtn.textContent = '×';

		banner.appendChild(body);
		banner.appendChild(closeBtn);
		document.body.appendChild(banner);

		banner.addEventListener('click', function (ev) {
			if (ev.target === closeBtn) {
				return;
			}
			window.location.href = pvpMenuUrl;
		});
		closeBtn.addEventListener('click', function (ev) {
			ev.preventDefault();
			ev.stopPropagation();
			var c = banner ? parseInt(banner.getAttribute('data-nu-count') || '0', 10) : 0;
			writeDismissed(isNaN(c) ? 0 : c);
			hideBanner();
		});
	}

	function showBanner(count) {
		if (!notifyEnabled) {
			return;
		}
		ensureBannerEl();
		banner.setAttribute('data-nu-count', String(count));
		banner.removeAttribute('hidden');
		banner.classList.add('is-visible');
	}

	function hideBanner() {
		if (!banner) {
			return;
		}
		banner.classList.remove('is-visible');
		banner.setAttribute('hidden', '');
	}

	function syncVisibility(count) {
		if (!notifyEnabled) {
			hideBanner();
			return;
		}
		if (isOnPvpSection()) {
			hideBanner();
			return;
		}
		var dismissed = readDismissed();
		/* 申請を処理して件数が減ったら「却下済み件数」が未来にならないよう合わせる */
		if (count < dismissed) {
			writeDismissed(count);
			dismissed = count;
		}
		if (count > dismissed) {
			showBanner(count);
		} else {
			hideBanner();
		}
	}

	function pollOnce() {
		if (isOnPvpSection()) {
			hideBanner();
			return;
		}
		fetch(pollUrl, {
			method: 'GET',
			credentials: 'same-origin',
			headers: { Accept: 'application/json' },
		})
			.then(function (res) {
				if (!res.ok) {
					return null;
				}
				return res.json();
			})
			.then(function (data) {
				if (!data || typeof data.count !== 'number') {
					return;
				}
				var c = Math.max(0, Math.floor(data.count));
				if (lastPolledCount !== null && c > lastPolledCount && notifyEnabled) {
					tryDesktopNotify(c);
				}
				lastPolledCount = c;
				syncVisibility(c);
			})
			.catch(function () {
				/* オフライン等は無視 */
			});
	}

	function start() {
		if (isOnPvpSection()) {
			return;
		}
		pollOnce();
		if (pollTimer != null) {
			clearInterval(pollTimer);
		}
		pollTimer = window.setInterval(pollOnce, POLL_MS);
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', start);
	} else {
		start();
	}
})();
