(function () {
	const app = document.getElementById('battle-app');
	if (!app) return;

	const battleLogModal = document.getElementById('battle-log-modal');
	const battleLogList = document.getElementById('battle-log-list');
	const battleLogOpenBtn = document.getElementById('battle-log-open');
	const battleLogCloseBtn = document.getElementById('battle-log-close');
	let lastEventLog = [];

	const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
	const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';
	const cardBack = document.querySelector('meta[name="card_back"]')?.getAttribute('content') || '';
	const contextPath = document.querySelector('meta[name="nine_universe_context_path"]')?.getAttribute('content') || '';
	const plateFbFull = document.querySelector('meta[name="card_plate_fallback"]')?.getAttribute('content') || '';
	const dataFbFull = document.querySelector('meta[name="card_data_fallback"]')?.getAttribute('content') || '';
	const pvpMatchId = document.querySelector('meta[name="pvp_match_id"]')?.getAttribute('content') || '';
	const battleIsPvp = pvpMatchId.length > 0;
	const battleApiBase = battleIsPvp ? (contextPath + '/battle/pvp/api/' + encodeURIComponent(pvpMatchId)) : null;
	const cpuThinkSpeedMeta = document.querySelector('meta[name="cpu_think_speed"]')?.getAttribute('content') || 'NORMAL';
	function cpuThinkWaitMs() {
		const raw = String(cpuThinkSpeedMeta || '').trim().toUpperCase();
		if (raw === 'FAST') {
			return 2000 + Math.floor(Math.random() * 1000);
		}
		if (raw === 'SLOW') {
			return 6000 + Math.floor(Math.random() * 2000);
		}
		return 3000 + Math.floor(Math.random() * 4000);
	}
	/** {@code battle-deck-select.js} と同じキー（降参後もデッキ選択の先頭に使う） */
	const LAST_BATTLE_DECK_STORAGE_KEY = battleIsPvp ? 'nu.lastBattleDeckId' : 'nu.lastCpuBattleDeckId';

	function persistLastBattleDeckIdForMenu() {
		var meta = document.querySelector('meta[name="my_battle_deck_id"]');
		var id = meta && meta.getAttribute('content');
		if (id == null || String(id).trim() === '') {
			return;
		}
		try {
			localStorage.setItem(LAST_BATTLE_DECK_STORAGE_KEY, String(id).trim());
		} catch (e) {
			/* private mode 等 */
		}
	}

	// Intercept surrender immediately (before async init finishes).
	const surrenderGuard = {
		installed: false,
		submitting: false
	};

	function absUrl(path) {
		if (path == null || path === '') return '';
		const p = String(path);
		if (p.startsWith('http://') || p.startsWith('https://')) return p;
		return contextPath + p;
	}

	function cssEscapeForBgUrl(u) {
		return String(u).replace(/\\/g, '\\\\').replace(/"/g, '\\"');
	}

	function fieldBackdropKeyFromState(activeField) {
		if (!activeField || activeField.cardId == null) return '';
		return String(activeField.cardId) + '\x1f' + String(activeField.instanceId != null ? activeField.instanceId : '');
	}

	function fieldBackdropBackgroundFromDef(d) {
		if (!d) return { image: '', n: 0 };
		const portrait = d.layerPortraitPath || d.layerPortrait || '';
		const base = d.layerBasePath || d.layerBase || '';
		const parts = [];
		if (portrait) parts.push('url("' + cssEscapeForBgUrl(absUrl(portrait)) + '")');
		if (base) parts.push('url("' + cssEscapeForBgUrl(absUrl(base)) + '")');
		return { image: parts.join(', '), n: parts.length };
	}

	function ensureBattleFieldBackdropLayer() {
		const page = document.querySelector('.page--battle');
		if (!page) return null;
		let layer = document.getElementById('battle-field-backdrop');
		if (!layer) {
			layer = document.createElement('div');
			layer.id = 'battle-field-backdrop';
			layer.className = 'battle-field-backdrop';
			layer.setAttribute('aria-hidden', 'true');
			page.insertBefore(layer, page.firstChild);
		}
		return layer;
	}

	function updateBattleFieldBackdrop(activeField, defs) {
		const page = document.querySelector('.page--battle');
		const layer = ensureBattleFieldBackdropLayer();
		if (!page || !layer) return;

		const key = fieldBackdropKeyFromState(activeField);
		const prevKey = ui._fieldBackdropKey != null ? ui._fieldBackdropKey : '';

		if (!activeField || activeField.cardId == null) {
			if (ui._fieldBackdropBurnEnd) {
				layer.removeEventListener('animationend', ui._fieldBackdropBurnEnd);
				ui._fieldBackdropBurnEnd = null;
			}
			page.classList.remove('page--battle-field-active');
			layer.classList.remove('battle-field-backdrop--burn');
			layer.style.backgroundImage = '';
			layer.style.backgroundSize = '';
			layer.style.backgroundPosition = '';
			layer.style.backgroundRepeat = '';
			ui._fieldBackdropKey = '';
			return;
		}

		const d = resolveCardDef(defs, activeField.cardId);
		const bg = fieldBackdropBackgroundFromDef(d);
		if (!bg.image) {
			if (ui._fieldBackdropBurnEnd) {
				layer.removeEventListener('animationend', ui._fieldBackdropBurnEnd);
				ui._fieldBackdropBurnEnd = null;
			}
			page.classList.remove('page--battle-field-active');
			layer.classList.remove('battle-field-backdrop--burn');
			layer.style.backgroundImage = '';
			layer.style.backgroundSize = '';
			layer.style.backgroundPosition = '';
			layer.style.backgroundRepeat = '';
			ui._fieldBackdropKey = '';
			return;
		}

		layer.style.backgroundImage = bg.image;
		if (bg.n > 1) {
			layer.style.backgroundSize = 'cover, cover';
			layer.style.backgroundPosition = 'center, center';
		} else {
			layer.style.backgroundSize = 'cover';
			layer.style.backgroundPosition = 'center';
		}
		layer.style.backgroundRepeat = 'no-repeat';

		page.classList.add('page--battle-field-active');
		ui._fieldBackdropKey = key;

		if (key !== prevKey) {
			if (ui._fieldBackdropBurnEnd) {
				layer.removeEventListener('animationend', ui._fieldBackdropBurnEnd);
				ui._fieldBackdropBurnEnd = null;
			}
			layer.classList.remove('battle-field-backdrop--burn');
			void layer.offsetWidth;
			layer.classList.add('battle-field-backdrop--burn');
			const onEnd = function (e) {
				if (!e || e.target !== layer) return;
				const nm = String(e.animationName || '');
				if (nm.indexOf('battle-field-burn-in') < 0) return;
				layer.removeEventListener('animationend', onEnd);
				ui._fieldBackdropBurnEnd = null;
				layer.classList.remove('battle-field-backdrop--burn');
			};
			ui._fieldBackdropBurnEnd = onEnd;
			layer.addEventListener('animationend', onEnd);
		}
	}

	// UI state (server state is fetched separately)
	const ui = {
		selectedInstanceId: null,
		levelUpRest: 0,
		levelUpStones: 0,
		levelUpDiscardIds: [],
		pay: { stones: 0, cardInstanceIds: [] },
		_cpuThinkTimer: null,
		_resolveTimer: null,
		/** HUMAN_EFFECT_PENDING 用: 同じ pendingEffect では resolve 用 3 秒タイマーを再描画でリセットしない */
		_resolveEffectWaitKey: null,
		_prevPowerByInstanceId: Object.create(null),
		warnLevelUpRest: null,
		warnLevelUpStone: null,
		sparkLevelUpRest: false,
		sparkLevelUpStone: false,
		_luPrevPowerInstanceId: null,
		_luPrevPower: null,
		_resultShown: false,
		_resultModalEl: null,
		_pvpPollTimer: null,
		_turnTimer: null,
		_lastTurnNotifyKey: null,
		_turnPopupTimer: null,
		_effectPopupEl: null,
		_powerContributorPopupEl: null,
		_powerContributorPopupHideTimer: null,
		/** 忍者配置: 入れ替え resolve を 3 秒待ちせず一度だけ起動する */
		_ninjaAutoResolveInFlight: false,
		/** 直前描画の共有〈フィールド〉（背景アニメ用キー） */
		_fieldBackdropKey: null,
		_fieldBackdropBurnEnd: null
	};

	const turnTimer = {
		key: null,
		warned30: false,
		warned15: false,
		firing: false
	};

	function pad2(n) {
		const s = String(Math.max(0, n | 0));
		return s.length >= 2 ? s : ('0' + s);
	}

	function fmtMmSs(totalSec) {
		const s = Math.max(0, totalSec | 0);
		const m = Math.floor(s / 60);
		const r = s % 60;
		return pad2(m) + ':' + pad2(r);
	}

	function ensureBattleToast() {
		let t = document.getElementById('battle-toast');
		if (t) return t;
		t = el('div', 'battle-toast', '');
		t.id = 'battle-toast';
		t.hidden = true;
		document.body.appendChild(t);
		return t;
	}

	function showBattleToast(text, kind) {
		const t = ensureBattleToast();
		t.classList.remove('battle-toast--warn', 'battle-toast--danger');
		if (kind) t.classList.add('battle-toast--' + kind);
		t.textContent = text || '';
		t.hidden = false;
		window.setTimeout(function () {
			t.hidden = true;
		}, 2600);
	}

	/** ターン表示をバトル領域に載せ、ビューポート固定（スクロール追従）にしない */
	function ensureBattleTurnPopupHost() {
		if (app.dataset.turnPopupHostReady === '1') {
			const h = document.getElementById('battle-turn-popup-host');
			return h || app.parentElement;
		}
		const parent = app.parentNode;
		if (!parent) return app;
		const host = document.createElement('div');
		host.id = 'battle-turn-popup-host';
		host.className = 'battle-turn-popup-host';
		parent.insertBefore(host, app);
		host.appendChild(app);
		app.dataset.turnPopupHostReady = '1';
		return host;
	}

	function showTurnChangePopup(isHumanTurn) {
		const text = isHumanTurn ? 'あなたのターン' : '相手のターン';
		const host = ensureBattleTurnPopupHost();
		let el = document.getElementById('battle-turn-popup');
		if (!el) {
			el = document.createElement('div');
			el.id = 'battle-turn-popup';
			el.setAttribute('role', 'status');
			el.setAttribute('aria-live', 'polite');
		}
		if (el.parentNode !== host) {
			host.appendChild(el);
		}
		if (ui._turnPopupTimer != null) {
			clearTimeout(ui._turnPopupTimer);
			ui._turnPopupTimer = null;
		}
		el.className =
			'battle-turn-popup' +
			(isHumanTurn ? ' battle-turn-popup--yours' : ' battle-turn-popup--opp');
		el.textContent = text;
		el.hidden = false;
		el.classList.remove('is-visible');
		void el.offsetWidth;
		el.classList.add('is-visible');
		ui._turnPopupTimer = window.setTimeout(function () {
			ui._turnPopupTimer = null;
			el.classList.remove('is-visible');
			window.setTimeout(function () {
				if (el && !el.classList.contains('is-visible')) {
					el.hidden = true;
				}
			}, 320);
		}, 2200);
	}

	/**
	 * ターン開始（turnStartedAtMs 更新）ごとに一度だけ通知。
	 */
	function syncTurnChangeNotice(st) {
		if (!st || st.gameOver) {
			if (st && st.gameOver) {
				ui._lastTurnNotifyKey = null;
			}
			return;
		}
		const key = String(st.turnStartedAtMs || 0) + '|' + (st.humansTurn ? '1' : '0');
		if (ui._lastTurnNotifyKey === key) {
			return;
		}
		ui._lastTurnNotifyKey = key;
		showTurnChangePopup(!!st.humansTurn);
	}

	const unwinnableDeployNotice = {
		timer: null,
		armedKey: null,
		shownKey: null,
		popEl: null
	};

	/** 敗北モーダル閉鎖後の「画面右・降参」案内（{@link #showUnwinnableDeployPop} と同レイアウト） */
	const postDefeatSurrenderAside = { el: null };

	function hidePostDefeatSurrenderAside() {
		if (postDefeatSurrenderAside.el && postDefeatSurrenderAside.el.parentNode) {
			postDefeatSurrenderAside.el.remove();
		}
		postDefeatSurrenderAside.el = null;
	}

	function showPostDefeatSurrenderAside() {
		hidePostDefeatSurrenderAside();
		const panel = el('aside', 'battle-unwinnable-pop');
		panel.setAttribute('role', 'status');
		panel.setAttribute('aria-live', 'polite');
		panel.appendChild(el('p', 'battle-unwinnable-pop__title', 'バトルは終了しました'));
		panel.appendChild(el(
			'p',
			'battle-unwinnable-pop__body',
			'メニューへ戻るには降参してください。'
		));
		const btn = el('button', 'btn btn--danger', '降参');
		btn.type = 'button';
		btn.addEventListener('click', function () {
			hidePostDefeatSurrenderAside();
			submitSurrenderWithoutConfirm();
		});
		panel.appendChild(btn);
		document.body.appendChild(panel);
		postDefeatSurrenderAside.el = panel;
	}

	function hideUnwinnableDeployPop() {
		if (unwinnableDeployNotice.popEl && unwinnableDeployNotice.popEl.parentNode) {
			unwinnableDeployNotice.popEl.remove();
		}
		unwinnableDeployNotice.popEl = null;
	}

	function showUnwinnableDeployPop() {
		hideUnwinnableDeployPop();
		const panel = el('aside', 'battle-unwinnable-pop');
		panel.setAttribute('role', 'status');
		panel.setAttribute('aria-live', 'polite');
		panel.appendChild(el('p', 'battle-unwinnable-pop__title', 'この手番では勝てません'));
		panel.appendChild(el(
			'p',
			'battle-unwinnable-pop__body',
			'相手バトルゾーンのファイターの強さ以上になる配置は、レベルアップやカード効果を含めて存在しません。'
		));
		const btn = el('button', 'btn btn--danger', '降参');
		btn.type = 'button';
		btn.addEventListener('click', function () {
			hideUnwinnableDeployPop();
			submitSurrenderWithoutConfirm();
		});
		panel.appendChild(btn);
		document.body.appendChild(panel);
		unwinnableDeployNotice.popEl = panel;
	}

	/**
	 * サーバが noLegalDeploy を返したとき、2秒後に右側へ通知（同一手番では1回だけ）。
	 */
	function syncUnwinnableDeployNotice(st) {
		const want =
			!!(
				st &&
				st.noLegalDeploy &&
				!st.gameOver &&
				st.humansTurn &&
				String(st.phase || '') === 'HUMAN_INPUT'
			);
		const key = want ? 'uw|' + String(st.turnStartedAtMs || 0) + '|' + String(st.phase || '') : null;

		if (!want) {
			if (unwinnableDeployNotice.timer != null) {
				clearTimeout(unwinnableDeployNotice.timer);
				unwinnableDeployNotice.timer = null;
			}
			unwinnableDeployNotice.armedKey = null;
			unwinnableDeployNotice.shownKey = null;
			hideUnwinnableDeployPop();
			return;
		}

		if (unwinnableDeployNotice.shownKey === key) {
			return;
		}
		if (unwinnableDeployNotice.armedKey === key && unwinnableDeployNotice.timer != null) {
			return;
		}

		if (unwinnableDeployNotice.timer != null) {
			clearTimeout(unwinnableDeployNotice.timer);
			unwinnableDeployNotice.timer = null;
		}
		hideUnwinnableDeployPop();
		unwinnableDeployNotice.shownKey = null;
		unwinnableDeployNotice.armedKey = key;

		const fireKey = key;
		unwinnableDeployNotice.timer = window.setTimeout(function () {
			unwinnableDeployNotice.timer = null;
			const cur = lastStateForHandPower;
			const curWant =
				!!(
					cur &&
					cur.noLegalDeploy &&
					!cur.gameOver &&
					cur.humansTurn &&
					String(cur.phase || '') === 'HUMAN_INPUT'
				);
			const curKey = curWant ? 'uw|' + String(cur.turnStartedAtMs || 0) + '|' + String(cur.phase || '') : null;
			if (curKey !== fireKey || !curWant) {
				return;
			}
			showUnwinnableDeployPop();
			unwinnableDeployNotice.shownKey = fireKey;
		}, 2000);
	}

	function teardownResultModal() {
		if (ui._resultModalEl && ui._resultModalEl.parentNode) {
			ui._resultModalEl.remove();
		}
		ui._resultModalEl = null;
	}

	function teardownSurrenderConfirmModal() {
		if (ui._surrenderConfirmEscapeHandler) {
			document.removeEventListener('keydown', ui._surrenderConfirmEscapeHandler, true);
			ui._surrenderConfirmEscapeHandler = null;
		}
		if (ui._surrenderConfirmEl && ui._surrenderConfirmEl.parentNode) {
			ui._surrenderConfirmEl.remove();
		}
		ui._surrenderConfirmEl = null;
		hidePostDefeatSurrenderAside();
	}

	function showResultModal(kind, title, detail, options) {
		const o = options || {};
		teardownSurrenderConfirmModal();
		teardownResultModal();
		hideBattleCardTooltip();
		hideBattleDeckTooltip();

		const overlay = el('div', 'battle-result-modal battle-result-modal--' + kind);
		overlay.setAttribute('role', 'dialog');
		overlay.setAttribute('aria-modal', 'true');
		overlay.setAttribute('aria-label', title || '結果');

		if (o.showy) {
			overlay.classList.add('battle-result-modal--showy');
			const burst = el('div', 'battle-result-modal__burst', null);
			burst.setAttribute('aria-hidden', 'true');
			overlay.appendChild(burst);
		}

		const panel = el('div', 'battle-result-modal__panel');
		const h = el('h2', 'battle-result-modal__title', title || '');
		const d = el('p', 'battle-result-modal__detail muted', detail || '');
		panel.appendChild(h);
		panel.appendChild(d);

		const actions = el('div', 'battle-result-modal__actions');
		const close = el('button', 'btn btn--ghost', '閉じる');
		close.type = 'button';
		actions.appendChild(close);
		panel.appendChild(actions);

		overlay.appendChild(panel);
		document.body.appendChild(overlay);
		ui._resultModalEl = overlay;

		function onClose() {
			teardownResultModal();
			if (typeof o.onClose === 'function') {
				o.onClose();
			}
		}
		close.addEventListener('click', onClose);
		// IMPORTANT: result modal must stay until user presses a button.
		// So we intentionally do NOT close on backdrop click or Escape.
		close.focus();
	}

	function showSurrenderConfirmModal(form) {
		teardownSurrenderConfirmModal();
		teardownResultModal();
		hideBattleCardTooltip();
		hideBattleDeckTooltip();

		const overlay = el('div', 'battle-result-modal battle-result-modal--defeat battle-surrender-confirm');
		overlay.setAttribute('role', 'dialog');
		overlay.setAttribute('aria-modal', 'true');
		overlay.setAttribute('aria-labelledby', 'battle-surrender-confirm-title');

		const panel = el('div', 'battle-result-modal__panel');
		const h = el('h2', 'battle-result-modal__title', '降参しますか？');
		h.id = 'battle-surrender-confirm-title';
		panel.appendChild(h);

		const actions = el('div', 'battle-result-modal__actions');
		const cancel = el('button', 'btn btn--ghost', 'キャンセル');
		cancel.type = 'button';
		const confirmBtn = el('button', 'btn btn--danger', '降参する');
		confirmBtn.type = 'button';
		actions.appendChild(cancel);
		actions.appendChild(confirmBtn);
		panel.appendChild(actions);

		overlay.appendChild(panel);
		document.body.appendChild(overlay);
		ui._surrenderConfirmEl = overlay;

		function close() {
			teardownSurrenderConfirmModal();
		}
		function doSurrender() {
			surrenderGuard.submitting = true;
			persistLastBattleDeckIdForMenu();
			teardownSurrenderConfirmModal();
			try {
				form.submit();
			} catch (_) {
				/* ignore */
			}
		}
		cancel.addEventListener('click', close);
		confirmBtn.addEventListener('click', doSurrender);
		overlay.addEventListener('click', function (e) {
			if (e.target === overlay) {
				close();
			}
		});
		ui._surrenderConfirmEscapeHandler = function (e) {
			if (e.key === 'Escape') {
				e.preventDefault();
				e.stopPropagation();
				close();
			}
		};
		document.addEventListener('keydown', ui._surrenderConfirmEscapeHandler, true);
		cancel.focus();
	}

	function isSurrenderForm(el) {
		if (!(el instanceof HTMLFormElement)) return false;
		const raw = (el.getAttribute('action') || '').trim();
		// Ignore query/hash and tolerate absolute/relative/context-prefixed URLs.
		const action = raw.split('#')[0].split('?')[0];
		if (action.indexOf('/battle/cpu/surrender') >= 0) return true;
		return /\/battle\/pvp\/api\/[^/]+\/surrender$/.test(action);
	}

	/**
	 * 右上の降参ボタン以外（「この手番では勝てません」等）からの即時降参。確認モーダルは出さない。
	 * {@link #installSurrenderIntercept} は submit ボタンのクリック／submit イベントのみ対象のため、
	 * {@code HTMLFormElement#submit()} はそのまま POST される。
	 */
	function submitSurrenderWithoutConfirm() {
		const form = document.querySelector('.topbar--battle form[action*="surrender"]');
		if (!(form instanceof HTMLFormElement) || !isSurrenderForm(form)) {
			return;
		}
		surrenderGuard.submitting = true;
		persistLastBattleDeckIdForMenu();
		try {
			form.submit();
		} catch (_) {
			/* ignore */
		}
	}

	function installSurrenderIntercept() {
		if (surrenderGuard.installed) return;
		surrenderGuard.installed = true;

		function openSurrenderModalAndBlock(form) {
			if (!isSurrenderForm(form)) return;
			if (surrenderGuard.submitting) return;
			showSurrenderConfirmModal(form);
		}

		// Capture click on the surrender submit button to block navigation even if submit event
		// is bypassed (e.g. by other handlers calling form.submit()).
		document.addEventListener(
			'click',
			function (e) {
				const t = e.target;
				if (!(t instanceof Element)) return;
				const form = t.closest('form');
				if (!isSurrenderForm(form)) return;
				// Only intercept genuine submit triggers.
				const isSubmitTrigger =
					(t.closest('button[type="submit"]') != null) ||
					(t instanceof HTMLInputElement && (t.type || '').toLowerCase() === 'submit');
				if (!isSubmitTrigger) return;
				if (surrenderGuard.submitting) return;
				e.preventDefault();
				e.stopPropagation();
				openSurrenderModalAndBlock(form);
			},
			true
		);

		document.addEventListener(
			'submit',
			function (e) {
				const t = e.target;
				if (!isSurrenderForm(t)) return;
				if (surrenderGuard.submitting) return;
				e.preventDefault();
				openSurrenderModalAndBlock(t);
			},
			true // capture: run even if other handlers exist
		);
	}

	function maybeShowGameOverModal(st) {
		if (!st) return;
		if (!st.gameOver) {
			ui._resultShown = false;
			teardownSurrenderConfirmModal();
			teardownResultModal();
			return;
		}
		if (ui._resultShown) return;
		ui._resultShown = true;

		if (st.myBattleDeckId != null && String(st.myBattleDeckId).trim() !== '') {
			try {
				localStorage.setItem(LAST_BATTLE_DECK_STORAGE_KEY, String(st.myBattleDeckId).trim());
			} catch (e) {
				/* ignore */
			}
		}

		const msg = st.lastMessage != null ? String(st.lastMessage) : '';
		if (st.humanWon) {
			const showy = msg.indexOf('勝利（CPUが相手以上のファイターを出せません）') >= 0;
			showResultModal('victory', '勝利', msg || '勝利しました。', {
				showy: showy,
				onClose: function () {
					// Return to home on victory close.
					window.location.href = contextPath + '/';
				}
			});
		} else {
			showResultModal('defeat', '敗北', msg || '敗北しました。', {
				onClose: function () {
					showPostDefeatSurrenderAside();
				}
			});
		}
	}

	/** CardDefDto → card-face-layer.js（ライブラリと同一テンプレート） */
	function buildBattleCardFaceShell(d, variant) {
		if (typeof buildLibraryCardFace !== 'function') {
			const err = document.createElement('p');
			err.className = 'muted';
			err.textContent = 'カード表示用スクリプトの読み込みに失敗しています。';
			return err;
		}
		const face = buildLibraryCardFace(d, {
			contextPath: contextPath,
			plateFallback: plateFbFull,
			dataFallback: dataFbFull,
			extraRootClasses: 'battle-layered battle-layered--' + variant
		});
		wireLibraryCardFaceImages(face, plateFbFull, dataFbFull);
		applyLibraryCardFaceSpark(face, d.rarity);
		return wrapLibraryCardInner(face);
	}

	/** ライブラリグリッドの library-card と同じ内側ラッパ（枠・はみ出し抑制） */
	function wrapLibraryCardInner(face) {
		const shell = document.createElement('div');
		shell.className = 'library-card__inner';
		shell.appendChild(face);
		return shell;
	}

	/**
	 * fragments/library-card.html と同じ階層: library-card__open > library-card__inner（内側は既に inner）
	 * バトルゾーンのツールチップ用に外側は .battle-zone-card と兼用
	 */
	function wrapLibraryCardOpenChrome(inner) {
		const open = document.createElement('div');
		open.className = 'library-card__open library-card__open--battle';
		open.appendChild(inner);
		return open;
	}

	/** JSON の defs キーが数値/文字列どちらでも解決 */
	function resolveCardDef(defs, cardId) {
		if (defs == null || cardId == null) return null;
		if (defs[cardId] != null) return defs[cardId];
		const n = Number(cardId);
		if (!Number.isNaN(n) && defs[n] != null) return defs[n];
		const s = String(cardId);
		if (defs[s] != null) return defs[s];
		return null;
	}

	/** 炭鉱夫で手札に戻ったインスタンスはカード面・ツールチップを「効果なし。」に */
	function defWithBlankEffectsIfNeeded(d, battleCard) {
		if (!d) return d;
		const blank = battleCard && (battleCard.blankEffects === true || battleCard.blankEffects === 'true');
		if (!blank) return d;
		return Object.assign({}, d, { abilityBlocks: [{ headline: '', body: '効果なし。' }] });
	}

	function resolveLayerBarPathForTribe(defs, tribeCode) {
		if (!defs || !tribeCode) return null;
		const want = String(tribeCode).trim().toUpperCase();
		for (const k in defs) {
			if (!Object.prototype.hasOwnProperty.call(defs, k)) continue;
			const cd = defs[k];
			if (cd && cd.attribute === want && cd.layerBarPath) return cd.layerBarPath;
		}
		return null;
	}

	function effectiveCardAttributeCode(def, battleCard) {
		if (!def) return '';
		const ov = battleCard && battleCard.battleTribeOverride;
		if (ov != null && String(ov).trim() !== '') return String(ov).trim().toUpperCase();
		return def.attribute != null ? String(def.attribute) : '';
	}

	function hasCardAttributeResolved(def, battleCard, tribe) {
		return hasCardAttribute(effectiveCardAttributeCode(def, battleCard), tribe);
	}

	/** CpuBattleEngine.hasAttributeForDeployPreview 相当（手札の次配置が SPEC-666 でアンデッド扱いになる予定） */
	function hasCardAttributeForDeployPreview(def, battleCard, spec666NextSlotPending, tribe) {
		if (!tribe) return false;
		if (
			spec666NextSlotPending &&
			(!battleCard || battleCard.battleTribeOverride == null || String(battleCard.battleTribeOverride).trim() === '')
		) {
			return tribe === 'UNDEAD';
		}
		return hasCardAttributeResolved(def, battleCard, tribe);
	}

	function defWithBattleTribeOverride(d, battleCard, defs) {
		const base = defWithBlankEffectsIfNeeded(d, battleCard);
		if (!base) return base;
		const ov = battleCard && battleCard.battleTribeOverride;
		if (ov == null || String(ov).trim() === '') return base;
		const code = String(ov).trim().toUpperCase();
		if (code !== 'UNDEAD') return base;
		const barPath = resolveLayerBarPathForTribe(defs, 'UNDEAD') || base.layerBarPath;
		return Object.assign({}, base, {
			attribute: 'UNDEAD',
			attributeLabelJa: 'アンデッド',
			attributeLabelLines: ['アンデッド'],
			layerBarPath: barPath
		});
	}

	function cardDefForBattleFace(def, battleCard, defs) {
		return defWithBattleTribeOverride(defWithBlankEffectsIfNeeded(def, battleCard), battleCard, defs);
	}

	const battleTipEl = document.getElementById('battle-card-tooltip');
	const battleTipName = battleTipEl ? battleTipEl.querySelector('.battle-card-tooltip__name') : null;
	const battleTipAttr = battleTipEl ? battleTipEl.querySelector('.battle-card-tooltip__attr') : null;
	const battleTipPack = battleTipEl ? battleTipEl.querySelector('.battle-card-tooltip__pack') : null;
	const battleTipAbility = battleTipEl ? battleTipEl.querySelector('.battle-card-tooltip__ability') : null;
	const battleTipPreview = battleTipEl ? battleTipEl.querySelector('.battle-card-tooltip__preview') : null;
	const battleTipPowerLabel = battleTipEl ? battleTipEl.querySelector('.battle-card-tooltip__power-label') : null;
	const battleTipPowerMods = battleTipEl ? battleTipEl.querySelector('.battle-card-tooltip__power-modifiers') : null;
	const deckTipEl = document.getElementById('battle-deck-tooltip');
	let lastDefsForTooltip = null;
	let lastStateForHandPower = null;

	function packSourcesForInitial(piRaw) {
		const pi = (piRaw || 'STD').trim().toUpperCase() || 'STD';
		if (pi === 'WH') return ['風吹く丘パック', 'スタンダードパック1'];
		if (pi === 'ET') return ['邪悪なる脅威パック', 'スタンダードパック1'];
		return ['スタンダードパック1'];
	}

	function hideBattleCardTooltip() {
		if (battleTipPreview) battleTipPreview.textContent = '';
		if (battleTipPowerMods) {
			battleTipPowerMods.textContent = '';
			battleTipPowerMods.hidden = true;
		}
		if (battleTipPowerLabel) battleTipPowerLabel.hidden = true;
		if (battleTipEl) battleTipEl.hidden = true;
	}

	function hideBattleDeckTooltip() {
		if (deckTipEl) deckTipEl.hidden = true;
	}

	function positionBattleDeckTooltip(clientX, clientY) {
		if (!deckTipEl) return;
		const pad = 12;
		const tw = deckTipEl.offsetWidth;
		const th = deckTipEl.offsetHeight;
		let x = clientX + pad;
		let y = clientY + pad;
		if (x + tw > window.innerWidth - pad) {
			x = Math.max(pad, clientX - tw - pad);
		}
		if (y + th > window.innerHeight - pad) {
			y = Math.max(pad, window.innerHeight - th - pad);
		}
		deckTipEl.style.left = x + 'px';
		deckTipEl.style.top = y + 'px';
	}

	function showBattleDeckTooltip(count, clientX, clientY) {
		if (!deckTipEl) return;
		deckTipEl.textContent = String(count) + '枚';
		deckTipEl.hidden = false;
		positionBattleDeckTooltip(clientX, clientY);
	}

	function formatBattleCardAttr(d) {
		if (!d) return '—';
		if (d.attributeLabelLines && d.attributeLabelLines.length) {
			return d.attributeLabelLines.join('／');
		}
		return d.attributeLabelJa || d.attribute || '—';
	}

	function battleCardAbilityTooltipText(d) {
		if (!d || !d.abilityBlocks || !d.abilityBlocks.length) {
			return '効果なし。';
		}
		const chunks = [];
		d.abilityBlocks.forEach(function (b) {
			const h = b.headline != null ? String(b.headline).trim() : '';
			let body = b.body != null ? String(b.body) : '';
			body = body.replace(/能力なし。/g, '効果なし。');
			if (h) chunks.push(h);
			chunks.push(body || '—');
		});
		return chunks.join('\n');
	}

	function resolveDefByAbilityDeployCode(defs, abilityDeployCode, promptHint) {
		if (!defs || !abilityDeployCode) return null;
		const raw = String(abilityDeployCode);
		const codesToTry = raw === 'SPEC0' || raw === 'SPEC1' ? ['SPEC1', 'SPEC0'] : [raw];
		const hint = promptHint != null ? String(promptHint) : '';
		let fallback = null;
		const keys = Object.keys(defs);
		for (let ci = 0; ci < codesToTry.length; ci++) {
			const code = codesToTry[ci];
			for (let i = 0; i < keys.length; i++) {
				const d = defs[keys[i]];
				if (!d) continue;
				if (d.abilityDeployCode !== code) continue;
				// Prefer exact name match when available (prompt is often the card name)
				if (hint && d.name && String(d.name) === hint) return d;
				if (!fallback) fallback = d;
			}
		}
		return fallback;
	}

	function parseBattlePowerContributorsFromHost(host) {
		if (!host || !host.dataset || !host.dataset.battlePowerContributors) return [];
		try {
			const v = JSON.parse(host.dataset.battlePowerContributors);
			return Array.isArray(v) ? v : [];
		} catch (e) {
			return [];
		}
	}

	function formatBattlePowerContributorText(defs, mod) {
		if (!mod || typeof mod !== 'object') return '—';
		const id = mod.sourceCardId;
		const label = mod.label != null ? String(mod.label) : '';
		if (id != null && defs) {
			const d = resolveCardDef(defs, id);
			const name = d && d.name ? String(d.name) : 'カード#' + String(id);
			return name + (label ? ' ' + label : '');
		}
		return label || '—';
	}

	function fillBattleTooltipPowerSection(host) {
		if (!battleTipPowerMods || !battleTipPowerLabel) return;
		battleTipPowerMods.textContent = '';
		const mods = parseBattlePowerContributorsFromHost(host);
		if (!mods.length) {
			battleTipPowerMods.hidden = true;
			battleTipPowerLabel.hidden = true;
			return;
		}
		battleTipPowerLabel.hidden = false;
		battleTipPowerMods.hidden = false;
		const defs = lastDefsForTooltip;
		mods.forEach(function (mod) {
			const li = document.createElement('li');
			li.textContent = formatBattlePowerContributorText(defs, mod);
			battleTipPowerMods.appendChild(li);
		});
	}

	function setBattleZonePowerContributorsDataset(el, mods) {
		if (!el || !el.dataset) return;
		if (mods && mods.length) {
			el.dataset.battlePowerContributors = JSON.stringify(mods);
		} else {
			delete el.dataset.battlePowerContributors;
		}
	}

	function hideBattlePowerContributorPopup() {
		if (ui._powerContributorPopupHideTimer != null) {
			clearTimeout(ui._powerContributorPopupHideTimer);
			ui._powerContributorPopupHideTimer = null;
		}
		if (ui._powerContributorPopupEl && ui._powerContributorPopupEl.parentNode) {
			ui._powerContributorPopupEl.remove();
		}
		ui._powerContributorPopupEl = null;
	}

	/**
	 * 強さの変動要因（カード名）ホバー用: 左にカード、右に詳細。メインモーダルより前面（z-index）。
	 */
	function buildBattlePowerContributorHoverPanel(srcDef) {
		const root = el('div', 'battle-power-contributor-popup');
		const grid = el('div', 'battle-power-contributor-popup__grid');
		const left = el('div', 'battle-power-contributor-popup__left');
		const right = el('div', 'battle-power-contributor-popup__right');
		const face = buildBattleCardFaceShell(srcDef, 'zone');
		face.classList.add('battle-power-contributor-popup__card');
		left.appendChild(face);
		right.appendChild(el('h4', 'battle-power-contributor-popup__name', srcDef.name || '—'));
		const stats = el('dl', 'battle-power-contributor-popup__stats');
		function statRow(label, value) {
			const wrap = el('div', 'battle-power-contributor-popup__stat');
			wrap.appendChild(el('dt', '', label));
			const dd = el('dd', '', value);
			if (label === '収録パック') dd.classList.add('battle-power-contributor-popup__pack');
			wrap.appendChild(dd);
			return wrap;
		}
		stats.appendChild(statRow('種族', formatBattleCardAttr(srcDef)));
		stats.appendChild(statRow('コスト', String(srcDef.cost != null ? srcDef.cost : '—')));
		stats.appendChild(
			statRow('強さ', srcDef.fieldCard ? '—' : String(srcDef.basePower != null ? srcDef.basePower : '—'))
		);
		stats.appendChild(statRow('★', String(srcDef.rarity != null ? srcDef.rarity : '—')));
		stats.appendChild(statRow('収録パック', packSourcesForInitial(srcDef.packInitial).join('\n')));
		right.appendChild(stats);
		right.appendChild(el('p', 'battle-power-contributor-popup__label', '効果'));
		const ability = el('div', 'battle-power-contributor-popup__ability');
		const raw = battleCardAbilityTooltipText(srcDef);
		const lines = String(raw || '').split('\n');
		const first = lines.length ? String(lines[0]).trim() : '';
		ability.textContent =
			first === '〈配置〉' || first === '〈常時〉' || first === '〈フィールド〉'
				? lines.slice(1).join('\n')
				: String(raw || '—');
		right.appendChild(ability);
		grid.appendChild(left);
		grid.appendChild(right);
		root.appendChild(grid);
		root.setAttribute('role', 'tooltip');
		return root;
	}

	function positionBattlePowerContributorPopup(anchorEl, popupEl) {
		const margin = 8;
		const gap = 10;
		const vw = window.innerWidth;
		const vh = window.innerHeight;
		popupEl.style.position = 'fixed';
		popupEl.style.zIndex = '80';
		popupEl.style.left = '0';
		popupEl.style.top = '0';
		popupEl.style.visibility = 'hidden';
		document.body.appendChild(popupEl);
		const rect = anchorEl.getBoundingClientRect();
		const pr = popupEl.getBoundingClientRect();
		let left = rect.right + gap;
		let top = rect.top;
		if (left + pr.width > vw - margin) {
			left = rect.left - pr.width - gap;
		}
		if (left < margin) {
			left = margin;
		}
		if (left + pr.width > vw - margin) {
			left = Math.max(margin, vw - margin - pr.width);
		}
		if (top + pr.height > vh - margin) {
			top = Math.max(margin, vh - margin - pr.height);
		}
		if (top < margin) {
			top = margin;
		}
		popupEl.style.left = left + 'px';
		popupEl.style.top = top + 'px';
		popupEl.style.visibility = '';
	}

	function wireBattlePowerContributorHover(anchorEl, srcDef) {
		const delay = 160;
		function scheduleHide() {
			if (ui._powerContributorPopupHideTimer != null) {
				clearTimeout(ui._powerContributorPopupHideTimer);
			}
			ui._powerContributorPopupHideTimer = setTimeout(function () {
				ui._powerContributorPopupHideTimer = null;
				hideBattlePowerContributorPopup();
			}, delay);
		}
		function cancelHide() {
			if (ui._powerContributorPopupHideTimer != null) {
				clearTimeout(ui._powerContributorPopupHideTimer);
				ui._powerContributorPopupHideTimer = null;
			}
		}
		function show() {
			cancelHide();
			if (ui._powerContributorPopupEl) {
				hideBattlePowerContributorPopup();
			}
			const panel = buildBattlePowerContributorHoverPanel(srcDef);
			ui._powerContributorPopupEl = panel;
			positionBattlePowerContributorPopup(anchorEl, panel);
			panel.addEventListener('mouseenter', cancelHide);
			panel.addEventListener('mouseleave', scheduleHide);
		}
		anchorEl.addEventListener('mouseenter', show);
		anchorEl.addEventListener('mouseleave', scheduleHide);
	}

	function fillBattleTooltipAbility(el, raw) {
		if (!el) return;
		el.textContent = '';
		const text = raw == null || raw === '' ? '—' : raw;
		if (text === '—') {
			el.textContent = '—';
			return;
		}
		const nl = text.indexOf('\n');
		const head = nl >= 0 ? text.slice(0, nl) : text;
		const rest = nl >= 0 ? text.slice(nl + 1) : '';
		if (head === '〈配置〉' || head === '〈常時〉' || head === '〈フィールド〉') {
			const tag = document.createElement('span');
			tag.className = 'deck-tooltip__ability-tag';
			tag.textContent = head;
			el.appendChild(tag);
			if (rest) {
				el.appendChild(document.createElement('br'));
				const desc = document.createElement('span');
				desc.className = 'deck-tooltip__ability-desc';
				desc.textContent = rest;
				el.appendChild(desc);
			}
			return;
		}
		el.textContent = text;
	}

	/** battleCardAbilityTooltipText の先頭行が〈配置〉〈常時〉〈フィールド〉ならそのラベル（ポップアップ見出し用） */
	function battleAbilityHeadlineTagFromDef(def) {
		const raw = battleCardAbilityTooltipText(def);
		const lines = String(raw || '').split('\n');
		const first = lines.length ? String(lines[0]).trim() : '';
		if (first === '〈配置〉' || first === '〈常時〉' || first === '〈フィールド〉') {
			return first;
		}
		return '〈配置〉';
	}

	/** ホバー中のカードの右側（はみ出すときは左）にツールチップを置く */
	function positionBattleCardTooltip(host) {
		if (!battleTipEl || !(host instanceof Element)) return;
		const pad = 12;
		const r = host.getBoundingClientRect();
		const tw = battleTipEl.offsetWidth;
		const th = battleTipEl.offsetHeight;
		let x = r.right + pad;
		let y = r.top;
		if (x + tw > window.innerWidth - pad) {
			x = Math.max(pad, r.left - tw - pad);
		}
		if (y + th > window.innerHeight - pad) {
			y = Math.max(pad, window.innerHeight - th - pad);
		}
		if (y < pad) {
			y = pad;
		}
		battleTipEl.style.left = x + 'px';
		battleTipEl.style.top = y + 'px';
	}

	function showBattleCardTooltipFromDataset(host) {
		if (!battleTipEl || !battleTipName || !battleTipAttr || !battleTipAbility) return;
		hideBattleDeckTooltip();
		let def = null;
		let defDetail = null;
		if (battleTipPreview) {
			battleTipPreview.textContent = '';
			const cid = host.dataset.battleCardId;
			if (cid && lastDefsForTooltip) {
				defDetail = zoneOrHandDetailDef(host, cid);
				def = defDetail || resolveCardDef(lastDefsForTooltip, cid);
				if (def) {
					battleTipPreview.appendChild(buildBattleCardFaceShell(defDetail || def, 'tip'));
				}
			}
		}
		battleTipName.textContent = host.dataset.battleName || '';
		battleTipAttr.textContent = defDetail ? formatBattleCardAttr(defDetail) : host.dataset.battleAttr || '—';
		if (battleTipPack) {
			battleTipPack.textContent = def ? packSourcesForInitial(def.packInitial).join('\n') : '—';
		}
		fillBattleTooltipPowerSection(host);
		fillBattleTooltipAbility(battleTipAbility, host.dataset.battleAbility || '');
		battleTipEl.hidden = false;
		requestAnimationFrame(function () {
			positionBattleCardTooltip(host);
			requestAnimationFrame(function () {
				positionBattleCardTooltip(host);
			});
		});
	}

	function applyBattleCardTipData(el, d) {
		if (!el || !d) return;
		el.dataset.battleTip = '1';
		el.dataset.battleCardId = String(d.id != null ? d.id : '');
		el.dataset.battleName = d.name || '';
		el.dataset.battleAttr = formatBattleCardAttr(d);
		el.dataset.battleAbility = battleCardAbilityTooltipText(d);
	}

	function wireBattleCardTooltipHost(el) {
		if (!el || !battleTipEl) return;
		el.addEventListener('pointerenter', function () {
			showBattleCardTooltipFromDataset(el);
		});
		el.addEventListener('pointerleave', hideBattleCardTooltip);
	}

	function wireBattleCardTooltips(root) {
		if (!root || !battleTipEl) return;
		root.querySelectorAll('[data-battle-tip="1"]').forEach(wireBattleCardTooltipHost);
	}

	async function fetchState() {
		const url = battleIsPvp ? (battleApiBase + '/state') : (contextPath + '/battle/cpu/state');
		const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
		if (res.status === 204) return null;
		if (!res.ok) throw new Error('state fetch failed: ' + res.status);
		return await res.json();
	}

	async function timeoutTick() {
		const headers = { 'Accept': 'application/json' };
		if (csrfToken) headers[csrfHeader] = csrfToken;
		const url = battleIsPvp ? (battleApiBase + '/timeout') : (contextPath + '/battle/cpu/timeout');
		const res = await fetch(url, { method: 'POST', headers });
		if (!res.ok) throw new Error('timeout failed: ' + res.status);
		return await res.json();
	}

	async function commitAction(payload) {
		const headers = { 'Accept': 'application/json', 'Content-Type': 'application/json' };
		if (csrfToken) headers[csrfHeader] = csrfToken;
		const url = battleIsPvp ? (battleApiBase + '/commit') : (contextPath + '/battle/cpu/commit');
		const res = await fetch(url, { method: 'POST', headers, body: JSON.stringify(payload) });
		if (!res.ok) throw new Error('commit failed: ' + res.status);
		return await res.json();
	}

	async function cpuStep() {
		const headers = { 'Accept': 'application/json' };
		if (csrfToken) headers[csrfHeader] = csrfToken;
		const res = await fetch(contextPath + '/battle/cpu/cpu-step', { method: 'POST', headers });
		if (!res.ok) throw new Error('cpu-step failed: ' + res.status);
		return await res.json();
	}

	async function resolvePending() {
		const headers = { 'Accept': 'application/json' };
		if (csrfToken) headers[csrfHeader] = csrfToken;
		const url = battleIsPvp ? (battleApiBase + '/resolve') : (contextPath + '/battle/cpu/resolve');
		const res = await fetch(url, { method: 'POST', headers });
		if (!res.ok) throw new Error('resolve failed: ' + res.status);
		return await res.json();
	}

	function el(tag, cls, text) {
		const n = document.createElement(tag);
		if (cls) n.className = cls;
		if (text != null) n.textContent = text;
		return n;
	}

	function sleep(ms) {
		return new Promise(resolve => setTimeout(resolve, ms));
	}

	/** メタタグからバトル開始イントロ（名前衝突 → 先攻/後攻）を実行。メタが無い場合は何もしない。 */
	async function runBattleIntroFromMeta() {
		const myMeta = document.querySelector('meta[name="battle_intro_my_name"]');
		const oppMeta = document.querySelector('meta[name="battle_intro_opp_name"]');
		const firstMeta = document.querySelector('meta[name="battle_intro_i_am_first"]');
		const myName = myMeta && myMeta.content != null ? String(myMeta.content).trim() : '';
		const oppName = oppMeta && oppMeta.content != null ? String(oppMeta.content).trim() : '';
		if (!myName || !oppName) return;

		const iAmFirst = firstMeta && String(firstMeta.content).toLowerCase() === 'true';

		document.body.classList.add('battle-intro-is-running');

		const overlay = el('div', 'battle-intro-overlay');
		const arena = el('div', 'battle-intro-overlay__arena');

		const plateMe = el('div', 'battle-intro-overlay__plate battle-intro-overlay__plate--me');
		plateMe.appendChild(el('div', 'battle-intro-overlay__name', myName));
		const orderMe = el(
			'div',
			'battle-intro-overlay__order ' + (iAmFirst ? 'battle-intro-overlay__order--first' : 'battle-intro-overlay__order--second')
		);
		plateMe.appendChild(orderMe);

		const sparkHost = el('div', 'battle-intro-overlay__spark-host');
		sparkHost.appendChild(el('div', 'battle-intro-overlay__spark'));

		const plateOpp = el('div', 'battle-intro-overlay__plate battle-intro-overlay__plate--opp');
		plateOpp.appendChild(el('div', 'battle-intro-overlay__name', oppName));
		const orderOpp = el(
			'div',
			'battle-intro-overlay__order ' + (!iAmFirst ? 'battle-intro-overlay__order--first' : 'battle-intro-overlay__order--second')
		);
		plateOpp.appendChild(orderOpp);

		arena.appendChild(plateMe);
		arena.appendChild(sparkHost);
		arena.appendChild(plateOpp);
		overlay.appendChild(arena);
		document.body.appendChild(overlay);

		// ユーザー名のみを合計 3 秒表示（スライド終端で衝突 → 火花、その後も名前を表示し続ける）
		const BATTLE_INTRO_NAME_PHASE_MS = 3000;
		const impactAtMs = 720;
		await sleep(impactAtMs);
		overlay.classList.add('battle-intro-overlay--impact');
		await sleep(Math.max(0, BATTLE_INTRO_NAME_PHASE_MS - impactAtMs));

		orderMe.textContent = iAmFirst ? '先攻' : '後攻';
		orderOpp.textContent = iAmFirst ? '後攻' : '先攻';
		overlay.classList.add('battle-intro-overlay--show-order');

		await sleep(3000);

		overlay.classList.add('battle-intro-overlay--exit');
		await sleep(400);
		overlay.remove();
		document.body.classList.remove('battle-intro-is-running');
	}

	function clamp(n, min, max) {
		return Math.max(min, Math.min(max, n));
	}

	/** レベルアップでレストに捨てられる枚数の上限（配置カードを手札に残すため手札枚数−1） */
	function maxLevelUpRestDiscard(handLen) {
		const n = handLen | 0;
		return Math.max(0, n - 1);
	}

	/** ボタンラベル等のテキストノードを直接クリックしたとき、closest 用に要素へ正規化する */
	function eventTargetElement(ev) {
		const n = ev && ev.target;
		if (n instanceof Element) return n;
		if (n && n.nodeType === 3 && n.parentElement) return n.parentElement;
		return null;
	}

	function showFieldStonePayModal(st) {
		const sel = selectedCard(st);
		if (!sel) return;
		const def = st.defs[sel.cardId];
		const raw = def ? Number(def.cost || 0) : 0;
		const adj = sel.handDeployCostModifier != null ? Number(sel.handDeployCostModifier) : 0;
		const cost = Math.max(0, raw + adj);
		ui.pay = { stones: cost, cardInstanceIds: [] };

		const overlay = el('div', 'battle-pay-modal battle-pay-modal--field-only');
		overlay.setAttribute('role', 'dialog');
		overlay.setAttribute('aria-modal', 'true');

		const panel = el('div', 'battle-pay-modal__panel');
		const closeBtn = el('button', 'battle-pay-modal__close', '×');
		closeBtn.type = 'button';
		panel.appendChild(closeBtn);

		const mainCol = el('div', 'battle-pay-modal__main');
		mainCol.appendChild(el('h2', 'battle-pay-modal__title', 'フィールドのコスト'));
		mainCol.appendChild(
			el('p', 'muted', '《フィールド》は、左上のコスト（' + String(cost) + '）分をストーンだけで支払います。ターンは終わりません。')
		);
		const status = el('p', 'battle-pay-modal__status', '');
		mainCol.appendChild(status);

		const sectionStone = el('section', 'battle-pay-modal__section');
		sectionStone.appendChild(el('h3', '', 'ストーンで支払う'));
		const rowStone = el('div', 'battle-pay-modal__row');
		const minus = el('button', 'btn btn--ghost', '−');
		minus.type = 'button';
		const plus = el('button', 'btn btn--ghost', '+');
		plus.type = 'button';
		const stoneVal = el('div', 'battle-pay-modal__value', String(cost));
		rowStone.appendChild(minus);
		rowStone.appendChild(stoneVal);
		rowStone.appendChild(plus);
		sectionStone.appendChild(rowStone);
		mainCol.appendChild(sectionStone);

		const actions = el('div', 'battle-pay-modal__actions');
		const cancel = el('button', 'btn btn--ghost', 'キャンセル');
		cancel.type = 'button';
		const decideBtn = el('button', 'btn btn--primary', '決定');
		decideBtn.type = 'button';
		actions.appendChild(cancel);
		actions.appendChild(decideBtn);
		mainCol.appendChild(actions);

		const deployCol = el('div', 'battle-pay-modal__deploy');
		deployCol.appendChild(el('h3', 'battle-pay-modal__deploy-label', '配置するカード'));
		if (def) {
			const deployWrap = el('div', 'battle-pay-modal__deploy-face');
			const deployShell = buildBattleCardFaceShell(def, 'modal');
			deployWrap.appendChild(deployShell);
			applyBattleCardTipData(deployWrap, def);
			deployCol.appendChild(deployWrap);
		}

		const layout = el('div', 'battle-pay-modal__layout');
		layout.appendChild(mainCol);
		layout.appendChild(deployCol);
		panel.appendChild(layout);
		overlay.appendChild(panel);
		document.body.appendChild(overlay);
		wireBattleCardTooltips(overlay);

		function stonesAfterLevelUp() {
			return Math.max(0, st.humanStones - (ui.levelUpStones | 0));
		}

		function syncHandStonesPreview() {
			const val = document.querySelector('.battle-you-hand-stones__value');
			const wrap = document.querySelector('.battle-you-hand-stones');
			if (!(val instanceof Element) || !(wrap instanceof Element)) return;
			const n = humanStonesPreviewReserveAdjusted(st);
			val.textContent = String(n);
			wrap.setAttribute('aria-label', 'ストーン所持数 ' + String(n));
		}

		function refresh() {
			const cap = Math.min(cost, stonesAfterLevelUp());
			if (ui.pay.stones > cap) ui.pay.stones = cap;
			if (ui.pay.stones < 0) ui.pay.stones = 0;
			stoneVal.textContent = String(ui.pay.stones);
			const remain = cost - ui.pay.stones;
			status.textContent =
				remain > 0 ? 'ストーンを ' + String(cost) + ' つ支払ってください（残り ' + String(remain) + '）' : 'OK';
			decideBtn.disabled = remain !== 0 || ui.pay.stones !== cost;
			plus.disabled = ui.pay.stones >= cap;
			minus.disabled = ui.pay.stones <= 0;
			syncHandStonesPreview();
		}

		function teardown() {
			hideBattleCardTooltip();
			hideBattleDeckTooltip();
			overlay.remove();
		}

		function stopEv(ev) {
			ev.preventDefault();
			ev.stopPropagation();
		}

		minus.addEventListener(
			'click',
			function (ev) {
				stopEv(ev);
				ui.pay.stones = Math.max(0, ui.pay.stones - 1);
				refresh();
			},
			true
		);
		plus.addEventListener(
			'click',
			function (ev) {
				stopEv(ev);
				const cap = Math.min(cost, stonesAfterLevelUp());
				if (ui.pay.stones >= cap) return;
				ui.pay.stones = Math.min(cap, ui.pay.stones + 1);
				refresh();
			},
			true
		);

		closeBtn.addEventListener(
			'click',
			function (ev) {
				stopEv(ev);
				ui.pay = { stones: 0, cardInstanceIds: [] };
				teardown();
				rerenderWithFreshState();
			},
			true
		);
		cancel.addEventListener(
			'click',
			function (ev) {
				stopEv(ev);
				ui.pay = { stones: 0, cardInstanceIds: [] };
				teardown();
				rerenderWithFreshState();
			},
			true
		);
		overlay.addEventListener(
			'click',
			function (e) {
				if (e.target === overlay) {
					ui.pay = { stones: 0, cardInstanceIds: [] };
					teardown();
					rerenderWithFreshState();
				}
			},
			true
		);

		decideBtn.addEventListener(
			'click',
			function (ev) {
				stopEv(ev);
				if (decideBtn.disabled) return;
				const prev = captureAnimRects();
				const payload = {
					levelUpRest: ui.levelUpRest,
					levelUpDiscardInstanceIds: ui.levelUpDiscardIds,
					levelUpStones: ui.levelUpStones,
					deployInstanceId: String(sel.instanceId),
					payCostStones: cost,
					payCostCardInstanceIds: []
				};
				teardown();
				ui.pay = { stones: 0, cardInstanceIds: [] };
				commitAction(payload)
					.then(function (next) {
						ui.selectedInstanceId = null;
						ui.levelUpRest = 0;
						ui.levelUpStones = 0;
						ui.levelUpDiscardIds = [];
						ui.warnLevelUpRest = null;
						ui.warnLevelUpStone = null;
						ui._luPrevPowerInstanceId = null;
						ui._luPrevPower = null;
						ui.pay = { stones: 0, cardInstanceIds: [] };
						render(next);
						requestAnimationFrame(function () {
							playFLIP(prev);
						});
					})
					.catch(function (e) {
						// eslint-disable-next-line no-console
						console.error(e);
						alert('操作に失敗しました（' + (e && e.message ? e.message : String(e)) + '）');
						rerenderWithFreshState();
					});
			},
			true
		);

		refresh();
	}

	function showPayModal(st) {
		const sel = selectedCard(st);
		if (!sel) return;
		const def = st.defs[sel.cardId];
		const cost = def && !def.fieldCard ? effectiveFighterDeployCostFromState(def, st, sel) : def ? Number(def.cost || 0) : 0;
		if (def && def.fieldCard) {
			if (cost <= 0) {
				return;
			}
			/* ストーンが足りるときは第2モーダルで止まらないよう、そのままコミット */
			const available = Math.max(0, st.humanStones - (ui.levelUpStones | 0));
			if (available >= cost) {
				const prev = captureAnimRects();
				const payload = {
					levelUpRest: ui.levelUpRest,
					levelUpDiscardInstanceIds: ui.levelUpDiscardIds,
					levelUpStones: ui.levelUpStones,
					deployInstanceId: String(sel.instanceId),
					payCostStones: cost,
					payCostCardInstanceIds: []
				};
				commitAction(payload)
					.then(function (next) {
						ui.selectedInstanceId = null;
						ui.levelUpRest = 0;
						ui.levelUpStones = 0;
						ui.levelUpDiscardIds = [];
						ui.warnLevelUpRest = null;
						ui.warnLevelUpStone = null;
						ui._luPrevPowerInstanceId = null;
						ui._luPrevPower = null;
						ui.pay = { stones: 0, cardInstanceIds: [] };
						render(next);
						requestAnimationFrame(function () {
							playFLIP(prev);
						});
					})
					.catch(function (e) {
						// eslint-disable-next-line no-console
						console.error(e);
						alert('操作に失敗しました（' + (e && e.message ? e.message : String(e)) + '）');
						rerenderWithFreshState();
					});
				return;
			}
			showFieldStonePayModal(st);
			return;
		}
		if (cost <= 0) {
			// コスト0はモーダル不要（commit側でそのまま進める）
			return;
		}

		ui.pay = { stones: 0, cardInstanceIds: [] };

		const overlay = el('div', 'battle-pay-modal');
		overlay.setAttribute('role', 'dialog');
		overlay.setAttribute('aria-modal', 'true');

		const panel = el('div', 'battle-pay-modal__panel');
		const closeBtn = el('button', 'battle-pay-modal__close', '×');
		closeBtn.type = 'button';
		panel.appendChild(closeBtn);

		const layout = el('div', 'battle-pay-modal__layout');
		const mainCol = el('div', 'battle-pay-modal__main');

		mainCol.appendChild(el('h2', 'battle-pay-modal__title', 'コストの支払い'));
		mainCol.appendChild(el('p', 'muted', '必要コスト: ' + String(cost) + '（カード/ストーン/分割OK）'));

		const ownedStonesP = el('p', 'battle-pay-modal__owned muted', '');
		mainCol.appendChild(ownedStonesP);

		const status = el('p', 'battle-pay-modal__status', '');
		mainCol.appendChild(status);

		const sectionStone = el('section', 'battle-pay-modal__section');
		sectionStone.appendChild(el('h3', '', 'ストーンで支払う'));
		const rowStone = el('div', 'battle-pay-modal__row');
		const minus = el('button', 'btn btn--ghost', '−');
		minus.type = 'button';
		const plus = el('button', 'btn btn--ghost', '+');
		plus.type = 'button';
		const stoneVal = el('div', 'battle-pay-modal__value', '0');
		rowStone.appendChild(minus);
		rowStone.appendChild(stoneVal);
		rowStone.appendChild(plus);
		sectionStone.appendChild(rowStone);
		mainCol.appendChild(sectionStone);

		const sectionCards = el('section', 'battle-pay-modal__section');
		sectionCards.appendChild(el('h3', '', 'カードで支払う（手札から選択）'));
		const grid = el('div', 'battle-pay-modal__cardgrid');
		const payCandidates = st.humanHand.filter((c) => {
			if (c.instanceId === sel.instanceId) return false;
			// レベルアップで選択したカードは、コスト支払いに使えない（ファイター下に差し込むため）
			if (ui.levelUpDiscardIds && ui.levelUpDiscardIds.indexOf(c.instanceId) >= 0) return false;
			return true;
		});
		payCandidates.forEach((c) => {
			const d = st.defs[c.cardId];
			const btn = el('button', 'battle-pay-modal__card', null);
			btn.type = 'button';
			btn.dataset.instanceId = c.instanceId;
			btn.dataset.selected = 'false';
			const caret = el('span', 'battle-pay-modal__caret', '▼');
			caret.setAttribute('aria-hidden', 'true');
			btn.appendChild(caret);
			if (d) {
				const shell = buildBattleCardFaceShell(d, 'modal');
				applyCurrentPowerDisplayToBattleCardFace(st, st.defs, shell, c.instanceId, d, { includeNextDeployBonus: true });
				btn.appendChild(shell);
				applyBattleCardTipData(btn, d);
			}
			grid.appendChild(btn);
		});
		sectionCards.appendChild(grid);
		mainCol.appendChild(sectionCards);

		const actions = el('div', 'battle-pay-modal__actions');
		const cancel = el('button', 'btn btn--ghost', 'キャンセル');
		cancel.type = 'button';
		const payDecideBtn = el('button', 'btn btn--primary', '決定');
		payDecideBtn.type = 'button';
		payDecideBtn.disabled = true;
		actions.appendChild(cancel);
		actions.appendChild(payDecideBtn);
		mainCol.appendChild(actions);

		const deployCol = el('div', 'battle-pay-modal__deploy');
		deployCol.appendChild(el('h3', 'battle-pay-modal__deploy-label', '配置するカード'));
		if (def) {
			const deployWrap = el('div', 'battle-pay-modal__deploy-face');
			const deployShell = buildBattleCardFaceShell(def, 'modal');
			applyCurrentPowerDisplayToBattleCardFace(st, st.defs, deployShell, sel.instanceId, def, { includeNextDeployBonus: true });
			deployWrap.appendChild(deployShell);
			applyBattleCardTipData(deployWrap, def);
			deployCol.appendChild(deployWrap);
		} else {
			deployCol.appendChild(el('p', 'muted', '—'));
		}

		layout.appendChild(mainCol);
		layout.appendChild(deployCol);
		panel.appendChild(layout);

		overlay.appendChild(panel);
		document.body.appendChild(overlay);
		wireBattleCardTooltips(overlay);

		function totalPaid() {
			return ui.pay.stones + ui.pay.cardInstanceIds.length;
		}

		function stonesAfterLevelUp() {
			return Math.max(0, st.humanStones - (ui.levelUpStones | 0));
		}

		function maxStonesForPay() {
			const cap = Math.max(0, cost - ui.pay.cardInstanceIds.length);
			return Math.min(stonesAfterLevelUp(), cap);
		}

		function syncHandStonesPreview() {
			const val = document.querySelector('.battle-you-hand-stones__value');
			const wrap = document.querySelector('.battle-you-hand-stones');
			if (!(val instanceof Element) || !(wrap instanceof Element)) return;
			const n = humanStonesPreviewReserveAdjusted(st);
			val.textContent = String(n);
			wrap.setAttribute('aria-label', 'ストーン所持数 ' + String(n));
		}

		function refresh() {
			const maxS = maxStonesForPay();
			if (ui.pay.stones > maxS) {
				ui.pay.stones = maxS;
			}
			const paid = totalPaid();
			const remain = cost - paid;
			stoneVal.textContent = String(ui.pay.stones);
			const afterLv = stonesAfterLevelUp();
			const ownedRemain = Math.max(0, afterLv - (ui.pay.stones | 0));
			ownedStonesP.textContent = '所持ストーン: ' + String(ownedRemain);
			status.textContent =
				remain > 0 ? '残り ' + String(remain) + ' 必要です' : 'OK（支払いが揃いました）';
			payDecideBtn.disabled = remain !== 0;
			plus.disabled = ui.pay.stones >= maxS;
			minus.disabled = ui.pay.stones <= 0;
			syncHandStonesPreview();
		}

		function teardown() {
			hideBattleCardTooltip();
			hideBattleDeckTooltip();
			overlay.remove();
		}

		function clampPay(n, min, max) {
			return Math.max(min, Math.min(max, n));
		}

		function stopPayEv(ev) {
			ev.preventDefault();
			ev.stopPropagation();
		}

		minus.addEventListener(
			'click',
			function (ev) {
				stopPayEv(ev);
				ui.pay.stones = Math.max(0, ui.pay.stones - 1);
				refresh();
			},
			true
		);
		plus.addEventListener(
			'click',
			function (ev) {
				stopPayEv(ev);
				const cap = maxStonesForPay();
				if (ui.pay.stones >= cap) return;
				ui.pay.stones = clampPay(ui.pay.stones + 1, 0, cap);
				refresh();
			},
			true
		);

		grid.addEventListener('click', function (e) {
			const t = eventTargetElement(e);
			if (!t) return;
			const btn = t.closest('.battle-pay-modal__card');
			if (!btn) return;
			const inst = btn.getAttribute('data-instance-id');
			if (!inst) return;

			const i = ui.pay.cardInstanceIds.indexOf(inst);
			if (i >= 0) {
				ui.pay.cardInstanceIds.splice(i, 1);
				btn.classList.remove('is-selected');
			} else {
				if (ui.pay.stones + ui.pay.cardInstanceIds.length >= cost) return;
				ui.pay.cardInstanceIds.push(inst);
				btn.classList.add('is-selected');
			}
			refresh();
		});

		function onClose() {
			ui.pay = { stones: 0, cardInstanceIds: [] };
			teardown();
			rerenderWithFreshState().catch(function (e) {
				// eslint-disable-next-line no-console
				console.error(e);
			});
		}

		closeBtn.addEventListener(
			'click',
			function (ev) {
				stopPayEv(ev);
				onClose();
			},
			true
		);
		cancel.addEventListener(
			'click',
			function (ev) {
				stopPayEv(ev);
				onClose();
			},
			true
		);
		overlay.addEventListener(
			'click',
			function (e) {
				if (e.target === overlay) onClose();
			},
			true
		);

		payDecideBtn.addEventListener(
			'click',
			function (ev) {
				stopPayEv(ev);
				if (payDecideBtn.disabled) return;
				const prev = captureAnimRects();
				const payload = {
					levelUpRest: ui.levelUpRest,
					levelUpDiscardInstanceIds: ui.levelUpDiscardIds,
					levelUpStones: ui.levelUpStones,
					deployInstanceId: String(sel.instanceId),
					payCostStones: ui.pay.stones,
					payCostCardInstanceIds: ui.pay.cardInstanceIds.slice()
				};
				teardown();
				ui.pay = { stones: 0, cardInstanceIds: [] };
				commitAction(payload)
					.then(function (next) {
						ui.selectedInstanceId = null;
						ui.levelUpRest = 0;
						ui.levelUpStones = 0;
						ui.levelUpDiscardIds = [];
						ui.warnLevelUpRest = null;
						ui.warnLevelUpStone = null;
						ui._luPrevPowerInstanceId = null;
						ui._luPrevPower = null;
						ui.pay = { stones: 0, cardInstanceIds: [] };
						render(next);
						requestAnimationFrame(function () {
							playFLIP(prev);
						});
					})
					.catch(function (e) {
						// eslint-disable-next-line no-console
						console.error(e);
						alert('操作に失敗しました（' + (e && e.message ? e.message : String(e)) + '）');
						rerenderWithFreshState();
					});
			},
			true
		);

		refresh();
	}

	function showRestModal(restCards, defs, titleText, battleState) {
		const list = Array.isArray(restCards) ? restCards.slice() : [];
		const title = titleText || 'レスト';

		hideBattleCardTooltip();
		hideBattleDeckTooltip();

		const previouslyFocused = document.activeElement instanceof HTMLElement ? document.activeElement : null;

		const overlay = el('div', 'battle-pay-modal');
		overlay.classList.add('battle-rest-list-modal');
		overlay.setAttribute('role', 'dialog');
		overlay.setAttribute('aria-modal', 'true');
		overlay.setAttribute('aria-label', title + '一覧');

		const panel = el('div', 'battle-pay-modal__panel');
		const closeBtn = el('button', 'battle-pay-modal__close', '×');
		closeBtn.type = 'button';
		panel.appendChild(closeBtn);

		panel.appendChild(el('h2', 'battle-pay-modal__title', title));
		panel.appendChild(el('p', 'muted', '合計: ' + String(list.length) + '枚'));

		const grid = el('div', 'battle-pay-modal__cardgrid');
		list.forEach(function (c) {
			const d0 = resolveCardDef(defs, c.cardId);
			const d = d0 ? cardDefForBattleFace(d0, c, defs) : null;
			const host = el('div', 'battle-pay-modal__card', null);
			if (c.instanceId != null) host.dataset.battleInstanceId = String(c.instanceId);
			if (d) {
				const shell = buildBattleCardFaceShell(d, 'modal');
				host.appendChild(shell);
				if (battleState && d0 && !d0.fieldCard) {
					applyCurrentCostDisplay(shell, d, battleState, c);
				}
				applyBattleCardTipData(host, d);
				host.addEventListener('click', function (e) {
					if (e.button !== 0) return;
					e.preventDefault();
					e.stopPropagation();
					showBattleZoneDetailModal(d, [], battleState, c);
				});
			} else {
				const im = document.createElement('img');
				im.src = absUrl(cardBack);
				im.alt = '裏';
				host.appendChild(im);
			}
			grid.appendChild(host);
		});
		panel.appendChild(grid);

		overlay.appendChild(panel);
		document.body.appendChild(overlay);
		wireBattleCardTooltips(overlay);

		function teardown() {
			hideBattleCardTooltip();
			hideBattleDeckTooltip();
			overlay.remove();
			document.removeEventListener('keydown', onKey);
			if (previouslyFocused) {
				previouslyFocused.focus();
			}
		}

		function onKey(e) {
			if (e.key === 'Escape') {
				e.preventDefault();
				teardown();
			}
		}

		closeBtn.addEventListener('click', teardown);
		overlay.addEventListener('click', function (e) {
			if (e.target === overlay) teardown();
		});
		document.addEventListener('keydown', onKey);
		// フォーカスをモーダルへ（Enter/Spaceで開いた時にキー操作が効くように）
		closeBtn.focus();
	}

	function showLevelUpDiscardConfirmModal(st, onOk) {
		const sel = selectedCard(st);
		if (!sel) return;
		const hand = Array.isArray(st.humanHand) ? st.humanHand.slice() : [];
		const n = Math.max(0, Math.min(ui.levelUpRest | 0, maxLevelUpRestDiscard(hand.length)));
		if (n <= 0) {
			if (typeof onOk === 'function') onOk();
			return;
		}
		const picked = [];

		hideBattleCardTooltip();
		hideBattleDeckTooltip();

		const overlay = el('div', 'battle-pay-modal');
		overlay.setAttribute('role', 'dialog');
		overlay.setAttribute('aria-modal', 'true');

		const panel = el('div', 'battle-pay-modal__panel');
		const closeBtn = el('button', 'battle-pay-modal__close', '×');
		closeBtn.type = 'button';
		panel.appendChild(closeBtn);

		panel.appendChild(el('h2', 'battle-pay-modal__title', 'カードを選択'));
		panel.appendChild(el('p', 'muted', '手札から' + String(n) + '枚選んで、ファイターの下に差し込みます'));

		const grid = el('div', 'battle-pay-modal__cardgrid');
		hand.forEach(function (c) {
			if (c.instanceId === sel.instanceId) return; // 配置カードは捨てられない
			const d = resolveCardDef(st.defs, c.cardId);
			const host = el('button', 'battle-pay-modal__card', null);
			host.type = 'button';
			host.dataset.instanceId = c.instanceId;
			if (d) {
				const shell = buildBattleCardFaceShell(d, 'modal');
				applyCurrentPowerDisplayToBattleCardFace(st, st.defs, shell, c.instanceId, d, { includeNextDeployBonus: true });
				host.appendChild(shell);
				applyBattleCardTipData(host, d);
			}
			grid.appendChild(host);
		});
		panel.appendChild(grid);

		const actions = el('div', 'battle-pay-modal__actions');
		const cancel = el('button', 'btn btn--ghost', 'キャンセル');
		cancel.type = 'button';
		const ok = el('button', 'btn btn--primary', 'OK');
		ok.type = 'button';
		ok.disabled = true;
		actions.appendChild(cancel);
		actions.appendChild(ok);
		panel.appendChild(actions);

		overlay.appendChild(panel);
		document.body.appendChild(overlay);
		wireBattleCardTooltips(overlay);

		function teardown() {
			hideBattleCardTooltip();
			hideBattleDeckTooltip();
			overlay.remove();
		}

		closeBtn.addEventListener('click', teardown);
		cancel.addEventListener('click', teardown);
		overlay.addEventListener('click', function (e) {
			if (e.target === overlay) teardown();
		});

		ok.addEventListener('click', function (ev) {
			ev.preventDefault();
			ev.stopPropagation();
			teardown();
			ui.levelUpDiscardIds = picked.slice();
			if (typeof onOk === 'function') onOk();
		});

		function refresh() {
			ok.disabled = picked.length !== n;
		}
		grid.addEventListener('click', function (e) {
			const t = eventTargetElement(e);
			if (!t) return;
			const btn = t.closest('.battle-pay-modal__card');
			if (!btn) return;
			const inst = btn.getAttribute('data-instance-id');
			if (!inst) return;
			const i = picked.indexOf(inst);
			if (i >= 0) {
				picked.splice(i, 1);
				btn.classList.remove('is-selected');
			} else {
				if (picked.length >= n) return;
				picked.push(inst);
				btn.classList.add('is-selected');
			}
			refresh();
		});
		refresh();
	}

	function wireDeckStackTooltip(wrap, count) {
		if (!deckTipEl || !wrap) return;
		wrap.addEventListener('pointerenter', function (e) {
			hideBattleCardTooltip();
			showBattleDeckTooltip(count, e.clientX, e.clientY);
		});
		wrap.addEventListener('pointermove', function (e) {
			if (!deckTipEl.hidden) positionBattleDeckTooltip(e.clientX, e.clientY);
		});
		wrap.addEventListener('pointerleave', hideBattleDeckTooltip);
	}

	function wireRestStackInteractions(wrap, count, onOpenList) {
		if (!wrap) return;
		// Hover: show "〇枚" popup (reuse deck tooltip)
		wireDeckStackTooltip(wrap, count);
		// 右クリック: 座標から重なっている .rest-stack__card を特定（CSS で pointer-events:none のため）
		wrap.addEventListener('contextmenu', function (e) {
			const stackEls = document.elementsFromPoint(e.clientX, e.clientY);
			for (let i = 0; i < stackEls.length; i++) {
				const el = stackEls[i];
				if (!(el instanceof HTMLElement)) continue;
				if (!el.classList.contains('rest-stack__card')) continue;
				const cid = el.dataset.battleCardId;
				const d = cid && lastDefsForTooltip ? resolveCardDef(lastDefsForTooltip, cid) : null;
				if (d) {
					e.preventDefault();
					const dFull = lastDefsForTooltip ? zoneOrHandDetailDef(el, el.dataset.battleCardId) : d;
					const bc = battleCardFromZoneHandOrRestEl(el);
					showBattleZoneDetailModal(dFull || d, [], lastStateForHandPower, bc);
				}
				return;
			}
		});
		// Click: open list modal（カード面上はダブルクリックと区別するため短い遅延）
		if (typeof onOpenList === 'function') {
			wrap.addEventListener('click', function (e) {
				const cardHit = e.target.closest('.rest-stack__card');
				clearTimeout(wrap._restListDelay);
				wrap._restListDelay = null;
				if (cardHit) {
					if (e.detail >= 2) {
						return;
					}
					wrap._restListDelay = setTimeout(function () {
						wrap._restListDelay = null;
						onOpenList();
					}, 280);
					return;
				}
				onOpenList();
			});
			wrap.addEventListener('keydown', function (e) {
				if (e.key === 'Enter' || e.key === ' ') {
					e.preventDefault();
					onOpenList();
				}
			});
		}
	}

	/** デッキ枚数ぶんカード裏を少しずつずらして重ねる（メタの card_back ＝ カードうら画像） */
	function renderDeckStackVisual(count, deckLabel, options) {
		const o = options || {};
		const offsetPx = o.stackOffsetPx != null ? o.stackOffsetPx : 3;
		const wrap = document.createElement('div');
		wrap.className = 'deck-stack deck-stack--visual';
		const n = Math.max(0, Math.floor(Number(count) || 0));
		wrap.setAttribute('role', 'img');
		wrap.setAttribute('aria-label', deckLabel + ' ' + n + '枚');

		if (n === 0) {
			wrap.appendChild(el('span', 'deck-stack__empty', '0枚'));
			wireDeckStackTooltip(wrap, n);
			return wrap;
		}

		const pile = el('div', 'deck-stack__pile');
		for (let i = 0; i < n; i++) {
			const im = document.createElement('img');
			im.src = absUrl(cardBack);
			im.alt = '';
			im.className = 'deck-stack__back';
			im.decoding = 'async';
			const fromTop = n - 1 - i;
			im.style.left = fromTop * offsetPx + 'px';
			im.style.top = fromTop * offsetPx + 'px';
			im.style.zIndex = String(i + 1);
			pile.appendChild(im);
		}
		wrap.appendChild(pile);
		wireDeckStackTooltip(wrap, n);
		return wrap;
	}

	/** レスト：置かれているカードを少し重ねて表示（一覧はクリックでモーダル・右クリックで詳細） */
	function renderRestStackVisual(restCards, defs, titleText, options) {
		const o = options || {};
		const list = Array.isArray(restCards) ? restCards : [];
		const n = Math.max(0, Math.floor(list.length));
		const maxVisual = o.maxVisual != null ? o.maxVisual : 5;
		const offsetPx = o.stackOffsetPx != null ? o.stackOffsetPx : 3;

		const wrap = document.createElement('button');
		wrap.type = 'button';
		wrap.className = 'rest-stack deck-stack deck-stack--visual';
		wrap.setAttribute('aria-label', (titleText || 'レスト') + ' ' + String(n) + '枚');
		wrap.title = 'クリックで一覧';

		if (n === 0) {
			wrap.appendChild(el('span', 'deck-stack__empty', '0枚'));
			wireRestStackInteractions(wrap, n, function () {
				showRestModal(list, defs, titleText || 'レスト', o.battleState);
			});
			return wrap;
		}

		const pile = el('div', 'deck-stack__pile rest-stack__pile');
		const vis = Math.min(n, Math.max(1, maxVisual));
		const tail = list.slice(Math.max(0, n - vis)); // 上に出すのは直近（末尾）側
		for (let i = 0; i < tail.length; i++) {
			const c = tail[i];
			const d = resolveCardDef(defs, c.cardId);
			const cardHost = el('div', 'rest-stack__card');
			const fromTop = tail.length - 1 - i;
			cardHost.style.left = fromTop * offsetPx + 'px';
			cardHost.style.top = fromTop * offsetPx + 'px';
			cardHost.style.zIndex = String(i + 1);
			if (c.instanceId != null) cardHost.dataset.battleInstanceId = String(c.instanceId);

			if (d) {
				// 表向き（レスト専用サイズは CSS で）
				const disp = cardDefForBattleFace(d, c, defs);
				const shell = buildBattleCardFaceShell(disp, 'rest');
				cardHost.appendChild(shell);
				if (o.battleState && !d.fieldCard) {
					applyCurrentCostDisplay(shell, disp, o.battleState, c);
				}
				applyBattleCardTipData(cardHost, disp);
				cardHost.addEventListener('dblclick', function (e) {
					e.preventDefault();
					e.stopPropagation();
					clearTimeout(wrap._restListDelay);
					wrap._restListDelay = null;
					showBattleZoneDetailModal(disp, [], o.battleState, c);
				});
			} else {
				const im = document.createElement('img');
				im.src = absUrl(cardBack);
				im.alt = '裏';
				im.className = 'deck-stack__back';
				im.decoding = 'async';
				cardHost.appendChild(im);
			}
			pile.appendChild(cardHost);
		}
		wrap.appendChild(pile);

		wireRestStackInteractions(wrap, n, function () {
			showRestModal(list, defs, titleText || 'レスト', o.battleState);
		});
		return wrap;
	}

	function renderHandCards(hand, defs, { faceDown, selectable, compactOpp, nextDeployBonus, nextElfOnlyBonus, nextDeployCostBonusTimes, nextMechanicStacks, battleState }) {
		const wrap = el('div', faceDown ? 'hand backs' : 'hand');
		if (faceDown && compactOpp) {
			wrap.classList.add('hand--opp-backs');
		}
		if (faceDown) {
			for (let i = 0; i < hand.length; i++) {
				const im = document.createElement('img');
				im.src = absUrl(cardBack);
				im.alt = '裏';
				wrap.appendChild(im);
			}
			return wrap;
		}
		hand.forEach((c) => {
			const dRaw = defs[c.cardId] != null ? defs[c.cardId] : resolveCardDef(defs, c.cardId);
			const d = cardDefForBattleFace(dRaw, c, defs);
			const pending666 =
				battleState &&
				(battleState.spec666NextHumanUndead === true || battleState.spec666NextHumanUndead === 'true');
			const cardWrap = el('button', 'hand-card battle-card', null);
			cardWrap.type = 'button';
			cardWrap.dataset.instanceId = c.instanceId;
			cardWrap.dataset.cardId = String(c.cardId);
			cardWrap.disabled = !selectable;
			if (ui.selectedInstanceId != null && String(ui.selectedInstanceId) === String(c.instanceId)) {
				cardWrap.classList.add('is-selected');
			}
			const caret = el('span', 'hand-card__select-caret', '▼');
			caret.setAttribute('aria-hidden', 'true');
			cardWrap.appendChild(caret);
			const focusWrap = el('div', 'hand-card__card-focus', null);
			focusWrap.dataset.animKey = 'card:' + c.instanceId;
			if (d) {
				const shell = buildBattleCardFaceShell(d, 'hand');
				const basePow = Number(d.basePower != null ? d.basePower : 0);
				let bonus = Number(nextDeployBonus || 0);
				if (Number(nextElfOnlyBonus || 0) > 0 && hasCardAttributeForDeployPreview(dRaw, c, pending666, 'ELF')) {
					bonus += Number(nextElfOnlyBonus || 0);
				}
				if (Number(nextDeployCostBonusTimes || 0) > 0) {
					const c = Number(d.cost != null ? d.cost : 0);
					bonus += c * Number(nextDeployCostBonusTimes || 0);
				}
				bonus += 3 * Number(nextMechanicStacks || 0);
				const curPow = lastStateForHandPower
					? previewHumanBattlePowerForHand(lastStateForHandPower, defs, d, bonus, c)
					: (basePow + bonus);
				applyCurrentPowerDisplay(shell, basePow, curPow);
				maybeSparkPowerIncrease(shell, c.instanceId, curPow);
				if (battleState && d) {
					applyCurrentCostDisplay(shell, d, battleState, c);
				}
				focusWrap.appendChild(shell);
				cardWrap.appendChild(focusWrap);
				applyBattleCardTipData(cardWrap, d);
			} else {
				cardWrap.appendChild(focusWrap);
			}
			wrap.appendChild(cardWrap);
		});
		return wrap;
	}

	function selectedCard(st) {
		if (ui.selectedInstanceId == null || ui.selectedInstanceId === '') return null;
		const want = String(ui.selectedInstanceId);
		const hand = st.humanHand;
		if (!Array.isArray(hand)) return null;
		return hand.find((c) => c != null && String(c.instanceId) === want) || null;
	}

	/** CpuBattleEngine と同じ ID（effectiveBattlePower プレビュー用） */
	const PREVIEW_CARD_IDS = {
		RYUOH: 30,
		KUSURI: 8,
		ARCHER: 12,
		DRAGON_RIDER: 10,
		GAIKOTSU: 18,
		SHIREI: 20,
		HONE: 24,
		ARTHUR: 43,
		FIELD_GLORIA: 41,
		FIELD_KAMUI: 49,
		/** 武器庫 VV-E4-PON（〈フィールド〉・マシン・ファイターコスト1） */
		WEAPON_DEPOT_FIELD: 64,
		/** ネムリィ（CpuBattleEngine.NEMURY_ID） */
		NEMURY: 40,
		/** ノクスクル（id=37 / STONIA：所持ストーン1つにつき強さ+1 / CpuBattleEngine） */
		STONIA: 37,
		/** シャイニ（常時：レストのカーバンクル種類×+2 / CpuBattleEngine.SHINY_ID） */
		SHINY: 31,
		/** 磁力合体デンジリオン / CpuBattleEngine.DENZIRION_ID */
		DENZIRION: 59,
		/** ガラクタレッグ / CpuBattleEngine.GARAKUTA_LEG_ID */
		GARAKUTA_LEG: 61,
		/** 廃棄工場 5C-R4P / CpuBattleEngine.SCRAPYARD_FIELD_ID */
		FIELD_SCRAPYARD: 63,
		/** 霊園教会 デスバウンス / CpuBattleEngine.DEATHBOUNCE_FIELD_ID */
		FIELD_DEATHBOUNCE: 68
	};

	/** 〈フィールド〉持続ターン系: 効果文の「◯ターンの間」を残りターン N に合わせる */
	function adjustScrapyardFieldAbilityTextForRemaining(text, remaining) {
		const r = remaining != null ? Math.max(0, Math.floor(Number(remaining))) : 0;
		if (r < 1 || text == null) return String(text || '');
		return String(text).replace(/\d+ターンの間/g, function () {
			return r + 'ターンの間';
		});
	}

	function defWithTimedFieldTurnOverlay(d, st) {
		if (!d || !st) return d;
		const id = Number(d.id);
		let r = 0;
		if (id === PREVIEW_CARD_IDS.FIELD_SCRAPYARD && st.scrapyardFieldTurnsRemaining != null) {
			r = Math.max(0, Math.floor(Number(st.scrapyardFieldTurnsRemaining)));
		} else if (id === PREVIEW_CARD_IDS.FIELD_DEATHBOUNCE && st.deathbounceFieldTurnsRemaining != null) {
			r = Math.max(0, Math.floor(Number(st.deathbounceFieldTurnsRemaining)));
		}
		if (r < 1 || !Array.isArray(d.abilityBlocks)) return d;
		const blocks = d.abilityBlocks.map(function (b) {
			const body = b.body != null ? String(b.body) : '';
			return Object.assign({}, b, { body: adjustScrapyardFieldAbilityTextForRemaining(body, r) });
		});
		return Object.assign({}, d, { abilityBlocks: blocks });
	}

	/**
	 * 薬売りデバフが自分ファイターにかかるか（CpuBattleEngine.fighterIgnoresKusuriDebuffDueToGarakutaLeg の否定）
	 */
	function kusuriDebuffAppliesToCardPreview(defs, mainDef, rest) {
		if (!mainDef) return true;
		const id = Number(mainDef.id);
		if (id === PREVIEW_CARD_IDS.GARAKUTA_LEG) return false;
		if (id !== PREVIEW_CARD_IDS.DENZIRION) return true;
		const r = Array.isArray(rest) ? rest : [];
		for (let i = 0; i < r.length; i++) {
			const rc = r[i];
			if (!rc) continue;
			const rcd = resolveCardDef(defs, rc.cardId);
			if (!weaponDepotMachineFighterForCost(rcd, null)) continue;
			if (Number(rc.cardId) === PREVIEW_CARD_IDS.DENZIRION) continue;
			if (Number(rc.cardId) === PREVIEW_CARD_IDS.GARAKUTA_LEG) return false;
		}
		return true;
	}

	function activeFieldIsKamui(st) {
		return st && st.activeField && Number(st.activeField.cardId) === PREVIEW_CARD_IDS.FIELD_KAMUI;
	}

	function weaponDepotFieldActive(st) {
		return st && st.activeField && Number(st.activeField.cardId) === PREVIEW_CARD_IDS.WEAPON_DEPOT_FIELD;
	}

	/** 〈フィールド〉以外のマシン・ファイター（印字コストとの差で緑／赤表示） */
	function weaponDepotMachineFighterForCost(def, handCard) {
		if (!def || def.fieldCard) return false;
		const ck = String(def.cardKind || '').trim().toUpperCase();
		/* CardDefDto に cardKind が無い古い応答でも、フィールドでないマシンはファイター扱い */
		if (ck !== '' && ck !== 'FIGHTER') return false;
		return hasCardAttributeResolved(def, handCard, 'MACHINE');
	}

	/** 隊長「コストぶん強化」用: 武器庫・ネムリィを反映（メカニック/手札コスト補正は含めない） */
	function characteristicDeployCostForCaptainBonus(def, handCard, st) {
		if (!def) return 0;
		let base = Number(def.cost != null ? def.cost : 0);
		if (weaponDepotFieldActive(st) && weaponDepotMachineFighterForCost(def, handCard)) {
			base = 1;
		}
		if (handCard && (handCard.blankEffects === true || handCard.blankEffects === 'true')) {
			return Math.max(0, base);
		}
		if (Number(def.id) === PREVIEW_CARD_IDS.NEMURY) {
			const defs = st && st.defs ? st.defs : {};
			const disc = countAttributeInRest(st && st.humanRest ? st.humanRest : [], defs, 'CARBUNCLE');
			return Math.max(0, base - disc);
		}
		return base;
	}

	/**
	 * 〈宝石の地 グロリア輝石台地〉: 場にある間、カーバンクルは強さ+2（CpuBattleEngine.fieldGloriaCarbunclePowerBonus と同順・同条件）
	 */
	function fieldGloriaCarbunclePowerBonus(st, def, battleCard) {
		if (!st || !def || def.fieldCard) return 0;
		if (!st.activeField || Number(st.activeField.cardId) !== PREVIEW_CARD_IDS.FIELD_GLORIA) return 0;
		if (!hasCardAttributeResolved(def, battleCard, 'CARBUNCLE')) return 0;
		return 2;
	}

	/** 画面上のストーン表示と同じ（レベルアップ・支払モーダルで予約した分を除く。ネビュラ坑道の+1は配置コミット応答に含まれる） */
	function humanStonesPreviewReserveAdjusted(st) {
		if (!st) return 0;
		const levelUpUsed = ui.levelUpStones | 0;
		const payUsed = ui.pay && typeof ui.pay.stones === 'number' ? ui.pay.stones | 0 : 0;
		return Math.max(0, Number(st.humanStones || 0) - levelUpUsed - payUsed);
	}

	function attrSegments(attribute) {
		if (attribute == null || String(attribute).trim() === '') return [];
		return String(attribute).split('_').filter(Boolean);
	}

	function hasCardAttribute(attribute, tribe) {
		if (!tribe) return false;
		const a = attribute == null ? '' : String(attribute);
		if (!a) return false;
		if (a === tribe) return true;
		return attrSegments(a).indexOf(tribe) >= 0;
	}

	function zoneMainDef(zone, defs) {
		if (!zone || !zone.main) return null;
		return resolveCardDef(defs, zone.main.cardId);
	}

	function hasRyuohInCpuZone(cpuBattle, defs) {
		const d = zoneMainDef(cpuBattle, defs);
		return d != null && Number(d.id) === PREVIEW_CARD_IDS.RYUOH;
	}

	function hasRyuohInHumanZone(humanBattle, defs) {
		const d = zoneMainDef(humanBattle, defs);
		return d != null && Number(d.id) === PREVIEW_CARD_IDS.RYUOH;
	}

	/**
	 * レベルアップで選んだ手札がレストへ移った後の自分レスト（配置前・プレビュー用）。
	 * discardIds は ui.levelUpDiscardIds（選択中の instanceId）に対応。
	 */
	function simulateHumanRestAfterLevelUp(st, discardIds) {
		const rest = (st.humanRest || []).slice();
		const hand = (st.humanHand || []).slice();
		const ids = Array.isArray(discardIds) ? discardIds : [];
		for (let i = 0; i < ids.length; i++) {
			const inst = ids[i];
			if (!inst) continue;
			const idx = hand.findIndex((c) => c.instanceId === inst);
			if (idx >= 0) {
				rest.push(hand.splice(idx, 1)[0]);
			}
		}
		return rest;
	}

	function restContainsTribe(rest, defs, tribe) {
		for (let i = 0; i < rest.length; i++) {
			const card = rest[i];
			const d = resolveCardDef(defs, card.cardId);
			if (d && hasCardAttributeResolved(d, card, tribe)) return true;
		}
		return false;
	}

	function countAttributeInRest(rest, defs, attr) {
		let c = 0;
		if (!Array.isArray(rest)) return 0;
		for (let i = 0; i < rest.length; i++) {
			const card = rest[i];
			const d = resolveCardDef(defs, card.cardId);
			if (d && hasCardAttributeResolved(d, card, attr)) c++;
		}
		return c;
	}

	function countUndeadInRest(rest, defs) {
		return countAttributeInRest(rest, defs, 'UNDEAD');
	}

	/** CpuBattleEngine.battleCostUnderInstanceIds 相当（自分バトルのコスト下） */
	function battleCostUnderInstanceIdSet(zone) {
		const o = Object.create(null);
		if (!zone || !Array.isArray(zone.costUnder)) return o;
		for (let i = 0; i < zone.costUnder.length; i++) {
			const c = zone.costUnder[i];
			if (c && c.instanceId != null) o[String(c.instanceId)] = true;
		}
		return o;
	}

	/**
	 * CpuBattleEngine.countDistinctCarbuncleTypesInRest と同じ（レストの種族カーバンクルの種類数。
	 * 自分バトルに重なっている instance は除外）
	 */
	function countDistinctCarbuncleTypesInRestForPreview(defs, rest, ownBattleZone) {
		if (!defs || !Array.isArray(rest)) return 0;
		const under = battleCostUnderInstanceIdSet(ownBattleZone);
		const seen = Object.create(null);
		let n = 0;
		for (let i = 0; i < rest.length; i++) {
			const c = rest[i];
			if (!c) continue;
			if (c.instanceId != null && under[String(c.instanceId)]) continue;
			const d = resolveCardDef(defs, c.cardId);
			if (!d || !hasCardAttributeResolved(d, c, 'CARBUNCLE')) continue;
			const cid = Number(c.cardId);
			if (seen[cid]) continue;
			seen[cid] = true;
			n++;
		}
		return n;
	}

	function computeDeployBonus(def, levelUpRest, levelUpStones) {
		if (!def) return 0;
		return levelUpRest * 2 + levelUpStones * 2;
	}

	/**
	 * ファイター配置の必要コスト（CpuBattleEngine.effectiveDeployCost と同順）
	 * ・ネムリィ: 自分レストのカーバンクル枚数ぶん基本コストから減算（炭鉱夫無効時は割引なし）
	 * ・メカニック残機・研究者アストリアの手札補正
	 */
	function effectiveFighterDeployCostFromState(def, st, handCard) {
		if (!def || def.fieldCard) return Number(def.cost != null ? def.cost : 0);
		let base = Number(def.cost != null ? def.cost : 0);
		if (weaponDepotFieldActive(st) && weaponDepotMachineFighterForCost(def, handCard)) {
			base = 1;
		}
		const mechanic = Number(st && st.humanNextMechanicStacks != null ? st.humanNextMechanicStacks : 0);
		let handAdj = 0;
		if (handCard && handCard.handDeployCostModifier != null) {
			handAdj = Number(handCard.handDeployCostModifier);
		}
		if (handCard && (handCard.blankEffects === true || handCard.blankEffects === 'true')) {
			return Math.max(0, base + mechanic + handAdj);
		}
		let core = base;
		if (Number(def.id) === PREVIEW_CARD_IDS.NEMURY) {
			const defs = st && st.defs ? st.defs : {};
			const disc = countAttributeInRest(st && st.humanRest ? st.humanRest : [], defs, 'CARBUNCLE');
			core = Math.max(0, base - disc);
		}
		return Math.max(0, core + mechanic + handAdj);
	}

	function computeHumanNextDeployBonusForDef(st, def, handCard) {
		if (!st || !def) return 0;
		let bonus = Number(st.humanNextDeployBonus || 0);
		const elfOnly = Number(st.humanNextElfOnlyBonus || 0);
		const p666 = !!(st.spec666NextHumanUndead === true || st.spec666NextHumanUndead === 'true');
		if (elfOnly > 0 && hasCardAttributeForDeployPreview(def, handCard, p666, 'ELF')) {
			bonus += elfOnly;
		}
		const costTimes = Number(st.humanNextDeployCostBonusTimes || 0);
		if (costTimes > 0) {
			bonus += characteristicDeployCostForCaptainBonus(def, handCard, st) * costTimes;
		}
		bonus += 3 * Number(st.humanNextMechanicStacks || 0);
		return bonus;
	}

	/** 手札表示用: 「いま配置したら」の常時効果込み強さ（UIで選んだレベルアップ指定は含めない） */
	function previewHumanBattlePowerForHand(st, defs, mainDef, deployBonus, handCard) {
		if (!mainDef) return 0;
		const id = Number(mainDef.id);
		const base = Number(mainDef.basePower != null ? mainDef.basePower : 0);
		let p = base + Number(deployBonus || 0);
		p += fieldGloriaCarbunclePowerBonus(st, mainDef, handCard);

		// ノクスクル(id=37/STONIA): 自分のターンの終わりまで所持ストーン数を加算（CpuBattleEngine と同様）
		if (id === PREVIEW_CARD_IDS.STONIA && st.humansTurn) {
			p += humanStonesPreviewReserveAdjusted(st);
		}

		// 竜王が相手ゾーンにいると〈常時〉等は無効化（〈宝石の地〉の+2は engine と同様に残す）
		if (hasRyuohInCpuZone(st.cpuBattle, defs)) {
			return Math.max(0, p);
		}

		// 以下は CpuBattleEngine.effectiveBattlePower 相当（自分視点）
		// 薬売り（相手の常時）: 相手が薬売りを配置している間、自分ファイターは相手ストーン数ぶん弱体化
		// ただし自分が竜王を配置している場合、相手の常時は無効
		if (!hasRyuohInHumanZone(st.humanBattle) && st.cpuBattle && st.cpuBattle.main) {
			const oppDef = resolveCardDef(defs, st.cpuBattle.main.cardId);
			if (oppDef && Number(oppDef.id) === PREVIEW_CARD_IDS.KUSURI && kusuriDebuffAppliesToCardPreview(defs, mainDef, st.humanRest)) {
				p -= Number(st.cpuStones || 0);
			}
		}

		if (handCard && (handCard.blankEffects === true || handCard.blankEffects === 'true')) {
			return Math.max(0, p);
		}

		if (id === PREVIEW_CARD_IDS.ARCHER) {
			const opp = st.cpuBattle;
			if (opp && opp.main) {
				const om = opp.main;
				const od = resolveCardDef(defs, om.cardId);
				if (!hasCardAttributeResolved(od, om, 'DRAGON')) {
					p += 1;
				}
			}
		}

		if (id === PREVIEW_CARD_IDS.DRAGON_RIDER) {
			if (restContainsTribe(st.humanRest || [], defs, 'DRAGON')) {
				p += 4;
			}
		}

		if (id === PREVIEW_CARD_IDS.GAIKOTSU) {
			const opp = st.cpuBattle;
			if (opp && opp.main) {
				const om = opp.main;
				const od = resolveCardDef(defs, om.cardId);
				if (hasCardAttributeResolved(od, om, 'ELF')) {
					p += 2;
				}
			}
		}

		if (id === PREVIEW_CARD_IDS.SHIREI) {
			const opp = st.cpuBattle;
			if (opp && opp.main) {
				const om = opp.main;
				const od = resolveCardDef(defs, om.cardId);
				if (!hasCardAttributeResolved(od, om, 'HUMAN')) {
					p += 1;
				}
			}
		}

		if (id === PREVIEW_CARD_IDS.HONE) {
			p += countUndeadInRest(st.humanRest || [], defs);
		}

		if (id === PREVIEW_CARD_IDS.SHINY) {
			p += countDistinctCarbuncleTypesInRestForPreview(defs, st.humanRest || [], st.humanBattle) * 2;
		}

		if (id === PREVIEW_CARD_IDS.ARTHUR && activeFieldIsKamui(st)) {
			p += 3;
		}

		return Math.max(0, p);
	}

	/**
	 * 選択カードを自分バトルゾーンに置いたときの強さ（CpuBattleEngine.effectiveBattlePower 相当）
	 */
	function previewHumanBattlePower(st, defs, mainDef, deployBonus, handCard) {
		if (!mainDef) return 0;
		const id = Number(mainDef.id);
		const base = Number(mainDef.basePower != null ? mainDef.basePower : 0);
		let p = base + deployBonus;
		p += fieldGloriaCarbunclePowerBonus(st, mainDef, handCard);

		if (id === PREVIEW_CARD_IDS.STONIA && st.humansTurn) {
			p += humanStonesPreviewReserveAdjusted(st);
		}

		if (hasRyuohInCpuZone(st.cpuBattle, defs)) {
			return Math.max(0, p);
		}

		const stonesAfterLevel = st.humanStones - (ui.levelUpStones | 0);
		const simRest = simulateHumanRestAfterLevelUp(st, ui.levelUpDiscardIds);

		// 薬売り（相手の常時）: 相手が薬売りを配置している間、自分ファイターは相手ストーン数ぶん弱体化
		// ただし自分が竜王を配置している場合、相手の常時は無効
		if (!hasRyuohInHumanZone(st.humanBattle) && st.cpuBattle && st.cpuBattle.main) {
			const oppDef = resolveCardDef(defs, st.cpuBattle.main.cardId);
			if (oppDef && Number(oppDef.id) === PREVIEW_CARD_IDS.KUSURI && kusuriDebuffAppliesToCardPreview(defs, mainDef, simRest)) {
				p -= Number(st.cpuStones || 0);
			}
		}

		if (handCard && (handCard.blankEffects === true || handCard.blankEffects === 'true')) {
			return Math.max(0, p);
		}

		if (id === PREVIEW_CARD_IDS.ARCHER) {
			const opp = st.cpuBattle;
			if (opp && opp.main) {
				const om = opp.main;
				const od = resolveCardDef(defs, om.cardId);
				if (!hasCardAttributeResolved(od, om, 'DRAGON')) {
					p += 1;
				}
			}
		}

		if (id === PREVIEW_CARD_IDS.DRAGON_RIDER) {
			if (restContainsTribe(simRest, defs, 'DRAGON')) {
				p += 4;
			}
		}

		if (id === PREVIEW_CARD_IDS.GAIKOTSU) {
			const opp = st.cpuBattle;
			if (opp && opp.main) {
				const om = opp.main;
				const od = resolveCardDef(defs, om.cardId);
				if (hasCardAttributeResolved(od, om, 'ELF')) {
					p += 2;
				}
			}
		}

		if (id === PREVIEW_CARD_IDS.SHIREI) {
			const opp = st.cpuBattle;
			if (opp && opp.main) {
				const om = opp.main;
				const od = resolveCardDef(defs, om.cardId);
				if (!hasCardAttributeResolved(od, om, 'HUMAN')) {
					p += 1;
				}
			}
		}

		if (id === PREVIEW_CARD_IDS.HONE) {
			p += countUndeadInRest(simRest, defs);
		}

		if (id === PREVIEW_CARD_IDS.SHINY) {
			p += countDistinctCarbuncleTypesInRestForPreview(defs, simRest, st.humanBattle) * 2;
		}

		if (id === PREVIEW_CARD_IDS.ARTHUR && activeFieldIsKamui(st)) {
			p += 3;
		}

		return Math.max(0, p);
	}

	function applyLevelUpPreviewPower(shellRoot, basePower, previewPower) {
		const powEl = shellRoot.querySelector('.card-face__power');
		if (!powEl || powEl.classList.contains('card-face__power--hidden')) return;
		const pv = Math.max(0, Math.floor(Number(previewPower)));
		const baseNum = Math.floor(Number(basePower));
		powEl.textContent = String(pv);
		powEl.classList.toggle('card-face__power--digit-4', pv === 4);
		powEl.classList.remove('battle-levelup-power--up', 'battle-levelup-power--down');
		if (pv > baseNum) powEl.classList.add('battle-levelup-power--up');
		else if (pv < baseNum) powEl.classList.add('battle-levelup-power--down');
	}

	function applyCurrentPowerDisplay(shellRoot, basePower, currentPower) {
		const powEl = shellRoot.querySelector('.card-face__power');
		if (!powEl || powEl.classList.contains('card-face__power--hidden')) return;
		const pv = Math.max(0, Math.floor(Number(currentPower)));
		const baseNum = Math.floor(Number(basePower));
		powEl.textContent = String(pv);
		powEl.classList.toggle('card-face__power--digit-4', pv === 4);
		powEl.classList.remove('battle-levelup-power--up', 'battle-levelup-power--down');
		powEl.style.color = '';
		if (pv > baseNum) {
			powEl.classList.add('battle-levelup-power--up');
			powEl.style.color = '#22c55e';
		} else if (pv < baseNum) {
			powEl.classList.add('battle-levelup-power--down');
			powEl.style.color = '#ef4444';
		}
	}

	/** 手札・モーダル：実効コストを表示。カードに印字のコストより下げ＝緑、上げ＝赤 */
	function applyCurrentCostDisplay(shellRoot, def, st, handCard) {
		if (!shellRoot || !def || def.fieldCard) return;
		const costEl = shellRoot.querySelector('.card-face__cost');
		if (!costEl) return;
		const baseNum = Math.max(0, Math.floor(Number(def.cost != null ? def.cost : 0)));
		const pv = Math.max(0, Math.floor(Number(effectiveFighterDeployCostFromState(def, st, handCard))));
		costEl.textContent = String(pv);
		costEl.classList.remove('card-face__cost--digit-1', 'card-face__cost--digit-2');
		if (pv === 1) costEl.classList.add('card-face__cost--digit-1');
		else if (pv === 2) costEl.classList.add('card-face__cost--digit-2');
		costEl.classList.remove('battle-deploy-cost--up', 'battle-deploy-cost--down');
		costEl.style.color = '';
		if (pv < baseNum) {
			costEl.classList.add('battle-deploy-cost--down');
			costEl.style.color = '#22c55e';
		} else if (pv > baseNum) {
			costEl.classList.add('battle-deploy-cost--up');
			costEl.style.color = '#ef4444';
		}
	}

	function maybeSparkPowerIncrease(shellRoot, instanceId, currentPower) {
		if (!shellRoot || !instanceId) return;
		const powEl0 = shellRoot.querySelector('.card-face__power');
		if (powEl0 && powEl0.classList.contains('card-face__power--hidden')) return;
		const pv = Math.max(0, Math.floor(Number(currentPower)));
		const prev = ui._prevPowerByInstanceId[instanceId];
		ui._prevPowerByInstanceId[instanceId] = pv;
		if (prev == null) return;
		if (pv <= prev) return;
		const powEl = shellRoot.querySelector('.card-face__power');
		const sparkHost = wrapLevelUpPreviewPowerSparkHost(powEl);
		if (sparkHost) {
			appendLevelUpValueSparkBurst(sparkHost);
		}
	}

	function applyCurrentPowerDisplayToBattleCardFace(st, defs, shellRoot, instanceId, def, options) {
		if (!shellRoot || !def) return;
		if (def.fieldCard) return;
		const o = options || {};
		let handCard = o.handCard;
		if (!handCard && st && st.humanHand && instanceId) {
			handCard = st.humanHand.find(function (x) {
				return x && String(x.instanceId) === String(instanceId);
			});
		}
		const basePow = Number(def.basePower != null ? def.basePower : 0);
		let bonus = 0;
		if (o.includeNextDeployBonus) {
			bonus += Number(st && st.humanNextDeployBonus || 0);
			const elfOnly = Number(st && st.humanNextElfOnlyBonus || 0);
			if (elfOnly > 0 && hasCardAttribute(def.attribute, 'ELF')) bonus += elfOnly;
			const costTimes = Number(st && st.humanNextDeployCostBonusTimes || 0);
			if (costTimes > 0) bonus += characteristicDeployCostForCaptainBonus(def, handCard, st) * costTimes;
			bonus += 3 * Number(st && st.humanNextMechanicStacks != null ? st.humanNextMechanicStacks : 0);
		}
		const curPow = st ? previewHumanBattlePowerForHand(st, defs, def, bonus, handCard) : (basePow + bonus);
		applyCurrentPowerDisplay(shellRoot, basePow, curPow);
		if (st) {
			applyCurrentCostDisplay(shellRoot, def, st, handCard);
		}
		if (instanceId) {
			maybeSparkPowerIncrease(shellRoot, instanceId, curPow);
		}
	}

	/** 強さ表示周りのキラ（レベルアップ数値と同一アニメ）用。card-face__power は absolute のためホストで枠を取る */
	function wrapLevelUpPreviewPowerSparkHost(powEl) {
		if (!powEl || !powEl.parentNode) return null;
		const p = powEl.parentNode;
		if (p.classList && p.classList.contains('battle-levelup-preview-power-spark-host')) {
			return p;
		}
		const host = document.createElement('span');
		host.className = 'battle-levelup-preview-power-spark-host battle-control__value--spark-host';
		p.insertBefore(host, powEl);
		host.appendChild(powEl);
		return host;
	}

	function renderBattleFieldSlot(dto, defs, opts) {
		opts = opts || {};
		const opponentZone = opts.opponentZone === true;
		const box = el('div', 'battle-field-slot');
		box.appendChild(el('p', 'battle-field-label', '《フィールド》'));
		if (!dto || dto.cardId == null) {
			box.appendChild(el('p', 'muted', '—'));
			return box;
		}
		const d = resolveCardDef(defs, dto.cardId);
		if (!d) {
			box.appendChild(el('p', 'muted', '—'));
			return box;
		}
		const st = opts.battleState;
		let rem = 0;
		if (st && Number(d.id) === PREVIEW_CARD_IDS.FIELD_SCRAPYARD && st.scrapyardFieldTurnsRemaining != null) {
			rem = Math.max(0, Math.floor(Number(st.scrapyardFieldTurnsRemaining)));
		} else if (st && Number(d.id) === PREVIEW_CARD_IDS.FIELD_DEATHBOUNCE && st.deathbounceFieldTurnsRemaining != null) {
			rem = Math.max(0, Math.floor(Number(st.deathbounceFieldTurnsRemaining)));
		}
		const dForFace = rem > 0 ? defWithTimedFieldTurnOverlay(d, st) : d;
		const wrap = el('div', 'library-card battle-zone-card battle-field-card', null);
		const shell = buildBattleCardFaceShell(dForFace, opponentZone ? 'hand' : 'zone');
		if (opponentZone) {
			const faceMount = el('div', 'hand-card__card-focus battle-zone-card__opp-face', null);
			faceMount.dataset.animKey = 'field:' + dto.instanceId;
			faceMount.appendChild(shell);
			const chrome = wrapLibraryCardOpenChrome(faceMount);
			chrome.style.position = 'relative';
			chrome.style.zIndex = '1';
			wrap.appendChild(chrome);
		} else {
			wrap.dataset.animKey = 'field:' + dto.instanceId;
			const chrome = wrapLibraryCardOpenChrome(shell);
			chrome.style.position = 'relative';
			chrome.style.zIndex = '1';
			wrap.appendChild(chrome);
		}
		applyBattleCardTipData(wrap, dForFace);
		if (rem > 0) {
			const badge = el('span', 'battle-field-turn-badge', String(rem));
			badge.setAttribute('aria-label', '残りターン ' + String(rem));
			wrap.appendChild(badge);
		}
		box.appendChild(wrap);
		return box;
	}

	function renderZone(zone, defs, power, opts) {
		opts = opts || {};
		const opponentZone = opts.opponentZone === true;
		const box = el('div', 'zone');
		if (!zone || !zone.main) {
			box.appendChild(el('p', 'muted', 'なし'));
			return box;
		}
		const d = cardDefForBattleFace(resolveCardDef(defs, zone.main.cardId), zone.main, defs);
		if (d) {
			const wrap = el('div', 'library-card battle-zone-card', null);
			if (zone.main.instanceId != null) wrap.dataset.battleInstanceId = String(zone.main.instanceId);
			// Under-cards (cost/levelup): show as a small fanned stack of card backs
			const under = Array.isArray(zone.costUnder)
				? zone.costUnder
				: Array.isArray(zone.under)
					? zone.under
					: [];
			if (under.length) {
				const stack = el('div', '', null);
				stack.style.position = 'absolute';
				// Peek out from behind the card (deck-like)
				stack.style.left = '-10px';
				stack.style.top = '-10px';
				stack.style.pointerEvents = 'none';
				stack.style.opacity = '0.95';
				stack.style.zIndex = '0';
				const n = Math.min(under.length, 6);
				for (let i = 0; i < n; i++) {
					const im = document.createElement('img');
					im.src = absUrl(cardBack);
					im.alt = '';
					im.style.position = 'absolute';
					im.style.width = '34px';
					im.style.height = '48px';
					im.style.borderRadius = '4px';
					im.style.boxShadow = '0 6px 16px rgba(0,0,0,.25)';
					im.style.transform = 'translate(' + (i * 5) + 'px,' + (i * 4) + 'px)';
					im.style.zIndex = String(1 + i);
					stack.appendChild(im);
				}
				if (under.length > n) {
					const badge = el('div', '', '+' + String(under.length - n));
					badge.style.position = 'absolute';
					badge.style.left = (n * 5 + 4) + 'px';
					badge.style.top = (n * 4 + 4) + 'px';
					badge.style.fontSize = '12px';
					badge.style.padding = '2px 6px';
					badge.style.borderRadius = '999px';
					badge.style.background = 'rgba(0,0,0,.55)';
					badge.style.color = '#fff';
					badge.style.boxShadow = '0 6px 16px rgba(0,0,0,.25)';
					stack.appendChild(badge);
				}
				wrap.style.position = 'relative';
				// allow stack to peek outside the card chrome
				wrap.style.overflow = 'visible';
				wrap.appendChild(stack);
			}
			const shell = buildBattleCardFaceShell(d, opponentZone ? 'hand' : 'zone');
			applyCurrentPowerDisplay(shell, Number(d.basePower || 0), power);
			maybeSparkPowerIncrease(shell, zone.main.instanceId, power);
			if (opts.battleState && d && !d.fieldCard) {
				applyCurrentCostDisplay(shell, d, opts.battleState, zone.main);
			}
			if (opponentZone) {
				/* 手札と同じ battle-layered--hand + hand-card__card-focus でキラ等の見え方を揃える */
				const faceMount = el('div', 'hand-card__card-focus battle-zone-card__opp-face', null);
				faceMount.dataset.animKey = 'card:' + zone.main.instanceId;
				faceMount.appendChild(shell);
				const chrome = wrapLibraryCardOpenChrome(faceMount);
				chrome.style.position = 'relative';
				chrome.style.zIndex = '1';
				wrap.appendChild(chrome);
			} else {
				wrap.dataset.animKey = 'card:' + zone.main.instanceId;
				const chrome = wrapLibraryCardOpenChrome(shell);
				chrome.style.position = 'relative';
				chrome.style.zIndex = '1';
				wrap.appendChild(chrome);
			}
			applyBattleCardTipData(wrap, d);
			setBattleZonePowerContributorsDataset(wrap, zone.powerModifiers);
			box.appendChild(wrap);
		}
		box.appendChild(el('p', 'muted', '強さ: ' + String(power)));
		return box;
	}

	function captureAnimRects() {
		const m = new Map();
		app.querySelectorAll('[data-anim-key]').forEach(function (node) {
			const key = node.getAttribute('data-anim-key');
			if (!key) return;
			m.set(key, node.getBoundingClientRect());
		});
		return m;
	}

	function playFLIP(prevRects) {
		if (!prevRects || prevRects.size === 0) return;
		app.querySelectorAll('[data-anim-key]').forEach(function (node) {
			const key = node.getAttribute('data-anim-key');
			if (!key) return;
			const prev = prevRects.get(key);
			if (!prev) return;
			const next = node.getBoundingClientRect();
			const dx = prev.left - next.left;
			const dy = prev.top - next.top;
			if (Math.abs(dx) < 1 && Math.abs(dy) < 1) return;
			node.animate(
				[
					{ transform: 'translate(' + dx + 'px,' + dy + 'px)' },
					{ transform: 'translate(0,0)' }
				],
				{ duration: 260, easing: 'cubic-bezier(.2,.8,.2,1)' }
			);
		});
	}

	function appendLevelUpValueSparkBurst(host) {
		host.classList.add('battle-control__value--spark-host');
		const backLayer = document.createElement('span');
		backLayer.className = 'battle-control__value-sparks battle-control__value-sparks--back';
		backLayer.setAttribute('aria-hidden', 'true');
		const layer = document.createElement('span');
		layer.className = 'battle-control__value-sparks';
		layer.setAttribute('aria-hidden', 'true');
		const n = 14;
		for (let i = 0; i < n; i++) {
			const t = (i / n) * Math.PI * 2 + i * 0.35;
			const r = 10 + (i % 4) * 5;
			const sx = (Math.cos(t) * r).toFixed(1) + 'px';
			const rise = '-' + (26 + (i % 5) * 4 + Math.floor(i / 3)).toFixed(1) + 'px';
			const delay = (i * 0.03).toFixed(3) + 's';

			const db = document.createElement('span');
			db.className = 'battle-control__value-spark battle-control__value-spark--back';
			db.style.setProperty('--sx', sx);
			db.style.setProperty('--rise', rise);
			db.style.setProperty('--delay', delay);
			backLayer.appendChild(db);

			const d = document.createElement('span');
			d.className = 'battle-control__value-spark';
			d.style.setProperty('--sx', sx);
			d.style.setProperty('--rise', rise);
			d.style.setProperty('--delay', delay);
			layer.appendChild(d);
		}
		host.appendChild(backLayer);
		host.appendChild(layer);
	}

	function buildLevelUpValueEl(numStr, sparkKey) {
		const wrap = document.createElement('div');
		wrap.className = 'battle-control__value';
		const num = document.createElement('span');
		num.className = 'battle-control__value-num';
		num.textContent = numStr;
		wrap.appendChild(num);
		if (sparkKey === 'rest' && ui.sparkLevelUpRest) {
			ui.sparkLevelUpRest = false;
			appendLevelUpValueSparkBurst(wrap);
		} else if (sparkKey === 'stone' && ui.sparkLevelUpStone) {
			ui.sparkLevelUpStone = false;
			appendLevelUpValueSparkBurst(wrap);
		}
		return wrap;
	}

	/** レベルアップポップアップの「決定」— 委譲では取りこぼす環境があるため専用リスナーから呼ぶ */
	function runLevelUpDecideCommitFlow() {
		fetchState()
			.then(function (st) {
				ui.levelUpStones = clamp(ui.levelUpStones, 0, st.humanStones);
				ui.levelUpRest = clamp(
					ui.levelUpRest,
					0,
					maxLevelUpRestDiscard((st.humanHand && st.humanHand.length) || 0)
				);
				const sel = selectedCard(st);
				if (!sel) {
					showBattleToast('配置するカードが手札に見つかりません。手札からもう一度選んでください。', 'warn');
					render(st);
					return;
				}

				function proceedAfterDiscardConfirm() {
					const def = resolveCardDef(st.defs, sel.cardId);
					const cost = def && !def.fieldCard ? effectiveFighterDeployCostFromState(def, st, sel) : def ? Number(def.cost || 0) : 0;
					if (def && def.fieldCard) {
						if ((ui.levelUpRest | 0) !== 0 || (ui.levelUpStones | 0) !== 0) {
							showBattleToast('フィールドはレベルアップできません', 'warn');
							return;
						}
					}
					if (cost <= 0) {
						const prev = captureAnimRects();
						const payload = {
							levelUpRest: ui.levelUpRest,
							levelUpDiscardInstanceIds: ui.levelUpDiscardIds,
							levelUpStones: ui.levelUpStones,
							deployInstanceId: String(sel.instanceId),
							payCostStones: 0,
							payCostCardInstanceIds: []
						};
						return commitAction(payload)
							.then(function (next) {
								ui.selectedInstanceId = null;
								ui.levelUpRest = 0;
								ui.levelUpStones = 0;
								ui.levelUpDiscardIds = [];
								ui.warnLevelUpRest = null;
								ui.warnLevelUpStone = null;
								ui._luPrevPowerInstanceId = null;
								ui._luPrevPower = null;
								ui.pay = { stones: 0, cardInstanceIds: [] };
								render(next);
								requestAnimationFrame(function () {
									playFLIP(prev);
								});
							})
							.catch(function (err) {
								// eslint-disable-next-line no-console
								console.error(err);
								alert('操作に失敗しました（' + (err && err.message ? err.message : String(err)) + '）');
								rerenderWithFreshState();
							});
					}
					showPayModal(st);
				}

				if ((ui.levelUpRest | 0) > 0) {
					showLevelUpDiscardConfirmModal(st, proceedAfterDiscardConfirm);
					return;
				}

				proceedAfterDiscardConfirm();
			})
			.catch(function (err) {
				// eslint-disable-next-line no-console
				console.error(err);
				alert('状態の取得に失敗しました（' + (err && err.message ? err.message : String(err)) + '）');
			});
	}

	function buildHumanControlOverlayCluster(st) {
		if (!st.humansTurn || st.gameOver) return null;
		const sel = selectedCard(st);
		const selName = sel ? (st.defs[sel.cardId]?.name || '—') : '（未選択）';
		const selDef = sel ? resolveCardDef(st.defs, sel.cardId) : null;

		const panel = el('section', 'panel battle-control battle-control--levelup');
		if (selDef && selDef.fieldCard) {
			panel.classList.add('battle-control--field-deploy');
			panel.appendChild(el('h2', '', 'フィールドを配置'));
		} else {
			panel.appendChild(el('h2', '', 'レベルアップ'));
		}

		const body = el('div', 'battle-control--levelup__body');
		const controlsCol = el('div', 'battle-control--levelup__controls');
		controlsCol.appendChild(el('p', 'muted', '配置するカード: ' + selName));

		if (selDef && selDef.fieldCard) {
			controlsCol.appendChild(
				el(
					'p',
					'muted battle-control--field-deploy__hint',
					'フィールドはレベルアップしません。コスト（左上の数字）分のストーンだけを支払います。ターンは終わらず、あとからファイターを配置してください。'
				)
			);
		}

		const row = el('div', 'battle-control__row');

		const restBox = el('div', 'battle-control__box');
		restBox.appendChild(el('div', 'battle-control__label', 'カードを捨ててレベルアップ'));
		restBox.appendChild(buildLevelUpValueEl(String(ui.levelUpRest), 'rest'));
		const restBtns = el('div', 'battle-control__btns');
		const restMinus = el('button', 'btn btn--ghost', '−');
		restMinus.type = 'button';
		restMinus.dataset.action = 'rest_minus';
		const restPlus = el('button', 'btn btn--ghost', '+');
		restPlus.type = 'button';
		restPlus.dataset.action = 'rest_plus';
		restBtns.appendChild(restMinus);
		restBtns.appendChild(restPlus);
		restBox.appendChild(restBtns);
		row.appendChild(restBox);

		const stoneBox = el('div', 'battle-control__box');
		stoneBox.appendChild(el('div', 'battle-control__label', 'ストーンを使ってレベルアップ'));
		stoneBox.appendChild(buildLevelUpValueEl(String(ui.levelUpStones), 'stone'));
		const stoneBtns = el('div', 'battle-control__btns');
		const stoneMinus = el('button', 'btn btn--ghost', '−');
		stoneMinus.type = 'button';
		stoneMinus.dataset.action = 'stone_minus';
		const stonePlus = el('button', 'btn btn--ghost', '+');
		stonePlus.type = 'button';
		stonePlus.dataset.action = 'stone_plus';
		stoneBtns.appendChild(stoneMinus);
		stoneBtns.appendChild(stonePlus);
		stoneBox.appendChild(stoneBtns);
		row.appendChild(stoneBox);

		if (!(selDef && selDef.fieldCard)) {
			controlsCol.appendChild(row);
		}
		if (ui.warnLevelUpRest) {
			const wr = el('p', 'battle-control--levelup__warn', ui.warnLevelUpRest);
			wr.setAttribute('role', 'alert');
			controlsCol.appendChild(wr);
		}
		if (ui.warnLevelUpStone) {
			const ws = el('p', 'battle-control--levelup__warn', ui.warnLevelUpStone);
			ws.setAttribute('role', 'alert');
			controlsCol.appendChild(ws);
		}
		body.appendChild(controlsCol);

		const previewCol = el('div', 'battle-control--levelup__preview');
		if (selDef) {
			if (selDef.fieldCard) {
				const previewWrap = el('div', 'library-card battle-control--levelup__preview-card');
				const shell = buildBattleCardFaceShell(selDef, 'hand');
				previewWrap.appendChild(shell);
				applyBattleCardTipData(previewWrap, selDef);
				previewCol.appendChild(previewWrap);
			} else {
			const deployB = computeDeployBonus(selDef, ui.levelUpRest, ui.levelUpStones)
				+ computeHumanNextDeployBonusForDef(st, selDef, sel);
			const previewPow = previewHumanBattlePower(st, st.defs, selDef, deployB, sel);
			const basePow = Number(selDef.basePower != null ? selDef.basePower : 0);
			const instId = sel.instanceId;
			let shouldSparkPower = false;
			if (
				ui._luPrevPowerInstanceId != null &&
				ui._luPrevPowerInstanceId === instId &&
				previewPow > ui._luPrevPower
			) {
				shouldSparkPower = true;
			}
			ui._luPrevPowerInstanceId = instId;
			ui._luPrevPower = previewPow;

			const previewWrap = el('div', 'library-card battle-control--levelup__preview-card');
			const faceDef = cardDefForBattleFace(selDef, sel, st.defs);
			const shell = buildBattleCardFaceShell(faceDef, 'hand');
			applyLevelUpPreviewPower(shell, basePow, previewPow);
			applyCurrentCostDisplay(shell, defWithBlankEffectsIfNeeded(selDef, sel), st, sel);
			const powEl = shell.querySelector('.card-face__power');
			const sparkHost = wrapLevelUpPreviewPowerSparkHost(powEl);
			if (shouldSparkPower && sparkHost) {
				appendLevelUpValueSparkBurst(sparkHost);
			}
			previewWrap.appendChild(shell);
			applyBattleCardTipData(previewWrap, faceDef);
			previewCol.appendChild(previewWrap);
			}
		} else {
			previewCol.appendChild(el('p', 'muted battle-control--levelup__preview-empty', '—'));
		}
		body.appendChild(previewCol);

		panel.appendChild(body);

		const cancel = el('button', 'btn btn--ghost battle-control__cancel-external', 'キャンセル');
		cancel.type = 'button';
		cancel.addEventListener(
			'click',
			function (ev) {
				ev.preventDefault();
				ev.stopPropagation();
				cancelLevelUpInProgress();
				rerenderWithFreshState();
			},
			true
		);

		const decide = el('button', 'btn btn--primary battle-control__decide-external', '決定');
		decide.type = 'button';
		decide.addEventListener(
			'click',
			function (ev) {
				ev.preventDefault();
				ev.stopPropagation();
				runLevelUpDecideCommitFlow();
			},
			true
		);

		const cluster = el('div', 'battle-control-overlay__cluster');
		const footer = el('div', 'battle-control__footer');
		footer.appendChild(cancel);
		footer.appendChild(decide);
		panel.appendChild(footer);
		cluster.appendChild(panel);

		return cluster;
	}

	async function sendChoice(payload) {
		const headers = { 'Accept': 'application/json', 'Content-Type': 'application/json' };
		if (csrfToken) headers[csrfHeader] = csrfToken;
		const url = battleIsPvp ? (battleApiBase + '/choice') : (contextPath + '/battle/cpu/choice');
		const res = await fetch(url, { method: 'POST', headers, body: JSON.stringify(payload) });
		if (!res.ok) throw new Error('choice failed: ' + res.status);
		return await res.json();
	}

	/** render のたびに重ねてしまうと、OK/決定が手前の1枚だけ閉じて下に残るため同一内容なら作り直さない */
	function pendingChoiceModalKey(pc) {
		if (!pc || typeof pc !== 'object') return '';
		const ids = (pc.optionInstanceIds || []).slice().sort().join(',');
		return [
			String(pc.kind || ''),
			String(pc.prompt || ''),
			String(pc.stoneCost != null ? pc.stoneCost : ''),
			String(pc.abilityDeployCode || ''),
			pc.cpuSlotChooses ? '1' : '0',
			String(pc.viewerMayRespond !== undefined && pc.viewerMayRespond !== null ? pc.viewerMayRespond : ''),
			ids
		].join('\x1f');
	}

	function showChoiceModal(st) {
		const pc = st.pendingChoice;
		if (!pc) return;
		/*
		 * viewerMayRespond / forHuman が JSON で欠落すると undefined になり、
		 * 「選択してください」だけが出てモーダルが開かないことがある（忍者入れ替え〈配置〉確認など）。
		 * cpuSlotChooses が明示されていればそれを優先する。
		 */
		const guestChooses = !!pc.cpuSlotChooses;
		let may = false;
		if (pc.viewerMayRespond !== undefined && pc.viewerMayRespond !== null) {
			may = !!pc.viewerMayRespond;
		} else if (pc.forHuman !== undefined && pc.forHuman !== null) {
			may = !!pc.forHuman && !guestChooses;
		} else {
			may = !guestChooses;
		}
		if (!may) return;

		/* 「この手番では勝てません」(z-index 110) の上に選択 UI を出す前提で、残っていれば掃除 */
		hideUnwinnableDeployPop();

		const choiceKey = pendingChoiceModalKey(pc);
		const existingChoice = document.getElementById('battle-pending-choice-modal');
		if (existingChoice && existingChoice.dataset.pendingKey === choiceKey) {
			return;
		}
		if (existingChoice) existingChoice.remove();

		hideBattleCardTooltip();
		hideBattleDeckTooltip();

		const overlay = el('div', 'battle-pay-modal');
		overlay.id = 'battle-pending-choice-modal';
		overlay.dataset.pendingKey = choiceKey;
		overlay.setAttribute('role', 'dialog');
		overlay.setAttribute('aria-modal', 'true');

		const panel = el('div', 'battle-pay-modal__panel');
		const closeBtn = el('button', 'battle-pay-modal__close', '×');
		closeBtn.type = 'button';
		panel.appendChild(closeBtn);
		panel.appendChild(el('h2', 'battle-pay-modal__title', pc.prompt || '選択'));

		const picked = [];

		if (pc.kind === 'CONFIRM_OPTIONAL_STONE') {
			panel.appendChild(el('p', 'muted', 'ストーンを' + String(pc.stoneCost || 0) + 'つ使用しますか？'));

			// Show ability details for the card that triggered this choice (best-effort via abilityDeployCode).
			const detailDef = resolveDefByAbilityDeployCode(st.defs, pc.abilityDeployCode, pc.prompt);
			if (detailDef) {
				const detail = el('div', 'muted', null);
				detail.style.whiteSpace = 'pre-wrap';
				detail.style.wordBreak = 'break-word';
				detail.style.overflowWrap = 'anywhere';
				detail.style.lineHeight = '1.55';
				detail.style.marginTop = '0.55rem';
				detail.style.padding = '0.55rem 0.65rem';
				detail.style.borderRadius = '10px';
				detail.style.border = '1px solid rgba(255, 255, 255, 0.12)';
				detail.style.background = 'rgba(0, 0, 0, 0.18)';
				const raw = battleCardAbilityTooltipText(detailDef);
				const lines = String(raw || '').split('\n');
				const first = lines.length ? String(lines[0]).trim() : '';
				detail.textContent =
					first === '〈配置〉' || first === '〈常時〉' || first === '〈フィールド〉'
						? lines.slice(1).join('\n')
						: String(raw || '—');
				panel.appendChild(detail);
			}

			const actions = el('div', 'battle-pay-modal__actions');
			const noBtn = el('button', 'btn btn--ghost', 'しない');
			noBtn.type = 'button';
			const yesBtn = el('button', 'btn btn--primary', '使用する');
			yesBtn.type = 'button';
			actions.appendChild(noBtn);
			actions.appendChild(yesBtn);
			panel.appendChild(actions);

			function teardown() {
				hideBattleCardTooltip();
				overlay.remove();
			}
			/* 「使用する」「しない」でのみ閉じる（×・背景クリックでは閉じない） */
			closeBtn.hidden = true;

			noBtn.addEventListener('click', function () {
				const prev = captureAnimRects();
				teardown();
				sendChoice({ confirm: false, pickedInstanceIds: [] }).then(function (next) {
					render(next);
					requestAnimationFrame(function () { playFLIP(prev); });
					// choice 後は効果処理を進める
					return resolvePending();
				}).then(function (next2) {
					if (next2) render(next2);
				}).catch(function () { rerenderWithFreshState(); });
			});
			yesBtn.addEventListener('click', function () {
				const prev = captureAnimRects();
				teardown();
				sendChoice({ confirm: true, pickedInstanceIds: [] }).then(function (next) {
					render(next);
					requestAnimationFrame(function () { playFLIP(prev); });
					return resolvePending();
				}).then(function (next2) {
					if (next2) render(next2);
				}).catch(function () { rerenderWithFreshState(); });
			});
		} else if (pc.kind === 'CONFIRM_MIRAJUKUL_MIRROR') {
			panel.appendChild(
				el('p', 'muted', 'コピーした効果を使う場合、相手カードと同じ手順（ストーンを使用しますか？など）で解決されます。')
			);
			const detailDefMj = resolveDefByAbilityDeployCode(st.defs, pc.abilityDeployCode, pc.prompt);
			if (detailDefMj) {
				const detailMj = el('div', 'muted', null);
				detailMj.style.whiteSpace = 'pre-wrap';
				detailMj.style.wordBreak = 'break-word';
				detailMj.style.overflowWrap = 'anywhere';
				detailMj.style.lineHeight = '1.55';
				detailMj.style.marginTop = '0.55rem';
				detailMj.style.padding = '0.55rem 0.65rem';
				detailMj.style.borderRadius = '10px';
				detailMj.style.border = '1px solid rgba(255, 255, 255, 0.12)';
				detailMj.style.background = 'rgba(0, 0, 0, 0.18)';
				const rawMj = battleCardAbilityTooltipText(detailDefMj);
				const linesMj = String(rawMj || '').split('\n');
				const firstMj = linesMj.length ? String(linesMj[0]).trim() : '';
				detailMj.textContent =
					firstMj === '〈配置〉' || firstMj === '〈常時〉' || firstMj === '〈フィールド〉'
						? linesMj.slice(1).join('\n')
						: String(rawMj || '—');
				panel.appendChild(detailMj);
			}
			const actionsMj = el('div', 'battle-pay-modal__actions');
			const noBtnMj = el('button', 'btn btn--ghost', '使わない');
			noBtnMj.type = 'button';
			const yesBtnMj = el('button', 'btn btn--primary', '使う');
			yesBtnMj.type = 'button';
			actionsMj.appendChild(noBtnMj);
			actionsMj.appendChild(yesBtnMj);
			panel.appendChild(actionsMj);
			function teardownMj() {
				hideBattleCardTooltip();
				overlay.remove();
			}
			closeBtn.hidden = true;
			noBtnMj.addEventListener('click', function () {
				const prev = captureAnimRects();
				teardownMj();
				sendChoice({ confirm: false, pickedInstanceIds: [] }).then(function (next) {
					render(next);
					requestAnimationFrame(function () { playFLIP(prev); });
					return resolvePending();
				}).then(function (next2) {
					if (next2) render(next2);
				}).catch(function () { rerenderWithFreshState(); });
			});
			yesBtnMj.addEventListener('click', function () {
				const prev = captureAnimRects();
				teardownMj();
				sendChoice({ confirm: true, pickedInstanceIds: [] }).then(function (next) {
					render(next);
					requestAnimationFrame(function () { playFLIP(prev); });
					return resolvePending();
				}).then(function (next2) {
					if (next2) render(next2);
				}).catch(function () { rerenderWithFreshState(); });
			});
		} else if (pc.kind === 'CONFIRM_NINJA_SWAPPED_DEPLOY') {
			panel.appendChild(
				el('p', 'muted', '入れ替えたカードの〈配置〉効果を発動するか選べます。')
			);
			const detailDefNj = resolveDefByAbilityDeployCode(st.defs, pc.abilityDeployCode, pc.prompt);
			if (detailDefNj) {
				const detailNj = el('div', 'muted', null);
				detailNj.style.whiteSpace = 'pre-wrap';
				detailNj.style.wordBreak = 'break-word';
				detailNj.style.overflowWrap = 'anywhere';
				detailNj.style.lineHeight = '1.55';
				detailNj.style.marginTop = '0.55rem';
				detailNj.style.padding = '0.55rem 0.65rem';
				detailNj.style.borderRadius = '10px';
				detailNj.style.border = '1px solid rgba(255, 255, 255, 0.12)';
				detailNj.style.background = 'rgba(0, 0, 0, 0.18)';
				const rawNj = battleCardAbilityTooltipText(detailDefNj);
				const linesNj = String(rawNj || '').split('\n');
				const firstNj = linesNj.length ? String(linesNj[0]).trim() : '';
				detailNj.textContent =
					firstNj === '〈配置〉' || firstNj === '〈常時〉' || firstNj === '〈フィールド〉'
						? linesNj.slice(1).join('\n')
						: String(rawNj || '—');
				panel.appendChild(detailNj);
			}
			const actionsNj2 = el('div', 'battle-pay-modal__actions');
			const noBtnNj2 = el('button', 'btn btn--ghost', '使わない');
			noBtnNj2.type = 'button';
			const yesBtnNj2 = el('button', 'btn btn--primary', '使う');
			yesBtnNj2.type = 'button';
			actionsNj2.appendChild(noBtnNj2);
			actionsNj2.appendChild(yesBtnNj2);
			panel.appendChild(actionsNj2);
			function teardownNj2() {
				hideBattleCardTooltip();
				overlay.remove();
			}
			closeBtn.hidden = true;
			noBtnNj2.addEventListener('click', function () {
				const prev = captureAnimRects();
				teardownNj2();
				sendChoice({ confirm: false, pickedInstanceIds: [] }).then(function (next) {
					render(next);
					requestAnimationFrame(function () { playFLIP(prev); });
					return resolvePending();
				}).then(function (next2) {
					if (next2) render(next2);
				}).catch(function () { rerenderWithFreshState(); });
			});
			yesBtnNj2.addEventListener('click', function () {
				const prev = captureAnimRects();
				teardownNj2();
				sendChoice({ confirm: true, pickedInstanceIds: [] }).then(function (next) {
					render(next);
					requestAnimationFrame(function () { playFLIP(prev); });
					return resolvePending();
				}).then(function (next2) {
					if (next2) render(next2);
				}).catch(function () { rerenderWithFreshState(); });
			});
		} else if (pc.kind === 'CONFIRM_ACCEPT_LOSS') {
			panel.appendChild(el('p', 'muted', pc.prompt || 'このまま進めますか？'));
			const actions = el('div', 'battle-pay-modal__actions');
			const cancelBtn = el('button', 'btn btn--ghost', 'キャンセル');
			cancelBtn.type = 'button';
			const okBtn = el('button', 'btn btn--primary', 'はい');
			okBtn.type = 'button';
			actions.appendChild(cancelBtn);
			actions.appendChild(okBtn);
			panel.appendChild(actions);

			function teardown() {
				hideBattleCardTooltip();
				overlay.remove();
			}
			/* 「キャンセル」「はい」のみ（×・背景クリックでは閉じない） */
			closeBtn.hidden = true;

			cancelBtn.addEventListener('click', function () {
				const prev = captureAnimRects();
				teardown();
				sendChoice({ confirm: false, pickedInstanceIds: [] }).then(function (next) {
					render(next);
					requestAnimationFrame(function () { playFLIP(prev); });
				}).catch(function () { rerenderWithFreshState(); });
			});
			okBtn.addEventListener('click', function () {
				const prev = captureAnimRects();
				teardown();
				sendChoice({ confirm: true, pickedInstanceIds: [] }).then(function (next) {
					render(next);
					requestAnimationFrame(function () { playFLIP(prev); });
				}).catch(function () { rerenderWithFreshState(); });
			});
		} else {
			/*
			 * カード選択系（フェザリアのレストから手札へ等）は、キャンセル・×・背景で閉じると
			 * sendChoice されずサーバが HUMAN_CHOICE のまま残り操作不能になる。決定までモーダルを固定する。
			 */
			closeBtn.hidden = true;

			const grid = el('div', 'battle-pay-modal__cardgrid');
			const ids = pc.optionInstanceIds || [];
			const pickHand = pc.cpuSlotChooses ? (st.cpuHand || []) : (st.humanHand || []);
			const pickRest = pc.cpuSlotChooses ? (st.cpuRest || []) : (st.humanRest || []);
			ids.forEach(function (inst) {
				let card = null;
				pickHand.forEach(function (c) { if (c.instanceId === inst) card = c; });
				pickRest.forEach(function (c) { if (c.instanceId === inst) card = c; });
				if (!card) return;
				const d = cardDefForBattleFace(resolveCardDef(st.defs, card.cardId), card, st.defs);
				const btn = el('button', 'battle-pay-modal__card', null);
				btn.type = 'button';
				btn.dataset.instanceId = inst;
				if (d) {
					btn.appendChild(buildBattleCardFaceShell(d, 'modal'));
					applyBattleCardTipData(btn, d);
					wireBattleCardTooltipHost(btn);
				}
				grid.appendChild(btn);
			});
			panel.appendChild(grid);

			const actions = el('div', 'battle-pay-modal__actions');
			const ok = el('button', 'btn btn--primary', '決定');
			ok.type = 'button';
			ok.disabled = true;
			actions.appendChild(ok);
			panel.appendChild(actions);

			function needCount() {
				if (pc.kind === 'SELECT_SWAP_REST_AND_HAND') return 2;
				if (pc.kind === 'SELECT_TWO_FROM_HAND_TO_REST') return 2;
				if (pc.kind === 'SELECT_UP_TO_TWO_FROM_REST_TO_HAND') return 2;
				return 1;
			}
			function refresh() {
				if (pc.kind === 'SELECT_UP_TO_TWO_FROM_REST_TO_HAND') {
					ok.disabled = picked.length > 2;
					return;
				}
				ok.disabled = picked.length !== needCount();
			}

			grid.addEventListener('click', function (e) {
				const t = eventTargetElement(e);
				if (!t) return;
				const btn = t.closest('.battle-pay-modal__card');
				if (!btn) return;
				const inst = btn.getAttribute('data-instance-id');
				if (!inst) return;
				const idx = picked.indexOf(inst);
				if (idx >= 0) {
					picked.splice(idx, 1);
					btn.classList.remove('is-selected');
				} else {
					picked.push(inst);
					btn.classList.add('is-selected');
					if (picked.length > needCount()) {
						// keep last N
						const drop = picked.shift();
						const dropBtn = grid.querySelector('[data-instance-id="' + drop + '"]');
						if (dropBtn) dropBtn.classList.remove('is-selected');
					}
				}
				refresh();
			});

			function teardown() {
				hideBattleCardTooltip();
				overlay.remove();
			}

			ok.addEventListener('click', function () {
				const prev = captureAnimRects();
				teardown();
				sendChoice({ confirm: true, pickedInstanceIds: picked.slice() }).then(function (next) {
					render(next);
					requestAnimationFrame(function () { playFLIP(prev); });
					return resolvePending();
				}).then(function (next2) {
					if (next2) render(next2);
				}).catch(function () { rerenderWithFreshState(); });
			});

			refresh();
		}

		overlay.appendChild(panel);
		document.body.appendChild(overlay);
	}

	function showBattleZoneDetailModal(def, powerContributors, battleState, battleCard) {
		if (!def) return;
		const contributors = Array.isArray(powerContributors) ? powerContributors : [];
		const stCost = battleState != null ? battleState : lastStateForHandPower;
		hideBattleCardTooltip();
		hideBattleDeckTooltip();

		const previouslyFocused = document.activeElement instanceof HTMLElement ? document.activeElement : null;

		const overlay = el('div', 'battle-zone-detail-modal');
		overlay.setAttribute('role', 'dialog');
		overlay.setAttribute('aria-modal', 'true');
		overlay.setAttribute('aria-label', 'カード詳細');

		const panel = el('div', 'battle-zone-detail-modal__panel');
		const closeBtn = el('button', 'battle-zone-detail-modal__close', '×');
		closeBtn.type = 'button';
		closeBtn.setAttribute('aria-label', '閉じる');
		panel.appendChild(closeBtn);

		const grid = el('div', 'battle-zone-detail-modal__grid');
		const left = el('div', 'battle-zone-detail-modal__left');
		const right = el('div', 'battle-zone-detail-modal__right');

		const face = buildBattleCardFaceShell(def, 'zone');
		face.classList.add('battle-zone-detail-modal__card');
		left.appendChild(face);
		if (stCost && battleCard && !def.fieldCard) {
			applyCurrentCostDisplay(face, def, stCost, battleCard);
		}

		right.appendChild(el('h3', 'battle-zone-detail-modal__name', def.name || '—'));

		const stats = el('dl', 'battle-zone-detail-modal__stats');
		function statRow(label, value) {
			const wrap = el('div', 'battle-zone-detail-modal__stat');
			wrap.appendChild(el('dt', '', label));
			const dd = el('dd', '', value);
			if (label === '収録パック') dd.classList.add('battle-zone-detail-modal__pack');
			wrap.appendChild(dd);
			return wrap;
		}
		stats.appendChild(statRow('種族', formatBattleCardAttr(def)));
		let costDisplay = String(def.cost != null ? def.cost : '—');
		if (stCost && battleCard && !def.fieldCard) {
			costDisplay = String(Math.max(0, Math.floor(Number(effectiveFighterDeployCostFromState(def, stCost, battleCard)))));
		}
		stats.appendChild(statRow('コスト', costDisplay));
		stats.appendChild(
			statRow('強さ', def.fieldCard ? '—' : String(def.basePower != null ? def.basePower : '—'))
		);
		stats.appendChild(statRow('★', String(def.rarity != null ? def.rarity : '—')));
		stats.appendChild(statRow('収録パック', packSourcesForInitial(def.packInitial).join('\n')));
		right.appendChild(stats);

		right.appendChild(el('p', 'battle-zone-detail-modal__label', '効果'));
		const ability = el('div', 'battle-zone-detail-modal__ability');
		ability.textContent = '';
		let raw = battleCardAbilityTooltipText(def);
		if (def.fieldCard && stCost) {
			let rr = 0;
			if (Number(def.id) === PREVIEW_CARD_IDS.FIELD_SCRAPYARD && stCost.scrapyardFieldTurnsRemaining != null) {
				rr = Math.max(0, Math.floor(Number(stCost.scrapyardFieldTurnsRemaining)));
			} else if (Number(def.id) === PREVIEW_CARD_IDS.FIELD_DEATHBOUNCE && stCost.deathbounceFieldTurnsRemaining != null) {
				rr = Math.max(0, Math.floor(Number(stCost.deathbounceFieldTurnsRemaining)));
			}
			if (rr > 0) {
				raw = adjustScrapyardFieldAbilityTextForRemaining(raw, rr);
			}
		}
		const lines = String(raw || '').split('\n');
		const first = lines.length ? String(lines[0]).trim() : '';
		ability.textContent =
			first === '〈配置〉' || first === '〈常時〉' || first === '〈フィールド〉'
				? lines.slice(1).join('\n')
				: String(raw || '—');
		right.appendChild(ability);

		if (contributors.length) {
			right.appendChild(el('p', 'battle-zone-detail-modal__label', '強さの変動要因'));
			const srcWrap = el('div', 'battle-zone-detail-modal__power-sources');
			const defs = lastDefsForTooltip;
			contributors.forEach(function (mod) {
				const line = el('div', 'battle-zone-detail-modal__power-line');
				const id = mod && mod.sourceCardId != null ? mod.sourceCardId : mod && mod.sourceCardId === 0 ? 0 : null;
				const srcDef = id != null && defs ? resolveCardDef(defs, id) : null;
				const text = formatBattlePowerContributorText(defs, mod);
				if (srcDef) {
					const s = el('span', 'battle-zone-detail-modal__source-hover', text);
					wireBattlePowerContributorHover(s, srcDef);
					line.appendChild(s);
				} else {
					line.appendChild(el('span', 'muted', text));
				}
				srcWrap.appendChild(line);
			});
			right.appendChild(srcWrap);
		}

		grid.appendChild(left);
		grid.appendChild(right);
		panel.appendChild(grid);
		overlay.appendChild(panel);
		document.body.appendChild(overlay);

		function teardown() {
			hideBattlePowerContributorPopup();
			overlay.remove();
			document.removeEventListener('keydown', onKey);
			if (previouslyFocused) previouslyFocused.focus();
		}
		function onKey(e) {
			if (e.key === 'Escape') {
				e.preventDefault();
				teardown();
			}
		}

		closeBtn.addEventListener('click', teardown);
		overlay.addEventListener('click', function (e) {
			if (e.target === overlay) teardown();
		});
		document.addEventListener('keydown', onKey);
		closeBtn.focus();
	}

	/** 効果待ちポップアップ（body 直下）と resolve タイマーを確実に片付ける */
	function removeBattleEffectPopup() {
		if (ui._resolveTimer != null) {
			clearTimeout(ui._resolveTimer);
			ui._resolveTimer = null;
		}
		ui._resolveEffectWaitKey = null;
		if (ui._effectPopupEl && ui._effectPopupEl.parentNode) {
			ui._effectPopupEl.remove();
		}
		ui._effectPopupEl = null;
		const stray = document.getElementById('battle-pending-effect-popup');
		if (stray && stray.parentNode) {
			stray.remove();
		}
	}

	function render(st) {
		lastStateForHandPower = st;
		if (st.defs && Object.keys(st.defs).length > 0) {
			lastDefsForTooltip = st.defs;
		} else {
			st.defs = lastDefsForTooltip || {};
		}
		app.innerHTML = '';
		hideBattleCardTooltip();
		hideBattleDeckTooltip();

		// Top "thinking" banner (fixed-ish inside app)
		if (st.phase === 'CPU_THINKING' || st.phase === 'OPPONENT_TURN') {
			const b = el('div', 'panel', null);
			b.style.position = 'sticky';
			b.style.top = '0';
			b.style.zIndex = '20';
			b.style.marginBottom = '10px';
			b.textContent = st.phase === 'OPPONENT_TURN' ? '相手の操作中…' : '考え中...';
			app.appendChild(b);
		}

		app.appendChild(el('p', 'battle-msg', st.lastMessage || '—'));

		const oppTop = el('section', 'battle-row battle-row--opp battle-band battle-band--opp');
		{
			const inner = el('div', 'battle-band__inner');

			const cellDeck = el('div', 'battle-cell battle-cell--compact battle-cell--opp-deck');
			cellDeck.appendChild(renderDeckStackVisual(st.cpuDeck.length, '相手デッキ'));
			inner.appendChild(cellDeck);

			const cellHand = el('div', 'battle-cell battle-cell--opp-hand');
			cellHand.setAttribute('aria-label', '相手の手札');
			const oppHandRow = el('div', 'battle-opp-hand-row');
			oppHandRow.appendChild(renderHandCards(st.cpuHand, st.defs, { faceDown: true, compactOpp: true, nextDeployBonus: 0, nextElfOnlyBonus: 0, nextDeployCostBonusTimes: 0 }));
			const oppStonesInline = el('div', 'battle-opp-hand-row__stones');
			oppStonesInline.setAttribute('aria-label', '相手ストーン所持数 ' + String(st.cpuStones));
			oppStonesInline.appendChild(el('span', 'battle-opp-hand-row__stones-label', 'ストーン'));
			oppStonesInline.appendChild(el('span', 'battle-opp-hand-row__stones-value', String(st.cpuStones)));
			oppHandRow.appendChild(oppStonesInline);
			cellHand.appendChild(oppHandRow);
			inner.appendChild(cellHand);

			const cellRest = el('div', 'battle-cell battle-cell--compact battle-cell--opp-rest');
			cellRest.appendChild(el('h3', '', 'レスト'));
			cellRest.appendChild(
				renderRestStackVisual(st.cpuRest, st.defs, 'レスト', { maxVisual: 4, stackOffsetPx: 2, battleState: st })
			);
			inner.appendChild(cellRest);

			oppTop.appendChild(inner);
		}
		app.appendChild(oppTop);

		const zonesRow = el('section', 'battle-row battle-row--zones-split');
		{
			const zonesWrap = el('div', 'battle-zones-wrap');
			const zonesStack = el('div', 'battle-zones-stack');
			const cellZoneOpp = el('div', 'battle-cell battle-cell--zone battle-cell--zone-cpu');
			cellZoneOpp.setAttribute('aria-label', '相手のバトルゾーン');
			const rowOpp = el('div', 'battle-zone-with-field');
			// 〈フィールド〉は共有だが、常に自分側のバトル列に表示する（相手ターンで上段に出さない）
			rowOpp.appendChild(renderZone(st.cpuBattle, st.defs, st.cpuBattlePower, { opponentZone: true, battleState: st }));
			cellZoneOpp.appendChild(rowOpp);
			zonesStack.appendChild(cellZoneOpp);

			const cellZoneYou = el('div', 'battle-cell battle-cell--zone battle-cell--zone-human');
			cellZoneYou.setAttribute('aria-label', '自分のバトルゾーン');
			const rowYou = el('div', 'battle-zone-with-field');
			if (st.activeField) {
				/* フィールド枠は absolute のため親の高さに寄与しない。min-height は CSS（--has-field）で確保 */
				rowYou.classList.add('battle-zone-with-field--has-field');
				rowYou.appendChild(renderBattleFieldSlot(st.activeField, st.defs, { opponentZone: false, battleState: st }));
			}
			rowYou.appendChild(renderZone(st.humanBattle, st.defs, st.humanBattlePower, { battleState: st }));
			cellZoneYou.appendChild(rowYou);
			zonesStack.appendChild(cellZoneYou);

			zonesWrap.appendChild(zonesStack);

			const controlCluster = buildHumanControlOverlayCluster(st);
			if (controlCluster && selectedCard(st) && st.phase !== 'HUMAN_CHOICE') {
				// Level-up is a popup overlay (do not move center zone cards).
				const overlay = el('div', 'battle-control-overlay battle-control-overlay--levelup-popup');
				overlay.setAttribute('role', 'region');
				overlay.setAttribute('aria-label', 'レベルアップ');
				overlay.appendChild(controlCluster);
				zonesWrap.appendChild(overlay);
			}

			zonesRow.appendChild(zonesWrap);
		}
		app.appendChild(zonesRow);

		const you = el('section', 'battle-row battle-row--you battle-band battle-band--you');
		{
			const inner = el('div', 'battle-band__inner');
			const cellRest = el('div', 'battle-cell battle-cell--compact battle-cell--you-rest');
			cellRest.appendChild(el('h3', '', 'レスト'));
			cellRest.appendChild(
				renderRestStackVisual(st.humanRest, st.defs, 'レスト', { maxVisual: 5, stackOffsetPx: 3, battleState: st })
			);
			inner.appendChild(cellRest);

			const cellHand = el('div', 'battle-cell battle-cell--you-hand');
			cellHand.setAttribute('aria-label', '自分の手札');
			const stonesTop = el('div', 'battle-you-hand-stones');
			const displayHumanStones = humanStonesPreviewReserveAdjusted(st);
			stonesTop.setAttribute('aria-label', 'ストーン所持数 ' + String(displayHumanStones));
			stonesTop.appendChild(el('span', 'battle-you-hand-stones__label', 'ストーン'));
			stonesTop.appendChild(el('span', 'battle-you-hand-stones__value', String(displayHumanStones)));
			cellHand.appendChild(stonesTop);
			cellHand.appendChild(renderHandCards(st.humanHand, st.defs, {
				faceDown: false,
				selectable: st.humansTurn && !st.gameOver,
				nextDeployBonus: st.humanNextDeployBonus || 0,
				nextElfOnlyBonus: st.humanNextElfOnlyBonus || 0,
				nextDeployCostBonusTimes: st.humanNextDeployCostBonusTimes || 0,
				nextMechanicStacks: st.humanNextMechanicStacks || 0,
				battleState: st
			}));
			inner.appendChild(cellHand);

			const cellDeck = el('div', 'battle-cell battle-cell--compact battle-cell--you-deck');
			cellDeck.appendChild(renderDeckStackVisual(st.humanDeck.length, '自分デッキ', { stackOffsetPx: 4 }));
			inner.appendChild(cellDeck);

			you.appendChild(inner);
		}
		app.appendChild(you);

		updateBattleFieldBackdrop(st.activeField, st.defs);

		lastEventLog = st.eventLog && st.eventLog.length ? st.eventLog.slice() : [];
		if (battleLogModal && !battleLogModal.hidden) {
			fillBattleLogList(lastEventLog);
		}

		wireBattleCardTooltips(app);

		installOrUpdateTurnTimer(st);

		// game over modal (only once per battle end)
		maybeShowGameOverModal(st);

		// CPU think delay（設定の「CPUが考えるはやさ」／対人戦では使わない）
		if (st.phase === 'CPU_THINKING' && !st.gameOver && !st.pvpMatch) {
			if (ui._cpuThinkTimer == null) {
				const waitMs = cpuThinkWaitMs();
				ui._cpuThinkTimer = window.setTimeout(function () {
					ui._cpuThinkTimer = null;
					const prev = captureAnimRects();
					cpuStep().then(function (next) {
						render(next);
						requestAnimationFrame(function () { playFLIP(prev); });
					}).catch(function (e) {
						// eslint-disable-next-line no-console
						console.error(e);
						rerenderWithFreshState();
					});
				}, waitMs);
			}
		} else if (ui._cpuThinkTimer != null) {
			clearTimeout(ui._cpuThinkTimer);
			ui._cpuThinkTimer = null;
		}

		if (st.pvpMatch && st.phase === 'OPPONENT_TURN' && !st.gameOver) {
			if (ui._pvpPollTimer == null) {
				ui._pvpPollTimer = window.setInterval(function () {
					rerenderWithFreshState().catch(function (e) {
						// eslint-disable-next-line no-console
						console.error(e);
					});
				}, 2000);
			}
		} else if (ui._pvpPollTimer != null) {
			clearInterval(ui._pvpPollTimer);
			ui._pvpPollTimer = null;
		}

		// Show effect for 3 seconds, then resolve（忍者は入れ替えを即 resolve し、任意〈配置〉確認へ）
		if ((st.phase === 'HUMAN_EFFECT_PENDING' || st.phase === 'CPU_EFFECT_PENDING') && st.pendingEffect && !st.gameOver) {
			const pe = st.pendingEffect;
			const isNinjaDeploy = String(pe.abilityDeployCode || '') === 'NINJA';
			if (isNinjaDeploy) {
				if (!ui._ninjaAutoResolveInFlight) {
					if (ui._resolveTimer != null) {
						clearTimeout(ui._resolveTimer);
						ui._resolveTimer = null;
					}
					removeBattleEffectPopup();
					ui._resolveEffectWaitKey = null;
					ui._ninjaAutoResolveInFlight = true;
					const prevNinja = captureAnimRects();
					resolvePending()
						.then(function (next) {
							ui._ninjaAutoResolveInFlight = false;
							render(next);
							requestAnimationFrame(function () {
								playFLIP(prevNinja);
							});
						})
						.catch(function (e) {
							ui._ninjaAutoResolveInFlight = false;
							// eslint-disable-next-line no-console
							console.error(e);
							rerenderWithFreshState();
						});
				}
			} else {
			const effectWaitKey = String(st.phase || '') + '\x1f' + String(pe.mainInstanceId || '') + '\x1f' + String(pe.cardId != null ? pe.cardId : '');
			const samePendingEffectWait = ui._resolveEffectWaitKey === effectWaitKey && ui._resolveTimer != null;
			/* 同じ効果待ちの再描画でタイマーを毎回クリアすると 3 秒が永遠に進まず resolve が呼ばれない */
			if (samePendingEffectWait) {
				/* ポップアップは既存のまま（body 直下・app の再構築の影響を受けない） */
			} else {
				ui._resolveEffectWaitKey = effectWaitKey;
				if (ui._resolveTimer != null) {
					clearTimeout(ui._resolveTimer);
					ui._resolveTimer = null;
				}
				if (ui._effectPopupEl && ui._effectPopupEl.parentNode) {
					ui._effectPopupEl.remove();
				}
				ui._effectPopupEl = null;

			const def = resolveCardDef(st.defs, pe.cardId);
			const side = el('div', 'panel', null);
			side.id = 'battle-pending-effect-popup';
			// absolute: scroll with the page (do NOT follow viewport)
			side.style.position = 'absolute';
			side.style.left = '16px';
			side.style.top = '92px';
			side.style.width = '360px';
			side.style.maxWidth = '42vw';
			side.style.maxHeight = '72vh';
			side.style.overflow = 'hidden';
			side.style.boxSizing = 'border-box';
			side.style.zIndex = '50';

			const head = el('div', '', null);
			head.style.display = 'flex';
			head.style.alignItems = 'baseline';
			head.style.justifyContent = 'space-between';
			head.style.gap = '10px';
			const title = el('h3', '', '効果');
			title.style.margin = '0';
			const tag = el('span', 'deck-tooltip__ability-tag', battleAbilityHeadlineTagFromDef(def));
			tag.style.flex = '0 0 auto';
			head.appendChild(title);
			head.appendChild(tag);
			side.appendChild(head);

			const name = el('p', 'muted', def && def.name ? String(def.name) : '—');
			name.style.marginTop = '6px';
			name.style.marginBottom = '8px';
			side.appendChild(name);

			const body = el('div', 'muted', null);
			body.style.whiteSpace = 'pre-wrap';
			body.style.wordBreak = 'break-word';
			body.style.overflowWrap = 'anywhere';
			body.style.lineHeight = '1.5';
			body.style.maxHeight = '52vh';
			body.style.overflow = 'auto';
			body.style.paddingRight = '6px'; // scrollbar gutter-ish
			const raw = battleCardAbilityTooltipText(def);
			// 先頭行が「〈配置〉」等の場合は見出しと重複するので落とす
			const lines = String(raw || '').split('\n');
			const first = lines.length ? String(lines[0]).trim() : '';
			body.textContent =
				first === '〈配置〉' || first === '〈常時〉' || first === '〈フィールド〉'
					? lines.slice(1).join('\n')
					: String(raw || '—');
			side.appendChild(body);

			ui._effectPopupEl = side;
			document.body.appendChild(side);

			// Position the effect popup next to the triggering fighter card (prefer the main instance).
			const positionEffectPopupNearCard = function () {
				const pad = 12;
				const inst = pe && pe.mainInstanceId ? String(pe.mainInstanceId) : '';
				if (!inst) return;
				const key = 'card:' + inst;
				const anchor = app.querySelector('[data-anim-key="' + key + '"]');
				if (!(anchor instanceof Element)) return;
				const r = anchor.getBoundingClientRect();
				// place to the right of card; if not enough space, flip to left
				const sw = side.offsetWidth || 360;
				const sh = side.offsetHeight || 240;
				let left = r.right + pad;
				if (left + sw > window.innerWidth - pad) {
					left = r.left - sw - pad;
				}
				left = Math.max(pad, Math.min(left, window.innerWidth - sw - pad));
				let top = r.top;
				top = Math.max(pad, Math.min(top, window.innerHeight - sh - pad));
				// convert viewport coords → document coords
				side.style.left = (left + window.scrollX) + 'px';
				side.style.top = (top + window.scrollY) + 'px';
			};

			// Initial position + keep stuck to the card while scrolling.
			positionEffectPopupNearCard();
			// Do not follow scroll: keep the position fixed after initial placement.

			ui._resolveTimer = window.setTimeout(function () {
				ui._resolveTimer = null;
				if (ui._effectPopupEl && ui._effectPopupEl.parentNode) {
					ui._effectPopupEl.remove();
				}
				ui._effectPopupEl = null;
				const prev = captureAnimRects();
				resolvePending().then(function (next) {
					render(next);
					requestAnimationFrame(function () { playFLIP(prev); });
				}).catch(function (e) {
					// eslint-disable-next-line no-console
					console.error(e);
					rerenderWithFreshState();
				});
			}, 3000);
			}
			}
		} else {
			/* HUMAN_INPUT 等へ移ったあとにタイマーだけ残る／パネルだけ残るケースを掃除 */
			removeBattleEffectPopup();
			ui._ninjaAutoResolveInFlight = false;
		}

		if (st.phase === 'HUMAN_CHOICE' && st.pendingChoice && !st.gameOver) {
			showChoiceModal(st);
		}

		syncTurnChangeNotice(st);
		syncUnwinnableDeployNotice(st);
	}

	function installOrUpdateTurnTimer(st) {
		// 制限時間・時間切れ処理は無効化（タイマーUIも表示しない）
		if (ui._turnTimer != null) {
			clearInterval(ui._turnTimer);
			ui._turnTimer = null;
		}
		return;

		const startedAt = Number(st.turnStartedAtMs || 0);
		const limitSec = Number(st.activeTimeLimitSec || 0);
		const stage = Number(st.activePenaltyStage || 0);
		const phase = String(st.phase || '');
		const counts = (phase === 'HUMAN_INPUT' || phase === 'CPU_THINKING' || phase === 'OPPONENT_TURN');

		const key = String(startedAt) + ':' + String(limitSec) + ':' + phase;
		if (turnTimer.key !== key) {
			turnTimer.key = key;
			turnTimer.warned30 = false;
			turnTimer.warned15 = false;
			turnTimer.firing = false;
		}

		function tick() {
			const box = document.getElementById('battle-turn-timer');
			if (!box) return;
			box.classList.remove('battle-turn-timer--warn', 'battle-turn-timer--danger');

			if (!counts || !startedAt || !limitSec) {
				box.textContent = '持ち時間: --:--';
				return;
			}

			const elapsed = Math.floor((Date.now() - startedAt) / 1000);
			const remain = limitSec - Math.max(0, elapsed);
			box.textContent = '持ち時間: ' + fmtMmSs(remain) + ' / ' + fmtMmSs(limitSec);

			if (remain <= 15) {
				box.classList.add('battle-turn-timer--danger');
			} else if (remain <= 30) {
				box.classList.add('battle-turn-timer--warn');
			}

			if (!turnTimer.warned30 && remain <= 30 && remain > 15) {
				turnTimer.warned30 = true;
				showBattleToast('残り30秒です', 'warn');
			}
			if (!turnTimer.warned15 && remain <= 15 && remain > 0) {
				turnTimer.warned15 = true;
				showBattleToast('残り15秒です', 'danger');
			}

			if (remain <= 0 && !turnTimer.firing) {
				turnTimer.firing = true;
				showBattleToast(stage >= 3 ? '時間切れ（降参扱い）' : '時間切れ（ターン終了）', 'danger');
				timeoutTick().then(function (next) {
					turnTimer.firing = false;
					render(next);
				}).catch(function () {
					turnTimer.firing = false;
				});
			}
		}

		if (ui._turnTimer == null) {
			ui._turnTimer = window.setInterval(tick, 250);
		}
		tick();
	}

	function fillBattleLogList(lines) {
		if (!battleLogList) return;
		battleLogList.innerHTML = '';
		if (!lines || !lines.length) {
			battleLogList.appendChild(el('li', 'battle-log-modal__empty', 'ログはまだありません。'));
			return;
		}
		lines.forEach(function (line) {
			battleLogList.appendChild(el('li', '', line));
		});
	}

	function openBattleLogModal() {
		if (!battleLogModal) return;
		fillBattleLogList(lastEventLog);
		battleLogModal.hidden = false;
		document.body.style.overflow = 'hidden';
		if (battleLogOpenBtn) battleLogOpenBtn.setAttribute('aria-expanded', 'true');
	}

	function closeBattleLogModal() {
		if (!battleLogModal) return;
		battleLogModal.hidden = true;
		document.body.style.overflow = '';
		if (battleLogOpenBtn) battleLogOpenBtn.setAttribute('aria-expanded', 'false');
	}

	function wireBattleLogModal() {
		if (battleLogOpenBtn) {
			battleLogOpenBtn.addEventListener('click', function () {
				openBattleLogModal();
			});
		}
		if (battleLogCloseBtn) {
			battleLogCloseBtn.addEventListener('click', function () {
				closeBattleLogModal();
			});
		}
		if (battleLogModal) {
			battleLogModal.addEventListener('click', function (e) {
				if (e.target === battleLogModal) {
					closeBattleLogModal();
				}
			});
		}
		document.addEventListener('keydown', function (e) {
			if (e.key === 'Escape' && battleLogModal && !battleLogModal.hidden) {
				closeBattleLogModal();
			}
		});
	}

	async function rerenderWithFreshState() {
		const st = await fetchState();
		ui.levelUpStones = clamp(ui.levelUpStones, 0, st.humanStones);
		ui.levelUpRest = clamp(ui.levelUpRest, 0, maxLevelUpRestDiscard(st.humanHand ? st.humanHand.length : 0));
		render(st);
	}

	/** レベルアップ欄を閉じる（外側クリック等）。手札カードを再選択すると再度開く */
	function cancelLevelUpInProgress() {
		ui.selectedInstanceId = null;
		ui.levelUpRest = 0;
		ui.levelUpStones = 0;
		ui.levelUpDiscardIds = [];
		ui.warnLevelUpRest = null;
		ui.warnLevelUpStone = null;
		ui.sparkLevelUpRest = false;
		ui.sparkLevelUpStone = false;
		ui._luPrevPowerInstanceId = null;
		ui._luPrevPower = null;
	}

	async function applyLevelUpAdjust(action) {
		const st = await fetchState();
		ui.levelUpStones = clamp(ui.levelUpStones, 0, st.humanStones);
		const handLen = st.humanHand ? st.humanHand.length : 0;
		const maxRest = maxLevelUpRestDiscard(handLen);
		ui.levelUpRest = clamp(ui.levelUpRest, 0, maxRest);

		if (action === 'rest_minus') {
			ui.warnLevelUpRest = null;
			ui.levelUpRest = clamp(ui.levelUpRest - 1, 0, maxRest);
		} else if (action === 'rest_plus') {
			if (ui.levelUpRest >= maxRest) {
				ui.warnLevelUpRest = '配置するカードを残すため、これ以上捨てられません';
			} else {
				ui.warnLevelUpRest = null;
				ui.levelUpRest += 1;
				ui.sparkLevelUpRest = true;
			}
		} else if (action === 'stone_minus') {
			ui.warnLevelUpStone = null;
			ui.levelUpStones = clamp(ui.levelUpStones - 1, 0, st.humanStones);
		} else if (action === 'stone_plus') {
			if (ui.levelUpStones >= st.humanStones) {
				ui.warnLevelUpStone = 'これ以上、ストーンがありません';
			} else {
				ui.warnLevelUpStone = null;
				ui.levelUpStones += 1;
				ui.sparkLevelUpStone = true;
			}
		}
		render(st);
	}

	function battleCardFromZoneHandOrRestEl(zoneOrHandEl) {
		const st = lastStateForHandPower;
		if (!zoneOrHandEl || !st) return null;
		const ds = zoneOrHandEl.dataset;
		const inst = ds && (ds.battleInstanceId || ds.instanceId);
		if (!inst) return null;
		const findInZone = function (z) {
			return z && z.main && String(z.main.instanceId) === String(inst) ? z.main : null;
		};
		let bc = findInZone(st.humanBattle) || findInZone(st.cpuBattle);
		if (bc) return bc;
		(st.humanHand || []).forEach(function (c) {
			if (!bc && c && String(c.instanceId) === String(inst)) bc = c;
		});
		if (!bc) {
			(st.cpuHand || []).forEach(function (c) {
				if (!bc && c && String(c.instanceId) === String(inst)) bc = c;
			});
		}
		if (!bc) {
			(st.humanRest || []).concat(st.cpuRest || []).forEach(function (c) {
				if (!bc && c && String(c.instanceId) === String(inst)) bc = c;
			});
		}
		return bc || null;
	}

	function zoneOrHandDetailDef(zoneOrHandEl, cardIdStr) {
		const defs = lastDefsForTooltip;
		const st = lastStateForHandPower;
		if (!defs || cardIdStr == null) return null;
		const base = resolveCardDef(defs, cardIdStr);
		if (!base) return null;
		const ds = zoneOrHandEl && zoneOrHandEl.dataset;
		const inst = ds && (ds.battleInstanceId || ds.instanceId);
		if (!inst || !st) return base;
		const findInZone = function (z) {
			return z && z.main && String(z.main.instanceId) === String(inst) ? z.main : null;
		};
		const zc = findInZone(st.humanBattle) || findInZone(st.cpuBattle);
		if (zc) return cardDefForBattleFace(base, zc, defs);
		let hc = null;
		(st.humanHand || []).forEach(function (c) {
			if (c && String(c.instanceId) === String(inst)) hc = c;
		});
		if (!hc) {
			(st.cpuHand || []).forEach(function (c) {
				if (c && String(c.instanceId) === String(inst)) hc = c;
			});
		}
		if (!hc) {
			(st.humanRest || []).concat(st.cpuRest || []).forEach(function (c) {
				if (c && String(c.instanceId) === String(inst)) hc = c;
			});
		}
		return hc ? cardDefForBattleFace(base, hc, defs) : base;
	}

	function attachHandlers() {
		app.addEventListener('click', function (e) {
			const t = eventTargetElement(e);
			if (!t) return;

			/* オーバーレイ内の決定/± 等を、バトルゾーン詳細（.battle-zone-card）より先に処理する */
			const actBtn = t.closest('button[data-action]');
			if (actBtn) {
				const action = actBtn.getAttribute('data-action');
				if (action) {
					if (
						action === 'rest_minus' ||
						action === 'rest_plus' ||
						action === 'stone_minus' ||
						action === 'stone_plus'
					) {
						applyLevelUpAdjust(action);
						return;
					}
				}
				return;
			}

			const zoneCard = t.closest('.battle-zone-card');
			if (zoneCard && zoneCard instanceof HTMLElement) {
				const cid = zoneCard.dataset.battleCardId;
				const d = cid ? zoneOrHandDetailDef(zoneCard, cid) : null;
				if (d) {
					showBattleZoneDetailModal(
						d,
						parseBattlePowerContributorsFromHost(zoneCard),
						lastStateForHandPower,
						battleCardFromZoneHandOrRestEl(zoneCard)
					);
				}
				return;
			}

			const cardBtn = t.closest('.battle-card');
			if (cardBtn && cardBtn instanceof HTMLButtonElement && !cardBtn.disabled) {
				const inst = cardBtn.dataset.instanceId || null;
				ui.selectedInstanceId = ui.selectedInstanceId === inst ? null : inst;
				if (!ui.selectedInstanceId) {
					ui._luPrevPowerInstanceId = null;
					ui._luPrevPower = null;
				}
				ui.warnLevelUpRest = null;
				ui.warnLevelUpStone = null;
				rerenderWithFreshState();
				return;
			}

			if (t.closest('.battle-control-overlay__cluster')) {
				return;
			}

			if (ui.selectedInstanceId) {
				cancelLevelUpInProgress();
				rerenderWithFreshState();
			}
		});

		/* #battle-app 外（ヘッダー等）や、app 内で取りこぼしたクリックでもレベルアップを閉じる */
		document.addEventListener(
			'click',
			function (e) {
				const t = eventTargetElement(e);
				if (!t) return;
				if (!ui.selectedInstanceId) return;
				if (!app.querySelector('.battle-control-overlay--levelup-popup')) return;
				if (
					document.querySelector('.battle-pay-modal') ||
					document.getElementById('battle-pending-choice-modal') ||
					document.querySelector('.battle-result-modal')
				) {
					return;
				}
				const logModal = document.getElementById('battle-log-modal');
				if (logModal && !logModal.hidden && logModal.contains(t)) return;
				if (t.closest('.battle-control-overlay__cluster')) return;
				const handPick = t.closest('button.hand-card.battle-card');
				if (handPick && !handPick.disabled) return;
				cancelLevelUpInProgress();
				rerenderWithFreshState();
			},
			false
		);

		app.addEventListener('contextmenu', function (e) {
			const t = eventTargetElement(e);
			if (!t) return;

			const zoneCard = t.closest('.battle-zone-card');
			if (zoneCard && zoneCard instanceof HTMLElement) {
				const cid = zoneCard.dataset.battleCardId;
				const d = cid ? zoneOrHandDetailDef(zoneCard, cid) : null;
				if (d) {
					e.preventDefault();
					showBattleZoneDetailModal(
						d,
						parseBattlePowerContributorsFromHost(zoneCard),
						lastStateForHandPower,
						battleCardFromZoneHandOrRestEl(zoneCard)
					);
				}
				return;
			}

			const handBtn = t.closest('button.hand-card.battle-card');
			if (handBtn && handBtn instanceof HTMLButtonElement) {
				const cid = handBtn.dataset.cardId;
				const d = cid ? zoneOrHandDetailDef(handBtn, cid) : null;
				if (d) {
					e.preventDefault();
					showBattleZoneDetailModal(d, [], lastStateForHandPower, battleCardFromZoneHandOrRestEl(handBtn));
				}
			}
		});
	}

	(async function init() {
		wireBattleLogModal();
		installSurrenderIntercept();
		try {
			function applyBattleZoom() {
				const appEl = document.getElementById('battle-app');
				if (!appEl) return;
				const base = 980; // CSS の #battle-app 幅と揃える
				// 画面に収まるよう縮小のみ（拡大はしない）
				const z = Math.max(0.72, Math.min(1, window.innerWidth / (base + 24)));
				appEl.style.setProperty('--battle-zoom', String(z));
			}

			document.addEventListener('scroll', hideBattleCardTooltip, true);
			document.addEventListener('scroll', hideBattleDeckTooltip, true);
			window.addEventListener('resize', applyBattleZoom);
			applyBattleZoom();
			ensureBattleTurnPopupHost();
			const st = await fetchState();
			/* 先攻/後攻・名前のイントロが終わるまで描画しない（描画内で CPU_THINKING タイマーが走るのを防ぐ） */
			await runBattleIntroFromMeta();
			render(st);
			attachHandlers();
		} catch (e) {
			app.innerHTML = '';
			const p = el('p', 'panel error', '読み込みに失敗しました。再読み込みしてください。');
			app.appendChild(p);
			// eslint-disable-next-line no-console
			console.error(e);
		}
	})();
})();

