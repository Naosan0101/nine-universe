package com.example.nineuniverse.repository;

import java.time.LocalDate;
import org.apache.ibatis.annotations.Param;

public interface SeasonMetaMapper {

	LocalDate findLastResetPeriodStart();

	LocalDate findLastResetPeriodStartForUpdate();

	int updateLastResetPeriodStart(@Param("start") LocalDate start);
}
