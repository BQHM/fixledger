package com.fixledger.modules.reminder.controller;

import com.fixledger.common.page.PageResponse;
import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.reminder.query.ReminderPageQuery;
import com.fixledger.modules.reminder.response.ReminderResponse;
import com.fixledger.modules.reminder.response.ReminderScanResponse;
import com.fixledger.modules.reminder.response.UnreadCountResponse;
import com.fixledger.modules.reminder.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/families/{familyId}/reminders")
public class ReminderController {

  private final ReminderService reminderService;

  public ReminderController(ReminderService reminderService) {
    this.reminderService = reminderService;
  }

  @GetMapping
  public Result<PageResponse<ReminderResponse>> pageReminders(
      @PathVariable Long familyId,
      @Valid ReminderPageQuery query
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(reminderService.pageReminders(userId, familyId, query));
  }

  @GetMapping("/unread-count")
  public Result<UnreadCountResponse> unreadCount(@PathVariable Long familyId) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(reminderService.unreadCount(userId, familyId));
  }

  @PatchMapping("/{reminderId}/read")
  public Result<ReminderResponse> markRead(
      @PathVariable Long familyId,
      @PathVariable Long reminderId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(reminderService.markRead(userId, familyId, reminderId));
  }

  @PatchMapping("/{reminderId}/ignore")
  public Result<ReminderResponse> ignore(
      @PathVariable Long familyId,
      @PathVariable Long reminderId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(reminderService.ignore(userId, familyId, reminderId));
  }

  @PostMapping("/scan")
  public Result<ReminderScanResponse> scan(@PathVariable Long familyId) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(reminderService.scanFamily(userId, familyId));
  }
}
