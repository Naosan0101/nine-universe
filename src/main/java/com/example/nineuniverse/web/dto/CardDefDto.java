package com.example.nineuniverse.web.dto;

import java.util.List;

public record CardDefDto(
		short id,
		String name,
		short cost,
		short basePower,
		String attribute,
		/** 表示用（カード右下など）: 所属パックのイニシャル（例: STD） */
		String packInitial,
		/** C / R / Ep / Reg（ライブラリの card-face と同じ） */
		String rarity,
		String rarityLabel,
		String imageFile,
		String abilityDeployCode,
		String attributeLabelJa,
		List<String> attributeLabelLines,
		String layerBasePath,
		String layerBarPath,
		String layerFramePath,
		String layerPortraitPath,
		/** 主 URL と異なる場合のみ（NFC ファイル名向け）。同一なら空 */
		String layerPortraitPathAlt,
		boolean fieldCard,
		/** FIGHTER / FIELD（バトルUIの〈フィールド〉コスト計算などに使用） */
		String cardKind,
		List<AbilityBlockDto> abilityBlocks
) {
}
