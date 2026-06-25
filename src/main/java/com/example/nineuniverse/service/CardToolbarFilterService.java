package com.example.nineuniverse.service;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.card.CardAttributeLabels;
import com.example.nineuniverse.card.CardAttributes;
import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.season.SeasonSchedule;
import com.example.nineuniverse.web.dto.ToolbarFilterOptionRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ライブラリ／デッキ編集／リサイクル等のツールバー絞り込み候補。
 * その日付でアンロックされているカード集合から種族・パック・コスト・強さ等を導出する。
 */
@Service
@RequiredArgsConstructor
public class CardToolbarFilterService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final List<String> TRIBE_ORDER = List.of(
			"HUMAN", "ELF", "UNDEAD", "DRAGON", "MACHINE", "CARBUNCLE", "MERFOLK", "COMIC", "ANGEL");

	private final CardCatalogService cardCatalogService;

	public Map<String, List<ToolbarFilterOptionRow>> optionsForToday() {
		return optionsForDate(LocalDate.now(ZoneId.systemDefault()));
	}

	public Map<String, List<ToolbarFilterOptionRow>> optionsForDate(LocalDate today) {
		List<CardDefinition> pool = unlockedCatalogCards(today);
		Map<String, List<ToolbarFilterOptionRow>> out = new LinkedHashMap<>();
		out.put("tribe", tribeOptions(pool));
		out.put("cardKind", cardKindOptions(today));
		out.put("pack", packOptions(today));
		out.put("cost", numericRangeOptions(pool, true));
		out.put("power", numericRangeOptions(pool, false));
		out.put("rarity", rarityOptions(pool));
		out.put("libSort", libSortOptions());
		return out;
	}

	public String optionsJsonForToday() {
		try {
			return OBJECT_MAPPER.writeValueAsString(optionsForToday());
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("toolbar filter options JSON", e);
		}
	}

	private List<CardDefinition> unlockedCatalogCards(LocalDate today) {
		boolean fieldUnlocked = SeasonSchedule.isCpuAdvancedUnlocked(today);
		List<CardDefinition> out = new ArrayList<>();
		for (CardDefinition c : cardCatalogService.all()) {
			if (c == null || GameConstants.excludedFromPackOpenAndLibraryListing(c.getId())) {
				continue;
			}
			if (!SeasonSchedule.isPackInitialVisible(c.getPackInitial(), today)) {
				continue;
			}
			if (isFieldCard(c) && !fieldUnlocked) {
				continue;
			}
			out.add(c);
		}
		return out;
	}

	private static boolean isFieldCard(CardDefinition c) {
		return c.getCardKind() != null && "FIELD".equalsIgnoreCase(c.getCardKind().trim());
	}

	private static List<ToolbarFilterOptionRow> tribeOptions(List<CardDefinition> pool) {
		Set<String> tribes = new LinkedHashSet<>();
		for (CardDefinition c : pool) {
			for (String seg : CardAttributes.segments(c.getAttribute())) {
				tribes.add(seg);
			}
			String primary = CardAttributes.primarySegment(c.getAttribute());
			if (!primary.isBlank()) {
				tribes.add(primary);
			}
		}
		List<ToolbarFilterOptionRow> rows = new ArrayList<>();
		rows.add(new ToolbarFilterOptionRow("", "すべて"));
		for (String code : TRIBE_ORDER) {
			if (tribes.contains(code)) {
				rows.add(new ToolbarFilterOptionRow(code, CardAttributeLabels.japaneseName(code)));
			}
		}
		return rows;
	}

	private static List<ToolbarFilterOptionRow> cardKindOptions(LocalDate today) {
		List<ToolbarFilterOptionRow> rows = new ArrayList<>();
		rows.add(new ToolbarFilterOptionRow("", "すべて"));
		rows.add(new ToolbarFilterOptionRow("fighter", "ファイター"));
		if (SeasonSchedule.isCpuAdvancedUnlocked(today)) {
			rows.add(new ToolbarFilterOptionRow("field", "フィールド"));
		}
		return rows;
	}

	private static List<ToolbarFilterOptionRow> packOptions(LocalDate today) {
		Set<String> visible = SeasonSchedule.visiblePackInitials(today);
		List<ToolbarFilterOptionRow> rows = new ArrayList<>();
		rows.add(new ToolbarFilterOptionRow("", "すべて"));
		if (visible.contains("STD") && visible.contains("WH") && visible.contains("ET")) {
			rows.add(new ToolbarFilterOptionRow("STANDARD_1", "スタンダードパック1（WH/ET）"));
		}
		if (visible.contains("JU") && visible.contains("IF")) {
			rows.add(new ToolbarFilterOptionRow("STANDARD_2", "スタンダードパック2（JU/IF）"));
		}
		if (visible.contains("OT") && visible.contains("CS")) {
			rows.add(new ToolbarFilterOptionRow("STANDARD_3", "スタンダードパック3（OT/CS）"));
		}
		if (visible.contains("WH")) {
			rows.add(new ToolbarFilterOptionRow("WH", "風吹く丘パック（WH）"));
		}
		if (visible.contains("ET")) {
			rows.add(new ToolbarFilterOptionRow("ET", "邪悪なる脅威パック（ET）"));
		}
		if (visible.contains("JU")) {
			rows.add(new ToolbarFilterOptionRow("JU", "宝石の秘境パック（JU）"));
		}
		if (visible.contains("IF")) {
			rows.add(new ToolbarFilterOptionRow("IF", "鉄面の艦隊パック（IF）"));
		}
		if (visible.contains("OT")) {
			rows.add(new ToolbarFilterOptionRow("OT", "海底の潮流パック（OT）"));
		}
		if (visible.contains("CS")) {
			rows.add(new ToolbarFilterOptionRow("CS", "創世の神域パック（CS）"));
		}
		return rows;
	}

	private static List<ToolbarFilterOptionRow> numericRangeOptions(List<CardDefinition> pool, boolean cost) {
		TreeSet<Integer> values = new TreeSet<>();
		for (CardDefinition c : pool) {
			int n = cost
					? (c.getCost() != null ? c.getCost() : 0)
					: (c.getBasePower() != null ? c.getBasePower() : 0);
			values.add(n);
		}
		List<ToolbarFilterOptionRow> rows = new ArrayList<>();
		rows.add(new ToolbarFilterOptionRow("", "すべて"));
		if (values.isEmpty()) {
			return rows;
		}
		int min = values.first();
		int max = values.last();
		for (int i = min; i <= max; i++) {
			rows.add(new ToolbarFilterOptionRow(Integer.toString(i), Integer.toString(i)));
		}
		return rows;
	}

	private static List<ToolbarFilterOptionRow> rarityOptions(List<CardDefinition> pool) {
		Set<String> seen = new LinkedHashSet<>();
		for (CardDefinition c : pool) {
			String r = c.getRarity();
			if (r != null && !r.isBlank()) {
				seen.add(r.trim());
			}
		}
		List<ToolbarFilterOptionRow> rows = new ArrayList<>();
		rows.add(new ToolbarFilterOptionRow("", "すべて"));
		addRarityIfPresent(rows, seen, "Reg", "レジェンダリー");
		addRarityIfPresent(rows, seen, "Ep", "エピック");
		addRarityIfPresent(rows, seen, "R", "レア");
		addRarityIfPresent(rows, seen, "C", "コモン");
		return rows;
	}

	private static void addRarityIfPresent(
			List<ToolbarFilterOptionRow> rows, Set<String> seen, String code, String label) {
		if (seen.contains(code)) {
			rows.add(new ToolbarFilterOptionRow(code, label));
		}
	}

	private static List<ToolbarFilterOptionRow> libSortOptions() {
		return List.of(
				new ToolbarFilterOptionRow("cost_asc", "コスト 小→大"),
				new ToolbarFilterOptionRow("cost_desc", "コスト 大→小"));
	}
}
