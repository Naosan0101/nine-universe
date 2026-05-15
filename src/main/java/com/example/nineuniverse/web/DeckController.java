package com.example.nineuniverse.web;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.dev.DevTestUserLoginBaselineService;
import com.example.nineuniverse.domain.Deck;
import com.example.nineuniverse.domain.LibraryCardView;
import com.example.nineuniverse.service.DeckService;
import com.example.nineuniverse.service.LibraryService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/decks")
@RequiredArgsConstructor
public class DeckController {

	private final DeckService deckService;
	private final LibraryService libraryService;
	private final DevTestUserLoginBaselineService devTestUserLoginBaselineService;

	@GetMapping
	public String list(Model model, HttpServletRequest request) {
		long uid = CurrentUser.require().getId();
		devTestUserLoginBaselineService.syncTestuserCollectionOnlyIfLocal(request);
		model.addAttribute("decks", deckService.listDecks(uid));
		model.addAttribute("leagueSets", deckService.listLeagueDeckSetSummaries(uid));
		String cp = request.getContextPath();
		model.addAttribute("contextPath", cp != null ? cp : "");
		addDeckEditStaticUrls(model);
		return "decks-list";
	}

	@GetMapping("/new")
	public String newForm(Model model, HttpServletRequest request) {
		long uid = CurrentUser.require().getId();
		devTestUserLoginBaselineService.syncTestuserCollectionOnlyIfLocal(request);
		var library = libraryService.library(uid);
		int maxBuildable = maxBuildableDeckSlots(library);
		boolean noOwned = library.stream().noneMatch(LibraryCardView::isOwned);
		model.addAttribute("library", library);
		model.addAttribute("deckName", "");
		model.addAttribute("selectedIds", List.<Short>of());
		model.addAttribute("editDeckId", null);
		model.addAttribute("leagueBlockedCsv", "");
		model.addAttribute("noOwnedCards", noOwned);
		model.addAttribute("insufficientCardsForDeck", !noOwned && maxBuildable < 8);
		model.addAttribute("maxBuildableSlots", maxBuildable);
		addDeckEditStaticUrls(model);
		return "deck-edit";
	}

	@PostMapping("/league/set/new")
	public String createLeagueSet(@RequestParam(defaultValue = "") String name, RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		try {
			deckService.createLeagueDeckSet(uid, name);
			ra.addFlashAttribute("msg", "リーグデッキを作成しました。デッキ1・2を編集してください。");
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/decks";
	}

	@PostMapping("/league/set/{setId}/delete")
	public String deleteLeagueSet(@PathVariable long setId, RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		try {
			deckService.deleteLeagueSet(uid, setId);
			ra.addFlashAttribute("msg", "リーグデッキを削除しました");
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/decks";
	}

	@GetMapping("/league/set/{setId}/slot/{slot}/edit")
	public String editLeagueSlot(@PathVariable long setId, @PathVariable int slot, Model model, HttpServletRequest request,
			RedirectAttributes ra) {
		if (slot != 1 && slot != 2) {
			ra.addFlashAttribute("error", "スロットが不正です");
			return "redirect:/decks";
		}
		long uid = CurrentUser.require().getId();
		devTestUserLoginBaselineService.syncTestuserCollectionOnlyIfLocal(request);
		try {
			long deckId = deckService.deckIdForLeagueSlot(uid, setId, slot);
			var deck = deckService.requireDeck(uid, deckId);
			int sib = slot == 1 ? 2 : 1;
			long sibDeckId = deckService.deckIdForLeagueSlot(uid, setId, sib);
			List<Short> blocked = deckService.cardIdsForDeck(sibDeckId);
			var library = libraryService.library(uid);
			int maxBuildable = maxBuildableDeckSlots(library);
			boolean noOwned = library.stream().noneMatch(LibraryCardView::isOwned);
			model.addAttribute("library", library);
			model.addAttribute("deckName", deck.getName());
			model.addAttribute("selectedIds", deckService.cardIdsForDeck(deckId));
			model.addAttribute("editDeckId", deckId);
			model.addAttribute("leagueBlockedCardIds", blocked);
			model.addAttribute(
					"leagueBlockedCsv",
					blocked.stream().map(String::valueOf).collect(Collectors.joining(",")));
			model.addAttribute("noOwnedCards", noOwned);
			model.addAttribute("insufficientCardsForDeck", !noOwned && maxBuildable < 8);
			model.addAttribute("maxBuildableSlots", maxBuildable);
			addDeckEditStaticUrls(model);
			return "deck-edit";
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/decks";
		}
	}

	@GetMapping(value = "/{id}/preview", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<Map<String, Object>> deckPreview(@PathVariable long id) {
		long uid = CurrentUser.require().getId();
		deckService.requireDeck(uid, id);
		List<LibraryCardView> views = libraryService.displayFacesForCardIds(deckService.cardIdsForDeck(id));
		return views.stream().map(DeckController::previewJson).collect(Collectors.toList());
	}

	private static Map<String, Object> previewJson(LibraryCardView v) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", v.getId());
		m.put("name", v.getName());
		m.put("layerBase", v.getLayerBasePath());
		m.put("layerPortrait", v.getLayerPortraitPath());
		m.put("layerPortraitAlt", v.getLayerPortraitPathAlt() != null ? v.getLayerPortraitPathAlt() : "");
		m.put("layerBar", v.getBarImagePath());
		m.put("layerFrame", v.getLayerFramePath());
		m.put("attribute", v.getAttribute());
		m.put("rarity", v.getRarity());
		m.put("rarityLabel", v.getRarityLabel());
		m.put("cost", v.getCost());
		m.put("power", v.getBasePower());
		m.put("fieldCard", v.isFieldCard());
		m.put("attributeLabelJa", v.getAttributeLabelJa());
		m.put("ability", v.getLibraryAbilityText());
		m.put("canonicalLine", v.getCanonicalAbilityLine());
		m.put("deployHelp", v.getDeployHelp());
		m.put("passiveHelp", v.getPassiveHelp());
		List<String> lines = v.getAttributeLabelLines();
		m.put("attrLines", lines != null ? lines : List.of());
		return m;
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable long id, Model model, HttpServletRequest request) {
		long uid = CurrentUser.require().getId();
		devTestUserLoginBaselineService.syncTestuserCollectionOnlyIfLocal(request);
		var deck = deckService.requireDeck(uid, id);
		model.addAttribute("library", libraryService.library(uid));
		model.addAttribute("deckName", deck.getName());
		model.addAttribute("selectedIds", deckService.cardIdsForDeck(id));
		model.addAttribute("editDeckId", id);
		model.addAttribute("leagueBlockedCsv", "");
		addDeckEditStaticUrls(model);
		return "deck-edit";
	}

	private static void addDeckEditStaticUrls(Model model) {
		model.addAttribute("cardPlateUrl", GameConstants.CARD_LAYER_BASE);
		model.addAttribute("cardDataUrl", GameConstants.CARD_LAYER_DATA);
	}

	@PostMapping("/save")
	public String save(@RequestParam String name, @RequestParam String cardIds,
			@RequestParam(required = false) Long editDeckId, RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		try {
			List<Short> ids;
			try {
				ids = parseIds(cardIds);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("カードIDの形式が不正です");
			}
			if (editDeckId != null) {
				deckService.updateDeck(uid, editDeckId, name, ids);
				ra.addFlashAttribute("msg", "デッキを保存しました");
			} else {
				deckService.createDeck(uid, name, ids);
				ra.addFlashAttribute("msg", "デッキを作成しました");
			}
			return "redirect:/decks";
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
			if (editDeckId != null) {
				try {
					var d = deckService.requireDeck(uid, editDeckId);
					if (d.getLeagueSetId() != null && d.getLeagueSlot() != null) {
						return "redirect:/decks/league/set/" + d.getLeagueSetId() + "/slot/" + d.getLeagueSlot() + "/edit";
					}
				} catch (IllegalArgumentException ignored) {
					/* fall through */
				}
				return "redirect:/decks/" + editDeckId + "/edit";
			}
			return "redirect:/decks/new";
		}
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable long id, RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		try {
			deckService.deleteDeck(uid, id);
			ra.addFlashAttribute("msg", "デッキを削除しました");
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/decks";
	}

	/** 同一カード最大2枚ルール（deck-edit.js と一致）でデッキに並べられる枚数の上限 */
	private static int maxBuildableDeckSlots(List<LibraryCardView> library) {
		return library.stream()
				.filter(LibraryCardView::isOwned)
				.mapToInt(v -> Math.min(2, v.getQuantity()))
				.sum();
	}

	private static List<Short> parseIds(String cardIds) {
		if (cardIds == null || cardIds.isBlank()) {
			return new ArrayList<>();
		}
		return Arrays.stream(cardIds.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(Short::parseShort)
				.collect(Collectors.toList());
	}
}
