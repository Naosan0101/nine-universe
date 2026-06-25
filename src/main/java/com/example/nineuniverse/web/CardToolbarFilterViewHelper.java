package com.example.nineuniverse.web;

import com.example.nineuniverse.service.CardToolbarFilterService;
import org.springframework.ui.Model;

/** カード一覧ツールバーの絞り込み候補を {@link Model} へ載せる。 */
public final class CardToolbarFilterViewHelper {

	private CardToolbarFilterViewHelper() {
	}

	public static void addToolbarFilterOptions(Model model, CardToolbarFilterService service) {
		model.addAttribute("toolbarFilterOptionsJson", service.optionsJsonForToday());
	}
}
