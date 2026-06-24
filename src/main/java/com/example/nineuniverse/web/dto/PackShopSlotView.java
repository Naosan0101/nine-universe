package com.example.nineuniverse.web.dto;

import com.example.nineuniverse.service.PackService.PackType;

/** パック購入画面の1枠分。 */
public record PackShopSlotView(
		PackType packType,
		String packTypeParam,
		boolean unlocked,
		String displayName,
		String unlockHint,
		int cost,
		String artWebPath,
		String detailModalId,
		String detailHeading,
		java.util.List<PackPreviewLine> previewLines) {
}
