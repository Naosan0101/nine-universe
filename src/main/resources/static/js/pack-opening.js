(function () {
	const row = document.getElementById('pack-opening-row');
	if (!row) return;

	const cards = Array.from(document.querySelectorAll('.pack-opening-card[data-pack-index]'));
	if (cards.length === 0) return;

	const afterOpenUrl =
		(document.body && document.body.getAttribute('data-pack-opening-after-url')) || '/pack/result';

	/** hidden のまま計測されると幅0でカード名フィットが失敗するため、表出後に再計測する */
	function refitCardNameAfterReveal(faceRootEl) {
		if (!faceRootEl) return;
		var cf = faceRootEl.querySelector('.card-face.card-face--layered');
		if (!cf) return;
		var nameEl = cf.querySelector('.card-face__name');
		if (nameEl && typeof window.resetCardFaceNameFitInline === 'function') {
			window.resetCardFaceNameFitInline(nameEl);
		}
		if (typeof window.fitCardFaceNameToOneLine !== 'function') return;
		function runFit() {
			try {
				window.fitCardFaceNameToOneLine(cf);
			} catch (e) {
				// noop
			}
		}
		requestAnimationFrame(function () {
			requestAnimationFrame(function () {
				runFit();
				setTimeout(runFit, 80);
			});
		});
	}

	const revealAllBtn = document.getElementById('pack-opening-reveal-all');

	let active = 0;
	let revealed = 0;

	function updateRevealAllEnabled() {
		if (!revealAllBtn) return;
		revealAllBtn.disabled = cards.every(function (c) {
			return c.classList.contains('is-revealed');
		});
	}

	function setActive(i) {
		active = i;
		cards.forEach(function (c, idx) {
			c.classList.toggle('is-active', idx === i);
		});
	}

	function shake() {
		document.body.classList.add('pack-shake');
		setTimeout(function () {
			document.body.classList.remove('pack-shake');
		}, 1000);
	}

	function revealCard(btn) {
		if (!btn || btn.classList.contains('is-revealed')) {
			updateRevealAllEnabled();
			return;
		}
		btn.classList.add('is-revealed');

		const face = btn.querySelector('.pack-opening-card__face');
		const back = btn.querySelector('.pack-opening-card__back');
		const spark = btn.querySelector('.pack-opening-card__spark');
		const arrow = btn.querySelector('.pack-opening-card__arrow');
		const rarity = (btn.dataset.rarity || 'C').trim();

		if (back) back.classList.add('is-flipped');
		if (face) face.hidden = false;
		refitCardNameAfterReveal(face);
		if (arrow) arrow.hidden = true;
		if (spark && typeof fillPackRevealBurstSpark === 'function') {
			fillPackRevealBurstSpark(spark, rarity);
		} else if (spark && typeof fillContinuousCardSpark === 'function') {
			fillContinuousCardSpark(spark, rarity);
		}

		// ライブラリ同様、カード面内のキラ（R/Ep/Reg は常時）
		const faceSpark = face ? face.querySelector('.card-face .card-spark') : null;
		if (faceSpark && typeof fillContinuousCardSpark === 'function') {
			fillContinuousCardSpark(faceSpark, rarity);
		}

		if (rarity === 'Reg') {
			shake();
		}

		revealed++;
		updateRevealAllEnabled();
		if (revealed >= cards.length) {
			setTimeout(function () {
				window.location.href = afterOpenUrl;
			}, 2000);
			return;
		}
		setActive(Math.min(cards.length - 1, active + 1));
	}

	setTimeout(function () {
		row.classList.add('is-dealt');
	}, 50);

	setActive(0);
	updateRevealAllEnabled();
	cards.forEach(function (btn) {
		btn.addEventListener('click', function () {
			const idx = parseInt(btn.dataset.packIndex, 10);
			if (idx !== active) return;
			revealCard(btn);
		});
	});
	if (revealAllBtn) {
		revealAllBtn.addEventListener('click', function () {
			cards.forEach(function (card) {
				revealCard(card);
			});
		});
	}
})();
