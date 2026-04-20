/**
 * バトル開始時のデッキ選択: 前回選んだデッキを先頭に並べ、送信時に localStorage へ保存する。
 * CPU バトル（/battle/cpu）では nu.lastCpuBattleDeckId / nu.lastCpuBattleLevel を使い、
 * PVP などでは従来どおり nu.lastBattleDeckId のみ。
 */
(function () {
	var STORAGE_KEY_DECK_GENERIC = 'nu.lastBattleDeckId';
	var STORAGE_KEY_DECK_CPU = 'nu.lastCpuBattleDeckId';
	var STORAGE_KEY_CPU_LEVEL = 'nu.lastCpuBattleLevel';

	function deckStorageKeyForForm(form) {
		return form && form.querySelector('input[name="cpuMode"]') ? STORAGE_KEY_DECK_CPU : STORAGE_KEY_DECK_GENERIC;
	}

	function readLastDeckIdForKey(storageKey) {
		try {
			if (storageKey === STORAGE_KEY_DECK_CPU) {
				var cpu = localStorage.getItem(STORAGE_KEY_DECK_CPU);
				if (cpu != null && cpu !== '') {
					return cpu;
				}
				/* 初回のみ: 従来キーから引き継ぐ */
				return localStorage.getItem(STORAGE_KEY_DECK_GENERIC);
			}
			return localStorage.getItem(storageKey);
		} catch (e) {
			return null;
		}
	}

	function moveLastUsedDeckFirst(select, storageKey) {
		if (!select || !select.options.length) {
			return;
		}
		var lastId = readLastDeckIdForKey(storageKey);
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

	function wireSaveDeckOnSubmit(form, select, storageKey) {
		form.addEventListener('submit', function () {
			try {
				localStorage.setItem(storageKey, String(select.value));
			} catch (e) {
				/* private mode 等 */
			}
		});
	}

	function readCpuLevelMap() {
		try {
			var raw = localStorage.getItem(STORAGE_KEY_CPU_LEVEL);
			if (!raw) {
				return {};
			}
			var o = JSON.parse(raw);
			return o && typeof o === 'object' ? o : {};
		} catch (e) {
			return {};
		}
	}

	function moveLastUsedCpuLevelFirst(form, levelSelect) {
		if (!form || !levelSelect || !levelSelect.options.length) {
			return;
		}
		var cpuInput = form.querySelector('input[name="cpuMode"]');
		if (!cpuInput) {
			return;
		}
		var mode = String(cpuInput.value || '');
		if (!mode) {
			return;
		}
		var last = readCpuLevelMap()[mode];
		if (last == null || last === '') {
			return;
		}
		var opts = levelSelect.options;
		for (var i = 0; i < opts.length; i++) {
			if (String(opts[i].value) === String(last)) {
				if (i > 0) {
					levelSelect.insertBefore(opts[i], opts[0]);
				}
				levelSelect.value = last;
				break;
			}
		}
	}

	function wireSaveCpuLevelOnSubmit(form, levelSelect) {
		var cpuInput = form.querySelector('input[name="cpuMode"]');
		if (!cpuInput || !levelSelect) {
			return;
		}
		form.addEventListener('submit', function () {
			try {
				var mode = String(cpuInput.value || '');
				if (!mode) {
					return;
				}
				var map = readCpuLevelMap();
				map[mode] = String(levelSelect.value);
				localStorage.setItem(STORAGE_KEY_CPU_LEVEL, JSON.stringify(map));
			} catch (e) {
				/* private mode 等 */
			}
		});
	}

	document.addEventListener('DOMContentLoaded', function () {
		var selects = document.querySelectorAll('select.js-cpu-battle-deck, select[name="deckId"]');
		for (var s = 0; s < selects.length; s++) {
			var select = selects[s];
			var form = select.closest('form');
			var deckKey = deckStorageKeyForForm(form);
			moveLastUsedDeckFirst(select, deckKey);
			if (form) {
				wireSaveDeckOnSubmit(form, select, deckKey);
			}
		}

		var cpuModeInputs = document.querySelectorAll('form input[name="cpuMode"]');
		for (var c = 0; c < cpuModeInputs.length; c++) {
			var cform = cpuModeInputs[c].closest('form');
			if (!cform) {
				continue;
			}
			var levelSel = cform.querySelector('select[name="level"]');
			if (!levelSel) {
				continue;
			}
			moveLastUsedCpuLevelFirst(cform, levelSel);
			wireSaveCpuLevelOnSubmit(cform, levelSel);
		}
	});
})();
