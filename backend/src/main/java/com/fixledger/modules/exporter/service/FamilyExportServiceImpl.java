package com.fixledger.modules.exporter.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.entity.DeviceCategoryEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.asset.mapper.DeviceCategoryMapper;
import com.fixledger.modules.exporter.config.ExportProperties;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.maintenance.entity.MaintenanceRecordEntity;
import com.fixledger.modules.maintenance.enums.MaintenanceStatus;
import com.fixledger.modules.maintenance.mapper.MaintenanceRecordMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FamilyExportServiceImpl implements FamilyExportService {

  private static final String EXPORT_REQUEST_METRIC = "fixledger.export.requests";
  private static final String EXPORT_DURATION_METRIC = "fixledger.export.duration";

  private final FamilyService familyService;
  private final DeviceAssetMapper deviceAssetMapper;
  private final DeviceCategoryMapper deviceCategoryMapper;
  private final MaintenanceRecordMapper maintenanceRecordMapper;
  private final ExportProperties properties;
  private final MeterRegistry meterRegistry;

  public FamilyExportServiceImpl(
      FamilyService familyService,
      DeviceAssetMapper deviceAssetMapper,
      DeviceCategoryMapper deviceCategoryMapper,
      MaintenanceRecordMapper maintenanceRecordMapper,
      ExportProperties properties,
      MeterRegistry meterRegistry
  ) {
    this.familyService = familyService;
    this.deviceAssetMapper = deviceAssetMapper;
    this.deviceCategoryMapper = deviceCategoryMapper;
    this.maintenanceRecordMapper = maintenanceRecordMapper;
    this.properties = properties;
    this.meterRegistry = meterRegistry;
  }

  @Override
  public CsvExportFile exportDevices(Long userId, Long familyId) {
    return measuredExport("devices", () -> buildDeviceExport(userId, familyId));
  }

  private CsvExportFile buildDeviceExport(Long userId, Long familyId) {
    familyService.checkFamilyMember(userId, familyId);
    List<DeviceAssetEntity> devices = deviceAssetMapper.selectPage(
        Page.of(1, properties.getMaxSyncRows() + 1L, false),
        new LambdaQueryWrapper<DeviceAssetEntity>()
            .eq(DeviceAssetEntity::getFamilyId, familyId)
            .orderByAsc(DeviceAssetEntity::getLocation)
            .orderByAsc(DeviceAssetEntity::getId)
    ).getRecords();
    ensureWithinSyncLimit(devices.size());
    Map<Long, String> categoryNames = listCategoryNames(familyId, devices);
    CsvBuilder csv = new CsvBuilder()
        .addRow(
            "设备ID",
            "设备名称",
            "品牌",
            "型号",
            "序列号",
            "分类",
            "状态",
            "购买日期",
            "购买渠道",
            "购买价格",
            "位置",
            "备注"
        );
    for (DeviceAssetEntity device : devices) {
      csv.addRow(
          text(device.getId()),
          device.getName(),
          device.getBrand(),
          device.getModel(),
          device.getSerialNumber(),
          device.getCategoryId() == null ? null : categoryNames.get(device.getCategoryId()),
          device.getStatus(),
          text(device.getPurchaseDate()),
          device.getPurchaseChannel(),
          money(device.getPurchasePrice()),
          device.getLocation(),
          device.getRemark()
      );
    }
    return new CsvExportFile("fixledger-devices-" + familyId + ".csv", csv.toBytes());
  }

  @Override
  public CsvExportFile exportMaintenanceCosts(
      Long userId,
      Long familyId,
      LocalDate startDate,
      LocalDate endDate
  ) {
    return measuredExport(
        "maintenance-costs",
        () -> buildMaintenanceCostExport(userId, familyId, startDate, endDate)
    );
  }

  private CsvExportFile buildMaintenanceCostExport(
      Long userId,
      Long familyId,
      LocalDate startDate,
      LocalDate endDate
  ) {
    familyService.checkFamilyMember(userId, familyId);
    validateDateRange(startDate, endDate);
    LocalDateTime start = startDate == null ? null : startDate.atStartOfDay();
    LocalDateTime end = endDate == null ? null : endDate.plusDays(1).atStartOfDay();
    LambdaQueryWrapper<MaintenanceRecordEntity> wrapper =
        new LambdaQueryWrapper<MaintenanceRecordEntity>()
            .eq(MaintenanceRecordEntity::getFamilyId, familyId)
            .ne(MaintenanceRecordEntity::getStatus, MaintenanceStatus.CANCELED.getCode())
            .isNotNull(MaintenanceRecordEntity::getRepairCost)
            .ge(start != null, MaintenanceRecordEntity::getCompletedAt, start)
            .lt(end != null, MaintenanceRecordEntity::getCompletedAt, end)
            .orderByDesc(MaintenanceRecordEntity::getCompletedAt)
            .orderByDesc(MaintenanceRecordEntity::getId);
    List<MaintenanceRecordEntity> records = maintenanceRecordMapper.selectPage(
        Page.of(1, properties.getMaxSyncRows() + 1L, false),
        wrapper
    ).getRecords();
    ensureWithinSyncLimit(records.size());
    Map<Long, String> deviceNames = listDeviceNames(familyId, records);
    CsvBuilder csv = new CsvBuilder()
        .addRow(
            "维修ID",
            "设备名称",
            "维修标题",
            "状态",
            "发生时间",
            "完成时间",
            "维修渠道",
            "维修费用",
            "处理结果"
        );
    for (MaintenanceRecordEntity record : records) {
      csv.addRow(
          text(record.getId()),
          deviceNames.get(record.getDeviceId()),
          record.getTitle(),
          record.getStatus(),
          text(record.getOccurredAt()),
          text(record.getCompletedAt()),
          record.getRepairChannel(),
          money(record.getRepairCost()),
          record.getResultDescription()
      );
    }
    return new CsvExportFile(
        "fixledger-maintenance-costs-" + familyId + ".csv",
        csv.toBytes()
    );
  }

  private void ensureWithinSyncLimit(int rowCount) {
    if (rowCount > properties.getMaxSyncRows()) {
      throw new BusinessException(
          ErrorCode.BAD_REQUEST,
          "同步导出最多支持 " + properties.getMaxSyncRows() + " 行，请缩小导出范围"
      );
    }
  }

  private CsvExportFile measuredExport(
      String type,
      Supplier<CsvExportFile> exporter
  ) {
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
      CsvExportFile file = exporter.get();
      meterRegistry.counter(EXPORT_REQUEST_METRIC, "type", type, "result", "success")
          .increment();
      return file;
    } catch (RuntimeException e) {
      meterRegistry.counter(EXPORT_REQUEST_METRIC, "type", type, "result", "failed")
          .increment();
      throw e;
    } finally {
      sample.stop(Timer.builder(EXPORT_DURATION_METRIC)
          .tag("type", type)
          .publishPercentileHistogram()
          .register(meterRegistry));
    }
  }

  private void validateDateRange(LocalDate startDate, LocalDate endDate) {
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      throw new BusinessException(
          ErrorCode.BAD_REQUEST,
          "导出开始日期不能晚于结束日期"
      );
    }
  }

  private Map<Long, String> listCategoryNames(Long familyId, List<DeviceAssetEntity> devices) {
    Set<Long> categoryIds = devices.stream()
        .map(DeviceAssetEntity::getCategoryId)
        .filter(id -> id != null)
        .collect(Collectors.toSet());
    if (categoryIds.isEmpty()) {
      return Map.of();
    }
    return deviceCategoryMapper.selectList(new LambdaQueryWrapper<DeviceCategoryEntity>()
            .eq(DeviceCategoryEntity::getFamilyId, familyId)
            .in(DeviceCategoryEntity::getId, categoryIds))
        .stream()
        .collect(Collectors.toMap(
            DeviceCategoryEntity::getId,
            DeviceCategoryEntity::getName,
            (left, right) -> left
        ));
  }

  private Map<Long, String> listDeviceNames(
      Long familyId,
      List<MaintenanceRecordEntity> records
  ) {
    Set<Long> deviceIds = records.stream()
        .map(MaintenanceRecordEntity::getDeviceId)
        .filter(id -> id != null)
        .collect(Collectors.toSet());
    if (deviceIds.isEmpty()) {
      return Map.of();
    }
    return deviceAssetMapper.selectList(new LambdaQueryWrapper<DeviceAssetEntity>()
            .eq(DeviceAssetEntity::getFamilyId, familyId)
            .in(DeviceAssetEntity::getId, deviceIds))
        .stream()
        .collect(Collectors.toMap(
            DeviceAssetEntity::getId,
            DeviceAssetEntity::getName,
            (left, right) -> left
        ));
  }

  private String money(BigDecimal value) {
    return value == null ? "" : value.stripTrailingZeros().toPlainString();
  }

  private String text(Object value) {
    return value == null ? "" : value.toString();
  }

  private static final class CsvBuilder {

    private final StringBuilder content = new StringBuilder("\uFEFF");

    CsvBuilder addRow(String... cells) {
      for (int i = 0; i < cells.length; i++) {
        if (i > 0) {
          content.append(',');
        }
        appendCell(cells[i]);
      }
      content.append("\r\n");
      return this;
    }

    byte[] toBytes() {
      return content.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendCell(String raw) {
      String value = raw == null ? "" : raw.replace('\r', ' ').replace('\n', ' ');
      boolean dangerous = isFormulaLike(value);
      if (dangerous) {
        value = "'" + value;
      }
      boolean quote = dangerous
          || value.contains(",")
          || value.contains("\"")
          || value.contains("\r")
          || value.contains("\n");
      if (!quote) {
        content.append(value);
        return;
      }
      content.append('"').append(value.replace("\"", "\"\"")).append('"');
    }

    private boolean isFormulaLike(String value) {
      String stripped = value.stripLeading();
      if (stripped.isEmpty()) {
        return false;
      }
      return switch (stripped.charAt(0)) {
        case '=', '+', '-', '@' -> true;
        default -> false;
      };
    }
  }
}
