package com.example.nineuniverse.web;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.service.LibraryService;
import com.example.nineuniverse.service.PackService;
import com.example.nineuniverse.service.PackService.PackType;
import com.example.nineuniverse.service.NicknameEpithetService;
import com.example.nineuniverse.service.RecycleService;
import com.example.nineuniverse.web.dto.PackRarityRateRow;
import com.example.nineuniverse.web.dto.RecycleStandardPackOption;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recycle")
@RequiredArgsConstructor
public class RecycleController {

	/** リサイクル確定パックの 1〜3 枚目（レジェンダリー以外）の内訳。{@link RecycleService} の抽選と一致。 */
	private static final List<PackRarityRateRow> RECYCLE_FIRST_THREE_SLOT_RATES = List.of(
			new PackRarityRateRow("エピック", "10%"),
			new PackRarityRateRow("レア", "30%"),
			new PackRarityRateRow("コモン", "60%"));

	/** エピック以上確定パックの 4 枚目のみ。{@link RecycleService} の抽選と一致。 */
	private static final List<PackRarityRateRow> RECYCLE_FOURTH_SLOT_EPIC_PLUS_RATES = List.of(
			new PackRarityRateRow("レジェンダリー", "10%"),
			new PackRarityRateRow("エピック", "90%"));

	private final RecycleService recycleService;
	private final AppUserMapper appUserMapper;
	private final LibraryService libraryService;
	private final PackService packService;
	private final NicknameEpithetService nicknameEpithetService;

	@GetMapping
	public String hub(Model model) {
		long uid = CurrentUser.require().getId();
		addRecycleBalances(model, uid);
		addRecycleShopCosts(model);
		model.addAttribute("crystalPerGem", GameConstants.RECYCLE_CRYSTAL_PER_GEM);
		return "recycle-hub";
	}

	@GetMapping("/cards")
	public String cardsPage(Model model, HttpServletRequest request) {
		long uid = CurrentUser.require().getId();
		model.addAttribute("lines", recycleService.recycleInventory(uid));
		addRecycleBalances(model, uid);
		model.addAttribute("crystalPerCardC", GameConstants.RECYCLE_CRYSTAL_PER_CARD_C);
		model.addAttribute("crystalPerCardR", GameConstants.RECYCLE_CRYSTAL_PER_CARD_R);
		model.addAttribute("crystalPerCardEp", GameConstants.RECYCLE_CRYSTAL_PER_CARD_EP);
		model.addAttribute("crystalPerCardReg", GameConstants.RECYCLE_CRYSTAL_PER_CARD_REG);
		String cp = request.getContextPath();
		model.addAttribute("contextPath", cp != null ? cp : "");
		model.addAttribute("cardPlateUrl", GameConstants.CARD_LAYER_BASE);
		model.addAttribute("cardDataUrl", GameConstants.CARD_LAYER_DATA);
		return "recycle-cards";
	}

	private void addRecycleBalances(Model model, long uid) {
		var u = appUserMapper.findById(uid);
		model.addAttribute("recycleCrystal", u != null && u.getRecycleCrystal() != null ? u.getRecycleCrystal() : 0);
		model.addAttribute("gems", u != null && u.getCoins() != null ? u.getCoins() : 0);
	}

	private static void addRecycleShopCosts(Model model) {
		model.addAttribute("costLegendaryPick", GameConstants.RECYCLE_SHOP_LEGENDARY_PICK_CRYSTAL);
		model.addAttribute("costLegendaryPack", GameConstants.RECYCLE_SHOP_LEGENDARY_PACK_CRYSTAL);
		model.addAttribute("costEpicPlusPack", GameConstants.RECYCLE_SHOP_EPIC_PLUS_PACK_CRYSTAL);
		model.addAttribute("costEpithetGacha", GameConstants.RECYCLE_SHOP_EPITHET_GACHA_CRYSTAL);
	}

	@PostMapping("/cards/surplus-keep-two")
	public String recycleSurplusKeepTwo(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		try {
			int gained = recycleService.recycleSurplusKeepingTwoPerCard(uid);
			ra.addFlashAttribute("recycleSuccess", "クリスタルを " + gained + " 獲得しました。");
		} catch (IllegalArgumentException | IllegalStateException e) {
			ra.addFlashAttribute("recycleError", e.getMessage());
		}
		return "redirect:/recycle/cards";
	}

	@PostMapping("/cards")
	public String recycleCards(@RequestParam Map<String, String> params, RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		Map<Short, Integer> req = new HashMap<>();
		for (Map.Entry<String, String> e : params.entrySet()) {
			String k = e.getKey();
			if (k == null || !k.startsWith("qty_")) {
				continue;
			}
			String idPart = k.substring("qty_".length());
			short cardId;
			try {
				cardId = Short.parseShort(idPart);
			} catch (NumberFormatException ex) {
				continue;
			}
			String raw = e.getValue();
			if (raw == null || raw.isBlank()) {
				continue;
			}
			int q;
			try {
				q = Integer.parseInt(raw.trim());
			} catch (NumberFormatException ex) {
				ra.addFlashAttribute("recycleError", "枚数の形式が不正です。");
				return "redirect:/recycle/cards";
			}
			if (q > 0) {
				req.merge(cardId, q, Integer::sum);
			}
		}
		try {
			int gained = recycleService.recycleCards(uid, req);
			ra.addFlashAttribute("recycleSuccess", "クリスタルを " + gained + " 獲得しました。");
		} catch (IllegalArgumentException | IllegalStateException e) {
			ra.addFlashAttribute("recycleError", e.getMessage());
		}
		return "redirect:/recycle/cards";
	}

	@PostMapping("/crystal-to-gems")
	public String crystalToGems(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		try {
			recycleService.exchangeCrystalForOneGem(uid);
			ra.addFlashAttribute("recycleSuccess",
					GameConstants.RECYCLE_CRYSTAL_PER_GEM + "クリスタルを1ジェムに交換しました。");
		} catch (IllegalArgumentException | IllegalStateException e) {
			ra.addFlashAttribute("recycleError", e.getMessage());
		}
		return "redirect:/recycle";
	}

	@GetMapping("/open/epic-plus")
	public String openEpicPlusPickPage(Model model, HttpServletRequest request) {
		addRecyclePackPickPage(model, request, false);
		return "recycle-open-pack-pick";
	}

	@GetMapping("/open/legendary")
	public String openLegendaryPickPage(Model model, HttpServletRequest request) {
		addRecyclePackPickPage(model, request, true);
		return "recycle-open-pack-pick";
	}

	private void addRecyclePackPickPage(Model model, HttpServletRequest request, boolean legendary) {
		long uid = CurrentUser.require().getId();
		addRecycleBalances(model, uid);
		model.addAttribute("recycleOpenLegendary", legendary);
		model.addAttribute("recycleUriSuffix", legendary ? "legendary" : "epic-plus");
		model.addAttribute("recyclePickPackTitle", legendary ? "レジェンダリー確定パック" : "エピック以上確定パック");
		model.addAttribute(
				"recyclePickPackCost",
				legendary ? GameConstants.RECYCLE_SHOP_LEGENDARY_PACK_CRYSTAL : GameConstants.RECYCLE_SHOP_EPIC_PLUS_PACK_CRYSTAL);
		model.addAttribute("recycleStandardPackOptions", buildRecycleStandardPackOptions());
		model.addAttribute("recycleFirstThreeSlotRates", RECYCLE_FIRST_THREE_SLOT_RATES);
		if (!legendary) {
			model.addAttribute("recycleFourthSlotRates", RECYCLE_FOURTH_SLOT_EPIC_PLUS_RATES);
		}
		model.addAttribute("standardPackPreview", PackController.buildPackPreviewLines(packService, PackType.STANDARD));
		model.addAttribute("standard2PackPreview", PackController.buildPackPreviewLines(packService, PackType.STANDARD_2));
		model.addAttribute("packArtCacheKey", PackController.getPackArtCacheKey());
		String cp = request.getContextPath();
		model.addAttribute("contextPath", cp != null ? cp : "");
	}

	private List<RecycleStandardPackOption> buildRecycleStandardPackOptions() {
		List<RecycleStandardPackOption> list = new ArrayList<>();
		for (PackType t : packService.recycleStandardBundlePackTypes()) {
			RecycleStandardPackOption o = new RecycleStandardPackOption();
			o.setPackTypeParam(t.name());
			o.setDisplayName(switch (t) {
				case STANDARD -> "スタンダードパック1（WH+ET）";
				case STANDARD_2 -> "スタンダードパック2（JU+IF）";
				default -> t.name();
			});
			o.setPackThumbKey(switch (t) {
				case STANDARD -> "standard1";
				case STANDARD_2 -> "standard2";
				default -> "standard1";
			});
			o.setPackThumbUrl(switch (t) {
				case STANDARD -> GameConstants.packArtImageWebPath(GameConstants.PACK_ART_FILE_STANDARD_1);
				case STANDARD_2 -> GameConstants.packArtImageWebPath(GameConstants.PACK_ART_FILE_STANDARD_2);
				default -> GameConstants.packArtImageWebPath(GameConstants.PACK_ART_FILE_STANDARD_1);
			});
			o.setPackDetailModalId(t == PackType.STANDARD ? "pack-detail-standard" : "pack-detail-standard-2");
			list.add(o);
		}
		return list;
	}

	private static PackType parseRecycleStandardPackParam(String raw, PackService packService) {
		if (raw == null || raw.isBlank()) {
			throw new IllegalArgumentException("スタンダードパックを選んでください。");
		}
		PackType t;
		try {
			t = PackType.valueOf(raw.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("パックの指定が不正です。");
		}
		if (!packService.isRecycleStandardBundlePack(t)) {
			throw new IllegalArgumentException("パックの指定が不正です。");
		}
		return t;
	}

	@PostMapping("/open/epic-plus")
	public String openEpicPlus(
			HttpSession session,
			RedirectAttributes ra,
			@RequestParam(value = "standardPack", required = false) String standardPackRaw) {
		long uid = CurrentUser.require().getId();
		try {
			PackType t = parseRecycleStandardPackParam(standardPackRaw, packService);
			List<CardDefinition> pulled = recycleService.openEpicPlusRecyclePack(uid, t);
			session.setAttribute("pack_last_pulled_ids", pulled.stream().map(CardDefinition::getId).toList());
			session.setAttribute("pack_last_type", "RECYCLE_EPIC_PLUS");
			session.setAttribute(PackController.SESSION_PACK_OPENING_THEME, "EPIC_PLUS");
			session.setAttribute(PackController.SESSION_PACK_AFTER_OPEN_REDIRECT, "/recycle");
		} catch (IllegalArgumentException | IllegalStateException e) {
			ra.addFlashAttribute("recycleError", e.getMessage());
			return "redirect:/recycle/open/epic-plus";
		}
		return "redirect:/pack/opening";
	}

	@PostMapping("/open/legendary")
	public String openLegendary(
			HttpSession session,
			RedirectAttributes ra,
			@RequestParam(value = "standardPack", required = false) String standardPackRaw) {
		long uid = CurrentUser.require().getId();
		try {
			PackType t = parseRecycleStandardPackParam(standardPackRaw, packService);
			List<CardDefinition> pulled = recycleService.openLegendaryRecyclePack(uid, t);
			session.setAttribute("pack_last_pulled_ids", pulled.stream().map(CardDefinition::getId).toList());
			session.setAttribute("pack_last_type", "RECYCLE_LEGENDARY");
			session.setAttribute(PackController.SESSION_PACK_OPENING_THEME, "LEGENDARY");
			session.setAttribute(PackController.SESSION_PACK_AFTER_OPEN_REDIRECT, "/recycle");
		} catch (IllegalArgumentException | IllegalStateException e) {
			ra.addFlashAttribute("recycleError", e.getMessage());
			return "redirect:/recycle/open/legendary";
		}
		return "redirect:/pack/opening";
	}

	@GetMapping("/legendary-pick")
	public String legendaryPick(Model model, HttpServletRequest request) {
		long uid = CurrentUser.require().getId();
		var u = appUserMapper.findById(uid);
		model.addAttribute("legendaryCards", libraryService.legendaryDefinitionsAsViews());
		model.addAttribute("recycleCrystal", u != null && u.getRecycleCrystal() != null ? u.getRecycleCrystal() : 0);
		model.addAttribute("costLegendaryPick", GameConstants.RECYCLE_SHOP_LEGENDARY_PICK_CRYSTAL);
		String cp = request.getContextPath();
		model.addAttribute("contextPath", cp != null ? cp : "");
		model.addAttribute("cardPlateUrl", GameConstants.CARD_LAYER_BASE);
		model.addAttribute("cardDataUrl", GameConstants.CARD_LAYER_DATA);
		return "recycle-legendary-pick";
	}

	@PostMapping("/legendary-pick/confirm")
	public Object legendaryPickConfirm(
			@RequestParam("cardId") short cardId,
			@RequestHeader(value = "Accept", required = false) String accept,
			RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		boolean wantJson = accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE);
		try {
			recycleService.claimLegendaryPick(uid, cardId);
			if (wantJson) {
				var u = appUserMapper.findById(uid);
				int crystal = u != null && u.getRecycleCrystal() != null ? u.getRecycleCrystal() : 0;
				Map<String, Object> body = new HashMap<>();
				body.put("ok", true);
				body.put("message", "レジェンダリーカードを獲得しました。");
				body.put("recycleCrystal", crystal);
				return ResponseEntity.ok(body);
			}
			ra.addFlashAttribute("recycleSuccess", "レジェンダリーカードを獲得しました。");
		} catch (IllegalArgumentException | IllegalStateException e) {
			if (wantJson) {
				Map<String, Object> err = new HashMap<>();
				err.put("ok", false);
				err.put("error", e.getMessage());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
			}
			ra.addFlashAttribute("recycleError", e.getMessage());
		}
		return "redirect:/recycle";
	}

	@GetMapping("/epithet-gacha")
	public String epithetGachaPage(Model model) {
		long uid = CurrentUser.require().getId();
		addRecycleBalances(model, uid);
		addRecycleShopCosts(model);
		model.addAttribute("canRollEpithetGacha", nicknameEpithetService.canRollGacha(uid));
		return "recycle-epithet-gacha";
	}

	@PostMapping("/epithet-gacha/roll")
	public Object epithetGachaRoll(
			@RequestHeader(value = "Accept", required = false) String accept,
			RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		boolean wantJson = accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE);
		try {
			var r = nicknameEpithetService.rollGacha(uid);
			if (wantJson) {
				var u = appUserMapper.findById(uid);
				int crystal = u != null && u.getRecycleCrystal() != null ? u.getRecycleCrystal() : 0;
				Map<String, Object> body = new HashMap<>();
				body.put("ok", true);
				body.put("upperGained", r.upperGained());
				body.put("lowerGained", r.lowerGained());
				body.put("recycleCrystal", crystal);
				body.put("canRollEpithetGacha", nicknameEpithetService.canRollGacha(uid));
				return ResponseEntity.ok(body);
			}
			ra.addFlashAttribute(
					"recycleSuccess",
					"二つ名を獲得しました：〈" + r.upperGained() + "〉〈" + r.lowerGained() + "〉");
		} catch (IllegalArgumentException | IllegalStateException e) {
			if (wantJson) {
				Map<String, Object> err = new HashMap<>();
				err.put("ok", false);
				err.put("error", e.getMessage());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
			}
			ra.addFlashAttribute("recycleError", e.getMessage());
		}
		return "redirect:/recycle/epithet-gacha";
	}
}
