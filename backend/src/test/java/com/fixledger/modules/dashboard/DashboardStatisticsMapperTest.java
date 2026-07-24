package com.fixledger.modules.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.fixledger.infrastructure.redis.TestRedisConfig;
import com.fixledger.modules.dashboard.mapper.DashboardStatisticsMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestRedisConfig.class)
@Transactional
class DashboardStatisticsMapperTest {

  private static final Long FAMILY_ID = 29001L;
  private static final LocalDate TODAY = LocalDate.of(2026, 7, 21);
  private static final LocalDateTime MONTH_START = TODAY.withDayOfMonth(1).atStartOfDay();
  private static final LocalDateTime MONTH_END = MONTH_START.plusMonths(1);

  @Autowired
  private DashboardStatisticsMapper dashboardStatisticsMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("首页摘要通过单次聚合查询返回全部统计口径")
  void selectDashboardSummaryInOneAggregateQuery() {
    insertFixture();

    var summary = dashboardStatisticsMapper.selectSummary(
        FAMILY_ID,
        TODAY,
        TODAY.plusDays(30),
        MONTH_START,
        MONTH_END
    );

    assertThat(summary.getDeviceTotal()).isEqualTo(1);
    assertThat(summary.getWarrantyExpiringCount()).isEqualTo(1);
    assertThat(summary.getWarrantyExpiredCount()).isEqualTo(1);
    assertThat(summary.getConsumableDueSoonCount()).isEqualTo(1);
    assertThat(summary.getConsumableOverdueCount()).isEqualTo(1);
    assertThat(summary.getRepairingCount()).isEqualTo(1);
    assertThat(summary.getMonthlyMaintenanceCost()).isEqualByComparingTo(
        BigDecimal.valueOf(120)
    );
  }

  private void insertFixture() {
    LocalDateTime now = TODAY.atTime(8, 0);
    jdbcTemplate.update("""
        INSERT INTO fl_device_asset (
          family_id, name, status, created_at, updated_at, deleted
        ) VALUES (?, '测试设备', 'NORMAL', ?, ?, 0)
        """, FAMILY_ID, now, now);
    jdbcTemplate.update("""
        INSERT INTO fl_warranty_record (
          family_id, device_id, start_date, end_date, created_at, updated_at, deleted
        ) VALUES
          (?, 1, ?, ?, ?, ?, 0),
          (?, 1, ?, ?, ?, ?, 0)
        """,
        FAMILY_ID, TODAY.minusYears(1), TODAY.plusDays(10), now, now,
        FAMILY_ID, TODAY.minusYears(2), TODAY.minusDays(1), now, now);
    jdbcTemplate.update("""
        INSERT INTO fl_consumable_item (
          family_id, device_id, name, cycle_days, status, enabled,
          created_at, updated_at, deleted
        ) VALUES
          (?, 1, '滤芯', 90, 'DUE_SOON', 1, ?, ?, 0),
          (?, 1, '滤网', 60, 'OVERDUE', 1, ?, ?, 0)
        """, FAMILY_ID, now, now, FAMILY_ID, now, now);
    jdbcTemplate.update("""
        INSERT INTO fl_maintenance_record (
          family_id, device_id, title, fault_description, status,
          repair_cost, completed_at, created_at, updated_at, deleted
        ) VALUES
          (?, 1, '处理中', '测试故障', 'REPAIRING', NULL, NULL, ?, ?, 0),
          (?, 1, '已完成', '测试故障', 'COMPLETED', 120, ?, ?, ?, 0)
        """, FAMILY_ID, now, now, FAMILY_ID, now, now, now);
  }
}
