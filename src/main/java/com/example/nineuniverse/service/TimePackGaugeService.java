package com.example.nineuniverse.service;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.domain.AppUser;
import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.service.NicknameEpithetService.EpithetGachaResult;
import com.example.nineuniverse.service.PackService.PackType;
import com.example.nineuniverse.web.dto.PackOpeningSessionSlot;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimePackGaugeService {

	private final AppUserMapper appUserMapper;
	private final PackService packService;
	private final NicknameEpithetService nicknameEpithetService;

	public record TimePackClaimResult(
			List<Short> flatCardIds,
			List<PackOpeningSessionSlot> openingSlots,
			List<EpithetGachaResult> epithetResults) {
	}

	public TimePackGaugeSnapshot snapshotForUser(long userId) {
		AppUser u = appUserMapper.findById(userId);
		Instant start = u != null && u.getTimePackCycleStart() != null
				? u.getTimePackCycleStart()
				: Instant.now();
		return computeSnapshot(start, Instant.now());
	}

	public static TimePackGaugeSnapshot computeSnapshot(Instant cycleStart, Instant now) {
		long elapsed = Duration.between(cycleStart, now).toMillis();
		if (elapsed < 0) {
			elapsed = 0;
		}
		long dur = GameConstants.TIME_PACK_CYCLE_DURATION_MS;
		double ratio = Math.min(1.0, (double) elapsed / dur);
		int packs = 0;
		if (ratio >= 1.0) {
			packs = 2;
		} else if (ratio >= 0.5) {
			packs = 1;
		}
		return new TimePackGaugeSnapshot(ratio, packs, cycleStart.toEpochMilli(), dur);
	}

	/**
	 * ボーナスパックを1パック分だけ開封する（カードは {@link GameConstants#PACK_CARD_COUNT} 枚＝1パック）。
	 * ゲージが MAX で2パック分あるときは、1回目の開封でサイクルをリセットし残り1パックを {@link AppUser#getTimePackBonusBank()} に預ける。
	 */
	@Transactional
	public TimePackClaimResult claimOneBonusPackFromGauge(long userId, PackType choice) {
		if (choice != PackType.STANDARD && choice != PackType.STANDARD_2 && choice != PackType.BONUS_EPITHET_GACHA) {
			throw new IllegalArgumentException("開封できるのはスタンダードパック1・2、または二つ名ガチャです。");
		}
		AppUser u = appUserMapper.findById(userId);
		if (u == null) {
			throw new IllegalStateException("ユーザーが見つかりません");
		}
		int bank = u.getTimePackBonusBank() != null ? Math.max(0, u.getTimePackBonusBank()) : 0;
		Instant start = u.getTimePackCycleStart() != null ? u.getTimePackCycleStart() : Instant.now();
		int timerPacks = computeSnapshot(start, Instant.now()).availablePacks();
		if (bank == 0 && timerPacks == 0) {
			throw new IllegalStateException("ゲージが半分に達していません。しばらく待ってから再度お試しください。");
		}

		if (bank > 0) {
			int rows = appUserMapper.subtractTimePackBonusBankIfPositive(userId);
			if (rows != 1) {
				throw new IllegalStateException("開封できるボーナスパックがありません。");
			}
		} else if (timerPacks == 2) {
			appUserMapper.updateTimePackCycleStart(userId, Instant.now());
			appUserMapper.addTimePackBonusBankDelta(userId, 1);
		} else if (timerPacks == 1) {
			appUserMapper.updateTimePackCycleStart(userId, Instant.now());
		} else {
			throw new IllegalStateException("開封できるボーナスパックがありません。");
		}

		List<Short> flatCardIds = new ArrayList<>();
		List<PackOpeningSessionSlot> openingSlots = new ArrayList<>();
		List<EpithetGachaResult> epithetResults = new ArrayList<>();
		if (choice == PackType.BONUS_EPITHET_GACHA) {
			EpithetGachaResult r = nicknameEpithetService.rollBonusEpithetGacha(userId);
			epithetResults.add(r);
			openingSlots.add(PackOpeningSessionSlot.epithet(r.upperGained(), r.lowerGained()));
		} else {
			List<CardDefinition> pulled = packService.openBonusPackWithoutGemCost(userId, choice);
			for (CardDefinition c : pulled) {
				if (c.getId() != null) {
					short cid = c.getId();
					flatCardIds.add(cid);
					openingSlots.add(PackOpeningSessionSlot.card(cid));
				}
			}
		}
		return new TimePackClaimResult(flatCardIds, openingSlots, epithetResults);
	}

	public record TimePackGaugeSnapshot(
			double fillRatio,
			int availablePacks,
			long cycleStartEpochMilli,
			long durationMs
	) {
		public int fillPercent() {
			return (int) Math.round(Math.min(1.0, Math.max(0.0, fillRatio)) * 100.0);
		}
	}
}
