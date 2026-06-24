(function () {
	const lockTooltipEl = document.getElementById('season-lock-tooltip');
	if (!lockTooltipEl) {
		return;
	}
	const lockTooltipName = lockTooltipEl.querySelector('.pack-buy-lock-tooltip__name');
	const lockTooltipUnlock = lockTooltipEl.querySelector('.pack-buy-lock-tooltip__unlock');

	function hideLockTooltip() {
		lockTooltipEl.hidden = true;
	}

	function positionLockTooltip(clientX, clientY) {
		const pad = 12;
		const tw = lockTooltipEl.offsetWidth;
		const th = lockTooltipEl.offsetHeight;
		let x = clientX + pad;
		let y = clientY + pad;
		if (x + tw > window.innerWidth - pad) {
			x = Math.max(pad, clientX - tw - pad);
		}
		if (y + th > window.innerHeight - pad) {
			y = Math.max(pad, window.innerHeight - th - pad);
		}
		lockTooltipEl.style.left = x + 'px';
		lockTooltipEl.style.top = y + 'px';
	}

	function showLockTooltip(el, clientX, clientY) {
		if (!lockTooltipUnlock) {
			return;
		}
		const hint = el.getAttribute('data-season-lock-hint') || '';
		if (!hint) {
			return;
		}
		if (lockTooltipName) {
			lockTooltipName.textContent = el.getAttribute('data-season-lock-name') || '？？？？';
		}
		lockTooltipUnlock.textContent = hint;
		lockTooltipEl.hidden = false;
		positionLockTooltip(clientX, clientY);
	}

	document.querySelectorAll('.js-season-locked').forEach(function (el) {
		el.addEventListener('pointerenter', function (e) {
			showLockTooltip(el, e.clientX, e.clientY);
		});
		el.addEventListener('pointermove', function (e) {
			if (!lockTooltipEl.hidden) {
				positionLockTooltip(e.clientX, e.clientY);
			}
		});
		el.addEventListener('pointerleave', hideLockTooltip);
	});
})();
