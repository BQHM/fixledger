package com.fixledger.modules.consumable.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fixledger.modules.consumable.entity.ConsumableItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 文件功能说明：耗材数据访问 Mapper，基于 MyBatis Plus 提供数据库访问入口。
 * </p>
 *
 * @Author FixLedger
 */
@Mapper
public interface ConsumableItemMapper extends BaseMapper<ConsumableItemEntity> {
}
