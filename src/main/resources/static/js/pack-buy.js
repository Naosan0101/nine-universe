(function () {
	const bar = document.getElementById('pack-buy-confirm-bar');
	const title = document.getElementById('pack-buy-confirm-title');
	const paymentEl = document.getElementById('pack-buy-confirm-payment');
	const typeInput = document.getElementById('pack-buy-type');
	const confirmBtn = document.getElementById('pack-buy-confirm');
	const cancelBtn = document.getElementById('pack-buy-cancel');

	let selected = null;

	function getUserGems() {
		const raw = document.body.getAttribute('data-user-gems');
		const n = parseInt(raw, 10);
		return Number.isFinite(n) ? n : 0;
	}

	function updatePaymentSummary(costStr) {
		if (!paymentEl) return;
		if (!costStr) {
			paymentEl.hidden = true;
			paymentEl.textContent = '';
			paymentEl.classList.remove('pack-buy-confirm__payment--insufficient');
			return;
		}
		const cost = parseInt(costStr, 10) || 0;
		const now = getUserGems();
		const after = now - cost;
		paymentEl.hidden = false;
		paymentEl.classList.toggle('pack-buy-confirm__payment--insufficient', after < 0);
		if (after >= 0) {
			paymentEl.textContent =
				'支払い ' + cost + 'ジェム（購入後の所持 ' + after + 'ジェム）';
		} else {
			paymentEl.textContent =
				'支払い ' +
				cost +
				'ジェム（所持 ' +
				now +
				'ジェムのため、あと ' +
				(-after) +
				'ジェム不足）';
		}
	}

	function anyDetailOpen() {
		return Array.from(document.querySelectorAll('.pack-detail-modal')).some(function (m) {
			return !m.hidden;
		});
	}

	function syncBodyScroll() {
		document.body.style.overflow = anyDetailOpen() ? 'hidden' : '';
	}

	function closeDetailModals() {
		document.querySelectorAll('.pack-detail-modal').forEach(function (m) {
			m.hidden = true;
		});
		syncBodyScroll();
	}

	function openDetailModal(id) {
		if (!id) return;
		closeDetailModals();
		const el = document.getElementById(id);
		if (el) {
			el.hidden = false;
			syncBodyScroll();
		}
	}

	function setSelected(btn) {
		selected = btn || null;
		document.querySelectorAll('.pack-buy__item.is-selected').forEach(function (el) {
			el.classList.remove('is-selected');
		});
		if (!selected) {
			if (typeInput) typeInput.value = '';
			if (title) title.textContent = 'パックを選択してください';
			updatePaymentSummary('');
			if (confirmBtn) {
				confirmBtn.disabled = true;
				confirmBtn.textContent = '購入';
			}
			if (cancelBtn) cancelBtn.disabled = true;
			if (bar) bar.hidden = true;
			return;
		}
		selected.classList.add('is-selected');
		const packType = selected.dataset.packType || '';
		const packName = selected.dataset.packName || 'パック';
		const packCost = selected.dataset.packCost || '';
		if (typeInput) typeInput.value = packType;
		if (title) title.textContent = packName + 'を購入しますか？';
		updatePaymentSummary(packCost);
		if (confirmBtn) {
			confirmBtn.disabled = !packType;
			confirmBtn.textContent = packCost ? packCost + 'ジェムで購入' : '購入';
		}
		if (cancelBtn) cancelBtn.disabled = !packType;
		if (bar) bar.hidden = !packType;
	}

	function clearSelection() {
		setSelected(null);
	}

	document.querySelectorAll('.pack-buy__item[data-pack-type]').forEach(function (btn) {
		btn.addEventListener('click', function () {
			setSelected(btn);
		});
	});

	document.querySelectorAll('[data-open-pack-detail]').forEach(function (btn) {
		btn.addEventListener('click', function (e) {
			e.preventDefault();
			e.stopPropagation();
			openDetailModal(btn.getAttribute('data-open-pack-detail'));
		});
	});

	document.querySelectorAll('[data-pack-detail-close]').forEach(function (el) {
		el.addEventListener('click', function () {
			closeDetailModals();
		});
	});

	if (cancelBtn) cancelBtn.addEventListener('click', clearSelection);
	document.addEventListener('keydown', function (e) {
		if (e.key === 'Escape') {
			closeDetailModals();
			clearSelection();
		}
	});

	clearSelection();
})();
