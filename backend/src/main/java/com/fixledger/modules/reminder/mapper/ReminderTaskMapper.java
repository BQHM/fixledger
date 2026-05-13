package com.fixledger.modules.reminder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fixledger.modules.reminder.entity.ReminderTaskEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 文件功能说明：提醒通知数据访问 Mapper，基于 MyBatis Plus 提供数据库访问入口。
 * </p>
 *
 * @Author FixLedger
 */
@Mapper
public interface ReminderTaskMapper extends BaseMapper<ReminderTaskEntity> {
}
