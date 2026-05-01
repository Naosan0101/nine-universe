/**
 * クリック中だけカーソル画像を左へ30°回転（Canvas の data URL を適用）。
 *
 * html だけ更新しても、button/a 等は cursor:inherit で body の明示的な cursor が
 * 使われるため回転が見えない／解除時に一瞬だけ不整合になることがある。
 * そのため html と body の両方に同じ cursor をセットする。
 *
 * 解除時は removeProperty せず通常画像へ setProperty する（取り除くと1フレーム既定カーソルが挟まる）。
 */
(function () {
	'use strict';

	var href = typeof window.__NU_CURSOR_ICON_HREF === 'string' ? window.__NU_CURSOR_ICON_HREF.trim() : '';
	if (!href) {
		return;
	}

	function escapeForCssUrl(u) {
		return String(u || '')
			.replace(/\\/g, '/')
			.replace(/"/g, '\\"');
	}

	var normalCursorCss = 'url("' + escapeForCssUrl(href) + '") 0 0, auto';

	var clickCursorCss = null;
	var down = false;

	function setRootCursor(css) {
		var el = document.documentElement;
		var b = document.body;
		try {
			el.style.setProperty('cursor', css, 'important');
			if (b) {
				b.style.setProperty('cursor', css, 'important');
			}
		} catch (_) {
			try {
				el.style.cursor = css;
				if (b) {
					b.style.cursor = css;
				}
			} catch (_) {
				/* ignore */
			}
		}
	}

	function clearClickCursor() {
		if (!down) {
			return;
		}
		down = false;
		setRootCursor(normalCursorCss);
	}

	function applyClickCursor() {
		if (!clickCursorCss) {
			return;
		}
		down = true;
		setRootCursor(clickCursorCss);
	}

	function isTextEditingTarget(t) {
		if (!(t instanceof Element)) {
			return false;
		}
		var tag = (t.tagName || '').toUpperCase();
		if (tag === 'TEXTAREA') {
			return true;
		}
		if (tag === 'INPUT') {
			var ty = String(t.type || 'text').toLowerCase();
			return (
				ty === 'text' ||
				ty === 'search' ||
				ty === 'password' ||
				ty === 'email' ||
				ty === 'url' ||
				ty === 'tel' ||
				ty === 'number'
			);
		}
		if (t.isContentEditable) {
			return true;
		}
		return false;
	}

	function onPointerDown(e) {
		if (!clickCursorCss) {
			return;
		}
		if (e.pointerType === 'mouse' && e.button !== 0) {
			return;
		}
		if (e.pointerType !== 'mouse' && e.pointerType !== 'pen' && e.pointerType !== 'touch') {
			return;
		}
		if (isTextEditingTarget(e.target)) {
			return;
		}
		applyClickCursor();
	}

	function buildRotatedCursorUrl(done) {
		var img = new Image();
		img.onload = function () {
			try {
				var w = img.naturalWidth || img.width || 32;
				var h = img.naturalHeight || img.height || 32;
				var rad = (-30 * Math.PI) / 180;
				var size = Math.max(32, Math.ceil(Math.hypot(w, h) * 1.35));
				var c = document.createElement('canvas');
				c.width = size;
				c.height = size;
				var ctx = c.getContext('2d');
				if (!ctx) {
					done(false);
					return;
				}
				ctx.translate(size / 2, size / 2);
				ctx.rotate(rad);
				ctx.drawImage(img, -w / 2, -h / 2);
				var lx = -w / 2;
				var ly = -h / 2;
				var rx = lx * Math.cos(rad) - ly * Math.sin(rad);
				var ry = lx * Math.sin(rad) + ly * Math.cos(rad);
				var hx = Math.round(size / 2 + rx);
				var hy = Math.round(size / 2 + ry);
				var dataUrl = c.toDataURL('image/png');
				clickCursorCss = 'url("' + escapeForCssUrl(dataUrl) + '") ' + hx + ' ' + hy + ', auto';
				done(true);
			} catch (_) {
				done(false);
			}
		};
		img.onerror = function () {
			done(false);
		};
		img.src = href;
	}

	buildRotatedCursorUrl(function (ok) {
		if (!ok) {
			return;
		}
		window.addEventListener('pointerdown', onPointerDown, true);
		window.addEventListener('pointerup', clearClickCursor, true);
		window.addEventListener('pointercancel', clearClickCursor, true);
		window.addEventListener('blur', clearClickCursor, true);
	});
})();
