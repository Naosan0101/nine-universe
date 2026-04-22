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

	const libZone = document.getElementById('recycle-lib-zone');
	const recycleZone = document.getElementById('recycle-zone');
	const crystalCompleteVal = document.getElementById('recycle-crystal-complete-val');
	const completeBtn = document.getElementById('recycle-complete-btn');
	const clearAllBtn = document.getElementById('recycle-clear-all-btn');
	const surplusOpenBtn = document.getElementById('recycle-surplus-open-btn');
	const surplusModal = document.getElementById('recycle-surplus-modal');
	const surplusModalCrystal = document.getElementById('recycle-surplus-modal-crystal');
	const completeModal = document.getElementById('recycle-complete-modal');
	const completeModalCrystal = document.getElementById('recycle-complete-modal-crystal');
	const surplusForm = document.getElementById('recycle-surplus-form');
	const mainForm = document.getElementById('recycle-main-form');
	const formFields = document.getElementById('recycle-form-fields');
	const libSearch = document.getElementById('recycle-lib-search');
	const libSort = document.getElementById('recycle-lib-sort');
	const libFilterAttr = document.getElementById('recycle-lib-filter-attr');
	const libFilterPower = document.getElementById('recycle-lib-filter-power');
	const libFilterCost = document.getElementById('recycle-lib-filter-cost');
	const libFilterRarity = document.getElementById('recycle-lib-filter-rarity');
	const libFilterPack = document.getElementById('recycle-lib-filter-pack');
	const libFilterCardKind = document.getElementById('recycle-lib-filter-card-kind');
	const detailModal = document.getElementById('library-detail-modal');
	const modalLayerBase = document.getElementById('lib-modal-layer-base');
	const modalLayerPortrait = document.getElementById('lib-modal-layer-portrait');
	const modalLayerBar = document.getElementById('lib-modal-layer-bar');
	const modalLayerFrame = document.getElementById('lib-modal-layer-frame');
	const modalCost = document.getElementById('lib-modal-cost');
	const modalPower = document.getElementById('lib-modal-power');
	const modalName = document.getElementById('lib-modal-name');
	const modalAttr = document.getElementById('lib-modal-attr');
	const modalRarity = document.getElementById('lib-modal-rarity');
	const modalPackInitial = document.getElementById('lib-modal-pack-initial');
	const modalAbility = document.getElementById('lib-modal-ability');
	const sideTitle = document.getElementById('lib-modal-side-title');
	const sideAttr = document.getElementById('lib-modal-side-attr');
	const sideCost = document.getElementById('lib-modal-side-cost');
	const sidePower = document.getElementById('lib-modal-side-power');
	const sideRarity = document.getElementById('lib-modal-side-rarity');
	const sidePack = document.getElementById('lib-modal-side-pack');
	const sideAbility = document.getElementById('lib-modal-side-ability');
	const tooltipEl = document.getElementById('deck-tooltip');
	const tooltipName = tooltipEl ? tooltipEl.querySelector('.deck-tooltip__name') : null;
	const tooltipAttr = tooltipEl ? tooltipEl.querySelector('.deck-tooltip__attr') : null;
	const tooltipCost = tooltipEl ? tooltipEl.querySelector('.deck-tooltip__cost') : null;
	const tooltipPower = tooltipEl ? tooltipEl.querySelector('.deck-tooltip__power') : null;
	const tooltipAbility = tooltipEl ? tooltipEl.querySelector('.deck-tooltip__ability') : null;

	const ATTR_LABEL = {
		HUMAN: '人間',
		ELF: 'エルフ',
		UNDEAD: 'アンデッド',
		DRAGON: 'ドラゴン',
		MACHINE: 'マシン',
		CARBUNCLE: 'カーバンクル'
	};
	const RARITY_LABEL = { Reg: 'レジェンダリー', Ep: 'エピック', R: 'レア', C: 'コモン' };
	const PACK_JA = {
		STD: 'スタンダードパック1',
		WH: '風吹く丘パック',
		ET: '邪悪なる脅威パック',
		JU: '宝石の秘境パック',
		IF: '鉄面の艦隊パック'
	};

	function packSourcesForInitial(piRaw) {
		const pi = (piRaw || 'STD').trim().toUpperCase() || 'STD';
		if (pi === 'WH') return ['風吹く丘パック', 'スタンダードパック1'];
		if (pi === 'ET') return ['邪悪なる脅威パック', 'スタンダードパック1'];
		if (pi === 'JU') return [PACK_JA.JU];
		if (pi === 'IF') return [PACK_JA.IF];
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

	const PACK_IDS_STANDARD_1 = ['STD', 'WH', 'ET'];
	const PACK_IDS_STANDARD_2 = ['JU', 'IF'];

	function matchesPackFilter(pi, filterVal) {
		if (!filterVal) return true;
		const n = (pi || 'STD').trim().toUpperCase() || 'STD';
		if (filterVal === 'STANDARD_1') return PACK_IDS_STANDARD_1.indexOf(n) !== -1;
		if (filterVal === 'STANDARD_2') return PACK_IDS_STANDARD_2.indexOf(n) !== -1;
		return n === filterVal;
	}

	function matchesTribeFilter(cardAttr, filterVal) {
		if (!filterVal) return true;
		if (!cardAttr) return false;
		if (cardAttr === filterVal) return true;
		return cardAttr.split('_').indexOf(filterVal) !== -1;
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

	/** 選択エリア内カード：左クリックで1枚ライブラリへ戻す（Enter / Space も可） */
	function bindRecycleSlotRemove(copy, removeHandler) {
		copy.addEventListener('click', function (e) {
			if (typeof e.button === 'number' && e.button !== 0) {
				return;
			}
			removeHandler();
		});
		copy.addEventListener('keydown', function (ev) {
			if (ev.key !== 'Enter' && ev.key !== ' ') return;
			ev.preventDefault();
			removeHandler();
		});
	}

	const seeds = Array.from(document.querySelectorAll('#recycle-lib-seed .seed')).map(function (el) {
		const p = parseInt(el.dataset.power, 10);
		const cost = parseInt(el.dataset.cost, 10);
		const rec = parseInt(el.dataset.recyclable, 10);
		const cry = parseInt(el.dataset.crystal, 10);
		return {
			id: parseInt(el.dataset.id, 10),
			img: staticUrl(el.dataset.img || ''),
			layerBase: el.dataset.layerBase || '',
			layerPortrait: el.dataset.layerPortrait || '',
			layerBar: el.dataset.layerBar || '',
			layerFrame: el.dataset.layerFrame || '',
			rarity: (el.dataset.rarity || 'C').trim(),
			rarityLabel: (el.dataset.rarityLabel || '').trim(),
			qty: parseInt(el.dataset.qty, 10) || 0,
			recyclable: isNaN(rec) ? 0 : rec,
			crystal: isNaN(cry) ? 0 : cry,
			name: el.dataset.name || '',
			attribute: el.dataset.attribute || '',
			attributeLabelJa: el.dataset.attributeLabel || '',
			attrLines: (function () {
				const p0 = el.dataset.attrPipe || '';
				return p0 ? p0.split('|').filter(Boolean) : [];
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
	}).filter(function (c) {
		return c.qty > 0 && !isNaN(c.id);
	});

	const TRIBE_SORT_ORDER = ['HUMAN', 'ELF', 'UNDEAD', 'DRAGON', 'MACHINE', 'CARBUNCLE'];

	function tribeSortIndex(segment) {
		const u = (segment || '').trim().toUpperCase();
		const i = TRIBE_SORT_ORDER.indexOf(u);
		return i >= 0 ? i : 99;
	}

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

	function countInRecycle(id) {
		const node = recycleZone.querySelector('.mini-card--deck[data-id="' + id + '"]');
		if (!node) {
			return 0;
		}
		const c = parseInt(node.dataset.recycleStack, 10);
		return isNaN(c) ? 1 : c;
	}

	function canAddToRecycle(id) {
		const row = seeds.find(function (s) {
			return s.id === id;
		});
		if (!row) return false;
		return countInRecycle(id) < row.recyclable;
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

	function appendLibCardFace(parent, c) {
		appendCardImage(parent, c);
		const qty = document.createElement('span');
		qty.className = 'mini-card__qty';
		qty.textContent = '×' + c.qty;
		parent.appendChild(qty);
	}

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

	/** リサイクル上段：同一カードの積み枚数（2以上で右上バッジ） */
	function setRecycleStackBadge(el, n) {
		let b = el.querySelector('.card-pool-badge');
		if (n <= 1) {
			if (b) {
				b.remove();
			}
			el.dataset.recycleStack = '1';
			return;
		}
		el.dataset.recycleStack = String(n);
		if (!b) {
			b = document.createElement('span');
			b.className = 'card-pool-badge';
			b.setAttribute('aria-hidden', 'true');
			el.insertBefore(b, el.firstChild);
		}
		b.textContent = String(n);
	}

	function recyclePendingAriaLabel(c, n) {
		const name = c.name || 'カード';
		if (n <= 1) {
			return name + '。左クリックで1枚戻す、右クリックで詳細';
		}
		return name + '、' + n + '枚選択中。左クリックで1枚戻す、右クリックで詳細';
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
			modalRarity.textContent = rarity;
		}
		if (sideRarity) {
			sideRarity.textContent = rarityLabel;
		}
		if (modalPackInitial) {
			const raw = (c.packInitial || '').trim();
			modalPackInitial.textContent = raw === '' ? 'STD' : raw.toUpperCase();
			modalPackInitial.hidden = false;
		}
		if (sidePack) {
			sidePack.textContent = packSourcesForInitial(c.packInitial).join('\n');
		}
		if (modalCost) {
			modalCost.textContent = String(c.cost);
			modalCost.className = 'card-face__cost';
			if (c.cost === 1) modalCost.classList.add('card-face__cost--digit-1');
			if (c.cost === 2) modalCost.classList.add('card-face__cost--digit-2');
		}
		if (modalPower) {
			if (c.fieldCard) {
				modalPower.textContent = '';
				modalPower.className = 'card-face__power card-face__power--hidden';
			} else {
				modalPower.textContent = String(c.power);
				modalPower.className = 'card-face__power';
				if (c.power === 4) modalPower.classList.add('card-face__power--digit-4');
			}
		}
		if (modalName) {
			modalName.textContent = c.name || '';
			if (typeof window.resetCardFaceNameFitInline === 'function') {
				window.resetCardFaceNameFitInline(modalName);
			}
		}
		if (sideTitle) sideTitle.textContent = c.name || '';
		if (sideCost) sideCost.textContent = String(c.cost);
		if (sidePower) {
			sidePower.textContent = c.fieldCard ? '—' : String(c.power);
		}
		if (modalAttr) {
			const pipe = (c.attrPipe || '').trim();
			let lines = pipe ? pipe.split('|').filter(Boolean) : [];
			if (lines.length <= 1 && c.attribute && c.attribute.indexOf('_') !== -1) {
				lines = c.attribute.split('_').map(function (seg) {
					return ATTR_LABEL[seg] || seg;
				}).filter(Boolean);
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
				modalAttr.textContent = attributeLabelJa(c.attribute, c.attributeLabelJa) || '';
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
			const t = blocks
				.map(function (b) {
					return b.h ? b.h + '\n' + b.b : b.b;
				})
				.join('\n\n');
			sideAbility.textContent = t || '—';
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
		if (!tooltipName) return;
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
		if (!tooltipEl) return;
		tooltipEl.hidden = true;
		tooltipEl.classList.remove('deck-tooltip--wide-attr');
		if (tooltipAttr) tooltipAttr.classList.remove('deck-tooltip__attr--oneline');
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

	function bindRightClickOpenCardDetail(el, c) {
		el.addEventListener('contextmenu', function (e) {
			e.preventDefault();
			hideTooltip();
			openCardDetailModal(c);
		});
	}

	function totalEstimateCrystal() {
		let sum = 0;
		Array.from(recycleZone.querySelectorAll('.mini-card--deck')).forEach(function (n) {
			const x = parseInt(n.dataset.crystal, 10);
			const cnt = parseInt(n.dataset.recycleStack, 10);
			const q = isNaN(cnt) ? 1 : cnt;
			if (!isNaN(x)) {
				sum += x * q;
			}
		});
		return sum;
	}

	function totalPendingCardsInRecycle() {
		let n = 0;
		recycleZone.querySelectorAll('.mini-card--deck').forEach(function (el) {
			const c = parseInt(el.dataset.recycleStack, 10);
			n += isNaN(c) ? 1 : c;
		});
		return n;
	}

	function inDecksForSeed(c) {
		const owned = c.qty;
		const rec = c.recyclable;
		return Math.max(0, owned - rec);
	}

	function surplusRecycleQty(c) {
		const owned = c.qty;
		const inD = inDecksForSeed(c);
		const keep = Math.max(2, inD);
		return Math.max(0, owned - keep);
	}

	function totalSurplusCrystal() {
		let sum = 0;
		seeds.forEach(function (c) {
			const n = surplusRecycleQty(c);
			if (n > 0) {
				sum += n * c.crystal;
			}
		});
		return sum;
	}

	function addPendingCardCopy(c) {
		const existing = recycleZone.querySelector('.mini-card--deck[data-id="' + c.id + '"]');
		if (existing) {
			const next =
				(parseInt(existing.dataset.recycleStack, 10) || 1) + 1;
			existing.dataset.recycleStack = String(next);
			setRecycleStackBadge(existing, next);
			existing.setAttribute('aria-label', recyclePendingAriaLabel(c, next));
			return;
		}
		const copy = document.createElement('div');
		copy.className = 'mini-card mini-card--deck';
		copy.dataset.id = String(c.id);
		copy.dataset.crystal = String(c.crystal);
		copy.dataset.recycleStack = '1';
		copy.setAttribute('role', 'button');
		copy.setAttribute('tabindex', '0');
		copy.setAttribute('aria-label', recyclePendingAriaLabel(c, 1));
		appendCardImage(copy, c);
		setRecycleStackBadge(copy, 1);
		bindRightClickOpenCardDetail(copy, c);
		bindCardTooltip(copy, c);
		const removeOne = function () {
			const cur = parseInt(copy.dataset.recycleStack, 10) || 1;
			if (cur > 1) {
				const left = cur - 1;
				copy.dataset.recycleStack = String(left);
				setRecycleStackBadge(copy, left);
				copy.setAttribute('aria-label', recyclePendingAriaLabel(c, left));
				refreshLib();
				update();
				return;
			}
			copy.remove();
			refreshLib();
			update();
		};
		bindRecycleSlotRemove(copy, removeOne);
		recycleZone.appendChild(copy);
		refitMiniCardFaceName(copy);
	}

	function update() {
		const n = totalPendingCardsInRecycle();
		if (crystalCompleteVal) {
			crystalCompleteVal.textContent = String(totalEstimateCrystal());
		}
		if (completeBtn) {
			completeBtn.style.display = n > 0 ? '' : 'none';
		}
		if (clearAllBtn) {
			clearAllBtn.disabled = n === 0;
		}
		if (surplusOpenBtn) {
			surplusOpenBtn.disabled = totalSurplusCrystal() <= 0;
		}
	}

	function refreshLib() {
		libZone.innerHTML = '';
		hideTooltip();
		const list = sortedLibraryList();
		let added = 0;
		list.forEach(function (c) {
			const inRec = countInRecycle(c.id);
			const el = document.createElement('button');
			el.type = 'button';
			el.className = 'mini-card mini-card--lib';
			el.dataset.id = String(c.id);
			const blocked = c.recyclable <= 0;
			if (blocked) {
				el.disabled = true;
				el.classList.add('mini-card--disabled');
			}
			el.setAttribute(
				'aria-label',
				c.name +
					'（所持×' +
					c.qty +
					'・リサイクル可' +
					c.recyclable +
					'）。左クリックで選択エリアへ追加、右クリックで詳細'
			);
			appendLibCardFace(el, c);
			setSelectionBadge(el, inRec);
			bindCardTooltip(el, c);
			bindRightClickOpenCardDetail(el, c);

			el.addEventListener('click', function (e) {
				if (el.disabled) return;
				if (e.button !== 0) return;
				if (!canAddToRecycle(c.id)) return;
				addPendingCardCopy(c);
				sortUpperZoneByTribeThenId(recycleZone);
				refreshLib();
				update();
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

	function clearRecycleZone() {
		recycleZone.querySelectorAll('.mini-card').forEach(function (n) {
			n.remove();
		});
		hideTooltip();
		closeCardDetailModal();
		refreshLib();
		update();
	}

	if (clearAllBtn) {
		clearAllBtn.addEventListener('click', clearRecycleZone);
	}

	function openSurplusModal() {
		if (!surplusModal || !surplusModalCrystal) return;
		surplusModalCrystal.textContent = String(totalSurplusCrystal());
		surplusModal.hidden = false;
		document.body.style.overflow = 'hidden';
	}

	function closeSurplusModal() {
		if (!surplusModal) return;
		surplusModal.hidden = true;
		document.body.style.overflow = '';
	}

	function openCompleteConfirmModal() {
		if (!completeModal) {
			return;
		}
		if (completeModalCrystal) {
			completeModalCrystal.textContent = String(totalEstimateCrystal());
		}
		completeModal.hidden = false;
		document.body.style.overflow = 'hidden';
	}

	function closeCompleteConfirmModal() {
		if (!completeModal) {
			return;
		}
		completeModal.hidden = true;
		document.body.style.overflow = '';
	}

	function submitRecycleConversion() {
		if (!mainForm || !formFields) {
			return;
		}
		formFields.innerHTML = '';
		const byId = {};
		Array.from(recycleZone.querySelectorAll('.mini-card--deck')).forEach(function (n) {
			const id = parseInt(n.dataset.id, 10);
			if (isNaN(id)) {
				return;
			}
			const cnt = parseInt(n.dataset.recycleStack, 10);
			const q = isNaN(cnt) ? 1 : cnt;
			byId[id] = (byId[id] || 0) + q;
		});
		Object.keys(byId).forEach(function (k) {
			const inp = document.createElement('input');
			inp.type = 'hidden';
			inp.name = 'qty_' + k;
			inp.value = String(byId[k]);
			formFields.appendChild(inp);
		});
		mainForm.submit();
	}

	if (surplusOpenBtn) {
		surplusOpenBtn.addEventListener('click', function () {
			if (surplusOpenBtn.disabled) return;
			openSurplusModal();
		});
	}
	if (surplusModal) {
		surplusModal.addEventListener('click', function (e) {
			if (e.target === surplusModal) closeSurplusModal();
		});
		surplusModal.querySelectorAll('[data-recycle-surplus-close]').forEach(function (el) {
			el.addEventListener('click', closeSurplusModal);
		});
		const noBtn = surplusModal.querySelector('[data-recycle-surplus-no]');
		if (noBtn) {
			noBtn.addEventListener('click', closeSurplusModal);
		}
		const yesBtn = surplusModal.querySelector('[data-recycle-surplus-yes]');
		if (yesBtn && surplusForm) {
			yesBtn.addEventListener('click', function () {
				closeSurplusModal();
				surplusForm.submit();
			});
		}
	}

	if (completeBtn && mainForm && formFields) {
		completeBtn.addEventListener('click', function () {
			if (totalPendingCardsInRecycle() <= 0) {
				return;
			}
			openCompleteConfirmModal();
		});
	}
	if (completeModal) {
		completeModal.addEventListener('click', function (e) {
			if (e.target === completeModal) {
				closeCompleteConfirmModal();
			}
		});
		completeModal.querySelectorAll('[data-recycle-complete-close]').forEach(function (el) {
			el.addEventListener('click', closeCompleteConfirmModal);
		});
		const completeNo = completeModal.querySelector('[data-recycle-complete-no]');
		if (completeNo) {
			completeNo.addEventListener('click', closeCompleteConfirmModal);
		}
		const completeYes = completeModal.querySelector('[data-recycle-complete-yes]');
		if (completeYes) {
			completeYes.addEventListener('click', function () {
				closeCompleteConfirmModal();
				submitRecycleConversion();
			});
		}
	}

	function onFilterChange() {
		refreshLib();
	}

	if (libSearch) libSearch.addEventListener('input', onFilterChange);
	if (libSort) libSort.addEventListener('change', onFilterChange);
	if (libFilterAttr) libFilterAttr.addEventListener('change', onFilterChange);
	if (libFilterPower) libFilterPower.addEventListener('change', onFilterChange);
	if (libFilterCost) libFilterCost.addEventListener('change', onFilterChange);
	if (libFilterRarity) libFilterRarity.addEventListener('change', onFilterChange);
	if (libFilterPack) libFilterPack.addEventListener('change', onFilterChange);
	if (libFilterCardKind) libFilterCardKind.addEventListener('change', onFilterChange);

	document.addEventListener('scroll', hideTooltip, true);
	window.addEventListener('blur', hideTooltip);

	if (detailModal) {
		detailModal.addEventListener('click', function (e) {
			if (e.target === detailModal) closeCardDetailModal();
		});
		detailModal.querySelectorAll('[data-library-detail-close]').forEach(function (el) {
			el.addEventListener('click', closeCardDetailModal);
		});
	}

	document.addEventListener('keydown', function (e) {
		if (e.key !== 'Escape') {
			return;
		}
		if (completeModal && !completeModal.hidden) {
			closeCompleteConfirmModal();
			return;
		}
		if (surplusModal && !surplusModal.hidden) {
			closeSurplusModal();
			return;
		}
		if (detailModal && !detailModal.hidden) {
			closeCardDetailModal();
		}
	});

	(function bindRecycleZoneHorizontalDrag() {
		const zone = recycleZone;
		if (!zone) {
			return;
		}
		let drag = null;
		zone.addEventListener('pointerdown', function (e) {
			if (e.button !== 0) {
				return;
			}
			/* カード上のクリックはキャプチャしない（capture すると click がカードに届かず1枚戻しが効かない） */
			if (e.target && e.target.closest && e.target.closest('.mini-card--deck')) {
				return;
			}
			drag = {
				id: e.pointerId,
				x0: e.clientX,
				sl0: zone.scrollLeft,
				suppressed: false
			};
			try {
				zone.setPointerCapture(e.pointerId);
			} catch (err) {
				// noop
			}
		});
		zone.addEventListener('pointermove', function (e) {
			if (!drag || e.pointerId !== drag.id) {
				return;
			}
			const dx = e.clientX - drag.x0;
			if (Math.abs(dx) > 8) {
				drag.suppressed = true;
			}
			if (drag.suppressed) {
				zone.scrollLeft = drag.sl0 - dx;
				e.preventDefault();
			}
		});
		function endDrag(e) {
			if (!drag || e.pointerId !== drag.id) {
				return;
			}
			if (drag.suppressed) {
				zone.addEventListener(
					'click',
					function cap(ev) {
						ev.preventDefault();
						ev.stopPropagation();
						ev.stopImmediatePropagation();
					},
					true
				);
				setTimeout(function () {
					zone.removeEventListener('click', cap, true);
				}, 0);
			}
			try {
				zone.releasePointerCapture(e.pointerId);
			} catch (err2) {
				// noop
			}
			drag = null;
		}
		zone.addEventListener('pointerup', endDrag);
		zone.addEventListener('pointercancel', endDrag);
	})();

	refreshLib();
	update();
})();
