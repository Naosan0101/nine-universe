/**
 * fragments/card-face.html（ライブラリのカード表示）と同一構造の DOM を生成する。
 * テキスト・効果ブロック・キラ（card-spark.js）のルールをアプリ全体で共有する。
 */
(function (global) {
	'use strict';

	function clamp(n, lo, hi) {
		return Math.max(lo, Math.min(hi, n));
	}

	/**
	 * カード名が長い場合、フォントサイズを落として1行に収める。
	 * 省略（…）は使わず、必要なら字間も詰めて「全文表示」を優先する。
	 */
	function fitCardFaceNameToOneLine(faceRoot) {
		if (!faceRoot) return;
		const nameEl = faceRoot.querySelector
			? faceRoot.querySelector('.card-face__name')
			: null;
		if (!nameEl) return;
		const text = (nameEl.textContent || '').trim();
		if (!text) return;

		// 既に調整済みならスキップ
		if (nameEl.dataset && nameEl.dataset.nameFitDone === 'true') return;
		if (nameEl.dataset) nameEl.dataset.nameFitDone = 'true';

		const cs = window.getComputedStyle ? window.getComputedStyle(nameEl) : null;

		// 1行に収めるための調整（折り返し判定は scrollWidth で行う）
		nameEl.style.display = 'block';
		nameEl.style.whiteSpace = 'nowrap';
		nameEl.style.overflow = 'hidden';
		nameEl.style.textOverflow = 'clip';

		const fontPxRaw = cs ? parseFloat(cs.fontSize) : NaN;
		const startPx = Number.isFinite(fontPxRaw) && fontPxRaw > 0 ? fontPxRaw : 14;
		// かなり長いカード名でも全文表示できるよう、下限は低めにする
		const minPx = clamp(startPx * 0.55, 7, startPx);
		let px = startPx;

		// 最大80ステップで縮める（表示の揺れを抑える）
		for (let i = 0; i < 80; i++) {
			// clientWidth が 0（非表示/描画前）ならやめる
			if (nameEl.clientWidth <= 0) break;
			if (nameEl.scrollWidth <= nameEl.clientWidth + 0.5) return;
			if (px <= minPx + 0.01) break;
			px = Math.max(minPx, px - 0.25);
			nameEl.style.fontSize = px + 'px';
		}

		// まだ溢れているなら字間を詰める（最小フォントでも収まらないケース）
		if (nameEl.clientWidth > 0 && nameEl.scrollWidth > nameEl.clientWidth + 0.5) {
			nameEl.style.letterSpacing = '-0.04em';
		}
		// さらに縮める（字間調整後）
		for (let i = 0; i < 60; i++) {
			if (nameEl.clientWidth <= 0) break;
			if (nameEl.scrollWidth <= nameEl.clientWidth + 0.5) return;
			if (px <= 7.01) break;
			px = Math.max(7, px - 0.25);
			nameEl.style.fontSize = px + 'px';
		}

		// ここまで来たら、可能な限り縮めた状態で全文表示（省略はしない）
		nameEl.style.textOverflow = 'clip';
		}
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

	function absPath(path, contextPath) {
		if (path == null || path === '') return '';
		const p = String(path);
		if (p.startsWith('http://') || p.startsWith('https://')) return p;
		return (contextPath || '') + p;
	}

	function elSpan(cls, text) {
		const s = document.createElement('span');
		s.className = cls;
		s.textContent = text != null ? String(text) : '';
		return s;
	}

	function fillNarrativeFromBlocks(abWrap, blocks) {
		if (!blocks || !blocks.length) {
			const p = document.createElement('p');
			p.className = 'card-face__ability-body';
			p.textContent = '効果なし。';
			abWrap.appendChild(p);
			return;
		}
		blocks.forEach(function (b) {
			const head = b.headline != null && String(b.headline).trim() !== '' ? String(b.headline) : '';
			const body = b.body != null ? String(b.body) : '';
			if (head) {
				const hp = document.createElement('p');
				hp.className = 'card-face__ability-head';
				hp.textContent = head;
				abWrap.appendChild(hp);
			}
			const bp = document.createElement('p');
			bp.className = 'card-face__ability-body';
			bp.textContent = body || '—';
			abWrap.appendChild(bp);
		});
	}

	/** deck-edit の data-ability（モーダル用テキスト）と同じ分割ルール */
	function fillNarrativeFromAbilityString(abWrap, rawAbility) {
		const raw = (rawAbility != null ? String(rawAbility) : '').trim();
		if (!raw) {
			const p = document.createElement('p');
			p.className = 'card-face__ability-body';
			p.textContent = '効果なし。';
			abWrap.appendChild(p);
			return;
		}
		const nl = raw.indexOf('\n');
		const head = nl >= 0 ? raw.slice(0, nl) : raw;
		const rest = nl >= 0 ? raw.slice(nl + 1) : '';
		if (head === '〈配置〉' || head === '〈常時〉') {
			const hp = document.createElement('p');
			hp.className = 'card-face__ability-head';
			hp.textContent = head;
			abWrap.appendChild(hp);
			const bp = document.createElement('p');
			bp.className = 'card-face__ability-body';
			bp.textContent = rest || '—';
			abWrap.appendChild(bp);
		} else {
			const bp = document.createElement('p');
			bp.className = 'card-face__ability-body';
			bp.textContent = raw;
			abWrap.appendChild(bp);
		}
	}

	/**
	 * @param {object} card — layerBase(Path), layerBar(Path), layerFrame(Path), attribute, rarity, rarityLabel,
	 *   cost, basePower または power, name, attributeLabelLines または attrLines, attributeLabelJa,
	 *   abilityBlocks または ability（文字列）
	 * @param {object} [options]
	 * @param {string} [options.contextPath]
	 * @param {string} [options.plateFallback] — card_plate_fallback のフル URL
	 * @param {string} [options.dataFallback] — card_data_fallback のフル URL
	 * @param {string} [options.extraRootClasses] — 例: card-face--mini-deck, battle-layered--hand
	 */
	function buildLibraryCardFace(card, options) {
		options = options || {};
		const cp = options.contextPath != null ? options.contextPath : '';
		const plateFb = options.plateFallback || '';
		const dataFb = options.dataFallback || '';

		const layerBase = card.layerBasePath || card.layerBase || '';
		const layerPortrait = card.layerPortraitPath || card.layerPortrait || '';
		const layerBar = card.layerBarPath || card.layerBar || '';
		const layerFrame = card.layerFramePath || card.layerFrame || '';

		const face = document.createElement('div');
		face.className = 'card-face card-face--layered';
		const extra = (options.extraRootClasses || '').trim();
		if (extra) {
			extra.split(/\s+/).forEach(function (cl) {
				if (cl) face.classList.add(cl);
			});
		}
		if (card.attribute) {
			face.classList.add('card-face--attr-' + card.attribute);
		}
		const rar = String(card.rarity != null && card.rarity !== '' ? card.rarity : 'C').trim();
		if (rar === 'R' || rar === 'Ep' || rar === 'Reg' || rar === 'C') {
			face.classList.add('card-face--rarity-' + rar);
		} else {
			face.classList.add('card-face--rarity-C');
		}

		const stack = document.createElement('div');
		stack.className = 'card-face__stack';
		stack.setAttribute('aria-hidden', 'true');

		function pushLayer(classSuffix, url, fallback) {
			const im = document.createElement('img');
			im.className = 'card-face__layer-img card-face__layer-img--' + classSuffix;
			im.alt = '';
			im.src = absPath(url, cp) || fallback || '';
			if (classSuffix === 'frame') {
				im.setAttribute('fetchpriority', 'high');
			}
			stack.appendChild(im);
		}

		pushLayer('base', layerBase, plateFb);
		if (layerPortrait) {
			pushLayer('portrait', layerPortrait, '');
		}
		if (layerBar) {
			pushLayer('bar', layerBar, '');
		}
		pushLayer('frame', layerFrame, dataFb);

		face.appendChild(stack);

		const cost = Number(card.cost != null ? card.cost : 0);
		const pow = Number(
			card.basePower != null ? card.basePower : card.power != null ? card.power : 0
		);

		const datum = document.createElement('div');
		datum.className = 'card-face__layer card-face__datum';
		const costCls =
			'card-face__cost' +
			(cost === 1 ? ' card-face__cost--digit-1' : '') +
			(cost === 2 ? ' card-face__cost--digit-2' : '');
		datum.appendChild(elSpan(costCls, String(cost)));
		datum.appendChild(
			elSpan('card-face__power' + (pow === 4 ? ' card-face__power--digit-4' : ''), String(pow))
		);
		const nameEl = elSpan('card-face__name', card.name || '');
		datum.appendChild(nameEl);

		let attrLines = [];
		if (card.attributeLabelLines && card.attributeLabelLines.length) {
			attrLines = card.attributeLabelLines;
		} else if (card.attrLines && card.attrLines.length) {
			attrLines = card.attrLines;
		} else if (card.attributeLabelJa) {
			attrLines = [card.attributeLabelJa];
		}
		const attrWrap = document.createElement('span');
		attrWrap.className =
			'card-face__attr-label' + (attrLines.length > 1 ? ' card-face__attr-label--compound' : '');
		attrLines.forEach(function (ln) {
			attrWrap.appendChild(elSpan('card-face__attr-line', ln));
		});
		datum.appendChild(attrWrap);

		const rlab =
			card.rarityLabel != null && String(card.rarityLabel).trim() !== ''
				? String(card.rarityLabel)
				: rar;
		datum.appendChild(elSpan('card-face__rarity', rlab));

		const pi = card.packInitial != null ? String(card.packInitial).trim() : '';
		if (pi && pi.toUpperCase() !== 'STD') {
			datum.appendChild(elSpan('card-face__pack-initial', pi));
		}

		face.appendChild(datum);

		const nar = document.createElement('div');
		nar.className = 'card-face__layer card-face__narrative';
		const abWrap = document.createElement('div');
		abWrap.className = 'card-face__ability';
		if (card.abilityBlocks && card.abilityBlocks.length) {
			fillNarrativeFromBlocks(abWrap, card.abilityBlocks);
		} else {
			fillNarrativeFromAbilityString(abWrap, card.ability);
		}
		nar.appendChild(abWrap);
		face.appendChild(nar);

		const spark = document.createElement('div');
		spark.className = 'card-spark';
		spark.setAttribute('aria-hidden', 'true');
		face.appendChild(spark);

		// カード名の折り返しを1行に収める（必要な場合のみ）
		try {
			fitCardFaceNameToOneLine(face);
		} catch (e) {
			// noop
		}

		return face;
	}

	function wireLibraryCardFaceImages(faceRoot, plateFallback, dataFallback) {
		const plate = plateFallback || '';
		const data = dataFallback || '';
		const base = faceRoot.querySelector('img.card-face__layer-img--base');
		const portrait = faceRoot.querySelector('img.card-face__layer-img--portrait');
		const bar = faceRoot.querySelector('img.card-face__layer-img--bar');
		const frame = faceRoot.querySelector('img.card-face__layer-img--frame');
		applyOnceImgFallback(base, plate);
		applyOnceImgFallback(portrait, '');
		applyOnceImgFallback(bar, '');
		applyOnceImgFallback(frame, data);
	}

	function applyLibraryCardFaceSpark(faceRoot, rarity) {
		if (typeof global.fillContinuousCardSpark !== 'function') return;
		const spark = faceRoot.querySelector('.card-spark');
		global.fillContinuousCardSpark(spark, rarity || 'C');
	}

	global.buildLibraryCardFace = buildLibraryCardFace;
	global.wireLibraryCardFaceImages = wireLibraryCardFaceImages;
	global.applyLibraryCardFaceSpark = applyLibraryCardFaceSpark;
	global.fitCardFaceNameToOneLine = fitCardFaceNameToOneLine;

	// サーバーレンダリングされたカードも対象（ライブラリ/パック結果など）
	if (typeof document !== 'undefined') {
		function runFitAll() {
			document.querySelectorAll('.card-face.card-face--layered').forEach(function (face) {
				try {
					fitCardFaceNameToOneLine(face);
				} catch (e) {
					// noop
				}
			});
		}
		if (document.readyState === 'loading') {
			document.addEventListener('DOMContentLoaded', runFitAll);
		} else {
			setTimeout(runFitAll, 0);
		}
		// リサイズでレイアウトが変わったときも再調整（軽量なので全件でOK）
		window.addEventListener('resize', function () {
			// data フラグを外して再計測できるようにする
			document.querySelectorAll('.card-face__name[data-name-fit-done="true"]').forEach(function (el) {
				try {
					delete el.dataset.nameFitDone;
					el.style.fontSize = '';
					el.style.textOverflow = '';
						el.style.letterSpacing = '';
					el.style.whiteSpace = '';
					el.style.overflow = '';
					el.style.display = '';
				} catch (e) {
					// noop
				}
			});
			runFitAll();
		});
	}
})(typeof window !== 'undefined' ? window : globalThis);
