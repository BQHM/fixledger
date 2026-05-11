package com.fixledger.modules.consumable.controller;

import com.fixledger.common.page.PageResponse;
import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.consumable.query.ConsumableDueSoonQuery;
import com.fixledger.modules.consumable.request.CreateConsumableRequest;
import com.fixledger.modules.consumable.request.CreateReplaceRecordRequest;
import com.fixledger.modules.consumable.request.UpdateConsumableRequest;
import com.fixledger.modules.consumable.response.ConsumableReplaceRecordResponse;
import com.fixledger.modules.consumable.response.ConsumableResponse;
import com.fixledger.modules.consumable.service.ConsumableService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/families/{familyId}")
public class ConsumableController {

  private final ConsumableService consumableService;

  public ConsumableController(ConsumableService consumableService) {
    this.consumableService = consumableService;
  }

  @GetMapping("/devices/{deviceId}/consumables")
  public Result<List<ConsumableResponse>> listDeviceConsumables(
      @PathVariable Long familyId,
      @PathVariable Long deviceId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(consumableService.listDeviceConsumables(userId, familyId, deviceId));
  }

  @PostMapping("/devices/{deviceId}/consumables")
  public Result<ConsumableResponse> createConsumable(
      @PathVariable Long familyId,
      @PathVariable Long deviceId,
      @Valid @RequestBody CreateConsumableRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        consumableService.createConsumable(userId, familyId, deviceId, request)
    );
  }

  @PutMapping("/consumables/{consumableId}")
  public Result<ConsumableResponse> updateConsumable(
      @PathVariable Long familyId,
      @PathVariable Long consumableId,
      @Valid @RequestBody UpdateConsumableRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        consumableService.updateConsumable(userId, familyId, consumableId, request)
    );
  }

  @DeleteMapping("/consumables/{consumableId}")
  public Result<Boolean> deleteConsumable(
      @PathVariable Long familyId,
      @PathVariable Long consumableId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(consumableService.deleteConsumable(userId, familyId, consumableId));
  }

  @PostMapping("/consumables/{consumableId}/replace-records")
  public Result<ConsumableReplaceRecordResponse> createReplaceRecord(
      @PathVariable Long familyId,
      @PathVariable Long consumableId,
      @Valid @RequestBody CreateReplaceRecordRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        consumableService.createReplaceRecord(userId, familyId, consumableId, request)
    );
  }

  @GetMapping("/consumables/{consumableId}/replace-records")
  public Result<List<ConsumableReplaceRecordResponse>> listReplaceRecords(
      @PathVariable Long familyId,
      @PathVariable Long consumableId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(consumableService.listReplaceRecords(userId, familyId, consumableId));
  }

  @GetMapping("/consumables/due-soon")
  public Result<PageResponse<ConsumableResponse>> pageDueSoon(
      @PathVariable Long familyId,
      @Valid ConsumableDueSoonQuery query
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(consumableService.pageDueSoon(userId, familyId, query));
  }
}
