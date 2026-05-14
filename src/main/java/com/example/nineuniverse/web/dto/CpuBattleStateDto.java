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
		/** 霊園教会 デスバウンス の残りターン（6…1）。該当フィールドでないときは 0 */
		int deathbounceFieldTurnsRemaining,
		/** 深海神殿 アトランティスのカウント表示（2→0）。該当フィールドでないときは 0 */
		int atlantisFieldCounterDisplay,
		/** 週刊少年 CAMP のカウント表示（6…1）。該当フィールドでないときは 0 */
		int weeklyShonenCampFieldCounterDisplay,
		/** 週刊少年 CAMP のカウント2到達後（コミック+4 分） */
		boolean weeklyShonenCampCount2ComicBonus,
		/** 週刊少年 CAMP のカウント3到達ターン中、全カード配置コスト+1 */
		boolean weeklyShonenCampGlobalDeployCostPlusOneThisTurn,
		/** 世界の再構築〈フィールド〉のカウント（4→0）。該当でないときは 0 */
		int worldRebuildFieldCounterDisplay,
		/** ペーパーシティ〈フィールド〉のカウント（6→0）。該当でないときは 0 */
		int paperCityFieldCounterDisplay,
		int humanBattlePower,
		int cpuBattlePower,
		int humanNextDeployBonus,
		int humanNextElfOnlyBonus,
		int humanNextDeployCostBonusTimes,
		int humanNextMechanicStacks,
		/** 相手スロット（画面下段の相手側）の次の配置ボーナス系。クライアントは相手手札のコスト・強さプレビューに使う */
		int cpuNextDeployBonus,
		int cpuNextElfOnlyBonus,
		int cpuNextDeployCostBonusTimes,
		int cpuNextMechanicStacks,
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

