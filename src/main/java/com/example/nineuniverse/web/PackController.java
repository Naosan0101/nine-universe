package com.example.nineuniverse.web;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.domain.LibraryCardView;
import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.service.LibraryService;
import com.example.nineuniverse.service.PackService;
import com.example.nineuniverse.service.TimePackGaugeService;
import com.example.nineuniverse.service.PackService.PackType;
import com.example.nineuniverse.web.dto.PackOpeningSessionSlot;
import com.example.nineuniverse.web.dto.PackOpeningSlotView;
import com.example.nineuniverse.web.dto.PackPreviewLine;
import com.example.nineuniverse.web.dto.PackRarityRateRow;
import com.example.nineuniverse.web.dto.PackShopSlotView;
import com.example.nineuniverse.season.SeasonSchedule;
import java.time.LocalDate;
import java.time.ZoneId;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pack")
@RequiredArgsConstructor
public class PackController {

	/** 開封演出のあとに遷移する URL（セッション）。未設定時は {@code /pack/result}。 */
	public static final String SESSION_PACK_AFTER_OPEN_REDIRECT = "pack_after_open_redirect";

	/** {@link #SESSION_PACK_OPENING_SLOTS} と同じ順で、各カード枠がこの開封で初取得か（{@code pack_last_pulled_ids} と同順）。 */
	public static final String SESSION_PACK_LAST_PULLED_NEW_FLAGS = "pack_last_pulled_new_flags";

	/** 開封画面の背景演出（セッション）。値: {@code EPIC_PLUS} / {@code LEGENDARY}。未設定時は通常。 */
	public static final String SESSION_PACK_OPENING_THEME = "pack_opening_theme";

	/**
	 * 時間パックのボーナス開封から結果画面へ来た場合 true（表示後に {@link #result} でクリア）。
	 * 右上ナビを「ホームに戻る」のみにする。
	 */
	public static final String SESSION_PACK_RESULT_FROM_BONUS_PACK = "pack_result_from_bonus_pack";

	/**
	 * 新規登録プレゼントのスタンダードパック1開封から結果画面へ来た場合 true（表示後に {@link #result} でクリア）。
	 * 「もう一度引く」を出さない。
	 */
	public static final String SESSION_PACK_RESULT_FROM_STARTER_GIFT = "pack_result_from_starter_gift";

	/** 時間ゲージボーナス：開封順（カード／二つ名が混在しうる）。{@link com.example.nineuniverse.web.dto.PackOpeningSessionSlot} のリスト */
	public static final String SESSION_PACK_OPENING_SLOTS = "pack_opening_slots";

	/** 結果画面用：ボーナス二つ名の獲得一覧（{@link com.example.nineuniverse.service.NicknameEpithetService.EpithetGachaResult} のリスト） */
	public static final String SESSION_PACK_LAST_EPITHET_RESULTS = "pack_last_epithet_results";

	/** ホーム表示用：ボーナス二つ名ガチャの獲得テキスト（開封演出を挟まずポップアップで見せる） */
	public static final String SESSION_HOME_BONUS_EPITHET_UPPER = "home_bonus_epithet_upper";
	public static final String SESSION_HOME_BONUS_EPITHET_LOWER = "home_bonus_epithet_lower";

	private static final List<PackRarityRateRow> PACK_RARITY_RATES = List.of(
			new PackRarityRateRow("レジェンダリー", "2%"),
			new PackRarityRateRow("エピック", "10%"),
			new PackRarityRateRow("レア", "30%"),
			new PackRarityRateRow("コモン", "58%"));

	private final PackService packService;
	private final AppUserMapper appUserMapper;
	private final LibraryService libraryService;
	private final TimePackGaugeService timePackGaugeService;

	/** クラスパス走査用。パック絵は NFC ファイル名で {@code static/images/cards/} に配置。 */
	private static String packArtClasspathRel(String logicalFileName) {
		return "static/images/cards/" + Normalizer.normalize(logicalFileName.trim(), Normalizer.Form.NFC);
	}

	private static final List<String> PACK_ART_CLASSPATH_FILES = List.of(
			packArtClasspathRel(GameConstants.PACK_ART_FILE_STANDARD_1),
			packArtClasspathRel(GameConstants.PACK_ART_FILE_WINDY_HILL),
			packArtClasspathRel(GameConstants.PACK_ART_FILE_EVIL_THREAT),
			packArtClasspathRel(GameConstants.PACK_ART_FILE_STANDARD_2),
			packArtClasspathRel(GameConstants.PACK_ART_FILE_JEWEL_UTOPIA),
			packArtClasspathRel(GameConstants.PACK_ART_FILE_IRON_FLEET),
			packArtClasspathRel(GameConstants.PACK_ART_FILE_STANDARD_3),
			packArtClasspathRel(GameConstants.PACK_ART_FILE_OCEAN_TIDE),
			packArtClasspathRel(GameConstants.PACK_ART_FILE_CREATION_SANCTUM));

	private static final String CARD_BACK_CLASSPATH = "static/images/cards/card-back.PNG";

	/** 起動時に一度だけ算出（JAR 内リソースの lastModified 走査をリクエスト毎に繰り返さない） */
	private static final long PACK_ART_CACHE_KEY = computePackArtCacheKey();

	private static final long CARD_BACK_CACHE_KEY = computeCardBackCacheKey();

	public static long getPackArtCacheKey() {
		return PACK_ART_CACHE_KEY;
	}

	public static List<PackRarityRateRow> getPackRarityRatesForView() {
		return PACK_RARITY_RATES;
	}

	public static List<PackPreviewLine> buildPackPreviewLines(PackService packService, PackType type) {
		return toPreviewLines(packService.sortedEligibleCardsForPreview(type));
	}

	@GetMapping
	public String page(Model model) {
		long uid = CurrentUser.require().getId();
		var fresh = appUserMapper.findById(uid);
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		model.addAttribute("packArtCacheKey", PACK_ART_CACHE_KEY);
		model.addAttribute("gems", fresh != null && fresh.getCoins() != null ? fresh.getCoins() : 0);
		int starterGift = fresh != null && fresh.getStarterGiftStandard1Remaining() != null
				? fresh.getStarterGiftStandard1Remaining()
				: 0;
		model.addAttribute("starterGiftStandard1Remaining", starterGift);
		model.addAttribute("packRarityRates", PACK_RARITY_RATES);
		model.addAttribute("packShopSlots", buildPackShopSlots(today));
		return "pack-buy";
	}

	private static final List<PackShopSlotView> PACK_SHOP_CATALOG = List.of(
			slotDef(PackType.STANDARD, "STANDARD", "スタンダードパック1（WH+ET）", 3,
					GameConstants.packArtImageWebPath(GameConstants.PACK_ART_FILE_STANDARD_1),
					"pack-detail-standard", "スタンダードパック1（WH+ET）"),
			slotDef(PackType.WINDY_HILL, "WINDY_HILL", "風吹く丘パック（WH）", 4,
					GameConstants.packArtImageWebPath(GameConstants.PACK_ART_FILE_WINDY_HILL),
					"pack-detail-windy-hill", "風吹く丘パック（WH）"),
			slotDef(PackType.EVIL_THREAT, "EVIL_THREAT", "邪悪なる脅威パック（ET）", 5,
					GameConstants.packArtImageWebPath(GameConstants.PACK_ART_FILE_EVIL_THREAT),
					"pack-detail-evil-threat", "邪悪なる脅威パック（ET）"),
			slotDef(PackType.STANDARD_2, "STANDARD_2", "スタンダードパック2（JU+IF）", 3,
					GameConstants.packArtImageWebPath(GameConstants.PACK_ART_FILE_STANDARD_2),
					"pack-detail-standard-2", "スタンダードパック2（JU+IF）"),
			slotDef(PackType.JEWEL_UTOPIA, "JEWEL_UTOPIA", "宝石の秘境パック（JU）", 4,
					GameConstants.packArtImageWebPath(GameConstants.PACK_ART_FILE_JEWEL_UTOPIA),
					"pack-detail-jewel-utopia", "宝石の秘境パック（JU）"),
			slotDef(PackType.IRON_FLEET, "IRON_FLEET", "鉄面の艦隊パック（IF）", 5,
					GameConstants.packArtImageWebPath(GameConstants.PACK_ART_FILE_IRON_FLEET),
					"pack-detail-iron-fleet", "鉄面の艦隊パック（IF）"),
			slotDef(PackType.STANDARD_3, "STANDARD_3", "スタンダードパック3（OT/CS）", 3,
					GameConstants.packArtImageWebPath(GameConstants.PACK_ART_FILE_STANDARD_3),
					"pack-detail-standard-3", "スタンダードパック3（OT/CS）"),
			slotDef(PackType.OCEAN_TIDE, "OCEAN_TIDE", "海底の潮流パック（OT）", 4,
					GameConstants.packArtImageWebPath(GameConstants.PACK_ART_FILE_OCEAN_TIDE),
					"pack-detail-ocean-tide", "海底の潮流パック（OT）"),
			slotDef(PackType.CREATION_SANCTUM, "CREATION_SANCTUM", "創世の神域パック（CS）", 5,
					GameConstants.PACK_ART_WEB_CREATION_SANCTUM,
					"pack-detail-creation-sanctum", "創世の神域パック（CS）"));

	private static PackShopSlotView slotDef(
			PackType type, String param, String name, int cost, String art, String modalId, String heading) {
		return new PackShopSlotView(type, param, true, name, "", cost, art, modalId, heading, List.of());
	}

	private List<PackShopSlotView> buildPackShopSlots(LocalDate today) {
		List<PackShopSlotView> out = new ArrayList<>();
		for (PackShopSlotView def : PACK_SHOP_CATALOG) {
			boolean unlocked = SeasonSchedule.isPackUnlocked(def.packType(), today);
			String unlockHint = unlocked ? "" : SeasonSchedule.unlockLabelJa(
					SeasonSchedule.unlockDateFor(def.packType(), today));
			String displayName = unlocked ? def.displayName() : "？？？？";
			List<PackPreviewLine> preview = unlocked
					? toPreviewLines(packService.sortedEligibleCardsForPreview(def.packType()))
					: List.of(new PackPreviewLine("？？？？", "？？？？", "C"));
			out.add(new PackShopSlotView(
					def.packType(),
					def.packTypeParam(),
					unlocked,
					displayName,
					unlockHint,
					def.cost(),
					def.artWebPath(),
					def.detailModalId(),
					unlocked ? def.detailHeading() : "？？？？",
					preview));
		}
		return out;
	}

	/** 画像差し替え後もブラウザキャッシュで古い絵が残らないよう、最新の lastModified をクエリに付ける。 */
	private static long computePackArtCacheKey() {
		long max = 0L;
		for (String path : PACK_ART_CLASSPATH_FILES) {
			var r = new ClassPathResource(path);
			if (!r.exists()) {
				continue;
			}
			try {
				max = Math.max(max, r.lastModified());
			} catch (IOException ignored) {
				// 取得できなければ他ファイルの値のみ使う
			}
		}
		return max;
	}

	private static long computeCardBackCacheKey() {
		var r = new ClassPathResource(CARD_BACK_CLASSPATH);
		if (!r.exists()) {
			return 0L;
		}
		try {
			return r.lastModified();
		} catch (IOException e) {
			return 0L;
		}
	}

	private static List<PackPreviewLine> toPreviewLines(List<CardDefinition> cards) {
		return cards.stream()
				.map(c -> new PackPreviewLine(
						c.getName() != null ? c.getName() : "",
						rarityLabelJa(c.getRarity()),
						rarityCodeForCss(c.getRarity())))
				.toList();
	}

	private static String rarityCodeForCss(String code) {
		if (code == null || code.isBlank()) {
			return "C";
		}
		String t = code.trim();
		if ("Reg".equalsIgnoreCase(t)) {
			return "Reg";
		}
		if ("Ep".equalsIgnoreCase(t)) {
			return "Ep";
		}
		if ("R".equalsIgnoreCase(t)) {
			return "R";
		}
		return "C";
	}

	private static String rarityLabelJa(String code) {
		if (code == null || code.isBlank()) {
			return "コモン";
		}
		return switch (code.trim()) {
			case "Reg" -> "レジェンダリー";
			case "Ep" -> "エピック";
			case "R" -> "レア";
			case "C" -> "コモン";
			default -> code;
		};
	}

	@PostMapping("/open-starter-gift")
	public String openStarterGift(HttpSession session, RedirectAttributes ra) {
		try {
			long uid = CurrentUser.require().getId();
			session.removeAttribute(SESSION_PACK_RESULT_FROM_BONUS_PACK);
			session.removeAttribute(SESSION_PACK_RESULT_FROM_STARTER_GIFT);
			session.removeAttribute(SESSION_PACK_OPENING_SLOTS);
			session.removeAttribute(SESSION_PACK_LAST_EPITHET_RESULTS);
			session.removeAttribute(SESSION_PACK_LAST_PULLED_NEW_FLAGS);
			var pulled = packService.openStarterGiftStandard1Pack(uid);
			session.setAttribute("pack_last_pulled_ids", pulled.stream().map(r -> r.card().getId()).toList());
			session.setAttribute(SESSION_PACK_LAST_PULLED_NEW_FLAGS, pulled.stream().map(PackService.PackOpenRow::newToCollection).toList());
			session.setAttribute("pack_last_type", PackType.STANDARD.name());
			session.setAttribute(SESSION_PACK_RESULT_FROM_STARTER_GIFT, Boolean.TRUE);
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/pack";
		}
		return "redirect:/pack/opening";
	}

	@PostMapping("/buy")
	public String buy(@RequestParam(name = "type", required = false) String type, HttpSession session,
			RedirectAttributes ra) {
		try {
			long uid = CurrentUser.require().getId();
			session.removeAttribute(SESSION_PACK_RESULT_FROM_BONUS_PACK);
			session.removeAttribute(SESSION_PACK_RESULT_FROM_STARTER_GIFT);
			session.removeAttribute(SESSION_PACK_OPENING_SLOTS);
			session.removeAttribute(SESSION_PACK_LAST_EPITHET_RESULTS);
			session.removeAttribute(SESSION_PACK_LAST_PULLED_NEW_FLAGS);
			PackType t = parsePackType(type);
			var pulled = packService.openPack(uid, t);
			session.setAttribute("pack_last_pulled_ids", pulled.stream().map(r -> r.card().getId()).toList());
			session.setAttribute(SESSION_PACK_LAST_PULLED_NEW_FLAGS, pulled.stream().map(PackService.PackOpenRow::newToCollection).toList());
			session.setAttribute("pack_last_type", t != null ? t.name() : PackType.STANDARD.name());
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/pack";
		}
		return "redirect:/pack/opening";
	}

	@GetMapping("/opening")
	public String opening(Model model, HttpSession session, RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		var fresh = appUserMapper.findById(uid);
		model.addAttribute("gems", fresh != null && fresh.getCoins() != null ? fresh.getCoins() : 0);
		model.addAttribute("cardBackUrl", GameConstants.cardBackUrl());
		model.addAttribute("cardBackCacheKey", CARD_BACK_CACHE_KEY);
		Object slotsObj = session.getAttribute(SESSION_PACK_OPENING_SLOTS);
		List<PackOpeningSlotView> viewSlots = new ArrayList<>();
		if (slotsObj instanceof List<?> rawSlots && !rawSlots.isEmpty() && rawSlots.get(0) instanceof PackOpeningSessionSlot) {
			@SuppressWarnings("unchecked")
			List<PackOpeningSessionSlot> slots = (List<PackOpeningSessionSlot>) slotsObj;
			int idx = 0;
			for (PackOpeningSessionSlot s : slots) {
				if ("EPITHET".equals(s.kind())) {
					viewSlots.add(new PackOpeningSlotView(true, null, s.epithetUpper(), s.epithetLower(), idx++, false));
				} else if (s.cardId() != null) {
					var faces = libraryService.displayFacesForCardIds(List.of(s.cardId()));
					if (!faces.isEmpty()) {
						boolean isNew = Boolean.TRUE.equals(s.newToCollection());
						viewSlots.add(new PackOpeningSlotView(false, faces.get(0), null, null, idx++, isNew));
					}
				}
			}
			session.removeAttribute(SESSION_PACK_OPENING_SLOTS);
		} else {
			Object idsObj = session.getAttribute("pack_last_pulled_ids");
			List<Short> ids = coerceIds(idsObj);
			if (ids.isEmpty()) {
				ra.addFlashAttribute("error", "開封中のパックがありません");
				return "redirect:/pack";
			}
			List<Boolean> newFlags = coerceNewFlags(session.getAttribute(SESSION_PACK_LAST_PULLED_NEW_FLAGS));
			int idx = 0;
			for (int i = 0; i < ids.size(); i++) {
				Short id = ids.get(i);
				var faces = libraryService.displayFacesForCardIds(List.of(id));
				if (!faces.isEmpty()) {
					boolean isNew = i < newFlags.size() && Boolean.TRUE.equals(newFlags.get(i));
					viewSlots.add(new PackOpeningSlotView(false, faces.get(0), null, null, idx++, isNew));
				}
			}
		}
		if (viewSlots.isEmpty()) {
			ra.addFlashAttribute("error", "開封中のパックがありません");
			return "redirect:/pack";
		}
		model.addAttribute("packOpeningViewSlots", viewSlots);
		Object redir = session.getAttribute(SESSION_PACK_AFTER_OPEN_REDIRECT);
		String afterUrl = "/pack/result";
		if (redir instanceof String s && !s.isBlank()) {
			afterUrl = s;
			session.removeAttribute(SESSION_PACK_AFTER_OPEN_REDIRECT);
		}
		model.addAttribute("packOpeningAfterUrl", afterUrl);
		Object themeObj = session.getAttribute(SESSION_PACK_OPENING_THEME);
		String theme = null;
		String themeClass = null;
		if (themeObj instanceof String s && !s.isBlank()) {
			theme = s.trim();
			session.removeAttribute(SESSION_PACK_OPENING_THEME);
			if ("EPIC_PLUS".equals(theme)) {
				themeClass = "epic-plus";
			} else if ("LEGENDARY".equals(theme)) {
				themeClass = "legendary";
			}
		}
		model.addAttribute("packOpeningTheme", theme);
		model.addAttribute("packOpeningThemeClass", themeClass);
		return "pack-opening";
	}

	@GetMapping("/result")
	public String result(Model model, HttpSession session, RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		var fresh = appUserMapper.findById(uid);
		model.addAttribute("gems", fresh != null && fresh.getCoins() != null ? fresh.getCoins() : 0);
		Object idsObj = session.getAttribute("pack_last_pulled_ids");
		List<Short> ids = coerceIds(idsObj);
		Object epObj = session.getAttribute(SESSION_PACK_LAST_EPITHET_RESULTS);
		boolean hasEpithets = epObj instanceof List<?> el && !el.isEmpty();
		if (ids.isEmpty() && !hasEpithets) {
			ra.addFlashAttribute("error", "結果表示できるパックがありません");
			return "redirect:/pack";
		}
		List<LibraryCardView> resultCards = libraryService.displayFacesForCardIds(ids);
		List<Boolean> newFlags = coerceNewFlags(session.getAttribute(SESSION_PACK_LAST_PULLED_NEW_FLAGS));
		for (int i = 0; i < resultCards.size(); i++) {
			if (i < newFlags.size() && Boolean.TRUE.equals(newFlags.get(i))) {
				resultCards.get(i).setHighlightNewFromPack(true);
			}
		}
		model.addAttribute("cards", resultCards);
		if (hasEpithets) {
			model.addAttribute("packResultEpithets", epObj);
			session.removeAttribute(SESSION_PACK_LAST_EPITHET_RESULTS);
		}
		model.addAttribute("contextPath", "");
		model.addAttribute("cardPlateUrl", GameConstants.CARD_LAYER_BASE);
		model.addAttribute("cardDataUrl", GameConstants.CARD_LAYER_DATA);
		Object packTypeObj = session.getAttribute("pack_last_type");
		String lastPackType = PackType.STANDARD.name();
		if (packTypeObj instanceof String s && !s.isBlank()) {
			lastPackType = s;
		}
		model.addAttribute("lastPackType", lastPackType);
		Object bonusObj = session.getAttribute(SESSION_PACK_RESULT_FROM_BONUS_PACK);
		boolean fromBonusPack = Boolean.TRUE.equals(bonusObj);
		model.addAttribute("packResultFromBonusPack", fromBonusPack);
		boolean bonusAutoAdvance = false;
		if (fromBonusPack && fresh != null) {
			int bank = fresh.getTimePackBonusBank() != null ? Math.max(0, fresh.getTimePackBonusBank()) : 0;
			var gauge = timePackGaugeService.snapshotForUser(uid);
			bonusAutoAdvance = gauge.availablePacks() + bank > 0;
		}
		model.addAttribute("packResultBonusAutoAdvance", bonusAutoAdvance);
		Object starterGiftObj = session.getAttribute(SESSION_PACK_RESULT_FROM_STARTER_GIFT);
		model.addAttribute("packResultFromStarterGift", Boolean.TRUE.equals(starterGiftObj));
		session.removeAttribute(SESSION_PACK_RESULT_FROM_BONUS_PACK);
		session.removeAttribute(SESSION_PACK_RESULT_FROM_STARTER_GIFT);
		return "pack-result";
	}

	private static PackType parsePackType(String raw) {
		if (raw == null || raw.isBlank()) return PackType.STANDARD;
		String s = raw.trim().toUpperCase();
		return switch (s) {
			case "STANDARD" -> PackType.STANDARD;
			case "WINDY_HILL" -> PackType.WINDY_HILL;
			case "EVIL_THREAT" -> PackType.EVIL_THREAT;
			case "STANDARD_2" -> PackType.STANDARD_2;
			case "JEWEL_UTOPIA" -> PackType.JEWEL_UTOPIA;
			case "IRON_FLEET" -> PackType.IRON_FLEET;
			case "STANDARD_3" -> PackType.STANDARD_3;
			case "OCEAN_TIDE" -> PackType.OCEAN_TIDE;
			case "CREATION_SANCTUM" -> PackType.CREATION_SANCTUM;
			default -> PackType.STANDARD;
		};
	}

	@SuppressWarnings("unchecked")
	private static List<Short> coerceIds(Object pulledIds) {
		if (!(pulledIds instanceof List<?> raw) || raw.isEmpty()) {
			return List.of();
		}
		List<Short> ids = new ArrayList<>();
		for (Object o : raw) {
			if (o instanceof Short s) {
				ids.add(s);
			} else if (o instanceof Number n) {
				ids.add(n.shortValue());
			}
		}
		return ids;
	}

	private static List<Boolean> coerceNewFlags(Object raw) {
		if (!(raw instanceof List<?> list) || list.isEmpty()) {
			return List.of();
		}
		List<Boolean> out = new ArrayList<>();
		for (Object o : list) {
			if (o instanceof Boolean b) {
				out.add(b);
			}
		}
		return out;
	}
}
