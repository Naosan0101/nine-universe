package com.example.nineuniverse.battle;

public enum ChoiceKind {
	CONFIRM_OPTIONAL_STONE, // confirm=true to pay cost, false to skip
	CONFIRM_ACCEPT_LOSS, // confirm=true to accept loss, false to cancel and rollback
	SELECT_ONE_FROM_HAND_TO_REST,
	SELECT_TWO_FROM_HAND_TO_REST,
	SELECT_SWAP_REST_AND_HAND, // requires 2 ids: restId, handId
	SELECT_ONE_FROM_REST_TO_HAND,
	/** フェザリア: 0〜2枚（重複不可・フェザリア自身は対象外） */
	SELECT_UP_TO_TWO_FROM_REST_TO_HAND
}

