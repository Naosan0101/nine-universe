/**
 * デスクトップ版（Electron）: 任意更新の案内・インストーラ取得・起動。
 * バトル中・パック開封結果画面ではページ遷移が入るまでポップアップを遅延する。
 */
(function () {
	var STORAGE_PENDING = 'nuDesktopOptionalUpdatePending';
	var STORAGE_DISMISS = 'nuDesktopOptionalUpdateDismissed';

	function hasElectronApis() {
		var n = window.nuElectron;
		return (
			n &&
			typeof n.getDesktopUpdateInfo === 'function' &&
			typeof n.startDesktopInstallerDownload === 'function' &&
			typeof n.runDownloadedDesktopInstaller === 'function'
		);
	}

	function pathDefersOptionalDesktopUpdate(pathname) {
		var p = pathname || '';
		return (
			p.indexOf('/battle/cpu/play') !== -1 ||
			p.indexOf('/battle/pvp/play') !== -1 ||
			p.indexOf('/pack/opening') !== -1 ||
			p.indexOf('/pack/result') !== -1
		);
	}

	function isForcedGateActive() {
		return document.documentElement.classList.contains('nu-has-desktop-app-update-gate');
	}

	var currentInfo = null;
	var unsubProgress = null;

	function getEl(id) {
		return document.getElementById(id);
	}

	function setBar(bar, pctLabel, received, total) {
		if (!bar) {
			return;
		}
		if (!total || total <= 0) {
			bar.classList.add('nu-desktop-opt-update__fill--busy');
			bar.style.width = '';
			if (pctLabel) {
				pctLabel.textContent = '取得中…';
			}
			return;
		}
		bar.classList.remove('nu-desktop-opt-update__fill--busy');
		var pct = Math.min(100, Math.round((received / total) * 100));
		bar.style.width = pct + '%';
		if (pctLabel) {
			pctLabel.textContent = pct + '%';
		}
	}

	function showOverlay(root) {
		root.removeAttribute('hidden');
	}

	function hideOverlay(root) {
		root.setAttribute('hidden', 'hidden');
	}

	function showStep(stepName) {
		var n = getEl('nu-desktop-opt-step-notice');
		var p = getEl('nu-desktop-opt-step-progress');
		var e = getEl('nu-desktop-opt-step-error');
		if (n) {
			n.hidden = stepName !== 'notice';
		}
		if (p) {
			p.hidden = stepName !== 'progress';
		}
		if (e) {
			e.hidden = stepName !== 'error';
		}
	}

	function bindOverlayOnce() {
		var root = getEl('nu-desktop-opt-update-root');
		if (!root || root.dataset.bound === '1') {
			return;
		}
		root.dataset.bound = '1';

		var btnInstall = getEl('nu-desktop-opt-btn-install');
		var btnLater = getEl('nu-desktop-opt-btn-later');
		var btnRetry = getEl('nu-desktop-opt-btn-retry');
		var btnCloseErr = getEl('nu-desktop-opt-btn-close-err');

		function clearUnsub() {
			if (typeof unsubProgress === 'function') {
				unsubProgress();
				unsubProgress = null;
			}
		}

		function runDownloadFlow() {
			showStep('progress');
			var bar = getEl('nu-desktop-opt-bar');
			var pct = getEl('nu-desktop-opt-pct');
			var msg = getEl('nu-desktop-opt-progress-msg');
			if (msg) {
				msg.textContent =
					'最新のセットアップをダウンロードしています。完了するとログイン画面へ移り、インストーラが起動します。';
			}
			setBar(bar, pct, 0, 0);
			clearUnsub();
			if (window.nuElectron && typeof window.nuElectron.onDesktopInstallerProgress === 'function') {
				unsubProgress = window.nuElectron.onDesktopInstallerProgress(function (payload) {
					if (!payload) {
						return;
					}
					if (payload.phase === 'downloading') {
						setBar(bar, pct, payload.received || 0, payload.total || 0);
					}
					if (payload.phase === 'complete') {
						var t = payload.total || payload.received || 1;
						setBar(bar, pct, t, t);
						if (pct) {
							pct.textContent = '100%';
						}
						bar.classList.remove('nu-desktop-opt-update__fill--busy');
						setTimeout(function () {
							if (!window.nuElectron || typeof window.nuElectron.runDownloadedDesktopInstaller !== 'function') {
								return;
							}
							window.nuElectron
								.runDownloadedDesktopInstaller({ navigateToLoginFirst: true })
								.catch(function () {});
						}, 900);
					}
					if (payload.phase === 'error') {
						clearUnsub();
						var em = getEl('nu-desktop-opt-err-msg');
						if (em) {
							em.textContent = String(payload.message || '通信エラー');
						}
						showStep('error');
					}
				});
			}
			window.nuElectron
				.startDesktopInstallerDownload()
				.then(function (res) {
					if (!res || !res.ok) {
						clearUnsub();
						var em = getEl('nu-desktop-opt-err-msg');
						if (em) {
							em.textContent =
								res && res.error === 'no_update'
									? 'もう最新の状態のようです。ページを再読み込みしてください。'
									: 'ダウンロードを開始できませんでした。';
						}
						showStep('error');
					}
				})
				.catch(function () {
					clearUnsub();
					var em = getEl('nu-desktop-opt-err-msg');
					if (em) {
						em.textContent = 'ネットワークエラーが発生しました。';
					}
					showStep('error');
				});
		}

		if (btnInstall) {
			btnInstall.addEventListener('click', function () {
				runDownloadFlow();
			});
		}
		if (btnLater) {
			btnLater.addEventListener('click', function () {
				if (currentInfo && currentInfo.latestVersion) {
					try {
						sessionStorage.setItem(STORAGE_DISMISS, String(currentInfo.latestVersion));
					} catch (e) {
						/* ignore */
					}
				}
				try {
					sessionStorage.removeItem(STORAGE_PENDING);
				} catch (e2) {
					/* ignore */
				}
				hideOverlay(root);
			});
		}
		if (btnRetry) {
			btnRetry.addEventListener('click', function () {
				runDownloadFlow();
			});
		}
		if (btnCloseErr) {
			btnCloseErr.addEventListener('click', function () {
				showStep('notice');
				hideOverlay(root);
			});
		}
	}

	function openNoticeOverlay(info) {
		currentInfo = info;
		var root = getEl('nu-desktop-opt-update-root');
		if (!root) {
			return;
		}
		bindOverlayOnce();
		showStep('notice');
		var v = getEl('nu-desktop-opt-vers');
		if (v) {
			v.textContent =
				'この端末: v' + (info.currentVersion || '?') + '　／　配布の最新: v' + (info.latestVersion || '?');
		}
		showOverlay(root);
	}

	async function runFlow() {
		if (!hasElectronApis()) {
			return;
		}
		if (isForcedGateActive()) {
			return;
		}

		var info = await window.nuElectron.getDesktopUpdateInfo();
		if (!info || !info.needsUpdate) {
			try {
				sessionStorage.removeItem(STORAGE_PENDING);
			} catch (e) {
				/* ignore */
			}
			return;
		}

		var dismissed = null;
		try {
			dismissed = sessionStorage.getItem(STORAGE_DISMISS);
		} catch (e) {
			dismissed = null;
		}
		if (dismissed && dismissed === String(info.latestVersion)) {
			return;
		}

		var path = window.location.pathname || '';
		if (pathDefersOptionalDesktopUpdate(path)) {
			try {
				sessionStorage.setItem(STORAGE_PENDING, '1');
			} catch (e2) {
				/* ignore */
			}
			return;
		}

		try {
			sessionStorage.removeItem(STORAGE_PENDING);
		} catch (e3) {
			/* ignore */
		}

		openNoticeOverlay(info);
	}

	function scheduleRun() {
		if (document.readyState === 'loading') {
			document.addEventListener('DOMContentLoaded', function () {
				runFlow().catch(function () {});
			});
		} else {
			runFlow().catch(function () {});
		}
	}

	scheduleRun();

	window.addEventListener(
		'pageshow',
		function (ev) {
			if (ev.persisted) {
				runFlow().catch(function () {});
			}
		},
		false
	);
})();

