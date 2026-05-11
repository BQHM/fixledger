package com.fixledger.modules.reminder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fixledger.modules.reminder.entity.ReminderTaskEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReminderTaskMapper extends BaseMapper<ReminderTaskEntity> {
}
