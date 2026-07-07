package com.example.nineuniverse.season;

import com.example.nineuniverse.repository.SeasonMetaMapper;
import com.example.nineuniverse.repository.SeasonResetMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeasonResetService {

	private final SeasonMetaMapper seasonMetaMapper;
	private final SeasonResetMapper seasonResetMapper;

	/**
	 * 現在の日付が新しい6か月区切りに入っていれば、全ユーザーの進行データを消去する（ログイン・フレンド情報は維持）。
	 * カジュアル／リーグの作成デッキ・ミッション等は削除するが、所持カード（{@code user_collection}）は維持する。
	 */
	@Transactional
	public void ensureCurrentPeriodReset() {
		ensureCurrentPeriodResetOnDate(LocalDate.now(ZoneId.systemDefault()));
	}

	void ensureCurrentPeriodResetOnDate(LocalDate today) {
		if (!SeasonSchedule.isSeasonActive(today)) {
			return;
		}
		LocalDate currentStart = SeasonSchedule.periodStartContaining(today);
		LocalDate lastReset = seasonMetaMapper.findLastResetPeriodStart();
		if (lastReset == null) {
			// 行が無いだけのときは全ユーザー消去せず、現在区切りを記録するだけにする
			seasonMetaMapper.insertInitialIfAbsent(currentStart);
			return;
		}
		if (!currentStart.isAfter(lastReset)) {
			return;
		}
		resetIfNewPeriodLocked(currentStart);
	}

	private void resetIfNewPeriodLocked(LocalDate currentStart) {
		LocalDate lastReset = seasonMetaMapper.findLastResetPeriodStartForUpdate();
		if (lastReset == null) {
			seasonMetaMapper.insertInitialIfAbsent(currentStart);
			return;
		}
		if (!currentStart.isAfter(lastReset)) {
			return;
		}
		wipeAllUserProgress();
		seasonMetaMapper.updateLastResetPeriodStart(currentStart);
	}

	private void wipeAllUserProgress() {
		seasonResetMapper.deleteAllDeckEntries();
		seasonResetMapper.deleteAllDecks();
		seasonResetMapper.deleteAllLeagueDeckSets();
		seasonResetMapper.deleteAllDailyMissions();
		seasonResetMapper.deleteAllWeeklyMissions();
		seasonResetMapper.deleteAllUserEpithetsOwned();
		seasonResetMapper.resetAllUsersForSeason();
		seasonResetMapper.grantBeginnerEpithetsToAllUsers();
	}
}
