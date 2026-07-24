package com.fixledger.common.cache;

/**
 * 首页摘要缓存失效边界，业务模块不依赖看板模块的具体缓存实现。
 */
public interface DashboardCacheInvalidator {

  void evictAfterCommit(Long familyId);
}
