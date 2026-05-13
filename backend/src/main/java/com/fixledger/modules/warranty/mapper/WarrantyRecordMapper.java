package com.fixledger.modules.warranty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fixledger.modules.warranty.entity.WarrantyRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 文件功能说明：保修数据访问 Mapper，基于 MyBatis Plus 提供数据库访问入口。
 * </p>
 *
 * @Author FixLedger
 */
@Mapper
public interface WarrantyRecordMapper extends BaseMapper<WarrantyRecordEntity> {
}
