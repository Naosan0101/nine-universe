package com.example.nineuniverse.battle;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BattleCard implements Serializable {
	private String instanceId;
	private short cardId;
	/** 炭鉱夫等で手札に戻ったあと、〈配置〉〈常時〉が無効で表示は「効果なし。」 */
	private boolean blankEffects;
	/** 研究者アストリア等: 手札にある間の配置コスト補正（通常 0、-1 でコスト-1） */
	private int handDeployCostModifier;

	public BattleCard(String instanceId, short cardId) {
		this.instanceId = instanceId;
		this.cardId = cardId;
	}

	public BattleCard(String instanceId, short cardId, boolean blankEffects) {
		this.instanceId = instanceId;
		this.cardId = cardId;
		this.blankEffects = blankEffects;
	}
}
