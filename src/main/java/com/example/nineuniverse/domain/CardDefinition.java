package com.example.nineuniverse.domain;

import lombok.Data;

@Data
public class CardDefinition {
	private Short id;
	private String name;
	private Short cost;
	private Short basePower;
	private String attribute;
	/** FIGHTER / FIELD */
	private String cardKind;
	private String rarity;
	private String imageFile;
	/** 表示用（カード右下など）: 所属パックのイニシャル（例: STD） */
	private String packInitial;
	private String abilityDeployCode;
	private String abilityPassiveCode;
	private String deployHelp;
	private String passiveHelp;
}
