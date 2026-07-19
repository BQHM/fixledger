package com.fixledger.modules.exporter.service;

import java.time.LocalDate;

/**
 * 家庭数据导出服务。
 */
public interface FamilyExportService {

  CsvExportFile exportDevices(Long userId, Long familyId);

  CsvExportFile exportMaintenanceCosts(
      Long userId,
      Long familyId,
      LocalDate startDate,
      LocalDate endDate
  );
}
