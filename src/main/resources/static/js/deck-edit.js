(function () {
	const contextPath = document.querySelector('meta[name="nine_universe_context_path"]')?.getAttribute('content') || '';
	const plateFbFull = document.querySelector('meta[name="card_plate_fallback"]')?.getAttribute('content') || '';
	const dataFbFull = document.querySelector('meta[name="card_data_fallback"]')?.getAttribute('content') || '';
	function staticUrl(path) {
		if (path == null || path === '') return '';
		const p = String(path);
		if (p.startsWith('http://') || p.startsWith('https://')) return p;
		return contextPath + p;
	}

	const libZone = document.getElementById('lib-zone');
	const deckZone = document.getElementById('deck-zone');
	const deckCount = document.getElementById('deck-count');
	const completeBtn = document.getElementById('complete-btn');
	const clearAllDeckBtn = document.getElementById('deck-clear-all-btn');
	const cardIdsInput = document.getElementById('cardIds');
	const detailModal = document.getElementById('library-detail-modal');
	const detailArtWrap = document.getElementById('library-detail-art');
	const modalLayerBase = document.getElementById('lib-modal-layer-base');
	const modalLayerPortrait = document.getElementById('lib-modal-layer-portrait');
	const modalLayerBar = document.getElementById('lib-modal-layer-bar');
	const modalLayerFrame = document.getElementById('lib-modal-layer-frame');
	const modalCost = document.getElementById('lib-modal-cost');
	const modalPower = document.getElementById('lib-modal-power');
	const modalName = document.getElementById('lib-modal-name');
	const modalAttr = document.getElementById('lib-modal-attr');
	const modalRarity = document.getElementById('lib-modal-rarity');
	const modalAbility = document.getElementById('lib-modal-ability');
	const sideTitle = document.getElementById('lib-modal-side-title');
	const sideAttr = document.getElementById('lib-modal-side-attr');
	const sideCost = document.getElementById('lib-modal-side-cost');
	const sidePower = document.getElementById('lib-modal-side-power');
	const sideRarity = document.getElementById('lib-modal-side-rarity');
	const sidePack = document.getElementById('lib-modal-side-pack');
	const sideAbility = document.getElementById('lib-modal-side-ability');
	const libSearch = document.getElementById('lib-search');
	const libSort = document.getElementById('lib-sort');
	const libFilterAttr = document.getElementById('lib-filter-attr');
	const libFilterPower = document.getElementById('lib-filter-power');
	const libFilterCost = document.getElementById('lib-filter-cost');
	const libFilterRarity = document.getElementById('lib-filter-rarity');
	const libFilterPack = document.getElementById('lib-filter-pack');
	const libFilterCardKind = document.getElementById('lib-filter-card-kind');
	const tooltipEl = document.getElementById('deck-tooltip');
	const tooltipName = tooltipEl.querySelector('.deck-tooltip__name');
	const tooltipAttr = tooltipEl.querySelector('.deck-tooltip__attr');
	const tooltipCost = tooltipEl.querySelector('.deck-tooltip__cost');
	const tooltipPower = tooltipEl.querySelector('.deck-tooltip__power');
	const tooltipAbility = tooltipEl.querySelector('.deck-tooltip__ability');

	const ATTR_LABEL = {
		HUMAN: '人間',
		ELF: 'エルフ',
		UNDEAD: 'アンデッド',
		DRAGON: 'ドラゴン',
		MACHINE: 'マシン',
		CARBUNCLE: 'カーバンクル'
	};
	/** 上段カードの種族ソート順（種族フィルターと同じ並び） */
	const TRIBE_SORT_ORDER = ['HUMAN', 'ELF', 'UNDEAD', 'DRAGON', 'MACHINE', 'CARBUNCLE'];

	function tribeSortIndex(segment) {
		const u = (segment || '').trim().toUpperCase();
		const i = TRIBE_SORT_ORDER.indexOf(u);
		return i >= 0 ? i : 99;
	}

	/** 複合種族（A_B）は各コードの順位を並べたキーで比較 */
	function tribeSortKey(attr) {
		const raw = (attr || '').trim();
		if (!raw) return '99';
		const parts = raw
			.split('_')
			.map(function (s) {
				return s.trim();
			})
			.filter(Boolean);
		if (parts.length === 0) return '99';
		const idx = parts.map(tribeSortIndex).sort(function (a, b) {
			return a - b;
		});
		return idx
			.map(function (n) {
				return n.toString().padStart(2, '0');
			})
			.join(',');
	}

	/** デッキ上段：種族順 → ID 順 */
	function sortUpperZoneByTribeThenId(zone) {
		if (!zone) return;
		const nodes = Array.from(zone.querySelectorAll(':scope > .mini-card'));
		if (nodes.length <= 1) return;
		function cardFor(node) {
			const id = parseInt(node.dataset.id, 10);
			if (isNaN(id)) return null;
			return seeds.find(function (s) {
				return s.id === id;
			});
		}
		nodes.sort(function (a, b) {
			const ca = cardFor(a);
			const cb = cardFor(b);
			const ka = tribeSortKey(ca && ca.attribute ? ca.attribute : '');
			const kb = tribeSortKey(cb && cb.attribute ? cb.attribute : '');
			if (ka !== kb) {
				return ka < kb ? -1 : ka > kb ? 1 : 0;
			}
			const ida = parseInt(a.dataset.id, 10) || 0;
			const idb = parseInt(b.dataset.id, 10) || 0;
			return ida - idb;
		});
		nodes.forEach(function (n) {
			zone.appendChild(n);
		});
	}
	const RARITY_LABEL = { Reg: 'レジェンダリー', Ep: 'エピック', R: 'レア', C: 'コモン' };
	const PACK_JA = {
		STD: 'スタンダードパック1',
		STD2: 'スタンダードパック2',
		WH: '風吹く丘パック',
		ET: '邪悪なる脅威パック',
		JU: '宝石の秘境パック',
		IF: '鉄面の艦隊パック'
	};

	function packSourcesForInitial(piRaw) {
		const pi = (piRaw || 'STD').trim().toUpperCase() || 'STD';
		if (pi === 'WH') return [PACK_JA.WH, PACK_JA.STD];
		if (pi === 'ET') return [PACK_JA.ET, PACK_JA.STD];
		if (pi === 'JU') return [PACK_JA.JU, PACK_JA.STD2];
		if (pi === 'IF') return [PACK_JA.IF, PACK_JA.STD2];
		return [PACK_JA.STD];
	}

	function hideBrokenImg(img) {
		if (!img) return;
		img.setAttribute('hidden', '');
		img.removeAttribute('src');
	}

	function applyOnceImgFallback(img, fallbackSrc) {
		if (!img) return;
		if (img.dataset && img.dataset.fallbackWired === 'true') return;
		if (img.dataset) img.dataset.fallbackWired = 'true';
		function handleError() {
			if (fallbackSrc && img.dataset && img.dataset.fallbackTried !== 'true') {
				img.dataset.fallbackTried = 'true';
				img.src = fallbackSrc;
				return;
			}
			hideBrokenImg(img);
		}
		img.addEventListener('error', handleError);
		function crushIfAlreadyBroken() {
			try {
				if (img.complete && img.naturalWidth === 0) {
					handleError();
				}
			} catch (e) {
				// noop
			}
		}
		crushIfAlreadyBroken();
		setTimeout(crushIfAlreadyBroken, 0);
		setTimeout(crushIfAlreadyBroken, 250);
	}

	function buildAbilityBlocksFromCanonical(line) {
		if (!line) {
			return [{ h: '', b: '効果なし。' }];
		}
		let s = line.startsWith('・') ? line.slice(1) : line;
		if (s.indexOf('/効果なし。') !== -1 || s.indexOf('/能力なし。') !== -1) {
			return [{ h: '', b: '効果なし。' }];
		}
		let idx = s.indexOf('/フィールド：');
		if (idx >= 0) {
			return [{ h: '〈フィールド〉', b: s.slice(idx + '/フィールド：'.length) }];
		}
		idx = s.indexOf('/フィールド:');
		if (idx >= 0) {
			return [{ h: '〈フィールド〉', b: s.slice(idx + '/フィールド:'.length) }];
		}
		idx = s.indexOf('/配置：');
		if (idx >= 0) {
			return [{ h: '〈配置〉', b: s.slice(idx + '/配置：'.length) }];
		}
		idx = s.indexOf('/配置:');
		if (idx >= 0) {
			return [{ h: '〈配置〉', b: s.slice(idx + '/配置:'.length) }];
		}
		idx = s.indexOf('/常時：');
		if (idx >= 0) {
			return [{ h: '〈常時〉', b: s.slice(idx + '/常時：'.length) }];
		}
		idx = s.indexOf('/常時:');
		if (idx >= 0) {
			return [{ h: '〈常時〉', b: s.slice(idx + '/常時:'.length) }];
		}
		return [{ h: '', b: s }];
	}

	function rarityLabelJa(code, label) {
		const l = (label || '').trim();
		if (
			l === 'レジェンダリー' ||
			l === 'エピック' ||
			l === 'レア' ||
			l === 'コモン'
		) {
			return l;
		}
		const c = (code || 'C').trim();
		return RARITY_LABEL[c] || 'コモン';
	}

	function rarityCode4(code) {
		const c = (code || 'C').trim();
		if (c === 'Reg' || c === 'Ep' || c === 'R' || c === 'C') return c;
		return 'C';
	}

	function closeCardDetailModal() {
		hideTooltip();
		if (!detailModal) return;
		const modalSpark = document.getElementById('lib-modal-spark');
		if (modalSpark) {
			modalSpark.hidden = true;
			modalSpark.classList.remove('is-on', 'card-spark--continuous', 'spark--R', 'spark--Ep', 'spark--Reg');
			modalSpark.textContent = '';
		}
		detailModal.hidden = true;
		document.body.style.overflow = '';
	}

	function openCardDetailModal(c) {
		hideTooltip();
		if (!detailModal || !modalCost || !modalAbility) return;

		const rarity = rarityCode4(c.rarity);
		const rarityLabel = rarityLabelJa(rarity, c.rarityLabel || rarity || 'C');
		const modalFaceRoot = document.getElementById('library-modal-card-face');
		const modalSpark = document.getElementById('lib-modal-spark');

		if (modalFaceRoot) {
			modalFaceRoot.classList.remove('card-face--rarity-C', 'card-face--rarity-R', 'card-face--rarity-Ep', 'card-face--rarity-Reg');
			modalFaceRoot.classList.add('card-face--rarity-' + rarity);
			if (typeof window.syncCardFaceAttrClass === 'function') {
				window.syncCardFaceAttrClass(modalFaceRoot, c.attribute);
			}
		}
		if (modalRarity) {
			// カード面の表示はコード（Reg/Ep/R/C）
			modalRarity.textContent = rarity;
		}
		if (sideRarity) {
			sideRarity.textContent = rarityLabel;
		}
		if (sidePack) {
			sidePack.textContent = packSourcesForInitial(c.packInitial).join('\n');
		}

		if (modalCost) {
			modalCost.textContent = c.cost != null && c.cost !== '' ? String(c.cost) : '';
			const cn = parseInt(c.cost, 10);
			modalCost.className = 'card-face__cost';
			if (cn === 1) modalCost.classList.add('card-face__cost--digit-1');
			if (cn === 2) modalCost.classList.add('card-face__cost--digit-2');
		}
		if (modalPower) {
			if (c.fieldCard) {
				modalPower.textContent = '';
				modalPower.className = 'card-face__power card-face__power--hidden';
			} else {
				modalPower.textContent = c.power != null && c.power !== '' ? String(c.power) : '';
				const pn = parseInt(c.power, 10);
				modalPower.className = 'card-face__power';
				if (pn === 4) modalPower.classList.add('card-face__power--digit-4');
			}
		}
		if (modalName) {
			modalName.textContent = c.name || '';
			if (typeof window.resetCardFaceNameFitInline === 'function') {
				window.resetCardFaceNameFitInline(modalName);
			}
		}
		if (sideTitle) sideTitle.textContent = c.name || '';
		if (sideCost) sideCost.textContent = c.cost != null && c.cost !== '' ? String(c.cost) : '—';
		if (sidePower) {
			sidePower.textContent = c.fieldCard
				? '—'
				: c.power != null && c.power !== ''
					? String(c.power)
					: '—';
		}

		if (modalAttr) {
			const pipe = (c.attrPipe || '').trim();
			let lines = pipe ? pipe.split('|').filter(Boolean) : [];
			if (lines.length <= 1) {
				const code = (c.attribute || '').trim();
				if (code.indexOf('_') !== -1) {
					lines = code.split('_').map(function (seg) {
						return ATTR_LABEL[seg] || seg;
					}).filter(Boolean);
				}
			}
			if (lines.length > 1) {
				modalAttr.className = 'card-face__attr-label card-face__attr-label--compound';
				modalAttr.innerHTML = '';
				lines.forEach(function (ln) {
					const s = document.createElement('span');
					s.className = 'card-face__attr-line';
					s.textContent = ln;
					modalAttr.appendChild(s);
				});
			} else {
				modalAttr.className = 'card-face__attr-label';
				modalAttr.textContent = c.attributeLabelJa || ATTR_LABEL[c.attribute] || c.attribute || '';
			}
		}
		if (sideAttr) {
			sideAttr.textContent = deckEditTooltipAttribute(c);
		}

		modalAbility.innerHTML = '';
		const blocks = buildAbilityBlocksFromCanonical(c.canonicalLine);
		blocks.forEach(function (bl) {
			if (bl.h) {
				const ph = document.createElement('p');
				ph.className = 'card-face__ability-head';
				ph.textContent = bl.h;
				modalAbility.appendChild(ph);
			}
			const pb = document.createElement('p');
			pb.className = 'card-face__ability-body';
			pb.textContent = bl.b;
			modalAbility.appendChild(pb);
		});
		if (sideAbility) {
			const t = blocks.map(function (b) {
				return b.h ? (b.h + '\n' + b.b) : b.b;
			}).join('\n\n');
			sideAbility.textContent = t || '—';
		}

		if (detailArtWrap) {
			detailArtWrap.classList.remove('library-detail-modal__art-wrap--locked');
		}

		if (modalLayerBase) {
			applyOnceImgFallback(modalLayerBase, plateFbFull);
			modalLayerBase.removeAttribute('hidden');
			modalLayerBase.src = staticUrl(c.layerBase) || plateFbFull;
		}
		if (modalLayerPortrait) {
			applyOnceImgFallback(modalLayerPortrait, '');
			const pu = staticUrl(c.layerPortrait);
			if (pu) {
				modalLayerPortrait.removeAttribute('hidden');
				modalLayerPortrait.src = pu;
			} else {
				hideBrokenImg(modalLayerPortrait);
			}
		}
		if (modalLayerBar) {
			applyOnceImgFallback(modalLayerBar, '');
			const bu = staticUrl(c.layerBar);
			if (bu) {
				modalLayerBar.removeAttribute('hidden');
				modalLayerBar.src = bu;
			} else {
				hideBrokenImg(modalLayerBar);
			}
		}
		if (modalLayerFrame) {
			applyOnceImgFallback(modalLayerFrame, dataFbFull);
			modalLayerFrame.removeAttribute('hidden');
			modalLayerFrame.src = staticUrl(c.layerFrame) || dataFbFull;
		}

		detailModal.hidden = false;
		document.body.style.overflow = 'hidden';

		// モーダルのカード名が2行になる場合は1行に収める
		if (modalFaceRoot && typeof window.fitCardFaceNameToOneLine === 'function') {
			setTimeout(function () {
				try {
					window.fitCardFaceNameToOneLine(modalFaceRoot);
				} catch (e) {
					// noop
				}
			}, 0);
		}

		if (modalSpark && typeof fillContinuousCardSpark === 'function') {
			fillContinuousCardSpark(modalSpark, rarity);
		}
	}


	function matchesTribeFilter(cardAttr, filterVal) {
		if (!filterVal) return true;
		if (!cardAttr) return false;
		if (cardAttr === filterVal) return true;
		return cardAttr.split('_').indexOf(filterVal) !== -1;
	}

	// PackService.filterCardsForPack と同じ収録イニシャル
	var PACK_IDS_STANDARD_1 = ['STD', 'WH', 'ET'];
	var PACK_IDS_STANDARD_2 = ['JU', 'IF'];

	function matchesPackFilter(pi, filterVal) {
		if (!filterVal) return true;
		var n = (pi || 'STD').trim().toUpperCase() || 'STD';
		if (filterVal === 'STANDARD_1') return PACK_IDS_STANDARD_1.indexOf(n) !== -1;
		if (filterVal === 'STANDARD_2') return PACK_IDS_STANDARD_2.indexOf(n) !== -1;
		return n === filterVal;
	}

	function attributeLabelJa(code, preset) {
		if (preset) return preset;
		if (!code) return '';
		if (code.indexOf('_') !== -1) {
			return code.split('_').map(function (seg) {
				return ATTR_LABEL[seg] || seg;
			}).join(' ');
		}
		return ATTR_LABEL[code] || code;
	}

	/** ホバー詳細: 複合種族は「エルフ/アンデッド」形式で一行 */
	function deckEditTooltipAttribute(c) {
		if (c.attrLines && c.attrLines.length >= 2) {
			return c.attrLines.join('/');
		}
		if (c.attrLines && c.attrLines.length === 1) {
			return c.attrLines[0];
		}
		const code = c.attribute || '';
		if (code.indexOf('_') !== -1) {
			return code.split('_').map(function (seg) {
				return ATTR_LABEL[seg] || seg;
			}).join('/');
		}
		return attributeLabelJa(c.attribute, c.attributeLabelJa) || '—';
	}

	function deckEditTooltipAttributeIsCompound(c) {
		if (c.attrLines && c.attrLines.length >= 2) return true;
		const code = c.attribute || '';
		return code.indexOf('_') !== -1;
	}

	/** 上段デッキ内カード：左クリックで1枚ライブラリへ戻す（Enter / Space も可） */
	function bindDeckSlotRemove(copy, removeHandler) {
		copy.addEventListener('click', function (e) {
			if (e.button !== 0) return;
			removeHandler();
		});
		copy.addEventListener('keydown', function (ev) {
			if (ev.key !== 'Enter' && ev.key !== ' ') return;
			ev.preventDefault();
			removeHandler();
		});
	}

	const seeds = Array.from(document.querySelectorAll('#lib-seed .seed')).map(function (el) {
		const p = parseInt(el.dataset.power, 10);
		const cost = parseInt(el.dataset.cost, 10);
		return {
			id: parseInt(el.dataset.id, 10),
			img: staticUrl(el.dataset.img || ''),
			layerBase: el.dataset.layerBase || '',
			layerPortrait: el.dataset.layerPortrait || '',
			layerPortraitAlt: el.dataset.layerPortraitAlt || '',
			layerBar: el.dataset.layerBar || '',
			layerFrame: el.dataset.layerFrame || '',
			rarity: (el.dataset.rarity || 'C').trim(),
			rarityLabel: (el.dataset.rarityLabel || '').trim(),
			qty: parseInt(el.dataset.qty, 10) || 0,
			name: el.dataset.name || '',
			attribute: el.dataset.attribute || '',
			attributeLabelJa: el.dataset.attributeLabel || '',
			attrLines: (function () {
				const p = el.dataset.attrPipe || '';
				return p ? p.split('|').filter(Boolean) : [];
			})(),
			power: isNaN(p) ? 0 : p,
			fieldCard: el.dataset.fieldCard === 'true',
			cost: isNaN(cost) ? 0 : cost,
			ability: el.dataset.ability || '',
			canonicalLine: el.dataset.canonicalLine || '',
			deployHelp: el.dataset.deployHelp || '',
			passiveHelp: el.dataset.passiveHelp || '',
			attrPipe: el.dataset.attrPipe || '',
			packInitial: (function () {
				const v = (el.dataset.packInitial || 'STD').trim();
				const u = v.toUpperCase();
				return u || 'STD';
			})()
		};
	}).filter(function (c) { return c.qty > 0 && !isNaN(c.id); });

	const selectedSpans = document.querySelectorAll('#selected-seed span');
	const initialDeck = Array.from(selectedSpans).map(function (s) { return parseInt(s.textContent.trim(), 10); })
		.filter(function (n) { return !isNaN(n); });

	function countInDeck(id) {
		return Array.from(deckZone.querySelectorAll('.mini-card')).filter(function (n) {
			return parseInt(n.dataset.id, 10) === id;
		}).length;
	}

	function canAddToDeck(id, maxPerCard) {
		if (deckZone.querySelectorAll('.mini-card').length >= 8) return false;
		return countInDeck(id) < maxPerCard;
	}

	function maxPerForId(id) {
		const row = seeds.find(function (s) { return s.id === id; });
		const owned = row ? row.qty : 0;
		return Math.min(2, owned);
	}

	function cmpPower(a, b) {
		return a.power - b.power;
	}

	function cmpCost(a, b) {
		return a.cost - b.cost;
	}

	function cmpName(a, b) {
		return a.name.localeCompare(b.name, 'ja');
	}

	function matchesCardTextSearch(q, parts) {
		if (!q) return true;
		for (let i = 0; i < parts.length; i++) {
			const s = parts[i];
			if (s != null && s !== '' && String(s).indexOf(q) !== -1) return true;
		}
		return false;
	}

	function sortedLibraryList() {
		const q = libSearch ? libSearch.value.trim() : '';
		const attrF = libFilterAttr ? libFilterAttr.value : '';
		const powF = libFilterPower ? libFilterPower.value : '';
		const costF = libFilterCost ? libFilterCost.value : '';
		const rarF = libFilterRarity ? libFilterRarity.value : '';
		const packF = libFilterPack ? libFilterPack.value : '';
		const kindF = libFilterCardKind ? libFilterCardKind.value : '';
		let list = seeds.filter(function (c) {
			if (
				!matchesCardTextSearch(q, [
					c.name,
					c.ability,
					c.canonicalLine,
					c.deployHelp,
					c.passiveHelp
				])
			) {
				return false;
			}
			if (attrF && !matchesTribeFilter(c.attribute, attrF)) return false;
			if (kindF === 'fighter' && c.fieldCard) return false;
			if (kindF === 'field' && !c.fieldCard) return false;
			if (packF && !matchesPackFilter(c.packInitial, packF)) return false;
			if (powF !== '' && c.power !== parseInt(powF, 10)) return false;
			if (costF !== '' && c.cost !== parseInt(costF, 10)) return false;
			if (rarF && c.rarity !== rarF) return false;
			return true;
		});
		const mode = libSort ? libSort.value : 'cost_asc';
		list = list.slice();
		list.sort(function (a, b) {
			let r;
			if (mode === 'cost_desc') {
				r = cmpCost(b, a);
			} else {
				r = cmpCost(a, b);
			}
			if (r !== 0) return r;
			r = cmpPower(a, b);
			if (r !== 0) return r;
			return cmpName(a, b);
		});
		return list;
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
				attrLines: c.attrLines,
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
		wireLibraryCardFaceImages(face, plateFbFull, dataFbFull);
		if (typeof applyLibraryCardFaceSpark === 'function') {
			applyLibraryCardFaceSpark(face, c.rarity);
		}
		const inner = document.createElement('div');
		inner.className = 'library-card__inner';
		inner.appendChild(face);
		return inner;
	}

	/** デッキ上段に載せたあとでカード名の 1 行フィットを再計測（初回幅 0 回避） */
	function refitMiniCardFaceName(hostEl) {
		const face = hostEl.querySelector('.card-face.card-face--layered');
		if (!face || typeof window.fitCardFaceNameToOneLine !== 'function') return;
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

	function appendCardImage(parent, c) {
		if (c.layerBase || c.layerBar || c.layerFrame || c.layerPortrait) {
			parent.appendChild(buildMiniLayered(c));
			return;
		}
		if (c.img) {
			const im = document.createElement('img');
			im.src = c.img;
			im.alt = c.name;
			parent.appendChild(im);
			return;
		}
		const ph = document.createElement('div');
		ph.className = 'mini-card__no-art';
		ph.textContent = (c.name || '?').slice(0, 1);
		ph.title = c.name;
		parent.appendChild(ph);
	}

	/** ライブラリ一覧用：画像の下に枚数（×N） */
	function appendLibCardFace(parent, c) {
		appendCardImage(parent, c);
		const qty = document.createElement('span');
		qty.className = 'mini-card__qty';
		qty.textContent = '×' + c.qty;
		parent.appendChild(qty);
	}

	/** デッキに入れた枚数をカード右上に重ね表示 */
	function setSelectionBadge(el, n) {
		let b = el.querySelector('.card-pool-badge');
		if (n <= 0) {
			if (b) {
				b.remove();
			}
			return;
		}
		if (!b) {
			b = document.createElement('span');
			b.className = 'card-pool-badge';
			b.setAttribute('aria-hidden', 'true');
			el.insertBefore(b, el.firstChild);
		}
		b.textContent = String(n);
	}

	/** 右クリックで詳細（ブラウザのコンテキストメニューは出さない） */
	function bindRightClickOpenCardDetail(el, c) {
		el.addEventListener('contextmenu', function (e) {
			e.preventDefault();
			hideTooltip();
			openCardDetailModal(c);
		});
	}

	function fillDeckTooltipAbility(el, raw) {
		if (!el) return;
		el.textContent = '';
		const text = raw == null || raw === '' ? '—' : raw;
		if (text === '—') {
			el.textContent = '—';
			return;
		}
		const nl = text.indexOf('\n');
		const head = nl >= 0 ? text.slice(0, nl) : text;
		const rest = nl >= 0 ? text.slice(nl + 1) : '';
		if (head === '〈配置〉' || head === '〈常時〉' || head === '〈フィールド〉') {
			const tag = document.createElement('span');
			tag.className = 'deck-tooltip__ability-tag';
			tag.textContent = head;
			el.appendChild(tag);
			if (rest) {
				el.appendChild(document.createElement('br'));
				const desc = document.createElement('span');
				desc.className = 'deck-tooltip__ability-desc';
				desc.textContent = rest;
				el.appendChild(desc);
			}
			return;
		}
		el.textContent = text;
	}

	function positionTooltip(clientX, clientY) {
		const pad = 12;
		const tw = tooltipEl.offsetWidth;
		const th = tooltipEl.offsetHeight;
		let x = clientX + pad;
		let y = clientY + pad;
		if (x + tw > window.innerWidth - pad) {
			x = Math.max(pad, clientX - tw - pad);
		}
		if (y + th > window.innerHeight - pad) {
			y = Math.max(pad, window.innerHeight - th - pad);
		}
		tooltipEl.style.left = x + 'px';
		tooltipEl.style.top = y + 'px';
	}

	function showTooltip(c, clientX, clientY) {
		tooltipName.textContent = c.name;
		const compound = deckEditTooltipAttributeIsCompound(c);
		tooltipEl.classList.toggle('deck-tooltip--wide-attr', compound);
		tooltipAttr.textContent = deckEditTooltipAttribute(c);
		tooltipAttr.classList.toggle('deck-tooltip__attr--oneline', compound);
		tooltipCost.textContent = String(c.cost);
		tooltipPower.textContent = c.fieldCard ? '—' : String(c.power);
		fillDeckTooltipAbility(tooltipAbility, c.ability);
		tooltipEl.hidden = false;
		positionTooltip(clientX, clientY);
	}

	function hideTooltip() {
		tooltipEl.hidden = true;
		tooltipEl.classList.remove('deck-tooltip--wide-attr');
		tooltipAttr.classList.remove('deck-tooltip__attr--oneline');
	}

	function bindCardTooltip(el, c) {
		el.addEventListener('mouseenter', function (e) {
			showTooltip(c, e.clientX, e.clientY);
		});
		el.addEventListener('mousemove', function (e) {
			if (!tooltipEl.hidden) {
				positionTooltip(e.clientX, e.clientY);
			}
		});
		el.addEventListener('mouseleave', hideTooltip);
		el.addEventListener('blur', hideTooltip);
	}

	function refreshLib() {
		libZone.innerHTML = '';
		hideTooltip();
		const list = sortedLibraryList();
		let added = 0;
		list.forEach(function (c) {
			const cap = maxPerForId(c.id);
			const inDeck = countInDeck(c.id);
			const el = document.createElement('button');
			el.type = 'button';
			var deckCls = '';
			if (inDeck === 1) deckCls = ' mini-card--deck-1';
			else if (inDeck >= 2) deckCls = ' mini-card--deck-2';
			el.className = 'mini-card mini-card--lib' + deckCls;
			el.dataset.id = String(c.id);
			const inDeckHint = inDeck > 0 ? '。デッキに' + inDeck + '枚使用中' : '';
			el.setAttribute(
				'aria-label',
				c.name +
					(c.fieldCard ? '（×' : '（強さ' + c.power + '・×') +
					c.qty +
					inDeckHint +
					'）。左クリックでデッキへ、右クリックで詳細'
			);
			appendLibCardFace(el, c);
			setSelectionBadge(el, inDeck);
			bindRightClickOpenCardDetail(el, c);
			bindCardTooltip(el, c);
			const addToDeck = function () {
				if (!canAddToDeck(c.id, cap)) return;
				const copy = document.createElement('div');
				copy.className = 'mini-card mini-card--deck';
				copy.dataset.id = String(c.id);
				copy.setAttribute('role', 'button');
				copy.setAttribute('tabindex', '0');
				copy.setAttribute(
					'aria-label',
					c.name + '。左クリックでデッキから戻す、右クリックで詳細'
				);
				appendCardImage(copy, c);
				bindRightClickOpenCardDetail(copy, c);
				bindCardTooltip(copy, c);
				const removeFromDeck = function () {
					copy.remove();
					refreshLib();
					update();
				};
				bindDeckSlotRemove(copy, removeFromDeck);
				deckZone.appendChild(copy);
				sortUpperZoneByTribeThenId(deckZone);
				refitMiniCardFaceName(copy);
				refreshLib();
				update();
			};
			el.addEventListener('click', function (e) {
				if (e.button !== 0) return;
				addToDeck();
			});
			libZone.appendChild(el);
			added++;
		});
		if (added === 0 && list.length === 0 && seeds.length > 0) {
			const hasSearch = libSearch && libSearch.value.trim();
			const hasAttr = libFilterAttr && libFilterAttr.value;
			const hasPack = libFilterPack && libFilterPack.value;
			const hasPow = libFilterPower && libFilterPower.value !== '';
			const hasCost = libFilterCost && libFilterCost.value !== '';
			const hasRar = libFilterRarity && libFilterRarity.value;
			const hasKind = libFilterCardKind && libFilterCardKind.value;
			if (hasSearch || hasAttr || hasPack || hasPow || hasCost || hasRar || hasKind) {
				const p = document.createElement('p');
				p.className = 'muted deck-lib-empty-msg';
				p.textContent = '表示条件に一致するカードがありません。';
				libZone.appendChild(p);
			}
		}
	}

	function update() {
		const n = deckZone.querySelectorAll('.mini-card').length;
		deckCount.textContent = n + ' / 8';
		completeBtn.style.display = n === 8 ? '' : 'none';
		if (clearAllDeckBtn) {
			clearAllDeckBtn.disabled = n === 0;
		}
		const ids = Array.from(deckZone.querySelectorAll('.mini-card')).map(function (x) { return x.dataset.id; });
		cardIdsInput.value = ids.join(',');
	}

	function clearDeckToLibrary() {
		if (!deckZone) return;
		deckZone.querySelectorAll('.mini-card').forEach(function (node) {
			node.remove();
		});
		hideTooltip();
		closeCardDetailModal();
		refreshLib();
		update();
	}

	if (clearAllDeckBtn) {
		clearAllDeckBtn.addEventListener('click', function () {
			clearDeckToLibrary();
		});
	}

	function bootstrapDeck() {
		initialDeck.forEach(function (id) {
			const c = seeds.find(function (s) { return s.id === id; });
			if (!c) return;
			const cap = maxPerForId(id);
			if (!canAddToDeck(id, cap)) return;
			const copy = document.createElement('div');
			copy.className = 'mini-card mini-card--deck';
			copy.dataset.id = String(id);
			copy.setAttribute('role', 'button');
			copy.setAttribute('tabindex', '0');
			copy.setAttribute(
				'aria-label',
				c.name + '。左クリックでデッキから戻す、右クリックで詳細'
			);
			appendCardImage(copy, c);
			bindRightClickOpenCardDetail(copy, c);
			bindCardTooltip(copy, c);
			const removeFromDeck = function () {
				copy.remove();
				refreshLib();
				update();
			};
			bindDeckSlotRemove(copy, removeFromDeck);
			deckZone.appendChild(copy);
			refitMiniCardFaceName(copy);
		});
		sortUpperZoneByTribeThenId(deckZone);
	}

	function onFilterChange() {
		refreshLib();
	}

	if (libSearch) {
		libSearch.addEventListener('input', onFilterChange);
	}
	if (libSort) {
		libSort.addEventListener('change', onFilterChange);
	}
	if (libFilterAttr) {
		libFilterAttr.addEventListener('change', onFilterChange);
	}
	if (libFilterPower) {
		libFilterPower.addEventListener('change', onFilterChange);
	}
	if (libFilterCost) {
		libFilterCost.addEventListener('change', onFilterChange);
	}
	if (libFilterRarity) {
		libFilterRarity.addEventListener('change', onFilterChange);
	}
	if (libFilterPack) {
		libFilterPack.addEventListener('change', onFilterChange);
	}
	if (libFilterCardKind) {
		libFilterCardKind.addEventListener('change', onFilterChange);
	}

	document.addEventListener('scroll', hideTooltip, true);
	window.addEventListener('blur', hideTooltip);

	if (detailModal) {
		detailModal.addEventListener('click', function (e) {
			if (e.target === detailModal) closeCardDetailModal();
		});
		detailModal.querySelectorAll('[data-library-detail-close]').forEach(function (el) {
			el.addEventListener('click', closeCardDetailModal);
		});
		document.addEventListener('keydown', function (e) {
			if (e.key === 'Escape' && detailModal && !detailModal.hidden) closeCardDetailModal();
		});
	}

	bootstrapDeck();
	refreshLib();
	update();
})();
