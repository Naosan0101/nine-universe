package com.example.nineuniverse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.web.dto.ToolbarFilterOptionRow;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardToolbarFilterServiceTest {

	@Mock
	private CardCatalogService cardCatalogService;

	@InjectMocks
	private CardToolbarFilterService service;

	@Test
	void tier1Options_matchEarlySeasonExample() {
		LocalDate tier1 = LocalDate.of(2026, 6, 15);
		when(cardCatalogService.all()).thenReturn(List.of(
				card((short) 1, "HUMAN", "WH", 1, 3, "C", "FIGHTER"),
				card((short) 2, "ELF", "WH", 0, 2, "R", "FIGHTER"),
				card((short) 3, "UNDEAD", "ET", 2, 4, "C", "FIGHTER"),
				card((short) 4, "DRAGON", "ET", 3, 7, "Ep", "FIGHTER"),
				card((short) 5, "HUMAN", "JU", 4, 8, "C", "FIGHTER"),
				card((short) 6, "HUMAN", "WH", 1, 1, "C", "FIELD")));

		Map<String, List<ToolbarFilterOptionRow>> opts = service.optionsForDate(tier1);

		assertTribeCodes(opts.get("tribe"), "", "HUMAN", "ELF", "UNDEAD", "DRAGON");
		assertPackValues(opts.get("pack"), "", "STANDARD_1", "WH", "ET");
		assertCardKindValues(opts.get("cardKind"), "", "fighter");
		assertNumericValues(opts.get("cost"), "", "0", "1", "2", "3");
		assertNumericValues(opts.get("power"), "", "2", "3", "4", "5", "6", "7");
	}

	@Test
	void tier2AddsFieldKindAndMorePacks() {
		LocalDate tier2 = LocalDate.of(2026, 8, 15);
		when(cardCatalogService.all()).thenReturn(List.of(
				card((short) 1, "HUMAN", "WH", 1, 3, "C", "FIGHTER"),
				card((short) 2, "MACHINE", "JU", 4, 8, "R", "FIGHTER"),
				card((short) 3, "HUMAN", "IF", 2, 5, "C", "FIELD")));

		Map<String, List<ToolbarFilterOptionRow>> opts = service.optionsForDate(tier2);

		assertTrue(opts.get("cardKind").stream().anyMatch(r -> "field".equals(r.getV())));
		assertTrue(opts.get("pack").stream().anyMatch(r -> "STANDARD_2".equals(r.getV())));
		assertTrue(opts.get("pack").stream().anyMatch(r -> "JU".equals(r.getV())));
		assertTrue(opts.get("tribe").stream().anyMatch(r -> "MACHINE".equals(r.getV())));
		assertNumericValues(opts.get("cost"), "", "1", "2", "3", "4");
	}

	private static CardDefinition card(
			short id, String attr, String pack, int cost, int power, String rarity, String kind) {
		CardDefinition c = new CardDefinition();
		c.setId(id);
		c.setAttribute(attr);
		c.setPackInitial(pack);
		c.setCost((short) cost);
		c.setBasePower((short) power);
		c.setRarity(rarity);
		c.setCardKind(kind);
		c.setName("card-" + id);
		return c;
	}

	private static void assertTribeCodes(List<ToolbarFilterOptionRow> rows, String... codes) {
		assertValues(rows, codes);
	}

	private static void assertPackValues(List<ToolbarFilterOptionRow> rows, String... values) {
		assertValues(rows, values);
	}

	private static void assertCardKindValues(List<ToolbarFilterOptionRow> rows, String... values) {
		assertValues(rows, values);
	}

	private static void assertNumericValues(List<ToolbarFilterOptionRow> rows, String... values) {
		assertValues(rows, values);
	}

	private static void assertValues(List<ToolbarFilterOptionRow> rows, String... values) {
		assertEquals(values.length, rows.size());
		for (int i = 0; i < values.length; i++) {
			assertEquals(values[i], rows.get(i).getV(), "index " + i);
		}
	}
}
