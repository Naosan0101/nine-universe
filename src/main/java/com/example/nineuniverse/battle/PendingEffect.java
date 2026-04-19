package com.example.nineuniverse.battle;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingEffect implements Serializable {
	/** true: 人間が配置したファイターの配置効果 / false: CPU */
	private boolean ownerHuman;
	private String mainInstanceId;
	private short cardId;
	/** CardDefinition.abilityDeployCode（配置能力の識別） */
	private String abilityDeployCode;
	/** 配置効果の本体処理が既に適用済みか（選択待ちで二重適用しないため） */
	private boolean applied;
	/**
	 * クリスタクル〈配置〉の任意ストーン確認を、効果表示の前に済ませたか（resolve 内の二重モーダル防止）
	 */
	private boolean crystakulOptionalResolved;
	/**
	 * 忍者: 配置直後の物理入れ替え（メイン更新・{@link ZoneFighter#getNinjaSwapPowerPenalty()}）を resolve で済ませたか。
	 * 未処理の間だけ abilityDeployCode が NINJA のまま。
	 */
	private boolean ninjaSwapPhaseDone;
}

