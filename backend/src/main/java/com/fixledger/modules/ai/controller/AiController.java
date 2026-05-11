package com.fixledger.modules.ai.controller;

import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.ai.request.InvoiceParseRequest;
import com.fixledger.modules.ai.request.MaintenanceSummaryRequest;
import com.fixledger.modules.ai.request.TroubleshootingRequest;
import com.fixledger.modules.ai.response.InvoiceParseResponse;
import com.fixledger.modules.ai.response.MaintenanceSummaryResponse;
import com.fixledger.modules.ai.response.TroubleshootingResponse;
import com.fixledger.modules.ai.service.AiService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/families/{familyId}/ai")
public class AiController {

  private final AiService aiService;

  public AiController(AiService aiService) {
    this.aiService = aiService;
  }

  @PostMapping("/invoice-parse")
  public Result<InvoiceParseResponse> parseInvoice(
      @PathVariable Long familyId,
      @Valid @RequestBody InvoiceParseRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(aiService.parseInvoice(userId, familyId, request));
  }

  @PostMapping("/troubleshooting")
  public Result<TroubleshootingResponse> suggestTroubleshooting(
      @PathVariable Long familyId,
      @Valid @RequestBody TroubleshootingRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(aiService.suggestTroubleshooting(userId, familyId, request));
  }

  @PostMapping("/maintenance-summary")
  public Result<MaintenanceSummaryResponse> summarizeMaintenance(
      @PathVariable Long familyId,
      @Valid @RequestBody MaintenanceSummaryRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(aiService.summarizeMaintenance(userId, familyId, request));
  }
}
