package com.example.nineuniverse.web.dto;

import java.util.List;
import java.util.Map;

public record CpuBattleStateDto(
		boolean pvpMatch,
		/** CPU戦のみ: ORIGIN / ADVANCED */
		String cpuBattleMode,
		int cpuLevel,
		boolean humanGoesFirst,
		boolean humansTurn,
		String phase,
		long turnStartedAtMs,
		int activeTimeLimitSec,
		int activePenaltyStage,
		int humanStones,
		int cpuStones,
		List<BattleCardDto> humanDeck,
		List<BattleCardDto> humanHand,
		List<BattleCardDto> humanRest,
		ZoneFighterDto humanBattle,
		List<BattleCardDto> cpuDeck,
		List<BattleCardDto> cpuHand,
		List<BattleCardDto> cpuRest,
		ZoneFighterDto cpuBattle,
		/** 共有の〈フィールド〉（両者視点で同一。クライアントは自分側のバトル列に表示） */
		BattleCardDto activeField,
		/** 廃棄工場 5C-R4P の残りターン（4…1）。該当フィールドでないときは 0 */
		int scrapyardFieldTurnsRemaining,
		int humanBattlePower,
		int cpuBattlePower,
		int humanNextDeployBonus,
		int humanNextElfOnlyBonus,
		int humanNextDeployCostBonusTimes,
		int humanNextMechanicStacks,
		String lastMessage,
		boolean gameOver,
		boolean humanWon,
		// 現在の視点のプレイヤーが手番で、相手以上のファイターを出せない（配置不可）
		boolean noLegalDeploy,
		PendingEffectDto pendingEffect,
		PendingChoiceDto pendingChoice,
		List<String> eventLog,
		Map<Short, CardDefDto> defs,
		/** 現在の視点プレイヤーがこのバトルで使用しているデッキID（デッキ選択の「前回使用」用） */
		Long myBattleDeckId,
		/** SPEC-666: 次にホスト側スロットへ出すファイターがアンデッド扱いになる予定 */
		boolean spec666NextHumanUndead,
		/** SPEC-666: 次にゲスト/CPU 側スロットへ出すファイターがアンデッド扱いになる予定 */
		boolean spec666NextCpuUndead
) {
}

