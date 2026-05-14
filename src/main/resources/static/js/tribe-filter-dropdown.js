/**
 * ツールバー絞り込み: クリックで直下に一覧（スクロールなし）。値は type=hidden + change イベント。
 * マークアップ: <span class="tribe-filter" data-toolbar-filter data-filter-set="tribe|cardKind|pack|...">
 */
(function () {
	var FILTER_OPTION_SETS = {
		tribe: [
			{ v: '', t: 'すべて' },
			{ v: 'HUMAN', t: '人間' },
			{ v: 'ELF', t: 'エルフ' },
			{ v: 'UNDEAD', t: 'アンデッド' },
			{ v: 'DRAGON', t: 'ドラゴン' },
			{ v: 'MACHINE', t: 'マシン' },
			{ v: 'CARBUNCLE', t: 'カーバンクル' },
			{ v: 'MERFOLK', t: 'マーフォーク' },
			{ v: 'COMIC', t: 'コミック' },
			{ v: 'ANGEL', t: 'エンジェル' }
		],
		cardKind: [
			{ v: '', t: 'すべて' },
			{ v: 'fighter', t: 'ファイター' },
			{ v: 'field', t: 'フィールド' }
		],
		pack: [
			{ v: '', t: 'すべて' },
			{ v: 'STANDARD_1', t: 'スタンダードパック1（WH/ET）' },
			{ v: 'STANDARD_2', t: 'スタンダードパック2（JU/IF）' },
			{ v: 'STANDARD_3', t: 'スタンダードパック3（OT/CS）' },
			{ v: 'WH', t: '風吹く丘パック（WH）' },
			{ v: 'ET', t: '邪悪なる脅威パック（ET）' },
			{ v: 'JU', t: '宝石の秘境パック（JU）' },
			{ v: 'IF', t: '鉄面の艦隊パック（IF）' },
			{ v: 'OT', t: '海底の潮流パック（OT）' },
			{ v: 'CS', t: '創世の神域パック（CS）' }
		],
		cost: [
			{ v: '', t: 'すべて' },
			{ v: '0', t: '0' },
			{ v: '1', t: '1' },
			{ v: '2', t: '2' },
			{ v: '3', t: '3' },
			{ v: '4', t: '4' }
		],
		power: [
			{ v: '', t: 'すべて' },
			{ v: '0', t: '0' },
			{ v: '1', t: '1' },
			{ v: '2', t: '2' },
			{ v: '3', t: '3' },
			{ v: '4', t: '4' },
			{ v: '5', t: '5' },
			{ v: '6', t: '6' },
			{ v: '7', t: '7' },
			{ v: '8', t: '8' }
		],
		rarity: [
			{ v: '', t: 'すべて' },
			{ v: 'Reg', t: 'レジェンダリー' },
			{ v: 'Ep', t: 'エピック' },
			{ v: 'R', t: 'レア' },
			{ v: 'C', t: 'コモン' }
		],
		libSort: [
			{ v: 'cost_asc', t: 'コスト 小→大' },
			{ v: 'cost_desc', t: 'コスト 大→小' }
		]
	};

	function optionsFor(root) {
		var key = root.getAttribute('data-filter-set');
		return key ? FILTER_OPTION_SETS[key] : null;
	}

	function labelForValue(opts, val) {
		for (var i = 0; i < opts.length; i++) {
			if (opts[i].v === val) return opts[i].t;
		}
		return opts[0] ? opts[0].t : '';
	}

	function closePanel(root) {
		var btn = root.querySelector('.tribe-filter__trigger');
		var list = root.querySelector('.tribe-filter__list');
		if (!btn || !list) return;
		list.hidden = true;
		root.classList.remove('tribe-filter--open');
		btn.setAttribute('aria-expanded', 'false');
	}

	function openPanel(root) {
		document.querySelectorAll('[data-toolbar-filter].tribe-filter--open').forEach(function (other) {
			if (other !== root) closePanel(other);
		});
		var btn = root.querySelector('.tribe-filter__trigger');
		var list = root.querySelector('.tribe-filter__list');
		if (!btn || !list) return;
		list.hidden = false;
		root.classList.add('tribe-filter--open');
		btn.setAttribute('aria-expanded', 'true');
	}

	function syncTrigger(hidden, opts) {
		var root = hidden.closest('[data-toolbar-filter]');
		if (!root) return;
		var btn = root.querySelector('.tribe-filter__trigger');
		if (btn) btn.textContent = labelForValue(opts, hidden.value);
	}

	function buildList(root, hidden, list, opts) {
		list.innerHTML = '';
		for (var i = 0; i < opts.length; i++) {
			(function (row) {
				var li = document.createElement('li');
				li.setAttribute('role', 'none');
				var b = document.createElement('button');
				b.type = 'button';
				b.className = 'tribe-filter__option';
				b.setAttribute('role', 'option');
				b.setAttribute('aria-selected', hidden.value === row.v ? 'true' : 'false');
				b.dataset.value = row.v;
				b.textContent = row.t;
				b.addEventListener('click', function (ev) {
					ev.preventDefault();
					ev.stopPropagation();
					if (hidden.value !== row.v) {
						hidden.value = row.v;
						hidden.dispatchEvent(new Event('change', { bubbles: true }));
					}
					list.querySelectorAll('.tribe-filter__option').forEach(function (el) {
						el.setAttribute('aria-selected', el.dataset.value === hidden.value ? 'true' : 'false');
					});
					var btnEl = root.querySelector('.tribe-filter__trigger');
					if (btnEl) btnEl.textContent = labelForValue(opts, hidden.value);
					closePanel(root);
				});
				li.appendChild(b);
				list.appendChild(li);
			})(opts[i]);
		}
	}

	function initRoot(root) {
		if (root.dataset.toolbarFilterInit) return;
		var opts = optionsFor(root);
		if (!opts || !opts.length) return;
		root.dataset.toolbarFilterInit = '1';

		var hidden = root.querySelector('input.tribe-filter__value[type="hidden"]');
		var btn = root.querySelector('.tribe-filter__trigger');
		var list = root.querySelector('.tribe-filter__list');
		if (!hidden || !btn || !list) return;

		var valid = false;
		for (var j = 0; j < opts.length; j++) {
			if (opts[j].v === hidden.value) {
				valid = true;
				break;
			}
		}
		if (!valid) hidden.value = opts[0].v;

		buildList(root, hidden, list, opts);
		syncTrigger(hidden, opts);
		hidden.addEventListener('change', function () {
			syncTrigger(hidden, opts);
			list.querySelectorAll('.tribe-filter__option').forEach(function (el) {
				el.setAttribute('aria-selected', el.dataset.value === hidden.value ? 'true' : 'false');
			});
		});

		btn.addEventListener('click', function (ev) {
			ev.preventDefault();
			ev.stopPropagation();
			if (root.classList.contains('tribe-filter--open')) closePanel(root);
			else openPanel(root);
		});
	}

	document.addEventListener('mousedown', function (ev) {
		document.querySelectorAll('[data-toolbar-filter]').forEach(function (root) {
			if (root.contains(ev.target)) return;
			closePanel(root);
		});
	});

	document.addEventListener('keydown', function (ev) {
		if (ev.key === 'Escape') {
			document.querySelectorAll('[data-toolbar-filter].tribe-filter--open').forEach(closePanel);
		}
	});

	function boot() {
		document.querySelectorAll('[data-toolbar-filter]').forEach(initRoot);
	}
	if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot);
	else boot();
})();
