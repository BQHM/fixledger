package com.fixledger.modules.system.controller;

import com.fixledger.common.page.PageResponse;
import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.system.query.OperationLogPageQuery;
import com.fixledger.modules.system.response.OperationLogResponse;
import com.fixledger.modules.system.service.OperationLogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

  private final OperationLogService operationLogService;

  public SystemController(OperationLogService operationLogService) {
    this.operationLogService = operationLogService;
  }

  @GetMapping("/operation-logs")
  public Result<PageResponse<OperationLogResponse>> pageOperationLogs(
      @Valid OperationLogPageQuery query
  ) {
    return Result.success(operationLogService.pageLogs(CurrentUserContext.getUserId(), query));
  }
}
