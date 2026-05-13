package com.fixledger.modules.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fixledger.modules.asset.entity.DeviceCategoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 文件功能说明：设备档案数据访问 Mapper，基于 MyBatis Plus 提供数据库访问入口。
 * </p>
 *
 * @Author FixLedger
 */
@Mapper
public interface DeviceCategoryMapper extends BaseMapper<DeviceCategoryEntity> {
}
