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
	/**
	 * レベルアップ（レスト捨て・ストーン消費）のみによる配置時の強さ加算。
	 * 〈神秘の大樹 スカイア〉の「カード効果由来の相手ターン持続」には含めず、ターン境界で必ずリセットする。
	 */
	private int levelUpDeployPowerBonus;
	/** カード効果・特性コスト分など、レベルアップ分を除いた配置時の一時加算（ターン終了でリセット。スカイア下のエルフは相手ターンまで持続し得る） */
	private int temporaryPowerBonus;
	/**
	 * 忍者: コストと入れ替えたあとメインになったファイターに適用する強さ減少（通常 0 または 2）。
	 * 前列にいる間は相手ターンも含め持続し、前列が入れ替わるまでターン終了では消えない。
	 */
	private int ninjaSwapPowerPenalty;
	/**
	 * ボットバイク: 配置コストに「メカニック」が含まれた場合の強さ+3（次の相手ターン終了まで）。
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
	/**
	 * 薬売り〈配置〉: 配置した時点の配置側の所持ストーン数。相手ファイターの強さからこの値を減らす（メインが薬売りでないときは 0）。
	 */
	private int kusuriOpponentDebuffFromDeployStones;
}
