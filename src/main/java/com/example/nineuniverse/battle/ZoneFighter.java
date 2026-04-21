package com.example.nineuniverse.battle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ZoneFighter implements Serializable {
	private BattleCard main;
	private List<BattleCard> costUnder = new ArrayList<>();
	/** costUnder の先頭から何枚が「配置コストのカード支払い」か（レベルアップで下に重ねた枚数は含まない） */
	private int costPayCardCount;
	/** この配置でレベルアップ等により一時的に加算した強さ（ターン終了でリセット） */
	private int temporaryPowerBonus;
	/**
	 * 忍者: コストと入れ替えたあとメインになったファイターに適用する強さ減少（通常 0 または 2）。
	 * 前列にいる間は相手ターンも含め持続し、前列が入れ替わるまでターン終了では消えない。
	 */
	private int ninjaSwapPowerPenalty;
	/**
	 * ボットバイク: 配置コストに「メカニック」が含まれた場合の強さ+2（次の相手ターン終了まで）。
	 * {@link CpuBattleEngine#beginTurnGainStone} で 0 に戻す。
	 */
	private int botBikeMechanicPowerBonus;
	/** ふわふわゴースト等: 次にレストへ置かれる代わりに手札へ戻る */
	private boolean returnToHandOnKnock;
	/**
	 * 〈探鉱の洞窟〉がこの配置でストーン+1した（0→1 のみのケースと、未反映の1枚を区別するため）。
	 */
	private boolean fieldNebulaStoneGrantedForThisDeploy;
	/** SPEC-777: 〈配置〉で振った強さ（2～7）。未適用時は 0 */
	private int spec777RolledPower;
	/**
	 * この前列メインがバトルゾーンに置かれた順（1起算）。ガラクタレッグ系〈常時〉の先出し解決用。
	 * 0 は未設定（旧データ互換）。
	 */
	private int battleMainLineSeq;
}
