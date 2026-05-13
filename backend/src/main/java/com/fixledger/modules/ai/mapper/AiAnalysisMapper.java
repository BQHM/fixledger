package com.fixledger.modules.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fixledger.modules.ai.entity.AiAnalysisEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 文件功能说明：AI 辅助数据访问 Mapper，基于 MyBatis Plus 提供数据库访问入口。
 * </p>
 *
 * @Author FixLedger
 */
@Mapper
public interface AiAnalysisMapper extends BaseMapper<AiAnalysisEntity> {
}
