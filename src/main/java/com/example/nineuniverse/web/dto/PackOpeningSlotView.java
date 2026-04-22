package com.example.nineuniverse.web.dto;

import com.example.nineuniverse.domain.LibraryCardView;

/**
 * 開封演出テンプレート用：カード1枚分または二つ名1回分。
 */
public record PackOpeningSlotView(
		boolean epithet,
		LibraryCardView card,
		String epithetUpper,
		String epithetLower,
		int index) {
}
