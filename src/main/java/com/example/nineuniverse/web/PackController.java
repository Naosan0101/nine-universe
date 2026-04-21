package com.example.nineuniverse.web;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.service.LibraryService;
import com.example.nineuniverse.service.PackService;
import com.example.nineuniverse.service.PackService.PackType;
import com.example.nineuniverse.web.dto.PackPreviewLine;
import com.example.nineuniverse.web.dto.PackRarityRateRow;
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

	/** 開封画面の背景演出（セッション）。値: {@code EPIC_PLUS} / {@code LEGENDARY}。未設定時は通常。 */
	public static final String SESSION_PACK_OPENING_THEME = "pack_opening_theme";

	/**
	 * 時間パックのボーナス開封から結果画面へ来た場合 true（表示後に {@link #result} でクリア）。
	 * 右上ナビを「ホームに戻る」のみにする。
	 */
	public static final String SESSION_PACK_RESULT_FROM_BONUS_PACK = "pack_result_from_bonus_pack";

	private static final List<PackRarityRateRow> PACK_RARITY_RATES = List.of(
			new PackRarityRateRow("レジェンダリー", "2%"),
			new PackRarityRateRow("エピック", "10%"),
			new PackRarityRateRow("レア", "30%"),
			new PackRarityRateRow("コモン", "58%"));

	private final PackService packService;
	private final AppUserMapper appUserMapper;
	private final LibraryService libraryService;

	/** クラスパス走査用。実ファイルは NFD のため {@link Normalizer#normalize} で揃える。 */
	private static String packArtClasspathRel(String logicalNfcFileName) {
		return "static/images/cards/" + Normalizer.normalize(logicalNfcFileName, Normalizer.Form.NFD);
	}

	private static final List<String> PACK_ART_CLASSPATH_FILES = List.of(
			packArtClasspathRel("スタンダードパック1.PNG"),
			packArtClasspathRel("風吹く丘パック.PNG"),
			packArtClasspathRel("邪悪なる脅威パック.PNG"),
			packArtClasspathRel("スタンダードパック2.PNG"),
			packArtClasspathRel("宝石の秘境パック.PNG"),
			packArtClasspathRel("鉄面の艦隊パック.PNG"));

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
		model.addAttribute("packArtCacheKey", PACK_ART_CACHE_KEY);
		model.addAttribute("standardPackImage", GameConstants.packArtImageUrl("スタンダードパック1.PNG"));
		model.addAttribute("windyHillPackImage", GameConstants.packArtImageUrl("風吹く丘パック.PNG"));
		model.addAttribute("evilThreatPackImage", GameConstants.packArtImageUrl("邪悪なる脅威パック.PNG"));
		// 新パック（画像が未実装でも購入画面が崩れないよう、存在しない場合は onerror で非表示）
		model.addAttribute("standard2PackImage", GameConstants.packArtImageUrl("スタンダードパック2.PNG"));
		model.addAttribute("jewelUtopiaPackImage", GameConstants.packArtImageUrl("宝石の秘境パック.PNG"));
		model.addAttribute("ironFleetPackImage", GameConstants.packArtImageUrl("鉄面の艦隊パック.PNG"));
		model.addAttribute("gems", fresh != null && fresh.getCoins() != null ? fresh.getCoins() : 0);
		int starterGift = fresh != null && fresh.getStarterGiftStandard1Remaining() != null
				? fresh.getStarterGiftStandard1Remaining()
				: 0;
		model.addAttribute("starterGiftStandard1Remaining", starterGift);
		model.addAttribute("packRarityRates", PACK_RARITY_RATES);
		model.addAttribute("standardPackPreview", toPreviewLines(packService.sortedEligibleCardsForPreview(PackType.STANDARD)));
		model.addAttribute("windyHillPackPreview", toPreviewLines(packService.sortedEligibleCardsForPreview(PackType.WINDY_HILL)));
		model.addAttribute("evilThreatPackPreview", toPreviewLines(packService.sortedEligibleCardsForPreview(PackType.EVIL_THREAT)));
		model.addAttribute("standard2PackPreview", toPreviewLines(packService.sortedEligibleCardsForPreview(PackType.STANDARD_2)));
		model.addAttribute("jewelUtopiaPackPreview", toPreviewLines(packService.sortedEligibleCardsForPreview(PackType.JEWEL_UTOPIA)));
		model.addAttribute("ironFleetPackPreview", toPreviewLines(packService.sortedEligibleCardsForPreview(PackType.IRON_FLEET)));
		return "pack-buy";
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
			var pulled = packService.openStarterGiftStandard1Pack(uid);
			List<Short> ids = pulled.stream().map(c -> c.getId()).toList();
			session.setAttribute("pack_last_pulled_ids", ids);
			session.setAttribute("pack_last_type", PackType.STANDARD.name());
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
			PackType t = parsePackType(type);
			var pulled = packService.openPack(uid, t);
			List<Short> ids = pulled.stream().map(c -> c.getId()).toList();
			session.setAttribute("pack_last_pulled_ids", ids);
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
		Object idsObj = session.getAttribute("pack_last_pulled_ids");
		List<Short> ids = coerceIds(idsObj);
		if (ids.isEmpty()) {
			ra.addFlashAttribute("error", "開封中のパックがありません");
			return "redirect:/pack";
		}
		model.addAttribute("pulledFaces", libraryService.displayFacesForCardIds(ids));
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
		model.addAttribute("packImage", GameConstants.packImageUrl());
		Object idsObj = session.getAttribute("pack_last_pulled_ids");
		List<Short> ids = coerceIds(idsObj);
		if (ids.isEmpty()) {
			ra.addFlashAttribute("error", "結果表示できるパックがありません");
			return "redirect:/pack";
		}
		model.addAttribute("cards", libraryService.displayFacesForCardIds(ids));
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
		session.removeAttribute(SESSION_PACK_RESULT_FROM_BONUS_PACK);
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
}
