package com.example.nineuniverse.season;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nineuniverse.repository.SeasonMetaMapper;
import com.example.nineuniverse.repository.SeasonResetMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeasonResetServiceTest {

	@Mock
	private SeasonMetaMapper seasonMetaMapper;

	@Mock
	private SeasonResetMapper seasonResetMapper;

	@InjectMocks
	private SeasonResetService seasonResetService;

	@Test
	void ensureCurrentPeriodReset_whenMetaRowMissing_bootstrapsWithoutWiping() {
		LocalDate currentStart = SeasonSchedule.periodStartContaining(LocalDate.of(2026, 6, 25));
		when(seasonMetaMapper.findLastResetPeriodStart()).thenReturn(null);

		seasonResetService.ensureCurrentPeriodReset();

		verify(seasonMetaMapper).insertInitialIfAbsent(currentStart);
		verify(seasonResetMapper, never()).deleteAllUserCollections();
		verify(seasonResetMapper, never()).resetAllUsersForSeason();
	}

	@Test
	void ensureCurrentPeriodReset_whenAlreadyResetForCurrentPeriod_doesNothing() {
		LocalDate currentStart = SeasonSchedule.periodStartContaining(LocalDate.of(2026, 6, 25));
		when(seasonMetaMapper.findLastResetPeriodStart()).thenReturn(currentStart);

		seasonResetService.ensureCurrentPeriodReset();

		verify(seasonMetaMapper, never()).insertInitialIfAbsent(any());
		verify(seasonResetMapper, never()).deleteAllUserCollections();
	}
}
