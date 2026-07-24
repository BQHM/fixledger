package com.fixledger.modules.dashboard.mapper;

import com.fixledger.modules.dashboard.dto.DashboardSummaryStatisticsDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DashboardStatisticsMapper {

  DashboardSummaryStatisticsDTO selectSummary(
      @Param("familyId") Long familyId,
      @Param("today") LocalDate today,
      @Param("warrantyEnd") LocalDate warrantyEnd,
      @Param("monthStart") LocalDateTime monthStart,
      @Param("monthEnd") LocalDateTime monthEnd
  );
}
