package com.example.nineuniverse.service;

import com.example.nineuniverse.CanonicalLibraryCardText;
import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.card.CardAttributeLabels;
import com.example.nineuniverse.card.CardAttributes;
import com.example.nineuniverse.card.CardFaceAbilityFormatter;
import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.domain.LibraryCardView;
import com.example.nineuniverse.repository.UserCollectionMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LibraryService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final CardCatalogService cardCatalogService;
	private final UserCollectionMapper userCollectionMapper;

	public List<LibraryCardView> library(long userId) {
		Map<Short, Integer> qty = userCollectionMapper.findByUserId(userId).stream()
				.collect(Collectors.toMap(r -> r.getCardId(), r -> r.getQuantity()));
		List<LibraryCardView> out = new ArrayList<>();
		for (CardDefinition c : cardCatalogService.all()) {
			if (GameConstants.excludedFromPackOpenAndLibraryListing(c.getId())) {
				continue;
			}
			LibraryCardView v = new LibraryCardView();
			fillCardFace(v, c);
			int q = qty.getOrDefault(c.getId(), 0);
			v.setQuantity(q);
			v.setOwned(q > 0);
			out.add(v);
		}
		out.sort(Comparator
				.comparingInt((LibraryCardView v) -> v.getCost())
				.thenComparing((LibraryCardView v) -> CardAttributes.primarySegment(v.getAttribute()))
				.thenComparing(LibraryCardView::getAttribute)
				.thenComparingInt(LibraryCardView::getBasePower)
				.thenComparing(LibraryCardView::getName));
		return out;
	}

	/**
	 * パック開封後のフラッシュ復元用（ID のみ渡す）。順序は引きの順のまま。
	 */
	/**
	 * カード定義から表示用ビューを生成（リサイクル画面など）。
	 */
	public LibraryCardView viewForCardDefinition(CardDefinition c) {
		LibraryCardView v = new LibraryCardView();
		fillCardFace(v, c);
		return v;
	}

	/**
	 * レジェンダリー交換：一覧用。詳細表示のため {@link LibraryCardView#setOwned(boolean)} は呼び出し側で true にする。
	 */
	public List<LibraryCardView> legendaryDefinitionsAsViews() {
		List<LibraryCardView> out = new ArrayList<>();
		for (CardDefinition c : cardCatalogService.all()) {
			if (GameConstants.excludedFromPackOpenAndLibraryListing(c.getId())) {
				continue;
			}
			String r = c.getRarity();
			if (r == null || !"Reg".equalsIgnoreCase(r.trim())) {
				continue;
			}
			LibraryCardView v = viewForCardDefinition(c);
			v.setOwned(true);
			v.setQuantity(1);
			out.add(v);
		}
		out.sort(Comparator.comparing(LibraryCardView::getName, String.CASE_INSENSITIVE_ORDER));
		return out;
	}

	public List<LibraryCardView> displayFacesForCardIds(List<Short> cardIds) {
		Map<Short, CardDefinition> map = cardCatalogService.mapById();
		List<LibraryCardView> out = new ArrayList<>();
		for (Short id : cardIds) {
			if (id == null) {
				continue;
			}
			CardDefinition c = map.get(id);
			if (c == null) {
				continue;
			}
			LibraryCardView v = new LibraryCardView();
			fillCardFace(v, c);
			v.setQuantity(1);
			v.setOwned(true);
			out.add(v);
		}
		return out;
	}

	private void fillCardFace(LibraryCardView v, CardDefinition c) {
		fillCardFace(v, c, true);
	}

	/**
	 * @param assignCompanionDetailJson 一覧用の companion JSON を付けるか。インクナイト自己参照JSONのネスト回避で false。
	 */
	private void fillCardFace(LibraryCardView v, CardDefinition c, boolean assignCompanionDetailJson) {
		v.setId(c.getId());
		v.setName(c.getName());
		v.setAttribute(c.getAttribute());
		String pi = c.getPackInitial();
		v.setPackInitial(pi != null && !pi.isBlank() ? pi.trim() : "STD");
		if (GameConstants.excludedFromPackOpenAndLibraryListing(c.getId())) {
			v.setPackInitial("—");
		}
		String rarity = c.getRarity();
		v.setRarity(rarity != null && !rarity.isBlank() ? rarity : "C");
		v.setRarityLabel(v.getRarity());
		String portraitImageFile = GameConstants.effectiveCardImageFile(c.getId(), c.getImageFile());
		v.setImagePath(GameConstants.cardPortraitPath(c.getId(), portraitImageFile));
		v.setLayerPortraitPath(GameConstants.cardFacePortraitLayerPath(c.getAttribute(), c.getName(), portraitImageFile, c.getId()));
		v.setLayerPortraitPathAlt(GameConstants.cardFacePortraitLayerPathAltNfc(c.getAttribute(), c.getName(), portraitImageFile, c.getId()));
		v.setCost(c.getCost() != null ? c.getCost() : 0);
		v.setBasePower(c.getBasePower() != null ? c.getBasePower() : 0);
		String canon = CanonicalLibraryCardText.lineForId(c.getId());
		v.setCanonicalAbilityLine(canon != null ? canon : "");
		v.setLibraryAbilityText(CardFaceAbilityFormatter.tooltipAbilityTextForCardId(c.getId()));
		v.setAttributeLabelJa(CardAttributeLabels.japaneseName(c.getAttribute()));
		var attrLines = CardAttributeLabels.japaneseNameLines(c.getAttribute());
		v.setAttributeLabelLines(attrLines);
		v.setAttributeLabelPipe(attrLines.isEmpty() ? "" : String.join("|", attrLines));
		v.setBarImagePath(GameConstants.cardLayerBarPath(c.getAttribute()));
		v.setLayerBasePath(GameConstants.CARD_LAYER_BASE);
		boolean isField = c.getCardKind() != null && c.getCardKind().trim().equalsIgnoreCase("FIELD");
		v.setFieldCard(isField);
		v.setLayerFramePath(isField ? GameConstants.CARD_LAYER_DATA_FIELD : GameConstants.CARD_LAYER_DATA);
		String dep = c.getDeployHelp();
		String pas = c.getPassiveHelp();
		v.setDeployHelp(dep != null ? dep : "");
		v.setPassiveHelp(pas != null ? pas : "");
		v.setAbilityBlocks(CardFaceAbilityFormatter.blocksForCardId(c.getId()));
		v.setCostFaceCssClass(cardFaceCostClass(v.getCost()));
		v.setPowerFaceCssClass(isField ? "card-face__power card-face__power--hidden" : cardFacePowerClass(v.getBasePower()));
		if (assignCompanionDetailJson) {
			if (c.getId() != null && c.getId() == GameConstants.FOSSIL_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildFossilFieldCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.ATLANTIS_FIELD_CARD_ID) {
				v.setCompanionDetailJson(buildSwordfishCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.HEAVENS_GATE_FIELD_CARD_ID) {
				v.setCompanionDetailJson(buildHeavensGateMiracleCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.ZADKIEL_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildHeavensGateMiracleCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.RAMIEL_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildHeavensGateMiracleCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.VIRTUAL_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildHeavensGateMiracleCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.CELESTIA_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildHeavensGateMiracleCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.SERAPHIM_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildHeavensGateMiracleCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.GABRIEL_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildHeavensGateMiracleCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.LUCIFER_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildLuciferMiracleFallenCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.MIKAEL_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildMikaelMiracleDeckLinksCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.MIKAEL_WRATH_CARD_ID) {
				v.setCompanionDetailJson(buildMikaelDeckCardChainFromId(GameConstants.MIKAEL_WRATH_CARD_ID));
			} else if (c.getId() != null && c.getId() == GameConstants.MIKAEL_PUNCH_CARD_ID) {
				v.setCompanionDetailJson(buildMikaelDeckCardChainFromId(GameConstants.MIKAEL_PUNCH_CARD_ID));
			} else if (c.getId() != null && c.getId() == GameConstants.MIKAEL_STRATEGY_CARD_ID) {
				v.setCompanionDetailJson(buildHeavensGateMiracleCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.MIKAEL_MINION_A_CARD_ID) {
				v.setCompanionDetailJson(buildMikaelDeckCardChainFromId(GameConstants.MIKAEL_MINION_A_CARD_ID));
			} else if (c.getId() != null && c.getId() == GameConstants.MIKAEL_MINION_B_CARD_ID) {
				v.setCompanionDetailJson(buildMikaelDeckCardChainFromId(GameConstants.MIKAEL_MINION_B_CARD_ID));
			} else if (c.getId() != null && c.getId() == GameConstants.MIRACLE_TOKEN_CARD_ID) {
				v.setCompanionDetailJson(buildMiracleToFallenLuciferChainCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.COMIC_DINOSAUR_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildDragonEggCompanionDetailJson());
			} else if (c.getId() != null && (c.getId() == GameConstants.SEASERPENT_CARD_ID
					|| c.getId() == GameConstants.MERMAID_FIGHTER_CARD_ID
					|| c.getId() == GameConstants.KRAKEN_FIGHTER_CARD_ID
					|| c.getId() == GameConstants.SIREN_FIGHTER_CARD_ID
					|| c.getId() == GameConstants.POSEIDON_FIGHTER_CARD_ID)) {
				v.setCompanionDetailJson(buildSwordfishCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.INK_KNIGHT_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildInkKnightSelfCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.ANGEL_MAGE_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildAngelMageSelfCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.PAPER_CITY_FIELD_CARD_ID) {
				v.setCompanionDetailJson(buildPaperCityInkKnightCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.KING_MAKER_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildKingMakerCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.DOMINION_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildDominionMinionEffectLinksCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.MINION_SOLDIER_TOKEN_CARD_ID) {
				v.setCompanionDetailJson(buildMinionSoldierNextChampionCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.INK_KING_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildPaperCityInkKnightCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.SKETCHER_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildPaperCityInkKnightCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.COMIC_WITCH_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildPaperCityInkKnightCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.BELIEVER_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildDeathBounceFieldLinkCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.ARTHUR_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildKamuiFieldLinkCompanionDetailJson());
			} else if (c.getId() != null && c.getId() == GameConstants.BOT_BIKE_FIGHTER_CARD_ID) {
				v.setCompanionDetailJson(buildMechanicRuleCardLinkCompanionDetailJson());
			} else {
				v.setCompanionDetailJson(null);
			}
		} else {
			v.setCompanionDetailJson(null);
		}
	}

	/**
	 * 天界門 ヘヴンズゲートの拡大詳細から「奇跡」へ。{@code linkToken} は効果文中の「奇跡」括り付きと一致させる。
	 */
	private String buildHeavensGateMiracleCompanionDetailJson() {
		try {
			LinkedHashMap<String, Object> m = companionCardFaceJsonMap(GameConstants.MIRACLE_TOKEN_CARD_ID, false);
			if (m == null) {
				return null;
			}
			m.put("linkToken", "「奇跡」");
			return OBJECT_MAPPER.writeValueAsString(m);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * 「奇跡」拡大詳細の › で「堕天使ルシファー」へ。pack_initial は表示用「-」。
	 */
	private String buildMiracleToFallenLuciferChainCompanionDetailJson() {
		try {
			LinkedHashMap<String, Object> fallen = companionCardFaceJsonMap(GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID, false);
			LinkedHashMap<String, Object> miracle = companionCardFaceJsonMap(GameConstants.MIRACLE_TOKEN_CARD_ID, false);
			if (fallen == null || miracle == null) {
				return null;
			}
			fallen.put("packInitial", "-");
			miracle.put("nextCompanionDetailJson", OBJECT_MAPPER.writeValueAsString(fallen));
			return OBJECT_MAPPER.writeValueAsString(miracle);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * 「ルシファー」: 効果文の「奇跡」「堕天使ルシファー」リンクと ›（奇跡→堕天使）。
	 */
	private String buildLuciferMiracleFallenCompanionDetailJson() {
		try {
			LinkedHashMap<String, Object> miracle = companionCardFaceJsonMap(GameConstants.MIRACLE_TOKEN_CARD_ID, false);
			LinkedHashMap<String, Object> fallen = companionCardFaceJsonMap(GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID, false);
			if (miracle == null || fallen == null) {
				return null;
			}
			fallen.put("packInitial", "-");
			miracle.put("nextCompanionDetailJson", OBJECT_MAPPER.writeValueAsString(fallen));
			LinkedHashMap<String, Object> root = new LinkedHashMap<>();
			root.put("kind", "luciferMiracleFallenLinks");
			root.put("miracle", miracle);
			root.put("fallen", fallen);
			return OBJECT_MAPPER.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * 「ミカエル」: 効果文の「奇跡」「ミカエルデッキ（ミカエルのカード6枚からなるデッキ）」リンクと
	 * ›（奇跡→ミカエルの怒り→…→ミカエルの一閃）。〈配置〉は手札の奇跡2枚以上で奇跡をレストへ、ミカエルデッキから2枚をデッキ上へ。
	 */
	private String buildMikaelMiracleDeckLinksCompanionDetailJson() {
		try {
			LinkedHashMap<String, Object> miracle = companionCardFaceJsonMap(GameConstants.MIRACLE_TOKEN_CARD_ID, false);
			String chain = mikaelDeckChainSerializedFrom(GameConstants.MIKAEL_WRATH_CARD_ID);
			if (miracle == null || chain == null) {
				return null;
			}
			miracle.put("packInitial", "-");
			miracle.put("nextCompanionDetailJson", chain);
			LinkedHashMap<String, Object> root = new LinkedHashMap<>();
			root.put("kind", "mikaelMiracleDeckLinks");
			root.put("miracle", miracle);
			root.put("mikaelDeckLinkToken", "「ミカエルデッキ（ミカエルのカード6枚からなるデッキ）」");
			return OBJECT_MAPPER.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/** ミカエルデッキ各カードの › 用: {@code cardId} から一閃までの連鎖 JSON。 */
	private String buildMikaelDeckCardChainFromId(short cardId) {
		try {
			return mikaelDeckChainSerializedFrom(cardId);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	private String mikaelDeckChainSerializedFrom(short fromId) throws JsonProcessingException {
		if (fromId < GameConstants.MIKAEL_WRATH_CARD_ID || fromId > GameConstants.MIKAEL_FLASH_CARD_ID) {
			return null;
		}
		LinkedHashMap<String, Object> m = companionCardFaceJsonMap(fromId, false);
		if (m == null) {
			return null;
		}
		m.put("packInitial", "-");
		if (fromId < GameConstants.MIKAEL_FLASH_CARD_ID) {
			String nested = mikaelDeckChainSerializedFrom((short) (fromId + 1));
			if (nested != null) {
				m.put("nextCompanionDetailJson", nested);
			}
		}
		return OBJECT_MAPPER.writeValueAsString(m);
	}

	/**
	 * 〈化石（フィールド）〉の定義が DB に無い（マイグレーション未適用等）のほか、
	 * JSON 化やネストした {@link #fillCardFace} で例外が出た場合は null を返し、ライブラリ全体の表示は継続する。
	 */
	private String buildFossilFieldCompanionDetailJson() {
		try {
			CardDefinition fd = cardCatalogService.mapById().get(GameConstants.FOSSIL_FIELD_TRANSFORMS_TOKEN_CARD_ID);
			if (fd == null) {
				return null;
			}
			LibraryCardView comp = new LibraryCardView();
			fillCardFace(comp, fd);
			comp.setPackInitial("—");
			LinkedHashMap<String, Object> m = new LinkedHashMap<>();
			m.put("name", comp.getName());
			m.put("attribute", comp.getAttribute());
			m.put("rarity", comp.getRarity());
			m.put("rarityLabel", comp.getRarityLabel());
			m.put("packInitial", comp.getPackInitial());
			m.put("cost", Short.toString(comp.getCost()));
			m.put("basePower", Short.toString(comp.getBasePower()));
			m.put("fieldCard", Boolean.toString(comp.isFieldCard()));
			m.put("owned", "true");
			m.put("canonicalLine", comp.getCanonicalAbilityLine());
			m.put("ability", comp.getLibraryAbilityText());
			m.put("deployHelp", comp.getDeployHelp());
			m.put("passiveHelp", comp.getPassiveHelp());
			m.put("attrPipe", comp.getAttributeLabelPipe());
			m.put("attributeJa", comp.getAttributeLabelJa());
			m.put("layerBase", comp.getLayerBasePath());
			m.put("layerPortrait", comp.getLayerPortraitPath());
			m.put("layerPortraitAlt", comp.getLayerPortraitPathAlt());
			m.put("layerFrame", comp.getLayerFramePath());
			m.put("layerBar", comp.getBarImagePath());
			m.put("linkToken", "化石（フィールド）");
			return OBJECT_MAPPER.writeValueAsString(m);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * 「ソードフィッシュ」の定義が無い場合や JSON 化に失敗した場合は null。
	 */
	private String buildSwordfishCompanionDetailJson() {
		try {
			CardDefinition fd = cardCatalogService.mapById().get(GameConstants.SWORDFISH_TOKEN_CARD_ID);
			if (fd == null) {
				return null;
			}
			LibraryCardView comp = new LibraryCardView();
			fillCardFace(comp, fd);
			comp.setPackInitial("—");
			LinkedHashMap<String, Object> m = new LinkedHashMap<>();
			m.put("name", comp.getName());
			m.put("attribute", comp.getAttribute());
			m.put("rarity", comp.getRarity());
			m.put("rarityLabel", comp.getRarityLabel());
			m.put("packInitial", comp.getPackInitial());
			m.put("cost", Short.toString(comp.getCost()));
			m.put("basePower", Short.toString(comp.getBasePower()));
			m.put("fieldCard", Boolean.toString(comp.isFieldCard()));
			m.put("owned", "true");
			m.put("canonicalLine", comp.getCanonicalAbilityLine());
			m.put("ability", comp.getLibraryAbilityText());
			m.put("deployHelp", comp.getDeployHelp());
			m.put("passiveHelp", comp.getPassiveHelp());
			m.put("attrPipe", comp.getAttributeLabelPipe());
			m.put("attributeJa", comp.getAttributeLabelJa());
			m.put("layerBase", comp.getLayerBasePath());
			m.put("layerPortrait", comp.getLayerPortraitPath());
			m.put("layerPortraitAlt", comp.getLayerPortraitPathAlt());
			m.put("layerFrame", comp.getLayerFramePath());
			m.put("layerBar", comp.getBarImagePath());
			m.put("linkToken", "ソードフィッシュ");
			return OBJECT_MAPPER.writeValueAsString(m);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * 「ドラゴンの卵」の定義が無い場合や JSON 化に失敗した場合は null。
	 * 〈コミックダイナソー〉の拡大詳細からのリンク用（{@code linkToken} は効果文中の部分一致用）。
	 */
	private String buildDragonEggCompanionDetailJson() {
		try {
			CardDefinition fd = cardCatalogService.mapById().get(GameConstants.DRAGON_EGG_CARD_ID);
			if (fd == null) {
				return null;
			}
			LibraryCardView comp = new LibraryCardView();
			fillCardFace(comp, fd);
			comp.setPackInitial("—");
			LinkedHashMap<String, Object> m = new LinkedHashMap<>();
			m.put("name", comp.getName());
			m.put("attribute", comp.getAttribute());
			m.put("rarity", comp.getRarity());
			m.put("rarityLabel", comp.getRarityLabel());
			m.put("packInitial", comp.getPackInitial());
			m.put("cost", Short.toString(comp.getCost()));
			m.put("basePower", Short.toString(comp.getBasePower()));
			m.put("fieldCard", Boolean.toString(comp.isFieldCard()));
			m.put("owned", "true");
			m.put("canonicalLine", comp.getCanonicalAbilityLine());
			m.put("ability", comp.getLibraryAbilityText());
			m.put("deployHelp", comp.getDeployHelp());
			m.put("passiveHelp", comp.getPassiveHelp());
			m.put("attrPipe", comp.getAttributeLabelPipe());
			m.put("attributeJa", comp.getAttributeLabelJa());
			m.put("layerBase", comp.getLayerBasePath());
			m.put("layerPortrait", comp.getLayerPortraitPath());
			m.put("layerPortraitAlt", comp.getLayerPortraitPathAlt());
			m.put("layerFrame", comp.getLayerFramePath());
			m.put("layerBar", comp.getBarImagePath());
			m.put("linkToken", "ドラゴンの卵");
			return OBJECT_MAPPER.writeValueAsString(m);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * エンジェルメイジ拡大詳細: 効果文中の「エンジェルメイジ」リンクと › ナビ（自身定義へ）。
	 * {@code linkToken} は {@link CanonicalLibraryCardText} の括り付きと一致させる。
	 */
	private String buildAngelMageSelfCompanionDetailJson() {
		try {
			LinkedHashMap<String, Object> m = companionCardFaceJsonMap(GameConstants.ANGEL_MAGE_FIGHTER_CARD_ID, false);
			if (m == null) {
				return null;
			}
			m.put("linkToken", "「エンジェルメイジ」");
			return OBJECT_MAPPER.writeValueAsString(m);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * インクナイト自身の定義を companion として埋め込み（効果文中「インクナイト」リンク用）。{@link #fillCardFace} のネストを避けるため companion 付与なしで組み立てる。
	 */
	private String buildInkKnightSelfCompanionDetailJson() {
		try {
			CardDefinition fd = cardCatalogService.mapById().get(GameConstants.INK_KNIGHT_FIGHTER_CARD_ID);
			if (fd == null) {
				return null;
			}
			LibraryCardView comp = new LibraryCardView();
			fillCardFace(comp, fd, false);
			comp.setPackInitial("—");
			LinkedHashMap<String, Object> m = new LinkedHashMap<>();
			m.put("name", comp.getName());
			m.put("attribute", comp.getAttribute());
			m.put("rarity", comp.getRarity());
			m.put("rarityLabel", comp.getRarityLabel());
			m.put("packInitial", comp.getPackInitial());
			m.put("cost", Short.toString(comp.getCost()));
			m.put("basePower", Short.toString(comp.getBasePower()));
			m.put("fieldCard", Boolean.toString(comp.isFieldCard()));
			m.put("owned", "true");
			m.put("canonicalLine", comp.getCanonicalAbilityLine());
			m.put("ability", comp.getLibraryAbilityText());
			m.put("deployHelp", comp.getDeployHelp());
			m.put("passiveHelp", comp.getPassiveHelp());
			m.put("attrPipe", comp.getAttributeLabelPipe());
			m.put("attributeJa", comp.getAttributeLabelJa());
			m.put("layerBase", comp.getLayerBasePath());
			m.put("layerPortrait", comp.getLayerPortraitPath());
			m.put("layerPortraitAlt", comp.getLayerPortraitPathAlt());
			m.put("layerFrame", comp.getLayerFramePath());
			m.put("layerBar", comp.getBarImagePath());
			m.put("linkToken", "インクナイト");
			return OBJECT_MAPPER.writeValueAsString(m);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * カード1枚分の companion 用フラットマップ（linkToken なし）。{@link #fillCardFace} の assignCompanion に従う。
	 */
	private LinkedHashMap<String, Object> companionCardFaceJsonMap(short cardId, boolean assignCompanion) {
		CardDefinition fd = cardCatalogService.mapById().get(cardId);
		if (fd == null) {
			return null;
		}
		LibraryCardView comp = new LibraryCardView();
		fillCardFace(comp, fd, assignCompanion);
		comp.setPackInitial("—");
		LinkedHashMap<String, Object> m = new LinkedHashMap<>();
		m.put("name", comp.getName());
		m.put("attribute", comp.getAttribute());
		m.put("rarity", comp.getRarity());
		m.put("rarityLabel", comp.getRarityLabel());
		m.put("packInitial", comp.getPackInitial());
		m.put("cost", Short.toString(comp.getCost()));
		m.put("basePower", Short.toString(comp.getBasePower()));
		m.put("fieldCard", Boolean.toString(comp.isFieldCard()));
		m.put("owned", "true");
		m.put("canonicalLine", comp.getCanonicalAbilityLine());
		m.put("ability", comp.getLibraryAbilityText());
		m.put("deployHelp", comp.getDeployHelp());
		m.put("passiveHelp", comp.getPassiveHelp());
		m.put("attrPipe", comp.getAttributeLabelPipe());
		m.put("attributeJa", comp.getAttributeLabelJa());
		m.put("layerBase", comp.getLayerBasePath());
		m.put("layerPortrait", comp.getLayerPortraitPath());
		m.put("layerPortraitAlt", comp.getLayerPortraitPathAlt());
		m.put("layerFrame", comp.getLayerFramePath());
		m.put("layerBar", comp.getBarImagePath());
		return m;
	}

	/**
	 * 「インクナイト」の定義を companion として埋め込み（ペーパーシティ・インクキングの効果文「インクナイト」リンク用）。
	 */
	private String buildPaperCityInkKnightCompanionDetailJson() {
		try {
			LinkedHashMap<String, Object> m = companionCardFaceJsonMap(GameConstants.INK_KNIGHT_FIGHTER_CARD_ID, false);
			if (m == null) {
				return null;
			}
			m.put("linkToken", "「インクナイト」");
			return OBJECT_MAPPER.writeValueAsString(m);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * ドミニオン拡大詳細: 「ミニオンソルジャー」「ミニオンチャンピオン」リンクと ›（ソルジャー→チャンピオン）。
	 */
	private String buildDominionMinionEffectLinksCompanionDetailJson() {
		try {
			LinkedHashMap<String, Object> soldier = companionCardFaceJsonMap(GameConstants.MINION_SOLDIER_TOKEN_CARD_ID, false);
			LinkedHashMap<String, Object> champion = companionCardFaceJsonMap(GameConstants.MINION_CHAMPION_TOKEN_CARD_ID, false);
			if (soldier == null || champion == null) {
				return null;
			}
			soldier.put("linkToken", "「ミニオンソルジャー」");
			champion.put("linkToken", "「ミニオンチャンピオン」");
			soldier.put("nextCompanionDetailJson", OBJECT_MAPPER.writeValueAsString(champion));
			LinkedHashMap<String, Object> root = new LinkedHashMap<>();
			root.put("kind", "dominionMinionEffectLinks");
			root.put("minionSoldier", soldier);
			root.put("minionChampion", champion);
			return OBJECT_MAPPER.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/** ミニオンソルジャー単体: › でミニオンチャンピオンへ。 */
	private String buildMinionSoldierNextChampionCompanionDetailJson() {
		try {
			LinkedHashMap<String, Object> soldier = companionCardFaceJsonMap(GameConstants.MINION_SOLDIER_TOKEN_CARD_ID, false);
			LinkedHashMap<String, Object> champion = companionCardFaceJsonMap(GameConstants.MINION_CHAMPION_TOKEN_CARD_ID, false);
			if (soldier == null || champion == null) {
				return null;
			}
			soldier.put("linkToken", "「ミニオンソルジャー」");
			champion.put("linkToken", "「ミニオンチャンピオン」");
			soldier.put("nextCompanionDetailJson", OBJECT_MAPPER.writeValueAsString(champion));
			return OBJECT_MAPPER.writeValueAsString(soldier);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * 信奉者拡大詳細: 効果文中の「霊園教会 デスバウンス」を化石リンクと同様にクリック可能にする。
	 */
	private String buildDeathBounceFieldLinkCompanionDetailJson() {
		try {
			LinkedHashMap<String, Object> m = companionCardFaceJsonMap(GameConstants.DEATH_BOUNCE_FIELD_CARD_ID, false);
			if (m == null) {
				return null;
			}
			m.put("linkToken", "「霊園教会 デスバウンス」");
			return OBJECT_MAPPER.writeValueAsString(m);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * アーサー拡大詳細: 効果文中の「決戦の地 カムイ」を化石リンクと同様にクリック可能にする。
	 */
	private String buildKamuiFieldLinkCompanionDetailJson() {
		try {
			LinkedHashMap<String, Object> m = companionCardFaceJsonMap(GameConstants.KAMUI_FIELD_CARD_ID, false);
			if (m == null) {
				return null;
			}
			m.put("linkToken", "「決戦の地 カムイ」");
			return OBJECT_MAPPER.writeValueAsString(m);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * ボットバイク拡大詳細: 効果文中の「メカニック」（カード名）を化石リンクと同様にクリック可能にする。
	 */
	private String buildMechanicRuleCardLinkCompanionDetailJson() {
		try {
			LinkedHashMap<String, Object> m = companionCardFaceJsonMap(GameConstants.MECHANIC_RULE_REFERENCE_CARD_ID, false);
			if (m == null) {
				return null;
			}
			m.put("linkToken", "「メカニック」");
			return OBJECT_MAPPER.writeValueAsString(m);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * キングメーカー拡大詳細用: 効果文の「インクナイト」「インクキング」リンクと › ナビ（インクナイト→インクキング）。
	 */
	private String buildKingMakerCompanionDetailJson() {
		try {
			LinkedHashMap<String, Object> inkKnight = companionCardFaceJsonMap(GameConstants.INK_KNIGHT_FIGHTER_CARD_ID, false);
			LinkedHashMap<String, Object> inkKing = companionCardFaceJsonMap(GameConstants.INK_KING_FIGHTER_CARD_ID, false);
			if (inkKnight == null || inkKing == null) {
				return null;
			}
			inkKnight.put("linkToken", "「インクナイト」");
			String inkKnightLinkJson = buildPaperCityInkKnightCompanionDetailJson();
			if (inkKnightLinkJson != null) {
				inkKing.put("companionDetailJson", inkKnightLinkJson);
			}
			inkKnight.put("nextCompanionDetailJson", OBJECT_MAPPER.writeValueAsString(inkKing));
			LinkedHashMap<String, Object> root = new LinkedHashMap<>();
			root.put("kind", "kingMakerEffectLinks");
			root.put("inkKnight", inkKnight);
			root.put("inkKing", inkKing);
			return OBJECT_MAPPER.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static String cardFaceCostClass(short cost) {
		String s = "card-face__cost";
		if (cost == 1) {
			s += " card-face__cost--digit-1";
		}
		if (cost == 2) {
			s += " card-face__cost--digit-2";
		}
		return s;
	}

	private static String cardFacePowerClass(short basePower) {
		return basePower == 4 ? "card-face__power card-face__power--digit-4" : "card-face__power";
	}
}
