package com.example.nineuniverse.web;

import com.example.nineuniverse.season.SeasonSchedule;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.ui.Model;

/** シーズン段階ロックの画面用フラグを {@link Model} に載せる。 */
public final class SeasonLockViewHelper {

	private SeasonLockViewHelper() {
	}

	public static LocalDate today() {
		return LocalDate.now(ZoneId.systemDefault());
	}

	public static void addBattleModeLocks(Model model) {
		addBattleModeLocks(model, today());
	}

	public static void addBattleModeLocks(Model model, LocalDate today) {
		boolean league = SeasonSchedule.isLeagueBattleUnlocked(today);
		boolean advanced = SeasonSchedule.isCpuAdvancedUnlocked(today);
		model.addAttribute("seasonLeagueUnlocked", league);
		model.addAttribute("seasonCpuAdvancedUnlocked", advanced);
		model.addAttribute("seasonLeagueUnlockHint", league ? "" : SeasonSchedule.leagueBattleUnlockHint(today));
		model.addAttribute("seasonCpuAdvancedUnlockHint", advanced ? "" : SeasonSchedule.cpuAdvancedUnlockHint(today));
	}
}
