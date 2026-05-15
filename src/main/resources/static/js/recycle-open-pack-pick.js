(function () {
	var modal = document.getElementById('recycle-pack-open-confirm-modal');
	var titleEl = document.getElementById('recycle-pack-open-confirm-title');
	var hintEl = document.getElementById('recycle-pack-open-confirm-hint');
	var cancelBtn = document.getElementById('recycle-pack-open-confirm-cancel');
	var confirmBtn = document.getElementById('recycle-pack-open-confirm-ok');
	if (!modal || !titleEl || !cancelBtn || !confirmBtn) {
		return;
	}

	var pendingForm = null;
	var prevFocus = null;

	function packCost() {
		var raw = document.body.getAttribute('data-recycle-pack-cost');
		var n = parseInt(raw, 10);
		return Number.isFinite(n) ? n : 0;
	}

	function userCrystal() {
		var raw = document.body.getAttribute('data-recycle-crystal');
		var n = parseInt(raw, 10);
		return Number.isFinite(n) ? n : 0;
	}

	function updateHint(cost) {
		if (!hintEl) {
			return;
		}
		var now = userCrystal();
		var after = now - cost;
		if (cost <= 0) {
			hintEl.hidden = true;
			hintEl.textContent = '';
			confirmBtn.disabled = false;
			confirmBtn.classList.remove('btn--disabled');
			return;
		}
		hintEl.hidden = false;
		if (after >= 0) {
			hintEl.textContent =
				'支払い ' + cost + 'クリスタル（開封後の所持 ' + after + 'クリスタル）';
			hintEl.classList.remove('recycle-pack-open-confirm__hint--warn');
			confirmBtn.disabled = false;
			confirmBtn.classList.remove('btn--disabled');
		} else {
			hintEl.textContent =
				'クリスタルが足りません（所持 ' + now + '、あと ' + (-after) + ' 必要）';
			hintEl.classList.add('recycle-pack-open-confirm__hint--warn');
			confirmBtn.disabled = true;
			confirmBtn.classList.add('btn--disabled');
		}
	}

	function closeModal() {
		modal.hidden = true;
		modal.setAttribute('aria-hidden', 'true');
		pendingForm = null;
		document.removeEventListener('keydown', onKeyDown);
		if (prevFocus && typeof prevFocus.focus === 'function') {
			try {
				prevFocus.focus();
			} catch (e) {
				/* ignore */
			}
		}
		prevFocus = null;
	}

	function openModal(form) {
		pendingForm = form;
		prevFocus = document.activeElement;
		var name = (form.getAttribute('data-pack-name') || '').trim();
		var cost = packCost();
		titleEl.textContent = name
			? '「' + name + '」を' + cost + 'クリスタルで開封しますか？'
			: cost + 'クリスタルで開封しますか？';
		confirmBtn.textContent = cost > 0 ? cost + 'クリスタルで開封' : '開封する';
		updateHint(cost);
		modal.hidden = false;
		modal.setAttribute('aria-hidden', 'false');
		document.addEventListener('keydown', onKeyDown);
		try {
			cancelBtn.focus();
		} catch (e2) {
			/* ignore */
		}
	}

	function onKeyDown(e) {
		if (e.key === 'Escape') {
			e.preventDefault();
			closeModal();
		}
	}

	document.querySelectorAll('form.recycle-pack-pick__form').forEach(function (form) {
		form.addEventListener('submit', function (e) {
			e.preventDefault();
			openModal(form);
		});
	});

	cancelBtn.addEventListener('click', closeModal);
	modal.addEventListener('click', function (e) {
		if (e.target === modal) {
			closeModal();
		}
	});
	confirmBtn.addEventListener('click', function () {
		var f = pendingForm;
		if (!f || confirmBtn.disabled) {
			return;
		}
		closeModal();
		f.submit();
	});
})();
