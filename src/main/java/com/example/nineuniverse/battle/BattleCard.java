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
	/** 研究者アストリア・墓守神父等: 手札にある間の配置コスト補正（例: -1、-2） */
	private int handDeployCostModifier;
	/** SPEC-666 等: バトル中のみ種族として扱う上書き（例: UNDEAD）。null でカード定義どおり */
	private String battleTribeOverride;

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
