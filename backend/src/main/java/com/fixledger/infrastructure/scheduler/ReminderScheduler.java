package com.fixledger.infrastructure.scheduler;

import com.fixledger.modules.family.entity.FamilySpaceEntity;
import com.fixledger.modules.family.mapper.FamilySpaceMapper;
import com.fixledger.modules.reminder.response.ReminderScanResponse;
import com.fixledger.modules.reminder.service.ReminderService;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 文件功能说明：定时任务实现，封装外部依赖和技术细节。
 * </p>
 *
 * @Author FixLedger
 */
@Slf4j
@Component
public class ReminderScheduler {

  private final FamilySpaceMapper familySpaceMapper;
  private final ReminderService reminderService;

  public ReminderScheduler(
      FamilySpaceMapper familySpaceMapper,
      ReminderService reminderService
  ) {
    this.familySpaceMapper = familySpaceMapper;
    this.reminderService = reminderService;
  }

  /**
   * 每天按配置时间扫描所有家庭提醒，单个家庭失败不会影响其他家庭。
   */
  @Scheduled(cron = "${fixledger.reminder.scan-cron:0 0 8 * * *}")
  public void scanReminders() {
    log.info("Reminder scan started");
    int familyCount = 0;
    int failedCount = 0;
    ReminderScanResponse total = new ReminderScanResponse(0, 0, 0, 0, 0);
    for (FamilySpaceEntity family : familySpaceMapper.selectList(null)) {
      try {
        // 定时任务没有当前用户上下文，直接调用系统级扫描入口。
        familyCount++;
        total = total.plus(reminderService.scanFamily(family.getId(), LocalDate.now()));
      } catch (Exception e) {
        failedCount++;
        log.error("Reminder scan failed: familyId={}", family.getId(), e);
      }
    }
    log.info(
        "Reminder scan finished: families={}, warrantyCreated={}, consumableCreated={}, "
            + "notificationCreated={}, skippedDuplicate={}, failed={}",
        familyCount,
        total.warrantyCreated(),
        total.consumableCreated(),
        total.notificationCreated(),
        total.skippedDuplicate(),
        failedCount
    );
  }
}
