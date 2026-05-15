package com.fixledger.modules.reminder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fixledger.common.constant.RedisKeys;
import com.fixledger.infrastructure.redis.RedisService;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.consumable.mapper.ConsumableItemMapper;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.file.enums.FileBizType;
import com.fixledger.modules.reminder.enums.ReminderType;
import com.fixledger.modules.reminder.mapper.ReminderTaskMapper;
import com.fixledger.modules.reminder.response.ReminderScanResponse;
import com.fixledger.modules.reminder.service.ReminderCreationService;
import com.fixledger.modules.reminder.service.ReminderServiceImpl;
import com.fixledger.modules.warranty.entity.WarrantyRecordEntity;
import com.fixledger.modules.warranty.enums.WarrantyType;
import com.fixledger.modules.warranty.mapper.WarrantyRecordMapper;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.transaction.annotation.Transactional;

class ReminderServiceTransactionBoundaryTest {

  @Test
  @DisplayName("提醒扫描入口不使用大事务，创建提醒时才开启写库事务")
  void scanFamilyDoesNotUseWideTransaction() throws NoSuchMethodException {
    Method userScan = ReminderServiceImpl.class.getMethod("scanFamily", Long.class, Long.class);
    Method schedulerScan = ReminderServiceImpl.class.getMethod(
        "scanFamily",
        Long.class,
        LocalDate.class
    );
    Method createReminder = ReminderCreationService.class.getMethod(
        "createReminderIfAbsent",
        Long.class,
        ReminderType.class,
        String.class,
        Long.class,
        String.class,
        String.class,
        LocalDateTime.class
    );

    assertThat(userScan.isAnnotationPresent(Transactional.class)).isFalse();
    assertThat(schedulerScan.isAnnotationPresent(Transactional.class)).isFalse();
    assertThat(createReminder.isAnnotationPresent(Transactional.class)).isTrue();
  }

  @Test
  @DisplayName("提醒写库失败时释放 Redis 去重键，允许后续扫描重试")
  void deleteDedupeKeyWhenReminderCreationFails() {
    LocalDate today = LocalDate.of(2026, 5, 15);
    WarrantyRecordEntity warranty = warranty(today);
    DeviceAssetEntity device = device(warranty.getDeviceId());
    RedisService redisService = mock(RedisService.class);
    ReminderCreationService creationService = mock(ReminderCreationService.class);
    WarrantyRecordMapper warrantyRecordMapper = mock(WarrantyRecordMapper.class);
    ConsumableItemMapper consumableItemMapper = mock(ConsumableItemMapper.class);
    DeviceAssetMapper deviceAssetMapper = mock(DeviceAssetMapper.class);
    ReminderTaskMapper reminderTaskMapper = mock(ReminderTaskMapper.class);
    FamilyService familyService = mock(FamilyService.class);
    ReminderServiceImpl reminderService = new ReminderServiceImpl(
        reminderTaskMapper,
        warrantyRecordMapper,
        consumableItemMapper,
        deviceAssetMapper,
        familyService,
        redisService,
        creationService
    );
    String dedupeKey = RedisKeys.reminderDedupe(
        ReminderType.WARRANTY_EXPIRE_SOON.getCode(),
        warranty.getId(),
        today
    );

    when(warrantyRecordMapper.selectList(any())).thenReturn(List.of(warranty));
    when(deviceAssetMapper.selectById(warranty.getDeviceId())).thenReturn(device);
    when(redisService.setIfAbsent(eq(dedupeKey), eq("1"), any(Duration.class))).thenReturn(true);
    when(creationService.createReminderIfAbsent(
        anyLong(),
        any(ReminderType.class),
        anyString(),
        anyLong(),
        anyString(),
        anyString(),
        any(LocalDateTime.class)
    )).thenThrow(new IllegalStateException("database failed"));

    assertThatThrownBy(() -> reminderService.scanFamily(1L, today))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("database failed");

    InOrder inOrder = inOrder(redisService, creationService);
    inOrder.verify(redisService).setIfAbsent(eq(dedupeKey), eq("1"), any(Duration.class));
    inOrder.verify(creationService).createReminderIfAbsent(
        eq(1L),
        eq(ReminderType.WARRANTY_EXPIRE_SOON),
        eq(FileBizType.WARRANTY.getCode()),
        eq(warranty.getId()),
        eq(device.getName() + ReminderType.WARRANTY_EXPIRE_SOON.getDescription()),
        eq(device.getName() + "的保修结束日期为 " + warranty.getEndDate()),
        eq(today.atTime(8, 0))
    );
    verify(redisService).delete(dedupeKey);
    verifyNoInteractions(consumableItemMapper);
  }

  @Test
  @DisplayName("Redis 已命中去重键时跳过数据库写入事务")
  void skipCreationTransactionWhenDedupeKeyExists() {
    LocalDate today = LocalDate.of(2026, 5, 15);
    WarrantyRecordEntity warranty = warranty(today);
    RedisService redisService = mock(RedisService.class);
    ReminderCreationService creationService = mock(ReminderCreationService.class);
    WarrantyRecordMapper warrantyRecordMapper = mock(WarrantyRecordMapper.class);
    ConsumableItemMapper consumableItemMapper = mock(ConsumableItemMapper.class);
    DeviceAssetMapper deviceAssetMapper = mock(DeviceAssetMapper.class);
    ReminderTaskMapper reminderTaskMapper = mock(ReminderTaskMapper.class);
    FamilyService familyService = mock(FamilyService.class);
    ReminderServiceImpl reminderService = new ReminderServiceImpl(
        reminderTaskMapper,
        warrantyRecordMapper,
        consumableItemMapper,
        deviceAssetMapper,
        familyService,
        redisService,
        creationService
    );
    String dedupeKey = RedisKeys.reminderDedupe(
        ReminderType.WARRANTY_EXPIRE_SOON.getCode(),
        warranty.getId(),
        today
    );

    when(warrantyRecordMapper.selectList(any())).thenReturn(List.of(warranty));
    when(deviceAssetMapper.selectById(warranty.getDeviceId()))
        .thenReturn(device(warranty.getDeviceId()));
    when(consumableItemMapper.selectList(any())).thenReturn(List.of());
    when(redisService.setIfAbsent(eq(dedupeKey), eq("1"), any(Duration.class))).thenReturn(false);

    ReminderScanResponse response = reminderService.scanFamily(1L, today);

    assertThat(response.warrantyCreated()).isZero();
    assertThat(response.skippedDuplicate()).isEqualTo(1);
    verifyNoInteractions(creationService);
  }

  private WarrantyRecordEntity warranty(LocalDate today) {
    WarrantyRecordEntity warranty = new WarrantyRecordEntity();
    warranty.setId(20L);
    warranty.setFamilyId(1L);
    warranty.setDeviceId(10L);
    warranty.setWarrantyType(WarrantyType.OFFICIAL.getCode());
    warranty.setStartDate(today.minusYears(1));
    warranty.setEndDate(today.plusDays(5));
    warranty.setRemindDaysBefore(30);
    return warranty;
  }

  private DeviceAssetEntity device(Long deviceId) {
    DeviceAssetEntity device = new DeviceAssetEntity();
    device.setId(deviceId);
    device.setName("厨房净水器");
    return device;
  }
}