package com.example.nineuniverse.repository;

import com.example.nineuniverse.domain.AppUser;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

public interface AppUserMapper {
	AppUser findByUsername(@Param("username") String username);

	AppUser findById(@Param("id") long id);

	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(AppUser user);

	int updateCoins(@Param("id") long id, @Param("coins") int coins);

	/** コインを上書きし、ウェルカムジェム付与済みにする（開発用 testuser のログインリセットなど）。 */
	int updateCoinsAndMarkWelcomeHomeBonusGranted(@Param("id") long id, @Param("coins") int coins);

	/** {@code coins} に {@code delta} を加算する（報酬付与など）。 */
	int addCoinsDelta(@Param("id") long id, @Param("delta") int delta);

	/**
	 * {@code welcome_home_bonus_granted = false} のときだけ {@code amount} を加算し true にする。競合時は 0 行。
	 */
	int grantWelcomeHomeBonusIfPending(@Param("id") long id, @Param("amount") int amount);

	int updateLastMissionDate(@Param("id") long id, @Param("lastMissionDate") LocalDate lastMissionDate);

	int updateLastAccessAt(@Param("id") long id, @Param("at") LocalDateTime at);

	int updateTimePackCycleStart(@Param("id") long id, @Param("at") Instant at);

	int addTimePackBonusBankDelta(@Param("id") long id, @Param("delta") int delta);

	/** ボーナスパック預りを1減らす（正のときのみ 1 行更新）。 */
	int subtractTimePackBonusBankIfPositive(@Param("id") long id);

	/** プレゼントのスタンダードパック1を1つ消費（残数が正のときのみ 1 行更新）。 */
	int decrementStarterGiftStandard1IfPositive(@Param("id") long id);

	int setStarterGiftStandard1Remaining(@Param("id") long id, @Param("remaining") int remaining);

	int updateProfileSettings(@Param("id") long id, @Param("displayName") String displayName,
			@Param("cpuThinkSpeed") String cpuThinkSpeed);

	/**
	 * 設定画面：表示名・CPU速度・選択中二つ名を1回で更新（バトル表示は {@code selected_epithet_*} を参照）。
	 */
	int updateProfileCpuAndEpithets(
			@Param("id") long id,
			@Param("displayName") String displayName,
			@Param("cpuThinkSpeed") String cpuThinkSpeed,
			@Param("upperId") long upperId,
			@Param("lowerId") long lowerId);

	int addRecycleCrystalDelta(@Param("id") long id, @Param("delta") int delta);

	/** クリスタルが足りるときだけ減算。更新行数 1 で成功。 */
	int subtractRecycleCrystalIfEnough(@Param("id") long id, @Param("amount") int amount);

	int setRecycleCrystal(@Param("id") long id, @Param("crystal") int crystal);

	int updateSelectedEpithets(@Param("id") long id, @Param("upperId") long upperId, @Param("lowerId") long lowerId);
}
