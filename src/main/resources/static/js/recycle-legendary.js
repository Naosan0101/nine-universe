(function () {
	'use strict';
	var hiddenId = document.getElementById('legendary-pick-card-id');
	var confirmBtn = document.getElementById('legendary-pick-confirm-btn');
	var form = document.getElementById('legendary-pick-trade-form');
	var grid = document.getElementById('recycle-legendary-grid');
	var crystalVal = document.getElementById('recycle-legendary-crystal-val');
	var ajaxMsg = document.getElementById('recycle-legendary-ajax-msg');
	var csrfMeta = document.querySelector('meta[name="_csrf"]');
	var csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
	var csrfToken = csrfMeta ? csrfMeta.getAttribute('content') : '';
	var csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : '';

	function clearSelection() {
		if (!grid) {
			return;
		}
		grid.querySelectorAll('.library-card--picker.library-card--selected').forEach(function (el) {
			el.classList.remove('library-card--selected');
		});
	}

	document.addEventListener('nu-recycle-legendary-select', function (ev) {
		var btn = ev.detail && ev.detail.button;
		var cardEl = ev.detail && ev.detail.cardEl;
		if (!btn || !cardEl || !grid) {
			return;
		}
		clearSelection();
		cardEl.classList.add('library-card--selected');
		if (hiddenId) {
			hiddenId.value = btn.getAttribute('data-card-id') || '';
		}
		if (confirmBtn) {
			confirmBtn.disabled = !hiddenId || !String(hiddenId.value || '').trim();
		}
		if (ajaxMsg) {
			ajaxMsg.hidden = true;
			ajaxMsg.textContent = '';
		}
	});

	if (form && confirmBtn) {
		form.addEventListener('submit', function (e) {
			e.preventDefault();
			if (!hiddenId || !String(hiddenId.value || '').trim()) {
				return;
			}
			confirmBtn.disabled = true;
			var fd = new FormData(form);
			var headers = { Accept: 'application/json' };
			if (csrfToken && csrfHeader) {
				headers[csrfHeader] = csrfToken;
			}
			fetch(form.getAttribute('action') || form.action, {
				method: 'POST',
				body: fd,
				headers: headers,
				credentials: 'same-origin'
			})
				.then(function (r) {
					return r.json().then(function (data) {
						return { httpOk: r.ok, data: data };
					});
				})
				.then(function (result) {
					var d = result.data;
					if (d && d.ok === true) {
						if (crystalVal && d.recycleCrystal != null) {
							crystalVal.textContent = String(d.recycleCrystal);
						}
						if (ajaxMsg) {
							ajaxMsg.textContent = d.message || 'レジェンダリーカードを獲得しました。';
							ajaxMsg.className = 'ok';
							ajaxMsg.hidden = false;
						}
						if (hiddenId) {
							hiddenId.value = '';
						}
						clearSelection();
						return;
					}
					var err = (d && d.error) || '処理に失敗しました。';
					if (ajaxMsg) {
						ajaxMsg.textContent = err;
						ajaxMsg.className = 'error';
						ajaxMsg.hidden = false;
					}
				})
				.catch(function () {
					if (ajaxMsg) {
						ajaxMsg.textContent = '通信に失敗しました。';
						ajaxMsg.className = 'error';
						ajaxMsg.hidden = false;
					}
				})
				.finally(function () {
					if (confirmBtn) {
						confirmBtn.disabled = !hiddenId || !String((hiddenId && hiddenId.value) || '').trim();
					}
				});
		});
	}
})();
