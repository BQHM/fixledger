package com.fixledger.modules.asset.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 文件功能说明：设备档案响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record DeviceDetailResponse(
    Long id,
    Long categoryId,
    String categoryName,
    String name,
    String brand,
    String model,
    String serialNumber,
    String status,
    LocalDate purchaseDate,
    String purchaseChannel,
    BigDecimal purchasePrice,
    String location,
    String remark,
    List<Object> warranties,
    List<Object> consumables,
    List<Object> maintenanceRecords,
    List<Object> files
) {
}
