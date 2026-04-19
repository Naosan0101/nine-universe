/**
 * バトル開始時のデッキ選択: 前回選んだデッキを先頭に並べ、送信時に localStorage へ保存する。
 */
(function () {
	var STORAGE_KEY = 'nu.lastBattleDeckId';

	function moveLastUsedDeckFirst(select) {
		if (!select || !select.options.length) {
			return;
		}
		var lastId;
		try {
			lastId = localStorage.getItem(STORAGE_KEY);
		} catch (e) {
			return;
		}
		if (lastId == null || lastId === '') {
			return;
		}
		var opts = select.options;
		for (var i = 0; i < opts.length; i++) {
			if (String(opts[i].value) === String(lastId)) {
				if (i > 0) {
					select.insertBefore(opts[i], opts[0]);
				}
				// 先頭へ移動しても selected は元の index に付いたままなので、表示を前回デッキに合わせる
				select.value = lastId;
				break;
			}
		}
	}

	function wireSaveOnSubmit(form, select) {
		form.addEventListener('submit', function () {
			try {
				localStorage.setItem(STORAGE_KEY, String(select.value));
			} catch (e) {
				/* private mode 等 */
			}
		});
	}

	document.addEventListener('DOMContentLoaded', function () {
		var select = document.querySelector('select[name="deckId"]');
		if (!select) {
			return;
		}
		moveLastUsedDeckFirst(select);
		var form = select.closest('form');
		if (form) {
			wireSaveOnSubmit(form, select);
		}
	});
})();
