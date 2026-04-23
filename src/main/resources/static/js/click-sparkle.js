(function () {
	'use strict';

	function prefersReducedMotion() {
		return window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
	}

	function playBurst(clientX, clientY) {
		if (prefersReducedMotion()) return;

		var wrap = document.createElement('div');
		wrap.className = 'nu-click-sparkle';
		wrap.setAttribute('aria-hidden', 'true');
		wrap.style.left = clientX + 'px';
		wrap.style.top = clientY + 'px';

		var ringBack = document.createElement('span');
		ringBack.className = 'nu-click-sparkle__ring nu-click-sparkle__ring--back';
		wrap.appendChild(ringBack);

		var ring = document.createElement('span');
		ring.className = 'nu-click-sparkle__ring';
		wrap.appendChild(ring);

		var flashBack = document.createElement('span');
		flashBack.className = 'nu-click-sparkle__flash nu-click-sparkle__flash--back';
		wrap.appendChild(flashBack);

		var flash = document.createElement('span');
		flash.className = 'nu-click-sparkle__flash';
		wrap.appendChild(flash);

		var coreBack = document.createElement('span');
		coreBack.className = 'nu-click-sparkle__core nu-click-sparkle__core--back';
		wrap.appendChild(coreBack);

		var core = document.createElement('span');
		core.className = 'nu-click-sparkle__core';
		wrap.appendChild(core);

		function applyParticleVars(el, spec) {
			el.style.setProperty('--dx', spec.dx);
			el.style.setProperty('--dy', spec.dy);
			el.style.setProperty('--sx', spec.sx);
			el.style.setProperty('--sy', spec.sy);
			el.style.setProperty('--size', spec.size);
			el.style.setProperty('--s0', spec.s0);
			el.style.setProperty('--s1', spec.s1);
			el.style.setProperty('--d', spec.d);
			el.style.setProperty('--t', spec.t);
		}

		var count = 70;
		var specs = [];
		for (var i = 0; i < count; i++) {
			var ang = Math.random() * Math.PI * 2;
			var dist =
				Math.random() < 0.42
					? 0.9 + Math.random() * 9.5
					: 4 + Math.pow(Math.random(), 0.78) * 50;
			var dx = (Math.cos(ang) * dist).toFixed(2) + 'px';
			var dy = (Math.sin(ang) * dist).toFixed(2) + 'px';
			var sx = ((Math.random() - 0.5) * 6.5).toFixed(2) + 'px';
			var sy = ((Math.random() - 0.5) * 6.5).toFixed(2) + 'px';
			var sizeN = 0.72 + Math.pow(Math.random(), 1.92) * 2.45;
			var size = sizeN.toFixed(2) + 'px';
			var s0 = (0.12 + Math.random() * 0.38).toFixed(3);
			var s1 = (0.05 + Math.random() * 0.16).toFixed(3);
			var d = (Math.random() * 45).toFixed(0) + 'ms';
			var t = (220 + Math.random() * 120).toFixed(0) + 'ms';
			var kind = Math.random();
			specs.push({
				dx: dx,
				dy: dy,
				sx: sx,
				sy: sy,
				size: size,
				s0: s0,
				s1: s1,
				d: d,
				t: t,
				kind: kind,
				micro: sizeN < 1.52
			});
		}

		for (var f = 0; f < specs.length; f++) {
			var spec = specs[f];
			var pb = document.createElement('i');
			pb.className = 'nu-click-sparkle__pt nu-click-sparkle__pt--back';
			if (spec.micro) pb.classList.add('nu-click-sparkle__pt--micro');
			applyParticleVars(pb, spec);
			wrap.appendChild(pb);

			var p = document.createElement('i');
			p.className = 'nu-click-sparkle__pt';
			if (spec.micro) p.classList.add('nu-click-sparkle__pt--micro');
			if (spec.kind < 0.2) p.classList.add('nu-click-sparkle__pt--blue');
			else if (spec.kind < 0.38) p.classList.add('nu-click-sparkle__pt--hot');
			if (Math.random() < 0.18) p.classList.add('nu-click-sparkle__pt--dim');
			applyParticleVars(p, spec);
			wrap.appendChild(p);
		}

		document.body.appendChild(wrap);
		window.setTimeout(function () {
			if (wrap.parentNode) wrap.parentNode.removeChild(wrap);
		}, 620);
	}

	document.addEventListener(
		'click',
		function (e) {
			var x = e.clientX;
			var y = e.clientY;
			if (!Number.isFinite(x) || !Number.isFinite(y)) return;
			playBurst(x, y);
		},
		true
	);
})();

/* クリック演出を見せるための遷移遅延：通常リンク 0.3s、ホーム（/home）へは 0.15s、バトル開始系 POST は 0.3s */
(function () {
	'use strict';

	var DELAY_MS = 300;
	var DELAY_HOME_MS = 150;
	var bypassBattleSubmitDelay = false;

	function isHomeTopNavigationUrl(abs) {
		var p = (abs.pathname || '').replace(/\/+$/, '');
		return /\/home$/.test(p);
	}

	function isBattleIntroPostForm(actionUrl) {
		var path = actionUrl.pathname || '';
		if (path.indexOf('/battle/cpu/start') !== -1) {
			return true;
		}
		return /\/battle\/pvp\/invite\/[^/]+\/join\/?$/.test(path);
	}

	document.addEventListener(
		'click',
		function (e) {
			if (e.defaultPrevented) return;
			if (typeof e.isTrusted === 'boolean' && !e.isTrusted) return;
			if (e.button !== 0) return;
			if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;
			if (typeof e.target.closest !== 'function') return;

			var a = e.target.closest('a[href]');
			if (!a) return;
			if (a.hasAttribute('data-nav-no-delay')) return;
			var t = a.getAttribute('target');
			if (t && t !== '' && t.toLowerCase() !== '_self') return;
			if (a.hasAttribute('download')) return;

			var raw = a.getAttribute('href');
			if (!raw || raw.trim() === '') return;
			var rawTrim = raw.trim();
			if (rawTrim.toLowerCase().indexOf('javascript:') === 0) return;
			if (rawTrim.charAt(0) === '#' && rawTrim.indexOf('/') === -1) return;

			var abs;
			try {
				abs = new URL(a.href, window.location.href);
			} catch (err) {
				return;
			}
			if (abs.protocol !== 'http:' && abs.protocol !== 'https:') return;

			e.preventDefault();
			var dest = abs.href;
			var delayMs = isHomeTopNavigationUrl(abs) ? DELAY_HOME_MS : DELAY_MS;
			window.setTimeout(function () {
				window.location.assign(dest);
			}, delayMs);
		},
		true
	);

	document.addEventListener(
		'submit',
		function (e) {
			if (bypassBattleSubmitDelay) return;
			if (e.defaultPrevented) return;
			var form = e.target;
			if (!form || form.nodeName !== 'FORM') return;
			var method = (form.getAttribute('method') || 'get').toLowerCase();
			if (method !== 'post') return;

			var actionUrl;
			try {
				actionUrl = new URL(form.action, window.location.href);
			} catch (err2) {
				return;
			}
			if (actionUrl.protocol !== 'http:' && actionUrl.protocol !== 'https:') return;
			if (!isBattleIntroPostForm(actionUrl)) return;
			if (form.hasAttribute('data-nav-no-delay')) return;

			e.preventDefault();
			var sub = e.submitter;
			window.setTimeout(function () {
				bypassBattleSubmitDelay = true;
				try {
					if (typeof form.requestSubmit === 'function') {
						form.requestSubmit(sub || undefined);
					} else {
						form.submit();
					}
				} catch (err3) {
					try {
						form.submit();
					} catch (err4) {
						/* ignore */
					}
				} finally {
					bypassBattleSubmitDelay = false;
				}
			}, DELAY_MS);
		},
		true
	);
})();
