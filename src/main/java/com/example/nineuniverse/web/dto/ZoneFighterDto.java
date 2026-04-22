package com.example.nineuniverse.web.dto;

import java.util.List;

public record ZoneFighterDto(
		BattleCardDto main,
		List<BattleCardDto> costUnder,
		int temporaryPowerBonus,
		List<BattlePowerModifierDto> powerModifiers,
		/** SPEC-777 の出目（2～7）。該当でなければ 0 */
		int spec777RolledPower,
		/** 前列メインが置かれた順（1起算）。ガラクタレッグ系〈常時〉の先出し判定用 */
		int battleMainLineSeq
) {
}

