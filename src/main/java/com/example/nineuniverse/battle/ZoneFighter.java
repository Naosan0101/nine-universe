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
	/** ふわふわゴースト等: 次にレストへ置かれる代わりに手札へ戻る */
	private boolean returnToHandOnKnock;
	/**
	 * 〈探鉱の洞窟〉がこの配置でストーン+1した（0→1 のみのケースと、未反映の1枚を区別するため）。
	 */
	private boolean fieldNebulaStoneGrantedForThisDeploy;
}
