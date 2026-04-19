package com.example.nineuniverse.battle;

public enum ChoiceKind {
	CONFIRM_OPTIONAL_STONE, // confirm=true to pay cost, false to skip
	/** ミラージュクル: 相手の〈配置〉をコピーするか（true でコピー処理へ進む） */
	CONFIRM_MIRAJUKUL_MIRROR,
	/** 忍者: 入れ替え後のメインの〈配置〉を使うか（true で効果適用） */
	CONFIRM_NINJA_SWAPPED_DEPLOY,
	CONFIRM_ACCEPT_LOSS, // confirm=true to accept loss, false to cancel and rollback
	SELECT_ONE_FROM_HAND_TO_REST,
	/** 墓守神父: 手札のアンデッド・ファイター1枚にバトル終了までコスト-2（手札に留まる） */
	SELECT_ONE_UNDEAD_FIGHTER_FROM_HAND_FOR_COST,
	SELECT_TWO_FROM_HAND_TO_REST,
	SELECT_SWAP_REST_AND_HAND, // requires 2 ids: restId, handId
	SELECT_ONE_FROM_REST_TO_HAND,
	/** SPEC-1: レストのファイターを1枚デッキ上へ */
	SELECT_ONE_FROM_REST_TO_DECK_TOP,
	/** フェザリア: 0〜2枚（重複不可・フェザリア自身は対象外） */
	SELECT_UP_TO_TWO_FROM_REST_TO_HAND
}

