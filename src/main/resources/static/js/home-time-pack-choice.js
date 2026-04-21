(function () {
	var choiceModal = document.getElementById('time-pack-choice-modal');
	var openBtn = document.getElementById('time-pack-open-btn');
	var slot2 = document.getElementById('time-pack-slot2-wrap');
	var label1 = document.getElementById('time-pack-label-slot1');

	function anyHomeDetailOpen() {
		var a = document.getElementById('home-pack-detail-standard');
		var b = document.getElementById('home-pack-detail-standard-2');
		return (a && !a.hidden) || (b && !b.hidden);
	}

	function syncBodyScroll() {
		var choiceOpen = choiceModal && !choiceModal.hidden;
		document.body.style.overflow = choiceOpen || anyHomeDetailOpen() ? 'hidden' : '';
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
		if (choiceModal && !choiceModal.hidden) {
			closeChoiceModal();
		}
	});

	window.nuTimePackChoiceSyncSlot2 = function (packs) {
		var n = typeof packs === 'number' ? packs : 0;
		if (slot2) {
			slot2.hidden = n < 2;
		}
		if (label1) {
			label1.textContent = n > 1 ? '1パック目' : '開封するパック';
		}
		document.querySelectorAll('input[name="pack1"]').forEach(function (r) {
			r.disabled = n < 2;
		});
	};
})();
