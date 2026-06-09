package com.fixledger.modules.file;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.infrastructure.file.FileStorageService;
import com.fixledger.infrastructure.file.StoredFile;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.consumable.mapper.ConsumableItemMapper;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.file.entity.FileResourceEntity;
import com.fixledger.modules.file.entity.ManualTextIndexEntity;
import com.fixledger.modules.file.enums.FileBizType;
import com.fixledger.modules.file.mapper.FileResourceMapper;
import com.fixledger.modules.file.mapper.ManualTextIndexMapper;
import com.fixledger.modules.file.service.FileResourceServiceImpl;
import com.fixledger.modules.maintenance.mapper.MaintenanceRecordMapper;
import com.fixledger.modules.warranty.mapper.WarrantyRecordMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class FileResourceServiceCompensationTest {

  @Test
  @DisplayName("附件元数据写库失败时删除已写入的文件内容")
  void deleteStoredFileWhenMetadataInsertFails() {
    FileResourceMapper fileResourceMapper = mock(FileResourceMapper.class);
    ManualTextIndexMapper manualTextIndexMapper = mock(ManualTextIndexMapper.class);
    FileStorageService fileStorageService = mock(FileStorageService.class);
    FamilyService familyService = mock(FamilyService.class);
    DeviceAssetMapper deviceAssetMapper = mock(DeviceAssetMapper.class);
    WarrantyRecordMapper warrantyRecordMapper = mock(WarrantyRecordMapper.class);
    MaintenanceRecordMapper maintenanceRecordMapper = mock(MaintenanceRecordMapper.class);
    ConsumableItemMapper consumableItemMapper = mock(ConsumableItemMapper.class);
    FileResourceServiceImpl service = new FileResourceServiceImpl(
        fileResourceMapper,
        manualTextIndexMapper,
        fileStorageService,
        familyService,
        deviceAssetMapper,
        warrantyRecordMapper,
        maintenanceRecordMapper,
        consumableItemMapper
    );
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "invoice.jpg",
        "image/jpeg",
        new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
    );
    StoredFile storedFile = new StoredFile(
        "stored.jpg",
        "1/device/2026/05/stored.jpg",
        "jpg",
        file.getSize(),
        file.getContentType()
    );

    when(deviceAssetMapper.selectCount(any())).thenReturn(1L);
    when(fileStorageService.store(1L, FileBizType.DEVICE.getCode(), file)).thenReturn(storedFile);
    doThrow(new BusinessException(ErrorCode.SYSTEM_ERROR, "写库失败"))
        .when(fileResourceMapper)
        .insert(any(FileResourceEntity.class));

    assertThatThrownBy(() -> service.uploadFile(
        10L,
        1L,
        FileBizType.DEVICE.getCode(),
        20L,
        file
    )).isInstanceOf(BusinessException.class);

    verify(fileStorageService).delete(storedFile.storagePath());
  }

  @Test
  @DisplayName("说明书索引写入失败不影响附件上传")
  void keepUploadedManualFileWhenIndexInsertFails() {
    FileResourceMapper fileResourceMapper = mock(FileResourceMapper.class);
    ManualTextIndexMapper manualTextIndexMapper = mock(ManualTextIndexMapper.class);
    FileStorageService fileStorageService = mock(FileStorageService.class);
    FamilyService familyService = mock(FamilyService.class);
    DeviceAssetMapper deviceAssetMapper = mock(DeviceAssetMapper.class);
    WarrantyRecordMapper warrantyRecordMapper = mock(WarrantyRecordMapper.class);
    MaintenanceRecordMapper maintenanceRecordMapper = mock(MaintenanceRecordMapper.class);
    ConsumableItemMapper consumableItemMapper = mock(ConsumableItemMapper.class);
    FileResourceServiceImpl service = new FileResourceServiceImpl(
        fileResourceMapper,
        manualTextIndexMapper,
        fileStorageService,
        familyService,
        deviceAssetMapper,
        warrantyRecordMapper,
        maintenanceRecordMapper,
        consumableItemMapper
    );
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "manual.pdf",
        "application/pdf",
        "%PDF-1.7\nreset router".getBytes()
    );
    StoredFile storedFile = new StoredFile(
        "stored.pdf",
        "1/manual/2026/05/stored.pdf",
        "pdf",
        file.getSize(),
        file.getContentType()
    );

    when(deviceAssetMapper.selectCount(any())).thenReturn(1L);
    when(fileStorageService.store(1L, FileBizType.MANUAL.getCode(), file)).thenReturn(storedFile);
    doThrow(new BusinessException(ErrorCode.SYSTEM_ERROR, "索引失败"))
        .when(manualTextIndexMapper)
        .insert(any(ManualTextIndexEntity.class));

    service.uploadFile(10L, 1L, FileBizType.MANUAL.getCode(), 20L, file);

    verify(fileStorageService, never()).delete(storedFile.storagePath());
  }
}
