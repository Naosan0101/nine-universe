package com.example.nineuniverse.battle;

/**
 * CPU戦の相手タイプ。{@link #ORIGIN} は従来のカードプール（id 1〜30）と挙動。
 */
public enum CpuBattleMode {
	ORIGIN,
	/** 全カード＋〈フィールド〉をデッキに含め、フィールドを考慮した AI */
	ADVANCED
}
