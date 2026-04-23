(function () {
	var choiceModal = document.getElementById('time-pack-choice-modal');
	var openBtn = document.getElementById('time-pack-open-btn');
	var epithetModal = document.getElementById('home-bonus-epithet-modal');
	var epithetUpperEl = document.getElementById('home-bonus-epithet-upper');
	var epithetLowerEl = document.getElementById('home-bonus-epithet-lower');
	/** 二つ名ポップアップを閉じたあと、ボーナス選択モーダルを開くか */
	var epithetBonusThenOpenChoice = false;

	function anyHomeDetailOpen() {
		var a = document.getElementById('home-pack-detail-standard');
		var b = document.getElementById('home-pack-detail-standard-2');
		return (a && !a.hidden) || (b && !b.hidden);
	}

	function epithetBonusOpen() {
		return epithetModal && !epithetModal.hidden;
	}

	function syncBodyScroll() {
		var choiceOpen = choiceModal && !choiceModal.hidden;
		document.body.style.overflow =
			choiceOpen || anyHomeDetailOpen() || epithetBonusOpen() ? 'hidden' : '';
	}

	function closeChoiceModal() {
		if (!choiceModal) return;
		choiceModal.hidden = true;
		choiceModal.setAttribute('aria-hidden', 'true');
		syncBodyScroll();
		if (openBtn) openBtn.focus();
	}

	function openChoiceModal() {
		if (!choiceModal || !openBtn || openBtn.disabled) return;
		choiceModal.hidden = false;
		choiceModal.setAttribute('aria-hidden', 'false');
		syncBodyScroll();
		var first = choiceModal.querySelector('button, [href], input, select, textarea');
		if (first) first.focus();
	}

	function closeDetailModals() {
		document.querySelectorAll('#home-pack-detail-standard, #home-pack-detail-standard-2').forEach(function (m) {
			if (m) m.hidden = true;
		});
		syncBodyScroll();
	}

	function openDetailModal(id) {
		if (!id) return;
		closeDetailModals();
		var el = document.getElementById(id);
		if (el) {
			el.hidden = false;
			syncBodyScroll();
		}
	}

	function closeEpithetBonusModal() {
		if (!epithetModal) return;
		epithetModal.hidden = true;
		epithetModal.setAttribute('aria-hidden', 'true');
		syncBodyScroll();
	}

	function openEpithetBonusModalThenMaybeChoice() {
		if (!epithetModal || !document.body) return;
		var reveal = document.body.getAttribute('data-home-bonus-epithet-reveal') === 'true';
		if (!reveal) return;
		var u = epithetUpperEl ? document.body.getAttribute('data-home-bonus-epithet-upper') || '' : '';
		var l = epithetLowerEl ? document.body.getAttribute('data-home-bonus-epithet-lower') || '' : '';
		if (epithetUpperEl) epithetUpperEl.textContent = u || '—';
		if (epithetLowerEl) epithetLowerEl.textContent = l || '—';
		epithetModal.hidden = false;
		epithetModal.setAttribute('aria-hidden', 'false');
		syncBodyScroll();
		tryStripUrlParams(['showBonusEpithet']);
		var ok = epithetModal.querySelector('.epithet-gacha-result-modal__actions [data-home-bonus-epithet-close]');
		if (ok && typeof ok.focus === 'function') ok.focus();

		function afterClose() {
			var goChoice = epithetBonusThenOpenChoice;
			epithetBonusThenOpenChoice = false;
			if (goChoice) {
				openChoiceModal();
			} else if (openBtn) {
				openBtn.focus();
			}
		}

		epithetModal.addEventListener(
			'click',
			function onEpithetBonusClose(e) {
				var t = e.target;
				if (!t || typeof t.closest !== 'function') return;
				if (!t.closest('[data-home-bonus-epithet-close]')) return;
				epithetModal.removeEventListener('click', onEpithetBonusClose);
				closeEpithetBonusModal();
				afterClose();
			},
			{ once: true }
		);
	}

	function tryStripUrlParams(keys) {
		if (!window.history || !window.history.replaceState) return;
		try {
			var url = new URL(window.location.href);
			var changed = false;
			keys.forEach(function (k) {
				if (url.searchParams.has(k)) {
					url.searchParams.delete(k);
					changed = true;
				}
			});
			if (!changed) return;
			var qs = url.searchParams.toString();
			window.history.replaceState({}, '', url.pathname + (qs ? '?' + qs : '') + url.hash);
		} catch (e) {
			// noop
		}
	}

	if (openBtn && choiceModal) {
		openBtn.addEventListener('click', function () {
			if (!openBtn.disabled) openChoiceModal();
		});
	}

	document.querySelectorAll('[data-time-pack-choice-close]').forEach(function (el) {
		el.addEventListener('click', function () {
			closeChoiceModal();
		});
	});

	document.querySelectorAll('#time-pack-choice-modal [data-open-pack-detail]').forEach(function (btn) {
		btn.addEventListener('click', function (e) {
			e.preventDefault();
			e.stopPropagation();
			openDetailModal(btn.getAttribute('data-open-pack-detail'));
		});
	});

	document.querySelectorAll('#home-pack-detail-standard [data-pack-detail-close], #home-pack-detail-standard-2 [data-pack-detail-close]').forEach(function (el) {
		el.addEventListener('click', function () {
			closeDetailModals();
		});
	});

	document.addEventListener('keydown', function (e) {
		if (e.key !== 'Escape') return;
		if (anyHomeDetailOpen()) {
			closeDetailModals();
			return;
		}
		if (epithetBonusOpen()) {
			var goChoice = epithetBonusThenOpenChoice;
			epithetBonusThenOpenChoice = false;
			closeEpithetBonusModal();
			if (goChoice) {
				openChoiceModal();
			} else if (openBtn) {
				openBtn.focus();
			}
			return;
		}
		if (choiceModal && !choiceModal.hidden) {
			closeChoiceModal();
		}
	});

	try {
		var sp = new URLSearchParams(window.location.search);
		var wantOpenChoice = sp.get('openTimePackChoice') === '1';
		var bodyReveal = document.body && document.body.getAttribute('data-home-bonus-epithet-reveal') === 'true';

		if (bodyReveal) {
			epithetBonusThenOpenChoice = wantOpenChoice;
			openEpithetBonusModalThenMaybeChoice();
		} else if (wantOpenChoice && choiceModal && openBtn && !openBtn.disabled) {
			openChoiceModal();
		}

		if (wantOpenChoice) {
			tryStripUrlParams(['openTimePackChoice']);
		}
	} catch (e) {
		// noop
	}
})();
