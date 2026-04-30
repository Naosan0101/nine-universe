(function () {
	const contextPath = document.querySelector('meta[name="nine_universe_context_path"]')?.getAttribute('content') || '';
	const plateFbFull = document.querySelector('meta[name="card_plate_fallback"]')?.getAttribute('content') || '';
	const dataFbFull = document.querySelector('meta[name="card_data_fallback"]')?.getAttribute('content') || '';

	function staticUrl(path) {
		if (path == null || path === '') {
			return '';
		}
		const p = String(path);
		if (p.startsWith('http://') || p.startsWith('https://')) {
			return p;
		}
		return contextPath + p;
	}

	function buildMiniLayered(c) {
		if (typeof buildLibraryCardFace !== 'function') {
			const ph = document.createElement('div');
			ph.className = 'mini-card__no-art';
			ph.textContent = (c.name || '?').slice(0, 1);
			return ph;
		}
		const face = buildLibraryCardFace(
			{
				layerBase: c.layerBase,
				layerPortrait: c.layerPortrait,
				layerPortraitAlt: c.layerPortraitAlt,
				layerBar: c.layerBar,
				layerFrame: c.layerFrame,
				attribute: c.attribute,
				rarity: c.rarity,
				rarityLabel: c.rarityLabel || c.rarity || 'C',
				cost: c.cost,
				power: c.power,
				fieldCard: c.fieldCard,
				name: c.name,
				attrLines: c.attrLines || [],
				attributeLabelJa: c.attributeLabelJa,
				ability: c.ability
			},
			{
				contextPath: contextPath,
				plateFallback: plateFbFull,
				dataFallback: dataFbFull,
				extraRootClasses: 'card-face--mini-deck'
			}
		);
		if (typeof wireLibraryCardFaceImages === 'function') {
			wireLibraryCardFaceImages(face, plateFbFull, dataFbFull);
		}
		if (typeof applyLibraryCardFaceSpark === 'function') {
			applyLibraryCardFaceSpark(face, c.rarity);
		}
		const inner = document.createElement('div');
		inner.className = 'library-card__inner';
		inner.appendChild(face);
		return inner;
	}

	function refitMiniCardFaceName(hostEl) {
		const face = hostEl.querySelector('.card-face.card-face--layered');
		if (!face || typeof window.fitCardFaceNameToOneLine !== 'function') {
			return;
		}
		const nameEl = face.querySelector('.card-face__name');
		if (nameEl && typeof window.resetCardFaceNameFitInline === 'function') {
			window.resetCardFaceNameFitInline(nameEl);
		}
		requestAnimationFrame(function () {
			requestAnimationFrame(function () {
				try {
					window.fitCardFaceNameToOneLine(face);
				} catch (e) {
					// noop
				}
			});
		});
	}

	function mapJsonToCard(j) {
		return {
			name: j.name,
			layerBase: j.layerBase,
			layerPortrait: j.layerPortrait,
			layerPortraitAlt: j.layerPortraitAlt || '',
			layerBar: j.layerBar,
			layerFrame: j.layerFrame,
			attribute: j.attribute,
			rarity: j.rarity,
			rarityLabel: j.rarityLabel,
			cost: j.cost,
			power: j.power,
			fieldCard: j.fieldCard === true,
			attributeLabelJa: j.attributeLabelJa,
			ability: j.ability,
			attrLines: j.attrLines || []
		};
	}

	function fillPreviewPanel(panel, cards) {
		panel.innerHTML = '';
		const strip = document.createElement('div');
		strip.className = 'deck-list-preview__strip';
		cards.forEach(function (j) {
			const c = mapJsonToCard(j);
			const cell = document.createElement('div');
			cell.className = 'deck-list-preview__cell';
			cell.appendChild(buildMiniLayered(c));
			strip.appendChild(cell);
			refitMiniCardFaceName(cell);
		});
		panel.appendChild(strip);
	}

	document.querySelectorAll('[data-deck-preview-toggle]').forEach(function (btn) {
		btn.addEventListener('click', function () {
			const li = btn.closest('.deck-row--card');
			if (!li) {
				return;
			}
			const panel = li.querySelector('.deck-row__preview');
			if (!panel) {
				return;
			}
			const deckId = btn.getAttribute('data-deck-id');
			const open = panel.hidden === false;
			if (open) {
				panel.hidden = true;
				btn.setAttribute('aria-expanded', 'false');
				btn.textContent = '▼';
				return;
			}
			function showStrip() {
				panel.hidden = false;
				btn.setAttribute('aria-expanded', 'true');
				btn.textContent = '▲';
			}
			if (panel.dataset.loaded === 'true') {
				showStrip();
				return;
			}
			btn.disabled = true;
			fetch(staticUrl('/decks/' + deckId + '/preview'), {
				headers: { Accept: 'application/json' },
				credentials: 'same-origin'
			})
				.then(function (r) {
					if (!r.ok) {
						throw new Error('読み込みに失敗しました');
					}
					return r.json();
				})
				.then(function (cards) {
					fillPreviewPanel(panel, cards);
					panel.dataset.loaded = 'true';
					showStrip();
				})
				.catch(function () {
					panel.innerHTML = '';
					const err = document.createElement('p');
					err.className = 'muted deck-list-preview__err';
					err.textContent = 'デッキ内容を表示できませんでした。';
					panel.appendChild(err);
					panel.dataset.loaded = 'true';
					showStrip();
				})
				.finally(function () {
					btn.disabled = false;
				});
		});
	});
})();
