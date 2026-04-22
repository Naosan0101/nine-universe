package com.example.nineuniverse.service;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.domain.AppUser;
import com.example.nineuniverse.domain.NicknameEpithet;
import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.repository.NicknameEpithetMapper;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NicknameEpithetService {

	private final NicknameEpithetMapper nicknameEpithetMapper;
	private final AppUserMapper appUserMapper;
	private final MissionService missionService;

	public record EpithetDisplay(String upperText, String lowerText) {
		public static EpithetDisplay empty() {
			return new EpithetDisplay("", "");
		}
	}

	public record EpithetGachaResult(String upperGained, String lowerGained) {
	}

	/** バトル開始イントロなど：選択中の二つ名テキスト */
	public EpithetDisplay resolveDisplay(long userId) {
		AppUser u = appUserMapper.findById(userId);
		if (u == null || u.getSelectedEpithetUpperId() == null || u.getSelectedEpithetLowerId() == null) {
			return EpithetDisplay.empty();
		}
		String up = nicknameEpithetMapper.findPhraseById(u.getSelectedEpithetUpperId());
		String lo = nicknameEpithetMapper.findPhraseById(u.getSelectedEpithetLowerId());
		if (up == null) {
			up = "";
		}
		if (lo == null) {
			lo = "";
		}
		return new EpithetDisplay(up, lo);
	}

	@Transactional
	public void grantBeginnerOwnedForNewUser(long userId) {
		nicknameEpithetMapper.grantBeginnerOwned(userId);
	}

	@Transactional
	public EpithetGachaResult rollGacha(long userId) {
		return executeEpithetGachaDraw(userId, true);
	}

	/**
	 * 時間ゲージのボーナスパック：クリスタル消費なし。ミッションは {@link MissionService#onBonusPackOpened(long)} を1回。
	 */
	@Transactional
	public EpithetGachaResult rollBonusEpithetGacha(long userId) {
		EpithetGachaResult r = executeEpithetGachaDraw(userId, false);
		missionService.onBonusPackOpened(userId);
		return r;
	}

	private EpithetGachaResult executeEpithetGachaDraw(long userId, boolean payWithCrystal) {
		List<Long> unUp = nicknameEpithetMapper.listUnownedIdsByKind(userId, "UPPER");
		List<Long> unLo = nicknameEpithetMapper.listUnownedIdsByKind(userId, "LOWER");
		if (unUp.isEmpty() || unLo.isEmpty()) {
			throw new IllegalArgumentException("未入手の〈上の句〉と〈下の句〉の両方が残っていないため、二つ名ガチャを引けません。");
		}
		if (payWithCrystal) {
			int cost = GameConstants.RECYCLE_SHOP_EPITHET_GACHA_CRYSTAL;
			if (appUserMapper.subtractRecycleCrystalIfEnough(userId, cost) == 0) {
				throw new IllegalArgumentException("クリスタルが足りません（" + cost + "必要）。");
			}
		}
		Random rnd = new Random();
		long pickU = unUp.get(rnd.nextInt(unUp.size()));
		long pickL = unLo.get(rnd.nextInt(unLo.size()));
		nicknameEpithetMapper.insertOwned(userId, pickU);
		nicknameEpithetMapper.insertOwned(userId, pickL);
		String up = nicknameEpithetMapper.findPhraseById(pickU);
		String lo = nicknameEpithetMapper.findPhraseById(pickL);
		return new EpithetGachaResult(up != null ? up : "", lo != null ? lo : "");
	}

	/** 設定保存・DB更新の前に、入手済みかつマスタに存在するか検証する。 */
	public void validateEpithetSelection(long userId, long upperId, long lowerId) {
		if (!nicknameEpithetMapper.userOwns(userId, upperId)) {
			throw new IllegalArgumentException("選んだ〈上の句〉を入手していません。");
		}
		if (!nicknameEpithetMapper.userOwns(userId, lowerId)) {
			throw new IllegalArgumentException("選んだ〈下の句〉を入手していません。");
		}
		String ku = nicknameEpithetMapper.findPhraseById(upperId);
		String kl = nicknameEpithetMapper.findPhraseById(lowerId);
		if (ku == null || kl == null) {
			throw new IllegalArgumentException("二つ名の指定が不正です。");
		}
	}

	@Transactional
	public void updateSelection(long userId, long upperId, long lowerId) {
		validateEpithetSelection(userId, upperId, lowerId);
		int n = appUserMapper.updateSelectedEpithets(userId, upperId, lowerId);
		if (n == 0) {
			throw new IllegalStateException("ユーザーが見つかりません");
		}
	}

	public List<NicknameEpithet> listOwnedUpper(long userId) {
		return nicknameEpithetMapper.listOwnedByKind(userId, "UPPER");
	}

	public List<NicknameEpithet> listOwnedLower(long userId) {
		return nicknameEpithetMapper.listOwnedByKind(userId, "LOWER");
	}

	public boolean canRollGacha(long userId) {
		return !nicknameEpithetMapper.listUnownedIdsByKind(userId, "UPPER").isEmpty()
				&& !nicknameEpithetMapper.listUnownedIdsByKind(userId, "LOWER").isEmpty();
	}
}
