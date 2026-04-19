package com.example.nineuniverse.web.dto;

public record BattleCardDto(
		String instanceId,
		short cardId,
		/** 炭鉱夫効果などで能力テキストが「効果なし。」のインスタンス */
		boolean blankEffects,
		/** 研究者アストリア等による手札中の配置コスト補正 */
		int handDeployCostModifier
) {
	public BattleCardDto(String instanceId, short cardId) {
		this(instanceId, cardId, false, 0);
	}

	public BattleCardDto(String instanceId, short cardId, boolean blankEffects) {
		this(instanceId, cardId, blankEffects, 0);
	}
}

