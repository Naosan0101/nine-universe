/**
 * ライブラリ／デッキ編集／リサイクル等で共有する「化石（フィールド）」詳細ナビと、
 * モーダル右カラム効果文中の「化石（フィールド）」リンク（カード面左側はプレーンテキスト）。
 */
(function (global) {
	var FOSSIL_FIELD_TOKEN = '化石（フィールド）';

	function parseCompanionJson(raw) {
		if (raw == null || String(raw).trim() === '') {
			return null;
		}
		try {
			var o = JSON.parse(String(raw));
			return o && typeof o === 'object' ? o : null;
		} catch (e) {
			return null;
		}
	}

	/**
	 * 拡大表示のカード面（左）の効果欄。リンク色は付けずプレーンテキストのみ。
	 * 「化石（フィールド）」への導線は {@link #appendSideAbilityDetail} 側に置く。
	 */
	function appendAbilityBlocksToModal(modalAbility, blocks, companionDetailJson, onOpenCompanion) {
		modalAbility.innerHTML = '';
		blocks.forEach(function (bl) {
			if (bl.h) {
				var ph = document.createElement('p');
				ph.className = 'card-face__ability-head';
				ph.textContent = bl.h;
				modalAbility.appendChild(ph);
			}
			var pb = document.createElement('p');
			pb.className = 'card-face__ability-body';
			pb.textContent = bl.b == null ? '' : String(bl.b);
			modalAbility.appendChild(pb);
		});
	}

	/**
	 * モーダル右カラム「効果」本文。 companion が取れるときだけ「化石（フィールド）」を黄／ホバー青でクリック可能にする。
	 */
	function companionLinkToken(companionDetailJson) {
		var o = parseCompanionJson(companionDetailJson != null ? String(companionDetailJson) : '');
		if (o && o.linkToken != null && String(o.linkToken).trim() !== '') {
			return String(o.linkToken).trim();
		}
		return FOSSIL_FIELD_TOKEN;
	}

	function appendSideAbilityDetail(sideEl, blocks, companionDetailJson, onOpenCompanion, onOpenExtraCompanion) {
		if (!sideEl) return;
		sideEl.innerHTML = '';
		var compStr = companionDetailJson != null ? String(companionDetailJson) : '';
		var parsedComp = parseCompanionJson(compStr);
		var hasCompanion = parsedComp != null && compStr.trim() !== '';
		var linkTok = companionLinkToken(companionDetailJson);
		var onFn = typeof onOpenCompanion === 'function' ? onOpenCompanion : null;
		var onExtraFn = typeof onOpenExtraCompanion === 'function' ? onOpenExtraCompanion : null;
		if (!blocks || !blocks.length) {
			sideEl.appendChild(document.createTextNode('—'));
			return;
		}
		blocks.forEach(function (bl) {
			if (bl.h) {
				var ph = document.createElement('p');
				ph.className = 'library-detail-modal__side-ability-head';
				ph.textContent = bl.h;
				sideEl.appendChild(ph);
			}
			var pb = document.createElement('p');
			pb.className = 'library-detail-modal__side-ability-body';
			var body = bl.b == null ? '' : String(bl.b);
			if (
				hasCompanion &&
				parsedComp &&
				parsedComp.kind === 'kingMakerEffectLinks' &&
				onFn &&
				onExtraFn &&
				body.indexOf('「インクナイト」') !== -1 &&
				body.indexOf('「インクキング」') !== -1
			) {
				appendKingMakerDualLinkBody(pb, body, onFn, onExtraFn);
			} else if (
				hasCompanion &&
				parsedComp &&
				parsedComp.kind === 'dominionMinionEffectLinks' &&
				onFn &&
				onExtraFn &&
				body.indexOf('「ミニオンソルジャー」') !== -1 &&
				body.indexOf('「ミニオンキング」') !== -1
			) {
				appendDominionDualLinkBody(pb, body, onFn, onExtraFn);
			} else if (
				hasCompanion &&
				parsedComp &&
				parsedComp.kind === 'luciferMiracleFallenLinks' &&
				onFn &&
				onExtraFn &&
				body.indexOf('「奇跡」') !== -1 &&
				body.indexOf('「堕天使ルシファー」') !== -1
			) {
				appendLuciferMiracleFallenDualLinkBody(pb, body, onFn, onExtraFn);
			} else if (
				hasCompanion &&
				parsedComp &&
				parsedComp.kind === 'mikaelMiracleDeckLinks' &&
				onFn &&
				onExtraFn &&
				body.indexOf('「奇跡」') !== -1
			) {
				var deckTok =
					parsedComp.mikaelDeckLinkToken != null && String(parsedComp.mikaelDeckLinkToken).trim() !== ''
						? String(parsedComp.mikaelDeckLinkToken).trim()
						: '「ミカエルデッキ（ミカエルのカード6枚からなるデッキ）」';
				if (body.indexOf(deckTok) !== -1) {
					appendMikaelMiracleDeckDualLinkBody(pb, body, onFn, onExtraFn, deckTok);
				} else {
					pb.textContent = body;
				}
			} else if (hasCompanion && onFn && body.indexOf(linkTok) !== -1) {
				var parts = body.split(linkTok);
				for (var i = 0; i < parts.length; i++) {
					if (i > 0) {
						var sp = document.createElement('span');
						sp.className = 'nu-fossil-field-link';
						sp.textContent = linkTok;
						sp.tabIndex = 0;
						sp.setAttribute('role', 'link');
						sp.addEventListener('click', function (ev) {
							ev.preventDefault();
							ev.stopPropagation();
							onFn();
						});
						sp.addEventListener('keydown', function (ev) {
							if (ev.key === 'Enter' || ev.key === ' ') {
								ev.preventDefault();
								onFn();
							}
						});
						pb.appendChild(sp);
					}
					pb.appendChild(document.createTextNode(parts[i]));
				}
			} else {
				pb.textContent = body;
			}
			sideEl.appendChild(pb);
		});
	}

	function appendKingMakerDualLinkBody(pb, body, onInkKnight, onInkKing) {
		var tokens = [
			{ tok: '「インクナイト」', fn: onInkKnight },
			{ tok: '「インクキング」', fn: onInkKing }
		];
		function appendFragment(remaining) {
			if (remaining === '') {
				return;
			}
			var bestIdx = -1;
			var bestTok = null;
			var bestFn = null;
			for (var t = 0; t < tokens.length; t++) {
				var ix = remaining.indexOf(tokens[t].tok);
				if (ix >= 0 && (bestIdx < 0 || ix < bestIdx)) {
					bestIdx = ix;
					bestTok = tokens[t].tok;
					bestFn = tokens[t].fn;
				}
			}
			if (bestIdx < 0 || !bestTok || !bestFn) {
				pb.appendChild(document.createTextNode(remaining));
				return;
			}
			if (bestIdx > 0) {
				pb.appendChild(document.createTextNode(remaining.slice(0, bestIdx)));
			}
			var sp = document.createElement('span');
			sp.className = 'nu-fossil-field-link';
			sp.textContent = bestTok;
			sp.tabIndex = 0;
			sp.setAttribute('role', 'link');
			(function (fn) {
				sp.addEventListener('click', function (ev) {
					ev.preventDefault();
					ev.stopPropagation();
					fn();
				});
				sp.addEventListener('keydown', function (ev) {
					if (ev.key === 'Enter' || ev.key === ' ') {
						ev.preventDefault();
						fn();
					}
				});
			})(bestFn);
			pb.appendChild(sp);
			appendFragment(remaining.slice(bestIdx + bestTok.length));
		}
		appendFragment(body);
	}

	function appendDominionDualLinkBody(pb, body, onMinionSoldier, onMinionKing) {
		var tokens = [
			{ tok: '「ミニオンソルジャー」', fn: onMinionSoldier },
			{ tok: '「ミニオンキング」', fn: onMinionKing }
		];
		function appendFragment(remaining) {
			if (remaining === '') {
				return;
			}
			var bestIdx = -1;
			var bestTok = null;
			var bestFn = null;
			for (var t = 0; t < tokens.length; t++) {
				var ix = remaining.indexOf(tokens[t].tok);
				if (ix >= 0 && (bestIdx < 0 || ix < bestIdx)) {
					bestIdx = ix;
					bestTok = tokens[t].tok;
					bestFn = tokens[t].fn;
				}
			}
			if (bestIdx < 0 || !bestTok || !bestFn) {
				pb.appendChild(document.createTextNode(remaining));
				return;
			}
			if (bestIdx > 0) {
				pb.appendChild(document.createTextNode(remaining.slice(0, bestIdx)));
			}
			var sp = document.createElement('span');
			sp.className = 'nu-fossil-field-link';
			sp.textContent = bestTok;
			sp.tabIndex = 0;
			sp.setAttribute('role', 'link');
			(function (fn) {
				sp.addEventListener('click', function (ev) {
					ev.preventDefault();
					ev.stopPropagation();
					fn();
				});
				sp.addEventListener('keydown', function (ev) {
					if (ev.key === 'Enter' || ev.key === ' ') {
						ev.preventDefault();
						fn();
					}
				});
			})(bestFn);
			pb.appendChild(sp);
			appendFragment(remaining.slice(bestIdx + bestTok.length));
		}
		appendFragment(body);
	}

	function appendLuciferMiracleFallenDualLinkBody(pb, body, onMiracle, onFallen) {
		var tokens = [
			{ tok: '「奇跡」', fn: onMiracle },
			{ tok: '「堕天使ルシファー」', fn: onFallen }
		];
		function appendFragment(remaining) {
			if (remaining === '') {
				return;
			}
			var bestIdx = -1;
			var bestTok = null;
			var bestFn = null;
			for (var t = 0; t < tokens.length; t++) {
				var ix = remaining.indexOf(tokens[t].tok);
				if (ix >= 0 && (bestIdx < 0 || ix < bestIdx)) {
					bestIdx = ix;
					bestTok = tokens[t].tok;
					bestFn = tokens[t].fn;
				}
			}
			if (bestIdx < 0 || !bestTok || !bestFn) {
				pb.appendChild(document.createTextNode(remaining));
				return;
			}
			if (bestIdx > 0) {
				pb.appendChild(document.createTextNode(remaining.slice(0, bestIdx)));
			}
			var sp = document.createElement('span');
			sp.className = 'nu-fossil-field-link';
			sp.textContent = bestTok;
			sp.tabIndex = 0;
			sp.setAttribute('role', 'link');
			(function (fn) {
				sp.addEventListener('click', function (ev) {
					ev.preventDefault();
					ev.stopPropagation();
					fn();
				});
				sp.addEventListener('keydown', function (ev) {
					if (ev.key === 'Enter' || ev.key === ' ') {
						ev.preventDefault();
						fn();
					}
				});
			})(bestFn);
			pb.appendChild(sp);
			appendFragment(remaining.slice(bestIdx + bestTok.length));
		}
		appendFragment(body);
	}

	function appendMikaelMiracleDeckDualLinkBody(pb, body, onMiracle, onMikaelDeck, deckLinkTok) {
		var dt = deckLinkTok != null && String(deckLinkTok).trim() !== '' ? String(deckLinkTok).trim() : '';
		var tokens = [
			{ tok: '「奇跡」', fn: onMiracle },
			{ tok: dt, fn: onMikaelDeck }
		];
		function appendFragment(remaining) {
			if (remaining === '') {
				return;
			}
			var bestIdx = -1;
			var bestTok = null;
			var bestFn = null;
			for (var t = 0; t < tokens.length; t++) {
				if (!tokens[t].tok) {
					continue;
				}
				var ix = remaining.indexOf(tokens[t].tok);
				if (ix >= 0 && (bestIdx < 0 || ix < bestIdx)) {
					bestIdx = ix;
					bestTok = tokens[t].tok;
					bestFn = tokens[t].fn;
				}
			}
			if (bestIdx < 0 || !bestTok || !bestFn) {
				pb.appendChild(document.createTextNode(remaining));
				return;
			}
			if (bestIdx > 0) {
				pb.appendChild(document.createTextNode(remaining.slice(0, bestIdx)));
			}
			var sp = document.createElement('span');
			sp.className = 'nu-fossil-field-link';
			sp.textContent = bestTok;
			sp.tabIndex = 0;
			sp.setAttribute('role', 'link');
			(function (fn) {
				sp.addEventListener('click', function (ev) {
					ev.preventDefault();
					ev.stopPropagation();
					fn();
				});
				sp.addEventListener('keydown', function (ev) {
					if (ev.key === 'Enter' || ev.key === ' ') {
						ev.preventDefault();
						fn();
					}
				});
			})(bestFn);
			pb.appendChild(sp);
			appendFragment(remaining.slice(bestIdx + bestTok.length));
		}
		appendFragment(body);
	}

	function sideAbilityTextFromBlocks(blocks) {
		return blocks
			.map(function (b) {
				return b.h ? b.h + '\n' + b.b : b.b;
			})
			.join('\n\n');
	}

	var stack = [];

	function resetDetailStack() {
		stack = [];
	}

	function pushDetailPlain(plain) {
		try {
			stack.push(JSON.parse(JSON.stringify(plain)));
		} catch (e) {
			// noop
		}
	}

	function popDetailPlain() {
		return stack.length ? stack.pop() : null;
	}

	function canGoBackDetail() {
		return stack.length > 0;
	}

	function updateFossilDetailNavButtons(navPrev, navNext, companionDetailJson, canBack) {
		var hasNext =
			companionDetailJson != null &&
			String(companionDetailJson).trim() !== '' &&
			parseCompanionJson(String(companionDetailJson)) != null;
		if (navNext) {
			navNext.hidden = !hasNext;
			navNext.setAttribute('aria-hidden', hasNext ? 'false' : 'true');
		}
		if (navPrev) {
			navPrev.hidden = !canBack;
			navPrev.setAttribute('aria-hidden', canBack ? 'false' : 'true');
		}
	}

	/** サーバ JSON（LibraryService.buildFossilFieldCompanionDetailJson）→ ライブラリ openModal 用フラット形 */
	function normalizeCompanionPlainForLibraryModal(comp) {
		if (!comp) return null;
		var fc = comp.fieldCard === true || comp.fieldCard === 'true';
		var chainJson =
			comp.nextCompanionDetailJson != null && String(comp.nextCompanionDetailJson).trim() !== ''
				? String(comp.nextCompanionDetailJson)
				: comp.companionDetailJson != null && String(comp.companionDetailJson).trim() !== ''
					? String(comp.companionDetailJson)
					: '';
		return {
			name: comp.name != null ? String(comp.name) : '',
			attribute: comp.attribute != null ? String(comp.attribute) : '',
			rarity: comp.rarity != null ? String(comp.rarity) : 'C',
			rarityLabel: comp.rarityLabel != null ? String(comp.rarityLabel) : '',
			packInitial: comp.packInitial != null ? String(comp.packInitial) : '—',
			cost: comp.cost != null ? String(comp.cost) : '0',
			basePower: comp.basePower != null ? String(comp.basePower) : '0',
			fieldCard: fc ? 'true' : 'false',
			owned: 'true',
			forceDetail: 'true',
			canonicalLine: comp.canonicalLine != null ? String(comp.canonicalLine) : '',
			attrPipe: comp.attrPipe != null ? String(comp.attrPipe) : '',
			attributeJa: comp.attributeJa != null ? String(comp.attributeJa) : '',
			layerBase: comp.layerBase != null ? String(comp.layerBase) : '',
			layerPortrait: comp.layerPortrait != null ? String(comp.layerPortrait) : '',
			layerPortraitAlt: comp.layerPortraitAlt != null ? String(comp.layerPortraitAlt) : '',
			layerFrame: comp.layerFrame != null ? String(comp.layerFrame) : '',
			layerBar: comp.layerBar != null ? String(comp.layerBar) : '',
			companionDetailJson: chainJson
		};
	}

	/** 同上 → deck-edit / recycle の openCardDetailModal(c) 用 */
	function normalizeCompanionPlainForDeckRecycleModal(comp) {
		var lib = normalizeCompanionPlainForLibraryModal(comp);
		if (!lib) return null;
		var pow = parseInt(lib.basePower, 10);
		return {
			name: lib.name,
			attribute: lib.attribute,
			rarity: lib.rarity,
			rarityLabel: lib.rarityLabel,
			packInitial: lib.packInitial,
			cost: parseInt(lib.cost, 10) || 0,
			power: isNaN(pow) ? 0 : pow,
			fieldCard: lib.fieldCard === 'true',
			canonicalLine: lib.canonicalLine,
			attrPipe: lib.attrPipe,
			attributeLabelJa: lib.attributeJa,
			layerBase: lib.layerBase,
			layerPortrait: lib.layerPortrait,
			layerPortraitAlt: lib.layerPortraitAlt,
			layerBar: lib.layerBar,
			layerFrame: lib.layerFrame,
			companionDetailJson: lib.companionDetailJson != null ? String(lib.companionDetailJson) : ''
		};
	}

	global.NuLibDetailFossilUi = {
		fossilFieldToken: FOSSIL_FIELD_TOKEN,
		companionLinkToken: companionLinkToken,
		parseCompanionJson: parseCompanionJson,
		appendAbilityBlocksToModal: appendAbilityBlocksToModal,
		appendSideAbilityDetail: appendSideAbilityDetail,
		sideAbilityTextFromBlocks: sideAbilityTextFromBlocks,
		resetDetailStack: resetDetailStack,
		pushDetailPlain: pushDetailPlain,
		popDetailPlain: popDetailPlain,
		canGoBackDetail: canGoBackDetail,
		updateFossilDetailNavButtons: updateFossilDetailNavButtons,
		normalizeCompanionPlainForLibraryModal: normalizeCompanionPlainForLibraryModal,
		normalizeCompanionPlainForDeckRecycleModal: normalizeCompanionPlainForDeckRecycleModal
	};
})(typeof window !== 'undefined' ? window : this);
