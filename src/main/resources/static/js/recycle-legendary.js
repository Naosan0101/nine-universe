(function () {
	var hiddenId = document.getElementById('legendary-pick-card-id');
	var confirmBtn = document.getElementById('legendary-pick-confirm-btn');
	var form = document.getElementById('legendary-pick-trade-form');
	var search = document.getElementById('recycle-legendary-search');
	var grid = document.getElementById('recycle-legendary-grid');
	var emptyMsg = document.getElementById('recycle-legendary-empty');

	function clearSelection() {
		if (!grid) return;
		grid.querySelectorAll('.library-card--picker.library-card--selected').forEach(function (el) {
			el.classList.remove('library-card--selected');
		});
	}

	document.addEventListener('nu-recycle-legendary-select', function (ev) {
		var btn = ev.detail && ev.detail.button;
		var cardEl = ev.detail && ev.detail.cardEl;
		if (!btn || !cardEl || !grid) return;
		clearSelection();
		cardEl.classList.add('library-card--selected');
		if (hiddenId) {
			hiddenId.value = btn.getAttribute('data-card-id') || '';
		}
		if (confirmBtn) {
			confirmBtn.disabled = !hiddenId || !String(hiddenId.value || '').trim();
		}
	});

	if (form && confirmBtn) {
		form.addEventListener('submit', function (e) {
			if (!hiddenId || !String(hiddenId.value || '').trim()) {
				e.preventDefault();
			}
		});
	}

	if (search && grid) {
		var cards = Array.from(grid.querySelectorAll('.library-card'));
		function apply() {
			var q = search.value.trim().toLowerCase();
			var any = false;
			cards.forEach(function (card) {
				var btn = card.querySelector('.library-card__open');
				var name = (btn && btn.dataset && btn.dataset.name) ? String(btn.dataset.name) : '';
				var ok = !q || name.toLowerCase().indexOf(q) !== -1;
				card.hidden = !ok;
				if (ok) {
					any = true;
				}
			});
			if (emptyMsg) {
				emptyMsg.hidden = any;
			}
		}
		search.addEventListener('input', apply);
		apply();
	}
})();
