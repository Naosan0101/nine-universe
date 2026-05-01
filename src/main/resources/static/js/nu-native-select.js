/**
 * ネイティブ select の OS ドロップダウンはカーソル CSS が効かないため、
 * ボタン + リストで置き換え（値は隠した select に同期しフォーム送信は従来どおり）。
 */
(function () {
	'use strict';

	var OPEN_CLASS = 'nu-native-select--open';

	function shouldSkip(sel) {
		if (!sel || sel.tagName !== 'SELECT') {
			return true;
		}
		if (sel.multiple) {
			return true;
		}
		var sz = sel.getAttribute('size');
		if (sz != null && String(sz).trim() !== '' && parseInt(sz, 10) > 1) {
			return true;
		}
		if (sel.disabled) {
			return true;
		}
		if (sel.closest('.page--battle') || sel.closest('#battle-app')) {
			return true;
		}
		if (sel.dataset.nuNativeSelect === 'enhanced') {
			return true;
		}
		if (sel.classList.contains('nu-native-select-skip')) {
			return true;
		}
		return false;
	}

	function findLabelForSelect(sel) {
		var id = sel.id;
		if (id) {
			try {
				var esc = typeof CSS !== 'undefined' && typeof CSS.escape === 'function' ? CSS.escape(id) : id;
				return document.querySelector('label[for="' + esc + '"]');
			} catch (_) {
				return null;
			}
		}
		return null;
	}

	function labelTextForSelect(sel) {
		var lab = findLabelForSelect(sel);
		if (lab) {
			return lab.textContent.replace(/\s+/g, ' ').trim();
		}
		var p = sel.closest('label');
		if (p) {
			var clone = p.cloneNode(true);
			var innerSels = clone.querySelectorAll('select');
			for (var i = 0; i < innerSels.length; i++) {
				innerSels[i].remove();
			}
			return clone.textContent.replace(/\s+/g, ' ').trim();
		}
		return '';
	}

	function syncTriggerLabel(wrap) {
		var sel = wrap.querySelector('select.nu-native-select__real');
		var trig = wrap.querySelector('.nu-native-select__trigger');
		if (!sel || !trig) {
			return;
		}
		var opt = sel.options[sel.selectedIndex];
		var t = opt ? String(opt.textContent || '').trim() : '';
		if (!t && opt) {
			t = String(opt.value || '');
		}
		trig.textContent = t || '—';
	}

	function rebuildOptionButtons(wrap) {
		var sel = wrap.querySelector('select.nu-native-select__real');
		var panel = wrap.querySelector('.nu-native-select__list');
		if (!sel || !panel) {
			return;
		}
		panel.innerHTML = '';
		var opts = sel.options;
		for (var i = 0; i < opts.length; i++) {
			(function (idx) {
				var o = opts[idx];
				var btn = document.createElement('button');
				btn.type = 'button';
				btn.className = 'nu-native-select__option';
				btn.setAttribute('role', 'option');
				btn.setAttribute('aria-selected', idx === sel.selectedIndex ? 'true' : 'false');
				btn.textContent = String(o.textContent || '').trim() || String(o.value || '');
				btn.addEventListener('click', function (ev) {
					ev.preventDefault();
					ev.stopPropagation();
					sel.selectedIndex = idx;
					try {
						sel.dispatchEvent(new Event('input', { bubbles: true }));
						sel.dispatchEvent(new Event('change', { bubbles: true }));
					} catch (_) {
						/* ignore */
					}
					Array.prototype.forEach.call(panel.querySelectorAll('.nu-native-select__option'), function (b) {
						b.setAttribute('aria-selected', b === btn ? 'true' : 'false');
					});
					syncTriggerLabel(wrap);
					setOpen(wrap, false, { focusTrigger: true });
				});
				panel.appendChild(btn);
			})(i);
		}
	}

	function setOpen(wrap, open, opts) {
		opts = opts || {};
		var panel = wrap.querySelector('.nu-native-select__list');
		var trig = wrap.querySelector('.nu-native-select__trigger');
		if (!panel || !trig) {
			return;
		}
		if (open) {
			document.querySelectorAll('.nu-native-select.' + OPEN_CLASS).forEach(function (w) {
				if (w !== wrap) {
					setOpen(w, false, {});
				}
			});
			panel.removeAttribute('hidden');
			trig.setAttribute('aria-expanded', 'true');
			wrap.classList.add(OPEN_CLASS);
		} else {
			panel.setAttribute('hidden', 'hidden');
			trig.setAttribute('aria-expanded', 'false');
			wrap.classList.remove(OPEN_CLASS);
			if (opts.focusTrigger) {
				try {
					trig.focus();
				} catch (_) {
					/* ignore */
				}
			}
		}
	}

	function enhanceSelect(sel) {
		if (shouldSkip(sel)) {
			return;
		}
		if (sel.closest('.nu-native-select')) {
			return;
		}

		var wrap = document.createElement('div');
		wrap.className = 'nu-native-select';
		if (sel.classList.contains('lib-page-select')) {
			wrap.classList.add('nu-native-select--lib');
		}
		if (sel.classList.contains('deck-lib-select')) {
			wrap.classList.add('nu-native-select--deck-lib');
		}

		var parent = sel.parentNode;
		parent.insertBefore(wrap, sel);
		wrap.appendChild(sel);

		var wrapLabel = sel.closest('label');
		if (wrapLabel) {
			wrapLabel.classList.add('nu-native-select__label-wrap');
		}

		var trigger = document.createElement('button');
		trigger.type = 'button';
		trigger.className = 'nu-native-select__trigger';
		trigger.setAttribute('aria-haspopup', 'listbox');
		trigger.setAttribute('aria-expanded', 'false');
		var ariaLabel = labelTextForSelect(sel);
		if (ariaLabel) {
			trigger.setAttribute('aria-label', ariaLabel);
		}

		var panel = document.createElement('div');
		panel.className = 'nu-native-select__list';
		panel.setAttribute('role', 'listbox');
		panel.setAttribute('hidden', 'hidden');

		sel.classList.add('nu-native-select__real');
		sel.dataset.nuNativeSelect = 'enhanced';
		sel.setAttribute('tabindex', '-1');
		sel.setAttribute('aria-hidden', 'true');

		wrap.insertBefore(trigger, sel);
		wrap.insertBefore(panel, sel);

		rebuildOptionButtons(wrap);
		syncTriggerLabel(wrap);

		sel.addEventListener('change', function () {
			syncTriggerLabel(wrap);
			rebuildOptionButtons(wrap);
		});

		trigger.addEventListener('click', function (e) {
			e.preventDefault();
			e.stopPropagation();
			var open = wrap.classList.contains(OPEN_CLASS);
			setOpen(wrap, !open, {});
		});

		trigger.addEventListener('keydown', function (e) {
			if (e.key === 'Escape') {
				if (wrap.classList.contains(OPEN_CLASS)) {
					e.preventDefault();
					setOpen(wrap, false, { focusTrigger: true });
				}
				return;
			}
			if (e.key === 'ArrowDown' || e.key === 'Enter' || e.key === ' ') {
				if (!wrap.classList.contains(OPEN_CLASS)) {
					e.preventDefault();
					setOpen(wrap, true);
					var first = panel.querySelector('.nu-native-select__option');
					if (first) {
						try {
							first.focus();
						} catch (_) {
							/* ignore */
						}
					}
				}
			}
		});
	}

	function onDocPointerDown(ev) {
		var t = ev.target;
		if (!(t instanceof Element)) {
			return;
		}
		var clicked = t.closest('.nu-native-select');
		document.querySelectorAll('.nu-native-select.' + OPEN_CLASS).forEach(function (w) {
			if (clicked === w) {
				return;
			}
			setOpen(w, false, {});
		});
	}

	function onDocKeyDown(ev) {
		if (ev.key === 'Escape') {
			document.querySelectorAll('.nu-native-select.' + OPEN_CLASS).forEach(function (w) {
				setOpen(w, false, { focusTrigger: true });
			});
		}
	}

	function init() {
		var sels = document.querySelectorAll('select');
		for (var i = 0; i < sels.length; i++) {
			enhanceSelect(sels[i]);
		}
		if (!document.documentElement.dataset.nuNativeSelectDocBound) {
			document.documentElement.dataset.nuNativeSelectDocBound = '1';
			document.addEventListener('pointerdown', onDocPointerDown, true);
			document.addEventListener('keydown', onDocKeyDown, true);
		}
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	} else {
		init();
	}
})();
