package com.fixledger.modules.system.service;

import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.system.query.OperationLogPageQuery;
import com.fixledger.modules.system.response.OperationLogResponse;

public interface OperationLogService {

  void recordSuccess(
      Long userId,
      Long familyId,
      String module,
      String action,
      String bizType,
      Long bizId,
      String requestMethod,
      String requestUri
  );

  PageResponse<OperationLogResponse> pageLogs(Long userId, OperationLogPageQuery query);
}
