package com.fixledger.modules.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fixledger.modules.asset.entity.DeviceCategoryEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 文件功能说明：设备档案数据访问 Mapper，基于 MyBatis Plus 提供数据库访问入口。
 * </p>
 *
 * @Author FixLedger
 */
@Mapper
public interface DeviceCategoryMapper extends BaseMapper<DeviceCategoryEntity> {

  /**
   * 批量插入设备分类，用于家庭空间创建后的默认分类初始化。
   *
   * @param categories 待插入分类
   * @return 插入行数
   */
  int insertBatch(@Param("categories") List<DeviceCategoryEntity> categories);
}
