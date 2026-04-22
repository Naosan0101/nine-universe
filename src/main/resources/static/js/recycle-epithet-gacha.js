(function () {
	var form = document.getElementById('recycle-epithet-gacha-form');
	var modal = document.getElementById('epithet-gacha-result-modal');
	var modalBackdrop = modal && modal.querySelector('[data-epithet-gacha-modal-close]');
	var modalCloseBtns = modal ? modal.querySelectorAll('[data-epithet-gacha-modal-close]') : [];
	var upperEl = document.getElementById('epithet-gacha-result-upper');
	var lowerEl = document.getElementById('epithet-gacha-result-lower');
	var crystalStrong = document.querySelector('.recycle-crystal-banner strong');
	if (!form || !modal || !upperEl || !lowerEl) return;

	var cost = parseInt(form.getAttribute('data-cost') || '0', 10) || 0;

	function setBodyScroll(lock) {
		document.body.style.overflow = lock ? 'hidden' : '';
	}

	function openModal() {
		modal.hidden = false;
		modal.setAttribute('aria-hidden', 'false');
		setBodyScroll(true);
		var focusTarget = modal.querySelector('button, [href]');
		if (focusTarget) focusTarget.focus();
	}

	function closeModal() {
		modal.hidden = true;
		modal.setAttribute('aria-hidden', 'true');
		setBodyScroll(false);
	}

	modalCloseBtns.forEach(function (el) {
		el.addEventListener('click', function () {
			closeModal();
		});
	});

	document.addEventListener('keydown', function (e) {
		if (e.key === 'Escape' && modal && !modal.hidden) {
			closeModal();
		}
	});

	form.addEventListener('submit', function (e) {
		e.preventDefault();
		var btn = form.querySelector('button[type="submit"]');
		var fd = new FormData(form);
		if (btn) btn.disabled = true;
		fetch(form.action, {
			method: 'POST',
			body: new URLSearchParams(fd),
			headers: { Accept: 'application/json' },
			credentials: 'same-origin'
		})
			.then(function (res) {
				return res.json().then(function (data) {
					return { status: res.status, data: data };
				});
			})
			.then(function (pack) {
				var data = pack.data;
				if (pack.status >= 200 && pack.status < 300 && data && data.ok) {
					upperEl.textContent = data.upperGained != null ? String(data.upperGained) : '';
					lowerEl.textContent = data.lowerGained != null ? String(data.lowerGained) : '';
					if (crystalStrong && data.recycleCrystal != null) {
						crystalStrong.textContent = String(data.recycleCrystal);
					}
					if (btn) {
						var cry = data.recycleCrystal != null ? data.recycleCrystal : 0;
						var can = data.canRollEpithetGacha === true;
						btn.disabled = cry < cost || !can;
					}
					openModal();
				} else {
					var msg = data && data.error ? data.error : 'エラーが発生しました。';
					window.alert(msg);
					if (btn) btn.disabled = false;
				}
			})
			.catch(function () {
				window.alert('通信に失敗しました。');
				if (btn) btn.disabled = false;
			});
	});
})();
